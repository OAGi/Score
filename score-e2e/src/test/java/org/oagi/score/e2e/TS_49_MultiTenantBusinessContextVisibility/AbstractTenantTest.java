package org.oagi.score.e2e.TS_49_MultiTenantBusinessContextVisibility;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.ASCCPObject;
import org.oagi.score.e2e.obj.ACCObject;
import org.oagi.score.e2e.obj.LibraryObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.obj.TenantObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.admin.TenantPage;
import org.oagi.score.e2e.page.bie.CreateBIEForSelectBusinessContextsPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

/**
 * What the tests of the multi-tenant mode share: the mode is turned on for the run of the class and restored afterwards,
 * and what a test creates is deleted when the class is done. The mode is a global application setting, so these run alone.
 */
@Isolated("Multi-tenant mode is a global application setting used by other E2E tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
abstract class AbstractTenantTest extends BaseTest {

    private boolean previousTenantEnabled;

    /** Every business context of a test has this in its name, so the pages can be searched for just them. */
    protected final String businessContextPrefix = "mt" + randomAlphanumeric(6);

    private final List<AppUserObject> users = new ArrayList<>();
    private final List<TenantObject> tenants = new ArrayList<>();
    private final List<BusinessContextObject> contexts = new ArrayList<>();

    @BeforeAll
    void turnMultiTenantModeOn() {
        previousTenantEnabled = getAPIFactory().getApplicationSettingsAPI().isTenantEnabled();
        getAPIFactory().getApplicationSettingsAPI().setTenantEnable(true);
    }

    @BeforeEach
    @Override
    public void init() {
        super.init();
    }

    @AfterAll
    void deleteWhatTheTestsCreated() {
        for (TenantObject tenant : tenants) {
            getAPIFactory().getTenantAPI().deleteTenant(tenant);
        }
        // A user's BIEs go with the user, and a business context cannot go while a BIE is assigned to it.
        for (AppUserObject user : users) {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(user.getLoginId());
        }
        for (BusinessContextObject context : contexts) {
            getAPIFactory().getBusinessContextAPI().deleteBusinessContextById(context.getBusinessContextId());
        }
        getAPIFactory().getApplicationSettingsAPI().setTenantEnable(previousTenantEnabled);
    }

    protected AppUserObject endUser() {
        return track(getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false));
    }

    protected AppUserObject developer() {
        return track(getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false));
    }

    protected AppUserObject admin() {
        return track(getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true));
    }

    protected AppUserObject track(AppUserObject user) {
        users.add(user);
        return user;
    }

    protected TenantObject tenant() {
        TenantObject tenant = getAPIFactory().getTenantAPI().createRandomTenant();
        tenants.add(tenant);
        return tenant;
    }

    protected BusinessContextObject businessContext(AppUserObject creator) {
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI()
                .createRandomBusinessContext(creator, businessContextPrefix);
        contexts.add(context);
        return context;
    }

    /** The branch that the BIEs of the tests are made in. */
    protected static final String RELEASE_NUMBER = "10.7.1";

    /** A BIE of a random top-level concept, owned by the owner and assigned the business contexts. */
    protected TopLevelASBIEPObject bie(AppUserObject owner, String state, BusinessContextObject... businessContexts) {
        AppUserObject conceptOwner = developer();
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI()
                .getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, RELEASE_NUMBER);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(conceptOwner, release, namespace, "Published");
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc, conceptOwner, namespace, "Published");
        return getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(List.of(businessContexts), asccp, owner, state);
    }

    /** Open View/Edit BIE for the branch of the tests, listing the BIEs of the business contexts of the tests. */
    protected ViewEditBIEPage openBieListOfTheTests(HomePage homePage) {
        ViewEditBIEPage page = homePage.getBIEMenu().openViewEditBIESubMenu();
        page.showAdvancedSearchPanel();
        page.setBranch(RELEASE_NUMBER);
        page.setBusinessContext(businessContextPrefix);
        page.hitSearchButton();
        return page;
    }

    /** Which of the BIEs the View/Edit BIE page lists for the user, by the property terms. Signs out. */
    protected Set<String> biesListedFor(AppUserObject user, List<TopLevelASBIEPObject> bies) {
        HomePage homePage = loginPage().signIn(user.getLoginId(), user.getPassword());
        List<String> listed = openBieListOfTheTests(homePage).getListedDENs();
        homePage.logout();
        Set<String> result = new java.util.HashSet<>();
        for (TopLevelASBIEPObject bie : bies) {
            long times = listed.stream().filter(den -> den.contains(bie.getPropertyTerm())).count();
            if (times > 1) {
                throw new AssertionError("A BIE is listed " + times + " times: " + bie.getPropertyTerm() + " in " + listed);
            }
            if (times == 1) {
                result.add(bie.getPropertyTerm());
            }
        }
        return result;
    }

    protected static Set<String> terms(TopLevelASBIEPObject... bies) {
        return java.util.Arrays.stream(bies).map(TopLevelASBIEPObject::getPropertyTerm)
                .collect(java.util.stream.Collectors.toSet());
    }

    /** Sign in as the Admin and open Admin &gt; Tenant. The home page is what to sign out from. */
    protected record AdminSession(HomePage homePage, TenantPage tenantPage) {
    }

    protected AdminSession adminOpensTheTenantPage(AppUserObject admin) {
        HomePage homePage = loginPage().signIn(admin.getLoginId(), admin.getPassword());
        return new AdminSession(homePage, homePage.getAdminMenu().openTenantSubMenu());
    }

    /** Whether the user can start creating a BIE: signed in, and the 'Create BIE' menu item is enabled. Signs out. */
    protected boolean canCreateBie(AppUserObject user) {
        HomePage homePage = loginPage().signIn(user.getLoginId(), user.getPassword());
        boolean enabled = homePage.getBIEMenu().getCreateBIESubMenu(false).isEnabled();
        homePage.logout();
        return enabled;
    }

    /** The names of the business contexts of the tests that the Create BIE page lists for the user. Signs out. */
    protected Set<String> offeredTo(AppUserObject user) {
        HomePage homePage = loginPage().signIn(user.getLoginId(), user.getPassword());
        CreateBIEForSelectBusinessContextsPage page = homePage.getBIEMenu().openCreateBIESubMenu();
        page.setName(businessContextPrefix);
        page.hitSearchButton();
        List<String> listed = page.getListedBusinessContextNames();
        homePage.logout();
        return Set.copyOf(listed);
    }

}
