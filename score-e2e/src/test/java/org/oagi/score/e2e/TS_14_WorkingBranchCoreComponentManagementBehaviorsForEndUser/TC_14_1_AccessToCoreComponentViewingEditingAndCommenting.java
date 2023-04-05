package org.oagi.score.e2e.TS_14_WorkingBranchCoreComponentManagementBehaviorsForEndUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Acc;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.oagi.score.e2e.page.code_list.AddCommentDialog;
import org.oagi.score.e2e.page.core_component.*;
import org.openqa.selenium.TimeoutException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;


@Execution(ExecutionMode.CONCURRENT)
public class TC_14_1_AccessToCoreComponentViewingEditingAndCommenting extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_14_1_TA_1")
    public void test_TA_1() {
        AppUserObject endUser;
        ReleaseObject workingBranch;
        ArrayList<ACCObject> accForTesting = new ArrayList<>();
        ArrayList<BCCPObject> bccpForTesting = new ArrayList<>();
        ArrayList<BCCObject> bccForTesting = new ArrayList<>();
        ArrayList<ASCCPObject> asccpForTesting = new ArrayList<>();
        ArrayList<ASCCObject> asccForTesting = new ArrayList<>();
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);
            AppUserObject developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
            thisAccountWillBeDeletedAfterTests(developerA);
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            /**
             * Published Developer Core Components
             */
            ACCObject acc = coreComponentAPI.createRandomACC(developerB, workingBranch, namespace, "Published");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, developerB, namespace, "Published");
            DTObject dt = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", workingBranch.getReleaseNumber());
            BCCPObject bccp = coreComponentAPI.createRandomBCCP(dt, developerB, namespace, "Published");
            bccpForTesting.add(bccp);
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Published");
            bccForTesting.add(bcc);
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, developerB, namespace, "Published");
            asccpForTesting.add(asccp);

            acc = coreComponentAPI.createRandomACC(developerA, workingBranch, namespace, "Published");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, developerA, namespace, "Published");
            bccp = coreComponentAPI.createRandomBCCP(dt, developerA, namespace, "Published");
            bccpForTesting.add(bccp);
            bcc = coreComponentAPI.appendBCC(acc, bccp, "Published");
            bccForTesting.add(bcc);
            asccp = coreComponentAPI.createRandomASCCP(acc, developerA, namespace, "Published");
            asccpForTesting.add(asccp);

            /**
             * WIP developer Core Components
             */
            acc = coreComponentAPI.createRandomACC(developerB, workingBranch, namespace, "WIP");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, developerB, namespace, "WIP");
            DTObject dtWorkingRelease = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", workingBranch.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dtWorkingRelease, developerB, namespace, "WIP");
            bccpForTesting.add(bccp);
            bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bccForTesting.add(bcc);
            asccp = coreComponentAPI.createRandomASCCP(acc, developerB, namespace, "WIP");
            asccpForTesting.add(asccp);

            acc = coreComponentAPI.createRandomACC(developerA, workingBranch, namespace, "WIP");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, developerA, namespace, "WIP");
            bccp = coreComponentAPI.createRandomBCCP(dtWorkingRelease, developerA, namespace, "WIP");
            bccpForTesting.add(bccp);
            bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bccForTesting.add(bcc);
            asccp = coreComponentAPI.createRandomASCCP(acc, developerA, namespace, "WIP");
            asccpForTesting.add(asccp);

            /**
             * QA developer Core Components
             */
            acc = coreComponentAPI.createRandomACC(developerB, workingBranch, namespace, "QA");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, developerB, namespace, "QA");
            bccp = coreComponentAPI.createRandomBCCP(dtWorkingRelease, developerB, namespace, "QA");
            bccpForTesting.add(bccp);
            bcc = coreComponentAPI.appendBCC(acc, bccp, "QA");
            bccForTesting.add(bcc);
            asccp = coreComponentAPI.createRandomASCCP(acc, developerB, namespace, "QA");
            asccpForTesting.add(asccp);

            acc = coreComponentAPI.createRandomACC(developerA, workingBranch, namespace, "QA");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, developerA, namespace, "QA");
            bccp = coreComponentAPI.createRandomBCCP(dtWorkingRelease, developerA, namespace, "QA");
            bccpForTesting.add(bccp);
            bcc = coreComponentAPI.appendBCC(acc, bccp, "QA");
            bccForTesting.add(bcc);
            asccp = coreComponentAPI.createRandomASCCP(acc, developerA, namespace, "QA");
            asccpForTesting.add(asccp);

            /**
             * Candidate developer Core Components
             */
            acc = coreComponentAPI.createRandomACC(developerB, workingBranch, namespace, "Candidate");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, developerB, namespace, "Candidate");
            bccp = coreComponentAPI.createRandomBCCP(dtWorkingRelease, developerB, namespace, "Candidate");
            bccpForTesting.add(bccp);
            bcc = coreComponentAPI.appendBCC(acc, bccp, "Candidate");
            bccForTesting.add(bcc);
            asccp = coreComponentAPI.createRandomASCCP(acc, developerB, namespace, "Candidate");
            asccpForTesting.add(asccp);

            acc = coreComponentAPI.createRandomACC(developerA, workingBranch, namespace, "Candidate");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, developerA, namespace, "Candidate");
            bccp = coreComponentAPI.createRandomBCCP(dtWorkingRelease, developerA, namespace, "Candidate");
            bccpForTesting.add(bccp);
            bcc = coreComponentAPI.appendBCC(acc, bccp, "Candidate");
            bccForTesting.add(bcc);
            asccp = coreComponentAPI.createRandomASCCP(acc, developerA, namespace, "Candidate");
            asccpForTesting.add(asccp);

            /**
             * Deleted developer Core Components
             */
            acc = coreComponentAPI.createRandomACC(developerB, workingBranch, namespace, "Deleted");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, developerB, namespace, "Deleted");
            bccp = coreComponentAPI.createRandomBCCP(dtWorkingRelease, developerB, namespace, "Deleted");
            bccpForTesting.add(bccp);
            bcc = coreComponentAPI.appendBCC(acc, bccp, "Deleted");
            bccForTesting.add(bcc);
            asccp = coreComponentAPI.createRandomASCCP(acc, developerB, namespace, "Deleted");
            asccpForTesting.add(asccp);

            acc = coreComponentAPI.createRandomACC(developerA, workingBranch, namespace, "Deleted");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, developerA, namespace, "Deleted");
            bccp = coreComponentAPI.createRandomBCCP(dtWorkingRelease, developerA, namespace, "Deleted");
            bccpForTesting.add(bccp);
            bcc = coreComponentAPI.appendBCC(acc, bccp, "Deleted");
            bccForTesting.add(bcc);
            asccp = coreComponentAPI.createRandomASCCP(acc, developerA, namespace, "Deleted");
            asccpForTesting.add(asccp);

        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(workingBranch.getReleaseNumber());
        viewEditCoreComponentPage.selectAllComponentTypes();
        for (ACCObject acc : accForTesting) {
            viewEditCoreComponentPage.setDEN(acc.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            assertDoesNotThrow(() -> {
                viewEditCoreComponentPage.getTableRecordByValue(acc.getDen());
            });
        }

        for (BCCPObject bccp : bccpForTesting) {
            viewEditCoreComponentPage.setDEN(bccp.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            assertDoesNotThrow(() -> {
                viewEditCoreComponentPage.getTableRecordByValue(bccp.getDen());
            });
        }

        for (BCCObject bcc : bccForTesting) {
            viewEditCoreComponentPage.setDEN(bcc.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            assertDoesNotThrow(() -> {
                viewEditCoreComponentPage.getTableRecordByValue(bcc.getDen());
            });
        }
        for (ASCCPObject asccp : asccpForTesting) {
            viewEditCoreComponentPage.setDEN(asccp.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            assertDoesNotThrow(() -> {
                viewEditCoreComponentPage.getTableRecordByValue(asccp.getDen());
            });
        }
        for (ASCCObject ascc : asccForTesting) {
            viewEditCoreComponentPage.setDEN(ascc.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            assertDoesNotThrow(() -> {
                viewEditCoreComponentPage.getTableRecordByValue(ascc.getDen());
            });
        }
    }

    @Test
    @DisplayName("TC_14_1_TA_2")
    public void test_TA_2() {
        AppUserObject endUserA;
        AppUserObject endUserB;
        ReleaseObject workingBranch;
        ReleaseObject release;
        ArrayList<ACCObject> accForTesting = new ArrayList<>();
        ArrayList<BCCPObject> bccpForTesting = new ArrayList<>();
        ArrayList<BCCObject> bccForTesting = new ArrayList<>();
        ArrayList<ASCCPObject> asccpForTesting = new ArrayList<>();
        ArrayList<ASCCObject> asccForTesting = new ArrayList<>();
        Map<TopLevelASBIEPObject, ASCCPObject> topLevelAsbiepASCCPMap = new HashMap<>();
        Map<AppUserObject, NamespaceObject> userNamespaceMap = new HashMap<>();
        Map<TopLevelASBIEPObject, AppUserObject> topLevelASBIEPOwnerMap = new HashMap<>();
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);
            endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB);
            userNamespaceMap.put(endUserB, namespaceEU);

            ACCObject acc = coreComponentAPI.createRandomACC(developerB, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, developerB, namespace, "Published");
            DTObject dt = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            BCCPObject bccp = coreComponentAPI.createRandomBCCP(dt, developerB, namespace, "Published");
            coreComponentAPI.appendBCC(acc, bccp, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, developerB, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUserB);
            TopLevelASBIEPObject topLevelAsbiepEU = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, endUserB, "WIP");
            topLevelAsbiepASCCPMap.put(topLevelAsbiepEU, asccp);
            topLevelASBIEPOwnerMap.put(topLevelAsbiepEU, endUserB);

            /**
             * WIP end-user Core Components
             */
            acc = coreComponentAPI.createRandomACC(endUserB, release, namespaceEU, "WIP");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, endUserB, namespaceEU, "WIP");
            bccp = coreComponentAPI.createRandomBCCP(dt, endUserB, namespaceEU, "WIP");
            bccpForTesting.add(bccp);
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bccForTesting.add(bcc);
            asccp = coreComponentAPI.createRandomASCCP(acc, endUserB, namespaceEU, "WIP");
            asccpForTesting.add(asccp);

            /**
             * QA end-user Core Components
             */
            acc = coreComponentAPI.createRandomACC(endUserB, release, namespaceEU, "QA");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, endUserB, namespaceEU, "QA");
            bccp = coreComponentAPI.createRandomBCCP(dt, endUserB, namespaceEU, "QA");
            bccpForTesting.add(bccp);
            bcc = coreComponentAPI.appendBCC(acc, bccp, "QA");
            bccForTesting.add(bcc);
            asccp = coreComponentAPI.createRandomASCCP(acc, endUserB, namespaceEU, "QA");
            asccpForTesting.add(asccp);

            /**
             * Production end-user Core Components
             */
            acc = coreComponentAPI.createRandomACC(endUserB, release, namespaceEU, "Production");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, endUserB, namespaceEU, "Production");
            bccp = coreComponentAPI.createRandomBCCP(dt, endUserB, namespaceEU, "Production");
            bccpForTesting.add(bccp);
            bcc = coreComponentAPI.appendBCC(acc, bccp, "Production");
            bccForTesting.add(bcc);
            asccp = coreComponentAPI.createRandomASCCP(acc, endUserB, namespaceEU, "Production");
            asccpForTesting.add(asccp);

            /**
             * Deleted end-user Core Components
             */
            acc = coreComponentAPI.createRandomACC(endUserB, release, namespaceEU, "Deleted");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, endUserB, namespaceEU, "Deleted");
            bccp = coreComponentAPI.createRandomBCCP(dt, endUserB, namespaceEU, "Deleted");
            bccpForTesting.add(bccp);
            bcc = coreComponentAPI.appendBCC(acc, bccp, "Deleted");
            bccForTesting.add(bcc);
            asccp = coreComponentAPI.createRandomASCCP(acc, endUserB, namespaceEU, "Deleted");
            asccpForTesting.add(asccp);
        }

        /**
         * login as end user to create UEGACC
         */
        ArrayList<ACCObject> userExtensions = new ArrayList<>();
        HomePage homePage = loginPage().signIn(endUserB.getLoginId(), endUserB.getPassword());
        for (TopLevelASBIEPObject topLevelASBIEP : topLevelAsbiepASCCPMap.keySet()) {
            ASCCPObject asccp = topLevelAsbiepASCCPMap.get(topLevelASBIEP);
            AppUserObject owner = topLevelASBIEPOwnerMap.get(topLevelASBIEP);
            NamespaceObject namespace = userNamespaceMap.get(owner);
            BIEMenu bieMenu = homePage.getBIEMenu();
            ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
            EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
            ACCExtensionViewEditPage accExtensionViewEditPage =
                    editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
            String den = accExtensionViewEditPage.getDENFieldValue();
            ACCObject accExtension = getAPIFactory().getCoreComponentAPI().getACCByDENAndReleaseNum(den, release.getReleaseNumber());
            userExtensions.add(accExtension);
            accExtensionViewEditPage.setNamespace(namespace);
            accExtensionViewEditPage.hitUpdateButton();
            homePage.logout();
        }
        homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(workingBranch.getReleaseNumber());
        viewEditCoreComponentPage.selectAllComponentTypes();
        for (ACCObject acc : accForTesting) {
            viewEditCoreComponentPage.setDEN(acc.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            assertThrows(TimeoutException.class, () -> {
                viewEditCoreComponentPage.getTableRecordByValue(acc.getDen());
            });
        }
        for (ACCObject acc : userExtensions) {
            viewEditCoreComponentPage.setDEN(acc.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            assertThrows(TimeoutException.class, () -> {
                viewEditCoreComponentPage.getTableRecordByValue(acc.getDen());
            });
        }
        for (BCCPObject bccp : bccpForTesting) {
            viewEditCoreComponentPage.setDEN(bccp.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            assertThrows(TimeoutException.class, () -> {
                viewEditCoreComponentPage.getTableRecordByValue(bccp.getDen());
            });
        }
        for (BCCObject bcc : bccForTesting) {
            viewEditCoreComponentPage.setDEN(bcc.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            assertThrows(TimeoutException.class, () -> {
                viewEditCoreComponentPage.getTableRecordByValue(bcc.getDen());
            });
        }
        for (ASCCPObject asccp : asccpForTesting) {
            viewEditCoreComponentPage.setDEN(asccp.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            assertThrows(TimeoutException.class, () -> {
                viewEditCoreComponentPage.getTableRecordByValue(asccp.getDen());
            });
        }
        for (ASCCObject ascc : asccForTesting) {
            viewEditCoreComponentPage.setDEN(ascc.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            assertThrows(TimeoutException.class, () -> {
                viewEditCoreComponentPage.getTableRecordByValue(ascc.getDen());
            });
        }
    }

    @Test
    @DisplayName("TC_14_1_TA_3")
    public void test_TA_3() {
        AppUserObject endUser;
        ReleaseObject workingBranch;
        ArrayList<ACCObject> accForTesting = new ArrayList<>();
        ArrayList<BCCPObject> bccpForTesting = new ArrayList<>();
        ArrayList<ASCCPObject> asccpForTesting = new ArrayList<>();
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);
            AppUserObject developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
            thisAccountWillBeDeletedAfterTests(developerA);
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            /**
             * WIP developer Core Components
             */
            ACCObject acc = coreComponentAPI.createRandomACC(developerB, workingBranch, namespace, "WIP");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, developerB, namespace, "WIP");
            DTObject dtWorkingRelease = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", workingBranch.getReleaseNumber());
            BCCPObject bccp = coreComponentAPI.createRandomBCCP(dtWorkingRelease, developerB, namespace, "WIP");
            bccpForTesting.add(bccp);
            coreComponentAPI.appendBCC(acc, bccp, "WIP");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, developerB, namespace, "WIP");
            asccpForTesting.add(asccp);

            acc = coreComponentAPI.createRandomACC(developerA, workingBranch, namespace, "WIP");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, developerA, namespace, "WIP");
            bccp = coreComponentAPI.createRandomBCCP(dtWorkingRelease, developerA, namespace, "WIP");
            bccpForTesting.add(bccp);
            coreComponentAPI.appendBCC(acc, bccp, "WIP");
            asccp = coreComponentAPI.createRandomASCCP(acc, developerA, namespace, "WIP");
            asccpForTesting.add(asccp);

        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        for (ACCObject acc : accForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(acc.getOwnerUserId());
            assertTrue(owner.isDeveloper());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), workingBranch.getReleaseNumber());
            assertEquals("WIP", acc.getState());
            assertDisabled(accViewEditPage.getDefinitionField());
            assertDisabled(accViewEditPage.getDefinitionSourceField());
            assertDisabled(accViewEditPage.getObjectClassTermField());
            assertDisabled(accViewEditPage.getDENField());
            AddCommentDialog addCommentDialog = accViewEditPage.hitAddCommentButton();
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }

        for (BCCPObject bccp : bccpForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(bccp.getOwnerUserId());
            assertTrue(owner.isDeveloper());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByDenAndBranch(bccp.getDen(), workingBranch.getReleaseNumber());
            assertDisabled(bccpViewEditPage.getDefinitionField());
            assertDisabled(bccpViewEditPage.getDefinitionSourceField());
            assertDisabled(bccpViewEditPage.getDENField());
            assertDisabled(bccpViewEditPage.getPropertyTermField());
            assertEquals("WIP", bccp.getState());
            AddCommentDialog addCommentDialog = bccpViewEditPage.hitAddCommentButton();
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }

        for (ASCCPObject asccp : asccpForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(asccp.getOwnerUserId());
            assertTrue(owner.isDeveloper());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch(asccp.getDen(), workingBranch.getReleaseNumber());
            assertDisabled(asccpViewEditPage.getDefinitionField());
            assertDisabled(asccpViewEditPage.getDefinitionSourceField());
            assertDisabled(asccpViewEditPage.getDENField());
            assertDisabled(asccpViewEditPage.getPropertyTermField());
            assertEquals("WIP", asccp.getState());
            AddCommentDialog addCommentDialog = asccpViewEditPage.hitAddCommentButton();
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }
    }
    @Test
    @DisplayName("TC_14_1_TA_4")
    public void test_TA_4() {
        AppUserObject endUser;
        ReleaseObject workingBranch;
        ArrayList<ACCObject> accForTesting = new ArrayList<>();
        ArrayList<BCCPObject> bccpForTesting = new ArrayList<>();
        ArrayList<ASCCPObject> asccpForTesting = new ArrayList<>();
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);
            AppUserObject developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
            thisAccountWillBeDeletedAfterTests(developerA);
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            /**
             * QA developer Core Components
             */
            ACCObject acc = coreComponentAPI.createRandomACC(developerB, workingBranch, namespace, "QA");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, developerB, namespace, "QA");
            DTObject dtWorkingRelease = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", workingBranch.getReleaseNumber());
            BCCPObject bccp = coreComponentAPI.createRandomBCCP(dtWorkingRelease, developerB, namespace, "QA");
            bccpForTesting.add(bccp);
            coreComponentAPI.appendBCC(acc, bccp, "QA");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, developerB, namespace, "QA");
            asccpForTesting.add(asccp);

            acc = coreComponentAPI.createRandomACC(developerA, workingBranch, namespace, "QA");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, developerA, namespace, "QA");
            bccp = coreComponentAPI.createRandomBCCP(dtWorkingRelease, developerA, namespace, "QA");
            bccpForTesting.add(bccp);
            coreComponentAPI.appendBCC(acc, bccp, "QA");
            asccp = coreComponentAPI.createRandomASCCP(acc, developerA, namespace, "QA");
            asccpForTesting.add(asccp);

        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        for (ACCObject acc : accForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(acc.getOwnerUserId());
            assertTrue(owner.isDeveloper());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), workingBranch.getReleaseNumber());
            assertEquals("QA", acc.getState());
            assertDisabled(accViewEditPage.getDefinitionField());
            assertDisabled(accViewEditPage.getDefinitionSourceField());
            assertDisabled(accViewEditPage.getObjectClassTermField());
            assertDisabled(accViewEditPage.getDENField());
            AddCommentDialog addCommentDialog = accViewEditPage.hitAddCommentButton();
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }

        for (BCCPObject bccp : bccpForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(bccp.getOwnerUserId());
            assertTrue(owner.isDeveloper());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByDenAndBranch(bccp.getDen(), workingBranch.getReleaseNumber());
            assertDisabled(bccpViewEditPage.getDefinitionField());
            assertDisabled(bccpViewEditPage.getDefinitionSourceField());
            assertDisabled(bccpViewEditPage.getDENField());
            assertDisabled(bccpViewEditPage.getPropertyTermField());
            assertEquals("QA", bccp.getState());
            AddCommentDialog addCommentDialog = bccpViewEditPage.hitAddCommentButton();
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }

        for (ASCCPObject asccp : asccpForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(asccp.getOwnerUserId());
            assertTrue(owner.isDeveloper());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch(asccp.getDen(), workingBranch.getReleaseNumber());
            assertDisabled(asccpViewEditPage.getDefinitionField());
            assertDisabled(asccpViewEditPage.getDefinitionSourceField());
            assertDisabled(asccpViewEditPage.getDENField());
            assertDisabled(asccpViewEditPage.getPropertyTermField());
            assertEquals("QA", asccp.getState());
            AddCommentDialog addCommentDialog = asccpViewEditPage.hitAddCommentButton();
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }
    }
    @Test
    @DisplayName("TC_14_1_TA_5")
    public void test_TA_5() {
        AppUserObject endUser;
        ReleaseObject workingBranch;
        ArrayList<ACCObject> accForTesting = new ArrayList<>();
        ArrayList<BCCPObject> bccpForTesting = new ArrayList<>();
        ArrayList<ASCCPObject> asccpForTesting = new ArrayList<>();
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);
            AppUserObject developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
            thisAccountWillBeDeletedAfterTests(developerA);
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            /**
             * Candidate developer Core Components
             */
            ACCObject acc = coreComponentAPI.createRandomACC(developerB, workingBranch, namespace, "Candidate");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, developerB, namespace, "Candidate");
            DTObject dtWorkingRelease = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", workingBranch.getReleaseNumber());
            BCCPObject bccp = coreComponentAPI.createRandomBCCP(dtWorkingRelease, developerB, namespace, "Candidate");
            bccpForTesting.add(bccp);
            coreComponentAPI.appendBCC(acc, bccp, "Candidate");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, developerB, namespace, "Candidate");
            asccpForTesting.add(asccp);

            acc = coreComponentAPI.createRandomACC(developerA, workingBranch, namespace, "Candidate");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, developerA, namespace, "Candidate");
            bccp = coreComponentAPI.createRandomBCCP(dtWorkingRelease, developerA, namespace, "Candidate");
            bccpForTesting.add(bccp);
            coreComponentAPI.appendBCC(acc, bccp, "Candidate");
            asccp = coreComponentAPI.createRandomASCCP(acc, developerA, namespace, "Candidate");
            asccpForTesting.add(asccp);

        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        for (ACCObject acc : accForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(acc.getOwnerUserId());
            assertTrue(owner.isDeveloper());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), workingBranch.getReleaseNumber());
            assertEquals("Candidate", acc.getState());
            assertDisabled(accViewEditPage.getDefinitionField());
            assertDisabled(accViewEditPage.getDefinitionSourceField());
            assertDisabled(accViewEditPage.getObjectClassTermField());
            assertDisabled(accViewEditPage.getDENField());
            AddCommentDialog addCommentDialog = accViewEditPage.hitAddCommentButton();
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }

        for (BCCPObject bccp : bccpForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(bccp.getOwnerUserId());
            assertTrue(owner.isDeveloper());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByDenAndBranch(bccp.getDen(), workingBranch.getReleaseNumber());
            assertDisabled(bccpViewEditPage.getDefinitionField());
            assertDisabled(bccpViewEditPage.getDefinitionSourceField());
            assertDisabled(bccpViewEditPage.getDENField());
            assertDisabled(bccpViewEditPage.getPropertyTermField());
            assertEquals("Candidate", bccp.getState());
            AddCommentDialog addCommentDialog = bccpViewEditPage.hitAddCommentButton();
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }

        for (ASCCPObject asccp : asccpForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(asccp.getOwnerUserId());
            assertTrue(owner.isDeveloper());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch(asccp.getDen(), workingBranch.getReleaseNumber());
            assertDisabled(asccpViewEditPage.getDefinitionField());
            assertDisabled(asccpViewEditPage.getDefinitionSourceField());
            assertDisabled(asccpViewEditPage.getDENField());
            assertDisabled(asccpViewEditPage.getPropertyTermField());
            assertEquals("Candidate", asccp.getState());
            AddCommentDialog addCommentDialog = asccpViewEditPage.hitAddCommentButton();
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }
    }
    @Test
    @DisplayName("TC_14_1_TA_6")
    public void test_TA_6() {
        AppUserObject endUser;
        ReleaseObject workingBranch;
        ArrayList<ACCObject> accForTesting = new ArrayList<>();
        ArrayList<BCCPObject> bccpForTesting = new ArrayList<>();
        ArrayList<ASCCPObject> asccpForTesting = new ArrayList<>();
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);
            AppUserObject developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(true);
            thisAccountWillBeDeletedAfterTests(developerA);
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            /**
             * Release Draft developer Core Components
             */
            ACCObject acc = coreComponentAPI.createRandomACC(developerB, workingBranch, namespace, "ReleaseDraft");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, developerB, namespace, "ReleaseDraft");
            DTObject dtWorkingRelease = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", workingBranch.getReleaseNumber());
            BCCPObject bccp = coreComponentAPI.createRandomBCCP(dtWorkingRelease, developerB, namespace, "ReleaseDraft");
            bccpForTesting.add(bccp);
            coreComponentAPI.appendBCC(acc, bccp, "ReleaseDraft");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, developerB, namespace, "ReleaseDraft");
            asccpForTesting.add(asccp);

            acc = coreComponentAPI.createRandomACC(developerA, workingBranch, namespace, "ReleaseDraft");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, developerA, namespace, "ReleaseDraft");
            bccp = coreComponentAPI.createRandomBCCP(dtWorkingRelease, developerA, namespace, "ReleaseDraft");
            bccpForTesting.add(bccp);
            coreComponentAPI.appendBCC(acc, bccp, "ReleaseDraft");
            asccp = coreComponentAPI.createRandomASCCP(acc, developerA, namespace, "ReleaseDraft");
            asccpForTesting.add(asccp);

        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        for (ACCObject acc : accForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(acc.getOwnerUserId());
            assertTrue(owner.isDeveloper());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), workingBranch.getReleaseNumber());
            assertEquals("ReleaseDraft", acc.getState());
            assertDisabled(accViewEditPage.getDefinitionField());
            assertDisabled(accViewEditPage.getDefinitionSourceField());
            assertDisabled(accViewEditPage.getObjectClassTermField());
            assertDisabled(accViewEditPage.getDENField());
            AddCommentDialog addCommentDialog = accViewEditPage.hitAddCommentButton();
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }

        for (BCCPObject bccp : bccpForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(bccp.getOwnerUserId());
            assertTrue(owner.isDeveloper());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByDenAndBranch(bccp.getDen(), workingBranch.getReleaseNumber());
            assertDisabled(bccpViewEditPage.getDefinitionField());
            assertDisabled(bccpViewEditPage.getDefinitionSourceField());
            assertDisabled(bccpViewEditPage.getDENField());
            assertDisabled(bccpViewEditPage.getPropertyTermField());
            assertEquals("ReleaseDraft", bccp.getState());
            AddCommentDialog addCommentDialog = bccpViewEditPage.hitAddCommentButton();
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }

        for (ASCCPObject asccp : asccpForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(asccp.getOwnerUserId());
            assertTrue(owner.isDeveloper());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch(asccp.getDen(), workingBranch.getReleaseNumber());
            assertDisabled(asccpViewEditPage.getDefinitionField());
            assertDisabled(asccpViewEditPage.getDefinitionSourceField());
            assertDisabled(asccpViewEditPage.getDENField());
            assertDisabled(asccpViewEditPage.getPropertyTermField());
            assertEquals("ReleaseDraft", asccp.getState());
            AddCommentDialog addCommentDialog = asccpViewEditPage.hitAddCommentButton();
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
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
