package org.oagi.score.e2e.TS_49_MultiTenantBusinessContextVisibility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.TenantObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.admin.TenantBusinessContextPage;
import org.oagi.score.e2e.page.admin.TenantPage;
import org.oagi.score.e2e.page.admin.TenantUserPage;
import org.openqa.selenium.By;

import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

/** Admin &gt; Tenant: an Admin creates tenants and associates users and business contexts with them, and removes them again. */
public class TC_49_2_TenantAdministration extends AbstractTenantTest {

    private static final By NEW_TENANT_BUTTON = By.xpath("//span[contains(text(), \"New Tenant\")]");

    @Test
    @DisplayName("TC_49_2_TA_1: an Admin creates a tenant, and it is listed")
    public void admin_creates_tenant() {
        String name = "tenant_" + randomAlphanumeric(8);
        var session = adminOpensTheTenantPage(admin());

        session.tenantPage().createTenant(name);
        session.tenantPage().search(name);

        assertEquals(List.of(name), session.tenantPage().getListedTenantNames());
        getAPIFactory().getTenantAPI().deleteTenantByName(name);
    }

    @Test
    @DisplayName("TC_49_2_TA_2: an Admin associates a user with a tenant and dissociates the user again")
    public void admin_associates_and_dissociates_user() {
        AppUserObject user = endUser();
        TenantObject tenant = tenant();
        var session = adminOpensTheTenantPage(admin());
        session.tenantPage().search(tenant.getName());
        TenantUserPage users = session.tenantPage().openManageUsers(tenant.getName());

        users.search(user.getLoginId());
        assertEquals(List.of(), users.getListedKeys(), "The tenant has no user yet");

        users.switchToAddView();
        users.search(user.getLoginId());
        assertEquals(List.of(user.getLoginId()), users.getListedKeys(), "The user can be added");
        users.add(user.getLoginId());
        users.search(user.getLoginId());
        assertEquals(List.of(), users.getListedKeys(), "The user is no longer offered to be added");

        users.switchToListView();
        users.search(user.getLoginId());
        assertEquals(List.of(user.getLoginId()), users.getListedKeys(), "The user belongs to the tenant");

        users.remove(user.getLoginId());
        users.search(user.getLoginId());
        assertEquals(List.of(), users.getListedKeys(), "The user was removed");

        users.switchToAddView();
        users.search(user.getLoginId());
        assertEquals(List.of(user.getLoginId()), users.getListedKeys(), "The user can be added again");
    }

    @Test
    @DisplayName("TC_49_2_TA_3: an Admin associates a business context with a tenant and dissociates it again")
    public void admin_associates_and_dissociates_business_context() {
        AppUserObject creator = endUser();
        BusinessContextObject context = businessContext(creator);
        TenantObject tenant = tenant();
        var session = adminOpensTheTenantPage(admin());
        session.tenantPage().search(tenant.getName());
        TenantBusinessContextPage contexts = session.tenantPage().openManageBusinessContexts(tenant.getName());

        contexts.search(context.getName());
        assertEquals(List.of(), contexts.getListedKeys(), "The tenant has no business context yet");

        contexts.switchToAddView();
        contexts.search(context.getName());
        assertEquals(List.of(context.getName()), contexts.getListedKeys(), "The business context can be added");
        contexts.add(context.getName());
        contexts.search(context.getName());
        assertEquals(List.of(), contexts.getListedKeys(), "The business context is no longer offered to be added");

        contexts.switchToListView();
        contexts.search(context.getName());
        assertEquals(List.of(context.getName()), contexts.getListedKeys(), "The business context belongs to the tenant");

        contexts.remove(context.getName());
        contexts.search(context.getName());
        assertEquals(List.of(), contexts.getListedKeys(), "The business context was removed");

        contexts.switchToAddView();
        contexts.search(context.getName());
        assertEquals(List.of(context.getName()), contexts.getListedKeys(), "The business context can be added again");
    }

    @Test
    @DisplayName("TC_49_2_TA_4: only an Admin can manage tenants")
    public void only_admin_can_manage_tenants() {
        for (AppUserObject user : List.of(endUser(), developer())) {
            HomePage homePage = loginPage().signIn(user.getLoginId(), user.getPassword());
            assertFalse(homePage.hasAdminMenu(), "No 'Admin' menu for " + user.getLoginId());
            getDriver().get(getConfig().getBaseUrl().resolve("/tenant").toString());
            waitFor(java.time.Duration.ofSeconds(2));
            assertTrue(getDriver().findElements(NEW_TENANT_BUTTON).isEmpty(), "No tenant page for " + user.getLoginId());
            getDriver().get(getConfig().getBaseUrl().toString());
            homePage.logout();
        }
    }

    @Test
    @DisplayName("TC_49_2_TA_5: the Tenant menu exists only while the multi-tenant mode is on")
    public void tenant_menu_exists_only_in_multi_tenant_mode() {
        AppUserObject admin = admin();
        HomePage homePage = loginPage().signIn(admin.getLoginId(), admin.getPassword());
        assertTrue(homePage.getAdminMenu().hasTenantSubMenu());
        homePage.logout();

        getAPIFactory().getApplicationSettingsAPI().setTenantEnable(false);
        try {
            HomePage again = loginPage().signIn(admin.getLoginId(), admin.getPassword());
            assertFalse(again.getAdminMenu().hasTenantSubMenu());
            getDriver().get(getConfig().getBaseUrl().resolve("/tenant").toString());
            waitFor(java.time.Duration.ofSeconds(2));
            assertTrue(getDriver().findElements(NEW_TENANT_BUTTON).isEmpty());
        } finally {
            getAPIFactory().getApplicationSettingsAPI().setTenantEnable(true);
        }
    }

}
