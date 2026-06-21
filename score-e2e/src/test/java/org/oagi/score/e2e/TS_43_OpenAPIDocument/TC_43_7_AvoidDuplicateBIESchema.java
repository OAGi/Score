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
import org.oagi.score.e2e.page.oas.EditOpenAPIDocumentPage;
import org.oagi.score.e2e.page.oas.OpenAPIDocumentExport;
import org.oagi.score.e2e.page.oas.OpenAPIDocumentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.sendKeys;
import static org.oagi.score.e2e.impl.PageHelper.xpathLiteral;

/**
 * Test Case 43.7 - Avoid Duplicate BIE Schema in the Generated OpenAPI Document (Issue #1728).
 * <p>
 * When one top-level BIE backs both an array (list) operation and a non-array (single) operation,
 * generation must reuse a single shared {@code <BIEName>} schema (no orphan {@code <BIEName>ListEntry})
 * when both operations share the same Suppress Root option; when the Suppress Root option differs the
 * distinct {@code <BIEName>ListEntry} schema is correctly retained.
 * <p>
 * Runs in {@link ExecutionMode#SAME_THREAD}: each scenario drives the heavy OpenAPI Document editor
 * (Add-BIE dialog, per-row Array Indicator / Suppress Root toggles, Generate-and-download) against a
 * single shared dev stack. Running the scenarios concurrently put several browser sessions through that
 * flow at once and the Material snackbar overlays / row-render timing raced (intermittent
 * ElementClickIntercepted on the array-indicator checkbox, "Update" never becoming clickable, or the
 * second Add-BIE not registering its operation row). Serializing removes that contention, matching the
 * proven-stable {@code TC_46_2} BIE Package suite.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class TC_43_7_AvoidDuplicateBIESchema extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_43_7_1")
    public void same_bie_can_back_one_array_operation_and_one_single_operation() {
        Fixture fixture = base();

        List<WebElement> rows = rowsForDen(fixture.den);
        WebElement arrayRow = rowByVerb(fixture.editPage, rows, "PUT");
        WebElement singleRow = rowByVerb(fixture.editPage, rows, "POST");

        fixture.editPage.setRowArrayIndicator(arrayRow, true);

        String arrayOperationId = fixture.editPage.getRowOperationId(arrayRow);
        String singleOperationId = fixture.editPage.getRowOperationId(singleRow);

        assertTrue(arrayOperationId.endsWith("List"),
                "The array operation's Operation ID should carry a trailing 'List' (was: " + arrayOperationId + ")");
        assertFalse(singleOperationId.endsWith("List"),
                "The single operation's Operation ID should not carry a trailing 'List' (was: " + singleOperationId + ")");
    }

    @Test
    @DisplayName("TC_43_7_2")
    public void generating_with_same_suppress_root_downloads_a_single_yaml_without_error() {
        Fixture fixture = base();
        toggleArrayAndUpdate(fixture, false);

        File generated = fixture.editPage.clickGenerateAndDownload();

        assertNotNull(generated, "A generated OpenAPI document should have been downloaded");
        assertTrue(generated.getName().endsWith(".yml"),
                "The generated OpenAPI document should be a single YAML (.yml) file (was: " + generated.getName() + ")");
        // Parseable YAML => generation succeeded with no error.
        assertNotNull(OpenAPIDocumentExport.from(generated).raw());
    }

    @Test
    @DisplayName("TC_43_7_3")
    public void generated_document_has_one_shared_bie_schema_and_no_orphan_list_entry() {
        Fixture fixture = base();
        toggleArrayAndUpdate(fixture, false);

        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(fixture.editPage.clickGenerateAndDownload());

        assertEquals(0, export.countSchemaNamesContaining("ListEntry"),
                "No orphan '<BIEName>ListEntry' schema should be generated when both operations share Suppress Root");

        // Exactly one array wrapper schema and exactly one bare inner schema for the BIE.
        long arrayWrappers = export.schemaNames().stream().filter(name -> name.endsWith("List")).count();
        assertEquals(1, arrayWrappers,
                "Exactly one '<BIEName>List' array schema should be generated");
    }

    @Test
    @DisplayName("TC_43_7_4")
    public void array_and_single_operations_share_one_bie_schema() {
        Fixture fixture = base();
        toggleArrayAndUpdate(fixture, false);

        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(fixture.editPage.clickGenerateAndDownload());

        String arrayWrapper = export.schemaNames().stream()
                .filter(name -> name.endsWith("List"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected a '<BIEName>List' array schema"));

        String itemsRef = export.schemaItemsRef(arrayWrapper);
        assertNotNull(itemsRef, "The array schema should reference an inner item schema via items.$ref");

        String innerSchema = itemsRef.substring(itemsRef.lastIndexOf('/') + 1);
        assertTrue(export.hasSchema(innerSchema),
                "The inner item schema '" + innerSchema + "' should be declared as a shared component schema");
        assertTrue(export.refCount(itemsRef) >= 2,
                "The shared inner schema should be referenced by both the array items and the non-array operation");
    }

    @Test
    @DisplayName("TC_43_7_5")
    public void different_suppress_root_retains_a_distinct_list_entry_schema() {
        Fixture fixture = base();
        EditOpenAPIDocumentPage editPage = fixture.editPage;

        // Force the two operations to differ in Suppress Root: the array (PUT) operation does NOT
        // suppress the root, while the single (POST) operation does. (Newly assigned operations
        // default to suppressing the root, so an explicit difference must be established.)
        editPage.setRowSuppressRoot(rowByVerb(editPage, rowsForDen(fixture.den), "PUT"), false);
        editPage.setRowSuppressRoot(rowByVerb(editPage, rowsForDen(fixture.den), "POST"), true);

        assertFalse(editPage.isRowSuppressRootChecked(rowByVerb(editPage, rowsForDen(fixture.den), "PUT")),
                "Setup: the array operation's Suppress Root should be unchecked");
        assertTrue(editPage.isRowSuppressRootChecked(rowByVerb(editPage, rowsForDen(fixture.den), "POST")),
                "Setup: the single operation's Suppress Root should be checked");

        editPage.setRowArrayIndicator(rowByVerb(editPage, rowsForDen(fixture.den), "PUT"), true);
        editPage.hitUpdateButton();

        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(editPage.clickGenerateAndDownload());

        assertTrue(export.countSchemaNamesContaining("ListEntry") >= 1,
                "A distinct '<BIEName>ListEntry' schema should be retained when the operations differ in Suppress Root");
    }

    /**
     * Create an end user, a top-level BIE, an OpenAPI Document, and assign the same BIE twice: once as
     * a {@code POST} (single) operation and once as a {@code PUT} operation (which the caller turns into
     * the array operation).
     */
    private Fixture base() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI()
                .getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        BusinessContextObject businessContext = getAPIFactory().getBusinessContextAPI()
                .createRandomBusinessContext(endUser, "oas_dup_bc");

        TopLevelASBIEPObject bie = createRandomTopLevelBie(endUser, release, namespace, businessContext);
        OpenAPIDocumentObject openAPIDocument = getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);

        EditOpenAPIDocumentPage editPage = openEditOpenAPIDocumentPage(endUser, openAPIDocument);
        assignBie(editPage, bie, "POST", "Request");
        assignBie(editPage, bie, "PUT", "Request");

        return new Fixture(editPage, bie.getDen());
    }

    private void toggleArrayAndUpdate(Fixture fixture, boolean differentSuppressRoot) {
        List<WebElement> rows = rowsForDen(fixture.den);
        WebElement arrayRow = rowByVerb(fixture.editPage, rows, "PUT");
        fixture.editPage.setRowArrayIndicator(arrayRow, true);
        if (differentSuppressRoot) {
            // Array operation suppresses root; the single (POST) operation does not, so the shapes differ.
            fixture.editPage.setRowSuppressRoot(arrayRow, true);
        }
        fixture.editPage.hitUpdateButton();
    }

    private List<WebElement> rowsForDen(String den) {
        return getDriver().findElements(By.xpath(
                "//tbody/tr[.//*[contains(normalize-space(.), " + xpathLiteral(den) + ")]]"));
    }

    private WebElement rowByVerb(EditOpenAPIDocumentPage editPage, List<WebElement> rows, String verb) {
        return rows.stream()
                .filter(row -> verb.equals(editPage.getRowVerb(row)))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected an operation row with verb " + verb));
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
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
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

    @AfterEach
    public void tearDown() {
        super.tearDown();

        this.randomAccounts.forEach(randomAccount -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId());
        });
    }

    private static class Fixture {
        private final EditOpenAPIDocumentPage editPage;
        private final String den;

        private Fixture(EditOpenAPIDocumentPage editPage, String den) {
            this.editPage = editPage;
            this.den = den;
        }
    }
}
