package org.oagi.score.e2e.TS_40_WorkingBranchDataTypeManagementForEndUser;

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
import org.oagi.score.e2e.page.core_component.ViewEditDataTypePage;
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
    public void end_user_can_see_in_the_core_component_view_or_edit_page_all_data_types_owned_by_any_user_in_any_sta() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Number. Type", "Working");

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
            
            DTObject dtWIP = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseDT, developerB, namespace, "WIP");
            dtForTesting.add(dtWIP);

            DTObject dtDraft = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseDT, developerB, namespace, "Draft");
            dtForTesting.add(dtDraft);

            DTObject dtCandidate = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseDT, developerB, namespace, "Candidate");
            dtForTesting.add(dtCandidate);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        viewEditDataTypePage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(dt.getOwnerUserId());
            assertFalse(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(owner.isDeveloper());
            viewEditDataTypePage.setDEN(dt.getDen());
            viewEditDataTypePage.hitSearchButton();
            assertDoesNotThrow(() -> viewEditDataTypePage.getTableRecordByValue(dt.getDen()));
        }

    }
    @Test
    @DisplayName("TC_40_1_TA_2")
    public void end_user_can_view_but_cannot_edit_the_details_of_data_type_in_the_working_release_that_is_in_wip_sta() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Number. Type", "Working");

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            DTObject dtWIP = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseDT, developerB, namespace, "WIP");
            dtForTesting.add(dtWIP);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        viewEditDataTypePage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(dt.getState().equals("WIP"));
            DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
            assertDisabled(dtViewEditPage.getDefinitionField());
            assertDisabled(dtViewEditPage.getQualifierField());
            AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + dt.getDen());
            addCommentDialog.setComment("test comment");
            escape(getDriver());
        }

    }

    @Test
    @DisplayName("TC_40_1_TA_3")
    public void end_user_can_view_the_details_of_data_type_that_is_in_draft_and_owned_by_any_user_but_he_cannot_make() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Number. Type", "Working");

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            DTObject dtDraft = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseDT, developerB, namespace, "Draft");
            dtForTesting.add(dtDraft);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        viewEditDataTypePage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(dt.getState().equals("Draft"));
            DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
            assertDisabled(dtViewEditPage.getDefinitionField());
            assertDisabled(dtViewEditPage.getQualifierField());
            AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + dt.getDen());
            addCommentDialog.setComment("test comment");
            escape(getDriver());
        }

    }

    @Test
    @DisplayName("TC_40_1_TA_4")
    public void end_user_can_view_the_details_of_data_type_which_is_in_candidate_state_owned_by_any_user_but_he_cann() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Number. Type", "Working");

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            DTObject dtCandidate = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseDT, developerB, namespace, "Candidate");
            dtForTesting.add(dtCandidate);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        viewEditDataTypePage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(dt.getState().equals("Candidate"));
            DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
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
    public void end_user_can_view_the_details_of_data_type_that_is_in_deleted_but_he_cannot_make_any_change_except_a() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Number. Type", "Working");

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            DTObject dtDeleted = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseDT, developerB, namespace, "Deleted");
            dtForTesting.add(dtDeleted);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        viewEditDataTypePage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(dt.getState().equals("Deleted"));
            DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
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
    public void end_user_can_view_details_of_any_published_data_type_but_cannot_make_any_change_except_adding_commen() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Number. Type", "Working");

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            DTObject dtPublished = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseDT, developerB, namespace, "Published");
            dtForTesting.add(dtPublished);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        viewEditDataTypePage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(dt.getState().equals("Published"));
            DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
            assertDisabled(dtViewEditPage.getDefinitionField());
            assertDisabled(dtViewEditPage.getQualifierField());
            AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + dt.getDen());
            addCommentDialog.setComment("test comment");
            escape(getDriver());
        }

    }

    @Test
    @DisplayName("TC_40_1_TA_7")
    public void developer_cannot_make_a_new_revision_on_any_data_type() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Number. Type", "Working");

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            DTObject dtPublished = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseDT, developerB, namespace, "Published");
            dtForTesting.add(dtPublished);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(dt.getState().equals("Published"));
            ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
            DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
            assertThrows(TimeoutException.class, () -> {dtViewEditPage.getReviseButton();});
        }

    }

    @Test
    @DisplayName("TC_40_1_TA_8")
    public void developer_cannot_create_any_new_data_type() {
        AppUserObject endUserA;
        LibraryObject library;
        ReleaseObject branch;
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        viewEditDataTypePage.setBranch(branch.getReleaseNumber());
        assertFalse(endUserA.isDeveloper());
        assertThrows(TimeoutException.class, () -> viewEditDataTypePage.getNewDataTypeButton());
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
