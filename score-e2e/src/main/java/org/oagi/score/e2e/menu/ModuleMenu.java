package org.oagi.score.e2e.menu;

import org.oagi.score.e2e.page.module.ViewEditModuleSetPage;
import org.oagi.score.e2e.page.module.ViewEditModuleSetReleasePage;
import org.openqa.selenium.WebElement;

/**
 * An interface of module menu
 */
public interface ModuleMenu {

    /**
     * Return the UI element of the 'Module' menu.
     *
     * @return the UI element of the 'Module' menu
     */
    WebElement getModuleMenu();

    /**
     * Click the 'Module' menu to expand it.
     */
    void expandModuleMenu();

    /**
     * Return the UI element of the 'View/Edit Module Set' submenu.
     *
     * @return the UI element of the 'View/Edit Module Set' submenu
     */
    WebElement getViewEditModuleSetSubMenu();

    /**
     * Open the 'View/Edit Module Set' submenu to enter the page.
     *
     * @return the 'View/Edit Module Set' page object
     */
    ViewEditModuleSetPage openViewEditModuleSetSubMenu();

    /**
     * Return the UI element of the 'View/Edit Module Set Release' submenu.
     *
     * @return the UI element of the 'View/Edit Module Set Release' submenu
     */
    WebElement getViewEditModuleSetReleaseSubMenu();

    /**
     * Open the 'View/Edit Module Set Release' submenu to enter the page.
     *
     * @return the 'View/Edit Module Set Release' page object
     */
    ViewEditModuleSetReleasePage openViewEditModuleSetReleaseSubMenu();

}
