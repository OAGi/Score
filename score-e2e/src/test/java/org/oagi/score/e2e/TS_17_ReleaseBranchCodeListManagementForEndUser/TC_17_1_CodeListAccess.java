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
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.openqa.selenium.NoSuchElementException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Execution(ExecutionMode.CONCURRENT)
public class TC_17_1_CodeListAccess extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_17_1_TA_1")
    public void test_TA_1() {
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        AppUserObject endUserA;
        ReleaseObject branch;
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            AppUserObject developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB);
            /**
             * Create developer Code List for a particular release branch. States - WIP, Draft, Candidate, Release Draft and Published
             */
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            ReleaseObject workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            CodeListObject codeListWIP = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developerA, namespace, workingBranch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListWIP, developerA);
            codeListForTesting.add(codeListWIP);

            CodeListObject codeListDraft = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developerA, namespace, workingBranch, "Draft");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListDraft, developerA);
            codeListForTesting.add(codeListDraft);

            CodeListObject codeListCandidate = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developerA, namespace, workingBranch, "Candidate");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListCandidate, developerA);
            codeListForTesting.add(codeListCandidate);

            CodeListObject codeListReleaseDraft = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developerA, namespace, workingBranch, "ReleaseDraft");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListReleaseDraft, developerA);
            codeListForTesting.add(codeListReleaseDraft);

            CodeListObject codeListPublished = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developerA, namespace, branch, "Published");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListPublished, developerA);
            codeListForTesting.add(codeListPublished);

            /**
             * Create end-user Code List for a particular release branch. States - WIP, Draft and Production
             */
            codeListWIP = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserB, euNamespace, branch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListWIP, endUserB);
            codeListForTesting.add(codeListWIP);

            codeListDraft = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserB, euNamespace, branch, "Draft");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListDraft, endUserB);
            codeListForTesting.add(codeListDraft);

            CodeListObject codeListProduction = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserB, euNamespace, branch, "Production");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListProduction, endUserB);
            codeListForTesting.add(codeListProduction);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        for (CodeListObject cl : codeListForTesting) {
            assertNotEquals(endUserA.getAppUserId(), cl.getOwnerUserId());
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(cl.getOwnerUserId());
            if (owner.isDeveloper()){
                if (cl.getState().equals("Published")){
                    assertDoesNotThrow(() -> {viewEditCodeListPage.searchCodeListByNameAndBranch(cl.getName(), branch.getReleaseNumber());});
                }else{
                    assertThrows(NoSuchElementException.class, () ->{viewEditCodeListPage.searchCodeListByNameAndBranch(cl.getName(), branch.getReleaseNumber());});
                }
            }else{
                assertDoesNotThrow(() -> {viewEditCodeListPage.searchCodeListByNameAndBranch(cl.getName(), branch.getReleaseNumber());});
            }
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
