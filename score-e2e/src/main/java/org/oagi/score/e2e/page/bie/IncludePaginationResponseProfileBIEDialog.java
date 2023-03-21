package org.oagi.score.e2e.page.bie;

import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.Dialog;
import org.openqa.selenium.WebElement;

public interface IncludePaginationResponseProfileBIEDialog extends Dialog {

    WebElement getBusinessContextField();

    void setBusinessContext(String context);

    WebElement getSearchButton();

    void hitSearchButton();

    WebElement getTableRecordByValue(String value);

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    void selectPaginationResponseProfile(TopLevelASBIEPObject paginationResponseASBIEP, BusinessContextObject context);

    WebElement getSelectButton();
}
