package org.oagi.score.e2e.impl.page.module;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.module.CopyModuleFromExistingModuleSetDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class CopyModuleFromExistingModuleSetDialogImpl implements CopyModuleFromExistingModuleSetDialog {
    private static final By MODULE_SET_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-expansion-panel//*[text()= \"Module Set\"]//ancestor::mat-form-field[1]//mat-select/div/div[1]");
    private static final By COPY_BUTTON_LOCATOR =
            By.xpath("//mat-expansion-panel//span[contains(text(), \"Copy\")]//ancestor::button[1][@ng-reflect-disabled=\"false\"]");


    private final BasePageImpl parent;

    public CopyModuleFromExistingModuleSetDialogImpl(BasePageImpl parent) {
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
    public void setModuleSet(String moduleSetName) {
        retry(() -> {
            click(getModuleSetSelectField());
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//span[contains(text(), \"" + moduleSetName + "\")]//ancestor::mat-option[1]/span"));
            click(optionField);
            waitFor(ofMillis(500L));
        });
    }

    @Override
    public WebElement getModuleSetSelectField() {
        return visibilityOfElementLocated(getDriver(), MODULE_SET_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void selectModule(String moduleName) {
        click(getModuleByName(moduleName));
    }

    @Override
    public WebElement getModuleByName(String moduleName) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-dialog-container//*[text() = \"" + moduleName + "\"]//ancestor::div[1]"));
    }

    @Override
    public void copyModule() {
        click(getCopyButton());
    }
    private WebElement getCopyButton() {
        return elementToBeClickable(getDriver(), COPY_BUTTON_LOCATOR);
    }

    @Override
    public void toggleCopyAllSubmodules() {
        click(getCopyAllSubmodulesSelectField().findElement(By.tagName("label")));
    }

    @Override
    public WebElement getCopyAllSubmodulesSelectField() {
        return getCheckboxByName("Copy all submodules");
    }
    private WebElement getCheckboxByName(String name) {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//span[contains(text(), \"" + name + "\")]//ancestor::mat-checkbox"));
    }
}
