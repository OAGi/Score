package org.oagi.score.e2e.TS_41_ReleaseBranchDataTypeManagementForEndUser;

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
import org.oagi.score.e2e.page.core_component.DTViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.TimeoutException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.escape;

@Execution(ExecutionMode.CONCURRENT)
public class TC_41_1_DTAccess extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_41_1_TA_1")
    public void test_TA_1() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB, library);

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Numeric. Type", "10.8.4");
            DTObject dtWIP = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseDT, endUserB, namespace, "WIP");
            dtForTesting.add(dtWIP);

            DTObject dtQA = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseDT, endUserB, namespace, "QA");
            dtForTesting.add(dtQA);

            DTObject dtProduction = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseDT, endUserB, namespace, "Production");
            dtForTesting.add(dtProduction);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            viewEditCoreComponentPage.setDEN(dt.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            assertDoesNotThrow(() -> viewEditCoreComponentPage.getTableRecordByValue(dt.getDen()));
        }

    }
    @Test
    @DisplayName("TC_41_1_TA_2")
    public void test_TA_2() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());
            DTObject randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserA, namespace, "WIP");
            dtForTesting.add(randomBDT);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        for (DTObject dt : dtForTesting) {
            assertTrue(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(dt.getState().equals("WIP"));
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            dtViewEditPage.setQualifier("qualifier");
            dtViewEditPage.hitUpdateButton();
        }

    }
    @Test
    @DisplayName("TC_41_1_TA_3")
    public void test_TA_3() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB, library);

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());
            DTObject randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserB, namespace, "WIP");
            dtForTesting.add(randomBDT);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(dt.getState().equals("WIP"));
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            assertDisabled(dtViewEditPage.getDefinitionField());
            assertDisabled(dtViewEditPage.getQualifierField());
            AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + dt.getDen());
            addCommentDialog.setComment("test comment");
            escape(getDriver());
        }

    }
    @Test
    @DisplayName("TC_41_1_TA_4")
    public void test_TA_4() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB, library);

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());

            DTObject randomBDTQA = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserB, namespace, "QA");
            dtForTesting.add(randomBDTQA);

            DTObject randomBDTProduction = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserB, namespace, "Production");
            dtForTesting.add(randomBDTProduction);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(List.of("QA", "Production").contains(dt.getState()));
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            assertDisabled(dtViewEditPage.getDefinitionField());
            assertDisabled(dtViewEditPage.getQualifierField());
            AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + dt.getDen());
            addCommentDialog.setComment("test comment");
            escape(getDriver());
        }

    }
    @Test
    @DisplayName("TC_41_1_TA_5")
    public void test_TA_5() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());

            DTObject randomBDTPublished = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, developerB, namespace, "Published");
            dtForTesting.add(randomBDTPublished);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(dt.getState().equals("Published"));
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            assertDisabled(dtViewEditPage.getDefinitionField());
            assertDisabled(dtViewEditPage.getQualifierField());
            AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + dt.getDen());
            addCommentDialog.setComment("test comment");
            escape(getDriver());
        }

    }
    @Test
    @DisplayName("TC_41_1_TA_6")
    public void test_TA_6() {
        AppUserObject developerA;
        ReleaseObject branch;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");

        }
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        assertTrue(developerA.isDeveloper());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        assertThrows(TimeoutException.class, () -> viewEditCoreComponentPage.getCreateDTButton());
    }
    @Test
    @DisplayName("TC_41_1_TA_7")
    public void test_TA_7() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB, library);

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());

            DTObject randomBDTDeleted = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserB, namespace, "Deleted");
            dtForTesting.add(randomBDTDeleted);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(dt.getState().equals("Deleted"));
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            assertDoesNotThrow(() -> {DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.
                    openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());});
        }
    }
    @Test
    @DisplayName("TC_41_1_TA_8")
    public void test_TA_8() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());

            DTObject randomBDTDeleted = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserA, namespace, "Deleted");
            dtForTesting.add(randomBDTDeleted);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        for (DTObject dt : dtForTesting) {
            assertTrue(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(dt.getState().equals("Deleted"));
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            assertDisabled(dtViewEditPage.getDefinitionField());
            assertDisabled(dtViewEditPage.getQualifierField());
            AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + dt.getDen());
            addCommentDialog.setComment("test comment");
            escape(getDriver());
        }

    }
    @Test
    @DisplayName("TC_41_1_TA_9")
    public void test_TA_9() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB, library);

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());

            DTObject randomBDTDeleted = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserB, namespace, "Deleted");
            dtForTesting.add(randomBDTDeleted);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(dt.getState().equals("Deleted"));
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            assertDisabled(dtViewEditPage.getDefinitionField());
            assertDisabled(dtViewEditPage.getQualifierField());
            AddCommentDialog addCommentDialog = dtViewEditPage.hitAddCommentButton("/" + dt.getDen());
            addCommentDialog.setComment("test comment");
            escape(getDriver());
        }

    }
    @Test
    @DisplayName("TC_41_1_TA_10")
    public void test_TA_10() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());

            DTObject randomBDTDeleted = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserA, namespace, "Deleted");
            dtForTesting.add(randomBDTDeleted);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        for (DTObject dt : dtForTesting) {
            assertTrue(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(dt.getState().equals("Deleted"));
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            assertDoesNotThrow(() -> dtViewEditPage.hitRestoreButton());
        }

    }
    @Test
    @DisplayName("TC_41_1_TA_11")
    public void test_TA_11() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB, library);

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());

            DTObject randomBDTDeleted = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserB, namespace, "Deleted");
            dtForTesting.add(randomBDTDeleted);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        for (DTObject dt : dtForTesting) {
            assertFalse(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(dt.getState().equals("Deleted"));
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            assertDoesNotThrow(() -> dtViewEditPage.hitRestoreButton());
        }

    }
    @Test
    @DisplayName("TC_41_1_TA_12_a")
    public void test_TA_12_a() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());

            DTObject randomBDTWIPOne = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserA, namespace, "WIP");
            dtForTesting.add(randomBDTWIPOne);

            DTObject randomBDTWIPTwo = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserA, namespace, "WIP");
            dtForTesting.add(randomBDTWIPTwo);

            DTObject randomBDTWIPThree = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserA, namespace, "WIP");
            dtForTesting.add(randomBDTWIPThree);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            assertTrue(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(dt.getState().equals("WIP"));
            click(viewEditCoreComponentPage.getTableRecordByValue(dt.getDen()));
        }
        viewEditCoreComponentPage.hitMoveToQAButton();

    }
    @Test
    @DisplayName("TC_41_1_TA_12_b")
    public void test_TA_12_b() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());

            DTObject randomBDTQAOne = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserA, namespace, "QA");
            dtForTesting.add(randomBDTQAOne);

            DTObject randomBDTQATwo = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserA, namespace, "QA");
            dtForTesting.add(randomBDTQATwo);

            DTObject randomBDTQAThree = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserA, namespace, "QA");
            dtForTesting.add(randomBDTQAThree);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            assertTrue(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(dt.getState().equals("QA"));
            click(viewEditCoreComponentPage.getTableRecordByValue(dt.getDen()));
        }
        viewEditCoreComponentPage.hitBackToWIPButton();

    }
    @Test
    @DisplayName("TC_41_1_TA_12_c")
    public void test_TA_12_c() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.6");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());

            DTObject randomBDTQAOne = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserA, namespace, "QA");
            dtForTesting.add(randomBDTQAOne);

            DTObject randomBDTQATwo = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserA, namespace, "QA");
            dtForTesting.add(randomBDTQATwo);

            DTObject randomBDTQAThree = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserA, namespace, "QA");
            dtForTesting.add(randomBDTQAThree);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            assertTrue(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            assertTrue(dt.getState().equals("QA"));
            click(viewEditCoreComponentPage.getTableRecordByValue(dt.getDen()));
        }
        viewEditCoreComponentPage.hitMoveToProductionButton();

    }
    @Test
    @DisplayName("TC_41_1_TA_12_d")
    public void test_TA_12_d() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.6");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());

            DTObject randomBDTWIPOne = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserA, namespace, "WIP");
            dtForTesting.add(randomBDTWIPOne);

            DTObject randomBDTWIPTwo = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserA, namespace, "WIP");
            dtForTesting.add(randomBDTWIPTwo);

            DTObject randomBDTWIPThree = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserA, namespace, "WIP");
            dtForTesting.add(randomBDTWIPThree);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            assertTrue(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            click(viewEditCoreComponentPage.getTableRecordByValue(dt.getDen()));
        }
        viewEditCoreComponentPage.hitTransferOwnershipButton();
    }
    @Test
    @DisplayName("TC_41_1_TA_12_e")
    public void test_TA_12_e() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.8");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());

            DTObject randomBDTWIPOne = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserA, namespace, "WIP");
            dtForTesting.add(randomBDTWIPOne);

            DTObject randomBDTWIPTwo = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserA, namespace, "WIP");
            dtForTesting.add(randomBDTWIPTwo);

            DTObject randomBDTWIPThree = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserA, namespace, "WIP");
            dtForTesting.add(randomBDTWIPThree);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            assertTrue(dt.getOwnerUserId().equals(endUserA.getAppUserId()));
            click(viewEditCoreComponentPage.getTableRecordByValue(dt.getDen()));
        }
        viewEditCoreComponentPage.hitDeleteButton();
    }

    @Test
    @DisplayName("TC_41_1_TA_13")
    public void test_TA_13() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            NamespaceObject namespaceEUA = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);
            NamespaceObject namespaceEUB = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB, library);

            DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());

            DTObject randomBDTWIPOne = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserB, namespaceEUB, "WIP");
            dtForTesting.add(randomBDTWIPOne);

            DTObject randomBDTWIPTwo = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserA, namespaceEUA, "WIP");
            dtForTesting.add(randomBDTWIPTwo);

            DTObject randomBDTWIPThree = getAPIFactory().getCoreComponentAPI().createRandomBDT(cdt, endUserB, namespaceEUB, "WIP");
            dtForTesting.add(randomBDTWIPThree);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            click(viewEditCoreComponentPage.getTableRecordByValue(dt.getDen()));
        }
        assertThrows(TimeoutException.class, () -> viewEditCoreComponentPage.getTransferOwnershipButton());
        assertThrows(TimeoutException.class, () -> viewEditCoreComponentPage.getDeleteButton());
        assertThrows(TimeoutException.class, () -> viewEditCoreComponentPage.getMoveToDraftButton());
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
