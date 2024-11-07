package org.oagi.score.e2e.page;

import org.openqa.selenium.WebElement;

public interface SearchBarPage {

    /**
     * Return the UI element of 'Search' button.
     *
     * @return the UI element of 'Search' button
     */
    WebElement getSearchButton();

    /**
     * Return the UI element of the input field in the search bar.
     *
     * @return the UI element of the input field in the search bar
     */
    WebElement getInputFieldInSearchBar();

    /**
     * Return the UI element of 'Show Advanced Search' button.
     *
     * @return the UI element of 'Show Advanced Search' button
     */
    WebElement getShowAdvancedSearchButton();

    /**
     * Show 'Advanced Search' panel.
     */
    void showAdvancedSearchPanel();

    /**
     * Return the UI element of 'Hide Advanced Search' button.
     *
     * @return the UI element of 'Hide Advanced Search' button
     */
    WebElement getHideAdvancedSearchButton();

    /**
     * Hide 'Advanced Search' panel.
     */
    void hideAdvancedSearchPanel();

}
