package org.oagi.score.e2e.TS_3_EndUserAuthorizedFunctions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.HomePage;

import static org.junit.jupiter.api.Assertions.assertFalse;

@Execution(ExecutionMode.SAME_THREAD)
public class TC_3_3_EndUsersAuthorizedFunctionalitiesInTenantMode extends BaseTest {

    private AppUserObject appUser;
    private boolean previousTenantEnabled;

    @BeforeEach
    public void init() {
        super.init();

        previousTenantEnabled = getAPIFactory().getApplicationSettingsAPI().isTenantEnabled();
        getAPIFactory().getApplicationSettingsAPI().setTenantEnable(true);

        appUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
    }

    @Test
    @DisplayName("TC_3_3_TA_1 (BIE - Create BIE and Code List Menus Disabled)")
    public void test_bie_disabled_menus_in_tenant_mode_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();

        assertFalse(bieMenu.getCreateBIESubMenu(false).isEnabled());
        assertFalse(bieMenu.getViewEditCodeListSubMenu(false).isEnabled());
        assertFalse(bieMenu.getUpliftCodeListSubMenu(false).isEnabled());
    }

    @Test
    @DisplayName("TC_3_3_TA_2 (Context, Module, and Library Menus Hidden)")
    public void test_hidden_menus_in_tenant_mode_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());

        assertFalse(homePage.hasContextMenu());
        assertFalse(homePage.hasModuleMenu());
        assertFalse(homePage.hasLibraryMenu());
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        if (appUser != null) {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(appUser.getLoginId());
        }

        getAPIFactory().getApplicationSettingsAPI().setTenantEnable(previousTenantEnabled);
    }

}
