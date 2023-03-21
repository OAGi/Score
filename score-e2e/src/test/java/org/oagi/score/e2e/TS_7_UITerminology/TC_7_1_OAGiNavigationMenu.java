package org.oagi.score.e2e.TS_7_UITerminology;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.menu.CoreComponentMenu;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.HomePage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class TC_7_1_OAGiNavigationMenu extends BaseTest {

    private AppUserObject appUser;

    @BeforeEach
    public void init() {
        super.init();

        this.appUser = this.getAPIFactory().getAppUserAPI().getAppUserByLoginID("oagis");
        this.appUser.setPassword("oagis");
    }

    @Test
    @DisplayName("TC_7_1_TA_1_to_TA_6")
    public void test_TA_1_to_TA_6() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        homePage.getLoginIDMenu().getOAGISTerminologyButton().click();
        BIEMenu bieMenu = homePage.getBIEMenu();
        assertTrue(bieMenu.getCreateBIESubMenu().isEnabled());

        String bieMenuTitle = bieMenu.getBIEMenuButtonTitle();
        assertEquals("Manage Profiled Component, Noun, BOD", bieMenuTitle);


        String createBIETitle = bieMenu.getCreateBIESubMenuButtonTitle();
        assertEquals("Profile a Component, Noun, BOD", createBIETitle);

        String listBIETitle = bieMenu.getViewEditBIESubMenuButtonTitle();
        assertEquals("List of Profiled Components, Nouns, BODs", listBIETitle);

        String copyBIETitle = bieMenu.getCopyBIESubMenuButtonTitle();
        assertEquals("Copy a Profiled Component, Noun, BOD", copyBIETitle);

        String generateExprTitle = bieMenu.getGenerateExpressionButtonTitle();
        assertEquals("Generate Expression for a Profiled Component, Noun, BOD", generateExprTitle);

        CoreComponentMenu ccMenu = homePage.getCoreComponentMenu();

        String ccMenuTitle = ccMenu.getCoreComponentMenuButtonTitle();
        assertEquals("Manage Model Library", ccMenuTitle);
    }

}
