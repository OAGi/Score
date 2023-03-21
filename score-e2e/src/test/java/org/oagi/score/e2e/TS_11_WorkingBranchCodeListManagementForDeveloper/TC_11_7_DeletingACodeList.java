package org.oagi.score.e2e.TS_11_WorkingBranchCodeListManagementForDeveloper;

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
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_11_7_DeletingACodeList extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_11_7_TA_1")
    public void test_TA_1() {
        AppUserObject developerA;
        ReleaseObject workingBranch;
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<CodeListObject, CodeListValueObject> codeListCodeListValueMap = new HashMap<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "WIP");
            CodeListValueObject codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerA);
            codeListCodeListValueMap.put(codeList, codeListValue);
            codeListForTesting.add(codeList);
        }
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (CodeListObject codeList : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), workingBranch.getReleaseNumber());
            assertEquals("Working", getText(editCodeListPage.getReleaseField()));
            assertEquals(developerA.getLoginId(), getText(editCodeListPage.getOwnerField()));
            assertEquals("WIP", getText(editCodeListPage.getStateField()));
            editCodeListPage.hitDeleteButton();
            editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), workingBranch.getReleaseNumber());
            assertEquals("Deleted", getText(editCodeListPage.getStateField()));
        }
    }

    @Test
    @DisplayName("TC_11_7_TA_2")
    public void test_TA_2() {
        AppUserObject developerA;
        ReleaseObject workingBranch;
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<CodeListObject, CodeListValueObject> codeListCodeListValueMap = new HashMap<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "WIP");
            CodeListValueObject codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerA);
            codeListCodeListValueMap.put(codeList, codeListValue);
            codeListForTesting.add(codeList);
        }
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (CodeListObject codeList : codeListForTesting) {
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.createDT("Process Category_ Code. Type", workingBranch.getReleaseNumber());
            dtViewEditPage.showValueDomain();
            dtViewEditPage.addCodeListValueDomain(codeList.getName());
            String qualifier = "testDataType";
            dtViewEditPage.setQualifier(qualifier);
            String definition = getText(dtViewEditPage.getDefinitionField());
            dtViewEditPage.hitUpdateButton();
            if (definition == null){
                dtViewEditPage.hitUpdateAnywayButton();
            }
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), workingBranch.getReleaseNumber());
            editCodeListPage.hitDeleteButton();
            viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            DTViewEditPage dtViewEditPageNew = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(qualifier + "_ Code. Type", workingBranch.getReleaseNumber());
            dtViewEditPage.showValueDomain();
            assertDoesNotThrow(() -> dtViewEditPageNew.codeListIdMarkedAsDeleted(codeList.getName()));
            escape(getDriver());
            CodeListObject oagiCodeList = getAPIFactory().getCodeListAPI().getCodeListByCodeListNameAndReleaseNum("oacl_StateCode", workingBranch.getReleaseNumber());
            dtViewEditPage.changeCodeListValueDomain(oagiCodeList.getName());

            viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), workingBranch.getReleaseNumber());
            editCodeListPage.hitRestoreButton();
            viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(qualifier + "_ Code. Type", workingBranch.getReleaseNumber());
            dtViewEditPage.showValueDomain();
            assertThrows(NoSuchElementException.class, () -> {dtViewEditPageNew.codeListIdMarkedAsDeleted(codeList.getName());});
            escape(getDriver());
        }
    }
    @Test
    @DisplayName("TC_11_7_TA_4")
    public void test_TA_4() {
        AppUserObject developerA;
        ReleaseObject workingBranch;
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<CodeListObject, CodeListValueObject> codeListCodeListValueMap = new HashMap<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Published");
            CodeListValueObject codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerA);
            codeListCodeListValueMap.put(codeList, codeListValue);
            codeListForTesting.add(codeList);

            codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerB, namespace, workingBranch, "Published");
            codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerB);
            codeListCodeListValueMap.put(codeList, codeListValue);
            codeListForTesting.add(codeList);
        }
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (CodeListObject codeList : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), workingBranch.getReleaseNumber());
            editCodeListPage.hitRevise();
            viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPageNew = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), workingBranch.getReleaseNumber());
            assertEquals("WIP", getText(editCodeListPage.getStateField()));
            assertTrue(Integer.valueOf(getText(editCodeListPage.getRevisionField()))>1);
            assertThrows(TimeoutException.class, () -> {editCodeListPageNew.hitDeleteButton();});
            editCodeListPageNew.moveToDraft();
            assertEquals("Draft", getText(editCodeListPage.getStateField()));
            assertThrows(TimeoutException.class, () -> {editCodeListPageNew.hitDeleteButton();});
            editCodeListPageNew.moveToCandidate();
            assertEquals("Candidate", getText(editCodeListPage.getStateField()));
            assertThrows(TimeoutException.class, () -> {editCodeListPageNew.hitDeleteButton();});
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
