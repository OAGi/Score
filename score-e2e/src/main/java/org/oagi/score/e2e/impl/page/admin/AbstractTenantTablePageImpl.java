package org.oagi.score.e2e.impl.page.admin;

import org.oagi.score.e2e.impl.page.BaseSearchBarPageImpl;
import org.oagi.score.e2e.page.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

/**
 * What the pages of the tenant management have in common: a searchable table that is read by the text of one column.
 */
abstract class AbstractTenantTablePageImpl extends BaseSearchBarPageImpl {

    AbstractTenantTablePageImpl(BasePage parent) {
        super(parent);
    }

    /** The class of the column, e.g. {@code mat-column-name}, whose first span is the key of a row. */
    protected abstract String getKeyColumnClass();

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    public void search(String text) {
        WebElement input = getInputFieldInSearchBar();
        clear(input);
        sendKeys(input, text);
        retry(() -> {
            click(getSearchButton());
            waitFor(ofMillis(500L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
    }

    private String rowXPath(String key) {
        return "//tbody/tr[td[contains(@class, \"" + getKeyColumnClass() + "\")]"
                + "//*[self::span or self::a][normalize-space(.) = \"" + key + "\"]]";
    }

    protected WebElement buttonOfRow(String key, String buttonText) {
        return elementToBeClickable(getDriver(), By.xpath(rowXPath(key) + "//button[contains(., \"" + buttonText + "\")]"));
    }

    protected List<String> readKeys() {
        return retry(() -> getDriver().findElements(By.xpath("//tbody/tr/td[contains(@class, \""
                        + getKeyColumnClass() + "\")]//span[1]")).stream()
                .map(e -> e.getText().trim())
                .collect(Collectors.toList()));
    }

    /** Wait for the table to load and return the keys of its rows, once two reads in a row agree. */
    protected List<String> getStableKeys() {
        invisibilityOfLoadingContainerElement(getDriver());
        List<String> keys = readKeys();
        for (int i = 0; i < 5; i++) {
            waitFor(ofMillis(500L));
            invisibilityOfLoadingContainerElement(getDriver());
            List<String> again = readKeys();
            if (again.equals(keys)) {
                return keys;
            }
            keys = again;
        }
        return keys;
    }

}
