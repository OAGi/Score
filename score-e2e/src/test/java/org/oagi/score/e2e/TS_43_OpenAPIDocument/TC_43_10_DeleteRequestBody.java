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

        OpenAPIDocumentExport export =
                OpenAPIDocumentExport.from(fixture.editPage.clickGenerateAndDownload());

        assertEquals("3.0.3", String.valueOf(export.raw().get("openapi")));
        String deletePath = findDeletePath(export);
        assertNotNull(deletePath, "A DELETE operation should be emitted for the assigned BIE");
        assertFalse(export.operationHasRequestBody(deletePath, "delete"),
                "OpenAPI 3.0.3 should DROP the request body of a DELETE + Request operation");
        assertTrue(export.operationResponseCodes(deletePath, "delete").contains("202"),
                "A DELETE + Request operation should still declare a status-only 202 (Accepted) success in 3.0.3");
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
