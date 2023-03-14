package org.oagi.score.e2e.impl.page.code_list;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.CodeListObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.oagi.score.e2e.impl.PageHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.click;

public class ViewEditCodeListPageImpl extends BasePageImpl implements ViewEditCodeListPage {
    private static final By BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(),\"Branch\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");
    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Search\")]//ancestor::button[1]");

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
        CodeListObject codeList = getAPIFactory().getCoreComponentAPI().getCodeListByNameAndReleaseNum(name, branch);
        EditCodeListPage editCodeListPage = new EditCodeListPageImpl(this, codeList);
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
        return getText(tableData.findElement(By.cssSelector("div.den")));
    }

    @Override
    public WebElement getTableRecordByValue(String value){
        defaultWait(getDriver());
        return visibilityOfElementLocated(getDriver(), By.xpath("//*[contains(text(),\""+value+"\")]//ancestor::tr"));
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
}
