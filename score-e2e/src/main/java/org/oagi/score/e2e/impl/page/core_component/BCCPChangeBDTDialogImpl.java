package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.page.core_component.BCCPChangeBDTDialog;
import org.oagi.score.e2e.page.core_component.BCCPViewEditPage;
import org.openqa.selenium.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class BCCPChangeBDTDialogImpl implements BCCPChangeBDTDialog {

    private static final By STATE_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-dialog-container//*[contains(text(), \"State\")]//ancestor::mat-form-field[1]//mat-select");

    private static final By DEPRECATED_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-dialog-container//*[contains(text(), \"Deprecated\")]//ancestor::mat-form-field[1]//mat-select");

    private static final By COMMONLY_USED_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-dialog-container//*[contains(text(), \"Commonly Used\")]//ancestor::mat-form-field[1]//mat-select");

    private static final By OWNER_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-dialog-container//*[contains(text(), \"Owner\")]//ancestor::div[1]/mat-select[1]");

    private static final By UPDATER_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-dialog-container//*[contains(text(), \"Updater\")]//ancestor::div[1]/mat-select[1]");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//mat-dialog-container//input[@aria-label=\"dropdown search\"]");

    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//mat-dialog-container//input[contains(@placeholder, \"Updated start date\")]");

    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//mat-dialog-container//input[contains(@placeholder, \"Updated end date\")]");

    private static final By DEN_FIELD_LOCATOR =
            By.xpath("//mat-dialog-container//input[contains(@placeholder, \"DEN\")]");

    private static final By DEFINITION_FIELD_LOCATOR =
            By.xpath("//mat-dialog-container//input[contains(@placeholder, \"Definition\")]");

    private static final By MODULE_FIELD_LOCATOR =
            By.xpath("//mat-dialog-container//input[contains(@placeholder, \"Module\")]");

    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Search\")]//ancestor::button[1]");

    private static final By CANCEL_BUTTON_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Cancel\")]//ancestor::button[1]");

    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]");

    private BCCPViewEditPageImpl parent;

    public BCCPChangeBDTDialogImpl(BCCPViewEditPageImpl parent) {
        this.parent = parent;
    }

    private WebDriver getDriver() {
        return this.parent.getDriver();
    }

    @Override
    public boolean isOpened() {
        WebElement title;
        try {
            title = getTitle();
        } catch (TimeoutException e) {
            return false;
        }
        assert "Select BDT to update BCCP".equals(getText(title.findElement(By.tagName("span"))));
        return true;
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-dialog-container//div[contains(@class, \"mat-mdc-dialog-title\")]"));
    }

    @Override
    public WebElement getStateSelectField() {
        return visibilityOfElementLocated(getDriver(), STATE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setState(String state) {
        click(getStateSelectField());
        WebElement optionField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//mat-option//span[contains(text(), \"" + state + "\")]"));
        click(optionField);
        escape(getDriver());
    }

    @Override
    public WebElement getDeprecatedSelectField() {
        return visibilityOfElementLocated(getDriver(), DEPRECATED_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setDeprecated(boolean deprecated) {
        click(getDeprecatedSelectField());
        WebElement optionField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//mat-option//span[contains(text(), \"" + (deprecated ? "True" : "False") + "\")]"));
        click(optionField);
    }

    @Override
    public WebElement getCommonlyUsedSelectField() {
        return visibilityOfElementLocated(getDriver(), COMMONLY_USED_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setCommonlyUsed(boolean commonlyUsed) {
        click(getCommonlyUsedSelectField());
        WebElement optionField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//mat-option//span[contains(text(), \"" + (commonlyUsed ? "True" : "False") + "\")]"));
        click(optionField);
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
                By.xpath("//mat-dialog-container//mat-option//span[contains(text(), \"" + owner + "\")]"));
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
                By.xpath("//mat-dialog-container//mat-option//span[contains(text(), \"" + updater + "\")]"));
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
    public WebElement getDENField() {
        return visibilityOfElementLocated(getDriver(), DEN_FIELD_LOCATOR);
    }

    @Override
    public void setDEN(String den) {
        sendKeys(getDENField(), den);
    }

    @Override
    public WebElement getDefinitionField() {
        return visibilityOfElementLocated(getDriver(), DEFINITION_FIELD_LOCATOR);
    }

    @Override
    public void setDefinition(String definition) {
        sendKeys(getDefinitionField(), definition);
    }

    @Override
    public WebElement getModuleField() {
        return visibilityOfElementLocated(getDriver(), MODULE_FIELD_LOCATOR);
    }

    @Override
    public void setModule(String module) {
        sendKeys(getModuleField(), module);
    }

    @Override
    public WebElement getSearchButton() {
        return elementToBeClickable(getDriver(), SEARCH_BUTTON_LOCATOR);
    }

    @Override
    public void hitSearchButton() {
        click(getSearchButton());
        waitFor(ofMillis(500L));
    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-dialog-container//tbody/tr[" + idx + "]"));
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-dialog-container//td//span[contains(text(), \"" + value + "\")]/ancestor::tr"));
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    @Override
    public void setItemsPerPage(int items) {
        WebElement itemsPerPageField = elementToBeClickable(getDriver(),
                By.xpath("//mat-dialog-container//div[.=\" Items per page: \"]/following::mat-form-field//mat-select"));
        click(itemsPerPageField);
        waitFor(ofMillis(500L));
        WebElement itemField = elementToBeClickable(getDriver(),
                By.xpath("//mat-dialog-container//span[contains(text(), \"" + items + "\")]//ancestor::mat-option//div[1]//preceding-sibling::span"));
        click(itemField);
        waitFor(ofMillis(500L));
    }

    @Override
    public WebElement getCancelButton() {
        return elementToBeClickable(getDriver(), CANCEL_BUTTON_LOCATOR);
    }

    @Override
    public BCCPViewEditPage cancel() {
        click(getCancelButton());
        return parent;
    }

    @Override
    public WebElement getUpdateButton() {
        return elementToBeClickable(getDriver(), UPDATE_BUTTON_LOCATOR);
    }

    @Override
    public BCCPViewEditPage update(String bdtDen) {
        setDEN(bdtDen);
        hitSearchButton();

        retry(() -> {
            WebElement tr;
            WebElement td;
            try {
                tr = getTableRecordAtIndex(1);
                td = getColumnByName(tr, "den");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a DT using " + bdtDen, e);
            }
            String denColumn = getText(td.findElement(By.tagName("span")));
            if (!denColumn.contains(bdtDen)) {
                throw new NoSuchElementException("Cannot locate a DT using " + bdtDen);
            }
            WebElement select = getColumnByName(tr, "select");
            click(select);
        });

        click(getUpdateButton());
        waitFor(ofMillis(500L));

        invisibilityOfLoadingContainerElement(getDriver());

        assert "Updated".equals(getSnackBarMessage(getDriver()));
        return parent;
    }

}
