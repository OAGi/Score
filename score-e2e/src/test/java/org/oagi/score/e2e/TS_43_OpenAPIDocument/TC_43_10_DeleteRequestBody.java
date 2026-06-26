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
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.sendKeys;

/**
 * Test Case 43.10 - DELETE Operation Request Body Across OpenAPI Versions (Issue #1610).
 * <p>
 * A DELETE operation may now carry a {@code Request} message body. The body is HONORED in OpenAPI
 * 3.1.1 (a {@code requestBody} is emitted, paired with a status-only {@code 202} success) but DROPPED
 * in OpenAPI 3.0.3 (no {@code requestBody} is emitted, while the {@code 202} success remains). A
 * DELETE with a {@code Response} message body instead carries the BIE in a {@code 200} response and
 * has no request body, like the other verbs. These assertions are made against the generated
 * (downloaded) YAML, since the drop happens at generation time, not in the Add dialog.
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_43_10_DeleteRequestBody extends BaseTest {

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
    @DisplayName("TC_43_10_1")
    public void delete_request_body_is_honored_in_openapi_3_1_1() {
        Fixture fixture = newDocumentWithBie();
        fixture.editPage.setOpenAPIVersion("3.1.1");
        fixture.editPage.hitUpdateButton();
        assignBie(fixture.editPage, fixture.bie, "DELETE", "Request");

        // Sync on the assigned row so the operation list (and the banner state) has rendered.
        fixture.editPage.getTableRecordByValue(fixture.bie.getDen());
        assertFalse(fixture.editPage.isDeleteRequestBodyIgnoredWarningDisplayed(),
                "An OpenAPI 3.1.1 document honors a DELETE request body, so no 'ignored DELETE body' banner is shown");

        OpenAPIDocumentExport export =
                OpenAPIDocumentExport.from(fixture.editPage.clickGenerateAndDownload());

        assertEquals("3.1.1", String.valueOf(export.raw().get("openapi")));
        String deletePath = findDeletePath(export);
        assertNotNull(deletePath, "A DELETE operation should be emitted for the assigned BIE");
        assertTrue(export.operationHasRequestBody(deletePath, "delete"),
                "OpenAPI 3.1.1 should emit a requestBody for a DELETE + Request operation");
        assertTrue(export.operationResponseCodes(deletePath, "delete").contains("202"),
                "A DELETE + Request operation should declare a status-only 202 (Accepted) success");
    }

    @Test
    @DisplayName("TC_43_10_2")
    public void delete_request_body_is_dropped_in_openapi_3_0_3() {
        // The document created through the API defaults to OpenAPI Version 3.0.3.
        Fixture fixture = newDocumentWithBie();
        assignBie(fixture.editPage, fixture.bie, "DELETE", "Request");

        // Sync on the assigned row so the operation list (and the banner state) has rendered.
        fixture.editPage.getTableRecordByValue(fixture.bie.getDen());
        assertTrue(fixture.editPage.isDeleteRequestBodyIgnoredWarningDisplayed(),
                "An OpenAPI 3.0.3 document drops a DELETE request body, so the amber 'ignored DELETE body' banner is shown");

        OpenAPIDocumentExport export =
                OpenAPIDocumentExport.from(fixture.editPage.clickGenerateAndDownload());

        assertEquals("3.0.3", String.valueOf(export.raw().get("openapi")));
        String deletePath = findDeletePath(export);
        assertNotNull(deletePath, "A DELETE operation should be emitted for the assigned BIE");
        assertFalse(export.operationHasRequestBody(deletePath, "delete"),
                "OpenAPI 3.0.3 should DROP the request body of a DELETE + Request operation");
        assertTrue(export.operationResponseCodes(deletePath, "delete").contains("202"),
                "A DELETE + Request operation should still declare a status-only 202 (Accepted) success in 3.0.3");
        assertTrue(export.schemaNames().isEmpty(),
                "When the DELETE request body is dropped in 3.0.3, no orphan request schema is left in components/schemas");
    }

    @Test
    @DisplayName("TC_43_10_3")
    public void delete_with_response_body_carries_the_bie_in_a_200_and_has_no_request_body() {
        Fixture fixture = newDocumentWithBie();
        fixture.editPage.setOpenAPIVersion("3.1.1");
        fixture.editPage.hitUpdateButton();
        assignBie(fixture.editPage, fixture.bie, "DELETE", "Response");

        OpenAPIDocumentExport export =
                OpenAPIDocumentExport.from(fixture.editPage.clickGenerateAndDownload());

        String deletePath = findDeletePath(export);
        assertNotNull(deletePath, "A DELETE operation should be emitted for the assigned BIE");
        assertFalse(export.operationHasRequestBody(deletePath, "delete"),
                "A DELETE + Response operation carries no request body");
        assertTrue(export.operationResponseCodes(deletePath, "delete").contains("200"),
                "A DELETE + Response operation should carry the BIE in a 200 response");
        assertTrue(export.allRefs().stream().anyMatch(ref -> ref.startsWith("#/components/schemas/")),
                "A DELETE + Response operation should reference the BIE component schema via $ref");
    }

    @Test
    @DisplayName("TC_43_10_4")
    public void delete_request_problem_details_emits_415_and_422_in_3_1_1_but_drops_them_in_3_0_3_keeping_500() {
        // OpenAPI 3.1.1 honors a DELETE request body, so the 415/422 request-body error responses appear
        // in the defaulted error matrix; OpenAPI 3.0.3 drops the DELETE request body, so 415/422 are gone
        // while 500 (Internal Server Error, present for every verb in every version) always remains
        // (Issue #1610 x Issue #1347).
        Fixture fixture = newDocumentWithBie();
        fixture.editPage.setOpenAPIVersion("3.1.1");
        fixture.editPage.hitUpdateButton();
        assignBie(fixture.editPage, fixture.bie, "DELETE", "Request");

        // Re-fetch the assigned row (Angular re-renders the table on assign) and set the Error Response
        // body type to IETF Problem Details, which materializes the reusable RFC 9457 response components.
        WebElement row = fixture.editPage.getTableRecordByValue(fixture.bie.getDen());
        fixture.editPage.setRowErrorResponseBodyType(row, "IETF Problem Details");
        fixture.editPage.hitUpdateButton();

        OpenAPIDocumentExport export =
                OpenAPIDocumentExport.from(fixture.editPage.clickGenerateAndDownload());

        assertEquals("3.1.1", String.valueOf(export.raw().get("openapi")));
        String deletePath = findDeletePath(export);
        assertNotNull(deletePath, "A DELETE operation should be emitted for the assigned BIE");
        assertTrue(export.operationResponseCodes(deletePath, "delete").containsAll(Arrays.asList("415", "422", "500")),
                "OpenAPI 3.1.1 should emit the 415/422 request-body errors (DELETE carries a body) plus the always-present 500 (were: "
                        + export.operationResponseCodes(deletePath, "delete") + ")");
        assertEquals("#/components/responses/415_UnsupportedMediaType",
                export.responseRef(deletePath, "delete", "415"),
                "The 415 PROBLEM_DETAILS error response should $ref the reusable 415 response component");
        assertEquals("#/components/responses/422_UnprocessableContent",
                export.responseRef(deletePath, "delete", "422"),
                "The 422 PROBLEM_DETAILS error response should $ref the reusable 422 response component");
        assertTrue(export.hasComponentResponse("415_UnsupportedMediaType"),
                "The reusable 415 response component should be emitted");
        assertTrue(export.hasComponentResponse("422_UnprocessableContent"),
                "The reusable 422 response component should be emitted");

        // Switch the same document to OpenAPI 3.0.3, save, and regenerate: 3.0.3 drops the DELETE request
        // body, so 415/422 disappear from the error matrix while 500 remains.
        fixture.editPage.setOpenAPIVersion("3.0.3");
        fixture.editPage.hitUpdateButton();

        OpenAPIDocumentExport export30 =
                OpenAPIDocumentExport.from(fixture.editPage.clickGenerateAndDownload());

        assertEquals("3.0.3", String.valueOf(export30.raw().get("openapi")));
        String deletePath30 = findDeletePath(export30);
        assertNotNull(deletePath30, "A DELETE operation should be emitted for the assigned BIE");
        assertFalse(export30.operationResponseCodes(deletePath30, "delete").contains("415"),
                "OpenAPI 3.0.3 drops the DELETE request body, so the 415 request-body error must not appear (were: "
                        + export30.operationResponseCodes(deletePath30, "delete") + ")");
        assertFalse(export30.operationResponseCodes(deletePath30, "delete").contains("422"),
                "OpenAPI 3.0.3 drops the DELETE request body, so the 422 request-body error must not appear (were: "
                        + export30.operationResponseCodes(deletePath30, "delete") + ")");
        assertTrue(export30.operationResponseCodes(deletePath30, "delete").contains("500"),
                "The always-present 500 (Internal Server Error) must remain in OpenAPI 3.0.3 (were: "
                        + export30.operationResponseCodes(deletePath30, "delete") + ")");
    }

    @Test
    @DisplayName("TC_43_10_5")
    public void delete_request_body_dropped_in_3_0_3_still_emits_error_matrix_with_202_500_404_but_not_415_422() {
        // The document created through the API defaults to OpenAPI Version 3.0.3, which drops a DELETE
        // request body. The error matrix still defaults (body type NONE = description-only), carrying the
        // verb's 404/500 errors plus the status-only 202 success, but NOT the 415/422 request-body errors
        // (those need OpenAPI 3.1+ on DELETE) (Issue #1610 x Issue #1347).
        Fixture fixture = newDocumentWithBie();
        assignBie(fixture.editPage, fixture.bie, "DELETE", "Request");

        // Sync on the assigned row so the operation list (and the banner state) has rendered.
        fixture.editPage.getTableRecordByValue(fixture.bie.getDen());
        assertTrue(fixture.editPage.isDeleteRequestBodyIgnoredWarningDisplayed(),
                "An OpenAPI 3.0.3 document drops a DELETE request body, so the amber 'ignored DELETE body' banner is shown");

        OpenAPIDocumentExport export =
                OpenAPIDocumentExport.from(fixture.editPage.clickGenerateAndDownload());

        assertEquals("3.0.3", String.valueOf(export.raw().get("openapi")));
        String deletePath = findDeletePath(export);
        assertNotNull(deletePath, "A DELETE operation should be emitted for the assigned BIE");
        assertFalse(export.operationHasRequestBody(deletePath, "delete"),
                "OpenAPI 3.0.3 should DROP the request body of a DELETE + Request operation");
        assertTrue(export.operationResponseCodes(deletePath, "delete").contains("202"),
                "A DELETE + Request operation should still declare a status-only 202 (Accepted) success in 3.0.3 (were: "
                        + export.operationResponseCodes(deletePath, "delete") + ")");
        assertTrue(export.operationResponseCodes(deletePath, "delete").contains("500"),
                "The always-present 500 (Internal Server Error) must appear in the default error matrix (were: "
                        + export.operationResponseCodes(deletePath, "delete") + ")");
        assertTrue(export.operationResponseCodes(deletePath, "delete").contains("404"),
                "A DELETE operation's default error matrix carries 404 (Not Found) (were: "
                        + export.operationResponseCodes(deletePath, "delete") + ")");
        assertFalse(export.operationResponseCodes(deletePath, "delete").contains("415"),
                "OpenAPI 3.0.3 drops the DELETE request body, so the 415 request-body error must not appear (were: "
                        + export.operationResponseCodes(deletePath, "delete") + ")");
        assertFalse(export.operationResponseCodes(deletePath, "delete").contains("422"),
                "OpenAPI 3.0.3 drops the DELETE request body, so the 422 request-body error must not appear (were: "
                        + export.operationResponseCodes(deletePath, "delete") + ")");
    }

    private static String findDeletePath(OpenAPIDocumentExport export) {
        return export.pathNames().stream()
                .filter(path -> export.operation(path, "delete") != null)
                .findFirst()
                .orElse(null);
    }

    private Fixture newDocumentWithBie() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI()
                .getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        BusinessContextObject businessContext = getAPIFactory().getBusinessContextAPI()
                .createRandomBusinessContext(endUser, "oas_delete_bc");
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
}
