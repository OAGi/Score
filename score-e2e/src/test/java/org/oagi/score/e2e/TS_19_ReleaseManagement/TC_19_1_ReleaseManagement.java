package org.oagi.score.e2e.TS_19_ReleaseManagement;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.TS_18_DraftReleaseBranchCoreComponentCodeListAccessDevelopersEndUsers.TC_18_1_CoreComponentAccess;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.SelectAssociationDialog;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.oagi.score.e2e.page.namespace.CreateNamespacePage;
import org.oagi.score.e2e.page.namespace.ViewEditNamespacePage;
import org.oagi.score.e2e.page.release.CreateReleasePage;
import org.oagi.score.e2e.page.release.EditReleasePage;
import org.oagi.score.e2e.page.release.ReleaseAssignmentPage;
import org.oagi.score.e2e.page.release.ViewEditReleasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.elementToBeClickable;

@Execution(ExecutionMode.CONCURRENT)
public class TC_19_1_ReleaseManagement extends BaseTest {
    private List<AppUserObject> randomAccounts = new ArrayList<>();
    AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
    AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
    String existingReleaseNum = null;
    String newReleaseNum = String.valueOf((RandomUtils.nextInt(20230519, 20231231)));
    CodeListObject codeListCandidate;
    RandomCoreComponentWithStateContainer developerCoreComponentWithStateContainer;
    RandomCoreComponentWithStateContainer euCoreComponentWithStateContainer;

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

    public void cleanUp() {
        if (existingReleaseNum != null) {
            try {
                // move the draft release back to initialized state
                HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
                ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
                viewEditReleasePage.MoveBackToInitialized(existingReleaseNum);
                waitFor(Duration.ofSeconds(60L));
            } finally {
                existingReleaseNum = null;
                getDriver().quit();

            }

        }

    }


    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    public void test_TA_19_1_1() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        CreateReleasePage createReleasePage = viewEditReleasePage.createRelease();
        String newReleaseNum = String.valueOf((RandomUtils.nextInt(20230716, 20231231)));
        createReleasePage.setReleaseNumber(newReleaseNum);
        assertThrows(TimeoutException.class, () -> createReleasePage.hitCreateButton());
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        createReleasePage.setReleaseNamespace(namespace);
        createReleasePage.setReleaseNote("A release note");
        createReleasePage.setReleaseLicense("A release license");
        createReleasePage.hitCreateButton();

        viewEditReleasePage.openPage();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Initialized");
        assertEquals(newReleaseNum, getText(editReleasePage.getReleaseNumberField()));
    }
    @Test
    public void test_TA_19_1_2() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        CreateReleasePage createReleasePage = viewEditReleasePage.createRelease();
        String newReleaseNum = String.valueOf((RandomUtils.nextInt(20230716, 20231231)));
        createReleasePage.setReleaseNumber(newReleaseNum);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        createReleasePage.setReleaseNamespace(namespace);
        createReleasePage.setReleaseNote("A release note");
        createReleasePage.setReleaseLicense("A release license");
        createReleasePage.hitCreateButton();

        viewEditReleasePage.openPage();
        viewEditReleasePage.setReleaseNum(newReleaseNum);
        viewEditReleasePage.hitSearchButton();
        WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
        WebElement td = viewEditReleasePage.getColumnByName(tr, "select");
        click(td);
        click(elementToBeClickable(getDriver(), By.xpath("//mat-icon[contains(text(), \"more_vert\")]//ancestor::button[1]")));
        click(viewEditReleasePage.getDiscardButton());
        click(elementToBeClickable(getDriver(), By.xpath(
                "//mat-dialog-container//span[contains(text(), \"Discard\")]//ancestor::button[1]")));
        viewEditReleasePage.openPage();
        assertThrows(NoSuchElementException.class, () ->viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Initialized"));
    }
    @Test
    public void test_TA_19_1_3a() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        CreateReleasePage createReleasePage = viewEditReleasePage.createRelease();
        String newReleaseNum = String.valueOf((RandomUtils.nextInt(20230716, 20231231)));
        createReleasePage.setReleaseNumber(newReleaseNum);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        createReleasePage.setReleaseNamespace(namespace);
        createReleasePage.setReleaseNote("A release note");
        createReleasePage.setReleaseLicense("A release license");
        createReleasePage.hitCreateButton();

        if (existingReleaseNum == null) {
            draft_creation();
            existingReleaseNum = newReleaseNum;
        }

        viewEditReleasePage.openPage();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(existingReleaseNum, "Draft");
        assertEquals(existingReleaseNum, getText(editReleasePage.getReleaseNumberField()));
    }

    public void draft_creation() {
        ReleaseObject workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        ReleaseObject euBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.8");
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("Draft");
        ccStates.add("Candidate");
        ccStates.add("Deleted");
        developerCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, workingBranch, namespace, ccStates);
        ACCObject candidateACC = developerCoreComponentWithStateContainer.stateACCs.get("Candidate");
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(candidateACC.getAccManifestId());
        accViewEditPage.backToWIP();
        SelectAssociationDialog appendAssociationDialog = accViewEditPage.appendPropertyAtLast("/" + candidateACC.getDen());
        appendAssociationDialog.selectAssociation("Adjusted Total Tax Amount");
        accViewEditPage.moveToDraft();
        accViewEditPage.moveToCandidate();

        codeListCandidate = getAPIFactory().getCodeListAPI().
                createRandomCodeList(developer, namespace, workingBranch, "Published");
        getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListCandidate, developer);

        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeListCandidate.getName(), "Working");
        editCodeListPage.hitRevise();
        editCodeListPage.setVersion("99");
        editCodeListPage.setDefinition("random code list in candidate state");
        editCodeListPage.hitUpdateButton();
        editCodeListPage.moveToDraft();
        editCodeListPage.moveToCandidate();

        List<String> euCCStates = new ArrayList<>();
        euCCStates.add("WIP");
        euCCStates.add("QA");
        euCCStates.add("Production");

        euCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(endUser, euBranch, euNamespace, euCCStates);

        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();

        CreateReleasePage createReleasePage = viewEditReleasePage.createRelease();
        createReleasePage.setReleaseNumber(newReleaseNum);
        createReleasePage.setReleaseNamespace(namespace);
        createReleasePage.hitCreateButton();
        viewEditReleasePage.openPage();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitCreateButton();
        ReleaseObject newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        do {
            newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        } while (!newDraftRelease.getState().equals("Draft"));
        homePage.logout();
    }
    @Test
    public void test_TA_19_1_3b() {

    }
    @Test
    public void test_TA_19_1_3c() {

    }
    @Test
    public void test_TA_19_1_3d() {

    }

    @Test
    public void test_TA_19_1_3e() {

    }

    @Test
    public void test_TA_19_1_3f() {

    }

    @Test
    public void test_TA_19_1_3g() {

    }

    @Test
    public void test_TA_19_1_3h() {

    }

    @Test
    public void test_TA_19_1_3i() {

    }

    @Test
    public void test_TA_19_1_3j() {

    }

    @Test
    public void test_TA_19_1_3k() {

    }

    @Test
    public void test_TA_19_1_4() {

    }

    @Test
    public void test_TA_19_1_5() {

    }

    @Test
    public void test_TA_19_1_6() {

    }

    @Test
    public void test_TA_19_1_7() {

    }

    @Test
    public void test_TA_19_1_8() {

    }

    @Test
    public void test_TA_19_1_9() {

    }

    @Test
    public void test_TA_19_1_10() {

    }

    @Test
    public void test_TA_19_1_11() {

    }

    @Test
    public void test_TA_19_1_12() {

    }

    @Test
    public void test_TA_19_1_13() {

    }

    @Test
    public void test_TA_19_1_14() {

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
}
