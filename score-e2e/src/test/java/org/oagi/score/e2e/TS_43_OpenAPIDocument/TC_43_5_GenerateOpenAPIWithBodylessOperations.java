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
import org.oagi.score.e2e.page.oas.OpenAPIDocumentExport;
import org.oagi.score.e2e.page.oas.OpenAPIDocumentPage;
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

/**
 * Test Case 43.5 - Generate OpenAPI With Bodyless Operations (Issue #1730).
 * <p>
 * Verifies the generated (downloaded) OpenAPI YAML for "bodyless" operations: no requestBody, a
 * status-only response derived from the verb (DELETE -> 202 Accepted, PATCH -> 204 No Content), the
 * emitted operationId, path parameters, and a document mixing BIE-backed and bodyless operations.
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_43_5_GenerateOpenAPIWithBodylessOperations extends BaseTest {

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
    @DisplayName("TC_43_5_1")
    public void bodyless_delete_operation_emits_202_accepted_and_no_request_body() {
        Fixture fixture = newDocument();
        addBodylessOperation(fixture.editPage, "DELETE", "/item/{id}", null);
        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(fixture.editPage.clickGenerateAndDownload());

        assertTrue(export.hasPath("/item/{id}"), "The bodyless operation's path should be emitted");
        assertFalse(export.operationHasRequestBody("/item/{id}", "delete"),
                "A bodyless DELETE operation should have no requestBody");
        assertTrue(export.operationResponseCodes("/item/{id}", "delete").contains("202"),
                "A bodyless DELETE operation should declare a 202 response");
        assertEquals("Accepted", export.responseDescription("/item/{id}", "delete", "202"));
    }

    @Test
    @DisplayName("TC_43_5_2")
    public void bodyless_patch_operation_emits_204_no_content_and_no_request_body() {
        Fixture fixture = newDocument();
        addBodylessOperation(fixture.editPage, "PATCH", "/item/{id}", null);
        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(fixture.editPage.clickGenerateAndDownload());

        assertFalse(export.operationHasRequestBody("/item/{id}", "patch"),
                "A bodyless PATCH operation should have no requestBody");
        assertTrue(export.operationResponseCodes("/item/{id}", "patch").contains("204"),
                "A bodyless PATCH operation should declare a 204 response");
        assertEquals("No Content", export.responseDescription("/item/{id}", "patch", "204"));
    }

    @Test
    @DisplayName("TC_43_5_3")
    public void emitted_operation_id_matches_a_manual_override() {
        Fixture fixture = newDocument();
        AddOperationForOpenAPIDocumentDialog dialog = fixture.editPage.openAddOperationDialog();
        dialog.setVerb("DELETE");
        dialog.setResourceName("/item/{id}");
        dialog.setOperationId("removeItemById");
        dialog.hitAddButton();
        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(fixture.editPage.clickGenerateAndDownload());

        assertTrue(export.operationIds().contains("removeItemById"),
                "The manually overridden Operation ID should be emitted verbatim");
    }

    @Test
    @DisplayName("TC_43_5_4")
    public void path_variable_segment_is_emitted_as_a_path_parameter() {
        Fixture fixture = newDocument();
        addBodylessOperation(fixture.editPage, "DELETE", "/item/{id}", null);
        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(fixture.editPage.clickGenerateAndDownload());

        assertTrue(export.hasPath("/item/{id}"),
                "The brace path segment should be emitted as a path parameter in the path key");
        assertFalse(export.operationHasRequestBody("/item/{id}", "delete"),
                "The bodyless operation should still carry no request body");
    }

    @Test
    @DisplayName("TC_43_5_5")
    public void document_mixing_bie_backed_and_bodyless_operations_generates_successfully() {
        Fixture fixture = newDocumentWithBie();

        // BIE-backed operation.
        assignBie(fixture.editPage, fixture.bie, "POST", "Request");
        // Bodyless operation.
        addBodylessOperation(fixture.editPage, "DELETE", "/item/{id}", null);
        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(fixture.editPage.clickGenerateAndDownload());

        assertNotNull(export.raw(), "Generation should succeed and produce a parseable document");
        // The bodyless operation has no request body.
        assertTrue(export.hasPath("/item/{id}"));
        assertFalse(export.operationHasRequestBody("/item/{id}", "delete"));
        // The BIE-backed operation references a component schema.
        assertFalse(export.schemaNames().isEmpty(),
                "The BIE-backed operation should contribute at least one component schema");
        assertTrue(export.allRefs().stream().anyMatch(ref -> ref.startsWith("#/components/schemas/")),
                "The BIE-backed operation should reference a component schema");
    }

    private Fixture newDocument() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        OpenAPIDocumentObject openAPIDocument =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);
        EditOpenAPIDocumentPage editPage = openEditOpenAPIDocumentPage(endUser, openAPIDocument);
        return new Fixture(editPage, null);
    }

    private Fixture newDocumentWithBie() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI()
                .getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        BusinessContextObject businessContext = getAPIFactory().getBusinessContextAPI()
                .createRandomBusinessContext(endUser, "oas_bodyless_bc");
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

    private void addBodylessOperation(EditOpenAPIDocumentPage editPage, String verb,
                                      String resourceName, String tag) {
        AddOperationForOpenAPIDocumentDialog dialog = editPage.openAddOperationDialog();
        dialog.setVerb(verb);
        dialog.setResourceName(resourceName);
        if (tag != null) {
            dialog.setTag(tag);
        }
        dialog.hitAddButton();
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
}
