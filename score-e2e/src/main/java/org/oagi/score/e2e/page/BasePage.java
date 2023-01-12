package org.oagi.score.e2e.page;

import org.oagi.score.e2e.Configuration;
import org.oagi.score.e2e.api.APIFactory;
import org.openqa.selenium.WebDriver;

public interface BasePage extends Page {

    WebDriver getDriver();

    Configuration getConfig();

    APIFactory getAPIFactory();

}
