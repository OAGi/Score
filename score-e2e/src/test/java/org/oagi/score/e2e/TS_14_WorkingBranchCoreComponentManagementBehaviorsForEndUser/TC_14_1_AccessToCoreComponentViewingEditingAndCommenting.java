package org.oagi.score.e2e.TS_14_WorkingBranchCoreComponentManagementBehaviorsForEndUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


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

    @AfterEach
    public void tearDown() {
        super.tearDown();
        // Delete random accounts
        this.randomAccounts.forEach(newUser -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId());
        });
    }
}
