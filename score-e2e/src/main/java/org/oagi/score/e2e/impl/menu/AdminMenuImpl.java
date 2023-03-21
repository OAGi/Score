package org.oagi.score.e2e.impl.menu;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.DelegateBasePageImpl;
import org.oagi.score.e2e.impl.page.admin.AccountsPageImpl;
import org.oagi.score.e2e.impl.page.admin.PendingSSOPageImpl;
import org.oagi.score.e2e.menu.AdminMenu;
import org.oagi.score.e2e.page.admin.AccountsPage;
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

    private final By ACCOUNTS_SUB_MENU_LOCATOR =
            By.xpath("//button[contains(text(), \"Accounts\")]");

    private final By PENDING_SSO_SUB_MENU_LOCATOR =
            By.xpath("//button[contains(text(), \"Pending SSO\")]");

    public AdminMenuImpl(BasePageImpl delegate) {
        super(delegate);
    }

    private boolean isExpanded() {
        return retry(() -> elementToBeClickable(shortWait(getDriver()), ACCOUNTS_SUB_MENU_LOCATOR).isEnabled(), false);
    }

    @Override
    public WebElement getAdminMenu() {
        return elementToBeClickable(getDriver(), ADMIN_MENU_LOCATOR);
    }

    @Override
    public void expandAdminMenu() {
        click(getAdminMenu());
        assert getAccountsSubMenu().isEnabled();
    }

    @Override
    public WebElement getAccountsSubMenu() {
        if (!isExpanded()) {
            expandAdminMenu();
        }
        return elementToBeClickable(getDriver(), ACCOUNTS_SUB_MENU_LOCATOR);
    }

    @Override
    public AccountsPage openAccountsSubMenu() {
        AccountsPage accountsPage = new AccountsPageImpl(this);
        try {
            click(getAccountsSubMenu());
        } catch (WebDriverException e) {
            logger.warn("Failed to click the 'Accounts' menu.", e);
            accountsPage.openPage();
        }
        assert accountsPage.isOpened();
        return accountsPage;
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
            click(getPendingSSOSubMenu());
        } catch (WebDriverException e) {
            logger.warn("Failed to click the 'Pending SSO' menu.", e);
            pendingSSOPage.openPage();
        }
        assert pendingSSOPage.isOpened();
        return pendingSSOPage;
    }
}
