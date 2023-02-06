package org.oagi.score.e2e.page.business_term;

import org.oagi.score.e2e.page.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;

import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;

/**
 * An interface for 'Assign Business Term' page
 */
public interface AssignBusinessTermBIEPage extends BasePage {

    /**
     * Return the UI element of the 'Branch' select field.
     *
     * @return the UI element of the 'Branch' select field
     */
    WebElement getBranchSelectField();

    /**
     * Set the 'Branch' select field
     *
     * @param branch Branch name
     */
    void setBranch(String branch);

    /**
     * Return the UI element of the 'State' select field.
     *
     * @return the UI element of the 'State' select field
     */
    WebElement getStateSelectField();

    /**
     * Set the 'State' select field with the given text.
     *
     * @param state WIP, QA or Production
     */
    void setState(String state);

    /**
     * Return the UI element of the 'Owner' select field.
     *
     * @return the UI element of the 'Owner' select field
     */
    WebElement getOwnerSelectField();

    /**
     * Set the 'Owner' select field with the given text.
     *
     * @param  owner Owner
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
     *Return the UI element of the 'DEN' field
     * @return the UI element of the 'DEN' field
     */
    WebElement getBIEDenField();

    /**
     * Set the 'BIE DEN' field with the given BIE DEN
     * @param bieDen  BIE DEN
     */
    void setBIEDenField(String bieDen);

    /**
     *Return the UI element of the 'Top Level BIE' field
     * @return the UI element of the 'Top Level BIE' field
     */
    WebElement getTopLevelBIEField();

    /**
     * Set the 'Top Level BIE' field with the given Top Level BIE
     * @param topLevelBIE  Top Level BIE
     */
    void setTopLevelBIE(String topLevelBIE);

    /**
     * Return the UI element of the 'Type' field.
     *
     * @return the UI element of the 'Type' field
     */
    WebElement getTypeField();

    /**
     * Set the 'Type' select field with the given text.
     *
     * @param bieType BBIE or ASBIE
     */
    void setType(String bieType);

    /**
     * Return the UI element of the 'Business Context' field.
     *
     * @return the UI element of the 'Business Context' field
     */
    WebElement getBusinessContextField();

    /**
     * Set {@code businessContext} text to the 'Business Context' field.
     *
     * @param businessContext Business Context
     */
    void setBusinessContext(String businessContext);

    /**
     * Return the text of the 'Business Context' field.
     *
     * @return the text of the 'Business Context' field
     */
    String getBusinessContextFieldText();

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
     * Return the UI element of the 'Search' button.
     *
     * @return the UI element of the 'Search' button
     */
    WebElement getSearchButton();

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
     * Return the UI checkbox element in front of the given name
     * @param name
     * @return the UI checkbox element
     */
    WebElement getCheckboxByName(String name);

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
     * Return the UI element of the 'Next' button.
     *
     * @return the UI element of the 'Next' button
     */
    WebElement getNextButton();

    /**
     * Return Assign Business Term page with subtitle: Select Business Term
     * @return Assign Business Term page
     */
    AssignBusinessTermBTPage hitNextButton();

}
