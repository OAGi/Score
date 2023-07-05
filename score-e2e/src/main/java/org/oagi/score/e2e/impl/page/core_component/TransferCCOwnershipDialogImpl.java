package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.page.core_component.TransferCCOwnershipDialog;
import org.openqa.selenium.*;

import java.time.Duration;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class TransferCCOwnershipDialogImpl implements TransferCCOwnershipDialog {

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

    private ViewEditCoreComponentPageImpl parent;

    public TransferCCOwnershipDialogImpl(ViewEditCoreComponentPageImpl parent) {
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
        return visibilityOfElementLocated(getDriver(), By.className("mat-card-title"));
    }

    @Override
    public WebElement getLoginIDField() {
        return visibilityOfElementLocated(getDriver(), LOGIN_ID_FIELD_LOCATOR);
    }

    @Override
    public void setLoginID(String loginID) {
        sendKeys(getLoginIDField(), loginID);
    }

    @Override
    public WebElement getNameField() {
        return visibilityOfElementLocated(getDriver(), NAME_FIELD_LOCATOR);
    }

    @Override
    public void setName(String name) {
        sendKeys(getNameField(), name);
    }

    @Override
    public WebElement getOrganizationField() {
        return visibilityOfElementLocated(getDriver(), ORGANIZATION_FIELD_LOCATOR);
    }

    @Override
    public void setOrganization(String organization) {
        sendKeys(getOrganizationField(), organization);
    }

    @Override
    public WebElement getSearchButton() {
        return elementToBeClickable(getDriver(), SEARCH_BUTTON_LOCATOR);
    }

    @Override
    public void hitSearchButton() {
        retry(() -> click(getSearchButton()));
    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//score-transfer-ownership-dialog//tbody/tr[" + idx + "]"));
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//score-transfer-ownership-dialog//td//span[contains(text(), \"" + value + "\")]/ancestor::tr"));
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    @Override
    public WebElement getSelectCheckboxAtIndex(int idx) {
        WebElement tr = getTableRecordAtIndex(idx);
        WebElement td = getColumnByName(tr, "select");
        return td.findElement(By.xpath("mat-checkbox/label/span[1]"));
    }

    @Override
    public void setItemsPerPage(int items) {
        WebElement itemsPerPageField = elementToBeClickable(getDriver(),
                By.xpath("//score-transfer-ownership-dialog//div[.=\" Items per page: \"]/following::div[5]"));
        click(itemsPerPageField);
        waitFor(Duration.ofMillis(500L));
        WebElement itemField = elementToBeClickable(getDriver(),
                By.xpath("//score-transfer-ownership-dialog//span[contains(text(), \"" + items + "\")]//ancestor::mat-option//div[1]//preceding-sibling::span"));
        click(itemField);
        waitFor(Duration.ofMillis(500L));
    }

    @Override
    public WebElement getTransferButton() {
        return elementToBeClickable(getDriver(), TRANSFER_BUTTON_LOCATOR);
    }

    @Override
    public void transfer(String loginID) {
        setLoginID(loginID);
        hitSearchButton();

        retry(() -> {
            WebElement tr;
            WebElement td;
            try {
                tr = getTableRecordAtIndex(1);
                td = getColumnByName(tr, "loginId");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate an account using " + loginID, e);
            }
            if (!loginID.equals(getText(td))) {
                throw new NoSuchElementException("Cannot locate an account using " + loginID);
            }
            td = getColumnByName(tr, "select");
            click(td.findElement(By.xpath("mat-checkbox/label/span[1]")));

            click(getTransferButton());
            invisibilityOfLoadingContainerElement(getDriver());
            assert "Transferred".equals(getSnackBarMessage(getDriver()));
        });
    }

    @Override
    public WebElement getCancelButton() {
        return elementToBeClickable(getDriver(), CANCEL_BUTTON_LOCATOR);
    }

    @Override
    public void close() {
        click(getCancelButton());
    }
}