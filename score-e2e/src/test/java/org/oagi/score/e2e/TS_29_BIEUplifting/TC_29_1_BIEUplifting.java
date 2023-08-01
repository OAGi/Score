package org.oagi.score.e2e.TS_29_BIEUplifting;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
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
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.http.DumpHttpExchangeFilter;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.math.BigInteger;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.SAME_THREAD)
public class TC_29_1_BIEUplifting extends BaseTest {
    private List<AppUserObject> randomAccounts = new ArrayList<>();
    String prev_release = "10.8.8";
    String curr_release = "10.9.2";
    AppUserObject usera, userb, developer;
    Map<String, TopLevelASBIEPObject> testingBIEs = new HashMap<>();
    Map<String, TopLevelASBIEPObject> upliftedBIEs = new HashMap<>();
    Map<String, CodeListObject> testingCodeLists = new HashMap<>();
    Map<String, CodeListObject> upliftedCodeLists = new HashMap<>();
    Map<String, String> BIEContexts = new HashMap<>();

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
    public void TAs() {
        initialization();
        test_TA_29_1_5a();
        precondition_TA_29_1_2();

    }

    public void initialization() {
        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);

        userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(userb);

        developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
    }

    public void precondition_TA_29_1_2() {
        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);

        userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(userb);

        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());

        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = createBIEForSelectBusinessContextsPage.next(Arrays.asList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE("Enterprise Unit. Enterprise Unit", prev_release);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!testingBIEs.containsKey("BIE1QA")) {
            testingBIEs.put("BIE1QA", topLevelASBIEP);
        } else {
            testingBIEs.put("BIE1QA", topLevelASBIEP);
        }

        if (!BIEContexts.containsKey("BIE1QA")) {
            BIEContexts.put("BIE1QA", context.getName());
        } else {
            BIEContexts.put("BIE1QA", context.getName());
        }

        topLevelASBIEPPanel.setBusinessTerm("aBusinessTerm");
        topLevelASBIEPPanel.setRemark("aRemark");
        topLevelASBIEPPanel.setStatus("aStatus");
        editBIEPage.hitUpdateButton();

        waitFor(Duration.ofMillis(3000));
        ACCExtensionViewEditPage accExtensionViewEditPage =
                editBIEPage.extendBIELocallyOnNode("/Enterprise Unit/Extension");
        SelectAssociationDialog selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Enterprise Unit User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Product Classification. Classification");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Enterprise Unit User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Incorporation Location. Location");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Enterprise Unit User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Code List. Code List");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Enterprise Unit User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Revised Item Status. Status");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Enterprise Unit User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Usage Description. Text");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Enterprise Unit User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Last Modification Date Time. Date Time");

        accExtensionViewEditPage.setNamespace(euNamespace);
        accExtensionViewEditPage.hitUpdateButton();
        accExtensionViewEditPage.moveToQA();
        accExtensionViewEditPage.moveToProduction();

        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(prev_release);
        TopLevelASBIEPObject BIE1QA = testingBIEs.get("BIE1QA");
        editBIEPage = viewEditBIEPage.openEditBIEPage(BIE1QA);
        WebElement bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Last Modification Date Time");
        waitFor(Duration.ofMillis(2000));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Identifier");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Name");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        WebElement asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Incorporation Location");
        waitFor(Duration.ofMillis(2000));
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        asbiePanel.toggleUsed();
        asbiePanel.setRemark("aRemark");
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Identifier Set/Scheme Version Identifier");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setRemark("aRemark");
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Incorporation Location/CAGEID");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setRemark("aRemark");
        bbiePanel.setExample("anExample");
        bbiePanel.setValueConstraint("Fixed Value");
        bbiePanel.setFixedValue("99");
        bbiePanel.setValueDomain("language");
        bbiePanel.setContextDefinition("defcon");
        editBIEPage.hitUpdateButton();

        editBIEPage.getNodeByPath("/Enterprise Unit");
        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Usage Description");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setRemark("aRemark");
        bbiePanel.setExample("anExample");
        bbiePanel.setValueConstraint("Fixed Value");
        bbiePanel.setFixedValue("99");
        bbiePanel.setValueDomain("normalized string");
        bbiePanel.setContextDefinition("defcon");
        editBIEPage.hitUpdateButton();

        editBIEPage.getNodeByPath("/Enterprise Unit");
        asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Incorporation Location/Physical Address");
        waitFor(Duration.ofMillis(3000));
        asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        asbiePanel.toggleUsed();
        asbiePanel.setRemark("aRemark");
        asbiePanel.setContextDefinition("defcon");
        editBIEPage.hitUpdateButton();

        viewEditBIEPage.openPage();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);

        asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Code List/Code List Value");
        waitFor(Duration.ofMillis(3000));
        asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        asbiePanel.toggleUsed();
        asbiePanel.setRemark("aRemark");
        asbiePanel.setContextDefinition("defcon");
        editBIEPage.hitUpdateButton();

        editBIEPage.getNodeByPath("/Enterprise Unit");
        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Identifier");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.setCardinalityMin(11);
        bbiePanel.setCardinalityMax(99);
        bbiePanel.setExample("anExample");
        bbiePanel.setRemark("aRemark");
        bbiePanel.setContextDefinition("defcon");
        bbiePanel.setValueDomain("token");
        editBIEPage.hitUpdateButton();

        editBIEPage.getNodeByPath("/Enterprise Unit");
        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Type Code");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setExample("anExample");
        bbiePanel.setRemark("aRemark");
        bbiePanel.setContextDefinition("defcon");
        bbiePanel.setValueDomain("any URI");
        editBIEPage.hitUpdateButton();


        editBIEPage.getNodeByPath("/Enterprise Unit");
        WebElement bbieSCNode = editBIEPage.getNodeByPath("/Enterprise Unit/Cost Center Identifier/Scheme Agency Identifier");
        waitFor(Duration.ofMillis(2000));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setRemark("aRemark");
        bbiescPanel.setExample("anExample");
        bbiescPanel.setValueConstraint("Fixed Value");
        bbiescPanel.setFixedValue("99");
        bbiescPanel.setValueDomainRestriction("Code");
        bbiescPanel.setValueDomain("clm6ConstraintTypeCode1_ConstraintTypeCode");
        bbiescPanel.setContextDefinition("defcon");
        editBIEPage.hitUpdateButton();

        editBIEPage.getNodeByPath("/Enterprise Unit");
        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Indicator");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setCardinalityMin(11);
        bbiePanel.setCardinalityMax(99);
        bbiePanel.setExample("anExample");
        bbiePanel.setRemark("aRemark");
        bbiePanel.setContextDefinition("defcon");
        editBIEPage.hitUpdateButton();

        editBIEPage.getNodeByPath("/Enterprise Unit");
        asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Revised Item Status");
        waitFor(Duration.ofMillis(2000));
        asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        asbiePanel.toggleUsed();
        asbiePanel.setRemark("aRemark");
        asbiePanel.setContextDefinition("defcon");
        asbiePanel.setCardinalityMin(11);
        asbiePanel.setCardinalityMax(99);
        editBIEPage.hitUpdateButton();

        editBIEPage.getNodeByPath("/Enterprise Unit");
        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Revised Item Status/Reason Code");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setRemark("aRemark");
        bbiePanel.setExample("anExample");
        bbiePanel.setContextDefinition("defcon");
        bbiePanel.setValueConstraint("Default Value");
        bbiePanel.setDefaultValue("99");
        bbiePanel.setValueDomain("any URI");
        editBIEPage.hitUpdateButton();

        editBIEPage.moveToQA();
        homePage.logout();

    }

    @Test
    public void test_TA_29_1_1() {

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(curr_release);
        assertThrows(TimeoutException.class, () -> upliftBIEPage.setTargetBranch(prev_release));
    }

    @Test
    public void test_TA_29_1_2_QA_BIE_Uplift_and_TA_29_1_4_and_TA_29_1_6a() {
        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        TopLevelASBIEPObject BIECAGUplift = testingBIEs.get("BIECAGUplift");
        upliftBIEPage.setPropertyTerm(BIECAGUplift.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//td//*[contains(text(),\"" + BIECAGUplift.getPropertyTerm() + "\")]//ancestor::tr[1]/td[1]/mat-checkbox/label/span[1]")).size());

        homePage.logout();
        homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        bieMenu = homePage.getBIEMenu();
        upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setState("QA");
        TopLevelASBIEPObject BIE1QA = testingBIEs.get("BIE1QA");
        upliftBIEPage.setPropertyTerm(BIE1QA.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        click(upliftBIEPage.getNextButton());
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        By NEXT_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Next\")]//ancestor::button[1]");
        click(elementToBeClickable(getDriver(), NEXT_BUTTON_LOCATOR));
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        By UPLIFT_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Uplift\")]//ancestor::button[1]");
        click(elementToBeClickable(getDriver(), UPLIFT_BUTTON_LOCATOR));
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!upliftedBIEs.containsKey("BIE1QA")) {
            upliftedBIEs.put("BIE1QA", topLevelASBIEP);
        } else {
            upliftedBIEs.put("BIE1QA", topLevelASBIEP);
        }

        bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEquals(developer.getLoginId(), getText(topLevelASBIEPPanel.getOwnerField()));
        String bizContext = BIEContexts.get("BIE1QA");
        assertEquals(bizContext, getText(topLevelASBIEPPanel.getBusinessContextInputField()));
        assertEquals("aBusinessTerm", getText(topLevelASBIEPPanel.getBusinessTermField()));
        assertEquals("aRemark", getText(topLevelASBIEPPanel.getRemarkField()));
        assertEquals("aStatus", getText(topLevelASBIEPPanel.getStatusField()));

        WebElement bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Identifier");
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertChecked(bbiePanel.getUsedCheckbox());

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Type Code");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertChecked(bbiePanel.getUsedCheckbox());
        assertEquals("0", getText(bbiePanel.getCardinalityMinField()));
        assertEquals("1", getText(bbiePanel.getCardinalityMaxField()));
        assertEquals("anExample", getText(bbiePanel.getExampleField()));
        assertEquals("aRemark", getText(bbiePanel.getRemarkField()));
        assertEquals("99", getText(bbiePanel.getFixedValueField()));
        assertEquals("any URI", getText(bbiePanel.getValueDomainField()));
        assertEquals("defcon", getText(bbiePanel.getContextDefinitionField()));

        WebElement asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Identifier Set");
        waitFor(Duration.ofMillis(2000));
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        assertChecked(asbiePanel.getUsedCheckbox());

        // Verify no BIEnode in tree  "/Enterprise Unit/Extension/Incorporation Location"
        // verify no BIEnode in tree  "/Enterprise Unit/Extension/Last Modification Date Time"
        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(curr_release);
        assertTrue(viewEditBIEPage.openEditBIEPage(topLevelASBIEP).isOpened());
    }

    public void precondtions_TA_29_1_2_Production_BIE_Uplift() {
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        //BIEuserbProduction previousRelease
        bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = createBIEForSelectBusinessContextsPage.next(Arrays.asList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE("Change Acknowledge Shipment Status. Change Acknowledge Shipment Status", prev_release);
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!testingBIEs.containsKey("BIEUserbProduction")) {
            testingBIEs.put("BIEUserbProduction", topLevelASBIEP);
        } else {
            testingBIEs.put("BIEUserbProduction", topLevelASBIEP);
        }

        if (!BIEContexts.containsKey("BIEUserbProduction")) {
            BIEContexts.put("BIEUserbProduction", context.getName());
        } else {
            BIEContexts.put("BIEUserbProduction", context.getName());
        }

        WebElement bbieNode = editBIEPage.getNodeByPath("/Change Acknowledge Shipment Status/System Environment Code");
        waitFor(Duration.ofMillis(2000));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setRemark("aRemark");
        bbiePanel.setExample("anExample");
        bbiePanel.setContextDefinition("defcon");
        bbiePanel.setValueConstraint("Fixed Value");
        bbiePanel.setFixedValue("99");
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain("oacl_SystemEnvironmentCode");
        editBIEPage.hitUpdateButton();

        WebElement asbieNode = editBIEPage.getNodeByPath("/Change Acknowledge Shipment Status/Application Area");
        waitFor(Duration.ofMillis(2000));
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        asbiePanel.setRemark("aRemark");
        asbiePanel.setContextDefinition("defcon");
        editBIEPage.hitUpdateButton();

        WebElement bbieSCNode = editBIEPage.getNodeByPath("/Change Acknowledge Shipment Status/Application Area/Scenario Identifier/Scheme Version Identifier");
        waitFor(Duration.ofMillis(2000));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setRemark("aRemark");
        bbiescPanel.setExample("anExample");
        bbiescPanel.setValueConstraint("Fixed Value");
        bbiescPanel.setFixedValue("99");
        bbiescPanel.setValueDomainRestriction("Agency");
        bbiescPanel.setValueDomain("clm63055D16B_AgencyIdentification");
        bbiescPanel.setContextDefinition("defcon");
        editBIEPage.hitUpdateButton();
        editBIEPage.moveToQA();
        editBIEPage.moveToProduction();
        homePage.logout();
    }

    @Test
    public void test_TA_29_1_2_Production_BIE_Uplift() {
        developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        TopLevelASBIEPObject BIEUserbProduction = null;
        if (testingBIEs.containsKey("BIEUserbProduction")){
            BIEUserbProduction = testingBIEs.get("BIEUserbProduction");
        } else{
            precondtions_TA_29_1_2_Production_BIE_Uplift();
            BIEUserbProduction = testingBIEs.get("BIEUserbProduction");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        // Uplift Production BIE
        upliftBIEPage.openPage();
        upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setState("Production");
        upliftBIEPage.setPropertyTerm(BIEUserbProduction.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        click(upliftBIEPage.getNextButton());
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        By NEXT_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Next\")]//ancestor::button[1]");
        click(elementToBeClickable(getDriver(), NEXT_BUTTON_LOCATOR));
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        By UPLIFT_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Uplift\")]//ancestor::button[1]");
        click(elementToBeClickable(getDriver(), UPLIFT_BUTTON_LOCATOR));
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!upliftedBIEs.containsKey("BIEUserbProduction")) {
            upliftedBIEs.put("BIEUserbProduction", topLevelASBIEP);
        } else {
            upliftedBIEs.put("BIEUserbProduction", topLevelASBIEP);
        }
        bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
        EditBIEPage.TopLevelASBIEPPanel topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEquals(developer.getLoginId(), getText(topLevelASBIEPPanel.getOwnerField()));
        WebElement bbieNode = editBIEPage.getNodeByPath("/Change Acknowledge Shipment Status/System Environment Code");
        waitFor(Duration.ofMillis(2000));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertChecked(bbiePanel.getUsedCheckbox());
        assertEquals("0", getText(bbiePanel.getCardinalityMinField()));
        assertEquals("1", getText(bbiePanel.getCardinalityMaxField()));
        assertEquals("anExample", getText(bbiePanel.getExampleField()));
        assertEquals("aRemark", getText(bbiePanel.getRemarkField()));
        assertEquals("99", getText(bbiePanel.getFixedValueField()));
        assertEquals("Code", getText(bbiePanel.getValueDomainRestrictionSelectField()));
        assertEquals("oacl_SystemEnvironmentCode(1)", getText(bbiePanel.getValueDomainField()));
        assertEquals("defcon", getText(bbiePanel.getContextDefinitionField()));

        WebElement asbieNode = editBIEPage.getNodeByPath("/Change Acknowledge Shipment Status/Application Area");
        waitFor(Duration.ofMillis(2000));
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        assertChecked(asbiePanel.getUsedCheckbox());
        assertNotChecked(asbiePanel.getNillableCheckbox());
        assertEquals("1", getText(asbiePanel.getCardinalityMinField()));
        assertEquals("1", getText(asbiePanel.getCardinalityMaxField()));
        assertEquals("aRemark", getText(asbiePanel.getRemarkField()));
        assertEquals("defcon", getText(asbiePanel.getContextDefinitionField()));

        WebElement bbieScNode = editBIEPage.getNodeByPath("/Change Acknowledge Shipment Status/Application Area/Scenario Identifier/Scheme Version Identifier");
        waitFor(Duration.ofMillis(2000));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbieScNode);
        assertChecked(bbiescPanel.getUsedCheckbox());
        assertEquals("0", getText(bbiescPanel.getCardinalityMinField()));
        assertEquals("1", getText(bbiescPanel.getCardinalityMaxField()));
        assertEquals("anExample", getText(bbiescPanel.getExampleField()));
        assertEquals("aRemark", getText(bbiescPanel.getRemarkField()));
        assertEquals("99", getText(bbiescPanel.getFixedValueField()));
        assertEquals("Agency", getText(bbiescPanel.getValueDomainRestrictionSelectField()));
        assertEquals("clm63055D16B_AgencyIdentification", getText(bbiescPanel.getValueDomainField()));
        assertEquals("defcon", getText(bbiescPanel.getContextDefinitionField()));
        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(curr_release);
        assertTrue(viewEditBIEPage.openEditBIEPage(topLevelASBIEP).isOpened());
    }

    @Test
    public void test_TA_29_1_3() {
        userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(userb);
        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = createBIEForSelectBusinessContextsPage.next(Arrays.asList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE("Batch Certificate Of Analysis. Batch Certificate Of Analysis", prev_release);
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!testingBIEs.containsKey("BIEUserbWIP")) {
            testingBIEs.put("BIEUserbWIP", topLevelASBIEP);
        } else {
            testingBIEs.put("BIEUserbWIP", topLevelASBIEP);
        }
        homePage.logout();
        homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        TopLevelASBIEPObject BIEUserbWIP = testingBIEs.get("BIEUserbWIP");
        upliftBIEPage.setPropertyTerm(BIEUserbWIP.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//td//*[contains(text(),\"" + BIEUserbWIP.getPropertyTerm() + "\")]//ancestor::tr[1]/td[1]/mat-checkbox/label/span[1]")).size());
    }

    @Test
    public void test_TA_29_1_5a() {
        developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        TopLevelASBIEPObject BIE1QA = null;
        if (testingBIEs.containsKey("BIE1QA)")){
            BIE1QA = testingBIEs.get("BIE1QA");
        } else{
            precondition_TA_29_1_2();
            BIE1QA = testingBIEs.get("BIE1QA");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setState("QA");
        upliftBIEPage.setPropertyTerm(BIE1QA.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.Next();
        for (String path :
                Arrays.asList("/Enterprise Unit", "/Enterprise Unit/Type Code", "/Enterprise Unit/Identifier",
                        "/Enterprise Unit/Identifier Set/Scheme Version Identifier",
                        "/Enterprise Unit/Identifier Set/Scheme Agency Identifier",
                        "/Enterprise Unit/Identifier Set/Identifier",
                        "/Enterprise Unit/Name",
                        "/Enterprise Unit/Extension")) {
            WebElement sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE(path);
            waitFor(Duration.ofMillis(2000));
            assertFalse(isElementPresent(By.xpath("//mat-card-content/div[2]/div[1]//cdk-virtual-scroll-viewport//ancestor::div[1]/mat-checkbox[1]")));
        }

        for (String path :
                Arrays.asList("/Enterprise Unit/Type Code", "/Enterprise Unit/Identifier",
                        "/Enterprise Unit/Identifier Set/Scheme Version Identifier",
                        "/Enterprise Unit/Identifier Set/Identifier",
                        "/Enterprise Unit/Name",
                        "/Enterprise Unit/Extension")) {
            WebElement sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE(path);
            waitFor(Duration.ofMillis(2000));
            WebElement targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE(path);
            waitFor(Duration.ofMillis(2000));
            assertChecked(getDriver().findElement(By.xpath("//mat-card-content/div[2]/div[2]//cdk-virtual-scroll-viewport//ancestor::div[1]/mat-checkbox[1]")));
            assertTrue(getDriver().findElement(By.xpath("//mat-card-content/div[2]/div[2]//cdk-virtual-scroll-viewport//ancestor::div[1]/mat-checkbox[1]//input[@disabled]")).isDisplayed());
        }
        escape(getDriver());
        upliftBIEVerificationPage.next();
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));

        By UPLIFT_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Uplift\")]//ancestor::button[1]");
        click(elementToBeClickable(getDriver(), UPLIFT_BUTTON_LOCATOR));
        waitFor(Duration.ofMillis(2500));
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!upliftedBIEs.containsKey("BIE1QA")) {
            upliftedBIEs.put("BIE1QA", topLevelASBIEP);
        } else {
            upliftedBIEs.put("BIE1QA", topLevelASBIEP);
        }
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
        assertTrue(editBIEPage.isOpened());
    }
    protected boolean isElementPresent(By by){
        try{
            getDriver().findElement(by);
            return true;
        }catch(NoSuchElementException e){
            return false;
        }
    }
    @Test
    public void test_TA_29_1_5b() {
        TopLevelASBIEPObject BIE1QA = null;
        if (testingBIEs.containsKey("BIE1QA)")){
            BIE1QA = testingBIEs.get("BIE1QA");
        } else{
            precondition_TA_29_1_2();
            BIE1QA = testingBIEs.get("BIE1QA");
        }
        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setState("QA");
        upliftBIEPage.setPropertyTerm(BIE1QA.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.Next();

        WebElement sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Enterprise Unit/Extension/Indicator");
        WebElement targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Enterprise Unit/GL Entity Identifier/Scheme Version Identifier");
        assertDisabled(getDriver().findElement(By.xpath("//mat-card-content/div[2]/div[2]//cdk-virtual-scroll-viewport//ancestor::div[1]/mat-checkbox[1]")));
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Enterprise Unit/General Ledger Element");
        assertDisabled(getDriver().findElement(By.xpath("//mat-card-content/div[2]/div[2]//cdk-virtual-scroll-viewport//ancestor::div[1]/mat-checkbox[1]")));
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("Enterprise Unit/Profit Center Identifier");
        assertDisabled(getDriver().findElement(By.xpath("//mat-card-content/div[2]/div[2]//cdk-virtual-scroll-viewport//ancestor::div[1]/mat-checkbox[1]")));
        homePage.logout();
    }

    @Test
    public void test_TA_29_1_5c_and_TA_29_1_7_and_TA_29_1_8() {
        TopLevelASBIEPObject BIE1QA = null;
        if (testingBIEs.containsKey("BIE1QA)")){
            BIE1QA = testingBIEs.get("BIE1QA");
        } else{
            precondition_TA_29_1_2();
            BIE1QA = testingBIEs.get("BIE1QA");
        }
        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setState("QA");
        upliftBIEPage.setPropertyTerm(BIE1QA.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.Next();
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
        waitFor(Duration.ofMillis(3000));
        upliftBIEVerificationPage.next();
        By UPLIFT_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Uplift\")]//ancestor::button[1]");

        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        click(elementToBeClickable(getDriver(), UPLIFT_BUTTON_LOCATOR));

        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));

        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!upliftedBIEs.containsKey("BIE1QA")) {
            upliftedBIEs.put("BIE1QA", topLevelASBIEP);
        } else {
            upliftedBIEs.put("BIE1QA", topLevelASBIEP);
        }
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
        WebElement bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Status/Reason Code");
        waitFor(Duration.ofMillis(2500));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEnabled(bbiePanel.getUsedCheckbox());
        assertChecked(bbiePanel.getUsedCheckbox());
        assertEquals("aRemark", getText(bbiePanel.getRemarkField()));
        assertEquals("anExample", getText(bbiePanel.getExampleField()));
        assertEquals("defcon", getText(bbiePanel.getContextDefinitionField()));
        assertEquals("99", getText(bbiePanel.getDefaultValueField()));

        WebElement asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Status");
        waitFor(Duration.ofMillis(2500));
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        assertEnabled(asbiePanel.getUsedCheckbox());
        assertChecked(asbiePanel.getUsedCheckbox());

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Profit Center Identifier");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEnabled(bbiePanel.getUsedCheckbox());
        assertChecked(bbiePanel.getUsedCheckbox());
        assertEquals("aRemark", getText(bbiePanel.getRemarkField()));
        assertEquals("anExample", getText(bbiePanel.getExampleField()));
        assertEquals("defcon", getText(bbiePanel.getContextDefinitionField()));
        assertEquals("99", getText(bbiePanel.getFixedValueField()));
        assertEquals("language", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Description");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEnabled(bbiePanel.getUsedCheckbox());
        assertChecked(bbiePanel.getUsedCheckbox());
        assertEquals("aRemark", getText(bbiePanel.getRemarkField()));
        assertEquals("anExample", getText(bbiePanel.getExampleField()));
        assertEquals("defcon", getText(bbiePanel.getContextDefinitionField()));
        assertEquals("99", getText(bbiePanel.getFixedValueField()));
        assertEquals("normalized string", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Classification/Codes");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEnabled(bbiePanel.getUsedCheckbox());
        assertChecked(bbiePanel.getUsedCheckbox());
        assertEquals("aRemark", getText(bbiePanel.getRemarkField()));
        assertEquals("defcon", getText(bbiePanel.getContextDefinitionField()));

        asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Classification/Code List Value/Identifier Set");
        waitFor(Duration.ofMillis(2500));
        asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        assertEnabled(asbiePanel.getUsedCheckbox());
        assertChecked(asbiePanel.getUsedCheckbox());

        WebElement bbiescNode = editBIEPage.getNodeByPath("/Enterprise Unit/Identifier/Scheme Agency Identifier");
        waitFor(Duration.ofMillis(2500));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        assertEnabled(bbiescPanel.getUsedCheckbox());
        assertChecked(bbiescPanel.getUsedCheckbox());
        assertEquals("aRemark", getText(bbiescPanel.getRemarkField()));
        assertEquals("anExample", getText(bbiescPanel.getExampleField()));
        assertEquals("defcon", getText(bbiescPanel.getContextDefinitionField()));
        assertEquals("99", getText(bbiescPanel.getFixedValueField()));

        asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Classification");
        waitFor(Duration.ofMillis(2500));
        asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        assertEnabled(asbiePanel.getUsedCheckbox());
        assertChecked(asbiePanel.getUsedCheckbox());

        //Test part where only the association information are transferred
        upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setPropertyTerm(BIE1QA.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        tr = upliftBIEPage.getTableRecordAtIndex(1);
        td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        upliftBIEVerificationPage = upliftBIEPage.Next();
        upliftBIEVerificationPage.goToNodeInSourceBIE("/Enterprise Unit/Extension/Revised Item Status");
        upliftBIEVerificationPage.goToNodeInTargetBIE("/Enterprise Unit/General Ledger Element");
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("General Ledger Element"));
        upliftBIEVerificationPage.next();
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        click(elementToBeClickable(getDriver(), UPLIFT_BUTTON_LOCATOR));
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        currentUrl = getDriver().getCurrentUrl();
        topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!upliftedBIEs.containsKey("BIE1QA_TA5D")) {
            upliftedBIEs.put("BIE1QA_TA5D", topLevelASBIEP);
        } else {
            upliftedBIEs.put("BIE1QA_TA5D", topLevelASBIEP);
        }
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
        asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/General Ledger Element");
        waitFor(Duration.ofMillis(2500));
        asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        assertEnabled(asbiePanel.getUsedCheckbox());
        assertChecked(asbiePanel.getUsedCheckbox());
        assertEquals("unbounded", asbiePanel.getCardinalityMaxField());

        bbiescNode = editBIEPage.getNodeByPath("/Enterprise Unit/General Ledger Element/Element/Sequence Number Number");
        waitFor(Duration.ofMillis(2500));
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
            waitFor(Duration.ofMillis(1000));
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
            }else {
                ppath = ele.substring(ele.indexOf(" //"));
            }
            WebElement recreatedElement = getDriver().findElement(By.xpath(ppath));
            Actions action = new Actions(getDriver());
            action.moveToElement(recreatedElement).perform();
            recreatedElement.click();
        }
    }

    public void preconditions_TA_29_1_5d() {
        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        //BIEuserbReusedChild previousRelease
        BusinessContextObject contextForUserb = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = createBIEForSelectBusinessContextsPage.next(Arrays.asList(contextForUserb));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE("Unit Packaging. Packaging", prev_release);
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!testingBIEs.containsKey("BIEUserbReusedChild")) {
            testingBIEs.put("BIEUserbReusedChild", topLevelASBIEP);
        } else {
            testingBIEs.put("BIEUserbReusedChild", topLevelASBIEP);
        }

        if (!BIEContexts.containsKey("BIEUserbReusedChild")) {
            BIEContexts.put("BIEUserbReusedChild", contextForUserb.getName());
        } else {
            BIEContexts.put("BIEUserbReusedChild", contextForUserb.getName());
        }

        WebElement bbieNode = editBIEPage.getNodeByPath("/Unit Packaging/Capacity Per Package Quantity");
        waitFor(Duration.ofMillis(2500));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setRemark("aRemark");
        bbiePanel.setExample("anExample");
        bbiePanel.setContextDefinition("defcon");
        bbiePanel.setValueConstraint("Fixed Value");
        bbiePanel.setFixedValue("99");
        editBIEPage.hitUpdateButton();

        WebElement asbieNode = editBIEPage.getNodeByPath("/Unit Packaging/Dimensions");
        waitFor(Duration.ofMillis(2500));
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        asbiePanel.toggleUsed();
        asbiePanel.setCardinalityMax(99);
        asbiePanel.setCardinalityMin(11);
        asbiePanel.setRemark("aRemark");
        asbiePanel.setContextDefinition("defcon");
        editBIEPage.hitUpdateButton();

        WebElement bbieSCNode = editBIEPage.getNodeByPath("/Unit Packaging/UPC Packaging Level Code/List Agency Identifier");
        waitFor(Duration.ofMillis(2500));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setRemark("aRemark");
        bbiescPanel.setExample("anExample");
        bbiescPanel.setValueConstraint("Fixed Value");
        bbiescPanel.setFixedValue("99");
        bbiescPanel.setValueDomain("token");
        bbiescPanel.setContextDefinition("defcon");
        editBIEPage.hitUpdateButton();
        editBIEPage.moveToQA();
        editBIEPage.moveToProduction();

        //BIEuserbReusedParent previousRelease

        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
        createBIEForSelectTopLevelConceptPage = createBIEForSelectBusinessContextsPage.next(Arrays.asList(contextForUserb));
        editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE("From UOM Package. UOM Package", prev_release);
        currentUrl = getDriver().getCurrentUrl();
        topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!testingBIEs.containsKey("BIEUserbReusedParent")) {
            testingBIEs.put("BIEUserbReusedParent", topLevelASBIEP);
        } else {
            testingBIEs.put("BIEUserbReusedParent", topLevelASBIEP);
        }

        if (!BIEContexts.containsKey("BIEUserbReusedParent")) {
            BIEContexts.put("BIEUserbReusedParent", contextForUserb.getName());
        } else {
            BIEContexts.put("BIEUserbReusedParent", contextForUserb.getName());
        }

        bbieNode = editBIEPage.getNodeByPath("/From UOM Package/UOM Code");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setRemark("aRemark");
        bbiePanel.setExample("anExample");
        bbiePanel.setContextDefinition("defcon");
        bbiePanel.setValueConstraint("Fixed Value");
        bbiePanel.setFixedValue("99");
        bbiePanel.setValueDomain("token");
        editBIEPage.hitUpdateButton();

        asbieNode = editBIEPage.getNodeByPath("/From UOM Package/Unit Packaging");
        waitFor(Duration.ofMillis(2500));
        asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/From UOM Package/Unit Packaging");
        TopLevelASBIEPObject reusedBIE = testingBIEs.get("BIEUserbReusedChild");
        selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE);
        editBIEPage.moveToQA();
        editBIEPage.moveToProduction();

        //BIEUserbReusedScenario previousRelease
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
        createBIEForSelectTopLevelConceptPage = createBIEForSelectBusinessContextsPage.next(Arrays.asList(contextForUserb));
        editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE("UOM Code Conversion Rate. UOM Code Conversion Rate", prev_release);
        currentUrl = getDriver().getCurrentUrl();
        topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!testingBIEs.containsKey("BIEUserbReusedScenario")) {
            testingBIEs.put("BIEUserbReusedScenario", topLevelASBIEP);
        } else {
            testingBIEs.put("BBIEUserbReusedScenario", topLevelASBIEP);
        }

        if (!BIEContexts.containsKey("BIEUserbReusedScenario")) {
            BIEContexts.put("BIEUserbReusedScenario", contextForUserb.getName());
        } else {
            BIEContexts.put("BIEUserbReusedScenario", contextForUserb.getName());
        }

        selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/UOM Code Conversion Rate/From UOM Package");
        reusedBIE = testingBIEs.get("BIEUserbReusedParent");
        selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE);
        editBIEPage.moveToQA();
        homePage.logout();

    }

    @Test
    public void test_TA_29_1_5d_and_TA_29_1_6ab() {
        developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(userb);

        TopLevelASBIEPObject BIEUserbReusedChild = null;
        if (testingBIEs.containsKey("BIEUserbReusedChild)")){
            BIEUserbReusedChild = testingBIEs.get("BIEUserbReusedChild");
        } else{
            preconditions_TA_29_1_5d();
            BIEUserbReusedChild = testingBIEs.get("BIEUserbReusedChild");
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setPropertyTerm(BIEUserbReusedChild.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.Next();
        upliftBIEVerificationPage.next();
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));

        By UPLIFT_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Uplift\")]//ancestor::button[1]");
        click(elementToBeClickable(getDriver(), UPLIFT_BUTTON_LOCATOR));
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!upliftedBIEs.containsKey("BIEUserbReusedChild")) {
            upliftedBIEs.put("BIEUserbReusedChild", topLevelASBIEP);
        } else {
            upliftedBIEs.put("BIEUserbReusedChild", topLevelASBIEP);
        }
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
        editBIEPage.moveToQA();
        editBIEPage.moveToProduction();

        //uplift BIEUserbReusedParent
        upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        TopLevelASBIEPObject BIEUserbReusedParent = testingBIEs.get("BIEUserbReusedParent");
        upliftBIEPage.setPropertyTerm(BIEUserbReusedParent.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        tr = upliftBIEPage.getTableRecordAtIndex(1);
        td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        upliftBIEVerificationPage = upliftBIEPage.Next();
        SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = upliftBIEVerificationPage.reuseBIEOnNode("/From UOM Package/Unit Packaging", "Unit Packaging");
        TopLevelASBIEPObject reusedBIE = testingBIEs.get("BIEUserbReusedChild");
        selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE);
        upliftBIEVerificationPage.next();
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        click(elementToBeClickable(getDriver(), UPLIFT_BUTTON_LOCATOR));
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        currentUrl = getDriver().getCurrentUrl();
        topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!upliftedBIEs.containsKey("BIEUserbReusedParent")) {
            upliftedBIEs.put("BIEUserbReusedParent", topLevelASBIEP);
        } else {
            upliftedBIEs.put("BIEUserbReusedParent", topLevelASBIEP);
        }
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
        editBIEPage.moveToQA();
        editBIEPage.moveToProduction();

        //Test Assertion Verification

        upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        TopLevelASBIEPObject BIEUserbReusedScenario = testingBIEs.get("BIEUserbReusedScenario");
        upliftBIEPage.setPropertyTerm(BIEUserbReusedScenario.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        tr = upliftBIEPage.getTableRecordAtIndex(1);
        td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        upliftBIEVerificationPage = upliftBIEPage.Next();
        selectProfileBIEToReuseDialog = upliftBIEVerificationPage.reuseBIEOnNode("/UOM Code Conversion Rate/From UOM Package", "From UOM Package");
        assertEquals(0, getDriver().findElements(By.xpath("//*[contains(text(),\"Unit Packaging\")]//ancestor::tr[1]/td[1]/mat-checkbox/label/span[1]")).size());
        reusedBIE = upliftedBIEs.get("BIEUserbReusedParent");
        selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE);

        upliftBIEVerificationPage.next();
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        click(elementToBeClickable(getDriver(), UPLIFT_BUTTON_LOCATOR));
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        currentUrl = getDriver().getCurrentUrl();
        topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!upliftedBIEs.containsKey("BIEUserbReusedScenario")) {
            upliftedBIEs.put("BIEUserbReusedScenario", topLevelASBIEP);
        } else {
            upliftedBIEs.put("BIEUserbReusedScenario", topLevelASBIEP);
        }
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);

        WebElement asbieNode = editBIEPage.getNodeByPath("/UOM Code Conversion Rate/From UOM Package/Unit Packaging/Dimensions");
        waitFor(Duration.ofMillis(2500));
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        assertEnabled(asbiePanel.getUsedCheckbox());
        assertChecked(asbiePanel.getUsedCheckbox());
        assertNotChecked(asbiePanel.getNillableCheckbox());
        assertDisabled(asbiePanel.getNillableCheckbox());
        assertDisabled(asbiePanel.getCardinalityMinField());
        assertDisabled(asbiePanel.getCardinalityMaxField());
        assertEquals("aRemark", getText(asbiePanel.getRemarkField()));
        assertEquals("11", getText(asbiePanel.getCardinalityMinField()));
        assertEquals("99", getText(asbiePanel.getCardinalityMaxField()));
        assertEquals("defcon", getText(asbiePanel.getContextDefinitionField()));

        WebElement bbieNode = editBIEPage.getNodeByPath("/UOM Code Conversion Rate/From UOM Package/Unit Packaging/Capacity Per Package Quantity");
        waitFor(Duration.ofMillis(2500));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEnabled(bbiePanel.getUsedCheckbox());
        assertChecked(bbiePanel.getUsedCheckbox());
        assertChecked(bbiePanel.getNillableCheckbox());
        assertEnabled(bbiePanel.getNillableCheckbox());
        assertEquals("0", getText(bbiePanel.getCardinalityMinField()));
        assertDisabled(bbiePanel.getCardinalityMinField());
        assertEquals("1", getText(bbiePanel.getCardinalityMaxField()));
        assertDisabled(bbiePanel.getCardinalityMaxField());
        assertEquals("aRemark", getText(bbiePanel.getRemarkField()));
        assertDisabled(bbiePanel.getRemarkField());
        assertEquals("anExample", getText(bbiePanel.getExampleField()));
        assertDisabled(bbiePanel.getExampleField());
        assertEquals("defcon", getText(bbiePanel.getContextDefinitionField()));
        assertEquals("99", getText(bbiePanel.getFixedValueField()));

        editBIEPage.getNodeByPath("/UOM Code Conversion Rate/From UOM Package");
        bbieNode = editBIEPage.getNodeByPath("/UOM Code Conversion Rate/From UOM Package/UOM Code");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEnabled(bbiePanel.getUsedCheckbox());
        assertChecked(bbiePanel.getUsedCheckbox());
        assertChecked(bbiePanel.getNillableCheckbox());
        assertEnabled(bbiePanel.getNillableCheckbox());
        assertEquals("0", getText(bbiePanel.getCardinalityMinField()));
        assertDisabled(bbiePanel.getCardinalityMinField());
        assertEquals("1", getText(bbiePanel.getCardinalityMaxField()));
        assertDisabled(bbiePanel.getCardinalityMaxField());
        assertEquals("aRemark", getText(bbiePanel.getRemarkField()));
        assertDisabled(bbiePanel.getRemarkField());
        assertEquals("anExample", getText(bbiePanel.getExampleField()));
        assertDisabled(bbiePanel.getExampleField());
        assertEquals("defcon", getText(bbiePanel.getContextDefinitionField()));
        assertEquals("99", getText(bbiePanel.getFixedValueField()));
        assertEquals("token", getText(bbiePanel.getValueDomainField()));

        asbieNode = editBIEPage.getNodeByPath("/UOM Code Conversion Rate/From UOM Package");
        waitFor(Duration.ofMillis(2500));
        asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        assertEnabled(asbiePanel.getUsedCheckbox());
        assertChecked(asbiePanel.getUsedCheckbox());
        assertNotChecked(asbiePanel.getNillableCheckbox());
        assertDisabled(asbiePanel.getNillableCheckbox());

        //unreuse FROM UOM Package
        viewEditBIEPage.openPage();
        TopLevelASBIEPObject topLevelASBIEPObject = upliftedBIEs.get("BIEUserbReusedScenario");
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
        editBIEPage.getNodeByPath("/UOM Code Conversion Rate/From UOM Package");
        editBIEPage.clickOnDropDownMenuByPath("/UOM Code Conversion Rate/From UOM Package");
        click(getDriver().findElement(By.xpath("//span[contains(text(),\"Remove Reused BIE\")]")));
        click(getDriver().findElement(By.xpath("//span[contains(text(),\"Remove\")]//ancestor::button[1]")));
        waitFor(Duration.ofMillis(2500));
        editBIEPage.getNodeByPath("/UOM Code Conversion Rate/From UOM Package");
        assertEquals(0, getDriver().findElements(By.xpath("//span[.=\"From UOM Package\"]//ancestor::div[1]/fa-icon")).size());
        editBIEPage.getNodeByPath("/UOM Code Conversion Rate/From UOM Package/Unit Packaging");
        assertEquals(0, getDriver().findElements(By.xpath("//span[.=\"Unit Packaging\"]//ancestor::div[1]/fa-icon")).size());
        homePage.logout();
    }

    public void preconditions_TA_29_1_9() {
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        //TOPBIEGETBOM previousRelease
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = createBIEForSelectBusinessContextsPage.next(Arrays.asList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE("Get BOM. Get BOM", prev_release);
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!testingBIEs.containsKey("TOPBIEGETBOM")) {
            testingBIEs.put("TOPBIEGETBOM", topLevelASBIEP);
        } else {
            testingBIEs.put("TOPBIEGETBOM", topLevelASBIEP);
        }

        if (!BIEContexts.containsKey("TOPBIEGETBOM")) {
            BIEContexts.put("TOPBIEGETBOM", context.getName());
        } else {
            BIEContexts.put("TOPBIEGETBOM", context.getName());
        }

        WebElement bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area /BOM/BOM Header/Document Date Time");
        waitFor(Duration.ofMillis(2500));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Alternate BOM Reference/Status/Effective Time Period/Start Time");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Alternate BOM Reference/Status/Effective Time Period/Start Time");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/System Environment Code");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("any URI");
        editBIEPage.hitUpdateButton();

        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Note/Entry Date Time Date Time");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Note/Entry Date Time Date Time");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("gregorian month");
        editBIEPage.hitUpdateButton();

        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Note/Author Text");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Note/Author Text");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("normalized string");
        editBIEPage.hitUpdateButton();


        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Alternate BOM Reference/Status/Effective Time Period/Inclusive Indicator");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Alternate BOM Reference/Status/Effective Time Period/Inclusive Indicator");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Batch Size Quantity");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Batch Size Quantity");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("integer");
        editBIEPage.hitUpdateButton();

        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Alternate BOM Reference/Effectivity/Effective Range/Range Count Number");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Alternate BOM Reference/Effectivity/Effective Range/Range Count Number");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("float");
        editBIEPage.hitUpdateButton();

        viewEditBIEPage.openPage();
        //BIEPrimitiveDate  previousRelease
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
        createBIEForSelectTopLevelConceptPage = createBIEForSelectBusinessContextsPage.next(Arrays.asList(context));
        editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE("Start Separate Date Time. Separate Date Time", prev_release);
        currentUrl = getDriver().getCurrentUrl();
        topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!testingBIEs.containsKey("BIEPrimitiveDate")) {
            testingBIEs.put("BIEPrimitiveDate", topLevelASBIEP);
        } else {
            testingBIEs.put("BIEPrimitiveDate", topLevelASBIEP);
        }

        if (!BIEContexts.containsKey("BIEPrimitiveDate")) {
            BIEContexts.put("BIEPrimitiveDate", context.getName());
        } else {
            BIEContexts.put("BIEPrimitiveDate", context.getName());
        }

        bbieNode = editBIEPage.getNodeByPath("/Start Separate Date Time/Date");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();
        homePage.logout();
    }
    @Test
    public void test_TA_29_1_9a() {
        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);

        TopLevelASBIEPObject TOPBIEGETBOM= null;
        if (testingBIEs.containsKey("TOPBIEGETBOM")){
            TOPBIEGETBOM = testingBIEs.get("TOPBIEGETBOM");
        } else{
            preconditions_TA_29_1_9();
            TOPBIEGETBOM = testingBIEs.get("TOPBIEGETBOM");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setPropertyTerm(TOPBIEGETBOM.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.Next();
        upliftBIEVerificationPage.next();
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        By UPLIFT_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Uplift\")]//ancestor::button[1]");
        click(elementToBeClickable(getDriver(), UPLIFT_BUTTON_LOCATOR));
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!upliftedBIEs.containsKey("TOPBIEGETBOM")) {
            upliftedBIEs.put("TOPBIEGETBOM", topLevelASBIEP);
        } else {
            upliftedBIEs.put("TOPBIEGETBOM", topLevelASBIEP);
        }

        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);

        WebElement bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area /BOM/BOM Header/Document Date Time");
        waitFor(Duration.ofMillis(2500));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("date time", getText(bbiePanel.getValueDomainField()));

        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Alternate BOM Reference/Status/Effective Time Period/Start Time");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Alternate BOM Reference/Status/Effective Time Period/Start Time");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("time", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Get BOM/System Environment Code");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("any URI", getText(bbiePanel.getValueDomainField()));

        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Note/Entry Date Time Date Time");
        WebElement bbiescNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Note/Entry Date Time Date Time");
        waitFor(Duration.ofMillis(2500));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        assertEquals("gregorian month", getText(bbiescPanel.getValueDomainField()));

        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Note/Author Text");
        bbiescNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Note/Author Text");
        waitFor(Duration.ofMillis(2500));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        assertEquals("normalized string", getText(bbiescPanel.getValueDomainField()));

        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Alternate BOM Reference/Status/Effective Time Period/Inclusive Indicator");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Alternate BOM Reference/Status/Effective Time Period/Inclusive Indicator");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("xbt boolean", getText(bbiePanel.getValueDomainField()));

        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Batch Size Quantity");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Batch Size Quantity");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("integer", getText(bbiePanel.getValueDomainField()));

        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Alternate BOM Reference/Effectivity/Effective Range/Range Count Number");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Alternate BOM Reference/Effectivity/Effective Range/Range Count Number");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("float", getText(bbiePanel.getValueDomainField()));

        //BIEPrimitiveDate
        upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        TopLevelASBIEPObject BIEPrimitiveDate = testingBIEs.get("BIEPrimitiveDate");
        upliftBIEPage.setPropertyTerm(BIEPrimitiveDate.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        tr = upliftBIEPage.getTableRecordAtIndex(1);
        td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        upliftBIEVerificationPage = upliftBIEPage.Next();
        upliftBIEVerificationPage.next();
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        click(elementToBeClickable(getDriver(), UPLIFT_BUTTON_LOCATOR));
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        currentUrl = getDriver().getCurrentUrl();
        topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!upliftedBIEs.containsKey("BIEPrimitiveDate")) {
            upliftedBIEs.put("BIEPrimitiveDate", topLevelASBIEP);
        } else {
            upliftedBIEs.put("BIEPrimitiveDate", topLevelASBIEP);
        }
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
        bbieNode = editBIEPage.getNodeByPath("/Start Separate Date Time/Date");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("date", getText(bbiePanel.getValueDomainField()));
        homePage.logout();
    }

    @Test
    public void test_TA_29_1_9b_and_TA_29_1_9c() {
        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);

        TopLevelASBIEPObject TOPBIEGETBOM= null;
        if (testingBIEs.containsKey("TOPBIEGETBOM")){
            TOPBIEGETBOM = testingBIEs.get("TOPBIEGETBOM");
        } else{
            preconditions_TA_29_1_9();
            TOPBIEGETBOM = testingBIEs.get("TOPBIEGETBOM");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
        BIEMenu bieMenu = homePage.getBIEMenu();
        //BIECAGUplift prev_release
        bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(TOPBIEGETBOM);
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
        selectCCPropertyPage.selectAssociation("Request Language Code. Code");
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
        editBIEPage = viewEditBIEPage.openEditBIEPage(TOPBIEGETBOM);
        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Effectivity Relation Code");
        WebElement bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Effectivity Relation Code");
        waitFor(Duration.ofMillis(1500));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("any URI");
        editBIEPage.hitUpdateButton();
        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Validation Indicator");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Validation Indicator");
        waitFor(Duration.ofMillis(1500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();
        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Method Consequence Text");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Method Consequence Text");
        waitFor(Duration.ofMillis(1500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("any URI");
        editBIEPage.hitUpdateButton();

        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Record Set Reference Identifier");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Record Set Reference Identifier");
        waitFor(Duration.ofMillis(1500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("language");
        editBIEPage.hitUpdateButton();
        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Record Set Total Number");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Record Set Total Number");
        waitFor(Duration.ofMillis(1500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("positive integer");
        editBIEPage.hitUpdateButton();
        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Latest Start Date Time");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Latest Start Date Time");
        waitFor(Duration.ofMillis(1500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("gregorian day");
        editBIEPage.hitUpdateButton();
        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Request Language Code");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Request Language Code");
        waitFor(Duration.ofMillis(1500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("token");
        editBIEPage.hitUpdateButton();
        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Request Language Code/List Agency Identifier");
        WebElement bbiescNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Request Language Code/List Agency Identifier");
        waitFor(Duration.ofMillis(1500));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        bbiescPanel.toggleUsed();
        editBIEPage.hitUpdateButton();
        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Request Language Code/List Version Identifier");
        bbiescNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Request Language Code/List Version Identifier");
        waitFor(Duration.ofMillis(1500));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setValueDomain("token");
        editBIEPage.hitUpdateButton();
        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Transport Temperature");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Transport Temperature");
        waitFor(Duration.ofMillis(1500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("float");
        editBIEPage.hitUpdateButton();
        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Transport Temperature/Unit Code");
        bbiescNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Transport Temperature/Unit Code");
        waitFor(Duration.ofMillis(1500));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setValueDomain("string");
        editBIEPage.hitUpdateButton();
        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Correlation Identifier");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Correlation Identifier");
        waitFor(Duration.ofMillis(1500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomain("string");
        editBIEPage.hitUpdateButton();
        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Correlation Identifier/Scheme Agency Identifier");
        bbiescNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Correlation Identifier/Scheme Agency Identifier");
        waitFor(Duration.ofMillis(1500));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setValueDomain("normalized string");
        editBIEPage.hitUpdateButton();
        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Record Set Save Indicator");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Option/Extension/Record Set Save Indicator");
        waitFor(Duration.ofMillis(1500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        //Uplift TOPBIEGETBOM
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setPropertyTerm(TOPBIEGETBOM.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.Next();
        //match BBIE or BBIEsc nodes
        WebElement sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Get BOM/Data Area/BOM/BOM Option/Extension/Effectivity Relation Code");
        clickOn(sourceNode);
        WebElement targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Get BOM/Data Area/BOM/BOM Header/Revision Identifier");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Revision Identifier"));
        escape(getDriver());
        //
        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Get BOM/Data Area/BOM/BOM Option/Extension/Validation Indicator");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Get BOM/Data Area/BOM/BOM Header/Document Reference/Status/Effective Time Period/Inclusive Indicator");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Inclusive Indicator"));
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

        //BBIE to BBIE
        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Get BOM/Data Area/BOM/BOM Option/Extension/Transport Temperature");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Get BOM/Data Area/BOM/BOM Header/Alternate BOM Reference/Effectivity/Preference/Priority Sequence Value");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Priority Sequence Value"));
        escape(getDriver());

        //BBIE_SC to BBIE_SC
        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Get BOM/Data Area/BOM/BOM Option/Extension/Transport Temperature/Unit Code");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Get BOM/Application Area/Sender/Logical Identifier/Scheme Version Identifier");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Scheme Version Identifier"));
        escape(getDriver());

        upliftBIEVerificationPage.next();
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        By UPLIFT_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Uplift\")]//ancestor::button[1]");
        click(elementToBeClickable(getDriver(), UPLIFT_BUTTON_LOCATOR));
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!upliftedBIEs.containsKey("TOPBIEGETBOM")) {
            upliftedBIEs.put("TOPBIEGETBOM", topLevelASBIEP);
        } else {
            upliftedBIEs.put("TOPBIEGETBOM", topLevelASBIEP);
        }

        //Verification after uplifting
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Revision Identifier");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Revision Identifier");
        waitFor(Duration.ofMillis(1500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("any URI", getText(bbiePanel.getValueDomainField()));
        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Document Reference/Status/Effective Time Period/Inclusive Indicator");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Document Reference/Status/Effective Time Period/Inclusive Indicator");
        waitFor(Duration.ofMillis(1500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("xbt boolean", getText(bbiePanel.getValueDomainField()));
        editBIEPage.goToNodeByPath("/Get BOM/Application Area/Intermediary/Component Identifier");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Application Area/Intermediary/Component Identifier");
        waitFor(Duration.ofMillis(1500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("any URI", getText(bbiePanel.getValueDomainField()));
        editBIEPage.goToNodeByPath("/Get BOM/Application Area/Sender/Authorization Identifier");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Application Area/Sender/Authorization Identifier");
        waitFor(Duration.ofMillis(1500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("language", getText(bbiePanel.getValueDomainField()));
        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Attachment/File Size Quantity");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Attachment/File Size Quantity");
        waitFor(Duration.ofMillis(1500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("positive integer", getText(bbiePanel.getValueDomainField()));
        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Attachment/Document Date Time");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Attachment/Document Date Time");
        waitFor(Duration.ofMillis(1500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("gregorian day", getText(bbiePanel.getValueDomainField()));
        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Attachment/File Type Code/List Agency Identifier");
        bbiescNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Attachment/File Type Code/List Agency Identifier");
        waitFor(Duration.ofMillis(1500));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        assertEquals("normalized string", getText(bbiescPanel.getValueDomainField()));
        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Attachment/File Type Code");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Attachment/File Type Code");
        waitFor(Duration.ofMillis(1500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("token", getText(bbiePanel.getValueDomainField()));
        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Attachment/File Type Code/List Version Identifier");
        bbiescNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Attachment/File Type Code/List Version Identifier");
        waitFor(Duration.ofMillis(1500));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        assertEquals("token", getText(bbiescPanel.getValueDomainField()));
        editBIEPage.goToNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Alternate BOM Reference/Effectivity/Preference/Priority Sequence Value");
        bbieNode = editBIEPage.getNodeByPath("/Get BOM/Data Area/BOM/BOM Header/Alternate BOM Reference/Effectivity/Preference/Priority Sequence Value");
        waitFor(Duration.ofMillis(1500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("float", getText(bbiePanel.getValueDomainField()));
        editBIEPage.goToNodeByPath("/Get BOM/Application Area/Sender/Logical Identifier/Scheme Version Identifier");
        bbiescNode = editBIEPage.getNodeByPath("/Get BOM/Application Area/Sender/Logical Identifier/Scheme Version Identifier");
        waitFor(Duration.ofMillis(1500));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        assertEquals("string", getText(bbiescPanel.getValueDomainField()));
        homePage.logout();
    }

    @Test
    public void test_TA_29_1_10a() {
        developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(developer);
        //BIEBOMDoubleNested previousRelease
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = createBIEForSelectBusinessContextsPage.next(Arrays.asList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE("BOM. BOM", prev_release);
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!testingBIEs.containsKey("BIEBOMDoubleNested")) {
            testingBIEs.put("BIEBOMDoubleNested", topLevelASBIEP);
        } else {
            testingBIEs.put("BIEBOMDoubleNested", topLevelASBIEP);
        }

        if (!BIEContexts.containsKey("BIEBOMDoubleNested")) {
            BIEContexts.put("BIEBOMDoubleNested", context.getName());
        } else {
            BIEContexts.put("BIEBOMDoubleNested", context.getName());
        }

        viewEditBIEPage.openPage();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
        WebElement bbieNode = editBIEPage.getNodeByPath("/BOM/BOM Option/Default Indicator");
        waitFor(Duration.ofMillis(2500));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain("clm6TimeFormatCode1_TimeFormatCode");
        editBIEPage.hitUpdateButton();

        editBIEPage.goToNodeByPath("/BOM/BOM Option/Description/Language Code");
        WebElement bbieSCNode = editBIEPage.getNodeByPath("/BOM/BOM Option/Description/Language Code");
        waitFor(Duration.ofMillis(1500));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setValueDomainRestriction("Code");
        bbiescPanel.setValueDomain("clm6TimeFormatCode1_TimeFormatCode");
        editBIEPage.hitUpdateButton();

        editBIEPage.goToNodeByPath("//BOM/BOM Option/Identifier/Scheme Agency Identifier");
        bbieSCNode = editBIEPage.getNodeByPath("//BOM/BOM Option/Identifier/Scheme Agency Identifier");
        waitFor(Duration.ofMillis(1500));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setValueDomainRestriction("Agency");
        bbiescPanel.setValueDomain("clm63055D16B_AgencyIdentification");
        editBIEPage.hitUpdateButton();

        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        TopLevelASBIEPObject BIEBOMDoubleNested = testingBIEs.get("BIEBOMDoubleNested");
        upliftBIEPage.setPropertyTerm(BIEBOMDoubleNested.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.Next();
        upliftBIEVerificationPage.next();
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        By UPLIFT_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Uplift\")]//ancestor::button[1]");
        click(elementToBeClickable(getDriver(), UPLIFT_BUTTON_LOCATOR));
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        currentUrl = getDriver().getCurrentUrl();
        topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!upliftedBIEs.containsKey("BIEBOMDoubleNested")) {
            upliftedBIEs.put("BIEBOMDoubleNested", topLevelASBIEP);
        } else {
            upliftedBIEs.put("BIEBOMDoubleNested", topLevelASBIEP);
        }
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);

        editBIEPage.goToNodeByPath("/BOM/BOM Option/Default Indicator");
        bbieNode = editBIEPage.getNodeByPath("/BOM/BOM Option/Default Indicator");
        waitFor(Duration.ofMillis(1500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith("clm6TimeFormatCode1_TimeFormatCode"));

        editBIEPage.goToNodeByPath("/BOM/BOM Option/Description/Language Code");
        bbieSCNode = editBIEPage.getNodeByPath("/BOM/BOM Option/Description/Language Code");
        waitFor(Duration.ofMillis(1500));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertTrue(getText(bbiescPanel.getValueDomainField()).startsWith("clm6TimeFormatCode1_TimeFormatCode"));
        editBIEPage.goToNodeByPath("//BOM/BOM Option/Identifier/Scheme Agency Identifier");
        bbieSCNode = editBIEPage.getNodeByPath("//BOM/BOM Option/Identifier/Scheme Agency Identifier");
        waitFor(Duration.ofMillis(1500));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertTrue(getText(bbiescPanel.getValueDomainField()).startsWith("clm63055D16B_AgencyIdentification"));
        homePage.logout();
    }

    @Test
    public void test_TA_29_1_10b() {
        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        BIEMenu bieMenu = homePage.getBIEMenu();
        //JournalEntry prev_release
        bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = createBIEForSelectBusinessContextsPage.next(Arrays.asList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE("Post Acknowledge Journal Entry. Post Acknowledge Journal Entry", prev_release);
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!testingBIEs.containsKey("JournalEntry")) {
            testingBIEs.put("JournalEntry", topLevelASBIEP);
        } else {
            testingBIEs.put("JournalEntry", topLevelASBIEP);
        }

        if (!BIEContexts.containsKey("JournalEntry")) {
            BIEContexts.put("JournalEntry", context.getName());
        } else {
            BIEContexts.put("JournalEntry", context.getName());
        }
        viewEditBIEPage.openPage();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
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
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
        editBIEPage.goToNodeByPath("/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Extension/Usage Description");
        WebElement bbieNode = editBIEPage.getNodeByPath("/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Extension/Usage Description");
        waitFor(Duration.ofMillis(1500));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain("clm6TimeFormatCode1_TimeFormatCode");
        editBIEPage.hitUpdateButton();
        editBIEPage.goToNodeByPath("/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Extension/Control Objective Category");
        bbieNode = editBIEPage.getNodeByPath("/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Extension/Control Objective Category");
        waitFor(Duration.ofMillis(1500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain("oacl_RiskCode");
        editBIEPage.hitUpdateButton();
        editBIEPage.goToNodeByPath("/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Extension/Control Objective Category/List Version Identifier");
        WebElement bbiescNode = editBIEPage.getNodeByPath("/Post Acknowledge Journal Entry/Data Area/Post Acknowledge/Response Criteria/Change Status/Extension/Control Objective Category/List Version Identifier");
        waitFor(Duration.ofMillis(1500));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setValueDomainRestriction("Code");
        bbiescPanel.setValueDomain("clm6ConditionTypeCode1_ConditionTypeCode");
        editBIEPage.hitUpdateButton();

        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        TopLevelASBIEPObject JournalEntry = testingBIEs.get("JournalEntry");
        upliftBIEPage.setPropertyTerm(JournalEntry.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.Next();

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

        upliftBIEVerificationPage.next();
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        By UPLIFT_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Uplift\")]//ancestor::button[1]");
        click(elementToBeClickable(getDriver(), UPLIFT_BUTTON_LOCATOR));
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        currentUrl = getDriver().getCurrentUrl();
        topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!upliftedBIEs.containsKey("JournalEntry")) {
            upliftedBIEs.put("JournalEntry", topLevelASBIEP);
        } else {
            upliftedBIEs.put("JournalEntry", topLevelASBIEP);
        }
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
        editBIEPage.goToNodeByPath("/Post Acknowledge Journal Entry/Data Area/Journal Entry/Journal Entry Line/Debit Credit Code");
        bbieNode = editBIEPage.getNodeByPath("/Post Acknowledge Journal Entry/Data Area/Journal Entry/Journal Entry Line/Debit Credit Code");
        waitFor(Duration.ofMillis(1500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertChecked(bbiePanel.getUsedCheckbox());
        assertEnabled(bbiePanel.getUsedCheckbox());
        editBIEPage.goToNodeByPath("/Post Acknowledge Journal Entry/Data Area/Journal Entry/Journal Entry Line/Tax Base Functional Amount");
        bbieNode = editBIEPage.getNodeByPath("/Post Acknowledge Journal Entry/Data Area/Journal Entry/Journal Entry Line/Tax Base Functional Amount");
        waitFor(Duration.ofMillis(1500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith("oacl_RiskCode"));
        editBIEPage.goToNodeByPath("/Post Acknowledge Journal Entry/Application Area/Sender/Logical Identifier/Scheme Identifier");
        WebElement bbieSCNode = editBIEPage.getNodeByPath("/Post Acknowledge Journal Entry/Application Area/Sender/Logical Identifier/Scheme Identifier");
        waitFor(Duration.ofMillis(1500));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertTrue(getText(bbiescPanel.getValueDomainField()).startsWith("clm6ConditionTypeCode1_ConditionTypeCode"));
        homePage.logout();
    }
    @Test
    public void test_TA_29_1_11a_and_TA_29_11b() {
        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
        List<String> euCLStates = new ArrayList<>();
        euCLStates.add("WIP");
        euCLStates.add("QA");
        euCLStates.add("Production");
        euCLStates.add("Deleted");
        ReleaseObject prev_releaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(prev_release);
        RandomCodeListWithStateContainer euCodeListWithStateContainer = new RandomCodeListWithStateContainer(usera, prev_releaseObject, euNamespace, euCLStates);
        CodeListObject CLaccessUseraDeprecated = getAPIFactory().getCodeListAPI().createRandomCodeList(usera, euNamespace, prev_releaseObject, "Production");
        CodeListValueObject codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(CLaccessUseraDeprecated, usera);
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(CLaccessUseraDeprecated.getName(), prev_release);
        editCodeListPage.hitAmendButton();
        click(editCodeListPage.getDeprecatedSelectField());
        editCodeListPage.setDefinition("Check the Deprecated Checkbox");
        editCodeListPage.hitUpdateButton();
        editCodeListPage.moveToQA();
        editCodeListPage.moveToProduction();

        viewEditCodeListPage.openPage();
        editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch("oacl_MatchDocumentCode", prev_release);
        editCodeListPage.hitDeriveCodeListBasedOnThisButton();
        editCodeListPage.setName("CLuserderived_BIEUp");
        editCodeListPage.setNamespace(euNamespace);
        editCodeListPage.setDefinition("aDefinition");
        editCodeListPage.hitUpdateButton();

        //BIECAGUplift prev_release
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = createBIEForSelectBusinessContextsPage.next(Arrays.asList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE("Child Item Reference. Child Item Reference", prev_release);
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!testingBIEs.containsKey("BIECAGUplift")) {
            testingBIEs.put("BIECAGUplift", topLevelASBIEP);
        } else {
            testingBIEs.put("BIECAGUplift", topLevelASBIEP);
        }

        if (!BIEContexts.containsKey("BIECAGUplift")) {
            BIEContexts.put("BIECAGUplift", context.getName());
        } else {
            BIEContexts.put("BIECAGUplift", context.getName());
        }
        ACCExtensionViewEditPage accExtensionViewEditPage =
                editBIEPage.extendBIELocallyOnNode("/Child Item Reference/Extension");
        SelectAssociationDialog selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Effectivity Relation Code. Code");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Validation Indicator. Indicator");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Method Consequence Text. Text");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Record Set Reference Identifier. Identifier");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Record Set Total Number. Number");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Latest Start Date Time. Date Time");
        selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
        selectCCPropertyPage.selectAssociation("Request Language Code. Code");
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

        viewEditBIEPage.openPage();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);

        WebElement bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Effectivity Relation Code");
        waitFor(Duration.ofMillis(2500));
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        String CLaccessendUserwip = euCodeListWithStateContainer.stateCodeLists.get("WIP").toString();
        bbiePanel.setValueDomain(CLaccessendUserwip);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Validation Indicator");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        String CLaccessendUserqa = euCodeListWithStateContainer.stateCodeLists.get("QA").toString();
        bbiePanel.setValueDomain(CLaccessendUserqa);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Method Consequence Text");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        String CLaccessendUserproduction = euCodeListWithStateContainer.stateCodeLists.get("Production").toString();
        bbiePanel.setValueDomain(CLaccessendUserproduction);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Record Set Total Number");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        String CLaccessendUserdeleted = euCodeListWithStateContainer.stateCodeLists.get("Deleted").toString();
        bbiePanel.setValueDomain(CLaccessendUserdeleted);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Latest Start Date Time");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain(CLaccessUseraDeprecated.getName());
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Transport Temperature");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain("CLuserderived_BIEUp");
        editBIEPage.hitUpdateButton();

        WebElement bbiescNode = editBIEPage.getNodeByPath("/Child Item Reference/Extension/Reason/Sequence Number Number");
        waitFor(Duration.ofMillis(2500));
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setValueDomainRestriction("Code");
        bbiescPanel.setValueDomain(CLaccessendUserqa);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Technical Name");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain(CLaccessendUserwip);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Placard Endorsement");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain(CLaccessendUserqa);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Placard Notation");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain(CLaccessendUserproduction);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Marine Pollution Level Code");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain("CLuserderived_BIEUp");
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Toxicity Zone Code");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain(CLaccessendUserdeleted);
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Flashpoint Temperature");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain(CLaccessUseraDeprecated.getName());
        editBIEPage.hitUpdateButton();

        bbiescNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Primary Entry Route/Type Code");
        waitFor(Duration.ofMillis(2500));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setValueDomainRestriction("Code");
        bbiescPanel.setValueDomain(CLaccessendUserwip);
        editBIEPage.hitUpdateButton();

        bbiescNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/MFAGID/Scheme Agency Identifier");
        waitFor(Duration.ofMillis(2500));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setValueDomainRestriction("Code");
        bbiescPanel.setValueDomain(CLaccessendUserqa);
        editBIEPage.hitUpdateButton();

        bbiescNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/MFAGID/Scheme Version Identifier");
        waitFor(Duration.ofMillis(2500));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setValueDomainRestriction("Code");
        bbiescPanel.setValueDomain("CLuserderived_BIEUp");
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Export Control/Encryption Status Code");
        waitFor(Duration.ofMillis(2500));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain("clm6TimeFormatCode1_TimeFormatCode");
        editBIEPage.hitUpdateButton();

        bbiescNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Export Control/Certification Identifier/Scheme Agency Identifier");
        waitFor(Duration.ofMillis(2500));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbiescNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setValueDomainRestriction("Code");
        bbiescPanel.setValueDomain("clm6TimeFormatCode1_TimeFormatCode");
        editBIEPage.hitUpdateButton();

        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        TopLevelASBIEPObject BIECAGUplift = testingBIEs.get("BIECAUplift");
        upliftBIEPage.setPropertyTerm(BIECAGUplift.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.Next();

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

        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Child Item Reference/Extension/Reason/Sequence Number Number");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("//Child Item Reference/Child Line/Document Reference/Revision Identifier/Scheme Version Identifier");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Scheme Version Identifier"));

        upliftBIEVerificationPage.next();
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        By UPLIFT_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Uplift\")]//ancestor::button[1]");
        click(elementToBeClickable(getDriver(), UPLIFT_BUTTON_LOCATOR));
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        currentUrl = getDriver().getCurrentUrl();
        topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!upliftedBIEs.containsKey("BIECAGUplift")) {
            upliftedBIEs.put("BIECAGUplift", topLevelASBIEP);
        } else {
            upliftedBIEs.put("BIECAGUplift", topLevelASBIEP);
        }
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Technical Name");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("string", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Placard Endorsement");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("string", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Placard Notation");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("string", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Marine Pollution Level Code");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("normalized string", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Toxicity Zone Code");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("normalized string", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Flashpoint Temperature");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("decimal", getText(bbiePanel.getValueDomainField()));

        WebElement bbieSCNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Primary Entry Route/Type Code");
        waitFor(Duration.ofMillis(2000));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertEquals("token", getText(bbiescPanel.getValueDomainField()));

        bbieSCNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/MFAGID/Scheme Version Identifier");
        waitFor(Duration.ofMillis(2000));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertEquals("token", getText(bbiescPanel.getValueDomainField()));

        bbieSCNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/MFAGID/Scheme Agency Identifier");
        waitFor(Duration.ofMillis(2000));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertEquals("token", getText(bbiescPanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Export Control/Encryption Status Code");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("clm6TimeFormatCode1_TimeFormatCode", getText(bbiePanel.getValueDomainField()));

        bbieSCNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Export Control/Certification Identifier/Scheme Agency Identifier");
        waitFor(Duration.ofMillis(2000));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertEquals("clm6TimeFormatCode1_TimeFormatCode", getText(bbiescPanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Manufacturing Party/CCRID");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("normalized string", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Manufacturing Party/Account Identifier");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("normalized string", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Manufacturing Party/CAGEID");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("normalized string", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Manufacturing Party/DODAACID");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("normalized string", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Manufacturing Party/SCACID");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("normalized string", getText(bbiePanel.getValueDomainField()));

        //Uplift codeList page

        for (String codeListName : Arrays.asList(CLaccessendUserwip, CLaccessendUserqa, CLaccessendUserproduction, CLaccessendUserdeleted,
                CLaccessUseraDeprecated.getName())) {
            UpliftCodeListPage upliftCodeListPage = bieMenu.openUpliftCodeListSubMenu();
            upliftCodeListPage.setSourceRelease(prev_release);
            upliftCodeListPage.setTargetRelease(curr_release);
            upliftCodeListPage.setCodeList(codeListName);
            upliftCodeListPage.hitSearchButton();
            tr = upliftCodeListPage.getTableRecordAtIndex(1);
            td = upliftCodeListPage.getColumnByName(tr, "select");
            click(td);
            click(upliftCodeListPage.getUpliftButton(true));

            currentUrl = getDriver().getCurrentUrl();
            BigInteger codeListId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
            CodeListObject codeListObject = getAPIFactory().getCodeListAPI().getCodeListByManifestId(codeListId);

            if (!upliftedCodeLists.containsKey(codeListName)) {
                upliftedCodeLists.put(codeListName, codeListObject);
            } else {
                upliftedCodeLists.put(codeListName, codeListObject);
            }
        }

        UpliftCodeListPage upliftCodeListPage = bieMenu.openUpliftCodeListSubMenu();
        upliftCodeListPage.setSourceRelease(prev_release);
        upliftCodeListPage.setTargetRelease(curr_release);
        upliftCodeListPage.setCodeList("CLuserderived_BIEUp");
        upliftCodeListPage.hitSearchButton();
        tr = upliftCodeListPage.getTableRecordAtIndex(1);
        td = upliftCodeListPage.getColumnByName(tr, "select");
        click(td);
        click(upliftCodeListPage.getUpliftButton(true));

        currentUrl = getDriver().getCurrentUrl();
        BigInteger codeListId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        CodeListObject codeListObject = getAPIFactory().getCodeListAPI().getCodeListByManifestId(codeListId);

        if (!upliftedCodeLists.containsKey("CLuserderived_BIEUp")) {
            upliftedCodeLists.put("CLuserderived_BIEUp", codeListObject);
        } else {
            upliftedCodeLists.put("CLuserderived_BIEUp", codeListObject);
        }

        //Uplift BIECAGUplift after CodeList uplift

        upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        BIECAGUplift = testingBIEs.get("BIECAUplift");
        upliftBIEPage.setPropertyTerm(BIECAGUplift.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        tr = upliftBIEPage.getTableRecordAtIndex(1);
        td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        upliftBIEVerificationPage = upliftBIEPage.Next();

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

        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Child Item Reference/Extension/Reason/Sequence Number Number");
        clickOn(sourceNode);
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("//Child Item Reference/Child Line/Document Reference/Revision Identifier/Scheme Version Identifier");
        clickOn(targetNode);
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Scheme Version Identifier"));

        upliftBIEVerificationPage.next();
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        click(elementToBeClickable(getDriver(), UPLIFT_BUTTON_LOCATOR));
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        currentUrl = getDriver().getCurrentUrl();
        topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!upliftedBIEs.containsKey("BIECAGUplift2")) {
            upliftedBIEs.put("BIECAGUplift2", topLevelASBIEP);
        } else {
            upliftedBIEs.put("BIECAGUplift2", topLevelASBIEP);
        }
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Revision Identifier");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals(CLaccessendUserwip, getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Manufacturing Party/CAGEID");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals(CLaccessendUserqa, getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Manufacturing Party/DODAACID");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals(CLaccessendUserproduction, getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Manufacturing Party/SCACID");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals(CLaccessendUserdeleted, getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Manufacturing Party/CCRID");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals(CLaccessUseraDeprecated.getName(), getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Manufacturing Party/Account Identifier");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("CLuserderived_BIEUp", getText(bbiePanel.getValueDomainField()));

        bbieSCNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Line/Document Reference/Revision Identifier/Scheme Version Identifier");
        waitFor(Duration.ofMillis(2000));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertEquals(CLaccessendUserqa, getText(bbiescPanel.getValueDomainField()));

        bbieSCNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Technical Name");
        waitFor(Duration.ofMillis(2000));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertEquals(CLaccessendUserwip, getText(bbiescPanel.getValueDomainField()));

        bbieSCNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Placard Endorsement");
        waitFor(Duration.ofMillis(2000));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertEquals(CLaccessendUserqa, getText(bbiescPanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Placard Notation");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals(CLaccessendUserproduction, getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Marine Pollution Level Code");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals("CLuserderived_BIEUp", getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Toxicity Zone Code");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals(CLaccessendUserdeleted, getText(bbiePanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Flashpoint Temperature");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertEquals(CLaccessUseraDeprecated.getName(), getText(bbiePanel.getValueDomainField()));

        bbieSCNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/Primary Entry Route/Type Code");
        waitFor(Duration.ofMillis(2000));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertEquals(CLaccessendUserwip, getText(bbiescPanel.getValueDomainField()));

        bbieSCNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/MFAGID/Scheme Version Identifier");
        waitFor(Duration.ofMillis(2000));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertEquals("CLuserderived_BIEUp", getText(bbiescPanel.getValueDomainField()));

        bbieSCNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Hazardous Material/MFAGID/Scheme Agency Identifier");
        waitFor(Duration.ofMillis(2000));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertEquals(CLaccessendUserqa, getText(bbiescPanel.getValueDomainField()));

        bbieNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Export Control/Encryption Status Code");
        waitFor(Duration.ofMillis(2000));
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertTrue(getText(bbiePanel.getValueDomainField()).startsWith("clm6TimeFormatCode1_TimeFormatCode"));

        bbieSCNode = editBIEPage.getNodeByPath("/Child Item Reference/Child Item/Export Control/Certification Identifier/Scheme Agency Identifier");
        waitFor(Duration.ofMillis(2000));
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        assertTrue(getText(bbiescPanel.getValueDomainField()).startsWith("clm6TimeFormatCode1_TimeFormatCode"));
        homePage.logout();

    }

    @Test
    public void test_TA_29_1_12() {
        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        NamespaceObject euNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        BIEMenu bieMenu = homePage.getBIEMenu();
        //JournalEntry prev_release
        bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();

        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = createBIEForSelectBusinessContextsPage.next(Arrays.asList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE("Journal Entry. Journal Entry", prev_release);
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.indexOf("/profile_bie/") + "/profile_bie/".length()));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!testingBIEs.containsKey("ReusedJournalyEntry")) {
            testingBIEs.put("ReusedJournalyEntry", topLevelASBIEP);
        } else {
            testingBIEs.put("ReusedJournalyEntry", topLevelASBIEP);
        }

        if (!BIEContexts.containsKey("ReusedJournalyEntry")) {
            BIEContexts.put("ReusedJournalyEntry", context.getName());
        } else {
            BIEContexts.put("ReusedJournalyEntry", context.getName());
        }

        TopLevelASBIEPObject topLevelASBIEPObject = testingBIEs.get("JournalEntry");
        TopLevelASBIEPObject reusedBIE = testingBIEs.get("ReusedJournalyEntry");
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEPObject);
        viewEditBIEPage.openPage();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEPObject);
        SelectProfileBIEToReuseDialog
                selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/Post Acknowledge Journal Entry/Data Area/Journal Entry");
        selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE.getDen());
        editBIEPage.moveToQA();

        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        TopLevelASBIEPObject JournalEntry = testingBIEs.get("JournalEntry");
        upliftBIEPage.setPropertyTerm(JournalEntry.getPropertyTerm());
        upliftBIEPage.setState("QA");
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.Next();
        upliftBIEVerificationPage.next();
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(180));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        assertTrue(getDriver().findElement(By.xpath("//*[contains(text(),\"Usage Description\")]")).isDisplayed());
        assertTrue(getDriver().findElement(By.xpath("//*[contains(text(),\"Language Code\")]")).isDisplayed());
        assertTrue(getDriver().findElement(By.xpath("//*[contains(text(),\"Control Objective Category\")]")).isDisplayed());
        assertTrue(getDriver().findElement(By.xpath("//*[contains(text(),\"List Version Identifier\")]")).isDisplayed());
        assertTrue(getDriver().findElement(By.xpath("//*[contains(text(),\"BCCP\")]")).isDisplayed());
        assertTrue(getDriver().findElement(By.xpath("//*[contains(text(),\"DT_SC\")]")).isDisplayed());
        assertTrue(getDriver().findElement(By.xpath("//*[contains(text(),\"ASCCP\")]")).isDisplayed());
        assertTrue(getDriver().findElement(By.xpath("//*[contains(text(),\"Not selected\")]")).isDisplayed());
        assertTrue(getDriver().findElement(By.xpath("//*[contains(text(),\"System\")]")).isDisplayed());
        assertTrue(getDriver().findElement(By.xpath("//*[contains(text(),\"Unmatched\")]")).isDisplayed());
    }

    private class RandomCodeListWithStateContainer {
        private final AppUserObject appUser;
        private List<String> states = new ArrayList<>();
        private final HashMap<String, CodeListObject> stateCodeLists = new HashMap<>();
        private final HashMap<String, CodeListValueObject> stateCodeListValues = new HashMap<>();

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
