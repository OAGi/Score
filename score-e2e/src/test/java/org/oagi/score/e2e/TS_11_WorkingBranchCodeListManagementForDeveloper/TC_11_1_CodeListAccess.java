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
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Execution(ExecutionMode.CONCURRENT)
public class TC_11_1_CodeListAccess extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_11_1_TA_1")
    public void test_TA_1(){
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        AppUserObject developerB;
        ReleaseObject workingBranch;
        {
            developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            AppUserObject developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developerA);
            /**
             * Create Code List for Working branch. States - WIP, Draft and Candidate
             */
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            CodeListObject codeListWIP = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListWIP, developerA);
            codeListForTesting.add(codeListWIP);

            CodeListObject codeListDraft = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Draft");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListDraft, developerA);
            codeListForTesting.add(codeListDraft);

            CodeListObject codeListCandidate = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Candidate");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListCandidate, developerA);
            codeListForTesting.add(codeListCandidate);
        }
        HomePage homePage = loginPage().signIn(developerB.getLoginId(), developerB.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        getDriver().manage().window().maximize();
        for (CodeListObject cl : codeListForTesting){
            assertNotEquals(developerB.getAppUserId(), cl.getOwnerUserId());
            viewEditCodeListPage.searchCodeListByNameAndBranch(cl.getName(), workingBranch.getReleaseNumber());
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
