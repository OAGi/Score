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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.oagi.score.e2e.impl.PageHelper.escape;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

@Execution(ExecutionMode.CONCURRENT)
public class TC_41_8_EditingValueDomains extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_41_8_TA_1")
    public void test_TA_1() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        DTObject baseCDT;
        List<DTObject> derivedBDTs = new ArrayList<>();
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
            dtViewEditPage.showValueDomain();
            String representationTerm = dtViewEditPage.getRepresentationTermFieldValue();
            List<String> valueDomains = getAPIFactory().getCoreComponentAPI().getValueDomainsByCDTRepresentationTerm(representationTerm);
            if (valueDomains.contains("Token")){
                dtViewEditPage.addCodeListValueDomain(codeList.getName());
            }
            dtViewEditPage.hitUpdateButton();
            dtViewEditPage.addSupplementaryComponent("/" + dt.getDen());
            waitFor(Duration.ofMillis(3000L));
            DTSCObject dtSC = getAPIFactory().getCoreComponentAPI().getNewlyCreatedSCForDT(dt.getDtId(), branch.getReleaseNumber());
            String dtSCName = dtSC.getObjectClassTerm() + ". " + dtSC.getPropertyTerm() + ". " + dtSC.getRepresentationTerm();
            WebElement supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + dt.getDen() + "/" + dtSCName);
            assertTrue(supplementaryComponentNode.isDisplayed());
            DTViewEditPage.SupplementaryComponentPanel SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);
            dtViewEditPage.showValueDomain();
            List<String> representationTermsForCDTs = getAPIFactory().getCoreComponentAPI().getRepresentationTermsForCDTs(branch.getReleaseNumber());
            representationTerm = SCPanel.getRepresentationSelectFieldValue();
            valueDomains = getAPIFactory().getCoreComponentAPI().getValueDomainsByCDTRepresentationTerm(representationTerm);
            if (valueDomains.contains("Token")){
                dtViewEditPage.addCodeListValueDomain(codeList.getName());
            } else{
                for (String representationTermNew: representationTermsForCDTs){
                    valueDomains = getAPIFactory().getCoreComponentAPI().getValueDomainsByCDTRepresentationTerm(representationTermNew);
                    if (valueDomains.contains("Token")){
                        SCPanel.selectRepresentationTerm(representationTermNew);
                        dtViewEditPage.addCodeListValueDomain(codeList.getName());
                        String propertyTerm = SCPanel.getPropertyTermFieldValue();
                        representationTerm = SCPanel.getRepresentationSelectFieldValue();
                        dtSCName = dtSC.getObjectClassTerm() + ". " + propertyTerm + ". " + representationTerm;
                        break;
                    }
                }
            }
            dtViewEditPage.setDefinition("definition");
            dtViewEditPage.hitUpdateButton();
            for (DTObject derivedDT: derivedBDTs){
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
                viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(derivedDT.getDen(), branch.getReleaseNumber());
                dtViewEditPage.showValueDomain();
                assertDoesNotThrow(() -> dtViewEditPage.getTableRecordByValue(codeList.getName()));

                supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + derivedDT.getDen() + "/" + dtSCName);
                assertTrue(supplementaryComponentNode.isDisplayed());
                dtViewEditPage.getSCPanel(supplementaryComponentNode);
                dtViewEditPage.showValueDomain();
                assertDoesNotThrow(() -> dtViewEditPage.getTableRecordByValue(codeList.getName()));
            }

        }
    }
    @Test
    @DisplayName("TC_41_8_TA_2")
    public void test_TA_2() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        DTObject baseCDT;
        List<DTObject> derivedBDTs = new ArrayList<>();
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
            dtViewEditPage.showValueDomain();
            String representationTerm = dtViewEditPage.getRepresentationTermFieldValue();
            List<String> valueDomains = getAPIFactory().getCoreComponentAPI().getValueDomainsByCDTRepresentationTerm(representationTerm);
            if (valueDomains.contains("Token")){
                dtViewEditPage.addCodeListValueDomain(codeList.getName());
            }
            dtViewEditPage.hitUpdateButton();
            dtViewEditPage.selectValueDomain(codeList.getName());
            dtViewEditPage.discardValueDomain();
            dtViewEditPage.hitUpdateButton();
            dtViewEditPage.addSupplementaryComponent("/" + dt.getDen());
            waitFor(Duration.ofMillis(3000L));
            DTSCObject dtSC = getAPIFactory().getCoreComponentAPI().getNewlyCreatedSCForDT(dt.getDtId(), branch.getReleaseNumber());
            String dtSCName = dtSC.getObjectClassTerm() + ". " + dtSC.getPropertyTerm() + ". " + dtSC.getRepresentationTerm();
            WebElement supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + dt.getDen() + "/" + dtSCName);
            assertTrue(supplementaryComponentNode.isDisplayed());
            DTViewEditPage.SupplementaryComponentPanel SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);
            dtViewEditPage.showValueDomain();
            List<String> representationTermsForCDTs = getAPIFactory().getCoreComponentAPI().getRepresentationTermsForCDTs(branch.getReleaseNumber());
            representationTerm = SCPanel.getRepresentationSelectFieldValue();
            valueDomains = getAPIFactory().getCoreComponentAPI().getValueDomainsByCDTRepresentationTerm(representationTerm);
            if (valueDomains.contains("Token")){
                dtViewEditPage.addCodeListValueDomain(codeList.getName());
            } else{
                for (String representationTermNew: representationTermsForCDTs){
                    valueDomains = getAPIFactory().getCoreComponentAPI().getValueDomainsByCDTRepresentationTerm(representationTermNew);
                    if (valueDomains.contains("Token")){
                        SCPanel.selectRepresentationTerm(representationTermNew);
                        dtViewEditPage.addCodeListValueDomain(codeList.getName());
                        String propertyTerm = SCPanel.getPropertyTermFieldValue();
                        representationTerm = SCPanel.getRepresentationSelectFieldValue();
                        dtSCName = dtSC.getObjectClassTerm() + ". " + propertyTerm + ". " + representationTerm;
                        break;
                    }
                }
            }
            dtViewEditPage.setDefinition("definition");
            dtViewEditPage.hitUpdateButton();
            supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + dt.getDen() + "/" + dtSCName);
            assertTrue(supplementaryComponentNode.isDisplayed());
            SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);
            dtViewEditPage.selectValueDomain(codeList.getName());
            dtViewEditPage.discardValueDomain();
            dtViewEditPage.hitUpdateButton();
            for (DTObject derivedDT: derivedBDTs){
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
                viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(derivedDT.getDen(), branch.getReleaseNumber());
                dtViewEditPage.showValueDomain();
                assertThrows(TimeoutException.class, () -> dtViewEditPage.getTableRecordByValue(codeList.getName()));

                supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + derivedDT.getDen() + "/" + dtSCName);
                assertTrue(supplementaryComponentNode.isDisplayed());
                dtViewEditPage.getSCPanel(supplementaryComponentNode);
                dtViewEditPage.showValueDomain();
                assertThrows(TimeoutException.class, () -> dtViewEditPage.getTableRecordByValue(codeList.getName()));
            }

        }
    }
    @Test
    @DisplayName("TC_41_8_TA_3")
    public void test_TA_3() {
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
            dtViewEditPage.showValueDomain();
            String representationTerm = dtViewEditPage.getRepresentationTermFieldValue();
            List<String> valueDomains = getAPIFactory().getCoreComponentAPI().getValueDomainsByCDTRepresentationTerm(representationTerm);
            if (valueDomains.contains("Token")){
                dtViewEditPage.addCodeListValueDomain(codeList.getName());
                assertThrows(TimeoutException.class, () -> dtViewEditPage.addCodeListValueDomain(codeList.getName()));
                escape(getDriver());
            }
            dtViewEditPage.addSupplementaryComponent("/" + dt.getDen());
            waitFor(Duration.ofMillis(3000L));
            DTSCObject dtSC = getAPIFactory().getCoreComponentAPI().getNewlyCreatedSCForDT(dt.getDtId(), branch.getReleaseNumber());
            String dtSCName = dtSC.getObjectClassTerm() + ". " + dtSC.getPropertyTerm() + ". " + dtSC.getRepresentationTerm();
            WebElement supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + dt.getDen() + "/" + dtSCName);
            assertTrue(supplementaryComponentNode.isDisplayed());
            DTViewEditPage.SupplementaryComponentPanel SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);
            dtViewEditPage.showValueDomain();
            List<String> representationTermsForCDTs = getAPIFactory().getCoreComponentAPI().getRepresentationTermsForCDTs(branch.getReleaseNumber());
            representationTerm = SCPanel.getRepresentationSelectFieldValue();
            valueDomains = getAPIFactory().getCoreComponentAPI().getValueDomainsByCDTRepresentationTerm(representationTerm);
            if (valueDomains.contains("Token")){
                dtViewEditPage.addCodeListValueDomain(codeList.getName());
            } else{
                for (String representationTermNew: representationTermsForCDTs){
                    valueDomains = getAPIFactory().getCoreComponentAPI().getValueDomainsByCDTRepresentationTerm(representationTermNew);
                    if (valueDomains.contains("Token")){
                        SCPanel.selectRepresentationTerm(representationTermNew);
                        dtViewEditPage.addCodeListValueDomain(codeList.getName());
                        assertThrows(TimeoutException.class, () -> dtViewEditPage.addCodeListValueDomain(codeList.getName()));
                        escape(getDriver());
                        break;
                    }
                }
            }

        }
    }
    @Test
    @DisplayName("TC_41_8_TA_4")
    public void test_TA_4() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        DTObject baseCDT;
        List<DTObject> derivedBDTs = new ArrayList<>();
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
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        for (DTObject dt : dtForTesting) {
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            dtViewEditPage.showValueDomain();
            String representationTerm = dtViewEditPage.getRepresentationTermFieldValue();
            List<String> valueDomains = getAPIFactory().getCoreComponentAPI().getValueDomainsByCDTRepresentationTerm(representationTerm);
            String defaultDomainCC = getAPIFactory().getCoreComponentAPI().getDefaultValueDomainByCDTRepresentationTerm(representationTerm);
            for (String valueDomain: valueDomains){
                if (!valueDomain.equals(defaultDomainCC)){
                    dtViewEditPage.setDefaultValueDomain(valueDomain);
                    defaultDomainCC = valueDomain;
                    break;
                }
            }
            dtViewEditPage.hitUpdateButton();
            dtViewEditPage.addSupplementaryComponent("/" + dt.getDen());
            waitFor(Duration.ofMillis(3000L));
            DTSCObject dtSC = getAPIFactory().getCoreComponentAPI().getNewlyCreatedSCForDT(dt.getDtId(), branch.getReleaseNumber());
            String dtSCName = dtSC.getObjectClassTerm() + ". " + dtSC.getPropertyTerm() + ". " + dtSC.getRepresentationTerm();
            WebElement supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + dt.getDen() + "/" + dtSCName);
            assertTrue(supplementaryComponentNode.isDisplayed());
            DTViewEditPage.SupplementaryComponentPanel SCPanel = dtViewEditPage.getSCPanel(supplementaryComponentNode);
            dtViewEditPage.showValueDomain();
            representationTerm = SCPanel.getRepresentationSelectFieldValue();
            valueDomains = getAPIFactory().getCoreComponentAPI().getValueDomainsByCDTRepresentationTerm(representationTerm);
            String defaultDomainSC = getAPIFactory().getCoreComponentAPI().getDefaultValueDomainByCDTRepresentationTerm(representationTerm);
            for (String valueDomain: valueDomains){
                if (!valueDomain.equals(defaultDomainSC)){
                    dtViewEditPage.setDefaultValueDomain(valueDomain);
                    defaultDomainSC = valueDomain;
                    break;
                }
            }

            dtViewEditPage.setDefinition("definition");
            dtViewEditPage.hitUpdateButton();
            for (DTObject derivedDT: derivedBDTs){
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
                viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(derivedDT.getDen(), branch.getReleaseNumber());
                dtViewEditPage.showValueDomain();
                assertTrue(dtViewEditPage.getDefaultValueDomainFieldValue().contains(defaultDomainCC));

                supplementaryComponentNode = dtViewEditPage.getNodeByPath("/" + derivedDT.getDen() + "/" + dtSCName);
                assertTrue(supplementaryComponentNode.isDisplayed());
                dtViewEditPage.getSCPanel(supplementaryComponentNode);
                dtViewEditPage.showValueDomain();
                assertTrue(dtViewEditPage.getDefaultValueDomainFieldValue().contains(defaultDomainSC));
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
