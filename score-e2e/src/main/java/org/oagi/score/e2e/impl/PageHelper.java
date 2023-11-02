package org.oagi.score.e2e.impl;

import org.apache.commons.lang3.StringUtils;
import org.oagi.score.e2e.impl.page.MultiActionSnackBarImpl;
import org.oagi.score.e2e.page.MultiActionSnackBar;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.time.Duration.ofMillis;
import static org.apache.commons.lang3.StringUtils.trim;

public abstract class PageHelper {

    private static final int DEFAULT_RETRY_COUNT = 2;
    private static final long DEFAULT_SLEEP_TIMEOUT = 500;
    private static final Duration DEFAULT_WAIT_DURATION = ofMillis(DEFAULT_SLEEP_TIMEOUT);

    private PageHelper() {
    }

    public static void retry(Runnable runnable) {
        retry(runnable, DEFAULT_RETRY_COUNT);
    }

    public static void retry(Runnable runnable, int retry) {
        retry(() -> {
            runnable.run();
            return null;
        }, null, retry);
    }

    public static <T> T retry(Supplier<T> supplier) {
        return retry(supplier, null, DEFAULT_RETRY_COUNT);
    }

    public static <T> T retry(Supplier<T> supplier, T failureValue) {
        return retry(supplier, failureValue, DEFAULT_RETRY_COUNT);
    }

    public static <T> T retry(Supplier<T> supplier, T failureValue, int retry) {
        WebDriverException ex = null;
        while (retry > 0) {
            try {
                return supplier.get();
            } catch (WebDriverException e) {
                ex = e;
                retry--;
                waitFor(DEFAULT_WAIT_DURATION);
            }
        }
        if (failureValue != null) {
            return failureValue;
        }
        throw ex;
    }

    public static WebElement findElement(WebDriver driver, By locator) {
        return driver.findElement(locator);
    }

    public static Wait<WebDriver> defaultWait(WebDriver driver) {
        return wait(driver, Duration.ofSeconds(3L), ofMillis(100L));
    }

    public static Wait<WebDriver> shortWait(WebDriver driver) {
        return wait(driver, Duration.ofSeconds(1L), ofMillis(100L));
    }

    public static Wait<WebDriver> wait(WebDriver driver, Duration timeout, Duration interval) {
        return new FluentWait<>(driver).withTimeout(timeout).pollingEvery(interval);
    }

    public static WebElement visibilityOfElementLocated(WebDriver driver, By locator) {
        return visibilityOfElementLocated(defaultWait(driver), locator);
    }

    public static WebElement visibilityOfElementLocated(Wait<WebDriver> wait, By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static List<WebElement> visibilityOfAllElementsLocatedBy(WebDriver driver, By locator) {
        return visibilityOfAllElementsLocatedBy(defaultWait(driver), locator);
    }

    public static List<WebElement> visibilityOfAllElementsLocatedBy(Wait<WebDriver> wait, By locator) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    public static WebElement elementToBeClickable(WebDriver driver, By locator) {
        return elementToBeClickable(defaultWait(driver), locator);
    }

    public static WebElement elementToBeClickable(Wait<WebDriver> wait, By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static boolean invisibilityOfElementLocated(WebDriver driver, By locator) {
        return invisibilityOfElementLocated(defaultWait(driver), locator);
    }

    public static boolean invisibilityOfElementLocated(Wait<WebDriver> wait, By locator) {
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public static void invisibilityOfLoadingContainerElement(WebDriver driver) {
        invisibilityOfLoadingContainerElement(wait(driver, Duration.ofSeconds(10L), ofMillis(100L)));
    }

    public static void invisibilityOfLoadingContainerElement(Wait<WebDriver> wait) {
        // add try-catch statement in case the loading icon won't be caught by the xpath in the given time
        try {
            invisibilityOfElementLocated(wait, By.xpath("//*[contains(@class, 'loading-container')]"));
        } catch (WebDriverException ex) {
        }
    }

    public static String getText(WebElement element) {
        if (element == null) {
            return null;
        }
        String s;
        switch (element.getTagName()) {
            case "input":
            case "textarea":
                s = trim(element.getAttribute("value"));
                break;
            default:
                s = trim(element.getText());
        }
        return (s.length() > 0) ? s : null;
    }

    public static WebElement clear(WebElement element) {
        if (element != null) {
            element.clear();
            // ng-reflect-model attribute in some elements couldn't reflect this clear text.
            element.sendKeys(" ");
            element.sendKeys(Keys.BACK_SPACE);
            element.clear();
            if (!StringUtils.isEmpty(element.getText())) {
                waitFor(ofMillis(500L));
                element.sendKeys(Keys.CONTROL + "a");
                element.sendKeys(Keys.BACK_SPACE);
            }
        }
        return element;
    }

    public static WebElement sendKeys(WebElement element, LocalDateTime localDateTime) {
        return sendKeys(element, localDateTime.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
    }

    public static WebElement sendKeys(WebElement element, CharSequence... keysToSend) {
        if (element != null) {
            if (!StringUtils.isEmpty(getText(element))) {
                clear(element);
            }
            for (CharSequence cs : keysToSend) {
                if (cs == null) {
                    return element;
                }
            }
            element.sendKeys(keysToSend);
        }
        return element;
    }

    public static WebElement click(WebElement element) {
        return click(null, element);
    }

    public static WebElement click(WebDriver driver, WebElement element) {
        if (element != null) {
            String tagName = element.getTagName();
            try {
                element.click();
            } catch (ElementClickInterceptedException e) {
                if ("mat-select".equals(tagName)) {
                    WebElement arrowWrapper = element.findElement(By.cssSelector("div > div.mat-select-arrow-wrapper"));
                    click(arrowWrapper);
                } else {
                    if (driver != null) {
                        JavascriptExecutor executor = (JavascriptExecutor) driver;
                        executor.executeScript("arguments[0].click();", element);
                    } else {
                        throw e;
                    }
                }
            }
            waitFor(DEFAULT_WAIT_DURATION);
        }
        return element;
    }

    public static WebElement checkElement(WebDriver driver, WebElement element) {
        if (element != null) {
            waitFor(ofMillis(500L));
            Actions action = new Actions(driver);
            action.moveToElement(element).perform();
            element.sendKeys(Keys.SPACE);
        }
        return element;
    }

    public static MultiActionSnackBar getMultiActionSnackBar(WebDriver driver) {
        return new MultiActionSnackBarImpl(driver);
    }

    /**
     * Return the text message from the snackbar.
     *
     * @param driver web driver
     * @return the text message from the snackbar
     */
    public static String getSnackBarMessage(WebDriver driver) {
        String xpathExpr = "//simple-snack-bar/span";
        WebElement snackBar = retry(() -> visibilityOfElementLocated(driver, By.xpath(xpathExpr)));
        return getText(snackBar);
    }

    /**
     * Return the UI element of the snackbar with the given text message that contains in the snackbar.
     *
     * @param driver  web driver
     * @param message text message that the snack bar contains
     * @return the UI element of the snackbar
     */
    public static WebElement getSnackBar(WebDriver driver, String message) {
        String xpathExpr = "//simple-snack-bar//span[contains(text(), \"" + message + "\")]";
        return visibilityOfElementLocated(driver, By.xpath(xpathExpr));
    }

    public static String getDialogTitle(WebDriver driver) {
        String xpathExpr = "//mat-dialog-container//div[contains(@class, \"header\")]/span";
        WebElement snackBar = retry(() -> visibilityOfElementLocated(driver, By.xpath(xpathExpr)));
        return getText(snackBar);
    }

    public static WebElement getDialogButtonByName(WebDriver driver, String buttonName) {
        String xpathExpr = "//score-confirm-dialog//span[contains(text(), \"" + buttonName + "\")]//ancestor::button[1]";
        return retry(() -> elementToBeClickable(driver, By.xpath(xpathExpr)));
    }

    public static void switchToNextTab(WebDriver driver) {
        List<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(1));
    }

    public static void switchToMainTab(WebDriver driver) {
        List<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(0));
    }

    public static boolean isElementPresent(WebDriver driver, By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public static void waitFor(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WebDriverException(e);
        }
    }

    public static void escape(WebDriver driver) {
        new Actions(driver).sendKeys(Keys.ESCAPE).perform();
    }

    public static boolean isChecked(WebElement element) {
        String klass = element.getAttribute("class");
        return "true".equals(element.getAttribute("aria-checked")) ||
                "true".equals(element.getAttribute("ng-reflect-checked")) ||
                "checked".equals(element.getAttribute("ng-reflect-state")) ||
                "true".equals(element.getAttribute("aria-checked")) ||
                (!StringUtils.isEmpty(klass) && klass.contains("mat-checkbox-checked"));
    }
}
