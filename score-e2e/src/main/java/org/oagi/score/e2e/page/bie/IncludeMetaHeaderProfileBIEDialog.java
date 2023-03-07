package org.oagi.score.e2e.page.bie;

import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

public interface IncludeMetaHeaderProfileBIEDialog extends Dialog {
    void setBranch(String branch);

    WebElement getBranchSelectField();

    WebElement getBusinessContextField();

    void setBusinessContext(String den);

    WebElement getSearchButton();

    void hitSearchButton();

    WebElement getTableRecordByValue(String value);

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    void selectMetaHeaderProfile(TopLevelASBIEPObject metaHeaderASBIEP, BusinessContextObject context);

    WebElement getSelectButton();
}
