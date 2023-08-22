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
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.oagi.score.e2e.page.module.*;
import org.oagi.score.e2e.page.release.CreateReleasePage;
import org.oagi.score.e2e.page.release.EditReleasePage;
import org.oagi.score.e2e.page.release.ReleaseAssignmentPage;
import org.oagi.score.e2e.page.release.ViewEditReleasePage;
import org.openqa.selenium.*;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.Duration.ofMillis;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
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

    @AfterEach
    public void tearDown() {
        super.tearDown();
        // Delete random accounts
        this.randomAccounts.forEach(newUser -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId());
        });
    }

    @Test
    @DisplayName("TC_21_2_TA_1")
    public void test_TA_1() throws InterruptedException {
        AppUserObject developer;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditModuleSetReleasePage viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        CreateModuleSetReleasePage createModuleSetReleasePage = viewEditModuleSetReleasePage.hitNewModuleSetReleaseButton();
        String moduleSetReleaseName = "Test Module Set Release " + randomAlphanumeric(5, 10);
        createModuleSetReleasePage.setName(moduleSetReleaseName);
        String description = randomPrint(50, 100).trim();
        createModuleSetReleasePage.setDescription(description);
        List<ModuleSetObject> existingModuleSets = getAPIFactory().getModuleSetAPI().getAllModuleSets();
        for (ModuleSetObject moduleSet : existingModuleSets.stream().filter(e -> !e.getName().contains("Test")).collect(Collectors.toList())) {
            assertDoesNotThrow(() -> createModuleSetReleasePage.setModuleSet(moduleSet.getName()));
        }
        List<ReleaseObject> existingReleases = getAPIFactory().getReleaseAPI().getReleases();
        for (ReleaseObject release : existingReleases.stream().filter(e -> e.getState().equals("Published")).collect(Collectors.toList())) {
            assertDoesNotThrow(() -> createModuleSetReleasePage.setRelease(release.getReleaseNumber()));
        }

        createModuleSetReleasePage.hitCreateButton();

        viewEditModuleSetReleasePage.openPage();
        EditModuleSetReleasePage editModuleSetReleasePage = viewEditModuleSetReleasePage.openModuleSetReleaseByName(moduleSetReleaseName);
        waitFor(Duration.ofSeconds(2L)); // wait loading for the description
        assertEquals(description, getText(editModuleSetReleasePage.getDescriptionField()));
    }

    @Test
    @DisplayName("TC_21_2_TA_1_a")
    public void test_TA_1_a() throws InterruptedException {
        String releaseNumber = "10.8.4";
        AppUserObject developer;
        ModuleSetReleaseObject moduleSetRelease;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            moduleSetRelease = getAPIFactory().getModuleSetReleaseAPI().getModuleSetReleaseByName(releaseNumber);
            moduleSetRelease.setDefault(true);

            getAPIFactory().getModuleSetReleaseAPI().updateModuleSetRelease(moduleSetRelease);
        }

        try {
            HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
            ViewEditModuleSetReleasePage viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
            CreateModuleSetReleasePage createModuleSetReleasePage = viewEditModuleSetReleasePage.hitNewModuleSetReleaseButton();
            String moduleSetReleaseName = "Test Module Set Release " + randomAlphanumeric(5, 10);
            createModuleSetReleasePage.setName(moduleSetReleaseName);
            String description = randomPrint(50, 100).trim();
            createModuleSetReleasePage.setDescription(description);
            createModuleSetReleasePage.setModuleSet(releaseNumber);
            createModuleSetReleasePage.setRelease(releaseNumber);

            createModuleSetReleasePage.toggleDefault();
            assertThrows(TimeoutException.class, () -> createModuleSetReleasePage.hitCreateButton());

            WebElement createButtonInDialog = elementToBeClickable(getDriver(),
                    By.xpath("//mat-dialog-container//span[contains(text(), \"Create\")]//ancestor::button/span"));
            click(createButtonInDialog);

            viewEditModuleSetReleasePage.openPage();
            EditModuleSetReleasePage editModuleSetReleasePage = viewEditModuleSetReleasePage.openModuleSetReleaseByName(moduleSetReleaseName);
            waitFor(Duration.ofSeconds(2L)); // wait loading for the description
            assertEquals(description, getText(editModuleSetReleasePage.getDescriptionField()));
            assertEnabled(editModuleSetReleasePage.getDefaultCheckbox());
        } finally {
            // Roll the 'Default' option back
            getAPIFactory().getModuleSetReleaseAPI().updateModuleSetRelease(moduleSetRelease);
        }
    }

    @Test
    @DisplayName("TC_21_2_TA_2")
    public void test_TA_2() {
        String releaseNumber = "10.8.2";
        AppUserObject developer;
        ModuleSetReleaseObject moduleSetRelease;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            moduleSetRelease = getAPIFactory().getModuleSetReleaseAPI().getModuleSetReleaseByName(releaseNumber);
            moduleSetRelease.setDefault(true);

            getAPIFactory().getModuleSetReleaseAPI().updateModuleSetRelease(moduleSetRelease);
        }

        try {
            HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
            ViewEditModuleSetReleasePage viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
            CreateModuleSetReleasePage createModuleSetReleasePage = viewEditModuleSetReleasePage.hitNewModuleSetReleaseButton();
            String moduleSetReleaseName = "Test Module Set Release " + randomAlphanumeric(5, 10);
            createModuleSetReleasePage.setName(moduleSetReleaseName);
            String description = randomPrint(50, 100).trim();
            createModuleSetReleasePage.setDescription(description);
            createModuleSetReleasePage.setModuleSet(releaseNumber);
            createModuleSetReleasePage.setRelease(releaseNumber);

            createModuleSetReleasePage.toggleDefault();
            assertThrows(TimeoutException.class, () -> createModuleSetReleasePage.hitCreateButton());

            WebElement createButtonInDialog = elementToBeClickable(getDriver(),
                    By.xpath("//mat-dialog-container//span[contains(text(), \"Create\")]//ancestor::button/span"));
            click(createButtonInDialog);

            viewEditModuleSetReleasePage.openPage();
            EditModuleSetReleasePage editModuleSetReleasePage = viewEditModuleSetReleasePage.openModuleSetReleaseByName(releaseNumber);
            editModuleSetReleasePage.toggleDefault();
            EditModuleSetReleasePage finalEditModuleSetReleasePage = editModuleSetReleasePage;
            assertThrows(TimeoutException.class, () -> finalEditModuleSetReleasePage.hitUpdateButton());

            WebElement confirmDialogContent = visibilityOfElementLocated(getDriver(),
                    By.xpath("//score-confirm-dialog//div[@class = \"content\"]"));
            assertTrue(getText(confirmDialogContent).contains("There is another default module set release"));

            WebElement updateButtonInDialog = elementToBeClickable(getDriver(),
                    By.xpath("//mat-dialog-container//span[contains(text(), \"Update\")]//ancestor::button/span"));
            click(updateButtonInDialog);

            viewEditModuleSetReleasePage.openPage();
            editModuleSetReleasePage = viewEditModuleSetReleasePage.openModuleSetReleaseByName(moduleSetReleaseName);
            assertNotChecked(editModuleSetReleasePage.getDefaultCheckbox());
        } finally {
            // Roll the 'Default' option back
            getAPIFactory().getModuleSetReleaseAPI().updateModuleSetRelease(moduleSetRelease);
        }
    }

    @Test
    @DisplayName("TC_21_2_TA_3")
    public void test_TA_3() {
        String releaseNumber = "10.8.3";
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
        String moduleSetReleaseName = "Test Module Set Release " + randomAlphanumeric(5, 10);
        createModuleSetReleasePage.setName(moduleSetReleaseName);
        String description = randomPrint(50, 100).trim();
        createModuleSetReleasePage.setDescription(description);
        createModuleSetReleasePage.setModuleSet(releaseNumber);
        createModuleSetReleasePage.setRelease(releaseNumber);
        createModuleSetReleasePage.hitCreateButton();
        homePage.logout();
        waitFor(ofMillis(500L));

        homePage = loginPage().signIn(developerB.getLoginId(), developerB.getPassword());
        viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        EditModuleSetReleasePage editModuleSetReleasePage = viewEditModuleSetReleasePage.openModuleSetReleaseByName(moduleSetReleaseName);
        String newModuleSetReleaseName = "Updated Test Module Set Release " + randomAlphanumeric(5, 10);
        editModuleSetReleasePage.setName(newModuleSetReleaseName);
        String newDescription = randomPrint(50, 100).trim();
        editModuleSetReleasePage.setDescription(newDescription);
        editModuleSetReleasePage.hitUpdateButton();
        homePage.logout();
        waitFor(ofMillis(500L));

        homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        editModuleSetReleasePage = viewEditModuleSetReleasePage.openModuleSetReleaseByName(newModuleSetReleaseName);
        waitFor(Duration.ofSeconds(2L)); // wait loading for the description
        assertEquals(newDescription, getText(editModuleSetReleasePage.getDescriptionField()));
    }

    @Test
    @DisplayName("TC_21_2_TA_4")
    public void test_TA_4() {
        String releaseNumber = "10.8.3";
        AppUserObject developerA;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditModuleSetReleasePage viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        EditModuleSetReleasePage editModuleSetReleasePage = viewEditModuleSetReleasePage.openModuleSetReleaseByName(releaseNumber);
        File exportReleaseFile = null;
        try {
            exportReleaseFile = editModuleSetReleasePage.hitExportButton();
            waitFor(Duration.ofMillis(4000));
            assertNotNull(exportReleaseFile);
            assertTrue(exportReleaseFile.getName().endsWith(".zip"));
        } finally {
            if (exportReleaseFile != null) {
                exportReleaseFile.delete();
            }
        }
    }

    @Test
    @DisplayName("TC_21_2_TA_5")
    public void test_TA_5() {
        String releaseNumber = "10.8.3";
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
        String moduleSetReleaseName = "Test Module Set Release " + randomAlphanumeric(5, 10);
        createModuleSetReleasePage.setName(moduleSetReleaseName);
        String description = randomPrint(50, 100).trim();
        createModuleSetReleasePage.setDescription(description);
        createModuleSetReleasePage.setModuleSet(releaseNumber);
        createModuleSetReleasePage.setRelease(releaseNumber);
        createModuleSetReleasePage.hitCreateButton();
        homePage.logout();
        waitFor(ofMillis(500L));

        homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        EditModuleSetReleasePage editModuleSetReleasePage = viewEditModuleSetReleasePage.openModuleSetReleaseByName(moduleSetReleaseName);
        waitFor(Duration.ofSeconds(2L)); // wait loading for the description
        assertEquals(description, getText(editModuleSetReleasePage.getDescriptionField()));
        assertDisabled(editModuleSetReleasePage.getNameField());
        assertDisabled(editModuleSetReleasePage.getDescriptionField());
        assertThrows(TimeoutException.class, () -> editModuleSetReleasePage.getUpdateButton(true));
    }

    @Test
    @DisplayName("TC_21_2_TA_6b")
    public void test_TA_6b() {
        AppUserObject developer;
        NamespaceObject namespace;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();

        CreateReleasePage createReleasePage = viewEditReleasePage.createRelease();
        String newReleaseNum = String.valueOf((RandomUtils.nextInt(20230519, 20231231)));
        createReleasePage.setReleaseNumber(newReleaseNum);
        createReleasePage.setReleaseNamespace(namespace);
        createReleasePage.hitCreateButton();
        viewEditReleasePage.openPage();

        ReleaseObject newRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(newReleaseNum);

        ViewEditModuleSetReleasePage viewEditModuleSetReleasePage = homePage.getModuleMenu().openViewEditModuleSetReleaseSubMenu();
        CreateModuleSetReleasePage createModuleSetReleasePage = viewEditModuleSetReleasePage.hitNewModuleSetReleaseButton();
        String moduleSetReleaseName = "Test Module Set Release " + randomAlphanumeric(5, 10);
        createModuleSetReleasePage.setName(moduleSetReleaseName);
        String description = randomPrint(50, 100).trim();
        createModuleSetReleasePage.setDescription(description);
        createModuleSetReleasePage.setModuleSet("10.7.5");
        createModuleSetReleasePage.setRelease(newRelease.getReleaseNumber());
        createModuleSetReleasePage.hitCreateButton();
        waitFor(ofMillis(500L));

        viewEditReleasePage.openPage();
        viewEditReleasePage.hitDiscardButton(newRelease.getReleaseNumber());
        String errorMessage = getText(visibilityOfElementLocated(getDriver(), By.xpath("//snack-bar-container//div[contains(@class, 'message')]//span")));
        assertTrue(errorMessage.contains("It cannot be discarded because there are dependent module set releases."));
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
        String moduleSetReleaseName = "Test Module Set Release " + randomAlphanumeric(5, 10);
        createModuleSetReleasePage.setName(moduleSetReleaseName);
        String description = randomPrint(50, 100).trim();
        createModuleSetReleasePage.setDescription(description);
        createModuleSetReleasePage.setModuleSet("10.7.4");
        createModuleSetReleasePage.setRelease("10.7.4");
        createModuleSetReleasePage.hitCreateButton();
        waitFor(ofMillis(500L));

        viewEditModuleSetReleasePage.openPage();
        ModuleSetReleaseObject latestModuleSetRelease = getAPIFactory().getModuleSetReleaseAPI().getTheLatestModuleSetReleaseCreatedBy(developerA);
        EditModuleSetReleasePage editModuleSetReleasePage = viewEditModuleSetReleasePage.openModuleSetReleaseByName(latestModuleSetRelease);
        assertDoesNotThrow(() -> editModuleSetReleasePage.hitValidateButton());

        WebElement dialogHeader = visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//div[@class = \"header\"]"));
        assertEquals("Module Set Release - XML Schema Validation", getText(dialogHeader));
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
        String moduleSetReleaseName = "Test Module Set Release " + randomAlphanumeric(5, 10);
        createModuleSetReleasePage.setName(moduleSetReleaseName);
        String description = randomPrint(50, 100).trim();
        createModuleSetReleasePage.setDescription(description);
        createModuleSetReleasePage.setModuleSet("10.7.3");
        createModuleSetReleasePage.setRelease("10.7.3");
        createModuleSetReleasePage.hitCreateButton();
        waitFor(ofMillis(500L));

        viewEditModuleSetReleasePage.openPage();
        ModuleSetReleaseObject latestModuleSetRelease = getAPIFactory().getModuleSetReleaseAPI().getTheLatestModuleSetReleaseCreatedBy(developerA);
        EditModuleSetReleasePage editModuleSetReleasePage = viewEditModuleSetReleasePage.openModuleSetReleaseByName(latestModuleSetRelease);
        ModuleSetReleaseXMLSchemaValidationDialog validateDialog = editModuleSetReleasePage.hitValidateButton();

        long timeout = Duration.ofSeconds(300L).toMillis();
        long begin = System.currentTimeMillis();
        while (System.currentTimeMillis() - begin < timeout) {
            WebElement progressBar = validateDialog.getProgressBar();
            try {
                if ("determinate".equals(progressBar.getAttribute("mode"))) {
                    break;
                }
            } catch (StaleElementReferenceException ignore) {
            }
        }

        validateDialog.hitCopyToClipboardButton();
        assertTrue(getSnackBarMessage(getDriver()).equals("Copied to clipboard"));
    }
}
