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
import org.oagi.score.e2e.page.oas.CreateOpenAPIDocumentPage;
import org.oagi.score.e2e.page.oas.EditOpenAPIDocumentPage;
import org.oagi.score.e2e.page.oas.OpenAPIDocumentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.getMultiActionSnackBar;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.sendKeys;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;

/**
 * Test Case 43.13 - Multi-select Add &amp; OpenAPI Document uniqueness.
 * <p>
 * Two fixes are covered:
 * <ul>
 *   <li><b>Multi-select Add.</b> The "Add BIE" dialog previously de-duplicated a batch on
 *       {@code (propertyTerm, verb, messageBody)} only and ABORTED the whole Add on the first collision,
 *       so selecting two different BIEs that merely share a property term (e.g. two profiles of one ASCCP
 *       under different Business Contexts, both GET + Response) silently added nothing. The dialog must now
 *       add every selected BIE; the two distinct BIEs land on two distinct endpoints (their resource paths
 *       are derived from their different Business Contexts).</li>
 *   <li><b>Document uniqueness.</b> An OpenAPI document is unique on
 *       {@code (Title, OpenAPI Version, Document Version, License Name)}. Creating a duplicate is now
 *       rejected by the server (the request reaches the backend and the error is surfaced), and editing one
 *       document's metadata to duplicate another is blocked before any save.</li>
 * </ul>
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_43_13_MultiSelectAddAndDocumentUniqueness extends BaseTest {

    private static final String DUPLICATE_DOC_BACKEND_MESSAGE =
            "An OpenAPI document with the same Title, OpenAPI Version, Document Version, and License Name already exists.";

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
    @DisplayName("TC_43_13_1")
    public void selecting_multiple_bies_adds_every_selected_bie() {
        // Two profiles of ONE ASCCP under two different Business Contexts share the same DEN / property term.
        // Select BOTH in the Add BIE dialog, configure both as GET + Response (the exact (propertyTerm, verb,
        // messageBody) collision that previously aborted the batch), and Add them in one click.
        SameDenFixture fixture = newDocumentWithTwoSameDenBies();
        String den = fixture.bieA.getDen();
        assertEquals(den, fixture.bieB.getDen(),
                "Precondition: the two BIEs profile the same ASCCP, so they share one DEN / property term");

        AddBIEForOpenAPIDocumentDialog dialog = fixture.editPage.openAddBIEForOpenAPIDocumentDialog();
        sendKeys(dialog.getInputFieldInSearchBar(), den);
        dialog.hitSearchButton();

        WebElement firstRow = dialog.getTableRecordAtIndex(1);
        dialog.toggleSelect(firstRow);
        dialog.setVerb(firstRow, "GET");
        dialog.setMessageBody(firstRow, "Response");

        WebElement secondRow = dialog.getTableRecordAtIndex(2);
        dialog.toggleSelect(secondRow);
        dialog.setVerb(secondRow, "GET");
        dialog.setMessageBody(secondRow, "Response");

        dialog.hitAddButton();

        // Both selected BIEs are added: the grid now carries two rows for the shared DEN (one per Business
        // Context). Under the old batch-abort defect this was zero.
        assertEquals(2, rowsByDen(fixture.editPage, den).size(),
                "Both selected BIEs (same property term, different Business Context) must be added in one Add");
    }

    @Test
    @DisplayName("TC_43_13_2")
    public void creating_a_duplicate_openapi_document_is_rejected() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        // An existing document fixes the unique tuple (Title, OpenAPI Version, Document Version, License Name).
        OpenAPIDocumentObject existing =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);

        this.homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = this.homePage.getBIEMenu();
        OpenAPIDocumentPage openAPIDocumentPage = bieMenu.openOpenAPIDocumentSubMenu();
        CreateOpenAPIDocumentPage createPage = openAPIDocumentPage.openCreateOpenAPIDocumentPage();

        // Enter the SAME four-tuple as the existing document and hit Create.
        createPage.setOpenAPIVersion(existing.getOpenApiVersion());
        createPage.setTitle(existing.getTitle());
        createPage.setDocumentVersion(existing.getVersion());
        createPage.setLicenseName(existing.getLicenseName());
        click(createPage.getCreateButton(true));

        // The duplicate is rejected by the server and the error is surfaced; no second document is created.
        assertEquals(DUPLICATE_DOC_BACKEND_MESSAGE,
                getText(getMultiActionSnackBar(getDriver()).getMessageElement()),
                "Creating a document with the same (Title, OpenAPI Version, Document Version, License Name) is rejected");
    }

    @Test
    @DisplayName("TC_43_13_3")
    public void editing_an_openapi_document_to_duplicate_another_is_blocked() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        // Two distinct documents; both default to OpenAPI Version 3.0.3.
        OpenAPIDocumentObject existing =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);
        OpenAPIDocumentObject target =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);

        this.homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = this.homePage.getBIEMenu();
        OpenAPIDocumentPage openAPIDocumentPage = bieMenu.openOpenAPIDocumentSubMenu();
        EditOpenAPIDocumentPage editPage = openAPIDocumentPage.openEditOpenAPIDocumentPage(target);

        // Edit the target's metadata so its four-tuple equals the existing document's, then Update.
        editPage.setOpenAPIVersion(existing.getOpenApiVersion());
        editPage.setTitle(existing.getTitle());
        editPage.setDocumentVersion(existing.getVersion());
        editPage.setLicenseName(existing.getLicenseName());
        editPage.clickUpdateButton();

        // The duplicate update is blocked before any save (an "Invalid parameters" dialog explains the clash).
        WebElement dialog = visibilityOfElementLocated(getDriver(), By.xpath("//mat-dialog-container"));
        assertTrue(getText(dialog).contains("already exists"),
                "Updating a document to duplicate another's unique tuple must be blocked, not saved (was: "
                        + getText(dialog) + ")");
    }

    /* ------------------------------------------------------------------ helpers */

    // The grid rows on the edit page whose DEN cell carries the given DEN. Two profiles of one ASCCP share a
    // DEN, so a successful multi-select Add yields two such rows.
    private List<WebElement> rowsByDen(EditOpenAPIDocumentPage editPage, String den) {
        // Sync on at least one matching row being rendered before collecting them all.
        editPage.getTableRecordByValue(den);
        return getDriver().findElements(By.xpath(
                "//tbody/tr[.//*[contains(normalize-space(.), "
                        + org.oagi.score.e2e.impl.PageHelper.xpathLiteral(den) + ")]]"));
    }

    private SameDenFixture newDocumentWithTwoSameDenBies() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI()
                .getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        BusinessContextObject contextA = getAPIFactory().getBusinessContextAPI()
                .createRandomBusinessContext(endUser, "oas_multiadd_a");
        BusinessContextObject contextB = getAPIFactory().getBusinessContextAPI()
                .createRandomBusinessContext(endUser, "oas_multiadd_b");

        // ONE ACC + ASCCP, profiled into TWO BIEs under the two Business Contexts: same DEN, different paths.
        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        ACCObject acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Published");
        ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, endUser, namespace, "Published");
        TopLevelASBIEPObject bieA = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(contextA), asccp, endUser, "WIP");
        TopLevelASBIEPObject bieB = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(contextB), asccp, endUser, "WIP");

        OpenAPIDocumentObject openAPIDocument =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);
        EditOpenAPIDocumentPage editPage = openEditOpenAPIDocumentPage(endUser, openAPIDocument);
        return new SameDenFixture(editPage, bieA, bieB);
    }

    private EditOpenAPIDocumentPage openEditOpenAPIDocumentPage(AppUserObject endUser,
                                                               OpenAPIDocumentObject openAPIDocument) {
        this.homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = this.homePage.getBIEMenu();
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

    private static class SameDenFixture {
        private final EditOpenAPIDocumentPage editPage;
        private final TopLevelASBIEPObject bieA;
        private final TopLevelASBIEPObject bieB;

        private SameDenFixture(EditOpenAPIDocumentPage editPage, TopLevelASBIEPObject bieA, TopLevelASBIEPObject bieB) {
            this.editPage = editPage;
            this.bieA = bieA;
            this.bieB = bieB;
        }
    }
}
