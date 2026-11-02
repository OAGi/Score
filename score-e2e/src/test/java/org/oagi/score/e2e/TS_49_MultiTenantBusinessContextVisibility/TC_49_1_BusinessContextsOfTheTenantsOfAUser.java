package org.oagi.score.e2e.TS_49_MultiTenantBusinessContextVisibility;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.TenantObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.CreateBIEForSelectBusinessContextsPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * A multi-tenant instance with seven users, four tenants and four business contexts: what the Create BIE page offers a
 * user is the business contexts of the user's tenants. It changes the global multi-tenant setting, so it runs alone.
 */
@Isolated("Multi-tenant mode is a global application setting used by other E2E tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
public class TC_49_1_BusinessContextsOfTheTenantsOfAUser extends BaseTest {

    private boolean previousTenantEnabled;

    /** Every business context of the test has this in its name, so the pages can be searched for just them. */
    private String businessContextPrefix;

    private AppUserObject developerWithoutTenant;
    private AppUserObject adminDeveloperWithoutTenant;
    private AppUserObject endUserWithoutTenant;
    private AppUserObject developerOfTenantA;
    private AppUserObject endUserOfTenantB;
    private AppUserObject endUserOfTenantC;
    private AppUserObject endUserOfTenantsBAndC;

    private TenantObject tenantA;
    private TenantObject tenantB;
    private TenantObject tenantC;

    private BusinessContextObject contextOfTenantA;
    private BusinessContextObject contextOfTenantB;
    private BusinessContextObject contextOfTenantC;
    private BusinessContextObject contextOfNoTenant;

    private final List<AppUserObject> users = new ArrayList<>();
    private final List<TenantObject> tenants = new ArrayList<>();
    private final List<BusinessContextObject> contexts = new ArrayList<>();

    @BeforeAll
    void createTheTenants() {
        previousTenantEnabled = getAPIFactory().getApplicationSettingsAPI().isTenantEnabled();
        getAPIFactory().getApplicationSettingsAPI().setTenantEnable(true);

        developerWithoutTenant = user(getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false));
        adminDeveloperWithoutTenant = user(getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true));
        endUserWithoutTenant = user(getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false));
        developerOfTenantA = user(getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false));
        endUserOfTenantB = user(getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false));
        endUserOfTenantC = user(getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false));
        endUserOfTenantsBAndC = user(getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false));

        var tenantAPI = getAPIFactory().getTenantAPI();
        tenantA = tenant(tenantAPI.createRandomTenant());
        tenantB = tenant(tenantAPI.createRandomTenant());
        tenantC = tenant(tenantAPI.createRandomTenant());

        tenantAPI.addUserToTenant(developerOfTenantA, tenantA);
        tenantAPI.addUserToTenant(endUserOfTenantB, tenantB);
        tenantAPI.addUserToTenant(endUserOfTenantC, tenantC);
        tenantAPI.addUserToTenant(endUserOfTenantsBAndC, tenantB);
        tenantAPI.addUserToTenant(endUserOfTenantsBAndC, tenantC);

        businessContextPrefix = "mt" + randomAlphanumeric(6);
        var businessContextAPI = getAPIFactory().getBusinessContextAPI();
        contextOfTenantA = businessContext(businessContextAPI.createRandomBusinessContext(developerWithoutTenant, businessContextPrefix));
        contextOfTenantB = businessContext(businessContextAPI.createRandomBusinessContext(developerWithoutTenant, businessContextPrefix));
        contextOfTenantC = businessContext(businessContextAPI.createRandomBusinessContext(developerWithoutTenant, businessContextPrefix));
        contextOfNoTenant = businessContext(businessContextAPI.createRandomBusinessContext(developerWithoutTenant, businessContextPrefix));

        tenantAPI.addBusinessContextToTenant(contextOfTenantA, tenantA);
        tenantAPI.addBusinessContextToTenant(contextOfTenantB, tenantB);
        tenantAPI.addBusinessContextToTenant(contextOfTenantC, tenantC);
    }

    private AppUserObject user(AppUserObject user) {
        users.add(user);
        return user;
    }

    private BusinessContextObject businessContext(BusinessContextObject context) {
        contexts.add(context);
        return context;
    }

    private TenantObject tenant(TenantObject tenant) {
        tenants.add(tenant);
        return tenant;
    }

    @BeforeEach
    @Override
    public void init() {
        super.init();
    }

    @AfterAll
    void deleteTheTenants() {
        for (TenantObject tenant : tenants) {
            getAPIFactory().getTenantAPI().deleteTenant(tenant);
        }
        for (BusinessContextObject context : contexts) {
            getAPIFactory().getBusinessContextAPI().deleteBusinessContextById(context.getBusinessContextId());
        }
        for (AppUserObject user : users) {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(user.getLoginId());
        }
        getAPIFactory().getApplicationSettingsAPI().setTenantEnable(previousTenantEnabled);
    }

    /** The names of the business contexts of this test that the Create BIE page lists for the user. */
    private Set<String> offeredTo(AppUserObject user) {
        HomePage homePage = loginPage().signIn(user.getLoginId(), user.getPassword());
        CreateBIEForSelectBusinessContextsPage page = homePage.getBIEMenu().openCreateBIESubMenu();
        page.setName(businessContextPrefix);
        page.hitSearchButton();
        List<String> listed = page.getListedBusinessContextNames();
        assertEquals(listed.size(), Set.copyOf(listed).size(), "A business context is listed once: " + listed);
        return Set.copyOf(listed);
    }

    private static Set<String> names(BusinessContextObject... contexts) {
        return List.of(contexts).stream().map(BusinessContextObject::getName).collect(Collectors.toSet());
    }

    @Test
    @DisplayName("TC_49_1_TA_1: a user with one tenant is offered the business contexts of that tenant only")
    public void user_of_one_tenant_is_offered_the_business_contexts_of_the_tenant() {
        assertEquals(names(contextOfTenantA), offeredTo(developerOfTenantA));
    }

    @Test
    @DisplayName("TC_49_1_TA_1: an end user with one tenant is offered the business contexts of that tenant only")
    public void end_user_of_one_tenant_is_offered_the_business_contexts_of_the_tenant() {
        assertEquals(names(contextOfTenantB), offeredTo(endUserOfTenantB));
    }

    @Test
    @DisplayName("TC_49_1_TA_2: a user with several tenants is offered the business contexts of all of them, not those of the others")
    public void user_of_two_tenants_is_offered_the_business_contexts_of_both() {
        assertEquals(names(contextOfTenantB, contextOfTenantC), offeredTo(endUserOfTenantsBAndC));
    }

    @Test
    @DisplayName("TC_49_1_TA_2: the business contexts of a tenant that the user is not in are not offered")
    public void user_is_not_offered_the_business_contexts_of_the_other_tenants() {
        assertEquals(names(contextOfTenantC), offeredTo(endUserOfTenantC));
    }

    @Test
    @DisplayName("TC_49_1_TA_3: a user without a tenant cannot start creating a BIE, whatever the roles are")
    public void user_without_tenant_cannot_create_bie() {
        for (AppUserObject user : List.of(developerWithoutTenant, adminDeveloperWithoutTenant, endUserWithoutTenant)) {
            HomePage homePage = loginPage().signIn(user.getLoginId(), user.getPassword());
            assertFalse(homePage.getBIEMenu().getCreateBIESubMenu(false).isEnabled(), user.getLoginId());
            homePage.logout();
        }
    }

    @Test
    @DisplayName("TC_49_1_TA_4: a business context connected to several tenants is offered once")
    public void business_context_of_several_tenants_is_offered_once() {
        var tenantAPI = getAPIFactory().getTenantAPI();
        AppUserObject user = user(getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false));
        TenantObject tenantOfUser = tenant(tenantAPI.createRandomTenant());
        TenantObject otherTenant = tenant(tenantAPI.createRandomTenant());
        TenantObject anotherTenant = tenant(tenantAPI.createRandomTenant());
        BusinessContextObject sharedContext = businessContext(getAPIFactory().getBusinessContextAPI()
                .createRandomBusinessContext(developerWithoutTenant, businessContextPrefix));
        tenantAPI.addUserToTenant(user, tenantOfUser);
        tenantAPI.addBusinessContextToTenant(sharedContext, tenantOfUser);
        tenantAPI.addBusinessContextToTenant(sharedContext, otherTenant);
        tenantAPI.addBusinessContextToTenant(sharedContext, anotherTenant);

        assertEquals(names(sharedContext), offeredTo(user));
    }

}
