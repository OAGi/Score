package org.oagi.score.e2e.TS_17_ReleaseBranchCodeListManagementForEndUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.CodeListObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.oagi.score.e2e.page.core_component.DTViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
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

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

            /**
             * Create WIP end-user Code List for a particular release branch.
             */
            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUser, namespaceEU, branch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), branch.getReleaseNumber());
        assertTrue(getText(editCodeListPage.getRevisionField()).equals("1"));
        assertTrue(getText(editCodeListPage.getStateField()).equals("WIP"));
        assertTrue(getText(editCodeListPage.getOwnerField()).equals(endUser.getLoginId()));
        editCodeListPage.hitDeleteButton();
        editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), branch.getReleaseNumber());
        assertEquals("Deleted", getText(editCodeListPage.getStateField()));
    }

    @Test
    @DisplayName("TC_17_6_TA_2")
    public void test_TA_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        ReleaseObject branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
        NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
        CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, namespaceEU, branch, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.createDT("Process Category_ Code. Type", branch.getReleaseNumber());
        dtViewEditPage.showValueDomain();
        dtViewEditPage.addCodeListValueDomain(codeList.getName());
        String qualifier = "testDataType" + randomAlphabetic(5, 10);
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
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), branch.getReleaseNumber());
        editCodeListPage.hitDeleteButton();
        viewEditCoreComponentPage.openPage();
        DTViewEditPage dtViewEditPageNew = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(qualifier + "_ Code. Type", branch.getReleaseNumber());
        dtViewEditPageNew.showValueDomain();
        assertDoesNotThrow(() -> dtViewEditPageNew.codeListIdMarkedAsDeleted(codeList));
        escape(getDriver());
        dtViewEditPageNew.changeCodeListValueDomain(codeList.getName());

        viewEditCodeListPage.openPage();
        editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), branch.getReleaseNumber());
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
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

            CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, namespace, branch, "Production");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
            codeListForTesting.add(codeList);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        for (CodeListObject codeList : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), branch.getReleaseNumber());
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
