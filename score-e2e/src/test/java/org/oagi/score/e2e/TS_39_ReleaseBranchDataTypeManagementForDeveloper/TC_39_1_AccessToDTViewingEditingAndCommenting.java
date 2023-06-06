package org.oagi.score.e2e.TS_39_ReleaseBranchDataTypeManagementForDeveloper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.DTObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_39_1_AccessToDTViewingEditingAndCommenting extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_39_1_TA_1")
    public void test_TA_1() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            AppUserObject endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");
            NamespaceObject namespaceDeveloper = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA);

            DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Numeric. Type", branch.getReleaseNumber());
            DTObject dtDevPublished = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseDT, developerB, namespaceDeveloper, "Published");
            dtForTesting.add(dtDevPublished);

            DTObject dtEUWIP = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseDT, endUserA, namespaceEU, "WIP");
            dtForTesting.add(dtEUWIP);

            DTObject dtEUQA = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseDT, endUserA, namespaceEU, "QA");
            dtForTesting.add(dtEUQA);

            DTObject dtEUProduction = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseDT, endUserA, namespaceEU, "Production");
            dtForTesting.add(dtEUProduction);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        for (DTObject dt : dtForTesting) {
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(dt.getOwnerUserId());
            if (owner.isDeveloper()){
                assertTrue(dt.getState().equals("Published"));
            }
            viewEditCoreComponentPage.setDEN(dt.getDen());
            viewEditCoreComponentPage.hitSearchButton();
            assertDoesNotThrow(() -> viewEditCoreComponentPage.getTableRecordByValue(dt.getDen()));
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
