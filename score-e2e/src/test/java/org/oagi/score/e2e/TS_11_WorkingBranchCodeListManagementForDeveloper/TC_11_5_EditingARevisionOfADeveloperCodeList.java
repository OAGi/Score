package org.oagi.score.e2e.TS_11_WorkingBranchCodeListManagementForDeveloper;

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
import org.oagi.score.e2e.page.code_list.EditCodeListValueDialog;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.openqa.selenium.TimeoutException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.AssertionHelper.assertEnabled;
import static org.oagi.score.e2e.impl.PageHelper.getSnackBarMessage;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_11_5_EditingARevisionOfADeveloperCodeList extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_11_5_TA_1")
    public void test_TA_1() {
        AppUserObject developerA;
        ReleaseObject workingBranch;
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<CodeListObject, CodeListValueObject> codeListCodeListValueMap = new HashMap<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerB, namespace, workingBranch, "Published");
            CodeListValueObject codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerB);
            codeListCodeListValueMap.put(codeList, codeListValue);
            codeListForTesting.add(codeList);

            codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Published");
            codeList.setDeprecated(true);
            getAPIFactory().getCodeListAPI().updateCodeList(codeList);
            codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerA);
            codeListCodeListValueMap.put(codeList, codeListValue);
            codeListForTesting.add(codeList);
        }
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (CodeListObject codeList : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), workingBranch.getReleaseNumber());
            editCodeListPage.hitRevise();
            assertEquals("Working", getText(editCodeListPage.getReleaseField()));
            assertTrue(Integer.valueOf(getText(editCodeListPage.getRevisionField())) > 1);
            assertEquals("WIP", getText(editCodeListPage.getStateField()));
            boolean previousDeprecatedStatus = codeList.isDeprecated();
            if (previousDeprecatedStatus == true) {
                assertEnabled(editCodeListPage.getDeprecatedSelectField());
            } else {
                assertEnabled(editCodeListPage.getDeprecatedSelectField());
            }
            assertDisabled(editCodeListPage.getCodeListNameField());
            assertDisabled(editCodeListPage.getListIDField());
            assertDisabled(editCodeListPage.getAgencyIDListField());
            assertDisabled(editCodeListPage.getNamespaceSelectField());

            assertEnabled(editCodeListPage.getVersionField());
            editCodeListPage.setVersion("new version");
            assertEnabled(editCodeListPage.getDefinitionField());
            editCodeListPage.setDefinition("new definition");
            assertEnabled(editCodeListPage.getDefinitionSourceField());
            editCodeListPage.setDefinitionSource("new definition source");
            editCodeListPage.hitUpdateButton();
            String agencyIDList = getText(editCodeListPage.getAgencyIDListField());
            assertTrue(getAPIFactory().getCodeListAPI().checkCodeListUniqueness(codeList, agencyIDList));
        }
    }

    @Test
    @DisplayName("TC_11_5_TA_2")
    public void test_TA_2() {
        AppUserObject developerA;
        ReleaseObject workingBranch;
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<CodeListObject, ArrayList<CodeListValueObject>> codeListCodeListValueMap = new HashMap<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ArrayList<CodeListValueObject> values = new ArrayList<>();
            CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerB, namespace, workingBranch, "Published");
            CodeListValueObject codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerB);
            values.add(codeListValue);
            codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerB);
            codeListValue.setDeprecated(true);
            getAPIFactory().getCodeListValueAPI().updateCodeListValue(codeListValue);
            values.add(codeListValue);
            codeListCodeListValueMap.put(codeList, values);
            codeListForTesting.add(codeList);

        }
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (CodeListObject codeList : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), workingBranch.getReleaseNumber());
            editCodeListPage.hitRevise();
            assertEquals("Working", getText(editCodeListPage.getReleaseField()));
            assertTrue(Integer.valueOf(getText(editCodeListPage.getRevisionField())) > 1);
            assertEquals("WIP", getText(editCodeListPage.getStateField()));
            ArrayList<CodeListValueObject> values = codeListCodeListValueMap.get(codeList);
            for (CodeListValueObject value : values) {
                editCodeListPage.selectCodeListValue(value.getValue());
                assertThrows(Exception.class, () -> {
                    editCodeListPage.removeCodeListValue();
                });
                EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.editCodeListValue(value.getValue());
                editCodeListValueDialog.setMeaning("new meaning for value");
                editCodeListValueDialog.setDefinition("new definition for value");
                editCodeListValueDialog.setDefinitionSource("new definition source for value");
                boolean previousDeprecatedStatusForValue = value.isDeprecated();
                if (previousDeprecatedStatusForValue == true) {
                    assertEnabled(editCodeListValueDialog.getDeprecatedSelectField());
                } else {
                    assertEnabled(editCodeListValueDialog.getDeprecatedSelectField());
                }
                editCodeListValueDialog.hitSaveButton();
            }
        }

    }

    @Test
    @DisplayName("TC_11_5_TA_3")
    public void test_TA_3() {
        AppUserObject developerA;
        ReleaseObject workingBranch;
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<CodeListObject, CodeListValueObject> codeListCodeListValueMap = new HashMap<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerB, namespace, workingBranch, "Published");
            CodeListValueObject codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerB);
            codeListCodeListValueMap.put(codeList, codeListValue);
            codeListForTesting.add(codeList);
        }
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (CodeListObject codeList : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), workingBranch.getReleaseNumber());
            editCodeListPage.hitRevise();
            assertEquals("Working", getText(editCodeListPage.getReleaseField()));
            assertTrue(Integer.valueOf(getText(editCodeListPage.getRevisionField())) > 1);
            assertEquals("WIP", getText(editCodeListPage.getStateField()));
            EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
            String newValueCode = "new value code";
            editCodeListValueDialog.setCode(newValueCode);
            editCodeListValueDialog.setMeaning("new value meaning");
            editCodeListValueDialog.hitAddButton();
            editCodeListValueDialog = editCodeListPage.editCodeListValue(newValueCode);
            editCodeListValueDialog.setMeaning("changed meaning");
            editCodeListValueDialog.setDefinition("added definition");
            editCodeListValueDialog.setDefinitionSource("added definition source");
            editCodeListValueDialog.hitSaveButton();
        }
    }

    @Test
    @DisplayName("TC_11_5_TA_4")
    public void test_TA_4() {
        AppUserObject developerA;
        ReleaseObject workingBranch;
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<CodeListObject, CodeListValueObject> codeListCodeListValueMap = new HashMap<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerB, namespace, workingBranch, "Published");
            CodeListValueObject codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerB);
            codeListCodeListValueMap.put(codeList, codeListValue);
            codeListForTesting.add(codeList);
        }
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (CodeListObject codeList : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), workingBranch.getReleaseNumber());
            editCodeListPage.hitRevise();
            assertEquals("Working", getText(editCodeListPage.getReleaseField()));
            assertTrue(Integer.valueOf(getText(editCodeListPage.getRevisionField())) > 1);
            assertEquals("WIP", getText(editCodeListPage.getStateField()));
            CodeListValueObject value = codeListCodeListValueMap.get(codeList);
            EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
            editCodeListValueDialog.setCode(value.getValue());
            editCodeListValueDialog.setMeaning(value.getMeaning());
            String enteredValue = getText(editCodeListValueDialog.getCodeField());
            editCodeListValueDialog.hitAddButton();
            String message = enteredValue + " already exist";
            assert message.equals(getSnackBarMessage(getDriver()));
        }
    }

    @Test
    @DisplayName("TC_11_5_TA_5")
    public void test_TA_5() {
        AppUserObject developerA;
        ReleaseObject workingBranch;
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<CodeListObject, CodeListValueObject> codeListCodeListValueMap = new HashMap<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerB, namespace, workingBranch, "Published");
            CodeListValueObject codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerB);
            codeListCodeListValueMap.put(codeList, codeListValue);
            codeListForTesting.add(codeList);
        }
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (CodeListObject codeList : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), workingBranch.getReleaseNumber());
            editCodeListPage.hitRevise();
            assertEquals("Working", getText(editCodeListPage.getReleaseField()));
            assertTrue(Integer.valueOf(getText(editCodeListPage.getRevisionField())) > 1);
            assertEquals("WIP", getText(editCodeListPage.getStateField()));
            String newCodeValue = "new code value";
            EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
            editCodeListValueDialog.setCode(newCodeValue);
            editCodeListValueDialog.setMeaning("new code value meaning");
            editCodeListValueDialog.hitAddButton();
            editCodeListPage.selectCodeListValue(newCodeValue);
            assertDoesNotThrow(() -> editCodeListPage.removeCodeListValue());
        }
    }

    @Test
    @DisplayName("TC_11_5_TA_6")
    public void test_TA_6() {
        AppUserObject developerA;
        ReleaseObject workingBranch;
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<CodeListObject, CodeListValueObject> codeListCodeListValueMap = new HashMap<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerB, namespace, workingBranch, "Published");
            CodeListValueObject codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerB);
            codeListCodeListValueMap.put(codeList, codeListValue);
            codeListForTesting.add(codeList);
        }
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (CodeListObject codeList : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), workingBranch.getReleaseNumber());
            editCodeListPage.hitRevise();
            assertEquals("Working", getText(editCodeListPage.getReleaseField()));
            assertTrue(Integer.valueOf(getText(editCodeListPage.getRevisionField())) > 1);
            assertEquals("WIP", getText(editCodeListPage.getStateField()));
            editCodeListPage.setVersion("new version");
            editCodeListPage.setDefinition("new definition");
            editCodeListPage.setDefinitionSource("new definition source");
            String newCodeValue = "new code value";
            EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
            editCodeListValueDialog.setCode(newCodeValue);
            editCodeListValueDialog.setMeaning("new code value meaning");
            editCodeListValueDialog.hitAddButton();
            editCodeListPage.hitUpdateButton();
            editCodeListPage.hitCancelButton();
            CodeListValueObject oldValue = codeListCodeListValueMap.get(codeList);
            assertEquals("Published", getText(editCodeListPage.getStateField()));
            assertEquals("1", getText(editCodeListPage.getRevisionField()));
            assertEquals(codeList.getVersionId(), getText(editCodeListPage.getVersionField()));
            assertEquals(codeList.getDefinition(), getText(editCodeListPage.getDefinitionField()));
            assertEquals(codeList.getDefinitionSource(), getText(editCodeListPage.getDefinitionSourceField()));
            assertDoesNotThrow(() -> editCodeListPage.valueExists(oldValue.getValue()));
            assertThrows(TimeoutException.class, () -> {
                editCodeListPage.valueExists(newCodeValue);
            });

        }
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
