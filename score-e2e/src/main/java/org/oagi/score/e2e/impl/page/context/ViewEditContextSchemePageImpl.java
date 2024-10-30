package org.oagi.score.e2e.impl.page.context;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.BaseSearchBarPageImpl;
import org.oagi.score.e2e.obj.ContextSchemeObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.context.CreateContextSchemePage;
import org.oagi.score.e2e.page.context.EditContextSchemePage;
import org.oagi.score.e2e.page.context.ViewEditContextSchemePage;
import org.openqa.selenium.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class ViewEditContextSchemePageImpl extends BaseSearchBarPageImpl implements ViewEditContextSchemePage {

    private static final By UPDATER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Updater\")]//ancestor::div[1]/mat-select[1]");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");

    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Updated start date\")]");

    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Updated end date\")]");

    private static final By NEW_CONTEXT_SCHEME_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"New Context Scheme\")]//ancestor::button[1]");

    private static final By DISCARD_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Discard\")]//ancestor::button[1]");

    public ViewEditContextSchemePageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/context_management/context_scheme").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Context Scheme".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
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
    public WebElement getNameField() {
        return getInputFieldInSearchBar();
    }

    @Override
    public void setName(String name) {
        sendKeys(getNameField(), name);
    }

    @Override
    public void hitSearchButton() {
        retry(() -> {
            click(getSearchButton());
            waitFor(ofMillis(1000L));
        });
    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//tbody/tr[" + idx + "]"));
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//tbody//*[contains(text(), \"" + value + "\")]/ancestor::tr"));
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    @Override
    public void goToNextPage() {
        ((JavascriptExecutor) getDriver())
                .executeScript("window.scrollTo(0, document.body.scrollHeight)");
        click(elementToBeClickable(getDriver(), By.xpath("//button[@aria-label='Next page']")));
    }

    @Override
    public void goToPreviousPage() {
        ((JavascriptExecutor) getDriver())
                .executeScript("window.scrollTo(0, document.body.scrollHeight)");
        click(elementToBeClickable(getDriver(), By.xpath("//button[@aria-label='Previous page']")));
    }

    @Override
    public WebElement getNewContextSchemeButton() {
        return elementToBeClickable(getDriver(), NEW_CONTEXT_SCHEME_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getDiscardButton() {
        return elementToBeClickable(getDriver(), DISCARD_BUTTON_LOCATOR);
    }

    @Override
    public CreateContextSchemePage openCreateContextSchemePage() {
        click(getNewContextSchemeButton());
        CreateContextSchemePage createContextSchemePage = new CreateContextSchemePageImpl(this);
        assert createContextSchemePage.isOpened();
        return createContextSchemePage;
    }

    @Override
    public EditContextSchemePage openEditContextSchemePageByContextSchemeName(String contextSchemeName) throws NoSuchElementException {
        setName(contextSchemeName);

        return retry(() -> {
            hitSearchButton();

            WebElement td;
            try {
                WebElement tr = getTableRecordAtIndex(1);
                td = getColumnByName(tr, "schemeName");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a context scheme using " + contextSchemeName, e);
            }
            if (!contextSchemeName.equals(getText(td.findElement(By.cssSelector("a > span"))))) {
                throw new NoSuchElementException("Cannot locate a context scheme using " + contextSchemeName);
            }
            WebElement tdName = td.findElement(By.tagName("a"));
            click(tdName);
            waitFor(ofMillis(2000L));

            ContextSchemeObject contextScheme =
                    getAPIFactory().getContextSchemeAPI().getContextSchemeByName(contextSchemeName);
            EditContextSchemePage editContextSchemePage =
                    new EditContextSchemePageImpl(this, contextScheme);
            assert editContextSchemePage.isOpened();
            return editContextSchemePage;
        });
    }
}
