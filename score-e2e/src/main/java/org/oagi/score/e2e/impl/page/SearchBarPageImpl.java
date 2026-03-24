package org.oagi.score.e2e.impl.page;

import org.oagi.score.e2e.page.SearchBarPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.invisibilityOfLoadingContainerElement;
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

    private By showAdvancedSearchButtonLocator() {
        return xpath("//score-search-bar//div[contains(@class, \"main-search\")]"
                + "//button[.//mat-icon[normalize-space(.)=\"keyboard_arrow_down\"]]");
    }

    private By hideAdvancedSearchButtonLocator() {
        return xpath("//score-search-bar//div[contains(@class, \"main-search\")]"
                + "//button[.//mat-icon[normalize-space(.)=\"keyboard_arrow_up\"]]");
    }

    private By advancedSearchPanelLocator() {
        return xpath("//score-search-bar//div[contains(@class, \"advanced-search\")]");
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
        return visibilityOfElementLocated(getDriver(), showAdvancedSearchButtonLocator());
    }

    @Override
    public void showAdvancedSearchPanel() {
        invisibilityOfLoadingContainerElement(getDriver());
        if (!getDriver().findElements(advancedSearchPanelLocator()).isEmpty()) {
            return;
        }
        click(getDriver(), getShowAdvancedSearchButton());
    }

    @Override
    public WebElement getHideAdvancedSearchButton() {
        return visibilityOfElementLocated(getDriver(), hideAdvancedSearchButtonLocator());
    }

    @Override
    public void hideAdvancedSearchPanel() {
        invisibilityOfLoadingContainerElement(getDriver());
        if (getDriver().findElements(advancedSearchPanelLocator()).isEmpty()) {
            return;
        }
        click(getDriver(), getHideAdvancedSearchButton());
    }

}
