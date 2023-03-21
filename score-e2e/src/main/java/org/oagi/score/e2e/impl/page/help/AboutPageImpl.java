package org.oagi.score.e2e.impl.page.help;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.help.AboutPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class AboutPageImpl extends BasePageImpl implements AboutPage {

    public AboutPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/about").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Score".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.xpath("//score-about/div/div/h2[1]"));
    }

    @Override
    public VersionList getVersionList() {
        return new VersionListImpl();
    }

    @Override
    public WebElement getContributorsLink() {
        return visibilityOfElementLocated(getDriver(), By.xpath("//a[contains(text(), 'contributor')]"));
    }

    @Override
    public WebElement getLicense() {
        return visibilityOfElementLocated(getDriver(), By.xpath("//p[contains(text(), " +
                "'This project is licensed under the terms of the MIT license.')]"));
    }

    private class VersionListImpl implements VersionList {

        @Override
        public List<WebElement> getItems() {
            return visibilityOfAllElementsLocatedBy(getDriver(), By.xpath("//ul/li/p"));
        }

        @Override
        public WebElement getItemByName(String name) {
            return visibilityOfElementLocated(getDriver(), By.xpath("//li//b[contains(text(), \"" + name + "\")]//ancestor::p"));
        }

    }
}
