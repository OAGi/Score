package org.oagi.score.e2e.TS_29_BIEUplifting;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.math.BigInteger;
import java.time.Duration;
import java.util.*;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
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

    @Test
    public void test_TA_29_1_1() {
        String prevRelease = "10.8.7.1";
        String currRelease = "10.9";

        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(currRelease);
        assertThrows(TimeoutException.class, () -> upliftBIEPage.setTargetBranch(prevRelease));
    }

    @Test
    public void test_TA_29_1_2_BIE_Uplift() {
        String prevRelease = "10.8.7.1";
        String currRelease = "10.9";

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);

        Preconditions_TA_29_1_2 preconditionsTa2912 = preconditions_TA_29_1_2_Uplift_BIEUserbProduction(usera, prevRelease);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        // Uplift Production BIE
        upliftBIEPage.openPage();
        upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prevRelease);
        upliftBIEPage.setTargetBranch(currRelease);
        upliftBIEPage.setState("Production");
        upliftBIEPage.setPropertyTerm(preconditionsTa2912.topLevelASBIEP.getPropertyTerm());
        upliftBIEPage.setOwner(usera.getLoginId());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.next();
        EditBIEPage editBIEPage = upliftBIEVerificationPage.uplift();
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEquals(developer.getLoginId(), getText(topLevelASBIEPPanel.getOwnerField()));
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

    private Preconditions_TA_29_1_2 preconditions_TA_29_1_2_Uplift_BIEUserbProduction(AppUserObject usera, String prevRelease) {
        Preconditions_TA_29_1_2 preconditionsTa2912 = new Preconditions_TA_29_1_2(usera, prevRelease);

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

    private Preconditions_TA_29_1_BIE1QA preconditions_TA_9_1_4_and_TA_29_1_5a_and_TA_29_1_6a(AppUserObject usera, String prevRelease) {
        Preconditions_TA_29_1_BIE1QA preconditionsTa2914 = new Preconditions_TA_29_1_BIE1QA(usera, prevRelease);
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
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

        WebElement bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Last Modification Date Time");
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Identifier");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Name");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        WebElement asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Incorporation Location");
        waitFor(ofMillis(1000L));
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        asbiePanel.toggleUsed();
        asbiePanel.setRemark(preconditionsTa2914.asbieRemark);
        editBIEPage.hitUpdateButton();

        editBIEPage.openPage(); // refresh the page to load only one 'Scheme Version Identifier' node on display.
        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Identifier Set/Scheme Version Identifier");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setRemark(preconditionsTa2914.bbieRemark);
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

        editBIEPage.openPage(); // refresh the page to load only one 'Scheme Version Identifier' node on display.
        WebElement bbieSCNode = editBIEPage.getNodeByPath("/Enterprise Unit/Cost Center Identifier/Scheme Agency Identifier");
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setRemark(preconditionsTa2914.bbieScRemark);
        bbiescPanel.setExample(preconditionsTa2914.bbieScExample);
        bbiescPanel.setValueConstraint(preconditionsTa2914.bbieScValueConstraint);
        bbiescPanel.setFixedValue(preconditionsTa2914.bbieScFixedValue);
        bbiescPanel.setValueDomainRestriction(preconditionsTa2914.bbieScValueDomainRestriction);
        bbiescPanel.setValueDomain(preconditionsTa2914.bbieScValueDomain);
        bbiescPanel.setContextDefinition(preconditionsTa2914.bbieScContextDefinition);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Indicator");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setCardinalityMin(11);
        bbiePanel.setCardinalityMax(99);
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

        editBIEPage.moveToQA();
        homePage.logout();
        return preconditionsTa2914;
    }

    @Test
    public void test_TA_29_1_3() {
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
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setPropertyTerm(topLevelASBIEP.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//td//*[contains(text(), \"" + topLevelASBIEP.getPropertyTerm() + "\")]//ancestor::tr[1]/td[1]/mat-checkbox/label/span[1]")).size());
    }

    @Test
    public void test_TA_29_1_4_and_TA_29_1_5a_and_TA_29_1_6a() {
        String prev_release = "10.8.6";
        String curr_release = "10.9";
        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(userb);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        Preconditions_TA_29_1_BIE1QA preconditionsTa2914 = preconditions_TA_9_1_4_and_TA_29_1_5a_and_TA_29_1_6a(usera, prev_release);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setState("QA");
        upliftBIEPage.setPropertyTerm(preconditionsTa2914.topLevelASBIEP.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.next();
//        for (String path :
//                Arrays.asList("/Enterprise Unit", "/Enterprise Unit/Type Code", "/Enterprise Unit/Identifier",
//                        "/Enterprise Unit/Identifier Set/Scheme Version Identifier",
//                        "/Enterprise Unit/Identifier Set/Scheme Agency Identifier",
//                        "/Enterprise Unit/Identifier Set/Identifier",
//                        "/Enterprise Unit/Extension/Name",
//                        "/Enterprise Unit/Extension")) {
//            WebElement sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE(path);
//            waitFor(ofMillis(1000L));
//            assertFalse(isElementPresent(By.xpath("//mat-card-content[contains(@class, \"mat-mdc-card-content\")]" +
//                    "/div[2]/div[1]//cdk-virtual-scroll-viewport//ancestor::div[1]/mat-checkbox[1]")));
//        }

        for (String path :
                Arrays.asList("/Enterprise Unit/Type Code", "/Enterprise Unit/Identifier",
                        "/Enterprise Unit/Identifier Set/Scheme Version Identifier",
                        "/Enterprise Unit/Identifier Set/Identifier",
                        "/Enterprise Unit/Extension/Name",
                        "/Enterprise Unit/Extension")) {
            WebElement sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE(path);
            waitFor(ofMillis(1000L));
            WebElement targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE(path);
            waitFor(ofMillis(1000L));

            WebElement node = getDriver().findElement(By.xpath("//mat-card-content[contains(@class, \"mat-mdc-card-content\")]" +
                    "/div[2]/div[2]//cdk-virtual-scroll-viewport//ancestor::div[1]/mat-checkbox[1]"));
            assertChecked(node);
            assertDisabled(node);
        }
        escape(getDriver());
        EditBIEPage editBIEPage = upliftBIEVerificationPage.uplift();
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
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

        WebElement asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Identifier Set");
        waitFor(ofMillis(1000L));
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        assertChecked(asbiePanel.getUsedCheckbox());
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
    public void test_TA_29_1_5b() {
        String prev_release = "10.8.7.1";
        String curr_release = "10.9";
        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(userb);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        Preconditions_TA_29_1_BIE1QA preconditionsTa2915 = preconditions_TA_9_1_4_and_TA_29_1_5a_and_TA_29_1_6a(usera, prev_release);

        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setState("QA");
        upliftBIEPage.setPropertyTerm(preconditionsTa2915.topLevelASBIEP.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.next();

        WebElement sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Enterprise Unit/Extension/Indicator");
        WebElement targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Enterprise Unit/GL Entity Identifier/Scheme Version Identifier");
        assertTrue(getDriver().findElement(By.xpath("//mat-card-content[contains(@class, \"mat-mdc-card-content\")]" +
                "/div[2]/div[2]//cdk-virtual-scroll-viewport//ancestor::div[1]/mat-checkbox[1]")).isEnabled());
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Enterprise Unit/General Ledger Element");
        assertEnabled(getDriver().findElement(By.xpath("//mat-card-content[contains(@class, \"mat-mdc-card-content\")]" +
                "/div[2]/div[2]//cdk-virtual-scroll-viewport//ancestor::div[1]/mat-checkbox[1]")));
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("Enterprise Unit/Profit Center Identifier");
        assertEnabled(getDriver().findElement(By.xpath("//mat-card-content[contains(@class, \"mat-mdc-card-content\")]" +
                "/div[2]/div[2]//cdk-virtual-scroll-viewport//ancestor::div[1]/mat-checkbox[1]")));
        homePage.logout();
    }

    @Test
    public void test_TA_29_1_5c_and_TA_29_1_7_and_TA_29_1_8() {
        String prev_release = "10.8.8";
        String curr_release = "10.9";
        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(userb);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        Preconditions_TA_29_1_BIE1QA preconditionsTa2915 = preconditions_TA_9_1_4_and_TA_29_1_5a_and_TA_29_1_6a(usera, prev_release);

        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setState("QA");
        upliftBIEPage.setPropertyTerm(preconditionsTa2915.topLevelASBIEP.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.next();
        //different green
        WebElement sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Enterprise Unit/Extension/Incorporation Location/CAGEID");
        clickOn(sourceNode);
        WebElement targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Enterprise Unit/Profit Center Identifier");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Profit Center Identifier"));
        escape(getDriver());

        //same green
        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Enterprise Unit/Extension/Usage Description");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Enterprise Unit/Description");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Description"));
        escape(getDriver());

        //different blue
        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Enterprise Unit/Extension/Incorporation Location/Physical Address");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Enterprise Unit/Classification/Codes");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Codes"));
        escape(getDriver());

        //same blue
        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Enterprise Unit/Extension/Code List/Code List Value");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Enterprise Unit/Classification/Code List Value");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Code List Value"));
        escape(getDriver());

        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Enterprise Unit/Extension/Revised Item Status/Reason Code");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Enterprise Unit/Status/Reason Code");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Reason Code"));
        escape(getDriver());
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
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith(preconditionsTa2915.bbieValueDomain));

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Description");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEnabled(bbiePanel.getUsedCheckbox());
        assertChecked(bbiePanel.getUsedCheckbox());
        assertEquals(preconditionsTa2915.bbieRemark, getText(bbiePanel.getRemarkField()));
        assertEquals(preconditionsTa2915.bbieExample, getText(bbiePanel.getExampleField()));
        assertEquals(preconditionsTa2915.bbieContextDefinition, getText(bbiePanel.getContextDefinitionField()));
        assertEquals(preconditionsTa2915.bbieFixedValue, getText(bbiePanel.getFixedValueField()));
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith(preconditionsTa2915.bbieValueDomain));

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

        //Test part where only the association information are transferred
        upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setPropertyTerm(preconditionsTa2915.topLevelASBIEP.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        tr = upliftBIEPage.getTableRecordAtIndex(1);
        td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        upliftBIEVerificationPage = upliftBIEPage.next();
        upliftBIEVerificationPage.goToNodeInSourceBIE("/Enterprise Unit/Extension/Revised Item Status");
        upliftBIEVerificationPage.goToNodeInTargetBIE("/Enterprise Unit/General Ledger Element");
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("General Ledger Element"));
        editBIEPage = upliftBIEVerificationPage.uplift();
        asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/General Ledger Element");
        waitFor(ofMillis(1000L));
        asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        assertEnabled(asbiePanel.getUsedCheckbox());
        assertChecked(asbiePanel.getUsedCheckbox());
        assertEquals("99", getText(asbiePanel.getCardinalityMaxField()));

        bbiescNode = editBIEPage.getNodeByPath("/Enterprise Unit/General Ledger Element/Element/Sequence Number Number");
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        assertNotChecked(bbiescPanel.getUsedCheckbox());
        assertDisabled(bbiescPanel.getRemarkField());
        homePage.logout();
    }

    public void clickOn(WebElement element) {
        try {
            Actions action = new Actions(getDriver());
            action.moveToElement(element).perform();
            element.sendKeys(Keys.ENTER);
        } catch (Exception rerun) {
            waitFor(ofMillis(1000));
            String ele = element.toString();
            String ppath;
            //issue: sometime selenium adds a bracket ] at the end. Remove it.
            if (ele.contains("]]")) {
                ppath = ele.substring(ele.indexOf(" //"), ele.indexOf("]]") + 1);
            } else if (ele.contains("span]")) {
                ppath = ele.substring(ele.indexOf(" //"), ele.indexOf("span]") + 4);
            } else if (ele.contains("kbox]")) {
                ppath = ele.substring(ele.indexOf(" //"), ele.indexOf("kbox]") + 4);
            } else if (ele.contains("div]")) {
                ppath = ele.substring(ele.indexOf(" //"), ele.indexOf("div]") + 3);
            } else if (ele.contains("a]")) {
                ppath = ele.substring(ele.indexOf(" //"), ele.indexOf("a]") + 1);
            } else if (ele.contains("icon]")) {
                ppath = ele.substring(ele.indexOf(" //"), ele.indexOf("icon]") + 4);
            } else if (ele.contains("li]")) {
                ppath = ele.substring(ele.indexOf(" //"), ele.indexOf("li]") + 2);
            } else {
                ppath = ele.substring(ele.indexOf(" //"));
            }
            WebElement recreatedElement = getDriver().findElement(By.xpath(ppath));
            Actions action = new Actions(getDriver());
            action.moveToElement(recreatedElement).perform();
            recreatedElement.click();
        }
    }

    @Test
    public void test_TA_29_1_5d_and_TA_29_1_6ab() {
        String prev_release = "10.8.7.1";
        String curr_release = "10.9";
        AppUserObject userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(userb);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        Preconditions_TA_29_1_5d_BIEReusedChild preconditionsTa2915dReusedChild = preconditions_ta_29_1_5d_ReusedChild(userb, prev_release);
        Preconditions_TA_29_1_5d_BIEReusedParent preconditionsTa2915dReusedParent = preconditions_ta_29_1_5d_ReusedParent(userb, prev_release);
        Preconditions_TA_29_1_5d_BIEReusedScenario preconditionsTa2915dReusedScenario = new Preconditions_TA_29_1_5d_BIEReusedScenario(userb, prev_release);
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
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setPropertyTerm(preconditionsTa2915dReusedChild.topLevelASBIEP.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.next();
        editBIEPage = upliftBIEVerificationPage.uplift();
        editBIEPage.moveToQA();
        editBIEPage.moveToProduction();

        //uplift BIEUserbReusedParent
        upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setPropertyTerm(preconditionsTa2915dReusedParent.topLevelASBIEP.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        tr = upliftBIEPage.getTableRecordAtIndex(1);
        td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        upliftBIEVerificationPage = upliftBIEPage.next();
        selectProfileBIEToReuseDialog = upliftBIEVerificationPage.reuseBIEOnNode("/From UOM Package/Unit Packaging", "Unit Packaging");
        selectProfileBIEToReuseDialog.selectBIEToReuse(preconditionsTa2915dReusedChild.topLevelASBIEP);
        editBIEPage = upliftBIEVerificationPage.uplift();
        TopLevelASBIEPObject upliftedReusedParentTopLevelASBIEP = editBIEPage.getTopLevelASBIEP();
        editBIEPage.moveToQA();
        editBIEPage.moveToProduction();

        //Test Assertion Verification

        upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setPropertyTerm(preconditionsTa2915dReusedScenario.topLevelASBIEP.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        tr = upliftBIEPage.getTableRecordAtIndex(1);
        td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
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
        assertEquals("99", getText(asbiePanel.getCardinalityMaxField()));
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
        asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        assertEnabled(asbiePanel.getUsedCheckbox());
        assertChecked(asbiePanel.getUsedCheckbox());
        assertEquals("0", getText(asbiePanel.getCardinalityMinField()));
        assertEquals("1", getText(asbiePanel.getCardinalityMaxField()));

        //unreuse FROM UOM Package
        viewEditBIEPage.openPage();
        editBIEPage = viewEditBIEPage.openEditBIEPage(upliftedReusedScenarioTopLevelASBIEP);
        editBIEPage.getNodeByPath("/UOM Code Conversion Rate/From UOM Package");
        editBIEPage.clickOnDropDownMenuByPath("/UOM Code Conversion Rate/From UOM Package");
        waitFor(ofMillis(1000L));
        click(getDriver().findElement(By.xpath("//span[contains(text(), \"Remove Reused BIE\")]")));
        click(getDriver().findElement(By.xpath("//span[contains(text(), \"Remove\")]//ancestor::button[1]")));
        waitFor(ofMillis(1000L));
        editBIEPage.getNodeByPath("/UOM Code Conversion Rate/From UOM Package");
        assertEquals(0, getDriver().findElements(By.xpath("//span[.=\"From UOM Package\"]//ancestor::div[1]/fa-icon")).size());
        editBIEPage.getNodeByPath("/UOM Code Conversion Rate/From UOM Package/Unit Packaging");
        assertEquals(0, getDriver().findElements(By.xpath("//span[.=\"Unit Packaging\"]//ancestor::div[1]/fa-icon")).size());
        homePage.logout();
    }

    private Preconditions_TA_29_1_5d_BIEReusedChild preconditions_ta_29_1_5d_ReusedChild(AppUserObject usera, String prevRelease) {
        Preconditions_TA_29_1_5d_BIEReusedChild preconditionsTa2915d = new Preconditions_TA_29_1_5d_BIEReusedChild(usera, prevRelease);
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
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

    private Preconditions_TA_29_1_5d_BIEReusedParent preconditions_ta_29_1_5d_ReusedParent(AppUserObject usera, String prevRelease) {
        Preconditions_TA_29_1_5d_BIEReusedParent preconditionsTa2915d = new Preconditions_TA_29_1_5d_BIEReusedParent(usera, prevRelease);
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
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
    public void test_TA_29_1_9a() {
        String prev_release = "10.8.7.1";
        String curr_release = "10.9";

        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        Preconditions_TA_29_1_TOPBIEGETBOM preconditionsTa2919_TOPBIEGETBOM = preconditions_TA_29_1_TOPBIEGETBOM(usera, prev_release);
        Preconditions_TA_29_1_BIEPrimitiveDate preconditionsTa2919_BIEPrimitiveDate = preconditions_TA_29_1_BIEPrimitiveDate(usera, prev_release);
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        //Uplift TOPBIEGETBOM
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setPropertyTerm(preconditionsTa2919_TOPBIEGETBOM.topLevelASBIEP.getPropertyTerm());
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
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);

        upliftBIEPage.setPropertyTerm(preconditionsTa2919_BIEPrimitiveDate.topLevelASBIEP.getPropertyTerm());
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

    private Preconditions_TA_29_1_TOPBIEGETBOM preconditions_TA_29_1_TOPBIEGETBOM(AppUserObject usera, String prevRelease) {
        Preconditions_TA_29_1_TOPBIEGETBOM preconditionsTa2919a = new Preconditions_TA_29_1_TOPBIEGETBOM(usera, prevRelease);
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
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

    private Preconditions_TA_29_1_BIEPrimitiveDate preconditions_TA_29_1_BIEPrimitiveDate(AppUserObject usera, String prevRelease) {
        Preconditions_TA_29_1_BIEPrimitiveDate preconditionsTa2919a = new Preconditions_TA_29_1_BIEPrimitiveDate(usera, prevRelease);
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
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
    public void test_TA_29_1_9b_and_TA_29_1_9c() {
        String prev_release = "10.8.8";
        String curr_release = "10.9";
        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        Preconditions_TA_29_1_TOPBIEGETBOM preconditionsTa2919TOPBIEGETBOM = preconditions_TA_29_1_TOPBIEGETBOM(usera, prev_release);
        Preconditions_TA_29_1_BIEPrimitiveDate preconditionsTa2919BiePrimitiveDate = preconditions_TA_29_1_BIEPrimitiveDate(usera, prev_release);

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
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
        selectCCPropertyPage.selectAssociation("Method Consequence Text. Text");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/BOM Option User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Record Set Reference Identifier. Identifier");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/BOM Option User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Record Set Total Number. Number");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/BOM Option User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Latest Start Date Time. Date Time");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/BOM Option User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Request Language Code. Language_ Code");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/BOM Option User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Transport Temperature. Measure");
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

        //Uplift TOPBIEGETBOM
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setPropertyTerm(preconditionsTa2919TOPBIEGETBOM.topLevelASBIEP.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.next();
        //match BBIE or BBIEsc nodes
        WebElement sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Get BOM/Data Area/BOM/BOM Option/Extension/Effectivity Relation Code");
        clickOn(sourceNode);
        WebElement targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Get BOM/Data Area/BOM/BOM Header/Revision Identifier");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Revision Identifier"));
        escape(getDriver());

        //
        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Get BOM/Data Area/BOM/BOM Option/Extension/Method Consequence Text");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Get BOM/Application Area/Intermediary/Component Identifier");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Component Identifier"));
        escape(getDriver());
        //
        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Get BOM/Data Area/BOM/BOM Option/Extension/Record Set Reference Identifier");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Get BOM/Application Area/Sender/Authorization Identifier");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Authorization Identifier"));
        escape(getDriver());
        //
        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Get BOM/Data Area/BOM/BOM Option/Extension/Record Set Total Number");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Get BOM/Data Area/BOM/BOM Header/Attachment/File Size Quantity");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("File Size Quantity"));
        escape(getDriver());
        //
        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Get BOM/Data Area/BOM/BOM Option/Extension/Latest Start Date Time");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Get BOM/Data Area/BOM/BOM Header/Attachment/Document Date Time");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Document Date Time"));
        escape(getDriver());
        //BBIE to BBIE
        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Get BOM/Data Area/BOM/BOM Option/Extension/Request Language Code");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Get BOM/Data Area/BOM/BOM Header/Attachment/File Type Code");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("File Type Code"));
        escape(getDriver());

        //BBIE_SC to BBIE_SC
        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Get BOM/Data Area/BOM/BOM Option/Extension/Request Language Code/List Agency Identifier");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Get BOM/Data Area/BOM/BOM Header/Attachment/File Type Code/List Version Identifier");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("List Version Identifier"));
        escape(getDriver());

        //BBIE_SC to BBIE_SC
        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Get BOM/Data Area/BOM/BOM Option/Extension/Transport Temperature/Unit Code");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Get BOM/Application Area/Sender/Logical Identifier/Scheme Version Identifier");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Scheme Version Identifier"));
        escape(getDriver());

        editBIEPage = upliftBIEVerificationPage.uplift();
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Revision Identifier", 3);
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("any URI", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Application Area/Intermediary/Component Identifier", 3);
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("any URI", getText(bbiePanel.getValueDomainField()));
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Application Area/Sender/Authorization Identifier", 3);
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("language", getText(bbiePanel.getValueDomainField()));
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Attachment/File Size Quantity", 3);
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("positive integer", getText(bbiePanel.getValueDomainField()));

        editBIEPage.openPage();
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Attachment/File Type Code", 3);
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("token", getText(bbiePanel.getValueDomainField()));
        editBIEPage.expandTree("File Type Code");
        bbiescNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Attachment/File Type Code/List Agency Identifier");
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        assertEquals("token", getText(bbiescPanel.getValueDomainField()));
        bbiescNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Attachment/File Type Code/List Version Identifier");
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        assertEquals("token", getText(bbiescPanel.getValueDomainField()));

        editBIEPage.openPage();
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Application Area/Sender/Logical Identifier", 3);
        editBIEPage.expandTree("Logical Identifier");
        bbiescNode = editBIEPage.getNodeByPath("/Get BOM/Application Area/Sender/Logical Identifier/Scheme Version Identifier");
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        assertEquals("normalized string", getText(bbiescPanel.getValueDomainField()));
        homePage.logout();
    }

    @Test
    public void test_TA_29_1_10a() {
        String prev_release = "10.8.7.1";
        String curr_release = "10.9";
        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        //BIEBOMDoubleNested previousRelease
        Preconditions_TA_29_1_BIEBOMDoubleNested preconditionsTa2910BIEBOMDoubleNested = preconditions_TA_29_10a_BIEBOMDoubleNested(developer, prev_release);
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(preconditionsTa2910BIEBOMDoubleNested.topLevelASBIEP);

        //Uplift BIEBOMDoubleNested
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setPropertyTerm(preconditionsTa2910BIEBOMDoubleNested.topLevelASBIEP.getPropertyTerm());
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
        assertTrue(getText(bbiescPanel.getValueDomainField()).startsWith("clm63055D16B_AgencyIdentification"));
        homePage.logout();
    }

    private Preconditions_TA_29_1_BIEBOMDoubleNested preconditions_TA_29_10a_BIEBOMDoubleNested(AppUserObject developer, String prevRelease) {
        Preconditions_TA_29_1_BIEBOMDoubleNested preconditionsTa2910a = new Preconditions_TA_29_1_BIEBOMDoubleNested(developer, prevRelease);
        NamespaceObject devNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer);
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
    public void test_TA_29_1_10b() {
        String prev_release = "10.8.7.1";
        String curr_release = "10.9";
        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        Preconditions_TA_29_1_JournalEntry preconditionsTa2910JournalEntry = preconditions_TA_29_10b_JournalEntry(usera, prev_release);

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        BIEMenu bieMenu = homePage.getBIEMenu();
        //JournalEntry prev_release
        bieMenu = homePage.getBIEMenu();

        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setPropertyTerm(preconditionsTa2910JournalEntry.topLevelASBIEP.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.next();

        WebElement sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Extension/Usage Description");
        clickOn(sourceNode);
        WebElement targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Post Acknowledge Journal Entry/Data Area/Journal Entry/Journal Entry Line/Debit Credit Code");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Debit Credit Code"));

        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Extension/Control Objective Category");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Post Acknowledge Journal Entry/Data Area/Journal Entry/Journal Entry Line/Tax Base Functional Amount");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Tax Base Functional Amount"));

        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Extension/Control Objective Category/List Version Identifier");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Post Acknowledge Journal Entry/Application Area/Sender/Logical Identifier/Scheme Identifier");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Scheme Identifier"));

        EditBIEPage editBIEPage = upliftBIEVerificationPage.uplift();
        WebElement bbieNode = editBIEPage.getNodeByPath("/Post Acknowledge Journal Entry/Data Area/Journal Entry/Journal Entry Line/Debit Credit Code");
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertChecked(bbiePanel.getUsedCheckbox());
        assertEnabled(bbiePanel.getUsedCheckbox());
        bbieNode = editBIEPage.getNodeByPath("/Post Acknowledge Journal Entry/Data Area/Journal Entry/Journal Entry Line/Tax Base Functional Amount");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith("oacl_RiskCode"));
        WebElement bbieSCNode = editBIEPage.getNodeByPath("/Post Acknowledge Journal Entry/Application Area/Sender/Logical Identifier/Scheme Identifier");
        waitFor(ofMillis(1000L));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertTrue(getText(bbiescPanel.getValueDomainField()).startsWith("clm6ConditionTypeCode1_ConditionTypeCode"));
        homePage.logout();
    }

    private Preconditions_TA_29_1_JournalEntry preconditions_TA_29_10b_JournalEntry(AppUserObject usera, String prevRelease) {
        Preconditions_TA_29_1_JournalEntry preconditionsTa2910b = new Preconditions_TA_29_1_JournalEntry(usera, prevRelease);
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(preconditionsTa2910b.topLevelASBIEP);
        waitFor(Duration.ofMillis(2500));
        ACCExtensionViewEditPage accExtensionViewEditPage =
                editBIEPage.extendBIELocallyOnNode("/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Extension");
        SelectAssociationDialog selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Change Status User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Usage Description. Text");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Change Status User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Control Objective Category. Code");
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
    public void test_TA_29_1_11a_and_TA_29_11b() {
        String prev_release = "10.8.7.1";
        String curr_release = "10.9";
        Map<String, CodeListObject> upliftedCodeLists = new HashMap<>();
        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        //BIECAGUplift prev_release
        Preconditions_TA_29_1_BIECAGUplift preconditionsTa2911BIECAGUplift = preconditions_TA_29_11_BIECAGUplift(usera, prev_release);
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
        ReleaseObject prev_releaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(prev_release);
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
                getAPIFactory().getCodeListAPI().getCodeListByCodeListNameAndReleaseNum("oacl_MatchDocumentCode", prev_release)
        );
        editCodeListPage.hitDeriveCodeListBasedOnThisButton();
        editCodeListPage.setName("CLuserderived_BIEUp");
        editCodeListPage.setNamespace(euNamespace);
        editCodeListPage.setDefinition("aDefinition");
        editCodeListPage.hitUpdateButton();

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
        bbiePanel.setValueDomain("CLuserderived_BIEUp");
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
        bbiePanel.setValueDomain("CLuserderived_BIEUp");
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
        bbiescPanel.setValueDomain("CLuserderived_BIEUp");
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Export Control/Encryption Status Code");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain("clm6TimeFormatCode1_TimeFormatCode");
        editBIEPage.hitUpdateButton();

        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setPropertyTerm(preconditionsTa2911BIECAGUplift.topLevelASBIEP.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.next();

        WebElement sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Child Item Reference/Extension/Latest Start Date Time");
        clickOn(sourceNode);
        WebElement targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Child Item Reference/Child Item/Manufacturing Party/CCRID");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("CCRID"));

        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Child Item Reference/Extension/Transport Temperature");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Child Item Reference/Child Item/Manufacturing Party/Account Identifier");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Account Identifier"));

        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Child Item Reference/Extension/Validation Indicator");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Child Item Reference/Child Item/Manufacturing Party/CAGEID");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("CAGEID"));

        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Child Item Reference/Extension/Method Consequence Text");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Child Item Reference/Child Item/Manufacturing Party/DODAACID");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("DODAACID"));

        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Child Item Reference/Extension/Record Set Total Number");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Child Item Reference/Child Item/Manufacturing Party/SCACID");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("SCACID"));


        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Child Item Reference/Extension/Effectivity Relation Code");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Child Item Reference/Child Item/Revision Identifier");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Revision Identifier"));

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

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Manufacturing Party/CCRID");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("normalized string", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Manufacturing Party/Account Identifier");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("normalized string", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Manufacturing Party/CAGEID");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("normalized string", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Manufacturing Party/DODAACID");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("normalized string", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Manufacturing Party/SCACID");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("normalized string", getText(bbiePanel.getValueDomainField()));

        //Uplift codeList page
        ReleaseObject sourceRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(prev_release);
        ReleaseObject targetRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(curr_release);
        UpliftCodeListPage upliftCodeListPage = bieMenu.openUpliftCodeListSubMenu();

        for (CodeListObject codeList : Arrays.asList(
                euCodeListWithStateContainer.stateCodeLists.get("WIP"),
                euCodeListWithStateContainer.stateCodeLists.get("QA"),
                euCodeListWithStateContainer.stateCodeLists.get("Production"),
                euCodeListWithStateContainer.stateCodeLists.get("Deleted"),
                CLaccessUseraDeprecated,
                getAPIFactory().getCodeListAPI().getCodeListByCodeListNameAndReleaseNum("CLuserderived_BIEUp", prev_release))) {
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

        //Uplift BIECAGUplift after CodeList uplift

        upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setPropertyTerm(preconditionsTa2911BIECAGUplift.topLevelASBIEP.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        tr = upliftBIEPage.getTableRecordAtIndex(1);
        td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        upliftBIEVerificationPage = upliftBIEPage.next();

        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Child Item Reference/Extension/Effectivity Relation Code");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Child Item Reference/Child Item/Revision Identifier");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Revision Identifier"));

        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Child Item Reference/Extension/Validation Indicator");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Child Item Reference/Child Item/Manufacturing Party/CAGEID");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("CAGEID"));

        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Child Item Reference/Extension/Method Consequence Text");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Child Item Reference/Child Item/Manufacturing Party/DODAACID");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("DODAACID"));

        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Child Item Reference/Extension/Record Set Total Number");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Child Item Reference/Child Item/Manufacturing Party/SCACID");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("SCACID"));


        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Child Item Reference/Extension/Effectivity Relation Code");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Child Item Reference/Child Item/Revision Identifier");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Revision Identifier"));

        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Child Item Reference/Extension/Latest Start Date Time");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Child Item Reference/Child Item/Manufacturing Party/CCRID");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("CCRID"));


        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Child Item Reference/Extension/Transport Temperature");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Child Item Reference/Child Item/Manufacturing Party/Account Identifier");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Account Identifier"));

        editBIEPage = upliftBIEVerificationPage.uplift();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Manufacturing Party/CAGEID");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith(CLaccessendUserqa));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Manufacturing Party/DODAACID");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith(CLaccessendUserproduction));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Manufacturing Party/SCACID");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith(CLaccessendUserdeleted));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Manufacturing Party/CCRID");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith(CLaccessUseraDeprecated.getName()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Manufacturing Party/Account Identifier");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith("CLuserderived_BIEUp"));

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
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith("CLuserderived_BIEUp"));

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
        assertTrue(getText(bbiescPanel.getValueDomainField()).startsWith("CLuserderived_BIEUp"));

        bbieSCNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/MFAGID/Scheme Agency Identifier");
        waitFor(ofMillis(1000L));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertTrue(getText(bbiescPanel.getValueDomainField()).startsWith(CLaccessendUserqa));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Export Control/Encryption Status Code");
        waitFor(ofMillis(1000L));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith("clm6TimeFormatCode1_TimeFormatCode"));
    }

    private Preconditions_TA_29_1_BIECAGUplift preconditions_TA_29_11_BIECAGUplift(AppUserObject usera, String prevRelease) {
        Preconditions_TA_29_1_BIECAGUplift preconditionsTa2911 = new Preconditions_TA_29_1_BIECAGUplift(usera, prevRelease);
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
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
        selectCCPropertyPage.selectAssociation("Transport Temperature. Measure");
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
    public void test_TA_29_1_12() {
        String prev_release = "10.8.8";
        String curr_release = "10.9";

        AppUserObject usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        //JournalEntry prev_release
        Preconditions_TA_29_1_JournalEntry preconditionsTa2912JournalEntry = preconditions_TA_29_10b_JournalEntry(usera, prev_release);
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
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setPropertyTerm(preconditionsTa2912JournalEntry.topLevelASBIEP.getPropertyTerm());
        upliftBIEPage.setState("QA");
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.next();
        click(getDriver(), upliftBIEVerificationPage.getNextButton());
        invisibilityOfLoadingContainerElement(PageHelper.wait(getDriver(), ofSeconds(900L), ofMillis(500L)));
        assertTrue(getDriver().findElement(By.xpath("//*[contains(text(), \"Usage Description\")]")).isDisplayed());
        assertTrue(getDriver().findElement(By.xpath("//*[contains(text(), \"Control Objective Category\")]")).isDisplayed());
        assertTrue(getDriver().findElement(By.xpath("//*[contains(text(), \"List Version Identifier\")]")).isDisplayed());
        assertTrue(getDriver().findElement(By.xpath("//*[contains(text(), \"BCCP\")]")).isDisplayed());
        assertTrue(getDriver().findElement(By.xpath("//*[contains(text(), \"DT_SC\")]")).isDisplayed());
        assertTrue(getDriver().findElement(By.xpath("//*[contains(text(), \"ASCCP\")]")).isDisplayed());
        assertTrue(getDriver().findElement(By.xpath("//*[contains(text(), \"Not selected\")]")).isDisplayed());
        assertTrue(getDriver().findElement(By.xpath("//*[contains(text(), \"System\")]")).isDisplayed());
        assertTrue(getDriver().findElement(By.xpath("//*[contains(text(), \"Unmatched\")]")).isDisplayed());
    }

    private class Preconditions_TA_29_1_2 {

        private final TopLevelASBIEPObject topLevelASBIEP;

        // ASBIE
        private final String asbiePath = "/Change Acknowledge Shipment Status/Application Area";
        private final String asbieRemark = randomPrint(50, 100).trim();
        private final String asbieContextDefinition = randomPrint(50, 100).trim();

        // BBIE
        private final String bbiePath = "/Change Acknowledge Shipment Status/System Environment Code";
        private final String bbieRemark = randomPrint(50, 100).trim();
        private final String bbieExample = randomPrint(50, 100).trim();
        private final String bbieContextDefinition = randomPrint(50, 100).trim();
        private final String bbieValueConstraint = "Fixed Value";
        private final String bbieFixedValue = randomAlphanumeric(50, 100).trim();
        private final String bbieValueDomainRestriction = "Code";
        private final String bbieValueDomain = "oacl_SystemEnvironmentCode";

        // BBIE_SC
        private final String bbieScPath = "/Change Acknowledge Shipment Status/Application Area/Scenario Identifier/Scheme Version Identifier";
        private final String bbieScRemark = randomPrint(50, 100).trim();
        private final String bbieScExample = randomPrint(50, 100).trim();
        private final String bbieScContextDefinition = randomPrint(50, 100).trim();
        private final String bbieScValueConstraint = "Fixed Value";
        private final String bbieScFixedValue = randomAlphanumeric(50, 100).trim();
        private final String bbieScValueDomainRestriction = "Agency";
        private final String bbieScValueDomain = "clm63055D16B_AgencyIdentification";

        Preconditions_TA_29_1_2(AppUserObject usera, String prevRelease) {
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                    "Change Acknowledge Shipment Status. Change Acknowledge Shipment Status", prevRelease);
            this.topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");
        }

    }

    private class Preconditions_TA_29_1_BIE1QA {
        private final TopLevelASBIEPObject topLevelASBIEP;
        private final String topLevelASBIEPBusinessTerm = "biz_term_" + randomAlphanumeric(5, 10);
        private final String topLevelASBIEPRemark = randomPrint(50, 100).trim();
        private final String topLevelASBIEPStatus = "status_" + randomAlphanumeric(5, 10);

        // ASBIE
        private final ArrayList<String> asbiePaths = new ArrayList<>();
        private final String asbieRemark = randomPrint(50, 100).trim();
        private final String asbieContextDefinition = randomPrint(50, 100).trim();

        // BBIE
        private final ArrayList<String> bbiePaths = new ArrayList<>();
        private final String bbieRemark = randomPrint(50, 100).trim();
        private final String bbieExample = randomPrint(50, 100).trim();
        private final String bbieContextDefinition = randomPrint(50, 100).trim();
        private final String bbieValueConstraint = "Fixed Value";
        private final String bbieFixedValue = randomAlphanumeric(50, 100).trim();
        private final String bbieValueDomainRestriction = "Code";
        private final String bbieValueDomain = "oacl_SystemEnvironmentCode";

        // BBIE_SC
        private final ArrayList<String> bbieScPaths = new ArrayList<>();
        private final String bbieScRemark = randomPrint(50, 100).trim();
        private final String bbieScExample = randomPrint(50, 100).trim();
        private final String bbieScContextDefinition = randomPrint(50, 100).trim();
        private final String bbieScValueConstraint = "Fixed Value";
        private final String bbieScFixedValue = randomAlphanumeric(50, 100).trim();
        private final String bbieScValueDomainRestriction = "Agency";
        private final String bbieScValueDomain = "clm63055D16B_AgencyIdentification";

        Preconditions_TA_29_1_BIE1QA(AppUserObject usera, String prevRelease) {
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
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
        private final String asbieRemark = randomPrint(50, 100).trim();
        private final String asbieContextDefinition = randomPrint(50, 100).trim();

        // BBIE
        private final String bbiePath = "/Unit Packaging/Capacity Per Package Quantity";
        private final String bbieRemark = randomPrint(50, 100).trim();
        private final String bbieExample = randomPrint(50, 100).trim();
        private final String bbieContextDefinition = randomPrint(50, 100).trim();
        private final String bbieValueConstraint = "Fixed Value";
        private final String bbieFixedValue = randomAlphanumeric(50, 100).trim();
        private final String bbieValueDomainRestriction = "Code";
        private final String bbieValueDomain = "oacl_SystemEnvironmentCode";

        // BBIE_SC
        private final String bbieScPath = "/Unit Packaging/UPC Packaging Level Code/List Agency Identifier";
        private final String bbieScRemark = randomPrint(50, 100).trim();
        private final String bbieScExample = randomPrint(50, 100).trim();
        private final String bbieScContextDefinition = randomPrint(50, 100).trim();
        private final String bbieScValueConstraint = "Fixed Value";
        private final String bbieScFixedValue = randomAlphanumeric(50, 100).trim();
        private final String bbieScValueDomainRestriction = "Agency";
        private final String bbieScValueDomain = "clm63055D16B_AgencyIdentification";

        Preconditions_TA_29_1_5d_BIEReusedChild(AppUserObject usera, String prevRelease) {
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                    "Unit Packaging. Packaging", prevRelease);
            this.topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");
        }
    }

    private class Preconditions_TA_29_1_5d_BIEReusedParent {
        private final TopLevelASBIEPObject topLevelASBIEP;
        // ASBIE
        private final String asbiePath = "/From UOM Package/Unit Packaging";
        private final String asbieRemark = randomPrint(50, 100).trim();
        private final String asbieContextDefinition = randomPrint(50, 100).trim();

        // BBIE
        private final String bbiePath = "/From UOM Package/UOM Code";
        private final String bbieRemark = randomPrint(50, 100).trim();
        private final String bbieExample = randomPrint(50, 100).trim();
        private final String bbieContextDefinition = randomPrint(50, 100).trim();
        private final String bbieValueConstraint = "Fixed Value";
        private final String bbieFixedValue = randomAlphanumeric(50, 100).trim();
        private final String bbieValueDomainRestriction = "Code";
        private final String bbieValueDomain = "oacl_SystemEnvironmentCode";

        Preconditions_TA_29_1_5d_BIEReusedParent(AppUserObject usera, String prevRelease) {
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                    "From UOM Package. UOM Package", prevRelease);
            this.topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");
        }
    }

    private class Preconditions_TA_29_1_5d_BIEReusedScenario {
        private final TopLevelASBIEPObject topLevelASBIEP;

        Preconditions_TA_29_1_5d_BIEReusedScenario(AppUserObject usera, String prevRelease) {
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                    "UOM Code Conversion Rate. UOM Code Conversion Rate", prevRelease);
            this.topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");
        }
    }

    private class Preconditions_TA_29_1_TOPBIEGETBOM {
        private final TopLevelASBIEPObject topLevelASBIEP;

        // ASBIE
        private final ArrayList<String> asbiePaths = new ArrayList<>();
        private final String asbieRemark = randomPrint(50, 100).trim();
        private final String asbieContextDefinition = randomPrint(50, 100).trim();

        // BBIE
        private final ArrayList<String> bbiePaths = new ArrayList<>();
        private final String bbieRemark = randomPrint(50, 100).trim();
        private final String bbieExample = randomPrint(50, 100).trim();
        private final String bbieContextDefinition = randomPrint(50, 100).trim();
        private final String bbieValueConstraint = "Fixed Value";
        private final String bbieFixedValue = randomAlphanumeric(50, 100).trim();
        private final String bbieValueDomainRestriction = "Code";
        private final String bbieValueDomain = "oacl_SystemEnvironmentCode";

        // BBIE_SC
        private final ArrayList<String> bbieScPaths = new ArrayList<>();
        private final String bbieScRemark = randomPrint(50, 100).trim();
        private final String bbieScExample = randomPrint(50, 100).trim();
        private final String bbieScContextDefinition = randomPrint(50, 100).trim();
        private final String bbieScValueConstraint = "Fixed Value";
        private final String bbieScFixedValue = randomAlphanumeric(50, 100).trim();
        private final String bbieScValueDomainRestriction = "Agency";
        private final String bbieScValueDomain = "clm63055D16B_AgencyIdentification";

        Preconditions_TA_29_1_TOPBIEGETBOM(AppUserObject usera, String prevRelease) {
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                    "Get BOM. Get BOM", prevRelease);
            this.topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");
        }
    }

    private class Preconditions_TA_29_1_BIEPrimitiveDate {
        private final TopLevelASBIEPObject topLevelASBIEP;

        // ASBIE
        private final ArrayList<String> asbiePaths = new ArrayList<>();
        private final String asbieRemark = randomPrint(50, 100).trim();
        private final String asbieContextDefinition = randomPrint(50, 100).trim();

        // BBIE
        private final ArrayList<String> bbiePaths = new ArrayList<>();
        private final String bbieRemark = randomPrint(50, 100).trim();
        private final String bbieExample = randomPrint(50, 100).trim();
        private final String bbieContextDefinition = randomPrint(50, 100).trim();
        private final String bbieValueConstraint = "Fixed Value";
        private final String bbieFixedValue = randomAlphanumeric(50, 100).trim();
        private final String bbieValueDomainRestriction = "Code";
        private final String bbieValueDomain = "oacl_SystemEnvironmentCode";

        // BBIE_SC
        private final ArrayList<String> bbieScPaths = new ArrayList<>();
        private final String bbieScRemark = randomPrint(50, 100).trim();
        private final String bbieScExample = randomPrint(50, 100).trim();
        private final String bbieScContextDefinition = randomPrint(50, 100).trim();
        private final String bbieScValueConstraint = "Fixed Value";
        private final String bbieScFixedValue = randomAlphanumeric(50, 100).trim();
        private final String bbieScValueDomainRestriction = "Agency";
        private final String bbieScValueDomain = "clm63055D16B_AgencyIdentification";

        Preconditions_TA_29_1_BIEPrimitiveDate(AppUserObject usera, String prevRelease) {
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                    "Start Separate Date Time. Separate Date Time", prevRelease);
            this.topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");
        }
    }

    private class Preconditions_TA_29_1_BIEBOMDoubleNested {
        private final TopLevelASBIEPObject topLevelASBIEP;

        // ASBIE
        private final ArrayList<String> asbiePaths = new ArrayList<>();
        private final String asbieRemark = randomPrint(50, 100).trim();
        private final String asbieContextDefinition = randomPrint(50, 100).trim();

        // BBIE
        private final ArrayList<String> bbiePaths = new ArrayList<>();
        private final String bbieRemark = randomPrint(50, 100).trim();
        private final String bbieExample = randomPrint(50, 100).trim();
        private final String bbieContextDefinition = randomPrint(50, 100).trim();
        private final String bbieValueConstraint = "Fixed Value";
        private final String bbieFixedValue = randomAlphanumeric(50, 100).trim();
        private final String bbieValueDomainRestriction = "Code";
        private final String bbieValueDomain = "oacl_SystemEnvironmentCode";

        // BBIE_SC
        private final ArrayList<String> bbieScPaths = new ArrayList<>();
        private final String bbieScRemark = randomPrint(50, 100).trim();
        private final String bbieScExample = randomPrint(50, 100).trim();
        private final String bbieScContextDefinition = randomPrint(50, 100).trim();
        private final String bbieScValueConstraint = "Fixed Value";
        private final String bbieScFixedValue = randomAlphanumeric(50, 100).trim();
        private final String bbieScValueDomainRestriction = "Agency";
        private final String bbieScValueDomain = "clm63055D16B_AgencyIdentification";

        Preconditions_TA_29_1_BIEBOMDoubleNested(AppUserObject developer, String prevRelease) {
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                    "BOM. BOM", prevRelease);
            this.topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, developer, "WIP");
        }
    }

    private class Preconditions_TA_29_1_JournalEntry {
        private final TopLevelASBIEPObject topLevelASBIEP;

        // ASBIE
        private final ArrayList<String> asbiePaths = new ArrayList<>();
        private final String asbieRemark = randomPrint(50, 100).trim();
        private final String asbieContextDefinition = randomPrint(50, 100).trim();

        // BBIE
        private final ArrayList<String> bbiePaths = new ArrayList<>();
        private final String bbieRemark = randomPrint(50, 100).trim();
        private final String bbieExample = randomPrint(50, 100).trim();
        private final String bbieContextDefinition = randomPrint(50, 100).trim();
        private final String bbieValueConstraint = "Fixed Value";
        private final String bbieFixedValue = randomAlphanumeric(50, 100).trim();
        private final String bbieValueDomainRestriction = "Code";
        private final String bbieValueDomain = "oacl_SystemEnvironmentCode";

        // BBIE_SC
        private final ArrayList<String> bbieScPaths = new ArrayList<>();
        private final String bbieScRemark = randomPrint(50, 100).trim();
        private final String bbieScExample = randomPrint(50, 100).trim();
        private final String bbieScContextDefinition = randomPrint(50, 100).trim();
        private final String bbieScValueConstraint = "Fixed Value";
        private final String bbieScFixedValue = randomAlphanumeric(50, 100).trim();
        private final String bbieScValueDomainRestriction = "Agency";
        private final String bbieScValueDomain = "clm63055D16B_AgencyIdentification";

        Preconditions_TA_29_1_JournalEntry(AppUserObject usera, String prevRelease) {
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
                    "Post Acknowledge Journal Entry. Post Acknowledge Journal Entry", prevRelease);
            this.topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Collections.singletonList(context), asccp, usera, "WIP");
        }
    }

    private class Preconditions_TA_29_1_BIECAGUplift {
        private final TopLevelASBIEPObject topLevelASBIEP;

        // ASBIE
        private final ArrayList<String> asbiePaths = new ArrayList<>();
        private final String asbieRemark = randomPrint(50, 100).trim();
        private final String asbieContextDefinition = randomPrint(50, 100).trim();

        // BBIE
        private final ArrayList<String> bbiePaths = new ArrayList<>();
        private final String bbieRemark = randomPrint(50, 100).trim();
        private final String bbieExample = randomPrint(50, 100).trim();
        private final String bbieContextDefinition = randomPrint(50, 100).trim();
        private final String bbieValueConstraint = "Fixed Value";
        private final String bbieFixedValue = randomAlphanumeric(50, 100).trim();
        private final String bbieValueDomainRestriction = "Code";
        private final String bbieValueDomain = "oacl_SystemEnvironmentCode";

        // BBIE_SC
        private final ArrayList<String> bbieScPaths = new ArrayList<>();
        private final String bbieScRemark = randomPrint(50, 100).trim();
        private final String bbieScExample = randomPrint(50, 100).trim();
        private final String bbieScContextDefinition = randomPrint(50, 100).trim();
        private final String bbieScValueConstraint = "Fixed Value";
        private final String bbieScFixedValue = randomAlphanumeric(50, 100).trim();
        private final String bbieScValueDomainRestriction = "Agency";
        private final String bbieScValueDomain = "clm63055D16B_AgencyIdentification";

        Preconditions_TA_29_1_BIECAGUplift(AppUserObject usera, String prevRelease) {
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByDENAndReleaseNum(
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
