package org.oagi.score.e2e.TS_21_ModuleManagement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.MultiActionSnackBar;
import org.oagi.score.e2e.page.module.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_21_1_ManageModuleSet extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_21_1_TA_1")
    public void test_TA_1() {
        AppUserObject developer;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        assertNotNull(viewEditModuleSetPage.getNewModuleSetButton());
    }

    @Test
    @DisplayName("TC_21_1_TA_2")
    public void test_TA_2() {
        AppUserObject developer;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();

        assertEquals("true", createModuleSetPage.getNameField().getAttribute("aria-required"));
        assertEquals("false", createModuleSetPage.getDescriptionField().getAttribute("aria-required"));

        String moduleSetName = "Test Module " + randomAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = randomPrint(50, 100);
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();

        viewEditModuleSetPage.openPage();
        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);
        assertTrue(getText(editModuleSetPage.getDescriptionField()).contains(description));
    }

    @Test
    @DisplayName("TC_21_1_TA_2_a")
    public void test_TA_2_a() {
        AppUserObject developer;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();

        String moduleSetName = "Test Module " + randomAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = randomPrint(50, 100);
        createModuleSetPage.setDescription(description);

        createModuleSetPage.toggleCreateModuleSetRelease();
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");
        createModuleSetPage.setRelease(release.getReleaseNumber());
        createModuleSetPage.setModuleSetRelease(release.getReleaseNumber());
        createModuleSetPage.hitCreateButton();

        viewEditModuleSetPage.openPage();
        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);
        assertTrue(getText(editModuleSetPage.getDescriptionField()).contains(description));

        ViewEditModuleSetReleasePage viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        EditModuleSetReleasePage editModuleSetReleasePage = viewEditModuleSetReleasePage.openModuleSetReleaseByName(moduleSetName);
        assertEquals(moduleSetName, getText(editModuleSetReleasePage.getModuleSetSelectField()));
        assertEquals(release.getReleaseNumber() + " " + release.getState(), getText(editModuleSetReleasePage.getReleaseSelectField()));
    }

    @Test
    @DisplayName("TC_21_1_TA_3")
    public void test_TA_3() {
        AppUserObject developer;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();
        String moduleSetName = "Test Module Set " + randomAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = randomPrint(50, 100);
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();

        viewEditModuleSetPage.openPage();
        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);
        assertTrue(getText(editModuleSetPage.getDescriptionField()).contains(description));

        String newModuleSetName = "Updated Test Module Set " + randomAlphanumeric(5, 10);
        editModuleSetPage.setName(newModuleSetName);
        String newDescription = randomPrint(50, 100);
        editModuleSetPage.setDescription(newDescription);
        editModuleSetPage.hitUpdateButton();

        viewEditModuleSetPage.openPage();
        editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(newModuleSetName);
        assertTrue(getText(editModuleSetPage.getDescriptionField()).contains(newDescription));
    }

    @Test
    @DisplayName("TC_21_1_TA_4_a")
    public void test_TA_4_a() {
        AppUserObject developer;
        NamespaceObject namespace;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();
        String moduleSetName = "Test Module Set " + randomAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = randomPrint(50, 100);
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();

        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);
        editModuleSetPage.addModule();
        CreateModuleFileDialog createModuleFileDialog = editModuleSetPage.addNewModuleFile();
        assertEquals("true", createModuleFileDialog.getModuleFileNameField().getAttribute("aria-required"));
        String moduleFileName = "New module file" + randomAlphanumeric(5, 10);
        createModuleFileDialog.setModuleFileName(moduleFileName);
        createModuleFileDialog.setNamespace(namespace.getUri());
        String version = "Version " + randomAlphanumeric(5, 10);
        createModuleFileDialog.setModuleFileVersionNumber(version);
        assertEquals("false", createModuleFileDialog.getNamespaceField().getAttribute("aria-required"));
        assertEquals("false", createModuleFileDialog.getModuleFileVersionNumberField().getAttribute("aria-required"));
        createModuleFileDialog.createModuleFile();

        EditModuleFileDialog editModuleFileDialog = editModuleSetPage.editModuleFile(moduleFileName);
        assertEquals(namespace.getUri(), getText(editModuleFileDialog.getNamespaceSelectField()));
        assertEquals(version, getText(editModuleFileDialog.getModuleFileVersionNumberField()));
    }

    @Test
    @DisplayName("TC_21_1_TA_4_b")
    public void test_TA_4_b() {
        AppUserObject developer;
        NamespaceObject namespace;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();
        String moduleSetName = "Test Module Set " + randomAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = randomPrint(50, 100);
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();

        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);
        editModuleSetPage.addModule();
        CreateModuleFileDialog createModuleFileDialog = editModuleSetPage.addNewModuleFile();
        assertEquals("true", createModuleFileDialog.getModuleFileNameField().getAttribute("aria-required"));
        String moduleFileName = "New module file" + randomAlphanumeric(5, 10);
        createModuleFileDialog.setModuleFileName(moduleFileName);
        createModuleFileDialog.setNamespace(namespace.getUri());
        String version = "Version " + randomAlphanumeric(5, 10);
        createModuleFileDialog.setModuleFileVersionNumber(version);
        assertEquals("false", createModuleFileDialog.getNamespaceField().getAttribute("aria-required"));
        assertEquals("false", createModuleFileDialog.getModuleFileVersionNumberField().getAttribute("aria-required"));
        createModuleFileDialog.createModuleFile();

        editModuleSetPage.addModule();
        createModuleFileDialog = editModuleSetPage.addNewModuleFile();
        createModuleFileDialog.setModuleFileName(moduleFileName);
        createModuleFileDialog.setNamespace(namespace.getUri());
        createModuleFileDialog.setModuleFileVersionNumber(version);
        CreateModuleFileDialog finalCreateModuleFileDialog = createModuleFileDialog;
        assertThrows(TimeoutException.class, () -> finalCreateModuleFileDialog.createModuleFile());
        MultiActionSnackBar multiActionSnackBar = getMultiActionSnackBar(getDriver());
        assertTrue(getText(multiActionSnackBar.getMessageElement()).contains("Duplicate module name exist."));

        editModuleSetPage.openPage();
        EditModuleFileDialog editModuleFileDialog = editModuleSetPage.editModuleFile(moduleFileName);
        String newModuleFileName = "Changed module file" + randomAlphanumeric(5, 10);
        editModuleFileDialog.setModuleFileName(newModuleFileName);
        String newVersion = "Version " + randomAlphanumeric(5, 10);
        editModuleFileDialog.setModuleFileVersionNumber(newVersion);
        editModuleFileDialog.updateModuleFile();

        editModuleFileDialog = editModuleSetPage.editModuleFile(newModuleFileName);
        assertEquals(namespace.getUri(), getText(editModuleFileDialog.getNamespaceSelectField()));
        assertEquals(newVersion, getText(editModuleFileDialog.getModuleFileVersionNumberField()));
    }

    @Test
    @DisplayName("TC_21_1_TA_4_c")
    public void test_TA_4_c() {
        AppUserObject developer;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();
        String moduleSetName = "Test Module Set " + randomAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = randomPrint(50, 100);
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();

        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);
        editModuleSetPage.addModule();
        CreateModuleDirectoryDialog createModuleDirectoryDialog = editModuleSetPage.addNewModuleDirectory();
        String moduleDirectoryName = "Directory " + randomAlphanumeric(5, 10);
        assertEquals("true", createModuleDirectoryDialog.getModuleDirectoryNameField().getAttribute("aria-required"));
        createModuleDirectoryDialog.setModuleDirectoryName(moduleDirectoryName);
        createModuleDirectoryDialog.createModuleDirectory();

        EditModuleDirectoryDialog editModuleDirectoryDialog = editModuleSetPage.editModuleDirectory(moduleDirectoryName);
        assertEquals(moduleDirectoryName, getText(editModuleDirectoryDialog.getModuleDirectoryNameField()));
    }

    @Test
    @DisplayName("TC_21_1_TA_4_d")
    public void test_TA_4_d() {
        AppUserObject developer;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();
        String moduleSetName = "Test Module Set " + randomAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = randomPrint(50, 100);
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();

        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);
        editModuleSetPage.addModule();
        CreateModuleDirectoryDialog createModuleDirectoryDialog = editModuleSetPage.addNewModuleDirectory();
        String moduleDirectoryName = "Directory " + randomAlphanumeric(5, 10);
        createModuleDirectoryDialog.setModuleDirectoryName(moduleDirectoryName);
        createModuleDirectoryDialog.createModuleDirectory();

        editModuleSetPage.addModule();
        createModuleDirectoryDialog = editModuleSetPage.addNewModuleDirectory();
        createModuleDirectoryDialog.setModuleDirectoryName(moduleDirectoryName);
        CreateModuleDirectoryDialog finalCreateModuleDirectoryDialog = createModuleDirectoryDialog;
        assertThrows(TimeoutException.class, () -> finalCreateModuleDirectoryDialog.createModuleDirectory());
        MultiActionSnackBar multiActionSnackBar = getMultiActionSnackBar(getDriver());
        assertTrue(getText(multiActionSnackBar.getMessageElement()).contains("Duplicate module name exist."));

        editModuleSetPage.openPage();
        EditModuleDirectoryDialog editModuleDirectoryDialog = editModuleSetPage.editModuleDirectory(moduleDirectoryName);
        String newModuleDirectoryName = "Changed Directory " + randomAlphanumeric(5, 10);
        editModuleDirectoryDialog.setModuleDirectoryName(newModuleDirectoryName);
        editModuleDirectoryDialog.updateModuleDirectory();

        editModuleDirectoryDialog = editModuleSetPage.editModuleDirectory(newModuleDirectoryName);
        assertEquals(newModuleDirectoryName, getText(editModuleDirectoryDialog.getModuleDirectoryNameField()));
    }

    @Test
    @DisplayName("TC_21_1_TA_4_e")
    public void test_TA_4_e() {
        AppUserObject developer;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();
        String moduleSetName = "Test Module Set " + randomAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = randomPrint(50, 100);
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();

        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);
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

        assertNotNull(editModuleSetPage.getModuleByName(selectedModule.getName()));
        click(editModuleSetPage.getModuleByName(selectedModule.getName()));
        List<ModuleObject> submodules = getAPIFactory().getModuleAPI().getSubmodules(selectedModule.getModuleId());
        for (ModuleObject submodule : submodules) {
            assertNotNull(editModuleSetPage.getModuleByName(submodule.getName()));
        }
    }

    @Test
    @DisplayName("TC_21_1_TA_4_f")
    public void test_TA_4_f() {
        AppUserObject developer;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();
        String moduleSetName = "Test Module Set " + randomAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = randomPrint(50, 100);
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();

        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);
        editModuleSetPage.addModule();
        CopyModuleFromExistingModuleSetDialog copyModuleFromExistingModuleSetDialog = editModuleSetPage.copyFromExistingModuleSet();
        List<ModuleSetObject> existingModuleSet = getAPIFactory().getModuleSetAPI().getAllModuleSets();
        ModuleSetObject selectedModuleSet = existingModuleSet.get(1);
        copyModuleFromExistingModuleSetDialog.setModuleSet(selectedModuleSet.getName());
        List<ModuleObject> modules = getAPIFactory().getModuleAPI().getModulesByModuleSet(selectedModuleSet.getModuleSetId());
        ModuleObject selectedModuleSecond = modules.get(modules.size() - 1);
        List<ModuleObject> submodules = getAPIFactory().getModuleAPI().getSubmodules(selectedModuleSecond.getModuleId());
        ModuleObject selectedSubmodule = submodules.get(0);
        copyModuleFromExistingModuleSetDialog.selectModule(selectedModuleSecond.getName());
        copyModuleFromExistingModuleSetDialog.selectModule(selectedSubmodule.getName());
        copyModuleFromExistingModuleSetDialog.toggleCopyAllSubmodules();
        copyModuleFromExistingModuleSetDialog.copyModule();

        assertThrows(TimeoutException.class, () -> editModuleSetPage.getModuleByName(selectedModuleSecond.getName()));
        assertNotNull(editModuleSetPage.getModuleByName(selectedSubmodule.getName()));
    }

    @Test
    @DisplayName("TC_21_1_TA_5_a_and_b")
    public void test_TA_5_a_and_b() {
        AppUserObject developer;
        NamespaceObject namespace;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();
        String moduleSetName = "Test Module Set " + randomAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = randomPrint(50, 100);
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();

        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);
        editModuleSetPage.addModule();
        CreateModuleFileDialog createModuleFileDialog = editModuleSetPage.addNewModuleFile();
        String moduleFileName = "New module file" + randomAlphanumeric(5, 10);
        createModuleFileDialog.setModuleFileName(moduleFileName);
        createModuleFileDialog.setNamespace(namespace.getUri());
        String version = "Version " + randomAlphanumeric(5, 10);
        createModuleFileDialog.setModuleFileVersionNumber(version);
        createModuleFileDialog.createModuleFile();

        editModuleSetPage.openPage();
        EditModuleFileDialog editModuleFileDialog = editModuleSetPage.editModuleFile(moduleFileName);
        click(editModuleFileDialog.getDiscardModuleFileButton());
        String messageFileDiscard = "The CC assigned to this file will also be deleted.";
        assertEquals(messageFileDiscard, editModuleFileDialog.getDiscardFileMessage());
        click(editModuleFileDialog.getContinueToDiscardFileButton());
        assertThrows(TimeoutException.class, () -> editModuleSetPage.getModuleByName(moduleFileName));

        editModuleSetPage.addModule();
        CreateModuleDirectoryDialog createModuleDirectoryDialog = editModuleSetPage.addNewModuleDirectory();
        String moduleDirectoryName = "Directory " + randomAlphanumeric(5, 10);
        createModuleDirectoryDialog.setModuleDirectoryName(moduleDirectoryName);
        createModuleDirectoryDialog.createModuleDirectory();

        editModuleSetPage.openPage();
        EditModuleDirectoryDialog editModuleDirectoryDialog = editModuleSetPage.editModuleDirectory(moduleDirectoryName);
        click(editModuleDirectoryDialog.getDiscardModuleDirectoryButton());

        String messageDirectoryDiscard = "Are you sure you want to discard this and sub modules?";
        assertEquals(messageDirectoryDiscard, editModuleDirectoryDialog.getDiscardDirectoryMessage());
        click(editModuleDirectoryDialog.getContinueToDiscardDirectoryButton());
        assertThrows(TimeoutException.class, () -> editModuleSetPage.getModuleByName(moduleDirectoryName));
    }

    @Test
    @DisplayName("TC_21_1_TA_5_c")
    public void test_TA_5_c() {
    }

    @Test
    @DisplayName("TC_21_1_TA_6")
    public void test_TA_6() {
        AppUserObject developer;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();
        String moduleSetName = "Test Module Set " + randomAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = randomPrint(50, 100);
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();

        viewEditModuleSetPage.openPage();
        viewEditModuleSetPage.discardModuleSet(moduleSetName);
        assertThrows(NoSuchElementException.class, () -> viewEditModuleSetPage.openModuleSetByName(moduleSetName));
    }

    @Test
    @DisplayName("TC_21_1_TA_7")
    public void test_TA_7() {
        AppUserObject developer;
        ReleaseObject release;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();
        String moduleSetName = "Test Module Set " + randomAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = randomPrint(50, 100);
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();

        ViewEditModuleSetReleasePage viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        CreateModuleSetReleasePage createModuleSetReleasePage = viewEditModuleSetReleasePage.hitNewModuleSetReleaseButton();
        String moduleSetReleaseName = "Test Module Set Release for " + moduleSetName;
        createModuleSetReleasePage.setName(moduleSetReleaseName);
        String moduleSetReleaseDescription = randomPrint(50, 100);
        createModuleSetReleasePage.setDescription(moduleSetReleaseDescription);
        createModuleSetReleasePage.setModuleSet(moduleSetName);
        createModuleSetReleasePage.setRelease(release.getReleaseNumber());
        createModuleSetReleasePage.hitCreateButton();

        viewEditModuleSetPage.openPage();
        assertThrows(TimeoutException.class, () -> viewEditModuleSetPage.discardModuleSet(moduleSetName));
        MultiActionSnackBar multiActionSnackBar = getMultiActionSnackBar(getDriver());
        assertTrue(getText(multiActionSnackBar.getMessageElement()).contains("Module set in use cannot be discarded."));
    }

    @Test
    @DisplayName("TC_21_1_TA_8")
    public void test_TA_8() {
        AppUserObject developer;
        AppUserObject endUser;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();
        String moduleSetName = "Test Module Set " + randomAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = randomPrint(50, 100);
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();
        homePage.logout();

        homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);
        assertThrows(WebDriverException.class, () -> editModuleSetPage.setName("New Name EU"));
        assertThrows(WebDriverException.class, () -> editModuleSetPage.setDescription("New Description EU"));
        assertThrows(WebDriverException.class, () -> editModuleSetPage.hitUpdateButton());
    }

    @Test
    @DisplayName("TC_21_1_TA_9")
    public void test_TA_9() {
        AppUserObject developerA;
        AppUserObject developerB;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();
        String moduleSetName = "Test Module Set " + randomAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = randomPrint(50, 100);
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();
        homePage.logout();

        homePage = loginPage().signIn(developerB.getLoginId(), developerB.getPassword());
        viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);

        String newModuleSetName = "Updated Test Module Set " + randomAlphanumeric(5, 10);
        editModuleSetPage.setName(newModuleSetName);
        String newDescription = randomPrint(50, 100);
        editModuleSetPage.setDescription(newDescription);
        editModuleSetPage.hitUpdateButton();
        homePage.logout();

        homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(newModuleSetName);
        assertEquals(newDescription, getText(editModuleSetPage.getDescriptionField()));
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
