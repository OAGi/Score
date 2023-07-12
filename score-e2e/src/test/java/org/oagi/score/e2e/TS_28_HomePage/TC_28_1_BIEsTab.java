package org.oagi.score.e2e.TS_28_HomePage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.openqa.selenium.WebElement;

import java.util.*;

import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_28_1_BIEsTab extends BaseTest {

    private List<AppUserObject> randomAccounts = new ArrayList<>();

    private static int extractNumberFromText(String text) {
        return Integer.valueOf(text.replaceAll("\\D", ""));
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_28_1_1")
    public void developer_can_see_number_of_all_bies_per_state_in_total_bies_by_state_panel() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");

        UserTopLevelASBIEPContainer container = new UserTopLevelASBIEPContainer(developer, release);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());

        HomePage.TotalBIEsByStatesPanel totalBIEsByStatesPanel = homePage.openTotalBIEsByStatesPanel();
        WebElement wipStateInTotalTab = totalBIEsByStatesPanel.getStateProgressBarByState("WIP");
        assertTrue(wipStateInTotalTab.isDisplayed());
        assertTrue(container.numberOfWIPBIEs <= extractNumberFromText(getText(wipStateInTotalTab)));

        WebElement qaStateInTotalTab = totalBIEsByStatesPanel.getStateProgressBarByState("QA");
        assertTrue(qaStateInTotalTab.isDisplayed());
        assertTrue(container.numberOfQABIEs <= extractNumberFromText(getText(qaStateInTotalTab)));

        WebElement productionStateInTotalTab = totalBIEsByStatesPanel.getStateProgressBarByState("Production");
        assertTrue(productionStateInTotalTab.isDisplayed());
        assertTrue(container.numberOfProductionBIEs <= extractNumberFromText(getText(productionStateInTotalTab)));
    }

    private TopLevelASBIEPObject createTopLevelASBIEPByDEN(AppUserObject creator, BusinessContextObject businessContext,
                                                           String den, ReleaseObject release, String state) {
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(den, release.getReleaseNumber());
        return getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(businessContext), asccp, creator, state);
    }

    @Test
    @DisplayName("TC_28_1_2")
    public void developer_can_click_on_state_to_view_bies_of_that_state_in_total_bies_by_state_panel() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");

        UserTopLevelASBIEPContainer container = new UserTopLevelASBIEPContainer(developer, release);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        click(homePage.getBIEsTab());
        homePage.setBranch(release.getReleaseNumber());

        HomePage.TotalBIEsByStatesPanel totalBIEsByStatesPanel = homePage.openTotalBIEsByStatesPanel();

        ViewEditBIEPage viewEditBIEPageForWIP = totalBIEsByStatesPanel.clickStateProgressBar("WIP");
        viewEditBIEPageForWIP.setOwner(developer.getLoginId());
        viewEditBIEPageForWIP.hitSearchButton();

        assertEquals(container.numberOfWIPBIEs, viewEditBIEPageForWIP.getNumberOfOnlyBIEsPerStateAreListed("WIP"));
        assertEquals(0, viewEditBIEPageForWIP.getNumberOfOnlyBIEsPerStateAreListed("QA"));
        assertEquals(0, viewEditBIEPageForWIP.getNumberOfOnlyBIEsPerStateAreListed("Production"));

        click(homePage.getScoreLogo()); // to go to the home page again.
        click(homePage.getBIEsTab());
        ViewEditBIEPage viewEditBIEPageForQA = totalBIEsByStatesPanel.clickStateProgressBar("QA");
        viewEditBIEPageForQA.setOwner(developer.getLoginId());
        viewEditBIEPageForQA.hitSearchButton();

        assertEquals(0, viewEditBIEPageForQA.getNumberOfOnlyBIEsPerStateAreListed("WIP"));
        assertEquals(container.numberOfQABIEs, viewEditBIEPageForQA.getNumberOfOnlyBIEsPerStateAreListed("QA"));
        assertEquals(0, viewEditBIEPageForQA.getNumberOfOnlyBIEsPerStateAreListed("Production"));

        click(homePage.getScoreLogo()); // to go to the home page again.
        click(homePage.getBIEsTab());
        ViewEditBIEPage viewEditBIEPageForProduction = totalBIEsByStatesPanel.clickStateProgressBar("Production");
        viewEditBIEPageForProduction.setOwner(developer.getLoginId());
        viewEditBIEPageForProduction.hitSearchButton();

        assertEquals(0, viewEditBIEPageForProduction.getNumberOfOnlyBIEsPerStateAreListed("WIP"));
        assertEquals(0, viewEditBIEPageForProduction.getNumberOfOnlyBIEsPerStateAreListed("QA"));
        assertEquals(container.numberOfProductionBIEs, viewEditBIEPageForProduction.getNumberOfOnlyBIEsPerStateAreListed("Production"));
    }

    @Test
    @DisplayName("TC_28_1_3")
    public void developer_can_see_number_of_bies_owned_by_him_in_my_bies_by_state_panel() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");

        UserTopLevelASBIEPContainer container = new UserTopLevelASBIEPContainer(developer, release);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        click(homePage.getBIEsTab());
        homePage.setBranch(release.getReleaseNumber());

        HomePage.MyBIEsByStatesPanel myBIEsByStatesPanel = homePage.openMyBIEsByStatesPanel();
        WebElement wipStateInMyTab = myBIEsByStatesPanel.getStateProgressBarByState("WIP");
        assertTrue(wipStateInMyTab.isDisplayed());
        assertTrue(container.numberOfWIPBIEs <= extractNumberFromText(getText(wipStateInMyTab)));

        WebElement qaStateInMyTab = myBIEsByStatesPanel.getStateProgressBarByState("QA");
        assertTrue(qaStateInMyTab.isDisplayed());
        assertTrue(container.numberOfQABIEs <= extractNumberFromText(getText(qaStateInMyTab)));

        WebElement productionStateInMyTab = myBIEsByStatesPanel.getStateProgressBarByState("Production");
        assertTrue(productionStateInMyTab.isDisplayed());
        assertTrue(container.numberOfProductionBIEs <= extractNumberFromText(getText(productionStateInMyTab)));
    }

    @Test
    @DisplayName("TC_28_1_4")
    public void developer_can_click_on_state_to_view_bies_of_that_state_and_owned_by_him_in_bie_list_page() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");

        UserTopLevelASBIEPContainer container = new UserTopLevelASBIEPContainer(developer, release);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        click(homePage.getBIEsTab());
        homePage.setBranch(release.getReleaseNumber());

        HomePage.MyBIEsByStatesPanel myBIEsByStatesPanel = homePage.openMyBIEsByStatesPanel();

        ViewEditBIEPage viewEditBIEPageForWIP = myBIEsByStatesPanel.clickStateProgressBar("WIP");

        assertEquals(container.numberOfWIPBIEs, viewEditBIEPageForWIP.getNumberOfOnlyBIEsPerStateAreListed("WIP"));
        assertEquals(0, viewEditBIEPageForWIP.getNumberOfOnlyBIEsPerStateAreListed("QA"));
        assertEquals(0, viewEditBIEPageForWIP.getNumberOfOnlyBIEsPerStateAreListed("Production"));

        click(homePage.getScoreLogo()); // to go to the home page again.
        click(homePage.getBIEsTab());
        ViewEditBIEPage viewEditBIEPageForQA = myBIEsByStatesPanel.clickStateProgressBar("QA");

        assertEquals(0, viewEditBIEPageForQA.getNumberOfOnlyBIEsPerStateAreListed("WIP"));
        assertEquals(container.numberOfQABIEs, viewEditBIEPageForQA.getNumberOfOnlyBIEsPerStateAreListed("QA"));
        assertEquals(0, viewEditBIEPageForQA.getNumberOfOnlyBIEsPerStateAreListed("Production"));

        click(homePage.getScoreLogo()); // to go to the home page again.
        click(homePage.getBIEsTab());
        ViewEditBIEPage viewEditBIEPageForProduction = myBIEsByStatesPanel.clickStateProgressBar("Production");

        assertEquals(0, viewEditBIEPageForProduction.getNumberOfOnlyBIEsPerStateAreListed("WIP"));
        assertEquals(0, viewEditBIEPageForProduction.getNumberOfOnlyBIEsPerStateAreListed("QA"));
        assertEquals(container.numberOfProductionBIEs, viewEditBIEPageForProduction.getNumberOfOnlyBIEsPerStateAreListed("Production"));
    }

    @Test
    @DisplayName("TC_28_1_5")
    public void developer_can_see_number_of_bies_per_user_and_per_state_in_bies_by_users_and_states_panel() {
        List<AppUserObject> developers = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            developers.add(developer);
        }

        Map<String, UserTopLevelASBIEPContainer> containersFor10_8_3 = new HashMap<>();
        Map<String, UserTopLevelASBIEPContainer> containersFor10_8_4 = new HashMap<>();
        for (AppUserObject developer : developers) {
            containersFor10_8_3.put(developer.getLoginId(), new UserTopLevelASBIEPContainer(developer,
                    getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3")
            ));
            containersFor10_8_4.put(developer.getLoginId(), new UserTopLevelASBIEPContainer(developer,
                    getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4")
            ));
        }

        AppUserObject developer = getAPIFactory().getAppUserAPI().getAppUserByLoginID("oagis");
        developer.setPassword("oagis");
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        HomePage.BIEsByUsersAndStatesPanel biesByUsersAndStatesPanel = homePage.openBIEsByUsersAndStatesPanel();

        homePage.setBranch("All");
        for (AppUserObject appUser : developers) {
            WebElement tr = biesByUsersAndStatesPanel.getTableRecordByValue(appUser.getLoginId());
            WebElement td_WIP = biesByUsersAndStatesPanel.getColumnByName(tr, "WIP");
            WebElement td_QA = biesByUsersAndStatesPanel.getColumnByName(tr, "QA");
            WebElement td_Production = biesByUsersAndStatesPanel.getColumnByName(tr, "Production");
            WebElement td_Total = biesByUsersAndStatesPanel.getColumnByName(tr, "total");

            assertTrue(containersFor10_8_3.get(appUser.getLoginId()).numberOfWIPBIEs +
                    containersFor10_8_4.get(appUser.getLoginId()).numberOfWIPBIEs <= Integer.valueOf(getText(td_WIP)));
            assertTrue(containersFor10_8_3.get(appUser.getLoginId()).numberOfQABIEs +
                    containersFor10_8_4.get(appUser.getLoginId()).numberOfQABIEs <= Integer.valueOf(getText(td_QA)));
            assertTrue(containersFor10_8_3.get(appUser.getLoginId()).numberOfProductionBIEs +
                    containersFor10_8_4.get(appUser.getLoginId()).numberOfProductionBIEs <= Integer.valueOf(getText(td_Production)));
            assertTrue(containersFor10_8_3.get(appUser.getLoginId()).numberOfWIPBIEs +
                    containersFor10_8_3.get(appUser.getLoginId()).numberOfQABIEs +
                    containersFor10_8_3.get(appUser.getLoginId()).numberOfProductionBIEs +
                    containersFor10_8_4.get(appUser.getLoginId()).numberOfWIPBIEs +
                    containersFor10_8_4.get(appUser.getLoginId()).numberOfQABIEs +
                    containersFor10_8_4.get(appUser.getLoginId()).numberOfProductionBIEs <= Integer.valueOf(getText(td_Total)));
        }

        homePage.setBranch("10.8.3");
        for (UserTopLevelASBIEPContainer container : containersFor10_8_3.values()) {
            biesByUsersAndStatesPanel.setUsername(container.appUser.getLoginId());
            WebElement tr = biesByUsersAndStatesPanel.getTableRecordByValue(container.appUser.getLoginId());
            WebElement td_WIP = biesByUsersAndStatesPanel.getColumnByName(tr, "WIP");
            WebElement td_QA = biesByUsersAndStatesPanel.getColumnByName(tr, "QA");
            WebElement td_Production = biesByUsersAndStatesPanel.getColumnByName(tr, "Production");
            WebElement td_Total = biesByUsersAndStatesPanel.getColumnByName(tr, "total");

            assertTrue(container.numberOfWIPBIEs <= Integer.valueOf(getText(td_WIP)));
            assertTrue(container.numberOfQABIEs <= Integer.valueOf(getText(td_QA)));
            assertTrue(container.numberOfProductionBIEs <= Integer.valueOf(getText(td_Production)));
            assertTrue(container.numberOfWIPBIEs +
                    container.numberOfQABIEs +
                    container.numberOfProductionBIEs <=
                    Integer.valueOf(getText(td_Total)));
        }

        homePage.setBranch("10.8.4");
        for (UserTopLevelASBIEPContainer container : containersFor10_8_4.values()) {
            WebElement tr = biesByUsersAndStatesPanel.getTableRecordByValue(container.appUser.getLoginId());
            WebElement td_WIP = biesByUsersAndStatesPanel.getColumnByName(tr, "WIP");
            WebElement td_QA = biesByUsersAndStatesPanel.getColumnByName(tr, "QA");
            WebElement td_Production = biesByUsersAndStatesPanel.getColumnByName(tr, "Production");
            WebElement td_Total = biesByUsersAndStatesPanel.getColumnByName(tr, "total");

            assertTrue(container.numberOfWIPBIEs <= Integer.valueOf(getText(td_WIP)));
            assertTrue(container.numberOfQABIEs <= Integer.valueOf(getText(td_QA)));
            assertTrue(container.numberOfProductionBIEs <= Integer.valueOf(getText(td_Production)));
            assertTrue(container.numberOfWIPBIEs +
                    container.numberOfQABIEs +
                    container.numberOfProductionBIEs <=
                    Integer.valueOf(getText(td_Total)));
        }
    }

    @Test
    @DisplayName("TC_28_1_6")
    public void developer_can_select_user_to_narrow_down_the_list_and_see_only_number_of_his_bies_per_state() {
        List<AppUserObject> developers = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            developers.add(developer);
        }

        Map<String, UserTopLevelASBIEPContainer> containersFor10_8_4 = new HashMap<>();
        for (AppUserObject developer : developers) {
            containersFor10_8_4.put(developer.getLoginId(), new UserTopLevelASBIEPContainer(developer,
                    getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4")
            ));
        }

        AppUserObject developer = getAPIFactory().getAppUserAPI().getAppUserByLoginID("oagis");
        developer.setPassword("oagis");
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());

        HomePage.BIEsByUsersAndStatesPanel biesByUsersAndStatesPanel = homePage.openBIEsByUsersAndStatesPanel();
        homePage.setBranch("10.8.4");
        for (AppUserObject devUser : developers) {
            UserTopLevelASBIEPContainer container = containersFor10_8_4.get(devUser.getLoginId());
            biesByUsersAndStatesPanel.setUsername(devUser.getLoginId());

            assertEquals(1, biesByUsersAndStatesPanel.getTableRecords().size());

            WebElement tr = biesByUsersAndStatesPanel.getTableRecordByValue(container.appUser.getLoginId());
            WebElement td_WIP = biesByUsersAndStatesPanel.getColumnByName(tr, "WIP");
            WebElement td_QA = biesByUsersAndStatesPanel.getColumnByName(tr, "QA");
            WebElement td_Production = biesByUsersAndStatesPanel.getColumnByName(tr, "Production");
            WebElement td_Total = biesByUsersAndStatesPanel.getColumnByName(tr, "total");

            assertEquals(container.numberOfWIPBIEs, Integer.valueOf(getText(td_WIP)));
            assertEquals(container.numberOfQABIEs, Integer.valueOf(getText(td_QA)));
            assertEquals(container.numberOfProductionBIEs, Integer.valueOf(getText(td_Production)));
            assertEquals(container.numberOfWIPBIEs +
                            container.numberOfQABIEs +
                            container.numberOfProductionBIEs,
                    Integer.valueOf(getText(td_Total)));

            // Set the username again to uncheck it.
            biesByUsersAndStatesPanel.setUsername(devUser.getLoginId());
        }
    }

    @Test
    @DisplayName("TC_28_1_7")
    public void developer_can_click_on_table_cell_to_view_relevant_bies_in_bie_list_page() {
        List<AppUserObject> developers = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            developers.add(developer);
        }

        String releaseNumber = "10.8.4";

        Map<String, UserTopLevelASBIEPContainer> containersFor10_8_4 = new HashMap<>();
        for (AppUserObject developer : developers) {
            containersFor10_8_4.put(developer.getLoginId(), new UserTopLevelASBIEPContainer(developer,
                    getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(releaseNumber)
            ));
        }

        AppUserObject developer = getAPIFactory().getAppUserAPI().getAppUserByLoginID("oagis");
        developer.setPassword("oagis");
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());

        HomePage.BIEsByUsersAndStatesPanel biesByUsersAndStatesPanel = homePage.openBIEsByUsersAndStatesPanel();
        homePage.setBranch(releaseNumber);
        for (AppUserObject devUser : developers) {
            UserTopLevelASBIEPContainer container = containersFor10_8_4.get(devUser.getLoginId());

            WebElement tr = biesByUsersAndStatesPanel.getTableRecordByValue(container.appUser.getLoginId());
            WebElement td_Total = biesByUsersAndStatesPanel.getColumnByName(tr, "total");
            assertTrue(container.numberOfWIPBIEs +
                    container.numberOfQABIEs +
                    container.numberOfProductionBIEs <=
                    Integer.valueOf(getText(td_Total)));

            ViewEditBIEPage viewEditBIEPageByUserAndWIP = biesByUsersAndStatesPanel.openViewEditBIEPageByUsernameAndColumnName(
                    devUser.getLoginId(), "WIP");
            assertTrue(container.numberOfWIPBIEs <= viewEditBIEPageByUserAndWIP.getNumberOfOnlyBIEsPerStateAreListed("WIP"));
            assertEquals(0, viewEditBIEPageByUserAndWIP.getNumberOfOnlyBIEsPerStateAreListed("QA"));
            assertEquals(0, viewEditBIEPageByUserAndWIP.getNumberOfOnlyBIEsPerStateAreListed("Production"));

            click(homePage.getScoreLogo()); // to go to the home page again.
            click(homePage.getBIEsTab());
            ViewEditBIEPage viewEditBIEPageByUserAndQA = biesByUsersAndStatesPanel.openViewEditBIEPageByUsernameAndColumnName(
                    devUser.getLoginId(), "QA");
            assertEquals(0, viewEditBIEPageByUserAndQA.getNumberOfOnlyBIEsPerStateAreListed("WIP"));
            assertTrue(container.numberOfQABIEs <= viewEditBIEPageByUserAndQA.getNumberOfOnlyBIEsPerStateAreListed("QA"));
            assertEquals(0, viewEditBIEPageByUserAndQA.getNumberOfOnlyBIEsPerStateAreListed("Production"));

            click(homePage.getScoreLogo()); // to go to the home page again.
            click(homePage.getBIEsTab());
            ViewEditBIEPage viewEditBIEPageByUserAndProduction = biesByUsersAndStatesPanel.openViewEditBIEPageByUsernameAndColumnName(
                    devUser.getLoginId(), "Production");
            assertEquals(0, viewEditBIEPageByUserAndProduction.getNumberOfOnlyBIEsPerStateAreListed("WIP"));
            assertEquals(0, viewEditBIEPageByUserAndProduction.getNumberOfOnlyBIEsPerStateAreListed("QA"));
            assertTrue(container.numberOfProductionBIEs <= viewEditBIEPageByUserAndProduction.getNumberOfOnlyBIEsPerStateAreListed("Production"));

            click(homePage.getScoreLogo()); // to go to the home page again.
            click(homePage.getBIEsTab());
            ViewEditBIEPage viewEditBIEPageByUserAndTotal = biesByUsersAndStatesPanel.openViewEditBIEPageByUsernameAndColumnName(
                    devUser.getLoginId(), "total");
            // The total number of randomly-generated BIEs could be more than the default size of items, 10.
            // Thus, it should set 'Items per page' to more than 10 to count the total number of BIEs.
            viewEditBIEPageByUserAndTotal.setItemsPerPage(50);
            assertTrue(container.numberOfWIPBIEs <= viewEditBIEPageByUserAndTotal.getNumberOfOnlyBIEsPerStateAreListed("WIP"));
            assertTrue(container.numberOfQABIEs <= viewEditBIEPageByUserAndTotal.getNumberOfOnlyBIEsPerStateAreListed("QA"));
            assertTrue(container.numberOfProductionBIEs <= viewEditBIEPageByUserAndTotal.getNumberOfOnlyBIEsPerStateAreListed("Production"));

            click(homePage.getScoreLogo()); // to go to the home page again.
            click(homePage.getBIEsTab());
        }
    }

    @Test
    @DisplayName("TC_28_1_8")
    public void developer_can_see_last_5_bies_that_he_modified_or_created_in_my_recent_bies_panel() {
        List<AppUserObject> developers = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            developers.add(developer);
        }

        String releaseNumber = "10.8.4";

        Map<String, UserTopLevelASBIEPContainer> containersFor10_8_4 = new HashMap<>();
        for (AppUserObject developer : developers) {
            containersFor10_8_4.put(developer.getLoginId(), new UserTopLevelASBIEPContainer(developer,
                    getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(releaseNumber),
                    2, 2, 1
            ));
        }

        for (AppUserObject devUser : developers) {
            HomePage homePage = loginPage().signIn(devUser.getLoginId(), devUser.getPassword());
            HomePage.BIEsByUsersAndStatesPanel biesByUsersAndStatesPanel = homePage.openBIEsByUsersAndStatesPanel();
            HomePage.MyRecentBIEsPanel myRecentBIEsPanel = homePage.openMyRecentBIEsPanel();
            homePage.setBranch(releaseNumber);

            UserTopLevelASBIEPContainer container = containersFor10_8_4.get(devUser.getLoginId());
            int recentSize = container.recentBIEs.size();
            for (int i = 0; i < Math.min(recentSize, 5); i++) {
                String bieName = container.recentBIEs.get(i);
                assertTrue(myRecentBIEsPanel.getTableRecordByValue(bieName).isDisplayed());
            }

            // Set the username again to uncheck it.
            biesByUsersAndStatesPanel.setUsername(devUser.getLoginId());
            homePage.logout();
        }
    }

    @Test
    @DisplayName("TC_28_1_9")
    public void developer_can_click_on_bie_to_view_it_in_edit_bie_page_in_my_recent_bies_panel() {
        List<AppUserObject> developers = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            developers.add(developer);
        }

        String releaseNumber = "10.8.4";

        Map<String, UserTopLevelASBIEPContainer> containersFor10_8_4 = new HashMap<>();
        for (AppUserObject developer : developers) {
            containersFor10_8_4.put(developer.getLoginId(), new UserTopLevelASBIEPContainer(developer,
                    getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(releaseNumber),
                    2, 2, 1
            ));
        }

        for (AppUserObject devUser : developers) {
            HomePage homePage = loginPage().signIn(devUser.getLoginId(), devUser.getPassword());
            HomePage.MyRecentBIEsPanel myRecentBIEsPanel = homePage.openMyRecentBIEsPanel();
            homePage.setBranch(releaseNumber);

            UserTopLevelASBIEPContainer container = containersFor10_8_4.get(devUser.getLoginId());
            int recentSize = container.recentBIEs.size();
            for (int i = 0; i < Math.min(recentSize, 5); i++) {
                String bieName = container.recentBIEs.get(i);
                EditBIEPage editBIEPage = myRecentBIEsPanel.openEditBIEPageByDEN(bieName);
                assertTrue(bieName.startsWith(getText(editBIEPage.getTitle())));
                assertEquals(devUser.getLoginId(), getText(editBIEPage.getTopLevelASBIEPPanel().getOwnerField()));

                // to go to homepage
                click(homePage.getScoreLogo());
                click(homePage.getBIEsTab());
            }

            homePage.logout();
        }
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
        static List<String> SAMPLED_ASCCP_DEN_LIST = Arrays.asList(
                "Coordinate Reference. Sequenced Identifiers",
                "Account Identifiers. Named Identifiers",
                "Customer Item Identification. Item Identification",
                "Change Product Availability. Change Product Availability",
                "Collaboration Message. Collaboration Message",
                "Production Data. Production Data");
        int numberOfWIPBIEs;
        int numberOfQABIEs;
        int numberOfProductionBIEs;
        private AppUserObject appUser;
        private int yieldPointer = 0;
        private List<String> recentBIEs = new ArrayList<>();

        public UserTopLevelASBIEPContainer(AppUserObject appUser, ReleaseObject release) {
            this(appUser, release, nextInt(2, 5), nextInt(2, 5), nextInt(2, 5));
        }

        public UserTopLevelASBIEPContainer(AppUserObject appUser, ReleaseObject release,
                                           int numberOfWIPBIEs, int numberOfQABIEs, int numberOfProductionBIEs) {
            this.appUser = appUser;
            this.numberOfWIPBIEs = numberOfWIPBIEs;
            this.numberOfQABIEs = numberOfQABIEs;
            this.numberOfProductionBIEs = numberOfProductionBIEs;

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(this.appUser);

            for (int i = 0; i < numberOfWIPBIEs; ++i) {
                String randomASCCP = nextRandomASCCP();
                recentBIEs.add(randomASCCP);
                createTopLevelASBIEPByDEN(this.appUser, context, randomASCCP,
                        release, "WIP");
            }
            for (int i = 0; i < numberOfQABIEs; ++i) {
                String randomASCCP = nextRandomASCCP();
                recentBIEs.add(randomASCCP);
                createTopLevelASBIEPByDEN(this.appUser, context, randomASCCP,
                        release, "QA");
            }
            for (int i = 0; i < numberOfProductionBIEs; ++i) {
                String randomASCCP = nextRandomASCCP();
                recentBIEs.add(randomASCCP);
                createTopLevelASBIEPByDEN(this.appUser, context, randomASCCP,
                        release, "Production");
            }
        }

        String nextRandomASCCP() {
            if (this.yieldPointer == SAMPLED_ASCCP_DEN_LIST.size()) {
                this.yieldPointer = 0;
            }
            return SAMPLED_ASCCP_DEN_LIST.get(this.yieldPointer++);
        }
    }

}

