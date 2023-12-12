package org.oagi.score.e2e.impl.page.admin;

import org.oagi.score.e2e.AccountUpdateException;
import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.admin.AccountsPage;
import org.oagi.score.e2e.page.admin.EditAccountPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class EditAccountPageImpl extends BasePageImpl implements EditAccountPage {

    private static final By LOGIN_ID_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Login ID\")]//ancestor::div[1]/input");

    private static final By NAME_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Name\")]//ancestor::div[1]/input");

    private static final By ORGANIZATION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Organization\")]//ancestor::div[1]/input");

    private static final By DEVELOPER_CHECKBOX_LOCATOR =
            By.xpath("//span[contains(text(), \"Standard Developer\")]");

    private static final By ADMIN_CHECKBOX_LOCATOR =
            By.xpath("//span[contains(text(), \"Admin\")]");

    private static final By PASSWORD_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"New password\")]//ancestor::div[1]/input");

    private static final By CONFIRM_PASSWORD_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Confirm new password\")]//ancestor::div[1]/input");

    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");

    private static final By DISABLE_THIS_ACCOUNT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Disable this account\")]//ancestor::button[1]");

    private static final By ENABLE_THIS_ACCOUNT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Enable this account\")]//ancestor::button[1]");

    private static final By DELETE_BUTTON_LOCATOR = By.xpath("//span[contains(text(), \"Delete\")]//ancestor::button[1]");

    private final AccountsPageImpl parent;

    private final AppUserObject appUser;

    public EditAccountPageImpl(AccountsPageImpl parent, AppUserObject appUser) {
        super(parent);

        this.parent = parent;
        this.appUser = appUser;
    }

    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/account/" + this.appUser.getAppUserId()).toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Edit Account".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("mat-mdc-card-title"));
    }

    @Override
    public WebElement getLoginIDField() {
        return visibilityOfElementLocated(getDriver(), LOGIN_ID_FIELD_LOCATOR);
    }

    @Override
    public WebElement getNameField() {
        return visibilityOfElementLocated(getDriver(), NAME_FIELD_LOCATOR);
    }

    @Override
    public void setName(String name) {
        sendKeys(getNameField(), name);
    }

    @Override
    public WebElement getOrganizationField() {
        return visibilityOfElementLocated(getDriver(), ORGANIZATION_FIELD_LOCATOR);
    }

    @Override
    public void setOrganization(String organization) {
        sendKeys(getOrganizationField(), organization);
    }

    @Override
    public WebElement getDeveloperCheckbox() {
        return elementToBeClickable(getDriver(), DEVELOPER_CHECKBOX_LOCATOR);
    }

    @Override
    public WebElement getAdminCheckbox() {
        return elementToBeClickable(getDriver(), ADMIN_CHECKBOX_LOCATOR);
    }

    @Override
    public void setAdmin(boolean admin) {
        if (!admin) {
            return;
        }
        click(getAdminCheckbox());
    }

    @Override
    public WebElement getPasswordField() {
        return visibilityOfElementLocated(getDriver(), PASSWORD_FIELD_LOCATOR);
    }

    @Override
    public void setPassword(String password) {
        sendKeys(getPasswordField(), password);
    }

    @Override
    public WebElement getConfirmPasswordField() {
        return visibilityOfElementLocated(getDriver(), CONFIRM_PASSWORD_FIELD_LOCATOR);
    }

    @Override
    public void setConfirmPassword(String confirmPassword) {
        sendKeys(getConfirmPasswordField(), confirmPassword);
    }

    @Override
    public WebElement getUpdateButton() {
        return elementToBeClickable(getDriver(), UPDATE_BUTTON_LOCATOR);
    }

    @Override
    public AccountsPage updatePassword(String newPassword) throws AccountUpdateException {
        setPassword(newPassword);
        setConfirmPassword(newPassword);
        click(getUpdateButton());
        try {
            assert getSnackBar(getDriver(), "Updated").isDisplayed();
        } catch (TimeoutException e) {
            String snackBarMessage = getSnackBarMessage(getDriver());
            throw new AccountUpdateException(snackBarMessage);
        }
        return this.parent;
    }

    @Override
    public String getPasswordErrorMessage() {
        return getText(visibilityOfElementLocated(getDriver(), By.xpath("//input[@data-id=\"user.newPassword\"]/../../..//mat-error")));
    }

    @Override
    public WebElement getEnableThisAccountButton() {
        return elementToBeClickable(getDriver(), ENABLE_THIS_ACCOUNT_BUTTON_LOCATOR);
    }

    @Override
    public void enableAccount() {
        click(getEnableThisAccountButton());
        assert this.getDisableThisAccountButton().isEnabled();
    }

    @Override
    public WebElement getDisableThisAccountButton() {
        return elementToBeClickable(getDriver(), DISABLE_THIS_ACCOUNT_BUTTON_LOCATOR);
    }

    @Override
    public void disableAccount() {
        click(getDisableThisAccountButton());
        assert this.getEnableThisAccountButton().isEnabled();
    }

    @Override
    public boolean deleteAccountButtonIsPresent() {
        return isElementPresent(getDriver(), DELETE_BUTTON_LOCATOR);
    }
}
