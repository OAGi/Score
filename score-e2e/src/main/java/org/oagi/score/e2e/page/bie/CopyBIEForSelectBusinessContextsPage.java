package org.oagi.score.e2e.page.bie;

import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * An interface of 'Copy BIE - Select Business Contexts' page.
 */
public interface CopyBIEForSelectBusinessContextsPage extends Page {

    /**
     * Return the UI element of the page subtitle.
     *
     * @return the UI element of the page subtitle
     */
    WebElement getSubtitle();

    /**
     * Return the UI element of the 'Name' field.
     *
     * @return the UI element of the 'Name' field
     */
    WebElement getNameField();

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
     * Go to the next step for copying BIE with selected business contexts.
     *
     * @param businessContexts selected business contexts
     * @return 'Copy BIE - Select BIE' page object
     */
    CopyBIEForSelectBIEPage next(List<BusinessContextObject> businessContexts);

}
