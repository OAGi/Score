package org.oagi.score.e2e.impl.page.agency_id_list;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.AgencyIDListObject;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.agency_id_list.EditAgencyIDListPage;
import org.oagi.score.e2e.page.agency_id_list.ViewEditAgencyIDListPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class ViewEditAgencyIDListPageImpl extends BasePageImpl implements ViewEditAgencyIDListPage {

    private static final By NEW_AGENCY_ID_LIST_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"New Agency ID List\")]//ancestor::button[1]");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");

    private static final By BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Branch\")]//ancestor::mat-mdc-form-field[1]//mat-select//div[contains(@class, \"mat-select-arrow-wrapper\")]");

    private static final By STATE_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"State\")]//ancestor::mat-mdc-form-field[1]//mat-select//div[contains(@class, \"mat-select-arrow-wrapper\")]");

    private static final By DEPRECATED_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Deprecated\")]//ancestor::mat-mdc-form-field[1]//mat-select//div[contains(@class, \"mat-select-arrow-wrapper\")]");

    private static final By OWNER_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Owner\")]//ancestor::div[1]/mat-select[1]");

    private static final By UPDATER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Updater\")]//ancestor::div[1]/mat-select[1]");

    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@data-placeholder, \"Updated start date\")]");

    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@data-placeholder, \"Updated end date\")]");

    private static final By NAME_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Name\")]//ancestor::mat-mdc-form-field//input");

    private static final By DEFINITION_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Definition\")]//ancestor::mat-mdc-form-field//input");

    private static final By MODULE_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Module\")]//ancestor::mat-mdc-form-field//input");

    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Search\")]//ancestor::button[1]");

    public ViewEditAgencyIDListPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/agency_id_list").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Agency ID List".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public EditAgencyIDListPage openNewAgencyIDList(AppUserObject user, String release) {
        setBranch(release);
        retry(() -> {
            click(getNewAgencyIDListButton());
            waitFor(ofMillis(1000L));
        });

        invisibilityOfLoadingContainerElement(getDriver());
        AgencyIDListObject agencyIDList = getAPIFactory().getAgencyIDListAPI().getNewlyCreatedAgencyIDList(user, release);
        EditAgencyIDListPage editAgencyIDListPage = new EditAgencyIDListPageImpl(this, agencyIDList);
        assert editAgencyIDListPage.isOpened();
        return editAgencyIDListPage;
    }

    @Override
    public WebElement getNewAgencyIDListButton() {
        return elementToBeClickable(getDriver(), NEW_AGENCY_ID_LIST_BUTTON_LOCATOR);
    }

    @Override
    public EditAgencyIDListPage openEditAgencyIDListPage(AgencyIDListObject agencyIDList) {
        EditAgencyIDListPage editAgencyIDListPage = new EditAgencyIDListPageImpl(this, agencyIDList);
        editAgencyIDListPage.openPage();
        assert editAgencyIDListPage.isOpened();
        return editAgencyIDListPage;
    }

    @Override
    public EditAgencyIDListPage openEditAgencyIDListPageByNameAndBranch(String name, String branch) {
        setBranch(branch);
        setName(name);
        hitSearchButton();

        return retry(() -> {
            WebElement tr;
            WebElement td;
            try {
                tr = getTableRecordAtIndex(1);
                td = getColumnByName(tr, "name");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate an agency ID list using " + name, e);
            }
            if (!name.equals(getText(td.findElement(By.tagName("a"))))) {
                throw new NoSuchElementException("Cannot locate an agency ID list using " + name);
            }

            WebElement tdLoginID = td.findElement(By.cssSelector("a"));
            // TODO:
            // 'click' does not work when the browser hides the link.
            getDriver().get(tdLoginID.getAttribute("href"));

            String currentUrl = getDriver().getCurrentUrl();
            BigInteger agencyIdListManifestId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
            AgencyIDListObject agencyIDList = getAPIFactory().getAgencyIDListAPI().getAgencyIDListByManifestId(agencyIdListManifestId);
            EditAgencyIDListPage editAgencyIDListPage = new EditAgencyIDListPageImpl(this, agencyIDList);
            assert editAgencyIDListPage.isOpened();

            waitFor(ofMillis(1000L)); // delay for loading agency ID list data
            return editAgencyIDListPage;
        });
    }

    @Override
    public WebElement getBranchSelectField() {
        return visibilityOfElementLocated(getDriver(), BRANCH_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setBranch(String branch) {
        retry(() -> {
            click(getBranchSelectField());
            waitFor(ofSeconds(2L));
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//mat-option//span[text() = \"" + branch + "\"]"));
            click(optionField);
        });
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
    public WebElement getDeprecatedSelectField() {
        return visibilityOfElementLocated(getDriver(), DEPRECATED_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setDeprecated(boolean deprecated) {
        click(getDeprecatedSelectField());
        WebElement optionField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[contains(text(), \"" + (deprecated ? "True" : "False") + "\")]"));
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
    public WebElement getDefinitionField() {
        return visibilityOfElementLocated(getDriver(), DEFINITION_FIELD_LOCATOR);
    }

    @Override
    public void setDefinition(String definition) {
        sendKeys(getDefinitionField(), definition);
    }

    @Override
    public WebElement getModuleField() {
        return visibilityOfElementLocated(getDriver(), MODULE_FIELD_LOCATOR);
    }

    @Override
    public void setModule(String module) {
        sendKeys(getModuleField(), module);
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
                By.xpath("//div[@class = \"mat-paginator-range-label\"]"));
        String paginatorRangeLabel = getText(paginatorRangeLabelElement);
        return Integer.valueOf(paginatorRangeLabel.substring(paginatorRangeLabel.indexOf("of") + 2).trim());
    }

    @Override
    public WebElement getPreviousPageButton() {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//div[contains(@class, \"mat-paginator-range-actions\")]" +
                        "//button[@aria-label = \"Previous page\"]"));
    }

    @Override
    public WebElement getNextPageButton() {
        return visibilityOfElementLocated(getDriver(), By.xpath(
                "//div[contains(@class, \"mat-paginator-range-actions\")]" +
                        "//button[@aria-label = \"Next page\"]"));
    }

}
