package org.oagi.score.e2e.page.oas;

import org.oagi.score.e2e.page.Dialog;
import org.oagi.score.e2e.page.SearchBarPage;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;

public interface AddBIEForOpenAPIDocumentDialog extends Dialog, SearchBarPage {

    WebElement getBranchSelectField();

    void setBranch(String branch);

    WebElement getStateSelectField();

    void setState(String state);

    WebElement getBusinessContextField();

    void setBusinessContext(String businessContext);

    WebElement getVersionField();

    void setVersion(String version);

    WebElement getRemarkField();

    void setRemark(String remark);

    WebElement getOwnerSelectField();

    void setOwner(String owner);

    WebElement getUpdaterSelectField();

    void setUpdater(String updater);

    WebElement getUpdatedStartDateField();

    void setUpdatedStartDate(LocalDateTime updatedStartDate);

    WebElement getUpdatedEndDateField();

    void setUpdatedEndDate(LocalDateTime updatedEndDate);

    void hitSearchButton();

    WebElement getTableRecordAtIndex(int idx);

    WebElement getTableRecordByValue(String value);

    WebElement getColumnByName(WebElement tableRecord, String columnName);

    void toggleSelect(WebElement tableRecord);

    void setVerb(WebElement tableRecord, String verb);

    void setMessageBody(WebElement tableRecord, String messageBody);

    boolean isMessageBodyOptionDisabled(WebElement tableRecord, String messageBody);

    WebElement getAddButton(boolean enabled);

    void hitAddButton();

    WebElement getCloseButton();

    void close();
}
