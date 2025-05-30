package org.oagi.score.e2e.page.bie;

import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.Dialog;
import org.oagi.score.e2e.page.SearchBarPage;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;

public interface SelectBaseProfileBIEDialog extends Dialog, SearchBarPage {

    WebElement getOwnerSelectField();

    void setOwner(String owner);

    WebElement getUpdaterSelectField();

    void setUpdater(String updater);

    WebElement getUpdatedStartDateField();

    void setUpdatedStartDate(LocalDateTime updatedStartDate);

    WebElement getUpdatedEndDateField();

    void setUpdatedEndDate(LocalDateTime updatedEndDate);

    void hitSearchButton();

    void selectBaseBIE(TopLevelASBIEPObject bie);

    void selectBaseBIE(String topLevelBIEPropertyName);

    WebElement getTableRecordByValue(String value);

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    WebElement getSelectButton();
}
