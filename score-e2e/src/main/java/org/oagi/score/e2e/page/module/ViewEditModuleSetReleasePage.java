package org.oagi.score.e2e.page.module;

import org.oagi.score.e2e.obj.ModuleSetReleaseObject;
import org.oagi.score.e2e.page.Page;
import org.oagi.score.e2e.page.SearchBarPage;
import org.openqa.selenium.WebElement;

/**
 * An interface of 'View/Edit Module Set Release' page.
 */
public interface ViewEditModuleSetReleasePage extends Page, SearchBarPage {

    CreateModuleSetReleasePage hitNewModuleSetReleaseButton();

    WebElement getNewModuleSetReleaseButton();

    EditModuleSetReleasePage openModuleSetReleaseByName(ModuleSetReleaseObject moduleSetRelease);

    EditModuleSetReleasePage openModuleSetReleaseByName(String name);

    void hitSearchButton();

    WebElement getTableRecordAtIndex(int idx);

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    void setName(String name);

    WebElement getNameField();
}
