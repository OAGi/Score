package org.oagi.score.e2e.page.module;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

public interface CoreComponentAssignmentPage extends Page {
    void setDen(String name);

    WebElement getDenField();

    void selectCCByDEN(String name);

    WebElement getTableRecordAtIndex(int idx);

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    void hitAssignButton();

    WebElement getAssignButton();
}
