package org.oagi.score.e2e.TS_2_OAGISDeveloperCanManageUsers;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.SignInException;
import org.oagi.score.e2e.menu.AdminMenu;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.LoginPage;
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
public class TC_2_2_OAGISDeveloperCanManageEndUserAccounts extends BaseTest {

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
    @DisplayName("TC_2_2_TA_1")
    public void test_create_new_end_user_account() {
        HomePage homePage = loginPage().signIn(oagisUser.getLoginId(), oagisUser.getPassword());
        AdminMenu adminMenu = homePage.getAdminMenu();
        AccountsPage accountsPage = adminMenu.openAccountsSubMenu();
        NewAccountPage newAccountPage = accountsPage.openNewAccountPage();

        AppUserObject newUser = new AppUserObject();
        newUser.setLoginId("eu_" + RandomStringUtils.secure().nextAlphanumeric(5, 10));
        newUser.setPassword("eu_" + RandomStringUtils.secure().nextAlphanumeric(5, 10));
        newUser.setName(newUser.getLoginId());
        newUser.setOrganization("Test User-Agent");
        newUser.setDeveloper(false);
        newUser.setAdmin(false);
        newUser.setEnabled(true);
        thisAccountWillBeDeletedAfterTests(newUser);

        newAccountPage.createNewAccount(newUser);
        LoginPage loginPage = homePage.logout();
        waitFor(ofMillis(1000L));

        homePage = loginPage.signIn(newUser.getLoginId(), newUser.getPassword());
        WebElement loginIDMenu = homePage.getLoginIDMenu().getLoginIDMenuButton();
        assertTrue(loginIDMenu.getText().contains("(end-user)"));
        assertEquals("Signed in as " + newUser.getLoginId(), homePage.getLoginIDMenu().getSignInLabelText());
    }

    @Test
    @DisplayName("TC_2_2_TA_2")
    public void test_cannot_create_end_user_account_with_duplicated_login_id() {
        // Create random end-user via API
        AppUserObject appUser =
                getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(appUser);

        HomePage homePage = loginPage().signIn(oagisUser.getLoginId(), oagisUser.getPassword());
        AdminMenu adminMenu = homePage.getAdminMenu();
        AccountsPage accountsPage = adminMenu.openAccountsSubMenu();
        NewAccountPage newAccountPage = accountsPage.openNewAccountPage();

        assertThrows(TimeoutException.class, () ->
                newAccountPage.createNewAccount(appUser));
    }

    @Test
    @DisplayName("TC_2_2_TA_3")
    public void should_not_create_new_end_user_account_with_short_password() {
        HomePage homePage = loginPage().signIn(oagisUser.getLoginId(), oagisUser.getPassword());
        AdminMenu adminMenu = homePage.getAdminMenu();
        AccountsPage accountsPage = adminMenu.openAccountsSubMenu();
        NewAccountPage newAccountPage = accountsPage.openNewAccountPage();

        AppUserObject newUser = new AppUserObject();
        newUser.setLoginId("eu_" + RandomStringUtils.secure().nextAlphanumeric(5, 10));
        newUser.setPassword(RandomStringUtils.secure().nextAlphanumeric(1, 1)); // short password
        newUser.setName(newUser.getLoginId());
        newUser.setOrganization("Test User-Agent");
        newUser.setDeveloper(false);
        newUser.setAdmin(false);
        newUser.setEnabled(true);
        thisAccountWillBeDeletedAfterTests(newUser);

        assertThrows(TimeoutException.class, () ->
                newAccountPage.createNewAccount(newUser));

        assertEquals("Password must be at least 5 characters.", newAccountPage.getPasswordErrorMessage());
    }

    @Test
    @DisplayName("TC_2_2_TA_4")
    public void admin_user_can_update_login_ID_field_of_another_end_user_account() {
        // Create random end-user via API
        AppUserObject appUser =
                getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(appUser);

        HomePage homePage = loginPage().signIn(oagisUser.getLoginId(), oagisUser.getPassword());
        AdminMenu adminMenu = homePage.getAdminMenu();
        AccountsPage accountsPage = adminMenu.openAccountsSubMenu();
        EditAccountPage editAccountPage = accountsPage.openEditAccountPageByLoginID(appUser.getLoginId());
        assertTrue(editAccountPage.getLoginIDField().isEnabled());
    }

    @Test
    @DisplayName("TC_2_2_TA_5")
    public void admin_user_can_update_password_of_another_end_user_account() {
        // Create random end-user via API
        AppUserObject appUser =
                getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(appUser);

        HomePage homePage = loginPage().signIn(oagisUser.getLoginId(), oagisUser.getPassword());
        AdminMenu adminMenu = homePage.getAdminMenu();
        AccountsPage accountsPage = adminMenu.openAccountsSubMenu();
        EditAccountPage editAccountPage = accountsPage.openEditAccountPageByLoginID(appUser.getLoginId());

        String newPassword = "eu_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        editAccountPage.updatePassword(newPassword);

        homePage.logout();
        homePage = loginPage().signIn(appUser.getLoginId(), newPassword);
        WebElement loginIDMenu = homePage.getLoginIDMenu().getLoginIDMenuButton();
        assertTrue(loginIDMenu.getText().contains(appUser.getLoginId()));
    }

    @Test
    @DisplayName("TC_2_2_TA_6")
    public void admin_user_cannot_update_password_of_another_end_user_account_with_short_password() {
        // Create random end-user via API
        AppUserObject appUser =
                getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
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
        waitFor(ofMillis(1000L));

        homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        WebElement loginIDMenu = homePage.getLoginIDMenu().getLoginIDMenuButton();
        assertTrue(loginIDMenu.getText().contains(appUser.getLoginId()));
    }

    @Test
    @DisplayName("TC_2_2_TA_7")
    public void admin_user_can_disable_another_end_user_account() {
        // Create random end-user via API
        AppUserObject appUser =
                getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
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
    @DisplayName("TC_2_2_TA_8")
    public void admin_user_can_enable_another_end_user_account() {
        // Create random end-user via API
        AppUserObject appUser =
                getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
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
    @DisplayName("TC_2_2_TA_9")
    @Disabled
    public void test_TA_9() {
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
