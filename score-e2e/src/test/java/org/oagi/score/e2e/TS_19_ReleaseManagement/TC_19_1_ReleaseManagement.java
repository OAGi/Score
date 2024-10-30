package org.oagi.score.e2e.TS_19_ReleaseManagement;

import org.apache.commons.lang3.RandomUtils;
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
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.time.Duration.ofSeconds;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.*;
import static org.oagi.score.e2e.obj.ComponentType.Extension;

@Execution(ExecutionMode.SAME_THREAD)
public class TC_19_1_ReleaseManagement extends BaseTest {

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

    private TestTC_19_PreConditions create(AppUserObject developer, NamespaceObject developerNamespace) {
        if (!getAPIFactory().getReleaseAPI().getReleasesByStates(Arrays.asList("Initialized", "Draft")).isEmpty()) {
            throw new IllegalStateException("Pre-conditions must be created before creating a new random release");
        }

        TestTC_19_PreConditions conditions = new TestTC_19_PreConditions();
        ReleaseObject workingRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

        conditions.ACCreleaseTA3devxwip = coreComponentAPI.createRandomACC(developer, workingRelease, developerNamespace, "WIP");
        conditions.ACCreleaseTA3devxdraft = coreComponentAPI.createRandomACC(developer, workingRelease, developerNamespace, "Draft");
        conditions.ACCreleaseTA3devxcandidate = coreComponentAPI.createRandomACC(developer, workingRelease, developerNamespace, "Candidate");

        conditions.ASCCPreleaseTA3devxwip = coreComponentAPI.createRandomASCCP(
                conditions.ACCreleaseTA3devxwip,
                developer, developerNamespace, "WIP");
        conditions.ASCCPreleaseTA3devxdraft = coreComponentAPI.createRandomASCCP(
                conditions.ACCreleaseTA3devxdraft,
                developer, developerNamespace, "Draft");
        conditions.ASCCPreleaseTA3devxcandidate = coreComponentAPI.createRandomASCCP(
                conditions.ACCreleaseTA3devxcandidate,
                developer, developerNamespace, "Candidate");

        DTObject bdtForRandomBCCPs = coreComponentAPI.getBDTByDENAndReleaseNum(
                "System Environment_ Code. Type", workingRelease.getReleaseNumber()).get(0);
        conditions.BCCPreleaseTA3devxwip = coreComponentAPI.createRandomBCCP(bdtForRandomBCCPs,
                developer, developerNamespace, "WIP");
        conditions.BCCPreleaseTA3devxdraft = coreComponentAPI.createRandomBCCP(bdtForRandomBCCPs,
                developer, developerNamespace, "Draft");
        conditions.BCCPreleaseTA3devxcandidate = coreComponentAPI.createRandomBCCP(bdtForRandomBCCPs,
                developer, developerNamespace, "Candidate");

        //Revision
        conditions.ACCreleaseTA3revisionwip = coreComponentAPI.createRevisedACC(
                coreComponentAPI.createRandomACC(developer, workingRelease, developerNamespace, "Published"),
                developer, workingRelease, "WIP");
        conditions.ACCreleaseTA3revisiondraft = coreComponentAPI.createRevisedACC(
                coreComponentAPI.createRandomACC(developer, workingRelease, developerNamespace, "Published"),
                developer, workingRelease, "Draft");
        conditions.ACCreleaseTA3revisioncandidate = coreComponentAPI.createRevisedACC(
                coreComponentAPI.createRandomACC(developer, workingRelease, developerNamespace, "Published"),
                developer, workingRelease, "Candidate");

        conditions.ASCCPreleaseTA3revisionwip = coreComponentAPI.createRevisedASCCP(
                coreComponentAPI.createRandomASCCP(
                        coreComponentAPI.getACCByDENAndReleaseNum("Message. Details", workingRelease.getReleaseNumber()),
                        developer, developerNamespace, "Published"),
                coreComponentAPI.getACCByDENAndReleaseNum("Message. Details", workingRelease.getReleaseNumber()),
                developer, workingRelease, "WIP");
        conditions.ASCCPreleaseTA3revisiondraft = coreComponentAPI.createRevisedASCCP(
                coreComponentAPI.createRandomASCCP(
                        coreComponentAPI.getACCByDENAndReleaseNum("Document Reference. Details", workingRelease.getReleaseNumber()),
                        developer, developerNamespace, "Published"),
                coreComponentAPI.getACCByDENAndReleaseNum("Document Reference. Details", workingRelease.getReleaseNumber()),
                developer, workingRelease, "WIP");
        conditions.ASCCPreleaseTA3revisioncandidate = coreComponentAPI.createRevisedASCCP(
                coreComponentAPI.createRandomASCCP(
                        coreComponentAPI.getACCByDENAndReleaseNum("Shipping Route. Details", workingRelease.getReleaseNumber()),
                        developer, developerNamespace, "Published"),
                coreComponentAPI.getACCByDENAndReleaseNum("Shipping Route. Details", workingRelease.getReleaseNumber()),
                developer, workingRelease, "WIP");

        conditions.BCCPreleaseTA3revisionwip = coreComponentAPI.createRevisedBCCP(
                coreComponentAPI.createRandomBCCP(
                        coreComponentAPI.getBDTByDENAndReleaseNum("Open_ Quantity. Type", workingRelease.getReleaseNumber()).get(0), developer, developerNamespace, "Published"),
                coreComponentAPI.getBDTByDENAndReleaseNum("Open_ Quantity. Type", workingRelease.getReleaseNumber()).get(0),
                developer, workingRelease, "WIP");
        conditions.BCCPreleaseTA3revisiondraft = coreComponentAPI.createRevisedBCCP(
                coreComponentAPI.createRandomBCCP(
                        coreComponentAPI.getBDTByDENAndReleaseNum("Open_ Indicator. Type", workingRelease.getReleaseNumber()).get(0), developer, developerNamespace, "Published"),
                coreComponentAPI.getBDTByDENAndReleaseNum("Open_ Indicator. Type", workingRelease.getReleaseNumber()).get(0),
                developer, workingRelease, "Draft");
        conditions.BCCPreleaseTA3revisioncandidate = coreComponentAPI.createRevisedBCCP(
                coreComponentAPI.createRandomBCCP(
                        coreComponentAPI.getBDTByDENAndReleaseNum("Open_ Indicator. Type", workingRelease.getReleaseNumber()).get(0), developer, developerNamespace, "Published"),
                coreComponentAPI.getBDTByDENAndReleaseNum("Open_ Indicator. Type", workingRelease.getReleaseNumber()).get(0),
                developer, workingRelease, "Candidate");

        return conditions;
    }

    private class TestTC_19_PreConditions {

        ACCObject ACCreleaseTA3devxwip;
        ACCObject ACCreleaseTA3devxdraft;
        ACCObject ACCreleaseTA3devxcandidate;

        ASCCPObject ASCCPreleaseTA3devxwip;
        ASCCPObject ASCCPreleaseTA3devxdraft;
        ASCCPObject ASCCPreleaseTA3devxcandidate;

        BCCPObject BCCPreleaseTA3devxwip;
        BCCPObject BCCPreleaseTA3devxdraft;
        BCCPObject BCCPreleaseTA3devxcandidate;

        //Revision
        ACCObject ACCreleaseTA3revisionwip;
        ACCObject ACCreleaseTA3revisiondraft;
        ACCObject ACCreleaseTA3revisioncandidate;

        ASCCPObject ASCCPreleaseTA3revisionwip;
        ASCCPObject ASCCPreleaseTA3revisiondraft;
        ASCCPObject ASCCPreleaseTA3revisioncandidate;

        BCCPObject BCCPreleaseTA3revisionwip;
        BCCPObject BCCPreleaseTA3revisiondraft;
        BCCPObject BCCPreleaseTA3revisioncandidate;

    }
    
    @Test
    public void test_TA_19_1_3a() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        NamespaceObject developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer);

        TestTC_19_PreConditions conditions;
        {
            conditions = create(developer, developerNamespace);
        }
        ReleaseObject randomRelease = getAPIFactory().getReleaseAPI().createRandomRelease(developer, developerNamespace);
        String newReleaseNum = randomRelease.getReleaseNumber();

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        viewEditReleasePage.showAdvancedSearchPanel();
        viewEditReleasePage.setState("Draft");
        viewEditReleasePage.hitSearchButton();
        assertEquals(0, viewEditReleasePage.getTotalNumberOfItems());

        viewEditReleasePage.openPage();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();

        assertTrue(getDriver().findElements(By.xpath("//mat-card-content/div/div[2]//*[contains(text(), \"Candidate\")]")).size() > 0);
        assertEquals(0, getDriver().findElements(By.xpath("//mat-card-content/div/div[2]//*[contains(text(), \"WIP\")]")).size());
        assertEquals(0, getDriver().findElements(By.xpath("//mat-card-content/div/div[2]//*[contains(text(), \"Draft\")]")).size());
    }

    @Test
    public void test_TA_19_1_3c_case2_and_case3() {
        AppUserObject devx = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(devx);
        NamespaceObject developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(devx);

        TestTC_19_PreConditions conditions;
        {
            conditions = create(devx, developerNamespace);
        }
        ACCObject randomACC = getAPIFactory().getCoreComponentAPI().createRandomACC(devx,
                getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working"), developerNamespace, "Candidate");
        getAPIFactory().getCoreComponentAPI().appendASCC(randomACC, conditions.ASCCPreleaseTA3devxwip, "Candidate");
        getAPIFactory().getCoreComponentAPI().appendASCC(randomACC, conditions.ASCCPreleaseTA3devxdraft, "Candidate");
        getAPIFactory().getCoreComponentAPI().appendBCC(randomACC, conditions.BCCPreleaseTA3devxwip, "Candidate");
        getAPIFactory().getCoreComponentAPI().appendBCC(randomACC, conditions.BCCPreleaseTA3devxdraft, "Candidate");

        ReleaseObject randomRelease = getAPIFactory().getReleaseAPI().createRandomRelease(devx, developerNamespace);
        String newReleaseNum = randomRelease.getReleaseNumber();

        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();

        //Case 2 ACC to ASCCP
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] '" + conditions.ASCCPreleaseTA3devxwip.getDen() + "' is needed in the release assignment due to '" + randomACC.getDen() + "'.\")]")).size() >= 1);
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] '" + conditions.ASCCPreleaseTA3devxdraft.getDen() + "' is needed in the release assignment due to '" + randomACC.getDen() + "'.\")]")).size() >= 1);

        //Case 3 ACC to BCCP
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] '" + conditions.BCCPreleaseTA3devxwip.getDen() + "' is needed in the release assignment due to '" + randomACC.getDen() + "'.\")]")).size() >= 1);
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] '" + conditions.BCCPreleaseTA3devxdraft.getDen() + "' is needed in the release assignment due to '" + randomACC.getDen() + "'.\")]")).size() >= 1);
    }

    @Test
    public void test_TA_19_1_3c_case1() {
        AppUserObject devx = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(devx);
        NamespaceObject developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(devx);

        ACCObject randomACC = getAPIFactory().getCoreComponentAPI().createRandomACC(devx,
                getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working"), developerNamespace, "WIP");
        ASCCPObject randomASCCP = getAPIFactory().getCoreComponentAPI().createRandomASCCP(
                randomACC, devx, developerNamespace, "Candidate");

        ReleaseObject randomRelease = getAPIFactory().getReleaseAPI().createRandomRelease(devx, developerNamespace);
        String newReleaseNum = randomRelease.getReleaseNumber();

        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();
        // Case1 when acc wip
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] '" + randomACC.getDen() + "' is needed in the release assignment due to '" + randomASCCP.getDen() + "'.\")]")).size() >= 1);

        // Case1 when acc is moved to draft
        randomACC.setState("Draft");
        getAPIFactory().getCoreComponentAPI().updateACC(randomACC);
        releaseAssignmentPage.openPage();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();
        // Case1 when acc in draft state
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] '" + randomACC.getDen() + "' is needed in the release assignment due to '" + randomASCCP.getDen() + "'.\")]")).size() >= 1);
    }

    @Test
    public void test_TA_19_1_3c_case7() {
        AppUserObject devx = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(devx);
        NamespaceObject developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(devx);

        ACCObject randomBaseACC = getAPIFactory().getCoreComponentAPI().createRandomACC(devx,
                getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working"), developerNamespace, "WIP");
        ACCObject randomACC = getAPIFactory().getCoreComponentAPI().createRandomACC(devx,
                getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working"), developerNamespace, "Candidate");
        getAPIFactory().getCoreComponentAPI().updateBasedACC(randomACC, randomBaseACC);

        ReleaseObject randomRelease = getAPIFactory().getReleaseAPI().createRandomRelease(devx, developerNamespace);
        String newReleaseNum = randomRelease.getReleaseNumber();

        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();

        // Case7 when acc is in wip
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] '" + randomBaseACC.getDen() + "' is needed in the release assignment due to '" + randomACC.getDen() + "'.\")]")).size() >= 1);

        // Case7 when acc base is moved to draft
        randomBaseACC.setState("Draft");
        getAPIFactory().getCoreComponentAPI().updateACC(randomBaseACC);
        releaseAssignmentPage.openPage();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] '" + randomBaseACC.getDen() + "' is needed in the release assignment due to '" + randomACC.getDen() + "'.\")]")).size() >= 1);
    }

    @Ignore
    public void test_TA_19_1_3c_case8_and_case9_and_case10_and_test_TA_19_1_3h() {
        //replaced by field is not implemented yet
    }

    @Test
    public void test_TA_19_1_3d_and_1_3e_and_1_3f_and_1_3g() {
        AppUserObject devx = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(devx);
        NamespaceObject developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(devx);

        String randomObjectClassTerm = randomAlphabetic(5, 10).replaceAll(" ", "");
        randomObjectClassTerm = Character.toUpperCase(randomObjectClassTerm.charAt(0)) + randomObjectClassTerm.substring(1).toLowerCase();
        ACCObject extensionACC = getAPIFactory().getCoreComponentAPI().createRandomACC(devx,
                getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working"), developerNamespace, "Candidate",
                Extension, randomObjectClassTerm + " Extension");
        ASCCPObject extensionASCCP = getAPIFactory().getCoreComponentAPI().createRandomASCCP(
                extensionACC, devx, developerNamespace, "WIP");
        ACCObject randomACC = getAPIFactory().getCoreComponentAPI().createRandomACC(devx,
                getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working"), developerNamespace, "Candidate");
        getAPIFactory().getCoreComponentAPI().appendASCC(randomACC, extensionASCCP, "Candidate");

        ReleaseObject randomRelease = getAPIFactory().getReleaseAPI().createRandomRelease(devx, developerNamespace);
        String newReleaseNum = randomRelease.getReleaseNumber();

        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();

        // extension in WIP status
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] '" + extensionASCCP.getDen() + "' is needed in the release assignment due to '" + randomACC.getDen() + "'.\")]")).size() >= 1);

        // ext is moved to draft
        extensionASCCP.setState("Draft");
        getAPIFactory().getCoreComponentAPI().updateASCCP(extensionASCCP);
        releaseAssignmentPage.openPage();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] '" + extensionASCCP.getDen() + "' is needed in the release assignment due to '" + randomACC.getDen() + "'.\")]")).size() >= 1);

        // ASCCP extension in Candidate but the ACC in WIP
        extensionASCCP.setState("Candidate");
        getAPIFactory().getCoreComponentAPI().updateASCCP(extensionASCCP);
        extensionACC.setState("WIP");
        getAPIFactory().getCoreComponentAPI().updateACC(extensionACC);
        releaseAssignmentPage.openPage();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] '" + extensionACC.getDen() + "' is needed in the release assignment due to '" + extensionASCCP.getDen() + "'.\")]")).size() >= 1);

        // ASCCP extension in Candidate but the ACC in draft
        extensionACC.setState("Draft");
        getAPIFactory().getCoreComponentAPI().updateACC(extensionACC);
        releaseAssignmentPage.openPage();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();
        assertTrue(getDriver().findElements(By.xpath("//span[contains(text(),\"[Error] '" + extensionACC.getDen() + "' is needed in the release assignment due to '" + extensionASCCP.getDen() + "'.\")]")).size() >= 1);
    }

    @Ignore
    public void test_TA_19_1_3i() {
        //Validate non-reusable ASCCP is ensured in UI
    }

    @Test
    public void test_TA_19_1_3j() {
        AppUserObject devx = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(devx);
        NamespaceObject developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(devx);

        TestTC_19_PreConditions conditions;
        {
            conditions = create(devx, developerNamespace);
        }

        ReleaseObject randomRelease = getAPIFactory().getReleaseAPI().createRandomRelease(devx, developerNamespace);
        String newReleaseNum = randomRelease.getReleaseNumber();

        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();
        assertEquals("All components are valid.", getSnackBarMessage(getDriver()));

        releaseAssignmentPage.hitCreateButton();
        long timeout = Duration.ofSeconds(300L).toMillis();
        long begin = System.currentTimeMillis();
        while (System.currentTimeMillis() - begin < timeout) {
            viewEditReleasePage.openPage();
            viewEditReleasePage.setReleaseNum(newReleaseNum);
            viewEditReleasePage.hitSearchButton();

            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            String state = getText(viewEditReleasePage.getColumnByName(tr, "state"));
            assertNotEquals("Initialized", state);
            if ("Draft".equals(state)) {
                break;
            }
        }

        randomRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        assertEquals("Draft", randomRelease.getState());

        // move back to initialize to Cancel
        editReleasePage.openPage();
        editReleasePage.backToInitialized();

        begin = System.currentTimeMillis();
        while (System.currentTimeMillis() - begin < timeout) {
            viewEditReleasePage.openPage();
            viewEditReleasePage.setReleaseNum(newReleaseNum);
            viewEditReleasePage.hitSearchButton();

            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            String state = getText(viewEditReleasePage.getColumnByName(tr, "state"));
            if ("Initialized".equals(state)) {
                break;
            }
        }

        randomRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        assertEquals("Initialized", randomRelease.getState());
    }

    @Test
    public void test_TA_19_1_3k() {
        // Only the administrators can publish the draft release.
        AppUserObject devx = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(devx);
        NamespaceObject developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(devx);

        TestTC_19_PreConditions conditions;
        {
            conditions = create(devx, developerNamespace);
        }

        ReleaseObject randomRelease = getAPIFactory().getReleaseAPI().createRandomRelease(devx, developerNamespace);
        String newReleaseNum = randomRelease.getReleaseNumber();

        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();
        assertEquals("All components are valid.", getSnackBarMessage(getDriver()));

        releaseAssignmentPage.hitCreateButton();
        long timeout = Duration.ofSeconds(300L).toMillis();
        long begin = System.currentTimeMillis();
        while (System.currentTimeMillis() - begin < timeout) {
            viewEditReleasePage.openPage();
            viewEditReleasePage.setReleaseNum(newReleaseNum);
            viewEditReleasePage.hitSearchButton();

            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            String state = getText(viewEditReleasePage.getColumnByName(tr, "state"));
            assertNotEquals("Initialized", state);
            if ("Draft".equals(state)) {
                break;
            }
        }

        randomRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        assertEquals("Draft", randomRelease.getState());

        editReleasePage.openPage();
        editReleasePage.publish();

        begin = System.currentTimeMillis();
        while (System.currentTimeMillis() - begin < timeout) {
            viewEditReleasePage.openPage();
            viewEditReleasePage.setReleaseNum(newReleaseNum);
            viewEditReleasePage.hitSearchButton();

            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            String state = getText(viewEditReleasePage.getColumnByName(tr, "state"));
            if ("Published".equals(state)) {
                break;
            }
        }

        randomRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        assertEquals("Published", randomRelease.getState());
    }

    @Test
    public void test_TA_19_1_4() {
        AppUserObject devx = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(devx);
        NamespaceObject developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(devx);

        ReleaseObject randomRelease = getAPIFactory().getReleaseAPI().createRandomRelease(devx, developerNamespace);
        String newReleaseNum = randomRelease.getReleaseNumber();

        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Initialized");
        assertEquals(newReleaseNum, getText(editReleasePage.getReleaseNumberField()));

        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        waitFor(Duration.ofMillis(500L));
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
        AppUserObject devx = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(devx);
        NamespaceObject developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(devx);

        TestTC_19_PreConditions conditions;
        {
            conditions = create(devx, developerNamespace);
        }

        ReleaseObject randomRelease = getAPIFactory().getReleaseAPI().createRandomRelease(devx, developerNamespace);
        String newReleaseNum = randomRelease.getReleaseNumber();

        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();
        assertEquals("All components are valid.", getSnackBarMessage(getDriver()));

        releaseAssignmentPage.hitCreateButton();
        long timeout = Duration.ofSeconds(300L).toMillis();
        long begin = System.currentTimeMillis();
        while (System.currentTimeMillis() - begin < timeout) {
            viewEditReleasePage.openPage();
            viewEditReleasePage.setReleaseNum(newReleaseNum);
            viewEditReleasePage.hitSearchButton();

            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            String state = getText(viewEditReleasePage.getColumnByName(tr, "state"));
            assertNotEquals("Initialized", state);
            if ("Draft".equals(state)) {
                break;
            }
        }

        randomRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        assertEquals("Draft", randomRelease.getState());

        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.showAdvancedSearchPanel();
        viewEditCoreComponentPage.setBranch(newReleaseNum);
        viewEditCoreComponentPage.setOwner(devx.getLoginId());

        viewEditCoreComponentPage.setDEN(conditions.ACCreleaseTA3devxcandidate.getObjectClassTerm());
        viewEditCoreComponentPage.hitSearchButton();
        assertTrue(getDriver().findElements(By.xpath("//*[contains(text(), \"" + conditions.ACCreleaseTA3devxcandidate.getObjectClassTerm() + "\")]//ancestor::tr")).size() >= 1);

        viewEditCoreComponentPage.setDEN(conditions.ASCCPreleaseTA3devxcandidate.getPropertyTerm());
        viewEditCoreComponentPage.hitSearchButton();
        assertTrue(getDriver().findElements(By.xpath("//*[contains(text(), \"" + conditions.ASCCPreleaseTA3devxcandidate.getPropertyTerm() + "\")]//ancestor::tr")).size() >= 1);

        viewEditCoreComponentPage.setDEN(conditions.BCCPreleaseTA3devxcandidate.getPropertyTerm());
        viewEditCoreComponentPage.hitSearchButton();
        assertTrue(getDriver().findElements(By.xpath("//*[contains(text(), \"" + conditions.BCCPreleaseTA3devxcandidate.getPropertyTerm() + "\")]//ancestor::tr")).size() >= 1);

        viewEditCoreComponentPage.openPage();
        viewEditCoreComponentPage.setDEN(conditions.ACCreleaseTA3devxcandidate.getDen());
        viewEditCoreComponentPage.hitSearchButton();
        WebElement tr = viewEditCoreComponentPage.getTableRecordAtIndex(1);
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPage(tr);
        WebElement accNode = accViewEditPage.getNodeByPath("/" + conditions.ACCreleaseTA3devxcandidate.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("ReleaseDraft", getText(accPanel.getStateField()));
        assertDisabled(accPanel.getDENField());
        assertDisabled(accPanel.getObjectClassTermField());

        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(devx);
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Collections.singletonList(context));
        click(createBIEForSelectTopLevelConceptPage.getBranchSelectField());
        waitFor(ofSeconds(2L));
        assertEquals(0, getDriver().findElements(By.xpath("//*[contains(text(),\"" + newReleaseNum + "\")]//ancestor::mat-option[1]/span")).size());
        escape(getDriver());

        // move back to initialize to Cancel
        editReleasePage.openPage();
        editReleasePage.backToInitialized();

        begin = System.currentTimeMillis();
        while (System.currentTimeMillis() - begin < timeout) {
            viewEditReleasePage.openPage();
            viewEditReleasePage.setReleaseNum(newReleaseNum);
            viewEditReleasePage.hitSearchButton();

            tr = viewEditReleasePage.getTableRecordAtIndex(1);
            String state = getText(viewEditReleasePage.getColumnByName(tr, "state"));
            if ("Initialized".equals(state)) {
                break;
            }
        }

        randomRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        assertEquals("Initialized", randomRelease.getState());

        viewEditCoreComponentPage.openPage();
        viewEditCoreComponentPage.setDEN(conditions.ACCreleaseTA3devxcandidate.getDen());
        viewEditCoreComponentPage.hitSearchButton();
        tr = viewEditCoreComponentPage.getTableRecordAtIndex(1);
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPage(tr);
        accNode = accViewEditPage.getNodeByPath("/" + conditions.ACCreleaseTA3devxcandidate.getDen());
        accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("Candidate", getText(accPanel.getStateField()));
    }

    @Test
    public void test_TA_19_1_9_and_TA_19_1_10() {
        // Only the administrators can publish the draft release.
        AppUserObject devx = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(devx);
        NamespaceObject developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(devx);

        TestTC_19_PreConditions conditions;
        {
            conditions = create(devx, developerNamespace);
        }

        ReleaseObject randomRelease = getAPIFactory().getReleaseAPI().createRandomRelease(devx, developerNamespace);
        String newReleaseNum = randomRelease.getReleaseNumber();

        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();
        assertEquals("All components are valid.", getSnackBarMessage(getDriver()));

        releaseAssignmentPage.hitCreateButton();
        long timeout = Duration.ofSeconds(300L).toMillis();
        long begin = System.currentTimeMillis();
        while (System.currentTimeMillis() - begin < timeout) {
            viewEditReleasePage.openPage();
            viewEditReleasePage.setReleaseNum(newReleaseNum);
            viewEditReleasePage.hitSearchButton();

            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            String state = getText(viewEditReleasePage.getColumnByName(tr, "state"));
            assertNotEquals("Initialized", state);
            if ("Draft".equals(state)) {
                break;
            }
        }

        randomRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        assertEquals("Draft", randomRelease.getState());

        viewEditReleasePage.openPage();
        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Draft");
        assertTrue(editReleasePage.isOpened());
        editReleasePage.setReleaseNote("updated note");
        editReleasePage.hitUpdateButton();

        viewEditReleasePage.openPage();
        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Draft");
        assertEquals("updated note", getText(editReleasePage.getReleaseNoteField()));

        editReleasePage.openPage();
        editReleasePage.publish();

        begin = System.currentTimeMillis();
        while (System.currentTimeMillis() - begin < timeout) {
            viewEditReleasePage.openPage();
            viewEditReleasePage.setReleaseNum(newReleaseNum);
            viewEditReleasePage.hitSearchButton();

            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            String state = getText(viewEditReleasePage.getColumnByName(tr, "state"));
            if ("Published".equals(state)) {
                break;
            }
        }

        randomRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        assertEquals("Published", randomRelease.getState());

        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.openPage();
        waitFor(Duration.ofMillis(500));
        viewEditCoreComponentPage.showAdvancedSearchPanel();
        viewEditCoreComponentPage.setDEN(conditions.ACCreleaseTA3devxcandidate.getObjectClassTerm());
        viewEditCoreComponentPage.setOwner(devx.getLoginId());
        escape(getDriver());
        viewEditCoreComponentPage.hitSearchButton();

        WebElement tr = viewEditCoreComponentPage.getTableRecordAtIndex(1);
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPage(tr);
        WebElement accNode = accViewEditPage.getNodeByPath("/" + conditions.ACCreleaseTA3devxcandidate.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("Published", getText(accPanel.getStateField()));
    }

    @Test
    public void test_TA_19_1_11() {
        // Only the administrators can publish the draft release.
        AppUserObject devx = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(devx);
        NamespaceObject developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(devx);

        TestTC_19_PreConditions conditions;
        {
            conditions = create(devx, developerNamespace);
        }

        ReleaseObject randomRelease = getAPIFactory().getReleaseAPI().createRandomRelease(devx, developerNamespace);
        String newReleaseNum = randomRelease.getReleaseNumber();

        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();
        assertEquals("All components are valid.", getSnackBarMessage(getDriver()));

        releaseAssignmentPage.hitCreateButton();
        long timeout = Duration.ofSeconds(300L).toMillis();
        long begin = System.currentTimeMillis();
        while (System.currentTimeMillis() - begin < timeout) {
            viewEditReleasePage.openPage();
            viewEditReleasePage.setReleaseNum(newReleaseNum);
            viewEditReleasePage.hitSearchButton();

            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            String state = getText(viewEditReleasePage.getColumnByName(tr, "state"));
            assertNotEquals("Initialized", state);
            if ("Draft".equals(state)) {
                break;
            }
        }

        randomRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        assertEquals("Draft", randomRelease.getState());

        editReleasePage.openPage();
        editReleasePage.publish();

        begin = System.currentTimeMillis();
        while (System.currentTimeMillis() - begin < timeout) {
            viewEditReleasePage.openPage();
            viewEditReleasePage.setReleaseNum(newReleaseNum);
            viewEditReleasePage.hitSearchButton();

            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            String state = getText(viewEditReleasePage.getColumnByName(tr, "state"));
            if ("Published".equals(state)) {
                break;
            }
        }

        randomRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        assertEquals("Published", randomRelease.getState());

        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Published");
        assertDisabled(editReleasePage.getReleaseNumberField());
        assertDisabled(editReleasePage.getReleaseNoteField());
    }

    @Test
    public void test_TA_19_1_12_and_TA_19_1_13() {
        // Only the administrators can publish the draft release.
        AppUserObject devx = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(devx);
        NamespaceObject developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(devx);

        TestTC_19_PreConditions conditions;
        {
            conditions = create(devx, developerNamespace);
        }

        ReleaseObject randomRelease = getAPIFactory().getReleaseAPI().createRandomRelease(devx, developerNamespace);
        String newReleaseNum = randomRelease.getReleaseNumber();

        HomePage homePage = loginPage().signIn(devx.getLoginId(), devx.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();
        assertEquals("All components are valid.", getSnackBarMessage(getDriver()));

        releaseAssignmentPage.hitCreateButton();
        long timeout = Duration.ofSeconds(300L).toMillis();
        long begin = System.currentTimeMillis();
        while (System.currentTimeMillis() - begin < timeout) {
            viewEditReleasePage.openPage();
            viewEditReleasePage.setReleaseNum(newReleaseNum);
            viewEditReleasePage.hitSearchButton();

            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            String state = getText(viewEditReleasePage.getColumnByName(tr, "state"));
            assertNotEquals("Initialized", state);
            if ("Draft".equals(state)) {
                break;
            }
        }

        randomRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        assertEquals("Draft", randomRelease.getState());

        editReleasePage.openPage();
        editReleasePage.publish();

        begin = System.currentTimeMillis();
        while (System.currentTimeMillis() - begin < timeout) {
            viewEditReleasePage.openPage();
            viewEditReleasePage.setReleaseNum(newReleaseNum);
            viewEditReleasePage.hitSearchButton();

            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            String state = getText(viewEditReleasePage.getColumnByName(tr, "state"));
            if ("Published".equals(state)) {
                break;
            }
        }

        randomRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        assertEquals("Published", randomRelease.getState());

        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true);
        thisAccountWillBeDeletedAfterTests(usera);

        homePage.logout();
        homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        ViewEditReleasePage finalViewEditReleasePage = viewEditReleasePage;
        assertThrows(TimeoutException.class, () -> finalViewEditReleasePage.createRelease());

        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Published");
        assertDisabled(editReleasePage.getReleaseNoteField());
    }

}
