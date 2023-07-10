package org.oagi.score.e2e.impl.page.module;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.module.EditModuleDirectoryDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class EditModuleDirectoryDialogImpl implements EditModuleDirectoryDialog {
    private static final By MODULE_DIRECTORY_NAME_FIELD_LOCATOR =
            By.xpath("//mat-dialog-content//mat-label[contains(text(), \"Name\")]//ancestor::mat-form-field//input");

    private static final By UPDATE_MODULE_DIRECTORY_BUTTON_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]");
    private static final By DISCARD_MODULE_DIRECTORY_BUTTON_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button[1]");
    private static final By CONTINUE_TO_DISCARD_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Discard anyway\")]//ancestor::button/span");
    private static final By DISCARD_WARNING_DIALOG_MESSAGE_LOCATOR =
            By.xpath("//mat-dialog-container//p");

    private final BasePageImpl parent;

    public EditModuleDirectoryDialogImpl(BasePageImpl parent) {
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
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-dialog-container//mat-card-title"));
    }

    @Override
    public void setModuleDirectoryName(String directoryName) {
        sendKeys(getModuleDirectoryNameField(), directoryName);
    }

    @Override
    public WebElement getModuleDirectoryNameField() {
        return visibilityOfElementLocated(getDriver(), MODULE_DIRECTORY_NAME_FIELD_LOCATOR);
    }

    @Override
    public void updateModuleDirectory() {
        click(getUpdateModuleDirectoryButton());
    }
    @Override
    public WebElement getUpdateModuleDirectoryButton() {
        return elementToBeClickable(getDriver(), UPDATE_MODULE_DIRECTORY_BUTTON_LOCATOR);
    }

    @Override
    public void discardDirectory() {
        click(getDiscardModuleDirectoryButton());
        click(getContinueToDiscardDirectoryButton());
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }
    @Override
    public WebElement getDiscardModuleDirectoryButton() {
        return elementToBeClickable(getDriver(), DISCARD_MODULE_DIRECTORY_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getContinueToDiscardDirectoryButton() {
        return elementToBeClickable(getDriver(), CONTINUE_TO_DISCARD_BUTTON_IN_DIALOG_LOCATOR);
    }

    @Override
    public String getDiscardDirectoryMessage() {
        return visibilityOfElementLocated(getDriver(), DISCARD_WARNING_DIALOG_MESSAGE_LOCATOR).getText();
    }
}
