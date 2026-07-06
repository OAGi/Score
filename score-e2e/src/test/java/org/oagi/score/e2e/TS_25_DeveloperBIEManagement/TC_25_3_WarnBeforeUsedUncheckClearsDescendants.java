package org.oagi.score.e2e.TS_25_DeveloperBIEManagement;

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
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.AssertionHelper.assertChecked;
import static org.oagi.score.e2e.AssertionHelper.assertNotChecked;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.getDialogButtonByName;
import static org.oagi.score.e2e.impl.PageHelper.getDialogTitle;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.isChecked;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

/**
 * Test Case 25.3 — Issue #1755.
 *
 * The BIE editor tree's left "Used" checkbox sits right next to the expand/collapse chevron, so a
 * misclick can un-check a node and cascade-clear "Used" across its whole subtree — silently when the
 * node is collapsed. A confirmation dialog guards the un-check of a node that has at least one USED
 * descendant (i.e. an un-check that would actually clear something); un-checking an expandable node
 * whose descendants are all un-used proceeds silently. The warning is intentionally generic (it names
 * the node but does not enumerate the descendants, because the tree lazy-loads children).
 *
 * See docs/test_cases/TestSuite25.md (Test Case 25.3).
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_25_3_WarnBeforeUsedUncheckClearsDescendants extends BaseTest {

    private static final String CONFIRM_DIALOG_XPATH = "//score-confirm-dialog";
    private static final String CONFIRM_DIALOG_HEADER = "Unchecking will clear used descendants";
    private static final String CONFIRM_ACTION_BUTTON = "Uncheck anyway";
    private static final String CONFIRM_CANCEL_BUTTON = "Cancel";

    // A "Text" BDT (release 10.8.8) that has NO supplementary components, so a BBIE built on it is a
    // leaf (non-expandable) node — used to prove the leaf case does NOT raise the confirmation.
    private static final String NO_SC_TEXT_BDT_GUID = "89be97039be04d6f9cfda107d75926b5";

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

    @Test
    @DisplayName("TC_25_3")
    public void warn_before_used_uncheck_clears_used_descendants() {
        AppUserObject developer;
        ASCCPObject rootAsccp, childAsccp;
        BCCPObject leafBccp, grandChildBccp;

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        String currentRelease = "10.8.8";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, currentRelease);

        TopLevelASBIEPObject developerBIE;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer, library);
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
            CoreComponentAPI cc = getAPIFactory().getCoreComponentAPI();

            // A "Text" BDT with no supplementary components -> BBIEs on it are leaves.
            DTObject noScBdt = cc.getBDTByGuidAndReleaseNum(library, NO_SC_TEXT_BDT_GUID, currentRelease);

            // Root ACC of the top-level BIE.
            ACCObject rootAcc = cc.createRandomACC(developer, release, namespace, "Published");

            // (a) An OPTIONAL leaf BBIE directly under the root (no SC -> not expandable).
            leafBccp = cc.createRandomBCCP(release, noScBdt, developer, namespace, "Published");
            cc.appendBCC(rootAcc, leafBccp, "Published"); // cardinality 0..-1 => optional => usable

            // (b) An OPTIONAL ASBIE under the root whose target ACC has an OPTIONAL child BBIE -> it is
            //     expandable (has a descendant). Because the descendant is optional it starts un-used, so
            //     it exercises BOTH #1755 cases: un-check with no used descendant (silent) and, once the
            //     descendant is marked Used, un-check that would clear it (warns).
            ACCObject childAcc = cc.createRandomACC(developer, release, namespace, "Published");
            grandChildBccp = cc.createRandomBCCP(release, noScBdt, developer, namespace, "Published");
            cc.appendBCC(childAcc, grandChildBccp, "Published"); // optional 0..-1 => starts un-used
            childAsccp = cc.createRandomASCCP(childAcc, developer, namespace, "Published");
            cc.appendASCC(rootAcc, childAsccp, "Published"); // optional ASBIE with descendants

            rootAsccp = cc.createRandomASCCP(rootAcc, developer, namespace, "Published");
            developerBIE = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Collections.singletonList(context), rootAsccp, developer, "WIP");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(developerBIE);

        String expandablePath = "/" + rootAsccp.getPropertyTerm() + "/" + childAsccp.getPropertyTerm();
        String grandChildPath = expandablePath + "/" + grandChildBccp.getPropertyTerm();
        String leafPath = "/" + rootAsccp.getPropertyTerm() + "/" + leafBccp.getPropertyTerm();

        // ---------------------------------------------------------------------------------------
        // Baseline: make the expandable node "Used". Checking a node ON never prompts (check
        // direction is silent), regardless of whether the node has descendants. (Assertion #25.3.5)
        // The cascade leaves its OPTIONAL descendant BBIE un-used.
        // ---------------------------------------------------------------------------------------
        WebElement expandableCheckbox = getTreeUsedCheckbox(editBIEPage, expandablePath);
        if (!isChecked(expandableCheckbox)) {
            click(expandableCheckbox);
            assertNoConfirmationDialog();
        }
        expandableCheckbox = getTreeUsedCheckbox(editBIEPage, expandablePath);
        assertChecked(expandableCheckbox);

        // ---------------------------------------------------------------------------------------
        // Assertion #25.3.6 (Issue #1755): un-checking a used, expandable node whose descendants are
        // all un-used proceeds SILENTLY — the guard fires only when the un-check would actually clear a
        // used descendant, not merely because the node is expandable.
        // ---------------------------------------------------------------------------------------
        click(expandableCheckbox);
        assertNoConfirmationDialog();
        expandableCheckbox = getTreeUsedCheckbox(editBIEPage, expandablePath);
        assertNotChecked(expandableCheckbox);

        // Re-arm: re-use the node, then mark its descendant "Used" so an un-check now HAS something to
        // clear. (Checking a node ON is always silent.)
        click(expandableCheckbox);
        assertNoConfirmationDialog();
        WebElement grandChildCheckbox = getTreeUsedCheckbox(editBIEPage, grandChildPath);
        if (!isChecked(grandChildCheckbox)) {
            click(grandChildCheckbox);
            assertNoConfirmationDialog();
        }
        assertChecked(getTreeUsedCheckbox(editBIEPage, grandChildPath));

        // ---------------------------------------------------------------------------------------
        // Assertion #25.3.1: un-checking a used node that HAS a used descendant raises the
        // confirmation dialog, titled and worded as specified, naming the node.
        // ---------------------------------------------------------------------------------------
        expandableCheckbox = getTreeUsedCheckbox(editBIEPage, expandablePath);
        click(expandableCheckbox);
        assertEquals(CONFIRM_DIALOG_HEADER, getDialogTitle(getDriver()));
        String firstLine = getText(visibilityOfElementLocated(getDriver(),
                By.xpath(CONFIRM_DIALOG_XPATH + "//div[contains(@class, \"content\")]//p[1]")));
        assertTrue(firstLine.contains(childAsccp.getPropertyTerm()),
                "The warning should name the un-checked node. Actual: " + firstLine);
        assertTrue(firstLine.contains("used descendants"),
                "The warning should mention used descendants. Actual: " + firstLine);

        // ---------------------------------------------------------------------------------------
        // Assertion #25.3.3: Cancel makes no change and restores the checkbox to its checked state
        // (the one-way [checked] binding would otherwise leave it visually un-checked).
        // ---------------------------------------------------------------------------------------
        click(getDialogButtonByName(getDriver(), CONFIRM_CANCEL_BUTTON));
        waitFor(Duration.ofMillis(700L));
        assertNoConfirmationDialog();
        expandableCheckbox = getTreeUsedCheckbox(editBIEPage, expandablePath);
        assertChecked(expandableCheckbox);

        // ---------------------------------------------------------------------------------------
        // Assertion #25.3.2: confirming ("Uncheck anyway") un-checks the node (and clears its used
        // descendants).
        // ---------------------------------------------------------------------------------------
        click(expandableCheckbox);
        assertEquals(CONFIRM_DIALOG_HEADER, getDialogTitle(getDriver()));
        click(getDialogButtonByName(getDriver(), CONFIRM_ACTION_BUTTON));
        waitFor(Duration.ofMillis(700L));
        assertNoConfirmationDialog();
        expandableCheckbox = getTreeUsedCheckbox(editBIEPage, expandablePath);
        assertNotChecked(expandableCheckbox);

        // ---------------------------------------------------------------------------------------
        // Assertion #25.3.5 (deterministic): re-checking the node ON does NOT prompt.
        // ---------------------------------------------------------------------------------------
        click(expandableCheckbox);
        assertNoConfirmationDialog();
        expandableCheckbox = getTreeUsedCheckbox(editBIEPage, expandablePath);
        assertChecked(expandableCheckbox);

        // ---------------------------------------------------------------------------------------
        // Assertion #25.3.4: un-checking a leaf node (no descendants) never prompts and is applied
        // immediately.
        // ---------------------------------------------------------------------------------------
        WebElement leafCheckbox = getTreeUsedCheckbox(editBIEPage, leafPath);
        if (!isChecked(leafCheckbox)) {
            click(leafCheckbox);
            assertNoConfirmationDialog();
            leafCheckbox = getTreeUsedCheckbox(editBIEPage, leafPath);
        }
        assertChecked(leafCheckbox);
        click(leafCheckbox);
        assertNoConfirmationDialog();
        leafCheckbox = getTreeUsedCheckbox(editBIEPage, leafPath);
        assertNotChecked(leafCheckbox);
    }

    /**
     * Locate a tree node by path (scrolling it into the virtual viewport) and return its tree-node
     * "Used" checkbox.
     */
    private WebElement getTreeUsedCheckbox(EditBIEPage editBIEPage, String path) {
        WebElement node = editBIEPage.getNodeByPath(path);
        return editBIEPage.getUsedCheckboxAtNode(node);
    }

    /**
     * Assert that the #1755 confirmation dialog is not currently open.
     */
    private void assertNoConfirmationDialog() {
        waitFor(Duration.ofMillis(700L));
        assertFalse(isElementPresent(By.xpath(CONFIRM_DIALOG_XPATH)),
                "No confirmation dialog should be displayed.");
    }

    private boolean isElementPresent(By by) {
        return !getDriver().findElements(by).isEmpty();
    }
}
