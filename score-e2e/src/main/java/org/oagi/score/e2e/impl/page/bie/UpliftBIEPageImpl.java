package org.oagi.score.e2e.impl.page.bie;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.bie.UpliftBIEPage;
import org.oagi.score.e2e.page.bie.UpliftBIEVerificationPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class UpliftBIEPageImpl extends BasePageImpl implements UpliftBIEPage {
    private static final By SOURCE_BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Source Branch\")]//ancestor::mat-form-field[1]//mat-select//div[contains(@class, \"mat-select-arrow-wrapper\")]");

    private static final By TARGET_BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Target Branch\")]//ancestor::mat-form-field[1]//mat-select//div[contains(@class, \"mat-select-arrow-wrapper\")]");

    private static final By STATE_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"State\")]//ancestor::mat-form-field[1]//mat-select//div[contains(@class, \"mat-select-arrow-wrapper\")]");

    private static final By OWNER_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Owner\")]//ancestor::div[1]/mat-select[1]");

    private static final By UPDATER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Updater\")]//ancestor::div[1]/mat-select[1]");

    private static final By PROPERTY_TERM_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Property Term\")]//ancestor::mat-form-field//input");

    private static final By BUSINESS_CONTEXT_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Business Context\")]//ancestor::mat-form-field//input");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");

    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@data-placeholder, \"Updated start date\")]");

    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@data-placeholder, \"Updated end date\")]");

    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Search\")]//ancestor::button[1]");
    private static final By NEXT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Next\")]//ancestor::button[1]");

    public UpliftBIEPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/profile_bie/uplift").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Uplift BIE".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-card-title"));
    }

    @Override
    public WebElement getSourceBranchSelectField() {
        return visibilityOfElementLocated(getDriver(), SOURCE_BRANCH_SELECT_FIELD_LOCATOR);
    }
    @Override
    public void setSourceBranch(String sourceBranch) {
        click(getSourceBranchSelectField());
        WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[text() = \"" + sourceBranch + "\"]"));
        click(searchedSelectField);
        escape(getDriver());
    }

    @Override
    public WebElement getTargetBranchSelectField() {
        return visibilityOfElementLocated(getDriver(), TARGET_BRANCH_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setTargetBranch(String targetBranch) {
        click(getTargetBranchSelectField());
        WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[text() = \"" + targetBranch + "\"]"));
        click(searchedSelectField);
        escape(getDriver());
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
    public WebElement getOwnerSelectField() {
        return visibilityOfElementLocated(getDriver(), OWNER_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setOwner(String owner) {
        click(getOwnerSelectField());
        sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), owner);
        WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[contains(text(), \"" + owner + "\")]"));
        click(searchedSelectField);
        escape(getDriver());

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
    public WebElement getPropertyTermField() {
        return visibilityOfElementLocated(getDriver(), PROPERTY_TERM_FIELD_LOCATOR);
    }

    @Override
    public void setPropertyTerm(String propertyTerm) {
        sendKeys(getPropertyTermField(), propertyTerm);
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
    public WebElement getNextButton() {
        return elementToBeClickable(getDriver(), NEXT_BUTTON_LOCATOR);
    }

    @Override
    public UpliftBIEVerificationPage Next() {
        click(getNextButton());
        waitFor(Duration.ofSeconds(12000));
        new WebDriverWait(getDriver(), Duration.ofSeconds(10)).until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        UpliftBIEVerificationPage upliftBIEVerificationPage = new UpliftBIEVerificationPageImpl(this);
        assert upliftBIEVerificationPage.isOpened();
        return upliftBIEVerificationPage;
    }
}
