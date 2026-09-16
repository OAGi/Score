package org.oagi.score.e2e.TS_29_BIEUplifting;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.impl.PageHelper;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.*;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.UpliftCodeListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.oagi.score.e2e.page.core_component.ACCExtensionViewEditPage;
import org.oagi.score.e2e.page.core_component.SelectAssociationDialog;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;

import java.math.BigInteger;
import java.time.Duration;
import java.util.*;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.SAME_THREAD)
public class TC_29_1_BIEUplifting extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Delete random accounts
        this.randomAccounts.forEach(randomAccount -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId());
        });
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    private String getPersistedBbiePath(EditBIEPage editBIEPage, String displayPath) {
        String nodePath = editBIEPage.getNodeByPath(displayPath).getAttribute("data-path");
        assertNotNull(nodePath, "The BBIE tree node must expose its manifest path: " + displayPath);
        int bccpIndex = nodePath.lastIndexOf(">BCCP-");
        assertTrue(bccpIndex > 0, "Expected a BBIEP/BDT property path: " + nodePath);
        // The edit tree exposes BBIEP.PATH (BBIE.PATH>BCCP-...>DT-...).
        // The API assertion accepts BBIE.PATH, so remove the BCCP/DT suffix.
        return nodePath.substring(0, bccpIndex);
    }

    private CodeListObject getOnlyCodeListInState(String codeListName, String releaseNum, String state) {
        return getOnlyCodeListInState(codeListName, releaseNum, state, null);
    }

    private CodeListObject getOnlyCodeListInState(String codeListName, String releaseNum,
                                                   String state, BigInteger ownerUserId) {
        List<CodeListObject> matchingCodeLists = getAPIFactory().getCodeListAPI()
                .getCodeListsByCodeListNameAndReleaseNum(codeListName, releaseNum).stream()
                .filter(codeList -> state.equals(codeList.getState()))
                .filter(codeList -> ownerUserId == null || ownerUserId.equals(codeList.getOwnerUserId()))
                .toList();
        assertEquals(1, matchingCodeLists.size(),
                "Expected exactly one " + state + " Code List named " + codeListName
                        + (ownerUserId == null ? "" : " owned by user " + ownerUserId)
                        + " in " + releaseNum + " but found " + matchingCodeLists.size());
        return matchingCodeLists.get(0);
    }

    @Test
    @DisplayName("TC_29_1_TA_1")
    public void bie_can_be_uplifted_only_to_a_newer_release_than_the_one_the_bie() {
        String prevRelease = "10.8.7.1";
        String currRelease = "10.9";

        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(currRelease);
        assertThrows(WebDriverException.class, () -> upliftBIEPage.setTargetBranch(prevRelease));
        escape(getDriver());
        assertThrows(WebDriverException.class, () -> upliftBIEPage.setTargetBranch(currRelease));
    }

    @Test
    @DisplayName("TC_29_1_TA_2 (Production, developer)")
    public void user_can_uplift_any_qa_or_production_bie_and_takes_ownership_of_the_uplifted_bie() {
        assertPublishedBieOwnershipTransfer("Production", true);
    }

    @Test
    @DisplayName("TC_29_1_TA_2 (QA, end user)")
    public void end_user_can_uplift_another_users_qa_bie_and_takes_ownership() {
        assertPublishedBieOwnershipTransfer("QA", false);
    }

    private void assertPublishedBieOwnershipTransfer(String sourceState, boolean developerUplifter) {
        String prevRelease = "10.8.7.1";
        String currRelease = "10.9";

        AppUserObject uplifter = developerUplifter
                ? getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false)
                : getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(uplifter);
        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        Preconditions_TA_29_1_2 preconditionsTa2912 = preconditions_TA_29_1_2_Uplift_BIEUserbProduction(usera, library, prevRelease);

        preconditionsTa2912.topLevelASBIEP.setState(sourceState);
        getAPIFactory().getBusinessInformationEntityAPI().updateTopLevelASBIEP(preconditionsTa2912.topLevelASBIEP);

        HomePage homePage = loginPage().signIn(uplifter.getLoginId(), uplifter.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        // Uplift another account's published BIE.
        upliftBIEPage.openPage();
        upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.showAdvancedSearchPanel();
        upliftBIEPage.setSourceBranch(prevRelease);
        upliftBIEPage.setTargetBranch(currRelease);
        upliftBIEPage.setState(sourceState);
        upliftBIEPage.setDEN(preconditionsTa2912.topLevelASBIEP.getDen());
        upliftBIEPage.setOwner(usera.getLoginId());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.next();
        EditBIEPage editBIEPage = upliftBIEVerificationPage.uplift();
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEquals(uplifter.getLoginId(), getText(topLevelASBIEPPanel.getOwnerField()));
        WebElement bbieNode = editBIEPage.getNodeByPath(preconditionsTa2912.bbiePath);
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertChecked(bbiePanel.getUsedCheckbox());
        assertEquals("0", getText(bbiePanel.getCardinalityMinField()));
        assertEquals("1", getText(bbiePanel.getCardinalityMaxField()));
        assertEquals(preconditionsTa2912.bbieExample, getText(bbiePanel.getExampleField()));
        assertEquals(preconditionsTa2912.bbieRemark, getText(bbiePanel.getRemarkField()));
        assertEquals(preconditionsTa2912.bbieFixedValue, getText(bbiePanel.getFixedValueField()));
        assertEquals(preconditionsTa2912.bbieValueDomainRestriction, getText(bbiePanel.getValueDomainRestrictionSelectField()));
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith(preconditionsTa2912.bbieValueDomain));
        assertEquals(preconditionsTa2912.bbieContextDefinition, getText(bbiePanel.getContextDefinitionField()));

        WebElement asbieNode = editBIEPage.getNodeByPath(preconditionsTa2912.asbiePath);
        waitFor(ofMillis(1000L));
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        assertChecked(asbiePanel.getUsedCheckbox());
        assertNotChecked(asbiePanel.getNillableCheckbox());
        assertEquals("1", getText(asbiePanel.getCardinalityMinField()));
        assertEquals("1", getText(asbiePanel.getCardinalityMaxField()));
        assertEquals(preconditionsTa2912.asbieRemark, getText(asbiePanel.getRemarkField()));
        assertEquals(preconditionsTa2912.asbieContextDefinition, getText(asbiePanel.getContextDefinitionField()));

        WebElement bbieScNode = editBIEPage.getNodeByPath(preconditionsTa2912.bbieScPath);
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbieScNode);
        assertChecked(bbiescPanel.getUsedCheckbox());
        assertEquals("0", getText(bbiescPanel.getCardinalityMinField()));
        assertEquals("1", getText(bbiescPanel.getCardinalityMaxField()));
        assertEquals(preconditionsTa2912.bbieScExample, getText(bbiescPanel.getExampleField()));
        assertEquals(preconditionsTa2912.bbieScRemark, getText(bbiescPanel.getRemarkField()));
        assertEquals(preconditionsTa2912.bbieScFixedValue, getText(bbiescPanel.getFixedValueField()));
        assertEquals(preconditionsTa2912.bbieScValueDomainRestriction, getText(bbiescPanel.getValueDomainRestrictionSelectField()));
        assertTrue(getText(bbiescPanel.getValueDomainField()).startsWith(preconditionsTa2912.bbieScValueDomain));
        assertEquals(preconditionsTa2912.bbieScContextDefinition, getText(bbiescPanel.getContextDefinitionField()));
    }

    private Preconditions_TA_29_1_2 preconditions_TA_29_1_2_Uplift_BIEUserbProduction(
            AppUserObject usera, LibraryObject library, String prevRelease) {
        Preconditions_TA_29_1_2 preconditionsTa2912 = new Preconditions_TA_29_1_2(usera, library, prevRelease);

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(preconditionsTa2912.topLevelASBIEP);

        WebElement bbieNode = editBIEPage.getNodeByPath(preconditionsTa2912.bbiePath);
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setRemark(preconditionsTa2912.bbieRemark);
        bbiePanel.setExample(preconditionsTa2912.bbieExample);
        bbiePanel.setContextDefinition(preconditionsTa2912.bbieContextDefinition);
        bbiePanel.setValueConstraint(preconditionsTa2912.bbieValueConstraint);
        bbiePanel.setFixedValue(preconditionsTa2912.bbieFixedValue);
        bbiePanel.setValueDomainRestriction(preconditionsTa2912.bbieValueDomainRestriction);
        bbiePanel.setValueDomain(preconditionsTa2912.bbieValueDomain);
        editBIEPage.hitUpdateButton();

        WebElement asbieNode = editBIEPage.getNodeByPath(preconditionsTa2912.asbiePath);
        waitFor(ofMillis(1000L));
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        asbiePanel.setRemark(preconditionsTa2912.asbieRemark);
        asbiePanel.setContextDefinition(preconditionsTa2912.asbieContextDefinition);
        editBIEPage.hitUpdateButton();

        WebElement scenarioIdentifierNode = editBIEPage.getNodeByPath("/Change Acknowledge Shipment Status/Application Area/Scenario Identifier");
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIEPanel scenarioIdentifierPanel = editBIEPage.getBBIEPanel(scenarioIdentifierNode);
        scenarioIdentifierPanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        WebElement bbieScNode = editBIEPage.getNodeByPath(preconditionsTa2912.bbieScPath);
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbieScNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setRemark(preconditionsTa2912.bbieScRemark);
        bbiescPanel.setExample(preconditionsTa2912.bbieScExample);
        bbiescPanel.setValueConstraint(preconditionsTa2912.bbieScValueConstraint);
        bbiescPanel.setFixedValue(preconditionsTa2912.bbieScFixedValue);
        bbiescPanel.setValueDomainRestriction(preconditionsTa2912.bbieScValueDomainRestriction);
        bbiescPanel.setValueDomain(preconditionsTa2912.bbieScValueDomain);
        bbiescPanel.setContextDefinition(preconditionsTa2912.bbieScContextDefinition);
        editBIEPage.hitUpdateButton();
        editBIEPage.moveToQA();
        editBIEPage.moveToProduction();
        homePage.logout();

        return preconditionsTa2912;
    }

    private Preconditions_TA_29_1_BIE1QA preconditions_TA_9_1_4_and_TA_29_1_5a_and_TA_29_1_6a(
            AppUserObject usera, LibraryObject library, String prevRelease) {
        Preconditions_TA_29_1_BIE1QA preconditionsTa2914 = new Preconditions_TA_29_1_BIE1QA(usera, library, prevRelease);
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(preconditionsTa2914.topLevelASBIEP);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        topLevelASBIEPPanel.setBusinessTerm(preconditionsTa2914.topLevelASBIEPBusinessTerm);
        topLevelASBIEPPanel.setRemark(preconditionsTa2914.topLevelASBIEPRemark);
        topLevelASBIEPPanel.setStatus(preconditionsTa2914.topLevelASBIEPStatus);
        editBIEPage.hitUpdateButton();

        waitFor(ofMillis(3000L));
        ACCExtensionViewEditPage accExtensionViewEditPage =
                editBIEPage.extendBIELocallyOnNode("/Enterprise Unit/Extension");
        assertEquals("WIP", getText(accExtensionViewEditPage.getStateField()));
        accExtensionViewEditPage.setNamespace(euNamespace);
        accExtensionViewEditPage.hitUpdateButton();

        SelectAssociationDialog selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Enterprise Unit User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Product Classification. Classification");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Enterprise Unit User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Incorporation Location. Location");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Enterprise Unit User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Code List. Code List");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Enterprise Unit User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Revised Item Status. Status");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Enterprise Unit User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Usage Description. Description_ Text");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Enterprise Unit User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Last Modification Date Time. Open_ Date Time");

        accExtensionViewEditPage.moveToQA();
        accExtensionViewEditPage.moveToProduction();

        viewEditBIEPage.openPage();
        editBIEPage = viewEditBIEPage.openEditBIEPage(preconditionsTa2914.topLevelASBIEP);

        WebElement asbieExtNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension");
        waitFor(ofMillis(1000L));
        EditBIEPage.ASBIEPanel asbieExtPanel = editBIEPage.getASBIEPanel(asbieExtNode);
        asbieExtPanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        WebElement bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Last Modification Date Time");
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        WebElement asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Incorporation Location");
        waitFor(ofMillis(1000L));
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        asbiePanel.toggleUsed();
        asbiePanel.setRemark(preconditionsTa2914.asbieRemark);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Incorporation Location/CAGEID");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setRemark(preconditionsTa2914.bbieRemark);
        bbiePanel.setExample(preconditionsTa2914.bbieExample);
        bbiePanel.setContextDefinition(preconditionsTa2914.bbieContextDefinition);
        bbiePanel.setValueConstraint(preconditionsTa2914.bbieValueConstraint);
        bbiePanel.setFixedValue(preconditionsTa2914.bbieFixedValue);
        bbiePanel.setValueDomainRestriction(preconditionsTa2914.bbieValueDomainRestriction);
        bbiePanel.setValueDomain(preconditionsTa2914.bbieValueDomain);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Usage Description");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setRemark(preconditionsTa2914.bbieRemark);
        bbiePanel.setExample(preconditionsTa2914.bbieExample);
        bbiePanel.setContextDefinition(preconditionsTa2914.bbieContextDefinition);
        bbiePanel.setValueConstraint(preconditionsTa2914.bbieValueConstraint);
        bbiePanel.setFixedValue(preconditionsTa2914.bbieFixedValue);
        bbiePanel.setValueDomainRestriction(preconditionsTa2914.bbieValueDomainRestriction);
        bbiePanel.setValueDomain(preconditionsTa2914.bbieValueDomain);
        editBIEPage.hitUpdateButton();

        asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Incorporation Location/Physical Address");
        waitFor(ofMillis(1000L));
        asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        asbiePanel.toggleUsed();
        asbiePanel.setRemark(preconditionsTa2914.asbieRemark);
        asbiePanel.setContextDefinition(preconditionsTa2914.asbieContextDefinition);
        editBIEPage.hitUpdateButton();

        asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Code List/Code List Value");
        waitFor(ofMillis(1000L));
        asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        asbiePanel.toggleUsed();
        asbiePanel.setRemark(preconditionsTa2914.asbieRemark);
        asbiePanel.setContextDefinition(preconditionsTa2914.asbieContextDefinition);
        editBIEPage.hitUpdateButton();

        editBIEPage.openPage(); // refresh the page to load only one 'Identifier' node on display.
        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Identifier");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setCardinalityMin(11);
        bbiePanel.setCardinalityMax(99);
        bbiePanel.setRemark(preconditionsTa2914.bbieRemark);
        bbiePanel.setExample(preconditionsTa2914.bbieExample);
        bbiePanel.setContextDefinition(preconditionsTa2914.bbieContextDefinition);
        editBIEPage.hitUpdateButton();

        editBIEPage.openPage(); // refresh the page to load only one 'Type Code' node on display.
        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Type Code");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setRemark(preconditionsTa2914.bbieRemark);
        bbiePanel.setExample(preconditionsTa2914.bbieExample);
        bbiePanel.setContextDefinition(preconditionsTa2914.bbieContextDefinition);
        editBIEPage.hitUpdateButton();

        asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Revised Item Status");
        waitFor(ofMillis(1000L));
        asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        asbiePanel.toggleUsed();
        asbiePanel.setRemark(preconditionsTa2914.asbieRemark);
        asbiePanel.setContextDefinition(preconditionsTa2914.asbieContextDefinition);
        asbiePanel.setCardinalityMin(11);
        asbiePanel.setCardinalityMax(99);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Revised Item Status/Reason Code");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setRemark(preconditionsTa2914.bbieRemark);
        bbiePanel.setExample(preconditionsTa2914.bbieExample);
        bbiePanel.setContextDefinition(preconditionsTa2914.bbieContextDefinition);
        bbiePanel.setValueConstraint(preconditionsTa2914.bbieValueConstraint);
        bbiePanel.setFixedValue(preconditionsTa2914.bbieFixedValue);
        bbiePanel.setValueDomainRestriction(preconditionsTa2914.bbieValueDomainRestriction);
        bbiePanel.setValueDomain(preconditionsTa2914.bbieValueDomain);
        editBIEPage.hitUpdateButton();

        // Materialize and profile the source SCs whose information is asserted after uplift.
        for (String scPath : preconditionsTa2914.bbieScPaths) {
            String parentPath = scPath.substring(0, scPath.lastIndexOf('/'));
            EditBIEPage.BBIEPanel parentPanel = editBIEPage.getBBIEPanel(editBIEPage.getNodeByPath(parentPath));
            if (!isChecked(parentPanel.getUsedCheckbox())) {
                parentPanel.toggleUsed();
                editBIEPage.hitUpdateButton();
            }

            EditBIEPage.BBIESCPanel scPanel = editBIEPage.getBBIESCPanel(editBIEPage.getNodeByPath(scPath));
            if (!isChecked(scPanel.getUsedCheckbox())) {
                scPanel.toggleUsed();
            }
            scPanel.setRemark(preconditionsTa2914.bbieScRemark);
            scPanel.setExample(preconditionsTa2914.bbieScExample);
            scPanel.setContextDefinition(preconditionsTa2914.bbieScContextDefinition);
            scPanel.setValueConstraint(preconditionsTa2914.bbieScValueConstraint);
            scPanel.setFixedValue(preconditionsTa2914.bbieScFixedValue);
            scPanel.setValueDomainRestriction(preconditionsTa2914.bbieScValueDomainRestriction);
            scPanel.setValueDomain(preconditionsTa2914.bbieScValueDomain);
            editBIEPage.hitUpdateButton();

            // Fail in fixture setup, not in uplift verification, if source profiling was not saved.
            editBIEPage.openPage();
            parentPanel = editBIEPage.getBBIEPanel(editBIEPage.getNodeByPath(parentPath));
            assertChecked(parentPanel.getUsedCheckbox());
            scPanel = editBIEPage.getBBIESCPanel(editBIEPage.getNodeByPath(scPath));
            assertChecked(scPanel.getUsedCheckbox());
            assertEquals(preconditionsTa2914.bbieScRemark, getText(scPanel.getRemarkField()));
            assertEquals(preconditionsTa2914.bbieScExample, getText(scPanel.getExampleField()));
            assertEquals(preconditionsTa2914.bbieScContextDefinition, getText(scPanel.getContextDefinitionField()));
            assertEquals(preconditionsTa2914.bbieScFixedValue, getText(scPanel.getFixedValueField()));
            assertEquals(preconditionsTa2914.bbieScValueDomainRestriction,
                    getText(scPanel.getValueDomainRestrictionSelectField()));
            assertEquals(preconditionsTa2914.bbieScValueDomain, getText(scPanel.getValueDomainField()));
        }

        editBIEPage.moveToQA();
        homePage.logout();
        return preconditionsTa2914;
    }

    @Test
    @DisplayName("TC_29_1_TA_3")
    public void user_cannot_uplift_a_wip_bie_he_does_not_own() {
        String prev_release = "10.8.7.1";
        String curr_release = "10.9";
        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(userb);
        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = createBIEForSelectBusinessContextsPage.next(Collections.singletonList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE("Receive Delivery. Receive Delivery", prev_release);
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);
        homePage.logout();
        homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.showAdvancedSearchPanel();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setDEN(topLevelASBIEP.getDen());
        upliftBIEPage.hitSearchButton();
        assertTrue(getDriver().findElements(By.cssSelector(
                "a[href='/profile_bie/" + topLevelAsbiepId + "']")).isEmpty(),
                "A non-owner must not see the WIP BIE in uplift candidates");
    }

    @Test
    @DisplayName("TC_29_1_TA_4, TC_29_1_TA_5_a, TC_29_1_TA_6_a")
    public void uplift_transfers_business_contexts_and_preserves_system_mapped_enabled_nodes() {
        String prev_release = "10.8.6";
        String curr_release = "10.9";
        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(userb);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        Preconditions_TA_29_1_BIE1QA preconditionsTa2914 = preconditions_TA_9_1_4_and_TA_29_1_5a_and_TA_29_1_6a(usera, library, prev_release);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        EditBIEPage sourceEditPage = bieMenu.openViewEditBIESubMenu()
                .openEditBIEPage(preconditionsTa2914.topLevelASBIEP);
        Set<String> sourceContexts = new HashSet<>();
        for (WebElement context : sourceEditPage.getTopLevelASBIEPPanel().getBusinessContextList()) {
            sourceContexts.add(getText(context));
        }
        assertFalse(sourceContexts.isEmpty());
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.showAdvancedSearchPanel();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setDEN(preconditionsTa2914.topLevelASBIEP.getDen());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.next();

        for (String path :
                Arrays.asList("/Enterprise Unit/Type Code",
                        "/Enterprise Unit/Identifier")) {
            WebElement sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIEByExpandingPath(path);
            assertChecked(sourceNode.findElement(By.xpath("./mat-checkbox")));
            assertFalse(sourceNode.findElement(By.cssSelector("input[type='checkbox']")).isEnabled());
            WebElement targetNode = upliftBIEVerificationPage.goToNodeInTargetBIEByExpandingPath(path);
            waitFor(ofMillis(500L));

            WebElement node = targetNode.findElement(By.xpath(".//mat-checkbox[1]"));
            assertChecked(node);
            assertTrue(node.findElement(By.cssSelector("input[type='checkbox']")).isEnabled(),
                    "A system mapping can be overridden for a compatible selected source");
        }
        escape(getDriver());
        EditBIEPage editBIEPage = upliftBIEVerificationPage.uplift();
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        Set<String> targetContexts = new HashSet<>();
        for (WebElement context : topLevelASBIEPPanel.getBusinessContextList()) {
            targetContexts.add(getText(context));
        }
        assertEquals(sourceContexts, targetContexts);
        assertEquals(developer.getLoginId(), getText(topLevelASBIEPPanel.getOwnerField()));
        assertEquals(preconditionsTa2914.topLevelASBIEPBusinessTerm, getText(topLevelASBIEPPanel.getBusinessTermField()));
        assertEquals(preconditionsTa2914.topLevelASBIEPRemark, getText(topLevelASBIEPPanel.getRemarkField()));
        assertEquals(preconditionsTa2914.topLevelASBIEPStatus, getText(topLevelASBIEPPanel.getStatusField()));

        WebElement bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Identifier");
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertChecked(bbiePanel.getUsedCheckbox());

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Type Code");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertChecked(bbiePanel.getUsedCheckbox());
        assertEquals("0", getText(bbiePanel.getCardinalityMinField()));
        assertEquals("1", getText(bbiePanel.getCardinalityMaxField()));
        assertEquals(preconditionsTa2914.bbieExample, getText(bbiePanel.getExampleField()));
        assertEquals(preconditionsTa2914.bbieRemark, getText(bbiePanel.getRemarkField()));
        assertEquals(preconditionsTa2914.bbieContextDefinition, getText(bbiePanel.getContextDefinitionField()));
    }

    @Test
    @DisplayName("TC_29_1_TA_6_b")
    public void unused_bbie_and_supplementary_component_details_are_not_transferred() {
        AppUserObject owner = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(owner);
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(owner);
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "Party. Party", "10.8.7.1");
        TopLevelASBIEPObject source = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, owner, "WIP");
        HomePage home = loginPage().signIn(owner.getLoginId(), owner.getPassword());
        EditBIEPage editor = home.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(source);
        String bbiePath = "/Party/Identifier";
        String scPath = bbiePath + "/Scheme Agency Identifier";
        EditBIEPage.BBIEPanel bbie = editor.getBBIEPanel(editor.getNodeByPath(bbiePath));
        bbie.toggleUsed();
        bbie.setRemark("unused BBIE must not be copied");
        editor.hitUpdateButton();
        EditBIEPage.BBIESCPanel sc = editor.getBBIESCPanel(editor.getNodeByPath(scPath));
        sc.toggleUsed();
        sc.setRemark("unused SC must not be copied");
        editor.hitUpdateButton();
        sc = editor.getBBIESCPanel(editor.getNodeByPath(scPath));
        sc.toggleUsed();
        editor.hitUpdateButton();
        assertNotChecked(editor.getBBIESCPanel(editor.getNodeByPath(scPath)).getUsedCheckbox());

        // Keep the parent used: this exercises the SC filter independently of parent omission.
        editor = upliftOwnWipBie(home, source);
        bbie = editor.getBBIEPanel(editor.getNodeByPath(bbiePath));
        assertChecked(bbie.getUsedCheckbox());
        assertEquals("unused BBIE must not be copied", getText(bbie.getRemarkField()));
        sc = editor.getBBIESCPanel(editor.getNodeByPath(scPath));
        assertNotChecked(sc.getUsedCheckbox());
        assertNull(getText(sc.getRemarkField()));

        editor = home.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(source);
        bbie = editor.getBBIEPanel(editor.getNodeByPath(bbiePath));
        assertChecked(bbie.getUsedCheckbox());
        bbie.toggleUsed();
        editor.hitUpdateButton();
        assertNotChecked(editor.getBBIEPanel(editor.getNodeByPath(bbiePath)).getUsedCheckbox());
        editor = upliftOwnWipBie(home, source);
        bbie = editor.getBBIEPanel(editor.getNodeByPath(bbiePath));
        assertNotChecked(bbie.getUsedCheckbox());
        assertNull(getText(bbie.getRemarkField()));
        sc = editor.getBBIESCPanel(editor.getNodeByPath(scPath));
        assertNotChecked(sc.getUsedCheckbox());
        assertNull(getText(sc.getRemarkField()));
        home.logout();
    }

    private EditBIEPage upliftOwnWipBie(HomePage home, TopLevelASBIEPObject source) {
        UpliftBIEPage uplift = home.getBIEMenu().openUpliftBIESubMenu();
        uplift.showAdvancedSearchPanel();
        uplift.setSourceBranch("10.8.7.1");
        uplift.setTargetBranch("10.9");
        uplift.setState("WIP");
        uplift.setDEN(source.getDen());
        uplift.hitSearchButton();
        click(getDriver(), uplift.getColumnByName(getUpliftTableRecordForBIE(uplift, source), "select"));
        return uplift.next().uplift();
    }

    protected boolean isElementPresent(By by) {
        try {
            getDriver().findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    @Test
    @DisplayName("TC_29_1_TA_5_b (type compatibility)")
    public void in_case_that_a_node_does_not_match_i_e_because_of_refactoring_a() {
        String prev_release = "10.8.7.1";
        String curr_release = "10.9";
        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(userb);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        Preconditions_TA_29_1_BIE1QA preconditionsTa2915 = preconditions_TA_9_1_4_and_TA_29_1_5a_and_TA_29_1_6a(usera, library, prev_release);

        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.showAdvancedSearchPanel();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setState("QA");
        upliftBIEPage.setDEN(preconditionsTa2915.topLevelASBIEP.getDen());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.next();

        WebElement sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Enterprise Unit/Extension/Usage Description");
        WebElement targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Enterprise Unit/GL Entity Identifier/Scheme Version Identifier");
        assertFalse(targetNode.findElement(By.cssSelector("mat-checkbox input")).isEnabled());
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Enterprise Unit/General Ledger Element");
        assertFalse(targetNode.findElement(By.cssSelector("mat-checkbox input")).isEnabled());
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Enterprise Unit/Profit Center Identifier");
        assertTrue(targetNode.findElement(By.cssSelector("mat-checkbox input")).isEnabled());
        homePage.logout();
    }

    @Test
    @DisplayName("TC_29_1_TA_5_b, TC_29_1_TA_5_c, TC_29_1_TA_7, TC_29_1_TA_8")
    public void manual_mapping_checks_ancestor_nodes_and_skips_unmatched_node_information() {
        String prev_release = "10.8.8";
        String curr_release = "10.9";
        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(userb);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        Preconditions_TA_29_1_BIE1QA preconditionsTa2915 = preconditions_TA_9_1_4_and_TA_29_1_5a_and_TA_29_1_6a(usera, library, prev_release);

        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.showAdvancedSearchPanel();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setState("QA");
        upliftBIEPage.setDEN(preconditionsTa2915.topLevelASBIEP.getDen());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.next();
        // Cancelling the different-DT confirmation must leave the source unmatched.
        String sourcePath = "/Enterprise Unit/Extension/Incorporation Location/CAGEID";
        assertNotChecked(upliftBIEVerificationPage.goToNodeInSourceBIE(sourcePath)
                .findElement(By.xpath("./mat-checkbox")));
        upliftBIEVerificationPage.cancelNodeMapping(sourcePath, "/Enterprise Unit/Profit Center Identifier");
        assertNotChecked(upliftBIEVerificationPage.goToNodeInSourceBIE(sourcePath)
                .findElement(By.xpath("./mat-checkbox")));
        //different green
        upliftBIEVerificationPage.mapNode(
                "/Enterprise Unit/Extension/Incorporation Location/CAGEID",
                "/Enterprise Unit/Profit Center Identifier");

        //same green
        upliftBIEVerificationPage.mapNode(
                "/Enterprise Unit/Extension/Usage Description",
                "/Enterprise Unit/Description");

        //different blue
        upliftBIEVerificationPage.mapNode(
                "/Enterprise Unit/Extension/Incorporation Location/Physical Address",
                "/Enterprise Unit/Classification/Codes");

        //same blue
        upliftBIEVerificationPage.mapNode(
                "/Enterprise Unit/Extension/Code List/Code List Value",
                "/Enterprise Unit/Classification/Code List Value");

        upliftBIEVerificationPage.mapNode(
                "/Enterprise Unit/Extension/Revised Item Status",
                "/Enterprise Unit/Status");

        upliftBIEVerificationPage.mapNode(
                "/Enterprise Unit/Extension/Revised Item Status/Reason Code",
                "/Enterprise Unit/Status/Reason Code");
        waitFor(ofMillis(3000));
        EditBIEPage editBIEPage = upliftBIEVerificationPage.uplift();
        WebElement bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Status/Reason Code");
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEnabled(bbiePanel.getUsedCheckbox());
        assertChecked(bbiePanel.getUsedCheckbox());
        assertEquals(preconditionsTa2915.bbieRemark, getText(bbiePanel.getRemarkField()));
        assertEquals(preconditionsTa2915.bbieExample, getText(bbiePanel.getExampleField()));
        assertEquals(preconditionsTa2915.bbieContextDefinition, getText(bbiePanel.getContextDefinitionField()));
        assertEquals(preconditionsTa2915.bbieFixedValue, getText(bbiePanel.getFixedValueField()));
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith(preconditionsTa2915.bbieValueDomain));

        WebElement asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Status");
        waitFor(ofMillis(1000L));
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        assertEnabled(asbiePanel.getUsedCheckbox());
        assertChecked(asbiePanel.getUsedCheckbox());

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Profit Center Identifier");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEnabled(bbiePanel.getUsedCheckbox());
        assertChecked(bbiePanel.getUsedCheckbox());
        assertEquals(preconditionsTa2915.bbieRemark, getText(bbiePanel.getRemarkField()));
        assertEquals(preconditionsTa2915.bbieExample, getText(bbiePanel.getExampleField()));
        assertEquals(preconditionsTa2915.bbieContextDefinition, getText(bbiePanel.getContextDefinitionField()));
        assertEquals(preconditionsTa2915.bbieFixedValue, getText(bbiePanel.getFixedValueField()));
        // Without an explicit target code-list restriction, preserve the source code list.
        assertEquals(preconditionsTa2915.bbieValueDomainRestriction,
                getText(bbiePanel.getValueDomainRestrictionSelectField()));
        String identifierValueDomain = getText(bbiePanel.getValueDomainField());
        assertTrue(identifierValueDomain.startsWith(preconditionsTa2915.bbieValueDomain + " ("),
                () -> "Expected source code list on Profit Center Identifier, but was: " + identifierValueDomain);

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Description");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEnabled(bbiePanel.getUsedCheckbox());
        assertChecked(bbiePanel.getUsedCheckbox());
        assertEquals(preconditionsTa2915.bbieRemark, getText(bbiePanel.getRemarkField()));
        assertEquals(preconditionsTa2915.bbieExample, getText(bbiePanel.getExampleField()));
        assertEquals(preconditionsTa2915.bbieContextDefinition, getText(bbiePanel.getContextDefinitionField()));
        assertEquals(preconditionsTa2915.bbieFixedValue, getText(bbiePanel.getFixedValueField()));
        assertEquals(preconditionsTa2915.bbieValueDomainRestriction,
                getText(bbiePanel.getValueDomainRestrictionSelectField()));
        String descriptionValueDomain = getText(bbiePanel.getValueDomainField());
        assertTrue(descriptionValueDomain.startsWith(preconditionsTa2915.bbieValueDomain + " ("),
                () -> "Expected source code list on Description, but was: " + descriptionValueDomain);

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Classification/Codes");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEnabled(bbiePanel.getUsedCheckbox());
        assertChecked(bbiePanel.getUsedCheckbox());
        assertEquals(preconditionsTa2915.asbieRemark, getText(bbiePanel.getRemarkField()));
        assertEquals(preconditionsTa2915.asbieContextDefinition, getText(bbiePanel.getContextDefinitionField()));

        editBIEPage.openPage();
        asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Classification/Code List Value");
        waitFor(ofMillis(1000L));
        asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        assertEnabled(asbiePanel.getUsedCheckbox());
        assertChecked(asbiePanel.getUsedCheckbox());

        editBIEPage.openPage();
        WebElement bbiescNode = editBIEPage.getNodeByPath("/Enterprise Unit/Cost Center Identifier/Scheme Agency Identifier");
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        assertEnabled(bbiescPanel.getUsedCheckbox());
        assertChecked(bbiescPanel.getUsedCheckbox());
        assertEquals(preconditionsTa2915.bbieScRemark, getText(bbiescPanel.getRemarkField()));
        assertEquals(preconditionsTa2915.bbieScExample, getText(bbiescPanel.getExampleField()));
        assertEquals(preconditionsTa2915.bbieScContextDefinition, getText(bbiescPanel.getContextDefinitionField()));
        assertEquals(preconditionsTa2915.bbieScFixedValue, getText(bbiescPanel.getFixedValueField()));

        asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Classification");
        waitFor(ofMillis(1000L));
        asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        assertEnabled(asbiePanel.getUsedCheckbox());
        assertChecked(asbiePanel.getUsedCheckbox());
        assertNull(getText(asbiePanel.getRemarkField()),
                "An implicitly enabled ancestor must not receive source association details");
        assertNull(getText(asbiePanel.getContextDefinitionField()));

        //Test part where only the association information are transferred
        upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.showAdvancedSearchPanel();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setDEN(preconditionsTa2915.topLevelASBIEP.getDen());
        upliftBIEPage.hitSearchButton();
        tr = upliftBIEPage.getTableRecordAtIndex(1);
        td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        upliftBIEVerificationPage = upliftBIEPage.next();
        upliftBIEVerificationPage.mapNode(
                "/Enterprise Unit/Extension/Revised Item Status",
                "/Enterprise Unit/General Ledger Element");
        editBIEPage = upliftBIEVerificationPage.uplift();
        asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/General Ledger Element");
        waitFor(ofMillis(1000L));
        asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        assertEnabled(asbiePanel.getUsedCheckbox());
        assertChecked(asbiePanel.getUsedCheckbox());
        assertTrue(getText(asbiePanel.getCardinalityMaxField()).startsWith("99"));

        bbiescNode = editBIEPage.getNodeByPath("/Enterprise Unit/General Ledger Element/Element/Sequence Number Number");
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        assertNotChecked(bbiescPanel.getUsedCheckbox());
        assertDisabled(bbiescPanel.getRemarkField());
        homePage.logout();
    }

    @Test
    @DisplayName("TC_29_1_TA_5_d")
    public void if_a_node_of_the_source_bie_is_a_reuse_node_and_it_was() {
        String prev_release = "10.8.7.1";
        String curr_release = "10.9";
        AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(userb);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        Preconditions_TA_29_1_5d_BIEReusedChild preconditionsTa2915dReusedChild = preconditions_ta_29_1_5d_ReusedChild(userb, library, prev_release);
        Preconditions_TA_29_1_5d_BIEReusedParent preconditionsTa2915dReusedParent = preconditions_ta_29_1_5d_ReusedParent(userb, library, prev_release);
        Preconditions_TA_29_1_5d_BIEReusedScenario preconditionsTa2915dReusedScenario = new Preconditions_TA_29_1_5d_BIEReusedScenario(userb, library, prev_release);
        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(preconditionsTa2915dReusedParent.topLevelASBIEP);
        WebElement asbieNode = editBIEPage.getNodeByPath("/From UOM Package/Unit Packaging");
        waitFor(ofMillis(1000L));
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/From UOM Package/Unit Packaging");
        selectProfileBIEToReuseDialog.selectBIEToReuse(preconditionsTa2915dReusedChild.topLevelASBIEP);
        editBIEPage.moveToQA();
        editBIEPage.moveToProduction();

        viewEditBIEPage.openPage();
        editBIEPage = viewEditBIEPage.openEditBIEPage(preconditionsTa2915dReusedScenario.topLevelASBIEP);
        asbieNode = editBIEPage.getNodeByPath("/UOM Code Conversion Rate/From UOM Package");
        waitFor(ofMillis(1000L));
        asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/UOM Code Conversion Rate/From UOM Package");
        selectProfileBIEToReuseDialog.selectBIEToReuse(preconditionsTa2915dReusedParent.topLevelASBIEP);
        editBIEPage.moveToQA();
        homePage.logout();

        homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.showAdvancedSearchPanel();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setOwner(userb.getLoginId());
        upliftBIEPage.setDEN(preconditionsTa2915dReusedChild.topLevelASBIEP.getDen());
        upliftBIEPage.hitSearchButton();
        WebElement tr = getUpliftTableRecordForBIE(
                upliftBIEPage, preconditionsTa2915dReusedChild.topLevelASBIEP);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(getDriver(), td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.next();
        editBIEPage = upliftBIEVerificationPage.uplift();
        TopLevelASBIEPObject upliftedReusedChild = editBIEPage.getTopLevelASBIEP();
        editBIEPage.moveToQA();
        editBIEPage.moveToProduction();

        //uplift BIEUserbReusedParent
        upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.showAdvancedSearchPanel();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setOwner(userb.getLoginId());
        upliftBIEPage.setDEN(preconditionsTa2915dReusedParent.topLevelASBIEP.getDen());
        upliftBIEPage.hitSearchButton();
        tr = getUpliftTableRecordForBIE(
                upliftBIEPage, preconditionsTa2915dReusedParent.topLevelASBIEP);
        td = upliftBIEPage.getColumnByName(tr, "select");
        click(getDriver(), td);
        upliftBIEVerificationPage = upliftBIEPage.next();
        selectProfileBIEToReuseDialog = upliftBIEVerificationPage.reuseBIEOnNode("/From UOM Package/Unit Packaging", "Unit Packaging");
        selectProfileBIEToReuseDialog.selectBIEToReuse(upliftedReusedChild);
        editBIEPage = upliftBIEVerificationPage.uplift();
        TopLevelASBIEPObject upliftedReusedParentTopLevelASBIEP = editBIEPage.getTopLevelASBIEP();
        editBIEPage.moveToQA();
        editBIEPage.moveToProduction();

        //Test Assertion Verification

        upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.showAdvancedSearchPanel();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setOwner(userb.getLoginId());
        upliftBIEPage.setDEN(preconditionsTa2915dReusedScenario.topLevelASBIEP.getDen());
        upliftBIEPage.hitSearchButton();
        tr = getUpliftTableRecordForBIE(
                upliftBIEPage, preconditionsTa2915dReusedScenario.topLevelASBIEP);
        td = upliftBIEPage.getColumnByName(tr, "select");
        click(getDriver(), td);
        upliftBIEVerificationPage = upliftBIEPage.next();
        selectProfileBIEToReuseDialog = upliftBIEVerificationPage.reuseBIEOnNode("/UOM Code Conversion Rate/From UOM Package", "From UOM Package");
        assertEquals(0, getDriver().findElements(By.xpath("//*[contains(text(), \"Unit Packaging\")]//ancestor::tr[1]/td[1]/mat-checkbox/label/span[1]")).size());
        selectProfileBIEToReuseDialog.selectBIEToReuse(upliftedReusedParentTopLevelASBIEP);

        editBIEPage = upliftBIEVerificationPage.uplift();
        TopLevelASBIEPObject upliftedReusedScenarioTopLevelASBIEP = editBIEPage.getTopLevelASBIEP();
        asbieNode = editBIEPage.getNodeByPath("/UOM Code Conversion Rate/From UOM Package/Unit Packaging/Dimensions");
        waitFor(ofMillis(1000L));
        asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        assertEnabled(asbiePanel.getUsedCheckbox());
        assertChecked(asbiePanel.getUsedCheckbox());
        assertNotChecked(asbiePanel.getNillableCheckbox());
        assertDisabled(asbiePanel.getNillableCheckbox());
        assertDisabled(asbiePanel.getCardinalityMinField());
        assertDisabled(asbiePanel.getCardinalityMaxField());
        assertEquals("11", getText(asbiePanel.getCardinalityMinField()));
        assertTrue(getText(asbiePanel.getCardinalityMaxField()).startsWith("99"));
        assertEquals(preconditionsTa2915dReusedChild.asbieRemark, getText(asbiePanel.getRemarkField()));
        assertEquals(preconditionsTa2915dReusedChild.asbieContextDefinition, getText(asbiePanel.getContextDefinitionField()));

        WebElement bbieNode = editBIEPage.getNodeByPath("/UOM Code Conversion Rate/From UOM Package/Unit Packaging/Capacity Per Package Quantity");
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEnabled(bbiePanel.getUsedCheckbox());
        assertChecked(bbiePanel.getUsedCheckbox());
        assertChecked(bbiePanel.getNillableCheckbox());
        assertEnabled(bbiePanel.getNillableCheckbox());
        assertEquals("0", getText(bbiePanel.getCardinalityMinField()));
        assertEquals("1", getText(bbiePanel.getCardinalityMaxField()));
        assertEquals(preconditionsTa2915dReusedChild.bbieExample, getText(bbiePanel.getExampleField()));
        assertEquals(preconditionsTa2915dReusedChild.bbieRemark, getText(bbiePanel.getRemarkField()));
        assertEquals(preconditionsTa2915dReusedChild.bbieValueDomainRestriction, getText(bbiePanel.getValueDomainRestrictionSelectField()));
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith(preconditionsTa2915dReusedChild.bbieValueDomain));
        assertEquals(preconditionsTa2915dReusedChild.bbieContextDefinition, getText(bbiePanel.getContextDefinitionField()));

        editBIEPage.getNodeByPath("/UOM Code Conversion Rate/From UOM Package");
        bbieNode = editBIEPage.getNodeByPath("/UOM Code Conversion Rate/From UOM Package/UOM Code");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEnabled(bbiePanel.getUsedCheckbox());
        assertChecked(bbiePanel.getUsedCheckbox());
        assertChecked(bbiePanel.getNillableCheckbox());
        assertEnabled(bbiePanel.getNillableCheckbox());
        assertEquals("0", getText(bbiePanel.getCardinalityMinField()));
        assertEquals("1", getText(bbiePanel.getCardinalityMaxField()));
        assertEquals(preconditionsTa2915dReusedParent.bbieExample, getText(bbiePanel.getExampleField()));
        assertEquals(preconditionsTa2915dReusedParent.bbieRemark, getText(bbiePanel.getRemarkField()));
        assertEquals(preconditionsTa2915dReusedParent.bbieContextDefinition, getText(bbiePanel.getContextDefinitionField()));

        asbieNode = editBIEPage.getNodeByPath("/UOM Code Conversion Rate/From UOM Package");
        waitFor(ofMillis(1000L));
        Set<String> windowHandlesBeforeNodeSelection = new HashSet<>(getDriver().getWindowHandles());
        asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        assertEquals(windowHandlesBeforeNodeSelection, getDriver().getWindowHandles(),
                "Selecting a reused node must not open the reused BIE in another tab");
        assertEnabled(asbiePanel.getUsedCheckbox());
        assertChecked(asbiePanel.getUsedCheckbox());
        assertEquals("0", getText(asbiePanel.getCardinalityMinField()));
        assertEquals("1", getText(asbiePanel.getCardinalityMaxField()));

        // Issue #1735: a reuse node selected during uplift must keep its reference to the
        // reused BIE (it must not be inline-copied). On reopen the uplifted nodes therefore
        // still expose their reuse links in the tree: 'From UOM Package' references the
        // uplifted reused parent, and the nested 'Unit Packaging' references the uplifted
        // reused child.
        viewEditBIEPage.openPage();
        editBIEPage = viewEditBIEPage.openEditBIEPage(upliftedReusedScenarioTopLevelASBIEP);
        editBIEPage.getNodeByPath("/UOM Code Conversion Rate/From UOM Package");
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"From UOM Package\"]//ancestor::div[1]/fa-icon")).size());
        editBIEPage.getNodeByPath("/UOM Code Conversion Rate/From UOM Package/Unit Packaging");
        assertEquals(1, getDriver().findElements(By.xpath("//span[.=\"Unit Packaging\"]//ancestor::div[1]/fa-icon")).size());
        homePage.logout();
    }

    @Test
    @DisplayName("TC_29_1_TA_5_e (warning and inline copy)")
    public void if_a_node_of_the_source_bie_is_a_reuse_node_left_unselected_warn_before_uplift() {
        // Issue #1735 (Problem 2): leaving a mapped reuse node without selecting a
        // BIE to reuse for the target release inline-copies the reuse fields and
        // does NOT keep a reference to the reused BIE. Before creating the uplifted
        // BIE the system must warn the user listing the unselected reuse node(s);
        // if the user continues, the uplifted reuse node carries no reuse reference
        // (contrast with if_a_node_of_the_source_bie_is_a_reuse_node_and_it_was,
        // where selecting a reuse BIE keeps the reference / reuse icon).
        String prev_release = "10.8.7.1";
        String curr_release = "10.9";
        AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(userb);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");

        Preconditions_TA_29_1_5d_BIEReusedChild preconditionsReusedChild =
                preconditions_ta_29_1_5d_ReusedChild(userb, library, prev_release);
        Preconditions_TA_29_1_5d_BIEReusedParent preconditionsReusedParent =
                preconditions_ta_29_1_5d_ReusedParent(userb, library, prev_release);

        // The parent BIE reuses the child on '/From UOM Package/Unit Packaging',
        // then is published so the developer can uplift it.
        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(preconditionsReusedParent.topLevelASBIEP);
        editBIEPage.getNodeByPath("/From UOM Package/Unit Packaging");
        waitFor(ofMillis(1000L));
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog =
                editBIEPage.reuseBIEOnNode("/From UOM Package/Unit Packaging");
        selectProfileBIEToReuseDialog.selectBIEToReuse(preconditionsReusedChild.topLevelASBIEP);
        editBIEPage.moveToQA();
        editBIEPage.moveToProduction();
        homePage.logout();

        // The developer uplifts the parent but leaves the 'Unit Packaging' reuse
        // node unselected on the verification page.
        homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.showAdvancedSearchPanel();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setDEN(preconditionsReusedParent.topLevelASBIEP.getDen());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        // Use the driver-aware click so a lingering snackbar is waited out and the
        // row-select checkbox is scrolled into view before clicking.
        click(getDriver(), td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.next();

        // Submit the report without selecting a reuse BIE: the unselected-reuse
        // warning must appear and list the '/From UOM Package/Unit Packaging' node.
        upliftBIEVerificationPage.submitUpliftReport();
        assertTrue(getText(upliftBIEVerificationPage.getUnselectedReuseWarning())
                .contains("Proceed without selecting reuse BIEs"));
        assertEquals(1, getDriver().findElements(By.xpath(
                "//mat-dialog-container//li[contains(text(), \"Unit Packaging\")]")).size());

        String verificationUrl = getDriver().getCurrentUrl();
        By warningDialog = By.xpath("//mat-dialog-container[.//span[contains(., 'Proceed without selecting reuse BIEs')]]");
        click(getDriver(), getDriver().findElement(warningDialog).findElement(
                By.xpath(".//button[.//span[normalize-space(.)='Cancel']]")));
        invisibilityOfElementLocated(getDriver(), warningDialog);
        assertEquals(verificationUrl, getDriver().getCurrentUrl());
        assertTrue(upliftBIEVerificationPage.getNextButton().isEnabled());
        upliftBIEVerificationPage.submitUpliftReport();
        assertTrue(upliftBIEVerificationPage.getUnselectedReuseWarning().isDisplayed());

        // Continue: the reuse fields are inline-copied with no reference kept, so
        // the uplifted 'Unit Packaging' node exposes no reuse icon in the tree.
        editBIEPage = upliftBIEVerificationPage.confirmUnselectedReuseAndUplift();
        TopLevelASBIEPObject upliftedParentTopLevelASBIEP = editBIEPage.getTopLevelASBIEP();
        viewEditBIEPage.openPage();
        editBIEPage = viewEditBIEPage.openEditBIEPage(upliftedParentTopLevelASBIEP);
        editBIEPage.getNodeByPath("/From UOM Package/Unit Packaging");
        assertEquals(0, getDriver().findElements(By.xpath(
                "//span[.=\"Unit Packaging\"]//ancestor::div[1]/fa-icon")).size());
        homePage.logout();
    }

    @Test
    @DisplayName("TC_29_1_TA_5_e, TC_29_1_TA_5_g (distinct children)")
    public void all_selected_and_unselected_combinations_of_two_bom_item_data_reuses_are_uplifted() {
        String sourceRelease = "10.8.7.1";
        String targetRelease = "10.9";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");

        AppUserObject firstChildOwner = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        AppUserObject secondChildOwner = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        AppUserObject parentOwner = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        Arrays.asList(firstChildOwner, secondChildOwner, parentOwner, developer)
                .forEach(this::thisAccountWillBeDeletedAfterTests);

        ASCCPObject bomAsccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM. BOM", sourceRelease);
        ASCCPObject bomItemDataAsccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM Item Data. BOM Item Data", sourceRelease);

        TopLevelASBIEPObject firstSourceChild = createAndPublishBOMReuseBIE(
                firstChildOwner, bomItemDataAsccp);
        TopLevelASBIEPObject secondSourceChild = createAndPublishBOMReuseBIE(
                secondChildOwner, bomItemDataAsccp);

        TopLevelASBIEPObject firstTargetChild = upliftAndPublishBOMReuseBIE(
                developer, firstChildOwner, firstSourceChild, sourceRelease, targetRelease, false);
        TopLevelASBIEPObject secondTargetChild = upliftAndPublishBOMReuseBIE(
                developer, secondChildOwner, secondSourceChild, sourceRelease, targetRelease, false);

        TopLevelASBIEPObject sourceParent = createBOMParentWithTwoReuses(
                parentOwner, bomAsccp, firstSourceChild, secondSourceChild);

        List<BOMReuseCombination> combinations = Arrays.asList(
                new BOMReuseCombination(true, true),
                new BOMReuseCombination(false, true),
                new BOMReuseCombination(true, false),
                new BOMReuseCombination(false, false));
        for (BOMReuseCombination combination : combinations) {
            upliftBOMParentForCombination(
                    developer,
                    parentOwner,
                    sourceParent,
                    firstTargetChild,
                    secondTargetChild,
                    sourceRelease,
                    targetRelease,
                    combination);
        }
    }

    @Test
    @DisplayName("TC_29_1_TA_5_g, TC_29_1_TA_13 (same child)")
    public void the_same_bom_item_data_reuse_with_a_nested_party_is_inlined_at_both_target_paths() {
        String sourceRelease = "10.8.7.1";
        String targetRelease = "10.9";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");

        AppUserObject partyOwner = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        AppUserObject childOwner = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        AppUserObject parentOwner = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        Arrays.asList(partyOwner, childOwner, parentOwner, developer)
                .forEach(this::thisAccountWillBeDeletedAfterTests);

        ASCCPObject bomAsccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM. BOM", sourceRelease);
        ASCCPObject bomItemDataAsccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM Item Data. BOM Item Data", sourceRelease);
        ASCCPObject partyAsccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "Party. Party", sourceRelease);

        TopLevelASBIEPObject nestedParty = createAndPublishBOMReuseBIE(partyOwner, partyAsccp);
        TopLevelASBIEPObject sourceChild = createAndPublishBOMReuseBIEWithNestedParty(
                childOwner, bomItemDataAsccp, nestedParty);
        assertEquals(Collections.singletonList(nestedParty.getTopLevelAsbiepId()),
                getAPIFactory().getBusinessInformationEntityAPI()
                        .getReusedTopLevelAsbiepIds(sourceChild.getTopLevelAsbiepId()),
                "The source BOM Item Data must retain its nested Party reuse");
        TopLevelASBIEPObject targetChild = upliftAndPublishBOMReuseBIE(
                developer, childOwner, sourceChild, sourceRelease, targetRelease, true);
        assertTrue(getAPIFactory().getBusinessInformationEntityAPI()
                        .getReusedTopLevelAsbiepIds(targetChild.getTopLevelAsbiepId()).isEmpty(),
                "The unselected nested Party reuse must be inlined in the uplifted BOM Item Data");
        int nestedBOMItemDataBbieScCount = getAPIFactory().getBusinessInformationEntityAPI()
                .countBbieSc(targetChild.getTopLevelAsbiepId());
        TopLevelASBIEPObject sourceParent = createBOMParentWithTwoReuses(
                parentOwner, bomAsccp, sourceChild, sourceChild);
        List<BigInteger> sourceParentReuseIds = getAPIFactory().getBusinessInformationEntityAPI()
                .getReusedTopLevelAsbiepIds(sourceParent.getTopLevelAsbiepId());
        assertEquals(2, sourceParentReuseIds.size(),
                "The source BOM must contain two reuse occurrences");
        assertEquals(2, Collections.frequency(sourceParentReuseIds, sourceChild.getTopLevelAsbiepId()),
                "Both source BOM occurrences must reuse the same BOM Item Data");

        // Both target occurrences reference the same source BIE, and the nested Party is also
        // reused. This reproduces the duplicate source occurrence from the reported failure.
        // Exercise all four choices. Every unselected occurrence must inline an independent
        // copy of the already-inlined BOM Item Data, including its nested Party.
        List<BOMReuseCombination> combinations = Arrays.asList(
                new BOMReuseCombination(true, true),
                new BOMReuseCombination(false, true),
                new BOMReuseCombination(true, false),
                new BOMReuseCombination(false, false));
        for (BOMReuseCombination combination : combinations) {
            int inlineOccurrenceCount = (combination.firstSelected ? 0 : 1)
                    + (combination.secondSelected ? 0 : 1);
            upliftBOMParentForCombination(
                    developer, parentOwner, sourceParent, targetChild, targetChild,
                    sourceRelease, targetRelease, combination,
                    inlineOccurrenceCount * nestedBOMItemDataBbieScCount,
                    true);
        }
    }

    @Test
    @DisplayName("TC_29_1_TA_5_h, TC_29_1_TA_13")
    public void nested_reuse_uplift_supports_base_and_inherited_bies_at_repeated_occurrences() {
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        String sourceRelease = "10.8.7.1";
        String targetRelease = "10.9";

        AppUserObject partyOwner = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        AppUserObject childOwner = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        AppUserObject parentOwner = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        Arrays.asList(partyOwner, childOwner, parentOwner, developer)
                .forEach(this::thisAccountWillBeDeletedAfterTests);

        ASCCPObject bomAsccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM. BOM", sourceRelease);
        ASCCPObject bomItemDataAsccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM Item Data. BOM Item Data", sourceRelease);
        ASCCPObject partyAsccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "Party. Party", sourceRelease);

        TopLevelASBIEPObject sourceParty = createAndPublishBOMReuseBIE(partyOwner, partyAsccp);
        TopLevelASBIEPObject sourceBaseChild = createAndPublishBOMReuseBIEWithNestedParty(
                childOwner, bomItemDataAsccp, sourceParty);

        TopLevelASBIEPObject targetBaseChild = upliftAndPublishBOMReuseBIE(
                developer, childOwner, sourceBaseChild, sourceRelease, targetRelease, true);
        int inlinedBOMItemDataBbieScCount = getAPIFactory().getBusinessInformationEntityAPI()
                .countBbieSc(targetBaseChild.getTopLevelAsbiepId());
        assertEquals(2, inlinedBOMItemDataBbieScCount,
                "The uplifted BOM Item Data must own its BBIE_SC and the inlined Party BBIE_SC");
        TopLevelASBIEPObject targetInheritedChild = createAndPublishInheritedBIE(developer, targetBaseChild);
        assertEquals(targetBaseChild.getTopLevelAsbiepId(), targetInheritedChild.getBasedTopLevelAsbiepId(),
                "An inherited BIE must retain the base top-level ASBIEP id");

        TopLevelASBIEPObject sourceBaseParent = createBOMParentWithTwoReuses(
                parentOwner, bomAsccp, sourceBaseChild, sourceBaseChild);
        TopLevelASBIEPObject sourceInheritedParent = createAndPublishInheritedBIE(
                parentOwner, sourceBaseParent);

        // The base source parent verifies that the two occurrences can retain different target
        // identities: one base BIE and one inherited BIE. The inherited source parent then mixes
        // inline copy with an inherited target reference. The four generic selected/unselected
        // combinations are covered separately by the repeated-occurrence regression above.
        upliftBOMParentForCombination(
                developer, parentOwner, sourceBaseParent,
                targetBaseChild, targetInheritedChild,
                sourceRelease, targetRelease, new BOMReuseCombination(true, true));
        upliftBOMParentForCombination(
                developer, parentOwner, sourceInheritedParent,
                targetBaseChild, targetInheritedChild,
                sourceRelease, targetRelease, new BOMReuseCombination(false, true),
                inlinedBOMItemDataBbieScCount, true);
    }

    @Test
    @DisplayName("TC_29_1_TA_5_f, TC_29_1_TA_5_g")
    public void nested_reuse_uplift_supports_valid_custom_mapping_and_rejects_cross_parent_mapping() {
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        String sourceRelease = "10.8.7.1";
        String targetRelease = "10.9";

        AppUserObject partyOwner = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        AppUserObject childOwner = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        AppUserObject parentOwner = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        Arrays.asList(partyOwner, childOwner, parentOwner, developer)
                .forEach(this::thisAccountWillBeDeletedAfterTests);

        ASCCPObject bomAsccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM. BOM", sourceRelease);
        ASCCPObject bomItemDataAsccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM Item Data. BOM Item Data", sourceRelease);
        ASCCPObject partyAsccp = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "Party. Party", sourceRelease);

        TopLevelASBIEPObject sourceParty = createAndPublishBOMReuseBIE(partyOwner, partyAsccp);
        TopLevelASBIEPObject sourceChild = createAndPublishBOMReuseBIEWithNestedParty(
                childOwner, bomItemDataAsccp, sourceParty);
        TopLevelASBIEPObject targetChild = upliftAndPublishBOMReuseBIE(
                developer, childOwner, sourceChild, sourceRelease, targetRelease, true);
        int expectedInlineBbieScCount = getAPIFactory().getBusinessInformationEntityAPI()
                .countBbieSc(targetChild.getTopLevelAsbiepId());
        TopLevelASBIEPObject sourceParent = createBOMParentWithTwoReuses(
                parentOwner, bomAsccp, sourceChild, sourceChild);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        UpliftBIEPage upliftBIEPage = homePage.getBIEMenu().openUpliftBIESubMenu();
        upliftBIEPage.showAdvancedSearchPanel();
        upliftBIEPage.setSourceBranch(sourceRelease);
        upliftBIEPage.setTargetBranch(targetRelease);
        upliftBIEPage.setState("Production");
        upliftBIEPage.setOwner(parentOwner.getLoginId());
        upliftBIEPage.setDEN(sourceParent.getDen());
        upliftBIEPage.hitSearchButton();
        WebElement row = getUpliftTableRecordForBIE(upliftBIEPage, sourceParent);
        click(getDriver(), upliftBIEPage.getColumnByName(row, "select"));
        UpliftBIEVerificationPage verificationPage = upliftBIEPage.next();

        String secondParentPath = "/BOM/BOM Option/BOM Item Data";
        verificationPage.goToNodeInSourceBIE(secondParentPath);
        verificationPage.goToNodeInTargetBIE(secondParentPath);
        WebElement parentCheckbox = verificationPage.getCheckBoxOfNodeInTargetBIE(secondParentPath);
        assertChecked(parentCheckbox);
        click(getDriver(), parentCheckbox);
        verificationPage.goToNodeInSourceBIE(secondParentPath + "/Party");
        WebElement unmappedParentTarget = verificationPage.goToNodeInTargetBIE(secondParentPath + "/Manufacturing Party");
        assertFalse(unmappedParentTarget.findElement(By.cssSelector("mat-checkbox input")).isEnabled(),
                "A descendant cannot map while its structural parent is unmapped");
        verificationPage.mapNode(secondParentPath, secondParentPath);

        // The second source occurrence may map to another compatible Party association under
        // the same parent, but it must not cross into the first BOM Item Data occurrence.
        verificationPage.goToNodeInSourceBIEByExpandingPath(
                "/BOM/BOM Option/BOM Item Data/Party");
        WebElement wrongParentTarget = verificationPage.goToNodeInTargetBIEByExpandingPath(
                "/BOM/BOM Item Data/Manufacturing Party");
        assertFalse(wrongParentTarget.findElement(By.cssSelector("mat-checkbox input")).isEnabled());

        WebElement validCustomTarget = verificationPage.goToNodeInTargetBIEByExpandingPath(
                "/BOM/BOM Option/BOM Item Data/Manufacturing Party");
        WebElement validCustomTargetCheckbox = validCustomTarget.findElement(By.xpath("./mat-checkbox[1]"));
        assertTrue(validCustomTargetCheckbox.findElement(By.cssSelector("input")).isEnabled());
        verificationPage.mapNode("/BOM/BOM Option/BOM Item Data/Party",
                "/BOM/BOM Option/BOM Item Data/Manufacturing Party");

        // Keep a target reuse reference at the first occurrence while the second occurrence is
        // inline-copied with its nested Party manually mapped to Manufacturing Party.
        SelectProfileBIEToReuseDialog dialog = verificationPage.reuseBIEOnNodeByExpandingPath(
                "/BOM/BOM Item Data", "BOM Item Data");
        dialog.selectBIEToReuse(targetChild);
        WebElement coveredSource = verificationPage.goToNodeInSourceBIE("/BOM/BOM Item Data/Party");
        WebElement coveredTarget = verificationPage.goToNodeInTargetBIE("/BOM/BOM Item Data/Party");
        assertTrue(coveredSource.findElements(By.xpath("./mat-checkbox")).isEmpty());
        assertTrue(coveredTarget.findElements(By.xpath("./mat-checkbox")).isEmpty());
        verificationPage.submitUpliftReport();
        assertTrue(getText(verificationPage.getUnselectedReuseWarning())
                .contains("Proceed without selecting reuse BIEs"));
        EditBIEPage editBIEPage = verificationPage.confirmUnselectedReuseAndUplift();

        assertBOMReuseReference(editBIEPage, "/BOM/BOM Item Data", true, targetChild.getVersion());
        assertBOMReuseReference(editBIEPage, "/BOM/BOM Option/BOM Item Data", false,
                targetChild.getVersion());
        WebElement mappedManufacturingParty = editBIEPage.getNodeByPath(
                "/BOM/BOM Option/BOM Item Data/Manufacturing Party");
        assertTrue(mappedManufacturingParty.findElements(By.xpath(
                ".//fa-icon[@mattooltip=\"Reused\"]")).isEmpty());

        BigInteger upliftedId = editBIEPage.getTopLevelASBIEP().getTopLevelAsbiepId();
        assertEquals(Collections.singletonList(targetChild.getTopLevelAsbiepId()),
                getAPIFactory().getBusinessInformationEntityAPI().getReusedTopLevelAsbiepIds(upliftedId));
        List<String> bbieScPaths = getAPIFactory().getBusinessInformationEntityAPI()
                .getBbiePathsHavingBbieSc(upliftedId);
        assertEquals(expectedInlineBbieScCount, bbieScPaths.size());
        assertEquals(expectedInlineBbieScCount, new HashSet<>(bbieScPaths).size());
        homePage.logout();
    }

    private TopLevelASBIEPObject createAndPublishBOMReuseBIE(
            AppUserObject owner, ASCCPObject asccp) {
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI()
                .createRandomBusinessContext(owner);
        TopLevelASBIEPObject bie = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, owner, "WIP");
        getAPIFactory().getBusinessInformationEntityAPI().createBbieNodesForUsedElements(
                bie.getTopLevelAsbiepId(), owner.getAppUserId());
        if ("Party".equals(asccp.getPropertyTerm())) {
            getAPIFactory().getBusinessInformationEntityAPI().createBbieScForBbieAndDtSc(
                    bie.getTopLevelAsbiepId(), owner.getAppUserId(),
                    "Identifier", "Scheme Agency", "Identifier");
        } else {
            getAPIFactory().getBusinessInformationEntityAPI().createBbieScForFirstBbie(
                    bie.getTopLevelAsbiepId(), owner.getAppUserId());
        }
        String sourceVersion = "source-" + owner.getLoginId();
        bie.setVersion(sourceVersion);
        setBIEToProduction(bie);
        return bie;
    }

    private TopLevelASBIEPObject createAndPublishInheritedBIE(
            AppUserObject owner, TopLevelASBIEPObject baseBIE) {
        HomePage homePage = loginPage().signIn(owner.getLoginId(), owner.getPassword());
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        viewEditBIEPage.showAdvancedSearchPanel();
        viewEditBIEPage.setState("Production");
        viewEditBIEPage.setOwner(owner.getLoginId());
        viewEditBIEPage.setDEN(baseBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        viewEditBIEPage.hitCreateInheritedBIE(getBIERecordById(viewEditBIEPage,
                baseBIE.getTopLevelAsbiepId()));

        TopLevelASBIEPObject inheritedBIE = await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .until(
                        () -> getAPIFactory().getBusinessInformationEntityAPI()
                                .getLatestInheritedTopLevelASBIEP(
                                        baseBIE.getTopLevelAsbiepId(), owner.getAppUserId()),
                        Objects::nonNull);

        viewEditBIEPage.openPage();
        viewEditBIEPage.showAdvancedSearchPanel();
        viewEditBIEPage.setState("WIP");
        viewEditBIEPage.setOwner(owner.getLoginId());
        viewEditBIEPage.setDEN(baseBIE.getDen());
        viewEditBIEPage.hitSearchButton();
        WebElement inheritedRecord = getBIERecordById(
                viewEditBIEPage, inheritedBIE.getTopLevelAsbiepId());

        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(inheritedRecord);
        assertNotNull(editBIEPage.getTopLevelASBIEPPanel().getBaseTopLevelASBIEPPanel(),
                "The inherited BIE must expose its base panel before uplift");
        assertEquals(inheritedBIE.getTopLevelAsbiepId(),
                editBIEPage.getTopLevelASBIEP().getTopLevelAsbiepId());
        editBIEPage.moveToQA();
        editBIEPage.moveToProduction();
        homePage.logout();
        return inheritedBIE;
    }

    private TopLevelASBIEPObject createAndPublishBOMReuseBIEWithNestedParty(
            AppUserObject owner, ASCCPObject asccp, TopLevelASBIEPObject nestedParty) {
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI()
                .createRandomBusinessContext(owner);
        TopLevelASBIEPObject bie = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, owner, "WIP");
        getAPIFactory().getBusinessInformationEntityAPI().createBbieNodesForUsedElements(
                bie.getTopLevelAsbiepId(), owner.getAppUserId());
        getAPIFactory().getBusinessInformationEntityAPI().createBbieScForFirstBbie(
                bie.getTopLevelAsbiepId(), owner.getAppUserId());

        HomePage homePage = loginPage().signIn(owner.getLoginId(), owner.getPassword());
        EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(bie);
        SelectProfileBIEToReuseDialog dialog = editBIEPage.reuseBIEOnNode(
                "/" + asccp.getPropertyTerm() + "/Party");
        dialog.selectBIEToReuse(nestedParty);
        homePage.logout();

        bie.setVersion("source-" + owner.getLoginId());
        setBIEToProduction(bie);
        return bie;
    }

    private TopLevelASBIEPObject upliftAndPublishBOMReuseBIE(
            AppUserObject developer,
            AppUserObject sourceOwner,
            TopLevelASBIEPObject sourceBIE,
            String sourceRelease,
            String targetRelease,
            boolean confirmUnselectedReuse) {
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        UpliftBIEPage upliftBIEPage = homePage.getBIEMenu().openUpliftBIESubMenu();
        upliftBIEPage.showAdvancedSearchPanel();
        upliftBIEPage.setSourceBranch(sourceRelease);
        upliftBIEPage.setTargetBranch(targetRelease);
        upliftBIEPage.setState("Production");
        upliftBIEPage.setOwner(sourceOwner.getLoginId());
        upliftBIEPage.setDEN(sourceBIE.getDen());
        upliftBIEPage.hitSearchButton();
        WebElement row = upliftBIEPage.getTableRecordAtIndex(1);
        click(getDriver(), upliftBIEPage.getColumnByName(row, "select"));
        UpliftBIEVerificationPage verificationPage = upliftBIEPage.next();
        EditBIEPage editBIEPage;
        if (confirmUnselectedReuse) {
            // The warning considers loaded reuse nodes; load the nested Party before reporting.
            verificationPage.goToNodeInSourceBIE("/BOM Item Data/Party");
            verificationPage.submitUpliftReport();
            assertTrue(getText(verificationPage.getUnselectedReuseWarning())
                    .contains("Proceed without selecting reuse BIEs"));
            editBIEPage = verificationPage.confirmUnselectedReuseAndUplift();
        } else {
            editBIEPage = verificationPage.uplift();
        }
        TopLevelASBIEPObject upliftedBIE = editBIEPage.getTopLevelASBIEP();
        editBIEPage.moveToQA();
        editBIEPage.moveToProduction();
        homePage.logout();
        return upliftedBIE;
    }

    private TopLevelASBIEPObject createBOMParentWithTwoReuses(
            AppUserObject owner,
            ASCCPObject bomAsccp,
            TopLevelASBIEPObject firstSourceChild,
            TopLevelASBIEPObject secondSourceChild) {
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI()
                .createRandomBusinessContext(owner);
        TopLevelASBIEPObject parent = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Collections.singletonList(context), bomAsccp, owner, "WIP");

        HomePage homePage = loginPage().signIn(owner.getLoginId(), owner.getPassword());
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(parent);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode(
                "/BOM/BOM Item Data");
        selectProfileBIEToReuseDialog.selectBIEToReuse(firstSourceChild);
        editBIEPage.openPage();
        selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode(
                "/BOM/BOM Option/BOM Item Data");
        selectProfileBIEToReuseDialog.selectBIEToReuse(secondSourceChild);
        editBIEPage.moveToQA();
        editBIEPage.moveToProduction();
        homePage.logout();
        return parent;
    }

    private WebElement getBIERecordById(ViewEditBIEPage viewEditBIEPage, BigInteger topLevelAsbiepId) {
        for (int i = 1; i <= viewEditBIEPage.getTotalNumberOfItems(); i++) {
            WebElement row = viewEditBIEPage.getTableRecordAtIndex(i);
            for (WebElement link : row.findElements(By.cssSelector("a[href*='/profile_bie/']"))) {
                if (link.getAttribute("href").endsWith("/profile_bie/" + topLevelAsbiepId)) {
                    return row;
                }
            }
        }
        fail("Cannot find BIE record with id " + topLevelAsbiepId);
        return null;
    }

    private WebElement getUpliftTableRecordForBIE(
            UpliftBIEPage upliftBIEPage, TopLevelASBIEPObject sourceBIE) {
        for (int i = 1; i <= 10; i++) {
            try {
                WebElement row = upliftBIEPage.getTableRecordAtIndex(i);
                for (WebElement link : row.findElements(By.cssSelector("a[href*='/profile_bie/']"))) {
                    if (link.getAttribute("href").endsWith("/profile_bie/" + sourceBIE.getTopLevelAsbiepId())) {
                        return row;
                    }
                }
            } catch (TimeoutException | NoSuchElementException e) {
                break;
            }
        }
        fail("Cannot find uplift source BIE record with id " + sourceBIE.getTopLevelAsbiepId());
        return null;
    }

    private void upliftBOMParentForCombination(
            AppUserObject developer,
            AppUserObject sourceOwner,
            TopLevelASBIEPObject sourceParent,
            TopLevelASBIEPObject firstTargetChild,
            TopLevelASBIEPObject secondTargetChild,
            String sourceRelease,
            String targetRelease,
            BOMReuseCombination combination) {
        int expectedBbieScCount = (combination.firstSelected ? 0 : 1)
                + (combination.secondSelected ? 0 : 1);
        upliftBOMParentForCombination(
                developer,
                sourceOwner,
                sourceParent,
                firstTargetChild,
                secondTargetChild,
                sourceRelease,
                targetRelease,
                combination,
                expectedBbieScCount,
                false);
    }

    private void upliftBOMParentForCombination(
            AppUserObject developer,
            AppUserObject sourceOwner,
            TopLevelASBIEPObject sourceParent,
            TopLevelASBIEPObject firstTargetChild,
            TopLevelASBIEPObject secondTargetChild,
            String sourceRelease,
            String targetRelease,
            BOMReuseCombination combination,
            int expectedBbieScCount,
            boolean assertNestedParty) {
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        UpliftBIEPage upliftBIEPage = homePage.getBIEMenu().openUpliftBIESubMenu();
        upliftBIEPage.showAdvancedSearchPanel();
        upliftBIEPage.setSourceBranch(sourceRelease);
        upliftBIEPage.setTargetBranch(targetRelease);
        upliftBIEPage.setState("Production");
        upliftBIEPage.setOwner(sourceOwner.getLoginId());
        upliftBIEPage.setDEN(sourceParent.getDen());
        upliftBIEPage.hitSearchButton();
        WebElement row = getUpliftTableRecordForBIE(upliftBIEPage, sourceParent);
        click(getDriver(), upliftBIEPage.getColumnByName(row, "select"));
        UpliftBIEVerificationPage verificationPage = upliftBIEPage.next();

        if (assertNestedParty) {
            // Regression coverage for inherited/used descendants below a nested reuse:
            // the source and target trees must expose the full Party -> Identifier ->
            // Scheme Agency Identifier path so it can participate in mapping.
            WebElement sourceSchemeAgencyIdentifier = verificationPage.goToNodeInSourceBIE(
                    "/BOM/BOM Item Data/Party/Identifier/Scheme Agency Identifier");
            WebElement targetSchemeAgencyIdentifier = verificationPage.goToNodeInTargetBIE(
                    "/BOM/BOM Item Data/Party/Identifier/Scheme Agency Identifier");
            assertChecked(sourceSchemeAgencyIdentifier.findElement(By.xpath("./mat-checkbox[1]")));
            WebElement targetCheckbox = targetSchemeAgencyIdentifier.findElement(By.xpath("./mat-checkbox[1]"));
            assertTrue(targetCheckbox.isDisplayed(),
                    "The nested Scheme Agency Identifier mapping control must be visible in the target tree");
        }

        if (combination.firstSelected) {
            SelectProfileBIEToReuseDialog dialog = verificationPage.reuseBIEOnNodeByExpandingPath(
                    "/BOM/BOM Item Data", "BOM Item Data");
            dialog.selectBIEToReuse(firstTargetChild);
        }
        if (combination.secondSelected) {
            SelectProfileBIEToReuseDialog dialog = verificationPage.reuseBIEOnNodeByExpandingPath(
                    "/BOM/BOM Option/BOM Item Data", "BOM Item Data");
            dialog.selectBIEToReuse(secondTargetChild);
        }

        EditBIEPage editBIEPage;
        if (combination.firstSelected && combination.secondSelected) {
            editBIEPage = verificationPage.uplift();
        } else {
            verificationPage.submitUpliftReport();
            assertTrue(getText(verificationPage.getUnselectedReuseWarning())
                    .contains("Proceed without selecting reuse BIEs"));
            editBIEPage = verificationPage.confirmUnselectedReuseAndUplift();
        }

        assertBOMReuseReference(editBIEPage, "/BOM/BOM Item Data", combination.firstSelected,
                firstTargetChild.getVersion());
        assertBOMReuseReference(editBIEPage, "/BOM/BOM Option/BOM Item Data", combination.secondSelected,
                secondTargetChild.getVersion());
        if (!combination.firstSelected && assertNestedParty) {
            assertInlinedBOMItemDataParty(editBIEPage, "/BOM/BOM Item Data");
        }
        if (!combination.secondSelected && assertNestedParty) {
            assertInlinedBOMItemDataParty(editBIEPage, "/BOM/BOM Option/BOM Item Data");
        }
        List<BigInteger> expectedReferences = new ArrayList<>();
        if (combination.firstSelected) {
            expectedReferences.add(firstTargetChild.getTopLevelAsbiepId());
        }
        if (combination.secondSelected) {
            expectedReferences.add(secondTargetChild.getTopLevelAsbiepId());
        }
        List<BigInteger> actualReferences = new ArrayList<>(getAPIFactory().getBusinessInformationEntityAPI()
                .getReusedTopLevelAsbiepIds(editBIEPage.getTopLevelASBIEP().getTopLevelAsbiepId()));
        Collections.sort(expectedReferences);
        Collections.sort(actualReferences);
        assertEquals(expectedReferences, actualReferences);
        for (TopLevelASBIEPObject targetChild : Arrays.asList(firstTargetChild, secondTargetChild)) {
            if (expectedReferences.contains(targetChild.getTopLevelAsbiepId()) && targetChild.getBasedTopLevelAsbiepId() != null) {
                assertEquals(targetChild.getBasedTopLevelAsbiepId(),
                        getAPIFactory().getBusinessInformationEntityAPI()
                                .getTopLevelASBIEPByID(targetChild.getTopLevelAsbiepId()).getBasedTopLevelAsbiepId());
            }
        }
        BigInteger upliftedTopLevelAsbiepId = editBIEPage.getTopLevelASBIEP().getTopLevelAsbiepId();
        if (sourceParent.getBasedTopLevelAsbiepId() != null) {
            assertNull(getAPIFactory().getBusinessInformationEntityAPI()
                    .getTopLevelASBIEPByID(upliftedTopLevelAsbiepId).getBasedTopLevelAsbiepId(),
                    "Uplift creates an independent target BIE, not an inheritance link to the old release");
        }
        assertEquals(expectedBbieScCount, getAPIFactory().getBusinessInformationEntityAPI()
                .countBbieSc(upliftedTopLevelAsbiepId));
        List<String> bbiePathsHavingBbieSc = getAPIFactory().getBusinessInformationEntityAPI()
                .getBbiePathsHavingBbieSc(upliftedTopLevelAsbiepId);
        assertEquals(expectedBbieScCount, bbiePathsHavingBbieSc.size());
        assertEquals(expectedBbieScCount, new HashSet<>(bbiePathsHavingBbieSc).size());
        assertTrue(getAPIFactory().getBusinessInformationEntityAPI()
                .hasValidBbieOwnership(upliftedTopLevelAsbiepId));
        homePage.logout();
    }

    private void assertBOMReuseReference(
            EditBIEPage editBIEPage, String path, boolean expected, String expectedVersion) {
        WebElement node = editBIEPage.getNodeByPath(path);
        boolean hasReuseIcon = !node.findElements(By.xpath(".//fa-icon[@mattooltip=\"Reused\"]")).isEmpty();
        assertEquals(expected, hasReuseIcon, path);
        if (expected) {
            String mainWindowHandle = getDriver().getWindowHandle();
            Set<String> windowHandlesBeforePanelClick = new HashSet<>(getDriver().getWindowHandles());
            EditBIEPage.ReusedASBIEPanel reusedPanel = editBIEPage.getReusedASBIEPanel(node);
            assertEquals(mainWindowHandle, getDriver().getWindowHandle(),
                    "Selecting a reused ASBIEP must keep the main browser tab active: " + path);
            assertEquals(windowHandlesBeforePanelClick, getDriver().getWindowHandles(),
                    "Selecting a reused ASBIEP must not open the reused BIE in another tab: " + path);
            assertEquals(expectedVersion, getText(reusedPanel.getVersionField()), path);
        }
    }

    private void assertInlinedBOMItemDataParty(EditBIEPage editBIEPage, String bomItemDataPath) {
        WebElement partyNode = editBIEPage.getNodeByPath(bomItemDataPath + "/Party");
        assertTrue(partyNode.findElements(By.xpath(
                ".//fa-icon[@mattooltip=\"Reused\"]")).isEmpty(), bomItemDataPath);
    }

    private void setBIEToProduction(TopLevelASBIEPObject bie) {
        bie.setState("Production");
        getAPIFactory().getBusinessInformationEntityAPI().updateTopLevelASBIEP(bie);
    }

    private static final class BOMReuseCombination {
        private final boolean firstSelected;
        private final boolean secondSelected;

        private BOMReuseCombination(boolean firstSelected, boolean secondSelected) {
            this.firstSelected = firstSelected;
            this.secondSelected = secondSelected;
        }
    }

    private Preconditions_TA_29_1_5d_BIEReusedChild preconditions_ta_29_1_5d_ReusedChild(
            AppUserObject usera, LibraryObject library, String prevRelease) {
        Preconditions_TA_29_1_5d_BIEReusedChild preconditionsTa2915d = new Preconditions_TA_29_1_5d_BIEReusedChild(usera, library, prevRelease);
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(preconditionsTa2915d.topLevelASBIEP);

        editBIEPage.getNodeByPath(preconditionsTa2915d.bbiePath);
        WebElement bbieNode = editBIEPage.getNodeByPath(preconditionsTa2915d.bbiePath);
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setRemark(preconditionsTa2915d.bbieRemark);
        bbiePanel.setExample(preconditionsTa2915d.bbieExample);
        bbiePanel.setContextDefinition(preconditionsTa2915d.bbieContextDefinition);
        bbiePanel.setValueConstraint(preconditionsTa2915d.bbieValueConstraint);
        bbiePanel.setFixedValue(preconditionsTa2915d.bbieFixedValue);
        bbiePanel.setValueDomainRestriction(preconditionsTa2915d.bbieValueDomainRestriction);
        bbiePanel.setValueDomain(preconditionsTa2915d.bbieValueDomain);
        editBIEPage.hitUpdateButton();


        editBIEPage.getNodeByPath(preconditionsTa2915d.asbiePath);
        WebElement asbieNode = editBIEPage.getNodeByPath(preconditionsTa2915d.asbiePath);
        waitFor(ofMillis(1000L));
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        asbiePanel.toggleUsed();
        asbiePanel.setCardinalityMax(99);
        asbiePanel.setCardinalityMin(11);
        asbiePanel.setRemark(preconditionsTa2915d.asbieRemark);
        asbiePanel.setContextDefinition(preconditionsTa2915d.asbieContextDefinition);
        editBIEPage.hitUpdateButton();

        WebElement bbieScNode = editBIEPage.getNodeByPath(preconditionsTa2915d.bbieScPath);
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbieScNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setRemark(preconditionsTa2915d.bbieScRemark);
        bbiescPanel.setExample(preconditionsTa2915d.bbieScExample);
        bbiescPanel.setValueConstraint(preconditionsTa2915d.bbieScValueConstraint);
        bbiescPanel.setFixedValue(preconditionsTa2915d.bbieScFixedValue);
        bbiescPanel.setValueDomainRestriction(preconditionsTa2915d.bbieScValueDomainRestriction);
        bbiescPanel.setValueDomain(preconditionsTa2915d.bbieScValueDomain);
        bbiescPanel.setContextDefinition(preconditionsTa2915d.bbieScContextDefinition);
        editBIEPage.hitUpdateButton();
        editBIEPage.moveToQA();
        editBIEPage.moveToProduction();
        homePage.logout();
        return preconditionsTa2915d;
    }

    private Preconditions_TA_29_1_5d_BIEReusedParent preconditions_ta_29_1_5d_ReusedParent(
            AppUserObject usera, LibraryObject library, String prevRelease) {
        Preconditions_TA_29_1_5d_BIEReusedParent preconditionsTa2915d = new Preconditions_TA_29_1_5d_BIEReusedParent(usera, library, prevRelease);
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(preconditionsTa2915d.topLevelASBIEP);

        editBIEPage.getNodeByPath(preconditionsTa2915d.bbiePath);
        WebElement bbieNode = editBIEPage.getNodeByPath(preconditionsTa2915d.bbiePath);
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setRemark(preconditionsTa2915d.bbieRemark);
        bbiePanel.setExample(preconditionsTa2915d.bbieExample);
        bbiePanel.setContextDefinition(preconditionsTa2915d.bbieContextDefinition);
        bbiePanel.setValueConstraint(preconditionsTa2915d.bbieValueConstraint);
        bbiePanel.setFixedValue(preconditionsTa2915d.bbieFixedValue);
        editBIEPage.hitUpdateButton();

        editBIEPage.getNodeByPath(preconditionsTa2915d.asbiePath);
        WebElement asbieNode = editBIEPage.getNodeByPath(preconditionsTa2915d.asbiePath);
        waitFor(ofMillis(1000L));
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        asbiePanel.toggleUsed();
        asbiePanel.setRemark(preconditionsTa2915d.asbieRemark);
        asbiePanel.setContextDefinition(preconditionsTa2915d.asbieContextDefinition);
        editBIEPage.hitUpdateButton();
        homePage.logout();
        return preconditionsTa2915d;
    }

    @Test
    @DisplayName("TC_29_1_TA_9_a")
    public void be_transferred_to_the_target_bbie_or_bbie_sc_node_in_case_of_system() {
        String prev_release = "10.8.7.1";
        String curr_release = "10.9";

        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        Preconditions_TA_29_1_TOPBIEGETBOM preconditionsTa2919_TOPBIEGETBOM = preconditions_TA_29_1_TOPBIEGETBOM(usera, library, prev_release);
        Preconditions_TA_29_1_BIEPrimitiveDate preconditionsTa2919_BIEPrimitiveDate = preconditions_TA_29_1_BIEPrimitiveDate(usera, library, prev_release);
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        //Uplift TOPBIEGETBOM
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.showAdvancedSearchPanel();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setDEN(preconditionsTa2919_TOPBIEGETBOM.topLevelASBIEP.getDen());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.next();
        EditBIEPage editBIEPage = upliftBIEVerificationPage.uplift();
        WebElement bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area /BOM/BOM Header/Document Date Time");
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("date time", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/System Environment Code");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("any URI", getText(bbiePanel.getValueDomainField()));

        WebElement bbiescNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Note/Entry Date Time Date Time", 3);
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        assertEquals("gregorian month day", getText(bbiescPanel.getValueDomainField()));

        bbiescNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Note/Author Text", 3);
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        assertEquals("normalized string", getText(bbiescPanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Batch Size Quantity", 3);
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("integer", getText(bbiePanel.getValueDomainField()));

        //BIEPrimitiveDate
        upliftBIEPage.openPage();
        upliftBIEPage.showAdvancedSearchPanel();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setDEN(preconditionsTa2919_BIEPrimitiveDate.topLevelASBIEP.getDen());
        upliftBIEPage.hitSearchButton();
        tr = upliftBIEPage.getTableRecordAtIndex(1);
        td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        upliftBIEVerificationPage = upliftBIEPage.next();
        editBIEPage = upliftBIEVerificationPage.uplift();
        bbieNode = editBIEPage.getNodeByPath("/Start Separate Date Time/Date");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("date", getText(bbiePanel.getValueDomainField()));
        homePage.logout();
    }

    private Preconditions_TA_29_1_TOPBIEGETBOM preconditions_TA_29_1_TOPBIEGETBOM(AppUserObject usera, LibraryObject library, String prevRelease) {
        Preconditions_TA_29_1_TOPBIEGETBOM preconditionsTa2919a = new Preconditions_TA_29_1_TOPBIEGETBOM(usera, library, prevRelease);
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(preconditionsTa2919a.topLevelASBIEP);
        WebElement bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Document Date Time");
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Alternate BOM Reference/Status/Effective Time Period/Start Time", 5);
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/System Environment Code");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("any URI");
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Note/Entry Date Time Date Time", 3);
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("gregorian month");
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Note/Author Text", 3);
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("normalized string");
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Alternate BOM Reference/Status/Effective Time Period/Inclusive Indicator", 3);
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Batch Size Quantity", 3);
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("integer");
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Alternate BOM Reference/Effectivity/Effective Range/Range Count Number", 5);
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("float");
        editBIEPage.hitUpdateButton();
        homePage.logout();
        return preconditionsTa2919a;
    }

    private Preconditions_TA_29_1_BIEPrimitiveDate preconditions_TA_29_1_BIEPrimitiveDate(
            AppUserObject usera, LibraryObject library, String prevRelease) {
        Preconditions_TA_29_1_BIEPrimitiveDate preconditionsTa2919a = new Preconditions_TA_29_1_BIEPrimitiveDate(usera, library, prevRelease);
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(preconditionsTa2919a.topLevelASBIEP);
        WebElement bbieNode = editBIEPage.getNodeByPath("/Start Separate Date Time/Date");
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();
        homePage.logout();
        return preconditionsTa2919a;
    }

    @Test
    @DisplayName("TC_29_1_TA_9_b, TC_29_1_TA_9_c")
    public void manual_mapping_transfers_allowed_primitive_values_and_defaults_disallowed_values() {
        String prev_release = "10.8.8";
        String curr_release = "10.9";
        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        Preconditions_TA_29_1_TOPBIEGETBOM preconditionsTa2919TOPBIEGETBOM = preconditions_TA_29_1_TOPBIEGETBOM(usera, library, prev_release);
        Preconditions_TA_29_1_BIEPrimitiveDate preconditionsTa2919BiePrimitiveDate = preconditions_TA_29_1_BIEPrimitiveDate(usera, library, prev_release);

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);
        BIEMenu bieMenu = homePage.getBIEMenu();
        //TOPBIEGETBOM prev_release
        bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(preconditionsTa2919TOPBIEGETBOM.topLevelASBIEP);
        waitFor(ofMillis(1000L));
        ACCExtensionViewEditPage accExtensionViewEditPage =
                editBIEPage.extendBIELocallyOnNode("/Get BOM/Data Area/BOM/BOM Option/Extension");
        SelectAssociationDialog selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/BOM Option User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Effectivity Relation Code. Code");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/BOM Option User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Validation Indicator. Indicator");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("BOM Option User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Method Consequence Text. Open_ Text");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/BOM Option User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Record Set Reference Identifier. Identifier");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/BOM Option User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Record Set Total Number. Positive Integer Number_ Number");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/BOM Option User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Latest Start Date Time. Open_ Date Time");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/BOM Option User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Request Language Code. Language_ Code");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/BOM Option User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Transport Temperature. Temperature_ Open_ Measure");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/BOM Option User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Correlation Identifier. Identifier");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/BOM Option User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Reason. Sequenced_ Open_ Text");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/BOM Option User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Record Set Save Indicator. Indicator");

        accExtensionViewEditPage.setNamespace(euNamespace);
        accExtensionViewEditPage.hitUpdateButton();
        accExtensionViewEditPage.moveToQA();
        accExtensionViewEditPage.moveToProduction();

        viewEditBIEPage.openPage();
        editBIEPage = viewEditBIEPage.openEditBIEPage(preconditionsTa2919TOPBIEGETBOM.topLevelASBIEP);
        WebElement bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Effectivity Relation Code", 6);
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("any URI");
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Validation Indicator");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Method Consequence Text");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("any URI");
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Record Set Reference Identifier");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("language");
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Record Set Total Number");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("positive integer");
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Latest Start Date Time");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("gregorian day");
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Request Language Code");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("token");
        editBIEPage.hitUpdateButton();

        editBIEPage.expandTree("Request Language Code");
        WebElement bbiescNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Request Language Code/List Agency Identifier");
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        bbiescPanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        bbiescNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Request Language Code/List Version Identifier");
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setValueDomain("token");
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Transport Temperature");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("float");
        editBIEPage.hitUpdateButton();

        editBIEPage.expandTree("Transport Temperature");
        bbiescNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Transport Temperature/Unit Code");
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setValueDomain("string");
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Correlation Identifier");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("string");
        editBIEPage.hitUpdateButton();

        editBIEPage.expandTree("Correlation Identifier");
        bbiescNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Correlation Identifier/Scheme Agency Identifier");
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setValueDomain("normalized string");
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Record Set Save Indicator");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        BigInteger sourceRequestLanguageCodeXbtId = getAPIFactory().getBusinessInformationEntityAPI()
                .getBbieXbtIdByPath(
                        preconditionsTa2919TOPBIEGETBOM.topLevelASBIEP.getTopLevelAsbiepId(),
                        getPersistedBbiePath(editBIEPage, "/Get BOM/Data Area/BOM/BOM Option/Extension/Request Language Code"));
        BigInteger sourceRequestLanguageCodeListVersionXbtId = getAPIFactory().getBusinessInformationEntityAPI()
                .getBbieScXbtIdByBbiePathAndPropertyAndRepresentationTerm(
                        preconditionsTa2919TOPBIEGETBOM.topLevelASBIEP.getTopLevelAsbiepId(),
                        getPersistedBbiePath(editBIEPage, "/Get BOM/Data Area/BOM/BOM Option/Extension/Request Language Code"),
                        "List Version",
                        "Identifier");

        //Uplift TOPBIEGETBOM
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.showAdvancedSearchPanel();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setDEN(preconditionsTa2919TOPBIEGETBOM.topLevelASBIEP.getDen());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.next();
        // Keep the Element targets within the mapped BOM Option parent.
        upliftBIEVerificationPage.mapNode(
                "/Get BOM/Data Area/BOM/BOM Option/Extension/Effectivity Relation Code",
                "/Get BOM/Data Area/BOM/BOM Option/Extension/Name");

        // BBIEP Element -> BBIEP Element.
        upliftBIEVerificationPage.mapNode(
                "/Get BOM/Data Area/BOM/BOM Option/Extension/Method Consequence Text",
                "/Get BOM/Data Area/BOM/BOM Option/Extension/Text");
        // User Extension BCCs are created as Elements; map to an Element target.
        upliftBIEVerificationPage.mapNode(
                "/Get BOM/Data Area/BOM/BOM Option/Extension/Record Set Reference Identifier",
                "/Get BOM/Data Area/BOM/BOM Option/Extension/Identifier");
        // User Extension BCCs are created as Elements; map to an Element target.
        upliftBIEVerificationPage.mapNode(
                "/Get BOM/Data Area/BOM/BOM Option/Extension/Record Set Total Number",
                "/Get BOM/Data Area/BOM/BOM Option/Extension/Quantity");
        // BBIEP Element -> BBIEP Element.
        upliftBIEVerificationPage.mapNode(
                "/Get BOM/Data Area/BOM/BOM Option/Extension/Latest Start Date Time",
                "/Get BOM/Data Area/BOM/BOM Option/Extension/Date Time");
        //BBIE to BBIE
        upliftBIEVerificationPage.mapNode(
                "/Get BOM/Data Area/BOM/BOM Option/Extension/Request Language Code",
                "/Get BOM/Data Area/BOM/BOM Option/Extension/Code");

        //BBIE_SC to BBIE_SC
        upliftBIEVerificationPage.mapNode(
                "/Get BOM/Data Area/BOM/BOM Option/Extension/Request Language Code/List Agency Identifier",
                "/Get BOM/Data Area/BOM/BOM Option/Extension/Code/List Version Identifier");

        //BBIE to BBIE (DISALLOWED-primitive case): source Measure "Transport Temperature" was set to "float"
        //(line ~1330). The target "Identifier" is an Identifier BDT whose approved primitives do NOT
        //include "float" in 10.9, so the uplift must DEFAULT it to the node default ("normalized string"),
        //NOT carry "float" over. This exercises the "defaults disallowed values" half of the contract (#29.1.9.c).
        upliftBIEVerificationPage.mapNode(
                "/Get BOM/Data Area/BOM/BOM Option/Extension/Transport Temperature",
                "/Get BOM/Data Area/BOM/BOM Option/Identifier");

        //BBIE_SC to BBIE_SC
        upliftBIEVerificationPage.mapNode(
                "/Get BOM/Data Area/BOM/BOM Option/Extension/Transport Temperature/Unit Code",
                "/Get BOM/Data Area/BOM/BOM Option/Identifier/Scheme Version Identifier");

        editBIEPage = upliftBIEVerificationPage.uplift();
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Name", 3);
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("any URI", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Text", 3);
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("any URI", getText(bbiePanel.getValueDomainField()));
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Identifier", 3);
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("language", getText(bbiePanel.getValueDomainField()));
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Quantity", 3);
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("positive integer", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Date Time");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("gregorian day", getText(bbiePanel.getValueDomainField()));

        editBIEPage.openPage();
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Code", 3);
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("token", getText(bbiePanel.getValueDomainField()));
        bbiescNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Code/List Agency Identifier");
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        assertEquals("token", getText(bbiescPanel.getValueDomainField()));
        bbiescNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Code/List Version Identifier");
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        assertEquals("token", getText(bbiescPanel.getValueDomainField()));

        BigInteger upliftedTopLevelAsbiepId = editBIEPage.getTopLevelASBIEP().getTopLevelAsbiepId();
        assertEquals(sourceRequestLanguageCodeXbtId,
                getAPIFactory().getBusinessInformationEntityAPI().getBbieXbtIdByPath(
                        upliftedTopLevelAsbiepId,
                        getPersistedBbiePath(editBIEPage, "/Get BOM/Data Area/BOM/BOM Option/Extension/Code")));
        assertEquals(sourceRequestLanguageCodeListVersionXbtId,
                getAPIFactory().getBusinessInformationEntityAPI().getBbieScXbtIdByBbiePathAndPropertyAndRepresentationTerm(
                        upliftedTopLevelAsbiepId,
                        getPersistedBbiePath(editBIEPage, "/Get BOM/Data Area/BOM/BOM Option/Extension/Code"),
                        "List Version",
                        "Identifier"));

        editBIEPage.openPage();
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Identifier", 3);
        waitFor(ofMillis(1000L));
        // DISALLOWED-primitive default case: "Transport Temperature" (float) was mapped onto this Identifier
        // node, where "float" is NOT an approved primitive in 10.9, so the uplift must default it to the node's
        // default primitive ("normalized string") rather than carry "float". Guards the allowed-primitive gate
        // in BieUpliftingService.setValueDomain (#29.1.9.c). Before that fix this carried "float" (unselectable).
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("normalized string", getText(bbiePanel.getValueDomainField()));
        bbiescNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Identifier/Scheme Version Identifier");
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        // Source node "Transport Temperature/Unit Code" was set to "string" (line ~1338) and manually
        // mapped here. For the target's BDT (Identifier, dt_sc "Scheme Version Identifier") in release
        // 10.9, "string" IS an allowed primitive (dt_sc_awd_pri; default is "token", not "normalized
        // string"), so per #29.1.9.b the uplift transfers the allowed source value verbatim => "string".
        // The prior expectation "normalized string" was a copy-paste from the unrelated node at line ~1353.
        assertEquals("string", getText(bbiescPanel.getValueDomainField()));
        homePage.logout();
    }

    @Test
    @DisplayName("TC_29_1_TA_10_a")
    public void it_should_be_transferred_to_the_target_bbie_or_bbie_sc_node_in_case() {
        String prev_release = "10.8.7.1";
        String curr_release = "10.9";

        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        //BIEBOMDoubleNested previousRelease
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        Preconditions_TA_29_1_BIEBOMDoubleNested preconditionsTa2910BIEBOMDoubleNested = preconditions_TA_29_10a_BIEBOMDoubleNested(developer, library, prev_release);
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(preconditionsTa2910BIEBOMDoubleNested.topLevelASBIEP);

        //Uplift BIEBOMDoubleNested
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.showAdvancedSearchPanel();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setDEN(preconditionsTa2910BIEBOMDoubleNested.topLevelASBIEP.getDen());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.next();
        editBIEPage = upliftBIEVerificationPage.uplift();

        editBIEPage.getNodeByPath("/BOM/BOM Option/Default Indicator");
        WebElement bbieNode = editBIEPage.getNodeByPath("/BOM/BOM Option/Default Indicator");
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith("clm6TimeFormatCode1_TimeFormatCode"));

        WebElement bbieSCNode = editBIEPage.getNodeByPath("/BOM/BOM Option/Description/Language Code", 3);
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertTrue(getText(bbiescPanel.getValueDomainField()).startsWith("clm6TimeFormatCode1_TimeFormatCode"));

        bbieSCNode = editBIEPage.getNodeByPath("/BOM/BOM Option/Identifier/Scheme Agency Identifier", 3);
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        // The constrained DT_SC must retain the matching Agency ID List.
        assertTrue(getText(bbiescPanel.getValueDomainField()).startsWith("clm63055D16B_AgencyIdentification"));
        homePage.logout();
    }

    private Preconditions_TA_29_1_BIEBOMDoubleNested preconditions_TA_29_10a_BIEBOMDoubleNested(
            AppUserObject developer, LibraryObject library, String prevRelease) {
        Preconditions_TA_29_1_BIEBOMDoubleNested preconditionsTa2910a = new Preconditions_TA_29_1_BIEBOMDoubleNested(developer, library, prevRelease);
        NamespaceObject devNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer, library);
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(preconditionsTa2910a.topLevelASBIEP);
        WebElement bbieNode = editBIEPage.getNodeByPath("/BOM/BOM Option/Default Indicator");
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain("clm6TimeFormatCode1_TimeFormatCode");
        editBIEPage.hitUpdateButton();

        WebElement bbieSCNode = editBIEPage.getNodeByPath("/BOM/BOM Option/Description/Language Code", 3);
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setValueDomainRestriction("Code");
        bbiescPanel.setValueDomain("clm6TimeFormatCode1_TimeFormatCode");
        editBIEPage.hitUpdateButton();

        bbieSCNode = editBIEPage.getNodeByPath("/BOM/BOM Option/Identifier/Scheme Agency Identifier", 3);
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setValueDomainRestriction("Agency");
        bbiescPanel.setValueDomain("clm63055D16B_AgencyIdentification");
        editBIEPage.hitUpdateButton();
        homePage.logout();
        return preconditionsTa2910a;
    }

    @Test
    @DisplayName("TC_29_1_TA_10_b")
    public void it_should_be_transferred_to_the_target_bbie_or_bbie_sc_node_in_case_scenario_2() {
        String prev_release = "10.8.7.1";
        String curr_release = "10.9";
        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        Preconditions_TA_29_1_JournalEntry preconditionsTa2910JournalEntry = preconditions_TA_29_10b_JournalEntry(usera, library, prev_release);

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        BIEMenu bieMenu = homePage.getBIEMenu();
        //JournalEntry prev_release
        bieMenu = homePage.getBIEMenu();

        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.showAdvancedSearchPanel();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setDEN(preconditionsTa2910JournalEntry.topLevelASBIEP.getDen());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.next();

        // Keep Element mappings within the mapped Change Status parent.
        upliftBIEVerificationPage.mapNode(
                "/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Extension/Usage Description",
                "/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Description");

        upliftBIEVerificationPage.mapNode(
                "/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Extension/Control Objective Category",
                "/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Code");

        upliftBIEVerificationPage.mapNode(
                "/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Extension/Control Objective Category/List Version Identifier",
                "/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Code/List Version Identifier");

        EditBIEPage editBIEPage = upliftBIEVerificationPage.uplift();
        WebElement bbieNode = editBIEPage.getNodeByPath("/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Description");
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertChecked(bbiePanel.getUsedCheckbox());
        assertEnabled(bbiePanel.getUsedCheckbox());
        bbieNode = editBIEPage.getNodeByPath("/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Code");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith("oacl_RiskCode"));
        WebElement bbieSCNode = editBIEPage.getNodeByPath("/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Code/List Version Identifier");
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertTrue(getText(bbiescPanel.getValueDomainField()).startsWith("clm6ConditionTypeCode1_ConditionTypeCode"));
        homePage.logout();
    }

    private Preconditions_TA_29_1_JournalEntry preconditions_TA_29_10b_JournalEntry(AppUserObject usera, LibraryObject library, String prevRelease) {
        Preconditions_TA_29_1_JournalEntry preconditionsTa2910b = new Preconditions_TA_29_1_JournalEntry(usera, library, prevRelease);
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(preconditionsTa2910b.topLevelASBIEP);
        waitFor(Duration.ofMillis(2500));
        ACCExtensionViewEditPage accExtensionViewEditPage =
                editBIEPage.extendBIELocallyOnNode("/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Extension");
        SelectAssociationDialog selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Change Status User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Usage Description. Description_ Text");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Change Status User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Control Objective Category. Risk_ Code");
        accExtensionViewEditPage.setNamespace(euNamespace);
        accExtensionViewEditPage.hitUpdateButton();
        accExtensionViewEditPage.moveToQA();
        accExtensionViewEditPage.moveToProduction();

        viewEditBIEPage.openPage();
        editBIEPage = viewEditBIEPage.openEditBIEPage(preconditionsTa2910b.topLevelASBIEP);
        WebElement bbieNode = editBIEPage.getNodeByPath("/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Extension/Usage Description", 3);
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain("clm6TimeFormatCode1_TimeFormatCode");
        editBIEPage.hitUpdateButton();
        bbieNode = editBIEPage.getNodeByPath("/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Extension/Control Objective Category");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain("oacl_RiskCode");
        editBIEPage.hitUpdateButton();
        WebElement bbiescNode = editBIEPage.getNodeByPath("/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Extension/Control Objective Category/List Version Identifier", 5);
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setValueDomainRestriction("Code");
        bbiescPanel.setValueDomain("clm6ConditionTypeCode1_ConditionTypeCode");
        editBIEPage.hitUpdateButton();
        homePage.logout();
        return preconditionsTa2910b;
    }

    @Test
    @DisplayName("TC_29_1_TA_10_b, TC_29_1_TA_11_a, TC_29_1_TA_11_b")
    public void it_should_be_transferred_to_the_target_bbie_or_bbie_sc_node_in_case_scenario_3() {
        String prev_release = "10.8.7.1";
        String curr_release = "10.9";
        Map<String, CodeListObject> upliftedCodeLists = new HashMap<>();
        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        //BIECAGUplift prev_release
        Preconditions_TA_29_1_BIECAGUplift preconditionsTa2911BIECAGUplift = preconditions_TA_29_11_BIECAGUplift(usera, library, prev_release);
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);
        ReleaseObject prev_releaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, prev_release);
        RandomCodeListWithStateContainer euCodeListWithStateContainer = new RandomCodeListWithStateContainer(
                usera, prev_releaseObject, euNamespace, Arrays.asList("WIP", "QA", "Production", "Deleted"));
        CodeListObject CLaccessUseraDeprecated = getAPIFactory().getCodeListAPI().createRandomCodeList(usera, euNamespace, prev_releaseObject, "Production");
        CodeListValueObject codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(CLaccessUseraDeprecated, usera);
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(CLaccessUseraDeprecated);
        editCodeListPage.hitAmendButton();
        click(editCodeListPage.getDeprecatedSelectField());
        editCodeListPage.setDefinition("Check the Deprecated Checkbox");
        editCodeListPage.hitUpdateButton();
        editCodeListPage.moveToQA();
        editCodeListPage.moveToProduction();

        viewEditCodeListPage.openPage();
        editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(
                getOnlyCodeListInState("oacl_MatchDocumentCode", prev_release, "Published")
        );
        editCodeListPage.hitDeriveCodeListBasedOnThisButton();
        String derivedCodeListName = "CLuserderived_BIEUp_" + usera.getAppUserId();
        editCodeListPage.setName(derivedCodeListName);
        editCodeListPage.setNamespace(euNamespace);
        editCodeListPage.setDefinition("aDefinition");
        editCodeListPage.hitUpdateButton();
        CodeListObject derivedCodeList = getOnlyCodeListInState(
                derivedCodeListName, prev_release, "WIP", usera.getAppUserId());

        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(preconditionsTa2911BIECAGUplift.topLevelASBIEP);

        WebElement bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Effectivity Relation Code");
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        String CLaccessendUserwip = euCodeListWithStateContainer.stateCodeLists.get("WIP").getName();
        bbiePanel.setValueDomain(CLaccessendUserwip);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Validation Indicator");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        String CLaccessendUserqa = euCodeListWithStateContainer.stateCodeLists.get("QA").getName();
        bbiePanel.setValueDomain(CLaccessendUserqa);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Method Consequence Text");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        String CLaccessendUserproduction = euCodeListWithStateContainer.stateCodeLists.get("Production").getName();
        bbiePanel.setValueDomain(CLaccessendUserproduction);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Record Set Total Number");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        String CLaccessendUserdeleted = euCodeListWithStateContainer.stateCodeLists.get("Deleted").getName();
        bbiePanel.setValueDomain(CLaccessendUserdeleted);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Latest Start Date Time");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain(CLaccessUseraDeprecated.getName());
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Transport Temperature");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain(derivedCodeListName);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Technical Name");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain(CLaccessendUserwip);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Placard Endorsement");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain(CLaccessendUserqa);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Placard Notation");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain(CLaccessendUserproduction);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Marine Pollution Level Code");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain(derivedCodeListName);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Toxicity Zone Code");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain(CLaccessendUserdeleted);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Flashpoint Temperature");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain(CLaccessUseraDeprecated.getName());
        editBIEPage.hitUpdateButton();
        WebElement bbiescNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Primary Entry Route/Type Code");
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setValueDomainRestriction("Code");
        bbiescPanel.setValueDomain(CLaccessendUserwip);
        editBIEPage.hitUpdateButton();

        bbiescNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/MFAGID/Scheme Agency Identifier");
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setValueDomainRestriction("Code");
        bbiescPanel.setValueDomain(CLaccessendUserqa);
        editBIEPage.hitUpdateButton();

        bbiescNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/MFAGID/Scheme Version Identifier");
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setValueDomainRestriction("Code");
        bbiescPanel.setValueDomain(derivedCodeListName);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Export Control/Encryption Status Code");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain("clm6TimeFormatCode1_TimeFormatCode");
        editBIEPage.hitUpdateButton();

        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.showAdvancedSearchPanel();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setDEN(preconditionsTa2911BIECAGUplift.topLevelASBIEP.getDen());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.next();

        // Extension BCCs are Elements. Use the standard Extension Elements so mappings
        // stay outside the separately mapped Child Item subtree.
        upliftBIEVerificationPage.mapNode(
                "/Child Item Reference/Extension/Latest Start Date Time",
                "/Child Item Reference/Extension/Date Time");

        upliftBIEVerificationPage.mapNode(
                "/Child Item Reference/Extension/Transport Temperature",
                "/Child Item Reference/Extension/Measure");

        upliftBIEVerificationPage.mapNode(
                "/Child Item Reference/Extension/Validation Indicator",
                "/Child Item Reference/Extension/Indicator");

        upliftBIEVerificationPage.mapNode(
                "/Child Item Reference/Extension/Method Consequence Text",
                "/Child Item Reference/Extension/Text");

        upliftBIEVerificationPage.mapNode(
                "/Child Item Reference/Extension/Record Set Total Number",
                "/Child Item Reference/Extension/Number");


        upliftBIEVerificationPage.mapNode(
                "/Child Item Reference/Extension/Effectivity Relation Code",
                "/Child Item Reference/Extension/Code");

        editBIEPage = upliftBIEVerificationPage.uplift();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Technical Name");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("string", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Placard Endorsement");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("string", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Placard Notation");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("string", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Marine Pollution Level Code");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("normalized string", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Toxicity Zone Code");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("normalized string", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Flashpoint Temperature");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("decimal", getText(bbiePanel.getValueDomainField()));

        WebElement bbieSCNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Primary Entry Route/Type Code");
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertEquals("token", getText(bbiescPanel.getValueDomainField()));

        bbieSCNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/MFAGID/Scheme Version Identifier");
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertEquals("token", getText(bbiescPanel.getValueDomainField()));

        bbieSCNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/MFAGID/Scheme Agency Identifier");
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertEquals("token", getText(bbiescPanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Export Control/Encryption Status Code");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith("clm6TimeFormatCode1_TimeFormatCode"));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Date Time");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("date time", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Measure");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("decimal", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Indicator");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("xbt boolean", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Text");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("string", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Number");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("decimal", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Code");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("normalized string", getText(bbiePanel.getValueDomainField()));

        //Uplift codeList page
        ReleaseObject sourceRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, prev_release);
        ReleaseObject targetRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, curr_release);
        UpliftCodeListPage upliftCodeListPage = bieMenu.openUpliftCodeListSubMenu();

        for (CodeListObject codeList : Arrays.asList(
                euCodeListWithStateContainer.stateCodeLists.get("WIP"),
                euCodeListWithStateContainer.stateCodeLists.get("QA"),
                euCodeListWithStateContainer.stateCodeLists.get("Production"),
                euCodeListWithStateContainer.stateCodeLists.get("Deleted"),
                CLaccessUseraDeprecated,
                derivedCodeList)) {
            retry(() -> {
                try {
                    upliftCodeListPage.hitUpliftButton(codeList, sourceRelease, targetRelease);
                } catch (WebDriverException e) {
                    upliftCodeListPage.openPage();
                    throw e;
                }
            });

            String currentUrl = getDriver().getCurrentUrl();
            BigInteger codeListManifestId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/code_list/") + "/code_list/".length()));
            CodeListObject upliftedCodeList = getAPIFactory().getCodeListAPI().getCodeListByManifestId(codeListManifestId);
            String codeListName = codeList.getName();
            if (!upliftedCodeLists.containsKey(codeListName)) {
                upliftedCodeLists.put(codeListName, upliftedCodeList);
            } else {
                upliftedCodeLists.put(codeListName, upliftedCodeList);
            }

            upliftCodeListPage.openPage();
        }

        // The first BIE uplift above runs before these end-user Code Lists exist
        // in the target release and verifies primitive fallback.  After the
        // real Code Lists (including a derived list) are uplifted, this second
        // run verifies that the target availability query finds and retains
        // the corresponding value domains on BBIE and BBIE_SC nodes.

        upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.showAdvancedSearchPanel();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setDEN(preconditionsTa2911BIECAGUplift.topLevelASBIEP.getDen());
        upliftBIEPage.hitSearchButton();
        tr = upliftBIEPage.getTableRecordAtIndex(1);
        td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        upliftBIEVerificationPage = upliftBIEPage.next();

        upliftBIEVerificationPage.mapNode(
                "/Child Item Reference/Extension/Effectivity Relation Code",
                "/Child Item Reference/Extension/Code");

        upliftBIEVerificationPage.mapNode(
                "/Child Item Reference/Extension/Validation Indicator",
                "/Child Item Reference/Extension/Indicator");

        upliftBIEVerificationPage.mapNode(
                "/Child Item Reference/Extension/Method Consequence Text",
                "/Child Item Reference/Extension/Text");

        upliftBIEVerificationPage.mapNode(
                "/Child Item Reference/Extension/Record Set Total Number",
                "/Child Item Reference/Extension/Number");



        upliftBIEVerificationPage.mapNode(
                "/Child Item Reference/Extension/Latest Start Date Time",
                "/Child Item Reference/Extension/Date Time");


        upliftBIEVerificationPage.mapNode(
                "/Child Item Reference/Extension/Transport Temperature",
                "/Child Item Reference/Extension/Measure");

        editBIEPage = upliftBIEVerificationPage.uplift();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Indicator");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith(CLaccessendUserqa));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Text");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith(CLaccessendUserproduction));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Number");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith(CLaccessendUserdeleted));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Date Time");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith(CLaccessUseraDeprecated.getName()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Measure");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith(derivedCodeListName));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Code");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith(CLaccessendUserwip));

        bbieSCNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Technical Name");
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertTrue(getText(bbiescPanel.getValueDomainField()).startsWith(CLaccessendUserwip));

        bbieSCNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Placard Endorsement");
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertTrue(getText(bbiescPanel.getValueDomainField()).startsWith(CLaccessendUserqa));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Placard Notation");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith(CLaccessendUserproduction));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Marine Pollution Level Code");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith(derivedCodeListName));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Toxicity Zone Code");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith(CLaccessendUserdeleted));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Flashpoint Temperature");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith(CLaccessUseraDeprecated.getName()));

        bbieSCNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/MFAGID/Scheme Version Identifier");
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertTrue(getText(bbiescPanel.getValueDomainField()).startsWith(derivedCodeListName));

        bbieSCNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/MFAGID/Scheme Agency Identifier");
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertTrue(getText(bbiescPanel.getValueDomainField()).startsWith(CLaccessendUserqa));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Export Control/Encryption Status Code");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith("clm6TimeFormatCode1_TimeFormatCode"));
    }

    private Preconditions_TA_29_1_BIECAGUplift preconditions_TA_29_11_BIECAGUplift(AppUserObject usera, LibraryObject library, String prevRelease) {
        Preconditions_TA_29_1_BIECAGUplift preconditionsTa2911 = new Preconditions_TA_29_1_BIECAGUplift(usera, library, prevRelease);
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera, library);
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(preconditionsTa2911.topLevelASBIEP);
        waitFor(Duration.ofMillis(1500));

        ACCExtensionViewEditPage accExtensionViewEditPage =
                editBIEPage.extendBIELocallyOnNode("/Child Item Reference/Extension");
        SelectAssociationDialog selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Effectivity Relation Code. Code");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Validation Indicator. Indicator");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Method Consequence Text. Open_ Text");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Record Set Reference Identifier. Identifier");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Record Set Total Number. Positive Integer Number_ Number");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Latest Start Date Time. Open_ Date Time");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Request Language Code. Language_ Code");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Transport Temperature. Temperature_ Open_ Measure");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Correlation Identifier. Identifier");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Reason. Sequenced_ Open_ Text");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Record Set Save Indicator. Indicator");

        accExtensionViewEditPage.setNamespace(euNamespace);
        accExtensionViewEditPage.hitUpdateButton();
        accExtensionViewEditPage.moveToQA();
        accExtensionViewEditPage.moveToProduction();
        homePage.logout();
        return preconditionsTa2911;
    }

    @Test
    @DisplayName("TC_29_1_TA_12")
    public void paths_of_unmapped_source_nodes_including_the_code_list_and_agency_id_list_nodes() {
        String prev_release = "10.8.8";
        String curr_release = "10.9";

        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        //JournalEntry prev_release
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        Preconditions_TA_29_1_JournalEntry preconditionsTa2912JournalEntry = preconditions_TA_29_10b_JournalEntry(usera, library, prev_release);
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();

        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage =
                createBIEForSelectBusinessContextsPage.next(Collections.singletonList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE("Journal Entry. Journal Entry", prev_release);
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        TopLevelASBIEPObject ReusedJournalENtryTopLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        editBIEPage.moveToQA();
        editBIEPage.moveToProduction();

        viewEditBIEPage.openPage();
        editBIEPage = viewEditBIEPage.openEditBIEPage(preconditionsTa2912JournalEntry.topLevelASBIEP);
        SelectProfileBIEToReuseDialog
                selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/Post Acknowledge Journal Entry/Data Area/Journal Entry");
        selectProfileBIEToReuseDialog.selectBIEToReuse(ReusedJournalENtryTopLevelASBIEP.getPropertyTerm());
        editBIEPage.moveToQA();

        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.showAdvancedSearchPanel();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setDEN(preconditionsTa2912JournalEntry.topLevelASBIEP.getDen());
        upliftBIEPage.setState("QA");
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.next();
        click(getDriver(), upliftBIEVerificationPage.getNextButton());
        invisibilityOfElementLocated(PageHelper.wait(getDriver(), ofSeconds(120L), ofMillis(100L)),
                By.cssSelector("mat-dialog-container .loading-container"));
        String extension = "/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Extension/";
        assertUpliftReportRow(extension + "Usage Description", "BCCP", "Unmatched", "");
        assertUpliftReportRow(extension + "Control Objective Category", "BCCP", "Unmatched", "");
        assertUpliftReportRow(extension + "Control Objective Category/List Version Identifier", "DT_SC", "Unmatched", "");
        assertUpliftReportRow("/Post Acknowledge Journal Entry/Data Area/Journal Entry", "ASCCP", "System", "Not selected");

        WebElement issuesOnly = visibilityOfElementLocated(getDriver(), By.xpath(
                "//score-report-dialog//mat-checkbox[normalize-space(.)='View Issues Only']"));
        assertChecked(issuesOnly);
        By cleanSystemRows = By.xpath("//score-report-dialog//tr[td[contains(@class,'mat-column-match')][normalize-space(.)='System']]"
                + "[td[contains(@class,'mat-column-reuse')][normalize-space(.)='']]"
                + "[td[contains(@class,'mat-column-validCode')][normalize-space(.)='']]");
        assertTrue(getDriver().findElements(cleanSystemRows).isEmpty());
        click(getDriver(), issuesOnly);
        assertFalse(visibilityOfAllElementsLocatedBy(getDriver(), cleanSystemRows).isEmpty());
        assertUpliftReportRow(extension + "Usage Description", "BCCP", "Unmatched", "");
        assertTrue(elementToBeClickable(getDriver(), By.xpath(
                "//score-report-dialog//button[normalize-space(.)='Download']")).isEnabled());

    }

    private void assertUpliftReportRow(String sourcePath, String type, String match, String reuse) {
        WebElement row = visibilityOfElementLocated(getDriver(), By.xpath(
                "//score-report-dialog//tr[td[contains(@class,'mat-column-displayPath')]"
                        + "//div[contains(@class,'display-path-content')]/span[1][normalize-space(.)="
                        + xpathLiteral(sourcePath) + "]]") );
        int displayPathPartCount = row.findElements(By.cssSelector(".display-path-content > span")).size();
        assertEquals("Unmatched".equals(match) ? 1 : 3, displayPathPartCount, sourcePath);
        assertEquals(type, getText(row.findElement(By.cssSelector(".mat-column-ccType"))), sourcePath);
        assertEquals(match, getText(row.findElement(By.cssSelector(".mat-column-match"))), sourcePath);
        // PageHelper.getText returns null for a rendered-but-empty cell. Treat it as the
        // empty value represented by this report assertion.
        assertEquals(reuse, Objects.requireNonNullElse(
                getText(row.findElement(By.cssSelector(".mat-column-reuse"))), ""), sourcePath);
    }

    private class Preconditions_TA_29_1_2 {

        private final TopLevelASBIEPObject topLevelASBIEP;

        // ASBIE
        private final String asbiePath = "/Change Acknowledge Shipment Status/Application Area";
        private final String asbieRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String asbieContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        // BBIE
        private final String bbiePath = "/Change Acknowledge Shipment Status/System Environment Code";
        private final String bbieRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieExample = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieValueConstraint = "Fixed Value";
        private final String bbieFixedValue = RandomStringUtils.secure().nextAlphanumeric(50, 100).trim();
        private final String bbieValueDomainRestriction = "Code";
        private final String bbieValueDomain = "oacl_SystemEnvironmentCode";

        // BBIE_SC
        private final String bbieScPath = "/Change Acknowledge Shipment Status/Application Area/Scenario Identifier/Scheme Version Identifier";
        private final String bbieScRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScExample = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScValueConstraint = "Fixed Value";
        private final String bbieScFixedValue = RandomStringUtils.secure().nextAlphanumeric(50, 100).trim();
        private final String bbieScValueDomainRestriction = "Primitive";
        private final String bbieScValueDomain = "token";

        Preconditions_TA_29_1_2(AppUserObject usera, LibraryObject library, String prevRelease) {
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, 
                    "Change Acknowledge Shipment Status. Change Acknowledge Shipment Status", prevRelease);
            this.topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");
        }

    }

    private class Preconditions_TA_29_1_BIE1QA {
        private final TopLevelASBIEPObject topLevelASBIEP;
        private final String topLevelASBIEPBusinessTerm = "biz_term_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);
        private final String topLevelASBIEPRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String topLevelASBIEPStatus = "status_" + RandomStringUtils.secure().nextAlphanumeric(5, 10);

        // ASBIE
        private final ArrayList<String> asbiePaths = new ArrayList<>();
        private final String asbieRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String asbieContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        // BBIE
        private final ArrayList<String> bbiePaths = new ArrayList<>();
        private final String bbieRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieExample = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieValueConstraint = "Fixed Value";
        private final String bbieFixedValue = RandomStringUtils.secure().nextAlphanumeric(50, 100).trim();
        private final String bbieValueDomainRestriction = "Code";
        private final String bbieValueDomain = "oacl_SystemEnvironmentCode";

        // BBIE_SC
        private final ArrayList<String> bbieScPaths = new ArrayList<>();
        private final String bbieScRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScExample = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScValueConstraint = "Fixed Value";
        private final String bbieScFixedValue = RandomStringUtils.secure().nextAlphanumeric(50, 100).trim();
        private final String bbieScValueDomainRestriction = "Primitive";
        private final String bbieScValueDomain = "token";

        Preconditions_TA_29_1_BIE1QA(AppUserObject usera, LibraryObject library, String prevRelease) {
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, 
                    "Enterprise Unit. Enterprise Unit", prevRelease);
            this.topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");

            asbiePaths.add("/Enterprise Unit/Extension/Incorporation Location");
            asbiePaths.add("/Enterprise Unit/Extension/Incorporation Location/Physical Address");
            asbiePaths.add("/Enterprise Unit/Extension/Code List/Code List Value");
            asbiePaths.add("/Enterprise Unit/Extension/Revised Item Status");

            bbiePaths.add("/Enterprise Unit/Extension/Last Modification Date Time");
            bbiePaths.add("/Enterprise Unit/Extension/Identifier");
            bbiePaths.add("/Enterprise Unit/Extension/Name");
            bbiePaths.add("/Enterprise Unit/Identifier Set/Scheme Version Identifier");
            bbiePaths.add("/Enterprise Unit/Extension/Incorporation Location/CAGEID");
            bbiePaths.add("/Enterprise Unit/Extension/Usage Description");
            bbiePaths.add("/Enterprise Unit/Identifier");
            bbiePaths.add("/Enterprise Unit/Type Code");
            bbiePaths.add("/Enterprise Unit/Extension/Indicator");
            bbiePaths.add("/Enterprise Unit/Extension/Revised Item Status/Reason Code");

            bbieScPaths.add("/Enterprise Unit/Cost Center Identifier/Scheme Agency Identifier");
        }
    }

    private class Preconditions_TA_29_1_5d_BIEReusedChild {
        private final TopLevelASBIEPObject topLevelASBIEP;
        // ASBIE
        private final String asbiePath = "/Unit Packaging/Dimensions";
        private final String asbieRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String asbieContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        // BBIE
        private final String bbiePath = "/Unit Packaging/Capacity Per Package Quantity";
        private final String bbieRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieExample = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieValueConstraint = "Fixed Value";
        private final String bbieFixedValue = RandomStringUtils.secure().nextAlphanumeric(50, 100).trim();
        private final String bbieValueDomainRestriction = "Primitive";
        private final String bbieValueDomain = "decimal";

        // BBIE_SC
        private final String bbieScPath = "/Unit Packaging/UPC Packaging Level Code/List Agency Identifier";
        private final String bbieScRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScExample = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScValueConstraint = "Fixed Value";
        private final String bbieScFixedValue = RandomStringUtils.secure().nextAlphanumeric(50, 100).trim();
        private final String bbieScValueDomainRestriction = "Agency";
        private final String bbieScValueDomain = "clm63055D16B_AgencyIdentification";

        Preconditions_TA_29_1_5d_BIEReusedChild(AppUserObject usera, LibraryObject library, String prevRelease) {
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, 
                    "Unit Packaging. Packaging", prevRelease);
            this.topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");
        }
    }

    private class Preconditions_TA_29_1_5d_BIEReusedParent {
        private final TopLevelASBIEPObject topLevelASBIEP;
        // ASBIE
        private final String asbiePath = "/From UOM Package/Unit Packaging";
        private final String asbieRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String asbieContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        // BBIE
        private final String bbiePath = "/From UOM Package/UOM Code";
        private final String bbieRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieExample = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieValueConstraint = "Fixed Value";
        private final String bbieFixedValue = RandomStringUtils.secure().nextAlphanumeric(50, 100).trim();
        private final String bbieValueDomainRestriction = "Code";
        private final String bbieValueDomain = "oacl_SystemEnvironmentCode";

        Preconditions_TA_29_1_5d_BIEReusedParent(AppUserObject usera, LibraryObject library, String prevRelease) {
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, 
                    "From UOM Package. UOM Package", prevRelease);
            this.topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");
        }
    }

    private class Preconditions_TA_29_1_5d_BIEReusedScenario {
        private final TopLevelASBIEPObject topLevelASBIEP;

        Preconditions_TA_29_1_5d_BIEReusedScenario(AppUserObject usera, LibraryObject library, String prevRelease) {
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, 
                    "UOM Code Conversion Rate. UOM Code Conversion Rate", prevRelease);
            this.topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");
        }
    }

    private class Preconditions_TA_29_1_TOPBIEGETBOM {
        private final TopLevelASBIEPObject topLevelASBIEP;

        // ASBIE
        private final ArrayList<String> asbiePaths = new ArrayList<>();
        private final String asbieRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String asbieContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        // BBIE
        private final ArrayList<String> bbiePaths = new ArrayList<>();
        private final String bbieRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieExample = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieValueConstraint = "Fixed Value";
        private final String bbieFixedValue = RandomStringUtils.secure().nextAlphanumeric(50, 100).trim();
        private final String bbieValueDomainRestriction = "Code";
        private final String bbieValueDomain = "oacl_SystemEnvironmentCode";

        // BBIE_SC
        private final ArrayList<String> bbieScPaths = new ArrayList<>();
        private final String bbieScRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScExample = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScValueConstraint = "Fixed Value";
        private final String bbieScFixedValue = RandomStringUtils.secure().nextAlphanumeric(50, 100).trim();
        private final String bbieScValueDomainRestriction = "Agency";
        private final String bbieScValueDomain = "clm63055D16B_AgencyIdentification";

        Preconditions_TA_29_1_TOPBIEGETBOM(AppUserObject usera, LibraryObject library, String prevRelease) {
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, 
                    "Get BOM. Get BOM", prevRelease);
            this.topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");
        }
    }

    private class Preconditions_TA_29_1_BIEPrimitiveDate {
        private final TopLevelASBIEPObject topLevelASBIEP;

        // ASBIE
        private final ArrayList<String> asbiePaths = new ArrayList<>();
        private final String asbieRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String asbieContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        // BBIE
        private final ArrayList<String> bbiePaths = new ArrayList<>();
        private final String bbieRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieExample = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieValueConstraint = "Fixed Value";
        private final String bbieFixedValue = RandomStringUtils.secure().nextAlphanumeric(50, 100).trim();
        private final String bbieValueDomainRestriction = "Code";
        private final String bbieValueDomain = "oacl_SystemEnvironmentCode";

        // BBIE_SC
        private final ArrayList<String> bbieScPaths = new ArrayList<>();
        private final String bbieScRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScExample = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScValueConstraint = "Fixed Value";
        private final String bbieScFixedValue = RandomStringUtils.secure().nextAlphanumeric(50, 100).trim();
        private final String bbieScValueDomainRestriction = "Agency";
        private final String bbieScValueDomain = "clm63055D16B_AgencyIdentification";

        Preconditions_TA_29_1_BIEPrimitiveDate(AppUserObject usera, LibraryObject library, String prevRelease) {
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, 
                    "Start Separate Date Time. Separate Date Time", prevRelease);
            this.topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");
        }
    }

    private class Preconditions_TA_29_1_BIEBOMDoubleNested {
        private final TopLevelASBIEPObject topLevelASBIEP;

        // ASBIE
        private final ArrayList<String> asbiePaths = new ArrayList<>();
        private final String asbieRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String asbieContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        // BBIE
        private final ArrayList<String> bbiePaths = new ArrayList<>();
        private final String bbieRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieExample = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieValueConstraint = "Fixed Value";
        private final String bbieFixedValue = RandomStringUtils.secure().nextAlphanumeric(50, 100).trim();
        private final String bbieValueDomainRestriction = "Code";
        private final String bbieValueDomain = "oacl_SystemEnvironmentCode";

        // BBIE_SC
        private final ArrayList<String> bbieScPaths = new ArrayList<>();
        private final String bbieScRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScExample = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScValueConstraint = "Fixed Value";
        private final String bbieScFixedValue = RandomStringUtils.secure().nextAlphanumeric(50, 100).trim();
        private final String bbieScValueDomainRestriction = "Agency";
        private final String bbieScValueDomain = "clm63055D16B_AgencyIdentification";

        Preconditions_TA_29_1_BIEBOMDoubleNested(AppUserObject developer, LibraryObject library, String prevRelease) {
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, 
                    "BOM. BOM", prevRelease);
            this.topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, developer, "WIP");
        }
    }

    private class Preconditions_TA_29_1_JournalEntry {
        private final TopLevelASBIEPObject topLevelASBIEP;

        // ASBIE
        private final ArrayList<String> asbiePaths = new ArrayList<>();
        private final String asbieRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String asbieContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        // BBIE
        private final ArrayList<String> bbiePaths = new ArrayList<>();
        private final String bbieRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieExample = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieValueConstraint = "Fixed Value";
        private final String bbieFixedValue = RandomStringUtils.secure().nextAlphanumeric(50, 100).trim();
        private final String bbieValueDomainRestriction = "Code";
        private final String bbieValueDomain = "oacl_SystemEnvironmentCode";

        // BBIE_SC
        private final ArrayList<String> bbieScPaths = new ArrayList<>();
        private final String bbieScRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScExample = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScValueConstraint = "Fixed Value";
        private final String bbieScFixedValue = RandomStringUtils.secure().nextAlphanumeric(50, 100).trim();
        private final String bbieScValueDomainRestriction = "Agency";
        private final String bbieScValueDomain = "clm63055D16B_AgencyIdentification";

        Preconditions_TA_29_1_JournalEntry(AppUserObject usera, LibraryObject library, String prevRelease) {
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, 
                    "Post Acknowledge Journal Entry. Post Acknowledge Journal Entry", prevRelease);
            this.topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");
        }
    }

    private class Preconditions_TA_29_1_BIECAGUplift {
        private final TopLevelASBIEPObject topLevelASBIEP;

        // ASBIE
        private final ArrayList<String> asbiePaths = new ArrayList<>();
        private final String asbieRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String asbieContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();

        // BBIE
        private final ArrayList<String> bbiePaths = new ArrayList<>();
        private final String bbieRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieExample = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieValueConstraint = "Fixed Value";
        private final String bbieFixedValue = RandomStringUtils.secure().nextAlphanumeric(50, 100).trim();
        private final String bbieValueDomainRestriction = "Code";
        private final String bbieValueDomain = "oacl_SystemEnvironmentCode";

        // BBIE_SC
        private final ArrayList<String> bbieScPaths = new ArrayList<>();
        private final String bbieScRemark = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScExample = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScContextDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();
        private final String bbieScValueConstraint = "Fixed Value";
        private final String bbieScFixedValue = RandomStringUtils.secure().nextAlphanumeric(50, 100).trim();
        private final String bbieScValueDomainRestriction = "Agency";
        private final String bbieScValueDomain = "clm63055D16B_AgencyIdentification";

        Preconditions_TA_29_1_BIECAGUplift(AppUserObject usera, LibraryObject library, String prevRelease) {
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(library, 
                    "Child Item Reference. Child Item Reference", prevRelease);
            this.topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");
        }
    }

    private class RandomCodeListWithStateContainer {
        private final AppUserObject appUser;
        private final HashMap<String, CodeListObject> stateCodeLists = new HashMap<>();
        private final HashMap<String, CodeListValueObject> stateCodeListValues = new HashMap<>();
        private List<String> states = new ArrayList<>();

        public RandomCodeListWithStateContainer(AppUserObject appUser, ReleaseObject release, NamespaceObject namespace, List<String> states) {
            this.appUser = appUser;
            this.states = states;

            for (int i = 0; i < this.states.size(); ++i) {
                CodeListObject codeList;
                CodeListValueObject codeListValue;
                String state = this.states.get(i);
                {
                    codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(this.appUser, namespace, release, state);
                    codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, this.appUser);
                    stateCodeLists.put(state, codeList);
                    stateCodeListValues.put(state, codeListValue);
                }
            }
        }
    }
}
