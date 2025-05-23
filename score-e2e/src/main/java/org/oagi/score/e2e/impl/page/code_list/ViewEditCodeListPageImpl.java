package org.oagi.score.e2e.impl.page.code_list;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.BaseSearchBarPageImpl;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.CodeListObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.time.Duration;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class ViewEditCodeListPageImpl extends BaseSearchBarPageImpl implements ViewEditCodeListPage {

    private static final By BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//div[contains(@class, \"branch-selector\")]//mat-select[1]");
    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");
    private static final By NEW_CODE_LIST_BUTTON_LOCATOR =
            By.xpath("//button[contains(@mattooltip, \"New Code List\")]");
    private static final By DEPRECATED_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Deprecated\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By STATE_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"State\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By OWNER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Owner\")]//ancestor::mat-form-field[1]//mat-select");

    public ViewEditCodeListPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/code_list").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Code List".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public EditCodeListPage openCodeListViewEditPage(CodeListObject codeList) {
        return openCodeListViewEditPage(codeList, false);
    }

    @Override
    public EditCodeListPage openCodeListViewEditPage(CodeListObject codeList, boolean openWithoutSearching) {
        EditCodeListPage editCodeListPage = new EditCodeListPageImpl(this, codeList);
        if (openWithoutSearching) {
            editCodeListPage.openPage();
        } else {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseById(codeList.getReleaseId());
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(codeList.getOwnerUserId());
            showAdvancedSearchPanel();
            setBranch(release.getReleaseNumber());
            setOwner(owner.getLoginId());
            openCodeListByName(codeList.getName());

            waitFor(ofMillis(500L));
        }

        assert editCodeListPage.isOpened();
        return editCodeListPage;
    }

    @Override
    public WebElement getNameField() {
        return getInputFieldInSearchBar();
    }

    @Override
    public void setName(String name) {
        sendKeys(getNameField(), name);
    }

    private void openCodeListByName(String name) {
        setName(name);

        retry(() -> {
            hitSearchButton();

            WebElement td;
            WebElement tr;
            try {
                tr = getTableRecordByValue(name);
                td = getColumnByName(tr, "codeListName");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a code list using " + name, e);
            }
            String nameField = getNameFieldFromTheTable(td);
            if (!name.equals(nameField)) {
                throw new NoSuchElementException("Cannot locate a code list using " + name);
            }
            WebElement tdLoginID = td.findElement(By.cssSelector("a"));
            // TODO:
            // 'click' does not work when the browser hides the link.
            getDriver().get(tdLoginID.getAttribute("href"));
        });
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
        waitFor(Duration.ofMillis(500L));
        WebElement itemField = elementToBeClickable(getDriver(),
                By.xpath("//span[contains(text(), \"" + items + "\")]//ancestor::mat-option//div[1]//preceding-sibling::span"));
        click(getDriver(), itemField);
        waitFor(Duration.ofMillis(500L));
    }

    private String getNameFieldFromTheTable(WebElement tableData) {
        return getText(tableData.findElement(By.cssSelector("div.den > a > span")));
    }

    @Override
    public WebElement getTableRecordByValue(String value) {
        defaultWait(getDriver());
        return visibilityOfElementLocated(getDriver(), By.xpath("//*[contains(text(),\"" + value + "\")]//ancestor::tr"));
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
    public void setBranch(String branch) {
        retry(() -> {
            click(getDriver(), getBranchSelectField());
            sendKeys(visibilityOfElementLocated(getDriver(), DROPDOWN_SEARCH_FIELD_LOCATOR), branch);
            WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//div[@class = \"cdk-overlay-container\"]//mat-option//span[text() = \"" + branch + "\"]"));
            click(searchedSelectField);
            escape(getDriver());
        });
    }

    @Override
    public WebElement getBranchSelectField() {
        return elementToBeClickable(getDriver(), BRANCH_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void searchCodeListByNameAndBranch(String name, String releaseNumber) {
        setBranch(releaseNumber);
        sendKeys(getNameField(), name);
        retry(() -> {
            hitSearchButton();

            WebElement td;
            WebElement tr;
            try {
                tr = getTableRecordByValue(name);
                td = getColumnByName(tr, "codeListName");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a code list using " + name, e);
            }
            String nameField = getNameFieldFromTheTable(td);
            if (!name.equals(nameField)) {
                throw new NoSuchElementException("Cannot locate a code list using " + name);
            }
        });
    }

    @Override
    public void searchCodeListByNameAndDeprecation(CodeListObject cl, String releaseNumber) {
        showAdvancedSearchPanel();
        setBranch(releaseNumber);
        setDeprecated(cl);
        sendKeys(getNameField(), cl.getName());
        retry(() -> {
            hitSearchButton();

            WebElement td;
            WebElement tr;
            try {
                tr = getTableRecordByValue(cl.getName());
                td = getColumnByName(tr, "codeListName");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a code list using " + cl.getName(), e);
            }
            String nameField = getNameFieldFromTheTable(td);
            if (!cl.getName().equals(nameField)) {
                throw new NoSuchElementException("Cannot locate a code list using " + cl.getName());
            }
        });
    }

    @Override
    public void setDeprecated(CodeListObject codeList) {
        retry(() -> {
            click(getDeprecatedSelectField());
            waitFor(ofSeconds(2L));
            if (codeList.isDeprecated()) {
                /**
                 * Check if the opposite option is checked.
                 */
                WebElement optionField = visibilityOfElementLocated(getDriver(),
                        By.xpath("//mat-option//span[contains(text(), \"False\")]/preceding-sibling::mat-pseudo-checkbox"));
                if (optionField.getAttribute("class").contains("mat-pseudo-checkbox-checked")) {
                    click(optionField);
                }
                optionField = visibilityOfElementLocated(getDriver(),
                        By.xpath("//mat-option//span[contains(text(), \"True\")]/preceding-sibling::mat-pseudo-checkbox"));
                if (optionField.getAttribute("class").contains("mat-pseudo-checkbox-checked")) {
                    escape(getDriver());
                } else {
                    optionField = visibilityOfElementLocated(getDriver(),
                            By.xpath("//mat-option//span[contains(text(), \"True\")]"));
                    click(optionField);
                    escape(getDriver());
                }
            } else {
                WebElement optionField = visibilityOfElementLocated(getDriver(),
                        By.xpath("//mat-option//span[contains(text(), \"True\")]/preceding-sibling::mat-pseudo-checkbox"));
                if (optionField.getAttribute("class").contains("mat-pseudo-checkbox-checked")) {
                    click(optionField);
                }
                optionField = visibilityOfElementLocated(getDriver(),
                        By.xpath("//mat-option//span[contains(text(), \"False\")]/preceding-sibling::mat-pseudo-checkbox"));
                if (optionField.getAttribute("class").contains("mat-pseudo-checkbox-checked")) {
                    escape(getDriver());
                } else {
                    optionField = visibilityOfElementLocated(getDriver(),
                            By.xpath("//mat-option//span[contains(text(), \"False\")]"));
                    click(optionField);
                    escape(getDriver());
                }
            }
        });
    }

    @Override
    public WebElement getDeprecatedSelectField() {
        return visibilityOfElementLocated(getDriver(), DEPRECATED_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setState(String state) {
        retry(() -> {
            click(getStateSelectField());
            waitFor(ofSeconds(2L));

            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//mat-option//span[contains(text(), \"" + state + "\")]"));
            click(optionField);
            escape(getDriver());
        });
    }

    @Override
    public WebElement getStateSelectField() {
        return elementToBeClickable(getDriver(), STATE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setOwner(String owner) {
        retry(() -> {
            click(getOwnerSelectField());
            waitFor(ofSeconds(2L));

            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//mat-option//span[contains(text(), \"" + owner + "\")]"));
            click(optionField);
            escape(getDriver());
        });
    }

    @Override
    public WebElement getOwnerSelectField() {
        return elementToBeClickable(getDriver(), OWNER_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void searchCodeListByDefinitionAndBranch(CodeListObject codeList, String releaseNumber) {
        showAdvancedSearchPanel();
        setBranch(releaseNumber);
        sendKeys(getDefinitionField(), codeList.getDefinition());
        retry(() -> {
            hitSearchButton();

            WebElement td;
            WebElement tr;
            try {
                tr = getTableRecordByValue(codeList.getName());
                td = getColumnByName(tr, "codeListName");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a code list using " + codeList.getName(), e);
            }
            String nameField = getNameFieldFromTheTable(td);
            if (!codeList.getName().equals(nameField)) {
                throw new NoSuchElementException("Cannot locate a code list using " + codeList.getName());
            }
        });
    }

    @Override
    public WebElement getDefinitionField() {
        return visibilityOfElementLocated(getDriver(), By.xpath("//input[contains(@placeholder, \"Definition\")]"));
    }

    @Override
    public void searchCodeListByModuleAndBranch(CodeListObject codeList, String releaseNumber) {
        showAdvancedSearchPanel();
        setBranch(releaseNumber);
        String moduleSetName = getAPIFactory().getCodeListAPI().getModuleNameForCodeList(codeList, releaseNumber);
        sendKeys(getModuleField(), moduleSetName);
        sendKeys(getNameField(), codeList.getName());
        retry(() -> {
            hitSearchButton();
            WebElement td;
            WebElement tr;
            try {
                tr = getTableRecordByValue(codeList.getName());
                td = getColumnByName(tr, "codeListName");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a code list using " + codeList.getName(), e);
            }
            String nameField = getNameFieldFromTheTable(td);
            if (!codeList.getName().equals(nameField)) {
                throw new NoSuchElementException("Cannot locate a code list using " + codeList.getName());
            }
        });
    }

    @Override
    public WebElement getModuleField() {
        return visibilityOfElementLocated(getDriver(), By.xpath("//input[contains(@placeholder, \"Module\")]"));
    }

    @Override
    public void searchCodeListByUpdatedDateAndBranch(CodeListObject codeList, String releaseNumber) {
        setBranch(releaseNumber);
        sendKeys(getUpdatedDateField(), codeList.getLastUpdateTimestamp());
        retry(() -> {
            hitSearchButton();

            WebElement td;
            WebElement tr;
            try {
                tr = getTableRecordByValue(codeList.getName());
                td = getColumnByName(tr, "codeListName");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a code list using " + codeList.getName(), e);
            }
            String nameField = getNameFieldFromTheTable(td);
            if (!codeList.getName().equals(nameField)) {
                throw new NoSuchElementException("Cannot locate a code list using " + codeList.getName());
            }
        });
    }

    @Override
    public WebElement getUpdatedDateField() {
        return visibilityOfElementLocated(getDriver(), By.xpath("//input[contains(@placeholder, \"Updated start date\")]"));
    }

    @Override
    public EditCodeListPage openNewCodeList(AppUserObject user, String releaseNumber) {
        retry(() -> {
            click(getNewCodeListButton());
            waitFor(ofMillis(1000L));
        });

        invisibilityOfLoadingContainerElement(getDriver());
        CodeListObject codeList = getAPIFactory().getCodeListAPI().getNewlyCreatedCodeList(user, releaseNumber);
        EditCodeListPage editCodeListPage = new EditCodeListPageImpl(this, codeList);
        assert editCodeListPage.isOpened();
        return editCodeListPage;
    }

    @Override
    public WebElement getNewCodeListButton() {
        return elementToBeClickable(getDriver(), NEW_CODE_LIST_BUTTON_LOCATOR);
    }
}
