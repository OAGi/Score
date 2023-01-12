package org.oagi.score.e2e.page.context;

import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.BusinessContextValueObject;
import org.oagi.score.e2e.obj.ContextSchemeValueObject;
import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'Create Business Context' page.
 */
public interface CreateBusinessContextPage extends Page {

    /**
     * Return the UI element of the 'Name' field.
     *
     * @return the UI element of the 'Name' field
     */
    WebElement getNameField();

    /**
     * Set {@code name} text to the 'Name' field.
     *
     * @param name name text
     */
    void setName(String name);

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
     * Open the business context value dialog for a new one.
     *
     * @return the business context value dialog object
     */
    BusinessContextValueDialog openBusinessContextValueDialog();

    /**
     * Open the business context value dialog for the given business context value
     *
     * @return the business context value dialog
     */
    BusinessContextValueDialog openBusinessContextValueDialog(BusinessContextValueObject businessContextValue);

    /**
     * Remove the business context value
     *
     * @param businessContextValue business context value
     */
    void removeBusinessContextValue(BusinessContextValueObject businessContextValue);

    /**
     * Remove the business context value by the given context scheme value
     *
     * @param contextSchemeValue context scheme value
     */
    void removeBusinessContextValueByContextSchemeValue(ContextSchemeValueObject contextSchemeValue);

    /**
     * Return the UI element of the 'Create' button.
     *
     * @return the UI element of the 'Create' button
     */
    WebElement getCreateButton();

    /**
     * Create a business context with the given information.
     *
     * @param businessContext business context
     * @return 'View/Edit Business Context' page object
     */
    ViewEditBusinessContextPage createBusinessContext(BusinessContextObject businessContext);
}
