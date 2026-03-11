package org.oagi.score.e2e.TS_3_EndUserAuthorizedFunctions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class TC_3_2_EndUsersAuthorizedFunctionalitiesInStandardBrowsingMode extends BaseTest {

    private AppUserObject appUser;
    private boolean previousBrowseStandardModeEnabled;

    @BeforeEach
    public void init() {
        super.init();

        previousBrowseStandardModeEnabled = getAPIFactory().getApplicationSettingsAPI().isBrowseStandardModeEnabled();
        getAPIFactory().getApplicationSettingsAPI().setBrowseStandardModeEnable(true);

        appUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
    }

    @Test
    @DisplayName("TC_3_2_TA_1 (Browse Standard Menu)")
    public void test_browse_standard_menu_end_user() {
        assertTrue(getAPIFactory().getApplicationSettingsAPI().isBrowseStandardModeEnabled());

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        assertTrue(homePage.hasBrowseStandardMenu());
        assertFalse(homePage.hasCoreComponentMenu());

        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.openBrowseStandardMenu();
        WebElement title = viewEditCoreComponentPage.getTitle();
        assertTrue(title.isDisplayed());
        assertEquals("Standard", title.getText());
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        if (appUser != null) {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(appUser.getLoginId());
        }

        getAPIFactory().getApplicationSettingsAPI().setBrowseStandardModeEnable(previousBrowseStandardModeEnabled);
    }

}
