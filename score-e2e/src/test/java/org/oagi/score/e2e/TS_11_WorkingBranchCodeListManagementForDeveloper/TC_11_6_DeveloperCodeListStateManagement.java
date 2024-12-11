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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_11_6_DeveloperCodeListStateManagement extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_11_6_from_TA_1_to_TA_4")
    public void test_from_TA_1_to_TA_4() {
        AppUserObject developerA;
        LibraryObject library;
        ReleaseObject workingBranch;
        List<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<BigInteger, CodeListValueObject> codeListCodeListValueMap = new HashMap<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "WIP");
            CodeListValueObject codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerA);
            codeListCodeListValueMap.put(codeList.getCodeListManifestId(), codeListValue);
            codeListForTesting.add(codeList);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        for (CodeListObject codeList : codeListForTesting) {
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
            assertEquals("Working", getText(editCodeListPage.getReleaseField()));
            assertEquals(developerA.getLoginId(), getText(editCodeListPage.getOwnerField()));
            assertEquals("WIP", getText(editCodeListPage.getStateField()));
            editCodeListPage.moveToDraft();
            assertEquals("Draft", getText(editCodeListPage.getStateField()));
            editCodeListPage.backToWIP();
            assertEquals("WIP", getText(editCodeListPage.getStateField()));
            editCodeListPage.moveToDraft();
            editCodeListPage.moveToCandidate();
            assertEquals("Candidate", getText(editCodeListPage.getStateField()));
            editCodeListPage.backToWIP();
            assertEquals("WIP", getText(editCodeListPage.getStateField()));

            viewEditCodeListPage.openPage();
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
