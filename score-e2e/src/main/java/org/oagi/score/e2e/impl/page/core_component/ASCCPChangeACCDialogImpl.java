package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.page.core_component.ASCCPChangeACCDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;

public class ASCCPChangeACCDialogImpl implements ASCCPChangeACCDialog {

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
            By.xpath("//input[contains(@data-placeholder, \"Updated start date\")]");

    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@data-placeholder, \"Updated end date\")]");

    private static final By DEN_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"DEN\")]//ancestor::mat-form-field//input");

    private static final By DEFINITION_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Definition\")]//ancestor::mat-form-field//input");

    private static final By MODULE_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Module\")]//ancestor::mat-form-field//input");

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

    @Override
    public boolean isOpened() {
        return false;
    }

    @Override
    public WebElement getTitle() {
        return null;
    }

    @Override
    public WebElement getStateSelectField() {
        return null;
    }

    @Override
    public void setState(String state) {

    }

    @Override
    public WebElement getDeprecatedSelectField() {
        return null;
    }

    @Override
    public void setDeprecated(boolean deprecated) {

    }

    @Override
    public WebElement getOwnerSelectField() {
        return null;
    }

    @Override
    public void setOwner(String owner) {

    }

    @Override
    public WebElement getUpdaterSelectField() {
        return null;
    }

    @Override
    public void setUpdater(String updater) {

    }

    @Override
    public WebElement getUpdatedStartDateField() {
        return null;
    }

    @Override
    public void setUpdatedStartDate(LocalDateTime updatedStartDate) {

    }

    @Override
    public WebElement getUpdatedEndDateField() {
        return null;
    }

    @Override
    public void setUpdatedEndDate(LocalDateTime updatedEndDate) {

    }

    @Override
    public WebElement getDENField() {
        return null;
    }

    @Override
    public void setDEN(String den) {

    }

    @Override
    public String getDENFieldLabel() {
        return null;
    }

    @Override
    public WebElement getDefinitionField() {
        return null;
    }

    @Override
    public void setDefinition(String definition) {

    }

    @Override
    public WebElement getModuleField() {
        return null;
    }

    @Override
    public void setModule(String module) {

    }

    @Override
    public String getModuleFieldLabel() {
        return null;
    }

    @Override
    public WebElement getTagSelectField() {
        return null;
    }

    @Override
    public void setTag(String tag) {

    }

    @Override
    public WebElement getSearchButton() {
        return null;
    }

    @Override
    public void hitSearchButton() {

    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return null;
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        return null;
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return null;
    }

    @Override
    public void setItemsPerPage(int items) {

    }

    @Override
    public WebElement getCancelButton() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public void hitCancelButton() {

    }

    @Override
    public WebElement getUpdateButton() {
        return null;
    }

    @Override
    public void hitUpdateButton() {

    }
}
