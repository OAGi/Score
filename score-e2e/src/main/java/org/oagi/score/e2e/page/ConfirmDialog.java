package org.oagi.score.e2e.page;

import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * The confirm dialog, which asks the user to go on (the action) or to cancel.
 */
public interface ConfirmDialog extends Dialog {

    /**
     * Return the text of the dialog, without its list.
     *
     * @return the text of the dialog
     */
    String getMessage();

    /**
     * Return the texts of the list items of the dialog.
     *
     * @return the texts of the list items
     */
    List<String> getListItems();

    /**
     * Return the labels of the buttons of the dialog, in the order they are shown.
     *
     * @return the labels of the buttons
     */
    List<String> getButtonLabels();

    /**
     * Return the UI element of the button with the given label.
     *
     * @param label label of the button, e.g. {@code Trim and continue}
     * @return the UI element of the button
     */
    WebElement getButton(String label);

    /**
     * Click the button with the given label.
     *
     * @param label label of the button
     */
    void clickButton(String label);
}
