package org.oagi.score.e2e.impl.page.bie;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.bie.IncludeMetaHeaderProfileBIEDialog;
import org.openqa.selenium.*;

import java.io.File;

import static org.oagi.score.e2e.impl.PageHelper.*;
import static org.openqa.selenium.OutputType.FILE;

public class IncludeMetaHeaderProfileBIEDialogImpl implements IncludeMetaHeaderProfileBIEDialog {

    private static final By STATE_SELECT_FIELD_LOCATOR =
            By.xpath("//div[contains(@class, \"mat-mdc-dialog-content\")]//*[contains(text(), \"State\")]//ancestor::mat-form-field[1]//mat-select");

    private static final By OWNER_SELECT_FIELD_LOCATOR =
            By.xpath("//div[contains(@class, \"mat-mdc-dialog-content\")]//mat-label[contains(text(), \"Owner\")]//ancestor::div[1]/mat-select[1]");

    private static final By UPDATER_SELECT_FIELD_LOCATOR =
            By.xpath("//div[contains(@class, \"mat-mdc-dialog-content\")]//*[contains(text(), \"Updater\")]//ancestor::div[1]/mat-select[1]");

    private static final By BUSINESS_CONTEXT_FIELD_LOCATOR =
            By.xpath("//div[contains(@class, \"mat-mdc-dialog-content\")]//input[contains(@placeholder, \"Business Context\")]");

    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//div[contains(@class, \"mat-mdc-dialog-content\")]//input[contains(@placeholder, \"Updated start date\")]");

    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//div[contains(@class, \"mat-mdc-dialog-content\")]//input[contains(@placeholder, \"Updated end date\")]");
    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//div[contains(@class, \"mat-mdc-dialog-content\")]//*[contains(text(), \"Search\")]//ancestor::button[1]");
    private static final By SELECT_BUTTON_LOCATOR =
            By.id("btn-meta-header-dialog-select");
    private final BasePageImpl parent;

    public IncludeMetaHeaderProfileBIEDialogImpl(BasePageImpl parent) {
        this.parent = parent;
    }

    private WebDriver getDriver() {
        return this.parent.getDriver();
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
    public WebElement getBusinessContextField() {
        return visibilityOfElementLocated(getDriver(), BUSINESS_CONTEXT_FIELD_LOCATOR);
    }

    @Override
    public void setBusinessContext(String den) {
        sendKeys(getBusinessContextField(), den);
    }

    @Override
    public WebElement getSearchButton() {
        return elementToBeClickable(getDriver(), SEARCH_BUTTON_LOCATOR);
    }

    @Override
    public void hitSearchButton() {
        click(getSearchButton());
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
    public void selectMetaHeaderProfile(TopLevelASBIEPObject metaHeaderASBIEP, BusinessContextObject context) {
        retry(() -> {
            WebElement tr = getTableRecordByValue(metaHeaderASBIEP.getDen());
            WebElement td = getColumnByName(tr, "select");
            WebElement ele = td.findElement(By.xpath("mat-checkbox"));
            if (!isChecked(ele)) {
                click(getDriver(), ele);
            }
        });
        click(getSelectButton());
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public WebElement getSelectButton() {
        return elementToBeClickable(getDriver(), SELECT_BUTTON_LOCATOR);
    }
}
