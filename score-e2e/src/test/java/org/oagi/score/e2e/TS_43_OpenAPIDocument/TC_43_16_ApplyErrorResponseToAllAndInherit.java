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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.getText;

/**
 * Test Case 43.16 - Apply the Error Response Body Type to ALL operations, and inherit it on newly added
 * operations (Issue #1347).
 * <p>
 * Beyond the per-operation Error Response selector (TC_43_11), the Endpoint Details toolbar carries a
 * document-level {@code Set Error Responses} selector + {@code Apply} button. Because the grid is
 * server-paginated, {@code Apply} is a SERVER-SIDE action that updates EVERY operation of the document
 * (not just the visible page): {@code No Response Body} / {@code IETF Problem Details} apply to every
 * operation regardless of release, while {@code OAGi Confirm Message} opens the {@code Select
 * ConfirmMessage BIE} dialog (whose Branch selector spans the whole document's releases) and applies to
 * the chosen release's operations plus bodyless operations. In addition, a NEWLY created operation
 * inherits the document's prevailing body type: all-{@code IETF Problem Details} &rarr; Problem Details;
 * all-{@code OAGi Confirm Message} (a single unambiguous ConfirmMessage BIE) &rarr; that ConfirmMessage;
 * a mix (or all {@code No Response Body}) &rarr; No Response Body.
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_43_16_ApplyErrorResponseToAllAndInherit extends BaseTest {

    // The DEN of the standard 'Confirm Message' component. The picker is locked to this DEN, so the test
    // profiles this very ASCCP into a BIE (rather than a random one) for the candidate to appear.
    private static final String CONFIRM_MESSAGE_DEN = "Confirm Message. Confirm Message";

    private static final String NONE = "No Response Body";
    private static final String PROBLEM_DETAILS = "IETF Problem Details";
    private static final String CONFIRM_MESSAGE = "OAGi Confirm Message";

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_43_16_1")
    public void toolbar_selector_defaults_to_none_and_apply_is_gated_on_having_operations() {
        EditOpenAPIDocumentPage editPage = newDocument();

        assertTrue(editPage.isBulkErrorResponseSelectorEnabled(),
                "The document-level 'Set Error Responses' selector is enabled on an editable document");
        assertEquals(NONE, editPage.getBulkErrorResponseBodyType(),
                "The document-level 'Set Error Responses' selector defaults to 'No Response Body'");
        assertFalse(editPage.isBulkErrorResponseApplyEnabled(),
                "'Apply' is disabled while the document has no operations");

        addBodylessOperation(editPage, "DELETE", "/a1", "createA1");
        assertTrue(editPage.isBulkErrorResponseApplyEnabled(),
                "'Apply' enables once the document has at least one operation");
    }

    @Test
    @DisplayName("TC_43_16_2")
    public void apply_problem_details_to_all_covers_every_operation_and_persists_server_side() {
        EditOpenAPIDocumentPage editPage = newDocument();
        addBodylessOperation(editPage, "DELETE", "/b1", "createB1");
        addBodylessOperation(editPage, "DELETE", "/b2", "createB2");
        addBodylessOperation(editPage, "DELETE", "/b3", "createB3");

        editPage.setBulkErrorResponseBodyType(PROBLEM_DETAILS);
        editPage.applyBulkErrorResponseBodyType();

        // Every operation — not just one page — reads the applied body type.
        assertAllRowsBodyType(editPage, 3, PROBLEM_DETAILS);

        // Reopen the document: a client-only apply would not survive, so this proves the SERVER applied it
        // to every operation.
        editPage.openPage();
        assertAllRowsBodyType(editPage, 3, PROBLEM_DETAILS);
    }

    @Test
    @DisplayName("TC_43_16_3")
    public void apply_none_to_all_resets_every_operation() {
        EditOpenAPIDocumentPage editPage = newDocument();
        addBodylessOperation(editPage, "DELETE", "/c1", "createC1");
        addBodylessOperation(editPage, "DELETE", "/c2", "createC2");

        editPage.setBulkErrorResponseBodyType(PROBLEM_DETAILS);
        editPage.applyBulkErrorResponseBodyType();
        assertAllRowsBodyType(editPage, 2, PROBLEM_DETAILS);

        editPage.setBulkErrorResponseBodyType(NONE);
        editPage.applyBulkErrorResponseBodyType();
        for (int i = 1; i <= 2; i++) {
            WebElement row = editPage.getTableRecordAtIndex(i);
            assertEquals(NONE, editPage.getRowErrorResponseBodyType(row),
                    "Applying 'No Response Body' to all resets every operation");
            assertFalse(editPage.isRowConfirmMessageChipDisplayed(row),
                    "No Confirm Message chip remains after resetting to 'No Response Body'");
        }
    }

    @Test
    @DisplayName("TC_43_16_4")
    public void apply_confirm_message_to_all_opens_the_dialog_and_sets_every_operation() {
        ConfirmFixture fixture = newDocumentWithConfirmBie();
        EditOpenAPIDocumentPage editPage = fixture.editPage;
        addBodylessOperation(editPage, "DELETE", "/d1", "createD1");
        addBodylessOperation(editPage, "DELETE", "/d2", "createD2");

        editPage.setBulkErrorResponseBodyType(CONFIRM_MESSAGE);
        OasDocConfirmMessageDialog dialog = editPage.applyBulkErrorResponseBodyTypeForConfirmMessage();
        assertEquals("Select Confirm Message BIE", getText(dialog.getTitle()),
                "Applying 'OAGi Confirm Message' to all opens the ConfirmMessage selection dialog");
        assertFalse(dialog.isSelectEnabled(), "Select is disabled until a BIE is chosen");
        dialog.selectCandidateByBusinessContext(fixture.businessContext.getName());
        dialog.hitSelect();
        editPage.confirmBulkErrorResponseApplied();

        for (int i = 1; i <= 2; i++) {
            WebElement row = editPage.getTableRecordAtIndex(i);
            assertEquals(CONFIRM_MESSAGE, editPage.getRowErrorResponseBodyType(row),
                    "Every operation reads 'OAGi Confirm Message' after apply-to-all");
            assertTrue(editPage.isRowConfirmMessagePicked(row),
                    "Every operation shows the picked Confirm Message BIE after apply-to-all");
            assertEquals(fixture.confirmBie.getTopLevelAsbiepId(), editPage.getRowConfirmMessagePickedBieId(row),
                    "Every operation links the picked Confirm Message BIE after apply-to-all");
        }

        // The apply is server-side, so it survives reopening the document on every operation.
        editPage.openPage();
        for (int i = 1; i <= 2; i++) {
            WebElement row = editPage.getTableRecordAtIndex(i);
            assertEquals(CONFIRM_MESSAGE, editPage.getRowErrorResponseBodyType(row),
                    "'OAGi Confirm Message' persists on every operation across Update + reopen");
            assertEquals(fixture.confirmBie.getTopLevelAsbiepId(), editPage.getRowConfirmMessagePickedBieId(row),
                    "The picked Confirm Message BIE persists on every operation across reopen");
        }
    }

    @Test
    @DisplayName("TC_43_16_5")
    public void a_new_operation_inherits_problem_details_when_all_others_are_problem_details() {
        EditOpenAPIDocumentPage editPage = newDocument();
        addBodylessOperation(editPage, "DELETE", "/e1", "createE1");

        // Apply Problem Details to the whole document (server-side), so a newly added operation observes an
        // all-Problem-Details document and inherits it.
        editPage.setBulkErrorResponseBodyType(PROBLEM_DETAILS);
        editPage.applyBulkErrorResponseBodyType();
        Set<String> priorNames = resourceNames(editPage, 1);

        addBodylessOperation(editPage, "DELETE", "/e2", "createE2");
        WebElement newRow = newlyAddedRow(editPage, 2, priorNames);
        assertEquals(PROBLEM_DETAILS, editPage.getRowErrorResponseBodyType(newRow),
                "A new operation inherits 'IETF Problem Details' when every existing operation is Problem Details");
    }

    @Test
    @DisplayName("TC_43_16_6")
    public void a_new_operation_inherits_confirm_message_when_all_others_are_confirm_message() {
        ConfirmFixture fixture = newDocumentWithConfirmBie();
        EditOpenAPIDocumentPage editPage = fixture.editPage;
        addBodylessOperation(editPage, "DELETE", "/f1", "createF1");

        // Make the (only) existing operation OAGi Confirm Message and persist it, so a newly added
        // operation observes an all-ConfirmMessage document with a single unambiguous BIE and inherits it.
        // (Uses the proven per-operation picker path; the inheritance decision is independent of how the
        // existing operation became ConfirmMessage.)
        pickConfirmMessageViaSelector(editPage, editPage.getTableRecordAtIndex(1), fixture.businessContext);
        editPage.hitUpdateButton();
        Set<String> priorNames = resourceNames(editPage, 1);

        addBodylessOperation(editPage, "DELETE", "/f2", "createF2");
        WebElement newRow = newlyAddedRow(editPage, 2, priorNames);
        assertEquals(CONFIRM_MESSAGE, editPage.getRowErrorResponseBodyType(newRow),
                "A new operation inherits 'OAGi Confirm Message' when the document has a single unambiguous Confirm Message BIE");
        assertTrue(editPage.isRowConfirmMessagePicked(newRow),
                "The inherited operation shows the picked Confirm Message BIE");
        assertEquals(fixture.confirmBie.getTopLevelAsbiepId(), editPage.getRowConfirmMessagePickedBieId(newRow),
                "The inherited operation links the document's Confirm Message BIE");
    }

    @Test
    @DisplayName("TC_43_16_7")
    public void a_new_operation_gets_no_response_body_when_the_document_is_mixed() {
        EditOpenAPIDocumentPage editPage = newDocument();
        addBodylessOperation(editPage, "DELETE", "/g1", "createG1");
        addBodylessOperation(editPage, "DELETE", "/g2", "createG2");

        // Make the document all-Problem-Details, then flip ONE operation back to No Response Body and save,
        // so the document holds a MIX (Problem Details + No Response Body).
        editPage.setBulkErrorResponseBodyType(PROBLEM_DETAILS);
        editPage.applyBulkErrorResponseBodyType();
        editPage.setRowErrorResponseBodyType(editPage.getTableRecordAtIndex(1), NONE);
        editPage.hitUpdateButton();
        Set<String> priorNames = resourceNames(editPage, 2);

        addBodylessOperation(editPage, "DELETE", "/g3", "createG3");
        WebElement newRow = newlyAddedRow(editPage, 3, priorNames);
        assertEquals(NONE, editPage.getRowErrorResponseBodyType(newRow),
                "A new operation gets 'No Response Body' when the document's operations are mixed");
        assertFalse(editPage.isRowConfirmMessageChipDisplayed(newRow),
                "A mixed-document new operation shows no Confirm Message chip");
    }

    @Test
    @DisplayName("TC_43_16_8")
    public void generated_document_reflects_bulk_applied_problem_details_on_every_operation() {
        EditOpenAPIDocumentPage editPage = newDocument();
        addBodylessOperation(editPage, "DELETE", "/gen1", "createGen1");
        addBodylessOperation(editPage, "DELETE", "/gen2", "createGen2");

        String path1 = editPage.getRowResourceName(editPage.getTableRecordAtIndex(1));
        String path2 = editPage.getRowResourceName(editPage.getTableRecordAtIndex(2));

        editPage.setBulkErrorResponseBodyType(PROBLEM_DETAILS);
        editPage.applyBulkErrorResponseBodyType();
        // The bulk apply is server-side (already persisted), so there is nothing left to Update — the
        // Update button is disabled; Generate reads the persisted state directly.
        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(editPage.clickGenerateAndDownload());

        // Every operation the bulk apply touched emits the Problem Details matrix: the version-independent
        // 500 response is a $ref to the reusable component referencing the shared ProblemDetails schema.
        for (String path : Arrays.asList(path1, path2)) {
            assertEquals("#/components/responses/500_InternalServerError",
                    export.responseRef(path, "delete", "500"),
                    "Every bulk-applied operation's 500 response should $ref the reusable Problem Details component");
        }
        assertTrue(export.hasComponentResponse("500_InternalServerError"),
                "The reusable 500 response component should be emitted");
        assertEquals("#/components/schemas/ProblemDetails",
                export.componentResponseContentSchemaRef("500_InternalServerError", "application/problem+json"),
                "The reusable 500 response component should reference the ProblemDetails schema");
        assertTrue(export.hasSchema("ProblemDetails"),
                "The shared RFC 9457 ProblemDetails schema should be emitted");
    }

    /* ------------------------------------------------------------------ helpers */

    private void assertAllRowsBodyType(EditOpenAPIDocumentPage editPage, int totalRows, String expectedLabel) {
        for (int i = 1; i <= totalRows; i++) {
            WebElement row = editPage.getTableRecordAtIndex(i);
            assertEquals(expectedLabel, editPage.getRowErrorResponseBodyType(row),
                    "Every operation row should read '" + expectedLabel + "' after apply-to-all (row " + i + ")");
        }
    }

    // Resource Name is rendered as an <input>, so the current rows are keyed by their input value.
    private Set<String> resourceNames(EditOpenAPIDocumentPage editPage, int totalRows) {
        Set<String> names = new HashSet<>();
        for (int i = 1; i <= totalRows; i++) {
            names.add(editPage.getRowResourceName(editPage.getTableRecordAtIndex(i)));
        }
        return names;
    }

    // The one row whose Resource Name is not among the known prior names (i.e. the just-added operation).
    // Robust to grid re-sorting on reload, which makes positional indexing unreliable.
    private WebElement newlyAddedRow(EditOpenAPIDocumentPage editPage, int totalRows, Set<String> priorNames) {
        for (int i = 1; i <= totalRows; i++) {
            WebElement row = editPage.getTableRecordAtIndex(i);
            if (!priorNames.contains(editPage.getRowResourceName(row))) {
                return row;
            }
        }
        throw new AssertionError("Could not find the newly added operation row");
    }

    private void pickConfirmMessageViaSelector(EditOpenAPIDocumentPage editPage, WebElement row,
                                               BusinessContextObject businessContext) {
        OasDocConfirmMessageDialog dialog = editPage.openConfirmMessageDialogViaSelector(row);
        dialog.selectCandidateByBusinessContext(businessContext.getName());
        dialog.hitSelect();
    }

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
