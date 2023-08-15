package org.oagi.score.e2e.impl.page.core_component;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.LogObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.core_component.HistoryCompareDialog;
import org.oagi.score.e2e.page.core_component.HistoryPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.Arrays;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class HistoryPageImpl extends BasePageImpl implements HistoryPage {

    private static final By COMPARE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Compare\")]//ancestor::button[1]");

    private final LogObject logObject;

    public HistoryPageImpl(BasePage parent,
                           LogObject logObject) {
        super(parent);
        this.logObject = logObject;
    }

    @Override
    protected String getPageUrl() {
        String type = this.logObject.getType();
        if (Arrays.asList("ACC", "ASCCP", "BCCP", "DT").contains(type.toUpperCase())) {
            return getConfig().getBaseUrl().resolve("/log/core-component/" +
                    this.logObject.getReference() + "?type=" + type + "&manifestId=" + logObject.getManifestId()).toString();
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "History".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        defaultWait(getDriver());
        return visibilityOfElementLocated(getDriver(), By.xpath("//tbody/tr[" + idx + "]"));
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        defaultWait(getDriver());
        return visibilityOfElementLocated(getDriver(), By.xpath("//*[contains(text(),\"" + value + "\")]//ancestor::tr"));
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    @Override
    public void setItemsPerPage(int items) {
        WebElement itemsPerPageField = elementToBeClickable(getDriver(),
                By.xpath("//div[.=\" Items per page: \"]/following::div[5]"));
        click(itemsPerPageField);
        waitFor(Duration.ofMillis(500L));
        WebElement itemField = elementToBeClickable(getDriver(),
                By.xpath("//span[contains(text(), \"" + items + "\")]//ancestor::mat-option//div[1]//preceding-sibling::span"));
        click(itemField);
        waitFor(Duration.ofMillis(500L));
    }

    @Override
    public void goToNextPage() {
        ((JavascriptExecutor) getDriver())
                .executeScript("window.scrollTo(0, document.body.scrollHeight)");
        click(elementToBeClickable(getDriver(), By.xpath("//button[@aria-label='Next page']")));
    }

    @Override
    public void goToPreviousPage() {
        ((JavascriptExecutor) getDriver())
                .executeScript("window.scrollTo(0, document.body.scrollHeight)");
        click(elementToBeClickable(getDriver(), By.xpath("//button[@aria-label='Previous page']")));
    }

    @Override
    public void checkRecordAtIndex(int idx) {
        WebElement tr = getTableRecordAtIndex(idx);
        WebElement td = getColumnByName(tr, "check");
        checkElement(getDriver(), td.findElement(By.tagName("input")));
    }

    @Override
    public WebElement getCompareButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), COMPARE_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), COMPARE_BUTTON_LOCATOR);
        }
    }

    @Override
    public HistoryCompareDialog compare() {
        click(getCompareButton(true));

        HistoryCompareDialog historyCompareDialog = new HistoryCompareDialogImpl(this, this.logObject);
        assert historyCompareDialog.isOpened();
        return historyCompareDialog;
    }

}
