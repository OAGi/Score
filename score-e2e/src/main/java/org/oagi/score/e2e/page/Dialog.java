package org.oagi.score.e2e.page;

import org.openqa.selenium.WebElement;

/**
 * Dialog object interface
 */
public interface Dialog {

    /**
     * Return {@code true} if the dialog is opened, otherwise {@code false}.
     *
     * @return {@code true} if the dialog is opened, otherwise {@code false}
     */
    boolean isOpened();

    /**
     * Return the UI element of the dialog title.
     *
     * @return the UI element of the dialog title
     */
    WebElement getTitle();

}
