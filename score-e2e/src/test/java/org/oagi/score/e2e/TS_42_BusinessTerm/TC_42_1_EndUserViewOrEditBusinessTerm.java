package org.oagi.score.e2e.TS_42_BusinessTerm;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.condition.DisabledIfBusinessTermProperty;
import org.oagi.score.e2e.impl.AuthenticatedApiClient;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.BieBusinessTermAssignDialog;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.business_term.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
@DisabledIfBusinessTermProperty(value = false)
public class TC_42_1_EndUserViewOrEditBusinessTerm extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @BeforeEach
    public void init() {
        super.init();
    }

    @Test
    @DisplayName("TC_42_1_1")
    public void end_user_opens_business_term_page_from_bie_menu() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        String viewEditBusinessTermPageTitle = getText(bieMenu.openViewEditBusinessTermSubMenu().getTitle());
        assertEquals("Business Term", viewEditBusinessTermPageTitle);
    }

    @Test
    @DisplayName("TC_42_1_2")
    public void end_user_can_create_business_term_with_only_required_fields() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBusinessTermPage viewEditBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu();
        CreateBusinessTermPage createBusinessTermPage = viewEditBusinessTermPage.openCreateBusinessTermPage();

        BusinessTermObject businessTerm = new BusinessTermObject();
        businessTerm.setBusinessTerm("bt_" + RandomStringUtils.secure().nextAlphanumeric(5, 10));
        businessTerm.setExternalReferenceUri("http://www." + RandomStringUtils.secure().nextAscii(3, 8) + ".com");
        viewEditBusinessTermPage = createBusinessTermPage.createBusinessTerm(businessTerm);
        EditBusinessTermPage editBusinessTermPage = viewEditBusinessTermPage.openEditBusinessTermPageByTerm(businessTerm.getBusinessTerm());
        assertEquals(businessTerm.getBusinessTerm(), editBusinessTermPage.getBusinessTermFieldText());
        assertTrue(StringUtils.isEmpty(editBusinessTermPage.getDefinitionFieldText()));
    }

    @Test
    @DisplayName("TC_42_1_3")
    public void end_user_cannot_create_business_term_if_any_required_field_is_missing() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBusinessTermPage viewEditBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu();
        CreateBusinessTermPage createBusinessTermPage = viewEditBusinessTermPage.openCreateBusinessTermPage();

        BusinessTermObject businessTerm1 = new BusinessTermObject();
        businessTerm1.setBusinessTerm("bt_" + RandomStringUtils.secure().nextAlphanumeric(5, 10));
        assertThrows(TimeoutException.class, () -> createBusinessTermPage.createBusinessTerm(businessTerm1));

        BusinessTermObject businessTerm2 = new BusinessTermObject();
        businessTerm2.setExternalReferenceUri("http://www." + RandomStringUtils.secure().nextAscii(3, 8) + ".com");
        assertThrows(TimeoutException.class, () -> createBusinessTermPage.createBusinessTerm(businessTerm2));
    }

    @Test
    @DisplayName("TC_42_1_4")
    public void end_user_can_search_business_term_by_term_only() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBusinessTermPage viewEditBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu();
        viewEditBusinessTermPage.setTerm(randomBusinessTerm.getBusinessTerm());
        viewEditBusinessTermPage.hitSearchButton();
        assertBusinessTermNameInTheSearchResultsAtFirst(viewEditBusinessTermPage, randomBusinessTerm.getBusinessTerm(), "businessTerm");
    }

    @Test
    @DisplayName("TC_42_1_5")
    public void end_user_can_search_business_term_by_external_reference_uri() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBusinessTermPage viewEditBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu();
        viewEditBusinessTermPage.showAdvancedSearchPanel();
        viewEditBusinessTermPage.setExternalReferenceURI(randomBusinessTerm.getExternalReferenceUri());
        viewEditBusinessTermPage.hitSearchButton();
        assertBusinessTermNameInTheSearchResultsAtFirst(viewEditBusinessTermPage, randomBusinessTerm.getExternalReferenceUri(), "externalReferenceUri");
    }

    private void assertBusinessTermNameInTheSearchResultsAtFirst(ViewEditBusinessTermPage viewEditBusinessTermPage, String searchString, String columnName) {
        retry(() -> {
            WebElement tr = viewEditBusinessTermPage.getTableRecordAtIndex(1);
            WebElement td = viewEditBusinessTermPage.getColumnByName(tr, columnName);
            assertEquals(searchString, td.findElement(By.cssSelector("a > span")).getText());
        });
    }

    @Test
    @DisplayName("TC_42_1_6")
    public void end_user_can_update_business_term_from_edit_page() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        EditBusinessTermPage editBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu().openEditBusinessTermPageByTerm(randomBusinessTerm.getBusinessTerm());

        String oldTermName = randomBusinessTerm.getBusinessTerm();
        randomBusinessTerm.setBusinessTerm("bt_" + RandomStringUtils.secure().nextAlphanumeric(5, 10));
        assertNotEquals(oldTermName, randomBusinessTerm.getBusinessTerm());

        String oldExternalRefUri = randomBusinessTerm.getExternalReferenceUri();
        randomBusinessTerm.setExternalReferenceUri("http://www." + RandomStringUtils.secure().nextAscii(3, 8) + ".com");
        assertNotEquals(oldExternalRefUri, randomBusinessTerm.getExternalReferenceUri());

        String oldExternalRefID = randomBusinessTerm.getExternalReferenceId();
        randomBusinessTerm.setExternalReferenceId(Integer.toString(RandomUtils.secure().randomInt(1, 10)));
        assertNotEquals(oldExternalRefID, randomBusinessTerm.getExternalReferenceId());

        String oldComment = randomBusinessTerm.getComment();
        randomBusinessTerm.setComment(RandomStringUtils.secure().nextPrint(20, 50).trim());
        assertNotEquals(oldComment, randomBusinessTerm.getComment());

        editBusinessTermPage.updateBusinessTerm(randomBusinessTerm);

        assertThrows(NoSuchElementException.class, () -> bieMenu.openViewEditBusinessTermSubMenu().openEditBusinessTermPageByTerm(oldTermName));

        editBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu().openEditBusinessTermPageByTerm(randomBusinessTerm.getBusinessTerm());

        assertEquals(randomBusinessTerm.getBusinessTerm(), editBusinessTermPage.getBusinessTermFieldText());
        assertEquals(randomBusinessTerm.getExternalReferenceUri(), editBusinessTermPage.getExternalReferenceURIFieldText());
        assertEquals(randomBusinessTerm.getExternalReferenceId(), editBusinessTermPage.getExternalReferenceIDFieldText());
        assertEquals(randomBusinessTerm.getComment(), editBusinessTermPage.getCommentFieldText());
    }

    @Test
    @DisplayName("TC_42_1_7")
    public void end_user_cannot_change_definition_field_in_edit_business_term_page() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        EditBusinessTermPage editBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu()
                .openEditBusinessTermPageByTerm(randomBusinessTerm.getBusinessTerm());

        WebElement definitionField = editBusinessTermPage.getDefinitionField();
        assertDisabled(definitionField);
    }

    @Test
    @DisplayName("TC_42_1_8")
    public void end_user_cannot_create_duplicate_business_term_with_same_term_and_uri() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        CreateBusinessTermPage createBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu().openCreateBusinessTermPage();
        createBusinessTermPage.setBusinessTerm(randomBusinessTerm.getBusinessTerm());
        createBusinessTermPage.setExternalReferenceURI(randomBusinessTerm.getExternalReferenceUri());
        click(createBusinessTermPage.getCreateButton());
        assertTrue(getDriver().findElement(By.xpath(
                "//*[contains(text(), \"Another business term with the same business term and " +
                        "external reference URI already exists!\")]")).isDisplayed());
    }

    @Test
    @DisplayName("TC_42_1_9")
    public void end_user_cannot_discard_business_term_when_it_is_used_in_assignments() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        //use pre-existing BBIE node
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Collections.singletonList(randomBusinessContext), asccp, endUser, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Note";
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();
        // #1754: assign the business term in place via the BIE editor's 'Business Terms' chip field
        // (the standalone 'Assign Business Term' page was removed). The '+' button is enabled once the
        // used node is saved.
        assertTrue(bbiePanel.getAddBusinessTermButton().isEnabled());
        BieBusinessTermAssignDialog assignDialog = editBIEPage.getBBIEPanel(editBIEPage.getNodeByPath(path))
                .openBusinessTermAssignDialog();
        assignDialog.setSearchBusinessTerm(randomBusinessTerm.getBusinessTerm());
        assignDialog.hitSearch();
        click(assignDialog.getRowCheckboxByTerm(randomBusinessTerm.getBusinessTerm()));
        assignDialog.hitAssign();

        BIEMenu bieMenu = homePage.getBIEMenu();
        EditBusinessTermPage editBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu().openEditBusinessTermPageByTerm(randomBusinessTerm.getBusinessTerm());

        // #1752 - H2: an in-use term can no longer be discarded from the edit page. The fix populates
        // the `used` flag server-side and hides the Discard button (@if (!businessTerm.used)) rather
        // than letting the delete fail with a misleading foreign-key HTTP 500. Wait for the form to
        // render (its always-present Business Term field) before asserting the button is absent.
        assertEquals(randomBusinessTerm.getBusinessTerm(), editBusinessTermPage.getBusinessTermFieldText());
        assertFalse(editBusinessTermPage.isDiscardButtonPresent());
    }

    @Test
    @DisplayName("TC_42_1_10")
    public void end_user_can_discard_business_term_after_removing_its_assignments() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        //use pre-existing BBIE node
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, "Source Activity. Source Activity", release.getReleaseNumber());
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Collections.singletonList(randomBusinessContext), asccp, endUser, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);

        String path = "/" + asccp.getPropertyTerm() + "/Note";
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();
        // #1754: assign the business term in place via the BIE editor's 'Business Terms' chip field
        // (the standalone 'Assign Business Term' page was removed).
        assertTrue(bbiePanel.getAddBusinessTermButton().isEnabled());
        BieBusinessTermAssignDialog assignDialog = editBIEPage.getBBIEPanel(editBIEPage.getNodeByPath(path))
                .openBusinessTermAssignDialog();
        assignDialog.setSearchBusinessTerm(randomBusinessTerm.getBusinessTerm());
        assignDialog.hitSearch();
        click(assignDialog.getRowCheckboxByTerm(randomBusinessTerm.getBusinessTerm()));
        assignDialog.hitAssign();

        BIEMenu bieMenu = homePage.getBIEMenu();
        EditBusinessTermPage editBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu().openEditBusinessTermPageByTerm(randomBusinessTerm.getBusinessTerm());

        // #1752 - H2: while the term is assigned (used) the edit-page Discard button is hidden.
        assertEquals(randomBusinessTerm.getBusinessTerm(), editBusinessTermPage.getBusinessTermFieldText());
        assertFalse(editBusinessTermPage.isDiscardButtonPresent());

        // #1754: remove the assignment in place via the BIE editor's chip field (unassign the chip and
        // confirm the confirmation dialog).
        EditBIEPage editBIEPageForDiscard = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);
        WebElement bbieNodeForDiscard = editBIEPageForDiscard.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanelForDiscard = editBIEPageForDiscard.getBBIEPanel(bbieNodeForDiscard);
        WebElement chipForDiscard = bbiePanelForDiscard.getBusinessTermChipByTerm(randomBusinessTerm.getBusinessTerm());
        bbiePanelForDiscard.removeBusinessTermChip(chipForDiscard);

        // #1752 - H2: once the assignment is gone the term is no longer used, so the Discard button
        // reappears on the edit page and the term can be discarded with a clean success.
        editBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu().openEditBusinessTermPageByTerm(randomBusinessTerm.getBusinessTerm());
        assertEquals(randomBusinessTerm.getBusinessTerm(), editBusinessTermPage.getBusinessTermFieldText());
        assertTrue(editBusinessTermPage.isDiscardButtonPresent());
        editBusinessTermPage.discardBusinessTerm();

        ViewEditBusinessTermPage viewEditBusinessTermPageForDiscard = bieMenu.openViewEditBusinessTermSubMenu();
        assertThrows(NoSuchElementException.class, () -> {
            viewEditBusinessTermPageForDiscard.openEditBusinessTermPageByTerm(randomBusinessTerm.getBusinessTerm());
        });
    }

    @Test
    @DisplayName("TC_42_1_11")
    public void end_user_can_save_a_long_external_reference_uri_on_the_edit_page() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        EditBusinessTermPage editBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu()
                .openEditBusinessTermPageByTerm(randomBusinessTerm.getBusinessTerm());

        // #1752 - M6: a URI longer than the old 45-character edit-form limit must be stored in full
        // (regression guard for #1458, where the create form was fixed but the edit form was not).
        String longUri = "https://example.com/resources/articles/" + RandomStringUtils.secure().nextAlphanumeric(30);
        assertTrue(longUri.length() > 45);
        randomBusinessTerm.setExternalReferenceUri(longUri);
        editBusinessTermPage.updateBusinessTerm(randomBusinessTerm);

        editBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu()
                .openEditBusinessTermPageByTerm(randomBusinessTerm.getBusinessTerm());
        assertEquals(longUri, editBusinessTermPage.getExternalReferenceURIFieldText());
    }

    @Test
    @DisplayName("TC_42_1_12")
    public void business_term_endpoints_reject_a_developer_via_direct_api() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        // #1752 - H1: the navbar and BIE editor hide the Business Term area from developers
        // (isBusinessTermEnabled && !isDeveloper). The REST endpoints must not trust the UI: a direct
        // API call by a developer is rejected server-side with 403, for both reads and writes. The
        // suite only runs when the feature flag is on, so a 403 here is unambiguously the role gate.
        loginPage().signIn(developer.getLoginId(), developer.getPassword());
        AuthenticatedApiClient api = new AuthenticatedApiClient(getDriver(), getConfig().getBaseUrl());

        AuthenticatedApiClient.ApiResponse readResponse = api.getJson("/api/business-terms/1");
        assertEquals(403, readResponse.statusCode());
        assertTrue(String.valueOf(readResponse.header("X-Error-Message")).contains("developer"));

        String createBody = "{\"businessTerm\":\"bt_" + RandomStringUtils.secure().nextAlphanumeric(8)
                + "\",\"externalReferenceUri\":\"https://example.org/"
                + RandomStringUtils.secure().nextAlphanumeric(8) + "\"}";
        AuthenticatedApiClient.ApiResponse writeResponse = api.postJson("/api/business-terms", createBody);
        assertEquals(403, writeResponse.statusCode());
        assertTrue(String.valueOf(writeResponse.header("X-Error-Message")).contains("developer"));
    }

    @Test
    @DisplayName("TC_42_1_13")
    public void list_filter_survives_a_page_reload() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBusinessTermPage viewEditBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu();
        viewEditBusinessTermPage.showAdvancedSearchPanel();
        viewEditBusinessTermPage.setExternalReferenceURI(randomBusinessTerm.getExternalReferenceUri());
        viewEditBusinessTermPage.hitSearchButton();
        assertBusinessTermNameInTheSearchResultsAtFirst(viewEditBusinessTermPage, randomBusinessTerm.getExternalReferenceUri(), "externalReferenceUri");

        // #1752 - M2: the External Reference URI filter must round-trip through the URL so a
        // reload/bookmark keeps it (its (de)serialization keys are now aligned). The advanced-search
        // panel is restored open on reload (adv_ser flag), so the filter value is still shown.
        getDriver().navigate().refresh();
        assertEquals(randomBusinessTerm.getExternalReferenceUri(),
                viewEditBusinessTermPage.getExternalReferenceURIField().getAttribute("value"));
        assertBusinessTermNameInTheSearchResultsAtFirst(viewEditBusinessTermPage, randomBusinessTerm.getExternalReferenceUri(), "externalReferenceUri");
    }

    @Test
    @DisplayName("TC_42_1_14")
    public void create_rejects_a_malformed_external_reference_uri() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        CreateBusinessTermPage createBusinessTermPage = bieMenu.openViewEditBusinessTermSubMenu().openCreateBusinessTermPage();
        createBusinessTermPage.setBusinessTerm("bt_" + RandomStringUtils.secure().nextAlphanumeric(5, 10));
        // #1752 - M5: the create form has no URI format check, but the JSON create path now validates
        // the URI server-side, so a malformed URI is rejected with a 400 surfaced as an error snackbar.
        createBusinessTermPage.setExternalReferenceURI("not a valid uri !!!");
        click(createBusinessTermPage.getCreateButton());

        WebElement errorSnackBar = visibilityOfElementLocated(getDriver(),
                By.xpath("//score-multi-actions-snack-bar//div[contains(@class, \"message\")]"));
        assertTrue(getText(errorSnackBar).contains("is not a valid URI"));
    }

    @Test
    @DisplayName("TC_42_1_15")
    public void csv_import_reports_created_and_updated_counts() throws IOException {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject existing = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();

        // #1753 - L6: one row reuses an existing external reference URI (-> updated) and one is new
        // (-> created); the import dialog's result step must report 1 created and 1 updated.
        String newName = "bt_" + RandomStringUtils.secure().nextAlphanumeric(8);
        String newUri = "https://example.org/" + RandomStringUtils.secure().nextAlphanumeric(12);
        String csv = "\"businessTerm\",\"externalReferenceUri\",\"externalReferenceId\",\"definition\",\"comment\"\n"
                + "\"" + existing.getBusinessTerm() + "\",\"" + existing.getExternalReferenceUri() + "\",\"\",\"\",\"\"\n"
                + "\"" + newName + "\",\"" + newUri + "\",\"\",\"\",\"\"\n";
        Path csvFile = Files.createTempFile("bt-import-", ".csv");
        Files.write(csvFile, csv.getBytes(StandardCharsets.UTF_8));

        UploadBusinessTermsPage uploadBusinessTermsPage = bieMenu.openViewEditBusinessTermSubMenu().hitUploadBusinessTermsButton();
        uploadBusinessTermsPage.uploadFile(csvFile.toAbsolutePath().toString());
        uploadBusinessTermsPage.proceedToPreview();
        uploadBusinessTermsPage.hitImportButton();
        String summary = uploadBusinessTermsPage.getResultSummaryText();
        assertTrue(summary.contains("1 created"));
        assertTrue(summary.contains("1 updated"));
    }

    @Test
    @DisplayName("TC_42_1_16")
    public void update_rejects_a_body_that_targets_a_different_id() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        AuthenticatedApiClient api = new AuthenticatedApiClient(getDriver(), getConfig().getBaseUrl());

        BigInteger id = randomBusinessTerm.getBusinessTermId();
        BigInteger otherId = id.add(BigInteger.ONE);
        // #1753 - L2: PUT /business-terms/{id} honors the path id and rejects a body that targets a
        // different id with a 400.
        String mismatchBody = "{\"businessTermId\":" + otherId
                + ",\"businessTerm\":\"" + randomBusinessTerm.getBusinessTerm()
                + "\",\"externalReferenceUri\":\"" + randomBusinessTerm.getExternalReferenceUri() + "\"}";
        assertEquals(400, api.putJson("/api/business-terms/" + id, mismatchBody).statusCode());

        // Positive control: a matching id succeeds (also proves an end-user passes the H1 gate).
        String matchBody = "{\"businessTermId\":" + id
                + ",\"businessTerm\":\"" + randomBusinessTerm.getBusinessTerm()
                + "\",\"externalReferenceUri\":\"" + randomBusinessTerm.getExternalReferenceUri() + "\"}";
        assertEquals(204, api.putJson("/api/business-terms/" + id, matchBody).statusCode());
    }

    @Test
    @DisplayName("TC_42_1_17")
    public void catalog_uniqueness_is_enforced_server_side() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessTermObject existing = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
        BusinessTermObject other = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        AuthenticatedApiClient api = new AuthenticatedApiClient(getDriver(), getConfig().getBaseUrl());

        // #1754 - catalog uniqueness is enforced server-side, not only in the UI. A business term is
        // uniquely identified by the (name + External Reference URI) pair, so a direct create that
        // duplicates that pair is rejected with HTTP 400.
        String duplicatePairBody = "{\"businessTerm\":\"" + existing.getBusinessTerm()
                + "\",\"externalReferenceUri\":\"" + existing.getExternalReferenceUri() + "\"}";
        assertEquals(400, api.postJson("/api/business-terms", duplicatePairBody).statusCode());

        // A create that reuses an existing name but supplies a DIFFERENT External Reference URI is a
        // distinct term and is accepted (name alone is not a uniqueness key).
        String sameNameNewUriBody = "{\"businessTerm\":\"" + existing.getBusinessTerm()
                + "\",\"externalReferenceUri\":\"https://example.org/"
                + RandomStringUtils.secure().nextAlphanumeric(10) + "\"}";
        assertEquals(200, api.postJson("/api/business-terms", sameNameNewUriBody).statusCode());

        // An update that points one record at another's (name + URI) pair is rejected with HTTP 400.
        String updateToDuplicatePair = "{\"businessTermId\":" + other.getBusinessTermId()
                + ",\"businessTerm\":\"" + existing.getBusinessTerm()
                + "\",\"externalReferenceUri\":\"" + existing.getExternalReferenceUri() + "\"}";
        assertEquals(400, api.putJson("/api/business-terms/" + other.getBusinessTermId(), updateToDuplicatePair).statusCode());

        // An update that collides only on name while keeping its own distinct URI is accepted.
        String updateSameNameOwnUri = "{\"businessTermId\":" + other.getBusinessTermId()
                + ",\"businessTerm\":\"" + existing.getBusinessTerm()
                + "\",\"externalReferenceUri\":\"" + other.getExternalReferenceUri() + "\"}";
        assertEquals(204, api.putJson("/api/business-terms/" + other.getBusinessTermId(), updateSameNameOwnUri).statusCode());
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random accounts
        this.randomAccounts.forEach(randomAccount -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId());
        });
    }

}
