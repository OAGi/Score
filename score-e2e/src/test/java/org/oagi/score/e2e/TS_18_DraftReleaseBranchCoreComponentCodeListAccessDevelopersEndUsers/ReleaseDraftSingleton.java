package org.oagi.score.e2e.TS_18_DraftReleaseBranchCoreComponentCodeListAccessDevelopersEndUsers;

import org.apache.commons.lang3.RandomUtils;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.Configuration;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.impl.api.DSLContextAPIFactory;
import org.oagi.score.e2e.impl.page.LoginPageImpl;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.LoginPage;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.SelectAssociationDialog;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.oagi.score.e2e.page.release.CreateReleasePage;
import org.oagi.score.e2e.page.release.EditReleasePage;
import org.oagi.score.e2e.page.release.ReleaseAssignmentPage;
import org.oagi.score.e2e.page.release.ViewEditReleasePage;
import org.openqa.selenium.WebDriver;

import java.time.Duration;
import java.util.*;

import static org.oagi.score.e2e.impl.PageHelper.waitFor;

public class ReleaseDraftSingleton {

    private static ReleaseDraftSingleton instance;

    private AppUserObject developer;
    private AppUserObject endUser;

    private String existingReleaseNum = null;
    private String newReleaseNum = String.valueOf((RandomUtils.nextInt(20230519, 20231231)));
    private CodeListObject codeListCandidate;
    private RandomCoreComponentWithStateContainer developerCoreComponentWithStateContainer;
    private RandomCoreComponentWithStateContainer euCoreComponentWithStateContainer;

    public AppUserObject getDeveloper() {
        return developer;
    }

    public AppUserObject getEndUser() {
        return endUser;
    }

    public String getExistingReleaseNum() {
        return existingReleaseNum;
    }

    public String getNewReleaseNum() {
        return newReleaseNum;
    }

    public CodeListObject getCodeListCandidate() {
        return codeListCandidate;
    }

    public RandomCoreComponentWithStateContainer getDeveloperCoreComponentWithStateContainer() {
        return developerCoreComponentWithStateContainer;
    }

    public RandomCoreComponentWithStateContainer getEuCoreComponentWithStateContainer() {
        return euCoreComponentWithStateContainer;
    }

    private ReleaseDraftSingleton() {
        Configuration config = Configuration.load();
        APIFactory apiFactory = DSLContextAPIFactory.build(config);
        WebDriver driver = config.newWebDriver();
        try {
            LoginPage loginPage = new LoginPageImpl(driver, config, apiFactory);

            developer = apiFactory.getAppUserAPI().createRandomDeveloperAccount(false);
            endUser = apiFactory.getAppUserAPI().createRandomEndUserAccount(false);

            ReleaseObject workingBranch = apiFactory.getReleaseAPI().getReleaseByReleaseNumber("Working");
            ReleaseObject euBranch = apiFactory.getReleaseAPI().getTheLatestRelease();
            NamespaceObject euNamespace = apiFactory.getNamespaceAPI().createRandomEndUserNamespace(endUser);
            NamespaceObject namespace = apiFactory.getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            List<String> ccStates = new ArrayList<>();
            ccStates.add("WIP");
            ccStates.add("Draft");
            ccStates.add("Candidate");
            ccStates.add("Deleted");
            developerCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(apiFactory, developer, workingBranch, namespace, ccStates);
            ACCObject candidateACC = developerCoreComponentWithStateContainer.stateACCs.get("Candidate");
            HomePage homePage = loginPage.signIn(developer.getLoginId(), developer.getPassword());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(candidateACC.getAccManifestId());
            accViewEditPage.backToWIP();
            SelectAssociationDialog appendAssociationDialog = accViewEditPage.appendPropertyAtLast("/" + candidateACC.getDen());
            appendAssociationDialog.selectAssociation("Adjusted Total Tax Amount");
            accViewEditPage.moveToDraft();
            accViewEditPage.moveToCandidate();

            codeListCandidate = apiFactory.getCodeListAPI().
                    createRandomCodeList(developer, namespace, workingBranch, "Published");
            apiFactory.getCodeListValueAPI().createRandomCodeListValue(codeListCandidate, developer);

            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeListCandidate.getName(), "Working");
            editCodeListPage.hitRevise();
            codeListCandidate.setVersionId("99");
            codeListCandidate.setDefinition("random code list in candidate state");
            editCodeListPage.moveToDraft();
            editCodeListPage.moveToCandidate();

            List<String> euCCStates = new ArrayList<>();
            euCCStates.add("WIP");
            euCCStates.add("QA");
            euCCStates.add("Production");

            euCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(apiFactory, endUser, euBranch, euNamespace, euCCStates);

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
            waitFor(Duration.ofSeconds(300L));
            existingReleaseNum = newReleaseNum;
            homePage.logout();
        } finally {
            driver.quit();
        }
    }

    public class RandomCoreComponentWithStateContainer {
        private AppUserObject appUser;
        private List<String> states = new ArrayList<>();
        private Map<String, ACCObject> stateACCs = new HashMap<>();
        private Map<String, ASCCPObject> stateASCCPs = new HashMap<>();
        private Map<String, BCCPObject> stateBCCPs = new HashMap<>();

        public Map<String, ACCObject> getStateACCs() {
            return stateACCs;
        }

        public Map<String, ASCCPObject> getStateASCCPs() {
            return stateASCCPs;
        }

        public Map<String, BCCPObject> getStateBCCPs() {
            return stateBCCPs;
        }

        public RandomCoreComponentWithStateContainer(APIFactory apiFactory, AppUserObject appUser, ReleaseObject release, NamespaceObject namespace, List<String> states) {
            this.appUser = appUser;
            this.states = states;


            for (int i = 0; i < this.states.size(); ++i) {
                ASCCPObject asccp;
                BCCPObject bccp;
                ACCObject acc;
                String state = this.states.get(i);

                {
                    CoreComponentAPI coreComponentAPI = apiFactory.getCoreComponentAPI();

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

    public static ReleaseDraftSingleton getInstance() {
        synchronized (ReleaseDraftSingleton.class) {
            if (instance == null) {
                instance = new ReleaseDraftSingleton();
            }
            return instance;
        }
    }

    public void release() {
        synchronized (ReleaseDraftSingleton.class) {
            if (instance != null) {
                Configuration config = Configuration.load();
                APIFactory apiFactory = DSLContextAPIFactory.build(config);
                WebDriver driver = config.newWebDriver();
                try {
                    LoginPage loginPage = new LoginPageImpl(driver, config, apiFactory);

                    // move the draft release back to initialized state
                    HomePage homePage = loginPage.signIn(developer.getLoginId(), developer.getPassword());
                    ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
                    viewEditReleasePage.MoveBackToInitialized(existingReleaseNum);
                    waitFor(Duration.ofSeconds(60L));
                } finally {
                    driver.quit();
                }

                for (AppUserObject appUser : Arrays.asList(developer, endUser)) {
                    apiFactory.getAppUserAPI().deleteAppUserByLoginId(appUser.getLoginId());
                }
            }

            instance = null;
        }
    }

}
