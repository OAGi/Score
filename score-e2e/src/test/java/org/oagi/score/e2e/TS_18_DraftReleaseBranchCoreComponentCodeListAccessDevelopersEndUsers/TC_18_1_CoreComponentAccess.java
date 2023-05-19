package org.oagi.score.e2e.TS_18_DraftReleaseBranchCoreComponentCodeListAccessDevelopersEndUsers;

import org.apache.commons.lang3.RandomUtils;
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
import org.oagi.score.e2e.page.release.EditReleasePage;
import org.oagi.score.e2e.page.release.ReleaseAssignmentPage;
import org.oagi.score.e2e.page.release.ViewEditReleasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_18_1_CoreComponentAccess extends BaseTest {
    private List<AppUserObject> randomAccounts = new ArrayList<>();
    String newReleaseNum = String.valueOf((RandomUtils.nextInt(20230519, 20231231)));
    RandomCoreComponentWithStateContainer developerCoreComponentWithStateContainer;
    RandomCoreComponentWithStateContainer euCoreComponentWithStateContainer;
    @BeforeEach
    public void init() {
        super.init();
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
        developerCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, workingBranch, namespace, ccStates);
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

        euCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(endUser, euBranch, euNamespace, euCCStates);

        HomePage homePage = loginPage().signIn("oagis", "oagis");
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();

        EditReleasePage editReleasePage = viewEditReleasePage.createRelease();
        editReleasePage.setReleaseNum(newReleaseNum);
        editReleasePage.setReleaseNamespace(namespace);
        editReleasePage.hitUpdateButton();
        viewEditReleasePage.openPage();
        editReleasePage =  viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage =  editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitCreateButton();
        waitFor(Duration.ofMillis(6000L));
        homePage.logout();
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
        HomePage homePage = loginPage().signIn("oagis", "oagis");
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        for (Map.Entry<String, ACCObject> entry : developerCoreComponentWithStateContainer.stateACCs.entrySet()) {
            String state = entry.getKey();
            ACCObject acc = developerCoreComponentWithStateContainer.stateACCs.get(state);
            viewEditCoreComponentPage.openPage();
            viewEditCoreComponentPage.setBranch(newReleaseNum);
            if (!state.equalsIgnoreCase("Candidate")) {
                viewEditCoreComponentPage.setDEN(acc.getDen());
                viewEditCoreComponentPage.hitSearchButton();
                assertEquals(0, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"" + acc.getDen() + "\")]//ancestor::tr/td[1]//label/span[1]")).size());
            }else{
                viewEditCoreComponentPage.setDEN(acc.getDen());
                viewEditCoreComponentPage.hitSearchButton();
                assertEquals(1, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"" + acc.getDen() + "\")]//ancestor::tr/td[1]//label/span[1]")).size());
            }
        }
        viewEditCoreComponentPage.openPage();
        viewEditCoreComponentPage.setBranch(newReleaseNum);
        assertEquals(0, getDriver().findElements(By.xpath("//button[@mattooltip=\"Create Component\"]")).size());
    }

    @Test
    public void test_TA_18_1_2() {
        HomePage homePage = loginPage().signIn("oagis", "oagis");
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("QA");
        ccStates.add("Production");
        ccStates.add("Deleted");
        viewEditCoreComponentPage.setBranch(newReleaseNum);

        for (String state : ccStates){
            viewEditCoreComponentPage.setState(state);
            viewEditCoreComponentPage.hitSearchButton();
            assertEquals(0, getDriver().findElements(By.xpath("//score-cc-list//table//tbody//tr")).size());
        }
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
