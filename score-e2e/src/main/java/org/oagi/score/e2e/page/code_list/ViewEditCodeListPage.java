package org.oagi.score.e2e.page.code_list;

import org.oagi.score.e2e.page.Page;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'View/Edit Code List' page.
 */
public interface ViewEditCodeListPage extends Page {

    EditCodeListPage openCodeListViewEditPageByNameAndBranch(String name, String branch);

    WebElement getNameField();

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    WebElement getTableRecordByValue(String value);

    void hitSearchButton();

    WebElement getSearchButton();

    void setBranch(String branch);

    WebElement getBranchSelectField();

    void searchCodeListByNameAndBranch(String name, String releaseNumber);
}
