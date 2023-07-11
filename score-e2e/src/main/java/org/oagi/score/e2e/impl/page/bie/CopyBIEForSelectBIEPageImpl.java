package org.oagi.score.e2e.impl.page.bie;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.bie.CopyBIEForSelectBIEPage;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class CopyBIEForSelectBIEPageImpl extends BasePageImpl implements CopyBIEForSelectBIEPage {

    private static final By BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Branch\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");

    private static final By OWNER_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Owner\")]//ancestor::div[1]/mat-select[1]");

    private static final By UPDATER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Updater\")]//ancestor::div[1]/mat-select[1]");

    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@data-placeholder, \"Updated start date\")]");

    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@data-placeholder, \"Updated end date\")]");

    private static final By DEN_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"DEN\")]//ancestor::div[1]/input");

    private static final By BUSINESS_CONTEXT_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Business Context\")]//ancestor::mat-form-field//input");

    private static final By STATE_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"State\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");

    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Search\")]//ancestor::button[1]");

    private static final By COPY_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Copy\")]//ancestor::button[1]");

    private final List<BusinessContextObject> selectedBusinessContexts;

    public CopyBIEForSelectBIEPageImpl(BasePage parent, List<BusinessContextObject> selectedBusinessContexts) {
        super(parent);
        this.selectedBusinessContexts = selectedBusinessContexts;
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert getText(getTitle()).contains("Copy BIE");
        assert getText(getSubtitle()).contains("Select BIE");
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-card-title"));
    }

    @Override
    protected String getPageUrl() {
        String selectedBusinessContextIdList = this.selectedBusinessContexts.stream()
                .map(e -> e.getBusinessContextId().toString()).collect(Collectors.joining(","));
        return getConfig().getBaseUrl().resolve("/profile_bie/copy/bie?bizCtxIds=" + selectedBusinessContextIdList).toString();
    }

    @Override
    public WebElement getSubtitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-card-subtitle"));
    }

    @Override
    public WebElement getBranchSelectField() {
        return visibilityOfElementLocated(getDriver(), BRANCH_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setBranch(String branch) {
        retry(() -> {
            click(getBranchSelectField());
            sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), branch);
            WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//mat-option//span[contains(text(), \"" + branch + "\")]"));
            click(searchedSelectField);
            escape(getDriver());
        });
    }

    @Override
    public WebElement getOwnerSelectField() {
        return visibilityOfElementLocated(getDriver(), OWNER_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setOwner(String owner) {
        retry(() -> {
            click(getOwnerSelectField());
            sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), owner);
            WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//mat-option//span[contains(text(), \"" + owner + "\")]"));
            click(searchedSelectField);
            escape(getDriver());
        });
    }

    @Override
    public WebElement getUpdaterSelectField() {
        return visibilityOfElementLocated(getDriver(), UPDATER_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setUpdater(String updater) {
        retry(() -> {
            click(getUpdaterSelectField());
            sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), updater);
            WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//mat-option//span[contains(text(), \"" + updater + "\")]"));
            click(searchedSelectField);
            escape(getDriver());
        });
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
    public WebElement getDENField() {
        return visibilityOfElementLocated(getDriver(), DEN_FIELD_LOCATOR);
    }

    @Override
    public void setDEN(String den) {
        sendKeys(getDENField(), den);
    }

    @Override
    public WebElement getBusinessContextField() {
        return visibilityOfElementLocated(getDriver(), BUSINESS_CONTEXT_FIELD_LOCATOR);
    }

    @Override
    public void setBusinessContext(String businessContext) {
        sendKeys(getBusinessContextField(), businessContext);
    }

    @Override
    public WebElement getStateSelectField() {
        return visibilityOfElementLocated(getDriver(), STATE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setState(String state) {
        click(getStateSelectField());
        WebElement optionField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[contains(text(), \"" + state + "\")]"));
        click(optionField);
        escape(getDriver());
    }

    @Override
    public WebElement getSearchButton() {
        return elementToBeClickable(getDriver(), SEARCH_BUTTON_LOCATOR);
    }

    @Override
    public void hitSearchButton() {
        retry(() -> click(getSearchButton()));
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
        waitFor(Duration.ofMillis(500L));
        WebElement itemField = elementToBeClickable(getDriver(),
                By.xpath("//span[contains(text(), \"" + items + "\")]//ancestor::mat-option//div[1]//preceding-sibling::span"));
        click(itemField);
        waitFor(Duration.ofMillis(500L));
    }

    @Override
    public int getTotalNumberOfItems() {
        WebElement paginatorRangeLabelElement = visibilityOfElementLocated(getDriver(),
                By.xpath("//div[@class = \"mat-paginator-range-label\"]"));
        String paginatorRangeLabel = getText(paginatorRangeLabelElement);
        return Integer.valueOf(paginatorRangeLabel.substring(paginatorRangeLabel.indexOf("of") + 2).trim());
    }

    @Override
    public WebElement getCopyButton() {
        return elementToBeClickable(getDriver(), COPY_BUTTON_LOCATOR);
    }

    @Override
    public ViewEditBIEPage copyBIE(String asccpDEN, String branch) {
        setDEN(asccpDEN);
        setBranch(branch);
        hitSearchButton();

        return retry(() -> {
            WebElement tr;
            WebElement td;
            try {
                tr = getTableRecordAtIndex(1);
                td = getColumnByName(tr, "den");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a BIE using " + asccpDEN, e);
            }
            if (!asccpDEN.equals(getText(td.findElement(By.tagName("a"))))) {
                throw new NoSuchElementException("Cannot locate a BIE using " + asccpDEN);
            }
            WebElement select = getColumnByName(tr, "select");
            click(select);
            click(getCopyButton());
            assert "Copying request queued".equals(getSnackBarMessage(getDriver()));

            ViewEditBIEPage viewEditBIEPage = new ViewEditBIEPageImpl(this);
            assert viewEditBIEPage.isOpened();
            return viewEditBIEPage;
        });
    }

}
