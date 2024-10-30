package org.oagi.score.e2e.page.bie;

import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.page.Page;
import org.oagi.score.e2e.page.SearchBarPage;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.util.List;

/**
 * An interface of 'Create BIE - Select Business Contexts' page.
 */
public interface CreateBIEForSelectBusinessContextsPage extends Page, SearchBarPage {

    /**
     * Return the UI element of the page subtitle.
     *
     * @return the UI element of the page subtitle
     */
    WebElement getSubtitle();

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
     * Select the business context.
     *
     * @param businessContext business context
     */
    void selectBusinessContext(BusinessContextObject businessContext);

    /**
     * Return the UI element of the 'Next' button.
     *
     * @return the UI element of the 'Next' button
     */
    WebElement getNextButton();

    /**
     * Return the UI element of the 'Create' button.
     *
     * @return the UI element of the 'Create' button
     */
    WebElement getCreateButton();

    /**
     * Go to the next step for creating BIE with selected business contexts.
     *
     * @param businessContexts selected business contexts
     * @return 'Create BIE - Select Top-Level Concept' page object
     */
    CreateBIEForSelectTopLevelConceptPage next(List<BusinessContextObject> businessContexts);

}
