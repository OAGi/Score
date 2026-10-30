package org.oagi.score.e2e.TS_49_MultiTenantBusinessContextVisibility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.TenantObject;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What a user is offered when creating a BIE follows what an Admin associates with the tenants in Admin &gt; Tenant:
 * the user's own tenants, and the business contexts of those tenants.
 */
public class TC_49_3_VisibilityFollowsTenantAssociations extends AbstractTenantTest {

    private static Set<String> names(BusinessContextObject... contexts) {
        return java.util.Arrays.stream(contexts).map(BusinessContextObject::getName).collect(java.util.stream.Collectors.toSet());
    }

    @Test
    @DisplayName("TC_49_3_TA_1: a user is offered the business contexts of a tenant once an Admin adds the user to it, and no longer once removed")
    public void adding_and_removing_a_user_changes_what_the_user_is_offered() {
        AppUserObject admin = admin();
        AppUserObject user = endUser();
        TenantObject tenant = tenant();
        BusinessContextObject context = businessContext(user);
        getAPIFactory().getTenantAPI().addBusinessContextToTenant(context, tenant);

        assertFalse(canCreateBie(user), "A user of no tenant cannot create a BIE");

        var session = adminOpensTheTenantPage(admin);
        session.tenantPage().search(tenant.getName());
        var users = session.tenantPage().openManageUsers(tenant.getName());
        users.switchToAddView();
        users.search(user.getLoginId());
        users.add(user.getLoginId());
        session.homePage().logout();

        assertEquals(names(context), offeredTo(user), "Added to the tenant, the user is offered its business context");

        session = adminOpensTheTenantPage(admin);
        session.tenantPage().search(tenant.getName());
        users = session.tenantPage().openManageUsers(tenant.getName());
        users.search(user.getLoginId());
        users.remove(user.getLoginId());
        session.homePage().logout();

        assertFalse(canCreateBie(user), "Removed from the tenant, the user cannot create a BIE again");
    }

    @Test
    @DisplayName("TC_49_3_TA_2: the users of a tenant are offered a business context once an Admin adds it to the tenant, and no longer once removed")
    public void adding_and_removing_a_business_context_changes_what_the_users_are_offered() {
        AppUserObject admin = admin();
        AppUserObject user = endUser();
        TenantObject tenant = tenant();
        getAPIFactory().getTenantAPI().addUserToTenant(user, tenant);
        BusinessContextObject first = businessContext(user);
        BusinessContextObject second = businessContext(user);
        getAPIFactory().getTenantAPI().addBusinessContextToTenant(first, tenant);

        assertEquals(names(first), offeredTo(user));

        var session = adminOpensTheTenantPage(admin);
        session.tenantPage().search(tenant.getName());
        var contexts = session.tenantPage().openManageBusinessContexts(tenant.getName());
        contexts.switchToAddView();
        contexts.search(second.getName());
        contexts.add(second.getName());
        session.homePage().logout();

        assertEquals(names(first, second), offeredTo(user), "A business context added to the tenant is offered");

        session = adminOpensTheTenantPage(admin);
        session.tenantPage().search(tenant.getName());
        contexts = session.tenantPage().openManageBusinessContexts(tenant.getName());
        contexts.search(first.getName());
        contexts.remove(first.getName());
        session.homePage().logout();

        assertEquals(names(second), offeredTo(user), "A business context removed from the tenant is not offered");

        session = adminOpensTheTenantPage(admin);
        session.tenantPage().search(tenant.getName());
        contexts = session.tenantPage().openManageBusinessContexts(tenant.getName());
        contexts.search(second.getName());
        contexts.remove(second.getName());
        session.homePage().logout();

        assertTrue(canCreateBie(user), "The user still belongs to the tenant");
        assertEquals(Set.of(), offeredTo(user), "No business context is left to be offered");
    }

}
