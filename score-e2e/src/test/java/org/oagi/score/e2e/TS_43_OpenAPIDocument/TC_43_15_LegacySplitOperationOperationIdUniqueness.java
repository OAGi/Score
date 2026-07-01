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
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.oas.EditOpenAPIDocumentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Issue #1757 — a legacy "split operation" must not trigger a false Operation ID uniqueness error.
 *
 * <p>Before Issue #1492 (Option 2) collapsed one {@code oas_operation} per {@code (path, verb)}, a single
 * endpoint's Request and Response could be stored on two SEPARATE {@code oas_operation} rows that share one
 * {@code operation_id}. On such legacy data the BIE-root {@code OpenAPI Document Information} panel
 * (Issue #1519) raised a false {@code Operation ID must be unique within the document.} error, because its
 * duplicate check keyed the distinct-operation set on the {@code oas_operation_id} row PK rather than on the
 * {@code (Resource Name, Verb)} operation identity — so the endpoint's two rows looked like two operations
 * sharing one Operation ID. Issue #1757 extracts a single shared validator ({@code OasOperationValidator})
 * that BOTH the OpenAPI Document editor and the BIE-root panel delegate to, and whose distinct-operation
 * identity is always {@code (Resource Name, Verb)}, never the row PK.</p>
 *
 * <p>The OpenAPI Document editor cannot create the split shape any more, so these tests seed it directly in
 * the database (via {@code OpenAPIDocumentAPI#seedOpenAPIOperationWithBody}). They verify, end to end:</p>
 * <ol>
 *   <li>a legacy split operation surfaces on the BIE root as two cards whose shared Operation ID is NOT
 *       flagged, and the panel stays savable (Issue #1757);</li>
 *   <li>the OpenAPI Document editor reads the same seeded data back with the same non-error verdict, i.e.
 *       both screens share one validator (Issues #1757, #1492);</li>
 *   <li>the narrowed key does NOT stop flagging a GENUINE cross-operation Operation ID collision — two
 *       distinct operations on different resources that share one id ARE still flagged and block saving
 *       (Issue #1757).</li>
 * </ol>
 *
 * <p>Note: generation of un-migrated legacy split data is a separate, out-of-scope concern — the collision
 * guard still rejects it at {@code Generate} time — so these tests exercise only the editor's validation UX,
 * which is exactly what Issue #1757 corrects.</p>
 */
@Execution(ExecutionMode.SAME_THREAD)
public class TC_43_15_LegacySplitOperationOperationIdUniqueness extends BaseTest {

    private static final String OPERATION_ID_UNIQUE_ERROR = "Operation ID must be unique within the document.";

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_43_15_1")
    public void a_legacy_split_operation_is_not_flagged_as_a_duplicate_operation_id_on_the_bie_root() {
        Ctx ctx = newContext();
        TopLevelASBIEPObject bie = createBie(ctx, "oas_1757_1_bc", "WIP");
        OpenAPIDocumentObject doc = createDoc(ctx);

        // Legacy split: one POST endpoint whose Request and Response live on two separate operations that
        // share the SAME (path, verb, operationId). Both message bodies point at the same BIE, so the BIE
        // root surfaces both.
        String path = "/legacy-split-order";
        String operationId = "createLegacyOrder";
        seedOperation(ctx, doc, bie, path, "POST", operationId, "Request");
        seedOperation(ctx, doc, bie, path, "POST", operationId, "Response");

        HomePage homePage = signIn(ctx.endUser);
        EditBIEPage editBIEPage = openBieEditor(homePage, bie);
        assertTrue(editBIEPage.isOpenAPIDocumentInformationPanelDisplayed(),
                "The OpenAPI Document Information panel should surface on the BIE root for the seeded binding");

        EditBIEPage.OpenAPIDocumentInformationPanel panel = editBIEPage.openOpenAPIDocumentInformationPanel();
        List<WebElement> cards = panel.getBindingCards();
        assertEquals(2, cards.size(),
                "The legacy split operation must surface as two binding cards (its Request and its Response)");

        List<String> messageBodies = new ArrayList<>();
        for (WebElement card : cards) {
            messageBodies.add(panel.getMessageBody(card));
            assertEquals(operationId, panel.getOperationId(card),
                    "Both cards of the split operation carry the shared Operation ID");
            // Issue #1757: the shared Operation ID is NOT a duplicate — the two rows are ONE operation
            // (same Resource Name + Verb), not two operations sharing an id.
            assertEquals("", panel.getOperationIdError(card),
                    "A legacy split operation's shared Operation ID must not be flagged as a duplicate (Issue #1757)");
            // The Request and the Response occupy different body slots, so neither is a duplicate body.
            assertEquals("", panel.getResourceNameError(card),
                    "A Request + Response pair on one (Resource Name, Verb) is not a duplicate body slot");
        }
        assertTrue(messageBodies.contains("Request") && messageBodies.contains("Response"),
                "The two cards should be the Request and the Response of the one operation (was: " + messageBodies + ")");

        // Usability: with no false error, a benign edit is savable — the panel is not blocked (Issue #1757).
        panel.setTag(panel.getBindingCards().get(0), "TC431515Tag");
        assertTrue(panel.isUpdateButtonEnabled(),
                "With no false uniqueness error present, an edited legacy binding must be savable (Issue #1757)");
    }

    @Test
    @DisplayName("TC_43_15_2")
    public void the_openapi_document_editor_reads_the_legacy_split_operation_back_without_a_uniqueness_error() {
        // Parity: the OpenAPI Document editor (already correct via Issue #1492) and the BIE-root panel now
        // share ONE validator, so the same seeded split operation is judged identically on both screens.
        Ctx ctx = newContext();
        TopLevelASBIEPObject bie = createBie(ctx, "oas_1757_2_bc", "WIP");
        OpenAPIDocumentObject doc = createDoc(ctx);

        String path = "/legacy-split-invoice";
        String operationId = "createLegacyInvoice";
        seedOperation(ctx, doc, bie, path, "POST", operationId, "Request");
        seedOperation(ctx, doc, bie, path, "POST", operationId, "Response");

        HomePage homePage = signIn(ctx.endUser);
        EditOpenAPIDocumentPage editOasPage = openOasEditor(homePage, doc);

        WebElement requestRow = rowByDenAndMessageBody(editOasPage, bie.getDen(), "Request");
        WebElement responseRow = rowByDenAndMessageBody(editOasPage, bie.getDen(), "Response");

        assertEquals(operationId, editOasPage.getRowOperationId(requestRow),
                "The Request row shows the seeded shared Operation ID");
        assertEquals(operationId, editOasPage.getRowOperationId(responseRow),
                "The Response row shows the seeded shared Operation ID");
        assertEquals("", editOasPage.getRowOperationIdError(requestRow),
                "The OpenAPI Document editor must not flag the split operation's shared Operation ID (Issues #1757, #1492)");
        assertEquals("", editOasPage.getRowOperationIdError(responseRow),
                "The OpenAPI Document editor must not flag the split operation's shared Operation ID (Issues #1757, #1492)");
        assertFalse(editOasPage.isRowDuplicateBodyWarningDisplayed(requestRow),
                "A Request + Response pair on one (Resource Name, Verb) is not a duplicate body slot");
        assertFalse(editOasPage.isRowDuplicateBodyWarningDisplayed(responseRow),
                "A Request + Response pair on one (Resource Name, Verb) is not a duplicate body slot");
    }

    @Test
    @DisplayName("TC_43_15_3")
    public void a_genuine_cross_operation_operation_id_collision_is_still_flagged_on_the_bie_root() {
        // Guard against over-correction: narrowing the key to (Resource Name, Verb) must NOT disable the
        // check. Two DISTINCT operations on DIFFERENT resources that share one Operation ID ARE a real
        // duplicate and must still be flagged and block saving.
        Ctx ctx = newContext();
        TopLevelASBIEPObject bie = createBie(ctx, "oas_1757_3_bc", "WIP");
        OpenAPIDocumentObject doc = createDoc(ctx);

        String sharedOperationId = "queryLegacyThing";
        seedOperation(ctx, doc, bie, "/legacy-alpha", "GET", sharedOperationId, "Response");
        seedOperation(ctx, doc, bie, "/legacy-beta", "GET", sharedOperationId, "Response");

        HomePage homePage = signIn(ctx.endUser);
        EditBIEPage editBIEPage = openBieEditor(homePage, bie);
        EditBIEPage.OpenAPIDocumentInformationPanel panel = editBIEPage.openOpenAPIDocumentInformationPanel();

        List<WebElement> cards = panel.getBindingCards();
        assertEquals(2, cards.size(), "The two distinct operations should surface as two binding cards");
        for (WebElement card : cards) {
            assertEquals(sharedOperationId, panel.getOperationId(card),
                    "Both operations carry the same hand-set Operation ID");
            assertEquals(OPERATION_ID_UNIQUE_ERROR, panel.getOperationIdError(card),
                    "A genuine cross-operation Operation ID collision must still be flagged (Issue #1757)");
        }

        // A benign edit cannot be saved while the genuine duplicate stands (the error, not the lack of a
        // change, is what blocks the Update button).
        panel.setTag(panel.getBindingCards().get(0), "TC431515Tag");
        assertFalse(panel.isUpdateButtonEnabled(),
                "A genuine duplicate Operation ID must keep the Update OpenAPI Information button disabled (Issue #1757)");
    }

    /* ================================================== helpers ================================================== */

    private void seedOperation(Ctx ctx, OpenAPIDocumentObject doc, TopLevelASBIEPObject bie,
                               String path, String verb, String operationId, String messageBody) {
        getAPIFactory().getOpenAPIDocumentAPI()
                .seedOpenAPIOperationWithBody(doc, bie, path, verb, operationId, messageBody, ctx.endUser);
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
        return getDriver().findElements(By.xpath(
                "//tbody/tr[.//*[contains(normalize-space(.), "
                        + org.oagi.score.e2e.impl.PageHelper.xpathLiteral(den) + ")]]"));
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
