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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_41_3_EditingBrandNewEndUserDT extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_41_3_TA_1")
    public void test_TA_1() {
        AppUserObject endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUserA);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
        ReleaseObject branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

        DTObject cdt = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());

        library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
        NamespaceObject endUserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);

        CodeListObject codeList = getAPIFactory().getCodeListAPI().
                createRandomCodeList(endUserA, endUserNamespace, branch, "WIP");
        DTObject randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, cdt, endUserA, endUserNamespace, "WIP");

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByDenAndBranch(randomBDT.getDen(), branch.getReleaseNumber());

        /**
         * Test Assertion #41.3.1.a
         */
        assertEquals("true", dtViewEditPage.getNamespaceField().getAttribute("aria-required"));
        List<NamespaceObject> standardNamespaces = getAPIFactory().getNamespaceAPI().getNonStandardNamespacesURIs(library);
        for (NamespaceObject namespace : standardNamespaces) {
            click(dtViewEditPage.getNamespaceField());
            waitFor(ofMillis(1000L));
            WebElement option = elementToBeClickable(getDriver(), By.xpath(
                    "//span[contains(text(), \"" + namespace.getUri() + "\")]//ancestor::mat-option"));
            assertNotNull(option);
            waitFor(ofMillis(1000L));
            escape(getDriver());
        }

        dtViewEditPage.showValueDomain();
        assertDoesNotThrow(() -> dtViewEditPage.getValueDomainByTypeNameAndXSDExpression("Primitive", "NormalizedString", "normalized string"));
        assertDoesNotThrow(() -> dtViewEditPage.getValueDomainByTypeNameAndXSDExpression("Primitive", "String", "string"));

        assertDisabled(dtViewEditPage.getCheckboxForValueDomainByTypeAndName("Primitive", "NormalizedString"));
        dtViewEditPage.addCodeListValueDomain(codeList.getName());
        dtViewEditPage.selectValueDomain(codeList.getName());
        dtViewEditPage.discardValueDomain();

        /**
         * Test Assertion #41.3.1.b
         */
        dtViewEditPage.setQualifier("newQualifier, " + randomBDT.getQualifier());

        /**
         * Test Assertion #41.3.1.c
         */
        assertEquals("false", dtViewEditPage.getSixHexadecimalIdentifierField().getAttribute("aria-required"));

        /**
         * Test Assertion #41.3.1.d
         */
        assertEquals("false", dtViewEditPage.getContentComponentDefinitionField().getAttribute("aria-required"));
        assertEquals("false", dtViewEditPage.getDefinitionField().getAttribute("aria-required"));
        assertEquals("false", dtViewEditPage.getDefinitionSourceField().getAttribute("aria-required"));
        dtViewEditPage.setDefinition("");
        click(dtViewEditPage.getUpdateButton(true));
        assertEquals("Are you sure you want to update this without definitions?",
                dtViewEditPage.getDefinitionWarningDialogMessage());
        dtViewEditPage.hitUpdateAnywayButton();
    }

    @Test
    @DisplayName("TC_41_3_TA_2_and_TA_3")
    public void test_TA_2_and_TA_3() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        DTObject baseCDT;
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            baseCDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);

            DTObject randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseCDT, endUserA, namespace, "WIP");
            dtForTesting.add(randomBDT);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        for (DTObject dt : dtForTesting) {
            DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
            assertDoesNotThrow(() -> dtViewEditPage.addSupplementaryComponent("/" + dt.getDen()));
            List<DTSCObject> supplementaryComponentsFromTheBaseDT = getAPIFactory().getCoreComponentAPI().getSupplementaryComponentsForDT(
                    baseCDT.getDtId(), getAPIFactory().getReleaseAPI().getReleaseById(baseCDT.getReleaseId()).getReleaseNumber());
            for (DTSCObject dtSC : supplementaryComponentsFromTheBaseDT) {
                String dtSCName = dtSC.getObjectClassTerm() + ". " + dtSC.getPropertyTerm() + ". " + dtSC.getRepresentationTerm();
                assertDoesNotThrow(() -> dtViewEditPage.getNodeByPath("/" + dt.getDen() + "/" + dtSCName));
            }
        }
    }

    @Test
    @DisplayName("TC_41_3_TA_4")
    public void test_TA_4() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        Map<DTObject, DTObject> derivedBDTs = new HashMap<>();
        DTObject baseCDT;
        CodeListObject codeList;
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            baseCDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);

            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserA, namespace, branch, "WIP");

            DTObject randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseCDT, endUserA, namespace, "WIP");
            dtForTesting.add(randomBDT);

            DTObject derivedBDTLevelOne = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, randomBDT, endUserA, namespace, "WIP");
            derivedBDTs.put(randomBDT, derivedBDTLevelOne);

            DTObject derivedBDTLevelTwo = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, derivedBDTLevelOne, endUserA, namespace, "WIP");
            derivedBDTs.put(derivedBDTLevelOne, derivedBDTLevelTwo);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        for (DTObject dt : dtForTesting) {
            DTObject derivedDT = derivedBDTs.get(dt);
            DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(derivedDT.getDtManifestId());
            String derivedBDTDefinition = "Derived BDT definition";
            String derivedBDTDefinitionSource = "Derived BDT definition source";
            String derivedBDTContentComponentDefinition = "Derived BDT Content Component definition";
            dtViewEditPage.setDefinition(derivedBDTDefinition);
            dtViewEditPage.setDefinitionSource(derivedBDTDefinitionSource);
            dtViewEditPage.setContentComponentDefinition(derivedBDTContentComponentDefinition);
            dtViewEditPage.hitUpdateButton();

            homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
            viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
            String baseBDTDefinition = "Base BDT definition";
            String baseBDTDefinitionSource = "Base BDT definition source";
            String baseBDTContentComponentDefinition = "Base BDT Content Component definition";
            dtViewEditPage.setDefinition(baseBDTDefinition);
            dtViewEditPage.setDefinitionSource(baseBDTDefinitionSource);
            dtViewEditPage.setContentComponentDefinition(baseBDTContentComponentDefinition);
            dtViewEditPage.showValueDomain();
            dtViewEditPage.addCodeListValueDomain(codeList.getName());
            List<DTSCObject> supplementaryComponentsFromTheBaseDT =
                    getAPIFactory().getCoreComponentAPI().getSupplementaryComponentsForDT(
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
            supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + derivedDT.getDen() + "/" + dtSCName);
            assertTrue(supplementaryComponentNode.isDisplayed());
            SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);

            DTObject derivedDTLevelTwo = derivedBDTs.get(derivedDT);
            homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
            viewEditDataTypePage.openDTViewEditPageByDenAndBranch(derivedDTLevelTwo.getDen(), branch.getReleaseNumber());
            assertTrue(dtViewEditPage.getDefinitionFieldValue().equals(derivedBDTDefinition));
            assertTrue(dtViewEditPage.getDefinitionSourceFieldValue().equals(derivedBDTDefinitionSource));
            assertTrue(dtViewEditPage.getContentComponentDefinitionFieldValue().equals(derivedBDTContentComponentDefinition));
            dtViewEditPage.showValueDomain();
            assertDoesNotThrow(() -> dtViewEditPage.getTableRecordByValue(codeList.getName()));
            supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + derivedDTLevelTwo.getDen() + "/" + dtSCName);
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
