package org.oagi.score.e2e.menu;

import org.oagi.score.e2e.page.admin.AccountsPage;
import org.oagi.score.e2e.page.admin.PendingSSOPage;
import org.openqa.selenium.WebElement;

/**
 * An interface of admin menu
 */
public interface AdminMenu {

    /**
     * Return the UI element of the 'Admin' menu.
     *
     * @return the UI element of the 'Admin' menu
     */
    WebElement getAdminMenu();

    /**
     * Click the 'Admin' menu to expand it.
     */
    void expandAdminMenu();

    /**
     * Return the UI element of the 'Accounts' submenu.
     *
     * @return the UI element of the 'Accounts' submenu
     */
    WebElement getAccountsSubMenu();

    /**
     * Open the 'Accounts' submenu to enter the page.
     *
     * @return the 'Accounts' page object.
     */
    AccountsPage openAccountsSubMenu();

    /**
     * Return the UI element of the 'Pending SSO' submenu.
     *
     * @return the UI element of the 'Pending SSO' submenu
     */
    WebElement getPendingSSOSubMenu();

    /**
     * Open the 'Pending SSO' submenu to enter the page.
     *
     * @return the 'Pending SSO' page object.
     */
    PendingSSOPage openPendingSSOSubMenu();

}
