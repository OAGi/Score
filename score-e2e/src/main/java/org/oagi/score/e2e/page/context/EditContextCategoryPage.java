package org.oagi.score.e2e.page.context;

import org.oagi.score.e2e.obj.ContextCategoryObject;
import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'Edit Context Category' page.
 */
public interface EditContextCategoryPage extends Page {

    /**
     * Return the UI element of the 'Name' field.
     *
     * @return the UI element of the 'Name' field
     */
    WebElement getNameField();

    /**
     * Set {@code name} text to the 'Name' field.
     *
     * @param name name text
     */
    void setName(String name);

    /**
     * Return the text of the 'Name' field.
     *
     * @return the text of the 'Name' field
     */
    String getNameFieldText();

    /**
     * Return the UI element of the 'Description' field.
     *
     * @return the UI element of the 'Description' field
     */
    WebElement getDescriptionField();

    /**
     * Set {@code description} text to the 'Description' field.
     *
     * @param description description text
     */
    void setDescription(String description);

    /**
     * Return the text of the 'Description' field.
     *
     * @return the text of the 'Description' field
     */
    String getDescriptionFieldText();

    /**
     * Return the UI element of the 'Update' button.
     *
     * @return the UI element of the 'Update' button
     */
    WebElement getUpdateButton();

    /**
     * Update a context category with the given information.
     *
     * @param contextCategory a context category information for updating
     */
    void updateContextCategory(ContextCategoryObject contextCategory);

    /**
     * Return the UI element of the 'Discard' button.
     *
     * @return the UI element of the 'Discard' button
     */
    WebElement getDiscardButton();

    /**
     * Discard the context category.
     *
     * @return 'View/Edit Context Category' page object
     */
    ViewEditContextCategoryPage discardContextCategory();

}
