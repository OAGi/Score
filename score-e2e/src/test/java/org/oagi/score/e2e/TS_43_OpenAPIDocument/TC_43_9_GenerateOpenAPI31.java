package org.oagi.score.e2e.TS_43_OpenAPIDocument;

import org.apache.commons.io.FileUtils;
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
}
