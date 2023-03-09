package org.oagi.score.e2e.TS_6_EndUserAccessRightScoreCoreFunctions;

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
import org.oagi.score.e2e.page.context.ContextSchemeValueDialog;
import org.oagi.score.e2e.page.context.EditContextSchemePage;
import org.oagi.score.e2e.page.context.LoadFromCodeListDialog;
import org.oagi.score.e2e.page.context.ViewEditContextSchemePage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertChecked;
import static org.oagi.score.e2e.AssertionHelper.assertEnabled;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_6_1_EndUserAuthorizedManagementContextSchemes extends BaseTest {

    private List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }
    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_6_1_TA_1")
    public void test_TA_1() {
        List<ContextSchemeObject> schemeForTesting = new ArrayList<>();
        {
            /**
             * Create Context Scheme for developer account with no admin role.
             */
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            ContextCategoryObject categoryDeveloper = getAPIFactory().getContextCategoryAPI().createRandomContextCategory(developer);
            ContextSchemeObject developerScheme = getAPIFactory().getContextSchemeAPI().createRandomContextScheme(categoryDeveloper, developer);
            getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(developerScheme);
            schemeForTesting.add(developerScheme);
            /**
             * Create Context Scheme for developer account with admin role.
             */
            AppUserObject developerAdmin = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
            thisAccountWillBeDeletedAfterTests(developerAdmin);
            ContextCategoryObject categoryDevAdmin = getAPIFactory().getContextCategoryAPI().createRandomContextCategory(developerAdmin);
            ContextSchemeObject developerAdminScheme = getAPIFactory().getContextSchemeAPI().createRandomContextScheme(categoryDevAdmin, developerAdmin);
            getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(developerAdminScheme);
            schemeForTesting.add(developerAdminScheme);
            /**
             * Create Context Scheme for EU account.
             */
            AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);
            ContextCategoryObject categoryEU = getAPIFactory().getContextCategoryAPI().createRandomContextCategory(endUser);
            ContextSchemeObject endUserScheme = getAPIFactory().getContextSchemeAPI().createRandomContextScheme(categoryEU, endUser);
            getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(endUserScheme);
            schemeForTesting.add(endUserScheme);
            /**
             * Create Context Scheme for EU account with admin role.
             */
            AppUserObject endUserAdmin = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true);
            thisAccountWillBeDeletedAfterTests(endUserAdmin);
            ContextCategoryObject categoryEUAdmin = getAPIFactory().getContextCategoryAPI().createRandomContextCategory(endUserAdmin);
            ContextSchemeObject endUserAdminScheme = getAPIFactory().getContextSchemeAPI().createRandomContextScheme(categoryEUAdmin, endUserAdmin);
            getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(endUserAdminScheme);
            schemeForTesting.add(endUserAdminScheme);
        }

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        for (ContextSchemeObject contextScheme : schemeForTesting) {
            viewEditContextSchemePage.setName(contextScheme.getSchemeName());

            retry(() -> {
                viewEditContextSchemePage.hitSearchButton();

                WebElement tr = viewEditContextSchemePage.getTableRecordByValue(contextScheme.getSchemeName());
                WebElement td = viewEditContextSchemePage.getColumnByName(tr, "schemeName");
                assertEquals(contextScheme.getSchemeName(), getText(td.findElement(By.cssSelector("a > span"))));
            });
        }
    }

    @Test
    @DisplayName("TC_6_1_TA_2")
    public void test_TA_2() {
        List<ContextSchemeObject> schemeForTesting = new ArrayList<>();
        Map<String, ContextSchemeValueObject> contextSchemeValueMap = new HashMap<>();
        {
            /**
             * Create Context Scheme for developer account with no admin role.
             */
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            ContextCategoryObject categoryDeveloper = getAPIFactory().getContextCategoryAPI().createRandomContextCategory(developer);
            ContextSchemeObject developerScheme = getAPIFactory().getContextSchemeAPI().createRandomContextScheme(categoryDeveloper, developer);
            contextSchemeValueMap.put(developerScheme.getSchemeName(),
                    getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(developerScheme));
            schemeForTesting.add(developerScheme);
            /**
             * Create Context Scheme for developer account with admin role.
             */
            AppUserObject developerAdmin = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
            thisAccountWillBeDeletedAfterTests(developerAdmin);
            ContextCategoryObject categoryDevAdmin = getAPIFactory().getContextCategoryAPI().createRandomContextCategory(developerAdmin);
            ContextSchemeObject developerAdminScheme = getAPIFactory().getContextSchemeAPI().createRandomContextScheme(categoryDevAdmin, developerAdmin);
            contextSchemeValueMap.put(developerAdminScheme.getSchemeName(),
                    getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(developerAdminScheme));
            schemeForTesting.add(developerAdminScheme);
            /**
             * Create Context Scheme for EU account.
             */
            AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);
            ContextCategoryObject categoryEU = getAPIFactory().getContextCategoryAPI().createRandomContextCategory(endUser);
            ContextSchemeObject endUserScheme = getAPIFactory().getContextSchemeAPI().createRandomContextScheme(categoryEU, endUser);
            contextSchemeValueMap.put(endUserScheme.getSchemeName(),
                    getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(endUserScheme));
            schemeForTesting.add(endUserScheme);
            /**
             * Create Context Scheme for EU account with admin role.
             */
            AppUserObject endUserAdmin = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true);
            thisAccountWillBeDeletedAfterTests(endUserAdmin);
            ContextCategoryObject categoryEUAdmin = getAPIFactory().getContextCategoryAPI().createRandomContextCategory(endUserAdmin);
            ContextSchemeObject endUserAdminScheme = getAPIFactory().getContextSchemeAPI().createRandomContextScheme(categoryEUAdmin, endUserAdmin);
            contextSchemeValueMap.put(endUserAdminScheme.getSchemeName(),
                    getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(endUserAdminScheme));
            schemeForTesting.add(endUserAdminScheme);
        }

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        for (ContextSchemeObject contextScheme : schemeForTesting) {
            ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
            viewEditContextSchemePage.setName(contextScheme.getSchemeName());
            viewEditContextSchemePage.hitSearchButton();
            EditContextSchemePage editContextSchemePage = viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(contextScheme.getSchemeName());
            assertEnabled(editContextSchemePage.getVersionField());
            assertEnabled(editContextSchemePage.getDescriptionField());

            ContextSchemeValueObject contextSchemeValue = contextSchemeValueMap.get(contextScheme.getSchemeName());
            ContextSchemeValueDialog contextSchemeValueDialog = editContextSchemePage.openContextSchemeValueDialog(contextSchemeValue);
            assertEnabled(contextSchemeValueDialog.getMeaningField());
            assertEnabled(contextSchemeValueDialog.getValueField());
            escape(getDriver());
        }
    }

    @Test
    @DisplayName("TC_6_1_TA_3")
    public void test_TA_3() {
        ContextSchemeObject endUserScheme;
        Map<ContextSchemeObject, ContextSchemeValueObject> contextSchemeValueMap = new HashMap<>();
        Map<CodeListObject, CodeListValueObject> codeListValueMap = new HashMap<>();
        List<CodeListObject> codeListsForTesting = new ArrayList<>();
        CodeListObject codeListWorkingBranch;
        Map<CodeListObject, ReleaseObject> codeListReleaseMap = new HashMap<>();
        {
            /**
             * Create Code List for the latest and older release
             */
            AppUserObject developerUserForCodeList = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerUserForCodeList);
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developerUserForCodeList);
            ReleaseObject latestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");
            CodeListObject codeListLatestRelease = getAPIFactory().getCodeListAPI().createRandomCodeList(developerUserForCodeList, namespace, latestRelease, "Published");
            codeListValueMap.put(codeListLatestRelease, getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListLatestRelease, developerUserForCodeList));
            codeListsForTesting.add(codeListLatestRelease);
            codeListReleaseMap.put(codeListLatestRelease, latestRelease);
            ReleaseObject olderRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
            CodeListObject codeListOlderRelease = getAPIFactory().getCodeListAPI().createRandomCodeList(developerUserForCodeList, namespace, olderRelease, "Published");
            codeListValueMap.put(codeListOlderRelease, getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListOlderRelease, developerUserForCodeList));
            codeListsForTesting.add(codeListOlderRelease);
            codeListReleaseMap.put(codeListOlderRelease, olderRelease);
            /**
             * Create Code List for Working branch. States - WIP, Draft and Candidate
             */
            ReleaseObject workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            codeListWorkingBranch = getAPIFactory().getCodeListAPI().createRandomCodeList(developerUserForCodeList, namespace, workingBranch, "WIP");
            codeListValueMap.put(codeListWorkingBranch, getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListLatestRelease, developerUserForCodeList));
            codeListReleaseMap.put(codeListWorkingBranch, workingBranch);

            /**
             * Create Context Scheme for EU account.
             */
            AppUserObject endUserForContextScheme = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserForContextScheme);
            ContextCategoryObject categoryEU = getAPIFactory().getContextCategoryAPI().createRandomContextCategory(endUserForContextScheme);
            endUserScheme = getAPIFactory().getContextSchemeAPI().createRandomContextScheme(categoryEU, endUserForContextScheme);
            ContextSchemeValueObject schemeValue = getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(endUserScheme);
            contextSchemeValueMap.put(endUserScheme, schemeValue);
        }

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        EditContextSchemePage editContextSchemePage = viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(endUserScheme.getSchemeName());

        for (CodeListObject codeList : codeListsForTesting) {
            // Assert the message is displayed.
            click(editContextSchemePage.getLoadFromCodeListButton());
            assertEquals("All existing values will be removed and replaced with values from the code list.",
                    editContextSchemePage.getConfirmationDialogMessage());
            LoadFromCodeListDialog loadFromCodeListDialog = editContextSchemePage.continuToLoadFromCodeListDialog();
            ReleaseObject release = codeListReleaseMap.get(codeList);
            loadFromCodeListDialog.selectCodeListByCodeListNameAndBranch(codeList.getName(), release.getReleaseNumber());

            // Assert initial Context Scheme values are not present.
            ContextSchemeValueObject contextSchemeValue = contextSchemeValueMap.get(endUserScheme);
            assertThrows(NoSuchElementException.class, () -> {
                editContextSchemePage.openContextSchemeValueDialog(contextSchemeValue);
            });

            //Assert values from loaded Code List are present.
            CodeListValueObject codeListValue = codeListValueMap.get(codeList);
            assertDoesNotThrow(() -> {
                editContextSchemePage.openContextSchemeValueDialogByValue(codeListValue.getValue());
            });
            escape(getDriver());
        }

        // The EU cannot add values from a developer code list which is in the Working branch.
        editContextSchemePage.getLoadFromCodeListButton().click();
        LoadFromCodeListDialog loadFromCodeListDialog = editContextSchemePage.continuToLoadFromCodeListDialog();
        ReleaseObject release = codeListReleaseMap.get(codeListWorkingBranch);
        // Assert Working branch is not present.
        assertThrows(NoSuchElementException.class, () -> {
            loadFromCodeListDialog.selectCodeListByCodeListNameAndBranch(codeListWorkingBranch.getName(), release.getReleaseNumber());
        });
    }

    @Test
    @DisplayName("TC_6_1_TA_4")
    public void test_TA_4() {
        ContextSchemeObject endUserScheme;
        Map<ContextSchemeObject, ContextSchemeValueObject> contextSchemeValueMap = new HashMap<>();
        Map<CodeListObject, CodeListValueObject> codeListValueMap = new HashMap<>();
        List<CodeListObject> codeListsForTesting = new ArrayList<>();
        CodeListObject codeListWorkingBranch;
        Map<CodeListObject, ReleaseObject> codeListReleaseMap = new HashMap<>();
        {
            /**
             * Create Code List in the WIP, QA and Production state
             */
            AppUserObject developerUserForCodeList = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerUserForCodeList);
            NamespaceObject namespaceDev = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developerUserForCodeList);
            ReleaseObject latestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");
            ReleaseObject olderRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");

            CodeListObject codeListWIP = getAPIFactory().getCodeListAPI().createRandomCodeList(developerUserForCodeList, namespaceDev, latestRelease, "WIP");
            codeListValueMap.put(codeListWIP, getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListWIP, developerUserForCodeList));
            codeListsForTesting.add(codeListWIP);
            codeListReleaseMap.put(codeListWIP, latestRelease);

            CodeListObject codeListQA = getAPIFactory().getCodeListAPI().createRandomCodeList(developerUserForCodeList, namespaceDev, latestRelease, "QA");
            codeListValueMap.put(codeListQA, getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListQA, developerUserForCodeList));
            codeListsForTesting.add(codeListQA);
            codeListReleaseMap.put(codeListQA, latestRelease);

            CodeListObject codeListProductionLatestRelease = getAPIFactory().getCodeListAPI().createRandomCodeList(developerUserForCodeList, namespaceDev, latestRelease, "Production");
            CodeListValueObject value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListProductionLatestRelease, developerUserForCodeList);
            codeListValueMap.put(codeListProductionLatestRelease, value);
            codeListsForTesting.add(codeListProductionLatestRelease);
            codeListReleaseMap.put(codeListProductionLatestRelease, latestRelease);

            AppUserObject endUserForCodeList = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserForCodeList);
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserForCodeList);
            CodeListObject codeListDerivedFromDevListLatestRelease = getAPIFactory().getCodeListAPI().createDerivedCodeList(codeListProductionLatestRelease, endUserForCodeList, namespaceEU, latestRelease, "Production");
            codeListValueMap.put(codeListDerivedFromDevListLatestRelease, value);
            codeListsForTesting.add(codeListDerivedFromDevListLatestRelease);
            codeListReleaseMap.put(codeListDerivedFromDevListLatestRelease, latestRelease);

            CodeListObject codeListProductionOlderRelease = getAPIFactory().getCodeListAPI().createRandomCodeList(developerUserForCodeList, namespaceDev, olderRelease, "Production");
            value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListProductionOlderRelease, developerUserForCodeList);
            codeListValueMap.put(codeListProductionOlderRelease, value);
            codeListsForTesting.add(codeListProductionOlderRelease);
            codeListReleaseMap.put(codeListProductionOlderRelease, olderRelease);

            CodeListObject codeListDerivedFromDevListOlderRelease = getAPIFactory().getCodeListAPI().createDerivedCodeList(codeListProductionOlderRelease, endUserForCodeList, namespaceEU, olderRelease, "Production");
            codeListValueMap.put(codeListDerivedFromDevListOlderRelease, value);
            codeListsForTesting.add(codeListDerivedFromDevListOlderRelease);
            codeListReleaseMap.put(codeListDerivedFromDevListOlderRelease, olderRelease);

            /**
             * Create Context Scheme for EU account.
             */
            AppUserObject endUserForContextScheme = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserForContextScheme);
            ContextCategoryObject categoryEU = getAPIFactory().getContextCategoryAPI().createRandomContextCategory(endUserForContextScheme);
            endUserScheme = getAPIFactory().getContextSchemeAPI().createRandomContextScheme(categoryEU, endUserForContextScheme);
            ContextSchemeValueObject schemeValue = getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(endUserScheme);
            contextSchemeValueMap.put(endUserScheme, schemeValue);
        }

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();
        EditContextSchemePage editContextSchemePage = viewEditContextSchemePage.openEditContextSchemePageByContextSchemeName(endUserScheme.getSchemeName());

        for (CodeListObject codeList : codeListsForTesting) {
            editContextSchemePage.getLoadFromCodeListButton().click();
            LoadFromCodeListDialog loadFromCodeListDialog = editContextSchemePage.continuToLoadFromCodeListDialog();
            ReleaseObject release = codeListReleaseMap.get(codeList);
            if (codeList.getState().equals("Production")) {
                if (codeList.getBasedCodeListManifestId() != null) {
                    //Assert values from loaded Production Code List (that is  derived) are present.
                    loadFromCodeListDialog.selectCodeListByCodeListNameAndBranch(codeList.getName(), release.getReleaseNumber());
                    CodeListValueObject codeListValue = codeListValueMap.get(codeList);
                    assertDoesNotThrow(() -> {
                        editContextSchemePage.openContextSchemeValueDialogByValue(codeListValue.getValue());
                    });
                    escape(getDriver());
                } else {
                    // Assert values from loaded Production Code List (that is not derived) are present.
                    loadFromCodeListDialog.selectCodeListByCodeListNameAndBranch(codeList.getName(), release.getReleaseNumber());
                    CodeListValueObject codeListValue = codeListValueMap.get(codeList);
                    assertDoesNotThrow(() -> {
                        editContextSchemePage.openContextSchemeValueDialogByValue(codeListValue.getValue());
                    });
                    escape(getDriver());
                }
            } else {
                // Assert WIP and QA Code Lists are not present in the table.
                assertThrows(NoSuchElementException.class, () -> {
                    loadFromCodeListDialog.selectCodeListByCodeListNameAndBranch(codeList.getName(), release.getReleaseNumber());
                });
                escape(getDriver());
            }
        }
    }

    @Test
    @DisplayName("TC_6_1_TA_5")
    public void test_TA_5() {
        ContextSchemeObject endUserScheme;
        {
            /**
             * Create Context Scheme for EU account.
             */
            for (int i = 0; i < 12; i++) {
                AppUserObject endUserForContextScheme = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
                thisAccountWillBeDeletedAfterTests(endUserForContextScheme);
                ContextCategoryObject categoryEU = getAPIFactory().getContextCategoryAPI().createRandomContextCategory(endUserForContextScheme);
                endUserScheme = getAPIFactory().getContextSchemeAPI().createRandomContextScheme(categoryEU, endUserForContextScheme);
                getAPIFactory().getContextSchemeValueAPI().createRandomContextSchemeValue(endUserScheme);
            }
        }

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ContextMenu contextMenu = homePage.getContextMenu();
        ViewEditContextSchemePage viewEditContextSchemePage = contextMenu.openViewEditContextSchemeSubMenu();

        By checkboxOfFirstRecordLocator = By.xpath("//table/tbody" +
                "/tr[" + RandomUtils.nextInt(1, 10) + "]/td[1]//mat-checkbox[@ng-reflect-disabled=\"true\" or not(@disabled='true')]//input");
        retry(() -> {
            WebElement checkboxOfFirstRecord = new FluentWait<>(getDriver())
                    .withTimeout(ofSeconds(3L))
                    .pollingEvery(ofMillis(100L))
                    .until(ExpectedConditions.elementToBeClickable(checkboxOfFirstRecordLocator));

            // Click the checkbox
            new Actions(getDriver()).moveToElement(checkboxOfFirstRecord).perform();
            checkboxOfFirstRecord.sendKeys(Keys.SPACE);
        });

        viewEditContextSchemePage.goToNextPage();
        viewEditContextSchemePage.goToPreviousPage();

        retry(() -> {
            WebElement checkboxOfFirstRecord = new FluentWait<>(getDriver())
                    .withTimeout(ofSeconds(3L))
                    .pollingEvery(ofMillis(100L))
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
