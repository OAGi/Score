package org.oagi.score.e2e.TS_43_OpenAPIDocument;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import org.oagi.score.e2e.obj.BCCPObject;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.DTObject;
import org.oagi.score.e2e.obj.LibraryObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.obj.OpenAPIDocumentObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.CreateBIEForSelectTopLevelConceptPage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.oagi.score.e2e.page.oas.AddBIEForOpenAPIDocumentDialog;
import org.oagi.score.e2e.page.oas.EditOpenAPIDocumentPage;
import org.oagi.score.e2e.page.oas.OpenAPIDocumentExport;
import org.oagi.score.e2e.page.oas.OpenAPIDocumentPage;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.getSnackBarMessage;
import static org.oagi.score.e2e.impl.PageHelper.sendKeys;

/**
 * Test Case 43.9 - Generate an OpenAPI 3.1.1 Document (Issue #1610).
 * <p>
 * Verifies that an OpenAPI Document whose OpenAPI Version is set to 3.1.1 generates a document that
 * is shaped for OpenAPI 3.1 / JSON Schema 2020-12: the root {@code openapi} field reflects the
 * configured version, the 3.0-only {@code nullable: true} construct is NOT used, and BIE-backed
 * operations still contribute component schemas referenced via {@code $ref}. The companion case
 * confirms a 3.0.3 document round-trips its version into the generated output (the generator branches
 * on the stored OpenAPI Version), and DELETE request-body handling is covered by Test Case 43.10.
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_43_9_GenerateOpenAPI31 extends BaseTest {

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
    @DisplayName("TC_43_9_1")
    public void document_set_to_3_1_1_generates_a_3_1_1_shaped_document() throws IOException {
        Fixture fixture = newDocumentWithBie();
        // Switch the document to OpenAPI 3.1.1 and persist (generation runs against the saved document).
        fixture.editPage.setOpenAPIVersion("3.1.1");
        fixture.editPage.hitUpdateButton();
        // A BIE-backed operation contributes a component schema (and a request body) to inspect.
        assignBie(fixture.editPage, fixture.bie, "POST", "Request");

        File file = fixture.editPage.clickGenerateAndDownload();
        String yaml = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(file);

        assertEquals("3.1.1", String.valueOf(export.raw().get("openapi")),
                "The generated document's openapi field should be the configured 3.1.1");
        assertFalse(yaml.contains("nullable: true"),
                "An OpenAPI 3.1 document must not use the 3.0-only 'nullable: true' keyword "
                        + "(3.1 expresses nullability with a JSON Schema 2020-12 type union or anyOf null)");
        assertFalse(export.schemaNames().isEmpty(),
                "A BIE-backed operation should contribute at least one component schema");
        assertTrue(export.allRefs().stream().anyMatch(ref -> ref.startsWith("#/components/schemas/")),
                "A BIE-backed operation should reference a component schema via $ref");
    }

    @Test
    @DisplayName("TC_43_9_2")
    public void document_left_at_3_0_3_round_trips_its_version_into_the_generated_output() throws IOException {
        // A document created through the API defaults to OpenAPI Version 3.0.3, so this exercises the
        // 3.0.3 generator branch. The openapi field equalling 3.0.3 also asserts the default is honored.
        Fixture fixture = newDocumentWithBie();
        assignBie(fixture.editPage, fixture.bie, "POST", "Request");

        OpenAPIDocumentExport export =
                OpenAPIDocumentExport.from(fixture.editPage.clickGenerateAndDownload());

        assertEquals("3.0.3", String.valueOf(export.raw().get("openapi")),
                "The generated document's openapi field should reflect the document's OpenAPI Version (3.0.3), "
                        + "confirming the generator branches on the configured version");
    }

    @Test
    @DisplayName("TC_43_9_3")
    public void generate_is_blocked_while_there_are_unsaved_changes() throws IOException {
        // A BIE-backed operation makes the Endpoint Details table non-empty so Generate is enabled.
        Fixture fixture = newDocumentWithBie();
        assignBie(fixture.editPage, fixture.bie, "POST", "Request");

        // Change the OpenAPI Version but do NOT click Update: the document now has unsaved changes.
        // The document is generated from its persisted record, so generation must be blocked until the
        // edit is saved (Issue #1610).
        fixture.editPage.setOpenAPIVersion("3.1.1");
        fixture.editPage.clickGenerateButton();
        assertEquals("There are unsaved changes. Please click Update before generating the document.",
                getSnackBarMessage(getDriver()),
                "Generate must be blocked while an unsaved OpenAPI Version change is pending");

        // After Update, Generate succeeds and reflects the newly saved version (3.1.1).
        fixture.editPage.hitUpdateButton();
        OpenAPIDocumentExport export =
                OpenAPIDocumentExport.from(fixture.editPage.clickGenerateAndDownload());
        assertEquals("3.1.1", String.valueOf(export.raw().get("openapi")),
                "After Update, the generated document reflects the saved OpenAPI Version");
    }

    @Test
    @DisplayName("TC_43_9_5")
    @Disabled("Issue #1610 B7: the fixed-value-BBIE fixture is built through the multi-step BIE editor, "
            + "which is timing-fragile against the dev frontend (setBranch dropdown). The const/enum "
            + "generator logic is covered by OpenAPI31/30GenerateExpressionTest; a robust e2e needs an "
            + "API seed for BBIE.FIXED_VALUE (e.g. BusinessInformationEntityAPI.seedAllBbieFixedValue).")
    public void bbie_fixed_value_emits_const_in_3_1_1_and_single_element_enum_in_3_0_3() throws IOException {
        // Issue #1610 B7: a fixed-value BBIE is expressed with JSON Schema 2020-12 'const' in an
        // OpenAPI 3.1.1 document and with a single-element 'enum' in an OpenAPI 3.0.3 document.
        // The fixed value is set through the BIE editor (the supported product path; there is no
        // API seed for BBIE.FIXED_VALUE), then the same BIE is generated under both versions.
        String fixedValue = "test value";
        FixedValueBie fixedValueBie = newFixedValueBie(fixedValue);

        // --- OpenAPI 3.1.1: the fixed value must surface as `const` (and never as `enum`). ---
        OpenAPIDocumentObject document31 =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(fixedValueBie.owner);
        EditOpenAPIDocumentPage editPage31 = openEditOpenAPIDocumentPage(fixedValueBie.owner, document31);
        editPage31.setOpenAPIVersion("3.1.1");
        editPage31.hitUpdateButton();
        assignBie(editPage31, fixedValueBie.bie, "POST", "Request");

        OpenAPIDocumentExport export31 =
                OpenAPIDocumentExport.from(editPage31.clickGenerateAndDownload());
        SchemaProperty const31 = findPropertyWithConst(export31, fixedValue);
        assertNotNull(const31,
                "An OpenAPI 3.1.1 document must express a fixed-value BBIE with `const: " + fixedValue + "`");
        assertEquals(fixedValue, export31.schemaConst(const31.schemaName, const31.propertyName),
                "The 3.1.1 fixed-value property's `const` must equal the configured fixed value");
        assertNull(export31.schemaProperty(const31.schemaName, const31.propertyName).get("enum"),
                "An OpenAPI 3.1.1 document must not also emit the 3.0-only single-value `enum`");

        // --- OpenAPI 3.0.3: the same fixed value must surface as a single-element `enum`. ---
        OpenAPIDocumentObject document30 =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(fixedValueBie.owner);
        EditOpenAPIDocumentPage editPage30 = openEditOpenAPIDocumentPage(fixedValueBie.owner, document30);
        // A document created through the API defaults to OpenAPI Version 3.0.3 (the 3.0 generator branch).
        assignBie(editPage30, fixedValueBie.bie, "POST", "Request");

        OpenAPIDocumentExport export30 =
                OpenAPIDocumentExport.from(editPage30.clickGenerateAndDownload());
        SchemaProperty enum30 = findPropertyWithEnum(export30, fixedValue);
        assertNotNull(enum30,
                "An OpenAPI 3.0.3 document must express a fixed-value BBIE with a single-element `enum`");
        assertEquals(Arrays.asList(fixedValue), export30.schemaProperty(enum30.schemaName, enum30.propertyName).get("enum"),
                "The 3.0.3 fixed-value property's `enum` must be exactly [" + fixedValue + "]");
        assertNull(export30.schemaConst(enum30.schemaName, enum30.propertyName),
                "An OpenAPI 3.0.3 document must not emit the 3.1-only `const`");
    }

    /**
     * Create a BIE whose single scalar (max-cardinality-1) BBIE carries the given fixed value. The BIE
     * is built through the editor because that is the only supported way to set BBIE.FIXED_VALUE (the
     * e2e API exposes BBIE profiling and value-domain seeds, but not a fixed-value seed). Mirrors the
     * proven fixed-value flow of TC_6_2 (same Text BDT and Primitive 'token' value domain).
     */
    private FixedValueBie newFixedValueBie(String fixedValue) {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI()
                .getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI()
                .createRandomBusinessContext(endUser, "oas_31_fixed_bc");

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        ACCObject acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Published");
        DTObject textDataType = coreComponentAPI.getBDTByGuidAndReleaseNum(
                library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
        BCCPObject bccp = coreComponentAPI.createRandomBCCP(release, textDataType, endUser, namespace, "Published");
        coreComponentAPI.appendBCC(acc, bccp, "Published");
        ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, endUser, namespace, "Published");

        this.homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = this.homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage selectConceptPage =
                viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        EditBIEPage editBIEPage = selectConceptPage.createBIE(asccp.getDen(), release.getReleaseNumber());

        WebElement bbieNode = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueConstraint("Fixed Value");
        bbiePanel.setFixedValue(fixedValue);
        bbiePanel.setValueDomainRestriction("Primitive");
        bbiePanel.setValueDomain("token");
        editBIEPage.hitUpdateButton();

        TopLevelASBIEPObject bie = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByDENAndReleaseNum(asccp.getDen(), release.getReleaseNumber());
        return new FixedValueBie(endUser, bie);
    }

    /**
     * Discover the (schema, property) pair whose `const` equals the given value, by scanning the
     * generated document's component schemas (the schema and property names are derived from the
     * ASCCP/BCCP property terms by the generator, so they are discovered rather than hard-coded).
     */
    private SchemaProperty findPropertyWithConst(OpenAPIDocumentExport export, String value) {
        for (String schemaName : export.schemaNames()) {
            Map<String, Object> properties = propertiesOf(export, schemaName);
            for (String propertyName : properties.keySet()) {
                if (value.equals(export.schemaConst(schemaName, propertyName))) {
                    return new SchemaProperty(schemaName, propertyName);
                }
            }
        }
        return null;
    }

    /**
     * Discover the (schema, property) pair whose `enum` is the single-element list [value].
     */
    private SchemaProperty findPropertyWithEnum(OpenAPIDocumentExport export, String value) {
        for (String schemaName : export.schemaNames()) {
            Map<String, Object> properties = propertiesOf(export, schemaName);
            for (String propertyName : properties.keySet()) {
                Map<String, Object> property = export.schemaProperty(schemaName, propertyName);
                if (property != null && Arrays.asList(value).equals(property.get("enum"))) {
                    return new SchemaProperty(schemaName, propertyName);
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> propertiesOf(OpenAPIDocumentExport export, String schemaName) {
        Map<String, Object> schema = export.schema(schemaName);
        Object properties = schema == null ? null : schema.get("properties");
        return (properties instanceof Map)
                ? (Map<String, Object>) properties
                : java.util.Collections.emptyMap();
    }

    private Fixture newDocumentWithBie() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI()
                .getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        BusinessContextObject businessContext = getAPIFactory().getBusinessContextAPI()
                .createRandomBusinessContext(endUser, "oas_31_bc");
        TopLevelASBIEPObject bie = createRandomTopLevelBie(endUser, release, namespace, businessContext);

        OpenAPIDocumentObject openAPIDocument =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);
        EditOpenAPIDocumentPage editPage = openEditOpenAPIDocumentPage(endUser, openAPIDocument);
        return new Fixture(editPage, bie);
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

        private Fixture(EditOpenAPIDocumentPage editPage, TopLevelASBIEPObject bie) {
            this.editPage = editPage;
            this.bie = bie;
        }
    }

    /** A fixed-value BIE together with the end user that owns it (so a document can be created for it). */
    private static class FixedValueBie {
        private final AppUserObject owner;
        private final TopLevelASBIEPObject bie;

        private FixedValueBie(AppUserObject owner, TopLevelASBIEPObject bie) {
            this.owner = owner;
            this.bie = bie;
        }
    }

    /** A (component schema name, property name) pair discovered in a generated document. */
    private static class SchemaProperty {
        private final String schemaName;
        private final String propertyName;

        private SchemaProperty(String schemaName, String propertyName) {
            this.schemaName = schemaName;
            this.propertyName = propertyName;
        }
    }
}
