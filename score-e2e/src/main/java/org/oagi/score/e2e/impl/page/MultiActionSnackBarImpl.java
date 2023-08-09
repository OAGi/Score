package org.oagi.score.e2e.impl.page;

import org.oagi.score.e2e.page.MultiActionSnackBar;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;

public class MultiActionSnackBarImpl implements MultiActionSnackBar {
    private final WebDriver driver;

    public MultiActionSnackBarImpl(WebDriver driver) {
        this.driver = driver;
    }

    private WebDriver getDriver() {
        return driver;
    }

    @Override
    public WebElement getHeaderElement() {
        return visibilityOfElementLocated(getDriver(),
                By.xpath("//score-multi-actions-snack-bar//div[contains(@class, \"header\")]"));
    }

    @Override
    public WebElement getMessageElement() {
        return visibilityOfElementLocated(getDriver(),
                By.xpath("//score-multi-actions-snack-bar//div[contains(@class, \"message\")]"));
    }

    @Override
    public WebElement getActionButtonByName(String name) {
        return visibilityOfElementLocated(getDriver(),
                By.xpath("//score-multi-actions-snack-bar//div[contains(@class, \"actions\")]" +
                        "//button[contains(text(), \"" + name + "\")]"));
    }

}
