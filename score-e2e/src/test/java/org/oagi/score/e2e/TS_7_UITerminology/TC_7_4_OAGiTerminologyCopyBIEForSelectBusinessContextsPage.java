package org.oagi.score.e2e.TS_7_UITerminology;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.CopyBIEForSelectBIEPage;
import org.oagi.score.e2e.page.bie.CopyBIEForSelectBusinessContextsPage;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_7_4_OAGiTerminologyCopyBIEForSelectBusinessContextsPage extends BaseTest {

    private AppUserObject appUser;

    private BusinessContextObject randomBusinessContext;

    @BeforeEach
    public void init() {
        super.init();

        this.appUser = this.getAPIFactory().getAppUserAPI().getAppUserByLoginID("oagis");
        this.appUser.setPassword("oagis");
    }

    @Test
    @DisplayName("TC_7_4_TA_1")
    public void test_copy_bie_page_title_first_page() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        homePage.getLoginIDMenu().checkOAGISTerminology();
        BIEMenu bieMenu = homePage.getBIEMenu();
        assertTrue(bieMenu.getCopyBIESubMenu().isEnabled());
        String pageTitle = getText(homePage.getBIEMenu().openCopyBIESubMenu().getTitle());
        assertEquals("Copy BIE (Profiled Component, Noun, BOD)", pageTitle);
    }

    @Test
    @DisplayName("TC_7_4_TA_2")
    public void test_copy_bie_page_title_second_page() {

        //test assertion in the documentation should be changed because the subtitle of the second page is not “Select BIE (Profiled Component, Noun, BOD)”

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        homePage.getLoginIDMenu().checkOAGISTerminology();

        BIEMenu bieMenu = homePage.getBIEMenu();
        assertTrue(bieMenu.getCopyBIESubMenu().isEnabled());

        CopyBIEForSelectBusinessContextsPage copyBIEForSelectBusinessContextsPage =
                bieMenu.openCopyBIESubMenu();

        this.randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(appUser);
        CopyBIEForSelectBIEPage copyBIEForSelectBIEPage =
                copyBIEForSelectBusinessContextsPage.next(Arrays.asList(randomBusinessContext));
        String title = getText(copyBIEForSelectBIEPage.getTitle());
        assertEquals("Copy BIE (Profiled Component, Noun, BOD) with " + randomBusinessContext.getName(), title);
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random business contexts and associated records
        if (randomBusinessContext != null) {
            getAPIFactory().getBusinessContextAPI().deleteBusinessContextById(
                    randomBusinessContext.getBusinessContextId());
        }
    }

}
