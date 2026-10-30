package org.oagi.score.e2e.impl.page.admin;

import org.oagi.score.e2e.page.BasePage;
import org.oagi.score.e2e.page.admin.TenantBusinessContextPage;
import org.oagi.score.e2e.page.admin.TenantEditPage;
import org.oagi.score.e2e.page.admin.TenantPage;
import org.oagi.score.e2e.page.admin.TenantUserPage;
import org.openqa.selenium.By;

import java.util.List;

import static java.time.Duration.ofMillis;
import static org.oagi.score.e2e.impl.PageHelper.*;

public class TenantPageImpl extends AbstractTenantTablePageImpl implements TenantPage {

    private static final By NEW_TENANT_BUTTON_LOCATOR =
            By.xpath("//span[contains(text(), \"New Tenant\")]//ancestor::button[1]");

    private static final By NAME_FIELD_LOCATOR =
            By.xpath("//mat-label[normalize-space(.) = \"Name\"]//ancestor::mat-form-field[1]//input");

    private static final By CREATE_BUTTON_LOCATOR =
            By.xpath("//score-detail-page//button[normalize-space(.) = \"Create\"]");

    public TenantPageImpl(BasePage parent) {
        super(parent);
    }

    @Override
    protected String getPageUrl() {
        return getConfig().getBaseUrl().resolve("/tenant").toString();
    }

    @Override
    protected String getKeyColumnClass() {
        return "mat-column-name";
    }

    @Override
    public void openPage() {
        getDriver().get(getPageUrl());
        assert isOpened();
    }

    @Override
    public void createTenant(String name) {
        tryCreateTenant(name);
        // The page goes back to the list once the tenant is created.
        visibilityOfElementLocated(getDriver(), NEW_TENANT_BUTTON_LOCATOR);
        waitFor(ofMillis(500L));
    }

    @Override
    public String tryCreateTenant(String name) {
        retry(() -> click(elementToBeClickable(getDriver(), NEW_TENANT_BUTTON_LOCATOR)));
        sendKeys(visibilityOfElementLocated(getDriver(), NAME_FIELD_LOCATOR), name);
        retry(() -> click(elementToBeClickable(getDriver(), CREATE_BUTTON_LOCATOR)));
        return getSnackBarMessage(getDriver());
    }

    @Override
    public TenantEditPage openTenant(String name) {
        retry(() -> click(elementToBeClickable(getDriver(), By.xpath(
                "//tbody/tr/td[contains(@class, \"mat-column-name\")]//a[normalize-space(.) = \"" + name + "\"]"))));
        TenantEditPage page = new TenantEditPageImpl(this);
        assert page.isOpened();
        return page;
    }

    @Override
    public List<String> getListedTenantNames() {
        return getStableKeys();
    }

    @Override
    public TenantUserPage openManageUsers(String name) {
        retry(() -> click(buttonOfRow(name, "Manage Users")));
        TenantUserPage page = new TenantUserPageImpl(this);
        assert page.isOpened();
        return page;
    }

    @Override
    public TenantBusinessContextPage openManageBusinessContexts(String name) {
        retry(() -> click(buttonOfRow(name, "Manage Business Context")));
        TenantBusinessContextPage page = new TenantBusinessContextPageImpl(this);
        assert page.isOpened();
        return page;
    }

}
