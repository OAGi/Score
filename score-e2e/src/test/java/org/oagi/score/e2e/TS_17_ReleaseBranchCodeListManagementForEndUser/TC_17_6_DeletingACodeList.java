package org.oagi.score.e2e.TS_17_ReleaseBranchCodeListManagementForEndUser;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.oagi.score.e2e.page.core_component.DTViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditDataTypePage;
import org.openqa.selenium.TimeoutException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.escape;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_17_6_DeletingACodeList extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_17_6_TA_1")
    public void test_TA_1() {
        AppUserObject endUser;
        ReleaseObject branch;
        CodeListObject codeList;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

            /**
             * Create WIP end-user Code List for a particular release branch.
             */
            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUser, namespaceEU, branch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        assertTrue(getText(editCodeListPage.getRevisionField()).equals("1"));
        assertTrue(getText(editCodeListPage.getStateField()).equals("WIP"));
        assertTrue(getText(editCodeListPage.getOwnerField()).equals(endUser.getLoginId()));
        editCodeListPage.hitDeleteButton();
        codeList.setState("Deleted");

        viewEditCodeListPage.openPage();
        editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        assertEquals("Deleted", getText(editCodeListPage.getStateField()));
    }

    @Test
    @DisplayName("TC_17_6_TA_2")
    public void test_TA_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
        NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, namespaceEU, branch, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        DTViewEditPage dtViewEditPage = viewEditDataTypePage.createDT("Process Category_ Code. Type", branch.getReleaseNumber());
        dtViewEditPage.showValueDomain();
        dtViewEditPage.addCodeListValueDomain(codeList.getName());
        String qualifier = "testDataType" + RandomStringUtils.secure().nextAlphabetic(5, 10);
        dtViewEditPage.setQualifier(qualifier);
        String definition = getText(dtViewEditPage.getDefinitionField());
        dtViewEditPage.setNamespace(namespaceEU);
        if (definition != null) {
            dtViewEditPage.hitUpdateButton();
        } else {
            try {
                dtViewEditPage.hitUpdateButton();
            } catch (TimeoutException ignore) {
            }
            dtViewEditPage.hitUpdateAnywayButton();
        }

        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        editCodeListPage.hitDeleteButton();
        codeList.setState("Deleted");

        viewEditDataTypePage.openPage();
        DTViewEditPage dtViewEditPageNew = viewEditDataTypePage.openDTViewEditPageByDenAndBranch(qualifier + "_ Code. Type", branch.getReleaseNumber());
        dtViewEditPageNew.showValueDomain();
        assertDoesNotThrow(() -> dtViewEditPageNew.codeListIdMarkedAsDeleted(codeList));
        escape(getDriver());
        dtViewEditPageNew.changeCodeListValueDomain(codeList.getName());

        viewEditCodeListPage.openPage();
        editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        editCodeListPage.hitRestoreButton();

        dtViewEditPageNew.openPage();
        dtViewEditPageNew.showValueDomain();
        assertThrows(TimeoutException.class, () -> dtViewEditPageNew.codeListIdMarkedAsDeleted(codeList));
    }

    @Test
    @DisplayName("TC_17_6_TA_3")
    public void test_TA_3() {
        AppUserObject endUser;
        ReleaseObject branch;
        List<CodeListObject> codeListForTesting = new ArrayList<>();
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

            CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, namespace, branch, "Production");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
            codeListForTesting.add(codeList);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        for (CodeListObject codeList : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
            editCodeListPage.hitAmendButton();
            assertEquals("WIP", getText(editCodeListPage.getStateField()));
            assertTrue(Integer.valueOf(getText(editCodeListPage.getRevisionField())) > 1);
            assertThrows(TimeoutException.class, () -> {
                editCodeListPage.hitDeleteButton();
            });
            editCodeListPage.moveToQA();
            assertEquals("QA", getText(editCodeListPage.getStateField()));
            assertThrows(TimeoutException.class, () -> {
                editCodeListPage.hitDeleteButton();
            });
            editCodeListPage.moveToProduction();
            assertEquals("Production", getText(editCodeListPage.getStateField()));
            assertThrows(TimeoutException.class, () -> {
                editCodeListPage.hitDeleteButton();
            });
        }
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        // Delete random accounts
        this.randomAccounts.forEach(newUser -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId());
        });
    }

}
