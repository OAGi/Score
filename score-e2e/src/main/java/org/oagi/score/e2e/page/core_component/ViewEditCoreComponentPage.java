package org.oagi.score.e2e.page.core_component;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'View/Edit Core Component' page.
 */
public interface ViewEditCoreComponentPage extends Page {

    /**
     * Return the UI element of the 'DEN' field.
     *
     * @return the UI element of the 'DEN' field
     */
    WebElement getDENField();

    /**
     * Return the 'DEN' field label text.
     *
     * @return the 'DEN' field label text
     */
    String getDENFieldLabel();

    /**
     * Return the UI element of the 'Module' field.
     *
     * @return the UI element of the 'Module' field
     */
    WebElement getModuleField();

    /**
     * Return the 'Module' field label text.
     *
     * @return the 'Module' field label text
     */
    String getModuleFieldLabel();

    void setBranch(String branch);

    WebElement getBranchSelectField();

    ACCViewEditPage openACCViewEditPageByDenAndBranch(String den, String branch);

    ASCCPViewEditPage openASCCPViewEditPageByDenAndBranch(String den, String branch);

    BCCPViewEditPage openBCCPViewEditPageByDenAndBranch(String den, String branch);

    DTViewEditPage openDTViewEditPageByDenAndBranch(String den, String branch);

    WebElement getSearchButton();

    WebElement getTableRecordAtIndex(int idx);

    /**
     * Return the UI element of the table record containing the given value.
     *
     * @param value value
     * @return the UI element of the table record
     */
    WebElement getTableRecordByValue(String value);

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    /**
     * Return the number of only Core Components by state
     * @param state the Core Component state: WIP, QA or Production
     * @return the quantity of Only Core Components by state
     */
    int getNumberOfOnlyCCsPerStateAreListed(String state);

    /**
     * Return a unique table record based on the Core Component name and the owner
     * @param name  the Core Component name
     * @param owner
     * @return a single table record based on the Core Component name and the owner
     */
    WebElement getTableRecordByCCNameAndOwner(String name, String owner);

    /**
     * Set the size of items to the 'Items per page' select field.
     *
     * @param items the size of items; 10, 25, 50
     */
    void setItemsPerPage(int items);

}
