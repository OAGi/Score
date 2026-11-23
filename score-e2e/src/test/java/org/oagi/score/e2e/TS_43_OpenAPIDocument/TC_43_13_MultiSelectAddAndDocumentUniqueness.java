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
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.sendKeys;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;

/**
 * Test Case 43.13 - Multi-select Add &amp; OpenAPI Document uniqueness.
 * <p>
 * Two fixes are covered:
 * <ul>
 *   <li><b>Multi-select Add.</b> Two BIEs that merely share a property term (e.g. two profiles of one ASCCP under
 *       different Business Contexts, both GET + Response) land on two distinct endpoints (their resource paths are
 *       derived from their different Business Contexts) but would have the same Operation ID, which the OpenAPI
 *       specification wants unique among all operations of the API. The dialog asks the server about the BIEs as they
 *       are chosen together, says it at the later one, and does not offer Add until that is resolved.</li>
 *   <li><b>Document uniqueness.</b> An OpenAPI document is unique on
 *       {@code (Title, OpenAPI Version, Document Version)}; the License Name is not part of the key. Creating a
 *       duplicate and editing one document's metadata to duplicate another are both blocked before any save
 *       (the same check backs Create and Update).</li>
 * </ul>
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_43_13_MultiSelectAddAndDocumentUniqueness extends BaseTest {

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
    public void selecting_multiple_bies_says_an_operation_id_that_the_add_would_repeat() {
        // Two profiles of ONE ASCCP under two different Business Contexts share the same DEN / property term.
        // Select BOTH in the Add BIE dialog and configure both as GET + Response.
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

        // The two BIEs are of two endpoints (their Resource Names have their Business Contexts) and would have the same
        // Operation ID. The OpenAPI specification wants an Operation ID unique among all the operations of the API, so the
        // server cannot say that what was added can be saved: both are in the table, not saved, and both say it.
        dialog.hitAddButton();

        List<WebElement> rows = rowsByDen(fixture.editPage, den);
        assertEquals(2, rows.size(), "Both selected BIEs are added to the table");
        for (WebElement row : rows) {
            assertTrue(fixture.editPage.getRowOperationIdError(row).contains("Operation ID must be unique within the document"),
                    "Both rows say that the Operation ID is repeated");
        }
        assertThrows(TimeoutException.class, () -> fixture.editPage.getUpdateButton(true),
                "Update is not there while an Operation ID is repeated");

        // The user gives the later one an Operation ID of its own: it can be saved.
        fixture.editPage.setRowOperationId(rows.get(1), fixture.editPage.getRowOperationId(rows.get(1)) + "Other");
        fixture.editPage.clickUpdateButton();
        getDriver().navigate().refresh();

        assertEquals(2, rowsByDen(fixture.editPage, den).size(), "Both BIEs are saved after the Operation ID is fixed");
    }

    @Test
    @DisplayName("TC_43_13_2")
    public void creating_a_duplicate_openapi_document_is_rejected() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        // An existing document fixes the unique tuple (Title, OpenAPI Version, Document Version).
        OpenAPIDocumentObject existing =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);

        this.homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = this.homePage.getBIEMenu();
        OpenAPIDocumentPage openAPIDocumentPage = bieMenu.openOpenAPIDocumentSubMenu();
        CreateOpenAPIDocumentPage createPage = openAPIDocumentPage.openCreateOpenAPIDocumentPage();

        // Enter the SAME three-tuple as the existing document, with a DIFFERENT License Name (not part of the key).
        createPage.setOpenAPIVersion(existing.getOpenApiVersion());
        createPage.setTitle(existing.getTitle());
        createPage.setDocumentVersion(existing.getVersion());
        createPage.setLicenseName(existing.getLicenseName() + " other");
        click(createPage.getCreateButton(true));

        // The duplicate create is blocked before any save (an "Invalid parameters" dialog explains the clash).
        WebElement dialog = visibilityOfElementLocated(getDriver(), By.xpath("//mat-dialog-container"));
        assertTrue(getText(dialog).contains("already exists"),
                "Creating a document with the same (Title, OpenAPI Version, Document Version) must be blocked, not saved (was: "
                        + getText(dialog) + ")");
    }

    @Test
    @DisplayName("TC_43_13_3")
    public void editing_an_openapi_document_to_duplicate_another_is_blocked() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        // Two distinct documents; both default to OpenAPI Version 3.0.
        OpenAPIDocumentObject existing =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);
        OpenAPIDocumentObject target =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);

        this.homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = this.homePage.getBIEMenu();
        OpenAPIDocumentPage openAPIDocumentPage = bieMenu.openOpenAPIDocumentSubMenu();
        EditOpenAPIDocumentPage editPage = openAPIDocumentPage.openEditOpenAPIDocumentPage(target);

        // Edit the target's metadata so its three-tuple equals the existing document's (License Name differs).
        editPage.setOpenAPIVersion(existing.getOpenApiVersion());
        editPage.setTitle(existing.getTitle());
        editPage.setDocumentVersion(existing.getVersion());
        editPage.setLicenseName(existing.getLicenseName() + " other");

        // The server says that the change cannot be saved, and Update is not there until it is changed: the duplicate is
        // blocked before any save.
        List<String> violations = editPage.getChangeViolations();
        assertTrue(violations.stream().anyMatch(message -> message.contains("already exists")),
                "Updating a document to duplicate another's unique tuple must be blocked, not saved (was: " + violations + ")");
        assertThrows(org.openqa.selenium.TimeoutException.class, () -> editPage.getUpdateButton(true),
                "Update is not there while the document duplicates another's unique tuple");
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
