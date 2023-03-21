package org.oagi.score.e2e.page.context;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;

/**
 * An interface of 'View/Edit Context Category' page.
 */
public interface ViewEditContextCategoryPage extends Page {

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
     * Return the UI element of the 'Name' field.
     *
     * @return the UI element of the 'Name' field
     */
    WebElement getNameField();

    /**
     * Set the 'Name' select field with the given text.
     *
     * @param name Name
     */
    void setName(String name);

    /**
     * Return the UI element of the 'Description' field.
     *
     * @return the UI element of the 'Description' field
     */
    WebElement getDescriptionField();

    /**
     * Set the 'Description' select field with the given text.
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
     * Hit the 'Search' button.
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
     * Return the UI element of the column of the given table record with the column name.
     *
     * @param tableRecord the table record
     * @param columnName  the column name
     * @return the UI element of the column
     */
    WebElement getColumnByName(WebElement tableRecord, String columnName);

    /**
     * Move the table to the next page via the pagination.
     */
    void goToNextPage();

    /**
     * Move the table to the previous page via the pagination.
     */
    void goToPreviousPage();

    /**
     * Return the UI element of the 'New Context Category' button.
     *
     * @return the UI element of the 'New Context Category' button
     */
    WebElement getNewContextCategoryButton();

    /**
     * Return the UI element of the 'Discard' button.
     *
     * @return the UI element of the 'Discard' button
     */
    WebElement getDiscardButton();

    /**
     * Open the 'Create Context Category' page.
     *
     * @return the 'Create Context Category' page object
     */
    CreateContextCategoryPage openCreateContextCategoryPage();

    /**
     * Open the 'Edit Context Category' page by the given context category name.
     *
     * @param contextCategoryName context category name
     * @return the 'Edit Context Category' page object
     * @throws NoSuchElementException if the context category being requested does not exist.
     */
    EditContextCategoryPage openEditContextCategoryPageByContextCategoryName(String contextCategoryName)
            throws NoSuchElementException;

}
