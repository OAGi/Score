package org.oagi.score.e2e.page.context;

import org.oagi.score.e2e.obj.ContextSchemeValueObject;
import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'Context Scheme Value' dialog.
 */
public interface ContextSchemeValueDialog extends Dialog {

    /**
     * Return the UI element of the 'Value' field.
     *
     * @return the UI element of the 'Value' field
     */
    WebElement getValueField();

    /**
     * Set {@code value} to the 'Value' field.
     *
     * @param value value
     */
    void setValue(String value);

    /**
     * Return the UI element of the 'Meaning' field.
     *
     * @return the UI element of the 'Meaning' field
     */
    WebElement getMeaningField();

    /**
     * Set {@code meaning} to the 'Meaning' field.
     *
     * @param meaning meaning
     */
    void setMeaning(String meaning);

    /**
     * Return the UI element of the 'Add' button.
     *
     * @return the UI element of the 'Add' button
     */
    WebElement getAddButton();

    /**
     * Add a context scheme value
     *
     * @param contextSchemeValue context scheme value
     */
    void addContextSchemeValue(ContextSchemeValueObject contextSchemeValue);

    /**
     * Return the UI element of the 'Save' button.
     *
     * @return the UI element of the 'Save' button
     */
    WebElement getSaveButton();

    /**
     * Update a context scheme value
     *
     * @param contextSchemeValue context scheme value
     */
    void updateContextSchemeValue(ContextSchemeValueObject contextSchemeValue);

}
