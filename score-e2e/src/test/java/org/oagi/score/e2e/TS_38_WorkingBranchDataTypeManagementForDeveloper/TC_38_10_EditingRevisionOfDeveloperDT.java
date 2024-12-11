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
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

@Execution(ExecutionMode.CONCURRENT)
public class TC_38_10_EditingRevisionOfDeveloperDT extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_38_10_TA_1")
    public void test_TA_1() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        DTObject baseCDT;
        CodeListObject codeList;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            baseCDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());
            DTObject randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseCDT, developerA, namespace, "Published");
            dtForTesting.add(randomBDT);

            randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseCDT, developerB, namespace, "Published");
            dtForTesting.add(randomBDT);

            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developerA, namespace, branch, "WIP");
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        for (DTObject dt : dtForTesting) {
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            dtViewEditPage.hitReviseButton();
            assertTrue(dtViewEditPage.getStateFieldValue().equals("WIP"));
            assertTrue(Integer.valueOf(dtViewEditPage.getRevisionFieldValue()) > 1);

            /**
             * Test Assertion #38.10.1.a
             */
            if (dtViewEditPage.getDefinitionFieldValue() == null) {
                click(dtViewEditPage.getUpdateButton(true));
                assertEquals("Are you sure you want to update this without definitions?",
                        dtViewEditPage.getDefinitionWarningDialogMessage());
                dtViewEditPage.hitUpdateAnywayButton();
            }

            /**
             * Test Assertion #38.10.1.b
             */
            dtViewEditPage.showValueDomain();
            assertDoesNotThrow(() -> dtViewEditPage.addCodeListValueDomain(codeList.getName()));
            dtViewEditPage.selectValueDomain(codeList.getName());
            assertDoesNotThrow(() -> dtViewEditPage.discardValueDomain());

            /**
             * Test Assertion #38.10.1.c
             */
            dtViewEditPage.addSupplementaryComponent("/" + dt.getDen());
            waitFor(Duration.ofMillis(3000L));
            DTObject revisedDT = getAPIFactory().getCoreComponentAPI().getRevisedDT(dt);
            DTSCObject dtSC = getAPIFactory().getCoreComponentAPI().getNewlyCreatedSCForDT(revisedDT.getDtId(), branch.getReleaseNumber());
            String dtSCName = dtSC.getObjectClassTerm() + ". " + dtSC.getPropertyTerm() + ". " + dtSC.getRepresentationTerm();
            WebElement supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + revisedDT.getDen() + "/" + dtSCName);
            assertTrue(supplementaryComponentNode.isDisplayed());
            DTViewEditPage.SupplementaryComponentPanel SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);
            SCPanel.setDefinition("some definition");
            SCPanel.setCardinality("Required");
            SCPanel.setValueConstraintType("Fixed Value");
            SCPanel.setValueConstraint("fixed value");
            List<String> representationTermsForCDTs = getAPIFactory().getCoreComponentAPI().getRepresentationTermsForCDTs(branch.getReleaseNumber());
            String representationTerm = representationTermsForCDTs.get(representationTermsForCDTs.size() - 1);
            SCPanel.selectRepresentationTerm(representationTerm);
            dtSCName = dtSC.getObjectClassTerm() + ". " + dtSC.getPropertyTerm() + ". " + SCPanel.getRepresentationSelectFieldValue();
            dtViewEditPage.hitUpdateButton();
            dtViewEditPage.removeSupplementaryComponent("/" + revisedDT.getDen() + "/" + dtSCName);

            /**
             * Test Assertion #38.10.1.d
             */
            List<DTSCObject> supplementaryComponentsFromTheBaseDT = getAPIFactory().getCoreComponentAPI().getSupplementaryComponentsForDT(baseCDT.getDtId(), branch.getReleaseNumber());
            DTSCObject existingDTSC = supplementaryComponentsFromTheBaseDT.get(0);
            String existingDTSCName = existingDTSC.getObjectClassTerm() + ". " + existingDTSC.getPropertyTerm() + ". " + existingDTSC.getRepresentationTerm();
            supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + revisedDT.getDen() + "/" + existingDTSCName);
            assertTrue(supplementaryComponentNode.isDisplayed());
            SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);
            SCPanel.setCardinality("Required");
            SCPanel.setValueConstraintType("Fixed Value");
            SCPanel.setValueConstraint("fixed value");
            dtViewEditPage.hitUpdateButton();
            assertThrows(WebDriverException.class, () -> dtViewEditPage.removeSupplementaryComponent("/" + revisedDT.getDen() + "/" + existingDTSCName));

            viewEditCoreComponentPage.openPage();
        }
    }

    @Test
    @DisplayName("TC_38_10_TA_2")
    public void test_TA_2() {
        AppUserObject developerA;
        LibraryObject library;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        DTObject baseCDT;
        CodeListObject codeList;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developerA, namespace, branch, "Published");

            baseCDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());
            DTObject randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseCDT, developerA, namespace, "Published");
            dtForTesting.add(randomBDT);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        for (DTObject dt : dtForTesting) {

            homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            dtViewEditPage.hitReviseButton();
            DTObject revisedDT = getAPIFactory().getCoreComponentAPI().getRevisedDT(dt);

            homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            viewEditCoreComponentPage.createDT(dt.getDen(), branch.getReleaseNumber());
            String derivedBDTDefinition = "Derived BDT definition";
            String derivedBDTDefinitionSource = "Derived BDT definition source";
            String derivedBDTContentComponentDefinition = "Derived BDT Content Component definition";
            dtViewEditPage.setDefinition(derivedBDTDefinition);
            dtViewEditPage.setDefinitionSource(derivedBDTDefinitionSource);
            dtViewEditPage.setContentComponentDefinition(derivedBDTContentComponentDefinition);
            dtViewEditPage.setQualifier("newDerivedDT");
            dtViewEditPage.hitUpdateButton();
            DTObject derivedDT = getAPIFactory().getCoreComponentAPI().getBDTByGuidAndReleaseNum(library, dtViewEditPage.getGUIDFieldValue(), branch.getReleaseNumber());

            homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(revisedDT.getDen(), branch.getReleaseNumber());
            String baseBDTDefinition = "Base BDT definition";
            String baseBDTDefinitionSource = "Base BDT definition source";
            String baseBDTContentComponentDefinition = "Base BDT Content Component definition";
            dtViewEditPage.setDefinition(baseBDTDefinition);
            dtViewEditPage.setDefinitionSource(baseBDTDefinitionSource);
            dtViewEditPage.setContentComponentDefinition(baseBDTContentComponentDefinition);
            dtViewEditPage.showValueDomain();
            dtViewEditPage.addCodeListValueDomain(codeList.getName());
            List<DTSCObject> supplementaryComponentsFromTheBaseDT = getAPIFactory().getCoreComponentAPI().getSupplementaryComponentsForDT(baseCDT.getDtId(), branch.getReleaseNumber());
            DTSCObject dtSC = supplementaryComponentsFromTheBaseDT.get(0);
            String dtSCName = dtSC.getObjectClassTerm() + ". " + dtSC.getPropertyTerm() + ". " +dtSC.getRepresentationTerm();
            WebElement supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + dt.getDen() + "/" + dtSCName);
            assertTrue(supplementaryComponentNode.isDisplayed());
            DTViewEditPage.SupplementaryComponentPanel SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);
            SCPanel.setCardinality("Required");
            SCPanel.setValueConstraintType("Fixed Value");
            SCPanel.setValueConstraint("fixed value");
            dtViewEditPage.hitUpdateButton();

            homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(derivedDT.getDen(), branch.getReleaseNumber());
            assertFalse(dtViewEditPage.getDefinitionFieldValue().equals(baseBDTDefinition));
            assertFalse(dtViewEditPage.getDefinitionSourceFieldValue().equals(baseBDTDefinitionSource));
            assertFalse(dtViewEditPage.getContentComponentDefinitionFieldValue().equals(baseBDTContentComponentDefinition));
            dtViewEditPage.showValueDomain();
            assertDoesNotThrow(() -> dtViewEditPage.getTableRecordByValue(codeList.getName()));
            supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + dtSCName);
            assertTrue(supplementaryComponentNode.isDisplayed());
            SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);
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
