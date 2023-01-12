package org.oagi.score.e2e.TS_9_DataRetention;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.menu.AdminMenu;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.admin.AccountsPage;
import org.oagi.score.e2e.page.admin.EditAccountPage;
import org.oagi.score.e2e.page.admin.NewAccountPage;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Execution(ExecutionMode.CONCURRENT)
public class TC_9_1_NoUserAccountCanBeDeleted extends BaseTest {
    private AppUserObject appUser;

    private List<AppUserObject> newAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

        this.appUser = this.getAPIFactory().getAppUserAPI().getAppUserByLoginID("oagis");
        this.appUser.setPassword("oagis");
    }

    @Test
    @DisplayName("TC_9_1_TA_1")
    public void test_eu_account_cannot_be_deleted() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());

        AdminMenu adminMenu = homePage.getAdminMenu();
        AccountsPage accountsPage = adminMenu.openAccountsSubMenu();
        NewAccountPage newAccountPage = accountsPage.openNewAccountPage();

        AppUserObject newUser = new AppUserObject();
        newUser.setLoginId("eu_" + randomAlphanumeric(5, 10));
        newUser.setPassword("eu_" + randomAlphanumeric(5, 10));
        newUser.setName(newUser.getLoginId());
        newUser.setOrganization("Test User-Agent");
        newUser.setDeveloper(false);
        newUser.setAdmin(false);
        newUser.setEnabled(true);
        thisAccountWillBeDeletedAfterTests(newUser);
        newAccountPage.createNewAccount(newUser);

        EditAccountPage editAccountPage = accountsPage.openEditAccountPageByLoginID(newUser.getLoginId());
        assertEquals(false, editAccountPage.deleteAccountButtonIsPresent());
    }

    @Test
    @DisplayName("TC_9_1_TA_2")
    public void test_dev_account_cannot_be_deleted() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());

        AdminMenu adminMenu = homePage.getAdminMenu();
        AccountsPage accountsPage = adminMenu.openAccountsSubMenu();
        NewAccountPage newAccountPage = accountsPage.openNewAccountPage();

        AppUserObject newUser = new AppUserObject();
        newUser.setLoginId("dev_" + randomAlphanumeric(5, 10));
        newUser.setPassword("dev_" + randomAlphanumeric(5, 10));
        newUser.setName(newUser.getLoginId());
        newUser.setOrganization("Developer-Agent");
        newUser.setDeveloper(true);
        newUser.setAdmin(false);
        newUser.setEnabled(true);
        thisAccountWillBeDeletedAfterTests(newUser);
        newAccountPage.createNewAccount(newUser);

        EditAccountPage editAccountPage = accountsPage.openEditAccountPageByLoginID(newUser.getLoginId());
        assertEquals(false, editAccountPage.deleteAccountButtonIsPresent());
    }


    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.newAccounts.add(appUser);
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
