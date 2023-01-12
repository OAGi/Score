package org.oagi.score.e2e.impl.menu;

import org.oagi.score.e2e.impl.page.DelegateBasePageImpl;
import org.oagi.score.e2e.impl.page.HomePageImpl;
import org.oagi.score.e2e.impl.page.LoginPageImpl;
import org.oagi.score.e2e.impl.page.SettingsPageImpl;
import org.oagi.score.e2e.menu.LoginIDMenu;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.LoginPage;
import org.oagi.score.e2e.page.SettingsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class LoginIDMenuImpl extends DelegateBasePageImpl implements LoginIDMenu {

    private final HomePageImpl homePage;

    private final By SIGN_IN_LABEL_LOCATOR = By.xpath("//button[contains(text(), \"Signed in as\")]");

    private final By OAGIS_TERMINOLOGY_BUTTON_LOCATOR = By.xpath("//span[contains(text(), \"OAGIS Terminology\")]//ancestor::button[1]");

    private final By OAGIS_TERMINOLOGY_CHECKED_LOCATOR = By.xpath("//span[contains(text(), \"OAGIS Terminology\")]//ancestor::button[1]//mat-icon[contains(text(), \"done\")]");

    private final By CCTS_TERMINOLOGY_BUTTON_LOCATOR = By.xpath("//span[contains(text(), \"CCTS Terminology\")]//ancestor::button[1]");

    private final By CCTS_TERMINOLOGY_CHECKED_LOCATOR = By.xpath("//span[contains(text(), \"CCTS Terminology\")]//ancestor::button[1]//mat-icon[contains(text(), \"done\")]");

    private final By SETTINGS_SUB_MENU_LOCATOR =
            By.xpath("//button[contains(text(), \"Settings\")]");

    private final By LOGOUT_BUTTON_LOCATOR = By.xpath("//button[contains(text(), \"Logout\")]");

    public LoginIDMenuImpl(HomePageImpl homePage) {
        super(homePage);
        this.homePage = homePage;
    }

    private boolean isExpanded() {
        return retry(() -> elementToBeClickable(shortWait(getDriver()), LOGOUT_BUTTON_LOCATOR).isEnabled(), false);
    }

    @Override
    public HomePage getParent() {
        return this.homePage;
    }

    @Override
    public WebElement getLoginIDMenuButton() {
        String loginID = this.homePage.getLoginID();
        String xpathExpr = "//span[contains(text(), \"" + loginID + "\")]//ancestor::button[1]";
        return elementToBeClickable(getDriver(), By.xpath(xpathExpr));
    }

    @Override
    public void expandLoginIDMenu() {
        click(getLoginIDMenuButton());
        assert getLogoutButton().isEnabled();
    }

    @Override
    public WebElement getSignInLabel() {
        if (!isExpanded()) {
            expandLoginIDMenu();
        }
        return visibilityOfElementLocated(getDriver(), SIGN_IN_LABEL_LOCATOR);
    }

    @Override
    public String getSignInLabelText() {
        return getText(getSignInLabel());
    }

    @Override
    public WebElement getOAGISTerminologyButton() {
        if (!isExpanded()) {
            expandLoginIDMenu();
        }
        return elementToBeClickable(getDriver(), OAGIS_TERMINOLOGY_BUTTON_LOCATOR);
    }

    @Override
    public void checkOAGISTerminology() {
        if (isOAGISTerminologyChecked()) {
            return;
        }
        click(getOAGISTerminologyButton());
    }

    @Override
    public boolean isOAGISTerminologyChecked() {
        if (!isExpanded()) {
            expandLoginIDMenu();
        }

        return retry(() -> visibilityOfElementLocated(shortWait(getDriver()), OAGIS_TERMINOLOGY_CHECKED_LOCATOR).isDisplayed(), false);
    }

    @Override
    public WebElement getCCTSTerminologyButton() {
        if (!isExpanded()) {
            expandLoginIDMenu();
        }
        return elementToBeClickable(getDriver(), CCTS_TERMINOLOGY_BUTTON_LOCATOR);
    }

    @Override
    public void checkCCTSTerminology() {
        if (isCCTSTerminologyChecked()) {
            return;
        }
        click(getCCTSTerminologyButton());
    }

    @Override
    public boolean isCCTSTerminologyChecked() {
        if (!isExpanded()) {
            expandLoginIDMenu();
        }

        return retry(() -> visibilityOfElementLocated(shortWait(getDriver()), CCTS_TERMINOLOGY_CHECKED_LOCATOR).isDisplayed(), false);
    }

    @Override
    public WebElement getSettingsSubMenu() {
        if (!isExpanded()) {
            expandLoginIDMenu();
        }
        return elementToBeClickable(getDriver(), SETTINGS_SUB_MENU_LOCATOR);
    }

    @Override
    public SettingsPage openSettingsSubMenu() {
        retry(() -> click(getSettingsSubMenu()));
        SettingsPage settingsPage = new SettingsPageImpl(this);
        assert settingsPage.isOpened();
        return settingsPage;
    }

    @Override
    public WebElement getLogoutButton() {
        if (!isExpanded()) {
            expandLoginIDMenu();
        }
        return elementToBeClickable(getDriver(), LOGOUT_BUTTON_LOCATOR);
    }

    @Override
    public LoginPage logout() {
        click(getLogoutButton());
        return new LoginPageImpl(this.homePage);
    }
}
