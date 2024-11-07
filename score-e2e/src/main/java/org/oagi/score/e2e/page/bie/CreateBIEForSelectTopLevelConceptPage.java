package org.oagi.score.e2e.page.bie;

import org.oagi.score.e2e.page.Page;
import org.oagi.score.e2e.page.SearchBarPage;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;

/**
 * An interface of 'Create BIE - Select Top-Level Concept' page.
 */
public interface CreateBIEForSelectTopLevelConceptPage extends Page, SearchBarPage {

    /**
     * Return the UI element of the page subtitle.
     *
     * @return the UI element of the page subtitle
     */
    WebElement getSubtitle();

    /**
     * Return the UI element of the 'Selected Business Contexts' select field.
     *
     * @return the UI element of the 'Selected Business Contexts' select field
     */
    WebElement getSelectedBusinessContextsSelectField();

    /**
     * Return the UI element of the 'Branch' select field.
     *
     * @return the UI element of the 'Branch' select field
     */
    WebElement getBranchSelectField();

    /**
     * Set the 'Branch' select field with the given text.
     *
     * @param branch Branch
     */
    void setBranch(String branch);

    /**
     * Return the UI element of the 'State' select field.
     *
     * @return the UI element of the 'State' select field
     */
    WebElement getStateSelectField();

    /**
     * Return the UI element of the 'Deprecated' select field.
     *
     * @return the UI element of the 'Deprecated' select field
     */
    WebElement getDeprecatedSelectField();

    /**
     * @param deprecated
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
     * Return the UI element of the 'Module' field.
     *
     * @return the UI element of the 'Module' field
     */
    WebElement getModuleField();

    /**
     * Set the 'Module' select field with the given text.
     *
     * @param module Module
     */
    void setModule(String module);

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

    void selectCoreComponentByDEN(String ccDEN);

    /**
     * Return the UI element of the 'Create' button.
     *
     * @return the UI element of the 'Create' button
     */
    WebElement getCreateButton();

    /**
     * Create a new BIE with the given ASCCP DEN.
     *
     * @param asccpDEN the ASCCP DEN
     * @param branch   branch
     * @return 'Edit BIE' page object
     */
    EditBIEPage createBIE(String asccpDEN, String branch);

}
