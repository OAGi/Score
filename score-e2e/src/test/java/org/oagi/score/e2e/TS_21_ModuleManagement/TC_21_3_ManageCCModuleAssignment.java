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
import org.openqa.selenium.WebDriverException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

@Execution(ExecutionMode.CONCURRENT)
public class TC_21_3_ManageCCModuleAssignment extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_21_3_TA_3")
    public void test_TA_3() {
        AppUserObject developer;
        NamespaceObject namespace;
        ACCObject newACC;
        ASCCPObject newASCCP;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            ReleaseObject workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            newACC = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, workingBranch, namespace, "Candidate");
            newASCCP = getAPIFactory().getCoreComponentAPI().createRandomASCCP(newACC, developer, namespace, "Candidate");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());

        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage =  viewEditModuleSetPage.hitNewModuleSetButton();
        createModuleSetPage.setName("New Module Set" + randomAlphanumeric(5, 10));
        createModuleSetPage.setDescription("Description");
        createModuleSetPage.hitCreateButton();
        waitFor(ofMillis(500L));

        ModuleSetObject moduleSet = getAPIFactory().getModuleSetAPI().getTheLatestModuleSetCreatedBy(developer);
        EditModuleSetPage editModuleSetPage =  viewEditModuleSetPage.openModuleSetByName(moduleSet);
        editModuleSetPage.addModule();
        CopyModuleFromExistingModuleSetDialog copyModuleFromExistingModuleSetDialog =
                editModuleSetPage.copyFromExistingModuleSet();
        List<ModuleSetObject> existingModuleSet = getAPIFactory().getModuleSetAPI().getAllModuleSets();
        ModuleSetObject selectedMduleSet = existingModuleSet.get(0);
        copyModuleFromExistingModuleSetDialog.setModuleSet
                (selectedMduleSet.getName());
        List<ModuleObject> modules = getAPIFactory().getModuleAPI().getModulesByModuleSet(selectedMduleSet.getModuleSetId());
        ModuleObject selectedModule = modules.get(modules.size()-1);
        copyModuleFromExistingModuleSetDialog.selectModule(selectedModule.getName());
        copyModuleFromExistingModuleSetDialog.copyModule();
        waitFor(Duration.ofSeconds(30));

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
        createModuleSetReleasePage.setModuleSet(moduleSet.getName());
        createModuleSetReleasePage.setRelease(newDraftRelease.getReleaseNumber());
        createModuleSetReleasePage.hitCreateButton();
        waitFor(ofMillis(500L));
        viewEditModuleSetReleasePage.openPage();
        ModuleSetReleaseObject latestModuleSetRelease = getAPIFactory().getModuleSetReleaseAPI().getTheLatestModuleSetReleaseCreatedBy(developer);
        EditModuleSetReleasePage editModuleSetReleasePage = viewEditModuleSetReleasePage.openModuleSetReleaseByName(latestModuleSetRelease);
        CoreComponentAssignmentPage coreComponentAssignmentPage = editModuleSetReleasePage.hitAssignCCsButton(latestModuleSetRelease);
        /**
         * Test Assertion #21.3.3.a
         */
        coreComponentAssignmentPage.selectModule("OAGIS");
        coreComponentAssignmentPage.selectUnassignedCCByDEN(newACC.getDen());
        coreComponentAssignmentPage.hitAssignButton();
        assertDoesNotThrow(() -> coreComponentAssignmentPage.selectAssignedCCByDEN(newACC.getDen()));
        coreComponentAssignmentPage.hitUnassignButton();
        coreComponentAssignmentPage.openPage();

        /**
         * Test Assertion #21.3.3.b
         */
        click(coreComponentAssignmentPage.getColumnByName(coreComponentAssignmentPage.getTableRecordAtIndexUnassignedCC(1), "checkbox"));
        click(coreComponentAssignmentPage.getColumnByName(coreComponentAssignmentPage.getTableRecordAtIndexUnassignedCC(2), "checkbox"));
        coreComponentAssignmentPage.hitAssignButton();
        coreComponentAssignmentPage.openPage();

        /**
         * Test Assertion #21.3.3.f
         */
        click(coreComponentAssignmentPage.getColumnByName(coreComponentAssignmentPage.getTableRecordAtIndexAssignedCC(1), "checkbox"));
        click(coreComponentAssignmentPage.getColumnByName(coreComponentAssignmentPage.getTableRecordAtIndexAssignedCC(2), "checkbox"));
        coreComponentAssignmentPage.hitUnassignButton();
    }

    @Test
    @DisplayName("TC_21_3_TA_4")
    public void test_TA_4() {
        AppUserObject developer;
        AppUserObject endUser;
        NamespaceObject namespace;
        ACCObject newACC;
        ASCCPObject newASCCP;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);
            namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            ReleaseObject workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            newACC = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, workingBranch, namespace, "Candidate");
            newASCCP = getAPIFactory().getCoreComponentAPI().createRandomASCCP(newACC, developer, namespace, "Candidate");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());

        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage =  viewEditModuleSetPage.hitNewModuleSetButton();
        createModuleSetPage.setName("New Module Set" + randomAlphanumeric(5, 10));
        createModuleSetPage.setDescription("Description");
        createModuleSetPage.hitCreateButton();
        waitFor(ofMillis(500L));

        ModuleSetObject moduleSet = getAPIFactory().getModuleSetAPI().getTheLatestModuleSetCreatedBy(developer);
        EditModuleSetPage editModuleSetPage =  viewEditModuleSetPage.openModuleSetByName(moduleSet);
        editModuleSetPage.addModule();
        CopyModuleFromExistingModuleSetDialog copyModuleFromExistingModuleSetDialog =
                editModuleSetPage.copyFromExistingModuleSet();
        List<ModuleSetObject> existingModuleSet = getAPIFactory().getModuleSetAPI().getAllModuleSets();
        ModuleSetObject selectedMduleSet = existingModuleSet.get(0);
        copyModuleFromExistingModuleSetDialog.setModuleSet
                (selectedMduleSet.getName());
        List<ModuleObject> modules = getAPIFactory().getModuleAPI().getModulesByModuleSet(selectedMduleSet.getModuleSetId());
        ModuleObject selectedModule = modules.get(modules.size()-1);
        copyModuleFromExistingModuleSetDialog.selectModule(selectedModule.getName());
        copyModuleFromExistingModuleSetDialog.copyModule();
        waitFor(Duration.ofSeconds(30));

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
        createModuleSetReleasePage.setModuleSet(moduleSet.getName());
        createModuleSetReleasePage.setRelease(newDraftRelease.getReleaseNumber());
        createModuleSetReleasePage.hitCreateButton();
        waitFor(ofMillis(500L));
        viewEditModuleSetReleasePage.openPage();
        homePage.logout();

        homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        ModuleSetReleaseObject latestModuleSetRelease = getAPIFactory().getModuleSetReleaseAPI().getTheLatestModuleSetReleaseCreatedBy(developer);
        EditModuleSetReleasePage editModuleSetReleasePage = viewEditModuleSetReleasePage.openModuleSetReleaseByName(latestModuleSetRelease);
        CoreComponentAssignmentPage coreComponentAssignmentPage = editModuleSetReleasePage.viewAssignedCCs(latestModuleSetRelease);
        coreComponentAssignmentPage.selectModule("OAGIS");
        coreComponentAssignmentPage.selectUnassignedCCByDEN(newACC.getDen());
        assertThrows(WebDriverException.class, () -> coreComponentAssignmentPage.hitAssignButton());
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
