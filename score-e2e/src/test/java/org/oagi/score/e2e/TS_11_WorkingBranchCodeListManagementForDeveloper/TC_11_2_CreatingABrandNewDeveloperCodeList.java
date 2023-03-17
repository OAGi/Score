package org.oagi.score.e2e.TS_11_WorkingBranchCodeListManagementForDeveloper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.CodeListObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.code_list.AddCodeListCommentDialog;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.EditCodeListValueDialog;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.openqa.selenium.TimeoutException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.SAME_THREAD)
public class TC_11_2_CreatingABrandNewDeveloperCodeList extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }
    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_11_2_TA_1")
    public void test_TA_1() {
        AppUserObject developer;
        ReleaseObject workingBranch;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        getDriver().manage().window().maximize();
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage  = viewEditCodeListPage.hitNewCodeListButton(developer, workingBranch.getReleaseNumber());
        CodeListObject codeList = getAPIFactory().getCodeListAPI().getNewlyCreatedCodeList(developer, workingBranch.getReleaseNumber());
        assertEquals("Code List", getText(editCodeListPage.getCodeListNameField()));
        assertEquals("1", getText(editCodeListPage.getVersionField()));
        assertTrue(getAPIFactory().getCodeListAPI().isListIdUnique(codeList.getListId()));
        assertEquals(null, getText(editCodeListPage.getDefinitionSourceField()));
        assertEquals(null, getText(editCodeListPage.getDefinitionField()));
        assertDisabled(editCodeListPage.getDeprecatedSelectField());
        assertNotChecked(editCodeListPage.getDeprecatedSelectField());
        assertEquals("Namespace", getText(editCodeListPage.getNamespaceSelectField()));
        AddCodeListCommentDialog addCodeListCommentDialog = editCodeListPage.hitAddCommentButton();
        assertEquals(null, getText(addCodeListCommentDialog.getCommentField()));
        addCodeListCommentDialog.hitCloseButton();
        assertEquals("Working", getText(editCodeListPage.getReleaseField()));
        assertEquals("1", getText(editCodeListPage.getRevisionField()));
        ArrayList<String> oagisOwnedListIDs = getAPIFactory().getCodeListAPI().getOAGISOwnedLists();
        assertTrue(oagisOwnedListIDs.contains(getText(editCodeListPage.getAgencyIDListField())));
    }

    @Test
    @DisplayName("TC_11_2_TA_2")
    public void test_TA_2() {
        AppUserObject developer;
        ReleaseObject workingBranch;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        getDriver().manage().window().maximize();
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage  = viewEditCodeListPage.hitNewCodeListButton(developer, workingBranch.getReleaseNumber());
        CodeListObject codeList = getAPIFactory().getCodeListAPI().getNewlyCreatedCodeList(developer, workingBranch.getReleaseNumber());
        assertEquals(null, codeList.getBasedCodeListManifestId());
        editCodeListPage.setDefinition("test definition");
        editCodeListPage.setDefinitionSource("test definition source");
        editCodeListPage.setName("test code list");
        EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
        editCodeListValueDialog.setCode("code value");
        editCodeListValueDialog.setMeaning("code meaning");
        editCodeListValueDialog.hitAddButton();
        editCodeListValueDialog = editCodeListPage.addCodeListValue();
        editCodeListValueDialog.setCode("code value 2");
        editCodeListValueDialog.setMeaning("code meaning 2");
        editCodeListValueDialog.hitAddButton();
        editCodeListValueDialog = editCodeListPage.addCodeListValue();
        editCodeListValueDialog.setCode("code value");
        editCodeListValueDialog.setMeaning("code meaning");
        String enteredValue = getText(editCodeListValueDialog.getCodeField());
        editCodeListValueDialog.hitAddButton();
        String message = enteredValue+" already exist";
        assert message.equals(getSnackBarMessage(getDriver()));
    }
    @Test
    @DisplayName("TC_11_2_TA_3")
    public void test_TA_3() {
        AppUserObject developer;
        ReleaseObject workingBranch;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        getDriver().manage().window().maximize();
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage  = viewEditCodeListPage.hitNewCodeListButton(developer, workingBranch.getReleaseNumber());
        CodeListObject codeList = getAPIFactory().getCodeListAPI().getNewlyCreatedCodeList(developer, workingBranch.getReleaseNumber());
        assertEquals(null, codeList.getBasedCodeListManifestId());
        editCodeListPage.setDefinition("test definition");
        editCodeListPage.setDefinitionSource("test definition source");
        editCodeListPage.setName("test code list");
        EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
        editCodeListValueDialog.setCode("code value");
        editCodeListValueDialog.setMeaning("code meaning");
        editCodeListValueDialog.hitAddButton();
        editCodeListValueDialog = editCodeListPage.addCodeListValue();
        editCodeListValueDialog.setCode("code value 2");
        editCodeListValueDialog.setMeaning("code meaning 2");
        editCodeListValueDialog.hitAddButton();
        editCodeListPage.selectCodeListValue("code value 2");
        editCodeListPage.removeCodeListValue();
    }
    @Test
    @DisplayName("TC_11_2_TA_4")
    public void test_TA_4() {
        AppUserObject developer;
        ReleaseObject workingBranch;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        getDriver().manage().window().maximize();
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage  = viewEditCodeListPage.hitNewCodeListButton(developer, workingBranch.getReleaseNumber());
        CodeListObject codeList = getAPIFactory().getCodeListAPI().getNewlyCreatedCodeList(developer, workingBranch.getReleaseNumber());
        editCodeListPage.setVersion("test version");
        assertEquals("true", editCodeListPage.getVersionField().getAttribute("aria-required"));
        String agencyIDList = getText(editCodeListPage.getAgencyIDListField());
        assertTrue(getAPIFactory().getCodeListAPI().checkCodeListUniqueness(codeList, agencyIDList));
    }
    @Test
    @DisplayName("TC_11_2_TA_5")
    public void test_TA_5() {
        AppUserObject developerA;
        ReleaseObject release;
        CodeListObject codeList;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developerB);
            codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerB, namespace, release, "Published");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerB);
        }
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        getDriver().manage().window().maximize();
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage  = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeList.getName(), release.getReleaseNumber());
        assertThrows(TimeoutException.class, () -> editCodeListPage.getDeriveCodeListBasedOnThisButton());
    }

    @Test
    @DisplayName("TC_11_2_TA_6")
    public void test_TA_6() {
        AppUserObject developer;
        ReleaseObject release;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        getDriver().manage().window().maximize();
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        viewEditCodeListPage.setBranch(release.getReleaseNumber());
        assertThrows(TimeoutException.class, () -> viewEditCodeListPage.getNewCodeListButton());
    }
    @Test
    @DisplayName("TC_11_2_TA_7")
    public void test_TA_7() {
        AppUserObject developer;
        ReleaseObject workingBranch;
        {
            developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        getDriver().manage().window().maximize();
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage  = viewEditCodeListPage.hitNewCodeListButton(developer, workingBranch.getReleaseNumber());
        CodeListObject codeList = getAPIFactory().getCodeListAPI().getNewlyCreatedCodeList(developer, workingBranch.getReleaseNumber());
        assertEquals(null, codeList.getBasedCodeListManifestId());
        editCodeListPage.setDefinition("test definition");
        editCodeListPage.setDefinitionSource("test definition source");
        editCodeListPage.setName("test code list");
        EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
        editCodeListValueDialog.setCode("code value");
        editCodeListValueDialog.setMeaning("code meaning");
        assertDisabled(editCodeListValueDialog.getDeprecatedSelectField());
        editCodeListValueDialog.hitAddButton();
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
