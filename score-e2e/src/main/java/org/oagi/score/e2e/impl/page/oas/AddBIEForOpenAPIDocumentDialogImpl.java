package org.oagi.score.e2e.impl.page.oas;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.SearchBarPageImpl;
import org.oagi.score.e2e.page.oas.AddBIEForOpenAPIDocumentDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.elementToBeClickable;
import static org.oagi.score.e2e.impl.PageHelper.escape;
import static org.oagi.score.e2e.impl.PageHelper.invisibilityOfElementLocated;
import static org.oagi.score.e2e.impl.PageHelper.invisibilityOfLoadingContainerElement;
import static org.oagi.score.e2e.impl.PageHelper.shortWait;
import static org.oagi.score.e2e.impl.PageHelper.sendKeys;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

public class AddBIEForOpenAPIDocumentDialogImpl implements AddBIEForOpenAPIDocumentDialog {

    private static final String BASE_XPATH = "//mat-dialog-container";
    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR = By.xpath("//input[@aria-label=\"dropdown search\"]");
    private static final By BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath(BASE_XPATH + "//*[contains(text(), \"Branch\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By STATE_SELECT_FIELD_LOCATOR =
            By.xpath(BASE_XPATH + "//*[contains(text(), \"State\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By BUSINESS_CONTEXT_FIELD_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-label[contains(text(), \"Business Context\")]//ancestor::mat-form-field//input");
    private static final By VERSION_FIELD_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-label[contains(text(), \"Version\")]//ancestor::mat-form-field//input");
    private static final By REMARK_FIELD_LOCATOR =
            By.xpath(BASE_XPATH + "//mat-label[contains(text(), \"Remark\")]//ancestor::mat-form-field//input");
    private static final By OWNER_SELECT_FIELD_LOCATOR =
            By.xpath(BASE_XPATH + "//*[contains(text(), \"Owner\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By UPDATER_SELECT_FIELD_LOCATOR =
            By.xpath(BASE_XPATH + "//*[contains(text(), \"Updater\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath(BASE_XPATH + "//input[contains(@placeholder, \"Updated start date\")]");
    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath(BASE_XPATH + "//input[contains(@placeholder, \"Updated end date\")]");
    private static final By ADD_BUTTON_LOCATOR =
            By.xpath(BASE_XPATH + "//button[.//span[normalize-space(.) = \"Add\"]]");
    private static final By CLOSE_BUTTON_LOCATOR =
            By.xpath(BASE_XPATH + "//button[.//span[normalize-space(.) = \"Close\"]]");
    private static final By OVERLAY_BACKDROP_LOCATOR =
            By.cssSelector("div.cdk-overlay-backdrop.cdk-overlay-backdrop-showing");

    private final BasePageImpl parent;
    private final SearchBarPageImpl searchBarPage;

    public AddBIEForOpenAPIDocumentDialogImpl(BasePageImpl parent) {
        this.parent = parent;
        this.searchBarPage = new SearchBarPageImpl(getDriver(), BASE_XPATH);
    }

    private WebDriver getDriver() {
        return this.parent.getDriver();
    }

    private void closeOverlayIfOpen() {
        try {
            invisibilityOfElementLocated(shortWait(getDriver()), OVERLAY_BACKDROP_LOCATOR);
        } catch (TimeoutException e) {
            escape(getDriver());
            invisibilityOfElementLocated(getDriver(), OVERLAY_BACKDROP_LOCATOR);
        }
    }

    @Override
    public boolean isOpened() {
        try {
            getTitle();
        } catch (TimeoutException e) {
            return false;
        }
        return true;
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-dialog-container//div[contains(@class, \"mat-mdc-dialog-title\")]"));
    }

    @Override
    public WebElement getSearchButton() {
        return searchBarPage.getSearchButton();
    }

    @Override
    public WebElement getInputFieldInSearchBar() {
        return searchBarPage.getInputFieldInSearchBar();
    }

    @Override
    public WebElement getShowAdvancedSearchButton() {
        return searchBarPage.getShowAdvancedSearchButton();
    }

    @Override
    public void showAdvancedSearchPanel() {
        searchBarPage.showAdvancedSearchPanel();
    }

    @Override
    public WebElement getHideAdvancedSearchButton() {
        return searchBarPage.getHideAdvancedSearchButton();
    }

    @Override
    public void hideAdvancedSearchPanel() {
        searchBarPage.hideAdvancedSearchPanel();
    }

    @Override
    public WebElement getBranchSelectField() {
        return visibilityOfElementLocated(getDriver(), BRANCH_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setBranch(String branch) {
        click(getBranchSelectField());
        sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), branch);
        click(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[normalize-space(.) = \"" + branch + "\"]")));
        closeOverlayIfOpen();
    }

    @Override
    public WebElement getStateSelectField() {
        return visibilityOfElementLocated(getDriver(), STATE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setState(String state) {
        click(getStateSelectField());
        click(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[normalize-space(.) = \"" + state + "\"]")));
        closeOverlayIfOpen();
    }

    @Override
    public WebElement getBusinessContextField() {
        return visibilityOfElementLocated(getDriver(), BUSINESS_CONTEXT_FIELD_LOCATOR);
    }

    @Override
    public void setBusinessContext(String businessContext) {
        sendKeys(getBusinessContextField(), businessContext);
    }

    @Override
    public WebElement getVersionField() {
        return visibilityOfElementLocated(getDriver(), VERSION_FIELD_LOCATOR);
    }

    @Override
    public void setVersion(String version) {
        sendKeys(getVersionField(), version);
    }

    @Override
    public WebElement getRemarkField() {
        return visibilityOfElementLocated(getDriver(), REMARK_FIELD_LOCATOR);
    }

    @Override
    public void setRemark(String remark) {
        sendKeys(getRemarkField(), remark);
    }

    @Override
    public WebElement getOwnerSelectField() {
        return visibilityOfElementLocated(getDriver(), OWNER_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setOwner(String owner) {
        click(getOwnerSelectField());
        sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), owner);
        click(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[normalize-space(.) = \"" + owner + "\"]")));
        closeOverlayIfOpen();
    }

    @Override
    public WebElement getUpdaterSelectField() {
        return visibilityOfElementLocated(getDriver(), UPDATER_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setUpdater(String updater) {
        click(getUpdaterSelectField());
        sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), updater);
        click(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[normalize-space(.) = \"" + updater + "\"]")));
        closeOverlayIfOpen();
    }

    @Override
    public WebElement getUpdatedStartDateField() {
        return visibilityOfElementLocated(getDriver(), UPDATED_START_DATE_FIELD_LOCATOR);
    }

    @Override
    public void setUpdatedStartDate(LocalDateTime updatedStartDate) {
        sendKeys(getUpdatedStartDateField(), DateTimeFormatter.ofPattern("MM/dd/yyyy").format(updatedStartDate));
    }

    @Override
    public WebElement getUpdatedEndDateField() {
        return visibilityOfElementLocated(getDriver(), UPDATED_END_DATE_FIELD_LOCATOR);
    }

    @Override
    public void setUpdatedEndDate(LocalDateTime updatedEndDate) {
        sendKeys(getUpdatedEndDateField(), DateTimeFormatter.ofPattern("MM/dd/yyyy").format(updatedEndDate));
    }

    @Override
    public void hitSearchButton() {
        click(getSearchButton());
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return visibilityOfElementLocated(getDriver(), By.xpath(BASE_XPATH + "//tbody/tr[" + idx + "]"));
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        return visibilityOfElementLocated(getDriver(),
                By.xpath(BASE_XPATH + "//td//*[contains(normalize-space(.), " +
                        org.oagi.score.e2e.impl.PageHelper.xpathLiteral(value) + ")]/ancestor::tr"));
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    @Override
    public void toggleSelect(WebElement tableRecord) {
        WebElement selectCell = getColumnByName(tableRecord, "select");
        click(getDriver(), selectCell.findElement(By.tagName("mat-checkbox")));
    }

    @Override
    public void setVerb(WebElement tableRecord, String verb) {
        WebElement verbCell = getColumnByName(tableRecord, "verb");
        click(getDriver(), verbCell.findElement(By.tagName("mat-select")));
        WebElement option = elementToBeClickable(getDriver(),
                By.xpath("//mat-option[.//span[normalize-space(.) = \"" + verb + "\"]]"));
        click(getDriver(), option);
        waitFor(ofMillis(300L));
    }

    @Override
    public void setMessageBody(WebElement tableRecord, String messageBody) {
        WebElement messageBodyCell = getColumnByName(tableRecord, "messageBody");
        click(getDriver(), messageBodyCell.findElement(By.tagName("mat-select")));
        WebElement option = elementToBeClickable(getDriver(),
                By.xpath("//mat-option[.//span[normalize-space(.) = \"" + messageBody + "\"]]"));
        click(getDriver(), option);
        waitFor(ofMillis(300L));
    }

    @Override
    public boolean isMessageBodyOptionDisabled(WebElement tableRecord, String messageBody) {
        WebElement messageBodyCell = getColumnByName(tableRecord, "messageBody");
        click(getDriver(), messageBodyCell.findElement(By.tagName("mat-select")));
        WebElement option = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option[.//span[normalize-space(.) = \"" + messageBody + "\"]]"));
        boolean disabled = "true".equals(option.getAttribute("aria-disabled")) ||
                option.getAttribute("class").contains("mdc-list-item--disabled");
        escape(getDriver());
        return disabled;
    }

    @Override
    public WebElement getAddButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), ADD_BUTTON_LOCATOR);
        }
        return visibilityOfElementLocated(getDriver(), ADD_BUTTON_LOCATOR);
    }

    @Override
    public void hitAddButton() {
        click(getAddButton(true));
        waitFor(ofMillis(500L));
    }

    @Override
    public WebElement getCloseButton() {
        return elementToBeClickable(getDriver(), CLOSE_BUTTON_LOCATOR);
    }

    @Override
    public void close() {
        click(getCloseButton());
        waitFor(ofMillis(500L));
    }
}
