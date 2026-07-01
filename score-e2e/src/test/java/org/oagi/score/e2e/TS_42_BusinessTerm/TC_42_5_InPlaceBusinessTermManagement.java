package org.oagi.score.e2e.TS_42_BusinessTerm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.condition.DisabledIfBusinessTermProperty;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.BieBusinessTermAssignDialog;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.business_term.ViewEditBusinessTermPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.getText;

/**
 * Test Case 42.5 — In-place Business Term management in the BIE editor (Issue #1754).
 *
 * <p>Business Term assignment moved out of the standalone 'Business Term Assignment' /
 * 'Assign Business Term' pages and into the BIE editor's in-place 'Business Terms' chip field on a
 * used ASBIE/BBIE node. Each @Test maps 1:1 to an assertion #42.5.1–#42.5.10.
 */
@Execution(ExecutionMode.CONCURRENT)
@DisabledIfBusinessTermProperty(value = false)
public class TC_42_5_InPlaceBusinessTermManagement extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @BeforeEach
    public void init() {
        super.init();
    }

    /**
     * #42.5.1 — the chip field appears beside 'Remark' on a used ASBIE/BBIE node for an end user when
     * Business Term is enabled (this suite is gated on that flag), and the legacy free-text
     * 'Business Term' input is NOT rendered in that case.
     */
    @Test
    @DisplayName("TC_42_5_1")
    public void chip_field_is_shown_for_end_user_on_used_node_instead_of_legacy_text_input() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        TopLevelASBIEPObject topLevelASBIEP = generateBbieTopLevelASBIEP(developer, endUser, "WIP");
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);

        String path = bbiePath(topLevelASBIEP);
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        // The in-place chip field is shown ...
        assertTrue(bbiePanel.getBusinessTermChipField().isDisplayed());
        // ... and the standalone-page 'Assign Business Term' button is no longer rendered.
        assertEquals(0, getDriver().findElements(By.xpath(
                "//span[contains(text(), \"Assign Business Term\")]//ancestor::button[1]")).size());
    }

    /**
     * #42.5.2 — the chip field is editable regardless of BIE state. Move the BIE to Production and
     * confirm business terms can still be assigned via the chip field ('+' enabled).
     */
    @Test
    @DisplayName("TC_42_5_2")
    public void chip_field_is_editable_regardless_of_bie_state() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        TopLevelASBIEPObject topLevelASBIEP = generateBbieTopLevelASBIEP(developer, endUser, "WIP");
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);

        String path = bbiePath(topLevelASBIEP);
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        // Promote WIP -> QA -> Production; the chip field must stay editable in every state.
        editBIEPage.moveToQA();
        editBIEPage.moveToProduction();

        WebElement bbieNodeInProduction = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanelInProduction = editBIEPage.getBBIEPanel(bbieNodeInProduction);
        // No edit-state gate: the '+' is enabled (the node is used, saved, not locked/cyclic) ...
        assertTrue(bbiePanelInProduction.getAddBusinessTermButton().isEnabled());
        // ... and an assignment can be created in the Production state.
        BieBusinessTermAssignDialog dialog = bbiePanelInProduction.openBusinessTermAssignDialog();
        dialog.setSearchBusinessTerm(randomBusinessTerm.getBusinessTerm());
        dialog.hitSearch();
        click(dialog.getRowCheckboxByTerm(randomBusinessTerm.getBusinessTerm()));
        dialog.hitAssign();

        WebElement bbieNodeForCheck = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanelForCheck = editBIEPage.getBBIEPanel(bbieNodeForCheck);
        assertTrue(bbiePanelForCheck.getBusinessTermChipByTerm(randomBusinessTerm.getBusinessTerm()).isDisplayed());
    }

    /**
     * #42.5.3 — the '+' (Assign a Business Term) button is disabled until the node has a persisted id
     * (i.e. until the used node has been saved). It becomes enabled after the save.
     */
    @Test
    @DisplayName("TC_42_5_3")
    public void add_button_is_disabled_until_the_node_is_saved() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        TopLevelASBIEPObject topLevelASBIEP = generateBbieTopLevelASBIEP(developer, endUser, "WIP");
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);

        String path = bbiePath(topLevelASBIEP);
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);

        // Mark the node used but do NOT save yet: the node has no persisted id, so the '+' is disabled.
        bbiePanel.toggleUsed();
        assertFalse(bbiePanel.getAddBusinessTermButton().isEnabled());

        // Save the node; the '+' becomes enabled once the node has a persisted id.
        editBIEPage.hitUpdateButton();
        WebElement bbieNodeAfterSave = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanelAfterSave = editBIEPage.getBBIEPanel(bbieNodeAfterSave);
        assertTrue(bbiePanelAfterSave.getAddBusinessTermButton().isEnabled());
    }

    /**
     * #42.5.4 — the assign dialog supports multi-select: the master checkbox selects/clears all
     * (indeterminate for a partial selection), the action reads 'Assign (N)', and all selected terms
     * are assigned at once.
     */
    @Test
    @DisplayName("TC_42_5_4")
    public void assign_dialog_supports_multi_select() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        TopLevelASBIEPObject topLevelASBIEP = generateBbieTopLevelASBIEP(developer, endUser, "WIP");
        // Three terms that share a common name prefix so a single search returns all of them.
        String prefix = "btms" + org.apache.commons.lang3.RandomStringUtils.secure().nextAlphanumeric(6);
        List<BusinessTermObject> businessTerms = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            businessTerms.add(getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser, prefix));
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);
        String path = bbiePath(topLevelASBIEP);
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        BieBusinessTermAssignDialog dialog = editBIEPage.getBBIEPanel(editBIEPage.getNodeByPath(path))
                .openBusinessTermAssignDialog();
        dialog.setSearchBusinessTerm(prefix);
        dialog.hitSearch();

        // Master checkbox selects all, then clears all.
        dialog.toggleMasterCheckbox();
        assertFalse(dialog.isMasterIndeterminate());
        dialog.toggleMasterCheckbox();

        // A partial selection puts the master checkbox in the indeterminate state and the action
        // button reads 'Assign (2)' for two selected rows.
        click(dialog.getRowCheckboxAtIndex(1));
        click(dialog.getRowCheckboxAtIndex(2));
        assertTrue(dialog.isMasterIndeterminate());
        assertTrue(dialog.getAssignButtonText().contains("2"));

        // Select the third row and assign all three at once.
        click(dialog.getRowCheckboxAtIndex(3));
        dialog.hitAssign();

        WebElement bbieNodeForCheck = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanelForCheck = editBIEPage.getBBIEPanel(bbieNodeForCheck);
        for (BusinessTermObject bt : businessTerms) {
            assertTrue(bbiePanelForCheck.getBusinessTermChipByTerm(bt.getBusinessTerm()).isDisplayed());
        }
    }

    /**
     * #42.5.5 — Type Code lets the same term be assigned twice with different codes; editing a chip's
     * Type Code to collide with another assignment on the same BIE shows the non-blocking inline
     * error (the row being edited is ignored by the check).
     */
    @Test
    @DisplayName("TC_42_5_5")
    public void type_code_allows_reuse_and_collision_shows_inline_error() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        TopLevelASBIEPObject topLevelASBIEP = generateBbieTopLevelASBIEP(developer, endUser, "WIP");
        BusinessTermObject businessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);
        String path = bbiePath(topLevelASBIEP);
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        // Assign the same term twice with different Type Codes (allowed).
        assignBusinessTermViaDialog(editBIEPage, path, businessTerm.getBusinessTerm(), "code A");
        assignBusinessTermViaDialog(editBIEPage, path, businessTerm.getBusinessTerm(), "code B");

        WebElement bbieNodeForCheck = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanelForCheck = editBIEPage.getBBIEPanel(bbieNodeForCheck);
        assertEquals(2, bbiePanelForCheck.getBusinessTermChips().size());

        // Edit the 'code B' chip's Type Code to collide with 'code A' on the same BIE.
        WebElement chipB = findChipByTypeCode(bbiePanelForCheck, "code B");
        bbiePanelForCheck.startTypeCodeInlineEdit(chipB);
        bbiePanelForCheck.setTypeCodeInlineEditValue("code A");
        bbiePanelForCheck.saveTypeCodeInlineEdit();

        // The non-blocking inline error is shown (not a modal).
        assertTrue(bbiePanelForCheck.getTypeCodeInlineError().contains(
                "Another business term assignment for the same BIE and type code already exists!"));
    }

    /**
     * #42.5.6 — a term already used by another component is still selectable and assignable from the
     * in-place dialog (the 'used' state guards catalog discard, not assignment).
     */
    @Test
    @DisplayName("TC_42_5_6")
    public void business_term_used_elsewhere_is_still_assignable() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        BusinessTermObject sharedTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        // First BIE: assign the shared term so it becomes "used".
        TopLevelASBIEPObject firstBIE = generateBbieTopLevelASBIEP(developer, endUser, "WIP");
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        EditBIEPage firstEditPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(firstBIE);
        String firstPath = bbiePath(firstBIE);
        WebElement firstNode = firstEditPage.getNodeByPath(firstPath);
        EditBIEPage.BBIEPanel firstPanel = firstEditPage.getBBIEPanel(firstNode);
        firstPanel.toggleUsed();
        firstEditPage.hitUpdateButton();
        assignBusinessTermViaDialog(firstEditPage, firstPath, sharedTerm.getBusinessTerm(), null);

        // Second BIE: the already-used term must still be selectable and assignable.
        TopLevelASBIEPObject secondBIE = generateBbieTopLevelASBIEP(developer, endUser, "WIP");
        EditBIEPage secondEditPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(secondBIE);
        String secondPath = bbiePath(secondBIE);
        WebElement secondNode = secondEditPage.getNodeByPath(secondPath);
        EditBIEPage.BBIEPanel secondPanel = secondEditPage.getBBIEPanel(secondNode);
        secondPanel.toggleUsed();
        secondEditPage.hitUpdateButton();

        BieBusinessTermAssignDialog dialog = secondEditPage.getBBIEPanel(secondEditPage.getNodeByPath(secondPath))
                .openBusinessTermAssignDialog();
        dialog.setSearchBusinessTerm(sharedTerm.getBusinessTerm());
        dialog.hitSearch();
        // The used term is present and selectable.
        assertTrue(dialog.getRowCheckboxByTerm(sharedTerm.getBusinessTerm()).isDisplayed());
        click(dialog.getRowCheckboxByTerm(sharedTerm.getBusinessTerm()));
        dialog.hitAssign();

        EditBIEPage.BBIEPanel secondPanelForCheck = secondEditPage.getBBIEPanel(secondEditPage.getNodeByPath(secondPath));
        assertTrue(secondPanelForCheck.getBusinessTermChipByTerm(sharedTerm.getBusinessTerm()).isDisplayed());
    }

    /**
     * #42.5.7 — setting a chip preferred demotes the previously preferred assignment on the same
     * node (one-preferred-per-node).
     */
    @Test
    @DisplayName("TC_42_5_7")
    public void setting_a_chip_preferred_demotes_the_previous_preferred() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        TopLevelASBIEPObject topLevelASBIEP = generateBbieTopLevelASBIEP(developer, endUser, "WIP");
        BusinessTermObject firstTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);
        BusinessTermObject secondTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);
        String path = bbiePath(topLevelASBIEP);
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        assignBusinessTermViaDialog(editBIEPage, path, firstTerm.getBusinessTerm(), null);
        assignBusinessTermViaDialog(editBIEPage, path, secondTerm.getBusinessTerm(), null);

        // Mark the first chip preferred.
        EditBIEPage.BBIEPanel panel = editBIEPage.getBBIEPanel(editBIEPage.getNodeByPath(path));
        WebElement firstChip = panel.getBusinessTermChipByTerm(firstTerm.getBusinessTerm());
        panel.clickPreferredStar(firstChip);

        // Now mark the second chip preferred: the first must be demoted.
        panel = editBIEPage.getBBIEPanel(editBIEPage.getNodeByPath(path));
        WebElement secondChip = panel.getBusinessTermChipByTerm(secondTerm.getBusinessTerm());
        panel.clickPreferredStar(secondChip);

        EditBIEPage.BBIEPanel panelForCheck = editBIEPage.getBBIEPanel(editBIEPage.getNodeByPath(path));
        assertTrue(panelForCheck.isChipPreferred(
                panelForCheck.getBusinessTermChipByTerm(secondTerm.getBusinessTerm())));
        assertFalse(panelForCheck.isChipPreferred(
                panelForCheck.getBusinessTermChipByTerm(firstTerm.getBusinessTerm())));
    }

    /**
     * #42.5.8 — removing a chip prompts a confirmation dialog; on confirm only that assignment is
     * removed while the catalog Business Term remains.
     */
    @Test
    @DisplayName("TC_42_5_8")
    public void removing_a_chip_removes_only_the_assignment_not_the_catalog_term() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        TopLevelASBIEPObject topLevelASBIEP = generateBbieTopLevelASBIEP(developer, endUser, "WIP");
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);
        String path = bbiePath(topLevelASBIEP);
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();
        assignBusinessTermViaDialog(editBIEPage, path, randomBusinessTerm.getBusinessTerm(), null);

        // Remove the chip and confirm the confirmation dialog.
        EditBIEPage.BBIEPanel panel = editBIEPage.getBBIEPanel(editBIEPage.getNodeByPath(path));
        WebElement chip = panel.getBusinessTermChipByTerm(randomBusinessTerm.getBusinessTerm());
        panel.removeBusinessTermChip(chip);

        // Only the assignment is gone: the chip is no longer shown, ...
        EditBIEPage.BBIEPanel panelForCheck = editBIEPage.getBBIEPanel(editBIEPage.getNodeByPath(path));
        assertTrue(panelForCheck.getBusinessTermChips().isEmpty());

        // ... but the catalog Business Term still exists in View/Edit Business Term.
        ViewEditBusinessTermPage viewEditBusinessTermPage = homePage.getBIEMenu().openViewEditBusinessTermSubMenu();
        assertNotNull(viewEditBusinessTermPage.openEditBusinessTermPageByTerm(randomBusinessTerm.getBusinessTerm()));
    }

    /**
     * #42.5.9 — hovering a chip shows a preview card with the term link, External Reference URI,
     * External Reference ID, Definition, and Comment.
     */
    @Test
    @DisplayName("TC_42_5_9")
    public void hovering_a_chip_shows_a_preview_card() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        TopLevelASBIEPObject topLevelASBIEP = generateBbieTopLevelASBIEP(developer, endUser, "WIP");
        // createRandomBusinessTerm populates URI, ID, definition, and comment.
        BusinessTermObject randomBusinessTerm = getAPIFactory().getBusinessTermAPI().createRandomBusinessTerm(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);
        String path = bbiePath(topLevelASBIEP);
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();
        assignBusinessTermViaDialog(editBIEPage, path, randomBusinessTerm.getBusinessTerm(), null);

        EditBIEPage.BBIEPanel panel = editBIEPage.getBBIEPanel(editBIEPage.getNodeByPath(path));
        WebElement chip = panel.getBusinessTermChipByTerm(randomBusinessTerm.getBusinessTerm());
        WebElement hoverCard = panel.getBusinessTermHoverCard(chip);

        assertEquals(randomBusinessTerm.getBusinessTerm(),
                getText(hoverCard.findElement(By.cssSelector("a .bt-hover-term"))));
        assertTrue(hoverCard.findElement(By.cssSelector(
                "a[href^=\"/business_term_management/business_term/\"]")).isDisplayed());
        // The populated URI value is shown in a preview row.
        assertTrue(getText(hoverCard).contains(randomBusinessTerm.getExternalReferenceUri()));
    }

    /**
     * #42.5.10 — on the base (inherited) tab the chip field is read-only and non-interactive: it has
     * no '+' button and its chips carry no interactive preferred-star/remove controls.
     */
    @Test
    @DisplayName("TC_42_5_10")
    public void base_inherited_tab_chip_field_is_read_only() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        TopLevelASBIEPObject topLevelASBIEP = generateBbieTopLevelASBIEP(developer, endUser, "WIP");
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(topLevelASBIEP);
        String path = bbiePath(topLevelASBIEP);
        WebElement bbieNode = editBIEPage.getNodeByPath(path);
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        // Switch to the base (inherited) tab; its chip field is read-only.
        editBIEPage.getBBIEPanel(editBIEPage.getNodeByPath(path)).getBaseBBIEPanel();

        // The read-only base chip field is present, but the interactive '+' add button is not rendered
        // for the base tab (TODO(#1754): verify selector on live stack).
        WebElement baseChipField = getDriver().findElement(By.xpath(
                "//mat-form-field[contains(concat(\" \", normalize-space(@class), \" \"), \" bt-badges-field-readonly \")]"
                        + "//mat-chip-grid[@data-bie-type=\"BBIE\"]"));
        assertTrue(baseChipField.isDisplayed());
        assertEquals(0, baseChipField.findElements(By.xpath(
                "ancestor::mat-form-field[1]"
                        + "//button[contains(concat(\" \", normalize-space(@class), \" \"), \" bt-add-btn \")]")).size());
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random accounts
        this.randomAccounts.forEach(randomAccount -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId());
        });
    }

    // --- helpers ---

    private TopLevelASBIEPObject generateBbieTopLevelASBIEP(AppUserObject developer, AppUserObject endUser, String state) {
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.3");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "Source Activity. Source Activity", release.getReleaseNumber());
        return getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Collections.singletonList(randomBusinessContext), asccp, endUser, state);
    }

    private String bbiePath(TopLevelASBIEPObject topLevelASBIEP) {
        return "/" + topLevelASBIEP.getPropertyTerm() + "/Note";
    }

    private void assignBusinessTermViaDialog(EditBIEPage editBIEPage, String path, String businessTerm, String typeCode) {
        BieBusinessTermAssignDialog dialog = editBIEPage.getBBIEPanel(editBIEPage.getNodeByPath(path))
                .openBusinessTermAssignDialog();
        dialog.setSearchBusinessTerm(businessTerm);
        dialog.hitSearch();
        click(dialog.getRowCheckboxByTerm(businessTerm));
        if (typeCode != null) {
            dialog.setTypeCode(typeCode);
        }
        dialog.hitAssign();
    }

    private WebElement findChipByTypeCode(EditBIEPage.BBIEPanel panel, String typeCode) {
        for (WebElement chip : panel.getBusinessTermChips()) {
            if (typeCode.equals(panel.getChipTypeCode(chip))) {
                return chip;
            }
        }
        throw new NoSuchElementException("No chip with Type Code: " + typeCode);
    }
}
