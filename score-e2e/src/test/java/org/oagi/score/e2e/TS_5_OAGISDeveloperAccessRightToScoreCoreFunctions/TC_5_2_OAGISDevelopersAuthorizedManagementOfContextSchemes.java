package org.oagi.score.e2e.TS_5_OAGISDeveloperAccessRightToScoreCoreFunctions;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.menu.ContextMenu;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.context.*;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertChecked;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_5_2_OAGISDevelopersAuthorizedManagementOfContextSchemes extends BaseTest {

    private List<AppUserObject> randomAccounts = new ArrayList<>();

    private AppUserObject appUser;

    @BeforeEach
    public void init() {
        super.init();

        // Create random developer
        appUser = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(appUser);
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_5_2_TA_issue1245")
    public void test_TA_issue1245() {
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);
        ContextSchemeObject contextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        viewEditContextSchemePage.setName(contextScheme.getSchemeName());

        retry(() -> {
            viewEditContextSchemePage.hitSearchButton();

            WebElement tr = viewEditContextSchemePage.getTableRecordAtIndex(1);
            WebElement td = viewEditContextSchemePage.getColumnByName(tr, "schemeName");
            assertEquals(contextScheme.getSchemeName(), td.findElement(By.cssSelector("a > span")).getText());
        });
    }

    @Test
    @DisplayName("TC_5_2_TA_1 (Without context scheme values)")
    public void developer_can_create_context_scheme_with_only_required_information() {
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();

        CreateContextSchemePage createContextSchemePage = viewEditContextSchemePage.openCreateContextSchemePage();

        ContextSchemeObject contextScheme =
                ContextSchemeObject.createRandomContextScheme(randomContextCategory, appUser);
        viewEditContextSchemePage = createContextSchemePage.createContextScheme(randomContextCategory, contextScheme);

        EditContextSchemePage editContextSchemePage =
                viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(contextScheme.getSchemeName());
        assertEquals(randomContextCategory.getName(), editContextSchemePage.getContextCategorySelectField().getText());
        assertEquals(contextScheme.getSchemeName(), editContextSchemePage.getNameField().getAttribute("value"));
        assertEquals(contextScheme.getSchemeId(), editContextSchemePage.getSchemeIDField().getAttribute("value"));
        assertEquals(contextScheme.getSchemeAgencyId(), editContextSchemePage.getAgencyIDField().getAttribute("value"));
        assertEquals(contextScheme.getSchemeVersionId(), editContextSchemePage.getVersionField().getAttribute("value"));
        assertEquals(contextScheme.getDescription(), editContextSchemePage.getDescriptionField().getAttribute("value"));
    }

    @Test
    @DisplayName("TC_5_2_TA_2 (With context scheme values)")
    public void developer_can_create_context_scheme_with_all_information_specified() {
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();

        CreateContextSchemePage createContextSchemePage = viewEditContextSchemePage.openCreateContextSchemePage();

        ContextSchemeObject contextScheme =
                ContextSchemeObject.createRandomContextScheme(randomContextCategory, appUser);
        List<ContextSchemeValueObject> contextSchemeValueList = Arrays.asList(
                ContextSchemeValueObject.createRandomContextSchemeValue(contextScheme),
                ContextSchemeValueObject.createRandomContextSchemeValue(contextScheme),
                ContextSchemeValueObject.createRandomContextSchemeValue(contextScheme));

        viewEditContextSchemePage = createContextSchemePage.createContextScheme(randomContextCategory, contextScheme, contextSchemeValueList);

        EditContextSchemePage editContextSchemePage =
                viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(contextScheme.getSchemeName());
        assertEquals(randomContextCategory.getName(), editContextSchemePage.getContextCategorySelectField().getText());
        assertEquals(contextScheme.getSchemeName(), editContextSchemePage.getNameField().getAttribute("value"));
        assertEquals(contextScheme.getSchemeId(), editContextSchemePage.getSchemeIDField().getAttribute("value"));
        assertEquals(contextScheme.getSchemeAgencyId(), editContextSchemePage.getAgencyIDField().getAttribute("value"));
        assertEquals(contextScheme.getSchemeVersionId(), editContextSchemePage.getVersionField().getAttribute("value"));
        assertEquals(contextScheme.getDescription(), editContextSchemePage.getDescriptionField().getAttribute("value"));

        for (ContextSchemeValueObject contextSchemeValue : contextSchemeValueList) {
            assertContextSchemeValue(editContextSchemePage, contextSchemeValue);
        }
    }

    private void assertContextSchemeValue(EditContextSchemePage editContextSchemePage, ContextSchemeValueObject contextSchemeValue) {
        ContextSchemeValueDialog contextSchemeValueDialog = editContextSchemePage.openContextSchemeValueDialog(contextSchemeValue);
        assertEquals(contextSchemeValue.getValue(), contextSchemeValueDialog.getValueField().getAttribute("value"));
        assertEquals(contextSchemeValue.getMeaning(), contextSchemeValueDialog.getMeaningField().getAttribute("value"));
        escape(getDriver());
    }

    @Test
    @DisplayName("TC_5_2_TA_3")
    public void test_adding_and_removing_scheme_values_on_the_creation_page() {
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        CreateContextSchemePage createContextSchemePage = viewEditContextSchemePage.openCreateContextSchemePage();

        ContextSchemeObject contextScheme =
                ContextSchemeObject.createRandomContextScheme(randomContextCategory, appUser);
        List<ContextSchemeValueObject> contextSchemeValueList = Arrays.asList(
                ContextSchemeValueObject.createRandomContextSchemeValue(contextScheme),
                ContextSchemeValueObject.createRandomContextSchemeValue(contextScheme),
                ContextSchemeValueObject.createRandomContextSchemeValue(contextScheme));

        createContextSchemePage.setContextCategory(randomContextCategory);
        createContextSchemePage.setName(contextScheme.getSchemeName());
        createContextSchemePage.setSchemeID(contextScheme.getSchemeId());
        createContextSchemePage.setAgencyID(contextScheme.getSchemeAgencyId());
        createContextSchemePage.setVersion(contextScheme.getSchemeVersionId());
        createContextSchemePage.setDescription(contextScheme.getDescription());
        // Add
        for (ContextSchemeValueObject contextSchemeValue : contextSchemeValueList) {
            ContextSchemeValueDialog contextSchemeValueDialog = createContextSchemePage.openContextSchemeValueDialog();
            contextSchemeValueDialog.addContextSchemeValue(contextSchemeValue);
        }
        // Remove
        createContextSchemePage.removeContextSchemeValue(contextSchemeValueList.get(0));
        createContextSchemePage.removeContextSchemeValue(contextSchemeValueList.get(1));

        createContextSchemePage.hitCreateButton();

        viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        EditContextSchemePage editContextSchemePage =
                viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(contextScheme.getSchemeName());
        assertEquals(randomContextCategory.getName(), editContextSchemePage.getContextCategorySelectField().getText());
        assertEquals(contextScheme.getSchemeName(), editContextSchemePage.getNameField().getAttribute("value"));
        assertEquals(contextScheme.getSchemeId(), editContextSchemePage.getSchemeIDField().getAttribute("value"));
        assertEquals(contextScheme.getSchemeAgencyId(), editContextSchemePage.getAgencyIDField().getAttribute("value"));
        assertEquals(contextScheme.getSchemeVersionId(), editContextSchemePage.getVersionField().getAttribute("value"));
        assertEquals(contextScheme.getDescription(), editContextSchemePage.getDescriptionField().getAttribute("value"));

        assertThrows(NoSuchElementException.class, () -> assertContextSchemeValue(editContextSchemePage, contextSchemeValueList.get(0)));
        assertThrows(NoSuchElementException.class, () -> assertContextSchemeValue(editContextSchemePage, contextSchemeValueList.get(1)));
        assertContextSchemeValue(editContextSchemePage, contextSchemeValueList.get(2));
    }

    @Test
    @DisplayName("TC_5_2_TA_4")
    public void developer_cannot_create_context_scheme_with_missing_required_information_including_context_scheme_value() {
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        CreateContextSchemePage createContextSchemePage = viewEditContextSchemePage.openCreateContextSchemePage();

        ContextSchemeObject contextScheme =
                ContextSchemeObject.createRandomContextScheme(randomContextCategory, appUser);
        List<ContextSchemeValueObject> contextSchemeValueList = Arrays.asList(
                ContextSchemeValueObject.createRandomContextSchemeValue(contextScheme),
                ContextSchemeValueObject.createRandomContextSchemeValue(contextScheme),
                ContextSchemeValueObject.createRandomContextSchemeValue(contextScheme));

        createContextSchemePage.setContextCategory(randomContextCategory);
        createContextSchemePage.setName(contextScheme.getSchemeName());
        createContextSchemePage.setSchemeID(contextScheme.getSchemeId());
        createContextSchemePage.setAgencyID(contextScheme.getSchemeAgencyId());
        createContextSchemePage.setVersion(contextScheme.getSchemeVersionId());
        createContextSchemePage.setDescription(contextScheme.getDescription());

        // Add
        for (ContextSchemeValueObject contextSchemeValue : contextSchemeValueList) {
            ContextSchemeValueDialog contextSchemeValueDialog = createContextSchemePage.openContextSchemeValueDialog();
            contextSchemeValueDialog.addContextSchemeValue(contextSchemeValue);
        }

        clear(createContextSchemePage.getNameField());
        assertThrows(TimeoutException.class, () -> createContextSchemePage.getCreateButton());
        createContextSchemePage.setName(contextScheme.getSchemeName());

        clear(createContextSchemePage.getSchemeIDField());
        assertThrows(TimeoutException.class, () -> createContextSchemePage.getCreateButton());
        createContextSchemePage.setSchemeID(contextScheme.getSchemeId());

        clear(createContextSchemePage.getAgencyIDField());
        assertThrows(TimeoutException.class, () -> createContextSchemePage.getCreateButton());
        createContextSchemePage.setAgencyID(contextScheme.getSchemeAgencyId());

        clear(createContextSchemePage.getVersionField());
        assertThrows(TimeoutException.class, () -> createContextSchemePage.getCreateButton());
        createContextSchemePage.setVersion(contextScheme.getSchemeVersionId());

        ContextSchemeValueDialog contextSchemeValueDialog =
                createContextSchemePage.openContextSchemeValueDialog(contextSchemeValueList.get(0));
        clear(contextSchemeValueDialog.getValueField());
        assertThrows(TimeoutException.class, () -> contextSchemeValueDialog.getSaveButton());
    }

    @Test
    @DisplayName("TC_5_2_TA_5")
    public void developer_cannot_create_context_scheme_when_the_uniqueness_requirement_is_not_met() {
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        CreateContextSchemePage createContextSchemePage = viewEditContextSchemePage.openCreateContextSchemePage();

        assertThrows(TimeoutException.class, () -> {
            createContextSchemePage.createContextScheme(randomContextCategory, randomContextScheme);
        });
        WebElement warningDialog = visibilityOfElementLocated(getDriver(), By.xpath(
                "//*[contains(text(), \"other context scheme with the triplet (schemeID, AgencyID, Version) already exist\")]"));
        assertNotNull(warningDialog);
    }

    @Test
    @DisplayName("TC_5_2_TA_6")
    public void test_developer_tries_to_create_context_scheme_with_scheme_ID_and_agency_ID_that_are_the_same_as_existing_context_scheme_but_different_name() {
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        CreateContextSchemePage createContextSchemePage = viewEditContextSchemePage.openCreateContextSchemePage();

        String oldName = randomContextScheme.getSchemeName();
        String newName = "cs_" + randomAlphanumeric(5, 10);
        assertNotEquals(newName, oldName);
        randomContextScheme.setSchemeName(newName);

        String oldVersion = randomContextScheme.getSchemeVersionId();
        String newVersion = "cs_version_id_" + randomAlphanumeric(5, 10);
        assertNotEquals(newVersion, oldVersion);
        randomContextScheme.setSchemeVersionId(newVersion);

        assertThrows(TimeoutException.class, () -> {
            createContextSchemePage.createContextScheme(randomContextCategory, randomContextScheme);
        });
        WebElement createAnywayButton =
                elementToBeClickable(getDriver(), By.xpath("//span[contains(text(), \"Create anyway\")]"));
        assertNotNull(createAnywayButton);

        click(createAnywayButton);
        EditContextSchemePage editContextSchemePage =
                viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(newName);
        assertEquals(randomContextCategory.getName(), editContextSchemePage.getContextCategorySelectField().getText());
        assertEquals(newName, editContextSchemePage.getNameField().getAttribute("value"));
        assertEquals(randomContextScheme.getSchemeId(), editContextSchemePage.getSchemeIDField().getAttribute("value"));
        assertEquals(randomContextScheme.getSchemeAgencyId(), editContextSchemePage.getAgencyIDField().getAttribute("value"));
        assertEquals(randomContextScheme.getSchemeVersionId(), editContextSchemePage.getVersionField().getAttribute("value"));
        assertEquals(randomContextScheme.getDescription(), editContextSchemePage.getDescriptionField().getAttribute("value"));
    }

    @Test
    @DisplayName("TC_5_2_TA_7 (Developer)")
    public void developer_can_see_context_schemes_created_by_developer_user() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(developer);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, developer);
        List<ContextSchemeValueObject> randomContextSchemeValues = Arrays.asList(
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme),
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme),
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme)
        );

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();

        EditContextSchemePage editContextSchemePage =
                viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());
        assertEquals(randomContextCategory.getName(), editContextSchemePage.getContextCategorySelectField().getText());
        assertEquals(randomContextScheme.getSchemeName(), editContextSchemePage.getNameField().getAttribute("value"));
        assertEquals(randomContextScheme.getSchemeId(), editContextSchemePage.getSchemeIDField().getAttribute("value"));
        assertEquals(randomContextScheme.getSchemeAgencyId(), editContextSchemePage.getAgencyIDField().getAttribute("value"));
        assertEquals(randomContextScheme.getSchemeVersionId(), editContextSchemePage.getVersionField().getAttribute("value"));
        assertEquals(randomContextScheme.getDescription(), editContextSchemePage.getDescriptionField().getAttribute("value"));

        for (ContextSchemeValueObject contextSchemeValue : randomContextSchemeValues) {
            assertContextSchemeValue(editContextSchemePage, contextSchemeValue);
        }
    }

    @Test
    @DisplayName("TC_5_2_TA_7 (Developer+Admin)")
    public void developer_can_see_context_schemes_created_by_developer_admin_user() {
        AppUserObject developerAdmin = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developerAdmin);

        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(developerAdmin);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, developerAdmin);
        List<ContextSchemeValueObject> randomContextSchemeValues = Arrays.asList(
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme),
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme),
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme)
        );

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();

        EditContextSchemePage editContextSchemePage =
                viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());
        assertEquals(randomContextCategory.getName(), editContextSchemePage.getContextCategorySelectField().getText());
        assertEquals(randomContextScheme.getSchemeName(), editContextSchemePage.getNameField().getAttribute("value"));
        assertEquals(randomContextScheme.getSchemeId(), editContextSchemePage.getSchemeIDField().getAttribute("value"));
        assertEquals(randomContextScheme.getSchemeAgencyId(), editContextSchemePage.getAgencyIDField().getAttribute("value"));
        assertEquals(randomContextScheme.getSchemeVersionId(), editContextSchemePage.getVersionField().getAttribute("value"));
        assertEquals(randomContextScheme.getDescription(), editContextSchemePage.getDescriptionField().getAttribute("value"));

        for (ContextSchemeValueObject contextSchemeValue : randomContextSchemeValues) {
            assertContextSchemeValue(editContextSchemePage, contextSchemeValue);
        }
    }

    @Test
    @DisplayName("TC_5_2_TA_7 (End-User)")
    public void developer_can_see_context_schemes_created_by_end_user() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(endUser);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, endUser);
        List<ContextSchemeValueObject> randomContextSchemeValues = Arrays.asList(
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme),
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme),
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme)
        );

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();

        EditContextSchemePage editContextSchemePage =
                viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());
        assertEquals(randomContextCategory.getName(), editContextSchemePage.getContextCategorySelectField().getText());
        assertEquals(randomContextScheme.getSchemeName(), editContextSchemePage.getNameField().getAttribute("value"));
        assertEquals(randomContextScheme.getSchemeId(), editContextSchemePage.getSchemeIDField().getAttribute("value"));
        assertEquals(randomContextScheme.getSchemeAgencyId(), editContextSchemePage.getAgencyIDField().getAttribute("value"));
        assertEquals(randomContextScheme.getSchemeVersionId(), editContextSchemePage.getVersionField().getAttribute("value"));
        assertEquals(randomContextScheme.getDescription(), editContextSchemePage.getDescriptionField().getAttribute("value"));

        for (ContextSchemeValueObject contextSchemeValue : randomContextSchemeValues) {
            assertContextSchemeValue(editContextSchemePage, contextSchemeValue);
        }
    }

    @Test
    @DisplayName("TC_5_2_TA_7 (End-User+Admin)")
    public void developer_can_see_context_schemes_created_by_end_user_admin() {
        AppUserObject endUserAdmin = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true);
        thisAccountWillBeDeletedAfterTests(endUserAdmin);

        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(endUserAdmin);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, endUserAdmin);
        List<ContextSchemeValueObject> randomContextSchemeValues = Arrays.asList(
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme),
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme),
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme)
        );

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();

        EditContextSchemePage editContextSchemePage =
                viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());
        assertEquals(randomContextCategory.getName(), editContextSchemePage.getContextCategorySelectField().getText());
        assertEquals(randomContextScheme.getSchemeName(), editContextSchemePage.getNameField().getAttribute("value"));
        assertEquals(randomContextScheme.getSchemeId(), editContextSchemePage.getSchemeIDField().getAttribute("value"));
        assertEquals(randomContextScheme.getSchemeAgencyId(), editContextSchemePage.getAgencyIDField().getAttribute("value"));
        assertEquals(randomContextScheme.getSchemeVersionId(), editContextSchemePage.getVersionField().getAttribute("value"));
        assertEquals(randomContextScheme.getDescription(), editContextSchemePage.getDescriptionField().getAttribute("value"));

        for (ContextSchemeValueObject contextSchemeValue : randomContextSchemeValues) {
            assertContextSchemeValue(editContextSchemePage, contextSchemeValue);
        }
    }

    @Test
    @DisplayName("TC_5_2_TA_8_and_TA_9 (Developer)")
    public void developer_can_edit_context_scheme_created_by_developer_user() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(developer);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, developer);

        ContextCategoryObject appUserContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);
        ContextSchemeObject expectedContextScheme =
                ContextSchemeObject.createRandomContextScheme(appUserContextCategory, appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();

        EditContextSchemePage editContextSchemePage =
                viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());
        editContextSchemePage.updateContextScheme(appUserContextCategory, expectedContextScheme);

        viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        editContextSchemePage =
                viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(expectedContextScheme.getSchemeName());
        assertEquals(appUserContextCategory.getName(), editContextSchemePage.getContextCategorySelectField().getText());
        assertEquals(expectedContextScheme.getSchemeName(), editContextSchemePage.getNameField().getAttribute("value"));
        assertEquals(expectedContextScheme.getSchemeId(), editContextSchemePage.getSchemeIDField().getAttribute("value"));
        assertEquals(expectedContextScheme.getSchemeAgencyId(), editContextSchemePage.getAgencyIDField().getAttribute("value"));
        assertEquals(expectedContextScheme.getSchemeVersionId(), editContextSchemePage.getVersionField().getAttribute("value"));
        assertEquals(expectedContextScheme.getDescription(), editContextSchemePage.getDescriptionField().getAttribute("value"));
    }

    @Test
    @DisplayName("TC_5_2_TA_8_and_TA_9 (Developer+Admin)")
    public void developer_can_edit_context_scheme_created_by_developer_admin_user() {
        AppUserObject developerAdmin = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developerAdmin);

        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(developerAdmin);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, developerAdmin);

        ContextCategoryObject appUserContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);
        ContextSchemeObject expectedContextScheme =
                ContextSchemeObject.createRandomContextScheme(appUserContextCategory, appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();

        EditContextSchemePage editContextSchemePage =
                viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());
        editContextSchemePage.updateContextScheme(appUserContextCategory, expectedContextScheme);

        viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        editContextSchemePage =
                viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(expectedContextScheme.getSchemeName());
        assertEquals(appUserContextCategory.getName(), editContextSchemePage.getContextCategorySelectField().getText());
        assertEquals(expectedContextScheme.getSchemeName(), editContextSchemePage.getNameField().getAttribute("value"));
        assertEquals(expectedContextScheme.getSchemeId(), editContextSchemePage.getSchemeIDField().getAttribute("value"));
        assertEquals(expectedContextScheme.getSchemeAgencyId(), editContextSchemePage.getAgencyIDField().getAttribute("value"));
        assertEquals(expectedContextScheme.getSchemeVersionId(), editContextSchemePage.getVersionField().getAttribute("value"));
        assertEquals(expectedContextScheme.getDescription(), editContextSchemePage.getDescriptionField().getAttribute("value"));
    }

    @Test
    @DisplayName("TC_5_2_TA_8_and_TA_9 (End-User)")
    public void developer_can_edit_context_scheme_created_by_end_user() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(endUser);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, endUser);

        ContextCategoryObject appUserContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);
        ContextSchemeObject expectedContextScheme =
                ContextSchemeObject.createRandomContextScheme(appUserContextCategory, appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();

        EditContextSchemePage editContextSchemePage =
                viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());
        editContextSchemePage.updateContextScheme(appUserContextCategory, expectedContextScheme);

        viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        editContextSchemePage =
                viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(expectedContextScheme.getSchemeName());
        assertEquals(appUserContextCategory.getName(), editContextSchemePage.getContextCategorySelectField().getText());
        assertEquals(expectedContextScheme.getSchemeName(), editContextSchemePage.getNameField().getAttribute("value"));
        assertEquals(expectedContextScheme.getSchemeId(), editContextSchemePage.getSchemeIDField().getAttribute("value"));
        assertEquals(expectedContextScheme.getSchemeAgencyId(), editContextSchemePage.getAgencyIDField().getAttribute("value"));
        assertEquals(expectedContextScheme.getSchemeVersionId(), editContextSchemePage.getVersionField().getAttribute("value"));
        assertEquals(expectedContextScheme.getDescription(), editContextSchemePage.getDescriptionField().getAttribute("value"));
    }

    @Test
    @DisplayName("TC_5_2_TA_8_and_TA_9 (End-User+Admin)")
    public void developer_can_edit_context_scheme_created_by_end_user_admin() {
        AppUserObject endUserAdmin = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true);
        thisAccountWillBeDeletedAfterTests(endUserAdmin);

        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(endUserAdmin);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, endUserAdmin);

        ContextCategoryObject appUserContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);
        ContextSchemeObject expectedContextScheme =
                ContextSchemeObject.createRandomContextScheme(appUserContextCategory, appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();

        EditContextSchemePage editContextSchemePage =
                viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());
        editContextSchemePage.updateContextScheme(appUserContextCategory, expectedContextScheme);

        viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        editContextSchemePage =
                viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(expectedContextScheme.getSchemeName());
        assertEquals(appUserContextCategory.getName(), editContextSchemePage.getContextCategorySelectField().getText());
        assertEquals(expectedContextScheme.getSchemeName(), editContextSchemePage.getNameField().getAttribute("value"));
        assertEquals(expectedContextScheme.getSchemeId(), editContextSchemePage.getSchemeIDField().getAttribute("value"));
        assertEquals(expectedContextScheme.getSchemeAgencyId(), editContextSchemePage.getAgencyIDField().getAttribute("value"));
        assertEquals(expectedContextScheme.getSchemeVersionId(), editContextSchemePage.getVersionField().getAttribute("value"));
        assertEquals(expectedContextScheme.getDescription(), editContextSchemePage.getDescriptionField().getAttribute("value"));
    }

    @Test
    @DisplayName("TC_5_2_TA_10")
    public void developer_can_update_context_scheme_with_all_information_specified() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(developer);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, developer);
        List<ContextSchemeValueObject> randomContextSchemeValues = Arrays.asList(
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme),
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme),
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme)
        );

        ContextCategoryObject appUserContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);
        ContextSchemeObject expectedContextScheme =
                ContextSchemeObject.createRandomContextScheme(appUserContextCategory, appUser);
        List<ContextSchemeValueObject> expectedContextSchemeValues = Arrays.asList(
                ContextSchemeValueObject.createRandomContextSchemeValue(expectedContextScheme),
                ContextSchemeValueObject.createRandomContextSchemeValue(expectedContextScheme),
                ContextSchemeValueObject.createRandomContextSchemeValue(expectedContextScheme)
        );

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();

        EditContextSchemePage editContextSchemePage =
                viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());
        for (ContextSchemeValueObject randomContextSchemeValue : randomContextSchemeValues) {
            editContextSchemePage.removeContextSchemeValue(randomContextSchemeValue);
        }
        for (ContextSchemeValueObject expectedContextSchemeValue : expectedContextSchemeValues) {
            editContextSchemePage.openContextSchemeValueDialog()
                    .addContextSchemeValue(expectedContextSchemeValue);
        }
        editContextSchemePage.updateContextScheme(appUserContextCategory, expectedContextScheme);

        viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        editContextSchemePage =
                viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(expectedContextScheme.getSchemeName());

        assertEquals(appUserContextCategory.getName(), editContextSchemePage.getContextCategorySelectField().getText());
        assertEquals(expectedContextScheme.getSchemeName(), editContextSchemePage.getNameField().getAttribute("value"));
        assertEquals(expectedContextScheme.getSchemeId(), editContextSchemePage.getSchemeIDField().getAttribute("value"));
        assertEquals(expectedContextScheme.getSchemeAgencyId(), editContextSchemePage.getAgencyIDField().getAttribute("value"));
        assertEquals(expectedContextScheme.getSchemeVersionId(), editContextSchemePage.getVersionField().getAttribute("value"));
        assertEquals(expectedContextScheme.getDescription(), editContextSchemePage.getDescriptionField().getAttribute("value"));
        for (ContextSchemeValueObject expectedContextSchemeValue : expectedContextSchemeValues) {
            assertContextSchemeValue(editContextSchemePage, expectedContextSchemeValue);
        }
    }

    @Test
    @DisplayName("TC_5_2_TA_11")
    public void developer_cannot_update_context_scheme_with_missing_required_information() {
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, appUser);
        List<ContextSchemeValueObject> randomContextSchemeValues = Arrays.asList(
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme),
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme),
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme)
        );

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        EditContextSchemePage editContextSchemePage1 =
                contextMenu.openViewEditContextSchemeSubMenu()
                        .openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());
        clear(editContextSchemePage1.getNameField());
        assertThrows(TimeoutException.class, () -> editContextSchemePage1.getUpdateButton());

        EditContextSchemePage editContextSchemePage2 =
                contextMenu.openViewEditContextSchemeSubMenu()
                        .openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());
        clear(editContextSchemePage2.getSchemeIDField());
        assertThrows(TimeoutException.class, () -> editContextSchemePage2.getUpdateButton());

        EditContextSchemePage editContextSchemePage3 =
                contextMenu.openViewEditContextSchemeSubMenu()
                        .openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());
        clear(editContextSchemePage3.getAgencyIDField());
        assertThrows(TimeoutException.class, () -> editContextSchemePage3.getUpdateButton());

        EditContextSchemePage editContextSchemePage4 =
                contextMenu.openViewEditContextSchemeSubMenu()
                        .openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());
        clear(editContextSchemePage4.getVersionField());
        assertThrows(TimeoutException.class, () -> editContextSchemePage4.getUpdateButton());

        EditContextSchemePage editContextSchemePage5 =
                contextMenu.openViewEditContextSchemeSubMenu()
                        .openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());
        ContextSchemeValueDialog contextSchemeValueDialog =
                editContextSchemePage5.openContextSchemeValueDialog(randomContextSchemeValues.get(0));
        clear(contextSchemeValueDialog.getValueField());
        assertThrows(TimeoutException.class, () -> contextSchemeValueDialog.getSaveButton());
    }

    @Test
    @DisplayName("TC_5_2_TA_12 (Developer)")
    public void developer_can_discard_context_schemes_created_by_developer_user() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(developer);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, developer);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        EditContextSchemePage editContextSchemePage =
                contextMenu.openViewEditContextSchemeSubMenu()
                        .openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());

        ViewEditContextSchemePage viewEditContextSchemePage = editContextSchemePage.discard();
        assertThrows(NoSuchElementException.class, () -> {
            viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());
        });
    }

    @Test
    @DisplayName("TC_5_2_TA_12 (Developer+Admin)")
    public void developer_can_discard_context_schemes_created_by_developer_admin_user() {
        AppUserObject developerAdmin = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developerAdmin);

        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(developerAdmin);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, developerAdmin);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        EditContextSchemePage editContextSchemePage =
                contextMenu.openViewEditContextSchemeSubMenu()
                        .openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());

        ViewEditContextSchemePage viewEditContextSchemePage = editContextSchemePage.discard();
        assertThrows(NoSuchElementException.class, () -> {
            viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());
        });
    }

    @Test
    @DisplayName("TC_5_2_TA_12 (End-User)")
    public void developer_can_discard_context_schemes_created_by_end_user() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(endUser);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, endUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        EditContextSchemePage editContextSchemePage =
                contextMenu.openViewEditContextSchemeSubMenu()
                        .openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());

        ViewEditContextSchemePage viewEditContextSchemePage = editContextSchemePage.discard();
        assertThrows(NoSuchElementException.class, () -> {
            viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());
        });
    }

    @Test
    @DisplayName("TC_5_2_TA_12 (End-User+Admin)")
    public void developer_can_discard_context_schemes_created_by_end_user_admin() {
        AppUserObject endUserAdmin = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true);
        thisAccountWillBeDeletedAfterTests(endUserAdmin);

        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(endUserAdmin);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, endUserAdmin);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        EditContextSchemePage editContextSchemePage =
                contextMenu.openViewEditContextSchemeSubMenu()
                        .openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());

        ViewEditContextSchemePage viewEditContextSchemePage = editContextSchemePage.discard();
        assertThrows(NoSuchElementException.class, () -> {
            viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());
        });
    }

    @Test
    @DisplayName("TC_5_2_TA_13")
    public void developer_cannot_discard_context_scheme_that_has_business_context_referencing_it() {
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, appUser);
        ContextSchemeValueObject randomContextSchemeValue =
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme);
        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(appUser);
        BusinessContextValueObject randomBusinessContextValue =
                getAPIFactory().getBusinessContextValueAPI().createRandomBusinessContextValue(
                        randomBusinessContext, randomContextSchemeValue);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        EditContextSchemePage editContextSchemePage =
                contextMenu.openViewEditContextSchemeSubMenu()
                        .openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());

        assertThrows(TimeoutException.class, () -> editContextSchemePage.discard());
    }

    @Test
    @DisplayName("TC_5_2_TA_14 (Developer)")
    public void developer_can_update_business_context_referenced_context_scheme_created_by_developer_user() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(developer);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, developer);
        ContextSchemeValueObject randomContextSchemeValue =
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme);
        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        BusinessContextValueObject randomBusinessContextValue =
                getAPIFactory().getBusinessContextValueAPI().createRandomBusinessContextValue(randomBusinessContext, randomContextSchemeValue);

        ContextCategoryObject newContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);
        ContextSchemeObject newContextScheme =
                ContextSchemeObject.createRandomContextScheme(newContextCategory, appUser);
        ContextSchemeValueObject newContextSchemeValue =
                ContextSchemeValueObject.createRandomContextSchemeValue(newContextScheme);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        EditContextSchemePage editContextSchemePage =
                contextMenu.openViewEditContextSchemeSubMenu()
                        .openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());

        ContextSchemeValueDialog contextSchemeValueDialog =
                editContextSchemePage.openContextSchemeValueDialog(randomContextSchemeValue);
        contextSchemeValueDialog.updateContextSchemeValue(newContextSchemeValue);
        editContextSchemePage.updateContextScheme(newContextCategory, newContextScheme);

        ViewEditBusinessContextPage viewEditBusinessContextPage =
                contextMenu.openViewEditBusinessContextSubMenu();
        EditBusinessContextPage editBusinessContextPage =
                viewEditBusinessContextPage.openEditBusinessContextPageByBusinessContextName(randomBusinessContext.getName());
        BusinessContextValueDialog businessContextValueDialog =
                editBusinessContextPage.openBusinessContextValueDialog(randomBusinessContextValue);

        assertEquals(newContextCategory.getName(), getText(businessContextValueDialog.getContextCategorySelectField()));
        assertEquals(newContextCategory.getDescription(), getText(businessContextValueDialog.getContextCategoryDescriptionField()));

        assertEquals(newContextScheme.getSchemeName(), getText(businessContextValueDialog.getContextSchemeSelectField()));
        assertEquals(newContextScheme.getSchemeId(), getText(businessContextValueDialog.getContextSchemeIDField()));
        assertEquals(newContextScheme.getSchemeAgencyId(), getText(businessContextValueDialog.getContextSchemeAgencyIDField()));
        assertEquals(newContextScheme.getSchemeVersionId(), getText(businessContextValueDialog.getContextSchemeVersionField()));
        assertEquals(newContextScheme.getDescription(), getText(businessContextValueDialog.getContextSchemeDescriptionField()));

        assertEquals(newContextSchemeValue.getValue(), getText(businessContextValueDialog.getContextSchemeValueSelectField()));
        assertEquals(newContextSchemeValue.getMeaning(), getText(businessContextValueDialog.getContextSchemeValueMeaningField()));
    }

    @Test
    @DisplayName("TC_5_2_TA_14 (Developer+Admin)")
    public void developer_can_update_business_context_referenced_context_scheme_created_by_developer_admin_user() {
        AppUserObject developerAdmin = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developerAdmin);

        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(developerAdmin);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, developerAdmin);
        ContextSchemeValueObject randomContextSchemeValue =
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme);
        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developerAdmin);
        BusinessContextValueObject randomBusinessContextValue =
                getAPIFactory().getBusinessContextValueAPI().createRandomBusinessContextValue(randomBusinessContext, randomContextSchemeValue);

        ContextCategoryObject newContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);
        ContextSchemeObject newContextScheme =
                ContextSchemeObject.createRandomContextScheme(newContextCategory, appUser);
        ContextSchemeValueObject newContextSchemeValue =
                ContextSchemeValueObject.createRandomContextSchemeValue(newContextScheme);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        EditContextSchemePage editContextSchemePage =
                contextMenu.openViewEditContextSchemeSubMenu()
                        .openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());

        ContextSchemeValueDialog contextSchemeValueDialog =
                editContextSchemePage.openContextSchemeValueDialog(randomContextSchemeValue);
        contextSchemeValueDialog.updateContextSchemeValue(newContextSchemeValue);
        editContextSchemePage.updateContextScheme(newContextCategory, newContextScheme);

        ViewEditBusinessContextPage viewEditBusinessContextPage =
                contextMenu.openViewEditBusinessContextSubMenu();
        EditBusinessContextPage editBusinessContextPage =
                viewEditBusinessContextPage.openEditBusinessContextPageByBusinessContextName(randomBusinessContext.getName());
        BusinessContextValueDialog businessContextValueDialog =
                editBusinessContextPage.openBusinessContextValueDialog(randomBusinessContextValue);

        assertEquals(newContextCategory.getName(), getText(businessContextValueDialog.getContextCategorySelectField()));
        assertEquals(newContextCategory.getDescription(), getText(businessContextValueDialog.getContextCategoryDescriptionField()));

        assertEquals(newContextScheme.getSchemeName(), getText(businessContextValueDialog.getContextSchemeSelectField()));
        assertEquals(newContextScheme.getSchemeId(), getText(businessContextValueDialog.getContextSchemeIDField()));
        assertEquals(newContextScheme.getSchemeAgencyId(), getText(businessContextValueDialog.getContextSchemeAgencyIDField()));
        assertEquals(newContextScheme.getSchemeVersionId(), getText(businessContextValueDialog.getContextSchemeVersionField()));
        assertEquals(newContextScheme.getDescription(), getText(businessContextValueDialog.getContextSchemeDescriptionField()));

        assertEquals(newContextSchemeValue.getValue(), getText(businessContextValueDialog.getContextSchemeValueSelectField()));
        assertEquals(newContextSchemeValue.getMeaning(), getText(businessContextValueDialog.getContextSchemeValueMeaningField()));
    }

    @Test
    @DisplayName("TC_5_2_TA_14 (End-User)")
    public void developer_can_update_business_context_referenced_context_scheme_created_by_end_user() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(endUser);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, endUser);
        ContextSchemeValueObject randomContextSchemeValue =
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme);
        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        BusinessContextValueObject randomBusinessContextValue =
                getAPIFactory().getBusinessContextValueAPI().createRandomBusinessContextValue(randomBusinessContext, randomContextSchemeValue);

        ContextCategoryObject newContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);
        ContextSchemeObject newContextScheme =
                ContextSchemeObject.createRandomContextScheme(newContextCategory, appUser);
        ContextSchemeValueObject newContextSchemeValue =
                ContextSchemeValueObject.createRandomContextSchemeValue(newContextScheme);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        EditContextSchemePage editContextSchemePage =
                contextMenu.openViewEditContextSchemeSubMenu()
                        .openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());

        ContextSchemeValueDialog contextSchemeValueDialog =
                editContextSchemePage.openContextSchemeValueDialog(randomContextSchemeValue);
        contextSchemeValueDialog.updateContextSchemeValue(newContextSchemeValue);
        editContextSchemePage.updateContextScheme(newContextCategory, newContextScheme);

        ViewEditBusinessContextPage viewEditBusinessContextPage =
                contextMenu.openViewEditBusinessContextSubMenu();
        EditBusinessContextPage editBusinessContextPage =
                viewEditBusinessContextPage.openEditBusinessContextPageByBusinessContextName(randomBusinessContext.getName());
        BusinessContextValueDialog businessContextValueDialog =
                editBusinessContextPage.openBusinessContextValueDialog(randomBusinessContextValue);

        assertEquals(newContextCategory.getName(), getText(businessContextValueDialog.getContextCategorySelectField()));
        assertEquals(newContextCategory.getDescription(), getText(businessContextValueDialog.getContextCategoryDescriptionField()));

        assertEquals(newContextScheme.getSchemeName(), getText(businessContextValueDialog.getContextSchemeSelectField()));
        assertEquals(newContextScheme.getSchemeId(), getText(businessContextValueDialog.getContextSchemeIDField()));
        assertEquals(newContextScheme.getSchemeAgencyId(), getText(businessContextValueDialog.getContextSchemeAgencyIDField()));
        assertEquals(newContextScheme.getSchemeVersionId(), getText(businessContextValueDialog.getContextSchemeVersionField()));
        assertEquals(newContextScheme.getDescription(), getText(businessContextValueDialog.getContextSchemeDescriptionField()));

        assertEquals(newContextSchemeValue.getValue(), getText(businessContextValueDialog.getContextSchemeValueSelectField()));
        assertEquals(newContextSchemeValue.getMeaning(), getText(businessContextValueDialog.getContextSchemeValueMeaningField()));
    }

    @Test
    @DisplayName("TC_5_2_TA_14 (End-User+Admin)")
    public void developer_can_update_business_context_referenced_context_scheme_created_by_end_user_admin() {
        AppUserObject endUserAdmin = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true);
        thisAccountWillBeDeletedAfterTests(endUserAdmin);

        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(endUserAdmin);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, endUserAdmin);
        ContextSchemeValueObject randomContextSchemeValue =
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme);
        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUserAdmin);
        BusinessContextValueObject randomBusinessContextValue =
                getAPIFactory().getBusinessContextValueAPI().createRandomBusinessContextValue(randomBusinessContext, randomContextSchemeValue);

        ContextCategoryObject newContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);
        ContextSchemeObject newContextScheme =
                ContextSchemeObject.createRandomContextScheme(newContextCategory, appUser);
        ContextSchemeValueObject newContextSchemeValue =
                ContextSchemeValueObject.createRandomContextSchemeValue(newContextScheme);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        EditContextSchemePage editContextSchemePage =
                contextMenu.openViewEditContextSchemeSubMenu()
                        .openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());

        ContextSchemeValueDialog contextSchemeValueDialog =
                editContextSchemePage.openContextSchemeValueDialog(randomContextSchemeValue);
        contextSchemeValueDialog.updateContextSchemeValue(newContextSchemeValue);
        editContextSchemePage.updateContextScheme(newContextCategory, newContextScheme);

        ViewEditBusinessContextPage viewEditBusinessContextPage =
                contextMenu.openViewEditBusinessContextSubMenu();
        EditBusinessContextPage editBusinessContextPage =
                viewEditBusinessContextPage.openEditBusinessContextPageByBusinessContextName(randomBusinessContext.getName());
        BusinessContextValueDialog businessContextValueDialog =
                editBusinessContextPage.openBusinessContextValueDialog(randomBusinessContextValue);

        assertEquals(newContextCategory.getName(), getText(businessContextValueDialog.getContextCategorySelectField()));
        assertEquals(newContextCategory.getDescription(), getText(businessContextValueDialog.getContextCategoryDescriptionField()));

        assertEquals(newContextScheme.getSchemeName(), getText(businessContextValueDialog.getContextSchemeSelectField()));
        assertEquals(newContextScheme.getSchemeId(), getText(businessContextValueDialog.getContextSchemeIDField()));
        assertEquals(newContextScheme.getSchemeAgencyId(), getText(businessContextValueDialog.getContextSchemeAgencyIDField()));
        assertEquals(newContextScheme.getSchemeVersionId(), getText(businessContextValueDialog.getContextSchemeVersionField()));
        assertEquals(newContextScheme.getDescription(), getText(businessContextValueDialog.getContextSchemeDescriptionField()));

        assertEquals(newContextSchemeValue.getValue(), getText(businessContextValueDialog.getContextSchemeValueSelectField()));
        assertEquals(newContextSchemeValue.getMeaning(), getText(businessContextValueDialog.getContextSchemeValueMeaningField()));
    }

    @Test
    @DisplayName("TC_5_2_TA_15")
    public void developer_cannot_remove_context_scheme_value_if_it_is_used_by_business_context() {
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, appUser);
        ContextSchemeValueObject randomContextSchemeValue =
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme);
        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(appUser);
        BusinessContextValueObject randomBusinessContextValue =
                getAPIFactory().getBusinessContextValueAPI().createRandomBusinessContextValue(randomBusinessContext, randomContextSchemeValue);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        EditContextSchemePage editContextSchemePage =
                contextMenu.openViewEditContextSchemeSubMenu()
                        .openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());
        assertThrows(TimeoutException.class, () -> {
            editContextSchemePage.removeContextSchemeValue(randomContextSchemeValue);
        });
    }

    @Test
    @DisplayName("TC_5_2_TA_16")
    public void developer_cannot_add_duplicate_context_scheme_value() {
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, appUser);
        ContextSchemeValueObject randomContextSchemeValue =
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        EditContextSchemePage editContextSchemePage =
                contextMenu.openViewEditContextSchemeSubMenu()
                        .openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());
        ContextSchemeValueDialog contextSchemeValueDialog = editContextSchemePage.openContextSchemeValueDialog();
        contextSchemeValueDialog.addContextSchemeValue(randomContextSchemeValue);

        assertEquals(randomContextSchemeValue.getValue() + " already exist", getSnackBarMessage(getDriver()));
    }

    @Test
    @DisplayName("TC_5_2_TA_17 (Updater field)")
    public void test_search_feature_using_updater_field() {
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu;
        ViewEditContextSchemePage viewEditContextSchemePage;

        // Test 'Updater' field
        contextMenu = homePage.getContextMenu();
        viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        viewEditContextSchemePage.setUpdater(appUser.getLoginId());
        viewEditContextSchemePage.hitSearchButton();
        assertContextSchemeNameInTheSearchResultsAtFirst(
                viewEditContextSchemePage, randomContextScheme.getSchemeName());
    }

    @Test
    @DisplayName("TC_5_2_TA_17 (Name field)")
    public void test_search_feature_using_name_field() {
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu;
        ViewEditContextSchemePage viewEditContextSchemePage;

        // Test 'Name' field
        contextMenu = homePage.getContextMenu();
        viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        viewEditContextSchemePage.setName(randomContextScheme.getSchemeName());
        viewEditContextSchemePage.hitSearchButton();
        assertContextSchemeNameInTheSearchResultsAtFirst(
                viewEditContextSchemePage, randomContextScheme.getSchemeName());
    }

    private void assertContextSchemeNameInTheSearchResultsAtFirst(ViewEditContextSchemePage viewEditContextSchemePage, String name) {
        retry(() -> {
            WebElement tr = viewEditContextSchemePage.getTableRecordAtIndex(1);
            WebElement td = viewEditContextSchemePage.getColumnByName(tr, "schemeName");
            assertEquals(name, td.findElement(By.cssSelector("a > span")).getText());
        });
    }

    @Test
    @DisplayName("TC_5_2_TA_18a")
    public void developer_can_add_context_scheme_from_developer_code_list_in_latest_release() {
        NamespaceObject namespace =
                getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ReleaseObject latestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
        CodeListObject randomCodeList =
                getAPIFactory().getCodeListAPI().createRandomCodeList(appUser, namespace, latestRelease, "Published");
        List<CodeListValueObject> randomCodeListValues = Arrays.asList(
                getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(randomCodeList, appUser),
                getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(randomCodeList, appUser),
                getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(randomCodeList, appUser)
        );
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        CreateContextSchemePage createContextSchemePage = viewEditContextSchemePage.openCreateContextSchemePage();
        LoadFromCodeListDialog loadFromCodeListDialog = createContextSchemePage.openLoadFromCodeListDialog();
        loadFromCodeListDialog.selectCodeListByCodeListNameAndBranch(randomCodeList.getName(), latestRelease.getReleaseNumber());

        assertEquals(randomCodeList.getListId(), getText(createContextSchemePage.getSchemeIDField()));
        AgencyIDListValueObject agencyIdListValue =
                getAPIFactory().getAgencyIDListValueAPI().getAgencyIDListValueByManifestId(randomCodeList.getAgencyIdListValueManifestId());
        assertEquals(agencyIdListValue.getValue(), getText(createContextSchemePage.getAgencyIDField()));
        assertEquals(randomCodeList.getVersionId(), getText(createContextSchemePage.getVersionField()));
        for (CodeListValueObject codeListValue : randomCodeListValues) {
            ContextSchemeValueDialog contextSchemeValueDialog = createContextSchemePage.openContextSchemeValueDialogByValue(codeListValue.getValue());
            assertEquals(codeListValue.getMeaning(), getText(contextSchemeValueDialog.getMeaningField()));
            escape(getDriver());
        }

        createContextSchemePage.setContextCategory(randomContextCategory);
        String name = "cs_" + randomAlphanumeric(5, 10);
        createContextSchemePage.setName(name);
        createContextSchemePage.hitCreateButton();

        EditContextSchemePage editContextSchemePage = homePage.getContextMenu()
                .openViewEditContextSchemeSubMenu()
                .openEditContextSchemePageByContextSchemeName(name);
        assertEquals(randomContextCategory.getName(), getText(editContextSchemePage.getContextCategorySelectField()));
        assertEquals(name, getText(editContextSchemePage.getNameField()));
    }

    @Test
    @DisplayName("TC_5_2_TA_18b")
    public void developer_can_add_context_scheme_from_developer_code_list_in_older_release() {
        NamespaceObject namespace =
                getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ReleaseObject olderRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        CodeListObject randomCodeList =
                getAPIFactory().getCodeListAPI().createRandomCodeList(appUser, namespace, olderRelease, "Published");
        List<CodeListValueObject> randomCodeListValues = Arrays.asList(
                getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(randomCodeList, appUser),
                getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(randomCodeList, appUser),
                getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(randomCodeList, appUser)
        );
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        CreateContextSchemePage createContextSchemePage = viewEditContextSchemePage.openCreateContextSchemePage();
        LoadFromCodeListDialog loadFromCodeListDialog = createContextSchemePage.openLoadFromCodeListDialog();
        loadFromCodeListDialog.selectCodeListByCodeListNameAndBranch(randomCodeList.getName(), olderRelease.getReleaseNumber());

        assertEquals(randomCodeList.getListId(), getText(createContextSchemePage.getSchemeIDField()));
        AgencyIDListValueObject agencyIdListValue =
                getAPIFactory().getAgencyIDListValueAPI().getAgencyIDListValueByManifestId(randomCodeList.getAgencyIdListValueManifestId());
        assertEquals(agencyIdListValue.getValue(), getText(createContextSchemePage.getAgencyIDField()));
        assertEquals(randomCodeList.getVersionId(), getText(createContextSchemePage.getVersionField()));
        for (CodeListValueObject codeListValue : randomCodeListValues) {
            ContextSchemeValueDialog contextSchemeValueDialog = createContextSchemePage.openContextSchemeValueDialogByValue(codeListValue.getValue());
            assertEquals(codeListValue.getMeaning(), getText(contextSchemeValueDialog.getMeaningField()));
            escape(getDriver());
        }

        createContextSchemePage.setContextCategory(randomContextCategory);
        String name = "cs_" + randomAlphanumeric(5, 10);
        createContextSchemePage.setName(name);
        createContextSchemePage.hitCreateButton();

        EditContextSchemePage editContextSchemePage = homePage.getContextMenu()
                .openViewEditContextSchemeSubMenu()
                .openEditContextSchemePageByContextSchemeName(name);
        assertEquals(randomContextCategory.getName(), getText(editContextSchemePage.getContextCategorySelectField()));
        assertEquals(name, getText(editContextSchemePage.getNameField()));
    }

    @Test
    @DisplayName("TC_5_2_TA_18c_and_TA_19")
    public void developer_can_add_context_scheme_from_derived_end_user_code_list_in_latest_release() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        NamespaceObject namespace =
                getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
        ReleaseObject latestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
        CodeListObject baseCodeList = getAPIFactory().getCodeListAPI().getCodeListByCodeListNameAndReleaseNum(
                "oacl_RelatedCorrectiveActionCode", latestRelease.getReleaseNumber());
        CodeListObject derivedCodeList =
                getAPIFactory().getCodeListAPI().createDerivedCodeList(baseCodeList, endUser, namespace, latestRelease, "Published");
        List<CodeListValueObject> derivedCodeListValues =
                getAPIFactory().getCodeListValueAPI().getCodeListValuesByCodeListManifestId(derivedCodeList.getCodeListManifestId());
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        CreateContextSchemePage createContextSchemePage = viewEditContextSchemePage.openCreateContextSchemePage();
        LoadFromCodeListDialog loadFromCodeListDialog = createContextSchemePage.openLoadFromCodeListDialog();
        loadFromCodeListDialog.selectCodeListByCodeListNameAndBranch(derivedCodeList.getName(), latestRelease.getReleaseNumber());

        assertEquals(derivedCodeList.getListId(), getText(createContextSchemePage.getSchemeIDField()));
        AgencyIDListValueObject agencyIdListValue =
                getAPIFactory().getAgencyIDListValueAPI().getAgencyIDListValueByManifestId(derivedCodeList.getAgencyIdListValueManifestId());
        assertEquals(agencyIdListValue.getValue(), getText(createContextSchemePage.getAgencyIDField()));
        assertEquals(derivedCodeList.getVersionId(), getText(createContextSchemePage.getVersionField()));
        for (CodeListValueObject codeListValue : derivedCodeListValues) {
            ContextSchemeValueDialog contextSchemeValueDialog = createContextSchemePage.openContextSchemeValueDialogByValue(codeListValue.getValue());
            assertEquals(codeListValue.getMeaning(), getText(contextSchemeValueDialog.getMeaningField()));
            escape(getDriver());
        }

        createContextSchemePage.setContextCategory(randomContextCategory);
        String name = "cs_" + randomAlphanumeric(5, 10);
        createContextSchemePage.setName(name);
        createContextSchemePage.hitCreateButton();

        EditContextSchemePage editContextSchemePage = homePage.getContextMenu()
                .openViewEditContextSchemeSubMenu()
                .openEditContextSchemePageByContextSchemeName(name);
        assertEquals(randomContextCategory.getName(), getText(editContextSchemePage.getContextCategorySelectField()));
        assertEquals(name, getText(editContextSchemePage.getNameField()));
    }

    @Test
    @DisplayName("TC_5_2_TA_18d_and_TA_19")
    public void developer_can_add_context_scheme_from_derived_end_user_code_list_in_older_release() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        NamespaceObject namespace =
                getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
        ReleaseObject latestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.6");
        CodeListObject baseCodeList = getAPIFactory().getCodeListAPI().getCodeListByCodeListNameAndReleaseNum(
                "oacl_RelatedCorrectiveActionCode", latestRelease.getReleaseNumber());
        CodeListObject derivedCodeList =
                getAPIFactory().getCodeListAPI().createDerivedCodeList(baseCodeList, endUser, namespace, latestRelease, "Published");
        List<CodeListValueObject> derivedCodeListValues =
                getAPIFactory().getCodeListValueAPI().getCodeListValuesByCodeListManifestId(derivedCodeList.getCodeListManifestId());
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        CreateContextSchemePage createContextSchemePage = viewEditContextSchemePage.openCreateContextSchemePage();
        LoadFromCodeListDialog loadFromCodeListDialog = createContextSchemePage.openLoadFromCodeListDialog();
        loadFromCodeListDialog.selectCodeListByCodeListNameAndBranch(derivedCodeList.getName(), latestRelease.getReleaseNumber());

        assertEquals(derivedCodeList.getListId(), getText(createContextSchemePage.getSchemeIDField()));
        AgencyIDListValueObject agencyIdListValue =
                getAPIFactory().getAgencyIDListValueAPI().getAgencyIDListValueByManifestId(derivedCodeList.getAgencyIdListValueManifestId());
        assertEquals(agencyIdListValue.getValue(), getText(createContextSchemePage.getAgencyIDField()));
        assertEquals(derivedCodeList.getVersionId(), getText(createContextSchemePage.getVersionField()));
        for (CodeListValueObject codeListValue : derivedCodeListValues) {
            ContextSchemeValueDialog contextSchemeValueDialog = createContextSchemePage.openContextSchemeValueDialogByValue(codeListValue.getValue());
            assertEquals(codeListValue.getMeaning(), getText(contextSchemeValueDialog.getMeaningField()));
            escape(getDriver());
        }

        createContextSchemePage.setContextCategory(randomContextCategory);
        String name = "cs_" + randomAlphanumeric(5, 10);
        createContextSchemePage.setName(name);
        createContextSchemePage.hitCreateButton();

        EditContextSchemePage editContextSchemePage = homePage.getContextMenu()
                .openViewEditContextSchemeSubMenu()
                .openEditContextSchemePageByContextSchemeName(name);
        assertEquals(randomContextCategory.getName(), getText(editContextSchemePage.getContextCategorySelectField()));
        assertEquals(name, getText(editContextSchemePage.getNameField()));
    }

    @Test
    @DisplayName("TC_5_2_TA_20")
    public void developer_can_change_context_scheme_values_added_by_code_list() {
        NamespaceObject namespace =
                getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ReleaseObject latestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
        CodeListObject randomCodeList =
                getAPIFactory().getCodeListAPI().createRandomCodeList(appUser, namespace, latestRelease, "Published");
        List<CodeListValueObject> randomCodeListValues = Arrays.asList(
                getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(randomCodeList, appUser),
                getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(randomCodeList, appUser),
                getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(randomCodeList, appUser)
        );
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        CreateContextSchemePage createContextSchemePage = viewEditContextSchemePage.openCreateContextSchemePage();
        LoadFromCodeListDialog loadFromCodeListDialog = createContextSchemePage.openLoadFromCodeListDialog();
        loadFromCodeListDialog.selectCodeListByCodeListNameAndBranch(randomCodeList.getName(), latestRelease.getReleaseNumber());

        assertEquals(randomCodeList.getListId(), getText(createContextSchemePage.getSchemeIDField()));
        AgencyIDListValueObject agencyIdListValue =
                getAPIFactory().getAgencyIDListValueAPI().getAgencyIDListValueByManifestId(randomCodeList.getAgencyIdListValueManifestId());
        assertEquals(agencyIdListValue.getValue(), getText(createContextSchemePage.getAgencyIDField()));
        assertEquals(randomCodeList.getVersionId(), getText(createContextSchemePage.getVersionField()));

        List<ContextSchemeValueObject> randomContextSchemeValues = new ArrayList<>();
        for (CodeListValueObject codeListValue : randomCodeListValues) {
            ContextSchemeValueDialog contextSchemeValueDialog = createContextSchemePage.openContextSchemeValueDialogByValue(codeListValue.getValue());
            assertEquals(codeListValue.getMeaning(), getText(contextSchemeValueDialog.getMeaningField()));

            ContextSchemeValueObject randomContextSchemeValue = new ContextSchemeValueObject();
            randomContextSchemeValue.setValue("csv_" + randomAlphanumeric(5, 10));
            randomContextSchemeValue.setMeaning(randomPrint(50, 100).trim());
            randomContextSchemeValues.add(randomContextSchemeValue);

            contextSchemeValueDialog.setValue(randomContextSchemeValue.getValue());
            contextSchemeValueDialog.setMeaning(randomContextSchemeValue.getMeaning());

            click(contextSchemeValueDialog.getSaveButton());
        }

        createContextSchemePage.setContextCategory(randomContextCategory);
        String name = "cs_" + randomAlphanumeric(5, 10);
        createContextSchemePage.setName(name);
        createContextSchemePage.hitCreateButton();

        EditContextSchemePage editContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu()
                .openEditContextSchemePageByContextSchemeName(name);
        assertEquals(randomContextCategory.getName(), getText(editContextSchemePage.getContextCategorySelectField()));
        assertEquals(name, getText(editContextSchemePage.getNameField()));
        for (ContextSchemeValueObject randomContextSchemeValue : randomContextSchemeValues) {
            ContextSchemeValueDialog contextSchemeValueDialog =
                    editContextSchemePage.openContextSchemeValueDialog(randomContextSchemeValue);
            assertEquals(randomContextSchemeValue.getValue(), getText(contextSchemeValueDialog.getValueField()));
            assertEquals(randomContextSchemeValue.getMeaning(), getText(contextSchemeValueDialog.getMeaningField()));
            escape(getDriver());
        }
    }

    @Test
    @DisplayName("TC_5_2_TA_21")
    public void developer_can_add_value_to_context_scheme_after_values_loaded_from_code_list() {
        NamespaceObject namespace =
                getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ReleaseObject latestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
        CodeListObject randomCodeList =
                getAPIFactory().getCodeListAPI().createRandomCodeList(appUser, namespace, latestRelease, "Published");
        List<CodeListValueObject> randomCodeListValues = Arrays.asList(
                getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(randomCodeList, appUser),
                getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(randomCodeList, appUser),
                getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(randomCodeList, appUser)
        );
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        CreateContextSchemePage createContextSchemePage = viewEditContextSchemePage.openCreateContextSchemePage();
        LoadFromCodeListDialog loadFromCodeListDialog = createContextSchemePage.openLoadFromCodeListDialog();
        loadFromCodeListDialog.selectCodeListByCodeListNameAndBranch(randomCodeList.getName(), latestRelease.getReleaseNumber());

        assertEquals(randomCodeList.getListId(), getText(createContextSchemePage.getSchemeIDField()));
        AgencyIDListValueObject agencyIdListValue =
                getAPIFactory().getAgencyIDListValueAPI().getAgencyIDListValueByManifestId(randomCodeList.getAgencyIdListValueManifestId());
        assertEquals(agencyIdListValue.getValue(), getText(createContextSchemePage.getAgencyIDField()));
        assertEquals(randomCodeList.getVersionId(), getText(createContextSchemePage.getVersionField()));

        List<ContextSchemeValueObject> randomContextSchemeValues = new ArrayList<>();
        for (int i = 0, len = randomCodeListValues.size(); i < len; ++i) {
            ContextSchemeValueDialog contextSchemeValueDialog = createContextSchemePage.openContextSchemeValueDialog();

            ContextSchemeValueObject randomContextSchemeValue = new ContextSchemeValueObject();
            randomContextSchemeValue.setValue("csv_" + randomAlphanumeric(5, 10));
            randomContextSchemeValue.setMeaning(randomPrint(50, 100).trim());
            randomContextSchemeValues.add(randomContextSchemeValue);

            contextSchemeValueDialog.setValue(randomContextSchemeValue.getValue());
            contextSchemeValueDialog.setMeaning(randomContextSchemeValue.getMeaning());

            click(contextSchemeValueDialog.getAddButton());
        }

        createContextSchemePage.setContextCategory(randomContextCategory);
        String name = "cs_" + randomAlphanumeric(5, 10);
        createContextSchemePage.setName(name);
        createContextSchemePage.hitCreateButton();

        EditContextSchemePage editContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu()
                .openEditContextSchemePageByContextSchemeName(name);
        assertEquals(randomContextCategory.getName(), getText(editContextSchemePage.getContextCategorySelectField()));
        assertEquals(name, getText(editContextSchemePage.getNameField()));
        for (CodeListValueObject codeListValue : randomCodeListValues) {
            ContextSchemeValueDialog contextSchemeValueDialog = editContextSchemePage.openContextSchemeValueDialogByValue(codeListValue.getValue());
            assertEquals(codeListValue.getMeaning(), getText(contextSchemeValueDialog.getMeaningField()));
            escape(getDriver());
        }
        for (ContextSchemeValueObject randomContextSchemeValue : randomContextSchemeValues) {
            ContextSchemeValueDialog contextSchemeValueDialog =
                    editContextSchemePage.openContextSchemeValueDialog(randomContextSchemeValue);
            assertEquals(randomContextSchemeValue.getValue(), getText(contextSchemeValueDialog.getValueField()));
            assertEquals(randomContextSchemeValue.getMeaning(), getText(contextSchemeValueDialog.getMeaningField()));
            escape(getDriver());
        }
    }

    @Test
    @DisplayName("TC_5_2_TA_22")
    public void developer_can_delete_value_from_context_scheme_added_by_selected_code_list() {
        NamespaceObject namespace =
                getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ReleaseObject latestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
        CodeListObject randomCodeList =
                getAPIFactory().getCodeListAPI().createRandomCodeList(appUser, namespace, latestRelease, "Published");
        List<CodeListValueObject> randomCodeListValues = Arrays.asList(
                getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(randomCodeList, appUser),
                getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(randomCodeList, appUser),
                getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(randomCodeList, appUser)
        );
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        CreateContextSchemePage createContextSchemePage = viewEditContextSchemePage.openCreateContextSchemePage();
        LoadFromCodeListDialog loadFromCodeListDialog = createContextSchemePage.openLoadFromCodeListDialog();
        loadFromCodeListDialog.selectCodeListByCodeListNameAndBranch(randomCodeList.getName(), latestRelease.getReleaseNumber());

        assertEquals(randomCodeList.getListId(), getText(createContextSchemePage.getSchemeIDField()));
        AgencyIDListValueObject agencyIdListValue =
                getAPIFactory().getAgencyIDListValueAPI().getAgencyIDListValueByManifestId(randomCodeList.getAgencyIdListValueManifestId());
        assertEquals(agencyIdListValue.getValue(), getText(createContextSchemePage.getAgencyIDField()));
        assertEquals(randomCodeList.getVersionId(), getText(createContextSchemePage.getVersionField()));

        for (CodeListValueObject codeListValue : randomCodeListValues) {
            createContextSchemePage.removeContextSchemeValue(codeListValue.getValue());
        }

        createContextSchemePage.setContextCategory(randomContextCategory);
        String name = "cs_" + randomAlphanumeric(5, 10);
        createContextSchemePage.setName(name);
        createContextSchemePage.hitCreateButton();

        EditContextSchemePage editContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu()
                .openEditContextSchemePageByContextSchemeName(name);
        assertEquals(randomContextCategory.getName(), getText(editContextSchemePage.getContextCategorySelectField()));
        assertEquals(name, getText(editContextSchemePage.getNameField()));
        for (CodeListValueObject codeListValue : randomCodeListValues) {
            assertThrows(NoSuchElementException.class, () -> {
                editContextSchemePage.openContextSchemeValueDialogByValue(codeListValue.getValue());
            });
        }
    }

    @Test
    @DisplayName("TC_5_2_TA_23")
    public void developer_cannot_use_load_from_code_list_function_if_value_is_used_by_business_context() {
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);
        ContextSchemeObject randomContextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, appUser);
        ContextSchemeValueObject randomContextSchemeValue =
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextScheme);
        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(appUser);
        BusinessContextValueObject randomBusinessContextValue =
                getAPIFactory().getBusinessContextValueAPI().createRandomBusinessContextValue(
                        randomBusinessContext, randomContextSchemeValue);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        EditContextSchemePage editContextSchemePage =
                contextMenu.openViewEditContextSchemeSubMenu()
                        .openEditContextSchemePageByContextSchemeName(randomContextScheme.getSchemeName());

        assertThrows(TimeoutException.class, () -> editContextSchemePage.openLoadFromCodeListDialog());
    }

    @Test
    @DisplayName("TC_5_2_TA_24")
    public void test_checkbox_selection() {
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);
        String namePrefix = "cs_TC52_TA24";
        List<ContextSchemeObject> randomContextSchemes = new ArrayList<>();
        for (int i = 0; i < RandomUtils.nextInt(11, 20); ++i) {
            ContextSchemeObject randomContextScheme =
                    getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategory, appUser, namePrefix);
            randomContextSchemes.add(randomContextScheme);
        }

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();

        viewEditContextSchemePage.setName(namePrefix);
        viewEditContextSchemePage.hitSearchButton();

        By checkboxOfFirstRecordLocator = By.xpath("//table/tbody" +
                "/tr[" + RandomUtils.nextInt(1, 10) + "]/td[1]//mat-checkbox[@ng-reflect-disabled=\"true\" or not(@disabled='true')]//input");
        retry(() -> {
            WebElement checkboxOfFirstRecord = new FluentWait<>(getDriver())
                    .withTimeout(Duration.ofSeconds(3L))
                    .pollingEvery(Duration.ofMillis(100L))
                    .until(ExpectedConditions.elementToBeClickable(checkboxOfFirstRecordLocator));

            // Click the checkbox
            new Actions(getDriver()).moveToElement(checkboxOfFirstRecord).perform();
            checkboxOfFirstRecord.sendKeys(Keys.SPACE);
        });

        viewEditContextSchemePage.goToNextPage();
        viewEditContextSchemePage.goToPreviousPage();

        retry(() -> {
            WebElement checkboxOfFirstRecord = new FluentWait<>(getDriver())
                    .withTimeout(Duration.ofSeconds(3L))
                    .pollingEvery(Duration.ofMillis(100L))
                    .until(ExpectedConditions.elementToBeClickable(checkboxOfFirstRecordLocator));
            assertChecked(checkboxOfFirstRecord);
        });
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
