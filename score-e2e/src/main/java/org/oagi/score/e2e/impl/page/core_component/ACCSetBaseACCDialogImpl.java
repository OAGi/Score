package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.page.core_component.ACCSetBaseACCDialog;
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.ASCCPViewEditPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;

import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;

public class ACCSetBaseACCDialogImpl implements ACCSetBaseACCDialog {

    private static final By COMPONENT_TYPE_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Component Type\")]//ancestor::div[1]/mat-select[1]");

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

    private static final By CANCEL_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Cancel\")]//ancestor::button[1]");

    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//mat-dialog-container//button//*[contains(text(),\"Update\")]");

    private ACCViewEditPageImpl parent;

    public ACCSetBaseACCDialogImpl(ACCViewEditPageImpl parent){this.parent = parent;}

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
        assert "Select ACC to set a base ACC".equals(getText(title));
        return true;
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-dialog-container//span[contains(@class, \"title\")]"));
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
    public WebElement getComponentTypeSelectField() {
        return null;
    }

    @Override
    public void setComponentType(String componentType) {

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
    public ASCCPViewEditPage hitCancelButton() {
        return null;
    }

    @Override
    public WebElement getApplyButton() {
        return null;
    }

    @Override
    public ACCViewEditPage hitApplyButton(String accDen) {
        return null;
    }
}
