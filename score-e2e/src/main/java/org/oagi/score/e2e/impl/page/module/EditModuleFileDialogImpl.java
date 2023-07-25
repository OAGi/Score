package org.oagi.score.e2e.impl.page.module;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.module.EditModuleFileDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class EditModuleFileDialogImpl implements EditModuleFileDialog {
    private static final By UPDATE_MODULE_FILE_BUTTON_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]");
    private static final By DISCARD_MODULE_FILE_BUTTON_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button[1]");
    private static final By MODULE_FILE_NAME_FIELD_LOCATOR =
            By.xpath("//mat-dialog-content//mat-label[contains(text(), \"Name\")]//ancestor::mat-form-field//input");
    private static final By MODULE_FILE_VERSION_FIELD_LOCATOR =
            By.xpath("//mat-dialog-content//mat-label[contains(text(), \"Version\")]//ancestor::mat-form-field//input");
    private static final By NAMESPACE_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-dialog-content//*[text()= \"Namespace\"]//ancestor::mat-form-field[1]//mat-select/div/div[1]");
    private static final By CONTINUE_TO_DISCARD_BUTTON_IN_DIALOG_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Discard anyway\")]//ancestor::button/span");
    private static final By DISCARD_WARNING_DIALOG_MESSAGE_LOCATOR =
            By.xpath("//mat-dialog-container//p");

    private final BasePageImpl parent;

    public EditModuleFileDialogImpl(BasePageImpl parent) {
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
    public void updateModuleFile() {
        click(getUpdateModuleFileButton());
        waitFor(ofMillis(500L));
    }

    @Override
    public WebElement getUpdateModuleFileButton() {
        return elementToBeClickable(getDriver(), UPDATE_MODULE_FILE_BUTTON_LOCATOR);
    }
    @Override
    public WebElement getModuleFileNameField() {
        return visibilityOfElementLocated(getDriver(), MODULE_FILE_NAME_FIELD_LOCATOR);
    }

    @Override
    public void setModuleFileName(String moduleFileName) {
        sendKeys(getModuleFileNameField(), moduleFileName);
    }

    @Override
    public void setNamespace(String namespaceURI) {
        retry(() -> {
            click(getNamespaceSelectField());
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//span[contains(text(), \"" + namespaceURI + "\")]//ancestor::mat-option[1]/span"));
            click(optionField);
            waitFor(ofMillis(500L));
        });
    }

    @Override
    public void setModuleFileVersionNumber(String moduleFileVersion) {
        sendKeys(getModuleFileVersionNumberField(), moduleFileVersion);
    }

    @Override
    public WebElement getNamespaceSelectField() {
        return visibilityOfElementLocated(getDriver(), NAMESPACE_SELECT_FIELD_LOCATOR);
    }
    @Override
    public WebElement getModuleFileVersionNumberField() {
        return visibilityOfElementLocated(getDriver(), MODULE_FILE_VERSION_FIELD_LOCATOR);
    }

    @Override
    public void discardFile() {
        click(getDiscardModuleFileButton());
        click(getContinueToDiscardFileButton());
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));
    }
    @Override
    public WebElement getDiscardModuleFileButton() {
        return elementToBeClickable(getDriver(), DISCARD_MODULE_FILE_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getContinueToDiscardFileButton() {
        return elementToBeClickable(getDriver(), CONTINUE_TO_DISCARD_BUTTON_IN_DIALOG_LOCATOR);
    }

    @Override
    public String getDiscardFileMessage() {
        return visibilityOfElementLocated(getDriver(), DISCARD_WARNING_DIALOG_MESSAGE_LOCATOR).getText();
    }
}
