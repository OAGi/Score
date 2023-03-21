package org.oagi.score.e2e.menu;

import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.LoginPage;
import org.oagi.score.e2e.page.SettingsPasswordPage;
import org.openqa.selenium.WebElement;

/**
 * An interface of login ID menu
 */
public interface LoginIDMenu {

    /**
     * Return the parent of this page.
     *
     * @return the parent of this page
     */
    HomePage getParent();

    /**
     * Return the UI element of the 'Login ID' menu.
     *
     * @return the UI element of the 'Login ID' menu
     */
    WebElement getLoginIDMenuButton();


    /**
     * Click the 'Login ID' menu to expand it.
     */
    void expandLoginIDMenu();

    /**
     * Return the UI element of the 'Sign in' label.
     *
     * @return the UI element of the 'Sign in' label
     */
    WebElement getSignInLabel();

    /**
     * Return the text in the 'Sign in' label.
     *
     * @return the text in the 'Sign in' label
     */
    String getSignInLabelText();

    /**
     * Return the UI element of the 'OAGIS Terminology' button.
     *
     * @return the UI element of the 'OAGIS Terminology' button
     */
    WebElement getOAGISTerminologyButton();

    /**
     * Click 'OAGIS Terminology' button if it is not checked.
     */
    void checkOAGISTerminology();

    /**
     * Return {@code true} if 'OAGIS Terminology' button is checked, otherwise {@code false}.
     *
     * @return {@code true} if 'OAGIS Terminology' button is checked, otherwise {@code false}
     */
    boolean isOAGISTerminologyChecked();

    /**
     * Return the UI element of the 'CCTS Terminology' button.
     *
     * @return the UI element of the 'CCTS Terminology' button
     */
    WebElement getCCTSTerminologyButton();

    /**
     * Click 'CCTS Terminology' button if it is not checked.
     */
    void checkCCTSTerminology();

    /**
     * Return {@code true} if 'CCTS Terminology' button is checked, otherwise {@code false}.
     *
     * @return {@code true} if 'CCTS Terminology' button is checked, otherwise {@code false}
     */
    boolean isCCTSTerminologyChecked();

    /**
     * Return the UI element of the 'Settings' submenu.
     *
     * @return the UI element of the 'Settings' submenu
     */
    WebElement getSettingsSubMenu();

    /**
     * Open the 'Settings' submenu to enter the page.
     *
     * @return the 'Settings' page object
     */
    SettingsPasswordPage openSettingsSubMenu();

    /**
     * Return the UI element of the 'Logout' button.
     *
     * @return the UI element of the 'Logout' button
     */
    WebElement getLogoutButton();

    /**
     * Click the 'Logout' button. It redirects to login page.
     *
     * @return Login page object
     */
    LoginPage logout();

}
