package org.oagi.score.e2e.impl.page.bie;

import org.oagi.score.e2e.impl.page.BaseSearchBarPageImpl;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.MultiActionSnackBar;
import org.oagi.score.e2e.page.bie.CreateBIEForSelectBusinessContextsPage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.TransferBIEOwnershipDialog;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.openqa.selenium.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class ViewEditBIEPageImpl extends BaseSearchBarPageImpl implements ViewEditBIEPage {

    private static final By BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//div[contains(@class, \"branch-selector\")]//mat-select[1]");

    private static final By STATE_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"State\")]//ancestor::mat-form-field[1]//mat-select");

    private static final By OWNER_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Owner\")]//ancestor::div[1]/mat-select[1]");

    private static final By UPDATER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Updater\")]//ancestor::div[1]/mat-select[1]");

    private static final By BUSINESS_CONTEXT_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Business Context\")]");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");

    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Updated start date\")]");

    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Updated end date\")]");

    private static final By NEW_BIE_BUTTON_LOCATOR =
            By.xpath("//button[contains(@mattooltip, \"New BIE\")]");

    private static final By DISCARD_BUTTON_LOCATOR =
            By.xpath("//mat-icon[contains(text(), \"delete\")]//ancestor::button[1]");

    private static final By MOVE_TO_QA_BUTTON_LOCATOR =
            By.xpath("//button[contains(@mattooltip, \"Move to QA\")]");

    private static final By MOVE_TO_PRODUCTION_BUTTON_LOCATOR =
            By.xpath("//button[contains(@mattooltip, \"Move to Production\")]");

    private static final By BACK_TO_WIP_BUTTON_LOCATOR =
            By.xpath("//button[contains(@mattooltip, \"Back to WIP\")]");

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
        click(getDriver(), getBranchSelectField());
        sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), branch);
        WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                By.xpath("//div[@class = \"cdk-overlay-container\"]//mat-option//span[text() = \"" + branch + "\"]"));
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
    public WebElement getDENField() {
        return getInputFieldInSearchBar();
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
    public void hitSearchButton() {
        click(getSearchButton());
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1000L));
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
                By.xpath("//div[.=\" Items per page: \"]/following::mat-form-field//mat-select"));
        click(getDriver(), itemsPerPageField);
        waitFor(ofMillis(500L));
        WebElement itemField = elementToBeClickable(getDriver(),
                By.xpath("//span[contains(text(), \"" + items + "\")]//ancestor::mat-option//div[1]//preceding-sibling::span"));
        click(getDriver(), itemField);
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
    public TransferBIEOwnershipDialog openTransferBIEOwnershipDialog(WebElement tr) {
        WebElement td = getColumnByName(tr, "owner");
        click(td.findElement(By.tagName("mat-icon")));

        TransferBIEOwnershipDialog transferBIEOwnershipDialog =
                new TransferBIEOwnershipDialogImpl(this);
        assert transferBIEOwnershipDialog.isOpened();
        return transferBIEOwnershipDialog;
    }

    @Override
    public void hitCreateInheritedBIE(WebElement tr) {
        WebElement td = getColumnByName(tr, "more");
        click(td.findElement(By.tagName("button")));

        WebElement createInheritedBIEButton = elementToBeClickable(getDriver(), By.xpath(
                "//div[@class=\"cdk-overlay-container\"]" +
                        "//span[text() = \"Create Inherited BIE\"]//ancestor::button[1]"));
        click(createInheritedBIEButton);

        MultiActionSnackBar multiActionSnackBar = getMultiActionSnackBar(getDriver());
        assert "This may take a moment, so please check back shortly.".equals(
                getText(multiActionSnackBar.getMessageElement())
        );
        waitFor(ofMillis(1000L));
        click(multiActionSnackBar.getActionButtonByName("Search"));
    }

    @Override
    public WebElement getNewBIEButton() {
        return elementToBeClickable(getDriver(), NEW_BIE_BUTTON_LOCATOR);
    }

    @Override
    public CreateBIEForSelectBusinessContextsPage openCreateBIEPage() {
        click(getNewBIEButton());
        waitFor(ofMillis(500L));

        return retry(() -> {
            CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage =
                    new CreateBIEForSelectBusinessContextsPageImpl(this);
            assert createBIEForSelectBusinessContextsPage.isOpened();
            return createBIEForSelectBusinessContextsPage;
        });
    }

    @Override
    public EditBIEPage openEditBIEPage(TopLevelASBIEPObject topLevelASBIEP) {
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseById(topLevelASBIEP.getReleaseId());
        showAdvancedSearchPanel();
        setBranch(release.getReleaseNumber());
        setDEN(topLevelASBIEP.getDen());
        setState(topLevelASBIEP.getState());
        AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(topLevelASBIEP.getOwnerUserId());
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
            if (!getText(td.findElement(By.cssSelector("a > div > span"))).startsWith(topLevelASBIEP.getDen())) {
                throw new NoSuchElementException("Cannot locate a BIE using " + topLevelASBIEP.getDen());
            }
            WebElement tdName = td.findElement(By.tagName("a"));
            try {
                click(tdName);
            } catch (ElementNotInteractableException e) {
                String href = tdName.getAttribute("href");
                getDriver().get(href);
            }
            waitFor(ofMillis(500L));
            invisibilityOfLoadingContainerElement(getDriver());

            EditBIEPage editBIEPage = new EditBIEPageImpl(this, topLevelASBIEP);
            try {
                assert editBIEPage.isOpened();
            } catch (AssertionError e) {
                editBIEPage.openPage();
            }
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
            try {
                click(link);
            } catch (ElementNotInteractableException e) {
                getDriver().get(href);
            }

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
        showAdvancedSearchPanel();
        setBranch(topLevelASBIEP.getReleaseNumber());
        setOwner(getAPIFactory().getAppUserAPI().getAppUserByID(topLevelASBIEP.getOwnerUserId()).getLoginId());
        setDEN(topLevelASBIEP.getPropertyTerm());
        hitSearchButton();

        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(500L));

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

    @Override
    public WebElement getMoveToQA(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), MOVE_TO_QA_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), MOVE_TO_QA_BUTTON_LOCATOR);
        }
    }

    @Override
    public void moveToQA() {
        click(getMoveToQA(true));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1000L));
    }

    @Override
    public WebElement getMoveToProduction(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), MOVE_TO_PRODUCTION_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), MOVE_TO_PRODUCTION_BUTTON_LOCATOR);
        }
    }

    @Override
    public void moveToProduction() {
        click(getMoveToProduction(true));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1000L));
    }

    @Override
    public WebElement getBackToWIP(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), BACK_TO_WIP_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), BACK_TO_WIP_BUTTON_LOCATOR);
        }
    }

    @Override
    public void backToWIP() {
        click(getBackToWIP(true));
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button[1]")));
        invisibilityOfLoadingContainerElement(getDriver());
        waitFor(ofMillis(1000L));
    }
}
