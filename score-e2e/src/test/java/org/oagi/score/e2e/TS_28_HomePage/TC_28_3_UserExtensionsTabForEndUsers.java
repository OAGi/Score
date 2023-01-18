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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.RandomUtils.nextInt;

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
                    ccWIPList.add(new Pair<String, String>(acc.getDen(), this.appUser.getLoginId()));
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
            }

            for (int i = 0; i < numberOfQAUEGs; ++i) {
                ASCCPObject asccp;
                {
                    CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
                    NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

                    ACCObject acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
                    coreComponentAPI.appendExtension(acc, developer, namespace, "Published");

                    asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
                    ccQAList.add(new Pair<String, String>(acc.getDen(), this.appUser.getLoginId()));
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
                    ccProductionList.add(new Pair<String, String>(acc.getDen(), this.appUser.getLoginId()));
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

    }

    @Test
    @DisplayName("TC_28_3_2")
    public void end_user_can_click_state_to_view_extensions_of_that_state_in_total_user_extensions_panel() {

    }

    @Test
    @DisplayName("TC_28_3_3")
    public void end_user_can_see_number_of_extensions_owned_by_him_per_state_in_my_user_extensions_by_states_panel() {

    }

    @Test
    @DisplayName("TC_28_3_4")
    public void end_user_can_click_state_to_view_the_extensions_of_that_state_in_my_user_extensions_by_states_panel() {

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
