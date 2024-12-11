package org.oagi.score.e2e.impl.page.bie;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.SearchBarPageImpl;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.bie.SelectProfileBIEToReuseDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class SelectProfileBIEToReuseDialogImpl extends SearchBarPageImpl implements SelectProfileBIEToReuseDialog {

    private static final By OWNER_SELECT_FIELD_LOCATOR =
            By.xpath("//score-reuse-bie-dialog//*[contains(text(), \"Owner\")]//ancestor::div[1]/mat-select[1]");

    private static final By UPDATER_SELECT_FIELD_LOCATOR =
            By.xpath("//score-reuse-bie-dialog//*[contains(text(), \"Updater\")]//ancestor::div[1]/mat-select[1]");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//score-reuse-bie-dialog//input[@aria-label=\"dropdown search\"]");

    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//score-reuse-bie-dialog//input[contains(@placeholder, \"Updated start date\")]");

    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//score-reuse-bie-dialog//input[contains(@placeholder, \"Updated end date\")]");

    private static final By BUSINESS_CONTEXT_FIELD_LOCATOR =
            By.xpath("//score-reuse-bie-dialog//input[contains(@placeholder, \"Business Context\")]");

    private static final By VERSION_FIELD_LOCATOR =
            By.xpath("//score-reuse-bie-dialog//input[contains(@placeholder, \"Version\")]");

    private static final By REMARK_FIELD_LOCATOR =
            By.xpath("//score-reuse-bie-dialog//input[contains(@placeholder, \"Remark\")]");

    private static final By SELECT_BUTTON_LOCATOR =
            By.xpath("//score-reuse-bie-dialog//span[contains(text(), \"Select\")]//ancestor::button[1]");

    private final BasePageImpl parent;

    private final String contextMenuName;

    public SelectProfileBIEToReuseDialogImpl(BasePageImpl parent, String contextMenuName) {
        super(parent.getDriver(), "//mat-dialog-container");
        this.parent = parent;
        this.contextMenuName = contextMenuName;
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
    public WebElement getOwnerSelectField() {
        return visibilityOfElementLocated(getDriver(), OWNER_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setOwner(String owner) {
        click(getOwnerSelectField());
        sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), owner);
        WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[contains(text(), \"" + owner + "\")]"));
        click(searchedSelectField);
        escape(getDriver());
    }

    @Override
    public WebElement getUpdaterSelectField() {
        return visibilityOfElementLocated(getDriver(), UPDATER_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setUpdater(String updater) {
        click(getUpdaterSelectField());
        sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), updater);
        WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[contains(text(), \"" + updater + "\")]"));
        click(searchedSelectField);
        escape(getDriver());
    }

    @Override
    public WebElement getUpdatedStartDateField() {
        return visibilityOfElementLocated(getDriver(), UPDATED_START_DATE_FIELD_LOCATOR);
    }

    @Override
    public void setUpdatedStartDate(LocalDateTime updatedStartDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        sendKeys(getUpdatedStartDateField(), formatter.format(updatedStartDate));
    }

    @Override
    public WebElement getUpdatedEndDateField() {
        return visibilityOfElementLocated(getDriver(), UPDATED_END_DATE_FIELD_LOCATOR);
    }

    @Override
    public void setUpdatedEndDate(LocalDateTime updatedEndDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        sendKeys(getUpdatedEndDateField(), formatter.format(updatedEndDate));
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
    public void hitSearchButton() {
        click(getSearchButton());
        waitFor(ofMillis(500L));
    }

    @Override
    public void selectBIEToReuse(TopLevelASBIEPObject bie) {
        retry(() -> {
            showAdvancedSearchPanel();
            setVersion(bie.getVersion());
            hitSearchButton();

            WebElement tr = getTableRecordByValue(bie.getPropertyTerm());
            WebElement td = getColumnByName(tr, "select");
            click(td.findElement(By.xpath("mat-checkbox")));
        });
        click(getSelectButton());
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public void selectBIEToReuse(String topLevelBIEPropertyName) {
        retry(() -> {
            WebElement tr = getTableRecordByValue(topLevelBIEPropertyName);
            WebElement td = getColumnByName(tr, "select");
            click(td.findElement(By.xpath("mat-checkbox")));
        });
        click(getSelectButton());
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//td//span[contains(text(), \"" + value + "\")]/ancestor::tr"));
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    @Override
    public WebElement getSelectButton() {
        return elementToBeClickable(getDriver(), SELECT_BUTTON_LOCATOR);
    }

}
