package org.oagi.score.e2e.impl.page.namespace;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.BaseSearchBarPageImpl;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.namespace.CreateNamespacePage;
import org.oagi.score.e2e.page.namespace.EditNamespacePage;
import org.oagi.score.e2e.page.namespace.TransferNamespaceOwershipDialog;
import org.oagi.score.e2e.page.namespace.ViewEditNamespacePage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class ViewEditNamespacePageImpl extends BaseSearchBarPageImpl implements ViewEditNamespacePage {

    private static final By OWNER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Owner\")]//ancestor::div[1]/mat-select[1]");

    private static final By STANDARD_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Standard\")]//ancestor::div[1]/mat-select[1]");

    private static final By UPDATER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Updater\")]//ancestor::div[1]/mat-select[1]");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");

    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Updated start date\")]");

    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Updated end date\")]");

    private static final By PREFIX_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Prefix\")]//ancestor::div[1]/input");
    private static final By NEW_NAMESPACE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"New Namespace\")]//ancestor::button[1]");

    public ViewEditNamespacePageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/namespace").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Namespace".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public WebElement getOwerSelectField() {
        return visibilityOfElementLocated(getDriver(), OWNER_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setOwner(String owner) {
        click(getOwerSelectField());
        sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), owner);
        WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[contains(text(), \"" + owner + "\")]"));
        click(searchedSelectField);
        escape(getDriver());
    }

    @Override
    public WebElement getStandardSelectField() {
        return visibilityOfElementLocated(getDriver(), STANDARD_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void toggleStandard(boolean isStandard) {
        click(getStandardSelectField());
        WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-option//span[contains(text(), \"" + isStandard + "\")]"));
        click(searchedSelectField);
        escape(getDriver());
    }

    @Override
    public WebElement getURIField() {
        return getInputFieldInSearchBar();
    }

    @Override
    public void setURI(String uri) {
        sendKeys(getURIField(), uri);
    }

    @Override
    public WebElement getPrefixField() {
        return visibilityOfElementLocated(getDriver(), PREFIX_FIELD_LOCATOR);
    }

    @Override
    public void setPrefix(String prefix) {
        sendKeys(getPrefixField(), prefix);
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
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        defaultWait(getDriver());
        return visibilityOfElementLocated(getDriver(), By.xpath("//*[contains(text(),\"" + value + "\")]//ancestor::tr"));
    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//tbody/tr[" + idx + "]"));
    }

    @Override
    public void hitSearchButton() {
        retry(() -> {
            click(getSearchButton());
            waitFor(ofMillis(1000L));
        });
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public WebElement getNewNamespaceButton() {
        return elementToBeClickable(getDriver(), NEW_NAMESPACE_BUTTON_LOCATOR);
    }

    @Override
    public CreateNamespacePage hitNewNamespaceButton() {
        click(getNewNamespaceButton());
        waitFor(Duration.ofMillis(500L));

        CreateNamespacePage createNamespacePage = new CreateNamespacePageImpl(this);
        assert createNamespacePage.isOpened();
        return createNamespacePage;
    }

    @Override
    public EditNamespacePage openNamespaceByURIAndOwner(String uri, String owner) {
        showAdvancedSearchPanel();
        setOwner(owner);
        openNamespaceByURI(uri);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(uri);
        waitFor(ofMillis(500L));
        EditNamespacePage editNamespacePage = new EditNamespacePageImpl(this, namespace);
        assert editNamespacePage.isOpened();
        return editNamespacePage;
    }

    private void openNamespaceByURI(String uri) {
        setURI(uri);

        retry(() -> {
            hitSearchButton();

            WebElement td;
            WebElement tr;
            try {
                tr = getTableRecordByValue(uri);
                td = getColumnByName(tr, "uri");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a namespace using " + uri, e);
            }
            String nameField = getNameFieldFromTheTable(td);
            if (!uri.equals(nameField)) {
                throw new NoSuchElementException("Cannot locate a namespace using " + uri);
            }
            WebElement tdLoginID = td.findElement(By.cssSelector("a"));
            // TODO:
            // 'click' does not work when the browser hides the link.
            getDriver().get(tdLoginID.getAttribute("href"));
        });
    }

    private String getNameFieldFromTheTable(WebElement tableData) {
        return getText(tableData.findElement(By.cssSelector("td.mat-column-uri > a")));
    }

    @Override
    public TransferNamespaceOwershipDialog openTransferNamespaceOwnershipDialog(WebElement tr) {
        WebElement td = getColumnByName(tr, "owner");
        click(td.findElement(By.className("mat-icon")));

        TransferNamespaceOwershipDialog transferNamespaceOwershipDialog =
                new TransferNamespaceOwnershipDialogImpl(this);
        assert transferNamespaceOwershipDialog.isOpened();
        return transferNamespaceOwershipDialog;
    }
}
