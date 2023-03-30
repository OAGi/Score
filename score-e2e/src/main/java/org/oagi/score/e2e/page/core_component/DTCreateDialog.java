package org.oagi.score.e2e.page.core_component;

import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

public interface DTCreateDialog extends Dialog {
    void selectBasedDTByDEN(String den);

    WebElement getSearchButton();

    WebElement getCreateButton();

    void hitSearchButton();

    WebElement getTableRecordAtIndex(int idx);

    WebElement getTableRecordByValue(String value);

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    WebElement getDENField();

    void hitCreateButton();
}
