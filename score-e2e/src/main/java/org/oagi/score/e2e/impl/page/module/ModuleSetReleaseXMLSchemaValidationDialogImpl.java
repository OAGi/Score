package org.oagi.score.e2e.impl.page.module;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.module.ModuleSetReleaseXMLSchemaValidationDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class ModuleSetReleaseXMLSchemaValidationDialogImpl implements ModuleSetReleaseXMLSchemaValidationDialog {
    private static final By COPY_TO_CLIPBOARD__BUTTON_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Copy to clipboard\")]//ancestor::button[1]");
    private final BasePageImpl parent;

    public ModuleSetReleaseXMLSchemaValidationDialogImpl(BasePageImpl parent) {
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
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-dialog-container//div/span"));
    }

    @Override
    public void hitCopyToClipboardButton() {
        click(getCopyToClipboardButton());
    }

    @Override
    public WebElement getCopyToClipboardButton() {
        return elementToBeClickable(getDriver(), COPY_TO_CLIPBOARD__BUTTON_LOCATOR);
    }
}
