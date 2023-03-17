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
import org.openqa.selenium.NoSuchElementException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_11_3_EditingABrandNewDeveloperCodeList extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }
    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_11_3_TA_1")
    public void test_TA_1() {
        AppUserObject developer;
        ReleaseObject workingBranch;
        CodeListObject codeList;
        ArrayList<NamespaceObject> namespaceForTesting = new ArrayList<>();
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject OAGiNamespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer);
            namespaceForTesting.add(namespace);
            codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developer, OAGiNamespace, workingBranch, "WIP");
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);
            namespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developerB);
            namespaceForTesting.add(namespace);
            AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);
            namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
            namespaceForTesting.add(namespace);

        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        getDriver().manage().window().maximize();
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), workingBranch.getReleaseNumber());
        /**
         * Test Assertion #11.3.1.a
         */
        editCodeListPage.setName("new name");
        editCodeListPage.setVersion("new version");
        editCodeListPage.hitUpdateButton();
        String agencyIDList = getText(editCodeListPage.getAgencyIDListField());
        assertTrue(getAPIFactory().getCodeListAPI().checkCodeListUniqueness(codeList, agencyIDList));
        /**
         * Test Assertion #11.3.1.b
         * Note: For developer Based Code list is not visible on the UI
         */
        assertTrue(codeList.getBasedCodeListManifestId()==null);
        /**
         * Test Assertion #11.3.1.c
         */
        assertEquals("true", editCodeListPage.getCodeListNameField().getAttribute("aria-required"));
        assertEquals("true", editCodeListPage.getAgencyIDListField().getAttribute("aria-required"));
        assertEquals("true", editCodeListPage.getVersionField().getAttribute("aria-required"));
        assertEquals("true", editCodeListPage.getNamespaceSelectField().getAttribute("aria-required"));
        assertDisabled(editCodeListPage.getDeprecatedSelectField());
        assertNotChecked(editCodeListPage.getDeprecatedSelectField());
        /**
         * Test Assertion #11.3.1.d
         */
        editCodeListPage.setDefinition("");
        editCodeListPage.hitUpdateButton();
        assertEquals("Are you sure you want to update this without definitions?",
                editCodeListPage.getDefinitionWarningDialogMessage());
        editCodeListPage.hitUpdateAnywayButton();
        /**
         * Test Assertion #11.3.1.e
         */
        for (NamespaceObject namespace : namespaceForTesting){
            assertThrows(Exception.class, () -> {
                editCodeListPage.setNamespace(namespace);
            });
        }
        escape(getDriver());
        /**
         * Test Assertion #11.3.1.f
         */
        EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
        editCodeListValueDialog.setCode("test code");
        editCodeListValueDialog.setMeaning("test meaning");
        editCodeListValueDialog.setDefinition("test definition");
        editCodeListValueDialog.setDefinitionSource("test definition source");
        assertDisabled(editCodeListValueDialog.getDeprecatedSelectField());
        assertNotChecked(editCodeListValueDialog.getDeprecatedSelectField());
        editCodeListValueDialog.hitAddButton();
        editCodeListValueDialog = editCodeListPage.addCodeListValue();
        editCodeListValueDialog.setCode("test code");
        editCodeListValueDialog.setMeaning("different meaning");
        String enteredValue = getText(editCodeListValueDialog.getCodeField());
        editCodeListValueDialog.hitAddButton();
        String message = enteredValue+" already exist";
        assert message.equals(getSnackBarMessage(getDriver()));
    }
    @Test
    @DisplayName("TC_11_3_TA_2")
    public void test_TA_2() {
        AppUserObject developer;
        ReleaseObject workingBranch;
        CodeListObject codeList;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject OAGiNamespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developer, OAGiNamespace, workingBranch, "WIP");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        getDriver().manage().window().maximize();
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), workingBranch.getReleaseNumber());
        EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
        assertEquals("true", editCodeListValueDialog.getCodeField().getAttribute("aria-required"));
        assertEquals("true", editCodeListValueDialog.getMeaningField().getAttribute("aria-required"));
        assertEquals("false", editCodeListValueDialog.getDefinitionSourceField().getAttribute("aria-required"));
        assertEquals("false", editCodeListValueDialog.getDefinitionField().getAttribute("aria-required"));
    }
    @Test
    @DisplayName("TC_11_3_TA_3")
    public void test_TA_3() {
        AppUserObject developer;
        ReleaseObject workingBranch;
        CodeListObject codeList;
        CodeListValueObject codeListValue;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject OAGiNamespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developer, OAGiNamespace, workingBranch, "WIP");
            codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developer);
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        getDriver().manage().window().maximize();
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), workingBranch.getReleaseNumber());
        editCodeListPage.selectCodeListValue(codeListValue.getValue());
        assertDoesNotThrow(() -> editCodeListPage.removeCodeListValue());
    }

    @Test
    @DisplayName("TC_11_3_TA_4")
    public void test_TA_4() {
        AppUserObject developer;
        ReleaseObject workingBranch;
        CodeListObject codeList;
        CodeListValueObject codeListValueOne;
        CodeListValueObject codeListValueTwo;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject OAGiNamespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developer, OAGiNamespace, workingBranch, "WIP");
            codeListValueOne = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developer);
            codeListValueTwo = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developer);
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        getDriver().manage().window().maximize();
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), workingBranch.getReleaseNumber());
        EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.editCodeListValue(codeListValueOne.getValue());
        editCodeListValueDialog.setCode("new code");
        editCodeListValueDialog.setMeaning("new meaning");
        editCodeListValueDialog.setDefinition("new definition");
        editCodeListValueDialog.setDefinitionSource("new definition source");
        assertDisabled(editCodeListPage.getDeprecatedSelectField());
        editCodeListValueDialog.hitSaveButton();

        editCodeListValueDialog = editCodeListPage.editCodeListValue(codeListValueTwo.getValue());
        editCodeListValueDialog.setMeaning("new meaning");
        editCodeListValueDialog.setDefinition("new definition");
        editCodeListValueDialog.setDefinitionSource("new definition source");
        assertDisabled(editCodeListPage.getDeprecatedSelectField());
        editCodeListValueDialog.hitSaveButton();
    }

    @Test
    @DisplayName("TC_11_3_TA_5")
    public void test_TA_5() {
        AppUserObject developer;
        ReleaseObject workingBranch;
        CodeListObject codeList;
        CodeListValueObject codeListValueOne;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject OAGiNamespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developer, OAGiNamespace, workingBranch, "WIP");
            codeListValueOne = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developer);
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        getDriver().manage().window().maximize();
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), workingBranch.getReleaseNumber());
        EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
        editCodeListValueDialog.setCode(codeListValueOne.getValue());
        editCodeListValueDialog.setMeaning("different meaning");
        editCodeListValueDialog.setDefinition("different definition");
        editCodeListValueDialog.setDefinitionSource("different definition source");
        String enteredValue = getText(editCodeListValueDialog.getCodeField());
        editCodeListValueDialog.hitAddButton();
        String message = enteredValue+" already exist";
        assert message.equals(getSnackBarMessage(getDriver()));

        editCodeListValueDialog = editCodeListPage.addCodeListValue();
        editCodeListValueDialog.setCode(codeListValueOne.getValue());
        editCodeListValueDialog.setMeaning(codeListValueOne.getMeaning());
        editCodeListValueDialog.setDefinition(codeListValueOne.getDefinition());
        editCodeListValueDialog.setDefinitionSource(codeListValueOne.getDefinitionSource());
        enteredValue = getText(editCodeListValueDialog.getCodeField());
        editCodeListValueDialog.hitAddButton();
        message = enteredValue+" already exist";
        assert message.equals(getSnackBarMessage(getDriver()));
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
