package org.oagi.score.e2e.impl.page.code_list;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.code_list.UpliftCodeListPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class UpliftCodeListPageImpl extends BasePageImpl implements UpliftCodeListPage {
    private static final By SOURCE_BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Source Branch\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");
    private static final By TARGET_BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Target Branch\")]//ancestor::mat-form-field[1]//mat-select/div/div[1]");
    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Search\")]//ancestor::button[1]");
    private static final By CODE_LIST_FIELD_LOCATOR =
            By.xpath("//mat-card-content//span[contains(text(), \"Name\")]//ancestor::mat-form-field//input");

    public UpliftCodeListPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/code_list/uplift").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Uplift Code List".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-card-title"));
    }

    @Override
    public void setSourceRelease(String branch) {
        retry(() -> {
            click(getSourceBranchSelectField());
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//span[contains(text(), \"" + branch + "\")]//ancestor::mat-option[1]/span"));
            click(optionField);
            waitFor(ofMillis(500L));
        });
    }

    @Override
    public WebElement getSourceBranchSelectField() {
        return visibilityOfElementLocated(getDriver(), SOURCE_BRANCH_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setTargetRelease(String branch) {
        retry(() -> {
            click(getTargetBranchSelectField());
            WebElement optionField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//span[contains(text(), \"" + branch + "\")]//ancestor::mat-option[1]/span"));
            click(optionField);
            waitFor(ofMillis(500L));
        });
    }

    @Override
    public WebElement getTargetBranchSelectField() {
        return visibilityOfElementLocated(getDriver(), TARGET_BRANCH_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void selectCodeList(String name) {
        setCodeList(name);
        hitSearchButton();

        retry(() -> {
            WebElement tr;
            WebElement td;
            try {
                tr = getTableRecordAtIndex(1);
                td = getColumnByName(tr, "codeListName");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate a Code List using " + name, e);
            }
            String denColumn = getText(td.findElement(By.tagName("span")));
            if (!denColumn.contains(name)) {
                throw new NoSuchElementException("Cannot locate a Code List using " + name);
            }
            WebElement select = getColumnByName(tr, "select");
            click(select);
        });
    }

    @Override
    public WebElement getCodeListField() {
        return visibilityOfElementLocated(getDriver(), CODE_LIST_FIELD_LOCATOR);
    }

    @Override
    public void setCodeList(String name) {
        sendKeys(getCodeListField(), name);
    }

    @Override
    public WebElement getSearchButton() {
        return elementToBeClickable(getDriver(), SEARCH_BUTTON_LOCATOR);
    }

    @Override
    public void hitSearchButton() {
        click(getSearchButton());
        waitFor(ofMillis(500L));
    }
    @Override
    public WebElement getTableRecordAtIndex(int idx) {
        return visibilityOfElementLocated(getDriver(), By.xpath("//tbody/tr[" + idx + "]"));
    }
    @Override
    public WebElement getColumnByName(WebElement tableRecord, String columnName) {
        return tableRecord.findElement(By.className("mat-column-" + columnName));
    }
}
