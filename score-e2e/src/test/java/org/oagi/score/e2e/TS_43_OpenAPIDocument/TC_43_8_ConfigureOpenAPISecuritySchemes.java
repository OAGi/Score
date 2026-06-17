package org.oagi.score.e2e.TS_43_OpenAPIDocument;

import org.apache.commons.lang3.RandomStringUtils;
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
import org.oagi.score.e2e.page.oas.OasSecurityRequirementDialog;
import org.oagi.score.e2e.page.oas.OasSecuritySchemeDialog;
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
import static org.oagi.score.e2e.impl.PageHelper.getSnackBarMessage;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.sendKeys;

/**
 * Test Case 43.8 - Configure OpenAPI Security Schemes (Issue #1729).
 * <p>
 * The Edit OpenAPI Document page lets the end user declare named Security Schemes (apiKey | http |
 * oauth2 | openIdConnect), a document-level (root) security requirement, and per-operation security
 * overrides. The generated OpenAPI document emits {@code components.securitySchemes}, a root-level
 * {@code security}, and per-operation {@code security}; with no configured scheme it falls back to
 * the legacy single OAuth2 scheme.
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_43_8_ConfigureOpenAPISecuritySchemes extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_43_8_1")
    public void security_schemes_section_is_shown_with_defaults() {
        EditOpenAPIDocumentPage editPage = newDocument();

        assertTrue(editPage.isSecuritySchemesSectionDisplayed(),
                "The 'Security Schemes' section should be displayed");
        assertNotNull(editPage.getAddSecuritySchemeButton(), "An 'Add Security Scheme' button should be present");
        assertEquals("None", editPage.getDocumentSecuritySummary(),
                "With no requirement the Document Security button reads 'Document Security: None'");
        assertFalse(editPage.isDocumentSecurityButtonEnabled(),
                "The Document Security button is disabled while no scheme and no requirement exist");
        assertTrue(editPage.isNoSchemeHintDisplayed(), "The no-scheme hint should be displayed");
        assertTrue(editPage.getNoSchemeHint().contains("default OAuth 2.0 scheme"),
                "The hint should mention the default OAuth 2.0 scheme (was: " + editPage.getNoSchemeHint() + ")");
    }

    @Test
    @DisplayName("TC_43_8_2")
    public void add_scheme_dialog_offers_the_four_types_and_common_fields() {
        EditOpenAPIDocumentPage editPage = newDocument();
        OasSecuritySchemeDialog dialog = editPage.openAddSecuritySchemeDialog();

        assertEquals("Add Security Scheme", getText(dialog.getTitle()));
        List<String> typeOptions = dialog.getTypeOptions();
        assertTrue(typeOptions.containsAll(Arrays.asList("API Key", "HTTP", "OAuth 2.0", "OpenID Connect")),
                "Type options should be API Key, HTTP, OAuth 2.0, OpenID Connect (were: " + typeOptions + ")");
        assertFalse(dialog.getSchemeName().isEmpty(), "A Scheme Name field with a default value should be present");
        dialog.setDescription("API key based authentication"); // exercises the Description field
        dialog.cancel();
    }

    @Test
    @DisplayName("TC_43_8_3")
    public void api_key_type_reveals_in_and_name_and_enables_add() {
        EditOpenAPIDocumentPage editPage = newDocument();
        OasSecuritySchemeDialog dialog = editPage.openAddSecuritySchemeDialog();

        // The dialog defaults to API Key.
        assertTrue(dialog.isInFieldDisplayed(), "An 'In' selector should be displayed for API Key");
        List<String> inOptions = dialog.getInOptions();
        assertTrue(inOptions.containsAll(Arrays.asList("Query", "Header", "Cookie")),
                "In options should be Query, Header, Cookie (were: " + inOptions + ")");
        assertEquals("ApiKeyAuth", dialog.getSchemeName(), "Scheme Name should default to ApiKeyAuth");
        // In and Name are pre-seeded, so Add is enabled.
        assertTrue(dialog.isPrimaryButtonEnabled(), "Add should be enabled once In and Name are filled");
        // Clearing the Name disables Add; refilling re-enables it.
        dialog.clearApiKeyName();
        assertFalse(dialog.isPrimaryButtonEnabled(), "Add should be disabled while the apiKey Name is blank");
        dialog.setApiKeyName("X-API-Key");
        assertTrue(dialog.isPrimaryButtonEnabled(), "Add should re-enable once the apiKey Name is filled");
        dialog.cancel();
    }

    @Test
    @DisplayName("TC_43_8_4")
    public void http_type_bearer_reveals_bearer_format_basic_hides_it() {
        EditOpenAPIDocumentPage editPage = newDocument();
        OasSecuritySchemeDialog dialog = editPage.openAddSecuritySchemeDialog();
        dialog.setType("HTTP");

        dialog.setHttpScheme("Bearer");
        assertTrue(dialog.isBearerFormatFieldDisplayed(), "Bearer scheme should reveal a Bearer Format field");
        assertEquals("BearerAuth", dialog.getSchemeName(), "Bearer scheme should default the name to BearerAuth");

        dialog.setHttpScheme("Basic");
        assertFalse(dialog.isBearerFormatFieldDisplayed(), "Basic scheme should hide the Bearer Format field");
        assertEquals("BasicAuth", dialog.getSchemeName(), "Basic scheme should default the name to BasicAuth");
        dialog.cancel();
    }

    @Test
    @DisplayName("TC_43_8_5")
    public void oauth2_type_reveals_seeded_flows_editor() {
        EditOpenAPIDocumentPage editPage = newDocument();
        OasSecuritySchemeDialog dialog = editPage.openAddSecuritySchemeDialog();
        dialog.setType("OAuth 2.0");

        assertTrue(dialog.isOAuthFlowsSectionDisplayed(), "An 'OAuth Flows' editor should be displayed");
        assertEquals("OAuth2", dialog.getSchemeName(), "OAuth 2.0 should default the name to OAuth2");
        assertEquals(1, dialog.getFlowCount(), "The editor should be seeded with one flow");
        assertEquals("Authorization Code", dialog.getFlowType(1), "The seeded flow should be Authorization Code");
        assertTrue(dialog.isFlowAuthorizationUrlDisplayed(1), "The Authorization Code flow should show an Authorization URL");
        assertTrue(dialog.isFlowTokenUrlDisplayed(1), "The Authorization Code flow should show a Token URL");
        assertTrue(dialog.getFlowScopeNames(1).containsAll(Arrays.asList("read", "write", "admin")),
                "The seeded flow should declare read/write/admin scopes (were: " + dialog.getFlowScopeNames(1) + ")");

        dialog.addFlow();
        assertEquals(2, dialog.getFlowCount(), "Adding a flow should produce a second flow card");

        dialog.addScope(1);
        assertEquals(4, dialog.getFlowScopeNames(1).size(), "Adding a scope should append a scope row");
        dialog.removeLastScope(1);
        assertEquals(3, dialog.getFlowScopeNames(1).size(), "Removing a scope should drop the last scope row");
        dialog.cancel();
    }

    @Test
    @DisplayName("TC_43_8_6")
    public void openid_connect_type_reveals_url_and_gates_add() {
        EditOpenAPIDocumentPage editPage = newDocument();
        OasSecuritySchemeDialog dialog = editPage.openAddSecuritySchemeDialog();
        dialog.setType("OpenID Connect");

        assertTrue(dialog.isOpenIdConnectUrlFieldDisplayed(), "An 'OpenID Connect URL' field should be displayed");
        assertEquals("OpenID", dialog.getSchemeName(), "OpenID Connect should default the name to OpenID");
        // The URL is pre-seeded, so Add is enabled; clearing it disables Add until a URL is provided.
        dialog.clearOpenIdConnectUrl();
        assertFalse(dialog.isPrimaryButtonEnabled(), "Add should be disabled while the OpenID Connect URL is blank");
        dialog.setOpenIdConnectUrl("https://issuer.example.com/.well-known/openid-configuration");
        assertTrue(dialog.isPrimaryButtonEnabled(), "Add should enable once the OpenID Connect URL is provided");
        dialog.cancel();
    }

    @Test
    @DisplayName("TC_43_8_7")
    public void confirmed_scheme_becomes_a_card_that_edits_and_removes() {
        EditOpenAPIDocumentPage editPage = newDocument();
        addApiKeyScheme(editPage);

        assertTrue(editPage.hasSecuritySchemeCard("ApiKeyAuth"), "A card should appear for the added scheme");
        assertEquals("API Key", editPage.getSecuritySchemeCardType("ApiKeyAuth"), "The card should show the type label");
        assertTrue(editPage.getSecuritySchemeCardSummary("ApiKeyAuth").contains("header"),
                "The card summary should reflect the configured fields (was: "
                        + editPage.getSecuritySchemeCardSummary("ApiKeyAuth") + ")");

        OasSecuritySchemeDialog editDialog = editPage.clickSecuritySchemeCard("ApiKeyAuth");
        assertEquals("Edit Security Scheme", getText(editDialog.getTitle()), "The card should reopen in edit mode");
        assertEquals("ApiKeyAuth", editDialog.getSchemeName(), "The edit dialog should carry the saved Scheme Name");
        assertEquals("API Key", editDialog.getType(), "The edit dialog should carry the saved Type");
        editDialog.cancel();

        editPage.removeSecuritySchemeCard("ApiKeyAuth");
        assertFalse(editPage.hasSecuritySchemeCard("ApiKeyAuth"), "Removing the card should delete the scheme");
    }

    @Test
    @DisplayName("TC_43_8_8")
    public void primary_button_is_disabled_for_invalid_schemes() {
        EditOpenAPIDocumentPage editPage = newDocument();
        addApiKeyScheme(editPage); // an existing 'ApiKeyAuth' so a duplicate can be tested

        OasSecuritySchemeDialog dialog = editPage.openAddSecuritySchemeDialog();
        // Blank name.
        dialog.setSchemeName("");
        assertFalse(dialog.isPrimaryButtonEnabled(), "Add should be disabled while the Scheme Name is blank");
        // Duplicate name.
        dialog.setSchemeName("ApiKeyAuth");
        assertFalse(dialog.isPrimaryButtonEnabled(), "Add should be disabled while the Scheme Name duplicates another");
        // Unique name re-enables.
        dialog.setSchemeName("ApiKeyAuth2");
        assertTrue(dialog.isPrimaryButtonEnabled(), "Add should enable for a unique, complete scheme");
        // apiKey required field missing (Name).
        dialog.clearApiKeyName();
        assertFalse(dialog.isPrimaryButtonEnabled(), "Add should be disabled while the apiKey Name is missing");
        dialog.setApiKeyName("X-API-Key");
        assertTrue(dialog.isPrimaryButtonEnabled());
        // openIdConnect required field missing (URL).
        dialog.setType("OpenID Connect");
        dialog.clearOpenIdConnectUrl();
        assertFalse(dialog.isPrimaryButtonEnabled(), "Add should be disabled while the OpenID Connect URL is missing");
        dialog.cancel();
    }

    @Test
    @DisplayName("TC_43_8_9")
    public void document_security_requirement_can_be_built_and_summarized() {
        EditOpenAPIDocumentPage editPage = newDocument();
        addApiKeyScheme(editPage);
        addHttpBearerScheme(editPage);

        OasSecurityRequirementDialog dialog = editPage.openDocumentSecurityDialog();
        // Requirement 1: ApiKeyAuth AND BearerAuth.
        dialog.setRequirementScheme(1, 1, "ApiKeyAuth");
        dialog.addAndScheme(1);
        dialog.setRequirementScheme(1, 2, "BearerAuth");
        // Requirement 2 (OR alternative): anonymous.
        dialog.addAlternative();
        dialog.setAnonymous(2, true);
        assertTrue(dialog.isApplyEnabled(), "A complete requirement set should be applicable");
        dialog.hitApply();

        String summary = editPage.getDocumentSecuritySummary();
        assertTrue(summary.contains("ApiKeyAuth") && summary.contains("BearerAuth"),
                "The summary should reflect the AND-joined schemes (was: " + summary + ")");
        assertTrue(summary.contains("OR"), "The summary should reflect the OR alternative (was: " + summary + ")");
        assertTrue(summary.contains("anonymous"), "The summary should reflect the anonymous alternative (was: " + summary + ")");
    }

    @Test
    @DisplayName("TC_43_8_10")
    public void duplicate_or_alternatives_warn_and_block_apply() {
        EditOpenAPIDocumentPage editPage = newDocument();
        addApiKeyScheme(editPage);
        addHttpBearerScheme(editPage);

        OasSecurityRequirementDialog dialog = editPage.openDocumentSecurityDialog();
        dialog.setRequirementScheme(1, 1, "ApiKeyAuth");
        dialog.addAlternative();
        // Make the second alternative identical to the first.
        dialog.setRequirementScheme(2, 1, "ApiKeyAuth");

        assertTrue(dialog.isDuplicateWarningDisplayed(), "Identical OR alternatives should raise a duplicate warning");
        assertTrue(dialog.getDuplicateWarningText().contains("Duplicate of Requirement 1"),
                "The warning should reference the duplicated requirement (was: " + dialog.getDuplicateWarningText() + ")");
        assertFalse(dialog.isApplyEnabled(), "Apply should be disabled while a duplicate requirement exists");
        dialog.cancel();
    }

    @Test
    @DisplayName("TC_43_8_11")
    public void operation_security_override_and_public_change_the_cell_summary() {
        EditOpenAPIDocumentPage editPage = newDocument();
        addApiKeyScheme(editPage);
        addBodylessOperation(editPage, "DELETE", "/alpha/{id}", "deleteAlpha");
        addBodylessOperation(editPage, "DELETE", "/beta/{id}", "deleteBeta");

        WebElement row1 = editPage.getTableRecordAtIndex(1);
        assertEquals("Inherited", editPage.getRowSecuritySummary(row1),
                "An operation defaults to inheriting the document security");

        OasSecurityRequirementDialog dialog = editPage.openOperationSecurityDialog(row1);
        assertTrue(dialog.hasModeRadioGroup(), "The operation dialog should offer the inherit/none/custom modes");
        dialog.setMode("Override with selected schemes");
        dialog.setRequirementScheme(1, 1, "ApiKeyAuth");
        dialog.hitApply();
        assertEquals("ApiKeyAuth", editPage.getRowSecuritySummary(row1),
                "Overriding with a scheme should change the cell summary from Inherited to the scheme");

        WebElement row2 = editPage.getTableRecordAtIndex(2);
        OasSecurityRequirementDialog dialog2 = editPage.openOperationSecurityDialog(row2);
        dialog2.setMode("No security for this operation");
        dialog2.hitApply();
        assertEquals("Public", editPage.getRowSecuritySummary(row2),
                "Choosing 'No security' should show Public");
    }

    @Test
    @DisplayName("TC_43_8_12")
    public void generated_components_security_schemes_carry_type_specific_fields() {
        EditOpenAPIDocumentPage editPage = newDocument();
        addApiKeyScheme(editPage);
        addHttpBearerScheme(editPage);
        addOauth2Scheme(editPage);
        addOpenIdScheme(editPage);
        addBodylessOperation(editPage, "DELETE", "/widget/{id}", "deleteWidget");
        editPage.hitUpdateButton();

        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(editPage.clickGenerateAndDownload());

        assertTrue(export.securitySchemeNames().containsAll(
                        Arrays.asList("ApiKeyAuth", "BearerAuth", "OAuth2", "OpenID")),
                "All configured schemes should be emitted (were: " + export.securitySchemeNames() + ")");
        // apiKey
        assertEquals("apiKey", export.securitySchemeType("ApiKeyAuth"));
        assertEquals("header", export.securitySchemeField("ApiKeyAuth", "in"));
        assertEquals("X-API-Key", export.securitySchemeField("ApiKeyAuth", "name"));
        // http
        assertEquals("http", export.securitySchemeType("BearerAuth"));
        assertEquals("bearer", export.securitySchemeField("BearerAuth", "scheme"));
        // oauth2
        assertEquals("oauth2", export.securitySchemeType("OAuth2"));
        assertTrue(export.oauth2FlowScopes("OAuth2", "authorizationCode").keySet()
                        .containsAll(Arrays.asList("read", "write", "admin")),
                "The oauth2 scheme should emit its flow scopes (were: "
                        + export.oauth2FlowScopes("OAuth2", "authorizationCode").keySet() + ")");
        // openIdConnect
        assertEquals("openIdConnect", export.securitySchemeType("OpenID"));
        Object oidcUrl = export.securitySchemeField("OpenID", "openIdConnectUrl");
        assertNotNull(oidcUrl, "The openIdConnect scheme should emit an openIdConnectUrl");
        assertFalse(String.valueOf(oidcUrl).isEmpty());
    }

    @Test
    @DisplayName("TC_43_8_13")
    public void root_and_per_operation_security_are_emitted() {
        EditOpenAPIDocumentPage editPage = newDocument();
        addApiKeyScheme(editPage);
        addHttpBearerScheme(editPage);
        addBodylessOperation(editPage, "DELETE", "/alpha/{id}", "deleteAlpha");
        addBodylessOperation(editPage, "DELETE", "/beta/{id}", "deleteBeta");

        // Document-level requirement: ApiKeyAuth.
        OasSecurityRequirementDialog docDialog = editPage.openDocumentSecurityDialog();
        docDialog.setRequirementScheme(1, 1, "ApiKeyAuth");
        docDialog.hitApply();

        WebElement row1 = editPage.getTableRecordAtIndex(1);
        String overriddenPath = editPage.getRowResourceName(row1);
        WebElement row2 = editPage.getTableRecordAtIndex(2);
        String inheritedPath = editPage.getRowResourceName(row2);

        // Override the first operation with BearerAuth; leave the second inheriting.
        OasSecurityRequirementDialog opDialog = editPage.openOperationSecurityDialog(row1);
        opDialog.setMode("Override with selected schemes");
        opDialog.setRequirementScheme(1, 1, "BearerAuth");
        opDialog.hitApply();

        editPage.hitUpdateButton();
        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(editPage.clickGenerateAndDownload());

        assertTrue(export.hasRootSecurity(), "A root-level security requirement should be emitted");
        assertEquals(export.rootKeyIndex("info") + 1, export.rootKeyIndex("security"),
                "The root security should be placed right after info (keys: " + export.rootKeys() + ")");
        assertTrue(export.securityListReferencesScheme(export.rootSecurity(), "ApiKeyAuth"),
                "The root security should reference the document-level scheme");

        assertTrue(export.operationHasSecurity(overriddenPath, "delete"),
                "The overridden operation should carry a per-operation security");
        assertTrue(export.securityListReferencesScheme(
                        export.operationSecurityList(overriddenPath, "delete"), "BearerAuth"),
                "The per-operation override should reference the chosen scheme");
        assertFalse(export.operationHasSecurity(inheritedPath, "delete"),
                "An operation that inherits document security should emit no per-operation security key");
    }

    @Test
    @DisplayName("TC_43_8_14")
    public void no_configured_scheme_falls_back_to_legacy_oauth2() {
        BieFixture fixture = newDocumentWithBie();
        EditOpenAPIDocumentPage editPage = fixture.editPage;
        assignBie(editPage, fixture.bie, "POST", "Request");

        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(editPage.clickGenerateAndDownload());

        assertEquals(1, export.securitySchemeNames().size(),
                "With no configured scheme exactly one (legacy) scheme should be emitted (were: "
                        + export.securitySchemeNames() + ")");
        assertTrue(export.securitySchemeNames().contains("OAuth2"), "The legacy scheme should be named OAuth2");
        assertEquals("oauth2", export.securitySchemeType("OAuth2"));
        assertFalse(export.oauth2FlowScopes("OAuth2", "authorizationCode").isEmpty(),
                "The legacy OAuth2 scheme should declare accumulated per-BIE scopes");
        assertFalse(export.hasRootSecurity(), "The legacy default emits no root-level security");
        List<String> opSchemeNames = export.operationSecuritySchemeNamesUnderPaths();
        assertFalse(opSchemeNames.isEmpty(), "Every operation should carry a per-operation security in legacy mode");
        assertTrue(opSchemeNames.stream().allMatch("OAuth2"::equals),
                "Legacy per-operation security should reference only OAuth2 (were: " + opSchemeNames + ")");
        assertFalse(export.operationSecurityScopesUnderPaths("OAuth2").isEmpty(),
                "Legacy per-operation security should carry OAuth2 scopes");
    }

    @Test
    @DisplayName("TC_43_8_15")
    public void operation_override_survives_a_later_unrelated_update() {
        EditOpenAPIDocumentPage editPage = newDocument();
        addApiKeyScheme(editPage);
        addBodylessOperation(editPage, "DELETE", "/thing/{id}", "deleteThing");

        WebElement row = editPage.getTableRecordAtIndex(1);
        String path = editPage.getRowResourceName(row);
        OasSecurityRequirementDialog opDialog = editPage.openOperationSecurityDialog(row);
        opDialog.setMode("Override with selected schemes");
        opDialog.setRequirementScheme(1, 1, "ApiKeyAuth");
        opDialog.hitApply();
        editPage.hitUpdateButton();

        // A later Update that changes only the document Description must not wipe the operation override.
        editPage.setDescription("Updated " + RandomStringUtils.secure().nextAlphanumeric(6));
        editPage.hitUpdateButton();

        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(editPage.clickGenerateAndDownload());
        assertTrue(export.operationHasSecurity(path, "delete"),
                "The operation override should survive the later unrelated Update");
        assertTrue(export.securityListReferencesScheme(export.operationSecurityList(path, "delete"), "ApiKeyAuth"),
                "The surviving override should still reference the configured scheme rather than turning Public");
    }

    @Test
    @DisplayName("TC_43_8_16")
    public void valid_security_update_shows_a_single_updated_message() {
        EditOpenAPIDocumentPage editPage = newDocument();
        addApiKeyScheme(editPage);
        addBodylessOperation(editPage, "DELETE", "/order/{id}", "deleteOrder");

        editPage.hitUpdateButton();
        assertEquals("Updated", getSnackBarMessage(getDriver()),
                "A valid security Update should show a single 'Updated' confirmation");
        // Note: the Update button is also blocked while a scheme/requirement is incomplete; that guard is
        // enforced at the dialog level (an incomplete scheme cannot be saved) — see TC_43_8_8.
    }

    /* ------------------------------------------------------------------ helpers */

    private String addApiKeyScheme(EditOpenAPIDocumentPage editPage) {
        OasSecuritySchemeDialog dialog = editPage.openAddSecuritySchemeDialog();
        // Defaults: type apiKey, name ApiKeyAuth, In header, Name X-API-Key (all valid).
        String name = dialog.getSchemeName();
        dialog.hitPrimaryButton();
        return name;
    }

    private String addHttpBearerScheme(EditOpenAPIDocumentPage editPage) {
        OasSecuritySchemeDialog dialog = editPage.openAddSecuritySchemeDialog();
        dialog.setType("HTTP"); // seeds scheme bearer + name BearerAuth
        String name = dialog.getSchemeName();
        dialog.hitPrimaryButton();
        return name;
    }

    private String addOauth2Scheme(EditOpenAPIDocumentPage editPage) {
        OasSecuritySchemeDialog dialog = editPage.openAddSecuritySchemeDialog();
        dialog.setType("OAuth 2.0"); // seeds an Authorization Code flow + name OAuth2
        String name = dialog.getSchemeName();
        dialog.hitPrimaryButton();
        return name;
    }

    private String addOpenIdScheme(EditOpenAPIDocumentPage editPage) {
        OasSecuritySchemeDialog dialog = editPage.openAddSecuritySchemeDialog();
        dialog.setType("OpenID Connect"); // seeds an issuer URL + name OpenID
        String name = dialog.getSchemeName();
        dialog.hitPrimaryButton();
        return name;
    }

    private void addBodylessOperation(EditOpenAPIDocumentPage editPage, String verb,
                                      String resourceName, String operationId) {
        AddOperationForOpenAPIDocumentDialog dialog = editPage.openAddOperationDialog();
        dialog.setVerb(verb);
        dialog.setResourceName(resourceName);
        if (operationId != null) {
            dialog.setOperationId(operationId);
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

    private EditOpenAPIDocumentPage newDocument() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        OpenAPIDocumentObject openAPIDocument =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);
        return openEditOpenAPIDocumentPage(endUser, openAPIDocument);
    }

    private BieFixture newDocumentWithBie() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI()
                .getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        BusinessContextObject businessContext = getAPIFactory().getBusinessContextAPI()
                .createRandomBusinessContext(endUser, "oas_security_bc");
        TopLevelASBIEPObject bie = createRandomTopLevelBie(endUser, release, namespace, businessContext);

        OpenAPIDocumentObject openAPIDocument =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);
        EditOpenAPIDocumentPage editPage = openEditOpenAPIDocumentPage(endUser, openAPIDocument);
        return new BieFixture(editPage, bie);
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

    @AfterEach
    public void tearDown() {
        super.tearDown();

        this.randomAccounts.forEach(randomAccount -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId());
        });
    }

    private static class BieFixture {
        private final EditOpenAPIDocumentPage editPage;
        private final TopLevelASBIEPObject bie;

        private BieFixture(EditOpenAPIDocumentPage editPage, TopLevelASBIEPObject bie) {
            this.editPage = editPage;
            this.bie = bie;
        }
    }
}
