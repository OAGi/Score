package org.oagi.score.e2e.page.code_list;

import org.oagi.score.e2e.page.Page;
import org.oagi.score.e2e.page.bie.UpliftBIEVerificationPage;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;

/**
 * An interface of 'Uplift Code List' page.
 */
public interface UpliftCodeListPage extends Page {
    /**
     * Return the UI element of the 'Source Branch' select field.
     *
     * @return the UI element of the 'Source Branch' select field
     */
    WebElement getSourceBranchSelectField();

    /**
     * Set the 'Source Branch' select field with the given text.
     *
     * @param sourceBranch Branch
     */
    void setSourceBranch(String sourceBranch);

    /**
     * Return the UI element of the 'Target Branch' select field.
     *
     * @return the UI element of the 'Target Branch' select field
     */
    WebElement getTargetBranchSelectField();

    /**
     * Set the 'Target Branch' select field with the given text.
     *
     * @param targetBranch Branch
     */
    void setTargetBranch(String targetBranch);

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
     * @param deprecated True or False
     */
    void setDeprecated(String deprecated);

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
     * Return the UI element of the 'Name' field.
     *
     * @return the UI element of the 'Name' field
     */
    WebElement getNameField();

    /**
     * Set the 'Name' field with the given text.
     *
     * @param name Name
     */
    void setName(String name);

    /**
     * Return the UI element of the 'Definition' field.
     *
     * @return the UI element of the 'Definition' field
     */
    WebElement getDefinitionField();

    /**
     * Set the 'Definition' select field with the given text.
     *
     * @param definition Definition
     */
    void setDefinition(String definition);

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
     * Return the UI element of the 'Uplift' button in the paginator.
     *
     * @return the UI element of the 'Uplift' button in the paginator
     */
    WebElement getUpliftButton();
}
