package org.oagi.score.e2e.TS_16_ReleaseBranchCodeListManagementForDeveloper;

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

@Execution(ExecutionMode.CONCURRENT)
public class TC_16_1_CodeListAccess extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_16_1_TA_1")
    public void test_TA_1() {
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        AppUserObject developerA;
        ReleaseObject branch;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
            /**
             * Create developer Code List for a particular release branch. States - Published
             */
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            ReleaseObject workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            CodeListObject codeListWIP = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developerB, namespace, workingBranch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListWIP, developerB);
            codeListForTesting.add(codeListWIP);

            CodeListObject codeListDraft = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developerB, namespace, workingBranch, "Draft");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListDraft, developerB);
            codeListForTesting.add(codeListDraft);

            CodeListObject codeListCandidate = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developerB, namespace, workingBranch, "Candidate");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListCandidate, developerB);
            codeListForTesting.add(codeListCandidate);

            CodeListObject codeListPublished = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developerB, namespace, branch, "Published");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListPublished, developerB);
            codeListForTesting.add(codeListPublished);

            /**
             * Create end-user Code List for a particular release branch. States - WIP, Draft and Production
             */
            codeListWIP = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUser, euNamespace, branch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListWIP, endUser);
            codeListForTesting.add(codeListWIP);

            codeListDraft = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUser, euNamespace, branch, "Draft");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListDraft, endUser);
            codeListForTesting.add(codeListDraft);

            CodeListObject codeListProduction = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUser, euNamespace, branch, "Production");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListProduction, endUser);
            codeListForTesting.add(codeListProduction);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        for (CodeListObject cl : codeListForTesting) {
            assertNotEquals(developerA.getAppUserId(), cl.getOwnerUserId());
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
