package org.oagi.srt.test.testcase;

import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.oagi.srt.test.helper.ChromeDriverSingleton;
import org.openqa.selenium.*;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Miroslav Ljubicic.
 */
public abstract class BaseTestCase extends TestCase {
    private static WebDriver driver;
    private String baseUrl;
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();

    private enum OperatingSystem {
        Windows,
        MacOSX,
        Linux,
        Other
    }

    private OperatingSystem getOperatingSystem() {
        String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (osName.indexOf("mac") >= 0 || osName.indexOf("darwin") >= 0) {
            return OperatingSystem.MacOSX;
        } else if (osName.indexOf("win") >= 0) {
            return OperatingSystem.Windows;
        } else if (osName.indexOf("nux") >= 0) {
            return OperatingSystem.Linux;
        } else {
            return OperatingSystem.Other;
        }
    }

    @Before
    public void setUp() throws Exception {
        String webdriver;
        switch (getOperatingSystem()) {
            case Windows:
                webdriver = "chromedriver.exe";
                break;
            case MacOSX:
                webdriver = "chromedriver";
                break;
            default:
                throw new UnsupportedOperationException("Unsupported Operating System: " + (System.getProperty("os.name")));
        }

        System.setProperty("webdriver.chrome.driver", "./srt-webapp/src/test/resources/" + webdriver);
        driver = ChromeDriverSingleton.getInstance();
        baseUrl = "http://localhost:8080"; // http://129.6.33.174:8080
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
//        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!StringUtils.isEmpty(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }

    public WebDriver getDriver() {
        return driver;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public boolean isAcceptNextAlert() {
        return acceptNextAlert;
    }

    public StringBuffer getVerificationErrors() {
        return verificationErrors;
    }

    protected boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    protected boolean isAlertPresent() {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }

    protected String closeAlertAndGetItsText() {
        try {
            Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            if (acceptNextAlert) {
                alert.accept();
            } else {
                alert.dismiss();
            }
            return alertText;
        } finally {
            acceptNextAlert = true;
        }
    }
}
