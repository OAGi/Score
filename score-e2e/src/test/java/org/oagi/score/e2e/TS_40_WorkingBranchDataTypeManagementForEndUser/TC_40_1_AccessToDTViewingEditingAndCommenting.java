package org.oagi.score.e2e.TS_40_WorkingBranchDataTypeManagementForEndUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.DTObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.code_list.AddCommentDialog;
import org.oagi.score.e2e.page.core_component.DTViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.TimeoutException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.escape;

@Execution(ExecutionMode.CONCURRENT)
public class TC_40_1_AccessToDTViewingEditingAndCommenting extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_40_1_TA_1")
    public void test_TA_1() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

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

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(dt.getOwnerUserId());
            assertFalse(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(owner.isDeveloper());
            viewEditCoreComponentPage.setDEN(dt.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            assertDoesNotThrow(() -> viewEditCoreComponentPage.getTableRecordByValue(dt.getDen()));
        }

    }
    @Test
    @DisplayName("TC_40_1_TA_2")
    public void test_TA_2() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Numeric. Type", "Working");
            DTObject dtWIP = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseDT, developerB, namespace, "WIP");
            dtForTesting.add(dtWIP);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
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
    @DisplayName("TC_40_1_TA_3")
    public void test_TA_3() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Numeric. Type", "Working");
            DTObject dtDraft = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseDT, developerB, namespace, "Draft");
            dtForTesting.add(dtDraft);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(dt.getState().equals("Draft"));
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            assertDisabled(dtViewEditPage.getDefinitionField());
            assertDisabled(dtViewEditPage.getQualifierField());
            AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + dt.getDen());
            addCommentDialog.setComment("test comment");
            escape(getDriver());
        }

    }

    @Test
    @DisplayName("TC_40_1_TA_4")
    public void test_TA_4() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Numeric. Type", "Working");
            DTObject dtCandidate = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseDT, developerB, namespace, "Candidate");
            dtForTesting.add(dtCandidate);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(dt.getState().equals("Candidate"));
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            assertDisabled(dtViewEditPage.getDefinitionField());
            assertDisabled(dtViewEditPage.getQualifierField());
            AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + dt.getDen());
            addCommentDialog.setComment("test comment");
            escape(getDriver());
            assertThrows(TimeoutException.class, () -> {dtViewEditPage.getReviseButton();});
        }

    }

    @Test
    @DisplayName("TC_40_1_TA_5")
    public void test_TA_5() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Numeric. Type", "Working");
            DTObject dtDeleted = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseDT, developerB, namespace, "Deleted");
            dtForTesting.add(dtDeleted);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(dt.getState().equals("Deleted"));
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            assertDisabled(dtViewEditPage.getDefinitionField());
            assertDisabled(dtViewEditPage.getQualifierField());
            AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + dt.getDen());
            addCommentDialog.setComment("test comment");
            escape(getDriver());
            assertThrows(TimeoutException.class, () -> {dtViewEditPage.getRestoreButton();});
        }

    }

    @Test
    @DisplayName("TC_40_1_TA_6")
    public void test_TA_6() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Numeric. Type", "Working");
            DTObject dtPublished = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseDT, developerB, namespace, "Published");
            dtForTesting.add(dtPublished);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(dt.getState().equals("Published"));
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            assertDisabled(dtViewEditPage.getDefinitionField());
            assertDisabled(dtViewEditPage.getQualifierField());
            AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + dt.getDen());
            addCommentDialog.setComment("test comment");
            escape(getDriver());
        }

    }

    @Test
    @DisplayName("TC_40_1_TA_7")
    public void test_TA_7() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Numeric. Type", "Working");
            DTObject dtPublished = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseDT, developerB, namespace, "Published");
            dtForTesting.add(dtPublished);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(dt.getState().equals("Published"));
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            assertThrows(TimeoutException.class, () -> {dtViewEditPage.getReviseButton();});
        }

    }

    @Test
    @DisplayName("TC_40_1_TA_8")
    public void test_TA_8() {
        AppUserObject endUserA;
        ReleaseObject branch;
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        }
        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        assertFalse(endUserA.isDeveloper());
        assertThrows(TimeoutException.class, () -> {viewEditCoreComponentPage.getCreateDTButton();});
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
