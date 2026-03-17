package org.oagi.score.e2e.TS_39_ReleaseBranchDataTypeManagementForDeveloper;

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
import org.oagi.score.e2e.page.core_component.ViewEditDataTypePage;
import org.openqa.selenium.TimeoutException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.escape;

@Execution(ExecutionMode.CONCURRENT)
public class TC_39_1_AccessToDTViewingEditingAndCommenting extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_39_1_TA_1")
    public void developer_can_see_in_the_core_component_view_or_edit_page_all_data_types_owned_by_any_user_in_any_st() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            AppUserObject endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Number. Type", branch.getReleaseNumber());

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            NamespaceObject namespaceDeveloper = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);

            DTObject dtDevPublished = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseDT, developerB, namespaceDeveloper, "Published");
            dtForTesting.add(dtDevPublished);

            DTObject dtEUWIP = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseDT, endUserA, namespaceEU, "WIP");
            dtForTesting.add(dtEUWIP);

            DTObject dtEUQA = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseDT, endUserA, namespaceEU, "QA");
            dtForTesting.add(dtEUQA);

            DTObject dtEUProduction = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseDT, endUserA, namespaceEU, "Production");
            dtForTesting.add(dtEUProduction);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        viewEditDataTypePage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(dt.getOwnerUserId());
            if (owner.isDeveloper()){
                assertTrue(dt.getState().equals("Published"));
            }
            viewEditDataTypePage.setDEN(dt.getDen());
            viewEditDataTypePage.hitSearchButton();
            assertDoesNotThrow(() -> viewEditDataTypePage.getTableRecordByValue(dt.getDen()));
        }
    }

    @Test
    @DisplayName("TC_39_1_TA_2")
    public void developer_can_view_but_cannot_edit_the_details_of_data_type_in_the_selected_release_that_is_in_wip_s() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            AppUserObject endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Number. Type", branch.getReleaseNumber());

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);

            DTObject dtEUWIP = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseDT, endUserA, namespaceEU, "WIP");
            dtForTesting.add(dtEUWIP);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(developerA.getAppUserId()));
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
    @DisplayName("TC_39_1_TA_3")
    public void developer_can_view_the_details_of_data_type_that_is_in_qa_and_owned_by_any_user_but_he_cannot_make_a() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            AppUserObject endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Number. Type", branch.getReleaseNumber());

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);

            DTObject dtEUQA = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseDT, endUserA, namespaceEU, "QA");
            dtForTesting.add(dtEUQA);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(developerA.getAppUserId()));
            assertTrue(dt.getState().equals("QA"));
            DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
            assertDisabled(dtViewEditPage.getDefinitionField());
            assertDisabled(dtViewEditPage.getQualifierField());
            AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + dt.getDen());
            addCommentDialog.setComment("test comment");
            escape(getDriver());
        }

    }

    @Test
    @DisplayName("TC_39_1_TA_4")
    public void developer_can_view_the_details_of_data_type_which_is_in_production_state_owned_by_any_user_but_he_ca() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            AppUserObject endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Number. Type", branch.getReleaseNumber());

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);

            DTObject dtEUProduction = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseDT, endUserA, namespaceEU, "Production");
            dtForTesting.add(dtEUProduction);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(developerA.getAppUserId()));
            assertTrue(dt.getState().equals("Production"));
            DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
            assertDisabled(dtViewEditPage.getDefinitionField());
            assertDisabled(dtViewEditPage.getQualifierField());
            AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + dt.getDen());
            addCommentDialog.setComment("test comment");
            escape(getDriver());
            assertThrows(TimeoutException.class, () -> {dtViewEditPage.getAmendButton();});
        }

    }
    @Test
    @DisplayName("TC_39_1_TA_5")
    public void developer_can_view_the_details_of_data_type_that_is_in_deleted_but_he_cannot_make_any_change_except() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            AppUserObject endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Number. Type", branch.getReleaseNumber());

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);

            DTObject dtEUDeleted = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseDT, endUserA, namespaceEU, "Deleted");
            dtForTesting.add(dtEUDeleted);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(developerA.getAppUserId()));
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
    @DisplayName("TC_39_1_TA_6")
    public void developer_can_view_details_of_any_published_data_type_but_cannot_make_any_change_except_adding_comme() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            AppUserObject endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Number. Type", branch.getReleaseNumber());

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);
            NamespaceObject namespaceDeveloper = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            DTObject dtDevPublished = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseDT, developerB, namespaceDeveloper, "Published");
            dtForTesting.add(dtDevPublished);

            DTObject dtEUPublished = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseDT, endUserA, namespaceEU, "Published");
            dtForTesting.add(dtEUPublished);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(developerA.getAppUserId()));
            assertTrue(dt.getState().equals("Published"));
            ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
            DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
            assertDisabled(dtViewEditPage.getDefinitionField());
            assertDisabled(dtViewEditPage.getQualifierField());
            AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + dt.getDen());
            addCommentDialog.setComment("test comment");
            escape(getDriver());
        }

    }

    @Test
    @DisplayName("TC_39_1_TA_7")
    public void developer_cannot_make_a_new_revision_on_any_data_type() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            AppUserObject endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Number. Type", branch.getReleaseNumber());

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);
            NamespaceObject namespaceDeveloper = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            DTObject dtDevPublished = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseDT, developerB, namespaceDeveloper, "Published");
            dtForTesting.add(dtDevPublished);

            DTObject dtEUPublished = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseDT, endUserA, namespaceEU, "Published");
            dtForTesting.add(dtEUPublished);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(developerA.getAppUserId()));
            assertTrue(dt.getState().equals("Published"));
            ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
            DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
            assertThrows(TimeoutException.class, () -> {dtViewEditPage.getReviseButton();});
        }

    }

    @Test
    @DisplayName("TC_39_1_TA_8")
    public void developer_cannot_create_any_new_data_type() {
        AppUserObject developerA;
        ReleaseObject branch;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
        }
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        viewEditDataTypePage.setBranch(branch.getReleaseNumber());
        assertTrue(developerA.isDeveloper());
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
