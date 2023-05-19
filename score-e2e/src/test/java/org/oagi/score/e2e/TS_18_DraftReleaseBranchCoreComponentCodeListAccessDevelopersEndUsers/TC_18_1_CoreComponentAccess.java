package org.oagi.score.e2e.TS_18_DraftReleaseBranchCoreComponentCodeListAccessDevelopersEndUsers;

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
import org.oagi.score.e2e.page.release.ViewEditReleasePage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Execution(ExecutionMode.CONCURRENT)
public class TC_18_1_CoreComponentAccess extends BaseTest {
    private List<AppUserObject> randomAccounts = new ArrayList<>();

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
    public void test_TA_18_1_1() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        ReleaseObject workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        ReleaseObject euBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.8");
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("Draft");
        ccStates.add("Candidate");
        ccStates.add("Published");
        ccStates.add("Deleted");
        RandomCoreComponentWithStateContainer developerCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, workingBranch, namespace, ccStates);
        CodeListObject codeListCandidate = getAPIFactory().getCodeListAPI().
                createRandomCodeList(developer, namespace, workingBranch, "Candidate");
        getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListCandidate, developer);
        codeListCandidate.setVersionId("99");
        codeListCandidate.setDefinition("random code list in candidate state");
        getAPIFactory().getCodeListAPI().updateCodeList(codeListCandidate);

        List<String> euCCStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("QA");
        ccStates.add("Production");

        RandomCoreComponentWithStateContainer euCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(endUser, euBranch, euNamespace, euCCStates);

        HomePage homePage = loginPage().signIn("oagis", "oagis");
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();

















    }

    @Test
    public void test_TA_18_1_2() {

    }

    @Test
    public void test_TA_18_1_3() {

    }

    @Test
    public void test_TA_18_1_4() {

    }

    @Test
    public void test_TA_18_1_5_a() {

    }

    @Test
    public void test_TA_18_1_5_b() {

    }

    @Test
    public void test_TA_18_1_5_c() {

    }
    @Test
    public void test_TA_18_1_5_d() {

    }

    @Test
    public void test_TA_18_1_5_e() {

    }


}
