package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.page.core_component.DTCreateDialog;
import org.openqa.selenium.*;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class DTCreateDialogImpl implements DTCreateDialog {
    private static final By DEN_FIELD_LOCATOR =
            By.xpath("//div[contains(@class, \"mat-mdc-dialog-content\")]//input[contains(@placeholder, \"DEN\")]");
    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//div[contains(@class, \"mat-mdc-dialog-content\")]//span[contains(text(), \"Search\")]//ancestor::button[1]");
    private static final By CREATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Create\")]//ancestor::button[1]");

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
        assert "Select based DT".equals(getText(title.findElement(By.tagName("span"))));
        return true;
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-dialog-container//div[contains(@class, \"mat-mdc-dialog-title\")]"));
    }

    @Override
    public void selectBasedDTByDEN(String den) {
        sendKeys(getDENField(), den);
        click(getSearchButton());
        retry(() -> {
            WebElement td;
            WebElement tr;
            try {
                tr = getTableRecordAtIndex(1);
                td = getColumnByName(tr, "den");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a Data Type using " + den, e);
            }
            if (!den.equals(getDENFieldFromTheTable(td))) {
                throw new NoSuchElementException("Cannot locate a Data Type using " + den);
            }
            click(tr);
        });

    }

    @Override
    public WebElement getSearchButton() {
        return visibilityOfElementLocated(getDriver(), SEARCH_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getCreateButton() {
        return visibilityOfElementLocated(getDriver(), CREATE_BUTTON_LOCATOR);
    }

    @Override
    public void hitSearchButton() {
        retry(() -> click(getSearchButton()));
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//div[contains(@class, \"mat-mdc-dialog-content\")]//tbody/tr[" + idx + "]"));
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//td[contains(text(), \"" + value + "\")]/ancestor::tr"));
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    @Override
    public WebElement getDENField() {
        return visibilityOfElementLocated(getDriver(), DEN_FIELD_LOCATOR);
    }

    private String getDENFieldFromTheTable(WebElement tableData) {
        return getText(tableData.findElement(By.cssSelector("div.den > a > span")));
    }

    @Override
    public void hitCreateButton() {
        retry(() -> {
            click(getCreateButton());
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
    }
}
