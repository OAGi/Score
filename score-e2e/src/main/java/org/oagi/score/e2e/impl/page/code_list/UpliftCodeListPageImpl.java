package org.oagi.score.e2e.impl.page.code_list;

import org.oagi.score.e2e.impl.page.BaseSearchBarPageImpl;
import org.oagi.score.e2e.obj.CodeListObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.UpliftCodeListPage;
import org.openqa.selenium.*;

import java.math.BigInteger;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class UpliftCodeListPageImpl extends BaseSearchBarPageImpl implements UpliftCodeListPage {
    private static final By SOURCE_BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Source Branch\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By TARGET_BRANCH_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Target Branch\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By STATE_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"State\")]//ancestor::mat-form-field[1]//mat-select");
    private static final By OWNER_SELECT_FIELD_LOCATOR =
            By.xpath("//*[contains(text(), \"Owner\")]//ancestor::mat-form-field[1]//mat-select");
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
        selectBranch(SOURCE_BRANCH_SELECT_FIELD_LOCATOR, sourceBranch);
    }

    @Override
    public WebElement getSourceBranchSelectField() {
        return visibilityOfElementLocated(getDriver(), SOURCE_BRANCH_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setTargetRelease(String targetBranch) {
        selectBranch(TARGET_BRANCH_SELECT_FIELD_LOCATOR, targetBranch);
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
    public WebElement getNameField() {
        return getInputFieldInSearchBar();
    }

    @Override
    public void setCodeList(String name) {
        sendKeys(getNameField(), name);
    }

    @Override
    public WebElement getOwnerSelectField() {
        return visibilityOfElementLocated(getDriver(), OWNER_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setOwner(String owner) {
        retry(() -> {
            WebElement ownerSelect = openMatSelect(getDriver(), OWNER_SELECT_FIELD_LOCATOR);
            sendKeys(matSelectSearchField(getDriver(), ownerSelect), owner);
            click(matSelectOption(getDriver(), ownerSelect, owner));
            escape(getDriver());
        });
    }

    @Override
    public WebElement getStateSelectField() {
        return visibilityOfElementLocated(getDriver(), STATE_SELECT_FIELD_LOCATOR);
    }

    @Override
    public void setState(String state) {
        retry(() -> {
            WebElement stateSelect = openMatSelect(getDriver(), STATE_SELECT_FIELD_LOCATOR);
            click(matSelectOption(getDriver(), stateSelect, state));
            escape(getDriver());
        });
    }

    @Override
    public void hitSearchButton() {
        click(getSearchButton());
        waitFor(ofMillis(100L));
        invisibilityOfLoadingContainerElement(getDriver());
    }

    private void selectBranch(By selectLocator, String branch) {
        retry(() -> {
            WebElement branchSelect = openMatSelect(getDriver(), selectLocator);
            sendKeys(matSelectSearchField(getDriver(), branchSelect), branch);
            click(matSelectOption(getDriver(), branchSelect, branch));
            escape(getDriver());
            waitFor(ofMillis(100L));
            invisibilityOfLoadingContainerElement(getDriver());
        });
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
        showAdvancedSearchPanel();
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
