package org.oagi.score.e2e.TS_43_OpenAPIDocument;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.OpenAPIDocumentObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.oas.EditOpenAPIDocumentPage;
import org.oagi.score.e2e.page.oas.OpenAPIDocumentPage;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.getDialogButtonByName;
import static org.oagi.score.e2e.impl.PageHelper.getDialogTitle;
import static org.oagi.score.e2e.impl.PageHelper.getSnackBarMessage;
import static org.oagi.score.e2e.impl.PageHelper.getText;

/**
 * Test Case 43.3 - Discard Button Placement on the Edit OpenAPI Document Page (Issue #1731).
 * <p>
 * The detail-page Discard permanently deletes the entire OpenAPI Document, so it was moved away
 * from the frequently-used Update button: Discard sits at the far left of the top toolbar (right
 * after the title) and Update stays pinned to the far right.
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_43_3_DiscardButtonPlacement extends BaseTest {

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
    @DisplayName("TC_43_3_1")
    public void discard_button_is_separated_from_update_button_at_opposite_ends_of_toolbar() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        OpenAPIDocumentObject randomOpenAPIDocument =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);

        EditOpenAPIDocumentPage editOpenAPIDocumentPage = openEditOpenAPIDocumentPage(endUser, randomOpenAPIDocument);

        WebElement discardButton = editOpenAPIDocumentPage.getDiscardButton();
        WebElement updateButton = editOpenAPIDocumentPage.getUpdateButton(false);

        int discardX = discardButton.getLocation().getX();
        int updateX = updateButton.getLocation().getX();

        assertTrue(discardX < updateX,
                "The 'Discard' button should appear to the left of the 'Update' button");
        assertTrue(updateX - discardX > 100,
                "The 'Discard' and 'Update' buttons should be visually separated at opposite ends of the toolbar");
    }

    @Test
    @DisplayName("TC_43_3_2")
    public void discard_keeps_warn_style_and_update_keeps_primary_style_and_is_disabled_without_changes() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        OpenAPIDocumentObject randomOpenAPIDocument =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);

        EditOpenAPIDocumentPage editOpenAPIDocumentPage = openEditOpenAPIDocumentPage(endUser, randomOpenAPIDocument);

        WebElement discardButton = editOpenAPIDocumentPage.getDiscardButton();
        WebElement updateButton = editOpenAPIDocumentPage.getUpdateButton(false);

        assertTrue(discardButton.getAttribute("class").contains("mat-warn"),
                "The 'Discard' button should keep its warning (red) styling");
        assertTrue(updateButton.getAttribute("class").contains("mat-primary"),
                "The 'Update' button should keep its primary styling");
        assertFalse(updateButton.isEnabled(),
                "The 'Update' button should be disabled when there are no unsaved changes");
    }

    @Test
    @DisplayName("TC_43_3_3")
    public void clicking_discard_opens_confirmation_dialog_without_deleting_immediately() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        OpenAPIDocumentObject randomOpenAPIDocument =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);

        EditOpenAPIDocumentPage editOpenAPIDocumentPage = openEditOpenAPIDocumentPage(endUser, randomOpenAPIDocument);

        editOpenAPIDocumentPage.clickDiscardButtonToOpenDialog();

        assertEquals("Discard OpenAPI Doc?", getDialogTitle(getDriver()));
        assertNotNull(getDialogButtonByName(getDriver(), "Discard"));
        assertNotNull(getDialogButtonByName(getDriver(), "Cancel"));

        // The document must not be deleted just by opening the dialog; cancelling leaves it intact.
        click(getDialogButtonByName(getDriver(), "Cancel"));
        assertEquals(randomOpenAPIDocument.getTitle(), getText(editOpenAPIDocumentPage.getTitleField()));
    }

    @Test
    @DisplayName("TC_43_3_4")
    public void cancelling_the_confirmation_dialog_leaves_the_document_intact() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        OpenAPIDocumentObject randomOpenAPIDocument =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);

        EditOpenAPIDocumentPage editOpenAPIDocumentPage = openEditOpenAPIDocumentPage(endUser, randomOpenAPIDocument);

        editOpenAPIDocumentPage.clickDiscardButtonToOpenDialog();
        click(getDialogButtonByName(getDriver(), "Cancel"));

        // Still on the Edit OpenAPI Document page, document unchanged.
        assertEquals("Edit OpenAPI Document", getText(editOpenAPIDocumentPage.getTitle()));
        assertEquals(randomOpenAPIDocument.getTitle(), getText(editOpenAPIDocumentPage.getTitleField()));

        // And the document is still listed.
        BIEMenu bieMenu = homePage.getBIEMenu();
        OpenAPIDocumentPage openAPIDocumentPage = bieMenu.openOpenAPIDocumentSubMenu();
        openAPIDocumentPage.setTitle(randomOpenAPIDocument.getTitle());
        openAPIDocumentPage.hitSearchButton();
        assertNotNull(openAPIDocumentPage.getTableRecordAtIndex(1));
    }

    @Test
    @DisplayName("TC_43_3_5")
    public void confirming_discard_on_an_unused_document_removes_it_and_returns_to_list() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        OpenAPIDocumentObject randomOpenAPIDocument =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);

        EditOpenAPIDocumentPage editOpenAPIDocumentPage = openEditOpenAPIDocumentPage(endUser, randomOpenAPIDocument);

        editOpenAPIDocumentPage.clickDiscardButtonToOpenDialog();
        click(getDialogButtonByName(getDriver(), "Discard"));

        assertEquals("Discarded", getSnackBarMessage(getDriver()));

        // A successful discard navigates back to the OpenAPI Document list, whose own
        // 'Create OpenAPI Document' button collides with the navigation-menu locator. Go to the
        // home page first, then reopen the list via the menu to verify the document is gone.
        homePage.openPage();
        BIEMenu bieMenu = homePage.getBIEMenu();
        OpenAPIDocumentPage openAPIDocumentPage = bieMenu.openOpenAPIDocumentSubMenu();
        openAPIDocumentPage.setTitle(randomOpenAPIDocument.getTitle());
        openAPIDocumentPage.hitSearchButton();
        assertThrows(TimeoutException.class, () -> openAPIDocumentPage.getTableRecordAtIndex(1));
    }

    @Test
    @DisplayName("TC_43_3_6")
    public void confirming_discard_on_a_used_document_is_rejected_and_the_document_remains() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        OpenAPIDocumentObject randomOpenAPIDocument =
                getAPIFactory().getOpenAPIDocumentAPI().createRandomOpenAPIDocument(endUser);
        getAPIFactory().getOpenAPIDocumentAPI().createRandomServer(randomOpenAPIDocument, endUser);

        EditOpenAPIDocumentPage editOpenAPIDocumentPage = openEditOpenAPIDocumentPage(endUser, randomOpenAPIDocument);

        editOpenAPIDocumentPage.clickDiscardButtonToOpenDialog();
        click(getDialogButtonByName(getDriver(), "Discard"));

        assertEquals("Discard's forbidden! The OpenAPI Doc is used.", getSnackBarMessage(getDriver()));

        // The document is still present and openable.
        BIEMenu bieMenu = homePage.getBIEMenu();
        OpenAPIDocumentPage openAPIDocumentPage = bieMenu.openOpenAPIDocumentSubMenu();
        assertDoesNotThrow(() -> openAPIDocumentPage.openEditOpenAPIDocumentPage(randomOpenAPIDocument));
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
}
