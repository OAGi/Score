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
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

@Execution(ExecutionMode.CONCURRENT)
public class TC_38_4_AddBrandNewSC extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_38_4_TA_1")
    public void test_TA_1() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        DTObject baseCDT;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            baseCDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Code. Type", branch.getReleaseNumber());
            DTObject randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseCDT, developerA, namespace, "WIP");
            dtForTesting.add(randomBDT);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        for (DTObject dt : dtForTesting) {
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            assertDoesNotThrow(() -> dtViewEditPage.addSupplementaryComponent("/" + dt.getDen()));
        }

    }
    @Test
    @DisplayName("TC_38_4_TA_2")
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
            /**
             * Test Assertion #38.4.2.a
             */
            assertTrue(SCPanel.getObjectClassTermFieldValue().equals(baseCDT.getDataTypeTerm()));
            assertDisabled(SCPanel.getObjectClassTermField());
            /**
             * Test Assertion #38.4.2.b
             */
            String propertyTerm = "Property Term";
            assertTrue(SCPanel.getPropertyTermFieldValue().contains(propertyTerm));
            assertEquals("true", SCPanel.getPropertyTermField().getAttribute("aria-required"));
            assertTrue(getAPIFactory().getCoreComponentAPI().SCPropertyTermIsUnique(dt, branch.getReleaseNumber(),
                    dtSC.getObjectClassTerm(), dtSC.getRepresentationTerm(), dtSC.getPropertyTerm()));
            for(DTObject derivedDT: derivedBDTs){
                assertTrue(getAPIFactory().getCoreComponentAPI().SCPropertyTermIsUnique(derivedDT, branch.getReleaseNumber(),
                        dtSC.getObjectClassTerm(), dtSC.getRepresentationTerm(), dtSC.getPropertyTerm()));
            }

            /**
             * Test Assertion #38.4.2.c
             */
            assertEquals("true", SCPanel.getRepresentationSelectField().getAttribute("aria-required"));
            List<String> representationTermsForCDTs = getAPIFactory().getCoreComponentAPI().getRepresentationTermsForCDTs(branch.getReleaseNumber());
            SCPanel.showValueDomain();
            for (String representationTerm: representationTermsForCDTs){
                    assertDoesNotThrow(() -> SCPanel.selectRepresentationTerm(representationTerm));

                    /**
                     * Test Assertion #38.4.2.f
                     */
                    List<String> valueDomains = getAPIFactory().getCoreComponentAPI().getValueDomainsByCDTRepresentationTerm(representationTerm);
                    for (String valueDomain: valueDomains){
                        assertDoesNotThrow(() -> SCPanel.getTableRecordByValue(valueDomain));
                    }
                    /**
                     * Test Assertion #38.4.2.g
                     */
                    waitFor(Duration.ofMillis(3000L));
                    String defaultValueDomain = getAPIFactory().getCoreComponentAPI().getDefaultValueDomainByCDTRepresentationTerm(representationTerm);
                    assertTrue(SCPanel.getDefaultValueDomainFieldValue().contains(defaultValueDomain));
            }

            /**
             * Test Assertion #38.4.2.d
             */
            assertTrue(SCPanel.getCardinalityFieldValue().equals("Optional"));

            /**
             * Test Assertion #38.4.2.e
             */
            assertEquals("None", SCPanel.getValueConstraintTypeFieldValue());

            /**
             * Test Assertion #38.4.2.h
             */
            assertEquals(null, SCPanel.getDefinitionFieldValue());
            assertEquals(null, SCPanel.getDefinitionSourceFieldValue());
            assertEquals("false", SCPanel.getDefinitionField().getAttribute("aria-required"));
            assertEquals("false", SCPanel.getDefinitionSourceField().getAttribute("aria-required"));
            SCPanel.setDefinition("");
            click(dtViewEditPage.getUpdateButton(true));
            assertEquals("Are you sure you want to update this without definitions?",
                    dtViewEditPage.getDefinitionWarningDialogMessage());
            dtViewEditPage.hitUpdateAnywayButton();
        }

    }

    @Test
    @DisplayName("TC_38_4_TA_3")
    public void test_TA_3() {
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
            waitFor(Duration.ofMillis(3000L));
            DTSCObject dtSC = getAPIFactory().getCoreComponentAPI().getNewlyCreatedSCForDT(dt.getDtId(), branch.getReleaseNumber());
            String dtSCName = dtSC.getObjectClassTerm() + ". " + dtSC.getPropertyTerm() + ". " +dtSC.getRepresentationTerm();
            WebElement supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + dt.getDen() + "/" + dtSCName);
            assertTrue(supplementaryComponentNode.isDisplayed());
            DTViewEditPage.SupplementaryComponentPanel SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);

            for(DTObject derivedDT: derivedBDTs){
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
                viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(derivedDT.getDen(), branch.getReleaseNumber());
                assertDoesNotThrow(() -> dtViewEditPage.getNodeByPath("/" + derivedDT.getDen() + "/" + dtSCName));
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
