package org.oagi.score.e2e.TS_38_WorkingBranchDataTypeManagementForDeveloper;

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
import org.oagi.score.e2e.page.core_component.DTViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.TimeoutException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_38_1_DTAccess extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_38_1_TA_1")
    public void test_TA_1() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Numeric. Type", "Working");
            DTObject dtWIP = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseDT, developerB, namespace, "WIP");
            dtForTesting.add(dtWIP);

            DTObject dtDraft = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseDT, developerB, namespace, "Draft");
            dtForTesting.add(dtDraft);

            DTObject dtCandidate = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseDT, developerB, namespace, "Candidate");
            dtForTesting.add(dtCandidate);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(developerA.getAppUserId()));
            viewEditCoreComponentPage.setDEN(dt.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            assertDoesNotThrow(() -> viewEditCoreComponentPage.getTableRecordByValue(dt.getDen()));
        }

    }

    @Test
    @DisplayName("TC_38_1_TA_2")
    public void test_TA_2() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Code. Type", branch.getReleaseNumber());
            DTObject randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerA, namespace, "WIP");
            dtForTesting.add(randomBDT);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        for (DTObject dt : dtForTesting) {
            assertTrue(dt.getOwnerUserId().equals(developerA.getAppUserId()));
            assertTrue(dt.getState().equals("WIP"));
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            dtViewEditPage.setQualifier("qualifier");
            dtViewEditPage.hitUpdateButton();
        }

    }

    @Test
    @DisplayName("TC_38_1_TA_3")
    public void test_TA_3() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Code. Type", branch.getReleaseNumber());
            DTObject randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerB, namespace, "WIP");
            dtForTesting.add(randomBDT);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(developerA.getAppUserId()));
            assertTrue(dt.getState().equals("WIP"));
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            assertDisabled(dtViewEditPage.getDefinitionField());
            assertDisabled(dtViewEditPage.getQualifierField());
            AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + dt.getDen());
            addCommentDialog.setComment("test comment");
            escape(getDriver());
        }

    }

    @Test
    @DisplayName("TC_38_1_TA_4")
    public void test_TA_4() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Code. Type", branch.getReleaseNumber());

            DTObject randomBDTDraft = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerB, namespace, "Draft");
            dtForTesting.add(randomBDTDraft);

            DTObject randomBDTCandidate = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerB, namespace, "Candidate");
            dtForTesting.add(randomBDTCandidate);

            DTObject randomBDTReleaseDraft = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerB, namespace, "ReleaseDraft");
            dtForTesting.add(randomBDTReleaseDraft);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(developerA.getAppUserId()));
            assertTrue(List.of("Draft", "Candidate", "ReleaseDraft").contains(dt.getState()));
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            assertDisabled(dtViewEditPage.getDefinitionField());
            assertDisabled(dtViewEditPage.getQualifierField());
            AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + dt.getDen());
            addCommentDialog.setComment("test comment");
            escape(getDriver());
        }

    }
    @Test
    @DisplayName("TC_38_1_TA_5")
    public void test_TA_5() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Code. Type", branch.getReleaseNumber());

            DTObject randomBDTPublished = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerB, namespace, "Published");
            dtForTesting.add(randomBDTPublished);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(developerA.getAppUserId()));
            assertTrue(dt.getState().equals("Published"));
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            assertDisabled(dtViewEditPage.getDefinitionField());
            assertDisabled(dtViewEditPage.getQualifierField());
            AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + dt.getDen());
            addCommentDialog.setComment("test comment");
            escape(getDriver());
            assertDoesNotThrow(() -> {dtViewEditPage.getReviseButton();});
        }

    }
    @Test
    @DisplayName("TC_38_1_TA_6")
    public void test_TA_6() {
        AppUserObject developerA;
        AppUserObject endUser;
        ReleaseObject branch;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");

        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        assertFalse(endUser.isDeveloper());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        assertThrows(TimeoutException.class, () -> viewEditCoreComponentPage.getCreateDTButton());
    }

    @Test
    @DisplayName("TC_38_1_TA_7")
    public void test_TA_7() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Code. Type", branch.getReleaseNumber());

            DTObject randomBDTDeleted = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerB, namespace, "Deleted");
            dtForTesting.add(randomBDTDeleted);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(developerA.getAppUserId()));
            assertTrue(dt.getState().equals("Deleted"));
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            assertDoesNotThrow(() -> {DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.
                    openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());});
        }
    }

    @Test
    @DisplayName("TC_38_1_TA_8")
    public void test_TA_8() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Code. Type", branch.getReleaseNumber());

            DTObject randomBDTDeleted = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerA, namespace, "Deleted");
            dtForTesting.add(randomBDTDeleted);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (DTObject dt : dtForTesting) {
            assertTrue(dt.getOwnerUserId().equals(developerA.getAppUserId()));
            assertTrue(dt.getState().equals("Deleted"));
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            assertDisabled(dtViewEditPage.getDefinitionField());
            assertDisabled(dtViewEditPage.getQualifierField());
            AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + dt.getDen());
            addCommentDialog.setComment("test comment");
            escape(getDriver());
        }

    }

    @Test
    @DisplayName("TC_38_1_TA_9")
    public void test_TA_9() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Code. Type", branch.getReleaseNumber());

            DTObject randomBDTDeleted = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerB, namespace, "Deleted");
            dtForTesting.add(randomBDTDeleted);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(developerA.getAppUserId()));
            assertTrue(dt.getState().equals("Deleted"));
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            assertDisabled(dtViewEditPage.getDefinitionField());
            assertDisabled(dtViewEditPage.getQualifierField());
            AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + dt.getDen());
            addCommentDialog.setComment("test comment");
            escape(getDriver());
        }

    }
    @Test
    @DisplayName("TC_38_1_TA_10")
    public void test_TA_10() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Code. Type", branch.getReleaseNumber());

            DTObject randomBDTDeleted = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerA, namespace, "Deleted");
            dtForTesting.add(randomBDTDeleted);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (DTObject dt : dtForTesting) {
            assertTrue(dt.getOwnerUserId().equals(developerA.getAppUserId()));
            assertTrue(dt.getState().equals("Deleted"));
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            assertDoesNotThrow(() -> dtViewEditPage.hitRestoreButton());
        }

    }
    @Test
    @DisplayName("TC_38_1_TA_11")
    public void test_TA_11() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Code. Type", branch.getReleaseNumber());

            DTObject randomBDTDeleted = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerB, namespace, "Deleted");
            dtForTesting.add(randomBDTDeleted);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(developerA.getAppUserId()));
            assertTrue(dt.getState().equals("Deleted"));
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            assertDoesNotThrow(() -> dtViewEditPage.hitRestoreButton());
        }

    }

    @Test
    @DisplayName("TC_38_1_TA_12_a")
    public void test_TA_12_a() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Code. Type", branch.getReleaseNumber());

            DTObject randomBDTWIPOne = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerA, namespace, "WIP");
            dtForTesting.add(randomBDTWIPOne);

            DTObject randomBDTWIPTwo = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerA, namespace, "WIP");
            dtForTesting.add(randomBDTWIPTwo);

            DTObject randomBDTWIPThree = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerA, namespace, "WIP");
            dtForTesting.add(randomBDTWIPThree);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            assertTrue(dt.getOwnerUserId().equals(developerA.getAppUserId()));
            assertTrue(dt.getState().equals("WIP"));
            click(viewEditCoreComponentPage.getTableRecordByValue(dt.getDen()));
        }
        viewEditCoreComponentPage.hitMoveToDraftButton();

    }
    @Test
    @DisplayName("TC_38_1_TA_12_b")
    public void test_TA_12_b() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Code. Type", branch.getReleaseNumber());

            DTObject randomBDTDraftOne = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerA, namespace, "Draft");
            dtForTesting.add(randomBDTDraftOne);

            DTObject randomBDTDraftTwo = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerA, namespace, "Draft");
            dtForTesting.add(randomBDTDraftTwo);

            DTObject randomBDTDraftThree = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerA, namespace, "Draft");
            dtForTesting.add(randomBDTDraftThree);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            assertTrue(dt.getOwnerUserId().equals(developerA.getAppUserId()));
            assertTrue(dt.getState().equals("Draft"));
            click(viewEditCoreComponentPage.getTableRecordByValue(dt.getDen()));
        }
        viewEditCoreComponentPage.hitBackToWIPButton();

    }
    @Test
    @DisplayName("TC_38_1_TA_12_c")
    public void test_TA_12_c() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Code. Type", branch.getReleaseNumber());

            DTObject randomBDTDraftOne = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerA, namespace, "Draft");
            dtForTesting.add(randomBDTDraftOne);

            DTObject randomBDTDraftTwo = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerA, namespace, "Draft");
            dtForTesting.add(randomBDTDraftTwo);

            DTObject randomBDTDraftThree = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerA, namespace, "Draft");
            dtForTesting.add(randomBDTDraftThree);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            assertTrue(dt.getOwnerUserId().equals(developerA.getAppUserId()));
            assertTrue(dt.getState().equals("Draft"));
            click(viewEditCoreComponentPage.getTableRecordByValue(dt.getDen()));
        }
        viewEditCoreComponentPage.hitMoveToCandidateButton();

    }
    @Test
    @DisplayName("TC_38_1_TA_12_d")
    public void test_TA_12_d() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Code. Type", branch.getReleaseNumber());

            DTObject randomBDTWIPOne = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerA, namespace, "WIP");
            dtForTesting.add(randomBDTWIPOne);

            DTObject randomBDTWIPTwo = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerA, namespace, "WIP");
            dtForTesting.add(randomBDTWIPTwo);

            DTObject randomBDTWIPThree = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerA, namespace, "WIP");
            dtForTesting.add(randomBDTWIPThree);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            assertTrue(dt.getOwnerUserId().equals(developerA.getAppUserId()));
            click(viewEditCoreComponentPage.getTableRecordByValue(dt.getDen()));
        }
        viewEditCoreComponentPage.hitTransferOwnershipButton();
    }

    @Test
    @DisplayName("TC_38_1_TA_12_e")
    public void test_TA_12_e() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Code. Type", branch.getReleaseNumber());

            DTObject randomBDTWIPOne = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerA, namespace, "WIP");
            dtForTesting.add(randomBDTWIPOne);

            DTObject randomBDTWIPTwo = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerA, namespace, "WIP");
            dtForTesting.add(randomBDTWIPTwo);

            DTObject randomBDTWIPThree = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerA, namespace, "WIP");
            dtForTesting.add(randomBDTWIPThree);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            assertTrue(dt.getOwnerUserId().equals(developerA.getAppUserId()));
            click(viewEditCoreComponentPage.getTableRecordByValue(dt.getDen()));
        }
        viewEditCoreComponentPage.hitDeleteButton();
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
