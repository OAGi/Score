package org.oagi.score.e2e.page.context;

import org.oagi.score.e2e.obj.ContextCategoryObject;
import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'Create Context Category' page.
 */
public interface CreateContextCategoryPage extends Page {

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
     * Return the UI element of the 'Create' button.
     *
     * @return the UI element of the 'Create' button
     */
    WebElement getCreateButton();

    /**
     * Create a new context category with the given context category object.
     *
     * @param contextCategory context category object
     * @return the 'View/Edit Context Category' page object
     */
    ViewEditContextCategoryPage createContextCategory(ContextCategoryObject contextCategory);

}
