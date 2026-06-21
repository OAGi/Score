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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.getSnackBarMessage;
import static org.oagi.score.e2e.impl.PageHelper.sendKeys;

/**
 * Test Case 43.6 - Operation Identifier Naming (Issue #1732).
 * <p>
 * The operationId is built as {@code <verbWord><BIEName>[List]} with no business-context prefix and
 * no separators; it live-updates on Verb/Array changes, is freely editable, must be unique and
 * non-blank within the document, and is emitted verbatim in the generated OpenAPI document.
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_43_6_OperationIdNaming extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_43_6_1")
    public void verb_word_maps_correctly_for_every_verb() {
        EditOpenAPIDocumentPage editPage = assignPost();

        WebElement row = editPage.getTableRecordAtIndex(1);
        String createId = editPage.getRowOperationId(row);
        assertTrue(createId.startsWith("create"), "POST should map to 'create' (was: " + createId + ")");

        String name = createId.substring("create".length());

        editPage.setRowVerb(row, "GET");
        assertEquals("query" + name, editPage.getRowOperationId(row));

        editPage.setRowVerb(row, "PUT");
        assertEquals("replace" + name, editPage.getRowOperationId(row));

        editPage.setRowVerb(row, "PATCH");
        assertEquals("update" + name, editPage.getRowOperationId(row));

        editPage.setRowVerb(row, "DELETE");
        assertEquals("delete" + name, editPage.getRowOperationId(row));
    }

    @Test
    @DisplayName("TC_43_6_2")
    public void operation_id_has_no_business_context_prefix_or_separator() {
        EditOpenAPIDocumentPage editPage = assignPost();

        String operationId = editPage.getRowOperationId(editPage.getTableRecordAtIndex(1));
        assertTrue(operationId.startsWith("create"), "Operation ID should be the verb word + BIE name");
        assertFalse(operationId.contains("_"), "Operation ID should contain no underscore separator");
        assertFalse(operationId.contains(" "), "Operation ID should contain no whitespace separator");
    }

    @Test
    @DisplayName("TC_43_6_3")
    public void array_indicator_adds_list_suffix() {
        EditOpenAPIDocumentPage editPage = assignPost();

        WebElement row = editPage.getTableRecordAtIndex(1);
        editPage.setRowArrayIndicator(row, true);
        assertTrue(editPage.getRowOperationId(row).endsWith("List"),
                "An array operation's Operation ID should end with 'List'");
    }

    @Test
    @DisplayName("TC_43_6_4")
    public void changing_verb_swaps_only_the_verb_word_and_preserves_the_bie_name() {
        EditOpenAPIDocumentPage editPage = assignPost();

        WebElement row = editPage.getTableRecordAtIndex(1);
        String createId = editPage.getRowOperationId(row);
        String name = createId.substring("create".length());

        editPage.setRowVerb(row, "GET");
        assertEquals("query" + name, editPage.getRowOperationId(row),
                "Changing the Verb should swap only the leading verb word and preserve the BIE-name segment");
    }

    @Test
    @DisplayName("TC_43_6_5")
    public void toggling_array_indicator_adds_and_removes_the_list_suffix() {
        EditOpenAPIDocumentPage editPage = assignPost();

        WebElement row = editPage.getTableRecordAtIndex(1);
        editPage.setRowArrayIndicator(row, true);
        assertTrue(editPage.getRowOperationId(row).endsWith("List"));

        editPage.setRowArrayIndicator(row, false);
        assertFalse(editPage.getRowOperationId(row).endsWith("List"));
    }

    @Test
    @DisplayName("TC_43_6_6")
    public void operation_id_is_a_free_text_input() {
        EditOpenAPIDocumentPage editPage = assignPost();

        WebElement row = editPage.getTableRecordAtIndex(1);
        editPage.setRowOperationId(row, "myCustomOperationId");
        assertEquals("myCustomOperationId", editPage.getRowOperationId(row));
    }

    @Test
    @DisplayName("TC_43_6_7")
    public void duplicate_operation_id_is_flagged_with_an_inline_error() {
        Scenario scenario = newScenario();
        EditOpenAPIDocumentPage editPage = openEditOpenAPIDocumentPage(scenario.endUser, scenario.openAPIDocument);
        assignBie(editPage, scenario.bie, "POST", "Request");
        assignBie(editPage, scenario.bie, "POST", "Request");

        WebElement firstRow = editPage.getTableRecordAtIndex(1);
        WebElement secondRow = editPage.getTableRecordAtIndex(2);
        String firstOperationId = editPage.getRowOperationId(firstRow);

        editPage.setRowOperationId(secondRow, firstOperationId);
        assertEquals("Operation ID must be unique within the document.",
                editPage.getRowOperationIdError(secondRow));
    }

    @Test
    @DisplayName("TC_43_6_8")
    public void blank_operation_id_is_flagged_with_an_inline_error() {
        EditOpenAPIDocumentPage editPage = assignPost();

        WebElement row = editPage.getTableRecordAtIndex(1);
        editPage.setRowOperationId(row, "");
        assertEquals("Operation ID is required.", editPage.getRowOperationIdError(row));
    }

    @Test
    @DisplayName("TC_43_6_9")
    public void saving_with_a_blank_operation_id_is_blocked() {
        EditOpenAPIDocumentPage editPage = assignPost();

        WebElement row = editPage.getTableRecordAtIndex(1);
        editPage.setRowOperationId(row, "");
        click(editPage.getUpdateButton(true));

        assertEquals("Operation ID is required.", getSnackBarMessage(getDriver()));
    }

    @Test
    @DisplayName("TC_43_6_10")
    public void edited_operation_id_persists_and_appears_verbatim_in_the_generated_document() {
        EditOpenAPIDocumentPage editPage = assignPost();

        WebElement row = editPage.getTableRecordAtIndex(1);
        editPage.setRowOperationId(row, "customCreateWidget");
        editPage.hitUpdateButton();

        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(editPage.clickGenerateAndDownload());
        assertTrue(export.operationIds().contains("customCreateWidget"),
                "The edited Operation ID should appear verbatim in the generated OpenAPI document");
    }

    private EditOpenAPIDocumentPage assignPost() {
        Scenario scenario = newScenario();
        EditOpenAPIDocumentPage editPage = openEditOpenAPIDocumentPage(scenario.endUser, scenario.openAPIDocument);
        assignBie(editPage, scenario.bie, "POST", "Request");
        return editPage;
    }

    private Scenario newScenario() {
        Scenario scenario = new Scenario();
        scenario.endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(scenario.endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI()
                .getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        BusinessContextObject businessContext = getAPIFactory().getBusinessContextAPI()
                .createRandomBusinessContext(scenario.endUser, "oas_opid_bc");

        scenario.bie = createRandomTopLevelBie(scenario.endUser, release, namespace, businessContext);
        scenario.openAPIDocument = getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(scenario.endUser);
        return scenario;
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

    private static class Scenario {
        private AppUserObject endUser;
        private OpenAPIDocumentObject openAPIDocument;
        private TopLevelASBIEPObject bie;
    }
}
