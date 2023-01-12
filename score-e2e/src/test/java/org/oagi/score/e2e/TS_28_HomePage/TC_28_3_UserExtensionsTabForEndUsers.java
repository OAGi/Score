package org.oagi.score.e2e.TS_28_HomePage;

import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.oagi.score.e2e.page.core_component.ACCExtensionViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_28_3_UserExtensionsTabForEndUsers extends BaseTest {

    private List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    private class UserTopLevelASBIEPContainer {

        private AppUserObject appUser;
        private NamespaceObject userNamespace;
        int numberOfWIPUEGs;
        int numberOfQAUEGs;
        int numberOfProductionUEGs;

        private List<Pair<String, String>> ccWIPList = new ArrayList<Pair<String, String>>();
        private List<Pair<String, String>> ccQAList = new ArrayList<Pair<String, String>>();
        private List<Pair<String, String>> ccProductionList = new ArrayList<Pair<String, String>>();

        public UserTopLevelASBIEPContainer(AppUserObject appUser, ReleaseObject release, NamespaceObject namespace) {
            this(appUser, release, namespace, nextInt(1, 3), nextInt(1, 3), nextInt(1, 3));
        }

        public UserTopLevelASBIEPContainer(AppUserObject appUser, ReleaseObject release, NamespaceObject userNamespace,
                                           int numberOfWIPUEGs, int numberOfQAUEGs, int numberOfProductionUEGs) {
            this.appUser = appUser;
            this.userNamespace = userNamespace;
            this.numberOfWIPUEGs = numberOfWIPUEGs;
            this.numberOfQAUEGs = numberOfQAUEGs;
            this.numberOfProductionUEGs = numberOfProductionUEGs;

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(this.appUser);
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            HomePage homePage = loginPage().signIn(this.appUser.getLoginId(), this.appUser.getPassword());
            for (int i = 0; i < numberOfWIPUEGs; ++i) {
                ASCCPObject asccp;
                {
                    CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
                    NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

                    ACCObject acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
                    coreComponentAPI.appendExtension(acc, developer, namespace, "Published");

                    asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
                }
                TopLevelASBIEPObject topLevelAsbiep = getAPIFactory().getBusinessInformationEntityAPI()
                        .generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, this.appUser, "WIP");

                BIEMenu bieMenu = homePage.getBIEMenu();
                ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
                EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
                ACCExtensionViewEditPage accExtensionViewEditPage =
                        editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
                accExtensionViewEditPage.setNamespace(userNamespace);
                accExtensionViewEditPage.hitUpdateButton();
                ccWIPList.add(new Pair<String, String>(accExtensionViewEditPage.getDENFieldValue(), this.appUser.getLoginId()));
            }

            for (int i = 0; i < numberOfQAUEGs; ++i) {
                ASCCPObject asccp;
                {
                    CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
                    NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

                    ACCObject acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
                    coreComponentAPI.appendExtension(acc, developer, namespace, "Published");

                    asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
                }
                TopLevelASBIEPObject topLevelAsbiep = getAPIFactory().getBusinessInformationEntityAPI()
                        .generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, this.appUser, "WIP");

                BIEMenu bieMenu = homePage.getBIEMenu();
                ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
                EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
                ACCExtensionViewEditPage accExtensionViewEditPage =
                        editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
                accExtensionViewEditPage.setNamespace(userNamespace);
                accExtensionViewEditPage.hitUpdateButton();
                ccQAList.add(new Pair<String, String>(accExtensionViewEditPage.getDENFieldValue(), this.appUser.getLoginId()));
                accExtensionViewEditPage.moveToQA();

                topLevelAsbiep.setState("QA");
                getAPIFactory().getBusinessInformationEntityAPI()
                        .updateTopLevelASBIEP(topLevelAsbiep);

            }

            for (int i = 0; i < numberOfProductionUEGs; ++i) {
                ASCCPObject asccp;
                {
                    CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
                    NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

                    ACCObject acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
                    coreComponentAPI.appendExtension(acc, developer, namespace, "Published");

                    asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
                }
                TopLevelASBIEPObject topLevelAsbiep = getAPIFactory().getBusinessInformationEntityAPI()
                        .generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, this.appUser, "WIP");

                BIEMenu bieMenu = homePage.getBIEMenu();
                ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
                EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
                ACCExtensionViewEditPage accExtensionViewEditPage =
                        editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
                accExtensionViewEditPage.setNamespace(userNamespace);
                accExtensionViewEditPage.hitUpdateButton();

                ccProductionList.add(new Pair<String, String>(accExtensionViewEditPage.getDENFieldValue(), this.appUser.getLoginId()));
                accExtensionViewEditPage.moveToQA();

                topLevelAsbiep.setState("QA");
                getAPIFactory().getBusinessInformationEntityAPI()
                        .updateTopLevelASBIEP(topLevelAsbiep);

                accExtensionViewEditPage.moveToProduction();

                topLevelAsbiep.setState("Production");
                getAPIFactory().getBusinessInformationEntityAPI()
                        .updateTopLevelASBIEP(topLevelAsbiep);
            }

            homePage.logout();
        }

    }

    private static int extractNumberFromText(String text) {
        return Integer.valueOf(text.replaceAll("\\D", ""));
    }

    @Test
    @DisplayName("TC_28_3_1")
    public void end_user_can_see_number_of_all_extensions_per_state_in_total_user_extensions_panel() {
        AppUserObject endUser1 = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser1);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");

        NamespaceObject endUserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser1);

        UserTopLevelASBIEPContainer container1 = new UserTopLevelASBIEPContainer(endUser1, release, endUserNamespace);

        HomePage homePage = loginPage().signIn(endUser1.getLoginId(), endUser1.getPassword());

        HomePage.TotalUEsByStatesPanel totalUEsByStatesPanel = homePage.openTotalUEsByStatesPanel();
        click(homePage.getUserExtensionsTab());
        WebElement wipStateInTotalTab = totalUEsByStatesPanel.getStateProgressBarByState("WIP");
        assertTrue(wipStateInTotalTab.isDisplayed());
        assertTrue(container1.numberOfWIPUEGs <= extractNumberFromText(getText(wipStateInTotalTab)));

        WebElement qaStateInTotalTab = totalUEsByStatesPanel.getStateProgressBarByState("QA");
        assertTrue(qaStateInTotalTab.isDisplayed());
        assertTrue(container1.numberOfQAUEGs  <= extractNumberFromText(getText(qaStateInTotalTab)));

        WebElement productionStateInTotalTab = totalUEsByStatesPanel.getStateProgressBarByState("Production");
        assertTrue(productionStateInTotalTab.isDisplayed());
        assertTrue(container1.numberOfProductionUEGs <= extractNumberFromText(getText(productionStateInTotalTab)));

    }

    @Test
    @DisplayName("TC_28_3_2")
    public void end_user_can_click_state_to_view_extensions_of_that_state_in_total_user_extensions_panel() {

        AppUserObject endUser1 = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser1);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");

        NamespaceObject endUserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser1);

        UserTopLevelASBIEPContainer container1 = new UserTopLevelASBIEPContainer(endUser1, release, endUserNamespace);

        HomePage homePage = loginPage().signIn(endUser1.getLoginId(), endUser1.getPassword());
        homePage.setBranch(release.getReleaseNumber());

        click(homePage.getUserExtensionsTab());
        ViewEditCoreComponentPage viewEditCCPageForWIP = homePage.openTotalUEsByStatesPanel()
                .clickStateProgressBar("WIP");
        click(viewEditCCPageForWIP.getSearchButton());

        assertTrue(container1.numberOfWIPUEGs <= viewEditCCPageForWIP.getNumberOfOnlyCCsPerStateAreListed("WIP"));
        assertEquals(0, viewEditCCPageForWIP.getNumberOfOnlyCCsPerStateAreListed("QA"));
        assertEquals(0, viewEditCCPageForWIP.getNumberOfOnlyCCsPerStateAreListed("Production"));

        //verify Core Component is listed
        for (int i = 0; i < container1.numberOfWIPUEGs; i++) {
            String ccName = container1.ccWIPList.get(i).getKey();
            String ownerName = container1.ccWIPList.get(i).getValue();
            assertTrue(viewEditCCPageForWIP.getTableRecordByCCNameAndOwner(ccName, ownerName).isDisplayed());
        }

        click(homePage.getScoreLogo()); // to go to the home page again.
        click(homePage.getUserExtensionsTab());
        ViewEditCoreComponentPage viewEditCCPageForQA = homePage.openTotalUEsByStatesPanel()
                .clickStateProgressBar("QA");
        click(viewEditCCPageForQA.getSearchButton());

        assertEquals(0, viewEditCCPageForQA.getNumberOfOnlyCCsPerStateAreListed("WIP"));
        assertTrue(container1.numberOfQAUEGs <= viewEditCCPageForQA.getNumberOfOnlyCCsPerStateAreListed("QA"));
        assertEquals(0, viewEditCCPageForQA.getNumberOfOnlyCCsPerStateAreListed("Production"));

        //verify Core Component is listed
        for (int i = 0; i < container1.numberOfQAUEGs; i++) {
            String ccName = container1.ccQAList.get(i).getKey();
            String ownerName = container1.ccQAList.get(i).getValue();
            assertTrue(viewEditCCPageForQA.getTableRecordByCCNameAndOwner(ccName, ownerName).isDisplayed());
        }

        click(homePage.getScoreLogo()); // to go to the home page again.
        click(homePage.getUserExtensionsTab());
        ViewEditCoreComponentPage viewEditCCPageForProduction = homePage.openTotalUEsByStatesPanel()
                .clickStateProgressBar("Production");
        click(viewEditCCPageForProduction.getSearchButton());

        assertEquals(0, viewEditCCPageForProduction.getNumberOfOnlyCCsPerStateAreListed("WIP"));
        assertEquals(0, viewEditCCPageForProduction.getNumberOfOnlyCCsPerStateAreListed("QA"));
        assertTrue(container1.numberOfProductionUEGs <=
                viewEditCCPageForProduction.getNumberOfOnlyCCsPerStateAreListed("Production"));

        //verify Core Component is listed
        for (int i = 0; i < container1.numberOfProductionUEGs; i++) {
            String ccName = container1.ccProductionList.get(i).getKey();
            String ownerName = container1.ccProductionList.get(i).getValue();
            assertTrue(viewEditCCPageForProduction.getTableRecordByCCNameAndOwner(ccName, ownerName).isDisplayed());
        }

    }

    @Test
    @DisplayName("TC_28_3_3")
    public void end_user_can_see_number_of_extensions_owned_by_him_per_state_in_my_user_extensions_by_states_panel() {
        AppUserObject endUser1 = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser1);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");

        NamespaceObject endUserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser1);

        UserTopLevelASBIEPContainer container1 = new UserTopLevelASBIEPContainer(endUser1, release, endUserNamespace);

        HomePage homePage = loginPage().signIn(endUser1.getLoginId(), endUser1.getPassword());

        HomePage.MyUEsByStatesPanel myUEsByStatesPanel = homePage.openMyUEsByStatesPanel();
        click(homePage.getUserExtensionsTab());
        WebElement wipStateInTotalTab = myUEsByStatesPanel.getStateProgressBarByState("WIP");
        assertTrue(wipStateInTotalTab.isDisplayed());
        assertTrue(container1.numberOfWIPUEGs <= extractNumberFromText(getText(wipStateInTotalTab)));

        WebElement qaStateInTotalTab = myUEsByStatesPanel.getStateProgressBarByState("QA");
        assertTrue(qaStateInTotalTab.isDisplayed());
        assertTrue(container1.numberOfQAUEGs  <= extractNumberFromText(getText(qaStateInTotalTab)));

        WebElement productionStateInTotalTab = myUEsByStatesPanel.getStateProgressBarByState("Production");
        assertTrue(productionStateInTotalTab.isDisplayed());
        assertTrue(container1.numberOfProductionUEGs <= extractNumberFromText(getText(productionStateInTotalTab)));



    }

    @Test
    @DisplayName("TC_28_3_4")
    public void end_user_can_click_state_to_view_the_extensions_of_that_state_in_my_user_extensions_by_states_panel() {

        AppUserObject endUser1 = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser1);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");

        NamespaceObject endUserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser1);

        UserTopLevelASBIEPContainer container1 = new UserTopLevelASBIEPContainer(endUser1, release, endUserNamespace);

        HomePage homePage = loginPage().signIn(endUser1.getLoginId(), endUser1.getPassword());
        homePage.setBranch(release.getReleaseNumber());

        click(homePage.getUserExtensionsTab());
        ViewEditCoreComponentPage viewEditCCPageForWIP = homePage.openMyUEsByStatesPanel()
                .clickStateProgressBar("WIP");
        click(viewEditCCPageForWIP.getSearchButton());

        assertTrue(container1.numberOfWIPUEGs <= viewEditCCPageForWIP.getNumberOfOnlyCCsPerStateAreListed("WIP"));
        assertEquals(0, viewEditCCPageForWIP.getNumberOfOnlyCCsPerStateAreListed("QA"));
        assertEquals(0, viewEditCCPageForWIP.getNumberOfOnlyCCsPerStateAreListed("Production"));

        //verify Core Component is listed
        for (int i = 0; i < container1.numberOfWIPUEGs; i++) {
            String ccName = container1.ccWIPList.get(i).getKey();
            String ownerName = container1.ccWIPList.get(i).getValue();
            assertTrue(viewEditCCPageForWIP.getTableRecordByCCNameAndOwner(ccName, ownerName).isDisplayed());
        }

        click(homePage.getScoreLogo()); // to go to the home page again.
        click(homePage.getUserExtensionsTab());
        ViewEditCoreComponentPage viewEditCCPageForQA = homePage.openMyUEsByStatesPanel()
                .clickStateProgressBar("QA");
        click(viewEditCCPageForQA.getSearchButton());

        assertEquals(0, viewEditCCPageForQA.getNumberOfOnlyCCsPerStateAreListed("WIP"));
        assertTrue(container1.numberOfQAUEGs <= viewEditCCPageForQA.getNumberOfOnlyCCsPerStateAreListed("QA"));
        assertEquals(0, viewEditCCPageForQA.getNumberOfOnlyCCsPerStateAreListed("Production"));

        //verify Core Component is listed
        for (int i = 0; i < container1.numberOfQAUEGs; i++) {
            String ccName = container1.ccQAList.get(i).getKey();
            String ownerName = container1.ccQAList.get(i).getValue();
            assertTrue(viewEditCCPageForQA.getTableRecordByCCNameAndOwner(ccName, ownerName).isDisplayed());
        }

        click(homePage.getScoreLogo()); // to go to the home page again.
        click(homePage.getUserExtensionsTab());
        ViewEditCoreComponentPage viewEditCCPageForProduction = homePage.openMyUEsByStatesPanel()
                .clickStateProgressBar("Production");
        click(viewEditCCPageForProduction.getSearchButton());

        assertEquals(0, viewEditCCPageForProduction.getNumberOfOnlyCCsPerStateAreListed("WIP"));
        assertEquals(0, viewEditCCPageForProduction.getNumberOfOnlyCCsPerStateAreListed("QA"));
        assertTrue(container1.numberOfProductionUEGs <=
                viewEditCCPageForProduction.getNumberOfOnlyCCsPerStateAreListed("Production"));

        //verify Core Component is listed
        for (int i = 0; i < container1.numberOfProductionUEGs; i++) {
            String ccName = container1.ccProductionList.get(i).getKey();
            String ownerName = container1.ccProductionList.get(i).getValue();
            assertTrue(viewEditCCPageForProduction.getTableRecordByCCNameAndOwner(ccName, ownerName).isDisplayed());
        }

    }

    @Test
    @DisplayName("TC_28_3_5")
    public void end_user_can_see_number_of_extensions_per_user_and_per_state_in_user_extensions_by_users_and_states_panel() {

    }

    @Test
    @DisplayName("TC_28_3_6")
    public void end_user_can_select_user_to_narrow_down_list_and_see_only_his_extensions_per_state_in_user_extensions_by_users_and_states_panel() {

    }

    @Test
    @DisplayName("TC_28_3_7")
    public void end_user_can_click_table_cell_view_relevant_user_extensions_in_user_extensions_by_users_and_states_panel() {

    }

    @Test
    @DisplayName("TC_28_3_8")
    public void end_user_can_see_associations_of_user_extensions_that_he_owns_and_not_used_in_bies_in_my_unused_extensions_in_bies_panel() {

    }

    @Test
    @DisplayName("TC_28_3_9")
    public void end_user_can_click_user_extensions_to_view_and_edit_in_my_unused_extensions_in_bies_panel() {

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
