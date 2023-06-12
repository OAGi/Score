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
import org.oagi.score.e2e.page.core_component.DTViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

@Execution(ExecutionMode.CONCURRENT)
public class TC_41_6_EditingBrandNewSC extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_41_6_TA_1")
    public void test_TA_1() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        DTObject baseCDT;
        CodeListObject codeList;
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.8");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA);

            baseCDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Code. Type", branch.getReleaseNumber());
            DTObject randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseCDT, endUserA, namespace, "WIP");
            dtForTesting.add(randomBDT);

            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserA, namespace, branch, "WIP");
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
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

            /**
             * Test Assertion #41.6.1.a
             */
            SCPanel.setPropertyTerm("newSC");
            click(dtViewEditPage.getUpdateButton(true));
            dtViewEditPage.hitUpdateAnywayButton();
            dtSC = getAPIFactory().getCoreComponentAPI().getNewlyCreatedSCForDT(dt.getDtId(), branch.getReleaseNumber());
            assertEquals("true", SCPanel.getPropertyTermField().getAttribute("aria-required"));
            assertTrue(getAPIFactory().getCoreComponentAPI().SCPropertyTermIsUnique(dt, branch.getReleaseNumber(),
                    dtSC.getObjectClassTerm(), dtSC.getRepresentationTerm(), dtSC.getPropertyTerm()));
            /**
             * Test Assertion #41.6.1.b
             */
            assertEquals("true", SCPanel.getRepresentationSelectField().getAttribute("aria-required"));
            SCPanel.setCardinality("Required");
            SCPanel.setValueConstraintType("Fixed Value");
            SCPanel.setValueConstraint("fixed value");
            click(dtViewEditPage.getUpdateButton(true));
            dtViewEditPage.hitUpdateAnywayButton();
            SCPanel.showValueDomain();
            List<String> representationTermsForCDTs = getAPIFactory().getCoreComponentAPI().getRepresentationTermsForCDTs(branch.getReleaseNumber());
            String representationTerm = representationTermsForCDTs.get(representationTermsForCDTs.size()-1);
            SCPanel.selectRepresentationTerm(representationTerm);
            assertEquals(null, SCPanel.getValueConstraintFieldValue());
            /**
             * Test Assertion #41.6.1.c
             */
            assertTrue(List.of("Optional", "Required").contains(SCPanel.getCardinalityFieldValue()));
            /**
             * Test Assertion #41.6.1.d
             */
            SCPanel.setValueConstraintType("Fixed Value");
            assertEquals("false", SCPanel.getValueConstraintTypeField().getAttribute("aria-required"));
            /**
             * Test Assertion #41.6.1.e
             */
            List<String> valueDomains = getAPIFactory().getCoreComponentAPI().getValueDomainsByCDTRepresentationTerm(representationTerm);
            String defaultValueDomain = getAPIFactory().getCoreComponentAPI().getDefaultValueDomainByCDTRepresentationTerm(representationTerm);
            if (valueDomains.contains("Token")){
                assertDoesNotThrow(() -> dtViewEditPage.addCodeListValueDomain(codeList.getName()));
                dtViewEditPage.selectValueDomain(codeList.getName());
                dtViewEditPage.discardValueDomain();
            } else{
                assertThrows(TimeoutException.class, () -> dtViewEditPage.addCodeListValueDomain(codeList.getName()));
                for (String representationTermNew: representationTermsForCDTs){
                    valueDomains = getAPIFactory().getCoreComponentAPI().getValueDomainsByCDTRepresentationTerm(representationTermNew);
                    defaultValueDomain = getAPIFactory().getCoreComponentAPI().getDefaultValueDomainByCDTRepresentationTerm(representationTermNew);
                    if (valueDomains.contains("Token")){
                        SCPanel.selectRepresentationTerm(representationTermNew);
                        break;
                    }
                }
                assertDoesNotThrow(() -> dtViewEditPage.addCodeListValueDomain(codeList.getName()));
                dtViewEditPage.selectValueDomain(codeList.getName());
                dtViewEditPage.discardValueDomain();
            }
            /**
             * Test Assertion #41.6.1.f
             */

            for (String valueDomain: valueDomains){
                if (!valueDomain.equals(defaultValueDomain)){
                    SCPanel.setDefaultValueDomain(valueDomain);
                    break;
                }
            }
            /**
             * Test Assertion #41.6.1.g
             */
            assertEquals("false", SCPanel.getDefinitionField().getAttribute("aria-required"));
            assertEquals("false", SCPanel.getDefinitionSourceField().getAttribute("aria-required"));
            SCPanel.setDefinition("");
            SCPanel.setDefinitionSource("new definition source");
            if (SCPanel.getDefinitionFieldValue() == null){
                click(dtViewEditPage.getUpdateButton(true));
                assertEquals("Are you sure you want to update this without definitions?",
                        dtViewEditPage.getDefinitionWarningDialogMessage());
                dtViewEditPage.hitUpdateAnywayButton();
            }

        }
    }
    @Test
    @DisplayName("TC_41_6_TA_2")
    public void test_TA_2() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        DTObject baseCDT;
        ArrayList<DTObject> derivedBDTs = new ArrayList<>();
        CodeListObject codeList;
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.8");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA);

            baseCDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum("Code. Type", branch.getReleaseNumber());
            DTObject randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseCDT, endUserA, namespace, "WIP");
            dtForTesting.add(randomBDT);

            DTObject derivedBDTLevelOne = getAPIFactory().getCoreComponentAPI().createRandomBDT(randomBDT, endUserA, namespace, "WIP");
            derivedBDTs.add(derivedBDTLevelOne);

            DTObject derivedBDTLevelTwo = getAPIFactory().getCoreComponentAPI().createRandomBDT(derivedBDTLevelOne, endUserA, namespace, "WIP");
            derivedBDTs.add(derivedBDTLevelTwo);

            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserA, namespace, branch, "WIP");
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        for (DTObject dt : dtForTesting) {
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            dtViewEditPage.addSupplementaryComponent("/" + dt.getDen());
            waitFor(Duration.ofMillis(3000L));
            DTSCObject dtSC = getAPIFactory().getCoreComponentAPI().getNewlyCreatedSCForDT(dt.getDtId(), branch.getReleaseNumber());
            String dtSCName = dtSC.getObjectClassTerm() + ". " + dtSC.getPropertyTerm() + ". " + dtSC.getRepresentationTerm();

            WebElement supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + dt.getDen() + "/" + dtSCName);
            assertTrue(supplementaryComponentNode.isDisplayed());
            DTViewEditPage.SupplementaryComponentPanel SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);
            dtViewEditPage.showValueDomain();
            List<String> representationTermsForCDTs = getAPIFactory().getCoreComponentAPI().getRepresentationTermsForCDTs(branch.getReleaseNumber());
            String representationTerm = SCPanel.getRepresentationSelectFieldValue();
            List<String> valueDomains = getAPIFactory().getCoreComponentAPI().getValueDomainsByCDTRepresentationTerm(representationTerm);
            if (valueDomains.contains("Token")){
                dtViewEditPage.addCodeListValueDomain(codeList.getName());
            } else{
                for (String representationTermNew: representationTermsForCDTs){
                    valueDomains = getAPIFactory().getCoreComponentAPI().getValueDomainsByCDTRepresentationTerm(representationTermNew);
                    if (valueDomains.contains("Token")){
                        SCPanel.selectRepresentationTerm(representationTermNew);
                        String propertyTerm = SCPanel.getPropertyTermFieldValue();
                        representationTerm = SCPanel.getRepresentationSelectFieldValue();
                        dtSCName = dtSC.getObjectClassTerm() + ". " + propertyTerm + ". " + representationTerm;
                        break;
                    }
                }
                dtViewEditPage.addCodeListValueDomain(codeList.getName());
            }
            String definition = "SC new definition";
            String definitionSource = "SC new definition source";
            SCPanel.setDefinition(definition);
            SCPanel.setDefinitionSource(definitionSource);
            SCPanel.setCardinality("Required");
            SCPanel.setValueConstraintType("Fixed Value");
            SCPanel.setValueConstraint("fixed value");
            dtViewEditPage.hitUpdateButton();

            for (DTObject derivedDT : derivedBDTs) {
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
                viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(derivedDT.getDen(), branch.getReleaseNumber());
                supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + derivedDT.getDen() + "/" + dtSCName);
                assertTrue(supplementaryComponentNode.isDisplayed());
                SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);
                assertTrue(SCPanel.getDefinitionFieldValue().equals(definition));
                assertTrue(SCPanel.getDefinitionSourceFieldValue().equals(definitionSource));
                /**
                 * “Restrictions applied to this SC in all DT derived from this DT will be lost”
                 */
                assertFalse(SCPanel.getCardinalityFieldValue().equals("Required"));
                assertFalse(SCPanel.getValueConstraintTypeFieldValue().equals("Fixed Value"));
                assertEquals(null, SCPanel.getValueConstraintFieldValue());
                dtViewEditPage.showValueDomain();
                assertDoesNotThrow(() -> dtViewEditPage.getTableRecordByValue(codeList.getName()));
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
