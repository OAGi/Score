package org.oagi.score.e2e.impl.page.module;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.module.CreateModuleFileDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.elementToBeClickable;

public class CreateModuleFileDialogImpl implements CreateModuleFileDialog {
    private static final By MODULE_FILE_NAME_FIELD_LOCATOR =
            By.xpath("//mat-panel-title[contains(text(), \"Create new module file\")]//ancestor::mat-expansion-panel" +
                    "//mat-label[contains(text(), \"Name\")]//ancestor::mat-form-field//input");
    private static final By MODULE_FILE_VERSION_FIELD_LOCATOR =
            By.xpath("//mat-panel-title[contains(text(), \"Create new module file\")]//ancestor::mat-expansion-panel" +
                    "//mat-label[contains(text(), \"Version\")]//ancestor::mat-form-field//input");
    private static final By NAMESPACE_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-panel-title[contains(text(), \"Create new module file\")]//ancestor::mat-expansion-panel" +
                    "//*[text()= \"Namespace\"]//ancestor::mat-form-field[1]//mat-select/div/div[1]");
    private static final By NAMESPACE_FIELD_LOCATOR =
            By.xpath("//mat-panel-title[contains(text(), \"Create new module file\")]//ancestor::mat-expansion-panel" +
                    "//*[text()= \"Namespace\"]//ancestor::mat-form-field[1]//mat-select");
    private static final By CREATE_MODULE_FILE_BUTTON_LOCATOR =
            By.xpath("//mat-panel-title[contains(text(), \"Create new module file\")]//ancestor::mat-expansion-panel" +
                    "//span[contains(text(), \"Create\")]//ancestor::button[1]");

    private final BasePageImpl parent;

    public CreateModuleFileDialogImpl(BasePageImpl parent) {
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
    public WebElement getNamespaceField() {
        return visibilityOfElementLocated(getDriver(), NAMESPACE_FIELD_LOCATOR);
    }

    @Override
    public WebElement getModuleFileVersionNumberField() {
        return visibilityOfElementLocated(getDriver(), MODULE_FILE_VERSION_FIELD_LOCATOR);
    }

    @Override
    public void createModuleFile() {
        click(getCreateModuleFileButton());
        waitFor(ofMillis(500L));
        assert "Created".equals(getSnackBarMessage(getDriver()));
    }

    @Override
    public WebElement getCreateModuleFileButton() {
        return elementToBeClickable(getDriver(), CREATE_MODULE_FILE_BUTTON_LOCATOR);
    }

}
