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
import org.oagi.score.e2e.page.core_component.ViewEditDataTypePage;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

@Execution(ExecutionMode.CONCURRENT)
public class TC_41_10_EditingRevisionOfAnEndUserDT extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_41_10_TA_1")
    public void test_TA_1() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        DTObject baseCDT;
        CodeListObject codeList;
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            baseCDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.8");
            NamespaceObject namespaceEUA = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);
            NamespaceObject namespaceEUB = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB, library);

            DTObject randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseCDT, endUserA, namespaceEUA, "Production");
            dtForTesting.add(randomBDT);

            randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseCDT, endUserB, namespaceEUB, "Production");
            dtForTesting.add(randomBDT);

            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserA, namespaceEUA, branch, "WIP");
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        for (DTObject dt : dtForTesting) {
            ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
            DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
            dtViewEditPage.hitAmendButton();
            assertTrue(dtViewEditPage.getStateFieldValue().equals("WIP"));
            assertTrue(Integer.valueOf(dtViewEditPage.getRevisionFieldValue()) > 1);

            /**
             * Test Assertion #41.10.1.a
             */
            if (dtViewEditPage.getDefinitionFieldValue() == null) {
                click(dtViewEditPage.getUpdateButton(true));
                assertEquals("Are you sure you want to update this without definitions?",
                        dtViewEditPage.getDefinitionWarningDialogMessage());
                dtViewEditPage.hitUpdateAnywayButton();
            }

            /**
             * Test Assertion #41.10.1.b
             */
            dtViewEditPage.showValueDomain();
            assertDoesNotThrow(() -> dtViewEditPage.addCodeListValueDomain(codeList.getName()));
            dtViewEditPage.selectValueDomain(codeList.getName());
            assertDoesNotThrow(() -> dtViewEditPage.discardValueDomain());

            /**
             * Test Assertion #41.10.1.c
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
            List<String> representationTermsForCDTs = getAPIFactory().getCoreComponentAPI().getRepresentationTermsForCDTs("3.1");
            String representationTerm = representationTermsForCDTs.get(representationTermsForCDTs.size() - 1);
            SCPanel.selectRepresentationTerm(representationTerm);
            dtSCName = dtSC.getObjectClassTerm() + ". " + dtSC.getPropertyTerm() + ". " + SCPanel.getRepresentationSelectFieldValue();
            dtViewEditPage.hitUpdateButton();
            dtViewEditPage.removeSupplementaryComponent("/" + revisedDT.getDen() + "/" + dtSCName);

            /**
             * Test Assertion #41.10.1.d
             */
            List<DTSCObject> supplementaryComponentsFromTheBaseDT = getAPIFactory().getCoreComponentAPI().getSupplementaryComponentsForDT(
                    baseCDT.getDtId(), getAPIFactory().getReleaseAPI().getReleaseById(baseCDT.getReleaseId()).getReleaseNumber());
            DTSCObject existingDTSC = supplementaryComponentsFromTheBaseDT.get(0);
            String existingDTSCName = existingDTSC.getObjectClassTerm() + ". " + existingDTSC.getPropertyTerm() + ". " + existingDTSC.getRepresentationTerm();
            supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + revisedDT.getDen() + "/" + existingDTSCName);
            assertTrue(supplementaryComponentNode.isDisplayed());
            SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);
            SCPanel.setCardinality("Required");
            SCPanel.setValueConstraintType("Fixed Value");
            SCPanel.setValueConstraint("fixed value");
            dtViewEditPage.hitUpdateButton();

            /**
             * Existing SC can be edited but it cannot be discarded.
             */
            assertThrows(WebDriverException.class, () -> dtViewEditPage.removeSupplementaryComponent("/" + revisedDT.getDen() + "/" + existingDTSCName));

            viewEditDataTypePage.openPage();
        }
    }

    @Test
    @DisplayName("TC_41_10_TA_2")
    public void test_TA_2() {
        AppUserObject endUserA;
        LibraryObject library;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        DTObject baseCDT;
        CodeListObject codeList;
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            baseCDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.8");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);

            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserA, namespace, branch, "WIP");

            DTObject randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseCDT, endUserA, namespace, "Production");
            dtForTesting.add(randomBDT);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        for (DTObject dt : dtForTesting) {

            homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
            DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
            dtViewEditPage.hitAmendButton();
            DTObject revisedDT = getAPIFactory().getCoreComponentAPI().getRevisedDT(dt);

            homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
            viewEditDataTypePage.createDT(dt.getDen(), branch.getReleaseNumber());
            String derivedBDTDefinition = "Derived BDT definition";
            String derivedBDTDefinitionSource = "Derived BDT definition source";
            String derivedBDTContentComponentDefinition = "Derived BDT Content Component definition";
            dtViewEditPage.setDefinition(derivedBDTDefinition);
            dtViewEditPage.setDefinitionSource(derivedBDTDefinitionSource);
            dtViewEditPage.setContentComponentDefinition(derivedBDTContentComponentDefinition);
            dtViewEditPage.setQualifier("newDerivedDT");
            dtViewEditPage.hitUpdateButton();
            DTObject derivedDT = getAPIFactory().getCoreComponentAPI().getBDTByGuidAndReleaseNum(library, dtViewEditPage.getGUIDFieldValue(), branch.getReleaseNumber());

            homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
            viewEditDataTypePage.openDTViewEditPageByManifestID(revisedDT.getDtManifestId());
            String baseBDTDefinition = "Base BDT definition";
            String baseBDTDefinitionSource = "Base BDT definition source";
            String baseBDTContentComponentDefinition = "Base BDT Content Component definition";
            dtViewEditPage.setDefinition(baseBDTDefinition);
            dtViewEditPage.setDefinitionSource(baseBDTDefinitionSource);
            dtViewEditPage.setContentComponentDefinition(baseBDTContentComponentDefinition);
            dtViewEditPage.showValueDomain();
            dtViewEditPage.addCodeListValueDomain(codeList.getName());
            List<DTSCObject> supplementaryComponentsFromTheBaseDT = getAPIFactory().getCoreComponentAPI().getSupplementaryComponentsForDT(
                    baseCDT.getDtId(), getAPIFactory().getReleaseAPI().getReleaseById(baseCDT.getReleaseId()).getReleaseNumber());
            DTSCObject dtSC = supplementaryComponentsFromTheBaseDT.get(0);
            String dtSCName = dtSC.getObjectClassTerm() + ". " + dtSC.getPropertyTerm() + ". " + dtSC.getRepresentationTerm();
            WebElement supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + dt.getDen() + "/" + dtSCName);
            assertTrue(supplementaryComponentNode.isDisplayed());
            DTViewEditPage.SupplementaryComponentPanel SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);
            SCPanel.setCardinality("Required");
            SCPanel.setValueConstraintType("Fixed Value");
            SCPanel.setValueConstraint("fixed value");
            dtViewEditPage.hitUpdateButton();

            homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
            viewEditDataTypePage.openDTViewEditPageByManifestID(derivedDT.getDtManifestId());
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
