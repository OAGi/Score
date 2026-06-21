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
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.BIEPackageManifest;
import org.oagi.score.e2e.page.bie.BIEPackageManifest.BackwardCompatibility;
import org.oagi.score.e2e.page.bie.EditBIEPackagePage;
import org.oagi.score.e2e.page.bie.ViewBIEPackagePage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Suite 46 / Test Case 46.2 - Backward Compatibility Indicator (issue #1733).
 *
 * <p>Verifies the per-BIE {@code backwardCompatibility} object emitted in the generated BIE Package
 * manifest. Per the suite scope note, only the syntax-dependent indicators {@code xmlSchema} and
 * {@code jsonSchema} are asserted ({@code true} = backward compatible, {@code false} = breaks that
 * syntax); {@code syntaxIndependent} is intentionally not asserted because its definition is still
 * under review.</p>
 *
 * <p>Each profiling scenario builds two Production BIEs on the same ASCCP, seeds a single controlled
 * difference into the second one via the e2e jOOQ layer, packages the baseline, revises the package,
 * replaces the baseline with the changed BIE, generates the package, and reads the changed BIE's
 * {@code backwardCompatibility} from {@code manifest.json}.</p>
 */
@Execution(ExecutionMode.SAME_THREAD)
public class TC_46_2_BackwardCompatibilityIndicator extends BaseTest {

    private static final String RELEASE_NUMBER = "10.8.8";
    // The "Text" BDT: default primitive xsd:string, and it also offers xsd:normalizedString and
    // xsd:token, which the value-domain narrowing scenario (#46.2.8) needs. (The cardinality/facet
    // scenarios are primitive-agnostic: they never assign an XBT, so the value-domain rule never fires.)
    private static final String TEXT_BDT_GUID = "89be97039be04d6f9cfda107d75926b5";

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

    /** The seeded profiling of every BBIE in a BIE. {@code max} of {@code -1} means unbounded. */
    private record BbieSeed(boolean used, int min, int max, Long maxLengthFacet) {
        static BbieSeed baseline() {
            return new BbieSeed(true, 0, 1, null);
        }
    }

    private BusinessInformationEntityAPI bieAPI() {
        return getAPIFactory().getBusinessInformationEntityAPI();
    }

    /**
     * Create a fresh ASCCP whose ACC carries a single text BBIE, in {@link #RELEASE_NUMBER}.
     */
    private ASCCPObject createSharedAsccp(AppUserObject user, LibraryObject library) {
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, RELEASE_NUMBER);
        CoreComponentAPI cc = getAPIFactory().getCoreComponentAPI();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(user, library);
        ACCObject acc = cc.createRandomACC(user, release, namespace, "Production");
        DTObject textDataType = cc.getBDTByGuidAndReleaseNum(library, TEXT_BDT_GUID, RELEASE_NUMBER);
        BCCPObject bccp = cc.createRandomBCCP(release, textDataType, user, namespace, "Production");
        cc.appendBCC(acc, bccp, "Production");
        return cc.createRandomASCCP(acc, user, namespace, "Production");
    }

    private TopLevelASBIEPObject createProductionBIE(AppUserObject user, ASCCPObject asccp) {
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(user);
        TopLevelASBIEPObject bie =
                bieAPI().generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, user, "Production");
        // generateRandomTopLevelASBIEP creates only the root ABIE/ASBIEP; materialize the child BBIE(s)
        // from the ACC's element BCC so a single per-element profiling diff can be seeded (issue #1733).
        bieAPI().materializeUsedBbieChildren(bie.getTopLevelAsbiepId(), user.getAppUserId());
        return bie;
    }

    /**
     * Create a new BIE Package via the UI, add the given BIE deterministically (via the API rather
     * than the Add-BIE dialog), and promote it to Production. Returns the Production package.
     */
    private EditBIEPackagePage buildProductionPackageWith(ViewBIEPackagePage viewPage,
                                                          AppUserObject user, TopLevelASBIEPObject bie) {
        EditBIEPackagePage pkg = viewPage.hitNewBIEPackageButton();
        pkg.setName("TC 46.2 Package " + user.getLoginId());
        pkg.setDescription("BIE Package for TC_46_2.");
        pkg.hitUpdateButton();
        bieAPI().addBieToBiePackage(pkg.getBiePackageId(), bie.getTopLevelAsbiepId(), user.getAppUserId());
        pkg.openPage(pkg.getBiePackageId());
        pkg.moveToQA();
        pkg.moveToProduction();
        return pkg;
    }

    /**
     * Run a profiling-diff scenario and return the changed BIE's backward compatibility indicator.
     * The baseline BIE is created and packaged to Production first (so the initial Add only ever sees
     * the baseline), then the variant BIE on the same ASCCP is created, the package is revised, and
     * the baseline is replaced with the variant.
     */
    private BackwardCompatibility profilingDiff(BbieSeed baseline, BbieSeed variant) {
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        AppUserObject user = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(user);

        ASCCPObject asccp = createSharedAsccp(user, library);
        TopLevelASBIEPObject baselineBie = createProductionBIE(user, asccp);
        bieAPI().seedAllBbieProfiling(baselineBie.getTopLevelAsbiepId(),
                baseline.used(), baseline.min(), baseline.max(), baseline.maxLengthFacet());
        TopLevelASBIEPObject variantBie = createProductionBIE(user, asccp);
        bieAPI().seedAllBbieProfiling(variantBie.getTopLevelAsbiepId(),
                variant.used(), variant.min(), variant.max(), variant.maxLengthFacet());

        HomePage homePage = loginPage().signIn(user.getLoginId(), user.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewBIEPackagePage viewBIEPackagePage = bieMenu.openBIEPackageSubMenu();
        EditBIEPackagePage firstVersion = buildProductionPackageWith(viewBIEPackagePage, user, baselineBie);

        // Revise, then replace the baseline BIE with the variant (same ASCCP) deterministically.
        EditBIEPackagePage revision = firstVersion.revise();
        bieAPI().replaceBieInBiePackage(revision.getBiePackageId(),
                baselineBie.getTopLevelAsbiepId(), variantBie.getTopLevelAsbiepId(), user.getAppUserId());
        revision.openPage(revision.getBiePackageId());

        revision.selectExpression("XML");
        // The backward compatibility indicator is only emitted in the draft 0.3 manifest.
        revision.selectManifestVersion("0.3");
        File generatedZip = revision.clickGenerateAndDownloadZip();
        // The revision's head-of-chain is the variant BIE, so the package has exactly one BIE.
        return BIEPackageManifest.fromGeneratedZip(generatedZip).backwardCompatibilityOfOnlyBie();
    }

    @Test
    @DisplayName("TC_46_2_1")
    public void a_brand_new_bie_with_no_prior_counterpart_breaks_both_syntaxes() {
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        AppUserObject user = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(user);

        ASCCPObject baselineAsccp = createSharedAsccp(user, library);
        TopLevelASBIEPObject baselineBie = createProductionBIE(user, baselineAsccp);
        ASCCPObject newAsccp = createSharedAsccp(user, library);
        TopLevelASBIEPObject newBie = createProductionBIE(user, newAsccp);

        HomePage homePage = loginPage().signIn(user.getLoginId(), user.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewBIEPackagePage viewBIEPackagePage = bieMenu.openBIEPackageSubMenu();
        EditBIEPackagePage firstVersion = buildProductionPackageWith(viewBIEPackagePage, user, baselineBie);

        // Revise and add a brand-new BIE (different ASCCP) that has no counterpart in the prior package.
        EditBIEPackagePage revision = firstVersion.revise();
        bieAPI().addBieToBiePackage(revision.getBiePackageId(), newBie.getTopLevelAsbiepId(), user.getAppUserId());
        revision.openPage(revision.getBiePackageId());

        revision.selectExpression("XML");
        // The backward compatibility indicator is only emitted in the draft 0.3 manifest.
        revision.selectManifestVersion("0.3");
        File generatedZip = revision.clickGenerateAndDownloadZip();
        BIEPackageManifest manifest = BIEPackageManifest.fromGeneratedZip(generatedZip);

        // The revision holds the carried-forward baseline BIE plus the brand-new BIE; the new one is
        // the entry not included in the prior package version.
        BackwardCompatibility bc = manifest.backwardCompatibilityOfFirstNewBie();
        // Assertion #46.2.1
        assertFalse(bc.xmlSchema(), "A brand-new BIE breaks XML schema compatibility.");
        assertFalse(bc.jsonSchema(), "A brand-new BIE breaks JSON schema compatibility.");
    }

    @Test
    @DisplayName("TC_46_2_2")
    public void removing_an_element_breaks_both_syntaxes() {
        // baseline: element used; variant: element unused (removed)
        BackwardCompatibility bc = profilingDiff(BbieSeed.baseline(), new BbieSeed(false, 0, 1, null));
        // Assertion #46.2.2
        assertFalse(bc.xmlSchema(), "Removing an element breaks XML schema compatibility.");
        assertFalse(bc.jsonSchema(), "Removing an element breaks JSON schema compatibility.");
    }

    @Test
    @DisplayName("TC_46_2_3")
    public void adding_a_required_element_breaks_both_syntaxes() {
        // baseline: element unused; variant: element used and required (min 1)
        BackwardCompatibility bc = profilingDiff(new BbieSeed(false, 0, 1, null), new BbieSeed(true, 1, 1, null));
        // Assertion #46.2.3
        assertFalse(bc.xmlSchema(), "Adding a required element breaks XML schema compatibility.");
        assertFalse(bc.jsonSchema(), "Adding a required element breaks JSON schema compatibility.");
    }

    @Test
    @DisplayName("TC_46_2_4")
    public void adding_an_optional_element_is_compatible() {
        // baseline: element unused; variant: element used and optional (min 0)
        BackwardCompatibility bc = profilingDiff(new BbieSeed(false, 0, 1, null), new BbieSeed(true, 0, 1, null));
        // Assertion #46.2.4
        assertTrue(bc.xmlSchema(), "Adding an optional element keeps XML schema compatibility.");
        assertTrue(bc.jsonSchema(), "Adding an optional element keeps JSON schema compatibility.");
    }

    @Test
    @DisplayName("TC_46_2_5")
    public void tightening_cardinality_breaks_both_syntaxes() {
        // baseline: min 0; variant: min 1 (tightened)
        BackwardCompatibility bc = profilingDiff(new BbieSeed(true, 0, 1, null), new BbieSeed(true, 1, 1, null));
        // Assertion #46.2.5
        assertFalse(bc.xmlSchema(), "Tightening cardinality breaks XML schema compatibility.");
        assertFalse(bc.jsonSchema(), "Tightening cardinality breaks JSON schema compatibility.");
    }

    @Test
    @DisplayName("TC_46_2_6")
    public void loosening_cardinality_breaks_json_only() {
        // baseline: max 1 (scalar); variant: max unbounded (array). Loosening the cardinality from 1 to unbounded
        // flips the JSON rendering from a bare value to an array, which is a JSON-only break; XSD keeps the same
        // element shape, so it stays XML compatible.
        BackwardCompatibility bc = profilingDiff(new BbieSeed(true, 0, 1, null), new BbieSeed(true, 0, -1, null));
        // Assertion #46.2.6
        assertTrue(bc.xmlSchema(), "Loosening cardinality keeps XML schema compatibility.");
        assertFalse(bc.jsonSchema(),
                "Loosening cardinality from 1 to unbounded flips the JSON shape to an array (JSON-only break).");
    }

    @Test
    @DisplayName("TC_46_2_7")
    public void tightening_a_facet_breaks_both_syntaxes() {
        // baseline: no max-length facet; variant: max-length 5
        BackwardCompatibility bc = profilingDiff(new BbieSeed(true, 0, 1, null), new BbieSeed(true, 0, 1, 5L));
        // Assertion #46.2.7
        assertFalse(bc.xmlSchema(), "Tightening a facet breaks XML schema compatibility.");
        assertFalse(bc.jsonSchema(), "Tightening a facet breaks JSON schema compatibility.");
    }

    @Test
    @DisplayName("TC_46_2_8")
    public void narrowing_a_value_domain_in_xml_only_breaks_xml_only() {
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        AppUserObject user = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(user);

        // Build a baseline and a variant BIE on the same (text) ASCCP, then narrow the variant's
        // primitive from normalizedString to token. token is a sub-type (restriction) of
        // normalizedString, so the XSD value space is narrowed (an XML break), while both map to the
        // same JSON Schema type "string", so JSON stays compatible. Both sides must carry an explicit
        // XBT or the backend short-circuits the value-domain comparison.
        ASCCPObject asccp = createSharedAsccp(user, library);
        TopLevelASBIEPObject baselineBie = createProductionBIE(user, asccp);
        bieAPI().seedAllBbieValueDomainByBuiltInType(baselineBie.getTopLevelAsbiepId(), "xsd:normalizedString");
        TopLevelASBIEPObject variantBie = createProductionBIE(user, asccp);
        bieAPI().seedAllBbieValueDomainByBuiltInType(variantBie.getTopLevelAsbiepId(), "xsd:token");

        HomePage homePage = loginPage().signIn(user.getLoginId(), user.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewBIEPackagePage viewBIEPackagePage = bieMenu.openBIEPackageSubMenu();
        EditBIEPackagePage firstVersion = buildProductionPackageWith(viewBIEPackagePage, user, baselineBie);

        // Revise, then replace the baseline BIE with the variant (same ASCCP) deterministically.
        EditBIEPackagePage revision = firstVersion.revise();
        bieAPI().replaceBieInBiePackage(revision.getBiePackageId(),
                baselineBie.getTopLevelAsbiepId(), variantBie.getTopLevelAsbiepId(), user.getAppUserId());
        revision.openPage(revision.getBiePackageId());

        revision.selectExpression("XML");
        // The backward compatibility indicator is only emitted in the draft 0.3 manifest.
        revision.selectManifestVersion("0.3");
        File generatedZip = revision.clickGenerateAndDownloadZip();
        BackwardCompatibility bc = BIEPackageManifest.fromGeneratedZip(generatedZip).backwardCompatibilityOfOnlyBie();

        // Assertion #46.2.8
        assertFalse(bc.xmlSchema(),
                "Narrowing the value domain from normalizedString to token breaks XML schema compatibility.");
        assertTrue(bc.jsonSchema(),
                "Narrowing the value domain to a sibling string-based type keeps JSON schema compatibility.");
    }

    @Test
    @DisplayName("TC_46_2_9")
    public void an_unchanged_bie_is_compatible() {
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        AppUserObject user = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(user);

        ASCCPObject asccp = createSharedAsccp(user, library);
        TopLevelASBIEPObject bie = createProductionBIE(user, asccp);

        HomePage homePage = loginPage().signIn(user.getLoginId(), user.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewBIEPackagePage viewBIEPackagePage = bieMenu.openBIEPackageSubMenu();
        EditBIEPackagePage firstVersion = buildProductionPackageWith(viewBIEPackagePage, user, bie);

        // Revise without changing anything; the BIE carries forward unchanged.
        EditBIEPackagePage revision = firstVersion.revise();
        revision.selectExpression("XML");
        // The backward compatibility indicator is only emitted in the draft 0.3 manifest.
        revision.selectManifestVersion("0.3");
        File generatedZip = revision.clickGenerateAndDownloadZip();
        BackwardCompatibility bc =
                BIEPackageManifest.fromGeneratedZip(generatedZip).backwardCompatibilityByDen(asccp.getDen());

        // Assertion #46.2.9
        assertTrue(bc.xmlSchema(), "An unchanged BIE keeps XML schema compatibility.");
        assertTrue(bc.jsonSchema(), "An unchanged BIE keeps JSON schema compatibility.");
    }
}
