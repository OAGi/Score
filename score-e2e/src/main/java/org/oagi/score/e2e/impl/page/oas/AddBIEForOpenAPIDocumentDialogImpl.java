package org.oagi.score.e2e.impl.page.oas;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.oas.AddBIEForOpenAPIDocumentDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;

public class AddBIEForOpenAPIDocumentDialogImpl implements AddBIEForOpenAPIDocumentDialog {

    private final BasePageImpl parent;

    public AddBIEForOpenAPIDocumentDialogImpl(BasePageImpl parent) {
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
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-dialog-container//mat-mdc-card-title"));
    }

}
