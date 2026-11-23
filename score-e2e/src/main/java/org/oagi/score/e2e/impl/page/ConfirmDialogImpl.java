package org.oagi.score.e2e.impl.page;

import org.oagi.score.e2e.page.ConfirmDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class ConfirmDialogImpl implements ConfirmDialog {

    private static final By TITLE_LOCATOR =
            By.xpath("//mat-dialog-container//*[contains(@class, \"mat-mdc-dialog-title\")]/span[1]");

    private static final By MESSAGE_LOCATOR =
            By.xpath("//mat-dialog-container//div[contains(@class, \"mat-mdc-dialog-content\")]/p");

    private static final By LIST_ITEM_LOCATOR =
            By.xpath("//mat-dialog-container//div[contains(@class, \"mat-mdc-dialog-content\")]//li");

    private static final By ACTION_BUTTON_LOCATOR =
            By.xpath("//mat-dialog-container//div[contains(@class, \"mat-mdc-dialog-actions\")]//button");

    private final WebDriver driver;

    public ConfirmDialogImpl(WebDriver driver) {
        this.driver = driver;
    }

    private WebDriver getDriver() {
        return driver;
    }

    @Override
    public boolean isOpened() {
        try {
            return getTitle() != null;
        } catch (TimeoutException e) {
            return false;
        }
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), TITLE_LOCATOR);
    }

    @Override
    public String getMessage() {
        visibilityOfElementLocated(getDriver(), MESSAGE_LOCATOR);
        return getDriver().findElements(MESSAGE_LOCATOR).stream()
                .map(e -> getText(e)).reduce("", (a, b) -> a.isEmpty() ? b : a + " " + b);
    }

    @Override
    public List<String> getListItems() {
        visibilityOfElementLocated(getDriver(), MESSAGE_LOCATOR);
        return getDriver().findElements(LIST_ITEM_LOCATOR).stream().map(e -> getText(e)).toList();
    }

    @Override
    public List<String> getButtonLabels() {
        visibilityOfElementLocated(getDriver(), ACTION_BUTTON_LOCATOR);
        return getDriver().findElements(ACTION_BUTTON_LOCATOR).stream().map(e -> getText(e)).toList();
    }

    @Override
    public WebElement getButton(String label) {
        return elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//div[contains(@class, \"mat-mdc-dialog-actions\")]" +
                        "//button[normalize-space(.) = \"" + label + "\"]"));
    }

    @Override
    public void clickButton(String label) {
        click(getButton(label));
    }
}
