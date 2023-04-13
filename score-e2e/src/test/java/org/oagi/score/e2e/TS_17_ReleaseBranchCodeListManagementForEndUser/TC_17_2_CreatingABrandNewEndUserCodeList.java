package org.oagi.score.e2e.TS_17_ReleaseBranchCodeListManagementForEndUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.code_list.AddCommentDialog;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.EditCodeListValueDialog;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.AssertionHelper.assertNotChecked;
import static org.oagi.score.e2e.impl.PageHelper.getSnackBarMessage;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_17_2_CreatingABrandNewEndUserCodeList extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_17_2_TA_1")
    public void test_TA_1() {
        AppUserObject endUserA;
        ReleaseObject branch;
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
        }
        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        viewEditCodeListPage.setBranch(branch.getReleaseNumber());
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openNewCodeList(endUserA, branch.getReleaseNumber());
        assertEquals("Code List", getText(editCodeListPage.getCodeListNameField()));
        assertTrue(getAPIFactory().getCodeListAPI().isListIdUnique(getText(editCodeListPage.getListIDField())));
        assertEquals("Mutually defined (ZZZ)", getText(editCodeListPage.getAgencyIDListValueField()));
        assertEquals("1", getText(editCodeListPage.getVersionField()));
        assertEquals(null, getText(editCodeListPage.getDefinitionField()));
        assertEquals(null, getText(editCodeListPage.getDefinitionSourceField()));
        assertEquals(null, getText(editCodeListPage.getRemarkField()));
        assertDisabled(editCodeListPage.getDeprecatedSelectField());
        assertNotChecked(editCodeListPage.getDeprecatedSelectField());
        assertEquals("Namespace", getText(editCodeListPage.getNamespaceSelectField()));
        AddCommentDialog addCodeListCommentDialog = editCodeListPage.hitAddCommentButton();
        assertEquals(null, getText(addCodeListCommentDialog.getCommentField()));
        addCodeListCommentDialog.hitCloseButton();
        assertEquals("1", getText(editCodeListPage.getRevisionField()));

        CodeListObject codeList = getAPIFactory().getCodeListAPI().getNewlyCreatedCodeList(endUserA, branch.getReleaseNumber());
        ReleaseObject anotherBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.6");
        homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        assertThrows(NoSuchElementException.class, () -> {viewEditCodeListPage.searchCodeListByNameAndBranch(codeList.getName(), anotherBranch.getReleaseNumber());});
    }

    @Test
    @DisplayName("TC_17_2_TA_2")
    public void test_TA_2() {
        AppUserObject endUserA;
        ReleaseObject branch;
        NamespaceObject namespaceEU;
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA);
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
        }
        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        viewEditCodeListPage.setBranch(branch.getReleaseNumber());
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openNewCodeList(endUserA, branch.getReleaseNumber());
        CodeListObject codeList = getAPIFactory().getCodeListAPI().getNewlyCreatedCodeList(endUserA, branch.getReleaseNumber());
        assertEquals(null, codeList.getBasedCodeListManifestId());
        editCodeListPage.setDefinition("test definition");
        editCodeListPage.setDefinitionSource("test definition source");
        editCodeListPage.setName("test code list");
        editCodeListPage.setNamespace(namespaceEU);
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
        String message = enteredValue + " already exist";
        assert message.equals(getSnackBarMessage(getDriver()));
        editCodeListPage.hitUpdateButton();
    }

    @Test
    @DisplayName("TC_17_2_TA_3")
    public void test_TA_3() {
        AppUserObject endUserA;
        ReleaseObject branch;
        NamespaceObject namespaceEU;
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA);
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
        }
        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        viewEditCodeListPage.setBranch(branch.getReleaseNumber());
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openNewCodeList(endUserA, branch.getReleaseNumber());
        CodeListObject codeList = getAPIFactory().getCodeListAPI().getNewlyCreatedCodeList(endUserA, branch.getReleaseNumber());
        assertEquals(null, codeList.getBasedCodeListManifestId());
        editCodeListPage.setDefinition("test definition");
        editCodeListPage.setDefinitionSource("test definition source");
        editCodeListPage.setName("test code list");
        editCodeListPage.setNamespace(namespaceEU);
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
        editCodeListPage.hitUpdateButton();
    }
    @Test
    @DisplayName("TC_17_2_TA_4")
    public void test_TA_4() {
        AppUserObject endUserA;
        ReleaseObject branch;
        NamespaceObject namespaceEU;
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA);
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
        }
        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        viewEditCodeListPage.setBranch(branch.getReleaseNumber());
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openNewCodeList(endUserA, branch.getReleaseNumber());
        CodeListObject codeList = getAPIFactory().getCodeListAPI().getNewlyCreatedCodeList(endUserA, branch.getReleaseNumber());
        editCodeListPage.setVersion("test version");
        editCodeListPage.setNamespace(namespaceEU);
        editCodeListPage.hitUpdateButton();
        assertEquals("true", editCodeListPage.getVersionField().getAttribute("aria-required"));
        String agencyIDList = getText(editCodeListPage.getAgencyIDListField());
        assertTrue(getAPIFactory().getCodeListAPI().checkCodeListUniqueness(codeList, agencyIDList));
    }
    @Test
    @DisplayName("TC_17_2_TA_5")
    public void test_TA_5() {
        AppUserObject endUserA;
        ReleaseObject branch;
        CodeListObject codeListPublished;
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");

            AppUserObject developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            /**
             * Create Published developer Code List for a particular release branch.
             */
            codeListPublished = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developerA, namespace, branch, "Published");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListPublished, developerA);
        }
        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeListPublished.getName(), branch.getReleaseNumber());
        AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(codeListPublished.getOwnerUserId());
        assertTrue(codeListPublished.getState().equals("Published"));
        assertTrue(owner.isDeveloper());
        assertDoesNotThrow(() -> {editCodeListPage.hitDeriveCodeListBasedOnThisButton();});
    }
    @Test
    @DisplayName("TC_17_2_TA_6")
    public void test_TA_6() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<CodeListObject> codeListForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);
            NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB);

            /**
             * Create end-user Code List for a particular release branch. States - WIP, Draft and Production
             */
            CodeListObject codeListWIP = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserB, euNamespace, branch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListWIP, endUserB);
            codeListForTesting.add(codeListWIP);

            CodeListObject codeListDraft = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserB, euNamespace, branch, "Draft");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListDraft, endUserB);
            codeListForTesting.add(codeListDraft);

            CodeListObject codeListProduction = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserB, euNamespace, branch, "Production");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListProduction, endUserB);
            codeListForTesting.add(codeListProduction);
        }
        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        for (CodeListObject cl: codeListForTesting){
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(cl.getName(), branch.getReleaseNumber());
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(cl.getOwnerUserId());
            assertFalse(owner.isDeveloper());
            assertThrows(TimeoutException.class, () -> editCodeListPage.getDeriveCodeListBasedOnThisButton());
        }
    }
    @Test
    @DisplayName("TC_17_2_TA_7")
    public void test_TA_7() {
        AppUserObject endUserA;
        AppUserObject developerA;
        ReleaseObject branchTwo;
        CodeListObject codeListPublished;
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            ReleaseObject branchOne = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");
            branchTwo = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            ReleaseObject branchThree = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.6");

            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            /**
             * Create Published developer Code List for a particular release branch.
             */
            codeListPublished = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developerA, namespace, branchOne, "Published");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListPublished, developerA);
            CodeListObject revisedCodeList = getAPIFactory().getCodeListAPI().createRevisionOfACodeListAndPublishInAnotherRelease(codeListPublished, branchTwo, developerA, 2);
            getAPIFactory().getCodeListAPI().createRevisionOfACodeListAndPublishInAnotherRelease(revisedCodeList, branchThree, developerA, 3);
        }
        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(codeListPublished.getName(), branchTwo.getReleaseNumber());
        assertDoesNotThrow(() -> {editCodeListPage.hitDeriveCodeListBasedOnThisButton();});
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
