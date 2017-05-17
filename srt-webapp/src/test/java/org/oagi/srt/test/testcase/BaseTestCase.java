package org.oagi.srt.test.testcase;

import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.oagi.srt.test.helper.ChromeDriverSingleton;
import org.openqa.selenium.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.net.URL;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Miroslav Ljubicic.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class BaseTestCase extends TestCase {

    @Autowired
    private WebApplicationContext context;

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

    private String getWebdriverPath() {
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

        ClassLoader classLoader = getClass().getClassLoader();
        URL webdriverResource = classLoader.getResource(webdriver);
        String webdriverPath = (webdriverResource != null) ? webdriverResource.getFile() : null;
        if (StringUtils.isEmpty(webdriverPath)) {
            throw new IllegalStateException("Can't find webdriver from resources.");
        }
        return webdriverPath;
    }

    @Before
    public void setUp() throws Exception {
        String webdriverPath = getWebdriverPath();

        System.setProperty("webdriver.chrome.driver", webdriverPath);
        driver = ChromeDriverSingleton.getInstance();
        baseUrl = "http://localhost:8080"; // http://129.6.33.174:8080
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        ChromeDriverSingleton.quitDriver();

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
