package org.oagi.score.e2e.menu;

import org.oagi.score.e2e.page.context.ViewEditBusinessContextPage;
import org.oagi.score.e2e.page.context.ViewEditContextCategoryPage;
import org.oagi.score.e2e.page.context.ViewEditContextSchemePage;
import org.openqa.selenium.WebElement;

/**
 * An interface of context menu
 */
public interface ContextMenu {

    /**
     * Return the UI element of the 'Context' menu.
     *
     * @return the UI element of the 'Context' menu
     */
    WebElement getContextMenu();

    /**
     * Click the 'Context' menu to expand it.
     */
    void expandContextMenu();

    /**
     * Return the UI element of the 'View/Edit Context Category' submenu.
     *
     * @return the UI element of the 'View/Edit Context Category' submenu
     */
    WebElement getViewEditContextCategorySubMenu();

    /**
     * Open the 'View/Edit Context Category' submenu to enter the page.
     *
     * @return the 'View/Edit Context Category' page object
     */
    ViewEditContextCategoryPage openViewEditContextCategorySubMenu();

    /**
     * Return the UI element of the 'View/Edit Context Scheme' submenu.
     *
     * @return the UI element of the 'View/Edit Context Scheme' submenu
     */
    WebElement getViewEditContextSchemeSubMenu();

    /**
     * Open the 'View/Edit Context Scheme' submenu to enter the page.
     *
     * @return the 'View/Edit Context Scheme' page object
     */
    ViewEditContextSchemePage openViewEditContextSchemeSubMenu();

    /**
     * Return the UI element of the 'View/Edit Business Context' submenu.
     *
     * @return the UI element of the 'View/Edit Business Context' submenu
     */
    WebElement getViewEditBusinessContextSubMenu();

    /**
     * Open the 'View/Edit Business Context' submenu to enter the page.
     *
     * @return the 'View/Edit Business Context' page object
     */
    ViewEditBusinessContextPage openViewEditBusinessContextSubMenu();

}
