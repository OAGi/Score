package org.oagi.score.e2e.page.oas;

import org.oagi.score.e2e.obj.OpenAPIDocumentObject;
import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;

/**
 * An interface of 'OpenAPI Document' page.
 */
public interface OpenAPIDocumentPage extends Page {

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
     * Return the UI element of the 'Title' field.
     *
     * @return the UI element of the 'Title' field
     */
    WebElement getTitleField();

    /**
     * Set the 'Title' field with the given text.
     *
     * @param title Title
     */
    void setTitle(String title);

    /**
     * Return the UI element of the 'Description' field.
     *
     * @return the UI element of the 'Description' field
     */
    WebElement getDescriptionField();

    /**
     * Set the 'Description' field with the given text.
     *
     * @param description Description
     */
    void setDescription(String description);

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
     * Return the total number of items being paged.
     *
     * @return the total number of items being paged
     */
    int getTotalNumberOfItems();

    /**
     * Return the UI element of the 'Previous Page' button in the paginator.
     *
     * @return the UI element of the 'Previous Page' button in the paginator
     */
    WebElement getPreviousPageButton();

    /**
     * Return the UI element of the 'Next Page' button in the paginator.
     *
     * @return the UI element of the 'Next Page' button in the paginator
     */
    WebElement getNextPageButton();

    /**
     * Return the UI element of the 'New OpenAPI Document' button.
     *
     * @return the UI element of the 'New OpenAPI Document' button
     */
    WebElement getNewOpenAPIDocumentButton();

    /**
     * Open the 'Create OpenAPI Document' page to create a new OpenAPI Document.
     *
     * @return the 'Create OpenAPI Document' page object
     */
    CreateOpenAPIDocumentPage openCreateOpenAPIDocumentPage();

    /**
     * Open the 'Edit OpenAPI Document' page by the OpenAPI document object.
     *
     * @param openAPIDocument the OpenAPI document object
     * @return the 'Edit OpenAPI Document' page object
     */
    EditOpenAPIDocumentPage openEditOpenAPIDocumentPage(OpenAPIDocumentObject openAPIDocument);

    /**
     * Return the UI element of the 'Discard' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Discard' button
     */
    WebElement getDiscardButton(boolean enabled);

    /**
     * Discard the OpenAPI Document.
     *
     * @param openAPIDocument OpenAPI Document
     */
    void discard(OpenAPIDocumentObject openAPIDocument);

}
