package org.oagi.score.e2e.page.bie;

import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;

/**
 * An interface of 'View/Edit BIE' page.
 */
public interface ViewEditBIEPage extends Page {

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
     * Set the 'State' select field with the given text.
     *
     * @param state State
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
     * Return the UI element of the 'Business Context' field.
     *
     * @return the UI element of the 'Business Context' field
     */
    WebElement getBusinessContextField();

    /**
     * Set the 'Business Context' select field with the given text.
     *
     * @param businessContext Business Context
     */
    void setBusinessContext(String businessContext);

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
     * Open the 'Transfer BIE Ownership' dialog.
     *
     * @param tr the table record
     * @return the 'Transfer BIE Ownership' dialog object
     */
    TransferBIEOwnershipDialog openTransferBIEOwnershipDialog(WebElement tr);

    /**
     * Return the UI element of the 'New BIE' button.
     *
     * @return the UI element of the 'New BIE' button
     */
    WebElement getNewBIEButton();

    /**
     * Open the 'Create BIE - Select Business Contexts' page to create a new BIE.
     *
     * @return the 'Create BIE - Select Business Contexts' page object
     */
    CreateBIEForSelectBusinessContextsPage openCreateBIEPage();

    /**
     * Open the 'Edit BIE' page by the top-level ASBIEP.
     *
     * @param topLevelASBIEP the top-level ASBIEP
     * @return the 'Edit BIE' page object
     * @throws NoSuchElementException if it fails to open the 'Edit BIE' page.
     */
    EditBIEPage openEditBIEPage(TopLevelASBIEPObject topLevelASBIEP);

    /**
     * Open the 'Edit BIE' page by the table record.
     *
     * @param tr the table record
     * @return the 'Edit BIE' page object
     * @throws NoSuchElementException if it fails to open the 'Edit BIE' page.
     */
    EditBIEPage openEditBIEPage(WebElement tr);

    /**
     * Return the UI element of the 'Discard' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Discard' button
     */
    WebElement getDiscardButton(boolean enabled);

    /**
     * Discard the top-level ASBIEP.
     *
     * @param topLevelASBIEP Top-Level ASBIEP
     */
    void discard(TopLevelASBIEPObject topLevelASBIEP);

    /**
     * Return the number of only BIEs by state
     *
     * @param state the BIE state: WIP, QA or Production
     * @return the quantity of Only BIEs by state
     */
    int getNumberOfOnlyBIEsPerStateAreListed(String state);

    /**
     * Return the UI element of the 'Move to QA' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Move to QA' button
     */
    WebElement getMoveToQA(boolean enabled);

    /**
     * Make the BIE to the QA state. It works only if the BIE is in the WIP state and the 'Update' button is disabled.
     *
     * @throws org.openqa.selenium.TimeoutException if the BIE is not in the WIP state or the 'Update' button is enabled.
     */
    void moveToQA();

    /**
     * Return the UI element of the 'Move to Production' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Move to Production' button
     */
    WebElement getMoveToProduction(boolean enabled);

    /**
     * Make the BIE to the Production state. It works only if the BIE is in the QA state and the 'Update' button is disabled.
     *
     * @throws org.openqa.selenium.TimeoutException if the BIE is not in the QA state or the 'Update' button is enabled.
     */
    void moveToProduction();

    /**
     * Return the UI element of the 'Back to WIP' button.
     *
     * @param enabled {@code true} if the button should be enabled, otherwise {@code false}
     * @return the UI element of the 'Back to WIP' button
     */
    WebElement getBackToWIP(boolean enabled);

    /**
     * Make the BIE to the WIP state. It works only if the BIE is in the QA state and the 'Update' button is disabled.
     *
     * @throws org.openqa.selenium.TimeoutException if the BIE is not in the QA state or the 'Update' button is enabled.
     */
    void BackToWP();

}
