package org.oagi.score.e2e.menu;

import org.oagi.score.e2e.page.help.AboutPage;
import org.oagi.score.e2e.page.help.UserGuidePage;
import org.openqa.selenium.WebElement;

/**
 * An interface of help menu
 */
public interface HelpMenu {

    /**
     * Return the UI element of the 'Help' menu.
     *
     * @return the UI element of the 'Help' menu
     */
    WebElement getHelpMenu();

    /**
     * Click the 'Help' menu to expand it.
     */
    void expandHelpMenu();

    /**
     * Return the UI element of the 'About' submenu.
     *
     * @return the UI element of the 'About' submenu
     */
    WebElement getAboutSubMenu();

    /**
     * Open the 'About' submenu to enter the page.
     *
     * @return the 'About' page object
     */
    AboutPage openAboutSubMenu();

    /**
     * Return the UI element of the 'User Guide' submenu.
     *
     * @return the UI element of the 'User Guide' submenu
     */
    WebElement getUserGuideSubMenu();

    /**
     * Open the 'User Guide' submenu to enter the page.
     *
     * @return the 'User Guide' page object
     */
    UserGuidePage openUserGuideSubMenu();

}
