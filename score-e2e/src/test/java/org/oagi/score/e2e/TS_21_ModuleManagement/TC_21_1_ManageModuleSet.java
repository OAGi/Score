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
import org.oagi.score.e2e.page.module.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;

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
        assertDoesNotThrow(() -> viewEditModuleSetPage.getNewModuleSetButton());
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
        CreateModuleSetPage createModuleSetPage =  viewEditModuleSetPage.hitNewModuleSetButton();
        /**
         * Test Assertion #21.1.2
         */
        assertEquals("true", createModuleSetPage.getNameField().getAttribute("aria-required"));
        assertEquals("false", createModuleSetPage.getDescriptionField().getAttribute("aria-required"));
        createModuleSetPage.setName("new module");
        createModuleSetPage.setDescription("Description");
        /**
         * Test Assertion #21.1.2.a
         */
        createModuleSetPage.toggleCreateModuleSetRelease();
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");
        createModuleSetPage.setRelease(release.getReleaseNumber());
        createModuleSetPage.setModuleSetRelease("connectSpec 10.9 Module Set Release");
        createModuleSetPage.hitCreateButton();
        waitFor(ofMillis(500L));
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
        CreateModuleSetPage createModuleSetPage =  viewEditModuleSetPage.hitNewModuleSetButton();
        createModuleSetPage.setName("New Module Set");
        createModuleSetPage.setDescription("Description");
        createModuleSetPage.hitCreateButton();

        ModuleSetObject moduleSet = getAPIFactory().getModuleSetAPI().getTheLatestModuleSetCreatedBy(developer);
        EditModuleSetPage editModuleSetPage =  viewEditModuleSetPage.openModuleSetByName(moduleSet);
        editModuleSetPage.setName("Updated Module Set Name");
        editModuleSetPage.setDescription("Updated Description");
        editModuleSetPage.hitUpdateButton();
    }

    @Test
    @DisplayName("TC_21_1_TA_4")
    public void test_TA_4() {
        AppUserObject developer;
        NamespaceObject namespace;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage =  viewEditModuleSetPage.hitNewModuleSetButton();
        createModuleSetPage.setName("New Module Set");
        createModuleSetPage.setDescription("Description");
        createModuleSetPage.hitCreateButton();
        waitFor(ofMillis(500L));

        /**
         * Test Assertion #21.1.4.a
         */
        ModuleSetObject moduleSet = getAPIFactory().getModuleSetAPI().getTheLatestModuleSetCreatedBy(developer);
        EditModuleSetPage editModuleSetPage =  viewEditModuleSetPage.openModuleSetByName(moduleSet);
        editModuleSetPage.addModule();
        CreateModuleFileDialog createModuleFileDialog = editModuleSetPage.addNewModuleFile();
        assertEquals("true", createModuleFileDialog.getModuleFileNameField().getAttribute("aria-required"));
        String moduleFileName = "New module file";
        createModuleFileDialog.setModuleFileName(moduleFileName);
        createModuleFileDialog.setNamespace(namespace.getUri());
        createModuleFileDialog.setModuleFileVersionNumber("New version");
        assertEquals("false", createModuleFileDialog.getNamespaceField().getAttribute("aria-required"));
        assertEquals("false", createModuleFileDialog.getModuleFileVersionNumberField().getAttribute("aria-required"));
        createModuleFileDialog.createModuleFile();
        waitFor(ofMillis(500L));

        /**
         * Test Assertion #21.1.4.b
         */
        editModuleSetPage.addModule();
        createModuleFileDialog = editModuleSetPage.addNewModuleFile();
        createModuleFileDialog.setModuleFileName(moduleFileName);
        createModuleFileDialog.setNamespace(namespace.getUri());
        createModuleFileDialog.setModuleFileVersionNumber("New version");
        createModuleFileDialog.createModuleFile();
        String errorMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath("//snack-bar-container//div[contains(@class, 'message')]//span")));
        assertTrue(errorMessage.contains("Duplicate module name exist."));
        escape(getDriver());

        EditModuleFileDialog editModuleFileDialog = editModuleSetPage.editModuleFile(moduleFileName);
        editModuleFileDialog.setModuleFileName("Changed module file name");
        editModuleFileDialog.setModuleFileVersionNumber("");
        editModuleFileDialog.updateModuleFile();

        /**
         * Test Assertion #21.1.4.c
         */
        editModuleSetPage.addModule();
        CreateModuleDirectoryDialog createModuleDirectoryDialog = editModuleSetPage.addNewModuleDirectory();
        String moduleDirectoryName = "Directory A";
        assertEquals("true", createModuleDirectoryDialog.getModuleDirectoryNameField().getAttribute("aria-required"));
        createModuleDirectoryDialog.setModuleDirectoryName(moduleDirectoryName);
        createModuleDirectoryDialog.createModuleDirectory();
        waitFor(ofMillis(500L));

        /**
         * Test Assertion #21.1.4.d
         */
        editModuleSetPage.addModule();
        createModuleDirectoryDialog = editModuleSetPage.addNewModuleDirectory();
        createModuleDirectoryDialog.setModuleDirectoryName(moduleDirectoryName);
        createModuleDirectoryDialog.createModuleDirectory();
        errorMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath("//snack-bar-container//div[contains(@class, 'message')]//span")));
        assertTrue(errorMessage.contains("Duplicate module name exist."));
        escape(getDriver());

        EditModuleDirectoryDialog editModuleDirectoryDialog = editModuleSetPage.editModuleDirectory(moduleDirectoryName);
        editModuleDirectoryDialog.setModuleDirectoryName("Directory A - changed");
        editModuleDirectoryDialog.updateModuleDirectory();

        /**
         * Test Assertion #21.1.4.e
         */
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

        assertDoesNotThrow(() -> editModuleSetPage.getModuleByName(selectedModule.getName()));
        click(editModuleSetPage.getModuleByName(selectedModule.getName()));
        List<ModuleObject> submodules = getAPIFactory().getModuleAPI().getSubmodules(selectedModule.getModuleId());
        for (ModuleObject submodule: submodules){
            assertDoesNotThrow(() -> editModuleSetPage.getModuleByName(submodule.getName()));
        }

        /**
         * Test Assertion #21.1.4.f
         */
        editModuleSetPage.addModule();
        editModuleSetPage.copyFromExistingModuleSet();
        selectedMduleSet = existingModuleSet.get(1);
        copyModuleFromExistingModuleSetDialog.setModuleSet
                (selectedMduleSet.getName());
        modules = getAPIFactory().getModuleAPI().getModulesByModuleSet(selectedMduleSet.getModuleSetId());
        ModuleObject selectedModuleSecond = modules.get(modules.size()-1);
        submodules = getAPIFactory().getModuleAPI().getSubmodules(selectedModuleSecond.getModuleId());
        ModuleObject selectedSubmodule = submodules.get(0);
        copyModuleFromExistingModuleSetDialog.selectModule(selectedModuleSecond.getName());
        copyModuleFromExistingModuleSetDialog.selectModule(selectedSubmodule.getName());
        copyModuleFromExistingModuleSetDialog.toggleCopyAllSubmodules();
        copyModuleFromExistingModuleSetDialog.copyModule();
        click(editModuleSetPage.getModuleByName(selectedModuleSecond.getName()));
        submodules = getAPIFactory().getModuleAPI().getSubmodules(selectedSubmodule.getModuleId());
        for (ModuleObject submodule: submodules){
            assertThrows(WebDriverException.class, () -> editModuleSetPage.getModuleByName(submodule.getName()));
        }
    }

    @Test
    @DisplayName("TC_21_1_TA_5")
    public void test_TA_5() {
        AppUserObject developer;
        NamespaceObject namespace;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage =  viewEditModuleSetPage.hitNewModuleSetButton();
        createModuleSetPage.setName("New Module Set");
        createModuleSetPage.setDescription("Description");
        createModuleSetPage.hitCreateButton();
        waitFor(ofMillis(500L));

        ModuleSetObject moduleSet = getAPIFactory().getModuleSetAPI().getTheLatestModuleSetCreatedBy(developer);
        EditModuleSetPage editModuleSetPage =  viewEditModuleSetPage.openModuleSetByName(moduleSet);
        editModuleSetPage.addModule();
        CreateModuleFileDialog createModuleFileDialog = editModuleSetPage.addNewModuleFile();
        String moduleFileName = "File A";
        createModuleFileDialog.setModuleFileName(moduleFileName);
        createModuleFileDialog.setNamespace(namespace.getUri());
        createModuleFileDialog.setModuleFileVersionNumber("New version");
        createModuleFileDialog.createModuleFile();
        waitFor(ofMillis(500L));

        EditModuleFileDialog editModuleFileDialog = editModuleSetPage.editModuleFile(moduleFileName);
        /**
         * Test Assertion #21.1.5.b
         */
        String messageFileDiscard = "The CC assigned to this file will also be deleted.";

        click(editModuleFileDialog.getDiscardModuleFileButton());
        assertEquals(messageFileDiscard, editModuleFileDialog.getDiscardFileMessage());
        click(editModuleFileDialog.getContinueToDiscardFileButton());

        editModuleSetPage.addModule();
        CreateModuleDirectoryDialog createModuleDirectoryDialog = editModuleSetPage.addNewModuleDirectory();
        String moduleDirectoryName = "Directory A";
        createModuleDirectoryDialog.setModuleDirectoryName(moduleDirectoryName);
        createModuleDirectoryDialog.createModuleDirectory();
        waitFor(ofMillis(500L));

        EditModuleDirectoryDialog editModuleDirectoryDialog = editModuleSetPage.editModuleDirectory(moduleDirectoryName);
        click(editModuleDirectoryDialog.getDiscardModuleDirectoryButton());

        /**
         * Test Assertion #21.1.5.a
         */
        String messageDirectoryDiscard = "Are you sure you want to discard this and sub modules?";
        assertEquals(messageDirectoryDiscard, editModuleDirectoryDialog.getDiscardDirectoryMessage());
        click(editModuleDirectoryDialog.getContinueToDiscardDirectoryButton());

        /**
         * Test Assertion #21.1.5.c
         */

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
        CreateModuleSetPage createModuleSetPage =  viewEditModuleSetPage.hitNewModuleSetButton();
        createModuleSetPage.setName("New Module Set");
        createModuleSetPage.setDescription("Description");
        createModuleSetPage.hitCreateButton();
        waitFor(ofMillis(500L));
        viewEditModuleSetPage.openPage();
        ModuleSetObject moduleSet = getAPIFactory().getModuleSetAPI().getTheLatestModuleSetCreatedBy(developer);
        viewEditModuleSetPage.discardModuleSet(moduleSet.getName());
        assert "Discarded".equals(getSnackBarMessage(getDriver()));
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
        CreateModuleSetPage createModuleSetPage =  viewEditModuleSetPage.hitNewModuleSetButton();
        createModuleSetPage.setName("New Module Set");
        createModuleSetPage.setDescription("Description");
        createModuleSetPage.hitCreateButton();
        waitFor(ofMillis(500L));
        homePage.logout();

        homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ModuleSetObject moduleSet = getAPIFactory().getModuleSetAPI().getTheLatestModuleSetCreatedBy(developer);
        homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSet);
        assertThrows(WebDriverException.class, () -> editModuleSetPage.setName("New Name EU"));
        assertThrows(WebDriverException.class, () -> editModuleSetPage.setDescription("New Description EU"));
        assertThrows(WebDriverException.class, () -> editModuleSetPage.hitUpdateButton());
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
