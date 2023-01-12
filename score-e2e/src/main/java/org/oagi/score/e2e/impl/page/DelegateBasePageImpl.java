package org.oagi.score.e2e.impl.page;

import org.oagi.score.e2e.Configuration;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.page.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class DelegateBasePageImpl implements BasePage {

    private final BasePageImpl basePageImpl;

    public DelegateBasePageImpl(BasePageImpl basePageImpl) {
        this.basePageImpl = basePageImpl;
    }

    @Override
    public boolean isOpened() {
        return basePageImpl.isOpened();
    }

    @Override
    public void openPage() {
        basePageImpl.openPage();
    }

    @Override
    public WebElement getTitle() {
        return basePageImpl.getTitle();
    }

    public WebDriver getDriver() {
        return basePageImpl.getDriver();
    }

    public Configuration getConfig() {
        return basePageImpl.getConfig();
    }

    public APIFactory getAPIFactory() {
        return basePageImpl.getAPIFactory();
    }

}
