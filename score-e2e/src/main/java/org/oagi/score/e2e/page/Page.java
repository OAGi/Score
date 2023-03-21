package org.oagi.score.e2e.page;

import org.openqa.selenium.WebElement;

/**
 * Page object interface
 */
public interface Page {

    /**
     * Return {@code true} if the page is opened, otherwise {@code false}.
     *
     * @return {@code true} if the page is opened, otherwise {@code false}
     */
    boolean isOpened();

    /**
     * Open the page in the current browser window.
     */
    void openPage();

    /**
     * Return the UI element of the page title.
     *
     * @return the UI element of the page title
     */
    WebElement getTitle();

}
