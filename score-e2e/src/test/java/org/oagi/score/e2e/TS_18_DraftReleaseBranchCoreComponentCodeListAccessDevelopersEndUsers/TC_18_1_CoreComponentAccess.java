package org.oagi.score.e2e.TS_18_DraftReleaseBranchCoreComponentCodeListAccessDevelopersEndUsers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.SelectAssociationDialog;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.oagi.score.e2e.page.release.CreateReleasePage;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.SAME_THREAD)
public class TC_18_1_CoreComponentAccess extends BaseTest {

    AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
    AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
    String existingReleaseNum = null;
    String newReleaseNum = Long.toString(System.currentTimeMillis());
    CodeListObject codeListCandidate;
    RandomCoreComponentWithStateContainer developerCoreComponentWithStateContainer;
    RandomCoreComponentWithStateContainer euCoreComponentWithStateContainer;
    ACCObject candidateACCWithNewProperty;
    String associationDen;
    String associationPropertyTerm;
    private List<AppUserObject> randomAccounts = new ArrayList<>();

    public void draft_creation() {
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        ReleaseObject euBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.8");
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("Draft");
        ccStates.add("Candidate");
        ccStates.add("Deleted");
        developerCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(developer, workingBranch, namespace, ccStates);
        candidateACCWithNewProperty = getAPIFactory().getCoreComponentAPI()
                .createRandomACC(developer, workingBranch, namespace, "WIP");
        ASCCPObject candidateASCCP = developerCoreComponentWithStateContainer.stateASCCPs.get("Candidate");
        associationDen = candidateASCCP.getDen();
        associationPropertyTerm = candidateASCCP.getPropertyTerm();
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        homePage.setLibrary("connectSpec");
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(candidateACCWithNewProperty.getAccManifestId());
        SelectAssociationDialog appendAssociationDialog = accViewEditPage.appendPropertyAtLast("/" + candidateACCWithNewProperty.getDen());
        appendAssociationDialog.selectAssociation(associationDen);
        accViewEditPage.moveToDraft();
        accViewEditPage.moveToCandidate();

        codeListCandidate = getAPIFactory().getCodeListAPI().
                createRandomCodeList(developer, namespace, workingBranch, "Published");
        getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListCandidate, developer);

        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeListCandidate);
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
        releaseAssignmentPage.setOwner(developer.getLoginId());
        releaseAssignmentPage.hitSearchButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();
        List<String> validationErrors = getDriver().findElements(By.cssSelector(".errors .message"))
                .stream().map(WebElement::getText).toList();
        assertEquals("All components are valid.", getSnackBarMessage(getDriver()),
                "Only this test user's candidate components should be assigned: " + validationErrors);
        releaseAssignmentPage.hitCreateButton();
        if (getDriver().getCurrentUrl().contains("/assign")) {
            validationErrors = getDriver().findElements(By.cssSelector(".errors .message"))
                    .stream().map(WebElement::getText).toList();
            fail("The release draft was rejected by validation: " + validationErrors);
        }
        ReleaseObject newDraftRelease = null;
        long deadline = System.currentTimeMillis() + Duration.ofSeconds(300L).toMillis();
        while (System.currentTimeMillis() < deadline) {
            newDraftRelease = getAPIFactory().getReleaseAPI()
                    .getReleaseByReleaseNumber(library, newReleaseNum);
            if (newDraftRelease != null && "Draft".equals(newDraftRelease.getState())) {
                break;
            }
            waitFor(Duration.ofSeconds(2L));
        }
        assertTrue(newDraftRelease != null, "The newly created release must exist.");
        assertEquals("Draft", newDraftRelease.getState(),
                "The new release must reach Draft before tests select its branch.");
        homePage.logout();
    }

    @BeforeEach
    public void init() {
        super.init();
        if (existingReleaseNum == null) {
            draft_creation();
            existingReleaseNum = newReleaseNum;
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
    @DisplayName("TC_18_1_TA_1")
    public void developer_can_only_search_and_view_details_of_ccs_i_e_no_cc_creation() {
        thisAccountWillBeDeletedAfterTests(developer);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        for (Map.Entry<String, ACCObject> entry : developerCoreComponentWithStateContainer.stateACCs.entrySet()) {
            String state = entry.getKey();
            ACCObject acc = developerCoreComponentWithStateContainer.stateACCs.get(state);
            viewEditCoreComponentPage.openPage();
            viewEditCoreComponentPage.setBranch(existingReleaseNum);
            if (!state.equalsIgnoreCase("Candidate")) {
                viewEditCoreComponentPage.setDEN(acc.getDen());
                viewEditCoreComponentPage.hitSearchButton();
                assertEquals(0, getDriver().findElements(By.xpath("//*[contains(text(),\"" + acc.getDen() + "\")]//ancestor::tr")).size());
            } else {
                viewEditCoreComponentPage.setDEN(acc.getDen());
                viewEditCoreComponentPage.hitSearchButton();
                assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(),\"" + acc.getDen() + "\")]//ancestor::tr")).size());
            }
        }
        viewEditCoreComponentPage.openPage();
        viewEditCoreComponentPage.setBranch(existingReleaseNum);
        assertEquals(0, getDriver().findElements(By.xpath("//button[@mattooltip=\"Create Component\"]")).size());

    }

    @Test
    @DisplayName("TC_18_1_TA_2")
    public void no_cc_shall_be_listed_for_the_developer_when_state_filter_is_selected_that() {
        thisAccountWillBeDeletedAfterTests(developer);
        thisAccountWillBeDeletedAfterTests(endUser);
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("QA");
        ccStates.add("Production");
        ccStates.add("Deleted");
        viewEditCoreComponentPage.setBranch(existingReleaseNum);
        viewEditCoreComponentPage.showAdvancedSearchPanel();

        for (String state : ccStates) {
            viewEditCoreComponentPage.setState(state);
            escape(getDriver());
            viewEditCoreComponentPage.hitSearchButton();
            assertEquals(0, getDriver().findElements(By.xpath("//score-cc-list//table//tbody//tr")).size());
        }
    }

    @Test
    @DisplayName("TC_18_1_TA_3")
    public void end_user_can_only_search_and_view_details_of_ccs_i_e_no_cc() {
        thisAccountWillBeDeletedAfterTests(developer);
        thisAccountWillBeDeletedAfterTests(endUser);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();
        for (Map.Entry<String, ACCObject> entry : developerCoreComponentWithStateContainer.stateACCs.entrySet()) {
            String state = entry.getKey();
            ACCObject acc = developerCoreComponentWithStateContainer.stateACCs.get(state);
            viewEditCoreComponentPage.openPage();
            viewEditCoreComponentPage.setBranch(existingReleaseNum);
            if (!state.equalsIgnoreCase("Candidate")) {
                viewEditCoreComponentPage.setDEN(acc.getDen());
                viewEditCoreComponentPage.hitSearchButton();
                assertEquals(0, getDriver().findElements(By.xpath("//*[contains(text(),\"" + acc.getDen() + "\")]//ancestor::tr")).size());
            } else {
                viewEditCoreComponentPage.setDEN(acc.getDen());
                viewEditCoreComponentPage.hitSearchButton();
                assertEquals(1, getDriver().findElements(By.xpath("//*[contains(text(),\"" + acc.getDen() + "\")]//ancestor::tr")).size());
            }
        }
        viewEditCoreComponentPage.openPage();
        viewEditCoreComponentPage.setBranch(existingReleaseNum);
        assertEquals(0, getDriver().findElements(By.xpath("//button[@mattooltip=\"Create Component\"]")).size());
    }

    @Test
    @DisplayName("TC_18_1_TA_4")
    public void no_cc_shall_be_listed_for_the_end_user_when_state_filter_is_selected() {
        thisAccountWillBeDeletedAfterTests(developer);
        thisAccountWillBeDeletedAfterTests(endUser);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        List<String> ccStates = new ArrayList<>();
        ccStates.add("WIP");
        ccStates.add("QA");
        ccStates.add("Production");
        ccStates.add("Deleted");
        viewEditCoreComponentPage.setBranch(existingReleaseNum);
        viewEditCoreComponentPage.showAdvancedSearchPanel();

        for (String state : ccStates) {
            viewEditCoreComponentPage.setState(state);
            escape(getDriver());
            viewEditCoreComponentPage.hitSearchButton();
            assertEquals(0, getDriver().findElements(By.xpath("//score-cc-list//table//tbody//tr")).size());
        }

    }

    @Test
    @DisplayName("TC_18_1_TA_5.a_and_TA_5.b")
    public void core_component_access_new_property_added_to_an_acc_based_acc_changes() {
        thisAccountWillBeDeletedAfterTests(developer);
        thisAccountWillBeDeletedAfterTests(endUser);
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        homePage.setLibrary("connectSpec");
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(newReleaseNum);
        ACCObject candidateACC = candidateACCWithNewProperty;
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(candidateACC.getDen(), existingReleaseNum);
        WebElement asccpNode = accViewEditPage.getNodeByPath("/" + candidateACC.getDen() + "/" + associationPropertyTerm);
        assertTrue(asccpNode.isDisplayed());

        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        viewEditCodeListPage.setBranch(existingReleaseNum);
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeListCandidate);
        assertEquals("99", getText(editCodeListPage.getVersionField()));
        assertEquals("random code list in candidate state", getText(editCodeListPage.getDefinitionField()));
    }

    /**
     * We cannot change BDT now
     */
    @Test
    @Disabled
    public void changing_bdt_is_not_covered_yet() {
        thisAccountWillBeDeletedAfterTests(developer);
        thisAccountWillBeDeletedAfterTests(endUser);
    }

    /**
     * Position of an association changes.
     */
    @Test
    @Disabled
    public void changing_association_position_is_not_covered_yet() {
        thisAccountWillBeDeletedAfterTests(developer);
        thisAccountWillBeDeletedAfterTests(endUser);
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

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryById(release.getLibraryId());

            for (int i = 0; i < this.states.size(); ++i) {
                ASCCPObject asccp;
                BCCPObject bccp;
                ACCObject acc;
                String state = this.states.get(i);

                {
                    CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

                    acc = coreComponentAPI.createRandomACC(this.appUser, release, namespace, state);
                    DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
                    bccp = coreComponentAPI.createRandomBCCP(release, dataType, this.appUser, namespace, state);
                    BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, state);
                    bcc.setCardinalityMax(1);
                    coreComponentAPI.updateBCC(bcc);

                    ACCObject acc_association = coreComponentAPI.createRandomACC(this.appUser, release, namespace, state);
                    BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, this.appUser, namespace, state);
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
