package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.TenantObject;

/**
 * APIs for the tenants of the multi-tenant mode: the tenant itself, its users ({@code user_tenant})
 * and its business contexts ({@code tenant_business_ctx}).
 */
public interface TenantAPI {

    /**
     * Create a tenant with a random name.
     *
     * @return the created tenant
     */
    TenantObject createRandomTenant();

    /**
     * Make the user a user of the tenant.
     *
     * @param appUser the user
     * @param tenant  the tenant
     */
    void addUserToTenant(AppUserObject appUser, TenantObject tenant);

    /**
     * Connect the business context to the tenant.
     *
     * @param businessContext the business context
     * @param tenant          the tenant
     */
    void addBusinessContextToTenant(BusinessContextObject businessContext, TenantObject tenant);

    /**
     * Delete the tenant together with its users' and business contexts' connections to it.
     * The users and the business contexts themselves are kept.
     *
     * @param tenant the tenant
     */
    void deleteTenant(TenantObject tenant);

    /**
     * Delete the tenant of the given name, as {@link #deleteTenant(TenantObject)} does. It does nothing if there is none.
     *
     * @param name the name of the tenant
     */
    void deleteTenantByName(String name);

}
