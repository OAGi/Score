package org.oagi.score.e2e.TS_11_WorkingBranchCodeListManagementForDeveloper;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.code_list.AddCodeListCommentDialog;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.EditCodeListValueDialog;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.invisibilityOfLoadingContainerElement;

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
    @DisplayName("TC_11_1_TA_1_and_TA_14")
    public void test_TA_1_and_TA_14() {
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
        for (CodeListObject cl : codeListForTesting) {
            assertNotEquals(developerB.getAppUserId(), cl.getOwnerUserId());
            viewEditCodeListPage.searchCodeListByNameAndBranch(cl.getName(), workingBranch.getReleaseNumber());
        }

    }

    @Test
    @DisplayName("TC_11_1_TA_2")
    public void test_TA_2() {
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        AppUserObject developerA;
        ReleaseObject workingBranch;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
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
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        getDriver().manage().window().maximize();
        for (CodeListObject cl : codeListForTesting) {
            assertEquals(developerA.getAppUserId(), cl.getOwnerUserId());
            assertEquals(Boolean.valueOf("true"), developerA.isDeveloper());
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(cl.getName(), workingBranch.getReleaseNumber());
            if (cl.getState().equals("WIP")) {
                /**
                 * The developer can view and edit the details (including code values) of a CL that is in the WIP state and owned by him.
                 */
                editCodeListPage.setDefinition("test definition");
                editCodeListPage.setDefinitionSource("test definition source");
                EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
                editCodeListValueDialog.setCode("code");
                editCodeListValueDialog.setMeaning("meaning");
                editCodeListValueDialog.hitAddButton();
                editCodeListPage.hitUpdateButton();
            } else {
                assertDisabled(editCodeListPage.getDefinitionField());
                assertDisabled(editCodeListPage.getDefinitionSourceField());
            }
        }

    }

    @Test
    @DisplayName("TC_11_1_TA_3")
    public void test_TA_3() {
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
             * Create Code List for Working branch. States - WIP
             */
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            CodeListObject codeListWIP = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListWIP, developerA);
            codeListForTesting.add(codeListWIP);
        }
        HomePage homePage = loginPage().signIn(developerB.getLoginId(), developerB.getPassword());
        getDriver().manage().window().maximize();
        for (CodeListObject cl : codeListForTesting) {
            assertNotEquals(developerB.getAppUserId(), cl.getOwnerUserId());
            assertEquals("WIP", cl.getState());
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            /**
             * The developer CAN view but CANNOT edit the details of a CL that is in WIP state and owned by another developer.
             */
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(cl.getName(), workingBranch.getReleaseNumber());
            assertDisabled(editCodeListPage.getDefinitionField());
            assertDisabled(editCodeListPage.getDefinitionSourceField());
        }

    }
    @Test
    @DisplayName("TC_11_1_TA_4")
    public void test_TA_4() {
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
             * Create Code List for Working branch. States - Draft, Candidate, Deleted, Release Draft
             */
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            CodeListObject codeListDraft = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Draft");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListDraft, developerA);
            codeListForTesting.add(codeListDraft);

            CodeListObject codeListCandidate = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Candidate");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListCandidate, developerA);
            codeListForTesting.add(codeListCandidate);

            CodeListObject codeListDeleted = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Deleted");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListDeleted, developerA);
            codeListForTesting.add(codeListDeleted);

            /**
             * This code list should be in a draft release, not in a working branch
             */
            CodeListObject codeListReleaseDraft = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Release Draft");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListReleaseDraft, developerA);
            codeListForTesting.add(codeListReleaseDraft);
        }
        HomePage homePage = loginPage().signIn(developerB.getLoginId(), developerB.getPassword());
        getDriver().manage().window().maximize();
        for (CodeListObject cl : codeListForTesting) {
            /**
             * The developer can view the details of a CL that is in Draft, Candidate, Deleted, or Release Draft state and owned by any developer
             * but he cannot make any change except adding comments.
             */
            assertNotEquals(developerB.getAppUserId(), cl.getOwnerUserId());
            ArrayList<String> acceptedStates = new ArrayList<>(List.of("Draft", "Candidate", "Deleted", "Release Draft"));
            assertTrue(acceptedStates.contains(cl.getState()));
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(cl.getName(), workingBranch.getReleaseNumber());
            assertDisabled(editCodeListPage.getDefinitionField());
            assertDisabled(editCodeListPage.getDefinitionSourceField());
            AddCodeListCommentDialog addCommentDialog = editCodeListPage.hitAddCommentButton();
            addCommentDialog.setComment("test comment");
            pressEscape();
        }

    }
    @Test
    @DisplayName("TC_11_1_TA_5")
    public void test_TA_5() {
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
             * Create Published Code List
             */
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            CodeListObject codeListPublished = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Published");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListPublished, developerA);
            codeListForTesting.add(codeListPublished);
        }
        HomePage homePage = loginPage().signIn(developerB.getLoginId(), developerB.getPassword());
        getDriver().manage().window().maximize();
        for (CodeListObject cl : codeListForTesting) {
            /**
             * The developer can view the details of a Published CL owned by any developer but he cannot make
             * any change except adding comments or make a new revision of the CL.
             */
            assertNotEquals(developerB.getAppUserId(), cl.getOwnerUserId());
            assertEquals("Published", cl.getState());
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(cl.getName(), workingBranch.getReleaseNumber());
            assertDisabled(editCodeListPage.getDefinitionField());
            assertDisabled(editCodeListPage.getDefinitionSourceField());
            AddCodeListCommentDialog addCommentDialog = editCodeListPage.hitAddCommentButton();
            addCommentDialog.setComment("test comment");
            pressEscape();
            editCodeListPage.hitRevise();
        }

    }
    @Test
    @DisplayName("TC_11_1_TA_6")
    public void test_TA_6() {
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        AppUserObject developerB;
        ReleaseObject workingBranch;
        AppUserObject endUser;
        {
            developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            thisAccountWillBeDeletedAfterTests(developerB);

            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
            /**
             * End user Code Lists. States - WIP, Draft, Candidate and Published
             */
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");
            CodeListObject codeListWIP = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, namespace, release, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListWIP, endUser);
            codeListForTesting.add(codeListWIP);

            CodeListObject codeListDraft = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, namespace, release, "Draft");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListDraft, endUser);
            codeListForTesting.add(codeListDraft);

            CodeListObject codeListCandidate = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, namespace, release, "Candidate");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListCandidate, endUser);
            codeListForTesting.add(codeListCandidate);

            CodeListObject codeListPublished = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, namespace, release, "codeListPublished");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListPublished, endUser);
            codeListForTesting.add(codeListPublished);
        }
        HomePage homePage = loginPage().signIn(developerB.getLoginId(), developerB.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        getDriver().manage().window().maximize();
        for (CodeListObject cl : codeListForTesting) {
            assertEquals(endUser.getAppUserId(), cl.getOwnerUserId());
            assertFalse(endUser.isDeveloper());
            assertThrows(NoSuchElementException.class, () -> viewEditCodeListPage.searchCodeListByNameAndBranch(cl.getName(), workingBranch.getReleaseNumber()));
        }

    }
    @Test
    @DisplayName("TC_11_1_TA_7")
    public void test_TA_7() {
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        AppUserObject developerB;
        AppUserObject developerA;
        ReleaseObject workingBranch;
        {
            developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developerA);
            /**
             * Create Code List for Working branch. States - Deleted
             */
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            CodeListObject codeListDeleted = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Deleted");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListDeleted, developerA);
            codeListForTesting.add(codeListDeleted);
        }
        HomePage homePage = loginPage().signIn(developerB.getLoginId(), developerB.getPassword());
        getDriver().manage().window().maximize();
        for (CodeListObject cl : codeListForTesting) {
            /**
             * The developer can view the details of a CL that is in Deleted owned by any developer
             * but he cannot make any change except adding comments.
             */
            assertNotEquals(developerB.getAppUserId(), cl.getOwnerUserId());
            assertEquals(developerA.getAppUserId(), cl.getOwnerUserId());
            assertTrue(developerA.isDeveloper());
            assertEquals("Deleted",cl.getState());
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(cl.getName(), workingBranch.getReleaseNumber());
            assertDisabled(editCodeListPage.getDefinitionField());
            assertDisabled(editCodeListPage.getDefinitionSourceField());
            AddCodeListCommentDialog addCommentDialog = editCodeListPage.hitAddCommentButton();
            addCommentDialog.setComment("test comment");
            pressEscape();
        }

    }

    @Test
    @DisplayName("TC_11_1_TA_8")
    public void test_TA_8() {
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        AppUserObject developerB;
        AppUserObject developerA;
        ReleaseObject workingBranch;
        {
            developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developerA);
            /**
             * Create Code List for Working branch. States - Draft, Candidate, Deleted, Release Draft
             */
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            CodeListObject codeListDraft = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Draft");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListDraft, developerA);
            codeListForTesting.add(codeListDraft);

            CodeListObject codeListCandidate = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Candidate");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListCandidate, developerA);
            codeListForTesting.add(codeListCandidate);

            CodeListObject codeListDeleted = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Deleted");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListDeleted, developerA);
            codeListForTesting.add(codeListDeleted);
        }
        HomePage homePage = loginPage().signIn(developerB.getLoginId(), developerB.getPassword());
        getDriver().manage().window().maximize();
        for (CodeListObject cl : codeListForTesting) {
            /**
             * A developer can add comments to any developer CL in any state.
             */
            assertNotEquals(developerB.getAppUserId(), cl.getOwnerUserId());
            assertEquals(developerA.getAppUserId(), cl.getOwnerUserId());
            assertTrue(developerA.isDeveloper());
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(cl.getName(), workingBranch.getReleaseNumber());
            assertDisabled(editCodeListPage.getDefinitionField());
            assertDisabled(editCodeListPage.getDefinitionSourceField());
            AddCodeListCommentDialog addCommentDialog = editCodeListPage.hitAddCommentButton();
            addCommentDialog.setComment("test comment");
            pressEscape();
        }

    }
    @Test
    @DisplayName("TC_11_1_TA_9")
    public void test_TA_9() {
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        AppUserObject endUser;
        AppUserObject developerA;
        ReleaseObject workingBranch;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developerA);
            /**
             * Create Code List for Working branch. States - Draft, Candidate, Deleted, Release Draft
             */
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            CodeListObject codeListDraft = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Draft");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListDraft, developerA);
            codeListForTesting.add(codeListDraft);

            CodeListObject codeListCandidate = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Candidate");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListCandidate, developerA);
            codeListForTesting.add(codeListCandidate);

            CodeListObject codeListDeleted = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Deleted");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListDeleted, developerA);
            codeListForTesting.add(codeListDeleted);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        getDriver().manage().window().maximize();
        for (CodeListObject cl : codeListForTesting) {
            /**
             * An end user can add comments to any developer CL in any state.
             */
            assertNotEquals(endUser.getAppUserId(), cl.getOwnerUserId());
            assertEquals(developerA.getAppUserId(), cl.getOwnerUserId());
            assertFalse(endUser.isDeveloper());
            assertTrue(developerA.isDeveloper());
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(cl.getName(), workingBranch.getReleaseNumber());
            assertDisabled(editCodeListPage.getDefinitionField());
            assertDisabled(editCodeListPage.getDefinitionSourceField());
            AddCodeListCommentDialog addCommentDialog = editCodeListPage.hitAddCommentButton();
            addCommentDialog.setComment("test comment");
            pressEscape();
        }

    }
    @Test
    @DisplayName("TC_11_1_TA_10")
    public void test_TA_10() {
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        AppUserObject developerB;
        Map<CodeListObject, ReleaseObject> codeListReleaseMap = new HashMap<>();
        {
            developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            AppUserObject developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developerA);
            /**
             * Create Code List for Release 10.8.4. States - WIP, Draft and Candidate
             */
            ReleaseObject releaseOne = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");
            CodeListObject codeListWIP = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, releaseOne, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListWIP, developerA);
            codeListForTesting.add(codeListWIP);
            codeListReleaseMap.put(codeListWIP, releaseOne);

            CodeListObject codeListDraft = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, releaseOne, "Draft");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListDraft, developerA);
            codeListForTesting.add(codeListDraft);
            codeListReleaseMap.put(codeListDraft, releaseOne);

            CodeListObject codeListCandidate = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, releaseOne, "Candidate");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListCandidate, developerA);
            codeListForTesting.add(codeListCandidate);
            codeListReleaseMap.put(codeListCandidate, releaseOne);

            /**
             * Create Code List for Release 10.8.6. States - WIP, Draft and Candidate
             */
            ReleaseObject releaseTwo = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.6");
            codeListWIP = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, releaseTwo, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListWIP, developerA);
            codeListForTesting.add(codeListWIP);
            codeListReleaseMap.put(codeListWIP, releaseTwo);

            codeListDraft = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, releaseTwo, "Draft");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListDraft, developerA);
            codeListForTesting.add(codeListDraft);
            codeListReleaseMap.put(codeListDraft, releaseTwo);

            codeListCandidate = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, releaseTwo, "Candidate");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListCandidate, developerA);
            codeListForTesting.add(codeListCandidate);
            codeListReleaseMap.put(codeListCandidate, releaseTwo);
        }
        HomePage homePage = loginPage().signIn(developerB.getLoginId(), developerB.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        getDriver().manage().window().maximize();
        for (CodeListObject cl : codeListForTesting) {
            ReleaseObject release = codeListReleaseMap.get(cl);
            assertNotEquals(developerB.getAppUserId(), cl.getOwnerUserId());
            viewEditCodeListPage.searchCodeListByNameAndBranch(cl.getName(), release.getReleaseNumber());
        }

    }
    @Test
    @DisplayName("TC_11_1_TA_11")
    public void test_TA_11() {
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        AppUserObject developerA;
        ReleaseObject workingBranch;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developerA);
            /**
             * Create Code List for Working branch. States - WIP, Draft and Candidate
             */
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            CodeListObject codeListWIP = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "WIP");
            codeListWIP.setDeprecated(true);
            getAPIFactory().getCodeListAPI().updateCodeList(codeListWIP);
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListWIP, developerA);
            codeListForTesting.add(codeListWIP);

            CodeListObject codeListDraft = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Draft");
            codeListDraft.setDeprecated(true);
            getAPIFactory().getCodeListAPI().updateCodeList(codeListDraft);
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListDraft, developerA);
            codeListForTesting.add(codeListDraft);

            CodeListObject codeListCandidate = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Candidate");
            codeListCandidate.setDeprecated(true);
            getAPIFactory().getCodeListAPI().updateCodeList(codeListCandidate);
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListCandidate, developerA);
            codeListForTesting.add(codeListCandidate);

            codeListWIP = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListWIP, developerA);
            codeListForTesting.add(codeListWIP);

            codeListDraft = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Draft");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListDraft, developerA);
            codeListForTesting.add(codeListDraft);

            codeListCandidate = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Candidate");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListCandidate, developerA);
            codeListForTesting.add(codeListCandidate);
        }
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        getDriver().manage().window().maximize();
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        for (CodeListObject cl : codeListForTesting) {
            viewEditCodeListPage.searchCodeListByNameAndDeprecation(cl, workingBranch.getReleaseNumber());
        }

    }
    @Test
    @DisplayName("TC_11_1_TA_12")
    public void test_TA_12() {
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        AppUserObject developerA;
        ReleaseObject workingBranch;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
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
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        getDriver().manage().window().maximize();
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        String previousState = "";
        for (CodeListObject cl : codeListForTesting) {
            if (previousState.equals("")){
                String currentState = cl.getState();
                previousState = cl.getState();
                viewEditCodeListPage.toggleState(currentState);
                viewEditCodeListPage.searchCodeListByNameAndBranch(cl.getName(), workingBranch.getReleaseNumber());
            }else{
                viewEditCodeListPage.toggleState(previousState);
                String currentState = cl.getState();
                previousState = cl.getState();
                viewEditCodeListPage.toggleState(currentState);
                viewEditCodeListPage.searchCodeListByNameAndBranch(cl.getName(), workingBranch.getReleaseNumber());
            }
        }

    }
    @Test
    @DisplayName("TC_11_1_TA_15")
    public void test_TA_15() {
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
        for (CodeListObject cl : codeListForTesting) {
            viewEditCodeListPage.searchCodeListByDefinitionAndBranch(cl, workingBranch.getReleaseNumber());
        }

    }

    private void pressEscape(){
        invisibilityOfLoadingContainerElement(getDriver());
        Actions action = new Actions(getDriver());
        action.sendKeys(Keys.ESCAPE).build().perform();
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
