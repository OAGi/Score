package org.oagi.score.e2e.TS_19_ReleaseManagement;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.CreateBIEForSelectTopLevelConceptPage;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.oagi.score.e2e.page.core_component.*;
import org.oagi.score.e2e.page.release.CreateReleasePage;
import org.oagi.score.e2e.page.release.EditReleasePage;
import org.oagi.score.e2e.page.release.ReleaseAssignmentPage;
import org.oagi.score.e2e.page.release.ViewEditReleasePage;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.math.BigInteger;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.SAME_THREAD)
public class TC_19_1_ReleaseManagement extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();
    AppUserObject devx = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
    NamespaceObject developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(devx);
    String existingReleaseNum = null;
    String newReleaseNum = String.valueOf((RandomUtils.nextInt(20230716, 20231231)));
    Map<String, ACCObject> testingACCs = new HashMap<>();
    Map<String, ASCCPObject> testingASCCPs = new HashMap<>();
    Map<String, BCCPObject> testingBCCPs = new HashMap<>();

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
        String existingDraftRelease = null;
        ReleaseObject newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        ReleaseObject existingDraftReleaseObj;
        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        viewEditReleasePage.setState("Draft");
        escape(getDriver());
        viewEditReleasePage.hitSearchButton();
        int resultRows = getDriver().findElements(By.xpath("//table/tbody/tr")).size();
        if (resultRows > 0) {
            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPage(tr);
            assertTrue(editReleasePage.isOpened());
            existingDraftRelease = getText(editReleasePage.getReleaseNumberField());
            if (existingDraftRelease != null) {
                editReleasePage.backToInitialized();
                do {
                    existingDraftReleaseObj = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(existingDraftRelease);
                } while (!existingDraftReleaseObj.getState().equals("Initialized"));
            }
        }
        Boolean Revise_Button_existing = false;
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        waitFor(Duration.ofMillis(5000));
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
            waitFor(Duration.ofMillis(5000));
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
            waitFor(Duration.ofMillis(5000));
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
            waitFor(Duration.ofMillis(5000));
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
            waitFor(Duration.ofMillis(5000));
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
            waitFor(Duration.ofMillis(5000));
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
            waitFor(Duration.ofMillis(5000));
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
            waitFor(Duration.ofMillis(5000));
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
            waitFor(Duration.ofMillis(5000));
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
            waitFor(Duration.ofMillis(5000));
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
            waitFor(Duration.ofMillis(5000));
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
            waitFor(Duration.ofMillis(5000));
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
            waitFor(Duration.ofMillis(5000));
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
            waitFor(Duration.ofMillis(5000));
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
        viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
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
    public void test_TA_19_1_3c_case2_and_case3() {
        String branch = "Working";
        String existingDraftRelease = null;
        ReleaseObject newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        ReleaseObject existingDraftReleaseObj;
        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        viewEditReleasePage.setState("Draft");
        escape(getDriver());
        viewEditReleasePage.hitSearchButton();
        int resultRows = getDriver().findElements(By.xpath("//table/tbody/tr")).size();
        if (resultRows > 0) {
            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPage(tr);
            assertTrue(editReleasePage.isOpened());
            existingDraftRelease = getText(editReleasePage.getReleaseNumberField());
            if (existingDraftRelease != null) {
                editReleasePage.backToInitialized();
                do {
                    existingDraftReleaseObj = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(existingDraftRelease);
                } while (!existingDraftReleaseObj.getState().equals("Initialized"));
            }
        }
        Boolean Revise_Button_existing = false;
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
            waitFor(Duration.ofMillis(5000));
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
            waitFor(Duration.ofMillis(5000));
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
            waitFor(Duration.ofMillis(5000));
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
            waitFor(Duration.ofMillis(5000));
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
            waitFor(Duration.ofMillis(5000));
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
            waitFor(Duration.ofMillis(5000));
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
            waitFor(Duration.ofMillis(5000));
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
        viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
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
        waitFor(Duration.ofMillis(8000L));

        //Case 2 ACC to ASCCP
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] 'ASCCPrelease TA321wip. Entity Identifiers Group' is needed in the release assignment due to 'ACCrelease TA321. Details'.\")]")).size() >= 1);
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] 'ASCCPrelease TA321draft. Entity Identifiers Group' is needed in the release assignment due to 'ACCrelease TA321. Details'.\")]")).size() >= 1);

        //Case 3 ACC to BCCP
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] 'BCCPrelease TA321wip. Code' is needed in the release assignment due to 'ACCrelease TA321. Details'.\")]")).size() >= 1);
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] 'BCCPrelease TA321draft. Code' is needed in the release assignment due to 'ACCrelease TA321. Details'.\")]")).size() >= 1);
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
    public void test_TA_19_1_3c_case1() {
        String branch = "Working";
        String existingDraftRelease = null;
        ReleaseObject newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        ReleaseObject existingDraftReleaseObj;
        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        viewEditReleasePage.setState("Draft");
        escape(getDriver());
        viewEditReleasePage.hitSearchButton();
        int resultRows = getDriver().findElements(By.xpath("//table/tbody/tr")).size();
        if (resultRows > 0) {
            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPage(tr);
            assertTrue(editReleasePage.isOpened());
            existingDraftRelease = getText(editReleasePage.getReleaseNumberField());
            if (existingDraftRelease != null) {
                editReleasePage.backToInitialized();
                do {
                    existingDraftReleaseObj = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(existingDraftRelease);
                } while (!existingDraftReleaseObj.getState().equals("Initialized"));
            }
        }
        Boolean Revise_Button_existing = false;
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        waitFor(Duration.ofMillis(5000));
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
            waitFor(Duration.ofMillis(5000));
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
        viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
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
        waitFor(Duration.ofMillis(8000L));
        //Case1 when acc wip
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] 'ACCrelease TA321case1wip. Details' is needed in the release assignment due to\")]")).size() >= 1);
        //Case1 when acc is moved to draft
        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(5000));
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
        waitFor(Duration.ofMillis(8000L));
        //Case1 when acc in draft state
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] 'ACCrelease TA321case1draft. Details' is needed in the release assignment due to\")]")).size() >= 1);
    }

    @Test
    public void test_TA_19_1_3c_case7() {
        String branch = "Working";
        String existingDraftRelease = null;
        ReleaseObject newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        ReleaseObject existingDraftReleaseObj;
        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        viewEditReleasePage.setState("Draft");
        escape(getDriver());
        viewEditReleasePage.hitSearchButton();
        int resultRows = getDriver().findElements(By.xpath("//table/tbody/tr")).size();
        if (resultRows > 0) {
            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPage(tr);
            assertTrue(editReleasePage.isOpened());
            existingDraftRelease = getText(editReleasePage.getReleaseNumberField());
            if (existingDraftRelease != null) {
                editReleasePage.backToInitialized();
                do {
                    existingDraftReleaseObj = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(existingDraftRelease);
                } while (!existingDraftReleaseObj.getState().equals("Initialized"));
            }
        }
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
        viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
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
        waitFor(Duration.ofMillis(8000L));
        //Case7 when acc is in wip
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] 'ACCrelease TA321case7base. Details' is needed in the release assignment\")]")).size() >= 1);

        //Case7 when acc base is moved to draft
        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(5000));
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(ACCreleaseTA321case7parent.getAccManifestId());
        accViewEditPage.backToWIP();
        accViewEditPage.deleteBaseACC("/ACCrelease TA321case7parent. Details/ACCrelease TA321case7base. Details");

        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(5000));
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(ACCreleaseTA321case7base.getAccManifestId());
        accViewEditPage.moveToDraft();

        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(5000));
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
        waitFor(Duration.ofMillis(9000));
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] 'ACCrelease TA321case7base. Details' is needed in the release assignment\")]")).size() >= 1);
    }

    @Ignore
    public void test_TA_19_1_3c_case8_and_case9_and_case10_and_test_TA_19_1_3h() {
        //replaced by field is not implemented yet
    }

    @Test
    public void test_TA_19_1_3d_and_1_3e_and_1_3f_and_1_3g() {
        String branch = "Working";
        String existingDraftRelease = null;
        ReleaseObject newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        ReleaseObject existingDraftReleaseObj;
        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        viewEditReleasePage.setState("Draft");
        escape(getDriver());
        viewEditReleasePage.hitSearchButton();
        int resultRows = getDriver().findElements(By.xpath("//table/tbody/tr")).size();
        if (resultRows > 0) {
            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPage(tr);
            assertTrue(editReleasePage.isOpened());
            existingDraftRelease = getText(editReleasePage.getReleaseNumberField());
            if (existingDraftRelease != null) {
                editReleasePage.backToInitialized();
                do {
                    existingDraftReleaseObj = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(existingDraftRelease);
                } while (!existingDraftReleaseObj.getState().equals("Initialized"));
            }
        }
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
        viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
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
        waitFor(Duration.ofMillis(8000));
        //extension in WIP status
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] 'Extension. ACCrelease TA321case7parent Extension' is needed in the release\")]")).size() >= 1);

        //ACC ext is moved to draft
        By MOVE_TO_DRAFT_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Move to Draft\")]//ancestor::button[1]");
        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(5000L));
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
        if (asccpViewEditPage.getMoveToDraft(true).isEnabled()) {
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
        waitFor(Duration.ofMillis(8000));
        //ACC extension in Draft
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
        if (asccpViewEditPage.getMoveToCandidate(true).isEnabled()) {
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
        waitFor(Duration.ofMillis(8000));

        //ACC extension in Candidate but the ASCCP in WIP
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
        if (accViewEditPage.getMoveToDraft(true).isEnabled()) {
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
        waitFor(Duration.ofMillis(8000));
        //ACC extension in Candidate but the ASCCP in WIP
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] 'ACCrelease TA321case7parent Extension. Details' is needed in the release assignment\")]")).size() >= 1);
    }

    @Ignore
    public void test_TA_19_1_3i() {
        //Validate non-reusable ASCCP is ensured in UI
    }

    @Test
    public void test_TA_19_1_3j() {
        String branch = "Working";
        String existingDraftRelease = null;
        ReleaseObject newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        ReleaseObject existingDraftReleaseObj;
        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        viewEditReleasePage.setState("Draft");
        escape(getDriver());
        viewEditReleasePage.hitSearchButton();
        int resultRows = getDriver().findElements(By.xpath("//table/tbody/tr")).size();
        if (resultRows > 0) {
            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPage(tr);
            assertTrue(editReleasePage.isOpened());
            existingDraftRelease = getText(editReleasePage.getReleaseNumberField());
            if (existingDraftRelease != null) {
                editReleasePage.backToInitialized();
                do {
                    existingDraftReleaseObj = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(existingDraftRelease);
                } while (!existingDraftReleaseObj.getState().equals("Initialized"));
            }
        }
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
            waitFor(Duration.ofMillis(5000));
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(ACCreleaseTA321case7parent.getAccManifestId());
            accViewEditPage.moveToDraft();
            accViewEditPage.moveToCandidate();

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(5000));
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(ACCreleaseTA321case7base.getAccManifestId());
            accViewEditPage.moveToDraft();
            accViewEditPage.moveToCandidate();
        }
        viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
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
        waitFor(Duration.ofMillis(8000));
        releaseAssignmentPage.hitCreateButton();
        newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        do {
            newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        } while (!newDraftRelease.getState().equals("Draft"));
        viewEditReleasePage.openPage();
        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Draft");
        assertTrue(editReleasePage.isOpened());

        //move back to initialize to Cancel
        editReleasePage.backToInitialized();
        do {
            newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        } while (!newDraftRelease.getState().equals("Initialized"));

        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Initialized");
        assertTrue(editReleasePage.isOpened());
    }

    @Test
    public void test_TA_19_1_3k() {
        String branch = "Working";
        String existingDraftRelease = null;
        ReleaseObject existingDraftReleaseObj;
        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        viewEditReleasePage.setState("Draft");
        escape(getDriver());
        viewEditReleasePage.hitSearchButton();
        int resultRows = getDriver().findElements(By.xpath("//table/tbody/tr")).size();
        if (resultRows > 0) {
            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPage(tr);
            assertTrue(editReleasePage.isOpened());
            existingDraftRelease = getText(editReleasePage.getReleaseNumberField());
            if (existingDraftRelease != null) {
                editReleasePage.backToInitialized();
                do {
                    existingDraftReleaseObj = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(existingDraftRelease);
                } while (!existingDraftReleaseObj.getState().equals("Initialized"));
            }
        }
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
            waitFor(Duration.ofMillis(5000));
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(ACCreleaseTA321case7parent.getAccManifestId());
            accViewEditPage.moveToDraft();
            accViewEditPage.moveToCandidate();

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(5000));
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(ACCreleaseTA321case7base.getAccManifestId());
            accViewEditPage.moveToDraft();
            accViewEditPage.moveToCandidate();
        }
        viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
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
        waitFor(Duration.ofMillis(8000));
        releaseAssignmentPage.hitCreateButton();
        ReleaseObject newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        do {
            newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        } while (!newDraftRelease.getState().equals("Draft"));
        viewEditReleasePage.openPage();
        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Draft");
        assertTrue(editReleasePage.isOpened());
        editReleasePage.publish();
        do {
            newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        } while (!newDraftRelease.getState().equals("Published"));

        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Published");
        assertTrue(editReleasePage.isOpened());
    }

    @Test
    public void test_TA_19_1_4() {
        String branch = "Working";
        String existingDraftRelease = null;
        ReleaseObject newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        ReleaseObject existingDraftReleaseObj;
        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        viewEditReleasePage.setState("Draft");
        escape(getDriver());
        viewEditReleasePage.hitSearchButton();
        int resultRows = getDriver().findElements(By.xpath("//table/tbody/tr")).size();
        if (resultRows > 0) {
            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPage(tr);
            assertTrue(editReleasePage.isOpened());
            existingDraftRelease = getText(editReleasePage.getReleaseNumberField());
            if (existingDraftRelease != null) {
                editReleasePage.backToInitialized();
                do {
                    existingDraftReleaseObj = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(existingDraftRelease);
                } while (!existingDraftReleaseObj.getState().equals("Initialized"));
            }
        }
        viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        CreateReleasePage createReleasePage = viewEditReleasePage.createRelease();
        String newReleaseNum = String.valueOf((RandomUtils.nextInt(20230716, 20231231)));
        createReleasePage.setReleaseNumber(newReleaseNum);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        createReleasePage.setReleaseNamespace(namespace);
        createReleasePage.setReleaseNote("A release note");
        createReleasePage.setReleaseLicense("A release license");
        createReleasePage.hitCreateButton();

        viewEditReleasePage.openPage();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Initialized");
        assertEquals(newReleaseNum, getText(editReleasePage.getReleaseNumberField()));

        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        waitFor(Duration.ofMillis(5000));
        click(viewEditCoreComponentPage.getBranchSelectField());
        waitFor(ofSeconds(2L));
        assertEquals(0, getDriver().findElements(By.xpath("//*[contains(text(),\"" + newReleaseNum + "\")]//ancestor::mat-option[1]/span")).size());
        escape(getDriver());

        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(devx);
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
        click(createBIEForSelectTopLevelConceptPage.getBranchSelectField());
        waitFor(ofSeconds(2L));
        assertEquals(0, getDriver().findElements(By.xpath("//*[contains(text(),\"" + newReleaseNum + "\")]//ancestor::mat-option[1]/span")).size());
        escape(getDriver());
    }

    @Ignore
    public void test_TA_19_1_5() {
        //cannot be checked needs two phase commit
    }

    @Test
    public void test_TA_19_1_6_and_TA_19_1_7_and_TA_19_1_8() {
        String branch = "Working";
        String existingDraftRelease = null;
        ReleaseObject newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        ReleaseObject existingDraftReleaseObj;
        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        viewEditReleasePage.setState("Draft");
        escape(getDriver());
        viewEditReleasePage.hitSearchButton();
        int resultRows = getDriver().findElements(By.xpath("//table/tbody/tr")).size();
        if (resultRows > 0) {
            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPage(tr);
            assertTrue(editReleasePage.isOpened());
            existingDraftRelease = getText(editReleasePage.getReleaseNumberField());
            if (existingDraftRelease != null) {
                editReleasePage.backToInitialized();
                do {
                    existingDraftReleaseObj = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(existingDraftRelease);
                } while (!existingDraftReleaseObj.getState().equals("Initialized"));
            }

            editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(existingReleaseNum,
                    "Initialized");
            assertTrue(editReleasePage.isOpened());
        }

        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCObject ACCreleasedevxcandidate;
        ASCCPObject ASCCPreleasedevxcandidate;
        BCCPObject BCCPreleasedevxcandidate;
        {
            ReleaseObject workingRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            ACCreleasedevxcandidate = coreComponentAPI.createRandomACC(devx, workingRelease, developerNamespace, "WIP");
            ACCreleasedevxcandidate.setObjectClassTerm("ACCreleasedevxcandidate");
            coreComponentAPI.updateACC(ACCreleasedevxcandidate);
            if (!testingACCs.containsKey("ACCreleasedevxcandidate")) {
                testingACCs.put("ACCreleasedevxcandidate", ACCreleasedevxcandidate);
            } else {
                testingACCs.put("ACCreleasedevxcandidate", ACCreleasedevxcandidate);
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(5000));
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(ACCreleasedevxcandidate.getAccManifestId());
            accViewEditPage.moveToDraft();
            accViewEditPage.moveToCandidate();

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(5000));
            ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
            ASCCPViewEditPage
                    asccpViewEditPage = asccpCreateDialog.create("ACCreleasedevxcandidate. Details");
            ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPPanel();
            asccpPanel.setPropertyTerm("ASCCPreleasedevxcandidate");
            asccpPanel.setNamespace("http://www.openapplications.org/oagis/10");
            asccpPanel.setDefinition("ASCCPreleasedevxcandidate");
            asccpViewEditPage.hitUpdateButton();
            String url = getDriver().getCurrentUrl();
            BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
            ASCCPreleasedevxcandidate = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
            if (!testingASCCPs.containsKey("ASCCPreleasedevxcandidate")) {
                testingASCCPs.put("ASCCPreleasedevxcandidate", ASCCPreleasedevxcandidate);
            } else {
                testingASCCPs.put("ASCCPreleasedevxcandidate", ASCCPreleasedevxcandidate);
            }
            asccpViewEditPage.moveToDraft();
            asccpViewEditPage.moveToCandidate();

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(5000));
            BCCPCreateDialog bccpCreateDialog = viewEditCoreComponentPage.openBCCPCreateDialog(branch);
            BCCPViewEditPage bccpViewEditPage = bccpCreateDialog.create("System Environment_ Code. Type");
            BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
            bccpPanel.setPropertyTerm("BCCPreleasedevxcandidate");
            bccpPanel.setNamespace("http://www.openapplications.org/oagis/10");
            bccpPanel.setDefinition("BCCPreleasedevxcandidate");
            bccpViewEditPage.hitUpdateButton();
            url = getDriver().getCurrentUrl();
            BigInteger bccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
            BCCPreleasedevxcandidate = getAPIFactory().getCoreComponentAPI().getBCCPByManifestId(bccpManifestId);

            if (!testingBCCPs.containsKey("BCCPreleasedevxcandidate")) {
                testingBCCPs.put("BCCPreleasedevxcandidate", BCCPreleasedevxcandidate);
            } else {
                testingBCCPs.put("BCCPreleasedevxcandidate", BCCPreleasedevxcandidate);
            }
            bccpViewEditPage.moveToDraft();
            bccpViewEditPage.moveToCandidate();
        }
        viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
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
        waitFor(Duration.ofMillis(8000));
        releaseAssignmentPage.hitCreateButton();

        do {
            newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        } while (!newDraftRelease.getState().equals("Draft"));
        viewEditReleasePage.openPage();
        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Draft");
        assertTrue(editReleasePage.isOpened());

        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(5000));
        viewEditCoreComponentPage.setBranch(newReleaseNum);
        escape(getDriver());
        waitFor(Duration.ofMillis(2000));
        viewEditCoreComponentPage.setOwner(devx.getLoginId());
        escape(getDriver());
        viewEditCoreComponentPage.hitSearchButton();

        assertTrue(getDriver().findElements(By.xpath("//*[contains(text(),\"" + ACCreleasedevxcandidate.getObjectClassTerm() + "\")]//ancestor::tr")).size() >= 1);
        assertTrue(getDriver().findElements(By.xpath("//*[contains(text(),\"" + ASCCPreleasedevxcandidate.getPropertyTerm() + "\")]//ancestor::tr")).size() >= 1);
        assertTrue(getDriver().findElements(By.xpath("//*[contains(text(),\"" + BCCPreleasedevxcandidate.getPropertyTerm() + "\")]//ancestor::tr")).size() >= 1);

        viewEditCoreComponentPage.setDEN("ACCreleasedevxcandidate. Details");
        viewEditCoreComponentPage.hitSearchButton();
        WebElement tr = viewEditCoreComponentPage.getTableRecordAtIndex(1);
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPage(tr);
        WebElement accNode = accViewEditPage.getNodeByPath("/ACCreleasedevxcandidate. Details");
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertDisabled(accPanel.getDENField());
        assertDisabled(accPanel.getObjectClassTermField());

        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(devx);
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
        click(createBIEForSelectTopLevelConceptPage.getBranchSelectField());
        waitFor(ofSeconds(2L));
        assertEquals(0, getDriver().findElements(By.xpath("//*[contains(text(),\"" + newReleaseNum + "\")]//ancestor::mat-option[1]/span")).size());
        escape(getDriver());

        //move back to initialize to Cancel
        viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Draft");
        editReleasePage.backToInitialized();
        do {
            newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        } while (!newDraftRelease.getState().equals("Initialized"));

        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Initialized");
        assertTrue(editReleasePage.isOpened());

        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(5000));
        viewEditCoreComponentPage.setOwner(devx.getLoginId());
        escape(getDriver());
        viewEditCoreComponentPage.hitSearchButton();
        assertTrue(getDriver().findElements(By.xpath("//*[contains(text(),\"" + ACCreleasedevxcandidate.getObjectClassTerm() + "\")]//ancestor::tr")).size() >= 1);
        viewEditCoreComponentPage.setDEN("ACCreleasedevxcandidate. Details");
        viewEditCoreComponentPage.hitSearchButton();
        tr = viewEditCoreComponentPage.getTableRecordAtIndex(1);
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPage(tr);
        accNode = accViewEditPage.getNodeByPath("/ACCreleasedevxcandidate. Details");
        accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("Candidate", getText(accPanel.getStateField()));
    }

    @Test
    public void test_TA_19_1_9_and_TA_19_1_10() {
        String branch = "Working";
        String existingDraftRelease = null;
        ReleaseObject newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        ReleaseObject existingDraftReleaseObj;
        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        viewEditReleasePage.setState("Draft");
        escape(getDriver());
        viewEditReleasePage.hitSearchButton();
        int resultRows = getDriver().findElements(By.xpath("//table/tbody/tr")).size();
        if (resultRows > 0) {
            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPage(tr);
            assertTrue(editReleasePage.isOpened());
            existingDraftRelease = getText(editReleasePage.getReleaseNumberField());
            if (existingDraftRelease != null) {
                editReleasePage.backToInitialized();
                do {
                    existingDraftReleaseObj = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(existingDraftRelease);
                } while (!existingDraftReleaseObj.getState().equals("Initialized"));
            }
        }

        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCObject ACCreleasedevxcandidate;
        ASCCPObject ASCCPreleasedevxcandidate;
        BCCPObject BCCPreleasedevxcandidate;
        {
            ReleaseObject workingRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            ACCreleasedevxcandidate = coreComponentAPI.createRandomACC(devx, workingRelease, developerNamespace, "WIP");
            ACCreleasedevxcandidate.setObjectClassTerm("ACCreleasedevxcandidate");
            coreComponentAPI.updateACC(ACCreleasedevxcandidate);
            if (!testingACCs.containsKey("ACCreleasedevxcandidate")) {
                testingACCs.put("ACCreleasedevxcandidate", ACCreleasedevxcandidate);
            } else {
                testingACCs.put("ACCreleasedevxcandidate", ACCreleasedevxcandidate);
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(5000));
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(ACCreleasedevxcandidate.getAccManifestId());
            accViewEditPage.moveToDraft();
            accViewEditPage.moveToCandidate();

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(5000));
            ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
            ASCCPViewEditPage
                    asccpViewEditPage = asccpCreateDialog.create("ACCreleasedevxcandidate. Details");
            ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPPanel();
            asccpPanel.setPropertyTerm("ASCCPreleasedevxcandidate");
            asccpPanel.setNamespace("http://www.openapplications.org/oagis/10");
            asccpPanel.setDefinition("ASCCPreleasedevxcandidate");
            asccpViewEditPage.hitUpdateButton();
            String url = getDriver().getCurrentUrl();
            BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
            ASCCPreleasedevxcandidate = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
            if (!testingASCCPs.containsKey("ASCCPreleasedevxcandidate")) {
                testingASCCPs.put("ASCCPreleasedevxcandidate", ASCCPreleasedevxcandidate);
            } else {
                testingASCCPs.put("ASCCPreleasedevxcandidate", ASCCPreleasedevxcandidate);
            }
            asccpViewEditPage.moveToDraft();
            asccpViewEditPage.moveToCandidate();

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(5000));
            BCCPCreateDialog bccpCreateDialog = viewEditCoreComponentPage.openBCCPCreateDialog(branch);
            BCCPViewEditPage bccpViewEditPage = bccpCreateDialog.create("System Environment_ Code. Type");
            BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
            bccpPanel.setPropertyTerm("BCCPreleasedevxcandidate");
            bccpPanel.setNamespace("http://www.openapplications.org/oagis/10");
            bccpPanel.setDefinition("BCCPreleasedevxcandidate");
            bccpViewEditPage.hitUpdateButton();
            url = getDriver().getCurrentUrl();
            BigInteger bccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
            BCCPreleasedevxcandidate = getAPIFactory().getCoreComponentAPI().getBCCPByManifestId(bccpManifestId);

            if (!testingBCCPs.containsKey("BCCPreleasedevxcandidate")) {
                testingBCCPs.put("BCCPreleasedevxcandidate", BCCPreleasedevxcandidate);
            } else {
                testingBCCPs.put("BCCPreleasedevxcandidate", BCCPreleasedevxcandidate);
            }
            bccpViewEditPage.moveToDraft();
            bccpViewEditPage.moveToCandidate();
        }
        viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
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
        waitFor(Duration.ofMillis(8000));
        releaseAssignmentPage.hitCreateButton();

        do {
            newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        } while (!newDraftRelease.getState().equals("Draft"));
        viewEditReleasePage.openPage();
        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Draft");
        assertTrue(editReleasePage.isOpened());
        editReleasePage.setReleaseNote("updated note");
        editReleasePage.hitUpdateButton();

        viewEditReleasePage.openPage();
        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Draft");
        assertEquals("updated note", getText(editReleasePage.getReleaseNoteField()));

        editReleasePage.publish();
        do {
            newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        } while (!newDraftRelease.getState().equals("Published"));

        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Published");
        assertTrue(editReleasePage.isOpened());

        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(5000));
        viewEditCoreComponentPage.setDEN("ACCreleasedevxcandidate. Details");
        viewEditCoreComponentPage.setOwner(devx.getLoginId());
        escape(getDriver());
        viewEditCoreComponentPage.hitSearchButton();
        WebElement tr = viewEditCoreComponentPage.getTableRecordAtIndex(1);
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPage(tr);
        WebElement accNode = accViewEditPage.getNodeByPath("/ACCreleasedevxcandidate. Details");
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("Published", getText(accPanel.getStateField()));
    }

    @Test
    public void test_TA_19_1_11() {
        String branch = "Working";
        String existingDraftRelease = null;
        ReleaseObject newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        ReleaseObject existingDraftReleaseObj;
        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        viewEditReleasePage.setState("Draft");
        escape(getDriver());
        viewEditReleasePage.hitSearchButton();
        int resultRows = getDriver().findElements(By.xpath("//table/tbody/tr")).size();
        if (resultRows > 0) {
            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPage(tr);
            assertTrue(editReleasePage.isOpened());
            existingDraftRelease = getText(editReleasePage.getReleaseNumberField());
            if (existingDraftRelease != null) {
                editReleasePage.backToInitialized();
                do {
                    existingDraftReleaseObj = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(existingDraftRelease);
                } while (!existingDraftReleaseObj.getState().equals("Initialized"));
            }
        }

        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCObject ACCreleasedevxcandidate;
        ASCCPObject ASCCPreleasedevxcandidate;
        BCCPObject BCCPreleasedevxcandidate;
        {
            ReleaseObject workingRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            ACCreleasedevxcandidate = coreComponentAPI.createRandomACC(devx, workingRelease, developerNamespace, "WIP");
            ACCreleasedevxcandidate.setObjectClassTerm("ACCreleasedevxcandidate");
            coreComponentAPI.updateACC(ACCreleasedevxcandidate);
            if (!testingACCs.containsKey("ACCreleasedevxcandidate")) {
                testingACCs.put("ACCreleasedevxcandidate", ACCreleasedevxcandidate);
            } else {
                testingACCs.put("ACCreleasedevxcandidate", ACCreleasedevxcandidate);
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(5000));
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(ACCreleasedevxcandidate.getAccManifestId());
            accViewEditPage.moveToDraft();
            accViewEditPage.moveToCandidate();

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(5000));
            ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
            ASCCPViewEditPage
                    asccpViewEditPage = asccpCreateDialog.create("ACCreleasedevxcandidate. Details");
            ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPPanel();
            asccpPanel.setPropertyTerm("ASCCPreleasedevxcandidate");
            asccpPanel.setNamespace("http://www.openapplications.org/oagis/10");
            asccpPanel.setDefinition("ASCCPreleasedevxcandidate");
            asccpViewEditPage.hitUpdateButton();
            String url = getDriver().getCurrentUrl();
            BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
            ASCCPreleasedevxcandidate = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
            if (!testingASCCPs.containsKey("ASCCPreleasedevxcandidate")) {
                testingASCCPs.put("ASCCPreleasedevxcandidate", ASCCPreleasedevxcandidate);
            } else {
                testingASCCPs.put("ASCCPreleasedevxcandidate", ASCCPreleasedevxcandidate);
            }
            asccpViewEditPage.moveToDraft();
            asccpViewEditPage.moveToCandidate();

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(5000));
            BCCPCreateDialog bccpCreateDialog = viewEditCoreComponentPage.openBCCPCreateDialog(branch);
            BCCPViewEditPage bccpViewEditPage = bccpCreateDialog.create("System Environment_ Code. Type");
            BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
            bccpPanel.setPropertyTerm("BCCPreleasedevxcandidate");
            bccpPanel.setNamespace("http://www.openapplications.org/oagis/10");
            bccpPanel.setDefinition("BCCPreleasedevxcandidate");
            bccpViewEditPage.hitUpdateButton();
            url = getDriver().getCurrentUrl();
            BigInteger bccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
            BCCPreleasedevxcandidate = getAPIFactory().getCoreComponentAPI().getBCCPByManifestId(bccpManifestId);

            if (!testingBCCPs.containsKey("BCCPreleasedevxcandidate")) {
                testingBCCPs.put("BCCPreleasedevxcandidate", BCCPreleasedevxcandidate);
            } else {
                testingBCCPs.put("BCCPreleasedevxcandidate", BCCPreleasedevxcandidate);
            }
            bccpViewEditPage.moveToDraft();
            bccpViewEditPage.moveToCandidate();
        }
        viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
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
        waitFor(Duration.ofMillis(8000));
        releaseAssignmentPage.hitCreateButton();

        do {
            newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        } while (!newDraftRelease.getState().equals("Draft"));

        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Draft");
        editReleasePage.publish();
        do {
            newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        } while (!newDraftRelease.getState().equals("Published"));

        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Published");
        assertTrue(editReleasePage.isOpened());
        assertDisabled(editReleasePage.getReleaseNumberField());
        assertDisabled(editReleasePage.getReleaseNoteField());
    }

    @Test
    public void test_TA_19_1_12_and_TA_19_1_13() {
        String branch = "Working";
        String existingDraftRelease = null;
        ReleaseObject newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        ReleaseObject existingDraftReleaseObj;
        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        viewEditReleasePage.setState("Draft");
        escape(getDriver());
        viewEditReleasePage.hitSearchButton();
        int resultRows = getDriver().findElements(By.xpath("//table/tbody/tr")).size();
        if (resultRows > 0) {
            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPage(tr);
            assertTrue(editReleasePage.isOpened());
            existingDraftRelease = getText(editReleasePage.getReleaseNumberField());
            if (existingDraftRelease != null) {
                editReleasePage.backToInitialized();
                do {
                    existingDraftReleaseObj = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(existingDraftRelease);
                } while (!existingDraftReleaseObj.getState().equals("Initialized"));
            }
        }

        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCObject ACCreleasedevxcandidate;
        ASCCPObject ASCCPreleasedevxcandidate;
        BCCPObject BCCPreleasedevxcandidate;
        {
            ReleaseObject workingRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            ACCreleasedevxcandidate = coreComponentAPI.createRandomACC(devx, workingRelease, developerNamespace, "WIP");
            ACCreleasedevxcandidate.setObjectClassTerm("ACCreleasedevxcandidate");
            coreComponentAPI.updateACC(ACCreleasedevxcandidate);
            if (!testingACCs.containsKey("ACCreleasedevxcandidate")) {
                testingACCs.put("ACCreleasedevxcandidate", ACCreleasedevxcandidate);
            } else {
                testingACCs.put("ACCreleasedevxcandidate", ACCreleasedevxcandidate);
            }

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(5000));
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(ACCreleasedevxcandidate.getAccManifestId());
            accViewEditPage.moveToDraft();
            accViewEditPage.moveToCandidate();

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(5000));
            ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
            ASCCPViewEditPage
                    asccpViewEditPage = asccpCreateDialog.create("ACCreleasedevxcandidate. Details");
            ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPPanel();
            asccpPanel.setPropertyTerm("ASCCPreleasedevxcandidate");
            asccpPanel.setNamespace("http://www.openapplications.org/oagis/10");
            asccpPanel.setDefinition("ASCCPreleasedevxcandidate");
            asccpViewEditPage.hitUpdateButton();
            String url = getDriver().getCurrentUrl();
            BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
            ASCCPreleasedevxcandidate = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
            if (!testingASCCPs.containsKey("ASCCPreleasedevxcandidate")) {
                testingASCCPs.put("ASCCPreleasedevxcandidate", ASCCPreleasedevxcandidate);
            } else {
                testingASCCPs.put("ASCCPreleasedevxcandidate", ASCCPreleasedevxcandidate);
            }
            asccpViewEditPage.moveToDraft();
            asccpViewEditPage.moveToCandidate();

            viewEditCoreComponentPage.openPage();
            waitFor(Duration.ofMillis(5000));
            BCCPCreateDialog bccpCreateDialog = viewEditCoreComponentPage.openBCCPCreateDialog(branch);
            BCCPViewEditPage bccpViewEditPage = bccpCreateDialog.create("System Environment_ Code. Type");
            BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
            bccpPanel.setPropertyTerm("BCCPreleasedevxcandidate");
            bccpPanel.setNamespace("http://www.openapplications.org/oagis/10");
            bccpPanel.setDefinition("BCCPreleasedevxcandidate");
            bccpViewEditPage.hitUpdateButton();
            url = getDriver().getCurrentUrl();
            BigInteger bccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
            BCCPreleasedevxcandidate = getAPIFactory().getCoreComponentAPI().getBCCPByManifestId(bccpManifestId);

            if (!testingBCCPs.containsKey("BCCPreleasedevxcandidate")) {
                testingBCCPs.put("BCCPreleasedevxcandidate", BCCPreleasedevxcandidate);
            } else {
                testingBCCPs.put("BCCPreleasedevxcandidate", BCCPreleasedevxcandidate);
            }
            bccpViewEditPage.moveToDraft();
            bccpViewEditPage.moveToCandidate();
        }
        viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
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
        waitFor(Duration.ofMillis(8000));
        releaseAssignmentPage.hitCreateButton();

        do {
            newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        } while (!newDraftRelease.getState().equals("Draft"));

        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Draft");
        editReleasePage.publish();
        do {
            newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        } while (!newDraftRelease.getState().equals("Published"));

        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Published");

        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true);
        homePage.logout();
        homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        ViewEditReleasePage finalViewEditReleasePage = viewEditReleasePage;
        assertThrows(TimeoutException.class, () -> finalViewEditReleasePage.createRelease());

        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Published");
        assertDisabled(editReleasePage.getReleaseNoteField());
    }

}
