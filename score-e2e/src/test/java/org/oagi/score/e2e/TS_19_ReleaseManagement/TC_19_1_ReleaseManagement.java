package org.oagi.score.e2e.TS_19_ReleaseManagement;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.oagi.score.e2e.page.core_component.*;
import org.oagi.score.e2e.page.release.CreateReleasePage;
import org.oagi.score.e2e.page.release.EditReleasePage;
import org.oagi.score.e2e.page.release.ReleaseAssignmentPage;
import org.oagi.score.e2e.page.release.ViewEditReleasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.math.BigInteger;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.SAME_THREAD)
public class TC_19_1_ReleaseManagement extends BaseTest {
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
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

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
        assertThrows(NoSuchElementException.class, () -> viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Initialized"));
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
                testingASCCPs.put("ASCCPreleaseTA3devxwip", ASCCPreleaseTA3devxwip);
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
                testingASCCPs.put("ASCCPreleaseTA3devxdraft", ASCCPreleaseTA3devxdraft);
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
                testingASCCPs.put("ASCCPreleaseTA3devxcandidate", ASCCPreleaseTA3devxcandidate);
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
                testingBCCPs.put("BCCPreleaseTA3devxwip", BCCPreleaseTA3devxwip);
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
                testingBCCPs.put("BCCPreleaseTA3devxdraft", BCCPreleaseTA3devxdraft);
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
                testingBCCPs.put("BCCPreleaseTA3devxcandidate", BCCPreleaseTA3devxcandidate);
            }

            //Revision
            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            if (!testingACCs.containsKey("ACCreleaseTA3revisionwip")) {
                ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch("Notify Corrective Action Request Data Area. Details", "Working");
                Revise_Button_existing = 1 == getDriver().findElements(By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]")).size();
                if (Revise_Button_existing) {
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
            if (!testingACCs.containsKey("ACCreleaseTA3revisiondraft")) {
                ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch("Online Document Reference Base. Details", "Working");
                Revise_Button_existing = 1 == getDriver().findElements(By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]")).size();
                if (Revise_Button_existing) {
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
            if (!testingACCs.containsKey("ACCreleaseTA3revisioncandidate")) {
                ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch("Order Commission Base. Details", "Working");
                Revise_Button_existing = 1 == getDriver().findElements(By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]")).size();
                if (Revise_Button_existing) {
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
            if (!testingASCCPs.containsKey("ASCCPreleaseTA3revisionwip")) {
                asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch("Warning Process Message. Message", "Working");
                Revise_Button_existing = 1 == getDriver().findElements(By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]")).size();
                if (Revise_Button_existing) {
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
            if (!testingASCCPs.containsKey("ASCCPreleaseTA3revisiondraft")) {
                asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch("Transportation Term Reference. Document Reference", "Working");
                Revise_Button_existing = 1 == getDriver().findElements(By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]")).size();
                if (Revise_Button_existing) {
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
            if (!testingASCCPs.containsKey("ASCCPreleaseTA3revisioncandidate")) {
                asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch("Shipping Route. Shipping Route", "Working");
                Revise_Button_existing = 1 == getDriver().findElements(By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]")).size();
                if (Revise_Button_existing) {
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
            if (!testingBCCPs.containsKey("BCCPreleaseTA3revisionwip")) {
                bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByDenAndBranch("Accumulative Received Quantity. Open_ Quantity", "Working");
                Revise_Button_existing = 1 == getDriver().findElements(By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]")).size();
                if (Revise_Button_existing) {
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
            if (!testingBCCPs.containsKey("BCCPreleaseTA3revisiondraft")) {
                bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByDenAndBranch("Allow Substitution Indicator. Open_ Indicator", "Working");
                Revise_Button_existing = 1 == getDriver().findElements(By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]")).size();
                if (Revise_Button_existing) {
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
            if (!testingBCCPs.containsKey("BCCPreleaseTA3revisioncandidate")) {
                bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByDenAndBranch("Appointment Required Indicator. Open_ Indicator", "Working");
                Revise_Button_existing = 1 == getDriver().findElements(By.xpath("//span[contains(text(), \"Revise\")]//ancestor::button[1]")).size();
                if (Revise_Button_existing) {
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
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        NamespaceObject oagiNamespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        CreateReleasePage createReleasePage = viewEditReleasePage.createRelease();
        createReleasePage.setReleaseNumber(newReleaseNum);
        createReleasePage.setReleaseNamespace(oagiNamespace);
        createReleasePage.hitCreateButton();
        viewEditReleasePage.openPage();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();

        assertTrue(getDriver().findElements(By.xpath("//mat-card-content/div/div[2]//*[contains(text(),\"Candidate\")]")).size() > 0);
        assertEquals(0, getDriver().findElements(By.xpath("//mat-card-content/div/div[2]//*[contains(text(),\"WIP\")]")).size());
        assertEquals(0, getDriver().findElements(By.xpath("//mat-card-content/div/div[2]//*[contains(text(),\"Draft\")]")).size());

    }

    @Test
    public void test_TA_19_1_3b_case2() {
        String branch = "Working";
        Boolean Revise_Button_existing = false;
        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        {
            ReleaseObject workingRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            ACCObject ACCreleaseTA321 = coreComponentAPI.createRandomACC(devx, workingRelease, developerNamespace, "WIP");
            ACCreleaseTA321.setObjectClassTerm("ACCrelease TA321");
            coreComponentAPI.updateACC(ACCreleaseTA321);
            if (!testingACCs.containsKey("ACCreleaseTA321")) {
                testingACCs.put("ACCreleaseTA321", ACCreleaseTA321);
            } else {
                testingACCs.put("ACCreleaseTA321", ACCreleaseTA321);
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog("Working");
            ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create("Entity Identifiers Group. Details");
            ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPPanel();
            asccpPanel.setPropertyTerm("ASCCPrelease TA321wip");
            asccpPanel.setNamespace("http://www.openapplications.org/oagis/10");
            asccpPanel.setDefinition("ASCCPrelease TA321wip");
            asccpViewEditPage.hitUpdateButton();
            String url = getDriver().getCurrentUrl();
            BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
            ASCCPObject ASCCPreleaseTA321wip = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
            if (!testingASCCPs.containsKey("ASCCPreleaseTA321wip")) {
                testingASCCPs.put("ASCCPreleaseTA321wip", ASCCPreleaseTA321wip);
            } else {
                testingASCCPs.put("ASCCPreleaseTA321wip", ASCCPreleaseTA321wip);
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
            asccpViewEditPage = asccpCreateDialog.create("Entity Identifiers Group. Details");
            asccpPanel = asccpViewEditPage.getASCCPPanel();
            asccpPanel.setPropertyTerm("ASCCPrelease TA321draft");
            asccpPanel.setNamespace("http://www.openapplications.org/oagis/10");
            asccpPanel.setDefinition("ASCCPrelease TA321draft");
            asccpViewEditPage.hitUpdateButton();
            url = getDriver().getCurrentUrl();
            asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
            ASCCPObject ASCCPreleaseTA321draft = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
            asccpViewEditPage.moveToDraft();
            if (!testingASCCPs.containsKey("ASCCPreleaseTA321draft")) {
                testingASCCPs.put("ASCCPreleaseTA321draft", ASCCPreleaseTA321draft);
            } else {
                testingASCCPs.put("ASCCPreleaseTA321draft", ASCCPreleaseTA321draft);
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
            asccpViewEditPage = asccpCreateDialog.create("Entity Identifiers Group. Details");
            asccpPanel = asccpViewEditPage.getASCCPPanel();
            asccpPanel.setPropertyTerm("ASCCPrelease TA321candidate");
            asccpPanel.setNamespace("http://www.openapplications.org/oagis/10");
            asccpPanel.setDefinition("ASCCPrelease TA321candidate");
            asccpViewEditPage.hitUpdateButton();
            url = getDriver().getCurrentUrl();
            asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
            ASCCPObject ASCCPreleaseTA321candidate = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
            asccpViewEditPage.moveToDraft();
            asccpViewEditPage.moveToCandidate();
            if (!testingASCCPs.containsKey("ASCCPreleaseTA321candidate")) {
                testingASCCPs.put("ASCCPreleaseTA321candidate", ASCCPreleaseTA321candidate);
            } else {
                testingASCCPs.put("ASCCPreleaseTA321candidate", ASCCPreleaseTA321candidate);
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            BCCPCreateDialog bccpCreateDialog = viewEditCoreComponentPage.openBCCPCreateDialog(branch);
            BCCPViewEditPage bccpViewEditPage = bccpCreateDialog.create("System Environment_ Code. Type");
            BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
            bccpPanel.setPropertyTerm("BCCPreleaseTA321wip");
            bccpPanel.setNamespace("http://www.openapplications.org/oagis/10");
            bccpPanel.setDefinition("BCCPreleaseTA321wip");
            bccpViewEditPage.hitUpdateButton();
            url = getDriver().getCurrentUrl();
            BigInteger bccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
            BCCPObject BCCPreleaseTA321wip = getAPIFactory().getCoreComponentAPI().getBCCPByManifestId(bccpManifestId);

            if (!testingBCCPs.containsKey("BCCPreleaseTA321wip")) {
                testingBCCPs.put("BCCPreleaseTA321wip", BCCPreleaseTA321wip);
            } else {
                testingBCCPs.put("BCCPreleaseTA321wip", BCCPreleaseTA321wip);
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            bccpCreateDialog = viewEditCoreComponentPage.openBCCPCreateDialog(branch);
            bccpViewEditPage = bccpCreateDialog.create("System Environment_ Code. Type");
            bccpPanel.setPropertyTerm("BCCPreleaseTA321draft");
            bccpPanel.setNamespace("http://www.openapplications.org/oagis/10");
            bccpPanel.setDefinition("BCCPreleaseTA321draft");
            bccpViewEditPage.hitUpdateButton();
            url = getDriver().getCurrentUrl();
            bccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
            BCCPObject BCCPreleaseTA321draft = getAPIFactory().getCoreComponentAPI().getBCCPByManifestId(bccpManifestId);
            bccpViewEditPage.moveToDraft();

            if (!testingBCCPs.containsKey("BCCPreleaseTA321draft")) {
                testingBCCPs.put("BCCPreleaseTA321draft", BCCPreleaseTA321draft);
            } else {
                testingBCCPs.put("BCCPreleaseTA321draft", BCCPreleaseTA321draft);
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            bccpCreateDialog = viewEditCoreComponentPage.openBCCPCreateDialog(branch);
            bccpViewEditPage = bccpCreateDialog.create("System Environment_ Code. Type");
            bccpPanel.setPropertyTerm("BCCPreleaseTA321candidate");
            bccpPanel.setNamespace("http://www.openapplications.org/oagis/10");
            bccpPanel.setDefinition("BCCPreleaseTA321candidate");
            bccpViewEditPage.hitUpdateButton();
            url = getDriver().getCurrentUrl();
            bccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
            BCCPObject BCCPreleaseTA321candidate = getAPIFactory().getCoreComponentAPI().getBCCPByManifestId(bccpManifestId);
            bccpViewEditPage.moveToDraft();
            bccpViewEditPage.moveToCandidate();

            if (!testingBCCPs.containsKey("BCCPreleaseTA321candidate")) {
                testingBCCPs.put("BCCPreleaseTA321candidate", BCCPreleaseTA321candidate);
            } else {
                testingBCCPs.put("BCCPreleaseTA321candidate", BCCPreleaseTA321candidate);
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(ACCreleaseTA321.getAccManifestId());
            SelectAssociationDialog selectAssociationDialog = accViewEditPage.appendPropertyAtLast("/ACCrelease TA321. Details");
            selectAssociationDialog.selectAssociation(ASCCPreleaseTA321wip.getPropertyTerm());
            selectAssociationDialog = accViewEditPage.appendPropertyAtLast("/ACCrelease TA321. Details");
            selectAssociationDialog.selectAssociation(ASCCPreleaseTA321draft.getPropertyTerm());
            selectAssociationDialog = accViewEditPage.appendPropertyAtLast("/ACCrelease TA321. Details");
            selectAssociationDialog.selectAssociation(ASCCPreleaseTA321candidate.getPropertyTerm());
            selectAssociationDialog = accViewEditPage.appendPropertyAtLast("/ACCrelease TA321. Details");
            selectAssociationDialog.selectAssociation(BCCPreleaseTA321wip.getPropertyTerm());
            selectAssociationDialog = accViewEditPage.appendPropertyAtLast("/ACCrelease TA321. Details");
            selectAssociationDialog.selectAssociation(BCCPreleaseTA321draft.getPropertyTerm());
            selectAssociationDialog = accViewEditPage.appendPropertyAtLast("/ACCrelease TA321. Details");
            selectAssociationDialog.selectAssociation(BCCPreleaseTA321candidate.getPropertyTerm());
            accViewEditPage.moveToDraft();
            accViewEditPage.moveToCandidate();
        }
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        NamespaceObject oagiNamespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        CreateReleasePage createReleasePage = viewEditReleasePage.createRelease();
        createReleasePage.setReleaseNumber(newReleaseNum);
        createReleasePage.setReleaseNamespace(oagiNamespace);
        createReleasePage.hitCreateButton();
        viewEditReleasePage.openPage();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();

        getElementByXPath("//span[contains(text(),\"[Error] 'ASCCPrelease TA321wip. Entity Identifiers Group' is needed in the release assignment due to 'ACCrelease TA321. Details'.\")]");

        //Case 2 ACC to ASCCP
        assertEquals(1, getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] 'ASCCPrelease TA321wip. Entity Identifiers Group' is needed in the release assignment due to 'ACCrelease TA321. Details'.\")]")).size());
        assertEquals(1, getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] 'ASCCPrelease TA321draft. Entity Identifiers Group' is needed in the release assignment due to 'ACCrelease TA321. Details'.\")]")).size());

        //Case 3 ACC to BCCP
        assertEquals(1, getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] 'BCCPrelease TA321wip. Code' is needed in the release assignment due to 'ACCrelease TA321. Details'.\")]")).size());
        assertEquals(1, getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] 'BCCPrelease TA321draft. Code' is needed in the release assignment due to 'ACCrelease TA321. Details'.\")]")).size());
    }

    public WebElement getElementByXPath(String anXpath) {
        String url = getDriver().getCurrentUrl();

        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(10));
        try {
            waitForPageLoaded(url);
        } catch (Error | Exception retry) {
            getDriver().navigate().refresh();
            waitFor(Duration.ofMillis(1000));
            waitForPageLoaded(url);
        }

        try {
            waitForElementToBecomePresent(By.xpath(anXpath));
            //highlight elements
            JavascriptExecutor js = (JavascriptExecutor) getDriver();
            js.executeScript("arguments[0].setAttribute('style', arguments[1]);", getDriver().findElement(By.xpath(anXpath)), "color: red; border: 2px solid red;");
        } catch (StaleElementReferenceException sere) {
            System.out.println("stale exception prevented");
        }
        return getDriver().findElement(By.xpath(anXpath));
    }

    protected void waitForElementToBecomePresent(By by) {
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(4));
        wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public void waitForPageLoaded(String url) {
        ExpectedCondition<Boolean> expectation = new
                ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        return ((JavascriptExecutor) driver).executeScript("return document.readyState").toString().equals("complete");
                    }
                };
        try {
            //slow - change to 500
            Thread.sleep(500);
            WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(50));
            wait.until(expectation);
//            aRefresher.stopRefresher();
        } catch (Throwable error) {
            Assert.fail("Timeout waiting for Page Load Request to complete.");
        }
    }

    @Test
    public void test_TA_19_1_3b_case1() {
        String branch = "Working";
        Boolean Revise_Button_existing = false;
        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCObject ACCreleaseTA321wip, ACCreleaseTA321case1draft;
        ASCCPObject ASCCPreleaseTA321case1;
        {
            ReleaseObject workingRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            ACCreleaseTA321wip = coreComponentAPI.createRandomACC(devx, workingRelease, developerNamespace, "WIP");
            ACCreleaseTA321wip.setObjectClassTerm("ACCrelease TA321case1wip");
            coreComponentAPI.updateACC(ACCreleaseTA321wip);
            if (!testingACCs.containsKey("ACCreleaseTA321wip")) {
                testingACCs.put("ACCreleaseTA321wip", ACCreleaseTA321wip);
            } else {
                testingACCs.put("ACCreleaseTA321wip", ACCreleaseTA321wip);
            }

            ACCreleaseTA321case1draft = coreComponentAPI.createRandomACC(devx, workingRelease, developerNamespace, "Draft");
            ACCreleaseTA321case1draft.setObjectClassTerm("ACCrelease TA321case1draft");
            coreComponentAPI.updateACC(ACCreleaseTA321case1draft);
            if (!testingACCs.containsKey("ACCreleaseTA321case1draft")) {
                testingACCs.put("ACCreleaseTA321case1draft", ACCreleaseTA321case1draft);
            } else {
                testingACCs.put("ACCreleaseTA321case1draft", ACCreleaseTA321case1draft);
            }

            ACCObject ACCreleaseTA321case1candidate = coreComponentAPI.createRandomACC(devx, workingRelease, developerNamespace, "Candidate");
            ACCreleaseTA321case1candidate.setObjectClassTerm("ACCrelease TA321case1candidate");
            coreComponentAPI.updateACC(ACCreleaseTA321case1candidate);
            if (!testingACCs.containsKey("ACCreleaseTA321case1candidate")) {
                testingACCs.put("ACCreleaseTA321case1candidate", ACCreleaseTA321case1candidate);
            } else {
                testingACCs.put("ACCreleaseTA321case1candidate", ACCreleaseTA321case1candidate);
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
            ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create("ACCrelease TA321case1wip. Details");
            ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPPanel();
            asccpPanel.setPropertyTerm("ASCCPrelease TA321case1");
            asccpPanel.setNamespace("http://www.openapplications.org/oagis/10");
            asccpPanel.setDefinition("ASCCPrelease TA321case1");
            asccpViewEditPage.hitUpdateButton();
            String url = getDriver().getCurrentUrl();
            BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
            ASCCPreleaseTA321case1 = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
            if (!testingASCCPs.containsKey("ASCCPreleaseTA321case1")) {
                testingASCCPs.put("ASCCPreleaseTA321case1", ASCCPreleaseTA321case1);
            } else {
                testingASCCPs.put("ASCCPreleaseTA321case1", ASCCPreleaseTA321case1);
            }
            asccpViewEditPage.moveToDraft();
            asccpViewEditPage.moveToCandidate();
        }
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        NamespaceObject oagiNamespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        CreateReleasePage createReleasePage = viewEditReleasePage.createRelease();
        createReleasePage.setReleaseNumber(newReleaseNum);
        createReleasePage.setReleaseNamespace(oagiNamespace);
        createReleasePage.hitCreateButton();
        viewEditReleasePage.openPage();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();
        //Case1 when acc wip
        getElementByXPath("//span[contains(text(),\"[Error] 'ACCrelease TA321case1wip. Details' is needed in the release assignment due to\")]");

        //Case1 when acc is moved to draft
        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(2000));
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(ASCCPreleaseTA321case1.getAsccpManifestId());
        asccpViewEditPage.backToWIP();
        ASCCPChangeACCDialog asccpChangeACCDialog = asccpViewEditPage.openChangeACCDialog("/" + ASCCPreleaseTA321case1.getPropertyTerm());
        asccpChangeACCDialog.hitUpdateButton("ACCrelease TA321case1draft. Details");
        asccpViewEditPage.moveToDraft();
        asccpViewEditPage.moveToCandidate();

        viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        createReleasePage = viewEditReleasePage.createRelease();
        createReleasePage.setReleaseNumber(newReleaseNum);
        createReleasePage.setReleaseNamespace(oagiNamespace);
        createReleasePage.hitCreateButton();
        viewEditReleasePage.openPage();
        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Initialized");
        releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();
        //Case1 when acc wip
        getElementByXPath("//span[contains(text(),\"[Error] 'ACCrelease TA321case1draft. Details' is needed in the release assignment due to\")]");
    }

    @Test
    public void test_TA_19_1_3b_case7() {
        String branch = "Working";
        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCObject ACCreleaseTA321case7parent, ACCreleaseTA321case7base;
        {
            ReleaseObject workingRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            ACCreleaseTA321case7base = coreComponentAPI.createRandomACC(devx, workingRelease, developerNamespace, "WIP");
            ACCreleaseTA321case7base.setObjectClassTerm("ACCrelease TA321case7base");
            coreComponentAPI.updateACC(ACCreleaseTA321case7base);
            if (!testingACCs.containsKey("ACCreleaseTA321case7base")) {
                testingACCs.put("ACCreleaseTA321case7base", ACCreleaseTA321case7base);
            } else {
                testingACCs.put("ACCreleaseTA321case7base", ACCreleaseTA321case7base);
            }

            ACCreleaseTA321case7parent = coreComponentAPI.createRandomACC(devx, workingRelease, developerNamespace, "WIP");
            ACCreleaseTA321case7parent.setObjectClassTerm("ACCrelease TA321case7parent");
            coreComponentAPI.updateBasedACC(ACCreleaseTA321case7parent, ACCreleaseTA321case7base);
            coreComponentAPI.updateACC(ACCreleaseTA321case7parent);
            if (!testingACCs.containsKey("ACCreleaseTA321case7parent")) {
                testingACCs.put("ACCreleaseTA321case7parent", ACCreleaseTA321case7parent);
            } else {
                testingACCs.put("ACCreleaseTA321case7parent", ACCreleaseTA321case7parent);
            }
            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(ACCreleaseTA321case7parent.getAccManifestId());
            accViewEditPage.moveToDraft();
            accViewEditPage.moveToCandidate();
        }
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        NamespaceObject oagiNamespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        CreateReleasePage createReleasePage = viewEditReleasePage.createRelease();
        createReleasePage.setReleaseNumber(newReleaseNum);
        createReleasePage.setReleaseNamespace(oagiNamespace);
        createReleasePage.hitCreateButton();
        viewEditReleasePage.openPage();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();
        //Case7 when acc is in wip
        getElementByXPath("//span[contains(text(),\"[Error] 'ACCrelease TA321case7base. Details' is needed in the release assignment\")]");

        //Case7 when acc base is moved to draft
        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(2000));
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(ACCreleaseTA321case7parent.getAccManifestId());
        accViewEditPage.backToWIP();
        accViewEditPage.deleteBaseACC("/ACCrelease TA321case7parent. Details/ACCrelease TA321case7base. Details");

        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(2000));
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(ACCreleaseTA321case7base.getAccManifestId());
        accViewEditPage.moveToDraft();

        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(2000));
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(ACCreleaseTA321case7parent.getAccManifestId());
        ACCSetBaseACCDialog accSetBaseACCDialog = accViewEditPage.setBaseACC("/ACCrelease TA321case7parent. Details");
        accSetBaseACCDialog.hitApplyButton("ACCrelease TA321case7base. Details");
        accViewEditPage.moveToDraft();
        accViewEditPage.moveToCandidate();

        viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        createReleasePage = viewEditReleasePage.createRelease();
        createReleasePage.setReleaseNumber(newReleaseNum);
        createReleasePage.setReleaseNamespace(oagiNamespace);
        createReleasePage.hitCreateButton();
        viewEditReleasePage.openPage();
        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Initialized");
        releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();

        getElementByXPath("//span[contains(text(),\"[Error] 'ACCrelease TA321case7base. Details' is needed in the release assignment\")]");
    }

    @Test
    public void test_TA_19_1_3d() {
        String branch = "Working";
        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCObject ACCreleaseTA321case7parent, ACCreleaseTA321case7base;
        {
            ReleaseObject workingRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            ACCreleaseTA321case7base = coreComponentAPI.createRandomACC(devx, workingRelease, developerNamespace, "WIP");
            ACCreleaseTA321case7base.setObjectClassTerm("ACCrelease TA321case7base");
            coreComponentAPI.updateACC(ACCreleaseTA321case7base);
            if (!testingACCs.containsKey("ACCreleaseTA321case7base")) {
                testingACCs.put("ACCreleaseTA321case7base", ACCreleaseTA321case7base);
            } else {
                testingACCs.put("ACCreleaseTA321case7base", ACCreleaseTA321case7base);
            }

            ACCreleaseTA321case7parent = coreComponentAPI.createRandomACC(devx, workingRelease, developerNamespace, "WIP");
            ACCreleaseTA321case7parent.setObjectClassTerm("ACCrelease TA321case7parent");
            coreComponentAPI.updateBasedACC(ACCreleaseTA321case7parent, ACCreleaseTA321case7base);
            coreComponentAPI.updateACC(ACCreleaseTA321case7parent);
            if (!testingACCs.containsKey("ACCreleaseTA321case7parent")) {
                testingACCs.put("ACCreleaseTA321case7parent", ACCreleaseTA321case7parent);
            } else {
                testingACCs.put("ACCreleaseTA321case7parent", ACCreleaseTA321case7parent);
            }
            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(2000));
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(ACCreleaseTA321case7parent.getAccManifestId());
            accViewEditPage.createOAGiExtensionComponent("/ACCrelease TA321case7parent. Details");
            click(elementToBeClickable(getDriver(), By.xpath(
                    "//mat-dialog-container//span[contains(text(), \"Create\")]//ancestor::button[1]")));
            accViewEditPage.moveToDraft();
            accViewEditPage.moveToCandidate();
        }
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        NamespaceObject oagiNamespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        CreateReleasePage createReleasePage = viewEditReleasePage.createRelease();
        createReleasePage.setReleaseNumber(newReleaseNum);
        createReleasePage.setReleaseNamespace(oagiNamespace);
        createReleasePage.hitCreateButton();
        viewEditReleasePage.openPage();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();
        //extension in WIP status
        getElementByXPath("//span[contains(text(),\"[Error] 'Extension. ACCrelease TA321case7parent Extension' is needed in the release\")]");
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] 'Extension. ACCrelease TA321case7parent Extension' is needed in the release\")]")).size() >= 1);


        //ACC ext is moved to draft
        By MOVE_TO_DRAFT_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Move to Draft\")]//ancestor::button[1]");
        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(3000L));
        click(viewEditCoreComponentPage.getTypeSelectField());
        List<WebElement> options = getDriver().findElements(By.cssSelector("mat-option"));
        options = getDriver().findElements(By.cssSelector("mat-option"));
        for (String ccState : Arrays.asList("ACC", "BCCP", "CDT", "BDT")) {
            List<WebElement> result = options.stream().filter(e -> ccState.equals(getText(e))).collect(Collectors.toList());
            result.get(0).click();
        }
        escape(getDriver());
        viewEditCoreComponentPage.setOwner(devx.getLoginId());
        escape(getDriver());
        viewEditCoreComponentPage.setDEN("Extension. ACCrelease TA321case7parent Extension");
        viewEditCoreComponentPage.hitSearchButton();
        WebElement tr = viewEditCoreComponentPage.getTableRecordAtIndex(1);
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPage(tr);
        if (asccpViewEditPage.getMoveToDraft(true).isEnabled()){
            asccpViewEditPage.moveToDraft();
        }
        viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        createReleasePage = viewEditReleasePage.createRelease();
        createReleasePage.setReleaseNumber(newReleaseNum);
        createReleasePage.setReleaseNamespace(oagiNamespace);
        createReleasePage.hitCreateButton();
        viewEditReleasePage.openPage();
        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Initialized");
        releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();

        //ACC extension in Draft
        getElementByXPath("//span[contains(text(),\"[Error] 'Extension. ACCrelease TA321case7parent Extension' is needed in the release\")]");
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] 'Extension. ACCrelease TA321case7parent Extension' is needed in the release\")]")).size() >= 1);

        //ACC ext is moved to Candidate
        By MOVE_TO_CANDIDATE_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Move to Candidate\")]//ancestor::button[1]");
        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(5000));
        click(viewEditCoreComponentPage.getTypeSelectField());
        options = getDriver().findElements(By.cssSelector("mat-option"));
        for (String ccState : Arrays.asList("ACC", "BCCP", "CDT", "BDT")) {
            List<WebElement> result = options.stream().filter(e -> ccState.equals(getText(e))).collect(Collectors.toList());
            result.get(0).click();
        }
        escape(getDriver());
        viewEditCoreComponentPage.setOwner(devx.getLoginId());
        escape(getDriver());
        viewEditCoreComponentPage.setState("Draft");
        escape(getDriver());
        viewEditCoreComponentPage.setDEN("Extension. ACCrelease TA321case7parent Extension");
        viewEditCoreComponentPage.hitSearchButton();
        tr = viewEditCoreComponentPage.getTableRecordAtIndex(1);
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPage(tr);
        if (asccpViewEditPage.getMoveToCandidate(true).isEnabled()){
            asccpViewEditPage.moveToCandidate();
        }
        viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        createReleasePage = viewEditReleasePage.createRelease();
        createReleasePage.setReleaseNumber(newReleaseNum);
        createReleasePage.setReleaseNamespace(oagiNamespace);
        createReleasePage.hitCreateButton();
        viewEditReleasePage.openPage();
        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Initialized");
        releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();

        //ACC extension in Candidate but the ASCCP in WIP
        getElementByXPath("//span[contains(text(),\"[Error] 'ACCrelease TA321case7parent Extension. Details' is needed in the release assignment\")]");
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] 'ACCrelease TA321case7parent Extension. Details' is needed in the release assignment\")]")).size() >= 1);

        //ACC extension in Candidate but the ASCCP in draft
        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(5000L));
        click(viewEditCoreComponentPage.getTypeSelectField());
        options = getDriver().findElements(By.cssSelector("mat-option"));
        for (String ccState : Arrays.asList("ASCCP", "BCCP", "CDT", "BDT")) {
            List<WebElement> result = options.stream().filter(e -> ccState.equals(getText(e))).collect(Collectors.toList());
            result.get(0).click();
        }
        escape(getDriver());
        viewEditCoreComponentPage.setOwner(devx.getLoginId());
        escape(getDriver());
        viewEditCoreComponentPage.setDEN("ACCrelease TA321case7parent Extension. Details");
        viewEditCoreComponentPage.hitSearchButton();
        tr = viewEditCoreComponentPage.getTableRecordAtIndex(1);
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPage(tr);
        if (accViewEditPage.getMoveToDraft(true).isEnabled()){
            accViewEditPage.moveToDraft();
        }
        viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        createReleasePage = viewEditReleasePage.createRelease();
        createReleasePage.setReleaseNumber(newReleaseNum);
        createReleasePage.setReleaseNamespace(oagiNamespace);
        createReleasePage.hitCreateButton();
        viewEditReleasePage.openPage();
        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Initialized");
        releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();

        //ACC extension in Candidate but the ASCCP in WIP
        getElementByXPath("//span[contains(text(),\"[Error] 'ACCrelease TA321case7parent Extension. Details' is needed in the release assignment\")]");
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] 'ACCrelease TA321case7parent Extension. Details' is needed in the release assignment\")]")).size() >= 1);
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
        private final AppUserObject appUser;
        private List<String> states = new ArrayList<>();
        private final HashMap<String, ACCObject> stateACCs = new HashMap<>();
        private final HashMap<String, ASCCPObject> stateASCCPs = new HashMap<>();
        private final HashMap<String, BCCPObject> stateBCCPs = new HashMap<>();

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
