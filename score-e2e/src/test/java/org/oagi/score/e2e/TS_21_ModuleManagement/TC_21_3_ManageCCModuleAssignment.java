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
import org.openqa.selenium.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertEnabled;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.SAME_THREAD)
public class TC_21_3_ManageCCModuleAssignment extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        // Delete random accounts
        this.randomAccounts.forEach(newUser -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId());
        });
    }

    @Test
    @DisplayName("TC_21_2_TA_6a_and_6c")
    public void test_TA_6a_and_6c() {
        /*
         * This test case has been separated from TC_21_2 because Score allows only one active release.
         * Thus, it should be executed in this class to use the SAME_THREAD concurrent condition.
         */
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
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeListCandidate);
        editCodeListPage.hitRevise();
        editCodeListPage.setVersion("99");
        editCodeListPage.setDefinition("random code list in candidate state");
        editCodeListPage.hitUpdateButton();
        editCodeListPage.moveToDraft();
        editCodeListPage.moveToCandidate();

        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        viewEditReleasePage.setState("Draft");
        viewEditReleasePage.hitSearchButton();
        if (viewEditReleasePage.getTotalNumberOfItems() > 0){
            long timeout = Duration.ofSeconds(300L).toMillis();
            long begin = System.currentTimeMillis();
            while (System.currentTimeMillis() - begin < timeout) {
                viewEditReleasePage.openPage();
                viewEditReleasePage.setState("Draft");
                viewEditReleasePage.hitSearchButton();

                WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
                EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPage(tr);
                editReleasePage.backToInitialized();
                String state = getText(viewEditReleasePage.getColumnByName(tr, "state"));
                assertNotEquals("Draft", state);
                if ("Initialized".equals(state)) {
                    break;
                }
            }
        }

        CreateReleasePage createReleasePage = viewEditReleasePage.createRelease();
        String newReleaseNum = String.valueOf((RandomUtils.nextInt(20230519, 20231231)));
        createReleasePage.setReleaseNumber(newReleaseNum);
        createReleasePage.setReleaseNamespace(namespace);
        createReleasePage.hitCreateButton();

        viewEditReleasePage.openPage();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
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

        ReleaseObject newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        assertEquals("Draft", newDraftRelease.getState());

        /**
         * Test Assertion #21.2.6
         */
        ViewEditModuleSetReleasePage viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        CreateModuleSetReleasePage createModuleSetReleasePage = viewEditModuleSetReleasePage.hitNewModuleSetReleaseButton();
        String moduleSetReleaseName = "Test Module Set Release " + randomAlphanumeric(5, 10);
        createModuleSetReleasePage.setName(moduleSetReleaseName);
        String description = randomPrint(50, 100);
        createModuleSetReleasePage.setDescription(description);
        createModuleSetReleasePage.setModuleSet("10.8.1");
        createModuleSetReleasePage.setRelease(newDraftRelease.getReleaseNumber());
        createModuleSetReleasePage.hitCreateButton();

        viewEditModuleSetReleasePage.openPage();
        ModuleSetReleaseObject moduleSetRelease = getAPIFactory().getModuleSetReleaseAPI().getModuleSetReleaseByName(moduleSetReleaseName);
        EditModuleSetReleasePage editModuleSetReleasePage = viewEditModuleSetReleasePage.openModuleSetReleaseByName(moduleSetRelease);
        CoreComponentAssignmentPage coreComponentAssignmentPage = editModuleSetReleasePage.hitAssignCCsButton(moduleSetRelease);
        assertDoesNotThrow(() -> coreComponentAssignmentPage.selectUnassignedCCByDEN(codeListCandidate.getName()));
        coreComponentAssignmentPage.hitAssignButton();

        /**
         * Test Assertion #21.2.6.a
         */
        viewEditReleasePage.openPage();
        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Draft");
        editReleasePage.backToInitialized();

        begin = System.currentTimeMillis();
        while (System.currentTimeMillis() - begin < timeout) {
            viewEditReleasePage.openPage();
            viewEditReleasePage.setReleaseNum(newReleaseNum);
            viewEditReleasePage.hitSearchButton();

            WebElement tr = viewEditReleasePage.getTableRecordAtIndex(1);
            if ("Initialized".equals(getText(viewEditReleasePage.getColumnByName(tr, "state")))) {
                break;
            }
        }

        newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        assertEquals("Initialized", newDraftRelease.getState());

        viewEditModuleSetReleasePage.openPage();
        viewEditModuleSetReleasePage.openModuleSetReleaseByName(moduleSetRelease);
        coreComponentAssignmentPage.openPage();
        assertThrows(WebDriverException.class, () -> coreComponentAssignmentPage.selectUnassignedCCByDEN(codeListCandidate.getName()));

        /**
         * Test Assertion #21.2.6.c
         */
        viewEditReleasePage.openPage();
        editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Initialized");
        editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitCreateButton();

        begin = System.currentTimeMillis();
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

        viewEditModuleSetReleasePage.openPage();
        viewEditModuleSetReleasePage.openModuleSetReleaseByName(moduleSetRelease);

        coreComponentAssignmentPage.openPage();
        coreComponentAssignmentPage.setDenUnassigned(codeListCandidate.getName());
        WebElement tr = coreComponentAssignmentPage.getTableRecordAtIndexUnassignedCC(1);
        WebElement td = coreComponentAssignmentPage.getColumnByName(tr, "den");
        String denColumn = getText(td.findElement(By.tagName("a")));
        assertTrue(denColumn.contains(codeListCandidate.getName()));
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

        String moduleSetName = "Test Module " + randomAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        createModuleSetPage.setDescription(randomPrint(50, 100));
        createModuleSetPage.hitCreateButton();

        ModuleSetObject moduleSet = getAPIFactory().getModuleSetAPI().getModuleSetByName(moduleSetName);
        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSet);
        editModuleSetPage.addModule();
        CopyModuleFromExistingModuleSetDialog copyModuleFromExistingModuleSetDialog =
                editModuleSetPage.copyFromExistingModuleSet();
        List<ModuleSetObject> existingModuleSet = getAPIFactory().getModuleSetAPI().getAllModuleSets();
        ModuleSetObject selectedModuleSet = existingModuleSet.get(0);
        copyModuleFromExistingModuleSetDialog.setModuleSet(selectedModuleSet.getName());
        List<ModuleObject> modules = getAPIFactory().getModuleAPI().getModulesByModuleSet(selectedModuleSet.getModuleSetId());
        ModuleObject selectedModule = modules.get(modules.size() - 1);
        copyModuleFromExistingModuleSetDialog.selectModule(selectedModule.getName());
        copyModuleFromExistingModuleSetDialog.copyModule();

        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();

        CreateReleasePage createReleasePage = viewEditReleasePage.createRelease();
        String newReleaseNum = String.valueOf((RandomUtils.nextInt(20230519, 20231231)));
        createReleasePage.setReleaseNumber(newReleaseNum);
        createReleasePage.setReleaseNamespace(namespace);
        createReleasePage.hitCreateButton();
        viewEditReleasePage.openPage();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
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

        ReleaseObject newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        assertEquals("Draft", newDraftRelease.getState());

        ViewEditModuleSetReleasePage viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        CreateModuleSetReleasePage createModuleSetReleasePage = viewEditModuleSetReleasePage.hitNewModuleSetReleaseButton();
        String moduleSetReleaseName = "Test Module Set Release " + randomAlphanumeric(5, 10);
        createModuleSetReleasePage.setName(moduleSetReleaseName);
        createModuleSetReleasePage.setDescription(randomPrint(50, 100));
        createModuleSetReleasePage.setModuleSet(moduleSet.getName());
        createModuleSetReleasePage.setRelease(newDraftRelease.getReleaseNumber());
        createModuleSetReleasePage.hitCreateButton();

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

        coreComponentAssignmentPage.openPage();
        coreComponentAssignmentPage.selectModule("OAGIS");
        coreComponentAssignmentPage.selectAssignedCCByDEN(newACC.getDen());
        coreComponentAssignmentPage.hitUnassignButton();

        /**
         * Test Assertion #21.3.3.b
         */
        coreComponentAssignmentPage.openPage();
        coreComponentAssignmentPage.selectModule("OAGIS");
        click(coreComponentAssignmentPage.getColumnByName(coreComponentAssignmentPage.getTableRecordAtIndexUnassignedCC(1), "checkbox"));
        waitFor(ofMillis(1000L));
        click(coreComponentAssignmentPage.getColumnByName(coreComponentAssignmentPage.getTableRecordAtIndexUnassignedCC(2), "checkbox"));
        waitFor(ofMillis(1000L));

        coreComponentAssignmentPage.hitAssignButton();

        /**
         * Test Assertion #21.3.3.f
         */
        coreComponentAssignmentPage.openPage();
        coreComponentAssignmentPage.selectModule("OAGIS");
        click(coreComponentAssignmentPage.getColumnByName(coreComponentAssignmentPage.getTableRecordAtIndexAssignedCC(1), "checkbox"));
        waitFor(ofMillis(1000L));
        click(coreComponentAssignmentPage.getColumnByName(coreComponentAssignmentPage.getTableRecordAtIndexAssignedCC(2), "checkbox"));
        waitFor(ofMillis(1000L));

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

        String moduleSetName = "Test Module " + randomAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        createModuleSetPage.setDescription(randomPrint(50, 100));
        createModuleSetPage.hitCreateButton();

        ModuleSetObject moduleSet = getAPIFactory().getModuleSetAPI().getTheLatestModuleSetCreatedBy(developer);
        EditModuleSetPage editModuleSetPage =  viewEditModuleSetPage.openModuleSetByName(moduleSet);
        editModuleSetPage.addModule();
        CopyModuleFromExistingModuleSetDialog copyModuleFromExistingModuleSetDialog =
                editModuleSetPage.copyFromExistingModuleSet();
        List<ModuleSetObject> existingModuleSet = getAPIFactory().getModuleSetAPI().getAllModuleSets();
        ModuleSetObject selectedModuleSet = existingModuleSet.get(0);
        copyModuleFromExistingModuleSetDialog.setModuleSet(selectedModuleSet.getName());
        List<ModuleObject> modules = getAPIFactory().getModuleAPI().getModulesByModuleSet(selectedModuleSet.getModuleSetId());
        ModuleObject selectedModule = modules.get(modules.size()-1);
        copyModuleFromExistingModuleSetDialog.selectModule(selectedModule.getName());
        copyModuleFromExistingModuleSetDialog.copyModule();

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

        ReleaseObject newDraftRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);
        assertEquals("Draft", newDraftRelease.getState());

        ViewEditModuleSetReleasePage viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        CreateModuleSetReleasePage createModuleSetReleasePage = viewEditModuleSetReleasePage.hitNewModuleSetReleaseButton();
        createModuleSetReleasePage.setName("Module Set Release Test" + randomAlphanumeric(5, 10));
        createModuleSetReleasePage.setDescription("Description Test");
        createModuleSetReleasePage.setModuleSet(moduleSet.getName());
        createModuleSetReleasePage.setRelease(newDraftRelease.getReleaseNumber());
        createModuleSetReleasePage.hitCreateButton();
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
}
