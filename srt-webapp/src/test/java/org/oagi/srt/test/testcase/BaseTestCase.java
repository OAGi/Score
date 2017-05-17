package org.oagi.srt.test.testcase;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.oagi.srt.test.helper.ChromeDriverSingleton;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.concurrent.TimeUnit;

//import org.openqa.selenium.support.ui.Select;

/**
 * Created by Miroslav Ljubicic.
 */
public abstract class BaseTestCase extends TestCase {
    private static WebDriver driver;
    private String baseUrl;
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();

    @Before
    public void setUp() throws Exception {
        System.setProperty("webdriver.chrome.driver", "./srt-webapp/src/test/resources/chromedriver.exe");
        driver = ChromeDriverSingleton.getInstance();
        baseUrl = "http://localhost:8080"; // http://129.6.33.174:8080
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
//        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
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
