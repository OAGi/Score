package org.oagi.score.e2e.page.context;

import org.oagi.score.e2e.obj.BusinessContextValueObject;
import org.oagi.score.e2e.obj.ContextCategoryObject;
import org.oagi.score.e2e.obj.ContextSchemeObject;
import org.oagi.score.e2e.obj.ContextSchemeValueObject;
import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'Business Context Value' dialog.
 */
public interface BusinessContextValueDialog extends Dialog {

    /**
     * Return the UI element of the 'Context Category' select field.
     *
     * @return the UI element of the 'Context Category' select field
     */
    WebElement getContextCategorySelectField();

    /**
     * Set {@code contextCategory} to the 'Context Category' field.
     *
     * @param contextCategory context category
     */
    void setContextCategory(ContextCategoryObject contextCategory);

    /**
     * Return the UI element of the 'Context Category - Description' field.
     *
     * @return the UI element of the 'Context Category - Description' field
     */
    WebElement getContextCategoryDescriptionField();

    /**
     * Return the UI element of the 'Context Scheme' select field.
     *
     * @return the UI element of the 'Context Scheme' select field
     */
    WebElement getContextSchemeSelectField();

    /**
     * Set {@code contextScheme} to the 'Context Scheme' field.
     *
     * @param contextScheme context scheme
     */
    void setContextScheme(ContextSchemeObject contextScheme);

    /**
     * Return the UI element of the 'Context Scheme - Scheme ID' field.
     *
     * @return the UI element of the 'Context Scheme - Scheme ID' field
     */
    WebElement getContextSchemeIDField();

    /**
     * Return the UI element of the 'Context Scheme - Agency ID' field.
     *
     * @return the UI element of the 'Context Scheme - Agency ID' field
     */
    WebElement getContextSchemeAgencyIDField();

    /**
     * Return the UI element of the 'Context Scheme - Version' field.
     *
     * @return the UI element of the 'Context Scheme - Version' field
     */
    WebElement getContextSchemeVersionField();

    /**
     * Return the UI element of the 'Context Scheme - Description' field.
     *
     * @return the UI element of the 'Context Scheme - Description' field
     */
    WebElement getContextSchemeDescriptionField();

    /**
     * Return the UI element of the 'Context Scheme Value' select field.
     *
     * @return the UI element of the 'Context Scheme Value' select field
     */
    WebElement getContextSchemeValueSelectField();

    /**
     * Set {@code contextSchemeValue} to the 'Context Scheme Value' field.
     *
     * @param contextSchemeValue context scheme value
     */
    void setContextSchemeValue(ContextSchemeValueObject contextSchemeValue);

    /**
     * Return the UI element of the 'Context Scheme Value - Meaning' field.
     *
     * @return the UI element of the 'Context Scheme Value - Meaning' field
     */
    WebElement getContextSchemeValueMeaningField();

    /**
     * Return the UI element of the 'Add' button.
     *
     * @return the UI element of the 'Add' button
     */
    WebElement getAddButton();

    /**
     * Add a business context value
     *
     * @param businessContextValue business context value
     */
    void addBusinessContextValue(BusinessContextValueObject businessContextValue);

    /**
     * Return the UI element of the 'Save' button.
     *
     * @return the UI element of the 'Save' button
     */
    WebElement getSaveButton();

    /**
     * Update a business context value
     *
     * @param businessContextValue business context value
     */
    void updateBusinessContextValue(BusinessContextValueObject businessContextValue);

    /**
     * Close the dialog
     */
    void close();

}
