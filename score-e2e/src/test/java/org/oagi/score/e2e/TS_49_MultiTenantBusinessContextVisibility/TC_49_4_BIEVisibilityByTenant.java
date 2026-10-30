package org.oagi.score.e2e.TS_49_MultiTenantBusinessContextVisibility;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.TenantObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The BIE list in the multi-tenant mode: a user sees a BIE when one of the business contexts that the BIE is assigned to is
 * connected to one of the user's tenants. An Admin sees the BIEs, and is offered the business contexts, of every tenant.
 */
public class TC_49_4_BIEVisibilityByTenant extends AbstractTenantTest {

    private AppUserObject userOfTenantA;
    private AppUserObject userOfTenantB;
    private AppUserObject developerOfBothTenants;
    private AppUserObject userWithoutTenant;
    private AppUserObject admin;
    private AppUserObject adminOfTenantA;
    private BusinessContextObject contextOfTenantA;
    private BusinessContextObject contextOfTenantB;
    private BusinessContextObject contextOfNoTenant;

    private TopLevelASBIEPObject bieOfTenantA;
    private TopLevelASBIEPObject bieOfTenantB;
    private TopLevelASBIEPObject bieOfBothTenants;
    private TopLevelASBIEPObject bieOfNoTenant;
    private List<TopLevelASBIEPObject> bies;

    @BeforeAll
    void createTheTenantsAndTheBies() {
        AppUserObject owner = developer();
        userOfTenantA = endUser();
        userOfTenantB = endUser();
        developerOfBothTenants = developer();
        userWithoutTenant = endUser();
        admin = admin();
        adminOfTenantA = admin();

        TenantObject tenantA = tenant();
        TenantObject tenantB = tenant();
        var tenantAPI = getAPIFactory().getTenantAPI();
        tenantAPI.addUserToTenant(userOfTenantA, tenantA);
        tenantAPI.addUserToTenant(adminOfTenantA, tenantA);
        tenantAPI.addUserToTenant(userOfTenantB, tenantB);
        tenantAPI.addUserToTenant(developerOfBothTenants, tenantA);
        tenantAPI.addUserToTenant(developerOfBothTenants, tenantB);

        contextOfTenantA = businessContext(owner);
        contextOfTenantB = businessContext(owner);
        contextOfNoTenant = businessContext(owner);
        tenantAPI.addBusinessContextToTenant(contextOfTenantA, tenantA);
        tenantAPI.addBusinessContextToTenant(contextOfTenantB, tenantB);

        bieOfTenantA = bie(owner, "Production", contextOfTenantA);
        bieOfTenantB = bie(owner, "Production", contextOfTenantB);
        bieOfBothTenants = bie(owner, "Production", contextOfTenantA, contextOfTenantB);
        bieOfNoTenant = bie(owner, "Production", contextOfNoTenant);
        bies = List.of(bieOfTenantA, bieOfTenantB, bieOfBothTenants, bieOfNoTenant);
    }

    @Test
    @DisplayName("TC_49_4_TA_1: a user of one tenant sees the BIEs that are assigned a business context of that tenant")
    public void user_of_one_tenant_sees_the_bies_of_the_tenant() {
        assertEquals(terms(bieOfTenantA, bieOfBothTenants), biesListedFor(userOfTenantA, bies));
    }

    @Test
    @DisplayName("TC_49_4_TA_1: a user of another tenant sees the BIEs of that tenant, not those of the first")
    public void user_of_the_other_tenant_sees_the_bies_of_the_other_tenant() {
        assertEquals(terms(bieOfTenantB, bieOfBothTenants), biesListedFor(userOfTenantB, bies));
    }

    @Test
    @DisplayName("TC_49_4_TA_2: a user of several tenants sees the BIEs of all of them, each once")
    public void user_of_two_tenants_sees_the_bies_of_both_tenants_once() {
        assertEquals(terms(bieOfTenantA, bieOfTenantB, bieOfBothTenants), biesListedFor(developerOfBothTenants, bies));
    }

    @Test
    @DisplayName("TC_49_4_TA_3: a BIE whose business contexts are of no tenant is seen by nobody, and a user of no tenant sees none")
    public void user_without_tenant_sees_no_bie() {
        assertEquals(Set.of(), biesListedFor(userWithoutTenant, bies));
    }

    @Test
    @DisplayName("TC_49_4_TA_4: an Admin sees the BIEs of every tenant, even when the Admin belongs to no tenant")
    public void admin_sees_the_bies_of_every_tenant() {
        assertEquals(terms(bieOfTenantA, bieOfTenantB, bieOfBothTenants, bieOfNoTenant), biesListedFor(admin, bies));
    }

    @Test
    @DisplayName("TC_49_4_TA_5: an Admin who belongs to a tenant sees the BIEs of every tenant, and is offered every business context")
    public void admin_of_a_tenant_sees_every_bie_and_is_offered_every_business_context() {
        assertEquals(terms(bieOfTenantA, bieOfTenantB, bieOfBothTenants, bieOfNoTenant), biesListedFor(adminOfTenantA, bies));
        // Every business context of the class is offered, also those that other tests of the class made.
        assertTrue(offeredTo(adminOfTenantA).containsAll(
                Set.of(contextOfTenantA.getName(), contextOfTenantB.getName(), contextOfNoTenant.getName())));
    }

    @Test
    @DisplayName("TC_49_4_TA_6: the BIEs that a user sees follow what an Admin adds to and removes from a tenant")
    public void bies_follow_the_business_contexts_that_an_admin_associates_with_the_tenant() {
        AppUserObject owner = developer();
        AppUserObject user = endUser();
        TenantObject tenant = tenant();
        getAPIFactory().getTenantAPI().addUserToTenant(user, tenant);
        BusinessContextObject context = businessContext(owner);
        TopLevelASBIEPObject bie = bie(owner, "Production", context);

        assertEquals(Set.of(), biesListedFor(user, List.of(bie)));

        var session = adminOpensTheTenantPage(admin);
        session.tenantPage().search(tenant.getName());
        var contexts = session.tenantPage().openManageBusinessContexts(tenant.getName());
        contexts.switchToAddView();
        contexts.search(context.getName());
        contexts.add(context.getName());
        session.homePage().logout();

        assertEquals(terms(bie), biesListedFor(user, List.of(bie)), "The BIE is seen once its business context is added");

        session = adminOpensTheTenantPage(admin);
        session.tenantPage().search(tenant.getName());
        contexts = session.tenantPage().openManageBusinessContexts(tenant.getName());
        contexts.search(context.getName());
        contexts.remove(context.getName());
        session.homePage().logout();

        assertEquals(Set.of(), biesListedFor(user, List.of(bie)), "The BIE is not seen once its business context is removed");
    }

}
