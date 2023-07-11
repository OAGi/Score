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
import org.oagi.score.e2e.page.module.CreateModuleSetReleasePage;
import org.oagi.score.e2e.page.module.EditModuleSetReleasePage;
import org.oagi.score.e2e.page.module.ModuleSetReleaseXMLSchemaValidationDialog;
import org.oagi.score.e2e.page.module.ViewEditModuleSetReleasePage;
import org.openqa.selenium.TimeoutException;

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
    @DisplayName("TC_21_2_TA_6")
    public void test_TA_6() {

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
