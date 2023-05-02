package org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.acc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.SelectAssociationDialog;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_15_9_EditingAssociatiionsDuringEndUserAmendment extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random accounts
        this.randomAccounts.forEach(randomAccount -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId());
        });
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    private class RandomCoreComponentWithStateContainer {
        private AppUserObject appUser;
        private List<String> states = new ArrayList<>();
        private HashMap<String, ACCObject> stateACCs = new HashMap<>();
        private HashMap<String, ASCCPObject> stateASCCPs = new HashMap<>();
        private HashMap<String, BCCPObject> stateBCCPs = new HashMap<>();

        public RandomCoreComponentWithStateContainer(AppUserObject appUser, ReleaseObject release, NamespaceObject namespace, List<String> states) {
            this.appUser = appUser;
            this.states = states;


            for (int i = 0; i < this.states.size(); ++i) {
                ASCCPObject asccp;
                BCCPObject bccp;
                ACCObject acc;
                String state = this.states.get(i);

                {
                    CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

                    acc = coreComponentAPI.createRandomACC(this.appUser, release, namespace, state);
                    DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
                    bccp = coreComponentAPI.createRandomBCCP(dataType, this.appUser, namespace, state);
                    BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, state);
                    bcc.setCardinalityMax(1);
                    coreComponentAPI.updateBCC(bcc);

                    ACCObject acc_association = coreComponentAPI.createRandomACC(this.appUser, release, namespace, state);
                    BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, this.appUser, namespace, state);
                    coreComponentAPI.appendBCC(acc_association, bccp_to_append, state);

                    asccp = coreComponentAPI.createRandomASCCP(acc_association, this.appUser, namespace, state);
                    ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, state);
                    ascc.setCardinalityMax(1);
                    coreComponentAPI.updateASCC(ascc);
                    stateACCs.put(state, acc);
                    stateASCCPs.put(state, asccp);
                    stateBCCPs.put(state, bccp);
                }
            }
        }

    }

    @Test
    public void test_TA_15_9_1_a() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherUser= getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(anotherUser, release, namespace, "Production");
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();

        List<String> ccStates = new ArrayList<>();
        ccStates.add("Production");
        ccStates.add("QA");
        ccStates.add("WIP");
        ccStates.add("Deleted");
        RandomCoreComponentWithStateContainer randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(endUser, release, namespace, ccStates);

        ASCCPObject asccp;

        for (Map.Entry<String, ACCObject> entry : randomCoreComponentWithStateContainer.stateACCs.entrySet()) {
            String state = entry.getKey();
            asccp = randomCoreComponentWithStateContainer.stateASCCPs.get(state);

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            String basePath = "/" + acc.getDen();
            SelectAssociationDialog selectAssociationDialog = accViewEditPage.appendPropertyAtLast(basePath);
            selectAssociationDialog.selectAssociation(asccp.getDen());

            //Verify the asccp is associated
            WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
            ACCViewEditPage.ASCCPPanel asccpPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
            assertEquals(state, getText(asccpPanel.getStateField()));
        }

        ccStates = new ArrayList<>();
        ccStates.add("Published");
        ccStates.add("Draft");
        ccStates.add("Candidate");
        ccStates.add("Deleted");
        NamespaceObject namespaceForDeveloper = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        randomCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, release, namespaceForDeveloper, ccStates);


        for (Map.Entry<String, ACCObject> entry : randomCoreComponentWithStateContainer.stateACCs.entrySet()) {
            String state = entry.getKey();
            asccp = randomCoreComponentWithStateContainer.stateASCCPs.get(state);

            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            String basePath = "/" + acc.getDen();
            SelectAssociationDialog selectAssociationDialog = accViewEditPage.appendPropertyAtLast(basePath);
            selectAssociationDialog.selectAssociation(asccp.getDen());

            //Verify the asccp is associated
            WebElement asccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
            ACCViewEditPage.ASCCPPanel asccpPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
            assertEquals(state, getText(asccpPanel.getStateField()));
        }
    }

    @Test
    public void test_TA_15_9_1_b() {

    }

    @Test
    public void test_TA_15_9_1_c() {

    }


    @Test
    public void test_TA_15_9_1_d() {

    }

    @Test
    public void test_TA_15_9_1_e() {

    }

    @Test
    public void test_TA_15_9_2() {

    }

    @Test
    public void test_TA_15_9_3_a() {

    }

    @Test
    public void test_TA_15_9_3_b() {

    }

    @Test
    public void test_TA_15_9_3_c() {

    }

    @Test
    public void test_TA_15_9_3_d() {

    }

    @Test
    public void test_TA_15_9_3_e() {

    }

    @Test
    public void test_TA_15_9_3_f() {

    }

    @Test
    public void test_TA_15_9_4_a() {

    }

    @Test
    public void test_TA_15_9_4_b() {

    }

    @Test
    public void test_TA_15_9_4_c() {

    }

    @Test
    public void test_TA_15_9_4_d() {

    }

    @Test
    public void test_TA_15_9_4_e() {

    }

    @Test
    public void test_TA_15_9_4_f() {

    }

    @Test
    public void test_TA_15_9_5() {

    }

    @Test
    public void test_TA_15_9_6_a() {

    }

    @Test
    public void test_TA_15_9_6_b() {

    }

    @Test
    public void test_TA_15_9_6_c() {

    }

    @Test
    public void test_TA_15_9_6_d() {

    }

    @Test
    public void test_TA_15_9_6_e() {

    }

    @Test
    public void test_TA_15_9_6_f() {

    }

    @Test
    public void test_TA_15_9_7() {

    }

    @Test
    public void test_TA_15_9_8() {

    }

    @Test
    public void test_TA_15_9_9() {

    }

    @Test
    public void test_TA_15_9_10_a() {

    }

    @Test
    public void test_TA_15_9_10_b() {

    }

    @Test
    public void test_TA_15_9_10_c() {

    }

    @Test
    public void test_TA_15_9_10_d() {

    }

    @Test
    public void test_TA_15_9_10_e() {

    }

    @Test
    public void test_TA_15_9_11() {

    }

    @Test
    public void test_TA_15_9_12() {

    }

    @Test
    public void test_TA_15_9_13() {

    }

    @Test
    public void test_TA_15_9_14_a() {

    }

    @Test
    public void test_TA_15_9_14_b() {

    }

    @Test
    public void test_TA_15_9_14_c() {

    }

    @Test
    public void test_TA_15_9_14_d() {

    }

    @Test
    public void test_TA_15_9_15() {

    }
}
