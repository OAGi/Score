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
import org.oagi.score.e2e.page.core_component.ViewEditDataTypePage;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

@Execution(ExecutionMode.CONCURRENT)
public class TC_38_11_EditingExistingSupplementaryComponentsOfRevisionOfDeveloperDT extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_38_11_from_TA_1_to_TA_8")
    public void test_from_TA_1_to_TA_8() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        DTObject baseCDT;
        Map<DTObject, DTObject> derivedBDTs = new HashMap<>();
        CodeListObject codeList;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            baseCDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            DTObject randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseCDT, developerA, namespace, "Published");
            dtForTesting.add(randomBDT);

            DTObject derivedBDTLevelOne = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, randomBDT, developerA, namespace, "WIP");
            derivedBDTs.put(randomBDT, derivedBDTLevelOne);

            DTObject derivedBDTLevelTwo = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, derivedBDTLevelOne, developerA, namespace, "WIP");
            derivedBDTs.put(derivedBDTLevelOne, derivedBDTLevelTwo);

            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developerA, namespace, branch, "WIP");
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        for (DTObject dt : dtForTesting) {
            DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
            dtViewEditPage.hitReviseButton();
            dtViewEditPage.addSupplementaryComponent("/" + dt.getDen());
            waitFor(Duration.ofMillis(3000L));
            DTObject revisedDT = getAPIFactory().getCoreComponentAPI().getRevisedDT(dt);
            DTSCObject dtSC = getAPIFactory().getCoreComponentAPI().getNewlyCreatedSCForDT(revisedDT.getDtId(), branch.getReleaseNumber());
            String dtSCName = dtSC.getObjectClassTerm() + ". " + dtSC.getPropertyTerm() + ". " + dtSC.getRepresentationTerm();

            WebElement supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + revisedDT.getDen() + "/" + dtSCName);
            assertTrue(supplementaryComponentNode.isDisplayed());
            DTViewEditPage.SupplementaryComponentPanel SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);
            dtViewEditPage.showValueDomain();
            List<String> representationTermsForCDTs = getAPIFactory().getCoreComponentAPI().getRepresentationTermsForCDTs(branch.getReleaseNumber());
            String representationTerm = SCPanel.getRepresentationSelectFieldValue();
            List<String> valueDomains = getAPIFactory().getCoreComponentAPI().getValueDomainsByCDTRepresentationTerm(representationTerm);
            String defaultValueDomain = getAPIFactory().getCoreComponentAPI().getDefaultValueDomainByCDTRepresentationTerm(representationTerm);
            if (!valueDomains.contains("Token")) {
                for (String representationTermNew : representationTermsForCDTs) {
                    valueDomains = getAPIFactory().getCoreComponentAPI().getValueDomainsByCDTRepresentationTerm(representationTermNew);
                    defaultValueDomain = getAPIFactory().getCoreComponentAPI().getDefaultValueDomainByCDTRepresentationTerm(representationTermNew);
                    if (valueDomains.contains("Token")) {
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
            homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
            viewEditDataTypePage.openDTViewEditPageByDenAndBranch(derivedDTLevelOne.getDen(), branch.getReleaseNumber());
            supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + derivedDTLevelOne.getDen() + "/" + dtSCName);
            assertTrue(supplementaryComponentNode.isDisplayed());
            SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);
            /**
             * Test Assertion #38.7.1
             */
            assertDisabled(SCPanel.getPropertyTermField());
            /**
             * Test Assertion #38.7.2
             */
            assertDisabled(SCPanel.getRepresentationSelectField());
            /**
             * Test Assertion #38.7.3
             */
            dtViewEditPage.showValueDomain();
            String randomValueDomain = valueDomains.get(valueDomains.size() - 1);
            assertThrows(Exception.class, () -> dtViewEditPage.selectValueDomain(randomValueDomain));
            assertThrows(Exception.class, () -> dtViewEditPage.getDiscardValueDomainButton());
            /**
             * Test Assertion #38.7.4
             */
            if (valueDomains.contains("Token")) {
                assertDoesNotThrow(() -> dtViewEditPage.addCodeListValueDomain(codeList.getName()));
                dtViewEditPage.selectValueDomain(codeList.getName());
                dtViewEditPage.discardValueDomain();
                dtViewEditPage.addCodeListValueDomain(codeList.getName());
            }

            /**
             * Test Assertion #38.7.5
             */
            for (String valueDomain : valueDomains) {
                if (!valueDomain.equals(defaultValueDomain)) {
                    SCPanel.setDefaultValueDomain(valueDomain);
                    break;
                }
            }
            /**
             * Test Assertion #38.7.6
             */
            if (SCPanel.getCardinalityFieldValue().equals("Optional")) {
                SCPanel.setCardinality("Required");
                SCPanel.setCardinality("Optional");
                SCPanel.setCardinality("Required");
            }
            /**
             * Test Assertion #38.7.7
             */
            SCPanel.setValueConstraintType("Default Value");
            SCPanel.setValueConstraint("Default value");
            String derivedDTLevelOneDefinition = "Derived DT SC level One definition";
            String derivedDTLevelOneDefinitionSource = "Derived DT level SC One definition source";
            SCPanel.setDefinition(derivedDTLevelOneDefinition);
            SCPanel.setDefinitionSource(derivedDTLevelOneDefinitionSource);
            dtViewEditPage.hitUpdateButton();
            /**
             * Test Assertion #38.7.8
             */

            DTObject derivedDTLevelTwo = derivedBDTs.get(derivedDTLevelOne);
            homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
            viewEditDataTypePage.openDTViewEditPageByDenAndBranch(derivedDTLevelTwo.getDen(), branch.getReleaseNumber());
            supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + derivedDTLevelTwo.getDen() + "/" + dtSCName);
            assertTrue(supplementaryComponentNode.isDisplayed());
            SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);
            assertTrue(SCPanel.getDefinitionFieldValue().equals(derivedDTLevelOneDefinition));
            assertTrue(SCPanel.getDefinitionSourceFieldValue().equals(derivedDTLevelOneDefinitionSource));
            dtViewEditPage.showValueDomain();
            assertDoesNotThrow(() -> dtViewEditPage.getTableRecordByValue(codeList.getName()));
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
