package org.oagi.score.e2e.impl.menu;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.DelegateBasePageImpl;
import org.oagi.score.e2e.impl.page.admin.AccountPageImpl;
import org.oagi.score.e2e.impl.page.admin.PendingSSOPageImpl;
import org.oagi.score.e2e.menu.AdminMenu;
import org.oagi.score.e2e.page.admin.AccountPage;
import org.oagi.score.e2e.page.admin.PendingSSOPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class AdminMenuImpl extends DelegateBasePageImpl implements AdminMenu {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final By ADMIN_MENU_LOCATOR =
            By.xpath("//mat-toolbar-row/button/span[contains(text(), \"Admin\")]//ancestor::button[1]");

    private final By ACCOUNT_SUB_MENU_LOCATOR =
            By.xpath("//span[contains(text(), \"Account\")]//ancestor::button[1]");

    private final By PENDING_SSO_SUB_MENU_LOCATOR =
            By.xpath("//span[contains(text(), \"Pending SSO\")]//ancestor::button[1]");

    public AdminMenuImpl(BasePageImpl delegate) {
        super(delegate);
    }

    private boolean isExpanded() {
        return retry(() -> elementToBeClickable(shortWait(getDriver()), ACCOUNT_SUB_MENU_LOCATOR).isEnabled(), false);
    }

    @Override
    public WebElement getAdminMenu() {
        return elementToBeClickable(getDriver(), ADMIN_MENU_LOCATOR);
    }

    @Override
    public void expandAdminMenu() {
        click(getAdminMenu());
        assert getAccountSubMenu().isEnabled();
    }

    @Override
    public WebElement getAccountSubMenu() {
        if (!isExpanded()) {
            expandAdminMenu();
        }
        return elementToBeClickable(getDriver(), ACCOUNT_SUB_MENU_LOCATOR);
    }

    @Override
    public AccountPage openAccountSubMenu() {
        AccountPage accountPage = new AccountPageImpl(this);
        try {
            retry(() -> click(getDriver(), getAccountSubMenu()));
        } catch (WebDriverException e) {
            logger.warn("Failed to click the 'Account' menu.", e);
            accountPage.openPage();
        }
        assert accountPage.isOpened();
        return accountPage;
    }

    @Override
    public WebElement getPendingSSOSubMenu() {
        if (!isExpanded()) {
            expandAdminMenu();
        }
        return elementToBeClickable(getDriver(), PENDING_SSO_SUB_MENU_LOCATOR);
    }

    @Override
    public PendingSSOPage openPendingSSOSubMenu() {
        PendingSSOPage pendingSSOPage = new PendingSSOPageImpl(this);
        try {
            retry(() -> click(getDriver(), getPendingSSOSubMenu()));
        } catch (WebDriverException e) {
            logger.warn("Failed to click the 'Pending SSO' menu.", e);
            pendingSSOPage.openPage();
        }
        assert pendingSSOPage.isOpened();
        return pendingSSOPage;
    }
}
