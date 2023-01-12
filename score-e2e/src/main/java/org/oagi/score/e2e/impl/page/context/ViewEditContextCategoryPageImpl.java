package org.oagi.score.e2e.impl.page.context;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.ContextCategoryObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.context.CreateContextCategoryPage;
import org.oagi.score.e2e.page.context.EditContextCategoryPage;
import org.oagi.score.e2e.page.context.ViewEditContextCategoryPage;
import org.openqa.selenium.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class ViewEditContextCategoryPageImpl extends BasePageImpl implements ViewEditContextCategoryPage {

    private static final By UPDATER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Updater\")]//ancestor::div[1]/mat-select[1]");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");

    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@data-placeholder, \"Updated start date\")]");

    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@data-placeholder, \"Updated end date\")]");

    private static final By NAME_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Name\")]//ancestor::div[1]/input");

    private static final By DESCRIPTION_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Description\")]//ancestor::div[1]/input");

    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Search\")]//ancestor::button[1]");

    private static final By NEW_CONTEXT_CATEGORY_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"New Context Category\")]//ancestor::button[1]");

    private static final By DISCARD_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Discard\")]//ancestor::button[1]");

    public ViewEditContextCategoryPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/context_management/context_category").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Context Category".equals(getText(getTitle()));
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
        return visibilityOfElementLocated(getDriver(), NAME_FIELD_LOCATOR);
    }

    @Override
    public void setName(String name) {
        sendKeys(getNameField(), name);
    }

    @Override
    public WebElement getDescriptionField() {
        return visibilityOfElementLocated(getDriver(), DESCRIPTION_FIELD_LOCATOR);
    }

    @Override
    public void setDescription(String description) {
        sendKeys(getDescriptionField(), description);
    }

    @Override
    public WebElement getSearchButton() {
        return elementToBeClickable(getDriver(), SEARCH_BUTTON_LOCATOR);
    }

    @Override
    public void hitSearchButton() {
        retry(() -> click(getSearchButton()));
    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//tbody/tr[" + idx + "]"));
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
    public WebElement getNewContextCategoryButton() {
        return elementToBeClickable(getDriver(), NEW_CONTEXT_CATEGORY_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getDiscardButton() {
        return elementToBeClickable(getDriver(), DISCARD_BUTTON_LOCATOR);
    }

    @Override
    public CreateContextCategoryPage openCreateContextCategoryPage() {
        click(getNewContextCategoryButton());
        CreateContextCategoryPage createContextCategoryPage = new CreateContextCategoryPageImpl(this);
        assert createContextCategoryPage.isOpened();
        return createContextCategoryPage;
    }

    @Override
    public EditContextCategoryPage openEditContextCategoryPageByContextCategoryName(String contextCategoryName) throws NoSuchElementException {
        setName(contextCategoryName);
        hitSearchButton();

        return retry(() -> {
            WebElement td;
            try {
                WebElement tr = getTableRecordAtIndex(1);
                td = getColumnByName(tr, "name");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a context category using " + contextCategoryName, e);
            }
            if (!contextCategoryName.equals(getText(td.findElement(By.cssSelector("a > span"))))) {
                throw new NoSuchElementException("Cannot locate a context category using " + contextCategoryName);
            }
            WebElement tdName = td.findElement(By.tagName("a"));
            click(tdName);

            ContextCategoryObject contextCategory =
                    getAPIFactory().getContextCategoryAPI().getContextCategoryByName(contextCategoryName);
            EditContextCategoryPage editContextCategoryPage =
                    new EditContextCategoryPageImpl(this, contextCategory);
            assert editContextCategoryPage.isOpened();
            return editContextCategoryPage;
        });
    }

}
