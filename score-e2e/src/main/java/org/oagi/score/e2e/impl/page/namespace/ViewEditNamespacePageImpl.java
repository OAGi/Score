package org.oagi.score.e2e.impl.page.namespace;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.namespace.ViewEditNamespacePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class ViewEditNamespacePageImpl extends BasePageImpl implements ViewEditNamespacePage {

    private static final By OWNER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Owner\")]//ancestor::div[1]/mat-select[1]");

    private static final By STANDARD_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Standard\")]//ancestor::div[1]/mat-select[1]");

    private static final By UPDATER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Updater\")]//ancestor::div[1]/mat-select[1]");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");

    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@data-placeholder, \"Updated start date\")]");

    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@data-placeholder, \"Updated end date\")]");

    private static final By URI_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"URI\")]//ancestor::div[1]/input");

    private static final By PREFIX_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Prefix\")]//ancestor::div[1]/input");
    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Search\")]//ancestor::button[1]");
    private static final By NEW_NAMESPACE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"New Namespace\")]//ancestor::button[1]");

    public ViewEditNamespacePageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/namespace").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Namespace".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public WebElement getOwerSelectField() {
        return visibilityOfElementLocated(getDriver(), OWNER_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setOwner(String owner) {
        click(getOwerSelectField());
        sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), owner);
        WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[contains(text(), \"" + owner + "\")]"));
        click(searchedSelectField);
        escape(getDriver());
    }

    @Override
    public WebElement getStandardSelectField() {
        return visibilityOfElementLocated(getDriver(), STANDARD_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void toggleStandard(boolean isStandard) {
        click(getStandardSelectField());
        WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[contains(text(), \"" + isStandard + "\")]"));
        click(searchedSelectField);
        escape(getDriver());
    }

    @Override
    public WebElement getURIField() {
        return visibilityOfElementLocated(getDriver(), URI_FIELD_LOCATOR);
    }

    @Override
    public void setURI(String uri) {
        sendKeys(getURIField(), uri);
    }

    @Override
    public WebElement getPrefixField() {
        return visibilityOfElementLocated(getDriver(), PREFIX_FIELD_LOCATOR);
    }

    @Override
    public void setPrefix(String prefix) {
        sendKeys(getPrefixField(), prefix);
    }

    @Override
    public WebElement getUpdaterSelectField() {
        return visibilityOfElementLocated(getDriver(), UPDATER_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setUpdater(String updater) {
        click(getUpdaterSelectField());
        sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), updater);
        WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[contains(text(), \"" + updater + "\")]"));
        click(searchedSelectField);
        escape(getDriver());
    }

    @Override
    public WebElement getUpdatedStartDateField() {
        return visibilityOfElementLocated(getDriver(), UPDATED_START_DATE_FIELD_LOCATOR);
    }

    @Override
    public void setUpdatedStartDate(LocalDateTime updatedStartDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        sendKeys(getUpdatedStartDateField(), formatter.format(updatedStartDate));
    }

    @Override
    public WebElement getUpdatedEndDateField() {
        return visibilityOfElementLocated(getDriver(), UPDATED_END_DATE_FIELD_LOCATOR);
    }

    @Override
    public void setUpdatedEndDate(LocalDateTime updatedEndDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        sendKeys(getUpdatedEndDateField(), formatter.format(updatedEndDate));
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        defaultWait(getDriver());
        return visibilityOfElementLocated(getDriver(), By.xpath("//*[contains(text(),\"" + value + "\")]//ancestor::tr"));
    }

    @Override
    public void hitSearchButton() {
        retry(() -> {
            click(getSearchButton());
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public WebElement getSearchButton() {
        return elementToBeClickable(getDriver(), SEARCH_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getNewNamespaceButton() {
        return elementToBeClickable(getDriver(), NEW_NAMESPACE_BUTTON_LOCATOR);
    }
}
