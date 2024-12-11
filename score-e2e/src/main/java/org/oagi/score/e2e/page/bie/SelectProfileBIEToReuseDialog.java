package org.oagi.score.e2e.page.bie;

import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.Dialog;
import org.oagi.score.e2e.page.SearchBarPage;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;

public interface SelectProfileBIEToReuseDialog extends Dialog, SearchBarPage {

    WebElement getOwnerSelectField();

    void setOwner(String owner);

    WebElement getUpdaterSelectField();

    void setUpdater(String updater);

    WebElement getUpdatedStartDateField();

    void setUpdatedStartDate(LocalDateTime updatedStartDate);

    WebElement getUpdatedEndDateField();

    void setUpdatedEndDate(LocalDateTime updatedEndDate);

    WebElement getBusinessContextField();

    void setBusinessContext(String businessContext);

    WebElement getVersionField();

    void setVersion(String version);

    WebElement getRemarkField();

    void setRemark(String remark);

    void hitSearchButton();

    void selectBIEToReuse(TopLevelASBIEPObject bie);

    void selectBIEToReuse(String topLevelBIEPropertyName);

    WebElement getTableRecordByValue(String value);

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    WebElement getSelectButton();
}
