package org.oagi.score.e2e.impl.page.release;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.release.EditReleasePage;
import org.oagi.score.e2e.page.release.ViewEditReleasePage;
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

public class ViewEditReleasePageImpl extends BasePageImpl implements ViewEditReleasePage {

    private static final By CREATOR_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(),\"Creator\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");

    private static final By UPDATER_SELECT_FIELD_LOCATOR =
            By.xpath("//span[contains(text(),\"Updater\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");

    private static final By STATE_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(),\"State\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");

    private static final By NAMESPACE_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(),\"Namespace\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");

    private static final By CREATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@data-placeholder, \"Created start date\")]");

    private static final By CREATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@data-placeholder, \"Created end date\")]");

    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@data-placeholder, \"Updated start date\")]");

    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@data-placeholder, \"Updated end date\")]");

    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Search\")]//ancestor::button[1]");

    public ViewEditReleasePageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/release").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Release".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public WebElement getCreatorSelectField() {
        return visibilityOfElementLocated(getDriver(), CREATOR_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setCreator(String creator) {
        retry(() -> {
            click(getCreatorSelectField());
            waitFor(ofSeconds(2L));
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//mat-option//span[text() = \"" + creator + "\"]"));
            click(optionField);
        });
    }

    @Override
    public WebElement getCreatedStartDateField() {
        return visibilityOfElementLocated(getDriver(), CREATED_START_DATE_FIELD_LOCATOR);
    }

    @Override
    public void setCreatedStartDate(LocalDateTime createdStartDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        sendKeys(getUpdatedStartDateField(), formatter.format(createdStartDate));
    }

    @Override
    public WebElement getCreatedEndDateField() {
        return visibilityOfElementLocated(getDriver(), CREATED_END_DATE_FIELD_LOCATOR);
    }

    @Override
    public void setCreatedEndDate(LocalDateTime createdEndDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        sendKeys(getUpdatedStartDateField(), formatter.format(createdEndDate));
    }

    @Override
    public WebElement getUpdaterSelectField() {
        return visibilityOfElementLocated(getDriver(), UPDATER_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setUpdater(String updater) {
        retry(() -> {
            click(getCreatorSelectField());
            waitFor(ofSeconds(2L));
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//mat-option//span[text() = \"" + updater + "\"]"));
            click(optionField);
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
    public WebElement getNamespaceSelectField() {
        return visibilityOfElementLocated(getDriver(), NAMESPACE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setNamespace(String namespace) {
        retry(() -> {
            click(getCreatorSelectField());
            waitFor(ofSeconds(2L));
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//mat-option//span[text() = \"" + namespace + "\"]"));
            click(optionField);
        });

    }

    @Override
    public WebElement getStateSelectField() {
        return visibilityOfElementLocated(getDriver(), STATE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setState(String state) {
        retry(() -> {
            click(getCreatorSelectField());
            waitFor(ofSeconds(2L));
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//mat-option//span[text() = \"" + state + "\"]"));
            click(optionField);
        });
    }

    @Override
    public WebElement getReleaseNumField() {
        return visibilityOfElementLocated(getDriver(), By.xpath("//input[contains(@data-placeholder, \"Release Num\")]"));
    }

    @Override
    public void setReleaseNum(String releaseNum) {
        sendKeys(getReleaseNumField(), releaseNum);
    }

    @Override
    public WebElement getSearchButton() {
        return elementToBeClickable(getDriver(), SEARCH_BUTTON_LOCATOR);
    }

    @Override
    public void hitSearchButton() {
        waitFor(ofMillis(3000L));
        retry(() -> {
            click(getSearchButton());
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public EditReleasePage openReleaseViewEditPageByReleaseAndState(String releaseNum, String State) {
        openReleaseByReleaseNum(releaseNum);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(releaseNum);
        EditReleasePage editReleasePage = new EditReleasePageImpl(this, release);
        editReleasePage.openPage();
        assert editReleasePage.isOpened();
        return editReleasePage;
    }

    private void openReleaseByReleaseNum(String releaseNum) {

        sendKeys(getReleaseNumField(), releaseNum);

        retry(() -> {
            hitSearchButton();

            WebElement td;
            WebElement tr;
            try {
                tr = getTableRecordByValue(releaseNum);
                td = getColumnByName(tr, "Release");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a core component using " + releaseNum, e);
            }
            String releaseNumField = getReleaseNumFieldFromTheTable(td);
            if (!releaseNum.equals(releaseNumField)) {
                throw new NoSuchElementException("Cannot locate a core component using " + releaseNum);
            }
            WebElement tdLoginID = td.findElement(By.cssSelector("a"));
            // TODO:
            // 'click' does not work when the browser hides the link.
            getDriver().get(tdLoginID.getAttribute("href"));
        });
    }

    private String getReleaseNumFieldFromTheTable(WebElement tableData) {
        return getText(tableData.findElement(By.cssSelector("div.release > a > span")));
    }

    @Override
    public EditReleasePage openReleaseViewEditPageByID(BigInteger releaseID) {
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseById(releaseID);
        EditReleasePage editReleasePage = new EditReleasePageImpl(this, release);
        editReleasePage.openPage();
        assert editReleasePage.isOpened();
        return editReleasePage;
    }

    @Override
    public WebElement getNewReleaseButton() {
        return null;
    }

    @Override
    public ViewEditReleasePage createRelease() {
        return null;
    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return null;
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        return null;
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return null;
    }
}
