package org.oagi.score.e2e.impl.page.bie;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.bie.CreateBIEForSelectBusinessContextsPage;
import org.oagi.score.e2e.page.bie.CreateBIEForSelectTopLevelConceptPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class CreateBIEForSelectBusinessContextsPageImpl extends BasePageImpl implements CreateBIEForSelectBusinessContextsPage {

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

    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Search\")]//ancestor::button[1]");

    private static final By NEXT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Next\")]//ancestor::button[1]");

    private static final By CREATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Create\")]");

    public CreateBIEForSelectBusinessContextsPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/profile_bie/create").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert getText(getTitle()).contains("Create BIE");
        assert getText(getSubtitle()).contains("Select Business Contexts");
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-mdc-card-title"));
    }

    @Override
    public WebElement getSubtitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-mdc-card-subtitle"));
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
    public WebElement getSearchButton() {
        return elementToBeClickable(getDriver(), SEARCH_BUTTON_LOCATOR);
    }

    @Override
    public void hitSearchButton() {
        retry(() -> {
            click(getSearchButton());
            waitFor(ofMillis(500L));
        });
    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//tbody/tr[" + idx + "]"));
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//td/div/a/span[contains(text(), \"" + value + "\")]/ancestor::tr"));
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    @Override
    public void selectBusinessContext(BusinessContextObject businessContext) {
        sendKeys(getNameField(), businessContext.getName());

        retry(() -> {
            hitSearchButton();

            WebElement tr = getTableRecordByValue(businessContext.getName());
            WebElement td = getColumnByName(tr, "select");
            click(td.findElement(By.xpath("mat-checkbox/label/span[1]")));
        });
    }

    @Override
    public WebElement getNextButton() {
        return elementToBeClickable(getDriver(), NEXT_BUTTON_LOCATOR);
    }

    @Override
    public WebElement getCreateButton() {
        return elementToBeClickable(getDriver(), CREATE_BUTTON_LOCATOR);
    }

    @Override
    public CreateBIEForSelectTopLevelConceptPage next(List<BusinessContextObject> businessContexts) {
        for (BusinessContextObject businessContext : businessContexts) {
            selectBusinessContext(businessContext);
        }
        click(getNextButton());
        waitFor(ofMillis(2000L));

        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage =
                new CreateBIEForSelectTopLevelConceptPageImpl(this, businessContexts);
        assert createBIEForSelectTopLevelConceptPage.isOpened();
        return createBIEForSelectTopLevelConceptPage;
    }

}
