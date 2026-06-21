package org.oagi.score.e2e.TS_46_BIEPackageManagement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.BIEPackageManifest;
import org.oagi.score.e2e.page.bie.EditBIEPackagePage;
import org.oagi.score.e2e.page.bie.ViewBIEPackagePage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Suite 46 / Test Case 46.1 - Revision Reason Text (issue #1733).
 *
 * <p>Verifies the per-revision free-text "Revision Reason" captured on a BIE Package: it is hidden
 * on a brand-new package, shown and editable only on a revised WIP package, persisted/cleared via
 * Update, preserved across state transitions, emitted into the generated manifest after the prior
 * package version id (manifest version 0.3), and not inherited by the next revision.</p>
 */
@Execution(ExecutionMode.SAME_THREAD)
public class TC_46_1_RevisionReasonText extends BaseTest {

    private static final String RELEASE_NUMBER = "10.8.8";
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

    /**
     * Create a Production top-level BIE owned by {@code user} in {@link #RELEASE_NUMBER}, based on a
     * fresh ASCCP whose ACC has a single text BBIE.
     */
    private TopLevelASBIEPObject createProductionTopLevelBIE(AppUserObject user, LibraryObject library) {
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, RELEASE_NUMBER);
        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(user, library);

        ACCObject acc = coreComponentAPI.createRandomACC(user, release, namespace, "Production");
        DTObject textDataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, TEXT_BDT_GUID, RELEASE_NUMBER);
        BCCPObject bccp = coreComponentAPI.createRandomBCCP(release, textDataType, user, namespace, "Production");
        coreComponentAPI.appendBCC(acc, bccp, "Production");
        ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, user, namespace, "Production");

        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(user);
        return getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, user, "Production");
    }

    @Test
    @DisplayName("TC_46_1_1")
    public void revision_reason_field_is_not_shown_on_a_brand_new_bie_package() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewBIEPackagePage viewBIEPackagePage = bieMenu.openBIEPackageSubMenu();
        EditBIEPackagePage editBIEPackagePage = viewBIEPackagePage.hitNewBIEPackageButton();

        // Assertion #46.1.1 - the Revision Reason field is hidden on a brand-new (non-revised) package.
        assertFalse(editBIEPackagePage.isRevisionReasonFieldPresent(),
                "The Revision Reason field should not be shown on a brand-new BIE Package.");
    }

    @Test
    @DisplayName("TC_46_1_2")
    public void revision_reason_is_captured_persisted_preserved_and_emitted_in_the_manifest() {
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        TopLevelASBIEPObject bie = createProductionTopLevelBIE(endUser, library);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewBIEPackagePage viewBIEPackagePage = bieMenu.openBIEPackageSubMenu();

        // Create a first BIE Package, set required fields, add the BIE, and promote it to Production.
        EditBIEPackagePage firstVersion = viewBIEPackagePage.hitNewBIEPackageButton();
        firstVersion.setName("TC 46.1 Package " + endUser.getLoginId());
        firstVersion.setDescription("BIE Package for TC_46_1.");
        firstVersion.hitUpdateButton();
        // Populate the package's BIE deterministically (the Add-BIE dialog is exercised elsewhere).
        getAPIFactory().getBusinessInformationEntityAPI().addBieToBiePackage(
                firstVersion.getBiePackageId(), bie.getTopLevelAsbiepId(), endUser.getAppUserId());
        firstVersion.openPage(firstVersion.getBiePackageId());
        firstVersion.moveToQA();
        firstVersion.moveToProduction();

        // Revise the Production package.
        EditBIEPackagePage revision = firstVersion.revise();

        // Assertion #46.1.2 - the Revision Reason field is shown on a revised package.
        assertTrue(revision.isRevisionReasonFieldPresent(),
                "The Revision Reason field should be shown on a revised BIE Package.");
        // Assertion #46.1.3 (part 1) - editable while WIP.
        assertTrue(revision.isRevisionReasonFieldEnabled(),
                "The Revision Reason field should be editable while the revision is WIP.");

        // Assertion #46.1.4 - enter and persist a Revision Reason.
        String reason = "Tightened cardinality for compliance with the 2026 profile.";
        revision.setRevisionReason(reason);
        revision.hitUpdateButton();
        revision.openPage(revision.getBiePackageId());
        assertEquals(reason, revision.getRevisionReason(),
                "The saved Revision Reason should be shown when the package is reopened.");

        // Assertion #46.1.5 - clearing the Revision Reason removes it.
        revision.setRevisionReason("");
        revision.hitUpdateButton();
        revision.openPage(revision.getBiePackageId());
        assertEquals("", revision.getRevisionReason(),
                "A blank Revision Reason should be cleared after Update.");

        // Re-enter the reason and promote through QA -> Production.
        revision.setRevisionReason(reason);
        revision.hitUpdateButton();
        revision.moveToQA();
        // Assertion #46.1.3 (part 2) - read-only outside WIP, value intact.
        assertFalse(revision.isRevisionReasonFieldEnabled(),
                "The Revision Reason field should be read-only once the revision leaves WIP.");
        assertEquals(reason, revision.getRevisionReason(),
                "The Revision Reason should be unchanged after moving to QA.");
        revision.moveToProduction();
        // Assertion #46.1.6 - preserved across state transitions.
        assertEquals(reason, revision.getRevisionReason(),
                "The Revision Reason should be preserved through WIP -> QA -> Production.");

        // Assertion #46.1.7 - emitted in the generated manifest (version 0.3), after the prior package version id.
        revision.selectExpression("XML");
        // The revision reason is only emitted in the draft 0.3 manifest.
        revision.selectManifestVersion("0.3");
        File generatedZip = revision.clickGenerateAndDownloadZip();
        BIEPackageManifest manifest = BIEPackageManifest.fromGeneratedZip(generatedZip);
        assertEquals("0.3", manifest.manifestVersion(),
                "The BIE Package manifest version should be 0.3.");
        assertTrue(manifest.hasPriorPackageVersionId(),
                "A revised package's manifest should carry the prior package version id.");
        assertTrue(manifest.hasRevisionReason(),
                "The manifest should include the package-level Revision Reason.");
        assertEquals(reason, manifest.revisionReason(),
                "The manifest Revision Reason should match the value entered in the UI.");

        // Assertion #46.1.8 - the next revision does not inherit the prior revision's reason.
        EditBIEPackagePage nextRevision = revision.revise();
        assertTrue(nextRevision.isRevisionReasonFieldPresent(),
                "The Revision Reason field should be shown on the next revision.");
        assertEquals("", nextRevision.getRevisionReason(),
                "A new revision should start with an empty Revision Reason (no inheritance).");
    }

    @Test
    @DisplayName("TC_46_1_9")
    public void the_stable_0_2_manifest_omits_the_0_3_only_fields() {
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        TopLevelASBIEPObject bie = createProductionTopLevelBIE(endUser, library);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewBIEPackagePage viewBIEPackagePage = bieMenu.openBIEPackageSubMenu();

        // Create a Production BIE Package and revise it so the 0.3 manifest *would* emit the issue
        // #1733 fields (a revised package, plus a captured Revision Reason).
        EditBIEPackagePage firstVersion = viewBIEPackagePage.hitNewBIEPackageButton();
        firstVersion.setName("TC 46.1.9 Package " + endUser.getLoginId());
        firstVersion.setDescription("BIE Package for TC_46_1_9.");
        firstVersion.hitUpdateButton();
        getAPIFactory().getBusinessInformationEntityAPI().addBieToBiePackage(
                firstVersion.getBiePackageId(), bie.getTopLevelAsbiepId(), endUser.getAppUserId());
        firstVersion.openPage(firstVersion.getBiePackageId());
        firstVersion.moveToQA();
        firstVersion.moveToProduction();

        EditBIEPackagePage revision = firstVersion.revise();
        revision.setRevisionReason("This reason must not appear in the stable 0.2 manifest.");
        revision.hitUpdateButton();

        // Generate the stable 0.2 manifest and verify it omits the draft-only (0.3) fields while still
        // carrying the prior package version id.
        revision.selectExpression("XML");
        revision.selectManifestVersion("0.2");
        File generatedZip = revision.clickGenerateAndDownloadZip();
        BIEPackageManifest manifest = BIEPackageManifest.fromGeneratedZip(generatedZip);

        // Assertion #46.1.9
        assertEquals("0.2", manifest.manifestVersion(),
                "The stable BIE Package manifest version should be 0.2.");
        assertFalse(manifest.hasRevisionReason(),
                "The stable 0.2 manifest must omit the package-level Revision Reason.");
        assertFalse(manifest.hasAnyBackwardCompatibility(),
                "The stable 0.2 manifest must omit the per-BIE backward compatibility indicator.");
        assertTrue(manifest.hasPriorPackageVersionId(),
                "A revised package's 0.2 manifest still carries the prior package version id.");
    }
}
