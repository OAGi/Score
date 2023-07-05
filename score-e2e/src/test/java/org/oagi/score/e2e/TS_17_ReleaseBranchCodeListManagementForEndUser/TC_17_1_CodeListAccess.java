package org.oagi.score.e2e.TS_17_ReleaseBranchCodeListManagementForEndUser;

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
import org.oagi.score.e2e.page.code_list.EditCodeListValueDialog;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.escape;

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
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
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
    @DisplayName("TC_17_1_TA_2")
    public void test_TA_2() {
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<CodeListObject, CodeListValueObject> codeListValuesMap = new HashMap<>();
        AppUserObject endUserA;
        ReleaseObject branch;
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA);
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            /**
             * Create end-user Code List for a particular release branch in WIP state.
             */
            CodeListObject codeListWIP = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserA, euNamespace, branch, "WIP");
            CodeListValueObject value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListWIP, endUserA);
            codeListForTesting.add(codeListWIP);
            codeListValuesMap.put(codeListWIP, value);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        for (CodeListObject cl : codeListForTesting) {
            assertEquals(endUserA.getAppUserId(), cl.getOwnerUserId());
            assertFalse(endUserA.isDeveloper());
            assertTrue(cl.getState().equals("WIP"));
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(cl.getName(), branch.getReleaseNumber());
            editCodeListPage.setDefinition("new definition");
            editCodeListPage.setDefinitionSource("new definition source");
            editCodeListPage.setName("new name");
            editCodeListPage.setVersion("new version");

            CodeListValueObject value = codeListValuesMap.get(cl);
            EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.editCodeListValue(value.getValue());
            editCodeListValueDialog.setMeaning("new meaning");
            editCodeListValueDialog.hitSaveButton();

            editCodeListValueDialog = editCodeListPage.addCodeListValue();
            editCodeListValueDialog.setCode("newly added value code");
            editCodeListValueDialog.setMeaning("newly added value meaning");
            editCodeListValueDialog.hitAddButton();
            editCodeListPage.hitUpdateButton();
        }
    }

    @Test
    @DisplayName("TC_17_1_TA_3")
    public void test_TA_3() {
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<CodeListObject, CodeListValueObject> codeListValuesMap = new HashMap<>();
        AppUserObject endUserB;
        ReleaseObject branch;
        {
            AppUserObject endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA);
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            /**
             * Create end-user Code List for a particular release branch in WIP state.
             */
            CodeListObject codeListWIP = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserA, euNamespace, branch, "WIP");
            CodeListValueObject value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListWIP, endUserA);
            codeListForTesting.add(codeListWIP);
            codeListValuesMap.put(codeListWIP, value);
        }

        HomePage homePage = loginPage().signIn(endUserB.getLoginId(), endUserB.getPassword());
        for (CodeListObject cl : codeListForTesting) {
            assertNotEquals(endUserB.getAppUserId(), cl.getOwnerUserId());
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(cl.getOwnerUserId());
            assertFalse(owner.isDeveloper());
            assertTrue(cl.getState().equals("WIP"));
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(cl.getName(), branch.getReleaseNumber());
            assertDisabled(editCodeListPage.getCodeListNameField());
            assertDisabled(editCodeListPage.getDefinitionField());
            assertDisabled(editCodeListPage.getDefinitionSourceField());
            assertDisabled(editCodeListPage.getVersionField());
            assertThrows(TimeoutException.class, () -> {
                editCodeListPage.getAddCodeListValueButton();
            });
            CodeListValueObject value = codeListValuesMap.get(cl);
            assertDoesNotThrow(() -> editCodeListPage.getTableRecordByValue(value.getValue()));
            AddCommentDialog addCommentDialog = editCodeListPage.hitAddCommentButton();
            addCommentDialog.setComment("test comment");
            escape(getDriver());
        }
    }

    @Test
    @DisplayName("TC_17_1_TA_4")
    public void test_TA_4() {
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<CodeListObject, CodeListValueObject> codeListValuesMap = new HashMap<>();
        AppUserObject endUserB;
        ReleaseObject branch;
        {
            AppUserObject endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA);
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            /**
             * Create end-user Code List for a particular release branch in QA and Production state.
             */
            CodeListObject codeListQA = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserA, euNamespace, branch, "QA");
            CodeListValueObject value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListQA, endUserA);
            codeListForTesting.add(codeListQA);
            codeListValuesMap.put(codeListQA, value);

            CodeListObject codeListProduction = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserA, euNamespace, branch, "Production");
            value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListProduction, endUserA);
            codeListForTesting.add(codeListProduction);
            codeListValuesMap.put(codeListProduction, value);
        }

        HomePage homePage = loginPage().signIn(endUserB.getLoginId(), endUserB.getPassword());
        for (CodeListObject cl : codeListForTesting) {
            assertNotEquals(endUserB.getAppUserId(), cl.getOwnerUserId());
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(cl.getOwnerUserId());
            assertFalse(owner.isDeveloper());
            assertTrue(List.of("QA", "Production").contains(cl.getState()));
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(cl.getName(), branch.getReleaseNumber());
            assertDisabled(editCodeListPage.getCodeListNameField());
            assertDisabled(editCodeListPage.getDefinitionField());
            assertDisabled(editCodeListPage.getDefinitionSourceField());
            assertDisabled(editCodeListPage.getVersionField());
            assertThrows(TimeoutException.class, () -> {
                editCodeListPage.getAddCodeListValueButton();
            });
            CodeListValueObject value = codeListValuesMap.get(cl);
            assertDoesNotThrow(() -> editCodeListPage.getTableRecordByValue(value.getValue()));
            AddCommentDialog addCommentDialog = editCodeListPage.hitAddCommentButton();
            addCommentDialog.setComment("test comment");
            escape(getDriver());
        }
    }

    @Test
    @DisplayName("TC_17_1_TA_5")
    public void test_TA_5() {
        Map<CodeListObject, CodeListValueObject> codeListValuesMap = new HashMap<>();
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        AppUserObject endUserA;
        ReleaseObject branch;
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            /**
             * Create Published developer Code List for a particular release branch.
             */
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");

            CodeListObject codeListPublished = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developerA, namespace, branch, "Published");
            CodeListValueObject value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListPublished, developerA);
            codeListForTesting.add(codeListPublished);
            codeListValuesMap.put(codeListPublished, value);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        for (CodeListObject cl : codeListForTesting) {
            assertNotEquals(endUserA.getAppUserId(), cl.getOwnerUserId());
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(cl.getOwnerUserId());
            assertTrue(owner.isDeveloper());
            assertTrue(cl.getState().equals("Published"));
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(cl.getName(), branch.getReleaseNumber());
            assertDisabled(editCodeListPage.getCodeListNameField());
            assertDisabled(editCodeListPage.getDefinitionField());
            assertDisabled(editCodeListPage.getDefinitionSourceField());
            assertDisabled(editCodeListPage.getVersionField());
            assertThrows(TimeoutException.class, () -> {
                editCodeListPage.getAddCodeListValueButton();
            });
            CodeListValueObject value = codeListValuesMap.get(cl);
            assertDoesNotThrow(() -> editCodeListPage.getTableRecordByValue(value.getValue()));
            AddCommentDialog addCommentDialog = editCodeListPage.hitAddCommentButton();
            addCommentDialog.setComment("test comment");
            escape(getDriver());
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
