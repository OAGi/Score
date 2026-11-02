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
import org.oagi.score.e2e.page.agency_id_list.EditAgencyIDListPage;
import org.oagi.score.e2e.page.agency_id_list.EditAgencyIDListValueDialog;
import org.oagi.score.e2e.page.agency_id_list.ViewEditAgencyIDListPage;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.EditCodeListValueDialog;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.AssertionHelper.assertNotChecked;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_17_3_EditingABrandNewEndUserCodeList extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_17_3_TA_1")
    public void end_user_can_change_the_properties_of_the_code_list_and_save_changes() {
        AppUserObject endUser;
        LibraryObject library;
        ReleaseObject branch;
        CodeListObject codeList;
        AgencyIDListObject agencyIDList;
        AgencyIDListValueObject agencyIDListValue;
        AgencyIDListObject otherAgencyIDList;
        AgencyIDListValueObject otherAgencyIDListValue;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

            /**
             * Create WIP end-user Code List for a particular release branch.
             */
            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUser, namespaceEU, branch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
            agencyIDList = getAPIFactory().getAgencyIDListAPI()
                    .createRandomAgencyIDList(endUser, namespaceEU, branch, "Production");
            agencyIDListValue = getAPIFactory().getAgencyIDListValueAPI()
                    .createRandomAgencyIDListValue(endUser, agencyIDList);
            otherAgencyIDList = getAPIFactory().getAgencyIDListAPI()
                    .createRandomAgencyIDList(endUser, namespaceEU, branch, "Production");
            otherAgencyIDListValue = getAPIFactory().getAgencyIDListValueAPI()
                    .createRandomAgencyIDListValue(endUser, otherAgencyIDList);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        assertEquals("false", editCodeListPage.getAgencyIDListField().getAttribute("aria-required"));
        // Clear any inherited/default pair so the optional empty-pair behavior is explicit.
        if (!getDriver().findElements(By.xpath("//button[@aria-label='Clear Agency ID List']")).isEmpty()) {
            click(visibilityOfElementLocated(getDriver(),
                    By.xpath("//button[@aria-label='Clear Agency ID List']")));
        }
        assertEquals("false", editCodeListPage.getAgencyIDListValueField().getAttribute("aria-required"),
                "Agency ID List Value must be optional while the list is unset (selected list: " +
                        getText(editCodeListPage.getAgencyIDListField()) + ", required attribute: " +
                        editCodeListPage.getAgencyIDListValueField().getAttribute("required") + ").");
        editCodeListPage.setAgencyIDList(agencyIDList);
        assertEquals("true", editCodeListPage.getAgencyIDListValueField().getAttribute("aria-required"));
        click(editCodeListPage.getAgencyIDListValueField());
        assertTrue(getDriver().findElements(By.xpath("//mat-option//span[contains(text(), \"" + agencyIDListValue.getValue() + "\")]"))
                .size() > 0, "The selected Agency ID List's values should be available.");
        assertTrue(getDriver().findElements(By.xpath("//mat-option//span[contains(text(), \"" + otherAgencyIDListValue.getValue() + "\")]"))
                .isEmpty(), "Values from another Agency ID List must not be available.");
        escape(getDriver());
        editCodeListPage.setAgencyIDListValue(agencyIDListValue);
        /**
         * Test Assertion #11.3.1.a
         */
        editCodeListPage.setName("new name");
        editCodeListPage.setVersion("new version");
        editCodeListPage.hitUpdateButton();
        String agencyIDListText = getText(editCodeListPage.getAgencyIDListField());
        assertTrue(getAPIFactory().getCodeListAPI().checkCodeListUniqueness(codeList, agencyIDListText));
        /**
         * Test Assertion #11.3.1.b
         * Note: For developer Based Code list is not visible on the UI
         */
        assertTrue(codeList.getBasedCodeListManifestId() == null);
        /**
         * Test Assertion #11.3.1.c
         */
        assertEquals("true", editCodeListPage.getCodeListNameField().getAttribute("aria-required"));
        assertEquals("false", editCodeListPage.getAgencyIDListField().getAttribute("aria-required"));
        assertEquals("true", editCodeListPage.getAgencyIDListValueField().getAttribute("aria-required"));
        assertEquals("true", editCodeListPage.getVersionField().getAttribute("aria-required"));
        assertEquals("true", editCodeListPage.getNamespaceSelectField().getAttribute("aria-required"));
        assertDisabled(editCodeListPage.getDeprecatedSelectField());
        assertNotChecked(editCodeListPage.getDeprecatedSelectField());
        List<NamespaceObject> standardNamespaces = getAPIFactory().getNamespaceAPI().getStandardNamespacesURIs(
                getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec")
        );
        for (NamespaceObject namespace : standardNamespaces) {
            assertThrows(Exception.class, () -> {
                editCodeListPage.setNamespace(namespace);
            });
        }
        escape(getDriver());
        /**
         * Test Assertion #11.3.1.d
         */
        editCodeListPage.setDefinition("");
        editCodeListPage.hitUpdateButton();
        assertEquals("Are you sure you want to update this without definitions?",
                editCodeListPage.getDefinitionWarningDialogMessage());
        editCodeListPage.hitUpdateAnywayButton();
    }

    @Test
    @DisplayName("TC_17_3_TA_2")
    public void end_user_can_add_a_code_list_value_the_code_list_value_shall_have_the_following_field_code_short_nam() {
        AppUserObject endUser;
        LibraryObject library;
        ReleaseObject branch;
        CodeListObject codeList;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

            /**
             * Create WIP end-user Code List for a particular release branch.
             */
            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUser, namespaceEU, branch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
        editCodeListValueDialog.setCode("new value code");
        editCodeListValueDialog.setMeaning("new value meaning");
        editCodeListValueDialog.setDefinition("new value definition");
        editCodeListValueDialog.setDefinitionSource("new value definition source");
        assertDisabled(editCodeListValueDialog.getDeprecatedSelectField());
        assertNotChecked(editCodeListValueDialog.getDeprecatedSelectField());
        editCodeListValueDialog.hitAddButton();

        editCodeListValueDialog = editCodeListPage.addCodeListValue();
        editCodeListValueDialog.setCode("new value code");
        editCodeListValueDialog.setMeaning("new value meaning");
        editCodeListValueDialog.setDefinition("new value definition");
        editCodeListValueDialog.setDefinitionSource("new value definition source");
        assertDisabled(editCodeListValueDialog.getDeprecatedSelectField());
        assertNotChecked(editCodeListValueDialog.getDeprecatedSelectField());
        String enteredValue = getText(editCodeListValueDialog.getCodeField());
        editCodeListValueDialog.hitAddButton();
        String message = enteredValue + " already exist";
        assert message.equals(getSnackBarMessage(getDriver()));
    }

    @Test
    @DisplayName("TC_17_3_TA_3")
    public void a_code_list_with_base_the_end_user_can_remove_derived_code_list_values_and_save() {
        AppUserObject endUser;
        LibraryObject library;
        ReleaseObject branch;
        CodeListObject codeList;
        List<CodeListValueObject> values;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

            /**
             * Create derived WIP end-user Code List for a particular release branch.
             */
            CodeListObject baseCodeList = getAPIFactory().getCodeListAPI().
                    getCodeListByCodeListNameAndReleaseNum("oacl_ResponseCode", branch.getReleaseNumber());

            codeList = getAPIFactory().getCodeListAPI().
                    createDerivedCodeList(baseCodeList, endUser, namespaceEU, branch, "WIP");
            values = getAPIFactory().getCodeListValueAPI().getCodeListValuesByCodeListManifestId(codeList.getCodeListManifestId());
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        editCodeListPage.selectCodeListValue(values.get(1).getValue());
        editCodeListPage.removeCodeListValue();
        editCodeListPage.hitUpdateButton();
    }

    @Test
    @DisplayName("TC_17_3_TA_4")
    public void a_code_list_with_base_the_end_user_can_change_existing_code_list_values_and_their_details() {
        AppUserObject endUser;
        LibraryObject library;
        ReleaseObject branch;
        CodeListObject codeList;
        List<CodeListValueObject> values;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

            /**
             * Create derived WIP end-user Code List for a particular release branch.
             */
            CodeListObject baseCodeList = getAPIFactory().getCodeListAPI().
                    getCodeListByCodeListNameAndReleaseNum("oacl_ResponseCode", branch.getReleaseNumber());

            codeList = getAPIFactory().getCodeListAPI().
                    createDerivedCodeList(baseCodeList, endUser, namespaceEU, branch, "WIP");
            values = getAPIFactory().getCodeListValueAPI().getCodeListValuesByCodeListManifestId(codeList.getCodeListManifestId());

        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.editCodeListValue(values.get(1).getValue());
        editCodeListValueDialog.setMeaning("changed meaning");
        editCodeListValueDialog.setDefinition("changed definition");
        editCodeListValueDialog.setDefinitionSource("changed definition source");
        editCodeListValueDialog.hitSaveButton();
        editCodeListPage.hitUpdateButton();
    }

    @Test
    @DisplayName("TC_17_3_TA_5")
    public void new_code_list_value_only_the_code_and_short_name_are_required() {
        AppUserObject endUser;
        LibraryObject library;
        ReleaseObject branch;
        CodeListObject codeList;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

            /**
             * Create derived WIP end-user Code List for a particular release branch.
             */
            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUser, namespaceEU, branch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
        assertEquals("true", editCodeListValueDialog.getCodeField().getAttribute("aria-required"));
        assertEquals("true", editCodeListValueDialog.getMeaningField().getAttribute("aria-required"));
        assertEquals("false", editCodeListValueDialog.getDefinitionSourceField().getAttribute("aria-required"));
        assertEquals("false", editCodeListValueDialog.getDefinitionField().getAttribute("aria-required"));
        editCodeListValueDialog.setCode("new value code");
        editCodeListValueDialog.setMeaning("new value meaning");
        editCodeListValueDialog.hitAddButton();
        editCodeListPage.hitUpdateButton();
    }

    @Test
    @DisplayName("TC_17_3_TA_6")
    public void added_code_list_value_can_be_removed() {
        AppUserObject endUser;
        LibraryObject library;
        ReleaseObject branch;
        CodeListObject codeList;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

            /**
             * Create derived WIP end-user Code List for a particular release branch.
             */
            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUser, namespaceEU, branch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
        editCodeListValueDialog.setCode("new value code");
        editCodeListValueDialog.setMeaning("new value meaning");
        editCodeListValueDialog.hitAddButton();

        editCodeListPage.selectCodeListValue("new value code");
        editCodeListPage.removeCodeListValue();
    }

    @Test
    @DisplayName("TC_17_3_TA_7")
    public void end_user_can_edit_a_newly_added_code_list_value_details_except_the_deprecated_field_with_and_without() {
        AppUserObject endUser;
        LibraryObject library;
        ReleaseObject branch;
        CodeListObject codeList;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

            /**
             * Create derived WIP end-user Code List for a particular release branch.
             */
            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUser, namespaceEU, branch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
        editCodeListValueDialog.setCode("new value code");
        editCodeListValueDialog.setMeaning("new value meaning");
        editCodeListValueDialog.hitAddButton();

        editCodeListValueDialog = editCodeListPage.editCodeListValue("new value code");
        editCodeListValueDialog.setMeaning("changed meaning");
        editCodeListValueDialog.setDefinition("added definition");
        assertDisabled(editCodeListValueDialog.getDeprecatedSelectField());
        editCodeListValueDialog.hitSaveButton();
        editCodeListPage.hitUpdateButton();
    }

    @Test
    @DisplayName("TC_17_3_TA_8")
    public void end_user_can_select_end_user_agency_id_list_in_production_state_under_the_code_list() {
        AppUserObject endUser;
        LibraryObject library;
        ReleaseObject branch;
        CodeListObject codeList;
        NamespaceObject namespaceEU;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        homePage.setLibrary("connectSpec");
        branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
        assertNotNull(branch, "The Published branch used by the End User list scenario must exist.");
        assertEquals("Published", branch.getState());

        // Seed both records through the API; this case verifies that the Code List UI can select
        // a Production End User Agency ID List on the same published branch.
        codeList = getAPIFactory().getCodeListAPI().
                createRandomCodeList(endUser, namespaceEU, branch, "WIP");
        getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
        AgencyIDListObject agencyIDList = getAPIFactory().getAgencyIDListAPI()
                .createRandomAgencyIDList(endUser, namespaceEU, branch, "Production");
        AgencyIDListValueObject agencyIDListValue = getAPIFactory().getAgencyIDListValueAPI()
                .createRandomAgencyIDListValue(endUser, agencyIDList);
        AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(agencyIDList.getOwnerUserId());
        assertEquals("Production", agencyIDList.getState());
        assertFalse(owner.isDeveloper());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        editCodeListPage.setAgencyIDList(agencyIDList);
        editCodeListPage.setAgencyIDListValue(agencyIDListValue);
        editCodeListPage.hitUpdateButton();
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
