package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.core_component.FindWhereUsedDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;

public class FindWhereUsedDialogImpl implements FindWhereUsedDialog {
    private final BasePageImpl parent;

    private final String contextMenuName;

    public FindWhereUsedDialogImpl(BasePageImpl parent, String contextMenuName) {
        this.parent = parent;
        this.contextMenuName = contextMenuName;
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
    public WebElement getTableRecordByValue(String value) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-mdc-card-content//span[contains(text(), \"" + value + "\")]"));
    }
}
