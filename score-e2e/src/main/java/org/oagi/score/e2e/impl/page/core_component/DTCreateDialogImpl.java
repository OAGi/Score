package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.page.core_component.DTCreateDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;

public class DTCreateDialogImpl implements DTCreateDialog {

    private ViewEditCoreComponentPageImpl parent;

    private String branch;

    public DTCreateDialogImpl(ViewEditCoreComponentPageImpl parent, String branch) {
        this.parent = parent;
        this.branch = branch;
    }

    private WebDriver getDriver() {
        return this.parent.getDriver();
    }

    @Override
    public boolean isOpened() {
        WebElement title;
        try {
            title = getTitle();
        } catch (TimeoutException e) {
            return false;
        }
        assert "Select based DT".equals(getText(title));
        return true;
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-dialog-container//span[contains(@class, \"title\")]"));
    }

}
