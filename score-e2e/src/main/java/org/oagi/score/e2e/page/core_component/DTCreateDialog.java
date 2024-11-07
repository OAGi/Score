package org.oagi.score.e2e.page.core_component;

import org.oagi.score.e2e.page.Dialog;
import org.oagi.score.e2e.page.SearchBarPage;
import org.openqa.selenium.WebElement;

public interface DTCreateDialog extends Dialog, SearchBarPage {

    void selectBasedDTByDEN(String den);

    WebElement getCreateButton();

    void hitSearchButton();

    WebElement getTableRecordAtIndex(int idx);

    WebElement getTableRecordByValue(String value);

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    WebElement getDENField();

    void hitCreateButton();

}
