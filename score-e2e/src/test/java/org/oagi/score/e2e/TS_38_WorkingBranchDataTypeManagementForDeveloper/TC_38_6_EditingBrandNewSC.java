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
import org.oagi.score.e2e.page.core_component.ViewEditDataTypePage;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;


@Execution(ExecutionMode.CONCURRENT)
public class TC_38_6_EditingBrandNewSC extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_38_6_TA_1")
    public void test_TA_1() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        DTObject baseCDT;
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

            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developerA, namespace, branch, "WIP");
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        for (DTObject dt : dtForTesting) {
            DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
            dtViewEditPage.hitReviseButton();
            DTObject revisedDT = getAPIFactory().getCoreComponentAPI().getRevisedDT(dt);
            dtViewEditPage.addSupplementaryComponent("/" + revisedDT.getDen());
            waitFor(Duration.ofMillis(3000L));
            DTSCObject dtSC = getAPIFactory().getCoreComponentAPI().getNewlyCreatedSCForDT(revisedDT.getDtId(), branch.getReleaseNumber());
            String dtSCName = dtSC.getObjectClassTerm() + ". " + dtSC.getPropertyTerm() + ". " +dtSC.getRepresentationTerm();
            WebElement supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + revisedDT.getDen() + "/" + dtSCName);
            assertTrue(supplementaryComponentNode.isDisplayed());
            DTViewEditPage.SupplementaryComponentPanel SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);

            /**
             * Test Assertion #38.6.1.a
             */
            SCPanel.setPropertyTerm("newSC");
            click(dtViewEditPage.getUpdateButton(true));
            dtViewEditPage.hitUpdateAnywayButton();
            revisedDT = getAPIFactory().getCoreComponentAPI().getRevisedDT(dt);
            dtSC = getAPIFactory().getCoreComponentAPI().getNewlyCreatedSCForDT(revisedDT.getDtId(), branch.getReleaseNumber());
            assertEquals("true", SCPanel.getPropertyTermField().getAttribute("aria-required"));
            assertTrue(getAPIFactory().getCoreComponentAPI().SCPropertyTermIsUnique(revisedDT, branch.getReleaseNumber(),
                    dtSC.getObjectClassTerm(), dtSC.getRepresentationTerm(), dtSC.getPropertyTerm()));
            /**
             * Test Assertion #38.6.1.b
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
             * Test Assertion #38.6.1.c
             */
            assertTrue(List.of("Optional", "Required").contains(SCPanel.getCardinalityFieldValue()));
            /**
             * Test Assertion #38.6.1.d
             */
            SCPanel.setValueConstraintType("Fixed Value");
            assertEquals("false", SCPanel.getValueConstraintTypeField().getAttribute("aria-required"));
            /**
             * Test Assertion #38.6.1.e
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
             * Test Assertion #38.6.1.f
             */

            for (String valueDomain: valueDomains){
                if (!valueDomain.equals(defaultValueDomain)){
                    SCPanel.setDefaultValueDomain(valueDomain);
                    break;
                }
            }
            /**
             * Test Assertion #38.6.1.g
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
    @DisplayName("TC_38_6_TA_2")
    public void test_TA_2() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        DTObject baseCDT;
        ArrayList<DTObject> derivedBDTs = new ArrayList<>();
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

            DTObject randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseCDT, developerA, namespace, "WIP");
            dtForTesting.add(randomBDT);

            DTObject derivedBDTLevelOne = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, randomBDT, developerA, namespace, "WIP");
            derivedBDTs.add(derivedBDTLevelOne);

            DTObject derivedBDTLevelTwo = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, derivedBDTLevelOne, developerA, namespace, "WIP");
            derivedBDTs.add(derivedBDTLevelTwo);

            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developerA, namespace, branch, "WIP");
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        for (DTObject dt : dtForTesting) {
            DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
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
            dtViewEditPage.hitUpdateButton();

            for (DTObject derivedDT : derivedBDTs) {
                homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
                viewEditDataTypePage.openDTViewEditPageByManifestID(derivedDT.getDtManifestId());
                supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + derivedDT.getDen() + "/" + dtSCName);
                assertTrue(supplementaryComponentNode.isDisplayed());
                SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);
                assertTrue(SCPanel.getDefinitionFieldValue().equals(definition));
                assertTrue(SCPanel.getDefinitionSourceFieldValue().equals(definitionSource));
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
