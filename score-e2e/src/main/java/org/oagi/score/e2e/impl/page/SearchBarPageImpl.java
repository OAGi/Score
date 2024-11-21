package org.oagi.score.e2e.impl.page;

import org.oagi.score.e2e.page.SearchBarPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;

public class SearchBarPageImpl implements SearchBarPage {

    private WebDriver driver;
    private String baseXPath;

    public SearchBarPageImpl(WebDriver driver) {
        this.driver = driver;
    }

    public SearchBarPageImpl(WebDriver driver, String baseXPath) {
        this.driver = driver;
        this.baseXPath = baseXPath;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public By xpath(String expression) {
        String xpathExpression = (this.baseXPath != null ? this.baseXPath : "") + expression;
        return By.xpath(xpathExpression);
    }

    @Override
    public WebElement getSearchButton() {
        return visibilityOfElementLocated(getDriver(),
                xpath("//score-search-bar//div[@class=\"main-search\"]//button[1]"));
    }

    @Override
    public WebElement getInputFieldInSearchBar() {
        return visibilityOfElementLocated(getDriver(),
                xpath("//score-search-bar//div[@class=\"main-search\"]//mat-form-field//input"));
    }

    @Override
    public WebElement getShowAdvancedSearchButton() {
        return visibilityOfElementLocated(getDriver(),
                xpath("//score-search-bar//button[@ng-reflect-message=\"Show Advanced Search\"]"));
    }

    @Override
    public void showAdvancedSearchPanel() {
        try {
            click(getShowAdvancedSearchButton());
        } catch (TimeoutException e) {
            if (getHideAdvancedSearchButton() != null) { // the panel has been opened already
                return;
            }
            throw e;
        }
    }

    @Override
    public WebElement getHideAdvancedSearchButton() {
        return visibilityOfElementLocated(getDriver(),
                xpath("//score-search-bar//button[@ng-reflect-message=\"Hide Advanced Search\"]"));
    }

    @Override
    public void hideAdvancedSearchPanel() {
        try {
            click(getHideAdvancedSearchButton());
        } catch (TimeoutException e) {
            if (getShowAdvancedSearchButton() != null) { // the panel has been closed already
                return;
            }
            throw e;
        }
    }

}
