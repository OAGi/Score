package org.oagi.score.e2e.menu;

import org.oagi.score.e2e.page.admin.AccountPage;
import org.oagi.score.e2e.page.admin.PendingSSOPage;
import org.oagi.score.e2e.page.admin.TenantPage;
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
    WebElement getAccountSubMenu();

    /**
     * Open the 'Accounts' submenu to enter the page.
     *
     * @return the 'Accounts' page object.
     */
    AccountPage openAccountSubMenu();

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

    /**
     * Expand the 'Admin' menu and tell whether it has the 'Tenant' submenu, which only the multi-tenant mode has.
     *
     * @return {@code true} if the 'Tenant' submenu is there
     */
    boolean hasTenantSubMenu();

    /**
     * Return the UI element of the 'Tenant' submenu.
     *
     * @return the UI element of the 'Tenant' submenu
     */
    WebElement getTenantSubMenu();

    /**
     * Open the 'Tenant' submenu to enter the page.
     *
     * @return the 'Tenant' page object.
     */
    TenantPage openTenantSubMenu();

}
