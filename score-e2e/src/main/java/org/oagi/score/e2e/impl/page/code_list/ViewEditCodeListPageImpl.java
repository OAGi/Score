package org.oagi.score.e2e.impl.page.code_list;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.CodeListObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class ViewEditCodeListPageImpl extends BasePageImpl implements ViewEditCodeListPage {
    private static final By BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(),\"Branch\")]//ancestor::mat-form-field[1]//mat-select//div[contains(@class, \"mat-select-arrow-wrapper\")]");
    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Search\")]//ancestor::button[1]");
    private static final By NEW_CODE_LIST_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"New Code List\")]//ancestor::button[1]");
    private static final By DEPRECATED_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(),\"Deprecated\")]//ancestor::mat-form-field[1]//mat-select//div[contains(@class, \"mat-select-arrow-wrapper\")]");
    private static final By STATE_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(),\"State\")]//ancestor::mat-form-field[1]//mat-select//div[contains(@class, \"mat-select-arrow-wrapper\")]");

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
    public EditCodeListPage openCodeListViewEditPageByNameAndBranch(String name, String branch) {
        setBranch(branch);
        openCodeListByName(name);
        CodeListObject codeList = getAPIFactory().getCodeListAPI().getCodeListByNameAndReleaseNum(name, branch);
        waitFor(ofMillis(500L));
        EditCodeListPage editCodeListPage = new EditCodeListPageImpl(this, codeList);
        assert editCodeListPage.isOpened();
        return editCodeListPage;
    }

    @Override
    public EditCodeListPage openCodeListViewEditPageByManifestId(BigInteger codeListManifestId) {
        CodeListObject codeList = getAPIFactory().getCodeListAPI().getCodeListByManifestId(codeListManifestId);
        EditCodeListPage editCodeListPage = new EditCodeListPageImpl(this, codeList);
        editCodeListPage.openPage();
        assert editCodeListPage.isOpened();
        return editCodeListPage;
    }

    @Override
    public WebElement getNameField() {
        return visibilityOfElementLocated(getDriver(), By.xpath("//input[contains(@data-placeholder, \"Name\")]"));
    }

    private void openCodeListByName(String name) {
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
    public WebElement getSearchButton() {
        return elementToBeClickable(getDriver(), SEARCH_BUTTON_LOCATOR);
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
    public WebElement getBranchSelectField() {
        return visibilityOfElementLocated(getDriver(), BRANCH_SELECT_FIELD_LOCATOR);
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
                WebElement otherOptionField = visibilityOfElementLocated(getDriver(),
                        By.xpath("//mat-option//span[contains(text(), \"False\")]/preceding-sibling::mat-pseudo-checkbox"));
                String statusSecondOption = otherOptionField.getAttribute("ng-reflect-state");
                if (statusSecondOption.equals("checked")) {
                    click(otherOptionField);
                }
                String statusFirstOption = visibilityOfElementLocated(getDriver(),
                        By.xpath("//mat-option//span[contains(text(), \"True\")]/preceding-sibling::mat-pseudo-checkbox")).getAttribute("ng-reflect-state");

                if (statusFirstOption.equals("checked")) {
                    escape(getDriver());
                } else {
                    WebElement optionField = visibilityOfElementLocated(getDriver(),
                            By.xpath("//mat-option//span[contains(text(), \"True\")]"));
                    click(optionField);
                    escape(getDriver());
                }
            } else {
                WebElement otherOptionField = visibilityOfElementLocated(getDriver(),
                        By.xpath("//mat-option//span[contains(text(), \"True\")]/preceding-sibling::mat-pseudo-checkbox"));
                String status = otherOptionField.getAttribute("ng-reflect-state");
                if (status.equals("checked")) {
                    click(otherOptionField);
                }
                String statusFirstOption = visibilityOfElementLocated(getDriver(),
                        By.xpath("//mat-option//span[contains(text(), \"False\")]/preceding-sibling::mat-pseudo-checkbox")).getAttribute("ng-reflect-state");

                if (statusFirstOption.equals("checked")) {
                    escape(getDriver());
                } else {
                    WebElement optionField = visibilityOfElementLocated(getDriver(),
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
    public void toggleState(String state) {
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
        return visibilityOfElementLocated(getDriver(), STATE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void searchCodeListByDefinitionAndBranch(CodeListObject codeList, String releaseNumber) {
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
        return visibilityOfElementLocated(getDriver(), By.xpath("//input[contains(@data-placeholder, \"Definition\")]"));
    }

    @Override
    public void searchCodeListByModuleAndBranch(CodeListObject codeList, String releaseNumber) {
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
        return visibilityOfElementLocated(getDriver(), By.xpath("//input[contains(@data-placeholder, \"Module\")]"));
    }

    @Override
    public void searchCodeListByUpdatedDateAndBranch(CodeListObject codeList, String releaseNumber) {
        setBranch(releaseNumber);
        sendKeys(getUpdatedDateField(), codeList.getLastUpdateTimestamp().toString());
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
        return visibilityOfElementLocated(getDriver(), By.xpath("//input[contains(@data-placeholder, \"Updated start date\")]"));
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
