package org.oagi.score.e2e.impl.menu;

import org.oagi.score.e2e.impl.page.BasePageImpl;
import org.oagi.score.e2e.impl.page.DelegateBasePageImpl;
import org.oagi.score.e2e.impl.page.admin.AccountPageImpl;
import org.oagi.score.e2e.impl.page.admin.PendingSSOPageImpl;
import org.oagi.score.e2e.impl.page.admin.TenantPageImpl;
import org.oagi.score.e2e.menu.AdminMenu;
import org.oagi.score.e2e.page.admin.AccountPage;
import org.oagi.score.e2e.page.admin.PendingSSOPage;
import org.oagi.score.e2e.page.admin.TenantPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.oagi.score.e2e.impl.PageHelper.*;

public class AdminMenuImpl extends DelegateBasePageImpl implements AdminMenu {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final By ADMIN_MENU_LOCATOR =
            By.xpath("//mat-toolbar-row//button[contains(., \"Admin\")]");

    private final By ACCOUNT_SUB_MENU_LOCATOR =
            By.xpath("//button[contains(., \"Account\")]");

    private final By PENDING_SSO_SUB_MENU_LOCATOR =
            By.xpath("//button[contains(., \"Pending SSO\")]");

    private final By TENANT_SUB_MENU_LOCATOR =
            By.xpath("//button[contains(@class, \"mat-mdc-menu-item\") and normalize-space(.) = \"Tenant\"]");

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

    @Override
    public boolean hasTenantSubMenu() {
        if (!isExpanded()) {
            expandAdminMenu();
        }
        return !getDriver().findElements(TENANT_SUB_MENU_LOCATOR).isEmpty();
    }

    @Override
    public WebElement getTenantSubMenu() {
        if (!isExpanded()) {
            expandAdminMenu();
        }
        return elementToBeClickable(getDriver(), TENANT_SUB_MENU_LOCATOR);
    }

    @Override
    public TenantPage openTenantSubMenu() {
        TenantPage tenantPage = new TenantPageImpl(this);
        try {
            retry(() -> click(getDriver(), getTenantSubMenu()));
        } catch (WebDriverException e) {
            logger.warn("Failed to click the 'Tenant' menu.", e);
            tenantPage.openPage();
        }
        assert tenantPage.isOpened();
        return tenantPage;
    }

}
