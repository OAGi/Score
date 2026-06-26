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

    /**
     * Check/uncheck the {@code Array Indicator} on a candidate row in the Add-BIE dialog (issue #1732):
     * setting it AT ADD TIME makes the assigned operation's operationId carry the {@code List} suffix,
     * which the post-add grid toggle (TC_43_6_3) does not exercise.
     *
     * @param tableRecord the candidate row
     * @param checked     desired checkbox state
     */
    void setArrayIndicator(WebElement tableRecord, boolean checked);

    boolean isMessageBodyOptionDisabled(WebElement tableRecord, String messageBody);

    /**
     * Return the inline duplicate-body error message shown on a candidate row's {@code Message Body} cell
     * (Issue #1492, Option 2), or an empty string when none. When the row's chosen {@code (verb, Message
     * Body)} would duplicate a body already on the document (or another selected row in the same batch),
     * the dialog renders the {@code mat-error} {@code "This endpoint already has a <Request|Response> body."}.
     *
     * @param tableRecord the candidate row
     * @return the inline error message, or an empty string
     */
    String getRowMessageBodyError(WebElement tableRecord);

    /**
     * Return whether the dialog is currently warning that a selected candidate would add a duplicate body
     * to an endpoint that already has one (Issue #1492, Option 2). True when any candidate row shows the
     * {@code "This endpoint already has a ... body."} {@code mat-error} (which also disables {@code Add}).
     *
     * @return {@code true} when the duplicate-endpoint warning is displayed
     */
    boolean isDuplicateEndpointWarningDisplayed();

    WebElement getAddButton(boolean enabled);

    void hitAddButton();

    WebElement getCloseButton();

    void close();
}
