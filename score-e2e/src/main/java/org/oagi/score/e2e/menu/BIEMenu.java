package org.oagi.score.e2e.menu;

import org.oagi.score.e2e.page.bie.*;
import org.oagi.score.e2e.page.business_term.ViewEditBusinessTermPage;
import org.oagi.score.e2e.page.code_list.UpliftCodeListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.openqa.selenium.WebElement;

/**
 * An interface of BIE menu
 */
public interface BIEMenu {

    /**
     * Return the UI element of the 'BIE' menu.
     *
     * @return the UI element of the 'BIE' menu
     */
    WebElement getBIEMenu();

    /**
     * Click the 'BIE' menu to expand it.
     */
    void expandBIEMenu();

    /**
     * Return the UI element of the 'View/Edit BIE' submenu.
     *
     * @return the UI element of the 'View/Edit BIE' submenu
     */
    WebElement getViewEditBIESubMenu();

    /**
     * Open the 'View/Edit BIE' submenu to enter the page.
     *
     * @return the 'View/Edit BIE' page object
     */
    ViewEditBIEPage openViewEditBIESubMenu();

    /**
     * Return the UI element of the 'Create BIE' submenu.
     *
     * @return the UI element of the 'Create BIE' submenu
     */
    WebElement getCreateBIESubMenu();

    /**
     * Open the 'Create BIE' submenu to enter the page.
     *
     * @return the 'Create BIE' page object
     */
    CreateBIEForSelectBusinessContextsPage openCreateBIESubMenu();

    /**
     * Return the UI element of the 'Copy BIE' submenu.
     *
     * @return the UI element of the 'Copy BIE' submenu
     */
    WebElement getCopyBIESubMenu();

    /**
     * Open the 'Copy BIE' submenu to enter the page.
     *
     * @return the 'Copy BIE' page object
     */
    CopyBIEForSelectBusinessContextsPage openCopyBIESubMenu();

    /**
     * Return the UI element of the 'Uplift BIE' submenu.
     *
     * @return the UI element of the 'Uplift BIE' submenu
     */
    WebElement getUpliftBIESubMenu();

    /**
     * Open the 'Uplift BIE' submenu to enter the page.
     *
     * @return the 'Uplift BIE' page object
     */
    UpliftBIEPage openUpliftBIESubMenu();

    /**
     * Return the UI element of the 'Express BIE' submenu.
     *
     * @return the UI element of the 'Express BIE' submenu
     */
    WebElement getExpressBIESubMenu();

    /**
     * Open the 'Express BIE' submenu to enter the page.
     *
     * @return the 'Express BIE' page object
     */
    ExpressBIEPage openExpressBIESubMenu();

    /**
     * Return the UI element of the 'Reuse Report' submenu.
     *
     * @return the UI element of the 'Reuse Report' submenu
     */
    WebElement getReuseReportSubMenu();

    /**
     * Open the 'Reuse Report' submenu to enter the page.
     *
     * @return the 'Reuse Report' page object
     */
    ReuseReportPage openReuseReportSubMenu();

    /**
     * Return the UI element of the 'View/Edit Business Term' submenu.
     *
     * @return the UI element of the 'View/Edit Business Term' submenu
     */
    WebElement getViewEditBusinessTermSubMenu();

    /**
     * Open the 'View/Edit Business Term' submenu to enter the page.
     *
     * @return the 'View/Edit Business Term' page object
     */
    ViewEditBusinessTermPage openViewEditBusinessTermSubMenu();

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
     * Return the UI element of the 'Uplift Code List' submenu.
     *
     * @param clickable {@code true} if the menu is enabled, otherwise {@code false}.
     * @return the UI element of the 'Uplift Code List' submenu
     */
    WebElement getUpliftCodeListSubMenu(boolean clickable);

    /**
     * Open the 'Uplift Code List' submenu to enter the page.
     *
     * @return the 'Uplift Code List' page object
     */
    UpliftCodeListPage openUpliftCodeListSubMenu();

    /**
     * Return the title text of the 'BIE' menu button.
     *
     * @return the title text of the 'BIE' menu button
     */
    String getBIEMenuButtonTitle();

    /**
     * Return the title text of the 'Create BIE' submenu button.
     *
     * @return the title text of the 'Create BIE' submenu button
     */
    String getCreateBIESubMenuButtonTitle();

    /**
     * Return the title text of the 'View/Edit BIE' submenu button.
     *
     * @return the title text of the 'View/Edit BIE' submenu button
     */
    String getViewEditBIESubMenuButtonTitle();

    /**
     * Return the title text of the 'Copy BIE' submenu button.
     *
     * @return the title text of the 'Copy BIE' submenu button
     */
    String getCopyBIESubMenuButtonTitle();

    /**
     * Return the title text of the 'Express BIE' submenu button.
     *
     * @return the title text of the 'Express BIE' submenu button
     */
    String getGenerateExpressionButtonTitle();

}
