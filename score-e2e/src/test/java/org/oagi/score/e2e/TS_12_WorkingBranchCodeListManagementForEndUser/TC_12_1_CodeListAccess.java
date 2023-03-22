package org.oagi.score.e2e.TS_12_WorkingBranchCodeListManagementForEndUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.code_list.AddCodeListCommentDialog;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.*;


@Execution(ExecutionMode.CONCURRENT)
public class TC_12_1_CodeListAccess extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }
    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_12_1_TA_1")
    public void test_TA_1() {
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        AppUserObject endUser;
        ReleaseObject workingBranch;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            AppUserObject developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            /**
             * Create Code List for Working branch. States - WIP, Draft and Candidate
             */
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
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
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        for (CodeListObject cl : codeListForTesting) {
            assertNotEquals(endUser.getAppUserId(), cl.getOwnerUserId());
            viewEditCodeListPage.searchCodeListByNameAndBranch(cl.getName(), workingBranch.getReleaseNumber());
        }
    }
    @Test
    @DisplayName("TC_12_1_TA_2")
    public void test_TA_2() {
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        AppUserObject endUser;
        ReleaseObject workingBranch;
        Map<CodeListObject, CodeListValueObject> codeListValuesMap = new HashMap<>();
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            AppUserObject developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            /**
             * Create developer Code List for Working branch. States - WIP, Draft and Candidate
             */
            CodeListObject codeListWIP = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developerA, namespace, workingBranch, "WIP");
            CodeListValueObject value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListWIP, developerA);
            codeListValuesMap.put(codeListWIP, value);
            codeListForTesting.add(codeListWIP);

            CodeListObject codeListDraft = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developerA, namespace, workingBranch, "Draft");
            value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListDraft, developerA);
            codeListValuesMap.put(codeListDraft, value);
            codeListForTesting.add(codeListDraft);

            CodeListObject codeListCandidate = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developerA, namespace, workingBranch, "Candidate");
            value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListCandidate, developerA);
            codeListValuesMap.put(codeListCandidate, value);
            codeListForTesting.add(codeListCandidate);
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        for (CodeListObject cl : codeListForTesting) {
            assertNotEquals(endUser.getAppUserId(), cl.getOwnerUserId());
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(cl.getName(), workingBranch.getReleaseNumber());
            assertDisabled(editCodeListPage.getCodeListNameField());
            assertDisabled(editCodeListPage.getDefinitionField());
            assertDisabled(editCodeListPage.getDefinitionSourceField());
            assertDisabled(editCodeListPage.getVersionField());
            assertThrows(TimeoutException.class, () -> {editCodeListPage.getAddCodeListValueButton();});
            CodeListValueObject value = codeListValuesMap.get(cl);
            assertDoesNotThrow(() -> editCodeListPage.getTableRecordByValue(value.getValue()));
            AddCodeListCommentDialog addCommentDialog = editCodeListPage.hitAddCommentButton();
            addCommentDialog.setComment("test comment");
            escape(getDriver());
        }
    }
    @Test
    @DisplayName("TC_12_1_TA_3")
    public void test_TA_3() {
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        AppUserObject endUserA;
        ReleaseObject workingBranch;
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB);

            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");

            ReleaseObject codeListBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            /**
             * Create end-user Code List for 10.8.5 branch. States - WIP, QA and Production
             */
            CodeListObject codeListWIP = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserB, namespace, codeListBranch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListWIP, endUserB);
            codeListForTesting.add(codeListWIP);

            CodeListObject codeListQA = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserB, namespace, codeListBranch, "QA");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListQA, endUserB);
            codeListForTesting.add(codeListQA);

            CodeListObject codeListProduction = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserB, namespace, codeListBranch, "Production");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListProduction, endUserB);
            codeListForTesting.add(codeListProduction);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        for (CodeListObject cl : codeListForTesting) {
            assertNotEquals(endUserA.getAppUserId(), cl.getOwnerUserId());
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            assertThrows(NoSuchElementException.class, () -> {viewEditCodeListPage.searchCodeListByNameAndBranch(cl.getName(), workingBranch.getReleaseNumber());});
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
