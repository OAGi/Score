package org.oagi.score.e2e.page.code_list;

import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.CodeListObject;
import org.oagi.score.e2e.obj.ReleaseObject;
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

    /**
     * Return the UI element of the 'Owner' select field.
     *
     * @return the UI element of the 'Owner' select field
     */
    WebElement getOwnerSelectField();

    /**
     * Set the 'Owner' field.
     *
     * @param owner owner
     */
    void setOwner(String owner);

    /**
     * Return the UI element of the 'State' select field.
     *
     * @return the UI element of the 'State' select field
     */
    WebElement getStateSelectField();

    /**
     * Set the 'State' field.
     *
     * @param state state
     */
    void setState(String state);

    WebElement getSearchButton();

    void hitSearchButton();

    WebElement getTableRecordAtIndex(int idx);

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    EditCodeListPage hitUpliftButton(CodeListObject codeList, ReleaseObject sourceRelease, ReleaseObject targetRelease);

    WebElement getUpliftButton(boolean enabled);
}
