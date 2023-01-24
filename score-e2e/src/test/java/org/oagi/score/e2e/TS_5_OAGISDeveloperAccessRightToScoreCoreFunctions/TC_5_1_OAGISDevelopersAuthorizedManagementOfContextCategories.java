package org.oagi.score.e2e.TS_5_OAGISDeveloperAccessRightToScoreCoreFunctions;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.menu.ContextMenu;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.ContextCategoryObject;
import org.oagi.score.e2e.obj.ContextSchemeObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.context.CreateContextCategoryPage;
import org.oagi.score.e2e.page.context.EditContextCategoryPage;
import org.oagi.score.e2e.page.context.ViewEditContextCategoryPage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertChecked;
import static org.oagi.score.e2e.impl.PageHelper.retry;

@Execution(ExecutionMode.CONCURRENT)
public class TC_5_1_OAGISDevelopersAuthorizedManagementOfContextCategories extends BaseTest {

    private AppUserObject appUser;

    private List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

        // Create random end-user
        appUser = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(appUser);
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_5_1_TA_1")
    public void developer_can_create_context_category_with_only_required_information() {
        ContextCategoryObject contextCategory = new ContextCategoryObject();
        contextCategory.setName("cat_" + randomAlphanumeric(5, 10));

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextCategoryPage viewEditContextCategoryPage = contextMenu.openViewEditContextCategorySubMenu();
        CreateContextCategoryPage createContextCategoryPage = viewEditContextCategoryPage.openCreateContextCategoryPage();
        viewEditContextCategoryPage = createContextCategoryPage.createContextCategory(contextCategory);

        EditContextCategoryPage editContextCategoryPage = viewEditContextCategoryPage.openEditContextCategoryPageByContextCategoryName(contextCategory.getName());
        assertEquals(contextCategory.getName(), editContextCategoryPage.getNameFieldText());
        assertTrue(StringUtils.isEmpty(editContextCategoryPage.getDescriptionFieldText()));
    }

    @Test
    @DisplayName("TC_5_1_TA_2")
    public void developer_can_create_context_category_with_all_information_specified() {
        ContextCategoryObject contextCategory = new ContextCategoryObject();
        contextCategory.setName("cat_" + randomAlphanumeric(5, 10));
        contextCategory.setDescription(randomPrint(50, 100).trim());

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextCategoryPage viewEditContextCategoryPage = contextMenu.openViewEditContextCategorySubMenu();
        CreateContextCategoryPage createContextCategoryPage = viewEditContextCategoryPage.openCreateContextCategoryPage();
        viewEditContextCategoryPage = createContextCategoryPage.createContextCategory(contextCategory);

        EditContextCategoryPage editContextCategoryPage = viewEditContextCategoryPage.openEditContextCategoryPageByContextCategoryName(contextCategory.getName());
        assertEquals(contextCategory.getName(), editContextCategoryPage.getNameFieldText());
        assertEquals(contextCategory.getDescription(), editContextCategoryPage.getDescriptionFieldText());
    }

    @Test
    @DisplayName("TC_5_1_TA_3")
    public void developer_cannot_create_context_category_without_required_information() {
        ContextCategoryObject contextCategory = new ContextCategoryObject();
        contextCategory.setDescription(randomPrint(50, 100).trim());

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextCategoryPage viewEditContextCategoryPage = contextMenu.openViewEditContextCategorySubMenu();
        CreateContextCategoryPage createContextCategoryPage = viewEditContextCategoryPage.openCreateContextCategoryPage();
        assertThrows(TimeoutException.class, () ->
                createContextCategoryPage.createContextCategory(contextCategory));
    }

    @Test
    @DisplayName("TC_5_1_TA_4")
    public void developer_can_see_context_category_created_by_any_user_in_the_list() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        ContextCategoryObject developerContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(developer);

        AppUserObject developerAdmin = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developerAdmin);
        ContextCategoryObject developerAdminContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(developerAdmin);

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        ContextCategoryObject endUserContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(endUser);

        AppUserObject endUserAdmin = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true);
        thisAccountWillBeDeletedAfterTests(endUserAdmin);
        ContextCategoryObject endUserAdminContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(endUserAdmin);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());

        for (ContextCategoryObject contextCategory : Arrays.asList(
                developerContextCategory, developerAdminContextCategory,
                endUserContextCategory, endUserAdminContextCategory)) {
            ContextMenu contextMenu = homePage.getContextMenu();
            ViewEditContextCategoryPage viewEditContextCategoryPage = contextMenu.openViewEditContextCategorySubMenu();

            EditContextCategoryPage editContextCategoryPage =
                    viewEditContextCategoryPage.openEditContextCategoryPageByContextCategoryName(contextCategory.getName());
            assertTrue(editContextCategoryPage.isOpened());
        }
    }

    @Test
    @DisplayName("TC_5_1_TA_5")
    public void developer_can_see_and_edit_context_category_created_by_any_user() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        ContextCategoryObject developerContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(developer);

        AppUserObject developerAdmin = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developerAdmin);
        ContextCategoryObject developerAdminContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(developerAdmin);

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        ContextCategoryObject endUserContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(endUser);

        AppUserObject endUserAdmin = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true);
        thisAccountWillBeDeletedAfterTests(endUserAdmin);
        ContextCategoryObject endUserAdminContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(endUserAdmin);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());

        for (ContextCategoryObject contextCategory : Arrays.asList(
                developerContextCategory, developerAdminContextCategory,
                endUserContextCategory, endUserAdminContextCategory)) {
            ContextMenu contextMenu = homePage.getContextMenu();
            ViewEditContextCategoryPage viewEditContextCategoryPage = contextMenu.openViewEditContextCategorySubMenu();

            EditContextCategoryPage editContextCategoryPage =
                    viewEditContextCategoryPage.openEditContextCategoryPageByContextCategoryName(contextCategory.getName());
            assertEquals(contextCategory.getName(), editContextCategoryPage.getNameFieldText());
            assertEquals(contextCategory.getDescription(), editContextCategoryPage.getDescriptionFieldText());
            assertTrue(editContextCategoryPage.getDiscardButton().isEnabled());
        }
    }

    @Test
    @DisplayName("TC_5_1_TA_6")
    public void developer_can_update_context_category_with_only_required_information() {
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        EditContextCategoryPage editContextCategoryPage =
                contextMenu.openViewEditContextCategorySubMenu()
                        .openEditContextCategoryPageByContextCategoryName(randomContextCategory.getName());

        String oldName = randomContextCategory.getName();
        randomContextCategory.setName("cat_" + randomAlphanumeric(5, 10));
        assertFalse(oldName.equals(randomContextCategory.getName()));

        String oldDescription = randomContextCategory.getDescription();
        randomContextCategory.setDescription(null);
        assertFalse(oldDescription.equals(randomContextCategory.getDescription()));

        editContextCategoryPage.updateContextCategory(randomContextCategory);

        assertThrows(NoSuchElementException.class, () ->
                contextMenu.openViewEditContextCategorySubMenu()
                        .openEditContextCategoryPageByContextCategoryName(oldName));

        editContextCategoryPage =
                contextMenu.openViewEditContextCategorySubMenu()
                        .openEditContextCategoryPageByContextCategoryName(randomContextCategory.getName());
        assertEquals(randomContextCategory.getName(), editContextCategoryPage.getNameFieldText());
        assertTrue(StringUtils.isEmpty(editContextCategoryPage.getDescriptionFieldText()));
    }

    @Test
    @DisplayName("TC_5_1_TA_7")
    public void developer_can_update_context_category_with_all_information_specified() {
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        EditContextCategoryPage editContextCategoryPage =
                contextMenu.openViewEditContextCategorySubMenu()
                        .openEditContextCategoryPageByContextCategoryName(randomContextCategory.getName());

        String oldName = randomContextCategory.getName();
        randomContextCategory.setName("cat_" + randomAlphanumeric(5, 10));
        assertFalse(oldName.equals(randomContextCategory.getName()));

        String oldDescription = randomContextCategory.getDescription();
        randomContextCategory.setDescription(randomPrint(50, 100).trim());
        assertFalse(oldDescription.equals(randomContextCategory.getDescription()));

        editContextCategoryPage.updateContextCategory(randomContextCategory);

        assertThrows(NoSuchElementException.class, () ->
                contextMenu.openViewEditContextCategorySubMenu()
                        .openEditContextCategoryPageByContextCategoryName(oldName));

        editContextCategoryPage =
                contextMenu.openViewEditContextCategorySubMenu()
                        .openEditContextCategoryPageByContextCategoryName(randomContextCategory.getName());
        assertEquals(randomContextCategory.getName(), editContextCategoryPage.getNameFieldText());
        assertEquals(randomContextCategory.getDescription(), editContextCategoryPage.getDescriptionFieldText());
    }

    @Test
    @DisplayName("TC_5_1_TA_8")
    public void developer_cannot_update_context_category_with_missing_required_information() {
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        EditContextCategoryPage editContextCategoryPage =
                contextMenu.openViewEditContextCategorySubMenu()
                        .openEditContextCategoryPageByContextCategoryName(randomContextCategory.getName());

        String oldName = randomContextCategory.getName();
        randomContextCategory.setName(null);
        assertFalse(oldName.equals(randomContextCategory.getName()));

        String oldDescription = randomContextCategory.getDescription();
        randomContextCategory.setDescription(randomPrint(50, 100).trim());
        assertFalse(oldDescription.equals(randomContextCategory.getDescription()));

        assertThrows(TimeoutException.class, () -> editContextCategoryPage.updateContextCategory(randomContextCategory));
    }

    @Test
    @DisplayName("TC_5_1_TA_9")
    public void developer_can_discard_context_categories_created_by_any_user_provided_that_there_is_no_context_scheme_referencing_it() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        ContextCategoryObject developerContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(developer);

        AppUserObject developerAdmin = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developerAdmin);
        ContextCategoryObject developerAdminContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(developerAdmin);

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        ContextCategoryObject endUserContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(endUser);

        AppUserObject endUserAdmin = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true);
        thisAccountWillBeDeletedAfterTests(endUserAdmin);
        ContextCategoryObject endUserAdminContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(endUserAdmin);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());

        for (ContextCategoryObject contextCategory : Arrays.asList(
                developerContextCategory, developerAdminContextCategory,
                endUserContextCategory, endUserAdminContextCategory)) {
            ContextMenu contextMenu = homePage.getContextMenu();
            ViewEditContextCategoryPage viewEditContextCategoryPage = contextMenu.openViewEditContextCategorySubMenu();

            EditContextCategoryPage editContextCategoryPage =
                    viewEditContextCategoryPage.openEditContextCategoryPageByContextCategoryName(contextCategory.getName());
            editContextCategoryPage.discardContextCategory();

            assertThrows(NoSuchElementException.class, () ->
                    contextMenu.openViewEditContextCategorySubMenu()
                            .openEditContextCategoryPageByContextCategoryName(contextCategory.getName()));
        }
    }

    @Test
    @DisplayName("TC_5_1_TA_10")
    public void developer_cannot_discard_context_category_that_has_context_scheme_referencing_it() {
        ContextCategoryObject randomContextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);
        ContextSchemeObject randomContextScheme = getAPIFactory().getContextSchemeAPI()
                .createRandomContextScheme(randomContextCategory, appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextCategoryPage viewEditContextCategoryPage = contextMenu.openViewEditContextCategorySubMenu();

        EditContextCategoryPage editContextCategoryPage =
                viewEditContextCategoryPage.openEditContextCategoryPageByContextCategoryName(randomContextCategory.getName());
        assertThrows(TimeoutException.class, () -> editContextCategoryPage.discardContextCategory());

        assertTrue(getDriver().findElement(
                        By.xpath("//*[contains(text(), \"context category cannot be deleted\")]"))
                .isDisplayed());
    }

    @Test
    @DisplayName("TC_5_1_TA_11 (Updater field)")
    public void test_search_feature_using_updater_field() {
        ContextCategoryObject randomContextCategory = ContextCategoryObject.newRandomContextCategory(appUser);
        getAPIFactory().getContextCategoryAPI().createContextCategory(randomContextCategory, appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu;
        ViewEditContextCategoryPage viewEditContextCategoryPage;

        // Test 'Updater' field
        homePage.openPage();
        contextMenu = homePage.getContextMenu();
        viewEditContextCategoryPage = contextMenu.openViewEditContextCategorySubMenu();
        viewEditContextCategoryPage.setUpdater(appUser.getLoginId());
        viewEditContextCategoryPage.hitSearchButton();
        assertContextCategoryNameInTheSearchResultsAtFirst(
                viewEditContextCategoryPage, randomContextCategory.getName());
    }

    @Test
    @DisplayName("TC_5_1_TA_11 (Update Start/End Date fields)")
    public void test_search_feature_using_date_fields() {
        ContextCategoryObject randomContextCategory = ContextCategoryObject.newRandomContextCategory(appUser);
        randomContextCategory.setCreationTimestamp(LocalDateTime.of(
                RandomUtils.nextInt(2000, 2011),
                RandomUtils.nextInt(1, 13),
                RandomUtils.nextInt(1, 29),
                RandomUtils.nextInt(0, 24),
                RandomUtils.nextInt(0, 60),
                RandomUtils.nextInt(0, 60)
        ));
        randomContextCategory.setLastUpdateTimestamp(LocalDateTime.of(
                RandomUtils.nextInt(2011, 2022),
                RandomUtils.nextInt(1, 13),
                RandomUtils.nextInt(1, 29),
                RandomUtils.nextInt(0, 24),
                RandomUtils.nextInt(0, 60),
                RandomUtils.nextInt(0, 60)
        ));
        getAPIFactory().getContextCategoryAPI().createContextCategory(randomContextCategory, appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu;
        ViewEditContextCategoryPage viewEditContextCategoryPage;

        // Test 'Update Start Date'/'Update End Date' field
        homePage.openPage();
        contextMenu = homePage.getContextMenu();
        viewEditContextCategoryPage = contextMenu.openViewEditContextCategorySubMenu();
        viewEditContextCategoryPage.setUpdatedStartDate(randomContextCategory.getCreationTimestamp());
        viewEditContextCategoryPage.setUpdatedEndDate(randomContextCategory.getLastUpdateTimestamp());
        viewEditContextCategoryPage.hitSearchButton();
        assertContextCategoryNameInTheSearchResultsAtFirst(
                viewEditContextCategoryPage, randomContextCategory.getName());
    }

    @Test
    @DisplayName("TC_5_1_TA_11 (Name field)")
    public void test_search_feature_using_name_field() {
        ContextCategoryObject randomContextCategory = ContextCategoryObject.newRandomContextCategory(appUser);
        getAPIFactory().getContextCategoryAPI().createContextCategory(randomContextCategory, appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu;
        ViewEditContextCategoryPage viewEditContextCategoryPage;

        // Test 'Name' field
        homePage.openPage();
        contextMenu = homePage.getContextMenu();
        viewEditContextCategoryPage = contextMenu.openViewEditContextCategorySubMenu();
        viewEditContextCategoryPage.setName(randomContextCategory.getName());
        viewEditContextCategoryPage.hitSearchButton();
        assertContextCategoryNameInTheSearchResultsAtFirst(
                viewEditContextCategoryPage, randomContextCategory.getName());
    }

    @Test
    @DisplayName("TC_5_1_TA_11 (Description field)")
    public void test_search_feature_using_description_field() {
        ContextCategoryObject randomContextCategory = ContextCategoryObject.newRandomContextCategory(appUser);
        getAPIFactory().getContextCategoryAPI().createContextCategory(randomContextCategory, appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu;
        ViewEditContextCategoryPage viewEditContextCategoryPage;

        // Test 'Description' field
        homePage.openPage();
        contextMenu = homePage.getContextMenu();
        viewEditContextCategoryPage = contextMenu.openViewEditContextCategorySubMenu();
        viewEditContextCategoryPage.setDescription(randomContextCategory.getDescription().substring(0, 10));
        viewEditContextCategoryPage.hitSearchButton();
        assertContextCategoryNameInTheSearchResultsAtFirst(
                viewEditContextCategoryPage, randomContextCategory.getName());
    }

    private void assertContextCategoryNameInTheSearchResultsAtFirst(ViewEditContextCategoryPage viewEditContextCategoryPage, String name) {
        retry(() -> {
            WebElement tr = viewEditContextCategoryPage.getTableRecordAtIndex(1);
            WebElement td = viewEditContextCategoryPage.getColumnByName(tr, "name");
            assertEquals(name, td.findElement(By.cssSelector("a > span")).getText());
        });
    }

    @Test
    @DisplayName("TC_5_1_TA_12")
    public void test_checkbox_selection() {
        String namePrefix = "cs_TC51_TA12";
        List<ContextCategoryObject> randomContextCategories = new ArrayList<>();
        for (int i = 0; i < RandomUtils.nextInt(11, 20); ++i) {
            ContextCategoryObject randomContextCategory =
                    getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser, namePrefix);
            randomContextCategories.add(randomContextCategory);
        }

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextCategoryPage viewEditContextCategoryPage = contextMenu.openViewEditContextCategorySubMenu();

        viewEditContextCategoryPage.setName(namePrefix);
        viewEditContextCategoryPage.hitSearchButton();

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

        viewEditContextCategoryPage.goToNextPage();
        viewEditContextCategoryPage.goToPreviousPage();

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
        this.randomAccounts.forEach(randomAccount -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId());
        });
    }

}
