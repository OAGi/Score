package org.oagi.score.e2e.TS_3_EndUserAuthorizedFunctions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.menu.*;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.LoginPage;
import org.oagi.score.e2e.page.SettingsPasswordPage;
import org.oagi.score.e2e.page.agency_id_list.ViewEditAgencyIDListPage;
import org.oagi.score.e2e.page.bie.*;
import org.oagi.score.e2e.page.code_list.UpliftCodeListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.oagi.score.e2e.page.context.ViewEditBusinessContextPage;
import org.oagi.score.e2e.page.context.ViewEditContextCategoryPage;
import org.oagi.score.e2e.page.context.ViewEditContextSchemePage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.oagi.score.e2e.page.help.AboutPage;
import org.oagi.score.e2e.page.help.UserGuidePage;
import org.oagi.score.e2e.page.module.ViewEditModuleSetPage;
import org.oagi.score.e2e.page.module.ViewEditModuleSetReleasePage;
import org.oagi.score.e2e.page.namespace.ViewEditNamespacePage;
import org.oagi.score.e2e.page.release.ViewEditReleasePage;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_3_1_EndUsersAuthorizedFunctionalities extends BaseTest {

    private AppUserObject appUser;

    @BeforeEach
    public void init() {
        super.init();

        // Create random end-user
        appUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (BIE - View/Edit BIE Menu)")
    public void test_view_edit_bie_menu_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        assertTrue(bieMenu.getViewEditBIESubMenu().isEnabled());

        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        WebElement title = viewEditBIEPage.getTitle();
        assertTrue(title.isDisplayed());
        String titleText = title.getText();

        homePage.openPage();
        viewEditBIEPage.openPage();
        assertEquals(viewEditBIEPage.getTitle().getText(), titleText);
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (BIE - Create BIE Menu)")
    public void test_create_bie_menu_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        assertTrue(bieMenu.getCreateBIESubMenu().isEnabled());

        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage = bieMenu.openCreateBIESubMenu();
        WebElement title = createBIEForSelectBusinessContextsPage.getTitle();
        assertTrue(title.isDisplayed());
        String titleText = title.getText();

        homePage.openPage();
        createBIEForSelectBusinessContextsPage.openPage();
        assertEquals(createBIEForSelectBusinessContextsPage.getTitle().getText(), titleText);
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (BIE - Copy BIE Menu)")
    public void test_copy_bie_menu_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        assertTrue(bieMenu.getCopyBIESubMenu().isEnabled());

        CopyBIEForSelectBusinessContextsPage copyBIEForSelectBusinessContextsPage = bieMenu.openCopyBIESubMenu();
        WebElement title = copyBIEForSelectBusinessContextsPage.getTitle();
        assertTrue(title.isDisplayed());
        String titleText = title.getText();

        homePage.openPage();
        copyBIEForSelectBusinessContextsPage.openPage();
        assertEquals(copyBIEForSelectBusinessContextsPage.getTitle().getText(), titleText);
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (BIE - Uplift BIE Menu)")
    public void test_uplift_bie_menu_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        assertTrue(bieMenu.getUpliftBIESubMenu().isEnabled());

        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        WebElement title = upliftBIEPage.getTitle();
        assertTrue(title.isDisplayed());
        String titleText = title.getText();

        homePage.openPage();
        upliftBIEPage.openPage();
        assertEquals(upliftBIEPage.getTitle().getText(), titleText);
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (BIE - Express BIE Menu)")
    public void test_express_bie_menu_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        assertTrue(bieMenu.getExpressBIESubMenu().isEnabled());

        ExpressBIEPage expressBIEPage = bieMenu.openExpressBIESubMenu();
        WebElement title = expressBIEPage.getTitle();
        assertTrue(title.isDisplayed());
        String titleText = title.getText();

        homePage.openPage();
        expressBIEPage.openPage();
        assertEquals(expressBIEPage.getTitle().getText(), titleText);
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (BIE - Reuse Report Menu)")
    public void test_reuse_report_menu_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        assertTrue(bieMenu.getReuseReportSubMenu().isEnabled());

        ReuseReportPage reuseReportPage = bieMenu.openReuseReportSubMenu();
        WebElement title = reuseReportPage.getTitle();
        assertTrue(title.isDisplayed());
        String titleText = title.getText();

        homePage.openPage();
        reuseReportPage.openPage();
        assertEquals(reuseReportPage.getTitle().getText(), titleText);
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (BIE - View/Edit Code List Menu)")
    public void test_bie_view_edit_code_list_menu_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        assertTrue(bieMenu.getViewEditCodeListSubMenu().isEnabled());

        ViewEditCodeListPage viewEditCodeListPage = bieMenu.openViewEditCodeListSubMenu();
        WebElement title = viewEditCodeListPage.getTitle();
        assertTrue(title.isDisplayed());
        String titleText = title.getText();

        homePage.openPage();
        viewEditCodeListPage.openPage();
        assertEquals(viewEditCodeListPage.getTitle().getText(), titleText);
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (BIE - Uplift Code List Menu)")
    public void test_bie_uplift_code_list_menu_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        assertTrue(bieMenu.getUpliftCodeListSubMenu(true).isEnabled());

        UpliftCodeListPage upliftCodeListPage = bieMenu.openUpliftCodeListSubMenu();
        WebElement title = upliftCodeListPage.getTitle();
        assertTrue(title.isDisplayed());
        String titleText = title.getText();

        homePage.openPage();
        upliftCodeListPage.openPage();
        assertEquals(upliftCodeListPage.getTitle().getText(), titleText);
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (Context - View/Edit Context Category Menu)")
    public void test_view_edit_context_category_menu_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        assertTrue(contextMenu.getViewEditContextCategorySubMenu().isEnabled());

        ViewEditContextCategoryPage viewEditContextCategoryPage = contextMenu.openViewEditContextCategorySubMenu();
        WebElement title = viewEditContextCategoryPage.getTitle();
        assertTrue(title.isDisplayed());
        String titleText = title.getText();

        homePage.openPage();
        viewEditContextCategoryPage.openPage();
        assertEquals(viewEditContextCategoryPage.getTitle().getText(), titleText);
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (Context - View/Edit Context Scheme Menu)")
    public void test_view_edit_context_scheme_menu_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        assertTrue(contextMenu.getViewEditContextSchemeSubMenu().isEnabled());

        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        WebElement title = viewEditContextSchemePage.getTitle();
        assertTrue(title.isDisplayed());
        String titleText = title.getText();

        homePage.openPage();
        viewEditContextSchemePage.openPage();
        assertEquals(viewEditContextSchemePage.getTitle().getText(), titleText);
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (Context - View/Edit Business Context Menu)")
    public void test_view_edit_business_context_menu_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        assertTrue(contextMenu.getViewEditBusinessContextSubMenu().isEnabled());

        ViewEditBusinessContextPage viewEditBusinessContextPage = contextMenu.openViewEditBusinessContextSubMenu();
        WebElement title = viewEditBusinessContextPage.getTitle();
        assertTrue(title.isDisplayed());
        String titleText = title.getText();

        homePage.openPage();
        viewEditBusinessContextPage.openPage();
        assertEquals(viewEditBusinessContextPage.getTitle().getText(), titleText);
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (Core Component - View/Edit Core Component Menu)")
    public void test_view_edit_core_component_menu_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        assertTrue(coreComponentMenu.getViewEditCoreComponentSubMenu().isEnabled());

        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        WebElement title = viewEditCoreComponentPage.getTitle();
        assertTrue(title.isDisplayed());
        String titleText = title.getText();

        homePage.openPage();
        viewEditCoreComponentPage.openPage();
        assertEquals(viewEditCoreComponentPage.getTitle().getText(), titleText);
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (Core Component - View/Edit Code List Menu)")
    public void test_core_component_view_edit_code_list_menu_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        assertTrue(coreComponentMenu.getViewEditCodeListSubMenu().isEnabled());

        ViewEditCodeListPage viewEditCodeListPage = coreComponentMenu.openViewEditCodeListSubMenu();
        WebElement title = viewEditCodeListPage.getTitle();
        assertTrue(title.isDisplayed());
        String titleText = title.getText();

        homePage.openPage();
        viewEditCodeListPage.openPage();
        assertEquals(viewEditCodeListPage.getTitle().getText(), titleText);
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (Core Component - View/Edit Agency ID List Menu)")
    public void test_core_component_view_edit_agency_id_list_menu_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        assertTrue(coreComponentMenu.getViewEditAgencyIDListSubMenu().isEnabled());

        ViewEditAgencyIDListPage viewEditAgencyIDListPage = coreComponentMenu.openViewEditAgencyIDListSubMenu();
        WebElement title = viewEditAgencyIDListPage.getTitle();
        assertTrue(title.isDisplayed());
        String titleText = title.getText();

        homePage.openPage();
        viewEditAgencyIDListPage.openPage();
        assertEquals(viewEditAgencyIDListPage.getTitle().getText(), titleText);
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (Core Component - View/Edit Release Menu)")
    public void test_core_component_view_edit_release_menu_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        assertTrue(coreComponentMenu.getViewEditReleaseSubMenu().isEnabled());

        ViewEditReleasePage viewEditReleasePage = coreComponentMenu.openViewEditReleaseSubMenu();
        WebElement title = viewEditReleasePage.getTitle();
        assertTrue(title.isDisplayed());
        String titleText = title.getText();

        homePage.openPage();
        viewEditReleasePage.openPage();
        assertEquals(viewEditReleasePage.getTitle().getText(), titleText);
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (Core Component - View/Edit Namespace Menu)")
    public void test_core_component_view_edit_namespace_menu_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        assertTrue(coreComponentMenu.getViewEditNamespaceSubMenu().isEnabled());

        ViewEditNamespacePage viewEditNamespacePage = coreComponentMenu.openViewEditNamespaceSubMenu();
        WebElement title = viewEditNamespacePage.getTitle();
        assertTrue(title.isDisplayed());
        String titleText = title.getText();

        homePage.openPage();
        viewEditNamespacePage.openPage();
        assertEquals(viewEditNamespacePage.getTitle().getText(), titleText);
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (Module - View Module Set Menu)")
    public void test_module_view_module_set_menu_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ModuleMenu moduleMenu = homePage.getModuleMenu();
        assertTrue(moduleMenu.getViewEditModuleSetSubMenu().isEnabled());

        ViewEditModuleSetPage viewEditModuleSetPage = moduleMenu.openViewEditModuleSetSubMenu();
        WebElement title = viewEditModuleSetPage.getTitle();
        assertTrue(title.isDisplayed());
        String titleText = title.getText();

        homePage.openPage();
        viewEditModuleSetPage.openPage();
        assertEquals(viewEditModuleSetPage.getTitle().getText(), titleText);
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (Module - View Module Set Release Menu)")
    public void test_module_view_module_set_release_menu_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ModuleMenu moduleMenu = homePage.getModuleMenu();
        assertTrue(moduleMenu.getViewEditModuleSetReleaseSubMenu().isEnabled());

        ViewEditModuleSetReleasePage viewEditModuleSetReleasePage = moduleMenu.openViewEditModuleSetReleaseSubMenu();
        WebElement title = viewEditModuleSetReleasePage.getTitle();
        assertTrue(title.isDisplayed());
        String titleText = title.getText();

        homePage.openPage();
        viewEditModuleSetReleasePage.openPage();
        assertEquals(viewEditModuleSetReleasePage.getTitle().getText(), titleText);
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (Admin Menu - Hidden)")
    public void test_admin_menu_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        AdminMenu adminMenu = homePage.getAdminMenu();

        assertThrows(TimeoutException.class, () -> adminMenu.getAdminMenu());
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (Help - About Menu)")
    public void test_help_about_menu_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        HelpMenu helpMenu = homePage.getHelpMenu();
        assertTrue(helpMenu.getAboutSubMenu().isEnabled());

        AboutPage aboutPage = helpMenu.openAboutSubMenu();
        WebElement title = aboutPage.getTitle();
        assertTrue(title.isDisplayed());
        String titleText = title.getText();

        homePage.openPage();
        aboutPage.openPage();
        assertEquals(aboutPage.getTitle().getText(), titleText);
    }

    @Test
    @DisabledIf("isLocalhost")
    @DisplayName("TC_3_1_TA_1_1 (Help - User Guide Menu)")
    public void test_help_user_guide_menu_end_user() {
        // TODO:
        // Conditional test execution mechanism in JUnit 5 using pre-defined annotations such as
        // @Enabled or @Disabled is not working properly at this moment. If @DisabledIf("isLocalhost")
        // annotation is working, the following condition doesn't need to be existed.
        if (isLocalhost()) {
            return;
        }

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        HelpMenu helpMenu = homePage.getHelpMenu();
        assertTrue(helpMenu.getUserGuideSubMenu().isEnabled());

        UserGuidePage userGuidePage = helpMenu.openUserGuideSubMenu();
        WebElement title = userGuidePage.getTitle();
        assertTrue(title.isDisplayed());
        String titleText = title.getText();

        homePage.openPage();
        userGuidePage.openPage();
        assertEquals(userGuidePage.getTitle().getText(), titleText);
    }

    /**
     * Return {@code true} if the base URL for the testing host is 'localhost', otherwise {@code false}.
     * Used for conditional text executions such as {@link org.junit.jupiter.api.condition.EnabledIf} or
     * {@link org.junit.jupiter.api.condition.DisabledIf}.
     *
     * @return {@code true} if the base URL for the testing host is 'localhost', otherwise {@code false}
     */
    public boolean isLocalhost() {
        return "localhost".equals(getConfig().getBaseUrl().getHost());
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (Login ID - Signed in Label)")
    public void test_login_id_sign_in_label_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        LoginIDMenu loginIDMenu = homePage.getLoginIDMenu();
        assertTrue(loginIDMenu.getSignInLabel().isDisplayed());

        assertEquals("Signed in as " + appUser.getLoginId(), loginIDMenu.getSignInLabelText());
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (Login ID - OAGIS Terminology Button)")
    public void test_login_id_oagis_terminology_button_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        LoginIDMenu loginIDMenu = homePage.getLoginIDMenu();
        assertTrue(loginIDMenu.getOAGISTerminologyButton().isEnabled());

        loginIDMenu.checkOAGISTerminology();
        assertTrue(loginIDMenu.isOAGISTerminologyChecked());

        // Check if it's exclusively working with 'CCTS Terminology' button.
        loginIDMenu.checkCCTSTerminology();
        assertFalse(loginIDMenu.isOAGISTerminologyChecked());
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (Login ID - CCTS Terminology Button)")
    public void test_login_id_ccts_terminology_button_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        LoginIDMenu loginIDMenu = homePage.getLoginIDMenu();
        assertTrue(loginIDMenu.getCCTSTerminologyButton().isEnabled());

        loginIDMenu.checkCCTSTerminology();
        assertTrue(loginIDMenu.isCCTSTerminologyChecked());

        // Check if it's exclusively working with 'OAGIS Terminology' button.
        loginIDMenu.checkOAGISTerminology();
        assertFalse(loginIDMenu.isCCTSTerminologyChecked());
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (Login ID - Settings Menu)")
    public void test_login_id_settings_menu_end_user() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        LoginIDMenu loginIDMenu = homePage.getLoginIDMenu();
        assertTrue(loginIDMenu.getSettingsSubMenu().isEnabled());

        SettingsPasswordPage settingsPasswordPage = loginIDMenu.openSettingsSubMenu();
        WebElement title = settingsPasswordPage.getTitle();
        assertTrue(title.isDisplayed());
        String titleText = title.getText();

        homePage.openPage();
        settingsPasswordPage.openPage();
        assertEquals(settingsPasswordPage.getTitle().getText(), titleText);
    }

    @Test
    @DisplayName("TC_3_1_TA_1_1 (Login ID - Logout Button)")
    public void test_login_id_logout_button_end_user() throws URISyntaxException {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        LoginIDMenu loginIDMenu = homePage.getLoginIDMenu();
        assertTrue(loginIDMenu.getLogoutButton().isEnabled());

        LoginPage loginPage = loginIDMenu.logout();

        assertTrue(loginPage.getUsernameInput().isDisplayed());
        assertTrue(loginPage.getPasswordInput().isDisplayed());

        URI baseURL = this.getConfig().getBaseUrl().resolve("/");
        URI currentURL = new URI(getDriver().getCurrentUrl()).resolve("/");

        assertTrue(baseURL.equals(currentURL));
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random developer
        if (appUser != null) {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(appUser.getLoginId());
        }
    }

}
