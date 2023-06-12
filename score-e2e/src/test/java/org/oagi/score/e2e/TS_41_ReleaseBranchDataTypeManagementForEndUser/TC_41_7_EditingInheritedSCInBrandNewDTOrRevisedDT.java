package org.oagi.score.e2e.TS_41_ReleaseBranchDataTypeManagementForEndUser;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

@Execution(ExecutionMode.CONCURRENT)
public class TC_41_7_EditingInheritedSCInBrandNewDTOrRevisedDT extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_41_7_from_TA_1_to_TA_8")
    public void test_from_TA_1_to_TA_8() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        DTObject baseCDT;
        Map<DTObject, DTObject> derivedBDTs = new HashMap<>();
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
            derivedBDTs.put(randomBDT, derivedBDTLevelOne);

            DTObject derivedBDTLevelTwo = getAPIFactory().getCoreComponentAPI().createRandomBDT(derivedBDTLevelOne, endUserA, namespace, "WIP");
            derivedBDTs.put(derivedBDTLevelOne, derivedBDTLevelTwo);

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
            String defaultValueDomain = getAPIFactory().getCoreComponentAPI().getDefaultValueDomainByCDTRepresentationTerm(representationTerm);
            if (!valueDomains.contains("Token")){
                for (String representationTermNew: representationTermsForCDTs){
                    valueDomains = getAPIFactory().getCoreComponentAPI().getValueDomainsByCDTRepresentationTerm(representationTermNew);
                    defaultValueDomain = getAPIFactory().getCoreComponentAPI().getDefaultValueDomainByCDTRepresentationTerm(representationTermNew);
                    if (valueDomains.contains("Token")){
                        SCPanel.selectRepresentationTerm(representationTermNew);
                        String propertyTerm = SCPanel.getPropertyTermFieldValue();
                        representationTerm = SCPanel.getRepresentationSelectFieldValue();
                        dtSCName = dtSC.getObjectClassTerm() + ". " + propertyTerm + ". " + representationTerm;
                        break;
                    }
                }
            }
            String definition = "SC new definition";
            String definitionSource = "SC new definition source";
            SCPanel.setDefinition(definition);
            SCPanel.setDefinitionSource(definitionSource);
            SCPanel.setCardinality("Optional");
            SCPanel.setValueConstraintType("Fixed Value");
            SCPanel.setValueConstraint("fixed value");
            dtViewEditPage.hitUpdateButton();

            DTObject derivedDTLevelOne = derivedBDTs.get(dt);
            homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(derivedDTLevelOne.getDen(), branch.getReleaseNumber());
            supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + derivedDTLevelOne.getDen() + "/" + dtSCName);
            assertTrue(supplementaryComponentNode.isDisplayed());
            SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);
            /**
             * Test Assertion #41.7.1
             */
            assertDisabled(SCPanel.getPropertyTermField());
            /**
             * Test Assertion #41.7.2
             */
            assertDisabled(SCPanel.getRepresentationSelectField());
            /**
             * Test Assertion #41.7.3
             */
            dtViewEditPage.showValueDomain();
            String randomValueDomain = valueDomains.get(valueDomains.size()-1);
            dtViewEditPage.selectValueDomain(randomValueDomain);
            assertThrows(Exception.class, () -> dtViewEditPage.getDiscardValueDomainButton());
            /**
             * Test Assertion #41.7.4
             */
            if (valueDomains.contains("Token")){
                assertDoesNotThrow(() -> dtViewEditPage.addCodeListValueDomain(codeList.getName()));
                dtViewEditPage.selectValueDomain(codeList.getName());
                dtViewEditPage.discardValueDomain();
                dtViewEditPage.addCodeListValueDomain(codeList.getName());
            }

            /**
             * Test Assertion #41.7.5
             */
            for (String valueDomain: valueDomains){
                if (!valueDomain.equals(defaultValueDomain)){
                    SCPanel.setDefaultValueDomain(valueDomain);
                    break;
                }
            }
            /**
             * Test Assertion #41.7.6
             */
            if (SCPanel.getCardinalityFieldValue().equals("Optional")){
                SCPanel.setCardinality("Required");
                SCPanel.setCardinality("Optional");
                SCPanel.setCardinality("Required");
            }
            /**
             * Test Assertion #41.7.7
             */
            SCPanel.setValueConstraintType("Default Value");
            SCPanel.setValueConstraint("Default value");
            String derivedDTLevelOneDefinition = "Derived DT SC level One definition";
            String derivedDTLevelOneDefinitionSource = "Derived DT level SC One definition source";
            SCPanel.setDefinition(derivedDTLevelOneDefinition);
            SCPanel.setDefinitionSource(derivedDTLevelOneDefinitionSource);
            dtViewEditPage.hitUpdateButton();
            /**
             * Test Assertion #41.7.8
             */

            DTObject derivedDTLevelTwo = derivedBDTs.get(derivedDTLevelOne);
            homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(derivedDTLevelTwo.getDen(), branch.getReleaseNumber());
            supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + derivedDTLevelTwo.getDen() + "/" + dtSCName);
            assertTrue(supplementaryComponentNode.isDisplayed());
            SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);
            assertTrue(SCPanel.getDefinitionFieldValue().equals(derivedDTLevelOneDefinition));
            assertTrue(SCPanel.getDefinitionSourceFieldValue().equals(derivedDTLevelOneDefinitionSource));
            /**
             * Restrictions applied to this SC in all DT derived from this DT will be lost
             */
            assertFalse(SCPanel.getCardinalityFieldValue().equals("Required"));
            assertFalse(SCPanel.getValueConstraintTypeFieldValue().equals("Default Value"));
            assertEquals(null, SCPanel.getValueConstraintFieldValue());
            dtViewEditPage.showValueDomain();
            assertDoesNotThrow(() -> dtViewEditPage.getTableRecordByValue(codeList.getName()));
        }
    }
}
