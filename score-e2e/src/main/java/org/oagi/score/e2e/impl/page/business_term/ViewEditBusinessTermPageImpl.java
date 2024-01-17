package org.oagi.score.e2e.impl.page.business_term;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.BusinessTermObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.business_term.CreateBusinessTermPage;
import org.oagi.score.e2e.page.business_term.EditBusinessTermPage;
import org.oagi.score.e2e.page.business_term.UploadBusinessTermsPage;
import org.oagi.score.e2e.page.business_term.ViewEditBusinessTermPage;
import org.openqa.selenium.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class ViewEditBusinessTermPageImpl extends BasePageImpl implements ViewEditBusinessTermPage {

    private static final By UPDATER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Updater\")]//ancestor::div[1]/mat-select[1]");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");

    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Updated start date\")]");

    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Updated end date\")]");

    private static final By TERM_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Term\")]//ancestor::div[1]/input");

    private static final By EXTERNAL_REFERENCE_URI_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"External Reference URI\")]//ancestor::div[1]/input");

    private static final By EXTERNAL_REFERENCE_ID_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"External Reference ID\")]//ancestor::div[1]/input");

    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Search\")]//ancestor::button[1]");
    private static final By NEW_BUSINESS_TERM_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"New Business Term\")]//ancestor::button[1]");

    private static final By UPLOAD_BUSINESS_TERMS_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Upload Business Terms\")]//ancestor::button[1]");

    private static final By DISCARD_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Discard\")]//ancestor::button[1]");


    public ViewEditBusinessTermPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/business_term_management/business_term").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Business Term".equals(getText(getTitle()));
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
    public WebElement getTermField() {
        return visibilityOfElementLocated(getDriver(), TERM_FIELD_LOCATOR);
    }

    @Override
    public void setTerm(String termName) {
        sendKeys(getTermField(), termName);
    }

    @Override
    public WebElement getExternalReferenceURIField() {
        return visibilityOfElementLocated(getDriver(), EXTERNAL_REFERENCE_URI_FIELD_LOCATOR);
    }

    @Override
    public void setExternalReferenceURI(String externalReferenceURI) {
        sendKeys(getExternalReferenceURIField(), externalReferenceURI);
    }

    @Override
    public WebElement getExternalReferenceIDField() {
        return visibilityOfElementLocated(getDriver(), EXTERNAL_REFERENCE_ID_FIELD_LOCATOR);
    }

    @Override
    public void setExternalReferenceID(String externalReferenceID) {
        sendKeys(getExternalReferenceIDField(), externalReferenceID);
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
    public WebElement getSelectCheckboxAtIndex(int idx) {
        WebElement tr = getTableRecordAtIndex(idx);
        WebElement td = getColumnByName(tr, "select");
        return td.findElement(By.xpath("mat-checkbox"));
    }

    @Override
    public int getTotalNumberOfItems() {
        WebElement paginatorRangeLabelElement = visibilityOfElementLocated(getDriver(),
                By.xpath("//div[@class = \"mat-mdc-paginator-range-label\"]"));
        String paginatorRangeLabel = getText(paginatorRangeLabelElement);
        return Integer.valueOf(paginatorRangeLabel.substring(paginatorRangeLabel.indexOf("of") + 2).trim());
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
    public WebElement getNewBusinessTermButton() {
        return elementToBeClickable(getDriver(), NEW_BUSINESS_TERM_BUTTON_LOCATOR);
    }

    @Override
    public void hitNewBusinessTermButton() {
        click(getNewBusinessTermButton());
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public WebElement getUploadBusinessTermsButton() {
        return elementToBeClickable(getDriver(), UPLOAD_BUSINESS_TERMS_BUTTON_LOCATOR);
    }

    @Override
    public UploadBusinessTermsPage hitUploadBusinessTermsButton() {
        click(getUploadBusinessTermsButton());
        waitFor(Duration.ofMillis(500L));

        UploadBusinessTermsPage uploadBusinessTermsPage = new UploadBusinessTermsPageImpl(this);
        assert uploadBusinessTermsPage.isOpened();
        return uploadBusinessTermsPage;
    }

    @Override
    public WebElement getDiscardButton() {
        return elementToBeClickable(getDriver(), DISCARD_BUTTON_LOCATOR);
    }

    @Override
    public CreateBusinessTermPage openCreateBusinessTermPage() {
        click(getNewBusinessTermButton());
        waitFor(Duration.ofMillis(500L));

        CreateBusinessTermPage createBusinessTermPage = new CreateBusinessTermPageImpl(this);
        assert createBusinessTermPage.isOpened();
        return createBusinessTermPage;
    }

    @Override
    public EditBusinessTermPage openEditBusinessTermPageByTerm(String businessTermName) throws NoSuchElementException {
        setTerm(businessTermName);
        hitSearchButton();

        return retry(() -> {
            WebElement td;
            try {
                WebElement tr = getTableRecordAtIndex(1);
                td = getColumnByName(tr, "businessTerm");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a business term using " + businessTermName, e);
            }
            if (!businessTermName.equals(getText(td.findElement(By.cssSelector("a > span"))))) {
                throw new NoSuchElementException("Cannot locate a business term using " + businessTermName);
            }
            WebElement tdName = td.findElement(By.tagName("a"));
            click(tdName);
            waitFor(Duration.ofMillis(500L));

            BusinessTermObject businessTerm =
                    getAPIFactory().getBusinessTermAPI().getBusinessTermByName(businessTermName);
            EditBusinessTermPage editBusinessTermPage =
                    new EditBusinessTermPageImpl(this, businessTerm);
            assert editBusinessTermPage.isOpened();
            return editBusinessTermPage;
        });
    }

}
