package org.oagi.score.e2e.impl.page.bie;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.bie.CreateBIEForSelectBusinessContextsPage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.TransferBIEOwnershipDialog;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class ViewEditBIEPageImpl extends BasePageImpl implements ViewEditBIEPage {

    private static final By BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Branch\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");

    private static final By STATE_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"State\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");

    private static final By OWNER_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Owner\")]//ancestor::div[1]/mat-select[1]");

    private static final By UPDATER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Updater\")]//ancestor::div[1]/mat-select[1]");

    private static final By DEN_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"DEN\")]//ancestor::mat-form-field//input");

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

    private static final By NEW_BIE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"New BIE\")]//ancestor::button[1]");

    private static final By DISCARD_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Discard\")]//ancestor::button[1]");

    public ViewEditBIEPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/profile_bie").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "BIE".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public WebElement getBranchSelectField() {
        return visibilityOfElementLocated(getDriver(), BRANCH_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setBranch(String branch) {
        retry(() -> {
            click(getBranchSelectField());
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
    public TransferBIEOwnershipDialog openTransferBIEOwnershipDialog(WebElement tr) {
        WebElement td = getColumnByName(tr, "transferOwnership");
        click(td.findElement(By.tagName("button")));

        TransferBIEOwnershipDialog transferBIEOwnershipDialog =
                new TransferBIEOwnershipDialogImpl(this);
        assert transferBIEOwnershipDialog.isOpened();
        return transferBIEOwnershipDialog;
    }

    @Override
    public WebElement getNewBIEButton() {
        return elementToBeClickable(getDriver(), NEW_BIE_BUTTON_LOCATOR);
    }

    @Override
    public CreateBIEForSelectBusinessContextsPage openCreateBIEPage() {
        click(getNewBIEButton());
        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage =
                new CreateBIEForSelectBusinessContextsPageImpl(this);
        assert createBIEForSelectBusinessContextsPage.isOpened();
        return createBIEForSelectBusinessContextsPage;
    }

    @Override
    public EditBIEPage openEditBIEPage(TopLevelASBIEPObject topLevelASBIEP) {
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseById(topLevelASBIEP.getReleaseId());
        setBranch(release.getReleaseNumber());
        setDEN(topLevelASBIEP.getDen());
        setState(topLevelASBIEP.getState());
        AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(topLevelASBIEP.getOwnwerUserId());
        setOwner(owner.getLoginId());
        hitSearchButton();

        return retry(() -> {
            WebElement td;
            try {
                WebElement tr = getTableRecordAtIndex(1);
                td = getColumnByName(tr, "den");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a BIE using " + topLevelASBIEP.getDen(), e);
            }
            if (!topLevelASBIEP.getDen().equals(getText(td.findElement(By.cssSelector("a > span"))))) {
                throw new NoSuchElementException("Cannot locate a BIE using " + topLevelASBIEP.getDen());
            }
            WebElement tdName = td.findElement(By.tagName("a"));
            click(tdName);

            EditBIEPage editBIEPage = new EditBIEPageImpl(this, topLevelASBIEP);
            assert editBIEPage.isOpened();
            return editBIEPage;
        });
    }

    @Override
    public EditBIEPage openEditBIEPage(WebElement tr) {
        return retry(() -> {
            WebElement td;
            try {
                td = getColumnByName(tr, "den");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a BIE using the table record", e);
            }
            WebElement link = td.findElement(By.tagName("a"));

            String href = link.getAttribute("href");
            String topLevelAsbiepId = href.substring(href.indexOf("/profile_bie/") + "/profile_bie/".length());
            TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().getTopLevelASBIEPByID(new BigInteger(topLevelAsbiepId));

            click(link);

            EditBIEPage editBIEPage = new EditBIEPageImpl(this, topLevelASBIEP);
            assert editBIEPage.isOpened();
            return editBIEPage;
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
    public void discard(TopLevelASBIEPObject topLevelASBIEP) {
        setBranch(topLevelASBIEP.getReleaseNumber());
        setOwner(getAPIFactory().getAppUserAPI().getAppUserByID(topLevelASBIEP.getOwnwerUserId()).getLoginId());
        setDEN(topLevelASBIEP.getPropertyTerm());
        hitSearchButton();

        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(Duration.ofMillis(500L));

        WebElement tr = getTableRecordByValue(topLevelASBIEP.getPropertyTerm());
        WebElement td = getColumnByName(tr, "select");
        click(td);
        click(getDiscardButton(true));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button[1]")));

        assert "Discarded".equals(getSnackBarMessage(getDriver()));
    }

    @Override
    public int getNumberOfOnlyBIEsPerStateAreListed(String state) {
        return getDriver().findElements(By.xpath("//table//*[contains(text(), \"" + state + "\")][@class=\"" + state + " bie-state\"]")).size();
    }
}
