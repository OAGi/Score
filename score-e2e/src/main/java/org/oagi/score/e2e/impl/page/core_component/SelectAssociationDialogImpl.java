package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.core_component.SelectAssociationDialog;
import org.openqa.selenium.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class SelectAssociationDialogImpl implements SelectAssociationDialog {

    private static final By TYPE_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Type\")]//ancestor::div[1]/mat-select[1]");

    private static final By STATE_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"State\")]//ancestor::div[1]/mat-select[1]");

    private static final By DEPRECATED_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Deprecated\")]//ancestor::div[1]/mat-select[1]");

    private static final By OWNER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Owner\")]//ancestor::div[1]/mat-select[1]");

    private static final By UPDATER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Updater\")]//ancestor::div[1]/mat-select[1]");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");

    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Updated start date\")]");

    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Updated end date\")]");

    private static final By DEN_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"DEN\")]");

    private static final By DEFINITION_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Definition\")]");

    private static final By MODULE_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Module\")]");

    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Search\")]//ancestor::button[1]");

    private static final By INSERT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Insert\")]//ancestor::button[1]");

    private static final By APPEND_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Append\")]//ancestor::button[1]");

    private static final By CANCEL_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Cancel\")]//ancestor::button[1]");

    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");

    private static final By ASSOCIATION_TYPE_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Type\")]//ancestor::div[1]/mat-select[1]");

    private final BasePageImpl parent;

    private final String contextMenuName;

    public SelectAssociationDialogImpl(BasePageImpl parent, String contextMenuName) {
        this.parent = parent;
        this.contextMenuName = contextMenuName;
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
    public WebElement getTypeSelectField() {
        return elementToBeClickable(getDriver(), TYPE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public WebElement getStateSelectField() {
        return elementToBeClickable(getDriver(), STATE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setState(String state) {
        click(getStateSelectField());
        WebElement optionField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[contains(text(), \"" + state + "\")]"));
        click(optionField);
        escape(getDriver());
    }

    @Override
    public WebElement getDeprecatedSelectField() {
        return elementToBeClickable(getDriver(), DEPRECATED_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setDeprecated(boolean deprecated) {
        click(getDeprecatedSelectField());
        WebElement optionField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[contains(text(), \"" + (deprecated ? "True" : "False") + "\")]"));
        click(optionField);
        escape(getDriver());
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
    public WebElement getAssociationTypeSelectField() {
        return visibilityOfElementLocated(getDriver(), ASSOCIATION_TYPE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setAssociationType(String type) {
        click(getAssociationTypeSelectField());
        try {
            sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), type);
            WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//mat-option//span[contains(text(), \"" + type + "\")]"));
            click(searchedSelectField);
            escape(getDriver());
        } catch (TimeoutException e) {
            throw new NoSuchElementException("Cannot locate an association type " + type, e);
        }
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
    public String getDENFieldLabel() {
        return getDENField().getAttribute("data-placeholder");
    }

    @Override
    public String getModuleFieldLabel() {
        return getModuleField().getAttribute("data-placeholder");
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
        return visibilityOfElementLocated(getDriver(), By.xpath("//tbody/tr[" + idx + "]"));
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
    public void setItemsPerPage(int items) {
        WebElement itemsPerPageField = elementToBeClickable(getDriver(),
                By.xpath("//div[.=\" Items per page: \"]/following::div[5]"));
        click(itemsPerPageField);
        waitFor(ofMillis(500L));
        WebElement itemField = elementToBeClickable(getDriver(),
                By.xpath("//span[contains(text(), \"" + items + "\")]//ancestor::mat-option//div[1]//preceding-sibling::span"));
        click(itemField);
        waitFor(ofMillis(500L));
    }

    @Override
    public WebElement getInsertButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), INSERT_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), INSERT_BUTTON_LOCATOR);
        }
    }

    @Override
    public WebElement getAppendButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), APPEND_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), APPEND_BUTTON_LOCATOR);
        }
    }

    private WebElement getSelectButton() {
        if (this.contextMenuName.startsWith("Append")) {
            return getAppendButton(true);
        } else {
            return getInsertButton(true);
        }
    }

    @Override
    public void selectAssociation(String den) {
        setDEN(den);
        hitSearchButton();

        retry(() -> {
            WebElement tr;
            WebElement td;
            try {
                tr = getTableRecordAtIndex(1);
                td = getColumnByName(tr, "den");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate an association using " + den, e);
            }
            click(getColumnByName(tr, "select"));
            click(getSelectButton());

            assert parent.isOpened();
            waitFor(ofMillis(1000L));
        });
    }

    @Override
    public WebElement getCancelButton() {
        return elementToBeClickable(getDriver(), CANCEL_BUTTON_LOCATOR);
    }

    @Override
    public void hitCancelButton() {
        click(getCancelButton());
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public WebElement getUpdateButton() {
        return elementToBeClickable(getDriver(), UPDATE_BUTTON_LOCATOR);
    }

    @Override
    public void hitUpdateButton() {
        click(getUpdateButton());
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public void close() {
        click(getCancelButton());
    }

}
