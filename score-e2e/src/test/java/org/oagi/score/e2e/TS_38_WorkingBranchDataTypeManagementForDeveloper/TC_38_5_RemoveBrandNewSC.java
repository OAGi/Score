package org.oagi.score.e2e.TS_38_WorkingBranchDataTypeManagementForDeveloper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.DTViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

@Execution(ExecutionMode.CONCURRENT)
public class TC_38_5_RemoveBrandNewSC extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_38_5_TA_1")
    public void test_TA_1() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        DTObject baseCDT;
        ArrayList<DTObject> derivedBDTs = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            baseCDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Code. Type", branch.getReleaseNumber());
            DTObject randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseCDT, developerA, namespace, "WIP");
            dtForTesting.add(randomBDT);

            DTObject derivedBDTLevelOne = getAPIFactory().getCoreComponentAPI().createRandomBDT(randomBDT, developerA, namespace, "WIP");
            derivedBDTs.add(derivedBDTLevelOne);

            DTObject derivedBDTLevelTwo = getAPIFactory().getCoreComponentAPI().createRandomBDT(derivedBDTLevelOne, developerA, namespace, "WIP");
            derivedBDTs.add(derivedBDTLevelTwo);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        for (DTObject dt : dtForTesting) {
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            dtViewEditPage.addSupplementaryComponent("/" + dt.getDen());
            DTSCObject dtSC = getAPIFactory().getCoreComponentAPI().getNewlyCreatedSCForDT(dt.getDtId(), branch.getReleaseNumber());
            String dtSCName = dtSC.getObjectClassTerm() + ". " + dtSC.getPropertyTerm() + ". " +dtSC.getRepresentationTerm();
            waitFor(Duration.ofMillis(3000L));
            WebElement supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + dt.getDen() + "/" + dtSCName);
            assertTrue(supplementaryComponentNode.isDisplayed());
            DTViewEditPage.SupplementaryComponentPanel SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);
            assertDoesNotThrow(() -> dtViewEditPage.removeSupplementaryComponent("/" + dt.getDen() + "/" + dtSCName));

        }
    }

    @Test
    @DisplayName("TC_38_5_TA_2")
    public void test_TA_2() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        DTObject baseCDT;
        ArrayList<DTObject> derivedBDTs = new ArrayList<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            baseCDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Code. Type", branch.getReleaseNumber());
            DTObject randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseCDT, developerA, namespace, "WIP");
            dtForTesting.add(randomBDT);

            DTObject derivedBDTLevelOne = getAPIFactory().getCoreComponentAPI().createRandomBDT(randomBDT, developerA, namespace, "WIP");
            derivedBDTs.add(derivedBDTLevelOne);

            DTObject derivedBDTLevelTwo = getAPIFactory().getCoreComponentAPI().createRandomBDT(derivedBDTLevelOne, developerA, namespace, "WIP");
            derivedBDTs.add(derivedBDTLevelTwo);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        for (DTObject dt : dtForTesting) {
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            dtViewEditPage.addSupplementaryComponent("/" + dt.getDen());
            DTSCObject dtSC = getAPIFactory().getCoreComponentAPI().getNewlyCreatedSCForDT(dt.getDtId(), branch.getReleaseNumber());
            String dtSCName = dtSC.getObjectClassTerm() + ". " + dtSC.getPropertyTerm() + ". " +dtSC.getRepresentationTerm();
            waitFor(Duration.ofMillis(3000L));
            WebElement supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + dt.getDen() + "/" + dtSCName);
            assertTrue(supplementaryComponentNode.isDisplayed());
            DTViewEditPage.SupplementaryComponentPanel SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);
            SCPanel.setDefinition("some definition");
            dtViewEditPage.hitUpdateButton();

            dtViewEditPage.removeSupplementaryComponent("/" + dt.getDen() + "/" + dtSCName);
            for(DTObject derivedDT: derivedBDTs){
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
                viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(derivedDT.getDen(), branch.getReleaseNumber());
                assertThrows(TimeoutException.class, () -> dtViewEditPage.getNodeByPath("/" + derivedDT.getDen() + "/" + dtSCName));
            }

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
