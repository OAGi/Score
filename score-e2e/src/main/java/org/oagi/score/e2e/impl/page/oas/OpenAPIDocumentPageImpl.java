package org.oagi.score.e2e.impl.page.oas;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.OpenAPIDocumentObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.oas.CreateOpenAPIDocumentPage;
import org.oagi.score.e2e.page.oas.EditOpenAPIDocumentPage;
import org.oagi.score.e2e.page.oas.OpenAPIDocumentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class OpenAPIDocumentPageImpl extends BasePageImpl implements OpenAPIDocumentPage {

    private static final By UPDATER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Updater\")]//ancestor::div[1]/mat-select[1]");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");

    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Updated start date\")]");

    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Updated end date\")]");

    private static final By TITLE_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Title\")]//ancestor::mat-form-field//input");

    private static final By DESCRIPTION_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Description\")]//ancestor::mat-form-field//input");

    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Search\")]//ancestor::button[1]");

    private static final By NEW_OPENAPI_DOCUMENT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"New OpenAPI Document\")]//ancestor::button[1]");

    private static final By DISCARD_BUTTON_LOCATOR =
            By.xpath("//mat-icon[contains(text(), \"delete\")]//ancestor::button[1]");

    public OpenAPIDocumentPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/profile_bie/express/oas_doc").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "OpenAPI Document".equals(getText(getTitle()));
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
    public WebElement getTitleField() {
        return visibilityOfElementLocated(getDriver(), TITLE_FIELD_LOCATOR);
    }

    @Override
    public void setTitle(String title) {
        sendKeys(getTitleField(), title);
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
        click(getSearchButton());
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//tbody/tr[" + idx + "]"));
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//td//span[contains(text(), \"" + value + "\")]/ancestor::tr"));
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    @Override
    public void setItemsPerPage(int items) {
        WebElement itemsPerPageField = elementToBeClickable(getDriver(),
                By.xpath("//div[.=\" Items per page: \"]/following::div[5]"));
        click(itemsPerPageField);
        waitFor(ofMillis(500L));
        WebElement itemField = elementToBeClickable(getDriver(),
                By.xpath("//span[contains(text(), \"" + items + "\")]//ancestor::mat-option//div[1]//preceding-sibling::span"));
        click(itemField);
        waitFor(ofMillis(500L));
    }

    @Override
    public int getTotalNumberOfItems() {
        WebElement paginatorRangeLabelElement = visibilityOfElementLocated(getDriver(),
                By.xpath("//div[@class = \"mat-mdc-paginator-range-label\"]"));
        String paginatorRangeLabel = getText(paginatorRangeLabelElement);
        return Integer.valueOf(paginatorRangeLabel.substring(paginatorRangeLabel.indexOf("of") + 2).trim());
    }

    @Override
    public WebElement getPreviousPageButton() {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//div[contains(@class, \"mat-mdc-paginator-range-actions\")]" +
                        "//button[@aria-label = \"Previous page\"]"));
    }

    @Override
    public WebElement getNextPageButton() {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//div[contains(@class, \"mat-mdc-paginator-range-actions\")]" +
                        "//button[@aria-label = \"Next page\"]"));
    }

    @Override
    public WebElement getNewOpenAPIDocumentButton() {
        return elementToBeClickable(getDriver(), NEW_OPENAPI_DOCUMENT_BUTTON_LOCATOR);
    }

    @Override
    public CreateOpenAPIDocumentPage openCreateOpenAPIDocumentPage() {
        click(getNewOpenAPIDocumentButton());
        waitFor(ofMillis(500L));

        return retry(() -> {
            CreateOpenAPIDocumentPage createOpenAPIDocumentPage =
                    new CreateOpenAPIDocumentPageImpl(this);
            assert createOpenAPIDocumentPage.isOpened();
            return createOpenAPIDocumentPage;
        });
    }

    @Override
    public EditOpenAPIDocumentPage openEditOpenAPIDocumentPage(OpenAPIDocumentObject openAPIDocument) {
        setTitle(openAPIDocument.getTitle());
        hitSearchButton();

        return retry(() -> {
            WebElement td;
            try {
                WebElement tr = getTableRecordAtIndex(1);
                td = getColumnByName(tr, "title");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate an OpenAPI document using " + openAPIDocument.getTitle(), e);
            }
            if (!openAPIDocument.getTitle().equals(getText(td.findElement(By.cssSelector("a > span"))))) {
                throw new NoSuchElementException("Cannot locate an OpenAPI document using " + openAPIDocument.getTitle());
            }
            WebElement tdName = td.findElement(By.tagName("a"));
            click(tdName);
            waitFor(ofMillis(500L));
            invisibilityOfLoadingContainerElement(getDriver());

            EditOpenAPIDocumentPage editOpenAPIDocumentPage = new EditOpenAPIDocumentPageImpl(this, openAPIDocument);
            try {
                assert editOpenAPIDocumentPage.isOpened();
            } catch (AssertionError e) {
                editOpenAPIDocumentPage.openPage();
            }
            return editOpenAPIDocumentPage;
        });
    }

    @Override
    public WebElement getDiscardButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), DISCARD_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), DISCARD_BUTTON_LOCATOR);
        }
    }

    @Override
    public void discard(OpenAPIDocumentObject openAPIDocument) {
        setTitle(openAPIDocument.getTitle());
        hitSearchButton();

        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));

        WebElement tr = getTableRecordByValue(openAPIDocument.getTitle());
        WebElement td = getColumnByName(tr, "select");
        click(td);
        click(getDiscardButton(true));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button[1]")));

        assert "Discarded".equals(getSnackBarMessage(getDriver()));
    }

}
