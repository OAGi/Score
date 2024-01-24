package org.oagi.score.e2e.impl.page.module;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.module.CreateModuleDirectoryDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class CreateModuleDirectoryDialogImpl implements CreateModuleDirectoryDialog {
    private static final By MODULE_DIRECTORY_NAME_FIELD_LOCATOR =
            By.xpath("//mat-panel-title[contains(text(), \"Create new module directory\")]//ancestor::mat-expansion-panel" +
                    "//mat-label[contains(text(), \"Name\")]//ancestor::mat-form-field//input");
    private static final By CREATE_MODULE_DIRECTORY_BUTTON_LOCATOR =
            By.xpath("//mat-panel-title[contains(text(), \"Create new module directory\")]//ancestor::mat-expansion-panel" +
                    "//span[contains(text(), \"Create\")]//ancestor::button[1]");

    private final BasePageImpl parent;

    public CreateModuleDirectoryDialogImpl(BasePageImpl parent) {
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
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-dialog-container//div[contains(@class, \"mat-mdc-dialog-title\")]"));
    }

    @Override
    public void setModuleDirectoryName(String moduleDirectoryName) {
        sendKeys(getModuleDirectoryNameField(), moduleDirectoryName);
    }
    @Override
    public WebElement getModuleDirectoryNameField() {
        return visibilityOfElementLocated(getDriver(), MODULE_DIRECTORY_NAME_FIELD_LOCATOR);
    }

    @Override
    public void createModuleDirectory() {
        click(getCreateModuleDirectoryButton());
        waitFor(ofMillis(500L));
        assert "Created".equals(getSnackBarMessage(getDriver()));
    }
    @Override
    public WebElement getCreateModuleDirectoryButton() {
        return elementToBeClickable(getDriver(), CREATE_MODULE_DIRECTORY_BUTTON_LOCATOR);
    }
}
