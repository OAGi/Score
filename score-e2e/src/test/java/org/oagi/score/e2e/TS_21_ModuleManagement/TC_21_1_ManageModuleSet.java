package org.oagi.score.e2e.TS_21_ModuleManagement;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
    public void from_the_module_set_list_page_the_developer_can_invoke_create_a_module_set() {
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
    public void name_is_required() {
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

        String moduleSetName = "Test Module " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = RandomStringUtils.secure().nextPrint(50, 100).trim();
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();

        viewEditModuleSetPage.openPage();
        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);
        waitFor(Duration.ofSeconds(2L)); // wait loading for the description
        assertEquals(description, getText(editModuleSetPage.getDescriptionField()));
    }

    @Test
    @DisplayName("TC_21_1_TA_2_a")
    public void developer_can_also_create_a_corresponding_module_set_release_based_module_set_in_this_case_the_devel() {
        AppUserObject developer;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();

        String moduleSetName = "Test Module " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = RandomStringUtils.secure().nextPrint(50, 100).trim();
        createModuleSetPage.setDescription(description);

        createModuleSetPage.toggleCreateModuleSetRelease();
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
        createModuleSetPage.setRelease(release.getReleaseNumber());
        createModuleSetPage.setModuleSetRelease(release.getReleaseNumber());
        createModuleSetPage.hitCreateButton();

        viewEditModuleSetPage.openPage();
        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);
        waitFor(Duration.ofSeconds(2L)); // wait loading for the description
        assertEquals(description, getText(editModuleSetPage.getDescriptionField()));

        ViewEditModuleSetReleasePage viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        EditModuleSetReleasePage editModuleSetReleasePage = viewEditModuleSetReleasePage.openModuleSetReleaseByName(moduleSetName);
        assertEquals(moduleSetName, getText(editModuleSetReleasePage.getModuleSetSelectField()));
        assertEquals(release.getReleaseNumber() + " " + release.getState(), getText(editModuleSetReleasePage.getReleaseSelectField()));
    }

    @Test
    @DisplayName("TC_21_1_TA_3")
    public void developer_can_edit_the_details_of_an_existing_module_set_in_particular_the_developer_can_edit_the_na() {
        AppUserObject developer;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();
        String moduleSetName = "Test Module Set " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = RandomStringUtils.secure().nextPrint(50, 100).trim();
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();

        viewEditModuleSetPage.openPage();
        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);
        waitFor(Duration.ofSeconds(2L)); // wait loading for the description
        assertEquals(description, getText(editModuleSetPage.getDescriptionField()));

        String newModuleSetName = "Updated Test Module Set " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        editModuleSetPage.setName(newModuleSetName);
        String newDescription = RandomStringUtils.secure().nextPrint(50, 100).trim();
        editModuleSetPage.setDescription(newDescription);
        editModuleSetPage.hitUpdateButton();

        viewEditModuleSetPage.openPage();
        editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(newModuleSetName);
        waitFor(Duration.ofSeconds(2L)); // wait loading for the description
        assertEquals(newDescription, getText(editModuleSetPage.getDescriptionField()));
    }

    @Test
    @DisplayName("TC_21_1_TA_4_a")
    public void developer_can_create_a_new_module_file_and_add_it_to_the_set_for_the_detail_of_the_module_file_only() {
        AppUserObject developer;
        NamespaceObject namespace;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();
        String moduleSetName = "Test Module Set " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = RandomStringUtils.secure().nextPrint(50, 100).trim();
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();

        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);
        editModuleSetPage.addModule();
        CreateModuleFileDialog createModuleFileDialog = editModuleSetPage.addNewModuleFile();
        assertEquals("true", createModuleFileDialog.getModuleFileNameField().getAttribute("aria-required"));
        String moduleFileName = "New module file" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        createModuleFileDialog.setModuleFileName(moduleFileName);
        createModuleFileDialog.setNamespace(namespace.getUri());
        String version = "Version " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
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
    public void developer_can_change_the_details_of_a_module_using_a_dialog_view_or_edit_a_module_the_name_of_the_mo() {
        AppUserObject developer;
        NamespaceObject namespace;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();
        String moduleSetName = "Test Module Set " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = RandomStringUtils.secure().nextPrint(50, 100).trim();
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();

        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);
        editModuleSetPage.addModule();
        CreateModuleFileDialog createModuleFileDialog = editModuleSetPage.addNewModuleFile();
        assertEquals("true", createModuleFileDialog.getModuleFileNameField().getAttribute("aria-required"));
        String moduleFileName = "New module file" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        createModuleFileDialog.setModuleFileName(moduleFileName);
        createModuleFileDialog.setNamespace(namespace.getUri());
        String version = "Version " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
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
        String newModuleFileName = "Changed module file" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        editModuleFileDialog.setModuleFileName(newModuleFileName);
        String newVersion = "Version " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        editModuleFileDialog.setModuleFileVersionNumber(newVersion);
        editModuleFileDialog.updateModuleFile();

        editModuleFileDialog = editModuleSetPage.editModuleFile(newModuleFileName);
        assertEquals(namespace.getUri(), getText(editModuleFileDialog.getNamespaceSelectField()));
        assertEquals(newVersion, getText(editModuleFileDialog.getModuleFileVersionNumberField()));
    }

    @Test
    @DisplayName("TC_21_1_TA_4_c")
    public void developer_can_create_a_new_module_directory_and_add_it_to_the_set_for_the_detail_of_the_module_direc() {
        AppUserObject developer;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();
        String moduleSetName = "Test Module Set " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = RandomStringUtils.secure().nextPrint(50, 100).trim();
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();

        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);
        editModuleSetPage.addModule();
        CreateModuleDirectoryDialog createModuleDirectoryDialog = editModuleSetPage.addNewModuleDirectory();
        String moduleDirectoryName = "Directory " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        assertEquals("true", createModuleDirectoryDialog.getModuleDirectoryNameField().getAttribute("aria-required"));
        createModuleDirectoryDialog.setModuleDirectoryName(moduleDirectoryName);
        createModuleDirectoryDialog.createModuleDirectory();

        EditModuleDirectoryDialog editModuleDirectoryDialog = editModuleSetPage.editModuleDirectory(moduleDirectoryName);
        assertEquals(moduleDirectoryName, getText(editModuleDirectoryDialog.getModuleDirectoryNameField()));
    }

    @Test
    @DisplayName("TC_21_1_TA_4_d")
    public void developer_can_modify_the_name_of_a_module_directory_it_cannot_be_left_empty_though_the_name_of_the_m() {
        AppUserObject developer;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();
        String moduleSetName = "Test Module Set " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = RandomStringUtils.secure().nextPrint(50, 100).trim();
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();

        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);
        editModuleSetPage.addModule();
        CreateModuleDirectoryDialog createModuleDirectoryDialog = editModuleSetPage.addNewModuleDirectory();
        String moduleDirectoryName = "Directory " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
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
        String newModuleDirectoryName = "Changed Directory " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        editModuleDirectoryDialog.setModuleDirectoryName(newModuleDirectoryName);
        editModuleDirectoryDialog.updateModuleDirectory();

        editModuleDirectoryDialog = editModuleSetPage.editModuleDirectory(newModuleDirectoryName);
        assertEquals(newModuleDirectoryName, getText(editModuleDirectoryDialog.getModuleDirectoryNameField()));
    }

    @Test
    @DisplayName("TC_21_1_TA_4_e")
    public void developer_can_copy_a_module_directory_from_another_module_set_the_developer_should_also_have_the_opt() {
        AppUserObject developer;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();
        String moduleSetName = "Test Module Set " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = RandomStringUtils.secure().nextPrint(50, 100).trim();
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();

        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);
        editModuleSetPage.addModule();
        CopyModuleFromExistingModuleSetDialog copyModuleFromExistingModuleSetDialog =
                editModuleSetPage.copyFromExistingModuleSet();
        List<ModuleSetObject> existingModuleSet = getAPIFactory().getModuleSetAPI().getAllModuleSets("connectSpec");
        ModuleSetObject selectedModuleSet = existingModuleSet.get(0);
        copyModuleFromExistingModuleSetDialog.setModuleSet(selectedModuleSet.getName());

        List<ModuleObject> modules = getAPIFactory().getModuleAPI().getModulesByModuleSet(selectedModuleSet.getModuleSetId());
        ModuleObject selectedModule = modules.get(modules.size() - 1);
        copyModuleFromExistingModuleSetDialog.selectModule(selectedModule.getName());
        copyModuleFromExistingModuleSetDialog.copyModule();

        assertNotNull(editModuleSetPage.getModuleByName(selectedModule.getName()));
        click(editModuleSetPage.getModuleByName(selectedModule.getName()));
        waitFor(Duration.ofSeconds(2L)); // wait for complete loading of sub-modules
        List<ModuleObject> submodules = getAPIFactory().getModuleAPI().getSubmodules(selectedModule.getModuleId());
        for (ModuleObject submodule : submodules) {
            assertNotNull(editModuleSetPage.getModuleByName(submodule.getName()));
        }
    }

    @Test
    @DisplayName("TC_21_1_TA_4_f")
    public void developer_can_select_a_module_to_copy_from_another_module_set_rather_than_a_directory_in_that_case_o() {
        AppUserObject developer;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();
        String moduleSetName = "Test Module Set " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = RandomStringUtils.secure().nextPrint(50, 100).trim();
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();

        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);
        editModuleSetPage.addModule();
        CopyModuleFromExistingModuleSetDialog copyModuleFromExistingModuleSetDialog = editModuleSetPage.copyFromExistingModuleSet();
        List<ModuleSetObject> existingModuleSet = getAPIFactory().getModuleSetAPI().getAllModuleSets("connectSpec");
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
    public void manage_module_set_covers_ta_5_a_and_b() {
        AppUserObject developer;
        NamespaceObject namespace;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();
        String moduleSetName = "Test Module Set " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = RandomStringUtils.secure().nextPrint(50, 100).trim();
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();

        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);
        editModuleSetPage.addModule();
        CreateModuleFileDialog createModuleFileDialog = editModuleSetPage.addNewModuleFile();
        String moduleFileName = "New module file" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        createModuleFileDialog.setModuleFileName(moduleFileName);
        createModuleFileDialog.setNamespace(namespace.getUri());
        String version = "Version " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
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
        String moduleDirectoryName = "Directory " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
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
    @Disabled("Not yet automated - documented case 21.1.5.c (TA_5_c): discarding a module directory whose module " +
            "file is used in a module set release should prompt the user. Was an empty test body reporting " +
            "false-positive coverage; disabled until implemented.")
    @DisplayName("TC_21_1_TA_5_c")
    public void a_module_directory_contains_a_module_file_is_used_in_a_module_release_set_the_system_should_ask_the() {
    }

    @Test
    @DisplayName("TC_21_1_TA_6")
    public void developer_can_discard_a_module_set_when_it_has_not_been_assigned_to_any_release() {
        AppUserObject developer;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();
        String moduleSetName = "Test Module Set " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = RandomStringUtils.secure().nextPrint(50, 100).trim();
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();

        viewEditModuleSetPage.openPage();
        viewEditModuleSetPage.discardModuleSet(moduleSetName);
        assertThrows(NoSuchElementException.class, () -> viewEditModuleSetPage.openModuleSetByName(moduleSetName));
    }

    @Test
    @DisplayName("TC_21_1_TA_7")
    public void developer_cannot_discard_a_module_set_that_has_been_assigned_to_a_release() {
        AppUserObject developer;
        ReleaseObject release;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetPage viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        CreateModuleSetPage createModuleSetPage = viewEditModuleSetPage.hitNewModuleSetButton();
        String moduleSetName = "Test Module Set " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = RandomStringUtils.secure().nextPrint(50, 100).trim();
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();

        ViewEditModuleSetReleasePage viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        CreateModuleSetReleasePage createModuleSetReleasePage = viewEditModuleSetReleasePage.hitNewModuleSetReleaseButton();
        String moduleSetReleaseName = "Test Module Set Release for " + moduleSetName;
        createModuleSetReleasePage.setName(moduleSetReleaseName);
        String moduleSetReleaseDescription = RandomStringUtils.secure().nextPrint(50, 100).trim();
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
    public void end_user_can_view_module_sets_but_cannot_make_any_change_or_add_a_new_one() {
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
        String moduleSetName = "Test Module Set " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = RandomStringUtils.secure().nextPrint(50, 100).trim();
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
    public void developer_can_view_any_existing_module_set_he_can_also_edit_its_details() {
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
        String moduleSetName = "Test Module Set " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        createModuleSetPage.setName(moduleSetName);
        String description = RandomStringUtils.secure().nextPrint(50, 100).trim();
        createModuleSetPage.setDescription(description);
        createModuleSetPage.hitCreateButton();
        homePage.logout();

        homePage = loginPage().signIn(developerB.getLoginId(), developerB.getPassword());
        viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        EditModuleSetPage editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(moduleSetName);

        String newModuleSetName = "Updated Test Module Set " + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        editModuleSetPage.setName(newModuleSetName);
        String newDescription = RandomStringUtils.secure().nextPrint(50, 100).trim();
        editModuleSetPage.setDescription(newDescription);
        editModuleSetPage.hitUpdateButton();
        homePage.logout();

        homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        viewEditModuleSetPage = homePage.getModuleMenu().openViewEditModuleSetSubMenu();
        editModuleSetPage = viewEditModuleSetPage.openModuleSetByName(newModuleSetName);
        waitFor(Duration.ofSeconds(2L)); // wait loading for the description
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
