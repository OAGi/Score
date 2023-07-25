package org.oagi.score.e2e.TS_19_ReleaseManagement;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
import org.oagi.score.e2e.page.core_component.*;
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

import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.elementToBeClickable;

@Execution(ExecutionMode.SAME_THREAD)
public class TC_19_1_ReleaseManagement extends BaseTest {
    private List<AppUserObject> randomAccounts = new ArrayList<>();
    AppUserObject devx = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
    AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
    NamespaceObject developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(devx);
    String existingReleaseNum = null;
    String newReleaseNum = String.valueOf((RandomUtils.nextInt(20230716, 20231231)));
    CodeListObject codeListCandidate;
    RandomCoreComponentWithStateContainer developerCoreComponentWithStateContainer;
    RandomCoreComponentWithStateContainer euCoreComponentWithStateContainer;

    Map<String, ACCObject> testingACCs = new HashMap<>();
    Map<String, ASCCPObject> testingASCCPs = new HashMap<>();
    Map<String, BCCPObject> testingBCCPs = new HashMap<>();


    @BeforeEach
    public void init() {
        super.init();
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
        developerCoreComponentWithStateContainer = new RandomCoreComponentWithStateContainer(devx, workingBranch, namespace, ccStates);
        ACCObject candidateACC = developerCoreComponentWithStateContainer.stateACCs.get("Candidate");
        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(candidateACC.getAccManifestId());
        accViewEditPage.backToWIP();
        SelectAssociationDialog appendAssociationDialog = accViewEditPage.appendPropertyAtLast("/" + candidateACC.getDen());
        appendAssociationDialog.selectAssociation("Adjusted Total Tax Amount");
        accViewEditPage.moveToDraft();
        accViewEditPage.moveToCandidate();

        codeListCandidate = getAPIFactory().getCodeListAPI().
                createRandomCodeList(devx, namespace, workingBranch, "Published");
        getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListCandidate, devx);

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
                HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
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

        String branch = "Working";
        Boolean Revise_Button_existing = false;
        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        {
            ReleaseObject workingRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            ACCObject ACCreleaseTA3devxwip = coreComponentAPI.createRandomACC(devx, workingRelease, developerNamespace, "WIP");
            ACCreleaseTA3devxwip.setObjectClassTerm("ACCrelease TA3devxwip");
            coreComponentAPI.updateACC(ACCreleaseTA3devxwip);
            if (!testingACCs.containsKey("ACCreleaseTA3devxwip")) {
                testingACCs.put("ACCreleaseTA3devxwip", ACCreleaseTA3devxwip);
            } else {
                testingACCs.put("ACCreleaseTA3devxwip", ACCreleaseTA3devxwip);
            }

            ACCObject ACCreleaseTA3devxdraft = coreComponentAPI.createRandomACC(devx, workingRelease, developerNamespace, "Draft");
            ACCreleaseTA3devxdraft.setObjectClassTerm("ACCrelease TA3devxdraft");
            coreComponentAPI.updateACC(ACCreleaseTA3devxdraft);
            if (!testingACCs.containsKey("ACCreleaseTA3devxdraft")) {
                testingACCs.put("ACCreleaseTA3devxdraft", ACCreleaseTA3devxdraft);
            } else {
                testingACCs.put("ACCreleaseTA3devxdraft", ACCreleaseTA3devxdraft);
            }

            ACCObject ACCreleaseTA3devxcandidate = coreComponentAPI.createRandomACC(devx, workingRelease, developerNamespace, "Candidate");
            ACCreleaseTA3devxcandidate.setObjectClassTerm("ACCrelease TA3devxcandidate");
            coreComponentAPI.updateACC(ACCreleaseTA3devxcandidate);
            if (!testingACCs.containsKey("ACCreleaseTA3devxcandidate")) {
                testingACCs.put("ACCreleaseTA3devxcandidate", ACCreleaseTA3devxcandidate);
            } else {
                testingACCs.put("ACCreleaseTA3devxcandidate", ACCreleaseTA3devxcandidate);
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog("Working");
            ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create("Entity Identifiers Group. Details");
            ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPPanel();
            asccpPanel.setPropertyTerm("ASCCPrelease TA3devxwip");
            asccpPanel.setNamespace("http://www.openapplications.org/oagis/10");
            asccpPanel.setDefinition("ASCCPrelease TA3devxwip");
            asccpViewEditPage.hitUpdateButton();
            String url = getDriver().getCurrentUrl();
            BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
            ASCCPObject ASCCPreleaseTA3devxwip = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
            if (!testingASCCPs.containsKey("ASCCPreleaseTA3devxwip")) {
                testingASCCPs.put("ASCCPreleaseTA3devxwip", ASCCPreleaseTA3devxwip);
            } else {
                testingASCCPs.put("ASCCPreleaseTA3devxwip",ASCCPreleaseTA3devxwip);
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
            asccpViewEditPage = asccpCreateDialog.create("Entity Identifiers Group. Details");
            asccpPanel = asccpViewEditPage.getASCCPPanel();
            asccpPanel.setPropertyTerm("ASCCPrelease TA3devxdraft");
            asccpPanel.setNamespace("http://www.openapplications.org/oagis/10");
            asccpPanel.setDefinition("ASCCPrelease TA3devxdraft");
            asccpViewEditPage.hitUpdateButton();
            url = getDriver().getCurrentUrl();
            asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
            ASCCPObject ASCCPreleaseTA3devxdraft = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
            asccpViewEditPage.moveToDraft();
            if (!testingASCCPs.containsKey("ASCCPreleaseTA3devxdraft")) {
                testingASCCPs.put("ASCCPreleaseTA3devxdraft", ASCCPreleaseTA3devxdraft);
            } else {
                testingASCCPs.put("ASCCPreleaseTA3devxdraft",ASCCPreleaseTA3devxdraft);
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
            asccpViewEditPage = asccpCreateDialog.create("Entity Identifiers Group. Details");
            asccpPanel = asccpViewEditPage.getASCCPPanel();
            asccpPanel.setPropertyTerm("ASCCPrelease TA3devcandidate");
            asccpPanel.setNamespace("http://www.openapplications.org/oagis/10");
            asccpPanel.setDefinition("ASCCPrelease TA3devxcandidate");
            asccpViewEditPage.hitUpdateButton();
            url = getDriver().getCurrentUrl();
            asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
            ASCCPObject ASCCPreleaseTA3devxcandidate = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
            asccpViewEditPage.moveToDraft();
            asccpViewEditPage.moveToCandidate();
            if (!testingASCCPs.containsKey("ASCCPreleaseTA3devxcandidate")) {
                testingASCCPs.put("ASCCPreleaseTA3devxcandidate", ASCCPreleaseTA3devxcandidate);
            } else {
                testingASCCPs.put("ASCCPreleaseTA3devxcandidate",ASCCPreleaseTA3devxcandidate);
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            BCCPCreateDialog bccpCreateDialog = viewEditCoreComponentPage.openBCCPCreateDialog(branch);
            BCCPViewEditPage bccpViewEditPage = bccpCreateDialog.create("System Environment_ Code. Type");
            BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
            bccpPanel.setPropertyTerm("BCCPreleaseTA3devxwip");
            bccpPanel.setNamespace("http://www.openapplications.org/oagis/10");
            bccpPanel.setDefinition("BCCPreleaseTA3devxwip");
            bccpViewEditPage.hitUpdateButton();
            url = getDriver().getCurrentUrl();
            BigInteger bccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
            BCCPObject BCCPreleaseTA3devxwip = getAPIFactory().getCoreComponentAPI().getBCCPByManifestId(bccpManifestId);

            if (!testingBCCPs.containsKey("BCCPreleaseTA3devxwip")) {
                testingBCCPs.put("BCCPreleaseTA3devxwip", BCCPreleaseTA3devxwip);
            } else {
                testingBCCPs.put("BCCPreleaseTA3devxwip",BCCPreleaseTA3devxwip);
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            bccpCreateDialog = viewEditCoreComponentPage.openBCCPCreateDialog(branch);
            bccpViewEditPage = bccpCreateDialog.create("System Environment_ Code. Type");
            bccpPanel.setPropertyTerm("BCCPreleaseTA3devxdraft");
            bccpPanel.setNamespace("http://www.openapplications.org/oagis/10");
            bccpPanel.setDefinition("BCCPreleaseTA3devxdraft");
            bccpViewEditPage.hitUpdateButton();
            url = getDriver().getCurrentUrl();
            bccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
            BCCPObject BCCPreleaseTA3devxdraft = getAPIFactory().getCoreComponentAPI().getBCCPByManifestId(bccpManifestId);
            bccpViewEditPage.moveToDraft();

            if (!testingBCCPs.containsKey("BCCPreleaseTA3devxdraft")) {
                testingBCCPs.put("BCCPreleaseTA3devxdraft", BCCPreleaseTA3devxdraft);
            } else {
                testingBCCPs.put("BCCPreleaseTA3devxdraft",BCCPreleaseTA3devxdraft);
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            bccpCreateDialog = viewEditCoreComponentPage.openBCCPCreateDialog(branch);
            bccpViewEditPage = bccpCreateDialog.create("System Environment_ Code. Type");
            bccpPanel.setPropertyTerm("BCCPreleaseTA3devxcandidate");
            bccpPanel.setNamespace("http://www.openapplications.org/oagis/10");
            bccpPanel.setDefinition("BCCPreleaseTA3devxcandidate");
            bccpViewEditPage.hitUpdateButton();
            url = getDriver().getCurrentUrl();
            bccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
            BCCPObject BCCPreleaseTA3devxcandidate = getAPIFactory().getCoreComponentAPI().getBCCPByManifestId(bccpManifestId);
            bccpViewEditPage.moveToDraft();
            bccpViewEditPage.moveToCandidate();

            if (!testingBCCPs.containsKey("BCCPreleaseTA3devxcandidate")) {
                testingBCCPs.put("BCCPreleaseTA3devxcandidate", BCCPreleaseTA3devxcandidate);
            } else {
                testingBCCPs.put("BCCPreleaseTA3devxcandidate",BCCPreleaseTA3devxcandidate);
            }

            //Revision
            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            if (!testingACCs.containsKey("ACCreleaseTA3revisionwip")){
                ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch("Notify Corrective Action Request Data Area. Details", "Working");
                Revise_Button_existing = 1 == getDriver().findElements(By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]")).size();
                if(Revise_Button_existing){
                    accViewEditPage.hitReviseButton();
                    Revise_Button_existing = false;
                }
                url = getDriver().getCurrentUrl();
                BigInteger accManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
                ACCObject ACCreleaseTA3revisionwip = getAPIFactory().getCoreComponentAPI().getACCByManifestId(accManifestId);
                if (!testingACCs.containsKey("ACCreleaseTA3revisionwip")) {
                    testingACCs.put("ACCreleaseTA3revisionwip", ACCreleaseTA3revisionwip);
                } else {
                    testingACCs.put("ACCreleaseTA3revisionwip", ACCreleaseTA3revisionwip);
                }
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            if (!testingACCs.containsKey("ACCreleaseTA3revisiondraft")){
                ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch("Online Document Reference Base. Details", "Working");
                Revise_Button_existing = 1 == getDriver().findElements(By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]")).size();
                if(Revise_Button_existing){
                    accViewEditPage.hitReviseButton();
                    accViewEditPage.moveToDraft();
                    Revise_Button_existing = false;
                }
                url = getDriver().getCurrentUrl();
                BigInteger accManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
                ACCObject ACCreleaseTA3revisiondraft = getAPIFactory().getCoreComponentAPI().getACCByManifestId(accManifestId);
                if (!testingACCs.containsKey("ACCreleaseTA3revisiondraft")) {
                    testingACCs.put("ACCreleaseTA3revisiondraft", ACCreleaseTA3revisiondraft);
                } else {
                    testingACCs.put("ACCreleaseTA3revisiondraft", ACCreleaseTA3revisiondraft);
                }
            }
            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            if (!testingACCs.containsKey("ACCreleaseTA3revisioncandidate")){
                ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch("Order Commission Base. Details", "Working");
                Revise_Button_existing = 1 == getDriver().findElements(By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]")).size();
                if (Revise_Button_existing){
                    accViewEditPage.hitReviseButton();
                    accViewEditPage.moveToDraft();
                    accViewEditPage.moveToCandidate();
                    Revise_Button_existing = false;
                }
                url = getDriver().getCurrentUrl();
                BigInteger accManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
                ACCObject ACCreleaseTA3revisioncandidate = getAPIFactory().getCoreComponentAPI().getACCByManifestId(accManifestId);
                if (!testingACCs.containsKey("ACCreleaseTA3revisioncandidate")) {
                    testingACCs.put("ACCreleaseTA3revisioncandidate", ACCreleaseTA3revisioncandidate);
                } else {
                    testingACCs.put("ACCreleaseTA3revisioncandidate", ACCreleaseTA3revisioncandidate);
                }
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            if (!testingASCCPs.containsKey("ASCCPreleaseTA3revisionwip")){
                asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch("Warning Process Message. Message", "Working");
                Revise_Button_existing = 1 == getDriver().findElements(By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]")).size();
                if (Revise_Button_existing){
                    asccpViewEditPage.hitReviseButton();
                    Revise_Button_existing = false;
                }
                url = getDriver().getCurrentUrl();
                asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
                ASCCPObject ASCCPreleaseTA3revisionwip = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
                if (!testingASCCPs.containsKey("ASCCPreleaseTA3revisionwip")) {
                    testingASCCPs.put("ASCCPreleaseTA3revisionwip", ASCCPreleaseTA3revisionwip);
                } else {
                    testingASCCPs.put("ASCCPreleaseTA3revisionwip", ASCCPreleaseTA3revisionwip);
                }
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            if (!testingASCCPs.containsKey("ASCCPreleaseTA3revisiondraft")){
                asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch("Transportation Term Reference. Document Reference", "Working");
                Revise_Button_existing = 1 == getDriver().findElements(By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]")).size();
                if (Revise_Button_existing){
                    asccpViewEditPage.hitReviseButton();
                    asccpViewEditPage.moveToDraft();
                    Revise_Button_existing = false;
                }
                url = getDriver().getCurrentUrl();
                asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
                ASCCPObject ASCCPreleaseTA3revisiondraft = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
                if (!testingASCCPs.containsKey("ASCCPreleaseTA3revisiondraft")) {
                    testingASCCPs.put("ASCCPreleaseTA3revisiondraft", ASCCPreleaseTA3revisiondraft);
                } else {
                    testingASCCPs.put("ASCCPreleaseTA3revisiondraft", ASCCPreleaseTA3revisiondraft);
                }
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            if (!testingASCCPs.containsKey("ASCCPreleaseTA3revisioncandidate")){
                asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch("Shipping Route. Shipping Route", "Working");
                Revise_Button_existing = 1 == getDriver().findElements(By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]")).size();
                if (Revise_Button_existing){
                    asccpViewEditPage.hitReviseButton();
                    asccpViewEditPage.moveToDraft();
                    asccpViewEditPage.moveToCandidate();
                    Revise_Button_existing = false;
                }
                url = getDriver().getCurrentUrl();
                asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
                ASCCPObject ASCCPreleaseTA3revisioncandidate = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
                if (!testingASCCPs.containsKey("ASCCPreleaseTA3revisioncandidate")) {
                    testingASCCPs.put("ASCCPreleaseTA3revisioncandidate", ASCCPreleaseTA3revisioncandidate);
                } else {
                    testingASCCPs.put("ASCCPreleaseTA3revisioncandidate", ASCCPreleaseTA3revisioncandidate);
                }
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            if (!testingBCCPs.containsKey("BCCPreleaseTA3revisionwip")){
                bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByDenAndBranch("Accumulative Received Quantity. Open_ Quantity", "Working");
                Revise_Button_existing = 1 == getDriver().findElements(By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]")).size();
                if (Revise_Button_existing){
                    bccpViewEditPage.hitReviseButton();
                    Revise_Button_existing = false;
                }
                url = getDriver().getCurrentUrl();
                bccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
                BCCPObject BCCPreleaseTA3revisionwip = getAPIFactory().getCoreComponentAPI().getBCCPByManifestId(bccpManifestId);
                if (!testingBCCPs.containsKey("BCCPreleaseTA3revisionwip")) {
                    testingBCCPs.put("BCCPreleaseTA3revisionwip", BCCPreleaseTA3revisionwip);
                } else {
                    testingBCCPs.put("BCCPreleaseTA3revisionwip", BCCPreleaseTA3revisionwip);
                }
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            if (!testingBCCPs.containsKey("BCCPreleaseTA3revisiondraft")){
                bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByDenAndBranch("Allow Substitution Indicator. Open_ Indicator", "Working");
                Revise_Button_existing = 1 == getDriver().findElements(By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]")).size();
                if (Revise_Button_existing){
                    bccpViewEditPage.hitReviseButton();
                    bccpViewEditPage.moveToDraft();
                    Revise_Button_existing = false;
                }
                url = getDriver().getCurrentUrl();
                bccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
                BCCPObject BCCPreleaseTA3revisiondraft = getAPIFactory().getCoreComponentAPI().getBCCPByManifestId(bccpManifestId);
                if (!testingBCCPs.containsKey("BCCPreleaseTA3revisiondraft")) {
                    testingBCCPs.put("BCCPreleaseTA3revisiondraft", BCCPreleaseTA3revisiondraft);
                } else {
                    testingBCCPs.put("BCCPreleaseTA3revisiondraft", BCCPreleaseTA3revisiondraft);
                }
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            if (!testingBCCPs.containsKey("BCCPreleaseTA3revisioncandidate")){
                bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByDenAndBranch("Appointment Required Indicator. Open_ Indicator", "Working");
                Revise_Button_existing = 1 == getDriver().findElements(By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]")).size();
                if(Revise_Button_existing){
                    bccpViewEditPage.hitReviseButton();
                    bccpViewEditPage.moveToDraft();
                    bccpViewEditPage.moveToCandidate();
                    Revise_Button_existing = false;
                }
                url = getDriver().getCurrentUrl();
                bccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
                BCCPObject BCCPreleaseTA3revisioncandidate = getAPIFactory().getCoreComponentAPI().getBCCPByManifestId(bccpManifestId);
                if (!testingBCCPs.containsKey("BCCPreleaseTA3revisioncandidate")) {
                    testingBCCPs.put("BCCPreleaseTA3revisioncandidate", BCCPreleaseTA3revisioncandidate);
                } else {
                    testingBCCPs.put("BCCPreleaseTA3revisioncandidate", BCCPreleaseTA3revisioncandidate);
                }
            }

        }
        homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();

        CreateReleasePage createReleasePage = viewEditReleasePage.createRelease();
        createReleasePage.setReleaseNumber(newReleaseNum);
        createReleasePage.setReleaseNamespace(developerNamespace);
        createReleasePage.hitCreateButton();
        viewEditReleasePage.openPage();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();

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
