package org.oagi.score.e2e.TS_46_BIEPackageManagement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.BusinessInformationEntityAPI;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.obj.ACCObject;
import org.oagi.score.e2e.obj.ASCCPObject;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.BCCPObject;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.DTObject;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.sendKeys;

/**
 * Test Suite 46 / Test Case 46.3 - Array rendering follows the based component's cardinality (issue #1733).
 *
 * <p>Companion to {@link TC_46_2_BackwardCompatibilityIndicator}: where TC_46.2 asserts that the BIE Package
 * manifest's backward-compatibility <em>indicator</em> reflects the new rule, this case asserts that the
 * OpenAPI <em>generator</em> actually applies it. Since issue #1733 the array-vs-object decision for a nested
 * child follows the based ASCC/BCC maximum cardinality, not the (possibly narrowed) BIE max. So a child whose
 * based BCC is unbounded must still render as a JSON {@code type: array} even when the BIE narrows its own max
 * to 1; under the pre-#1733 rule (which read the BIE max) it would have rendered as a bare value.</p>
 *
 * <p>The scenario is built through the e2e API (no editor interaction for the structure): a fresh ASCCP carrying
 * a single text element BCC (whose own default cardinality is {@code 0..unbounded}), a BIE on that ASCCP, the
 * child BBIE materialized from the BCC (inheriting the unbounded based max), then the BBIE's own max narrowed to
 * 1 via {@link BusinessInformationEntityAPI#seedAllBbieProfiling}. The BIE is assigned to an OpenAPI Document and
 * the document is generated (exercising {@code OpenAPIGenerateExpression}); the downloaded YAML is then parsed and
 * the nested child's rendered {@code type} inspected.</p>
 *
 * <p>Runs in {@link ExecutionMode#SAME_THREAD}, matching the rest of TS_46 and the stabilized TS_43 OpenAPI
 * Document suite.</p>
 */
@Execution(ExecutionMode.SAME_THREAD)
public class TC_46_3_ArrayRenderingFollowsBasedCardinality extends BaseTest {

    // The text BDT exists in every connectSpec release; the fixture is built in the latest release so the
    // BIE shows up under the Add-BIE dialog's default (latest) branch without any extra search filtering.
    private static final String TEXT_BDT_GUID = "dd0c8f86b160428da3a82d2866a5b48d";

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        this.randomAccounts.forEach(randomAccount ->
                getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId()));
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    private BusinessInformationEntityAPI bieAPI() {
        return getAPIFactory().getBusinessInformationEntityAPI();
    }

    /**
     * Create a fresh ASCCP whose ACC carries a single text element BCC. The BCC's own cardinality is the
     * {@code createRandomBCC} default of {@code 0..unbounded} (max {@code -1}), which is exactly the based
     * component cardinality this case keys off.
     */
    private ASCCPObject createSharedAsccp(AppUserObject user, LibraryObject library) {
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        CoreComponentAPI cc = getAPIFactory().getCoreComponentAPI();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(user, library);
        ACCObject acc = cc.createRandomACC(user, release, namespace, "Production");
        DTObject textDataType = cc.getBDTByGuidAndReleaseNum(library, TEXT_BDT_GUID, release.getReleaseNumber());
        BCCPObject bccp = cc.createRandomBCCP(release, textDataType, user, namespace, "Production");
        cc.appendBCC(acc, bccp, "Production");
        return cc.createRandomASCCP(acc, user, namespace, "Production");
    }

    private void assignBie(EditOpenAPIDocumentPage editPage, TopLevelASBIEPObject bie, String verb, String messageBody) {
        AddBIEForOpenAPIDocumentDialog dialog = editPage.openAddBIEForOpenAPIDocumentDialog();
        sendKeys(dialog.getInputFieldInSearchBar(), bie.getDen());
        dialog.hitSearchButton();
        WebElement row = dialog.getTableRecordByValue(bie.getDen());
        dialog.toggleSelect(row);
        dialog.setVerb(row, verb);
        dialog.setMessageBody(row, messageBody);
        dialog.hitAddButton();
    }

    @Test
    @DisplayName("TC_46_3_1")
    public void nested_child_renders_as_array_from_based_cardinality_even_when_bie_max_is_one() {
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        AppUserObject user = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(user);

        ASCCPObject asccp = createSharedAsccp(user, library);
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(user);
        TopLevelASBIEPObject bie =
                bieAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, user, "WIP");
        // Materialize the child BBIE from the ACC's element BCC; it inherits the based BCC's unbounded max.
        bieAPI().materializeUsedBbieChildren(bie.getTopLevelAsbiepId(), user.getAppUserId());
        // Narrow the child BBIE's OWN cardinality to 0..1. The based BCC stays unbounded (-1), so #1733 keeps
        // the child rendered as an array; the pre-#1733 rule (reading the BIE max of 1) would make it a scalar.
        bieAPI().seedAllBbieProfiling(bie.getTopLevelAsbiepId(), true, 0, 1, null);

        OpenAPIDocumentObject document = getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(user);

        HomePage homePage = loginPage().signIn(user.getLoginId(), user.getPassword());
        OpenAPIDocumentPage openAPIDocumentPage = homePage.getBIEMenu().openOpenAPIDocumentSubMenu();
        EditOpenAPIDocumentPage editPage = openAPIDocumentPage.openEditOpenAPIDocumentPage(document);
        assignBie(editPage, bie, "POST", "Request");
        // Adding the BIE through the dialog persists the operation, so the document can be generated directly
        // (no array-indicator change is made, so the "Update" button stays disabled and is not needed here).

        File generated = editPage.clickGenerateAndDownload();
        assertNotNull(generated, "An OpenAPI (YAML) document should have been generated");

        OpenAPIDocumentExport export = OpenAPIDocumentExport.from(generated);
        Map<String, String> propertyTypes = export.objectPropertyTypes();

        // The fixture's ACC has exactly one (unbounded-based) element, so the only array-typed property is the
        // child under test: its presence proves the generator decided array-ness from the based BCC max (-1),
        // not from the BIE's narrowed max (1).
        assertTrue(propertyTypes.containsValue("array"),
                "A nested child whose based BCC is unbounded must render as 'type: array' in the generated OpenAPI "
                        + "document even when the BIE narrows its own max to 1 (issue #1733). "
                        + "Schema property types were: " + propertyTypes + "; schema names: " + export.schemaNames());
    }
}
