package org.oagi.score.e2e.menu;

import org.oagi.score.e2e.page.library.ViewLibraryPage;
import org.openqa.selenium.WebElement;

/**
 * An interface of library menu
 */
public interface LibraryMenu {

    /**
     * Return the UI element of the 'Library' menu.
     *
     * @return the UI element of the 'Library' menu
     */
    WebElement getLibraryMenu();

    /**
     * Click the 'Library' menu to expand it.
     */
    void expandLibraryMenu();

    /**
     * Return the UI element of the 'View Library' submenu.
     *
     * @return the UI element of the 'View Library' submenu
     */
    WebElement getViewLibrarySubMenu();

    /**
     * Open the 'View Library' submenu to enter the page.
     *
     * @return the 'View Library' page object
     */
    ViewLibraryPage openViewLibrarySubMenu();

}
