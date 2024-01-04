package org.oagi.score.e2e.impl.page.admin;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.admin.NewAccountPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class NewAccountPageImpl extends BasePageImpl implements NewAccountPage {

    private static final By LOGIN_ID_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Login ID\")]//ancestor::div[1]/input");

    private static final By NAME_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Name\")]//ancestor::div[1]/input");

    private static final By ORGANIZATION_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Organization\")]//ancestor::div[1]/input");

    private static final By DEVELOPER_CHECKBOX_LOCATOR =
            By.xpath("//label[contains(text(), \"Standard Developer\")]");

    private static final By ADMIN_CHECKBOX_LOCATOR =
            By.xpath("//span[contains(text(), \"Admin\")]");

    private static final By PASSWORD_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Password\")]//ancestor::div[1]/input");

    private static final By CONFIRM_PASSWORD_FIELD_LOCATOR =
            By.xpath("//mat-label[contains(text(), \"Confirm password\")]//ancestor::div[1]/input");

    private static final By CREATE_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"Create\")]//ancestor::button[1]");

    public NewAccountPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/account/create").toString();
    }

    @Override
    public void openPage() {
        String url = getPageUrl();
        getDriver().get(url);
        assert "Create Account".equals(getText(getTitle()));
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
    public void setLoginID(String loginID) {
        sendKeys(getLoginIDField(), loginID);
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
    public void setDeveloper(boolean developer) {
        if (!developer) {
            return;
        }
        click(getDeveloperCheckbox());
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
    public WebElement getCreateButton() {
        return elementToBeClickable(getDriver(), CREATE_BUTTON_LOCATOR);
    }

    @Override
    public void createNewAccount(AppUserObject user) {
        setLoginID(user.getLoginId());
        setName(user.getName());
        setOrganization(user.getOrganization());
        setDeveloper(user.isDeveloper());
        setAdmin(user.isAdmin());
        setPassword(user.getPassword());
        setConfirmPassword(user.getPassword());
        click(getCreateButton());
        assert getSnackBar(getDriver(), "Created").isDisplayed();
    }

    @Override
    public String getPasswordErrorMessage() {
        return getText(visibilityOfElementLocated(getDriver(), By.xpath("//input[@data-id=\"user.newPassword\"]/../../../..//mat-error")));
    }
}
