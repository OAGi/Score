package org.oagi.score.e2e.page.module;

import org.oagi.score.e2e.obj.ModuleSetObject;
import org.oagi.score.e2e.page.Page;
import org.oagi.score.e2e.page.SearchBarPage;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'View/Edit Module Set' page.
 */
public interface ViewEditModuleSetPage extends Page, SearchBarPage {

    WebElement getNewModuleSetButton();

    CreateModuleSetPage hitNewModuleSetButton();

    EditModuleSetPage openModuleSetByName(ModuleSetObject moduleSet);

    EditModuleSetPage openModuleSetByName(String moduleSetName);

    void hitSearchButton();

    WebElement getTableRecordAtIndex(int idx);

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    void setName(String name);

    WebElement getNameField();

    void discardModuleSet(String moduleSetName);

    WebElement clickOnDropDownMenu(WebElement element);
}
