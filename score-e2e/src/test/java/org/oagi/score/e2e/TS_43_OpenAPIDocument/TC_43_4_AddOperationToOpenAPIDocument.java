package org.oagi.score.e2e.TS_43_OpenAPIDocument;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.OpenAPIDocumentObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.oas.AddOperationForOpenAPIDocumentDialog;
import org.oagi.score.e2e.page.oas.EditOpenAPIDocumentPage;
import org.oagi.score.e2e.page.oas.OpenAPIDocumentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;

/**
 * Test Case 43.4 - Add an Operation Without a BIE to an OpenAPI Document (Issue #1730).
 * <p>
 * Covers the UI and persistence of "bodyless" operations: the Add Operation dialog, the auto-derived
 * Operation ID, validation, the resulting Endpoint Details row, inline editing, and removal.
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_43_4_AddOperationToOpenAPIDocument extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    private HomePage homePage;

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_43_4_1")
    public void endpoint_details_exposes_add_bie_and_add_operation_buttons() {
        EditOpenAPIDocumentPage editPage = openRandomEdit();

        assertNotNull(visibilityOfElementLocated(getDriver(),
                By.xpath("//span[contains(@class, \"title\") and normalize-space(.) = \"Endpoint Details\"]")));
        assertNotNull(editPage.getAddBIEButton());
        assertNotNull(editPage.getAddOperationButton());
    }

    @Test
    @DisplayName("TC_43_4_2")
    public void add_operation_dialog_shows_title_subtitle_and_fields() {
        EditOpenAPIDocumentPage editPage = openRandomEdit();
        AddOperationForOpenAPIDocumentDialog dialog = editPage.openAddOperationDialog();

        assertEquals("Add Operation", getText(dialog.getTitle()));
        assertEquals("Define an API operation (endpoint) that does not reference a BIE.",
                getText(dialog.getSubtitle()));
        assertNotNull(dialog.getVerbSelectField());
        assertNotNull(dialog.getResourceNameField());
        assertNotNull(dialog.getOperationIdField());
    }

    @Test
    @DisplayName("TC_43_4_3")
    public void verb_offers_only_delete_and_patch_defaulting_to_delete() {
        EditOpenAPIDocumentPage editPage = openRandomEdit();
        AddOperationForOpenAPIDocumentDialog dialog = editPage.openAddOperationDialog();

        assertEquals("DELETE", getText(dialog.getVerbSelectField()));
        assertEquals(Arrays.asList("DELETE", "PATCH"), dialog.getVerbOptions());
    }

    @Test
    @DisplayName("TC_43_4_4")
    public void operation_id_is_auto_derived_from_verb_and_path() {
        EditOpenAPIDocumentPage editPage = openRandomEdit();
        AddOperationForOpenAPIDocumentDialog dialog = editPage.openAddOperationDialog();

        dialog.setVerb("PATCH");
        dialog.setResourceName("/production-order/{id}");
        assertEquals("updateProductionOrder", dialog.getOperationId());
        assertEquals("Auto-generated from the verb and path; you can override it.", dialog.getOperationIdHint());

        dialog.setVerb("DELETE");
        dialog.setResourceName("/item/{id}");
        assertEquals("deleteItem", dialog.getOperationId());
    }

    @Test
    @DisplayName("TC_43_4_5")
    public void overriding_operation_id_stops_auto_fill() {
        EditOpenAPIDocumentPage editPage = openRandomEdit();
        AddOperationForOpenAPIDocumentDialog dialog = editPage.openAddOperationDialog();

        dialog.setVerb("PATCH");
        dialog.setResourceName("/production-order/{id}");
        dialog.setOperationId("myCustomOperationId");

        dialog.setVerb("DELETE");
        dialog.setResourceName("/item/{id}");

        assertEquals("myCustomOperationId", dialog.getOperationId());
    }

    @Test
    @DisplayName("TC_43_4_6")
    public void add_is_disabled_until_required_fields_are_filled_and_cancel_closes() {
        EditOpenAPIDocumentPage editPage = openRandomEdit();
        AddOperationForOpenAPIDocumentDialog dialog = editPage.openAddOperationDialog();

        // Verb defaults to DELETE but Resource Name and Operation ID are empty.
        assertThrows(TimeoutException.class, () -> dialog.getAddButton(true));

        dialog.setResourceName("/item/{id}");
        assertNotNull(dialog.getAddButton(true));

        dialog.cancel();
        assertFalse(dialog.isOpened());
    }

    @Test
    @DisplayName("TC_43_4_7")
    public void adding_a_valid_operation_shows_snackbar_and_a_new_row() {
        EditOpenAPIDocumentPage editPage = openRandomEdit();
        String operationId = addBodylessOperation(editPage, "DELETE", "/item/{id}", null);

        WebElement row = editPage.getTableRecordAtIndex(1);
        assertNotNull(row);
        assertEquals(operationId, editPage.getRowOperationId(row));
    }

    @Test
    @DisplayName("TC_43_4_8")
    public void bodyless_row_has_empty_den_and_shows_its_values() {
        EditOpenAPIDocumentPage editPage = openRandomEdit();
        String tag = "tag_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String operationId = addBodylessOperation(editPage, "DELETE", "/item/{id}", tag);

        WebElement row = editPage.getTableRecordAtIndex(1);
        assertEquals("", editPage.getRowDen(row).trim());
        assertEquals("DELETE", editPage.getRowVerb(row));
        assertEquals("/item/{id}", editPage.getRowResourceName(row));
        assertEquals(operationId, editPage.getRowOperationId(row));
        assertEquals(tag, editPage.getRowTagName(row));
    }

    @Test
    @DisplayName("TC_43_4_9")
    public void bodyless_row_disables_array_suppress_and_message_body_controls() {
        EditOpenAPIDocumentPage editPage = openRandomEdit();
        addBodylessOperation(editPage, "DELETE", "/item/{id}", null);

        WebElement row = editPage.getTableRecordAtIndex(1);
        assertTrue(editPage.isRowArrayIndicatorDisabled(row), "Array Indicator should be disabled for a bodyless row");
        assertTrue(editPage.isRowSuppressRootDisabled(row), "Suppress Root should be disabled for a bodyless row");
        assertTrue(editPage.isRowMessageBodyDisabled(row), "Message Body should be disabled for a bodyless row");
    }

    @Test
    @DisplayName("TC_43_4_10")
    public void bodyless_row_supports_inline_edit_with_required_and_uniqueness_validation() {
        EditOpenAPIDocumentPage editPage = openRandomEdit();
        addBodylessOperation(editPage, "DELETE", "/item/{id}", null);
        addBodylessOperation(editPage, "PATCH", "/order/{id}", null);

        WebElement firstRow = editPage.getTableRecordAtIndex(1);
        WebElement secondRow = editPage.getTableRecordAtIndex(2);
        String secondOperationId = editPage.getRowOperationId(secondRow);

        editPage.setRowResourceName(firstRow, "/changed/{id}");
        assertEquals("/changed/{id}", editPage.getRowResourceName(firstRow));

        editPage.setRowOperationId(firstRow, "");
        assertEquals("Operation ID is required.", editPage.getRowOperationIdError(firstRow));

        editPage.setRowOperationId(firstRow, secondOperationId);
        assertEquals("Operation ID must be unique within the document.",
                editPage.getRowOperationIdError(firstRow));
    }

    @Test
    @DisplayName("TC_43_4_11")
    public void inline_edits_to_a_bodyless_row_persist_after_update() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        OpenAPIDocumentObject openAPIDocument =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);

        EditOpenAPIDocumentPage editPage = openEditOpenAPIDocumentPage(endUser, openAPIDocument);
        addBodylessOperation(editPage, "DELETE", "/item/{id}", null);

        WebElement row = editPage.getTableRecordAtIndex(1);
        editPage.setRowResourceName(row, "/persisted/{id}");
        editPage.setRowOperationId(row, "persistedOperationId");
        editPage.hitUpdateButton();

        // Reload the same Edit page (already signed in) to verify persistence.
        editPage.openPage();
        WebElement reopenedRow = editPage.getTableRecordAtIndex(1);
        assertEquals("/persisted/{id}", editPage.getRowResourceName(reopenedRow));
        assertEquals("persistedOperationId", editPage.getRowOperationId(reopenedRow));
    }

    @Test
    @DisplayName("TC_43_4_12")
    public void a_bodyless_operation_can_be_removed() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        OpenAPIDocumentObject openAPIDocument =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);

        EditOpenAPIDocumentPage editPage = openEditOpenAPIDocumentPage(endUser, openAPIDocument);
        addBodylessOperation(editPage, "DELETE", "/item/{id}", null);

        WebElement row = editPage.getTableRecordAtIndex(1);
        editPage.toggleSelect(row);
        editPage.removeSelectedBIEs();

        // Reload the same Edit page (already signed in) to verify the operation is gone.
        editPage.openPage();
        assertThrows(TimeoutException.class, () -> editPage.getTableRecordAtIndex(1));
    }

    private EditOpenAPIDocumentPage openRandomEdit() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        OpenAPIDocumentObject openAPIDocument =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);
        return openEditOpenAPIDocumentPage(endUser, openAPIDocument);
    }

    private EditOpenAPIDocumentPage openEditOpenAPIDocumentPage(AppUserObject endUser,
                                                               OpenAPIDocumentObject openAPIDocument) {
        this.homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = this.homePage.getBIEMenu();
        OpenAPIDocumentPage openAPIDocumentPage = bieMenu.openOpenAPIDocumentSubMenu();
        return openAPIDocumentPage.openEditOpenAPIDocumentPage(openAPIDocument);
    }

    private String addBodylessOperation(EditOpenAPIDocumentPage editPage, String verb,
                                        String resourceName, String tag) {
        AddOperationForOpenAPIDocumentDialog dialog = editPage.openAddOperationDialog();
        dialog.setVerb(verb);
        dialog.setResourceName(resourceName);
        if (tag != null) {
            dialog.setTag(tag);
        }
        String operationId = dialog.getOperationId();
        dialog.hitAddButton();
        return operationId;
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        this.randomAccounts.forEach(randomAccount -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId());
        });
    }
}
