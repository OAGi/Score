package org.oagi.score.e2e.TS_21_ModuleManagement;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.oagi.score.e2e.page.module.*;
import org.oagi.score.e2e.page.release.CreateReleasePage;
import org.oagi.score.e2e.page.release.EditReleasePage;
import org.oagi.score.e2e.page.release.ReleaseAssignmentPage;
import org.oagi.score.e2e.page.release.ViewEditReleasePage;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_21_2_ManageReleaseModuleSet extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_21_2_TA_1")
    public void test_TA_1() {
        AppUserObject developer;
        NamespaceObject namespace;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            ReleaseObject draftRelease = getAPIFactory().getReleaseAPI().createDraftRelease(developer, namespace);
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetReleasePage viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        CreateModuleSetReleasePage createModuleSetReleasePage = viewEditModuleSetReleasePage.hitNewModuleSetReleaseButton();
        createModuleSetReleasePage.setName("Module Set Release Test" + randomAlphanumeric(5, 10));
        createModuleSetReleasePage.setDescription("Description Test");
        List<ModuleSetObject> existingModuleSets = getAPIFactory().getModuleSetAPI().getAllModuleSets();
        for (ModuleSetObject moduleSet : existingModuleSets){
            assertDoesNotThrow(() -> createModuleSetReleasePage.setModuleSet(moduleSet.getName()));
        }
        List<ReleaseObject> existingReleases = getAPIFactory().getReleaseAPI().getReleases();
        for (ReleaseObject release : existingReleases){
            assertDoesNotThrow(() ->  createModuleSetReleasePage.setRelease(release.getReleaseNumber()));
        }
        createModuleSetReleasePage.toggleDefault();
        createModuleSetReleasePage.hitCreateButton();
    }

    @Test
    @DisplayName("TC_21_2_TA_2")
    public void test_TA_2() {

    }

    @Test
    @DisplayName("TC_21_2_TA_3")
    public void test_TA_3() {
        AppUserObject developerA;
        AppUserObject developerB;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);
        }
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditModuleSetReleasePage viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        CreateModuleSetReleasePage createModuleSetReleasePage = viewEditModuleSetReleasePage.hitNewModuleSetReleaseButton();
        createModuleSetReleasePage.setName("Module Set Release Test" + randomAlphanumeric(5, 10));
        createModuleSetReleasePage.setDescription("Description Test");
        List<ModuleSetObject> existingModuleSets = getAPIFactory().getModuleSetAPI().getAllModuleSets();
        createModuleSetReleasePage.setModuleSet(existingModuleSets.get(0).getName());

        List<ReleaseObject> existingReleases = getAPIFactory().getReleaseAPI().getReleases();
        createModuleSetReleasePage.setRelease(existingReleases.get(0).getReleaseNumber());
        createModuleSetReleasePage.hitCreateButton();
        homePage.logout();

        waitFor(ofMillis(500L));
        homePage = loginPage().signIn(developerB.getLoginId(), developerB.getPassword());
        ModuleSetReleaseObject latestModuleSetRelease = getAPIFactory().getModuleSetReleaseAPI().getTheLatestModuleSetReleaseCreatedBy(developerA);
        viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        EditModuleSetReleasePage editModuleSetReleasePage = viewEditModuleSetReleasePage.openModuleSetReleaseByName(latestModuleSetRelease);
        editModuleSetReleasePage.setName("Release New Name");
        editModuleSetReleasePage.setDescription("Release New Description");
        editModuleSetReleasePage.hitUpdateButton();
    }

    @Test
    @DisplayName("TC_21_2_TA_4")
    public void test_TA_4() {
        AppUserObject developerA;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
        }
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditModuleSetReleasePage viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        CreateModuleSetReleasePage createModuleSetReleasePage = viewEditModuleSetReleasePage.hitNewModuleSetReleaseButton();
        createModuleSetReleasePage.setName("Module Set Release Test" + randomAlphanumeric(5, 10));
        createModuleSetReleasePage.setDescription("Description Test");
        List<ModuleSetObject> existingModuleSets = getAPIFactory().getModuleSetAPI().getAllModuleSets();
        createModuleSetReleasePage.setModuleSet(existingModuleSets.get(0).getName());

        List<ReleaseObject> existingReleases = getAPIFactory().getReleaseAPI().getReleases();
        createModuleSetReleasePage.setRelease(existingReleases.get(0).getReleaseNumber());
        createModuleSetReleasePage.hitCreateButton();
        waitFor(ofMillis(500L));
        viewEditModuleSetReleasePage.openPage();

        ModuleSetReleaseObject latestModuleSetRelease = getAPIFactory().getModuleSetReleaseAPI().getTheLatestModuleSetReleaseCreatedBy(developerA);
        EditModuleSetReleasePage editModuleSetReleasePage = viewEditModuleSetReleasePage.openModuleSetReleaseByName(latestModuleSetRelease);
        editModuleSetReleasePage.setName("Release New Name");
        editModuleSetReleasePage.setDescription("Release New Description");
        File exportReleaseFile = null;
        try {
            exportReleaseFile = editModuleSetReleasePage.hitExportButton();
        } finally {
            if (exportReleaseFile != null) {
                exportReleaseFile.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_21_2_TA_5")
    public void test_TA_5() {
        AppUserObject developer;
        AppUserObject endUser;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetReleasePage viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        CreateModuleSetReleasePage createModuleSetReleasePage = viewEditModuleSetReleasePage.hitNewModuleSetReleaseButton();
        createModuleSetReleasePage.setName("Module Set Release Test" + randomAlphanumeric(5, 10));
        createModuleSetReleasePage.setDescription("Description Test");
        List<ModuleSetObject> existingModuleSets = getAPIFactory().getModuleSetAPI().getAllModuleSets();
        createModuleSetReleasePage.setModuleSet(existingModuleSets.get(0).getName());

        List<ReleaseObject> existingReleases = getAPIFactory().getReleaseAPI().getReleases();
        createModuleSetReleasePage.setRelease(existingReleases.get(0).getReleaseNumber());
        createModuleSetReleasePage.hitCreateButton();
        homePage.logout();

        waitFor(ofMillis(500L));
        homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ModuleSetReleaseObject latestModuleSetRelease = getAPIFactory().getModuleSetReleaseAPI().getTheLatestModuleSetReleaseCreatedBy(developer);
        viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        EditModuleSetReleasePage editModuleSetReleasePage = viewEditModuleSetReleasePage.openModuleSetReleaseByName(latestModuleSetRelease);
        assertDisabled(editModuleSetReleasePage.getNameField());
        assertDisabled(editModuleSetReleasePage.getDescriptionField());
        assertThrows(TimeoutException.class, () -> editModuleSetReleasePage.getUpdateButton(true));
    }

    @Test
    @DisplayName("TC_21_2_TA_6a")
    public void test_TA_6() {
        AppUserObject developer;
        NamespaceObject namespace;
        CodeListObject codeListCandidate;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            ReleaseObject workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            codeListCandidate = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developer, namespace, workingBranch, "Published");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListCandidate, developer);
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeListCandidate.getName(), "Working");
        editCodeListPage.hitRevise();
        editCodeListPage.setVersion("99");
        editCodeListPage.setDefinition("random code list in candidate state");
        editCodeListPage.hitUpdateButton();
        editCodeListPage.moveToDraft();
        editCodeListPage.moveToCandidate();

        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();

        CreateReleasePage createReleasePage = viewEditReleasePage.createRelease();
        String newReleaseNum = String.valueOf((RandomUtils.nextInt(20230519, 20231231)));
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

        ViewEditModuleSetReleasePage viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        CreateModuleSetReleasePage createModuleSetReleasePage = viewEditModuleSetReleasePage.hitNewModuleSetReleaseButton();
        createModuleSetReleasePage.setName("Module Set Release Test" + randomAlphanumeric(5, 10));
        createModuleSetReleasePage.setDescription("Description Test");
        List<ModuleSetObject> existingModuleSets = getAPIFactory().getModuleSetAPI().getAllModuleSets();

        createModuleSetReleasePage.setModuleSet(existingModuleSets.get(0).getName());

        createModuleSetReleasePage.setRelease(newDraftRelease.getReleaseNumber());
        createModuleSetReleasePage.hitCreateButton();
        waitFor(ofMillis(500L));
        viewEditModuleSetReleasePage.openPage();
        ModuleSetReleaseObject latestModuleSetRelease = getAPIFactory().getModuleSetReleaseAPI().getTheLatestModuleSetReleaseCreatedBy(developer);
        EditModuleSetReleasePage editModuleSetReleasePage = viewEditModuleSetReleasePage.openModuleSetReleaseByName(latestModuleSetRelease);
        CoreComponentAssignmentPage coreComponentAssignmentPage = editModuleSetReleasePage.hitAssignCCsButton(latestModuleSetRelease);
        assertDoesNotThrow(() -> coreComponentAssignmentPage.selectCCByDEN(codeListCandidate.getName()));
        coreComponentAssignmentPage.hitAssignButton();

        viewEditReleasePage.openPage();
        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum,
                "Draft");
        editReleasePage.backToInitialized();
        do {
            newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        } while (!newDraftRelease.getState().equals("Initialized"));
        viewEditModuleSetReleasePage.openPage();
        viewEditModuleSetReleasePage.openModuleSetReleaseByName(latestModuleSetRelease);
        coreComponentAssignmentPage.openPage();
        assertThrows(WebDriverException.class, () -> coreComponentAssignmentPage.selectCCByDEN(codeListCandidate.getName()));
    }

    @Test
    @DisplayName("TC_21_2_TA_7")
    public void test_TA_7() {
        AppUserObject developerA;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
        }
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditModuleSetReleasePage viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        CreateModuleSetReleasePage createModuleSetReleasePage = viewEditModuleSetReleasePage.hitNewModuleSetReleaseButton();
        createModuleSetReleasePage.setName("Module Set Release Test" + randomAlphanumeric(5, 10));
        createModuleSetReleasePage.setDescription("Description Test");
        List<ModuleSetObject> existingModuleSets = getAPIFactory().getModuleSetAPI().getAllModuleSets();
        createModuleSetReleasePage.setModuleSet(existingModuleSets.get(0).getName());

        List<ReleaseObject> existingReleases = getAPIFactory().getReleaseAPI().getReleases();
        createModuleSetReleasePage.setRelease(existingReleases.get(0).getReleaseNumber());
        createModuleSetReleasePage.hitCreateButton();
        waitFor(ofMillis(500L));
        viewEditModuleSetReleasePage.openPage();
        ModuleSetReleaseObject latestModuleSetRelease = getAPIFactory().getModuleSetReleaseAPI().getTheLatestModuleSetReleaseCreatedBy(developerA);
        EditModuleSetReleasePage editModuleSetReleasePage = viewEditModuleSetReleasePage.openModuleSetReleaseByName(latestModuleSetRelease);
        assertDoesNotThrow(() -> editModuleSetReleasePage.hitValidateButton());
    }

    @Test
    @DisplayName("TC_21_2_TA_8")
    public void test_TA_8() {
        AppUserObject developerA;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
        }
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditModuleSetReleasePage viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        CreateModuleSetReleasePage createModuleSetReleasePage = viewEditModuleSetReleasePage.hitNewModuleSetReleaseButton();
        createModuleSetReleasePage.setName("Module Set Release Test" + randomAlphanumeric(5, 10));
        createModuleSetReleasePage.setDescription("Description Test");
        List<ModuleSetObject> existingModuleSets = getAPIFactory().getModuleSetAPI().getAllModuleSets();
        createModuleSetReleasePage.setModuleSet(existingModuleSets.get(0).getName());

        List<ReleaseObject> existingReleases = getAPIFactory().getReleaseAPI().getReleases();
        createModuleSetReleasePage.setRelease(existingReleases.get(0).getReleaseNumber());
        createModuleSetReleasePage.hitCreateButton();
        waitFor(ofMillis(500L));
        viewEditModuleSetReleasePage.openPage();
        ModuleSetReleaseObject latestModuleSetRelease = getAPIFactory().getModuleSetReleaseAPI().getTheLatestModuleSetReleaseCreatedBy(developerA);
        EditModuleSetReleasePage editModuleSetReleasePage = viewEditModuleSetReleasePage.openModuleSetReleaseByName(latestModuleSetRelease);
        ModuleSetReleaseXMLSchemaValidationDialog validateDialog = editModuleSetReleasePage.hitValidateButton();
        waitFor(Duration.ofSeconds(30));
        validateDialog.hitCopyToClipboardButton();
        assertTrue(getSnackBarMessage(getDriver()).equals("Copied to clipboard"));
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        // Delete random accounts
        this.randomAccounts.forEach(newUser -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId());
        });
    }
}
