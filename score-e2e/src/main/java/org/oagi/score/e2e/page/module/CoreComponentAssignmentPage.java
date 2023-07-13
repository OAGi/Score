package org.oagi.score.e2e.page.module;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

public interface CoreComponentAssignmentPage extends Page {
    void setDenUnassigned(String name);

    WebElement getDenAssignedField();

    void setDenAssigned(String name);

    WebElement getDenUnassignedField();

    void selectUnassignedCCByDEN(String name);

    void selectAssignedCCByDEN(String name);


    WebElement getTableRecordByValueUnassignedCC(String value);

    WebElement getTableRecordByValueAssignedCC(String value);

    WebElement getTableRecordAtIndexAssignedCC(int idx);

    WebElement getTableRecordAtIndexUnassignedCC(int idx);

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    void hitAssignButton();

    WebElement getAssignButton();

    void selectModule(String moduleName);

    WebElement getModuleByName(String moduleName);

    void hitUnassignButton();

    WebElement getUnassignButton();
}
