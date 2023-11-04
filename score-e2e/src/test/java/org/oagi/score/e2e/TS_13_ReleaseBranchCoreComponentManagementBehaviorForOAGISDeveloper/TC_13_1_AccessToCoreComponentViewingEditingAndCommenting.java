package org.oagi.score.e2e.TS_13_ReleaseBranchCoreComponentManagementBehaviorForOAGISDeveloper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
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
import static org.oagi.score.e2e.impl.PageHelper.invisibilityOfLoadingContainerElement;

@Execution(ExecutionMode.CONCURRENT)
public class TC_13_1_AccessToCoreComponentViewingEditingAndCommenting extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_13_1_TA_1")
    public void test_TA_1() {
        AppUserObject developerA;
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
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);
            AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            ReleaseObject workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            userNamespaceMap.put(developerA, namespace);
            userNamespaceMap.put(developerB, namespace);
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
            userNamespaceMap.put(endUser, namespaceEU);

            /**
             * Published Developer Core Components
             */
            ACCObject acc = coreComponentAPI.createRandomACC(developerB, release, namespace, "Published");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, developerB, namespace, "Published");
            DTObject dt = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            BCCPObject bccp = coreComponentAPI.createRandomBCCP(dt, developerB, namespace, "Published");
            bccpForTesting.add(bccp);
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Published");
            bccForTesting.add(bcc);
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, developerB, namespace, "Published");
            asccpForTesting.add(asccp);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
            TopLevelASBIEPObject topLevelAsbiepEU = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, endUser, "WIP");
            topLevelAsbiepASCCPMap.put(topLevelAsbiepEU, asccp);
            topLevelASBIEPOwnerMap.put(topLevelAsbiepEU, endUser);

            acc = coreComponentAPI.createRandomACC(developerA, release, namespace, "Published");
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
             * WIP end-user Core Components
             */
            acc = coreComponentAPI.createRandomACC(endUser, release, namespaceEU, "WIP");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, endUser, namespaceEU, "WIP");
            bccp = coreComponentAPI.createRandomBCCP(dt, endUser, namespaceEU, "WIP");
            bccpForTesting.add(bccp);
            bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bccForTesting.add(bcc);
            asccp = coreComponentAPI.createRandomASCCP(acc, endUser, namespaceEU, "WIP");
            asccpForTesting.add(asccp);

            /**
             * QA end-user Core Components
             */
            acc = coreComponentAPI.createRandomACC(endUser, release, namespaceEU, "QA");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, endUser, namespaceEU, "QA");
            bccp = coreComponentAPI.createRandomBCCP(dt, endUser, namespaceEU, "QA");
            bccpForTesting.add(bccp);
            bcc = coreComponentAPI.appendBCC(acc, bccp, "QA");
            bccForTesting.add(bcc);
            asccp = coreComponentAPI.createRandomASCCP(acc, endUser, namespaceEU, "QA");
            asccpForTesting.add(asccp);

            /**
             * Production end-user Core Components
             */
            acc = coreComponentAPI.createRandomACC(endUser, release, namespaceEU, "Production");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, endUser, namespaceEU, "Production");
            bccp = coreComponentAPI.createRandomBCCP(dt, endUser, namespaceEU, "Production");
            bccpForTesting.add(bccp);
            bcc = coreComponentAPI.appendBCC(acc, bccp, "Production");
            bccForTesting.add(bcc);
            asccp = coreComponentAPI.createRandomASCCP(acc, endUser, namespaceEU, "Production");
            asccpForTesting.add(asccp);

            /**
             * Deleted end-user Core Components
             */
            acc = coreComponentAPI.createRandomACC(endUser, release, namespaceEU, "Deleted");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, endUser, namespaceEU, "Deleted");
            bccp = coreComponentAPI.createRandomBCCP(dt, endUser, namespaceEU, "Deleted");
            bccpForTesting.add(bccp);
            bcc = coreComponentAPI.appendBCC(acc, bccp, "Deleted");
            bccForTesting.add(bcc);
            asccp = coreComponentAPI.createRandomASCCP(acc, endUser, namespaceEU, "Deleted");
            asccpForTesting.add(asccp);

        }
        /**
         * login as end user to create UEGACC
         */
        ArrayList<ACCObject> userExtensions = new ArrayList<>();
        for (TopLevelASBIEPObject topLevelASBIEP : topLevelAsbiepASCCPMap.keySet()) {
            ASCCPObject asccp = topLevelAsbiepASCCPMap.get(topLevelASBIEP);
            AppUserObject owner = topLevelASBIEPOwnerMap.get(topLevelASBIEP);
            NamespaceObject namespace = userNamespaceMap.get(owner);
            HomePage homePage = loginPage().signIn(owner.getLoginId(), owner.getPassword());
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

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(release.getReleaseNumber());
        viewEditCoreComponentPage.selectAllComponentTypes();
        for (ACCObject acc : accForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(acc.getOwnerUserId());
            viewEditCoreComponentPage.setDEN(acc.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            if (owner.isDeveloper() && !acc.getState().equals("Published")) {
                assertThrows(TimeoutException.class, () -> {
                    viewEditCoreComponentPage.getTableRecordByValue(acc.getDen());
                });
            } else {
                assertDoesNotThrow(() -> {
                    viewEditCoreComponentPage.getTableRecordByValue(acc.getDen());
                });
            }
        }

        for (ACCObject acc : userExtensions) {
            viewEditCoreComponentPage.setDEN(acc.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            assertDoesNotThrow(() -> {
                viewEditCoreComponentPage.getTableRecordByValue(acc.getDen());
            });
        }

        for (BCCPObject bccp : bccpForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(bccp.getOwnerUserId());
            viewEditCoreComponentPage.setDEN(bccp.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            if (owner.isDeveloper() && !bccp.getState().equals("Published")) {
                assertThrows(TimeoutException.class, () -> {
                    viewEditCoreComponentPage.getTableRecordByValue(bccp.getDen());
                });
            } else {
                assertDoesNotThrow(() -> {
                    viewEditCoreComponentPage.getTableRecordByValue(bccp.getDen());
                });
            }
        }

        for (BCCObject bcc : bccForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(bcc.getOwnerUserId());
            viewEditCoreComponentPage.setDEN(bcc.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            if (owner.isDeveloper() && !bcc.getState().equals("Published")) {
                assertThrows(TimeoutException.class, () -> {
                    viewEditCoreComponentPage.getTableRecordByValue(bcc.getDen());
                });
            } else {
                assertDoesNotThrow(() -> {
                    viewEditCoreComponentPage.getTableRecordByValue(bcc.getDen());
                });
            }
        }
        for (ASCCPObject asccp : asccpForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(asccp.getOwnerUserId());
            viewEditCoreComponentPage.setDEN(asccp.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            if (owner.isDeveloper() && !asccp.getState().equals("Published")) {
                assertThrows(TimeoutException.class, () -> {
                    viewEditCoreComponentPage.getTableRecordByValue(asccp.getDen());
                });
            } else {
                assertDoesNotThrow(() -> {
                    viewEditCoreComponentPage.getTableRecordByValue(asccp.getDen());
                });
            }
        }
        for (ASCCObject ascc : asccForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(ascc.getOwnerUserId());
            viewEditCoreComponentPage.setDEN(ascc.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            if (owner.isDeveloper() && !ascc.getState().equals("Published")) {
                assertThrows(TimeoutException.class, () -> {
                    viewEditCoreComponentPage.getTableRecordByValue(ascc.getDen());
                });
            } else {
                assertDoesNotThrow(() -> {
                    viewEditCoreComponentPage.getTableRecordByValue(ascc.getDen());
                });
            }
        }
    }

    @Test
    @DisplayName("TC_13_1_TA_2")
    public void test_TA_2() {
        AppUserObject developerA;
        ReleaseObject release;
        ArrayList<ACCObject> accForTesting = new ArrayList<>();
        ArrayList<BCCPObject> bccpForTesting = new ArrayList<>();
        ArrayList<ASCCPObject> asccpForTesting = new ArrayList<>();
        Map<TopLevelASBIEPObject, ASCCPObject> topLevelAsbiepASCCPMap = new HashMap<>();
        Map<AppUserObject, NamespaceObject> userNamespaceMap = new HashMap<>();
        Map<TopLevelASBIEPObject, AppUserObject> topLevelASBIEPOwnerMap = new HashMap<>();
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);
            AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            userNamespaceMap.put(developerA, namespace);
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
            userNamespaceMap.put(endUser, namespaceEU);

            /**
             * Published Developer Core Components
             */
            ACCObject acc = coreComponentAPI.createRandomACC(developerB, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, developerB, namespace, "Published");
            DTObject dt = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            BCCPObject bccp = coreComponentAPI.createRandomBCCP(dt, developerB, namespace, "Published");
            coreComponentAPI.appendBCC(acc, bccp, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, developerB, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
            TopLevelASBIEPObject topLevelAsbiepEU = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, endUser, "WIP");
            topLevelAsbiepASCCPMap.put(topLevelAsbiepEU, asccp);
            topLevelASBIEPOwnerMap.put(topLevelAsbiepEU, endUser);

            /**
             * WIP end-user Core Components
             */
            acc = coreComponentAPI.createRandomACC(endUser, release, namespaceEU, "WIP");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, endUser, namespaceEU, "WIP");
            bccp = coreComponentAPI.createRandomBCCP(dt, endUser, namespaceEU, "WIP");
            bccpForTesting.add(bccp);
            coreComponentAPI.appendBCC(acc, bccp, "WIP");
            asccp = coreComponentAPI.createRandomASCCP(acc, endUser, namespaceEU, "WIP");
            asccpForTesting.add(asccp);
        }
        /**
         * login as end user to create UEGACC
         */
        ArrayList<ACCObject> userExtensions = new ArrayList<>();
        for (TopLevelASBIEPObject topLevelASBIEP : topLevelAsbiepASCCPMap.keySet()) {
            ASCCPObject asccp = topLevelAsbiepASCCPMap.get(topLevelASBIEP);
            AppUserObject owner = topLevelASBIEPOwnerMap.get(topLevelASBIEP);
            NamespaceObject namespace = userNamespaceMap.get(owner);
            HomePage homePage = loginPage().signIn(owner.getLoginId(), owner.getPassword());
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

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (ACCObject acc : accForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(acc.getOwnerUserId());
            assertFalse(owner.isDeveloper());
            assertNotEquals(developerA.getAppUserId(), owner.getAppUserId());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), release.getReleaseNumber());
            assertEquals("WIP", acc.getState());
            assertDisabled(accViewEditPage.getDefinitionField());
            assertDisabled(accViewEditPage.getDefinitionSourceField());
            assertDisabled(accViewEditPage.getObjectClassTermField());
            assertDisabled(accViewEditPage.getDENField());
            AddCommentDialog addCommentDialog = accViewEditPage.openCommentsDialog("/" + acc.getDen());
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }

        for (ACCObject accExtension : userExtensions) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(accExtension.getOwnerUserId());
            assertFalse(owner.isDeveloper());
            assertNotEquals(developerA.getAppUserId(), owner.getAppUserId());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(accExtension.getDen(), release.getReleaseNumber());
            assertEquals("WIP", accExtension.getState());
            assertDisabled(accViewEditPage.getDefinitionField());
            assertDisabled(accViewEditPage.getDefinitionSourceField());
            assertDisabled(accViewEditPage.getObjectClassTermField());
            assertDisabled(accViewEditPage.getDENField());
            AddCommentDialog addCommentDialog = accViewEditPage.openCommentsDialog("/" + accExtension.getDen());
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }

        for (BCCPObject bccp : bccpForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(bccp.getOwnerUserId());
            assertFalse(owner.isDeveloper());
            assertNotEquals(developerA.getAppUserId(), owner.getAppUserId());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByDenAndBranch(bccp.getDen(), release.getReleaseNumber());
            assertDisabled(bccpViewEditPage.getDefinitionField());
            assertDisabled(bccpViewEditPage.getDefinitionSourceField());
            assertDisabled(bccpViewEditPage.getDENField());
            assertDisabled(bccpViewEditPage.getPropertyTermField());
            assertEquals("WIP", bccp.getState());
            AddCommentDialog addCommentDialog = bccpViewEditPage.openCommentsDialog("/" + bccp.getPropertyTerm());
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }

        for (ASCCPObject asccp : asccpForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(asccp.getOwnerUserId());
            assertFalse(owner.isDeveloper());
            assertNotEquals(developerA.getAppUserId(), owner.getAppUserId());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch(asccp.getDen(), release.getReleaseNumber());
            assertDisabled(asccpViewEditPage.getDefinitionField());
            assertDisabled(asccpViewEditPage.getDefinitionSourceField());
            assertDisabled(asccpViewEditPage.getDENField());
            assertDisabled(asccpViewEditPage.getPropertyTermField());
            assertEquals("WIP", asccp.getState());
            AddCommentDialog addCommentDialog = asccpViewEditPage.openCommentsDialog("/" + asccp.getPropertyTerm());
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }
    }

    @Test
    @DisplayName("TC_13_1_TA_3")
    public void test_TA_3() {
        AppUserObject developerA;
        ReleaseObject release;
        ArrayList<ACCObject> accForTesting = new ArrayList<>();
        ArrayList<BCCPObject> bccpForTesting = new ArrayList<>();
        ArrayList<ASCCPObject> asccpForTesting = new ArrayList<>();
        Map<TopLevelASBIEPObject, ASCCPObject> topLevelAsbiepASCCPMap = new HashMap<>();
        Map<AppUserObject, NamespaceObject> userNamespaceMap = new HashMap<>();
        Map<TopLevelASBIEPObject, AppUserObject> topLevelASBIEPOwnerMap = new HashMap<>();
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);
            AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            userNamespaceMap.put(developerA, namespace);
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
            userNamespaceMap.put(endUser, namespaceEU);

            /**
             * Published Developer Core Components
             */
            ACCObject acc = coreComponentAPI.createRandomACC(developerB, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, developerB, namespace, "Published");
            DTObject dt = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            BCCPObject bccp = coreComponentAPI.createRandomBCCP(dt, developerB, namespace, "Published");
            coreComponentAPI.appendBCC(acc, bccp, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, developerB, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
            TopLevelASBIEPObject topLevelAsbiepEU = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, endUser, "WIP");
            topLevelAsbiepASCCPMap.put(topLevelAsbiepEU, asccp);
            topLevelASBIEPOwnerMap.put(topLevelAsbiepEU, endUser);

            /**
             * QA end-user Core Components
             */
            acc = coreComponentAPI.createRandomACC(endUser, release, namespaceEU, "QA");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, endUser, namespaceEU, "QA");
            bccp = coreComponentAPI.createRandomBCCP(dt, endUser, namespaceEU, "QA");
            bccpForTesting.add(bccp);
            coreComponentAPI.appendBCC(acc, bccp, "QA");
            asccp = coreComponentAPI.createRandomASCCP(acc, endUser, namespaceEU, "QA");
            asccpForTesting.add(asccp);

            /**
             * Deleted end-user Core Components
             */
            acc = coreComponentAPI.createRandomACC(endUser, release, namespaceEU, "Deleted");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, endUser, namespaceEU, "Deleted");
            bccp = coreComponentAPI.createRandomBCCP(dt, endUser, namespaceEU, "Deleted");
            bccpForTesting.add(bccp);
            coreComponentAPI.appendBCC(acc, bccp, "Deleted");
            asccp = coreComponentAPI.createRandomASCCP(acc, endUser, namespaceEU, "Deleted");
            asccpForTesting.add(asccp);

        }
        /**
         * login as end user to create UEGACC
         */
        ArrayList<ACCObject> userExtensions = new ArrayList<>();
        for (TopLevelASBIEPObject topLevelASBIEP : topLevelAsbiepASCCPMap.keySet()) {
            ASCCPObject asccp = topLevelAsbiepASCCPMap.get(topLevelASBIEP);
            AppUserObject owner = topLevelASBIEPOwnerMap.get(topLevelASBIEP);
            NamespaceObject namespace = userNamespaceMap.get(owner);
            HomePage homePage = loginPage().signIn(owner.getLoginId(), owner.getPassword());
            BIEMenu bieMenu = homePage.getBIEMenu();
            ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
            EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
            ACCExtensionViewEditPage accExtensionViewEditPage =
                    editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
            String den = accExtensionViewEditPage.getDENFieldValue();
            accExtensionViewEditPage.setNamespace(namespace);
            accExtensionViewEditPage.hitUpdateButton();
            accExtensionViewEditPage.moveToQA();
            ACCObject accExtension = getAPIFactory().getCoreComponentAPI().getACCByDENAndReleaseNum(den, release.getReleaseNumber());
            userExtensions.add(accExtension);
            homePage.logout();
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ArrayList<String> acceptedStates = new ArrayList<>(List.of("QA", "Deleted"));
        for (ACCObject acc : accForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(acc.getOwnerUserId());
            assertFalse(owner.isDeveloper());
            assertNotEquals(developerA.getAppUserId(), owner.getAppUserId());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), release.getReleaseNumber());
            assertTrue(acceptedStates.contains(acc.getState()));
            assertDisabled(accViewEditPage.getDefinitionField());
            assertDisabled(accViewEditPage.getDefinitionSourceField());
            assertDisabled(accViewEditPage.getObjectClassTermField());
            assertDisabled(accViewEditPage.getDENField());
            AddCommentDialog addCommentDialog = accViewEditPage.openCommentsDialog("/" + acc.getDen());
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }

        for (ACCObject accExtension : userExtensions) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(accExtension.getOwnerUserId());
            assertFalse(owner.isDeveloper());
            assertNotEquals(developerA.getAppUserId(), owner.getAppUserId());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(accExtension.getDen(), release.getReleaseNumber());
            assertTrue(acceptedStates.contains(accExtension.getState()));
            assertDisabled(accViewEditPage.getDefinitionField());
            assertDisabled(accViewEditPage.getDefinitionSourceField());
            assertDisabled(accViewEditPage.getObjectClassTermField());
            assertDisabled(accViewEditPage.getDENField());
            AddCommentDialog addCommentDialog = accViewEditPage.openCommentsDialog("/" + accExtension.getDen());
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }

        for (BCCPObject bccp : bccpForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(bccp.getOwnerUserId());
            assertFalse(owner.isDeveloper());
            assertNotEquals(developerA.getAppUserId(), owner.getAppUserId());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByDenAndBranch(bccp.getDen(), release.getReleaseNumber());
            assertDisabled(bccpViewEditPage.getDefinitionField());
            assertDisabled(bccpViewEditPage.getDefinitionSourceField());
            assertDisabled(bccpViewEditPage.getDENField());
            assertDisabled(bccpViewEditPage.getPropertyTermField());
            assertTrue(acceptedStates.contains(bccp.getState()));
            AddCommentDialog addCommentDialog = bccpViewEditPage.openCommentsDialog("/" + bccp.getPropertyTerm());
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }

        for (ASCCPObject asccp : asccpForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(asccp.getOwnerUserId());
            assertFalse(owner.isDeveloper());
            assertNotEquals(developerA.getAppUserId(), owner.getAppUserId());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch(asccp.getDen(), release.getReleaseNumber());
            assertDisabled(asccpViewEditPage.getDefinitionField());
            assertDisabled(asccpViewEditPage.getDefinitionSourceField());
            assertDisabled(asccpViewEditPage.getDENField());
            assertDisabled(asccpViewEditPage.getPropertyTermField());
            assertTrue(acceptedStates.contains(asccp.getState()));
            AddCommentDialog addCommentDialog = asccpViewEditPage.openCommentsDialog("/" + asccp.getPropertyTerm());
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }
        homePage.logout();
        userExtensions.clear();
        for (TopLevelASBIEPObject topLevelASBIEP : topLevelAsbiepASCCPMap.keySet()) {
            ASCCPObject asccp = topLevelAsbiepASCCPMap.get(topLevelASBIEP);
            AppUserObject owner = topLevelASBIEPOwnerMap.get(topLevelASBIEP);
            homePage = loginPage().signIn(owner.getLoginId(), owner.getPassword());
            BIEMenu bieMenu = homePage.getBIEMenu();
            ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
            EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
            ACCExtensionViewEditPage accExtensionViewEditPage =
                    editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
            String den = accExtensionViewEditPage.getDENFieldValue();
            accExtensionViewEditPage.backToWIP();
            accExtensionViewEditPage.hitDeleteButton();
            ACCObject accExtension = getAPIFactory().getCoreComponentAPI().getACCByDENAndReleaseNum(den, release.getReleaseNumber());
            userExtensions.add(accExtension);
            homePage.logout();
        }
        homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (ACCObject accExtension : userExtensions) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(accExtension.getOwnerUserId());
            assertFalse(owner.isDeveloper());
            assertNotEquals(developerA.getAppUserId(), owner.getAppUserId());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            invisibilityOfLoadingContainerElement(getDriver());
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(accExtension.getDen(), release.getReleaseNumber());
            assertTrue(acceptedStates.contains(accExtension.getState()));
            assertDisabled(accViewEditPage.getDefinitionField());
            assertDisabled(accViewEditPage.getDefinitionSourceField());
            assertDisabled(accViewEditPage.getObjectClassTermField());
            assertDisabled(accViewEditPage.getDENField());
            AddCommentDialog addCommentDialog = accViewEditPage.openCommentsDialog("/" + accExtension.getDen());
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }
    }

    @Test
    @DisplayName("TC_13_1_TA_4")
    public void test_TA_4() {
        AppUserObject developerA;
        ReleaseObject release;
        ArrayList<ACCObject> accForTesting = new ArrayList<>();
        ArrayList<BCCPObject> bccpForTesting = new ArrayList<>();
        ArrayList<ASCCPObject> asccpForTesting = new ArrayList<>();
        Map<TopLevelASBIEPObject, ASCCPObject> topLevelAsbiepASCCPMap = new HashMap<>();
        Map<AppUserObject, NamespaceObject> userNamespaceMap = new HashMap<>();
        Map<TopLevelASBIEPObject, AppUserObject> topLevelASBIEPOwnerMap = new HashMap<>();
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);
            AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            userNamespaceMap.put(developerA, namespace);
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
            userNamespaceMap.put(endUser, namespaceEU);

            /**
             * Published Developer Core Components
             */
            ACCObject acc = coreComponentAPI.createRandomACC(developerB, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, developerB, namespace, "Published");
            DTObject dt = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            BCCPObject bccp = coreComponentAPI.createRandomBCCP(dt, developerB, namespace, "Published");
            coreComponentAPI.appendBCC(acc, bccp, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, developerB, namespace, "Published");

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
            TopLevelASBIEPObject topLevelAsbiepEU = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, endUser, "WIP");
            topLevelAsbiepASCCPMap.put(topLevelAsbiepEU, asccp);
            topLevelASBIEPOwnerMap.put(topLevelAsbiepEU, endUser);
            /**
             * Production end-user Core Components
             */
            acc = coreComponentAPI.createRandomACC(endUser, release, namespaceEU, "Production");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, endUser, namespaceEU, "Production");
            bccp = coreComponentAPI.createRandomBCCP(dt, endUser, namespaceEU, "Production");
            bccpForTesting.add(bccp);
            coreComponentAPI.appendBCC(acc, bccp, "Production");
            asccp = coreComponentAPI.createRandomASCCP(acc, endUser, namespaceEU, "Production");
            asccpForTesting.add(asccp);

        }
        /**
         * login as end user to create UEGACC
         */
        ArrayList<ACCObject> userExtensions = new ArrayList<>();
        for (TopLevelASBIEPObject topLevelASBIEP : topLevelAsbiepASCCPMap.keySet()) {
            ASCCPObject asccp = topLevelAsbiepASCCPMap.get(topLevelASBIEP);
            AppUserObject owner = topLevelASBIEPOwnerMap.get(topLevelASBIEP);
            NamespaceObject namespace = userNamespaceMap.get(owner);
            HomePage homePage = loginPage().signIn(owner.getLoginId(), owner.getPassword());
            BIEMenu bieMenu = homePage.getBIEMenu();
            ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
            EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
            ACCExtensionViewEditPage accExtensionViewEditPage =
                    editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
            String den = accExtensionViewEditPage.getDENFieldValue();
            accExtensionViewEditPage.setNamespace(namespace);
            accExtensionViewEditPage.hitUpdateButton();
            accExtensionViewEditPage.moveToQA();
            accExtensionViewEditPage.moveToProduction();
            ACCObject accExtension = getAPIFactory().getCoreComponentAPI().getACCByDENAndReleaseNum(den, release.getReleaseNumber());
            userExtensions.add(accExtension);
            homePage.logout();
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (ACCObject acc : accForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(acc.getOwnerUserId());
            assertFalse(owner.isDeveloper());
            assertNotEquals(developerA.getAppUserId(), owner.getAppUserId());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), release.getReleaseNumber());
            assertEquals("Production", acc.getState());
            assertDisabled(accViewEditPage.getDefinitionField());
            assertDisabled(accViewEditPage.getDefinitionSourceField());
            assertDisabled(accViewEditPage.getObjectClassTermField());
            assertDisabled(accViewEditPage.getDENField());
            assertThrows(TimeoutException.class, () -> {
                accViewEditPage.hitAmendButton();
            });
            AddCommentDialog addCommentDialog = accViewEditPage.openCommentsDialog("/" + acc.getDen());
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }

        for (ACCObject accExtension : userExtensions) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(accExtension.getOwnerUserId());
            assertFalse(owner.isDeveloper());
            assertNotEquals(developerA.getAppUserId(), owner.getAppUserId());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(accExtension.getDen(), release.getReleaseNumber());
            assertEquals("Production", accExtension.getState());
            assertDisabled(accViewEditPage.getDefinitionField());
            assertDisabled(accViewEditPage.getDefinitionSourceField());
            assertDisabled(accViewEditPage.getObjectClassTermField());
            assertDisabled(accViewEditPage.getDENField());
            assertThrows(TimeoutException.class, () -> {
                accViewEditPage.hitAmendButton();
            });
            AddCommentDialog addCommentDialog = accViewEditPage.openCommentsDialog("/" + accExtension.getDen());
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }

        for (BCCPObject bccp : bccpForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(bccp.getOwnerUserId());
            assertFalse(owner.isDeveloper());
            assertNotEquals(developerA.getAppUserId(), owner.getAppUserId());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByDenAndBranch(bccp.getDen(), release.getReleaseNumber());
            assertDisabled(bccpViewEditPage.getDefinitionField());
            assertDisabled(bccpViewEditPage.getDefinitionSourceField());
            assertDisabled(bccpViewEditPage.getDENField());
            assertDisabled(bccpViewEditPage.getPropertyTermField());
            assertEquals("Production", bccp.getState());
            assertThrows(TimeoutException.class, () -> {
                bccpViewEditPage.hitAmendButton();
            });
            AddCommentDialog addCommentDialog = bccpViewEditPage.openCommentsDialog("/" + bccp.getPropertyTerm());
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }

        for (ASCCPObject asccp : asccpForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(asccp.getOwnerUserId());
            assertFalse(owner.isDeveloper());
            assertNotEquals(developerA.getAppUserId(), owner.getAppUserId());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch(asccp.getDen(), release.getReleaseNumber());
            assertDisabled(asccpViewEditPage.getDefinitionField());
            assertDisabled(asccpViewEditPage.getDefinitionSourceField());
            assertDisabled(asccpViewEditPage.getDENField());
            assertDisabled(asccpViewEditPage.getPropertyTermField());
            assertEquals("Production", asccp.getState());
            assertThrows(TimeoutException.class, () -> {
                asccpViewEditPage.hitAmendButton();
            });
            AddCommentDialog addCommentDialog = asccpViewEditPage.openCommentsDialog("/" + asccp.getPropertyTerm());
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }
    }

    @Test
    @DisplayName("TC_13_1_TA_5_and_TC_13_1_TA_5")
    public void test_TA_5_and_TA_6() {
        AppUserObject developerA;
        ReleaseObject release;
        ArrayList<ACCObject> accForTesting = new ArrayList<>();
        ArrayList<BCCPObject> bccpForTesting = new ArrayList<>();
        ArrayList<ASCCPObject> asccpForTesting = new ArrayList<>();

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);
            AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);


            /**
             * Published Developer Core Components
             */
            ACCObject acc = coreComponentAPI.createRandomACC(developerB, release, namespace, "Published");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, developerB, namespace, "Published");
            DTObject dt = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            BCCPObject bccp = coreComponentAPI.createRandomBCCP(dt, developerB, namespace, "Published");
            bccpForTesting.add(bccp);
            coreComponentAPI.appendBCC(acc, bccp, "Published");
            ASCCPObject asccp = coreComponentAPI.createRandomASCCP(acc, developerB, namespace, "Published");
            asccpForTesting.add(asccp);

            /**
             * Published end-user Core Components
             */
            acc = coreComponentAPI.createRandomACC(endUser, release, namespaceEU, "Published");
            accForTesting.add(acc);
            coreComponentAPI.appendExtension(acc, endUser, namespaceEU, "Published");
            bccp = coreComponentAPI.createRandomBCCP(dt, endUser, namespaceEU, "Published");
            bccpForTesting.add(bccp);
            coreComponentAPI.appendBCC(acc, bccp, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, endUser, namespaceEU, "Published");
            asccpForTesting.add(asccp);


        }
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (ACCObject acc : accForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(acc.getOwnerUserId());
            assertNotEquals(developerA.getAppUserId(), owner.getAppUserId());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), release.getReleaseNumber());
            assertEquals("Published", acc.getState());
            assertDisabled(accViewEditPage.getDefinitionField());
            assertDisabled(accViewEditPage.getDefinitionSourceField());
            assertDisabled(accViewEditPage.getObjectClassTermField());
            assertDisabled(accViewEditPage.getDENField());
            assertThrows(TimeoutException.class, () -> {
                accViewEditPage.hitReviseButton();
            });
            AddCommentDialog addCommentDialog = accViewEditPage.openCommentsDialog("/" + acc.getDen());
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }
        for (BCCPObject bccp : bccpForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(bccp.getOwnerUserId());
            assertNotEquals(developerA.getAppUserId(), owner.getAppUserId());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByDenAndBranch(bccp.getDen(), release.getReleaseNumber());
            assertDisabled(bccpViewEditPage.getDefinitionField());
            assertDisabled(bccpViewEditPage.getDefinitionSourceField());
            assertDisabled(bccpViewEditPage.getDENField());
            assertDisabled(bccpViewEditPage.getPropertyTermField());
            assertEquals("Published", bccp.getState());
            assertThrows(TimeoutException.class, () -> {
                bccpViewEditPage.hitReviseButton();
            });
            AddCommentDialog addCommentDialog = bccpViewEditPage.openCommentsDialog("/" + bccp.getPropertyTerm());
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }
        for (ASCCPObject asccp : asccpForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(asccp.getOwnerUserId());
            assertNotEquals(developerA.getAppUserId(), owner.getAppUserId());
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch(asccp.getDen(), release.getReleaseNumber());
            assertDisabled(asccpViewEditPage.getDefinitionField());
            assertDisabled(asccpViewEditPage.getDefinitionSourceField());
            assertDisabled(asccpViewEditPage.getDENField());
            assertDisabled(asccpViewEditPage.getPropertyTermField());
            assertEquals("Published", asccp.getState());
            assertThrows(TimeoutException.class, () -> {
                asccpViewEditPage.hitReviseButton();
            });
            AddCommentDialog addCommentDialog = asccpViewEditPage.openCommentsDialog("/" + asccp.getPropertyTerm());
            addCommentDialog.setComment("some comment");
            addCommentDialog.hitCloseButton();
        }
    }

    @Test
    @DisplayName("TC_13_1_TA_7")
    public void test_TA_7() {
        AppUserObject developerA;
        ReleaseObject release;

        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
        }
        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(release.getReleaseNumber());
        assertThrows(TimeoutException.class, () -> {
            viewEditCoreComponentPage.getCreateACCButton();
        });
        assertThrows(TimeoutException.class, () -> {
            viewEditCoreComponentPage.getCreateASCCPButton();
        });
        assertThrows(TimeoutException.class, () -> {
            viewEditCoreComponentPage.getCreateBCCPButton();
        });
        assertThrows(TimeoutException.class, () -> {
            viewEditCoreComponentPage.getCreateDTButton();
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
