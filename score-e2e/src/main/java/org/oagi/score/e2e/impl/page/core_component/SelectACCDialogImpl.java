package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.page.core_component.SelectACCDialog;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;

public class SelectACCDialogImpl implements SelectACCDialog {

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
