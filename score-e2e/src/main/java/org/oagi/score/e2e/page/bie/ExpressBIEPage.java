package org.oagi.score.e2e.page.bie;

import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'Express BIE' page.
 */
public interface ExpressBIEPage extends Page {

    void selectBIEForExpression(TopLevelASBIEPObject topLevelASBIEP);

    void setBranch(String branch);

    WebElement getBranchSelectField();

    WebElement getDENField();

    void setDEN(String den);

    WebElement getSearchButton();

    void hitSearchButton();

    WebElement getTableRecordByValue(String value);

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    void hitGenerateButton();

    WebElement getGenerateButton();
}
