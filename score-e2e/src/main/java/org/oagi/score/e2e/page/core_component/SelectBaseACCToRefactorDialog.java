package org.oagi.score.e2e.page.core_component;

import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

public interface SelectBaseACCToRefactorDialog extends Dialog {

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
     * Return the UI element of the 'Cancel' button.
     *
     * @return the UI element of the 'Cancel' button
     */
    WebElement getCancelButton();

    ACCViewEditPage hitCancelButton();

    /**
     * Return the UI element of the 'Analyze' button.
     *
     * @return the UI element of the 'Analyze' button
     */
    WebElement getAnalyzeButton();

    SelectBaseACCToRefactorDialog hitAnalyzeButton();

    /**
     * Return the UI element of the 'Refactor' button.
     *
     * @return the UI element of the 'Refactor' button
     */
    WebElement getRefactorButton(boolean enabled);

    ACCViewEditPage hitRefactorButton();

}
