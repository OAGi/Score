package org.oagi.score.e2e.TS_28_HomePage;

import org.apache.commons.lang3.RandomUtils;
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
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_28_2_UserExtensionsTabForDevelopers extends BaseTest {

    private List<AppUserObject> randomAccounts = new ArrayList<>();

    private static int extractNumberFromText(String text) {
        return Integer.valueOf(text.replaceAll("\\D", ""));
    }

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_28_2_1")
    public void developer_can_see_number_of_all_extensions_per_state_in_total_user_extensions_panel() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser1 = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser1);
        AppUserObject endUser2 = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser2);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");

        NamespaceObject endUserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser1, library);

        UserTopLevelASBIEPContainer container1 = new UserTopLevelASBIEPContainer(endUser1, release, endUserNamespace);
        UserTopLevelASBIEPContainer container2 = new UserTopLevelASBIEPContainer(endUser2, release, endUserNamespace);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());

        HomePage.TotalUEsByStatesPanel totalUEsByStatesPanel = homePage.openTotalUEsByStatesPanel();
        WebElement wipStateInTotalTab = totalUEsByStatesPanel.getStateProgressBarByState("WIP");
        assertTrue(wipStateInTotalTab.isDisplayed());
        assertTrue(container1.numberOfWIPUEGs + container2.numberOfWIPUEGs
                <= extractNumberFromText(getText(wipStateInTotalTab)));

        WebElement qaStateInTotalTab = totalUEsByStatesPanel.getStateProgressBarByState("QA");
        assertTrue(qaStateInTotalTab.isDisplayed());
        assertTrue(container1.numberOfQAUEGs + container2.numberOfQAUEGs
                <= extractNumberFromText(getText(qaStateInTotalTab)));

        WebElement productionStateInTotalTab = totalUEsByStatesPanel.getStateProgressBarByState("Production");
        assertTrue(productionStateInTotalTab.isDisplayed());
        assertTrue(container1.numberOfProductionUEGs + container2.numberOfProductionUEGs
                <= extractNumberFromText(getText(productionStateInTotalTab)));
    }

    @Test
    @DisplayName("TC_28_2_2")
    public void developer_can_click_state_to_view_extensions_of_that_state_in_total_user_extensions_panel() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser1 = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser1);
        AppUserObject endUser2 = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser2);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");

        NamespaceObject endUserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser1, library);

        UserTopLevelASBIEPContainer container1 = new UserTopLevelASBIEPContainer(endUser1, release, endUserNamespace);
        UserTopLevelASBIEPContainer container2 = new UserTopLevelASBIEPContainer(endUser2, release, endUserNamespace);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        click(homePage.getBIEsTab());
        homePage.setBranch(release.getReleaseNumber());

        ViewEditCoreComponentPage viewEditCCPageForWIP = homePage.openTotalUEsByStatesPanel()
                .clickStateProgressBar("WIP");
        click(viewEditCCPageForWIP.getSearchButton());

        assertTrue(container1.numberOfWIPUEGs + container2.numberOfWIPUEGs <=
                viewEditCCPageForWIP.getNumberOfOnlyCCsPerStateAreListed("WIP"));
        assertEquals(0, viewEditCCPageForWIP.getNumberOfOnlyCCsPerStateAreListed("QA"));
        assertEquals(0, viewEditCCPageForWIP.getNumberOfOnlyCCsPerStateAreListed("Production"));

        //verify Core Component is listed
        for (int i = 0; i < container1.numberOfWIPUEGs; i++) {
            String ccName = container1.ccWIPList.get(i).getKey();
            String ownerName = container1.ccWIPList.get(i).getValue();
            assertTrue(viewEditCCPageForWIP.getTableRecordByCCNameAndOwner(ccName, ownerName).isDisplayed());
        }

        for (int i = 0; i < container2.numberOfWIPUEGs; i++) {
            String ccName = container2.ccWIPList.get(i).getKey();
            String ownerName = container2.ccWIPList.get(i).getValue();
            assertTrue(viewEditCCPageForWIP.getTableRecordByCCNameAndOwner(ccName, ownerName).isDisplayed());
        }

        click(homePage.getScoreLogo()); // to go to the home page again.
        ViewEditCoreComponentPage viewEditCCPageForQA = homePage.openTotalUEsByStatesPanel()
                .clickStateProgressBar("QA");
        click(viewEditCCPageForQA.getSearchButton());

        assertEquals(0, viewEditCCPageForQA.getNumberOfOnlyCCsPerStateAreListed("WIP"));
        assertTrue(container1.numberOfQAUEGs + container2.numberOfQAUEGs <=
                viewEditCCPageForQA.getNumberOfOnlyCCsPerStateAreListed("QA"));
        assertEquals(0, viewEditCCPageForQA.getNumberOfOnlyCCsPerStateAreListed("Production"));

        //verify Core Component is listed
        for (int i = 0; i < container1.numberOfQAUEGs; i++) {
            String ccName = container1.ccQAList.get(i).getKey();
            String ownerName = container1.ccQAList.get(i).getValue();
            assertTrue(viewEditCCPageForQA.getTableRecordByCCNameAndOwner(ccName, ownerName).isDisplayed());
        }

        for (int i = 0; i < container2.numberOfQAUEGs; i++) {
            String ccName = container2.ccQAList.get(i).getKey();
            String ownerName = container2.ccQAList.get(i).getValue();
            assertTrue(viewEditCCPageForQA.getTableRecordByCCNameAndOwner(ccName, ownerName).isDisplayed());
        }


        click(homePage.getScoreLogo()); // to go to the home page again.
        ViewEditCoreComponentPage viewEditCCPageForProduction = homePage.openTotalUEsByStatesPanel()
                .clickStateProgressBar("Production");
        click(viewEditCCPageForProduction.getSearchButton());

        assertEquals(0, viewEditCCPageForProduction.getNumberOfOnlyCCsPerStateAreListed("WIP"));
        assertEquals(0, viewEditCCPageForProduction.getNumberOfOnlyCCsPerStateAreListed("QA"));
        assertTrue(container1.numberOfProductionUEGs + container2.numberOfProductionUEGs <=
                viewEditCCPageForProduction.getNumberOfOnlyCCsPerStateAreListed("Production"));

        //verify Core Component is listed
        for (int i = 0; i < container1.numberOfProductionUEGs; i++) {
            String ccName = container1.ccProductionList.get(i).getKey();
            String ownerName = container1.ccProductionList.get(i).getValue();
            assertTrue(viewEditCCPageForProduction.getTableRecordByCCNameAndOwner(ccName, ownerName).isDisplayed());
        }

        for (int i = 0; i < container2.numberOfProductionUEGs; i++) {
            String ccName = container2.ccProductionList.get(i).getKey();
            String ownerName = container2.ccProductionList.get(i).getValue();
            assertTrue(viewEditCCPageForProduction.getTableRecordByCCNameAndOwner(ccName, ownerName).isDisplayed());
        }

    }

    @Test
    @DisplayName("TC_28_2_3")
    public void developer_can_see_number_of_extensions_per_user_and_per_state_in_user_extensions_panel() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser1 = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser1);
        AppUserObject endUser2 = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser2);
        String branch = "10.8.4";

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);

        NamespaceObject endUserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser1, library);

        UserTopLevelASBIEPContainer container1 = new UserTopLevelASBIEPContainer(endUser1, release, endUserNamespace);
        UserTopLevelASBIEPContainer container2 = new UserTopLevelASBIEPContainer(endUser2, release, endUserNamespace);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        click(homePage.getBIEsTab());
        homePage.setBranch(branch);

        HomePage.UEsByUsersAndStatesPanel uesByUsersAndStatesPanel = homePage.openUEsByUsersAndStatesPanel();
        // both endUsers are displayed
        uesByUsersAndStatesPanel.setUsername(endUser1.getLoginId());
        assertTrue(uesByUsersAndStatesPanel.getTableRecordByValue(endUser1.getLoginId()).isDisplayed());

        uesByUsersAndStatesPanel.setUsername(endUser1.getLoginId()); // to turn off the checkbox
        uesByUsersAndStatesPanel.setUsername(endUser2.getLoginId());
        assertTrue(uesByUsersAndStatesPanel.getTableRecordByValue(endUser2.getLoginId()).isDisplayed());

        uesByUsersAndStatesPanel.setUsername(endUser2.getLoginId()); // to turn off the checkbox
        uesByUsersAndStatesPanel.setUsername(endUser1.getLoginId());
        WebElement tr = uesByUsersAndStatesPanel.getTableRecordByValue(endUser1.getLoginId());
        WebElement td_WIP = uesByUsersAndStatesPanel.getColumnByName(tr, "WIP");
        WebElement td_QA = uesByUsersAndStatesPanel.getColumnByName(tr, "QA");
        WebElement td_Production = uesByUsersAndStatesPanel.getColumnByName(tr, "Production");
        WebElement td_Total = uesByUsersAndStatesPanel.getColumnByName(tr, "total");

        assertEquals(container1.numberOfWIPUEGs, Integer.valueOf(getText(td_WIP)));
        assertEquals(container1.numberOfQAUEGs, Integer.valueOf(getText(td_QA)));
        assertEquals(container1.numberOfProductionUEGs, Integer.valueOf(getText(td_Production)));
        assertEquals(container1.numberOfWIPUEGs +
                        container1.numberOfQAUEGs +
                        container1.numberOfProductionUEGs,
                Integer.valueOf(getText(td_Total)));

        uesByUsersAndStatesPanel.setUsername(endUser1.getLoginId()); // to turn off the checkbox
        uesByUsersAndStatesPanel.setUsername(endUser2.getLoginId());
        WebElement tr2 = uesByUsersAndStatesPanel.getTableRecordByValue(endUser2.getLoginId());
        WebElement td2_WIP = uesByUsersAndStatesPanel.getColumnByName(tr2, "WIP");
        WebElement td2_QA = uesByUsersAndStatesPanel.getColumnByName(tr2, "QA");
        WebElement td2_Production = uesByUsersAndStatesPanel.getColumnByName(tr2, "Production");
        WebElement td2_Total = uesByUsersAndStatesPanel.getColumnByName(tr2, "total");

        assertEquals(container2.numberOfWIPUEGs, Integer.valueOf(getText(td2_WIP)));
        assertEquals(container2.numberOfQAUEGs, Integer.valueOf(getText(td2_QA)));
        assertEquals(container2.numberOfProductionUEGs, Integer.valueOf(getText(td2_Production)));
        assertEquals(container2.numberOfWIPUEGs +
                        container2.numberOfQAUEGs +
                        container2.numberOfProductionUEGs,
                Integer.valueOf(getText(td2_Total)));
    }

    @Test
    @DisplayName("TC_28_2_4")
    public void developer_can_select_user_to_narrow_down_list_and_see_only_his_extensions_in_user_extensions_panel() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser1 = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser1);
        AppUserObject endUser2 = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser2);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");

        NamespaceObject endUserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser1, library);

        UserTopLevelASBIEPContainer container1 = new UserTopLevelASBIEPContainer(endUser1, release, endUserNamespace);
        UserTopLevelASBIEPContainer container2 = new UserTopLevelASBIEPContainer(endUser2, release, endUserNamespace);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        click(homePage.getBIEsTab());
        homePage.setBranch(release.getReleaseNumber());

        HomePage.UEsByUsersAndStatesPanel uesByUsersAndStatesPanel = homePage.openUEsByUsersAndStatesPanel();
        uesByUsersAndStatesPanel.setUsername(endUser1.getLoginId()); //select endUser1
        assertThrows(TimeoutException.class, () ->
                uesByUsersAndStatesPanel.getTableRecordByValue(endUser2.getLoginId())); //endUser2 not displayed

        WebElement tr = uesByUsersAndStatesPanel.getTableRecordByValue(endUser1.getLoginId());
        WebElement td_WIP = uesByUsersAndStatesPanel.getColumnByName(tr, "WIP");
        WebElement td_QA = uesByUsersAndStatesPanel.getColumnByName(tr, "QA");
        WebElement td_Production = uesByUsersAndStatesPanel.getColumnByName(tr, "Production");
        WebElement td_Total = uesByUsersAndStatesPanel.getColumnByName(tr, "total");

        assertEquals(container1.numberOfWIPUEGs, Integer.valueOf(getText(td_WIP)));
        assertEquals(container1.numberOfQAUEGs, Integer.valueOf(getText(td_QA)));
        assertEquals(container1.numberOfProductionUEGs, Integer.valueOf(getText(td_Production)));
        assertEquals(container1.numberOfWIPUEGs +
                        container1.numberOfQAUEGs +
                        container1.numberOfProductionUEGs,
                Integer.valueOf(getText(td_Total)));

        uesByUsersAndStatesPanel.setUsername(endUser1.getLoginId()); //un-select endUser1
        uesByUsersAndStatesPanel.setUsername(endUser2.getLoginId()); //select endUser2
        assertThrows(TimeoutException.class, () ->
                uesByUsersAndStatesPanel.getTableRecordByValue(endUser1.getLoginId())); //endUser1 not displayed

        WebElement tr2 = uesByUsersAndStatesPanel.getTableRecordByValue(endUser2.getLoginId());
        WebElement td2_WIP = uesByUsersAndStatesPanel.getColumnByName(tr2, "WIP");
        WebElement td2_QA = uesByUsersAndStatesPanel.getColumnByName(tr2, "QA");
        WebElement td2_Production = uesByUsersAndStatesPanel.getColumnByName(tr2, "Production");
        WebElement td2_Total = uesByUsersAndStatesPanel.getColumnByName(tr2, "total");

        assertEquals(container2.numberOfWIPUEGs, Integer.valueOf(getText(td2_WIP)));
        assertEquals(container2.numberOfQAUEGs, Integer.valueOf(getText(td2_QA)));
        assertEquals(container2.numberOfProductionUEGs, Integer.valueOf(getText(td2_Production)));
        assertEquals(container2.numberOfWIPUEGs +
                        container2.numberOfQAUEGs +
                        container2.numberOfProductionUEGs,
                Integer.valueOf(getText(td2_Total)));
    }

    @Test
    @DisplayName("TC_28_2_5")
    public void developer_can_click_table_cell_to_view_relevant_user_extensions_in_user_extensions_panel() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser1 = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser1);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");

        NamespaceObject endUserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser1, library);

        UserTopLevelASBIEPContainer container1 = new UserTopLevelASBIEPContainer(endUser1, release, endUserNamespace);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());

        HomePage.UEsByUsersAndStatesPanel uesByUsersAndStatesPanel = homePage.openUEsByUsersAndStatesPanel();
        homePage.setBranch(release.getReleaseNumber());
        uesByUsersAndStatesPanel.setUsername(endUser1.getLoginId()); //select endUser1
        WebElement tr = uesByUsersAndStatesPanel.getTableRecordByValue(endUser1.getLoginId());
        WebElement td_Total = uesByUsersAndStatesPanel.getColumnByName(tr, "total");
        assertEquals(container1.numberOfWIPUEGs +
                        container1.numberOfQAUEGs +
                        container1.numberOfProductionUEGs,
                Integer.valueOf(getText(td_Total)));

        ViewEditCoreComponentPage viewEditCCPageForWIP = uesByUsersAndStatesPanel.openViewEditCCPageByUsernameAndColumnName(
                endUser1.getLoginId(), "WIP");
        assertEquals(container1.numberOfWIPUEGs, viewEditCCPageForWIP.getNumberOfOnlyCCsPerStateAreListed("WIP"));
        assertEquals(0, viewEditCCPageForWIP.getNumberOfOnlyCCsPerStateAreListed("QA"));
        assertEquals(0, viewEditCCPageForWIP.getNumberOfOnlyCCsPerStateAreListed("Production"));

        click(homePage.getScoreLogo()); // to go to the home page again.
        uesByUsersAndStatesPanel = homePage.openUEsByUsersAndStatesPanel();
        uesByUsersAndStatesPanel.setUsername(endUser1.getLoginId()); //select endUser1
        ViewEditCoreComponentPage viewEditCCPageForQA = uesByUsersAndStatesPanel.openViewEditCCPageByUsernameAndColumnName(
                endUser1.getLoginId(), "QA");
        assertEquals(0, viewEditCCPageForQA.getNumberOfOnlyCCsPerStateAreListed("WIP"));
        assertEquals(container1.numberOfQAUEGs, viewEditCCPageForQA.getNumberOfOnlyCCsPerStateAreListed("QA"));
        assertEquals(0, viewEditCCPageForQA.getNumberOfOnlyCCsPerStateAreListed("Production"));

        click(homePage.getScoreLogo()); // to go to the home page again.
        uesByUsersAndStatesPanel = homePage.openUEsByUsersAndStatesPanel();
        uesByUsersAndStatesPanel.setUsername(endUser1.getLoginId()); //select endUser1
        ViewEditCoreComponentPage viewEditCCPageForProduction = uesByUsersAndStatesPanel.openViewEditCCPageByUsernameAndColumnName(
                endUser1.getLoginId(), "Production");
        assertEquals(0, viewEditCCPageForProduction.getNumberOfOnlyCCsPerStateAreListed("WIP"));
        assertEquals(0, viewEditCCPageForProduction.getNumberOfOnlyCCsPerStateAreListed("QA"));
        assertEquals(container1.numberOfProductionUEGs, viewEditCCPageForProduction.getNumberOfOnlyCCsPerStateAreListed("Production"));

        click(homePage.getScoreLogo()); // to go to the home page again.
        uesByUsersAndStatesPanel = homePage.openUEsByUsersAndStatesPanel();
        uesByUsersAndStatesPanel.setUsername(endUser1.getLoginId()); //select endUser1
        ViewEditCoreComponentPage viewEditCCPageForTotal = uesByUsersAndStatesPanel.openViewEditCCPageByUsernameAndColumnName(
                endUser1.getLoginId(), "total");
        // The total number of randomly-generated CCs could be more than the default size of items, 10.
        // Thus, it should set 'Items per page' to more than 10 to count the total number of CCs.
        viewEditCCPageForTotal.setItemsPerPage(50);
        assertEquals(container1.numberOfWIPUEGs, viewEditCCPageForTotal.getNumberOfOnlyCCsPerStateAreListed("WIP"));
        assertEquals(container1.numberOfQAUEGs, viewEditCCPageForTotal.getNumberOfOnlyCCsPerStateAreListed("QA"));
        assertEquals(container1.numberOfProductionUEGs, viewEditCCPageForTotal.getNumberOfOnlyCCsPerStateAreListed("Production"));
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random accounts
        this.randomAccounts.forEach(randomAccount -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId());
        });
    }

    private class UserTopLevelASBIEPContainer {

        int numberOfWIPUEGs;
        int numberOfQAUEGs;
        int numberOfProductionUEGs;
        private AppUserObject appUser;
        private NamespaceObject userNamespace;
        private List<Pair<String, String>> ccWIPList = new ArrayList<Pair<String, String>>();
        private List<Pair<String, String>> ccQAList = new ArrayList<Pair<String, String>>();
        private List<Pair<String, String>> ccProductionList = new ArrayList<Pair<String, String>>();

        public UserTopLevelASBIEPContainer(AppUserObject appUser, ReleaseObject release, NamespaceObject namespace) {
            this(appUser, release, namespace, RandomUtils.secure().randomInt(1, 3), RandomUtils.secure().randomInt(1, 3), RandomUtils.secure().randomInt(1, 3));
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

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryById(release.getLibraryId());

            HomePage homePage = loginPage().signIn(this.appUser.getLoginId(), this.appUser.getPassword());
            for (int i = 0; i < numberOfWIPUEGs; ++i) {
                ASCCPObject asccp;
                {
                    CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
                    NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

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
                    NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

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
                    NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

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

}
