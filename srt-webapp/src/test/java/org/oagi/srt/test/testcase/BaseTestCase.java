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

    protected void waitForElementPresent(By by) throws InterruptedException {
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(by)) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }
    }

    protected void createUser(String username, String pass) {
        getDriver().findElement(By.id("newAccountLink")).click();
        getDriver().findElement(By.id("sign_up_form:username")).clear();
        getDriver().findElement(By.id("sign_up_form:username")).sendKeys(username);
        getDriver().findElement(By.id("sign_up_form:user_password")).clear();
        getDriver().findElement(By.id("sign_up_form:user_password")).sendKeys(pass);
        getDriver().findElement(By.id("sign_up_form:user_confirm_password")).clear();
        getDriver().findElement(By.id("sign_up_form:user_confirm_password")).sendKeys(pass);
        getDriver().findElement(By.id("sign_up_form:create_account")).click();
    }

    protected void assertTextEqual(By by, String text) {
        try {
            assertEquals(text, getDriver().findElement(by).getText());
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
    }

    protected void assertElementPresent(By by) {
        try {
            assertTrue(isElementPresent(by));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
    }

    protected void type(By by, CharSequence text) {
        getDriver().findElement(by).clear();
        getDriver().findElement(by).sendKeys(text);
    }

    protected void click(By by) {
        getDriver().findElement(by).click();
    }

    protected void open(String relativeURL) {
        getDriver().get(getBaseUrl() + relativeURL);
    }

    protected void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void assertAttributeEquals(By by, String attribute, String text) {
        try {
            assertEquals(text, getDriver().findElement(by).getAttribute(attribute));
        } catch (Error e) {
            getVerificationErrors().append(e.toString());
        }
    }

    protected void sendKeys(By by, CharSequence keys) {
        getDriver().findElement(by).sendKeys(keys);
    }

    protected void sendKeys(By by, Keys keys) {
        getDriver().findElement(by).sendKeys(keys);
    }

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

    protected void logout(String user) {
        getDriver().findElement(By.linkText(user)).click();
        getDriver().findElement(By.linkText("Sign out")).click();
    }

    protected void login(String username, String password) {
        getDriver().findElement(By.id("username")).clear();
        getDriver().findElement(By.id("username")).sendKeys(username);
        getDriver().findElement(By.id("password")).clear();
        getDriver().findElement(By.id("password")).sendKeys(password);
        getDriver().findElement(By.id("signInBtn")).click();
    }


}
