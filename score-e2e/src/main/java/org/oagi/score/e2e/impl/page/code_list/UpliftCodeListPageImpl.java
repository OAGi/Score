package org.oagi.score.e2e.impl.page.code_list;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.CodeListObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.UpliftCodeListPage;
import org.openqa.selenium.*;

import java.math.BigInteger;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class UpliftCodeListPageImpl extends BasePageImpl implements UpliftCodeListPage {
    private static final By SOURCE_BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Source Branch\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By TARGET_BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Target Branch\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By STATE_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"State\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By OWNER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Owner\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By DROPDOWN_SEARCH_FIELD_LOCATOR =
            By.xpath("//input[@aria-label=\"dropdown search\"]");
    private static final By SEARCH_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Search\")]//ancestor::button[1]");
    private static final By CODE_LIST_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Name\")]//ancestor::mat-form-field//input");
    private static final By UPLIFT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Uplift\")]//ancestor::button[1]");

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
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public void setSourceRelease(String sourceBranch) {
        retry(() -> {
            click(getSourceBranchSelectField());
            WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//div[contains(@class, \"cdk-overlay-pane\")]//mat-option//span[text() = \"" + sourceBranch + "\"]"));
            click(searchedSelectField);
            escape(getDriver());
        });
    }

    @Override
    public WebElement getSourceBranchSelectField() {
        return visibilityOfElementLocated(getDriver(), SOURCE_BRANCH_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setTargetRelease(String targetBranch) {
        retry(() -> {
            click(getTargetBranchSelectField());
            WebElement searchedSelectField = visibilityOfElementLocated(getDriver(),
                    By.xpath("//div[contains(@class, \"cdk-overlay-pane\")]//mat-option//span[text() = \"" + targetBranch + "\"]"));
            click(searchedSelectField);
            escape(getDriver());
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
            waitFor(ofMillis(1000L));
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

    @Override
    public EditCodeListPage hitUpliftButton(CodeListObject codeList, ReleaseObject sourceRelease, ReleaseObject targetRelease) {
        setSourceRelease(sourceRelease.getReleaseNumber());
        setTargetRelease(targetRelease.getReleaseNumber());
        setOwner(getAPIFactory().getAppUserAPI().getAppUserByID(codeList.getOwnerUserId()).getLoginId());
        setState(codeList.getState());
        retry(() -> {
            selectCodeList(codeList.getName());
            click(getUpliftButton(true));
            waitFor(ofMillis(1000L));
        });

        String currentUrl = getDriver().getCurrentUrl();
        BigInteger codeListManifestId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/code_list/") + "/code_list/".length()));
        CodeListObject upliftedCodeList = getAPIFactory().getCodeListAPI().getCodeListByManifestId(codeListManifestId);
        EditCodeListPage editCodeListPage = new EditCodeListPageImpl(this, upliftedCodeList);
        assert editCodeListPage.isOpened();
        return editCodeListPage;
    }

    @Override
    public WebElement getUpliftButton(boolean enabled) {
        if (enabled) {
            return elementToBeClickable(getDriver(), UPLIFT_BUTTON_LOCATOR);
        } else {
            return visibilityOfElementLocated(getDriver(), UPLIFT_BUTTON_LOCATOR);
        }
    }

}
