package org.oagi.score.e2e.impl.page.bie;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.SearchBarPageImpl;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.bie.SelectBaseProfileBIEDialog;
import org.oagi.score.e2e.page.bie.SelectProfileBIEToReuseDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class SelectBaseProfileBIEDialogImpl extends SearchBarPageImpl implements SelectBaseProfileBIEDialog {

    private static final By OWNER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Owner\")]//ancestor::div[1]/mat-select[1]");

    private static final By UPDATER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Updater\")]//ancestor::div[1]/mat-select[1]");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");

    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Updated start date\")]");

    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//input[contains(@placeholder, \"Updated end date\")]");

    private static final By SELECT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Select\")]//ancestor::button[1]");

    private static final By TABLE_RECORD_OWNER_LOCATOR =
            By.xpath("//mat-dialog-container//tr[contains(@class, \"element-row\")]/td[contains(@class, \"mat-column-owner\")]");

    private final BasePageImpl parent;

    private final String contextMenuName;

    public SelectBaseProfileBIEDialogImpl(BasePageImpl parent, String contextMenuName) {
        super(parent.getDriver(), "//mat-dialog-container");
        this.parent = parent;
        this.contextMenuName = contextMenuName;
    }

    @Override
    public boolean isOpened() {
        try {
            getTitle();
        } catch (TimeoutException e) {
            return false;
        }
        return true;
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.xpath("(//mat-dialog-container//div[contains(@class, \"mat-mdc-dialog-title\")]//span | //mat-dialog-container//div[contains(@class, \"mat-mdc-dialog-title\")][not(.//span)])[1]"));
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
    public void setOwners(List<String> owners) {
        click(getOwnerSelectField());
        for (String owner : owners) {
            WebElement searchField = visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR);
            searchField.clear();
            sendKeys(searchField, owner);
            click(visibilityOfElementLocated(getDriver(),
                    By.xpath("//mat-option//span[contains(text(), \"" + owner + "\")]")));
        }
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
    public void hitSearchButton() {
        click(getSearchButton());
        waitFor(ofMillis(500L));
    }

    @Override
    public void selectBaseBIE(TopLevelASBIEPObject bie) {
        showAdvancedSearchPanel();
        setOwner(bie.getOwnerLoginId());
        hitSearchButton();
        retry(() -> {
            WebElement tr = getTableRecordByValue(bie.getPropertyTerm());
            WebElement td = getColumnByName(tr, "select");
            click(td.findElement(By.xpath("mat-checkbox")));
        });
        click(getSelectButton());
        click(getDialogButtonByName(getDriver(), "Proceed"));
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public void selectBaseBIE(String topLevelBIEPropertyName) {
        retry(() -> {
            WebElement tr = getTableRecordByValue(topLevelBIEPropertyName);
            WebElement td = getColumnByName(tr, "select");
            click(td.findElement(By.xpath("mat-checkbox")));
        });
        click(getSelectButton());
        click(getDialogButtonByName(getDriver(), "Proceed"));
        invisibilityOfLoadingContainerElement(getDriver());
    }

    @Override
    public List<String> getOwnerLoginIdsOfTableRecords() {
        try {
            visibilityOfElementLocated(getDriver(), TABLE_RECORD_OWNER_LOCATOR);
        } catch (TimeoutException e) {
            return Collections.emptyList();
        }
        return getDriver().findElements(TABLE_RECORD_OWNER_LOCATOR).stream()
                .map(e -> getText(e).trim())
                .collect(Collectors.toList());
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
    public WebElement getSelectButton() {
        return elementToBeClickable(getDriver(), SELECT_BUTTON_LOCATOR);
    }

}
