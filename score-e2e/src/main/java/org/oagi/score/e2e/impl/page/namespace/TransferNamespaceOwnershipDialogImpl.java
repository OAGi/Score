package org.oagi.score.e2e.impl.page.namespace;

import org.oagi.score.e2e.page.namespace.TransferNamespaceOwershipDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class TransferNamespaceOwnershipDialogImpl implements TransferNamespaceOwershipDialog {

    private static final By LOGIN_ID_FIELD_LOCATOR
            = By.xpath("//score-transfer-ownership-dialog//span[contains(text(), \"Login ID\")]//ancestor::div[1]/input");

    private static final By NAME_FIELD_LOCATOR
            = By.xpath("//score-transfer-ownership-dialog//span[contains(text(), \"Name\")]//ancestor::div[1]/input");

    private static final By ORGANIZATION_FIELD_LOCATOR
            = By.xpath("//score-transfer-ownership-dialog//span[contains(text(), \"Organization\")]//ancestor::div[1]/input");

    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//score-transfer-ownership-dialog//span[contains(text(), \"Search\")]//ancestor::button[1]");

    private static final By TRANSFER_BUTTON_LOCATOR =
            By.xpath("//score-transfer-ownership-dialog//span[contains(text(), \"Transfer\")]//ancestor::button[1]");

    private static final By CANCEL_BUTTON_LOCATOR =
            By.xpath("//score-transfer-ownership-dialog//span[contains(text(), \"Cancel\")]//ancestor::button[1]");

    private ViewEditNamespacePageImpl parent;

    public TransferNamespaceOwershipDialogImpl(ViewEditNamespacePageImpl parent) {
        this.parent = parent;
    }

    private WebDriver getDriver() {
        return this.parent.getDriver();
    }

    @Override
    public boolean isOpened() {
        return false;
    }

    @Override
    public WebElement getTitle() {
        return null;
    }

    @Override
    public WebElement getLoginIDField() {
        return null;
    }

    @Override
    public void setLoginID(String loginID) {

    }

    @Override
    public WebElement getNameField() {
        return null;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public WebElement getOrganizationField() {
        return null;
    }

    @Override
    public void setOrganization(String organization) {

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
    public WebElement getSelectCheckboxAtIndex(int idx) {
        return null;
    }

    @Override
    public void setItemsPerPage(int items) {

    }

    @Override
    public WebElement getTransferButton() {
        return null;
    }

    @Override
    public void transfer(String loginId) {

    }

    @Override
    public WebElement getCancelButton() {
        return null;
    }

    @Override
    public void close() {

    }
}
