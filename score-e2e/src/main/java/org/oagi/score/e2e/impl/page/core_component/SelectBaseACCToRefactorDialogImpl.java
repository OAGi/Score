package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.SelectBaseACCToRefactorDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class SelectBaseACCToRefactorDialogImpl implements SelectBaseACCToRefactorDialog {

    private static final By ANALYZE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Analyze\")]//ancestor::button[1]");

    private static final By CANCEL_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Cancel\")]//ancestor::button[1]");

    private static final By REFACTOR_BUTTON_LOCATOR =
            By.xpath("//score-based-acc-dialog//span[contains(text(),\"Refactor\")]//ancestor::button[1]");

    private ACCViewEditPageImpl parent;
    private String associationToRefactor;

    public SelectBaseACCToRefactorDialogImpl(ACCViewEditPageImpl parent, String associationPropertyTerm) {
        this.parent = parent;
        this.associationToRefactor = associationPropertyTerm;
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
        assert (getText(title).startsWith("Select a base ACC to move"));
        assert (getText(title).contains(this.associationToRefactor));
        return true;
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-dialog-container//mat-card-title[contains(@class, \"mat-card-title\")]"));
    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-dialog-container//tbody/tr[" + idx + "]"));
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-dialog-container//td//span[contains(text(), \"" + value + "\")]/ancestor::tr"));
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    @Override
    public WebElement getCancelButton() {
        return elementToBeClickable(getDriver(), CANCEL_BUTTON_LOCATOR);
    }

    @Override
    public ACCViewEditPage hitCancelButton() {
        click(getCancelButton());
        return parent;
    }

    @Override
    public WebElement getAnalyzeButton() {
        return elementToBeClickable(getDriver(), ANALYZE_BUTTON_LOCATOR);
    }

    @Override
    public SelectBaseACCToRefactorDialog hitAnalyzeButton() {
        click(getAnalyzeButton());
        return this;
    }

    @Override
    public WebElement getRefactorButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), REFACTOR_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), REFACTOR_BUTTON_LOCATOR);
        }
    }

    @Override
    public ACCViewEditPage hitRefactorButton() {
        click(getRefactorButton(true));
        return parent;
    }
}
