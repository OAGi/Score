package org.oagi.score.e2e.page.context;

import org.oagi.score.e2e.obj.ContextCategoryObject;
import org.oagi.score.e2e.obj.ContextSchemeObject;
import org.oagi.score.e2e.obj.ContextSchemeValueObject;
import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * An interface of 'Create Context Scheme' page.
 */
public interface CreateContextSchemePage extends Page {

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
     * Return the UI element of the 'Load from Code List' button.
     *
     * @return the UI element of the 'Load from Code List' button
     */
    WebElement getLoadFromCodeListButton();

    /**
     * Open the 'Load from Code List' dialog.
     *
     * @return the 'Load from Code List' dialog object
     */
    LoadFromCodeListDialog openLoadFromCodeListDialog();

    /**
     * Return the UI element of the 'Scheme ID' field.
     *
     * @return the UI element of the 'Scheme ID' field
     */
    WebElement getSchemeIDField();

    /**
     * Set {@code schemeID} text to the 'Scheme ID' field.
     *
     * @param schemeID scheme ID text
     */
    void setSchemeID(String schemeID);

    /**
     * Return the UI element of the 'Agency ID' field.
     *
     * @return the UI element of the 'Agency ID' field
     */
    WebElement getAgencyIDField();

    /**
     * Set {@code agencyID} text to the 'Agency ID' field.
     *
     * @param agencyID agency ID text
     */
    void setAgencyID(String agencyID);

    /**
     * Return the UI element of the 'Version' field.
     *
     * @return the UI element of the 'Version' field
     */
    WebElement getVersionField();

    /**
     * Set {@code version} text to the 'Version' field.
     *
     * @param version version text
     */
    void setVersion(String version);

    /**
     * Return the UI element of the 'Description' field.
     *
     * @return the UI element of the 'Description' field
     */
    WebElement getDescriptionField();

    /**
     * Set {@code description} text to the 'Description' field.
     *
     * @param description description
     */
    void setDescription(String description);

    /**
     * Return the UI element of the table record at the given index, which starts from 1.
     *
     * @param idx The index of the table record.
     * @return the UI element of the table record at the given index
     */
    WebElement getTableRecordAtIndex(int idx);

    /**
     * Return the UI element of the table record containing the given value.
     *
     * @param value value
     * @return the UI element of the table record
     */
    WebElement getTableRecordByValue(String value);

    /**
     * Return the UI element of the column of the given table record with the column name.
     *
     * @param tableRecord the table record
     * @param columnName  the column name
     * @return the UI element of the column
     */
    WebElement getColumnByName(WebElement tableRecord, String columnName);

    /**
     * Open the context scheme value dialog for a new one.
     *
     * @return the context scheme value dialog object
     */
    ContextSchemeValueDialog openContextSchemeValueDialog();

    /**
     * Open the context scheme value dialog for the given context scheme value
     *
     * @param contextSchemeValue the context scheme value
     * @return the context scheme value dialog
     */
    ContextSchemeValueDialog openContextSchemeValueDialog(ContextSchemeValueObject contextSchemeValue);

    /**
     * Open the context scheme value dialog for the given context scheme value
     *
     * @param value the context scheme value
     * @return the context scheme value dialog
     */
    ContextSchemeValueDialog openContextSchemeValueDialogByValue(String value);

    /**
     * Return {@code true} if the checkbox of the context scheme value is checked, otherwise {@code false}.
     *
     * @param value the context scheme value
     * @return {@code true} if the checkbox of the context scheme value is checked, otherwise {@code false}
     */
    boolean isContextSchemeValueChecked(String value);

    /**
     * Toggle the checkbox of the context scheme value.
     *
     * @param value the context scheme value
     */
    void toggleContextSchemeValue(String value);

    /**
     * Remove the context scheme value
     *
     * @param contextSchemeValue context scheme value
     */
    void removeContextSchemeValue(ContextSchemeValueObject contextSchemeValue);

    /**
     * Remove the context scheme value
     *
     * @param value context scheme value
     */
    void removeContextSchemeValue(String value);

    /**
     * Return the UI element of the 'Create' button.
     *
     * @return the UI element of the 'Create' button
     */
    WebElement getCreateButton();

    /**
     * Hit the 'Create' button.
     */
    void hitCreateButton();

    /**
     * Create a context scheme.
     *
     * @param contextCategory context category
     * @param contextScheme   context scheme
     * @return 'View/Edit Context Scheme' page object
     */
    ViewEditContextSchemePage createContextScheme(ContextCategoryObject contextCategory,
                                                  ContextSchemeObject contextScheme);

    /**
     * Create a context scheme with values.
     *
     * @param contextCategory        context category
     * @param contextScheme          context scheme
     * @param contextSchemeValueList context scheme values
     * @return 'View/Edit Context Scheme' page object
     */
    ViewEditContextSchemePage createContextScheme(ContextCategoryObject contextCategory,
                                                  ContextSchemeObject contextScheme,
                                                  List<ContextSchemeValueObject> contextSchemeValueList);

}
