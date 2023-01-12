package org.oagi.score.e2e.menu;

import org.oagi.score.e2e.page.agency_id_list.ViewEditAgencyIDListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.oagi.score.e2e.page.namespace.ViewEditNamespacePage;
import org.oagi.score.e2e.page.release.ViewEditReleasePage;
import org.openqa.selenium.WebElement;

/**
 * An interface of core component menu
 */
public interface CoreComponentMenu {

    /**
     * Return the UI element of the 'Core Component' menu.
     *
     * @return the UI element of the 'Core Component' menu
     */
    WebElement getCoreComponentMenu();

    /**
     * Click the 'Core Component' menu to expand it.
     */
    void expandCoreComponentMenu();

    /**
     * Return the UI element of the 'View/Edit Core Component' submenu.
     *
     * @return the UI element of the 'View/Edit Core Component' submenu
     */
    WebElement getViewEditCoreComponentSubMenu();

    /**
     * Open the 'View/Edit Core Component' submenu to enter the page.
     *
     * @return the 'View/Edit Core Component' page object
     */
    ViewEditCoreComponentPage openViewEditCoreComponentSubMenu();

    /**
     * Return the UI element of the 'View/Edit Code List' submenu.
     *
     * @return the UI element of the 'View/Edit Code List' submenu
     */
    WebElement getViewEditCodeListSubMenu();

    /**
     * Open the 'View/Edit Code List' submenu to enter the page.
     *
     * @return the 'View/Edit Code List' page object
     */
    ViewEditCodeListPage openViewEditCodeListSubMenu();

    /**
     * Return the UI element of the 'View/Edit Agency ID List' submenu.
     *
     * @return the UI element of the 'View/Edit Agency ID List' submenu
     */
    WebElement getViewEditAgencyIDListSubMenu();

    /**
     * Open the 'View/Edit Agency ID List' submenu to enter the page.
     *
     * @return the 'View/Edit Agency ID List' page object
     */
    ViewEditAgencyIDListPage openViewEditAgencyIDListSubMenu();

    /**
     * Return the UI element of the 'View/Edit Release' submenu.
     *
     * @return the UI element of the 'View/Edit Release' submenu
     */
    WebElement getViewEditReleaseSubMenu();

    /**
     * Open the 'View/Edit Release' submenu to enter the page.
     *
     * @return the 'View/Edit Release' page object
     */
    ViewEditReleasePage openViewEditReleaseSubMenu();

    /**
     * Return the UI element of the 'View/Edit Namespace' submenu.
     *
     * @return the UI element of the 'View/Edit Namespace' submenu
     */
    WebElement getViewEditNamespaceSubMenu();

    /**
     * Open the 'View/Edit Namespace' submenu to enter the page.
     *
     * @return the 'View/Edit Namespace' page object
     */
    ViewEditNamespacePage openViewEditNamespaceSubMenu();

    /**
     * Return the title text of the 'Core Component' menu button.
     *
     * @return the title text of the 'Core Component' menu button
     */
    String getCoreComponentMenuButtonTitle();

}
