package org.oagi.score.e2e.page.code_list;

import org.oagi.score.e2e.obj.CodeListObject;
import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'Uplift Code List' page.
 */
public interface UpliftCodeListPage extends Page {

    void setSourceRelease(String branch);

    WebElement getSourceBranchSelectField();

    void setTargetRelease(String branch);

    WebElement getTargetBranchSelectField();

    void selectCodeList(String name);

    WebElement getCodeListField();

    void setCodeList(String name);

    WebElement getSearchButton();

    void hitSearchButton();

    WebElement getTableRecordAtIndex(int idx);

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    EditCodeListPage hitUpliftButton(String name, String branch);

    WebElement getUpliftButton(boolean enabled);
}
