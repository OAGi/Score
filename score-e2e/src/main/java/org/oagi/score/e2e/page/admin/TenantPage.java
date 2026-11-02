package org.oagi.score.e2e.page.admin;

import org.oagi.score.e2e.page.Page;

import java.util.List;

/**
 * An interface of the 'Tenant' page of the Admin menu.
 */
public interface TenantPage extends Page {

    /**
     * Click 'New Tenant', enter the name and click 'Create'. Return to the list.
     *
     * @param name the name of the tenant
     */
    void createTenant(String name);

    /**
     * Click 'New Tenant', enter the name, click 'Create' and return the message of the snackbar that answers it,
     * which says whether the tenant was created. The page does not wait for the list.
     *
     * @param name the name of the tenant
     * @return the message of the snackbar
     */
    String tryCreateTenant(String name);

    /**
     * Open the page that edits the tenant, by its name in the list.
     *
     * @param name the name of the tenant
     * @return the page object that edits the tenant
     */
    TenantEditPage openTenant(String name);

    /**
     * Search the tenants by name and wait for the result.
     *
     * @param name the name of the tenant
     */
    void search(String name);

    /**
     * Return the names of the listed tenants, once the table has loaded.
     *
     * @return the names of the listed tenants
     */
    List<String> getListedTenantNames();

    /**
     * Click 'Manage Users' of the tenant.
     *
     * @param name the name of the tenant
     * @return the 'Manage Users' page object
     */
    TenantUserPage openManageUsers(String name);

    /**
     * Click 'Manage Business Context' of the tenant.
     *
     * @param name the name of the tenant
     * @return the 'Manage Business Context' page object
     */
    TenantBusinessContextPage openManageBusinessContexts(String name);

}
