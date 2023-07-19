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
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.oagi.score.e2e.page.core_component.ACCExtensionViewEditPage;
import org.oagi.score.e2e.page.core_component.SelectAssociationDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.sound.midi.ShortMessage;
import java.math.BigInteger;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.SAME_THREAD)
public class TC_29_1_BIEUplifting extends BaseTest {
    private List<AppUserObject> randomAccounts = new ArrayList<>();
    String prev_release = "10.8.4";
    String curr_release = "10.8.8";
    AppUserObject usera, userb, developer;
    Map<String, TopLevelASBIEPObject> testingBIEs = new HashMap<>();
    Map<String, TopLevelASBIEPObject> upliftedBIEs = new HashMap<>();
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
        precondition_TA_29_1_2();
        test_TA_29_1_5a();
    }

    public void initialization(){
        usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(usera);

        userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(userb);

        developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
    }

    public void precondition_TA_29_1_2(){

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
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!testingBIEs.containsKey("BIE1QA")){
            testingBIEs.put("BIE1QA", topLevelASBIEP);
        }else{
            testingBIEs.put("BIE1QA", topLevelASBIEP);
        }

        if (!BIEContexts.containsKey("BIE1QA")){
            BIEContexts.put("BIE1QA", context.getName());
        }else{
            BIEContexts.put("BIE1QA", context.getName());
        }

        topLevelASBIEPPanel.setBusinessTerm("aBusinessTerm");
        topLevelASBIEPPanel.setRemark("aRemark");
        topLevelASBIEPPanel.setStatus("aStatus");
        editBIEPage.hitUpdateButton();

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
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Identifier");
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Name");
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        editBIEPage.hitUpdateButton();

        WebElement asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Incorporation Location");
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        asbiePanel.toggleUsed();
        asbiePanel.setRemark("aRemark");
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Identifier Set/Scheme Version Identifier");
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setRemark("aRemark");
        editBIEPage.hitUpdateButton();

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Incorporation Location/CAGEID");
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setRemark("aRemark");
        bbiePanel.setExample("anExample");
        bbiePanel.setValueConstraint("Fixed Value");
        bbiePanel.setFixedValue("99");
        bbiePanel.setValueDomain("language");
        bbiePanel.setContextDefinition("defcon");
        editBIEPage.hitUpdateButton();

        editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Last Modification Date Time");
        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Usage Description");
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setRemark("aRemark");
        bbiePanel.setExample("anExample");
        bbiePanel.setValueConstraint("Fixed Value");
        bbiePanel.setFixedValue("99");
        bbiePanel.setValueDomain("normalized string");
        bbiePanel.setContextDefinition("defcon");
        editBIEPage.hitUpdateButton();

        editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Product Classification/Extension/Indicator");
        asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Incorporation Location/Physical Address");
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

        editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Product Classification/Extension/Indicator");
        WebElement bbieSCNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Incorporation Location/Physical Address/Status/Identifier/Scheme Agency Identifier");
        EditBIEPage.BBIESCPanel bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setRemark("aRemark");
        bbiescPanel.setExample("anExample");
        bbiescPanel.setValueConstraint("Fixed Value");
        bbiescPanel.setFixedValue("99");
        bbiescPanel.setValueDomain("normalized string");
        bbiescPanel.setContextDefinition("defcon");
        editBIEPage.hitUpdateButton();

        editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Product Classification/Extension/Indicator");
        bbieSCNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Incorporation Location/Physical Address/Postal Code/List Agency Identifier");
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setRemark("aRemark");
        bbiescPanel.setExample("anExample");
        bbiescPanel.setValueConstraint("Fixed Value");
        bbiescPanel.setFixedValue("99");
        bbiescPanel.setValueDomain("normalized string");
        bbiescPanel.setContextDefinition("defcon");
        editBIEPage.hitUpdateButton();

        editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Product Classification/Extension/Indicator");
        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Identifier");
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setCardinalityMin(11);
        bbiePanel.setCardinalityMax(99);
        bbiePanel.setExample("anExample");
        bbiePanel.setRemark("aRemark");
        bbiePanel.setContextDefinition("defcon");
        bbiePanel.setFixedValue("99");
        bbiePanel.setValueDomain("token");
        editBIEPage.hitUpdateButton();

        editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Product Classification/Extension/Indicator");
        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Type Code");
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setExample("anExample");
        bbiePanel.setRemark("aRemark");
        bbiePanel.setContextDefinition("defcon");
        bbiePanel.setFixedValue("99");
        bbiePanel.setValueDomain("any URI");
        editBIEPage.hitUpdateButton();


        editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Product Classification/Extension/Indicator");
        bbieSCNode = editBIEPage.getNodeByPath("/Enterprise Unit/Cost Center Identifier/Scheme Agency Identifier");
        bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
        bbiescPanel.toggleUsed();
        bbiescPanel.setRemark("aRemark");
        bbiescPanel.setExample("anExample");
        bbiescPanel.setValueConstraint("Fixed Value");
        bbiescPanel.setFixedValue("99");
        bbiescPanel.setValueDomainRestriction("Code");
        bbiescPanel.setValueDomain("clm6ConstraintTypeCode1_ConstraintTypeCode");
        bbiescPanel.setContextDefinition("defcon");
        editBIEPage.hitUpdateButton();

        editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Product Classification/Extension/Indicator");
        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Indicator");
        bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        bbiePanel.toggleUsed();
        bbiePanel.setCardinalityMin(11);
        bbiePanel.setCardinalityMax(99);
        bbiePanel.setExample("anExample");
        bbiePanel.setRemark("aRemark");
        bbiePanel.setContextDefinition("defcon");
        bbiePanel.setFixedValue("99");
        bbiePanel.setValueDomain("token");
        editBIEPage.hitUpdateButton();

        editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Product Classification/Extension/Indicator");
        asbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Revised Item Status");
        asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        asbiePanel.toggleUsed();
        asbiePanel.setRemark("aRemark");
        asbiePanel.setContextDefinition("defcon");
        asbiePanel.setCardinalityMin(11);
        asbiePanel.setCardinalityMax(99);
        editBIEPage.hitUpdateButton();

        editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Product Classification/Extension/Indicator");
        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Extension/Revised Item Status/Reason Code");
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
        waitFor(Duration.ofSeconds(12000));
        new WebDriverWait(getDriver(), Duration.ofSeconds(10)).until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        By NEXT_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Next\")]//ancestor::button[1]");
        click(elementToBeClickable(getDriver(), NEXT_BUTTON_LOCATOR));
        waitFor(Duration.ofSeconds(12000));
        new WebDriverWait(getDriver(), Duration.ofSeconds(10)).until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        By UPLIFT_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Uplift\")]//ancestor::button[1]");
        click(elementToBeClickable(getDriver(), UPLIFT_BUTTON_LOCATOR));
        waitFor(Duration.ofMillis(2500));
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!upliftedBIEs.containsKey("BIE1QA")){
            upliftedBIEs.put("BIE1QA", topLevelASBIEP);
        }else{
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
        assertEquals("aRemark",getText(topLevelASBIEPPanel.getRemarkField()));
        assertEquals("aStatus", getText(topLevelASBIEPPanel.getStatusField()));

        WebElement bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Identifier");
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
        assertChecked(bbiePanel.getUsedCheckbox());

        bbieNode = editBIEPage.getNodeByPath("/Enterprise Unit/Type Code");
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
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        assertChecked(asbiePanel.getUsedCheckbox());

        // Verify no BIEnode in tree  "/Enterprise Unit/Extension/Incorporation Location"
        // verify no BIEnode in tree  "/Enterprise Unit/Extension/Last Modification Date Time"
        viewEditBIEPage.openPage();
        viewEditBIEPage.setBranch(curr_release);
        assertTrue(viewEditBIEPage.openEditBIEPage(topLevelASBIEP).isOpened());
    }

    public void precondtions_TA_29_1_2_Production_BIE_Uplift(){
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
        String  currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
        TopLevelASBIEPObject  topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!testingBIEs.containsKey("BIEUserbProduction")){
            testingBIEs.put("BIEUserbProduction", topLevelASBIEP);
        }else{
            testingBIEs.put("BIEUserbProduction", topLevelASBIEP);
        }

        if (!BIEContexts.containsKey("BIEUserbProduction")){
            BIEContexts.put("BIEUserbProduction", context.getName());
        }else{
            BIEContexts.put("BIEUserbProduction", context.getName());
        }

        WebElement bbieNode = editBIEPage.getNodeByPath("/Change Acknowledge Shipment Status/System Environment Code");
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
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        asbiePanel.setRemark("aRemark");
        asbiePanel.setContextDefinition("defcon");
        editBIEPage.hitUpdateButton();

        WebElement bbieSCNode = editBIEPage.getNodeByPath("/Change Acknowledge Shipment Status/Application Area/Scenario Identifier/Scheme Version Identifier");
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
    }

    @Test
    public void test_TA_29_1_2_Production_BIE_Uplift() {
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        // Uplift Production BIE
        upliftBIEPage.openPage();
        upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setState("Production");
        TopLevelASBIEPObject BIEUserbProduction = testingBIEs.get("BIEUserbProduction");
        upliftBIEPage.setPropertyTerm(BIEUserbProduction.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        click(upliftBIEPage.getNextButton());
        waitFor(Duration.ofSeconds(12000));
        new WebDriverWait(getDriver(), Duration.ofSeconds(10)).until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        By NEXT_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Next\")]//ancestor::button[1]");
        click(elementToBeClickable(getDriver(), NEXT_BUTTON_LOCATOR));
        waitFor(Duration.ofSeconds(12000));
        new WebDriverWait(getDriver(), Duration.ofSeconds(10)).until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));
        By UPLIFT_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Uplift\")]//ancestor::button[1]");
        click(elementToBeClickable(getDriver(), UPLIFT_BUTTON_LOCATOR));
        waitFor(Duration.ofMillis(2500));
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!upliftedBIEs.containsKey("BIEUserbProduction")){
            upliftedBIEs.put("BIEUserbProduction", topLevelASBIEP);
        }else{
            upliftedBIEs.put("BIEUserbProduction", topLevelASBIEP);
        }

        bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
        EditBIEPage.TopLevelASBIEPPanel  topLevelASBIEPPanel = editBIEPage.getTopLevelASBIEPPanel();
        assertEquals(developer.getLoginId(), getText(topLevelASBIEPPanel.getOwnerField()));
        String bizContext = BIEContexts.get("BIEUserbProduction");
        assertEquals(bizContext, getText(topLevelASBIEPPanel.getBusinessContextInputField()));

        WebElement bbieNode = editBIEPage.getNodeByPath("/Change Acknowledge Shipment Status/System Environment Code");
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
        EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
        assertChecked(asbiePanel.getUsedCheckbox());
        assertNotChecked(asbiePanel.getNillableCheckbox());
        assertEquals("1", getText(asbiePanel.getCardinalityMinField()));
        assertEquals("1", getText(asbiePanel.getCardinalityMaxField()));
        assertEquals("aRemark", getText(asbiePanel.getRemarkField()));
        assertEquals("defcon", getText(asbiePanel.getContextDefinitionField()));

        WebElement bbieScNode = editBIEPage.getNodeByPath("/Change Acknowledge Shipment Status/Application Area/Scenario Identifier/Scheme Version Identifier");
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
        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
        CreateBIEForSelectTopLevelConceptPage  createBIEForSelectTopLevelConceptPage = createBIEForSelectBusinessContextsPage.next(Arrays.asList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE("Batch Certificate Of Analysis. Batch Certificate Of Analysis", prev_release);
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!testingBIEs.containsKey("BIEUserbWIP")){
            testingBIEs.put("BIEUserbWIP", topLevelASBIEP);
        }else{
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
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setState("QA");
        TopLevelASBIEPObject BIE1QA = testingBIEs.get("BIE1QA");
        upliftBIEPage.setPropertyTerm(BIE1QA.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.Next();
        upliftBIEVerificationPage.expandNodeInSourceBIE("Enterprise Unit");
        upliftBIEVerificationPage.expandNodeInTargetBIE("Enterprise Unit");
        for(String path:
                Arrays.asList("/Enterprise Unit", "/Enterprise Unit/Type Code", "/Enterprise Unit/Identifier",
                        "/Enterprise Unit/Identifier Set/Scheme Version Identifier",
                        "/Enterprise Unit/Identifier Set/Scheme Agency Identifier",
                        "/Enterprise Unit/Identifier Set/Identifier",
                        "/Enterprise Unit/Name",
                        "/Enterprise Unit/Extension")) {
            WebElement sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE(path);
            assertFalse(getDriver().findElement(By.xpath("//mat-card-content/div[2]/div[1]//cdk-virtual-scroll-viewport//ancestor::div[1]/mat-checkbox[1]")).isDisplayed());
        }

        for(String path:
                Arrays.asList("/Enterprise Unit", "/Enterprise Unit/Type Code", "/Enterprise Unit/Identifier",
                        "/Enterprise Unit/Identifier Set/Scheme Version Identifier",
                        "/Enterprise Unit/Identifier Set/Scheme Agency Identifier",
                        "/Enterprise Unit/Identifier Set/Identifier",
                        "/Enterprise Unit/Name",
                        "/Enterprise Unit/Extension")) {
            WebElement sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE(path);
            WebElement targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE(path);
            assertChecked(getDriver().findElement(By.xpath("//mat-card-content/div[2]/div[1]//cdk-virtual-scroll-viewport//ancestor::div[1]/mat-checkbox[1]")));
            assertDisabled(getDriver().findElement(By.xpath("//mat-card-content/div[2]/div[1]//cdk-virtual-scroll-viewport//ancestor::div[1]/mat-checkbox[1]")));
        }
        escape(getDriver());
        escape(getDriver());
        upliftBIEVerificationPage.next();
        waitFor(Duration.ofSeconds(12000));
        new WebDriverWait(getDriver(), Duration.ofSeconds(10)).until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class, 'loading-container')]")));

        By UPLIFT_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Uplift\")]//ancestor::button[1]");
        click(elementToBeClickable(getDriver(), UPLIFT_BUTTON_LOCATOR));
        waitFor(Duration.ofMillis(2500));
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!upliftedBIEs.containsKey("BIE1QA")){
            upliftedBIEs.put("BIE1QA", topLevelASBIEP);
        }else{
            upliftedBIEs.put("BIE1QA", topLevelASBIEP);
        }
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
        assertTrue(editBIEPage.isOpened());
    }

    @Test
    public void test_TA_29_1_5b() {
        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setState("QA");
        TopLevelASBIEPObject BIE1QA = testingBIEs.get("BIE1QA");
        upliftBIEPage.setPropertyTerm(BIE1QA.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.Next();

        WebElement sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Enterprise Unit/Extension/Indicator");
        WebElement targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Enterprise Unit/GL Entity Identifier/Scheme Version Identifier");
        assertDisabled(getDriver().findElement(By.xpath("//mat-card-content/div[2]/div[1]//cdk-virtual-scroll-viewport//ancestor::div[1]/mat-checkbox[1]")));
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Enterprise Unit/General Ledger Element");
        assertDisabled(getDriver().findElement(By.xpath("//mat-card-content/div[2]/div[1]//cdk-virtual-scroll-viewport//ancestor::div[1]/mat-checkbox[1]")));
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("Enterprise Unit/Profit Center Identifier");
        assertDisabled(getDriver().findElement(By.xpath("//mat-card-content/div[2]/div[1]//cdk-virtual-scroll-viewport//ancestor::div[1]/mat-checkbox[1]")));
        homePage.logout();
    }

    @Test
    public void test_TA_29_1_5c_and_TA_29_1_8() {
        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        UpliftBIEPage upliftBIEPage = bieMenu.openUpliftBIESubMenu();
        upliftBIEPage.setSourceBranch(prev_release);
        upliftBIEPage.setTargetBranch(curr_release);
        upliftBIEPage.setState("QA");
        TopLevelASBIEPObject BIE1QA = testingBIEs.get("BIE1QA");
        upliftBIEPage.setPropertyTerm(BIE1QA.getPropertyTerm());
        upliftBIEPage.hitSearchButton();
        WebElement tr = upliftBIEPage.getTableRecordAtIndex(1);
        WebElement td = upliftBIEPage.getColumnByName(tr, "select");
        click(td);
        UpliftBIEVerificationPage upliftBIEVerificationPage = upliftBIEPage.Next();
        //different green
        WebElement sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Enterprise Unit/Extension/Indicator");
        WebElement targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Enterprise Unit/Profit Center Identifier");
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("Profit Center Identifier"));
        escape(getDriver());

        //different blue
        sourceNode = upliftBIEVerificationPage.goToNodeInSourceBIE("/Enterprise Unit/Identifier Set");
        targetNode = upliftBIEVerificationPage.goToNodeInTargetBIE("/Enterprise Unit/General Ledger Element");
        click(upliftBIEVerificationPage.getCheckBoxOfNodeInTargetBIE("General Ledger Element"));
        escape(getDriver());

        By UPLIFT_BUTTON_LOCATOR =
                By.xpath("//span[contains(text(), \"Uplift\")]//ancestor::button[1]");
        click(elementToBeClickable(getDriver(), UPLIFT_BUTTON_LOCATOR));
        waitFor(Duration.ofMillis(2500));
        String currentUrl = getDriver().getCurrentUrl();
        BigInteger topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
                .getTopLevelASBIEPByID(topLevelAsbiepId);

        if (!upliftedBIEs.containsKey("BIE1QA")){
            upliftedBIEs.put("BIE1QA", topLevelASBIEP);
        }else{
            upliftedBIEs.put("BIE1QA", topLevelASBIEP);
        }
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);
        assertTrue(editBIEPage.isOpened());
    }

    @Test
    public void test_TA_29_1_5d() {

    }

    @Test
    public void test_TA_29_1_6a() {

    }

    @Test
    public void test_TA_29_1_6b() {

    }

    @Test
    public void test_TA_29_1_7() {

    }

    @Test
    public void test_TA_29_1_8() {

    }

    @Test
    public void test_TA_29_1_9a() {

    }

    @Test
    public void test_TA_29_1_9b() {

    }

    @Test
    public void test_TA_29_1_9c() {

    }

    @Test
    public void test_TA_29_1_10a() {

    }

    @Test
    public void test_TA_29_1_10b() {

    }

    @Test
    public void test_TA_29_1_11a() {

    }

    @Test
    public void test_TA_29_1_11b() {

    }

    @Test
    public void test_TA_29_1_12() {

    }

    @Test
    public void test_TA_29_1_13() {

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

//        public void preconditions(){
//
//
//            homePage.logout();
//            homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
//            //BIEuserbReusedChild previousRelease
//            BusinessContextObject contextForUserb = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
//            bieMenu = homePage.getBIEMenu();
//            viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
//            createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
//            createBIEForSelectTopLevelConceptPage = createBIEForSelectBusinessContextsPage.next(Arrays.asList(contextForUserb));
//            editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE("Unit Packaging. Packaging", prev_release);
//            currentUrl = getDriver().getCurrentUrl();
//            topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
//            topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
//                    .getTopLevelASBIEPByID(topLevelAsbiepId);
//
//            if (!testingBIEs.containsKey("BIEUserbReusedChild")){
//                testingBIEs.put("BIEUserbReusedChild", topLevelASBIEP);
//            }else{
//                testingBIEs.put("BIEUserbReusedChild", topLevelASBIEP);
//            }
//
//            if (!BIEContexts.containsKey("BIEUserbReusedChild")){
//                BIEContexts.put("BIEUserbReusedChild", contextForUserb.getName());
//            }else{
//                BIEContexts.put("BIEUserbReusedChild", contextForUserb.getName());
//            }
//
//            bbieNode = editBIEPage.getNodeByPath("/Unit Packaging/Capacity Per Package Quantity");
//            bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
//            bbiePanel.toggleUsed();
//            bbiePanel.setRemark("aRemark");
//            bbiePanel.setExample("anExample");
//            bbiePanel.setContextDefinition("defcon");
//            bbiePanel.setValueConstraint("Fixed Value");
//            bbiePanel.setFixedValue("99");
//            editBIEPage.hitUpdateButton();
//
//            WebElement asbieNode = editBIEPage.getNodeByPath("/Unit Packaging/Dimensions");
//            EditBIEPage.ASBIEPanel asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
//            asbiePanel.toggleUsed();
//            asbiePanel.setCardinalityMax(99);
//            asbiePanel.setCardinalityMin(11);
//            asbiePanel.setRemark("aRemark");
//            asbiePanel.setContextDefinition("defcon");
//            editBIEPage.hitUpdateButton();
//
//            WebElement bbieSCNode = editBIEPage.getNodeByPath("/Unit Packaging/UPC Packaging Level Code/List Agency Identifier");
//            EditBIEPage.BBIESCPanel  bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
//            bbiescPanel.toggleUsed();
//            bbiescPanel.setRemark("aRemark");
//            bbiescPanel.setExample("anExample");
//            bbiescPanel.setValueConstraint("Fixed Value");
//            bbiescPanel.setFixedValue("99");
//            bbiescPanel.setValueDomain("token");
//            bbiescPanel.setContextDefinition("defcon");
//            editBIEPage.hitUpdateButton();
//            editBIEPage.moveToQA();
//            editBIEPage.moveToProduction();
//
//            //BIEuserbReusedParent previousRelease
//
//            viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
//            createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
//            createBIEForSelectTopLevelConceptPage = createBIEForSelectBusinessContextsPage.next(Arrays.asList(contextForUserb));
//            editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE("From UOM Package. UOM Package", prev_release);
//            currentUrl = getDriver().getCurrentUrl();
//            topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
//            topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
//                    .getTopLevelASBIEPByID(topLevelAsbiepId);
//
//            if (!testingBIEs.containsKey("BIEUserbReusedParent")){
//                testingBIEs.put("BIEUserbReusedParent", topLevelASBIEP);
//            }else{
//                testingBIEs.put("BIEUserbReusedParent", topLevelASBIEP);
//            }
//
//            if (!BIEContexts.containsKey("BIEUserbReusedParent")){
//                BIEContexts.put("BIEUserbReusedParent", contextForUserb.getName());
//            }else{
//                BIEContexts.put("BIEUserbReusedParent", contextForUserb.getName());
//            }
//
//            bbieNode = editBIEPage.getNodeByPath("/From UOM Package/UOM Code");
//            bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
//            bbiePanel.toggleUsed();
//            bbiePanel.setRemark("aRemark");
//            bbiePanel.setExample("anExample");
//            bbiePanel.setContextDefinition("defcon");
//            bbiePanel.setValueConstraint("Fixed Value");
//            bbiePanel.setFixedValue("99");
//            bbiePanel.setValueDomain("token");
//            editBIEPage.hitUpdateButton();
//
//            asbieNode = editBIEPage.getNodeByPath("/From UOM Package/Unit Packaging");
//            asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
//            SelectProfileBIEToReuseDialog selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/From UOM Package/Unit Packaging");
//            TopLevelASBIEPObject reusedBIE = testingBIEs.get("BIEUserbReusedChild");
//            selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE);
//            editBIEPage.moveToQA();
//            editBIEPage.moveToProduction();
//
//            //BIEUserbReusedScenario previousRelease
//            viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
//            createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
//            createBIEForSelectTopLevelConceptPage = createBIEForSelectBusinessContextsPage.next(Arrays.asList(contextForUserb));
//            editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE("UOM Code Conversion Rate. UOM Code Conversion Rate", prev_release);
//            currentUrl = getDriver().getCurrentUrl();
//            topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
//            topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
//                    .getTopLevelASBIEPByID(topLevelAsbiepId);
//
//            if (!testingBIEs.containsKey("BIEUserbReusedScenario")){
//                testingBIEs.put("BIEUserbReusedScenario", topLevelASBIEP);
//            }else{
//                testingBIEs.put("BBIEUserbReusedScenario", topLevelASBIEP);
//            }
//
//            if (!BIEContexts.containsKey("BIEUserbReusedScenario")){
//                BIEContexts.put("BIEUserbReusedScenario", contextForUserb.getName());
//            }else{
//                BIEContexts.put("BIEUserbReusedScenario", contextForUserb.getName());
//            }
//
//            selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/UOM Code Conversion Rate/From UOM Package");
//            reusedBIE = testingBIEs.get("BIEUserbReusedParent");
//            selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE);
//            editBIEPage.moveToQA();
//
//            homePage.logout();
//            homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
//            //BIECustomerItemIdentification previousRelease
//            viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
//            createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
//            createBIEForSelectTopLevelConceptPage = createBIEForSelectBusinessContextsPage.next(Arrays.asList(context));
//            editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE("Customer Item Identification. Item Identification", prev_release);
//            currentUrl = getDriver().getCurrentUrl();
//            topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
//            topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
//                    .getTopLevelASBIEPByID(topLevelAsbiepId);
//
//            if (!testingBIEs.containsKey("BIECustomerItemIdentification")){
//                testingBIEs.put("BIECustomerItemIdentification", topLevelASBIEP);
//            }else{
//                testingBIEs.put("BIECustomerItemIdentification", topLevelASBIEP);
//            }
//
//            if (!BIEContexts.containsKey("BIECustomerItemIdentification")){
//                BIEContexts.put("BIECustomerItemIdentification", context.getName());
//            }else{
//                BIEContexts.put("BIECustomerItemIdentification", context.getName());
//            }
//
//            bbieNode = editBIEPage.getNodeByPath("/Customer Item Identification/Revision Identifier");
//            bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
//            bbiePanel.toggleUsed();
//            bbiePanel.setRemark("aRemark");
//            bbiePanel.setExample("anExample");
//            bbiePanel.setContextDefinition("defcon");
//            bbiePanel.setValueConstraint("Default Value");
//            bbiePanel.setDefaultValue("99");
//            editBIEPage.hitUpdateButton();
//            editBIEPage.moveToQA();
//            editBIEPage.moveToProduction();
//
//            //BIEBOMItemData previousRelease
//            viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
//            createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
//            createBIEForSelectTopLevelConceptPage = createBIEForSelectBusinessContextsPage.next(Arrays.asList(context));
//            editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE("BOM Item Data. BOM Item Data", prev_release);
//            currentUrl = getDriver().getCurrentUrl();
//            topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
//            topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
//                    .getTopLevelASBIEPByID(topLevelAsbiepId);
//
//            if (!testingBIEs.containsKey("BIEBOMItemData")){
//                testingBIEs.put("BIEBOMItemData", topLevelASBIEP);
//            }else{
//                testingBIEs.put("BBIEBOMItemData", topLevelASBIEP);
//            }
//
//            if (!BIEContexts.containsKey("BIEBOMItemData")){
//                BIEContexts.put("BIEBOMItemData", context.getName());
//            }else{
//                BIEContexts.put("BIEBOMItemData", context.getName());
//            }
//
//
//            selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/BOM Item Data/Customer Item Identification");
//            reusedBIE = testingBIEs.get("BIECustomerItemIdentification");
//            selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE);
//
//            bbieNode = editBIEPage.getNodeByPath("/BOM Item Data/Reference Designator Identifier");
//            bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
//            bbiePanel.toggleUsed();
//            bbiePanel.setRemark("aRemark");
//            bbiePanel.setExample("anExample");
//            bbiePanel.setContextDefinition("defcon");
//            bbiePanel.setValueConstraint("Default Value");
//            bbiePanel.setDefaultValue("99");
//            editBIEPage.hitUpdateButton();
//
//            bbieSCNode = editBIEPage.getNodeByPath("/BOM Item Data/Supplier Item Identification/Item Identifier Set/Identifier/Scheme Version Identifier");
//            bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
//            bbiescPanel.toggleUsed();
//            bbiescPanel.setRemark("aRemark");
//            bbiescPanel.setExample("anExample");
//            bbiescPanel.setContextDefinition("defcon");
//            assertEquals("1", getText(bbiescPanel.getCardinalityMaxField()));
//            editBIEPage.hitUpdateButton();
//            editBIEPage.moveToQA();
//            editBIEPage.moveToProduction();
//
//            //BIEBOMDoubleNested previousRelease
//            viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
//            createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
//            createBIEForSelectTopLevelConceptPage = createBIEForSelectBusinessContextsPage.next(Arrays.asList(context));
//            editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE("BOM. BOM", prev_release);
//            currentUrl = getDriver().getCurrentUrl();
//            topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
//            topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
//                    .getTopLevelASBIEPByID(topLevelAsbiepId);
//
//            if (!testingBIEs.containsKey("BIEBOMDoubleNested")){
//                testingBIEs.put("BIEBOMDoubleNested", topLevelASBIEP);
//            }else{
//                testingBIEs.put("BIEBOMDoubleNested", topLevelASBIEP);
//            }
//
//            if (!BIEContexts.containsKey("BIEBOMDoubleNested")){
//                BIEContexts.put("BIEBOMDoubleNested", context.getName());
//            }else{
//                BIEContexts.put("BIEBOMDoubleNested", context.getName());
//            }
//
//            bbieSCNode = editBIEPage.getNodeByPath("/BOM/BOM Header/Document Identifier Set/Identifier/Scheme Agency Identifier");
//            bbiescPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
//            bbiescPanel.toggleUsed();
//            selectProfileBIEToReuseDialog = editBIEPage.reuseBIEOnNode("/BOM/BOM Item Data");
//            reusedBIE = testingBIEs.get("BIEBOMItemData");
//            selectProfileBIEToReuseDialog.selectBIEToReuse(reusedBIE);
//
//            asbieNode = editBIEPage.getNodeByPath("/BOM/BOM Option/BOM Item Data/Hazardous Material");
//            asbiePanel = editBIEPage.getASBIEPanel(asbieNode);
//            asbiePanel.toggleUsed();
//            editBIEPage.hitUpdateButton();
//
//            homePage.logout();
//            homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
//            List<String> euCLStates = new ArrayList<>();
//            euCLStates.add("WIP");
//            euCLStates.add("QA");
//            euCLStates.add("Production");
//            euCLStates.add("Deleted");
//            ReleaseObject prev_releaseObject = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(prev_release);
//            RandomCodeListWithStateContainer euCodeListWithStateContainer = new RandomCodeListWithStateContainer(usera, prev_releaseObject, euNamespace, euCLStates);
//            CodeListObject CLaccessUseraDeprecated = getAPIFactory().getCodeListAPI().createRandomCodeList(usera, euNamespace, prev_releaseObject, "Production");
//            CodeListValueObject codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(CLaccessUseraDeprecated, usera);
//            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
//            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch(CLaccessUseraDeprecated.getName(), prev_release);
//            editCodeListPage.hitAmendButton();
//            click(editCodeListPage.getDeprecatedSelectField());
//            editCodeListPage.setDefinition("Check the Deprecated Checkbox");
//            editCodeListPage.hitUpdateButton();
//            editCodeListPage.moveToQA();
//            editCodeListPage.moveToProduction();
//
//            viewEditCodeListPage.openPage();
//            editCodeListPage =  viewEditCodeListPage.openCodeListViewEditPageByNameAndBranch("oacl_MatchDocumentCode", prev_release);
//            editCodeListPage.hitDeriveCodeListBasedOnThisButton();
//            editCodeListPage.setName("CLuserderived_BIEUp");
//            editCodeListPage.setNamespace(euNamespace);
//            editCodeListPage.setDefinition("aDefinition");
//            editCodeListPage.hitUpdateButton();
//
//            //BIECAGUplift prev_release
//            bieMenu = homePage.getBIEMenu();
//            viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
//            createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
//            createBIEForSelectTopLevelConceptPage = createBIEForSelectBusinessContextsPage.next(Arrays.asList(context));
//            editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE("Child Item Reference. Child Item Reference", prev_release);
//            currentUrl = getDriver().getCurrentUrl();
//            topLevelAsbiepId = new BigInteger(currentUrl.substring(currentUrl.lastIndexOf("/") + 1));
//            topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI()
//                    .getTopLevelASBIEPByID(topLevelAsbiepId);
//
//            if (!testingBIEs.containsKey("BIECAGUplift")){
//                testingBIEs.put("BIECAGUplift", topLevelASBIEP);
//            }else{
//                testingBIEs.put("BIECAGUplift", topLevelASBIEP);
//            }
//
//            if (!BIEContexts.containsKey("BIECAGUplift")){
//                BIEContexts.put("BIECAGUplift", context.getName());
//            }else{
//                BIEContexts.put("BIECAGUplift", context.getName());
//            }
//
//            accExtensionViewEditPage =
//                    editBIEPage.extendBIELocallyOnNode("/Child Item Reference/Extension");
//            selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
//            selectCCPropertyPage.selectAssociation("Effectivity Relation Code. Code");
//            selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
//            selectCCPropertyPage.selectAssociation("Validation Indicator. Indicator");
//            selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
//            selectCCPropertyPage.selectAssociation("Method Consequence Text. Text");
//            selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
//            selectCCPropertyPage.selectAssociation("Record Set Reference Identifier. Identifier");
//            selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
//            selectCCPropertyPage.selectAssociation("Record Set Total Number. Number");
//            selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
//            selectCCPropertyPage.selectAssociation("Latest Start Date Time. Date Time");
//            selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
//            selectCCPropertyPage.selectAssociation("Request Language Code. Code");
//            selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
//            selectCCPropertyPage.selectAssociation("Transport Temperature. Measure");
//            selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
//            selectCCPropertyPage.selectAssociation("Correlation Identifier. Identifier");
//            selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
//            selectCCPropertyPage.selectAssociation("Reason. Sequenced_ Open_ Text");
//            selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/Child Item Reference User Extension Group. Details");
//            selectCCPropertyPage.selectAssociation("Record Set Save Indicator. Indicator");
//
//            accExtensionViewEditPage.setNamespace(euNamespace);
//            accExtensionViewEditPage.hitUpdateButton();
//            accExtensionViewEditPage.moveToQA();
//            accExtensionViewEditPage.moveToProduction();
//            homePage.logout();
//
//        }

    }

}
