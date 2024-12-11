package org.oagi.score.e2e.TS_4_EndUsersProfileManagement;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.AccountUpdateException;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.menu.AdminMenu;
import org.oagi.score.e2e.menu.ContextMenu;
import org.oagi.score.e2e.menu.LoginIDMenu;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.SettingsAccountPage;
import org.oagi.score.e2e.page.context.ViewEditContextCategoryPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_4_1_EndUsersProfileManagement extends BaseTest {

    private AppUserObject appUser;

    @BeforeEach
    public void init() {
        super.init();

        // Create random end-user
        appUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
    }

    @Test
    @DisplayName("TC_4_1_TA_1_and_TA_2")
    public void end_user_cannot_see_admin_menu() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        AdminMenu adminMenu = homePage.getAdminMenu();
        assertThrows(TimeoutException.class, () -> adminMenu.getAdminMenu());
    }

    @Test
    @DisplayName("TC_4_1_TA_3")
    public void login_ID_field_must_not_be_present_in_settings_page() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        LoginIDMenu loginIDMenu = homePage.getLoginIDMenu();
        SettingsAccountPage settingsAccountPage = loginIDMenu.openSettingsSubMenu();

        By loginIDFieldLocator =
                By.xpath("//mat-label[contains(text(), \"Login ID\")]//ancestor::div[1]/input");
        assertThrows(NoSuchElementException.class, () ->
                getDriver().findElement(loginIDFieldLocator));
    }

    @Test
    @DisplayName("TC_4_1_TA_4")
    public void end_user_can_update_password_of_self() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        LoginIDMenu loginIDMenu = homePage.getLoginIDMenu();
        SettingsAccountPage settingsAccountPage = loginIDMenu.openSettingsSubMenu();

        String newPassword = "eu_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        settingsAccountPage.updatePassword(appUser.getPassword(), newPassword);

        homePage.logout();
        homePage = loginPage().signIn(appUser.getLoginId(), newPassword);
        WebElement loginIDMenuButton = homePage.getLoginIDMenu().getLoginIDMenuButton();
        assertTrue(loginIDMenuButton.getText().contains(appUser.getLoginId()));
    }

    @Test
    @DisplayName("TC_4_1_TA_5")
    public void end_user_cannot_update_password_of_self_with_short_password() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        LoginIDMenu loginIDMenu = homePage.getLoginIDMenu();
        SettingsAccountPage settingsAccountPage = loginIDMenu.openSettingsSubMenu();

        String newPassword = RandomStringUtils.secure().nextAlphanumeric(1, 1);
        assertThrows(TimeoutException.class, () ->
                settingsAccountPage.updatePassword(appUser.getPassword(), newPassword));

        assertEquals("Password must be at least 5 characters.", settingsAccountPage.getPasswordErrorMessage());
    }

    @Test
    @DisplayName("TC_4_1_TA_6")
    public void cannot_update_password_of_self_with_wrong_old_password() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        LoginIDMenu loginIDMenu = homePage.getLoginIDMenu();
        SettingsAccountPage settingsAccountPage = loginIDMenu.openSettingsSubMenu();

        String wrongOldPassword = "eu_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        assertFalse(wrongOldPassword.equals(appUser.getPassword()));

        String newPassword = "eu_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        AccountUpdateException error = assertThrows(AccountUpdateException.class, () ->
                settingsAccountPage.updatePassword(wrongOldPassword, newPassword));

        assertEquals("Invalid old password", error.getMessage());
    }

    @Test
    @DisplayName("TC_4_1_TA_7")
    public void end_user_cannot_update_password_of_self_with_wrong_confirm_new_password() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        LoginIDMenu loginIDMenu = homePage.getLoginIDMenu();
        SettingsAccountPage settingsAccountPage = loginIDMenu.openSettingsSubMenu();

        String newPassword = "eu_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String confirmNewPassword = "eu_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        assertFalse(newPassword.equals(confirmNewPassword));

        assertThrows(TimeoutException.class, () ->
                settingsAccountPage.updatePassword(appUser.getPassword(), newPassword, confirmNewPassword));
    }

    @Test
    @DisplayName("TC_4_1_TA_8")
    public void end_user_cannot_see_admin_menu_after_context_category_menu_opened() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextCategoryPage viewEditContextCategoryPage =
                contextMenu.openViewEditContextCategorySubMenu();
        assertTrue(viewEditContextCategoryPage.isOpened());

        AdminMenu adminMenu = homePage.getAdminMenu();
        assertThrows(TimeoutException.class, () -> adminMenu.getAdminMenu());
    }

    @Test
    @DisplayName("TC_4_1_TA_9")
    @Disabled
    public void test_TA_9() {
        // SSO cannot be checked yet
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random end-user
        if (appUser != null) {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(appUser.getLoginId());
        }
    }

}
