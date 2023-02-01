package org.oagi.score.e2e.page.business_term;

import org.oagi.score.e2e.page.BasePage;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;

public interface AssignBusinessTermBTPage extends BasePage {
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
     * Return the UI element of the 'Business Term' field.
     *
     * @return the UI element of the 'Business Term' field
     */
    WebElement getBusinessTermField();

    /**
     * Set the 'Business Term' select field with the given text.
     *
     * @param termName business term Name
     */
    void setBusinessTerm(String termName);

    /**
     * Return the UI element of the 'External Reference URI' field.
     *
     * @return the UI element of the 'External Reference URI' field
     */
    WebElement getExternalReferenceURIField();

    /**
     * Set the 'External Reference URI' select field with the given text.
     *
     * @param externalReferenceURI External Reference URI text
     */
    void setExternalReferenceURI(String externalReferenceURI);

    /**
     * Return the UI element of the 'External Reference ID' field.
     *
     * @return the UI element of the 'External Reference ID' field
     */
    WebElement getExternalReferenceIDField();

    /**
     * Set the 'External Reference ID' select field with the given text.
     *
     * @param externalReferenceID External Reference ID text
     */
    void setExternalReferenceID(String externalReferenceID);

    /**
     * Return the UI element of the 'Filter by same CC' checkbox.
     * @return the UI element of the 'Filter by same CC' checkbox
     */
    WebElement getFilterBySameCCCheckbox();

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
     * Return the UI checkbox for select at given index
     * @param idx index
     * @return the UI checkbox element
     */
    WebElement getSelectCheckboxAtIndex(int idx);

    /**
     * Move the table to the next page via the pagination.
     */
    void goToNextPage();

    /**
     * Move the table to the previous page via the pagination.
     */
    void goToPreviousPage();

    /**
     * Return the UI element of the 'Type Code' field.
     *
     * @return the UI element of the 'Type Code' field
     */
    WebElement getTypeCodeField();

    /**
     * Set the 'Type Code' field with the given text.
     *
     * @param typeCode Type Code
     */
    void setTypeCode(String typeCode);

    /**
     * Return the UI element of the 'Preferred Business Term' checkbox.
     * @return the UI element of the 'Preferred Business Term' checkbox
     */
    WebElement getPreferredBusinessTermCheckbox();

    /**
     * Return the UI element of the 'Create' button.
     *
     * @return the UI element of the 'Create' button
     */
    WebElement getCreateButton();
}
