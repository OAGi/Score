package org.oagi.score.e2e.TS_49_MultiTenantBusinessContextVisibility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.TenantObject;

import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.jupiter.api.Assertions.*;

/** Tenant names are unique, a tenant can be renamed, and a discarded tenant takes its associations with it. */
public class TC_49_6_TenantManagement extends AbstractTenantTest {

    @Test
    @DisplayName("TC_49_6_TA_1: a tenant cannot be created with the name of another tenant")
    public void tenant_cannot_be_created_with_an_existing_name() {
        TenantObject existing = tenant();
        var tenantPage = adminOpensTheTenantPage(admin()).tenantPage();

        String message = tenantPage.tryCreateTenant(existing.getName());

        assertEquals("Tenant with name " + existing.getName() + " already exists!", message);
        tenantPage.openPage();
        tenantPage.search(existing.getName());
        assertEquals(List.of(existing.getName()), tenantPage.getListedTenantNames());
    }

    @Test
    @DisplayName("TC_49_6_TA_2: a renamed tenant is listed by its new name and keeps its users and business contexts")
    public void renamed_tenant_keeps_its_associations() {
        AppUserObject user = endUser();
        TenantObject tenant = tenant();
        BusinessContextObject context = businessContext(user);
        getAPIFactory().getTenantAPI().addUserToTenant(user, tenant);
        getAPIFactory().getTenantAPI().addBusinessContextToTenant(context, tenant);
        String newName = "tenant_" + randomAlphanumeric(8);

        var session = adminOpensTheTenantPage(admin());
        session.tenantPage().search(tenant.getName());
        var edit = session.tenantPage().openTenant(tenant.getName());
        assertEquals(tenant.getName(), edit.getName());
        edit.setName(newName);
        assertEquals("Updated", edit.update());
        session.tenantPage().openPage();
        session.tenantPage().search(newName);
        assertEquals(List.of(newName), session.tenantPage().getListedTenantNames());
        session.tenantPage().search(tenant.getName());
        assertEquals(List.of(), session.tenantPage().getListedTenantNames(), "The old name is gone");
        session.homePage().logout();

        assertEquals(Set.of(context.getName()), offeredTo(user), "The user is still offered the business context of the tenant");
    }

    @Test
    @DisplayName("TC_49_6_TA_3: a tenant cannot be renamed to the name of another tenant")
    public void tenant_cannot_be_renamed_to_an_existing_name() {
        TenantObject tenant = tenant();
        TenantObject other = tenant();

        var session = adminOpensTheTenantPage(admin());
        session.tenantPage().search(tenant.getName());
        var edit = session.tenantPage().openTenant(tenant.getName());
        edit.setName(other.getName());

        assertEquals("Tenant with name " + other.getName() + " already exists!", edit.update());
        session.tenantPage().openPage();
        session.tenantPage().search(tenant.getName());
        assertEquals(List.of(tenant.getName()), session.tenantPage().getListedTenantNames(), "The tenant keeps its name");
    }

    @Test
    @DisplayName("TC_49_6_TA_4: discarding a tenant warns about its associations, can be cancelled, and removes the tenant")
    public void admin_discards_tenant() {
        AppUserObject user = endUser();
        TenantObject tenant = tenant();
        BusinessContextObject context = businessContext(user);
        getAPIFactory().getTenantAPI().addUserToTenant(user, tenant);
        getAPIFactory().getTenantAPI().addBusinessContextToTenant(context, tenant);
        assertTrue(canCreateBie(user), "Before, the user can create a BIE");

        var session = adminOpensTheTenantPage(admin());
        session.tenantPage().search(tenant.getName());
        var edit = session.tenantPage().openTenant(tenant.getName());
        String warning = edit.openDiscardDialog();
        assertTrue(warning.contains("1 Account(s) and 1 Business context(s)"), warning);
        edit.cancelDiscard();
        edit.openDiscardDialog();
        assertEquals("Tenant is deleted", edit.confirmDiscard());
        session.tenantPage().openPage();
        session.tenantPage().search(tenant.getName());
        assertEquals(List.of(), session.tenantPage().getListedTenantNames(), "The tenant is gone");
        session.homePage().logout();

        assertFalse(canCreateBie(user), "The user belonged to no other tenant, so cannot create a BIE any more");
        assertNotNull(getAPIFactory().getBusinessContextAPI().getBusinessContextByName(context.getName()),
                "The business context itself is kept");
    }

}
