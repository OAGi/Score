package org.oagi.score.e2e.TS_2_OAGISDeveloperCanManageUsers;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.AccountUpdateException;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.SignInException;
import org.oagi.score.e2e.menu.AdminMenu;
import org.oagi.score.e2e.menu.LoginIDMenu;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.LoginPage;
import org.oagi.score.e2e.page.SettingsAccountPage;
import org.oagi.score.e2e.page.admin.AccountsPage;
import org.oagi.score.e2e.page.admin.EditAccountPage;
import org.oagi.score.e2e.page.admin.NewAccountPage;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

@Execution(ExecutionMode.CONCURRENT)
public class TC_2_1_OAGISDeveloperCanManageOAGISDeveloperAccounts extends BaseTest {

    private AppUserObject oagisUser;

    private List<AppUserObject> newAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

        this.oagisUser = getAPIFactory().getAppUserAPI().getAppUserByLoginID("oagis");
        this.oagisUser.setPassword("oagis");
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.newAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_2_1_TA_1")
    public void test_create_new_developer_account() {
        HomePage homePage = loginPage().signIn(oagisUser.getLoginId(), oagisUser.getPassword());
        AdminMenu adminMenu = homePage.getAdminMenu();
        AccountsPage accountsPage = adminMenu.openAccountsSubMenu();
        NewAccountPage newAccountPage = accountsPage.openNewAccountPage();

        AppUserObject newUser = new AppUserObject();
        newUser.setLoginId("dev_" + RandomStringUtils.secure().nextAlphanumeric(5, 10));
        newUser.setPassword("dev_" + RandomStringUtils.secure().nextAlphanumeric(5, 10));
        newUser.setName(newUser.getLoginId());
        newUser.setOrganization("Test User-Agent");
        newUser.setDeveloper(true);
        newUser.setAdmin(false);
        newUser.setEnabled(true);
        thisAccountWillBeDeletedAfterTests(newUser);

        newAccountPage.createNewAccount(newUser);
        LoginPage loginPage = homePage.logout();
        waitFor(ofMillis(1000L));

        homePage = loginPage.signIn(newUser.getLoginId(), newUser.getPassword());
        WebElement loginIDMenu = homePage.getLoginIDMenu().getLoginIDMenuButton();
        assertTrue(loginIDMenu.getText().contains("(developer)"));
        assertEquals("Signed in as " + newUser.getLoginId(), homePage.getLoginIDMenu().getSignInLabelText());
    }

    @Test
    @DisplayName("TC_2_1_TA_2")
    public void test_cannot_create_developer_account_with_duplicated_login_id() {
        // Create random developer via API
        AppUserObject appUser =
                getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(appUser);

        HomePage homePage = loginPage().signIn(oagisUser.getLoginId(), oagisUser.getPassword());
        AdminMenu adminMenu = homePage.getAdminMenu();
        AccountsPage accountsPage = adminMenu.openAccountsSubMenu();
        NewAccountPage newAccountPage = accountsPage.openNewAccountPage();

        assertThrows(TimeoutException.class, () ->
                newAccountPage.createNewAccount(appUser));
    }

    @Test
    @DisplayName("TC_2_1_TA_3")
    public void should_not_create_new_developer_account_with_short_password() {
        HomePage homePage = loginPage().signIn(oagisUser.getLoginId(), oagisUser.getPassword());
        AdminMenu adminMenu = homePage.getAdminMenu();
        AccountsPage accountsPage = adminMenu.openAccountsSubMenu();
        NewAccountPage newAccountPage = accountsPage.openNewAccountPage();

        AppUserObject newUser = new AppUserObject();
        newUser.setLoginId("dev_" + RandomStringUtils.secure().nextAlphanumeric(5, 10));
        newUser.setPassword(RandomStringUtils.secure().nextAlphanumeric(1, 1)); // short password
        newUser.setName(newUser.getLoginId());
        newUser.setOrganization("Test User-Agent");
        newUser.setDeveloper(true);
        newUser.setAdmin(false);
        newUser.setEnabled(true);
        thisAccountWillBeDeletedAfterTests(newUser);

        assertThrows(TimeoutException.class, () ->
                newAccountPage.createNewAccount(newUser));

        assertEquals("Password must be at least 5 characters.", newAccountPage.getPasswordErrorMessage());
    }

    @Test
    @DisplayName("TC_2_1_TA_4")
    public void admin_user_can_update_login_ID_field_of_another_developer_account() {
        // Create random developer via API
        AppUserObject appUser =
                getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(appUser);

        HomePage homePage = loginPage().signIn(oagisUser.getLoginId(), oagisUser.getPassword());
        AdminMenu adminMenu = homePage.getAdminMenu();
        AccountsPage accountsPage = adminMenu.openAccountsSubMenu();
        EditAccountPage editAccountPage = accountsPage.openEditAccountPageByLoginID(appUser.getLoginId());
        assertTrue(editAccountPage.getLoginIDField().isEnabled());
    }

    @Test
    @DisplayName("TC_2_1_TA_5")
    public void admin_user_can_update_password_of_another_developer_account() {
        // Create random developer via API
        AppUserObject appUser =
                getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(appUser);

        HomePage homePage = loginPage().signIn(oagisUser.getLoginId(), oagisUser.getPassword());
        AdminMenu adminMenu = homePage.getAdminMenu();
        AccountsPage accountsPage = adminMenu.openAccountsSubMenu();
        EditAccountPage editAccountPage = accountsPage.openEditAccountPageByLoginID(appUser.getLoginId());

        String newPassword = "dev_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        editAccountPage.updatePassword(newPassword);

        homePage.logout();
        homePage = loginPage().signIn(appUser.getLoginId(), newPassword);
        WebElement loginIDMenu = homePage.getLoginIDMenu().getLoginIDMenuButton();
        assertTrue(loginIDMenu.getText().contains(appUser.getLoginId()));
    }

    @Test
    @DisplayName("TC_2_1_TA_6")
    public void admin_user_cannot_update_password_of_another_developer_account_with_short_password() {
        // Create random developer via API
        AppUserObject appUser =
                getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(appUser);

        HomePage homePage = loginPage().signIn(oagisUser.getLoginId(), oagisUser.getPassword());
        AdminMenu adminMenu = homePage.getAdminMenu();
        AccountsPage accountsPage = adminMenu.openAccountsSubMenu();
        EditAccountPage editAccountPage = accountsPage.openEditAccountPageByLoginID(appUser.getLoginId());

        String newPassword = RandomStringUtils.secure().nextAlphanumeric(1, 1);
        assertThrows(TimeoutException.class, () ->
                editAccountPage.updatePassword(newPassword));

        assertEquals("Password must be at least 5 characters.", editAccountPage.getPasswordErrorMessage());

        homePage.logout();
        homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        WebElement loginIDMenu = homePage.getLoginIDMenu().getLoginIDMenuButton();
        assertTrue(loginIDMenu.getText().contains(appUser.getLoginId()));
    }

    @Test
    @DisplayName("TC_2_1_TA_7")
    public void admin_user_can_update_login_ID_field_of_self() {
        // Create random admin via API
        AppUserObject appUser =
                getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        AdminMenu adminMenu = homePage.getAdminMenu();
        AccountsPage accountsPage = adminMenu.openAccountsSubMenu();
        EditAccountPage editAccountPage = accountsPage.openEditAccountPageByLoginID(appUser.getLoginId());
        assertTrue(editAccountPage.getLoginIDField().isEnabled());
    }

    @Test
    @DisplayName("TC_2_1_TA_8")
    public void developer_user_can_update_password_of_self() {
        // Create random developer via API
        AppUserObject appUser =
                getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        LoginIDMenu loginIDMenu = homePage.getLoginIDMenu();
        SettingsAccountPage settingsAccountPage = loginIDMenu.openSettingsSubMenu();

        String newPassword = "dev_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        settingsAccountPage.updatePassword(appUser.getPassword(), newPassword);

        homePage.logout();
        homePage = loginPage().signIn(appUser.getLoginId(), newPassword);
        WebElement loginIDMenuButton = homePage.getLoginIDMenu().getLoginIDMenuButton();
        assertTrue(loginIDMenuButton.getText().contains(appUser.getLoginId()));
    }

    @Test
    @DisplayName("TC_2_1_TA_9")
    public void cannot_update_password_of_self_with_wrong_old_password() {
        // Create random developer via API
        AppUserObject appUser =
                getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        LoginIDMenu loginIDMenu = homePage.getLoginIDMenu();
        SettingsAccountPage settingsAccountPage = loginIDMenu.openSettingsSubMenu();

        String wrongOldPassword = "dev_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        assertFalse(wrongOldPassword.equals(appUser.getPassword()));

        String newPassword = "dev_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        AccountUpdateException error = assertThrows(AccountUpdateException.class, () ->
                settingsAccountPage.updatePassword(wrongOldPassword, newPassword));

        assertEquals("Invalid old password", error.getMessage());
    }

    @Test
    @DisplayName("TC_2_1_TA_10")
    public void admin_user_cannot_update_password_of_self_with_short_password() {
        // Create random developer via API
        AppUserObject appUser =
                getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        LoginIDMenu loginIDMenu = homePage.getLoginIDMenu();
        SettingsAccountPage settingsAccountPage = loginIDMenu.openSettingsSubMenu();

        String newPassword = RandomStringUtils.secure().nextAlphanumeric(1, 1);
        assertThrows(TimeoutException.class, () ->
                settingsAccountPage.updatePassword(appUser.getPassword(), newPassword));

        assertEquals("Password must be at least 5 characters.", settingsAccountPage.getPasswordErrorMessage());
    }

    @Test
    @DisplayName("TC_2_1_TA_11")
    public void admin_user_cannot_update_password_of_self_with_wrong_confirm_new_password() {
        // Create random developer via API
        AppUserObject appUser =
                getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        LoginIDMenu loginIDMenu = homePage.getLoginIDMenu();
        SettingsAccountPage settingsAccountPage = loginIDMenu.openSettingsSubMenu();

        String newPassword = "dev_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String confirmNewPassword = "dev_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        assertFalse(newPassword.equals(confirmNewPassword));

        assertThrows(TimeoutException.class, () ->
                settingsAccountPage.updatePassword(appUser.getPassword(), newPassword, confirmNewPassword));
    }

    @Test
    @DisplayName("TC_2_1_TA_12")
    public void admin_user_can_disable_another_developer_account() {
        // Create random developer via API
        AppUserObject appUser =
                getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(appUser);

        HomePage homePage = loginPage().signIn(oagisUser.getLoginId(), oagisUser.getPassword());
        AdminMenu adminMenu = homePage.getAdminMenu();
        AccountsPage accountsPage = adminMenu.openAccountsSubMenu();
        EditAccountPage editAccountPage = accountsPage.openEditAccountPageByLoginID(appUser.getLoginId());
        editAccountPage.disableAccount();

        homePage.logout();
        SignInException expectedError = assertThrows(SignInException.class, () ->
                loginPage().signIn(appUser.getLoginId(), appUser.getPassword()));

        assertEquals("Account is disabled", expectedError.getMessage());
    }

    @Test
    @DisplayName("TC_2_1_TA_13")
    public void admin_user_can_enable_another_developer_account() {
        // Create random developer via API
        AppUserObject appUser =
                getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(appUser);

        getAPIFactory().getAppUserAPI().disableAccount(appUser);

        HomePage homePage = loginPage().signIn(oagisUser.getLoginId(), oagisUser.getPassword());
        AdminMenu adminMenu = homePage.getAdminMenu();
        AccountsPage accountsPage = adminMenu.openAccountsSubMenu();
        EditAccountPage editAccountPage = accountsPage.openEditAccountPageByLoginID(appUser.getLoginId());
        editAccountPage.enableAccount();

        homePage.logout();
        homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        WebElement loginIDMenuButton = homePage.getLoginIDMenu().getLoginIDMenuButton();
        assertTrue(loginIDMenuButton.getText().contains(appUser.getLoginId()));
    }

    @Test
    @DisplayName("TC_2_1_TA_14")
    @Disabled
    public void test_TA_14() {
        // SSO cannot be tested yet
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete new accounts
        this.newAccounts.forEach(newUser -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId());
        });
    }

}
