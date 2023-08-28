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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_11_8_RestoringDeveloperCodeList extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_11_8_TA_1")
    public void test_TA_1() {
        AppUserObject developerA;
        ReleaseObject workingBranch;
        List<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<BigInteger, CodeListValueObject> codeListCodeListValueMap = new HashMap<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "WIP");
            CodeListValueObject codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerA);
            codeListCodeListValueMap.put(codeList.getCodeListManifestId(), codeListValue);
            codeListForTesting.add(codeList);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (CodeListObject codeList : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
            assertEquals("Working", getText(editCodeListPage.getReleaseField()));
            assertEquals(developerA.getLoginId(), getText(editCodeListPage.getOwnerField()));
            assertEquals("WIP", getText(editCodeListPage.getStateField()));
            editCodeListPage.hitDeleteButton();
            codeList.setState("Deleted");

            viewEditCodeListPage.openPage();
            EditCodeListPage editCodeListPageNew = viewEditCodeListPage.openCodeListViewEditPage(codeList);
            assertEquals("Deleted", getText(editCodeListPage.getStateField()));
            editCodeListPage.hitRestoreButton();
            CodeListValueObject value = codeListCodeListValueMap.get(codeList.getCodeListManifestId());
            assertDoesNotThrow(() -> editCodeListPageNew.valueExists(value.getValue()));
        }
    }

    @Test
    @DisplayName("TC_11_8_TA_2")
    public void test_TA_2() {
        AppUserObject developerA;
        AppUserObject developerB;
        ReleaseObject workingBranch;
        List<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<BigInteger, CodeListValueObject> codeListCodeListValueMap = new HashMap<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerB, namespace, workingBranch, "WIP");
            CodeListValueObject codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerB);
            codeListCodeListValueMap.put(codeList.getCodeListManifestId(), codeListValue);
            codeListForTesting.add(codeList);
        }

        for (CodeListObject codeList : codeListForTesting) {
            HomePage homePage = loginPage().signIn(developerB.getLoginId(), developerB.getPassword());
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
            assertEquals("Working", getText(editCodeListPage.getReleaseField()));
            assertEquals(developerB.getLoginId(), getText(editCodeListPage.getOwnerField()));
            assertEquals("WIP", getText(editCodeListPage.getStateField()));
            editCodeListPage.hitDeleteButton();
            codeList.setState("Deleted");

            homePage.logout();
            homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
            viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPageNew = viewEditCodeListPage.openCodeListViewEditPage(codeList);
            assertEquals("Deleted", getText(editCodeListPage.getStateField()));
            assertNotEquals(developerA.getLoginId(), getText(editCodeListPage.getOwnerField()));
            editCodeListPage.hitRestoreButton();
            assertEquals(developerA.getLoginId(), getText(editCodeListPage.getOwnerField()));
            CodeListValueObject value = codeListCodeListValueMap.get(codeList.getCodeListManifestId());
            assertDoesNotThrow(() -> editCodeListPageNew.valueExists(value.getValue()));
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
