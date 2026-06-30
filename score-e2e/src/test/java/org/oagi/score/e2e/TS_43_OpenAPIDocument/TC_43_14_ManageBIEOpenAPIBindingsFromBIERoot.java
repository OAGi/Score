package org.oagi.score.e2e.TS_43_OpenAPIDocument;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.obj.ACCObject;
import org.oagi.score.e2e.obj.ASCCPObject;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.LibraryObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.obj.OpenAPIDocumentObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.BieOpenAPIDocumentAddDialog;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.oas.AddBIEForOpenAPIDocumentDialog;
import org.oagi.score.e2e.page.oas.EditOpenAPIDocumentPage;
import org.oagi.score.e2e.page.oas.OpenAPIDocumentExport;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.getSnackBarMessage;
import static org.oagi.score.e2e.impl.PageHelper.sendKeys;

/**
 * Issue #1519 — Manage a BIE's OpenAPI Document bindings from the BIE root.
 *
 * <p>The BIE-root editor surfaces a first-class "OpenAPI Document Information" panel that shows, adds, edits,
 * and removes the BIE's OpenAPI Document bindings in place. Because every write flows through the SAME backend
 * endpoints as the OpenAPI Document editor, a change made on the BIE root has the identical effect on the
 * database — and therefore on the generated OpenAPI document — as the same change made on the OpenAPI Document
 * screen. These tests verify, end to end:</p>
 * <ol>
 *   <li>the panel surfaces only on the BIE root and only when an OpenAPI Document exists;</li>
 *   <li>the Operation ID is derived identically on the BIE-root and OpenAPI Document screens (Issue #1732);</li>
 *   <li>an edit applied on the BIE root lands in the database exactly as the same edit applied on the OpenAPI
 *       Document screen (so the OpenAPI Document editor reads it back identically);</li>
 *   <li>every change is confirmed by generating the OpenAPI document and inspecting the YAML.</li>
 * </ol>
 * The previously delivered OpenAPI behaviors — OpenAPI 3.1 / DELETE body (Issue #1610), per-operation error
 * responses (Issue #1347), and one Request / one Response per (path, verb) (Issue #1492) — remain intact when
 * exercised through the new BIE-root edit path.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class TC_43_14_ManageBIEOpenAPIBindingsFromBIERoot extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    /* ============================================ Requirement 1: panel visibility ============================================ */

    @Test
    @DisplayName("TC_43_14_1")
    public void panel_surfaces_on_the_bie_root_when_the_bie_is_bound_to_an_openapi_document() {
        Ctx ctx = newContext();
        TopLevelASBIEPObject bie = createBie(ctx, "oas_1519_1_bc", "WIP");
        OpenAPIDocumentObject doc = createDoc(ctx);

        HomePage homePage = signIn(ctx.endUser);
        EditOpenAPIDocumentPage editOasPage = openOasEditor(homePage, doc);
        assignBie(editOasPage, bie, "GET", "Response");

        // Requirement 4: the binding generates a path-item with the derived Operation ID.
        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(editOasPage.clickGenerateAndDownload());
        String operationId = expectedOperationId("GET", bie.getPropertyTerm(), false);
        assertTrue(export.operationIds().contains(operationId),
                "Generated document should carry the derived Operation ID " + operationId);

        // Requirement 1: the panel surfaces on the BIE root and shows the binding.
        EditBIEPage editBIEPage = openBieEditor(homePage, bie);
        assertTrue(editBIEPage.isOpenAPIDocumentInformationPanelDisplayed(),
                "The OpenAPI Document Information panel should surface on the BIE root for a bound BIE");

        EditBIEPage.OpenAPIDocumentInformationPanel panel = editBIEPage.openOpenAPIDocumentInformationPanel();
        assertEquals(1, panel.getBindingCardCount());
        WebElement card = panel.getBindingCard(doc.getOasDocId());
        assertTrue(panel.getDocumentChipText(card).contains(doc.getTitle()),
                "The card's document chip should name the bound OpenAPI Document");
        assertEquals("GET", panel.getVerb(card));
        assertEquals("Response", panel.getMessageBody(card));
        assertEquals(operationId, panel.getOperationId(card));
    }

    @Test
    @DisplayName("TC_43_14_2")
    public void panel_shows_an_empty_state_and_add_button_for_an_editable_unbound_bie() {
        Ctx ctx = newContext();
        TopLevelASBIEPObject bie = createBie(ctx, "oas_1519_2_bc", "WIP");
        // At least one OpenAPI Document must exist so the panel surfaces (the BIE itself stays unbound).
        createDoc(ctx);

        HomePage homePage = signIn(ctx.endUser);
        EditBIEPage editBIEPage = openBieEditor(homePage, bie);

        assertTrue(editBIEPage.isOpenAPIDocumentInformationPanelDisplayed(),
                "An editable BIE root should show the panel so the first binding can be added, even with no binding");
        EditBIEPage.OpenAPIDocumentInformationPanel panel = editBIEPage.openOpenAPIDocumentInformationPanel();
        assertEquals(0, panel.getBindingCardCount());
        assertTrue(panel.isEmptyStateDisplayed(),
                "The panel should show the empty-state message for an unbound BIE");
        assertTrue(panel.isAddButtonDisplayed(),
                "The panel header '+' should be available on an editable BIE root");
    }

    @Test
    @DisplayName("TC_43_14_3")
    public void panel_is_hidden_for_a_non_editable_bie_with_no_binding() {
        Ctx ctx = newContext();
        // An OpenAPI Document exists, so the panel is hidden ONLY because the BIE is not editable and unbound
        // (a non-WIP BIE cannot be edited) — i.e. the (not-editable AND unbound) conjunct of showOasDocPanel().
        // The separate "no OpenAPI Document exists anywhere" guard (oasDocExists) is a DIFFERENT conjunct and is
        // not exercised here: a zero-document state is not deterministically reproducible in the shared e2e DB
        // because getOasDocList is global (it filters by neither owner nor release).
        createDoc(ctx);
        TopLevelASBIEPObject bie = createBie(ctx, "oas_1519_3_bc", "Production");

        HomePage homePage = signIn(ctx.endUser);
        EditBIEPage editBIEPage = openBieEditor(homePage, bie);

        assertFalse(editBIEPage.isOpenAPIDocumentInformationPanelDisplayed(),
                "The panel must not surface for a non-editable BIE that has no OpenAPI binding");
    }

    /* ===================================== Requirement 4 + #1519 add/remove via the BIE root ===================================== */

    @Test
    @DisplayName("TC_43_14_4")
    public void add_to_openapi_document_from_the_bie_root_binds_and_generates() {
        Ctx ctx = newContext();
        TopLevelASBIEPObject bie = createBie(ctx, "oas_1519_4_bc", "WIP");
        OpenAPIDocumentObject doc = createDoc(ctx);

        HomePage homePage = signIn(ctx.endUser);
        EditBIEPage editBIEPage = openBieEditor(homePage, bie);
        EditBIEPage.OpenAPIDocumentInformationPanel panel = editBIEPage.openOpenAPIDocumentInformationPanel();

        BieOpenAPIDocumentAddDialog dialog = panel.openAddDialog();
        dialog.selectOpenAPIDocument(doc.getTitle());
        dialog.setVerb("GET");
        dialog.setMessageBody("Response");
        String operationId = expectedOperationId("GET", bie.getPropertyTerm(), false);
        assertEquals(operationId, dialog.getOperationIdPreview(),
                "The add dialog should preview the same Operation ID the OpenAPI Document editor derives");
        dialog.hitAddButton();
        assertEquals("Added to OpenAPI Document.", getSnackBarMessage(getDriver()));

        WebElement card = panel.getBindingCard(doc.getOasDocId());
        assertEquals("GET", panel.getVerb(card));
        assertEquals(operationId, panel.getOperationId(card));

        // Requirement 4: generate from the OpenAPI Document screen and confirm the operation was created.
        EditOpenAPIDocumentPage editOasPage = openOasEditor(homePage, doc);
        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(editOasPage.clickGenerateAndDownload());
        assertTrue(export.operationIds().contains(operationId),
                "The BIE added from the BIE root should generate its operation in the OpenAPI document");
    }

    @Test
    @DisplayName("TC_43_14_5")
    public void unbinding_from_the_bie_root_removes_the_operation_from_the_generated_document() {
        Ctx ctx = newContext();
        TopLevelASBIEPObject bie = createBie(ctx, "oas_1519_5_bc", "WIP");
        OpenAPIDocumentObject doc = createDoc(ctx);

        HomePage homePage = signIn(ctx.endUser);
        EditOpenAPIDocumentPage editOasPage = openOasEditor(homePage, doc);
        // Bind two distinct operations so the document is still generatable after one is unbound.
        assignBie(editOasPage, bie, "GET", "Response");
        assignBie(editOasPage, bie, "POST", "Response");
        String getOperationId = expectedOperationId("GET", bie.getPropertyTerm(), false);
        String postOperationId = expectedOperationId("POST", bie.getPropertyTerm(), false);
        OpenAPIDocumentExport before = OpenAPIDocumentExport.from(editOasPage.clickGenerateAndDownload());
        assertTrue(before.operationIds().contains(getOperationId), "Pre-condition: the GET operation is present");
        assertTrue(before.operationIds().contains(postOperationId), "Pre-condition: the POST operation is present");

        EditBIEPage editBIEPage = openBieEditor(homePage, bie);
        EditBIEPage.OpenAPIDocumentInformationPanel panel = editBIEPage.openOpenAPIDocumentInformationPanel();
        assertEquals(2, panel.getBindingCardCount());
        WebElement getCard = panel.getBindingCardByOperationId(getOperationId);
        panel.unbind(getCard);
        assertEquals(1, panel.getBindingCardCount(), "Only the unbound operation's card should be removed");

        EditOpenAPIDocumentPage reopened = openOasEditor(homePage, doc);
        OpenAPIDocumentExport after = OpenAPIDocumentExport.from(reopened.clickGenerateAndDownload());
        assertFalse(after.operationIds().contains(getOperationId),
                "The generated document should no longer carry the unbound operation");
        assertTrue(after.operationIds().contains(postOperationId),
                "The remaining operation should still be generated");
    }

    /* ======================================= Requirement 2: identical Operation ID rule ======================================= */

    @Test
    @DisplayName("TC_43_14_6")
    public void operation_id_is_derived_identically_on_the_bie_root_and_openapi_screens() {
        Ctx ctx = newContext();
        TopLevelASBIEPObject bie = createBie(ctx, "oas_1519_6_bc", "WIP");
        OpenAPIDocumentObject docA = createDoc(ctx);
        OpenAPIDocumentObject docB = createDoc(ctx);

        HomePage homePage = signIn(ctx.endUser);

        // Read the OpenAPI Document editor's live-derived Operation IDs for every verb (GET, POST, PUT, PATCH,
        // DELETE) and for POST+array. (docA is only a read source — it is never saved or generated here.)
        EditOpenAPIDocumentPage editOasPage = openOasEditor(homePage, docA);
        assignBie(editOasPage, bie, "GET", "Response");
        WebElement rowA = editOasPage.getTableRecordByValue(bie.getDen());
        String oasGet = editOasPage.getRowOperationId(rowA);
        editOasPage.setRowVerb(rowA, "POST");
        String oasPost = editOasPage.getRowOperationId(rowA);
        editOasPage.setRowVerb(rowA, "PUT");
        String oasPut = editOasPage.getRowOperationId(rowA);
        editOasPage.setRowVerb(rowA, "PATCH");
        String oasPatch = editOasPage.getRowOperationId(rowA);
        editOasPage.setRowVerb(rowA, "DELETE");
        String oasDelete = editOasPage.getRowOperationId(rowA);
        editOasPage.setRowVerb(rowA, "POST");
        editOasPage.setRowArrayIndicator(rowA, true);
        String oasPostArray = editOasPage.getRowOperationId(rowA);

        // The BIE-root add dialog must preview the identical Operation IDs.
        EditBIEPage editBIEPage = openBieEditor(homePage, bie);
        EditBIEPage.OpenAPIDocumentInformationPanel panel = editBIEPage.openOpenAPIDocumentInformationPanel();
        BieOpenAPIDocumentAddDialog dialog = panel.openAddDialog();
        dialog.selectOpenAPIDocument(docB.getTitle());

        dialog.setVerb("GET");
        assertEquals(oasGet, dialog.getOperationIdPreview(), "GET Operation ID must match across both screens");
        assertEquals(expectedOperationId("GET", bie.getPropertyTerm(), false), oasGet);

        dialog.setVerb("POST");
        assertEquals(oasPost, dialog.getOperationIdPreview(), "POST Operation ID must match across both screens");
        assertEquals(expectedOperationId("POST", bie.getPropertyTerm(), false), oasPost);

        dialog.setVerb("PUT");
        assertEquals(oasPut, dialog.getOperationIdPreview(), "PUT Operation ID must match across both screens");
        assertEquals(expectedOperationId("PUT", bie.getPropertyTerm(), false), oasPut);

        dialog.setVerb("PATCH");
        assertEquals(oasPatch, dialog.getOperationIdPreview(), "PATCH Operation ID must match across both screens");
        assertEquals(expectedOperationId("PATCH", bie.getPropertyTerm(), false), oasPatch);

        dialog.setVerb("DELETE");
        assertEquals(oasDelete, dialog.getOperationIdPreview(), "DELETE Operation ID must match across both screens");
        assertEquals(expectedOperationId("DELETE", bie.getPropertyTerm(), false), oasDelete);

        dialog.setVerb("POST");
        dialog.setArrayIndicator(true);
        assertEquals(oasPostArray, dialog.getOperationIdPreview(),
                "POST + array Operation ID must match across both screens");
        assertEquals(expectedOperationId("POST", bie.getPropertyTerm(), true), oasPostArray);

        // Persist via the BIE root and confirm the same id is generated verbatim.
        dialog.setMessageBody("Response");
        dialog.hitAddButton();
        WebElement cardB = panel.getBindingCard(docB.getOasDocId());
        assertEquals(oasPostArray, panel.getOperationId(cardB));

        EditOpenAPIDocumentPage editOasPageB = openOasEditor(homePage, docB);
        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(editOasPageB.clickGenerateAndDownload());
        assertTrue(export.operationIds().contains(oasPostArray),
                "The Operation ID created from the BIE root should appear verbatim in the generated document");
    }

    /* ====================================== Requirement 3: identical effect on the database ====================================== */

    @Test
    @DisplayName("TC_43_14_7")
    public void edits_from_the_bie_root_persist_and_appear_on_the_openapi_screen_and_in_generate() {
        Ctx ctx = newContext();
        TopLevelASBIEPObject bie = createBie(ctx, "oas_1519_7_bc", "WIP");
        OpenAPIDocumentObject doc = createDoc(ctx);

        HomePage homePage = signIn(ctx.endUser);
        EditOpenAPIDocumentPage editOasPage = openOasEditor(homePage, doc);
        assignBie(editOasPage, bie, "GET", "Response");

        // Edit the binding from the BIE root: change the verb to POST, make it an array, set a tag.
        EditBIEPage editBIEPage = openBieEditor(homePage, bie);
        EditBIEPage.OpenAPIDocumentInformationPanel panel = editBIEPage.openOpenAPIDocumentInformationPanel();
        WebElement card = panel.getBindingCard(doc.getOasDocId());
        panel.setVerb(card, "POST");
        panel.setArray(card, true);
        panel.setTag(card, "TC4314Tag");
        String operationId = expectedOperationId("POST", bie.getPropertyTerm(), true);
        panel.hitUpdateButton();

        // The OpenAPI Document editor must read back exactly what the BIE root saved (identical DB effect).
        editOasPage = openOasEditor(homePage, doc);
        WebElement row = editOasPage.getTableRecordByValue(bie.getDen());
        assertEquals("POST", editOasPage.getRowVerb(row));
        assertTrue(editOasPage.isRowArrayIndicatorChecked(row), "The array indicator set on the BIE root should persist");
        assertEquals(operationId, editOasPage.getRowOperationId(row));
        assertEquals("TC4314Tag", editOasPage.getRowTagName(row));

        // Requirement 4: the edit is reflected in the generated document.
        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(editOasPage.clickGenerateAndDownload());
        assertTrue(export.operationIds().contains(operationId),
                "The Operation ID edited from the BIE root should be generated");
        assertTrue(export.countSchemaNamesContaining("List") >= 1,
                "The array binding edited from the BIE root should generate a '<BIEName>List' wrapper schema");
    }

    @Test
    @DisplayName("TC_43_14_8")
    public void the_same_edit_has_an_identical_effect_whether_made_on_the_bie_root_or_the_openapi_screen() {
        Ctx ctx = newContext();
        TopLevelASBIEPObject bie = createBie(ctx, "oas_1519_8_bc", "WIP");
        OpenAPIDocumentObject docOas = createDoc(ctx);   // edited on the OpenAPI Document screen
        OpenAPIDocumentObject docBie = createDoc(ctx);   // edited on the BIE root

        HomePage homePage = signIn(ctx.endUser);

        // Start both documents from the identical baseline: the same BIE as GET + Response.
        EditOpenAPIDocumentPage editOasA = openOasEditor(homePage, docOas);
        assignBie(editOasA, bie, "GET", "Response");
        EditOpenAPIDocumentPage editOasB = openOasEditor(homePage, docBie);
        assignBie(editOasB, bie, "GET", "Response");

        String sharedOperationId = "parityVerifyOperation";

        // Apply the edit-set on the OpenAPI Document screen for docOas.
        editOasA = openOasEditor(homePage, docOas);
        WebElement rowA = editOasA.getTableRecordByValue(bie.getDen());
        editOasA.setRowVerb(rowA, "POST");
        editOasA.setRowArrayIndicator(rowA, true);
        editOasA.setRowOperationId(rowA, sharedOperationId);
        editOasA.hitUpdateButton();

        // Apply the SAME edit-set from the BIE root for docBie.
        EditBIEPage editBIEPage = openBieEditor(homePage, bie);
        EditBIEPage.OpenAPIDocumentInformationPanel panel = editBIEPage.openOpenAPIDocumentInformationPanel();
        WebElement cardB = panel.getBindingCard(docBie.getOasDocId());
        panel.setVerb(cardB, "POST");
        panel.setArray(cardB, true);
        panel.setOperationId(cardB, sharedOperationId);
        panel.hitUpdateButton();

        // The two documents' persisted rows must now be identical (read both back on the OpenAPI screen).
        editOasA = openOasEditor(homePage, docOas);
        WebElement persistedA = editOasA.getTableRecordByValue(bie.getDen());
        String verbA = editOasA.getRowVerb(persistedA);
        boolean arrayA = editOasA.isRowArrayIndicatorChecked(persistedA);
        String opIdA = editOasA.getRowOperationId(persistedA);

        editOasB = openOasEditor(homePage, docBie);
        WebElement persistedB = editOasB.getTableRecordByValue(bie.getDen());
        assertEquals(verbA, editOasB.getRowVerb(persistedB), "Verb must be identical regardless of the editing screen");
        assertEquals(arrayA, editOasB.isRowArrayIndicatorChecked(persistedB),
                "Array indicator must be identical regardless of the editing screen");
        assertEquals(opIdA, editOasB.getRowOperationId(persistedB),
                "Operation ID must be identical regardless of the editing screen");
        assertEquals(sharedOperationId, opIdA);

        // Requirement 4: both generated documents carry the identical operation.
        OpenAPIDocumentExport exportA = OpenAPIDocumentExport.from(openOasEditor(homePage, docOas).clickGenerateAndDownload());
        OpenAPIDocumentExport exportB = OpenAPIDocumentExport.from(openOasEditor(homePage, docBie).clickGenerateAndDownload());
        assertTrue(exportA.operationIds().contains(sharedOperationId));
        assertTrue(exportB.operationIds().contains(sharedOperationId),
                "The BIE-root edit must generate the identical operation as the OpenAPI-screen edit");
    }

    /* =============================== Requirement 4 + non-regression of #1347 / #1492 via the BIE root =============================== */

    @Test
    @DisplayName("TC_43_14_9")
    public void error_response_body_type_set_from_the_bie_root_generates_the_error_matrix() {
        // Issue #1347 remains intact when driven from the BIE root.
        Ctx ctx = newContext();
        TopLevelASBIEPObject bie = createBie(ctx, "oas_1519_9_bc", "WIP");
        OpenAPIDocumentObject doc = createDoc(ctx);

        HomePage homePage = signIn(ctx.endUser);
        EditOpenAPIDocumentPage editOasPage = openOasEditor(homePage, doc);
        assignBie(editOasPage, bie, "GET", "Response");

        EditBIEPage editBIEPage = openBieEditor(homePage, bie);
        EditBIEPage.OpenAPIDocumentInformationPanel panel = editBIEPage.openOpenAPIDocumentInformationPanel();
        WebElement card = panel.getBindingCard(doc.getOasDocId());
        panel.setErrorResponseBodyType(card, "IETF Problem Details");
        assertEquals("IETF Problem Details", panel.getErrorResponseBodyType(card));
        panel.hitUpdateButton();

        editOasPage = openOasEditor(homePage, doc);
        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(editOasPage.clickGenerateAndDownload());
        assertTrue(export.hasComponentResponse("500_InternalServerError"),
                "Setting IETF Problem Details from the BIE root should emit the reusable 500 error response (Issue #1347)");
        // The defaulted error matrix serves Problem Details via the application/problem+json media type.
        String ref = export.componentResponseContentSchemaRef("500_InternalServerError", "application/problem+json");
        assertNotNull(ref);
        assertTrue(ref.contains("ProblemDetails"),
                "The 500 error response should reference the shared ProblemDetails schema (Issue #1347)");
    }

    @Test
    @DisplayName("TC_43_14_10")
    public void duplicate_request_response_slot_edited_from_the_bie_root_is_blocked() {
        // Issue #1492 remains intact when driven from the BIE root: one (Resource Name, Verb) can own at most
        // one Request and one Response body, so driving a second binding into an existing slot is blocked.
        Ctx ctx = newContext();
        TopLevelASBIEPObject bie = createBie(ctx, "oas_1519_10_bc", "WIP");
        OpenAPIDocumentObject doc = createDoc(ctx);

        HomePage homePage = signIn(ctx.endUser);
        EditOpenAPIDocumentPage editOasPage = openOasEditor(homePage, doc);
        assignBie(editOasPage, bie, "GET", "Response");
        assignBie(editOasPage, bie, "POST", "Response");

        EditBIEPage editBIEPage = openBieEditor(homePage, bie);
        EditBIEPage.OpenAPIDocumentInformationPanel panel = editBIEPage.openOpenAPIDocumentInformationPanel();
        assertEquals(2, panel.getBindingCardCount());

        // Drive the POST card's verb to GET so both bindings collide on (Resource Name, GET, Response).
        WebElement postCard = panel.getBindingCardByOperationId(
                expectedOperationId("POST", bie.getPropertyTerm(), false));
        panel.setVerb(postCard, "GET");

        assertEquals("Each (Resource Name, Verb) can have only one Request and one Response body.",
                panel.getResourceNameError(postCard),
                "Driving a binding into an occupied body slot from the BIE root should flag the duplicate (Issue #1492)");
        assertFalse(panel.isUpdateButtonEnabled(),
                "The 'Update OpenAPI Information' button must be disabled while a duplicate body slot is present (Issue #1492)");
    }

    /* ===================================== Requirement 4 + #1610 DELETE-body warning ===================================== */

    @Test
    @DisplayName("TC_43_14_11")
    public void delete_request_body_ignored_warning_surfaces_on_the_bie_root_for_a_3_0_document() {
        // Issue #1610 parity: the BIE-root panel warns (read-only), per card, that a DELETE Request body is
        // ignored when the binding's OpenAPI Document targets OpenAPI 3.0.x — and clears it once the document
        // is switched to 3.1.1 on the OpenAPI Document screen (the BIE screen never edits the OpenAPI Version).
        Ctx ctx = newContext();
        TopLevelASBIEPObject bie = createBie(ctx, "oas_1519_11_bc", "WIP");
        OpenAPIDocumentObject doc = createDoc(ctx);   // created at OpenAPI Version 3.0.3

        HomePage homePage = signIn(ctx.endUser);
        EditOpenAPIDocumentPage editOasPage = openOasEditor(homePage, doc);
        assignBie(editOasPage, bie, "DELETE", "Request");

        // 3.0.3 + DELETE + Request -> the per-card warning surfaces on the BIE root.
        EditBIEPage editBIEPage = openBieEditor(homePage, bie);
        EditBIEPage.OpenAPIDocumentInformationPanel panel = editBIEPage.openOpenAPIDocumentInformationPanel();
        WebElement card = panel.getBindingCard(doc.getOasDocId());
        assertTrue(panel.isDeleteRequestBodyIgnoredWarningDisplayed(card),
                "A 3.0.3 document with a DELETE + Request binding must warn on the BIE root that the body is ignored");

        // Switch the document to OpenAPI 3.1.1 on the OpenAPI Document screen -> the body is honored, warning clears.
        editOasPage = openOasEditor(homePage, doc);
        editOasPage.setOpenAPIVersion("3.1.1");
        editOasPage.hitUpdateButton();

        editBIEPage = openBieEditor(homePage, bie);
        panel = editBIEPage.openOpenAPIDocumentInformationPanel();
        card = panel.getBindingCard(doc.getOasDocId());
        assertFalse(panel.isDeleteRequestBodyIgnoredWarningDisplayed(card),
                "Switching the document to OpenAPI 3.1.1 must clear the BIE-root DELETE-body warning");
    }

    /* ===================================== Requirement #1492: add-time duplicate-body guard ===================================== */

    @Test
    @DisplayName("TC_43_14_12")
    public void adding_a_duplicate_body_slot_from_the_bie_root_add_dialog_is_blocked() {
        // Issue #1492 parity on the BIE-root Add dialog: a chosen (Verb, Message Body) that would duplicate a
        // body the BIE already has on the selected document is flagged inline and disables Add — the same
        // client-side guard the OpenAPI Document editor's Add dialog applies (the backend enforces it too).
        Ctx ctx = newContext();
        TopLevelASBIEPObject bie = createBie(ctx, "oas_1519_12_bc", "WIP");
        OpenAPIDocumentObject doc = createDoc(ctx);

        HomePage homePage = signIn(ctx.endUser);
        EditOpenAPIDocumentPage editOasPage = openOasEditor(homePage, doc);
        assignBie(editOasPage, bie, "GET", "Response");

        EditBIEPage editBIEPage = openBieEditor(homePage, bie);
        EditBIEPage.OpenAPIDocumentInformationPanel panel = editBIEPage.openOpenAPIDocumentInformationPanel();
        BieOpenAPIDocumentAddDialog dialog = panel.openAddDialog();
        dialog.selectOpenAPIDocument(doc.getTitle());
        dialog.setVerb("GET");
        dialog.setMessageBody("Response");

        // GET + Response already exists on this document -> the dialog flags the duplicate and disables Add.
        assertEquals("This endpoint already has a Response body.", dialog.getDuplicateBodyError(),
                "Re-adding an existing (Verb, Message Body) from the BIE root must flag the duplicate (Issue #1492)");
        assertFalse(dialog.getAddButton(false).isEnabled(),
                "The 'Add' button must be disabled while the selection duplicates an existing body slot (Issue #1492)");

        // Switching to a non-colliding operation (POST + Response) clears the error and re-enables Add.
        dialog.setVerb("POST");
        assertEquals("", dialog.getDuplicateBodyError(),
                "A non-duplicate (Verb, Message Body) must clear the inline duplicate error");
        assertTrue(dialog.getAddButton(false).isEnabled(),
                "The 'Add' button must re-enable once the selection no longer duplicates an existing body slot");
    }

    /* ================================================== helpers ================================================== */

    private static String expectedOperationId(String verb, String propertyTerm, boolean array) {
        String word;
        switch (verb) {
            case "GET": word = "query"; break;
            case "POST": word = "create"; break;
            case "PUT": word = "replace"; break;
            case "PATCH": word = "update"; break;
            case "DELETE": word = "delete"; break;
            default: word = "";
        }
        String name = propertyTerm.replaceAll("\\s", "");
        if (!name.isEmpty()) {
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
        return word + name + (array ? "List" : "");
    }

    private Ctx newContext() {
        Ctx ctx = new Ctx();
        ctx.endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(ctx.endUser);
        ctx.library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ctx.release = getAPIFactory().getReleaseAPI().getTheLatestRelease(ctx.library);
        ctx.namespace = getAPIFactory().getNamespaceAPI()
                .getNamespaceByURI(ctx.library, "http://www.openapplications.org/oagis/10");
        return ctx;
    }

    private TopLevelASBIEPObject createBie(Ctx ctx, String businessContextPrefix, String state) {
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI()
                .createRandomBusinessContext(ctx.endUser, businessContextPrefix);
        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        ACCObject acc = coreComponentAPI.createRandomACC(ctx.endUser, ctx.release, ctx.namespace, "Published");
        ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, ctx.endUser, ctx.namespace, "Published");
        return getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, ctx.endUser, state);
    }

    private OpenAPIDocumentObject createDoc(Ctx ctx) {
        return getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(ctx.endUser);
    }

    private HomePage signIn(AppUserObject endUser) {
        return loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
    }

    private EditOpenAPIDocumentPage openOasEditor(HomePage homePage, OpenAPIDocumentObject doc) {
        return homePage.getBIEMenu().openOpenAPIDocumentSubMenu().openEditOpenAPIDocumentPage(doc);
    }

    private EditBIEPage openBieEditor(HomePage homePage, TopLevelASBIEPObject bie) {
        return homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(bie);
    }

    private void assignBie(EditOpenAPIDocumentPage editOasPage, TopLevelASBIEPObject bie,
                           String verb, String messageBody) {
        AddBIEForOpenAPIDocumentDialog dialog = editOasPage.openAddBIEForOpenAPIDocumentDialog();
        sendKeys(dialog.getInputFieldInSearchBar(), bie.getDen());
        dialog.hitSearchButton();
        WebElement row = dialog.getTableRecordByValue(bie.getDen());
        dialog.toggleSelect(row);
        dialog.setVerb(row, verb);
        dialog.setMessageBody(row, messageBody);
        dialog.hitAddButton();
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        this.randomAccounts.forEach(randomAccount ->
                getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId()));
    }

    private static class Ctx {
        private AppUserObject endUser;
        private LibraryObject library;
        private ReleaseObject release;
        private NamespaceObject namespace;
    }
}
