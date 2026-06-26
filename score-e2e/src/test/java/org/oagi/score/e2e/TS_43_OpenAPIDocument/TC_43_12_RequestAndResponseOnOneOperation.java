package org.oagi.score.e2e.TS_43_OpenAPIDocument;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.menu.BIEMenu;
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
import org.oagi.score.e2e.page.oas.AddBIEForOpenAPIDocumentDialog;
import org.oagi.score.e2e.page.oas.AddOperationForOpenAPIDocumentDialog;
import org.oagi.score.e2e.page.oas.EditOpenAPIDocumentPage;
import org.oagi.score.e2e.page.oas.OasDocConfirmMessageDialog;
import org.oagi.score.e2e.page.oas.OpenAPIDocumentExport;
import org.oagi.score.e2e.page.oas.OpenAPIDocumentPage;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.getSnackBarMessage;
import static org.oagi.score.e2e.impl.PageHelper.sendKeys;

/**
 * Test Case 43.12 - Request and Response on a Single Operation (Issue #1492, Option 2).
 * <p>
 * One {@code oas_operation} owns at most one Request body and at most one Response body for a single
 * {@code (path, verb)} endpoint. Adding a Request BIE and then a Response BIE on the same {@code (path,
 * verb)} now find-or-creates ONE operation, surfaced as two grid rows that share one Operation ID. That
 * legitimate Request+Response pair must generate one path-item carrying BOTH a {@code requestBody} and a
 * body-bearing 2xx response, and must NOT be rejected by the collision validator. A <em>true</em>
 * duplicate — a second body of the SAME type on the same {@code (path, verb)} — is blocked at Add time
 * (dialog mat-error + disabled Add) and at Update time (snackbar), producing no second body and no
 * download. The recently-committed DELETE+Request (#1610) and per-operation Error Response Body Type
 * (#1347) features must all remain valid for such an operation, and removing one body of a two-body
 * operation must leave the sibling body intact (the critical H1 delete-path regression). Flipping an
 * existing row's Message Body (Request&lt;-&gt;Response) must CONVERT that body in place — replacing the
 * old body, not adding a second one that re-fetches as a duplicate row.
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_43_12_RequestAndResponseOnOneOperation extends BaseTest {

    // The DEN of the standard 'Confirm Message' component. The ConfirmMessage picker is locked to this DEN,
    // so the ConfirmMessage test profiles this very ASCCP into a BIE (rather than a random one).
    private static final String CONFIRM_MESSAGE_DEN = "Confirm Message. Confirm Message";

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
    @DisplayName("TC_43_12_1")
    public void a_request_and_a_response_on_one_path_verb_share_one_operation() {
        // Add a Request body then a Response body for the SAME BIE on the SAME (path, verb) = POST. Under
        // Option 2 the second Add find-or-creates the SAME operation, so the grid shows two rows that share
        // one Operation ID (the operationId is generated from the verb + BIE name; the second add does not
        // overwrite it).
        Fixture fixture = newDocumentWithBie();
        assignBie(fixture.editPage, fixture.bie, "POST", "Request");
        assignBie(fixture.editPage, fixture.bie, "POST", "Response");

        WebElement requestRow = rowByDenAndMessageBody(fixture.editPage, fixture.bie.getDen(), "Request");
        WebElement responseRow = rowByDenAndMessageBody(fixture.editPage, fixture.bie.getDen(), "Response");
        assertNotNull(requestRow, "The POST + Request row should be present");
        assertNotNull(responseRow, "The POST + Response row should be present");

        String requestOperationId = fixture.editPage.getRowOperationId(requestRow);
        String responseOperationId = fixture.editPage.getRowOperationId(responseRow);
        assertFalse(requestOperationId.isEmpty(), "The Request row should carry a non-empty Operation ID");
        assertEquals(requestOperationId, responseOperationId,
                "The two bodies of one operation share a single Operation ID");

        // Neither row is flagged as a duplicate body slot: the two bodies differ (Request vs Response), so
        // they legitimately co-exist on one operation.
        assertFalse(fixture.editPage.isRowDuplicateBodyWarningDisplayed(requestRow),
                "A Request + Response pair is NOT a duplicate body slot");
        assertFalse(fixture.editPage.isRowDuplicateBodyWarningDisplayed(responseRow),
                "A Request + Response pair is NOT a duplicate body slot");
    }

    @Test
    @DisplayName("TC_43_12_2")
    public void generate_emits_one_path_item_with_both_request_body_and_a_2xx_response() {
        Fixture fixture = newDocumentWithBie();
        assignBie(fixture.editPage, fixture.bie, "POST", "Request");
        assignBie(fixture.editPage, fixture.bie, "POST", "Response");

        OpenAPIDocumentExport export =
                OpenAPIDocumentExport.from(fixture.editPage.clickGenerateAndDownload());

        String postPath = findPathForVerb(export, "post");
        assertNotNull(postPath, "A POST operation should be emitted for the Request + Response pair");

        // Exactly ONE path-item under paths[postPath][post] carries BOTH the requestBody and the response.
        assertEquals(1, countPathsWithVerb(export, "post"),
                "The Request + Response pair must collapse into exactly one POST path-item");
        assertTrue(export.operationHasRequestBody(postPath, "post"),
                "The single POST operation should carry the Request body's requestBody");
        assertTrue(export.operationResponseCodes(postPath, "post").contains("200"),
                "The single POST operation should carry the Response body in a 200 response");
        String responseRef = export.responseContentSchemaRef(postPath, "post", "200", "application/json");
        assertNotNull(responseRef, "The 200 response should reference the BIE component schema");
        assertTrue(responseRef.startsWith("#/components/schemas/"),
                "The 200 response should reference a #/components/schemas/<BIEName> schema (was: " + responseRef + ")");
        String schemaName = responseRef.substring(responseRef.lastIndexOf('/') + 1);
        assertTrue(export.hasSchema(schemaName),
                "The referenced response BIE schema should be materialized in components/schemas");
    }

    @Test
    @DisplayName("TC_43_12_3")
    public void the_legitimate_request_and_response_pair_is_not_rejected_with_a_collision_error() {
        // Regression for the OasOperationCollisionValidator twin-row branch: two grid rows sharing one
        // oasOperationId must NOT be rejected as a (path, verb) collision. Generation succeeds and produces
        // a downloadable file (clickGenerateAndDownload throws if no file is produced).
        Fixture fixture = newDocumentWithBie();
        assignBie(fixture.editPage, fixture.bie, "POST", "Request");
        assignBie(fixture.editPage, fixture.bie, "POST", "Response");

        File generated = fixture.editPage.clickGenerateAndDownload();
        assertNotNull(generated, "Generation must succeed for a legitimate Request + Response pair");
        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(generated);
        assertFalse(export.paths().isEmpty(), "The generated document should contain the POST path");
    }

    @Test
    @DisplayName("TC_43_12_4")
    public void a_duplicate_request_body_on_one_path_verb_is_blocked_at_add_time_and_update_time() {
        Fixture fixture = newDocumentWithBie();
        assignBie(fixture.editPage, fixture.bie, "POST", "Request");

        // Add-time: re-selecting the same BIE with POST + Request duplicates the existing Request body, so
        // the dialog disables Add and shows the mat-error; no second body is created.
        AddBIEForOpenAPIDocumentDialog dialog = fixture.editPage.openAddBIEForOpenAPIDocumentDialog();
        sendKeys(dialog.getInputFieldInSearchBar(), fixture.bie.getDen());
        dialog.hitSearchButton();
        WebElement candidate = dialog.getTableRecordByValue(fixture.bie.getDen());
        dialog.toggleSelect(candidate);
        dialog.setVerb(candidate, "POST");
        dialog.setMessageBody(candidate, "Request");

        assertTrue(dialog.isDuplicateEndpointWarningDisplayed(),
                "Adding a second Request body on one (path, verb) should warn that the endpoint already has one");
        assertEquals("This endpoint already has a Request body.",
                dialog.getRowMessageBodyError(dialog.getTableRecordByValue(fixture.bie.getDen())));
        assertThrows(TimeoutException.class, () -> dialog.getAddButton(true),
                "The Add button is disabled while a selected row duplicates an existing body");
        dialog.close();

        // Still exactly one Request row for the BIE; the duplicate was never added.
        assertEquals(1, rowsByDen(fixture.editPage, fixture.bie.getDen()).size(),
                "No second body should have been added");

        // Complete the first operation legitimately with a POST Response body (one operation, two bodies).
        assignBie(fixture.editPage, fixture.bie, "POST", "Response");
        String firstResourceName = fixture.editPage.getRowResourceName(
                rowByDenAndMessageBody(fixture.editPage, fixture.bie.getDen(), "Response"));

        // Update-time: add a SECOND BIE on a different (path, verb) so it does NOT collide at Add time, then
        // inline-edit its Verb + Resource Name to match the first operation's POST Response slot. The two
        // rows now share (firstResourceName, POST, Response) — a true duplicate Response body.
        assignBie(fixture.editPage, fixture.secondBie, "GET", "Response");
        WebElement secondRow = fixture.editPage.getTableRecordByValue(fixture.secondBie.getDen());
        fixture.editPage.setRowVerb(secondRow, "POST");
        secondRow = fixture.editPage.getTableRecordByValue(fixture.secondBie.getDen());
        fixture.editPage.setRowResourceName(secondRow, firstResourceName);

        WebElement editedSecondRow = fixture.editPage.getTableRecordByValue(fixture.secondBie.getDen());
        assertTrue(fixture.editPage.isRowDuplicateBodyWarningDisplayed(editedSecondRow),
                "Editing a row into an existing (path, verb, body) slot should flag a duplicate body");

        // Clicking Update is blocked: the dup-body snackbar is shown instead of "Updated", and no file is
        // produced (the test reads the snackbar rather than a download).
        fixture.editPage.clickUpdateButton();
        assertEquals("Each (Resource Name, Verb) can have only one Request and one Response body.",
                getSnackBarMessage(getDriver()),
                "The update-time guard blocks the save with the duplicate-body message");
    }

    @Test
    @DisplayName("TC_43_12_5")
    public void delete_with_request_stays_valid_and_removing_one_body_keeps_the_sibling() {
        Fixture fixture = newDocumentWithBie();

        // #1610: a DELETE may carry a Request body. In OpenAPI 3.1.1 it is honored (requestBody emitted),
        // paired with a status-only 202 success. Add both a Request and a Response on the same DELETE
        // endpoint (one operation, two bodies).
        fixture.editPage.setOpenAPIVersion("3.1.1");
        fixture.editPage.hitUpdateButton();
        assignBie(fixture.editPage, fixture.bie, "DELETE", "Request");
        assignBie(fixture.editPage, fixture.bie, "DELETE", "Response");

        OpenAPIDocumentExport export311 =
                OpenAPIDocumentExport.from(fixture.editPage.clickGenerateAndDownload());
        String deletePath = findPathForVerb(export311, "delete");
        assertNotNull(deletePath, "A DELETE operation should be emitted");
        assertEquals(1, countPathsWithVerb(export311, "delete"),
                "The DELETE Request + Response pair must collapse into exactly one DELETE path-item");
        assertTrue(export311.operationHasRequestBody(deletePath, "delete"),
                "OpenAPI 3.1.1 emits a requestBody for a DELETE + Request operation");
        assertTrue(export311.operationResponseCodes(deletePath, "delete").contains("200"),
                "The DELETE + Response body should be carried in a 200 response");

        // #1610 in 3.0.3: the DELETE request body is dropped (the amber banner appears), while the Response
        // body's 200 survives. Switch versions, save, and verify the banner + generated shape.
        fixture.editPage.setOpenAPIVersion("3.0.3");
        fixture.editPage.hitUpdateButton();
        assertTrue(fixture.editPage.isDeleteRequestBodyIgnoredWarningDisplayed(),
                "An OpenAPI 3.0.3 document drops a DELETE request body, so the amber banner is shown");
        OpenAPIDocumentExport export303 =
                OpenAPIDocumentExport.from(fixture.editPage.clickGenerateAndDownload());
        String deletePath303 = findPathForVerb(export303, "delete");
        assertNotNull(deletePath303, "A DELETE operation should still be emitted in 3.0.3");
        assertFalse(export303.operationHasRequestBody(deletePath303, "delete"),
                "OpenAPI 3.0.3 drops the request body of a DELETE + Request operation");
        assertTrue(export303.operationResponseCodes(deletePath303, "delete").contains("200"),
                "The DELETE + Response body's 200 response survives in 3.0.3");

        // H1 regression: remove ONE body (the Request row) of the two-body operation. The sibling Response
        // body must remain intact (the old delete path orphaned/FK-violated the survivor).
        fixture.editPage.setOpenAPIVersion("3.1.1");
        fixture.editPage.hitUpdateButton();
        WebElement requestRow = rowByDenAndMessageBody(fixture.editPage, fixture.bie.getDen(), "Request");
        fixture.editPage.toggleSelect(requestRow);
        fixture.editPage.removeSelectedBIEs();

        WebElement survivingResponseRow = rowByDenAndMessageBody(fixture.editPage, fixture.bie.getDen(), "Response");
        assertNotNull(survivingResponseRow, "Removing the Request body must leave the Response body intact");
        assertThrows(TimeoutException.class,
                () -> rowByDenAndMessageBody(fixture.editPage, fixture.bie.getDen(), "Request"),
                "The removed Request body should be gone");

        // Removing a body persists the delete but leaves the editor in a "changed" state, so Generate would
        // be blocked with "There are unsaved changes...". Reload the (already-persisted) document for a clean
        // state before generating.
        fixture.editPage.openPage();
        OpenAPIDocumentExport afterRemoval =
                OpenAPIDocumentExport.from(fixture.editPage.clickGenerateAndDownload());
        String deletePathAfter = findPathForVerb(afterRemoval, "delete");
        assertNotNull(deletePathAfter, "The DELETE operation (with the surviving Response body) should still generate");
        assertFalse(afterRemoval.operationHasRequestBody(deletePathAfter, "delete"),
                "After removing the Request body, the DELETE operation carries no requestBody");
        assertTrue(afterRemoval.operationResponseCodes(deletePathAfter, "delete").contains("200"),
                "The surviving Response body is still emitted as a 200 response");
    }

    @Test
    @DisplayName("TC_43_12_6")
    public void error_response_body_type_still_applies_to_a_request_and_response_operation() {
        // #1347: the Error Response Body Type lives on the operation. An operation with BOTH a Request and a
        // Response body must still emit the defaulted 4xx/5xx matrix per the selected body type.
        Fixture fixture = newDocumentWithBie();
        assignBie(fixture.editPage, fixture.bie, "POST", "Request");
        assignBie(fixture.editPage, fixture.bie, "POST", "Response");

        // Set IETF Problem Details on the operation (set on the Request row; both rows share the operation).
        WebElement requestRow = rowByDenAndMessageBody(fixture.editPage, fixture.bie.getDen(), "Request");
        fixture.editPage.setRowErrorResponseBodyType(requestRow, "IETF Problem Details");
        fixture.editPage.hitUpdateButton();

        OpenAPIDocumentExport export =
                OpenAPIDocumentExport.from(fixture.editPage.clickGenerateAndDownload());
        String postPath = findPathForVerb(export, "post");
        assertNotNull(postPath, "A POST operation should be emitted");

        // The success response (Request + Response) co-exists with the defaulted error responses.
        assertTrue(export.operationHasRequestBody(postPath, "post"),
                "The operation should still carry the Request body");
        assertTrue(export.operationResponseCodes(postPath, "post").contains("200"),
                "The operation should still carry the Response body's 200 success");

        // 500 (Internal Server Error) is in the matrix for every verb. With IETF Problem Details it is a
        // $ref to a reusable components.responses entry referencing the shared ProblemDetails schema.
        assertTrue(export.operationHasResponse(postPath, "post", "500"),
                "An operation with an Error Response Body Type still emits the defaulted 500 response");
        assertEquals("#/components/responses/500_InternalServerError",
                export.responseRef(postPath, "post", "500"),
                "A PROBLEM_DETAILS error response should $ref the reusable response component");
        assertTrue(export.hasComponentResponse("500_InternalServerError"),
                "The reusable 500 response component should be emitted");
        assertTrue(export.hasSchema("ProblemDetails"),
                "The shared RFC 9457 ProblemDetails schema should be emitted");
    }

    @Test
    @DisplayName("TC_43_12_8")
    public void error_body_type_propagates_live_to_the_sibling_row_before_save() {
        // #1347 B37: the Error Response Body Type lives on the operation, so setting it on ONE of the two
        // grid rows of a Request + Response operation must propagate LIVE to the sibling row before any
        // Update — they share one operation. (IETF Problem Details needs no BIE picker, so a random BIE is
        // sufficient.) Re-fetch the sibling AFTER every set: the row is re-rendered and the old handle goes
        // stale.
        Fixture fixture = newDocumentWithBie();
        assignBie(fixture.editPage, fixture.bie, "POST", "Request");
        assignBie(fixture.editPage, fixture.bie, "POST", "Response");

        // Set IETF Problem Details on ONLY the Request twin.
        WebElement requestRow = rowByDenAndMessageBody(fixture.editPage, fixture.bie.getDen(), "Request");
        fixture.editPage.setRowErrorResponseBodyType(requestRow, "IETF Problem Details");

        // The Response twin live-reads the same body type WITHOUT an Update (re-fetch the sibling).
        WebElement responseRow = rowByDenAndMessageBody(fixture.editPage, fixture.bie.getDen(), "Response");
        assertEquals("IETF Problem Details", fixture.editPage.getRowErrorResponseBodyType(responseRow),
                "Setting the Error Response Body Type on the Request twin propagates live to the Response twin");

        // Flip the Response twin to No Response Body; the Request twin live-updates the same way.
        responseRow = rowByDenAndMessageBody(fixture.editPage, fixture.bie.getDen(), "Response");
        fixture.editPage.setRowErrorResponseBodyType(responseRow, "No Response Body");

        requestRow = rowByDenAndMessageBody(fixture.editPage, fixture.bie.getDen(), "Request");
        assertEquals("No Response Body", fixture.editPage.getRowErrorResponseBodyType(requestRow),
                "Setting the Error Response Body Type on the Response twin propagates live to the Request twin");
    }

    @Test
    @DisplayName("TC_43_12_7")
    public void twin_request_and_response_rows_keep_error_body_type_across_update_and_reopen() {
        // #1347 B29 (ConfirmMessage variant): the picked ConfirmMessage BIE lives on the operation, so it
        // must apply to BOTH grid rows of a Request + Response operation and survive Update + reopen. The
        // BIE MUST be the locked 'Confirm Message. Confirm Message' ASCCP under a unique Business Context
        // (a random BIE leaves the locked picker empty).
        ConfirmFixture fixture = newDocumentWithConfirmBie();
        EditOpenAPIDocumentPage editPage = fixture.editPage;
        assignBie(editPage, fixture.confirmBie, "POST", "Request");
        assignBie(editPage, fixture.confirmBie, "POST", "Response");

        // Pick ConfirmMessage on the Request twin (opens the locked picker, identified by Business Context).
        WebElement requestRow = rowByDenAndMessageBody(editPage, fixture.confirmBie.getDen(), "Request");
        OasDocConfirmMessageDialog dialog = editPage.openConfirmMessageDialogViaSelector(requestRow);
        dialog.selectCandidateByBusinessContext(fixture.businessContext.getName());
        dialog.hitSelect();

        // Pre-save: BOTH twins read the ConfirmMessage body type and show the DEN chip (live propagation).
        requestRow = rowByDenAndMessageBody(editPage, fixture.confirmBie.getDen(), "Request");
        WebElement responseRow = rowByDenAndMessageBody(editPage, fixture.confirmBie.getDen(), "Response");
        assertEquals("OAGi Confirm Message", editPage.getRowErrorResponseBodyType(requestRow),
                "The Request twin should read the ConfirmMessage body type");
        assertEquals("OAGi Confirm Message", editPage.getRowErrorResponseBodyType(responseRow),
                "The Response twin should read the ConfirmMessage body type (shared operation)");
        assertTrue(editPage.isRowConfirmMessageChipDisplayed(requestRow),
                "The Request twin should show the ConfirmMessage DEN chip");
        assertTrue(editPage.isRowConfirmMessageChipDisplayed(responseRow),
                "The Response twin should show the ConfirmMessage DEN chip (shared operation)");

        // Persist and reopen; both twins keep the ConfirmMessage body type, and the Response twin's chip
        // still carries the picked Confirm Message DEN.
        editPage.hitUpdateButton();
        editPage.openPage();

        WebElement reopenedRequest = rowByDenAndMessageBody(editPage, fixture.confirmBie.getDen(), "Request");
        WebElement reopenedResponse = rowByDenAndMessageBody(editPage, fixture.confirmBie.getDen(), "Response");
        assertEquals("OAGi Confirm Message", editPage.getRowErrorResponseBodyType(reopenedRequest),
                "The Request twin keeps the ConfirmMessage body type across Update + reopen");
        assertEquals("OAGi Confirm Message", editPage.getRowErrorResponseBodyType(reopenedResponse),
                "The Response twin keeps the ConfirmMessage body type across Update + reopen");
        assertTrue(editPage.getRowConfirmMessageChipText(reopenedResponse).contains(fixture.confirmBie.getDen()),
                "The Response twin's chip should carry the picked ConfirmMessage DEN (was: "
                        + editPage.getRowConfirmMessageChipText(reopenedResponse) + ")");
    }

    @Test
    @DisplayName("TC_43_12_10")
    public void removing_the_response_body_keeps_the_request_sibling_and_its_request_body_and_drops_the_200() {
        // #1492 B21: mirror of TC_43_12_5 (which removes the Request), but remove the RESPONSE twin. The
        // Request sibling (and its requestBody) must remain intact and the operation's 200 must drop.
        Fixture fixture = newDocumentWithBie();
        assignBie(fixture.editPage, fixture.bie, "POST", "Request");
        assignBie(fixture.editPage, fixture.bie, "POST", "Response");

        // Remove ONLY the Response twin.
        WebElement responseRow = rowByDenAndMessageBody(fixture.editPage, fixture.bie.getDen(), "Response");
        fixture.editPage.toggleSelect(responseRow);
        fixture.editPage.removeSelectedBIEs();

        // The Request sibling survives the Response removal; the Response row is gone. (This is the #1492
        // §3.5 / H1 guarantee that removing one body never orphans/FK-violates the sibling — the backend
        // delete ordering itself is covered by the BieForOasDocDeletePathTest unit test. The generated
        // output is not asserted here: a Request-only operation has no success response to emit, so its
        // generation is a separate, out-of-scope concern.)
        WebElement survivingRequestRow = rowByDenAndMessageBody(fixture.editPage, fixture.bie.getDen(), "Request");
        assertNotNull(survivingRequestRow, "Removing the Response body must leave the Request body intact");
        assertThrows(TimeoutException.class,
                () -> rowByDenAndMessageBody(fixture.editPage, fixture.bie.getDen(), "Response"),
                "The removed Response body should be gone");
    }

    @Test
    @DisplayName("TC_43_12_11")
    public void request_and_response_sharing_one_operation_id_is_not_flagged_duplicate() {
        // #1732 B24: a Request + Response pair on one (path, verb) shares ONE generated Operation ID and is
        // NOT flagged with the "must be unique" mat-error (contrast TC_43_6_7, where two distinct operations
        // colliding on one id ARE flagged). Keep both twins on the generated id (don't edit it).
        Fixture fixture = newDocumentWithBie();
        assignBie(fixture.editPage, fixture.bie, "POST", "Request");
        assignBie(fixture.editPage, fixture.bie, "POST", "Response");

        WebElement requestRow = rowByDenAndMessageBody(fixture.editPage, fixture.bie.getDen(), "Request");
        WebElement responseRow = rowByDenAndMessageBody(fixture.editPage, fixture.bie.getDen(), "Response");

        assertEquals(fixture.editPage.getRowOperationId(requestRow),
                fixture.editPage.getRowOperationId(responseRow),
                "The Request and Response bodies of one operation share a single Operation ID");
        assertTrue(fixture.editPage.getRowOperationIdError(requestRow).isEmpty(),
                "The Request twin's shared Operation ID is NOT flagged as a duplicate (was: "
                        + fixture.editPage.getRowOperationIdError(requestRow) + ")");
        assertTrue(fixture.editPage.getRowOperationIdError(responseRow).isEmpty(),
                "The Response twin's shared Operation ID is NOT flagged as a duplicate (was: "
                        + fixture.editPage.getRowOperationIdError(responseRow) + ")");
    }

    @Test
    @DisplayName("TC_43_12_12")
    public void flipping_message_body_request_to_response_converts_in_place_without_duplicating() {
        // Regression: flipping an existing operation row's Message Body Request->Response and clicking Update
        // used to LEAVE the original Request row and ADD a Response row, so the operation owned both bodies
        // and re-fetched as a duplicate (3 rows became 4). The inline flip must CONVERT the row in place:
        // exactly one row remains, now a Response, and no Request body lingers.
        Fixture fixture = newDocumentWithBie();
        assignBie(fixture.editPage, fixture.bie, "POST", "Request");
        assertEquals(1, rowsByDen(fixture.editPage, fixture.bie.getDen()).size(),
                "Precondition: the operation starts with a single Request body row");

        WebElement requestRow = rowByDenAndMessageBody(fixture.editPage, fixture.bie.getDen(), "Request");
        fixture.editPage.setRowMessageBody(requestRow, "Response");
        fixture.editPage.hitUpdateButton();

        // Reopen to re-fetch from the database (a duplicate, if any, surfaces on the union-all re-fetch).
        fixture.editPage.openPage();

        assertEquals(1, rowsByDen(fixture.editPage, fixture.bie.getDen()).size(),
                "Flipping Request->Response must convert the row in place, not add a second body row");
        assertEquals("Response", fixture.editPage.getRowMessageBody(
                        fixture.editPage.getTableRecordByValue(fixture.bie.getDen())),
                "The single surviving row should now be the Response body");
        assertThrows(TimeoutException.class,
                () -> rowByDenAndMessageBody(fixture.editPage, fixture.bie.getDen(), "Request"),
                "The original Request body must be gone after the flip (converted, not duplicated)");
    }

    @Test
    @DisplayName("TC_43_12_13")
    public void flipping_message_body_response_to_request_converts_in_place_without_duplicating() {
        // Symmetric to TC_43_12_12: Response->Request must also convert in place (POST keeps a Request body
        // valid). The now-replaced Response body must not linger as a duplicate row.
        Fixture fixture = newDocumentWithBie();
        assignBie(fixture.editPage, fixture.bie, "POST", "Response");
        assertEquals(1, rowsByDen(fixture.editPage, fixture.bie.getDen()).size(),
                "Precondition: the operation starts with a single Response body row");

        WebElement responseRow = rowByDenAndMessageBody(fixture.editPage, fixture.bie.getDen(), "Response");
        fixture.editPage.setRowMessageBody(responseRow, "Request");
        fixture.editPage.hitUpdateButton();

        fixture.editPage.openPage();

        assertEquals(1, rowsByDen(fixture.editPage, fixture.bie.getDen()).size(),
                "Flipping Response->Request must convert the row in place, not add a second body row");
        assertEquals("Request", fixture.editPage.getRowMessageBody(
                        fixture.editPage.getTableRecordByValue(fixture.bie.getDen())),
                "The single surviving row should now be the Request body");
        assertThrows(TimeoutException.class,
                () -> rowByDenAndMessageBody(fixture.editPage, fixture.bie.getDen(), "Response"),
                "The original Response body must be gone after the flip (converted, not duplicated)");
    }

    /* ------------------------------------------------------------------ helpers */

    private static String findPathForVerb(OpenAPIDocumentExport export, String verb) {
        return export.pathNames().stream()
                .filter(path -> export.operation(path, verb) != null)
                .findFirst()
                .orElse(null);
    }

    private static long countPathsWithVerb(OpenAPIDocumentExport export, String verb) {
        return export.pathNames().stream()
                .filter(path -> export.operation(path, verb) != null)
                .count();
    }

    // A grid row for a given DEN whose Message Body cell reads the given value (used to tell apart the two
    // rows of a single Request + Response operation).
    private WebElement rowByDenAndMessageBody(EditOpenAPIDocumentPage editPage, String den, String messageBody) {
        for (WebElement row : rowsByDen(editPage, den)) {
            if (messageBody.equals(editPage.getRowMessageBody(row))) {
                return row;
            }
        }
        throw new TimeoutException("No row found for DEN '" + den + "' with Message Body '" + messageBody + "'");
    }

    private List<WebElement> rowsByDen(EditOpenAPIDocumentPage editPage, String den) {
        // Sync on at least one matching row being rendered before collecting them all.
        editPage.getTableRecordByValue(den);
        return getDriver().findElements(org.openqa.selenium.By.xpath(
                "//tbody/tr[.//*[contains(normalize-space(.), "
                        + org.oagi.score.e2e.impl.PageHelper.xpathLiteral(den) + ")]]"));
    }

    private Fixture newDocumentWithBie() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI()
                .getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        BusinessContextObject businessContext = getAPIFactory().getBusinessContextAPI()
                .createRandomBusinessContext(endUser, "oas_one_op_bc");
        TopLevelASBIEPObject bie = createRandomTopLevelBie(endUser, release, namespace, businessContext);

        BusinessContextObject secondBusinessContext = getAPIFactory().getBusinessContextAPI()
                .createRandomBusinessContext(endUser, "oas_one_op_bc2");
        TopLevelASBIEPObject secondBie = createRandomTopLevelBie(endUser, release, namespace, secondBusinessContext);

        OpenAPIDocumentObject openAPIDocument =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);
        EditOpenAPIDocumentPage editPage = openEditOpenAPIDocumentPage(endUser, openAPIDocument);
        return new Fixture(editPage, bie, secondBie);
    }

    private TopLevelASBIEPObject createRandomTopLevelBie(AppUserObject owner, ReleaseObject release,
                                                         NamespaceObject namespace, BusinessContextObject context) {
        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        ACCObject acc = coreComponentAPI.createRandomACC(owner, release, namespace, "Published");
        ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, owner, namespace, "Published");
        return getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, owner, "WIP");
    }

    private EditOpenAPIDocumentPage openEditOpenAPIDocumentPage(AppUserObject endUser,
                                                               OpenAPIDocumentObject openAPIDocument) {
        this.homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = this.homePage.getBIEMenu();
        OpenAPIDocumentPage openAPIDocumentPage = bieMenu.openOpenAPIDocumentSubMenu();
        return openAPIDocumentPage.openEditOpenAPIDocumentPage(openAPIDocument);
    }

    private void assignBie(EditOpenAPIDocumentPage editPage, TopLevelASBIEPObject bie,
                           String verb, String messageBody) {
        AddBIEForOpenAPIDocumentDialog dialog = editPage.openAddBIEForOpenAPIDocumentDialog();
        sendKeys(dialog.getInputFieldInSearchBar(), bie.getDen());
        dialog.hitSearchButton();

        WebElement row = dialog.getTableRecordByValue(bie.getDen());
        dialog.toggleSelect(row);
        dialog.setVerb(row, verb);
        dialog.setMessageBody(row, messageBody);
        dialog.hitAddButton();
    }

    // Add an operation that does not reference a BIE (Issue #1730), via the 'Add Operation' dialog.
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

    // A document whose connected library carries a published 'Confirm Message' BIE under a unique Business
    // Context, so the locked ConfirmMessage picker shows exactly one selectable candidate.
    private ConfirmFixture newDocumentWithConfirmBie() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        BusinessContextObject businessContext = getAPIFactory().getBusinessContextAPI()
                .createRandomBusinessContext(endUser, "oas_one_op_confirm_bc");

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

    @AfterEach
    public void tearDown() {
        super.tearDown();

        this.randomAccounts.forEach(randomAccount -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId());
        });
    }

    private static class Fixture {
        private final EditOpenAPIDocumentPage editPage;
        private final TopLevelASBIEPObject bie;
        private final TopLevelASBIEPObject secondBie;

        private Fixture(EditOpenAPIDocumentPage editPage, TopLevelASBIEPObject bie, TopLevelASBIEPObject secondBie) {
            this.editPage = editPage;
            this.bie = bie;
            this.secondBie = secondBie;
        }
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
