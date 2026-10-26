package org.oagi.score.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.impl.api.DSLContextAPIFactory;
import org.oagi.score.e2e.impl.page.LoginPageImpl;
import org.oagi.score.e2e.page.LoginPage;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ExtendWith({BaseTest.ScreenshotOnFailure.class, BaseTest.APIFactoryLifecycle.class})
public class BaseTest {

    /**
     * Gives each test class its own APIFactory (a one-connection pool, so session settings such as FOREIGN_KEY_CHECKS
     * stay on one connection) and closes it once the class is done. Classes run concurrently, so the factory cannot
     * live in one shared field, and a pool that is never closed keeps its connection until the database refuses
     * new ones, which also makes the clean-up of the test data fail.
     */
    static class APIFactoryLifecycle implements BeforeAllCallback, AfterAllCallback {
        @Override
        public void beforeAll(ExtensionContext context) {
            BaseTest.config = Configuration.load();
            apiFactories.put(context.getRequiredTestClass(), DSLContextAPIFactory.build(config));
        }

        @Override
        public void afterAll(ExtensionContext context) throws Exception {
            APIFactory apiFactory = apiFactories.remove(context.getRequiredTestClass());
            if (apiFactory != null) {
                apiFactory.close();
            }
        }
    }

    /** Saves what the browser showed when a test failed, as target/screenshots/&lt;class&gt;.&lt;method&gt;.png. */
    static class ScreenshotOnFailure implements TestExecutionExceptionHandler {
        @Override
        public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
            try {
                WebDriver driver = ((BaseTest) context.getRequiredTestInstance()).getDriver();
                if (driver instanceof TakesScreenshot screenshot) {
                    Path directory = Path.of("target", "screenshots");
                    Files.createDirectories(directory);
                    String name = context.getRequiredTestClass().getSimpleName() + "." + context.getRequiredTestMethod().getName();
                    Files.write(directory.resolve(name + ".png"), screenshot.getScreenshotAs(OutputType.BYTES));
                }
            } catch (Exception ignored) {
                // The screenshot is a convenience; the original failure is what matters.
            }
            throw throwable;
        }
    }

    private static Configuration config;

    private static final Map<Class<?>, APIFactory> apiFactories = new ConcurrentHashMap<>();
    private WebDriver driver;

    private LoginPage loginPage;

    @BeforeAll
    public static void setUp() {
        // Turn off the ad messages of jOOQ
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");

        // Set a default log level
        // System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
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
        return apiFactories.get(getClass());
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
