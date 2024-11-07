package org.oagi.score.e2e.impl.page.admin;

import org.oagi.score.e2e.impl.page.BaseSearchBarPageImpl;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.admin.AccountsPage;
import org.oagi.score.e2e.page.admin.EditAccountPage;
import org.oagi.score.e2e.page.admin.NewAccountPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class AccountsPageImpl extends BaseSearchBarPageImpl implements AccountsPage {

    private static final By NEW_ACCOUNT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"New Account\")]//ancestor::button[1]");

    private static final By NAME_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Name\")]//ancestor::div[1]/input");

    private static final By ORGANIZATION_FIELD_LOCATOR =
            By.xpath("//span[contains(text(), \"Organization\")]//ancestor::div[1]/input");

    private static final By STATUS_SELECT_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Status\")]//ancestor::div[1]/mat-select");

    public AccountsPageImpl(BasePage parent) {
        super(parent);
    }

    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/account").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Accounts".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public WebElement getLoginIDField() {
        return getInputFieldInSearchBar();
    }

    @Override
    public WebElement getNameField() {
        return visibilityOfElementLocated(getDriver(), NAME_FIELD_LOCATOR);
    }

    @Override
    public WebElement getOrganizationField() {
        return visibilityOfElementLocated(getDriver(), ORGANIZATION_FIELD_LOCATOR);
    }

    @Override
    public WebElement getStatusSelectField() {
        return visibilityOfElementLocated(getDriver(), STATUS_SELECT_FIELD_LOCATOR);
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
    public WebElement getNewAccountButton() {
        return elementToBeClickable(getDriver(), NEW_ACCOUNT_BUTTON_LOCATOR);
    }

    @Override
    public NewAccountPage openNewAccountPage() {
        click(getNewAccountButton());
        NewAccountPage newAccountPage = new NewAccountPageImpl(this);
        assert newAccountPage.isOpened();
        return newAccountPage;
    }

    @Override
    public EditAccountPage openEditAccountPageByLoginID(String loginID) throws NoSuchElementException {
        sendKeys(getLoginIDField(), loginID);
        click(getSearchButton());

        return retry(() -> {
            WebElement td;
            try {
                WebElement tr = getTableRecordAtIndex(1);
                td = getColumnByName(tr, "loginId");
            } catch (TimeoutException e) {
                throw new NoSuchElementException("Cannot locate an account using " + loginID, e);
            }
            if (!loginID.equals(getText(td))) {
                throw new NoSuchElementException("Cannot locate an account using " + loginID);
            }
            WebElement tdLoginID = td.findElement(By.tagName("a"));

            click(tdLoginID);

            AppUserObject appUser = getAPIFactory().getAppUserAPI().getAppUserByLoginID(loginID);
            EditAccountPage editAccountPage = new EditAccountPageImpl(this, appUser);
            assert editAccountPage.isOpened();

            return editAccountPage;
        });
    }
}
