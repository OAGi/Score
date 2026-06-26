package org.oagi.score.e2e.TS_43_OpenAPIDocument;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.ASCCPObject;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.LibraryObject;
import org.oagi.score.e2e.obj.OpenAPIDocumentObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.oas.AddOperationForOpenAPIDocumentDialog;
import org.oagi.score.e2e.page.oas.EditOpenAPIDocumentPage;
import org.oagi.score.e2e.page.oas.OasDocConfirmMessageDialog;
import org.oagi.score.e2e.page.oas.OpenAPIDocumentExport;
import org.oagi.score.e2e.page.oas.OpenAPIDocumentPage;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.getText;

/**
 * Test Case 43.11 - Configure the per-operation Error Response Body Type (Issue #1347).
 * <p>
 * Each operation on the Edit OpenAPI Document page carries an inline Error Response selector
 * ({@code No Response Body} | {@code IETF Problem Details} | {@code OAGi Confirm Message}) that controls
 * the body of the defaulted 4xx/5xx error responses. {@code OAGi Confirm Message} opens a dedicated BIE-selection dialog to
 * pick the ConfirmMessage BIE (modeled on the 'Include Meta Header' picker). The generated OpenAPI
 * document emits the matrix of error responses as: description-only (NONE), a {@code $ref} to a reusable
 * RFC&nbsp;9457 ProblemDetails response (PROBLEM_DETAILS), or an {@code application/json} body referencing
 * the picked ConfirmMessage BIE schema (CONFIRM_MESSAGE).
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_43_11_ConfigureErrorResponseBodyType extends BaseTest {

    // The DEN of the standard 'Confirm Message' component. The picker is locked to this DEN, so the test
    // profiles this very ASCCP into a BIE (rather than a random one) for the candidate to appear.
    private static final String CONFIRM_MESSAGE_DEN = "Confirm Message. Confirm Message";

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_43_11_1")
    public void selector_offers_three_options_and_defaults_to_none() {
        EditOpenAPIDocumentPage editPage = newDocument();
        addBodylessOperation(editPage, "DELETE", "/alpha", "createAlpha");

        WebElement row = editPage.getTableRecordAtIndex(1);
        assertTrue(editPage.isRowErrorResponseSelectorEnabled(row),
                "The Error Response selector stays enabled even for a bodyless operation");
        assertEquals("No Response Body", editPage.getRowErrorResponseBodyType(row),
                "An operation defaults to the 'No Response Body' error-response body");
        assertFalse(editPage.isRowConfirmMessageChipDisplayed(row),
                "No ConfirmMessage chip is shown while the body type is No Response Body");
    }

    @Test
    @DisplayName("TC_43_11_2")
    public void selecting_problem_details_commits_without_opening_a_dialog() {
        EditOpenAPIDocumentPage editPage = newDocument();
        addBodylessOperation(editPage, "DELETE", "/beta", "createBeta");

        WebElement row = editPage.getTableRecordAtIndex(1);
        editPage.setRowErrorResponseBodyType(row, "IETF Problem Details");

        assertEquals("IETF Problem Details", editPage.getRowErrorResponseBodyType(row),
                "IETF Problem Details should commit straight from the selector");
        assertFalse(editPage.isRowConfirmMessageChipDisplayed(editPage.getTableRecordAtIndex(1)),
                "IETF Problem Details shows no ConfirmMessage chip");
    }

    @Test
    @DisplayName("TC_43_11_3")
    public void selecting_confirm_message_opens_picker_and_select_is_gated_until_a_bie_is_chosen() {
        ConfirmFixture fixture = newDocumentWithConfirmBie();
        EditOpenAPIDocumentPage editPage = fixture.editPage;
        addBodylessOperation(editPage, "DELETE", "/gamma", "createGamma");

        WebElement row = editPage.getTableRecordAtIndex(1);
        OasDocConfirmMessageDialog dialog = editPage.openConfirmMessageDialogViaSelector(row);
        assertEquals("Select ConfirmMessage BIE", getText(dialog.getTitle()),
                "The ConfirmMessage selection dialog should open");
        assertFalse(dialog.isSelectEnabled(), "Select is disabled until a BIE is chosen");

        // The picker is locked to 'Confirm Message. Confirm Message' in the connected BIE's release; the
        // candidate is identified by its (unique) Business Context.
        assertTrue(dialog.isCandidatePresent(fixture.confirmBie.getDen()),
                "A Confirm Message BIE should be listed");
        assertTrue(dialog.isCandidatePresentByBusinessContext(fixture.businessContext.getName()),
                "The published ConfirmMessage BIE should appear in the picker");
        dialog.selectCandidateByBusinessContext(fixture.businessContext.getName());
        assertTrue(dialog.isSelectEnabled(), "Select should enable once a BIE is selected");
        dialog.cancel();
    }

    @Test
    @DisplayName("TC_43_11_4")
    public void picking_a_confirm_message_bie_shows_the_den_chip_and_persists() {
        ConfirmFixture fixture = newDocumentWithConfirmBie();
        EditOpenAPIDocumentPage editPage = fixture.editPage;
        addBodylessOperation(editPage, "DELETE", "/delta", "createDelta");

        WebElement row = editPage.getTableRecordAtIndex(1);
        pickConfirmMessageViaSelector(editPage, row, fixture.businessContext);

        row = editPage.getTableRecordAtIndex(1);
        assertEquals("OAGi Confirm Message", editPage.getRowErrorResponseBodyType(row));
        assertTrue(editPage.isRowConfirmMessageChipDisplayed(row), "The DEN chip should be shown");
        assertTrue(editPage.getRowConfirmMessageChipText(row).contains(fixture.confirmBie.getDen()),
                "The chip should carry the picked DEN (was: " + editPage.getRowConfirmMessageChipText(row) + ")");

        editPage.hitUpdateButton();
        // Reopen the document and verify the body type + picked BIE survived the round-trip.
        editPage.openPage();
        WebElement reopened = editPage.getTableRecordAtIndex(1);
        assertEquals("OAGi Confirm Message", editPage.getRowErrorResponseBodyType(reopened),
                "The ConfirmMessage body type should persist across Update + reopen");
        assertTrue(editPage.getRowConfirmMessageChipText(reopened).contains(fixture.confirmBie.getDen()),
                "The picked ConfirmMessage BIE should persist across Update + reopen");
    }

    @Test
    @DisplayName("TC_43_11_5")
    public void cancelling_the_picker_with_no_prior_bie_reverts_to_the_previous_type() {
        EditOpenAPIDocumentPage editPage = newDocument();
        addBodylessOperation(editPage, "DELETE", "/epsilon", "createEpsilon");

        WebElement row = editPage.getTableRecordAtIndex(1);
        editPage.setRowErrorResponseBodyType(row, "IETF Problem Details");

        // Open the ConfirmMessage picker and cancel it without picking a BIE.
        OasDocConfirmMessageDialog dialog = editPage.openConfirmMessageDialogViaSelector(editPage.getTableRecordAtIndex(1));
        dialog.cancel();

        WebElement reverted = editPage.getTableRecordAtIndex(1);
        assertEquals("IETF Problem Details", editPage.getRowErrorResponseBodyType(reverted),
                "Cancelling without a BIE should revert the selector to the previously committed type");
        assertFalse(editPage.isRowConfirmMessageChipDisplayed(reverted),
                "No ConfirmMessage chip should remain after the revert");
    }

    @Test
    @DisplayName("TC_43_11_6")
    public void re_picking_via_the_chip_replaces_the_bie() {
        ConfirmFixture fixture = newDocumentWithConfirmBie();
        EditOpenAPIDocumentPage editPage = fixture.editPage;
        // A second Confirm Message BIE under a different Business Context (the candidates share the locked
        // DEN, so the Business Context is what tells them apart).
        BusinessContextObject secondBusinessContext = getAPIFactory().getBusinessContextAPI()
                .createRandomBusinessContext(fixture.endUser, "oas_confirm_bc2");
        TopLevelASBIEPObject secondBie = createPublishedConfirmBie(fixture, secondBusinessContext);
        addBodylessOperation(editPage, "DELETE", "/zeta", "createZeta");

        WebElement row = editPage.getTableRecordAtIndex(1);
        pickConfirmMessageViaSelector(editPage, row, fixture.businessContext);
        assertTrue(editPage.getRowConfirmMessageChipText(editPage.getTableRecordAtIndex(1))
                        .contains(fixture.confirmBie.getDen()),
                "The chip should first carry the original BIE");

        // Re-pick the second BIE (different Business Context) from the chip.
        OasDocConfirmMessageDialog dialog = editPage.openConfirmMessageDialogViaChip(editPage.getTableRecordAtIndex(1));
        assertTrue(dialog.isCandidatePresentByBusinessContext(secondBusinessContext.getName()),
                "The replacement BIE should be listed");
        dialog.selectCandidateByBusinessContext(secondBusinessContext.getName());
        dialog.hitSelect();

        WebElement updated = editPage.getTableRecordAtIndex(1);
        assertEquals("OAGi Confirm Message", editPage.getRowErrorResponseBodyType(updated), "Re-pick keeps the body type");
        // Both BIEs share the locked DEN, so the chip text alone cannot distinguish them; the re-pick is
        // exercised by selecting the second Business Context's row above.
        assertTrue(editPage.getRowConfirmMessageChipText(updated).contains(secondBie.getDen()),
                "The chip should carry the Confirm Message DEN (was: "
                        + editPage.getRowConfirmMessageChipText(updated) + ")");
    }

    @Test
    @DisplayName("TC_43_11_7")
    public void cancelling_the_chip_picker_keeps_the_existing_bie() {
        ConfirmFixture fixture = newDocumentWithConfirmBie();
        EditOpenAPIDocumentPage editPage = fixture.editPage;
        addBodylessOperation(editPage, "DELETE", "/eta", "createEta");

        WebElement row = editPage.getTableRecordAtIndex(1);
        pickConfirmMessageViaSelector(editPage, row, fixture.businessContext);

        OasDocConfirmMessageDialog dialog = editPage.openConfirmMessageDialogViaChip(editPage.getTableRecordAtIndex(1));
        dialog.cancel();

        WebElement kept = editPage.getTableRecordAtIndex(1);
        assertEquals("OAGi Confirm Message", editPage.getRowErrorResponseBodyType(kept),
                "Cancelling the chip picker keeps ConfirmMessage");
        assertTrue(editPage.getRowConfirmMessageChipText(kept).contains(fixture.confirmBie.getDen()),
                "Cancelling the chip picker keeps the existing BIE");
    }

    @Test
    @DisplayName("TC_43_11_8")
    public void switching_back_to_none_clears_the_confirm_message_bie() {
        ConfirmFixture fixture = newDocumentWithConfirmBie();
        EditOpenAPIDocumentPage editPage = fixture.editPage;
        addBodylessOperation(editPage, "DELETE", "/theta", "createTheta");

        WebElement row = editPage.getTableRecordAtIndex(1);
        pickConfirmMessageViaSelector(editPage, row, fixture.businessContext);

        editPage.setRowErrorResponseBodyType(editPage.getTableRecordAtIndex(1), "No Response Body");
        WebElement cleared = editPage.getTableRecordAtIndex(1);
        assertEquals("No Response Body", editPage.getRowErrorResponseBodyType(cleared));
        assertFalse(editPage.isRowConfirmMessageChipDisplayed(cleared),
                "Switching back to No Response Body should drop the ConfirmMessage chip/BIE");
    }

    @Test
    @DisplayName("TC_43_11_9")
    public void generated_error_responses_reflect_each_body_type() {
        ConfirmFixture fixture = newDocumentWithConfirmBie();
        EditOpenAPIDocumentPage editPage = fixture.editPage;
        addBodylessOperation(editPage, "DELETE", "/err-none", "createErrNone");
        addBodylessOperation(editPage, "DELETE", "/err-pd", "createErrPd");
        addBodylessOperation(editPage, "DELETE", "/err-cm", "createErrCm");

        // Read each row's resource name (= generated path key) and assign a body type to it.
        String pathNone = editPage.getRowResourceName(editPage.getTableRecordAtIndex(1));
        String pathPd = editPage.getRowResourceName(editPage.getTableRecordAtIndex(2));
        String pathCm = editPage.getRowResourceName(editPage.getTableRecordAtIndex(3));

        editPage.setRowErrorResponseBodyType(editPage.getTableRecordAtIndex(2), "IETF Problem Details");
        pickConfirmMessageViaSelector(editPage, editPage.getTableRecordAtIndex(3), fixture.businessContext);

        editPage.hitUpdateButton();
        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(editPage.clickGenerateAndDownload());

        // 500 (Internal Server Error) is in the matrix for every verb and is array-independent.
        // NONE: description only, no body, not a $ref.
        assertNotNull(export.operation(pathNone, "delete"), "The NONE operation should be generated");
        assertTrue(export.operationHasResponse(pathNone, "delete", "500"),
                "The NONE operation should emit a defaulted 500 response");
        assertEquals("Internal Server Error", export.responseDescription(pathNone, "delete", "500"));
        assertFalse(export.responseHasContent(pathNone, "delete", "500"),
                "A NONE error response is description-only (no content body)");
        assertEquals(null, export.responseRef(pathNone, "delete", "500"),
                "A NONE error response is inline, not a $ref");

        // PROBLEM_DETAILS: $ref to a reusable components.responses entry + ProblemDetails schema.
        assertEquals("#/components/responses/500_InternalServerError",
                export.responseRef(pathPd, "delete", "500"),
                "A PROBLEM_DETAILS error response should $ref the reusable response component");
        assertTrue(export.hasComponentResponse("500_InternalServerError"),
                "The reusable 500 response component should be emitted");
        assertEquals("#/components/schemas/ProblemDetails",
                export.componentResponseContentSchemaRef("500_InternalServerError", "application/problem+json"),
                "The ProblemDetails response component should reference the ProblemDetails schema via application/problem+json");
        assertTrue(export.hasSchema("ProblemDetails"),
                "The shared RFC 9457 ProblemDetails schema should be emitted");

        // CONFIRM_MESSAGE: application/json body referencing the picked ConfirmMessage BIE schema.
        assertTrue(export.responseHasContent(pathCm, "delete", "500"),
                "A CONFIRM_MESSAGE error response should carry a body");
        assertTrue(export.responseMediaTypes(pathCm, "delete", "500").contains("application/json"),
                "A CONFIRM_MESSAGE error response should be application/json (were: "
                        + export.responseMediaTypes(pathCm, "delete", "500") + ")");
        String cmRef = export.responseContentSchemaRef(pathCm, "delete", "500", "application/json");
        assertNotNull(cmRef, "The CONFIRM_MESSAGE response should reference a schema");
        assertTrue(cmRef.startsWith("#/components/schemas/"),
                "The CONFIRM_MESSAGE response should reference a components.schemas entry (was: " + cmRef + ")");
        String cmSchemaName = cmRef.substring(cmRef.lastIndexOf('/') + 1);
        assertTrue(export.hasSchema(cmSchemaName),
                "The referenced ConfirmMessage BIE schema should be materialized in components.schemas");
    }

    @Test
    @DisplayName("TC_43_11_10")
    public void error_responses_render_identically_in_a_3_0_3_document_none_desc_only_problem_details_ref_confirm_message_app_json() {
        ConfirmFixture fixture = newDocumentWithConfirmBie();
        EditOpenAPIDocumentPage editPage = fixture.editPage;

        addBodylessOperation(editPage, "DELETE", "/err-none-303", "createErrNone303");
        addBodylessOperation(editPage, "DELETE", "/err-pd-303", "createErrPd303");
        addBodylessOperation(editPage, "DELETE", "/err-cm-303", "createErrCm303");

        // Read each row's resource name (= generated path key) and assign a body type to it.
        String pathNone = editPage.getRowResourceName(editPage.getTableRecordAtIndex(1));
        String pathPd = editPage.getRowResourceName(editPage.getTableRecordAtIndex(2));
        String pathCm = editPage.getRowResourceName(editPage.getTableRecordAtIndex(3));

        editPage.setRowErrorResponseBodyType(editPage.getTableRecordAtIndex(2), "IETF Problem Details");
        pickConfirmMessageViaSelector(editPage, editPage.getTableRecordAtIndex(3), fixture.businessContext);

        // Target OpenAPI 3.0.3 (the document defaults to 3.1.1) and persist version + operations + body
        // types together in one save. (Setting the version and updating on a freshly-loaded empty document,
        // before any operation exists, is timing-fragile; the passing cases change the version on a warmed
        // page that already has content.)
        editPage.setOpenAPIVersion("3.0.3");
        editPage.hitUpdateButton();
        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(editPage.clickGenerateAndDownload());

        // The generated document targets OpenAPI 3.0.3.
        assertEquals("3.0.3", export.raw().get("openapi"),
                "The generated document should target OpenAPI 3.0.3");

        // 500 (Internal Server Error) is in the matrix for every verb and is array-independent, so it is
        // the version-independent anchor for each body type.
        // NONE: description only, no body, not a $ref.
        assertNotNull(export.operation(pathNone, "delete"), "The NONE operation should be generated");
        assertTrue(export.operationHasResponse(pathNone, "delete", "500"),
                "The NONE operation should emit a defaulted 500 response");
        assertFalse(export.responseHasContent(pathNone, "delete", "500"),
                "A NONE error response is description-only (no content body)");
        assertEquals(null, export.responseRef(pathNone, "delete", "500"),
                "A NONE error response is inline, not a $ref");
        assertEquals("Internal Server Error", export.responseDescription(pathNone, "delete", "500"));

        // PROBLEM_DETAILS: $ref to a reusable components.responses entry + ProblemDetails schema (the
        // ProblemDetails body type renders identically in 3.0.3 and 3.1.1).
        assertEquals("#/components/responses/500_InternalServerError",
                export.responseRef(pathPd, "delete", "500"),
                "A PROBLEM_DETAILS error response should $ref the reusable response component");
        assertEquals("#/components/schemas/ProblemDetails",
                export.componentResponseContentSchemaRef("500_InternalServerError", "application/problem+json"),
                "The ProblemDetails response component should reference the ProblemDetails schema via application/problem+json");
        assertTrue(export.hasSchema("ProblemDetails"),
                "The shared RFC 9457 ProblemDetails schema should be emitted");

        // CONFIRM_MESSAGE: application/json body referencing the picked ConfirmMessage BIE schema.
        assertTrue(export.responseMediaTypes(pathCm, "delete", "500").contains("application/json"),
                "A CONFIRM_MESSAGE error response should be application/json (were: "
                        + export.responseMediaTypes(pathCm, "delete", "500") + ")");
        String cmRef = export.responseContentSchemaRef(pathCm, "delete", "500", "application/json");
        assertNotNull(cmRef, "The CONFIRM_MESSAGE response should reference a schema");
        assertTrue(cmRef.startsWith("#/components/schemas/"),
                "The CONFIRM_MESSAGE response should reference a components.schemas entry (was: " + cmRef + ")");
        String cmSchemaName = cmRef.substring(cmRef.lastIndexOf('/') + 1);
        assertTrue(export.hasSchema(cmSchemaName),
                "The referenced ConfirmMessage BIE schema should be materialized in components.schemas");
    }

    @Test
    @DisplayName("TC_43_11_11")
    public void switch_to_no_response_body_then_update_and_reopen_clears_confirm_message_fk() {
        ConfirmFixture fixture = newDocumentWithConfirmBie();
        EditOpenAPIDocumentPage editPage = fixture.editPage;
        addBodylessOperation(editPage, "DELETE", "/theta-persist", "createThetaPersist");

        // Pick a ConfirmMessage BIE on the row and SAVE it, so there is a stored FK to clear. (Adding the
        // bodyless operation persists immediately and leaves nothing to update; picking then reverting the
        // body type without an intermediate save would net to no change and disable Update.)
        WebElement row = editPage.getTableRecordAtIndex(1);
        pickConfirmMessageViaSelector(editPage, row, fixture.businessContext);
        editPage.hitUpdateButton();

        // Switch the row back to 'No Response Body' and save again, then reopen the document.
        editPage.setRowErrorResponseBodyType(editPage.getTableRecordAtIndex(1), "No Response Body");
        editPage.hitUpdateButton();
        editPage.openPage();

        // The stored ConfirmMessage FK must be cleared (not merely hidden) by switching to No Response Body.
        WebElement reopened = editPage.getTableRecordAtIndex(1);
        assertEquals("No Response Body", editPage.getRowErrorResponseBodyType(reopened),
                "Switching to No Response Body should persist across Update + reopen");
        assertFalse(editPage.isRowConfirmMessageChipDisplayed(reopened),
                "Reopening should show no ConfirmMessage chip, proving the stored FK was cleared");
    }

    /* ------------------------------------------------------------------ helpers */

    private void addBodylessOperation(EditOpenAPIDocumentPage editPage, String verb,
                                      String resourceName, String operationId) {
        AddOperationForOpenAPIDocumentDialog dialog = editPage.openAddOperationDialog();
        dialog.setVerb(verb);
        dialog.setResourceName(resourceName);
        if (operationId != null) {
            dialog.setOperationId(operationId);
        }
        dialog.hitAddButton();
    }

    private void pickConfirmMessageViaSelector(EditOpenAPIDocumentPage editPage, WebElement row,
                                               BusinessContextObject businessContext) {
        OasDocConfirmMessageDialog dialog = editPage.openConfirmMessageDialogViaSelector(row);
        dialog.selectCandidateByBusinessContext(businessContext.getName());
        dialog.hitSelect();
    }

    private EditOpenAPIDocumentPage newDocument() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        OpenAPIDocumentObject openAPIDocument =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);
        return openEditOpenAPIDocumentPage(endUser, openAPIDocument);
    }

    private ConfirmFixture newDocumentWithConfirmBie() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        BusinessContextObject businessContext = getAPIFactory().getBusinessContextAPI()
                .createRandomBusinessContext(endUser, "oas_confirm_bc");

        ConfirmFixture fixture = new ConfirmFixture();
        fixture.endUser = endUser;
        fixture.library = library;
        fixture.releaseNum = release.getReleaseNumber();
        fixture.businessContext = businessContext;
        fixture.confirmBie = createPublishedConfirmBie(fixture, businessContext);

        OpenAPIDocumentObject openAPIDocument =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);
        fixture.editPage = openEditOpenAPIDocumentPage(endUser, openAPIDocument);
        return fixture;
    }

    // Profile the standard 'Confirm Message' ASCCP into a BIE (under the given Business Context) so it
    // carries the DEN the locked picker filters on; the Business Context makes it uniquely selectable.
    private TopLevelASBIEPObject createPublishedConfirmBie(ConfirmFixture fixture, BusinessContextObject businessContext) {
        ASCCPObject confirmAsccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(fixture.library, CONFIRM_MESSAGE_DEN, fixture.releaseNum);
        return getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContext), confirmAsccp, fixture.endUser, "WIP");
    }

    private EditOpenAPIDocumentPage openEditOpenAPIDocumentPage(AppUserObject endUser,
                                                               OpenAPIDocumentObject openAPIDocument) {
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        OpenAPIDocumentPage openAPIDocumentPage = bieMenu.openOpenAPIDocumentSubMenu();
        return openAPIDocumentPage.openEditOpenAPIDocumentPage(openAPIDocument);
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        this.randomAccounts.forEach(randomAccount -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId());
        });
    }

    private static class ConfirmFixture {
        private EditOpenAPIDocumentPage editPage;
        private AppUserObject endUser;
        private LibraryObject library;
        private String releaseNum;
        private BusinessContextObject businessContext;
        private TopLevelASBIEPObject confirmBie;
    }
}
