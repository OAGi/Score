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
import org.oagi.score.e2e.page.bie.CreateBIEForSelectBusinessContextsPage;
import org.oagi.score.e2e.page.bie.CreateBIEForSelectTopLevelConceptPage;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_7_3_OAGiTerminologyCreateBIEForSelectBusinessContextsPage extends BaseTest {

    private AppUserObject appUser;

    private BusinessContextObject randomBusinessContext;

    @BeforeEach
    public void init() {
        super.init();

        this.appUser = this.getAPIFactory().getAppUserAPI().getAppUserByLoginID("oagis");
        this.appUser.setPassword("oagis");
    }

    @Test
    @DisplayName("TC_7_3_TA_1")
    public void test_create_bie_page_title_first_page() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        homePage.getLoginIDMenu().checkOAGISTerminology();
        BIEMenu bieMenu = homePage.getBIEMenu();
        assertTrue(bieMenu.getCreateBIESubMenu().isEnabled());
        String createBIEPageTitle = getText(bieMenu.openCreateBIESubMenu().getTitle());
        assertEquals("Create BIE (Profiled Component, Noun, BOD)", createBIEPageTitle);
    }

    @Test
    @DisplayName("TC_7_3_TA_2")
    public void test_create_bie_page_title_second_page() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        homePage.getLoginIDMenu().checkOAGISTerminology();

        BIEMenu bieMenu = homePage.getBIEMenu();
        assertTrue(bieMenu.getCreateBIESubMenu().isEnabled());

        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage =
                bieMenu.openCreateBIESubMenu();

        this.randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(appUser);
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage =
                createBIEForSelectBusinessContextsPage.next(Arrays.asList(randomBusinessContext));
        String title = getText(createBIEForSelectTopLevelConceptPage.getTitle());
        assertEquals("Create BIE (Profiled Component, Noun, BOD)", title);
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
