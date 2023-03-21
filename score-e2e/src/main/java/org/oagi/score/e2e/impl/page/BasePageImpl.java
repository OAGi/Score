package org.oagi.score.e2e.impl.page;

import org.oagi.score.e2e.Configuration;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.page.BasePage;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasePageImpl implements BasePage {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final WebDriver driver;

    private final Configuration config;

    private final APIFactory apiFactory;

    public BasePageImpl(BasePage parent) {
        this(parent.getDriver(), parent.getConfig(), parent.getAPIFactory());
    }

    public BasePageImpl(WebDriver driver, Configuration config, APIFactory apiFactory) {
        this.driver = driver;
        this.config = config;
        this.apiFactory = apiFactory;
    }

    public WebDriver getDriver() {
        return this.driver;
    }

    public Configuration getConfig() {
        return this.config;
    }

    public APIFactory getAPIFactory() {
        return apiFactory;
    }

    @Override
    public boolean isOpened() {
        try {
            getTitle();
        } catch (TimeoutException e) {
            logger.error("Cannot locate the title of the page.", e);
            return false;
        }

        String url = getPageUrl();
        String currentUrl = getDriver().getCurrentUrl();
        return currentUrl.startsWith(url);
    }

    protected abstract String getPageUrl();

}
