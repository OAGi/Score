package org.oagi.score.e2e.page.core_component;

import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;

public interface SelectAssociationDialog extends Dialog {

    /**
     * Return the UI element of the 'Type' select field.
     *
     * @return the UI element of the 'Type' select field
     */
    WebElement getTypeSelectField();

    /**
     * Return the UI element of the 'State' select field.
     *
     * @return the UI element of the 'State' select field
     */
    WebElement getStateSelectField();

    /**
     * Set the 'State' select field with the given text.
     *
     * @param state State
     */
    void setState(String state);

    /**
     * Return the UI element of the 'Deprecated' select field.
     *
     * @return the UI element of the 'Deprecated' select field
     */
    WebElement getDeprecatedSelectField();

    /**
     * Set the 'Deprecated' select field with the given text.
     *
     * @param deprecated Deprecated
     */
    void setDeprecated(boolean deprecated);

    /**
     * Return the UI element of the 'Owner' select field.
     *
     * @return the UI element of the 'Owner' select field
     */
    WebElement getOwnerSelectField();

    /**
     * Set the 'Owner' select field with the given text.
     *
     * @param owner Owner
     */
    void setOwner(String owner);

    /**
     * Return the UI element of the 'Updater' select field.
     *
     * @return the UI element of the 'Updater' select field
     */
    WebElement getUpdaterSelectField();

    /**
     * Set the 'Updater' select field with the given text.
     *
     * @param updater Updater
     */
    void setUpdater(String updater);

    /**
     * Return the UI element of the 'Updated Start Date' field.
     *
     * @return the UI element of the 'Updated Start Date' field
     */
    WebElement getUpdatedStartDateField();

    /**
     * Set the 'Updated Start Date' field with the given date.
     *
     * @param updatedStartDate Updated Start Date
     */
    void setUpdatedStartDate(LocalDateTime updatedStartDate);

    /**
     * Return the UI element of the 'Updated End Date' field.
     *
     * @return the UI element of the 'Updated End Date' field
     */
    WebElement getUpdatedEndDateField();

    /**
     * Set the 'Updated End Date' field with the given date.
     *
     * @param updatedEndDate Updated End Date
     */
    void setUpdatedEndDate(LocalDateTime updatedEndDate);

    /**
     * Return the UI element of the 'DEN' field.
     *
     * @return the UI element of the 'DEN' field
     */
    WebElement getDENField();

    /**
     * Set the 'DEN' field with the given text.
     *
     * @param den DEN
     */
    void setDEN(String den);

    String getDENFieldLabel();

    /**
     * Return the UI element of the 'Definition' field.
     *
     * @return the UI element of the 'Definition' field
     */
    WebElement getDefinitionField();

    /**
     * Set the 'Definition' field with the given text.
     *
     * @param definition definition
     */
    void setDefinition(String definition);

    /**
     * Return the UI element of the 'Module' field.
     *
     * @return the UI element of the 'Module' field
     */
    WebElement getModuleField();

    /**
     * Set the 'Module' field with the given text.
     *
     * @param module module
     */
    void setModule(String module);

    String getModuleFieldLabel();

    /**
     * Return the UI element of the 'Search' button.
     *
     * @return the UI element of the 'Search' button
     */
    WebElement getSearchButton();

    /**
     * Hit the 'Search' button
     */
    void hitSearchButton();

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
     * Set the size of items to the 'Items per page' select field.
     *
     * @param items the size of items; 10, 25, 50
     */
    void setItemsPerPage(int items);

    /**
     * Return the UI element of the 'Insert' button.
     *
     * @return the UI element of the 'Insert' button
     */
    WebElement getInsertButton(boolean enabled);

    /**
     * Return the UI element of the 'Append' button.
     *
     * @return the UI element of the 'Append' button
     */
    WebElement getAppendButton(boolean enabled);

    /**
     * Select an association with the given DEN.
     *
     * @param den DEN
     */
    void selectAssociation(String den);

    /**
     * Return the UI element of the 'Cancel' button.
     *
     * @return the UI element of the 'Cancel' button
     */
    WebElement getCancelButton();

    /**
     * Close the dialog.
     */
    void close();

}
