package org.oagi.score.e2e.TS_26_UserGuideIsAccessbile;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.help.UserGuidePage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class TC_26_1_UserGuideIsAccessible extends BaseTest {

    private AppUserObject appEndUser;

    private AppUserObject appDevUser;

    private List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

        // Create random end-user
        appEndUser = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(appEndUser);
        // Create random developer-user
        appDevUser = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(appDevUser);
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_26_1_1")
    public void link_to_user_guide_is_available_to_oagis_developer_and_is_resolved() {
        HomePage homePage = loginPage().signIn(appDevUser.getLoginId(), appDevUser.getPassword());
        UserGuidePage userGuidePage = homePage.getHelpMenu().openUserGuideSubMenu();
        assertTrue(userGuidePage.getTitle().isDisplayed());
    }

    @Test
    @DisplayName("TC_26_1_2")
    public void link_to_user_guide_is_available_to_end_user_and_is_resolved() {
        HomePage homePage = loginPage().signIn(appEndUser.getLoginId(), appEndUser.getPassword());
        UserGuidePage userGuidePage = homePage.getHelpMenu().openUserGuideSubMenu();
        assertTrue(userGuidePage.getTitle().isDisplayed());

    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random accounts
        this.randomAccounts.forEach(randomAccount -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId());
        });
    }
}
