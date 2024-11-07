package org.oagi.score.e2e.page.admin;

import org.oagi.score.e2e.page.Page;
import org.oagi.score.e2e.page.SearchBarPage;
import org.openqa.selenium.WebElement;

import java.util.NoSuchElementException;

/**
 * An interface of 'Accounts' page.
 */
public interface AccountsPage extends Page, SearchBarPage {

    /**
     * Return the UI element of 'Login ID' field.
     *
     * @return the UI element of 'Login ID' field
     */
    WebElement getLoginIDField();

    /**
     * Return the UI element of 'Name' field.
     *
     * @return the UI element of 'Name' field
     */
    WebElement getNameField();

    /**
     * Return the UI element of 'Organization' field.
     *
     * @return the UI element of 'Organization' field
     */
    WebElement getOrganizationField();

    /**
     * Return the UI element of 'Status' select field.
     *
     * @return the UI element of 'Status' select field
     */
    WebElement getStatusSelectField();

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
     * Return the UI element of 'New Account' button.
     *
     * @return the UI element of 'New Account' button
     */
    WebElement getNewAccountButton();

    /**
     * Open the 'New Account' page.
     *
     * @return 'New Account' page object
     */
    NewAccountPage openNewAccountPage();

    /**
     * Open the 'Edit Account' page by the given Login ID text.
     *
     * @param loginID Login ID text
     * @return 'Edit Account' page object
     * @throws NoSuchElementException if the account being requested does not exist.
     */
    EditAccountPage openEditAccountPageByLoginID(String loginID) throws NoSuchElementException;

}
