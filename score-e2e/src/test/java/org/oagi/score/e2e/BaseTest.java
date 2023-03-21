package org.oagi.score.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.impl.api.DSLContextAPIFactory;
import org.oagi.score.e2e.impl.page.LoginPageImpl;
import org.oagi.score.e2e.page.LoginPage;
import org.openqa.selenium.WebDriver;

public class BaseTest {

    private static Configuration config;

    private static APIFactory apiFactory;
    private WebDriver driver;

    private LoginPage loginPage;

    @BeforeAll
    public static void setUp() {
        // Turn off the ad messages of jOOQ
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");

        BaseTest.config = Configuration.load();
        BaseTest.apiFactory = DSLContextAPIFactory.build(BaseTest.config);
    }

    @BeforeEach
    public void init() {
        this.driver = BaseTest.config.newWebDriver();

        this.loginPage = new LoginPageImpl(this.getDriver(), this.getConfig(), this.getAPIFactory());
    }

    public Configuration getConfig() {
        return config;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public APIFactory getAPIFactory() {
        return apiFactory;
    }

    public LoginPage loginPage() {
        return loginPage;
    }

    @AfterEach
    public void tearDown() {
        if (this.driver != null) {
            this.driver.quit();
        }
    }

}
