package org.oagi.score.e2e.page.core_component;

import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'CC Transfer Ownership' dialog.
 */
public interface TransferCCOwnershipDialog extends Dialog {

    /**
     * Return the UI element of the 'Login ID' field.
     *
     * @return the UI element of the 'Login ID' field
     */
    WebElement getLoginIDField();

    /**
     * Set the 'Login ID' field with the given text.
     *
     * @param loginID Login ID
     */
    void setLoginID(String loginID);

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
     * Return the UI element of the 'Organization' field.
     *
     * @return the UI element of the 'Organization' field
     */
    WebElement getOrganizationField();

    /**
     * Set the 'Organization' field with the given text.
     *
     * @param organization Organization
     */
    void setOrganization(String organization);

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
     * Return the UI checkbox for select at given index
     *
     * @param idx index
     * @return the UI checkbox element
     */
    WebElement getSelectCheckboxAtIndex(int idx);

    /**
     * Set the size of items to the 'Items per page' select field.
     *
     * @param items the size of items; 10, 25, 50
     */
    void setItemsPerPage(int items);

    /**
     * Return the UI element of the 'Transfer' button.
     *
     * @return the UI element of the 'Transfer' button
     */
    WebElement getTransferButton();

    /**
     * Transfer the ownership to the user who has the given login ID.
     *
     * @param loginId Login ID
     */
    void transfer(String loginId);

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