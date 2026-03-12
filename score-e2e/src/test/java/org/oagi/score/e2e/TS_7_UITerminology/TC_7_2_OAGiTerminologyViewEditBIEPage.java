package org.oagi.score.e2e.TS_7_UITerminology;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_7_2_OAGiTerminologyViewEditBIEPage extends BaseTest {

    private AppUserObject appUser;

    @BeforeEach
    public void init() {
        super.init();

        this.appUser = this.getAPIFactory().getAppUserAPI().getAppUserByLoginID("oagis");
        this.appUser.setPassword("oagis");
    }

    @Test
    @DisplayName("TC_7_2_TA_1")
    public void view_edit_bie_page_uses_bie_title() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        homePage.getLoginIDMenu().checkConnectSpecTerminology();

        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        assertEquals("BIE", getText(viewEditBIEPage.getTitle()));
    }

}
