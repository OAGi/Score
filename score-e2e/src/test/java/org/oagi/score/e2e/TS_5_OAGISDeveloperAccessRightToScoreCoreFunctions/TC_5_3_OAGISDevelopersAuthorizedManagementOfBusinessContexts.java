package org.oagi.score.e2e.TS_5_OAGISDeveloperAccessRightToScoreCoreFunctions;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.menu.ContextMenu;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.oagi.score.e2e.page.context.BusinessContextValueDialog;
import org.oagi.score.e2e.page.context.CreateBusinessContextPage;
import org.oagi.score.e2e.page.context.EditBusinessContextPage;
import org.oagi.score.e2e.page.context.ViewEditBusinessContextPage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertChecked;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_5_3_OAGISDevelopersAuthorizedManagementOfBusinessContexts extends BaseTest {

    private List<AppUserObject> randomAccounts = new ArrayList<>();

    private AppUserObject appUser;

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
    @DisplayName("TC_5_3_TA_1")
    public void developer_can_create_business_context_with_only_required_information() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditBusinessContextPage viewEditBusinessContextPage = contextMenu.openViewEditBusinessContextSubMenu();
        CreateBusinessContextPage createBusinessContextPage = viewEditBusinessContextPage.openCreateBusinessContextPage();

        BusinessContextObject randomBusinessContext = BusinessContextObject.createRandomBusinessContext(appUser);
        viewEditBusinessContextPage = createBusinessContextPage.createBusinessContext(randomBusinessContext);

        EditBusinessContextPage editBusinessContextPage = viewEditBusinessContextPage
                .openEditBusinessContextPageByBusinessContextName(randomBusinessContext.getName());
        assertEquals(randomBusinessContext.getName(), getText(editBusinessContextPage.getNameField()));
    }

    @Test
    @DisplayName("TC_5_3_TA_2")
    public void developer_can_remove_context_value_during_the_business_context_creation() {
        List<ContextCategoryObject> randomContextCategoryList = Arrays.asList(
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser),
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser),
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser)
        );
        List<ContextSchemeObject> randomContextSchemeList = Arrays.asList(
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategoryList.get(0), appUser),
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategoryList.get(1), appUser),
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategoryList.get(2), appUser)
        );
        List<ContextSchemeValueObject> randomContextSchemeValueList = Arrays.asList(
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextSchemeList.get(0)),
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextSchemeList.get(1)),
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextSchemeList.get(2))
        );

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditBusinessContextPage viewEditBusinessContextPage = contextMenu.openViewEditBusinessContextSubMenu();
        CreateBusinessContextPage createBusinessContextPage = viewEditBusinessContextPage.openCreateBusinessContextPage();

        BusinessContextObject randomBusinessContext = BusinessContextObject.createRandomBusinessContext(appUser);
        for (int i = 0, len = randomContextSchemeValueList.size(); i < len; ++i) {
            BusinessContextValueDialog businessContextValueDialog = createBusinessContextPage.openBusinessContextValueDialog();
            businessContextValueDialog.setContextCategory(randomContextCategoryList.get(i));
            businessContextValueDialog.setContextScheme(randomContextSchemeList.get(i));
            businessContextValueDialog.setContextSchemeValue(randomContextSchemeValueList.get(i));
            click(businessContextValueDialog.getAddButton());
        }

        createBusinessContextPage.removeBusinessContextValueByContextSchemeValue(randomContextSchemeValueList.get(0));
        createBusinessContextPage.removeBusinessContextValueByContextSchemeValue(randomContextSchemeValueList.get(2));

        viewEditBusinessContextPage = createBusinessContextPage.createBusinessContext(randomBusinessContext);

        EditBusinessContextPage editBusinessContextPage = viewEditBusinessContextPage.openEditBusinessContextPageByBusinessContextName(randomBusinessContext.getName());
        assertEquals(randomBusinessContext.getName(), getText(editBusinessContextPage.getNameField()));

        assertThrows(TimeoutException.class, () ->
                editBusinessContextPage.openBusinessContextValueDialogByContextSchemeValue(randomContextSchemeValueList.get(0)));
        assertThrows(TimeoutException.class, () ->
                editBusinessContextPage.openBusinessContextValueDialogByContextSchemeValue(randomContextSchemeValueList.get(2)));

        ContextCategoryObject contextCategory = randomContextCategoryList.get(1);
        ContextSchemeObject contextScheme = randomContextSchemeList.get(1);
        ContextSchemeValueObject contextSchemeValue = randomContextSchemeValueList.get(1);
        BusinessContextValueDialog businessContextValueDialog =
                editBusinessContextPage.openBusinessContextValueDialogByContextSchemeValue(contextSchemeValue);

        assertEquals(contextCategory.getName(), getText(businessContextValueDialog.getContextCategorySelectField()));
        assertEquals(contextCategory.getDescription(), getText(businessContextValueDialog.getContextCategoryDescriptionField()));

        assertEquals(contextScheme.getSchemeName(), getText(businessContextValueDialog.getContextSchemeSelectField()));
        assertEquals(contextScheme.getSchemeId(), getText(businessContextValueDialog.getContextSchemeIDField()));
        assertEquals(contextScheme.getSchemeAgencyId(), getText(businessContextValueDialog.getContextSchemeAgencyIDField()));
        assertEquals(contextScheme.getSchemeVersionId(), getText(businessContextValueDialog.getContextSchemeVersionField()));
        assertEquals(contextScheme.getDescription(), getText(businessContextValueDialog.getContextSchemeDescriptionField()));

        assertEquals(contextSchemeValue.getValue(), getText(businessContextValueDialog.getContextSchemeValueSelectField()));
        assertEquals(contextSchemeValue.getMeaning(), getText(businessContextValueDialog.getContextSchemeValueMeaningField()));

        businessContextValueDialog.close();
    }

    @Test
    @DisplayName("TC_5_3_TA_3")
    public void developer_can_create_business_context_with_all_information() {
        List<ContextCategoryObject> randomContextCategoryList = Arrays.asList(
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser),
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser),
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser)
        );
        List<ContextSchemeObject> randomContextSchemeList = Arrays.asList(
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategoryList.get(0), appUser),
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategoryList.get(1), appUser),
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategoryList.get(2), appUser)
        );
        List<ContextSchemeValueObject> randomContextSchemeValueList = Arrays.asList(
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextSchemeList.get(0)),
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextSchemeList.get(1)),
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextSchemeList.get(2))
        );

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditBusinessContextPage viewEditBusinessContextPage = contextMenu.openViewEditBusinessContextSubMenu();
        CreateBusinessContextPage createBusinessContextPage = viewEditBusinessContextPage.openCreateBusinessContextPage();

        BusinessContextObject randomBusinessContext = BusinessContextObject.createRandomBusinessContext(appUser);
        for (int i = 0, len = randomContextSchemeValueList.size(); i < len; ++i) {
            BusinessContextValueDialog businessContextValueDialog = createBusinessContextPage.openBusinessContextValueDialog();
            businessContextValueDialog.setContextCategory(randomContextCategoryList.get(i));
            businessContextValueDialog.setContextScheme(randomContextSchemeList.get(i));
            businessContextValueDialog.setContextSchemeValue(randomContextSchemeValueList.get(i));
            click(businessContextValueDialog.getAddButton());
        }
        viewEditBusinessContextPage = createBusinessContextPage.createBusinessContext(randomBusinessContext);

        EditBusinessContextPage editBusinessContextPage = viewEditBusinessContextPage.openEditBusinessContextPageByBusinessContextName(randomBusinessContext.getName());
        assertEquals(randomBusinessContext.getName(), getText(editBusinessContextPage.getNameField()));

        for (int i = 0, len = randomContextSchemeValueList.size(); i < len; ++i) {
            ContextCategoryObject contextCategory = randomContextCategoryList.get(i);
            ContextSchemeObject contextScheme = randomContextSchemeList.get(i);
            ContextSchemeValueObject contextSchemeValue = randomContextSchemeValueList.get(i);
            BusinessContextValueDialog businessContextValueDialog =
                    editBusinessContextPage.openBusinessContextValueDialogByContextSchemeValue(contextSchemeValue);

            assertEquals(contextCategory.getName(), getText(businessContextValueDialog.getContextCategorySelectField()));
            assertEquals(contextCategory.getDescription(), getText(businessContextValueDialog.getContextCategoryDescriptionField()));

            assertEquals(contextScheme.getSchemeName(), getText(businessContextValueDialog.getContextSchemeSelectField()));
            assertEquals(contextScheme.getSchemeId(), getText(businessContextValueDialog.getContextSchemeIDField()));
            assertEquals(contextScheme.getSchemeAgencyId(), getText(businessContextValueDialog.getContextSchemeAgencyIDField()));
            assertEquals(contextScheme.getSchemeVersionId(), getText(businessContextValueDialog.getContextSchemeVersionField()));
            assertEquals(contextScheme.getDescription(), getText(businessContextValueDialog.getContextSchemeDescriptionField()));

            assertEquals(contextSchemeValue.getValue(), getText(businessContextValueDialog.getContextSchemeValueSelectField()));
            assertEquals(contextSchemeValue.getMeaning(), getText(businessContextValueDialog.getContextSchemeValueMeaningField()));

            businessContextValueDialog.close();
        }
    }

    @Test
    @DisplayName("TC_5_3_TA_4")
    public void developer_cannot_create_business_context_with_missing_required_information() {
        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditBusinessContextPage viewEditBusinessContextPage = contextMenu.openViewEditBusinessContextSubMenu();
        CreateBusinessContextPage createBusinessContextPage = viewEditBusinessContextPage.openCreateBusinessContextPage();

        BusinessContextObject randomBusinessContext = BusinessContextObject.createRandomBusinessContext(appUser);
        randomBusinessContext.setName(null);
        assertThrows(TimeoutException.class, () -> createBusinessContextPage.createBusinessContext(randomBusinessContext));
    }

    @Test
    @DisplayName("TC_5_3_TA_5")
    public void developer_can_see_all_business_context_created_by_any_user() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        BusinessContextObject developerBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        AppUserObject developerAdmin = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developerAdmin);
        BusinessContextObject developerAdminBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developerAdmin);

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessContextObject endUserBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);

        AppUserObject endUserAdmin = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true);
        thisAccountWillBeDeletedAfterTests(endUserAdmin);
        BusinessContextObject endUserAdminBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUserAdmin);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditBusinessContextPage viewEditBusinessContextPage = contextMenu.openViewEditBusinessContextSubMenu();

        for (BusinessContextObject businessContext : Arrays.asList(
                developerBusinessContext, developerAdminBusinessContext,
                endUserBusinessContext, endUserAdminBusinessContext)) {
            EditBusinessContextPage editBusinessContextPage = viewEditBusinessContextPage.openEditBusinessContextPageByBusinessContextName(businessContext.getName());
            assertEquals(businessContext.getName(), getText(editBusinessContextPage.getNameField()));
            viewEditBusinessContextPage = contextMenu.openViewEditBusinessContextSubMenu();
        }
    }

    @Test
    @DisplayName("TC_5_3_TA_6_and_TA_7")
    public void developer_can_edit_business_context_created_by_any_user() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        BusinessContextObject developerBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        AppUserObject developerAdmin = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developerAdmin);
        BusinessContextObject developerAdminBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developerAdmin);

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessContextObject endUserBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);

        AppUserObject endUserAdmin = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true);
        thisAccountWillBeDeletedAfterTests(endUserAdmin);
        BusinessContextObject endUserAdminBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUserAdmin);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditBusinessContextPage viewEditBusinessContextPage = contextMenu.openViewEditBusinessContextSubMenu();

        for (BusinessContextObject businessContext : Arrays.asList(
                developerBusinessContext, developerAdminBusinessContext,
                endUserBusinessContext, endUserAdminBusinessContext)) {
            EditBusinessContextPage editBusinessContextPage = viewEditBusinessContextPage.openEditBusinessContextPageByBusinessContextName(businessContext.getName());

            String newName = "bc_" + randomAlphanumeric(5, 10);
            assertNotEquals(newName, businessContext.getName());
            editBusinessContextPage.setName(newName);
            click(editBusinessContextPage.getUpdateButton());

            viewEditBusinessContextPage = contextMenu.openViewEditBusinessContextSubMenu();
            viewEditBusinessContextPage.openEditBusinessContextPageByBusinessContextName(newName);
            assertEquals(newName, getText(editBusinessContextPage.getNameField()));
            viewEditBusinessContextPage = contextMenu.openViewEditBusinessContextSubMenu();
        }
    }

    @Test
    @DisplayName("TC_5_3_TA_8")
    public void developer_can_update_business_context_with_all_information_specified() {
        List<ContextCategoryObject> randomContextCategoryList = Arrays.asList(
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser),
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser),
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser)
        );
        List<ContextSchemeObject> randomContextSchemeList = Arrays.asList(
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategoryList.get(0), appUser),
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategoryList.get(1), appUser),
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(randomContextCategoryList.get(2), appUser)
        );
        List<ContextSchemeValueObject> randomContextSchemeValueList = Arrays.asList(
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextSchemeList.get(0)),
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextSchemeList.get(1)),
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextSchemeList.get(2))
        );

        BusinessContextObject randomBusinessContext = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(appUser);
        List<BusinessContextValueObject> randomBusinessContextValueList = new ArrayList<>();
        for (ContextSchemeValueObject randomContextSchemeValue : randomContextSchemeValueList) {
            BusinessContextValueObject randomBusinessContextValue =
                    getAPIFactory().getBusinessContextValueAPI().createRandomBusinessContextValue(randomBusinessContext, randomContextSchemeValue);
            randomBusinessContextValueList.add(randomBusinessContextValue);
        }

        List<ContextSchemeValueObject> expectedRandomContextSchemeValueList = Arrays.asList(
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextSchemeList.get(0)),
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextSchemeList.get(1)),
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(randomContextSchemeList.get(2))
        );

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditBusinessContextPage viewEditBusinessContextPage = contextMenu.openViewEditBusinessContextSubMenu();
        EditBusinessContextPage editBusinessContextPage = viewEditBusinessContextPage.openEditBusinessContextPageByBusinessContextName(randomBusinessContext.getName());

        String newName = "bc_" + randomAlphanumeric(5, 10);
        assertNotEquals(newName, randomBusinessContext.getName());
        editBusinessContextPage.setName(newName);

        for (BusinessContextValueObject businessContextValue : randomBusinessContextValueList) {
            editBusinessContextPage.removeBusinessContextValue(businessContextValue);
        }
        for (int i = 0, len = randomContextSchemeList.size(); i < len; ++i) {
            ContextCategoryObject contextCategory = randomContextCategoryList.get(i);
            ContextSchemeObject contextScheme = randomContextSchemeList.get(i);
            ContextSchemeValueObject contextSchemeValue = expectedRandomContextSchemeValueList.get(i);

            BusinessContextValueDialog businessContextValueDialog = editBusinessContextPage.openBusinessContextValueDialog();
            businessContextValueDialog.setContextCategory(contextCategory);
            businessContextValueDialog.setContextScheme(contextScheme);
            businessContextValueDialog.setContextSchemeValue(contextSchemeValue);
            click(businessContextValueDialog.getAddButton());
            businessContextValueDialog.close();
        }

        editBusinessContextPage.hitUpdateButton();

        viewEditBusinessContextPage = contextMenu.openViewEditBusinessContextSubMenu();
        editBusinessContextPage = viewEditBusinessContextPage.openEditBusinessContextPageByBusinessContextName(newName);
        assertEquals(newName, getText(editBusinessContextPage.getNameField()));

        for (int i = 0, len = expectedRandomContextSchemeValueList.size(); i < len; ++i) {
            ContextCategoryObject contextCategory = randomContextCategoryList.get(i);
            ContextSchemeObject contextScheme = randomContextSchemeList.get(i);
            ContextSchemeValueObject contextSchemeValue = expectedRandomContextSchemeValueList.get(i);
            BusinessContextValueDialog businessContextValueDialog =
                    editBusinessContextPage.openBusinessContextValueDialogByContextSchemeValue(contextSchemeValue);

            assertEquals(contextCategory.getName(), getText(businessContextValueDialog.getContextCategorySelectField()));
            assertEquals(contextCategory.getDescription(), getText(businessContextValueDialog.getContextCategoryDescriptionField()));

            assertEquals(contextScheme.getSchemeName(), getText(businessContextValueDialog.getContextSchemeSelectField()));
            assertEquals(contextScheme.getSchemeId(), getText(businessContextValueDialog.getContextSchemeIDField()));
            assertEquals(contextScheme.getSchemeAgencyId(), getText(businessContextValueDialog.getContextSchemeAgencyIDField()));
            assertEquals(contextScheme.getSchemeVersionId(), getText(businessContextValueDialog.getContextSchemeVersionField()));
            assertEquals(contextScheme.getDescription(), getText(businessContextValueDialog.getContextSchemeDescriptionField()));

            assertEquals(contextSchemeValue.getValue(), getText(businessContextValueDialog.getContextSchemeValueSelectField()));
            assertEquals(contextSchemeValue.getMeaning(), getText(businessContextValueDialog.getContextSchemeValueMeaningField()));

            businessContextValueDialog.close();
        }
    }

    @Test
    @DisplayName("TC_5_3_TA_9")
    public void developer_cannot_add_duplicate_context_value() {
        ContextCategoryObject contextCategory =
                getAPIFactory().getContextCategoryAPI().createRandomContextCategory(appUser);
        ContextSchemeObject contextScheme =
                getAPIFactory().getContextSchemeAPI().createRandomContextScheme(contextCategory, appUser);
        ContextSchemeValueObject contextSchemeValue =
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(contextScheme);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditBusinessContextPage viewEditBusinessContextPage = contextMenu.openViewEditBusinessContextSubMenu();
        CreateBusinessContextPage createBusinessContextPage = viewEditBusinessContextPage.openCreateBusinessContextPage();

        BusinessContextValueDialog businessContextValueDialog = createBusinessContextPage.openBusinessContextValueDialog();
        businessContextValueDialog.setContextCategory(contextCategory);
        businessContextValueDialog.setContextScheme(contextScheme);
        businessContextValueDialog.setContextSchemeValue(contextSchemeValue);
        click(businessContextValueDialog.getAddButton());

        businessContextValueDialog = createBusinessContextPage.openBusinessContextValueDialog();
        businessContextValueDialog.setContextCategory(contextCategory);
        businessContextValueDialog.setContextScheme(contextScheme);
        businessContextValueDialog.setContextSchemeValue(contextSchemeValue);
        click(businessContextValueDialog.getAddButton());

        assertEquals(contextSchemeValue.getValue() + " already exist", getSnackBarMessage(getDriver()));
    }

    @Test
    @DisplayName("TC_5_3_TA_10")
    public void developer_cannot_update_business_context_with_missing_required_information() {
        BusinessContextObject randomBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditBusinessContextPage viewEditBusinessContextPage = contextMenu.openViewEditBusinessContextSubMenu();
        EditBusinessContextPage editBusinessContextPage = viewEditBusinessContextPage.openEditBusinessContextPageByBusinessContextName(randomBusinessContext.getName());

        editBusinessContextPage.setName(null);
        assertThrows(TimeoutException.class, () -> editBusinessContextPage.hitUpdateButton());
    }

    @Test
    @DisplayName("TC_5_3_TA_11")
    public void developer_can_discard_business_context_created_by_any_user() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        BusinessContextObject developerBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);

        AppUserObject developerAdmin = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developerAdmin);
        BusinessContextObject developerAdminBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developerAdmin);

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessContextObject endUserBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);

        AppUserObject endUserAdmin = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true);
        thisAccountWillBeDeletedAfterTests(endUserAdmin);
        BusinessContextObject endUserAdminBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUserAdmin);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditBusinessContextPage viewEditBusinessContextPage = contextMenu.openViewEditBusinessContextSubMenu();

        for (BusinessContextObject businessContext : Arrays.asList(
                developerBusinessContext, developerAdminBusinessContext,
                endUserBusinessContext, endUserAdminBusinessContext)) {
            viewEditBusinessContextPage.discardBusinessContext(businessContext);
            assertThrows(NoSuchElementException.class, () -> viewEditBusinessContextPage.openEditBusinessContextPageByBusinessContextName(businessContext.getName()));
        }
    }

    @Test
    @DisplayName("TC_5_3_TA_12")
    public void developer_cannot_discard_business_context_referenced_by_BIE() {
        BusinessContextObject businessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(appUser);
        ASCCPObject asccp =
                getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum("Item Master. Item Master", "10.8.5");
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                Arrays.asList(businessContext), asccp, appUser, "WIP");

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditBusinessContextPage viewEditBusinessContextPage = contextMenu.openViewEditBusinessContextSubMenu();
        assertThrows(TimeoutException.class, () -> viewEditBusinessContextPage.discardBusinessContext(businessContext));
    }

    @Test
    @DisplayName("TC_5_3_TA_13")
    public void developer_can_update_BIE_referenced_business_context_created_by_any_user() {
        ASCCPObject asccp =
                getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum("Item Master. Item Master", "10.8.5");

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        BusinessContextObject developerBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        TopLevelASBIEPObject developerTopLevelASBIEP =
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(developerBusinessContext), asccp, developer, "WIP");

        AppUserObject developerAdmin = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
        thisAccountWillBeDeletedAfterTests(developerAdmin);
        BusinessContextObject developerAdminBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developerAdmin);
        TopLevelASBIEPObject developerAdminTopLevelASBIEP =
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(developerAdminBusinessContext), asccp, developerAdmin, "WIP");

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        BusinessContextObject endUserBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        TopLevelASBIEPObject endUserTopLevelASBIEP =
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(endUserBusinessContext), asccp, endUser, "WIP");

        AppUserObject endUserAdmin = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true);
        thisAccountWillBeDeletedAfterTests(endUserAdmin);
        BusinessContextObject endUserAdminBusinessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUserAdmin);
        TopLevelASBIEPObject endUserAdminTopLevelASBIEP =
                getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(
                        Arrays.asList(endUserAdminBusinessContext), asccp, endUserAdmin, "WIP");

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        for (BusinessContextObject businessContext : Arrays.asList(
                developerBusinessContext, developerAdminBusinessContext,
                endUserBusinessContext, endUserAdminBusinessContext)) {
            EditBusinessContextPage editBusinessContextPage = contextMenu.openViewEditBusinessContextSubMenu()
                    .openEditBusinessContextPageByBusinessContextName(businessContext.getName());
            String newName = "bc_" + randomAlphanumeric(5, 10);
            assertNotEquals(newName, businessContext.getName());
            businessContext.setName(newName);
            editBusinessContextPage.setName(newName);
            editBusinessContextPage.hitUpdateButton();

            ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
            viewEditBIEPage.setBusinessContext(newName);
            viewEditBIEPage.hitSearchButton();

            WebElement tr = viewEditBIEPage.getTableRecordAtIndex(1);
            WebElement td_bc = viewEditBIEPage.getColumnByName(tr, "businessContexts");
            assertEquals(newName, getText(td_bc.findElement(By.xpath("mat-chip-list//mat-chip"))));
        }
    }

    @Test
    @DisplayName("TC_5_3_TA_14 (Name field)")
    public void test_search_feature_using_name_field() {
        BusinessContextObject businessContext =
                getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(appUser);

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu;
        ViewEditBusinessContextPage viewEditBusinessContextPage;

        // Test 'Name' field
        contextMenu = homePage.getContextMenu();
        viewEditBusinessContextPage = contextMenu.openViewEditBusinessContextSubMenu();
        viewEditBusinessContextPage.setName(businessContext.getName());
        viewEditBusinessContextPage.hitSearchButton();
        assertBusinessContextNameInTheSearchResultsAtFirst(
                viewEditBusinessContextPage, businessContext.getName());
    }

    private void assertBusinessContextNameInTheSearchResultsAtFirst(ViewEditBusinessContextPage viewEditBusinessContextPage, String name) {
        retry(() -> {
            WebElement tr = viewEditBusinessContextPage.getTableRecordAtIndex(1);
            WebElement td = viewEditBusinessContextPage.getColumnByName(tr, "name");
            assertEquals(name, td.findElement(By.cssSelector("a > span")).getText());
        });
    }

    @Test
    @DisplayName("TC_5_3_TA_15")
    public void test_checkbox_selection() {
        String namePrefix = "bc_TC53_TA15";
        List<BusinessContextObject> randomBusinessContexts = new ArrayList<>();
        for (int i = 0; i < RandomUtils.nextInt(11, 20); ++i) {
            BusinessContextObject randomBusinessContext =
                    getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(appUser, namePrefix);
            randomBusinessContexts.add(randomBusinessContext);
        }

        HomePage homePage = loginPage().signIn(appUser.getLoginId(), appUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditBusinessContextPage viewEditBusinessContextPage = contextMenu.openViewEditBusinessContextSubMenu();

        viewEditBusinessContextPage.setName(namePrefix);
        viewEditBusinessContextPage.hitSearchButton();

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

        viewEditBusinessContextPage.goToNextPage();
        viewEditBusinessContextPage.goToPreviousPage();

        retry(() -> {
            WebElement checkboxOfFirstRecord = new FluentWait<>(getDriver())
                    .withTimeout(Duration.ofSeconds(3L))
                    .pollingEvery(Duration.ofMillis(100L))
                    .until(ExpectedConditions.elementToBeClickable(checkboxOfFirstRecordLocator));
            assertChecked(checkboxOfFirstRecord);
        });
    }

    @Test
    @DisplayName("TC_5_3_TA_16")
    @Disabled("Tested by TC_5_3_TA_3")
    public void test_business_context_values_are_listed() {
        developer_can_create_business_context_with_all_information();
    }

    @Test
    @DisplayName("TC_5_3_TA_17")
    @Disabled("Tested by TC_5_3_TA_3")
    public void test_all_details_of_business_context_are_displayed() {
        developer_can_create_business_context_with_all_information();
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
