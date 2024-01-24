package org.oagi.score.e2e.impl.page;

import org.oagi.score.e2e.AccountUpdateException;
import org.oagi.score.e2e.impl.menu.LoginIDMenuImpl;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.SettingsPasswordPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class SettingsPasswordPageImpl extends BasePageImpl implements SettingsPasswordPage {

    private static final By OLD_PASSWORD_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Old password\")]//ancestor::div[1]/input");

    private static final By NEW_PASSWORD_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"New password\")]//ancestor::div[1]/input");

    private static final By CONFIRM_NEW_PASSWORD_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Confirm new password\")]//ancestor::div[1]/input");

    private static final By UPDATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Update\")]//ancestor::button[1]");

    private final LoginIDMenuImpl loginIDMenuPage;

    public SettingsPasswordPageImpl(LoginIDMenuImpl parent) {
        super(parent);
        this.loginIDMenuPage = parent;
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/settings/password").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Change password".equals(getText(getTitle()));
    }

    @Override
    public WebElement getTitle() {
        return visibilityOfElementLocated(getDriver(), By.className("title"));
    }

    @Override
    public WebElement getOldPasswordField() {
        return visibilityOfElementLocated(getDriver(), OLD_PASSWORD_FIELD_LOCATOR);
    }

    @Override
    public void setOldPassword(String oldPassword) {
        sendKeys(getOldPasswordField(), oldPassword);
    }

    @Override
    public WebElement getNewPasswordField() {
        return visibilityOfElementLocated(getDriver(), NEW_PASSWORD_FIELD_LOCATOR);
    }

    @Override
    public void setNewPassword(String newPassword) {
        sendKeys(getNewPasswordField(), newPassword);
    }

    @Override
    public WebElement getConfirmNewPasswordField() {
        return visibilityOfElementLocated(getDriver(), CONFIRM_NEW_PASSWORD_FIELD_LOCATOR);
    }

    @Override
    public void setConfirmNewPassword(String confirmNewPassword) {
        sendKeys(getConfirmNewPasswordField(), confirmNewPassword);
    }

    @Override
    public WebElement getUpdateButton() {
        return elementToBeClickable(getDriver(), UPDATE_BUTTON_LOCATOR);
    }

    @Override
    public HomePage updatePassword(String oldPassword, String newPassword) throws AccountUpdateException {
        return updatePassword(oldPassword, newPassword, newPassword);
    }

    @Override
    public HomePage updatePassword(String oldPassword, String newPassword, String confirmNewPassword)
            throws AccountUpdateException {
        setOldPassword(oldPassword);
        setNewPassword(newPassword);
        setConfirmNewPassword(confirmNewPassword);
        click(getUpdateButton());
        try {
            assert getSnackBar(getDriver(), "Updated").isDisplayed();
        } catch (TimeoutException e) {
            String snackBarMessage = getText(getMultiActionSnackBar(getDriver()).getMessageElement());
            throw new AccountUpdateException(snackBarMessage);
        }
        return loginIDMenuPage.getParent();
    }

    @Override
    public String getPasswordErrorMessage() {
        return getText(visibilityOfElementLocated(getDriver(), By.xpath("//mat-label[contains(text(), \"New password\")]/../../../../..//mat-error")));
    }

}
