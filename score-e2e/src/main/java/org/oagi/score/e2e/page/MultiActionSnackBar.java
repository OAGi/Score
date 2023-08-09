package org.oagi.score.e2e.page;

import org.openqa.selenium.WebElement;

public interface MultiActionSnackBar {

    /**
     * Return the UI element of the header.
     *
     * @return the UI element of the header
     */
    WebElement getHeaderElement();

    /**
     * Return the UI element of the message.
     *
     * @return the UI element of the message
     */
    WebElement getMessageElement();

    /**
     * Return the UI element of the action button by given name.
     *
     * @param name button name
     * @return the UI element of the action button
     */
    WebElement getActionButtonByName(String name);

}
