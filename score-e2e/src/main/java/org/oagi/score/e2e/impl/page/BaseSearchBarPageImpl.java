package org.oagi.score.e2e.impl.page;

import org.oagi.score.e2e.Configuration;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.SearchBarPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class BaseSearchBarPageImpl extends BasePageImpl implements SearchBarPage {

    private SearchBarPage searchBarPage;

    public BaseSearchBarPageImpl(BasePage parent) {
        super(parent);
        setSearchBarPage(new SearchBarPageImpl(getDriver()));
    }

    public BaseSearchBarPageImpl(WebDriver driver, Configuration config, APIFactory apiFactory) {
        super(driver, config, apiFactory);
        setSearchBarPage(new SearchBarPageImpl(getDriver()));
    }

    public void setSearchBarPage(SearchBarPage searchBarPage) {
        this.searchBarPage = searchBarPage;
    }

    @Override
    public WebElement getSearchButton() {
        return this.searchBarPage.getSearchButton();
    }

    @Override
    public WebElement getInputFieldInSearchBar() {
        return this.searchBarPage.getInputFieldInSearchBar();
    }

    @Override
    public WebElement getShowAdvancedSearchButton() {
        return this.searchBarPage.getShowAdvancedSearchButton();
    }

    @Override
    public void showAdvancedSearchPanel() {
        this.searchBarPage.showAdvancedSearchPanel();
    }

    @Override
    public WebElement getHideAdvancedSearchButton() {
        return this.searchBarPage.getHideAdvancedSearchButton();
    }

    @Override
    public void hideAdvancedSearchPanel() {
        this.searchBarPage.hideAdvancedSearchPanel();
    }

}
