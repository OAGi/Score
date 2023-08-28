package org.oagi.score.e2e.TS_16_ReleaseBranchCodeListManagementForDeveloper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.code_list.AddCommentDialog;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.escape;

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
        List<CodeListObject> codeListForTesting = new ArrayList<>();
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
             * Create developer Code List for a particular release branch. States - WIP, Draft, Candidate and Published
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
            if (owner.isDeveloper()) {
                if (cl.getState().equals("Published")) {
                    assertDoesNotThrow(() -> {
                        viewEditCodeListPage.searchCodeListByNameAndBranch(cl.getName(), branch.getReleaseNumber());
                    });
                } else {
                    assertThrows(NoSuchElementException.class, () -> {
                        viewEditCodeListPage.searchCodeListByNameAndBranch(cl.getName(), branch.getReleaseNumber());
                    });
                }
            } else {
                assertDoesNotThrow(() -> {
                    viewEditCodeListPage.searchCodeListByNameAndBranch(cl.getName(), branch.getReleaseNumber());
                });
            }
        }
    }

    @Test
    @DisplayName("TC_16_1_TA_2")
    public void test_TA_2() {
        AppUserObject developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developerA);
        AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developerB);

        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        /**
         * Create Published developer Code List for a particular release branch
         */
        ReleaseObject branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
        CodeListObject codeListPublished = getAPIFactory().getCodeListAPI().
                createRandomCodeList(developerB, namespace, branch, "Published");
        getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListPublished, developerB);

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        assertNotEquals(developerA.getAppUserId(), codeListPublished.getOwnerUserId());
        AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(codeListPublished.getOwnerUserId());
        assertTrue(owner.isDeveloper());
        assertTrue(codeListPublished.getState().equals("Published"));
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        viewEditCodeListPage.openCodeListViewEditPage(codeListPublished);
    }

    @Test
    @DisplayName("TC_16_1_TA_3")
    public void test_TA_3() {
        List<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<BigInteger, CodeListValueObject> codeListValuesMap = new HashMap<>();
        AppUserObject developerA;
        ReleaseObject branch;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");

            /**
             * Create end-user Code List for a particular release branch. States - WIP, Draft and Production
             */
            CodeListObject codeListWIP = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUser, euNamespace, branch, "WIP");
            CodeListValueObject value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListWIP, endUser);
            codeListForTesting.add(codeListWIP);
            codeListValuesMap.put(codeListWIP.getCodeListManifestId(), value);

            CodeListObject codeListDraft = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUser, euNamespace, branch, "Draft");
            value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListDraft, endUser);
            codeListForTesting.add(codeListDraft);
            codeListValuesMap.put(codeListDraft.getCodeListManifestId(), value);

            CodeListObject codeListProduction = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUser, euNamespace, branch, "Production");
            value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListProduction, endUser);
            codeListForTesting.add(codeListProduction);
            codeListValuesMap.put(codeListProduction.getCodeListManifestId(), value);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        for (CodeListObject codeList : codeListForTesting) {
            assertNotEquals(developerA.getAppUserId(), codeList.getOwnerUserId());
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(codeList.getOwnerUserId());
            assertFalse(owner.isDeveloper());

            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList, true);
            assertDisabled(editCodeListPage.getCodeListNameField());
            assertDisabled(editCodeListPage.getDefinitionField());
            assertDisabled(editCodeListPage.getDefinitionSourceField());
            assertDisabled(editCodeListPage.getVersionField());
            assertThrows(TimeoutException.class, () -> editCodeListPage.getAddCodeListValueButton());
            CodeListValueObject value = codeListValuesMap.get(codeList.getCodeListManifestId());
            assertDoesNotThrow(() -> editCodeListPage.getTableRecordByValue(value.getValue()));
            AddCommentDialog addCommentDialog = editCodeListPage.hitAddCommentButton();
            addCommentDialog.setComment("test comment");
            escape(getDriver());

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
