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
import org.oagi.score.e2e.page.oas.OpenAPIDocumentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.getSnackBarMessage;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.sendKeys;

@Execution(ExecutionMode.CONCURRENT)
public class TC_43_2_AddBIEToOpenAPIDocument extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_43_2_1")
    public void enduser_should_open_dialog_titled_Add_BIE_For_OpenAPI_Document() {
        OpenApiAssignmentScenario scenario = createScenario(false);

        EditOpenAPIDocumentPage editOpenAPIDocumentPage =
                openEditOpenAPIDocumentPage(scenario.endUser, scenario.openAPIDocument);
        AddBIEForOpenAPIDocumentDialog addBIEForOpenAPIDocumentDialog =
                editOpenAPIDocumentPage.openAddBIEForOpenAPIDocumentDialog();

        assertEquals("Add BIE For OpenAPI Document",
                getText(addBIEForOpenAPIDocumentDialog.getTitle().findElement(By.tagName("span"))));
    }

    @Test
    @DisplayName("TC_43_2_2")
    public void enduser_can_view_and_select_candidate_bies_in_add_dialog() {
        OpenApiAssignmentScenario scenario = createScenario(false);

        EditOpenAPIDocumentPage editOpenAPIDocumentPage =
                openEditOpenAPIDocumentPage(scenario.endUser, scenario.openAPIDocument);
        AddBIEForOpenAPIDocumentDialog dialog = editOpenAPIDocumentPage.openAddBIEForOpenAPIDocumentDialog();

        searchByDen(dialog, scenario.targetBie.getDen());

        WebElement row = dialog.getTableRecordByValue(scenario.targetBie.getDen());
        assertNotNull(row);
        assertTrue(getText(dialog.getColumnByName(row, "den")).contains(scenario.targetBie.getDen()));

        dialog.toggleSelect(row);
        assertThrows(TimeoutException.class, () -> dialog.getAddButton(true));
    }

    @Test
    @DisplayName("TC_43_2_3")
    public void enduser_can_filter_candidate_bies_in_add_dialog() {
        OpenApiAssignmentScenario scenario = createScenario(true);

        EditOpenAPIDocumentPage editOpenAPIDocumentPage =
                openEditOpenAPIDocumentPage(scenario.endUser, scenario.openAPIDocument);

        AddBIEForOpenAPIDocumentDialog dialog = editOpenAPIDocumentPage.openAddBIEForOpenAPIDocumentDialog();
        searchByDen(dialog, scenario.targetBie.getDen());

        WebElement targetRow = dialog.getTableRecordByValue(scenario.targetBie.getDen());
        String targetVersion = getText(dialog.getColumnByName(targetRow, "version"));
        String targetRemark = getText(dialog.getColumnByName(targetRow, "remark"));
        String targetBusinessContext = getText(dialog.getColumnByName(targetRow, "businessContexts"));
        dialog.close();

        dialog = editOpenAPIDocumentPage.openAddBIEForOpenAPIDocumentDialog();
        dialog.showAdvancedSearchPanel();
        dialog.setBusinessContext(targetBusinessContext);
        searchByDen(dialog, scenario.targetBie.getDen());
        assertNotNull(dialog.getTableRecordByValue(scenario.targetBie.getDen()));
        dialog.close();

        dialog = editOpenAPIDocumentPage.openAddBIEForOpenAPIDocumentDialog();
        dialog.showAdvancedSearchPanel();
        dialog.setVersion(targetVersion);
        searchByDen(dialog, scenario.targetBie.getDen());
        assertNotNull(dialog.getTableRecordByValue(scenario.targetBie.getDen()));
        dialog.close();

        dialog = editOpenAPIDocumentPage.openAddBIEForOpenAPIDocumentDialog();
        dialog.showAdvancedSearchPanel();
        dialog.setRemark(targetRemark);
        searchByDen(dialog, scenario.targetBie.getDen());
        assertNotNull(dialog.getTableRecordByValue(scenario.targetBie.getDen()));
        dialog.close();

        dialog = editOpenAPIDocumentPage.openAddBIEForOpenAPIDocumentDialog();
        dialog.showAdvancedSearchPanel();
        dialog.setUpdatedStartDate(LocalDateTime.now().plusDays(1));
        searchByDen(dialog, scenario.targetBie.getDen());
        AddBIEForOpenAPIDocumentDialog futureDateDialog = dialog;
        assertThrows(TimeoutException.class, () -> futureDateDialog.getTableRecordByValue(scenario.targetBie.getDen()));
        dialog.close();

        dialog = editOpenAPIDocumentPage.openAddBIEForOpenAPIDocumentDialog();
        dialog.showAdvancedSearchPanel();
        dialog.setUpdatedStartDate(LocalDateTime.now().minusDays(1));
        dialog.setUpdatedEndDate(LocalDateTime.now().plusDays(1));
        searchByDen(dialog, scenario.targetBie.getDen());
        assertNotNull(dialog.getTableRecordByValue(scenario.targetBie.getDen()));
    }

    @Test
    @DisplayName("TC_43_2_4")
    public void enduser_must_choose_supported_verb_and_message_body_before_add_is_enabled() {
        OpenApiAssignmentScenario scenario = createScenario(false);

        EditOpenAPIDocumentPage editOpenAPIDocumentPage =
                openEditOpenAPIDocumentPage(scenario.endUser, scenario.openAPIDocument);
        AddBIEForOpenAPIDocumentDialog dialog = editOpenAPIDocumentPage.openAddBIEForOpenAPIDocumentDialog();

        searchByDen(dialog, scenario.targetBie.getDen());

        WebElement row = dialog.getTableRecordByValue(scenario.targetBie.getDen());
        dialog.toggleSelect(row);

        assertThrows(TimeoutException.class, () -> dialog.getAddButton(true));

        dialog.setVerb(row, "GET");
        assertTrue(dialog.isMessageBodyOptionDisabled(row, "Request"),
                "Request must not be selectable for a GET (a GET never carries a request body)");

        // Issue #1610: a DELETE may carry a Request body (honored in OpenAPI 3.1.1, dropped in 3.0.3),
        // so unlike GET the Request option must remain selectable when the Verb is DELETE.
        dialog.setVerb(row, "DELETE");
        assertFalse(dialog.isMessageBodyOptionDisabled(row, "Request"),
                "Request must be selectable for a DELETE (Issue #1610)");

        dialog.setMessageBody(row, "Response");
        assertNotNull(dialog.getAddButton(true));
    }

    @Test
    @DisplayName("TC_43_2_5")
    public void enduser_can_add_selected_bie_to_current_openapi_document() {
        OpenApiAssignmentScenario scenario = createScenario(false);

        EditOpenAPIDocumentPage editOpenAPIDocumentPage =
                openEditOpenAPIDocumentPage(scenario.endUser, scenario.openAPIDocument);

        assignBie(editOpenAPIDocumentPage, scenario.targetBie, "GET", "Response");

        assertEquals("Added", getSnackBarMessage(getDriver()));
    }

    @Test
    @DisplayName("TC_43_2_6")
    public void enduser_can_view_bie_assignments_attached_to_current_openapi_document() {
        OpenApiAssignmentScenario scenario = createScenario(false);

        EditOpenAPIDocumentPage editOpenAPIDocumentPage =
                openEditOpenAPIDocumentPage(scenario.endUser, scenario.openAPIDocument);

        assignBie(editOpenAPIDocumentPage, scenario.targetBie, "GET", "Response");

        WebElement row = editOpenAPIDocumentPage.getTableRecordByValue(scenario.targetBie.getDen());
        assertNotNull(row);
        assertEquals("GET", getText(editOpenAPIDocumentPage.getColumnByName(row, "verb")));
        assertEquals("Response", getText(editOpenAPIDocumentPage.getColumnByName(row, "messageBody")));
    }

    @Test
    @DisplayName("TC_43_2_7")
    public void enduser_can_remove_selected_bie_assignments_from_current_openapi_document() {
        OpenApiAssignmentScenario scenario = createScenario(false);

        EditOpenAPIDocumentPage editOpenAPIDocumentPage =
                openEditOpenAPIDocumentPage(scenario.endUser, scenario.openAPIDocument);

        assignBie(editOpenAPIDocumentPage, scenario.targetBie, "GET", "Response");

        WebElement row = editOpenAPIDocumentPage.getTableRecordByValue(scenario.targetBie.getDen());
        editOpenAPIDocumentPage.toggleSelect(row);
        editOpenAPIDocumentPage.removeSelectedBIEs();

        assertThrows(TimeoutException.class,
                () -> editOpenAPIDocumentPage.getTableRecordByValue(scenario.targetBie.getDen()));
    }

    @Test
    @DisplayName("TC_43_2_8")
    public void enduser_can_add_same_bie_again_when_operation_combination_differs() {
        OpenApiAssignmentScenario scenario = createScenario(false);

        EditOpenAPIDocumentPage editOpenAPIDocumentPage =
                openEditOpenAPIDocumentPage(scenario.endUser, scenario.openAPIDocument);

        assignBie(editOpenAPIDocumentPage, scenario.targetBie, "GET", "Response");
        assignBie(editOpenAPIDocumentPage, scenario.targetBie, "POST", "Request");

        List<WebElement> rows = getDriver().findElements(By.xpath(
                "//tbody/tr[.//*[contains(normalize-space(.), " +
                        org.oagi.score.e2e.impl.PageHelper.xpathLiteral(scenario.targetBie.getDen()) + ")]]"));
        assertEquals(2, rows.size());
    }

    @Test
    @DisplayName("TC_43_2_9")
    public void enduser_can_add_same_bie_again_when_generated_operation_id_differs() {
        OpenApiAssignmentScenario scenario = createScenario(false);

        EditOpenAPIDocumentPage editOpenAPIDocumentPage =
                openEditOpenAPIDocumentPage(scenario.endUser, scenario.openAPIDocument);

        assignBie(editOpenAPIDocumentPage, scenario.targetBie, "GET", "Response");

        AddBIEForOpenAPIDocumentDialog dialog = editOpenAPIDocumentPage.openAddBIEForOpenAPIDocumentDialog();
        searchByDen(dialog, scenario.targetBie.getDen());

        WebElement row = dialog.getTableRecordByValue(scenario.targetBie.getDen());
        dialog.toggleSelect(row);
        dialog.setVerb(row, "GET");
        dialog.setMessageBody(row, "Response");
        dialog.hitAddButton();

        assertFalse(dialog.isOpened());

        List<WebElement> rows = getDriver().findElements(By.xpath(
                "//tbody/tr[.//*[contains(normalize-space(.), " +
                        org.oagi.score.e2e.impl.PageHelper.xpathLiteral(scenario.targetBie.getDen()) + ")]]"));
        assertEquals(2, rows.size());
    }

    private OpenApiAssignmentScenario createScenario(boolean includeVisibleSecondaryBie) {
        OpenApiAssignmentScenario scenario = new OpenApiAssignmentScenario();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI()
                .getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        scenario.endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(scenario.endUser);

        scenario.openAPIDocument = getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(scenario.endUser);
        scenario.targetBusinessContext = getAPIFactory().getBusinessContextAPI()
                .createRandomBusinessContext(scenario.endUser, "oas_target_bc");

        scenario.targetBie = createRandomTopLevelBie(
                scenario.endUser, release, namespace, scenario.targetBusinessContext, "WIP");

        if (includeVisibleSecondaryBie) {
            scenario.otherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(scenario.otherUser);

            scenario.otherBusinessContext = getAPIFactory().getBusinessContextAPI()
                    .createRandomBusinessContext(scenario.otherUser, "oas_other_bc");

            scenario.otherVisibleBie = createRandomTopLevelBie(
                    scenario.otherUser, release, namespace, scenario.otherBusinessContext, "QA");
        }

        return scenario;
    }

    private TopLevelASBIEPObject createRandomTopLevelBie(AppUserObject owner, ReleaseObject release,
                                                         NamespaceObject namespace, BusinessContextObject context,
                                                         String state) {
        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        ACCObject acc = coreComponentAPI.createRandomACC(owner, release, namespace, "Published");
        ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, owner, namespace, "Published");
        return getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, owner, state);
    }

    private EditOpenAPIDocumentPage openEditOpenAPIDocumentPage(AppUserObject endUser,
                                                                OpenAPIDocumentObject openAPIDocument) {
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        OpenAPIDocumentPage openAPIDocumentPage = bieMenu.openOpenAPIDocumentSubMenu();
        return openAPIDocumentPage.openEditOpenAPIDocumentPage(openAPIDocument);
    }

    private void searchByDen(AddBIEForOpenAPIDocumentDialog dialog, String den) {
        sendKeys(dialog.getInputFieldInSearchBar(), den);
        dialog.hitSearchButton();
    }

    private void assignBie(EditOpenAPIDocumentPage editOpenAPIDocumentPage, TopLevelASBIEPObject bie,
                           String verb, String messageBody) {
        AddBIEForOpenAPIDocumentDialog dialog = editOpenAPIDocumentPage.openAddBIEForOpenAPIDocumentDialog();
        searchByDen(dialog, bie.getDen());

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

    private static class OpenApiAssignmentScenario {
        private AppUserObject endUser;
        private AppUserObject otherUser;
        private OpenAPIDocumentObject openAPIDocument;
        private BusinessContextObject targetBusinessContext;
        private BusinessContextObject otherBusinessContext;
        private TopLevelASBIEPObject targetBie;
        private TopLevelASBIEPObject otherVisibleBie;
    }
}
