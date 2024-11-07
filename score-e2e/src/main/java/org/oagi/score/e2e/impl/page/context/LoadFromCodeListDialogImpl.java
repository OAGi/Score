package org.oagi.score.e2e.impl.page.context;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.SearchBarPageImpl;
import org.oagi.score.e2e.page.context.LoadFromCodeListDialog;
import org.openqa.selenium.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class LoadFromCodeListDialogImpl extends SearchBarPageImpl implements LoadFromCodeListDialog {

    private static final By BRANCH_SELECT_FIELD_LOCATOR
            = By.xpath("//mat-dialog-container//div[contains(@class, \"branch-selector\")]//mat-select[1]");

    private static final By OWNER_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-dialog-container//mat-label[contains(text(), \"Owner\")]//ancestor::div[1]/mat-select[1]");

    private static final By UPDATER_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-dialog-container//*[contains(text(), \"Updater\")]//ancestor::div[1]/mat-select[1]");

    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//mat-dialog-container//input[@aria-label=\"dropdown search\"]");

    private static final By UPDATED_START_DATE_FIELD_LOCATOR =
            By.xpath("//mat-dialog-container//input[contains(@placeholder, \"Updated start date\")]");

    private static final By UPDATED_END_DATE_FIELD_LOCATOR =
            By.xpath("//mat-dialog-container//input[contains(@placeholder, \"Updated end date\")]");

    private static final By SELECT_BUTTON_LOCATOR =
            By.xpath("//mat-dialog-container//span[contains(text(), \"Select\")]//ancestor::button[1]");

    private final BasePageImpl parent;

    public LoadFromCodeListDialogImpl(BasePageImpl parent) {
        super(parent.getDriver(), "//mat-dialog-container");
        this.parent = parent;
    }

    @Override
    public boolean isOpened() {
        try {
            assert "Code List".equals(getText(getTitle()));
        } catch (TimeoutException e) {
            return false;
        }
        return true;
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-dialog-container//*[contains(@class, \"mat-mdc-dialog-title\")]/span"));
    }

    @Override
    public WebElement getBranchSelectField() {
        return visibilityOfElementLocated(getDriver(), BRANCH_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setBranch(String branch) {
        retry(() -> {
            WebElement optionField;
            try {
                click(getDriver(), getBranchSelectField());
                optionField = visibilityOfElementLocated(getDriver(),
                        By.xpath("//div[@class = \"cdk-overlay-container\"]//mat-option//span[text() = \"" + branch + "\"]"));
            } catch (Exception e) {
                throw new NoSuchElementException("Cannot locate a branch using " + branch, e);
            }
            click(optionField);
        });
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
        return getInputFieldInSearchBar();
    }

    @Override
    public void setName(String name) {
        sendKeys(getNameField(), name);
    }

    @Override
    public void hitSearchButton() {
        retry(() -> {
            click(getSearchButton());
            waitFor(ofMillis(500L));
            invisibilityOfLoadingContainerElement(getDriver());
        });
    }

    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-dialog-container//tbody/tr[" + idx + "]"));
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//mat-dialog-container//td//*[contains(text(), \"" + value + "\")]//ancestor::tr"));
    }

    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }

    @Override
    public void goToNextPage() {
        ((JavascriptExecutor) getDriver())
                .executeScript("window.scrollTo(0, document.body.scrollHeight)");
        click(elementToBeClickable(getDriver(), By.xpath("//mat-dialog-container//button[@aria-label='Next page']")));
    }

    @Override
    public void goToPreviousPage() {
        ((JavascriptExecutor) getDriver())
                .executeScript("window.scrollTo(0, document.body.scrollHeight)");
        click(elementToBeClickable(getDriver(), By.xpath("//mat-dialog-container//button[@aria-label='Previous page']")));
    }

    @Override
    public WebElement getSelectButton() {
        return elementToBeClickable(getDriver(), SELECT_BUTTON_LOCATOR);
    }

    @Override
    public void selectCodeListByCodeListNameAndBranch(String codeListName, String branch) {
        setBranch(branch);
        setName(codeListName);

        retry(() -> {
            hitSearchButton();

            WebElement tr;
            WebElement td;
            try {
                tr = getTableRecordAtIndex(1);
                td = getColumnByName(tr, "codeListName");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a code list using " + codeListName, e);
            }
            String actualText = getText(td.findElement(By.cssSelector("a > span")));
            if (!codeListName.equals(actualText)) {
                try {
                    tr = getTableRecordAtIndex(2);
                    td = getColumnByName(tr, "codeListName");
                } catch (TimeoutException e) {
                    throw new NoSuchElementException("Cannot locate a code list using " + codeListName, e);
                }

                actualText = getText(td.findElement(By.cssSelector("a > span")));
                if (!codeListName.equals(actualText)) {
                    throw new NoSuchElementException("Cannot locate a code list using " + codeListName);
                }
            }

            click(getColumnByName(tr, "select"));
            click(getSelectButton());
            waitFor(ofMillis(500));

            assert parent.isOpened();
        });
    }
}
