package org.oagi.score.e2e.TS_6_EndUserAccessRightScoreCoreFunctions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.oagi.score.e2e.page.core_component.ACCExtensionViewEditPage;
import org.oagi.score.e2e.page.core_component.SelectAssociationDialog;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_6_2_EndUserAuthorizedManagementBIE extends BaseTest {

    private List<AppUserObject> randomAccounts = new ArrayList<>();
    private String release = "10.8.4";

    @BeforeEach
    public void init() {
        super.init();

    }
    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_6_2_TA_1")
    public void test_TA_1() {
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject endUser;
        NamespaceObject endUserNamespace;
        ArrayList<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, developer, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            endUserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
            thisAccountWillBeDeletedAfterTests(endUser);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
            TopLevelASBIEPObject topLevelAsbiepWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, endUser, "WIP");
            biesForTesting.add(topLevelAsbiepWIP);

            TopLevelASBIEPObject topLevelAsbiepProduction = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, endUser, "Production");
            biesForTesting.add(topLevelAsbiepProduction);

            TopLevelASBIEPObject topLevelAsbiepQA = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, endUser, "QA");
            biesForTesting.add(topLevelAsbiepQA);

        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        for (TopLevelASBIEPObject topLevelAsbiep: biesForTesting){
            BIEMenu bieMenu = homePage.getBIEMenu();
            ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
            EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
            getDriver().manage().window().maximize();
            if (topLevelAsbiep.getState().equals("WIP")){
                editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
                String revision = getText(editBIEPage.getRevisionField());
                assertEquals("1", revision);
                editBIEPage.setNamespace(endUserNamespace.getUri());
                editBIEPage.hitUpdateButton();
                editBIEPage.moveToQA();
                editBIEPage.moveToProduction();
                bieMenu.openViewEditBIESubMenu();
                viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
                editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
                revision = getText(editBIEPage.getRevisionField());
                assertEquals("2", revision);
            }else{
                assertThrows(TimeoutException.class, () -> {
                    editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
                });
                escape(getDriver());
            }
        }
    }

    @Test
    @DisplayName("TC_6_2_TA_2")
    public void test_TA_2() {
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject firstEndUser;
        AppUserObject secondEndUser;
        TopLevelASBIEPObject topLevelASBIEPFirstUser;
        TopLevelASBIEPObject topLevelASBIEPSecondUser;
        NamespaceObject firstEndUserNamespace;
        {
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, developer, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
            firstEndUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            firstEndUserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(firstEndUser);
            thisAccountWillBeDeletedAfterTests(firstEndUser);

            BusinessContextObject contextFirstUser = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(firstEndUser);
            topLevelASBIEPFirstUser = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(contextFirstUser), asccp, firstEndUser, "WIP");

            secondEndUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(secondEndUser);

            BusinessContextObject contextSecondUser = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(secondEndUser);
            topLevelASBIEPSecondUser = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(contextSecondUser), asccp, secondEndUser, "WIP");

        }
        HomePage homePage = loginPage().signIn(firstEndUser.getLoginId(), firstEndUser.getPassword());

        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEPFirstUser);
        getDriver().manage().window().maximize();
        assertEquals("WIP", topLevelASBIEPFirstUser.getState());
        editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        editBIEPage.setNamespace(firstEndUserNamespace.getUri());
        editBIEPage.hitUpdateButton();
        editBIEPage.moveToQA();
        homePage.logout();
        loginPage().signIn(secondEndUser.getLoginId(), secondEndUser.getPassword());
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.openEditBIEPage(topLevelASBIEPSecondUser);
        assertEquals("WIP", topLevelASBIEPSecondUser.getState());
        editBIEPage.getExtendBIELocallyOptionForNode("/" + asccp.getPropertyTerm() + "/Extension");
        /**
         * Display a dialog indicating “The core component is being extended by “ + [the owner of the UEGACC] or similar.
         */
        assertEquals("Another user is working on the extension.",
                editBIEPage.getAtentionDialogMessage());
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.continueToExtendBIELocallyOnNode();
        /**
         * If the UEGACC is in QA state, the end user can view its details but cannot make any change.
         */
        assertEquals("QA", ACCExtensionViewEditPage.getStateFieldValue());
        assertThrows(TimeoutException.class, () -> {
            ACCExtensionViewEditPage.getUpdateButton(false);
        });
        assertThrows(TimeoutException.class, () -> {
            ACCExtensionViewEditPage.getMoveToQAButton(false);
        });
        assertThrows(TimeoutException.class, () -> {
            ACCExtensionViewEditPage.getMoveToProductionButton(false);
        });
        assertEquals(firstEndUser.getLoginId(), ACCExtensionViewEditPage.getOwnerFieldValue());
        assertDisabled(ACCExtensionViewEditPage.getDefinitionField());
        assertDisabled(ACCExtensionViewEditPage.getObjectClassTermField());
        assertDisabled(ACCExtensionViewEditPage.getDefinitionSourceField());
        switchToMainTab(getDriver());
        homePage.logout();

        loginPage().signIn(firstEndUser.getLoginId(), firstEndUser.getPassword());
        bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.openEditBIEPage(topLevelASBIEPFirstUser);
        editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        editBIEPage.backToWIP();
        homePage.logout();

        loginPage().signIn(secondEndUser.getLoginId(), secondEndUser.getPassword());
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.openEditBIEPage(topLevelASBIEPSecondUser);
        editBIEPage.getExtendBIELocallyOptionForNode("/" + asccp.getPropertyTerm() + "/Extension");
        assertEquals("Editing extension already exist.", getSnackBarMessage(getDriver()));

    }

    @Test
    @DisplayName("TC_6_2_TA_3")
    public void test_TA_3() {
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject endUser;
        ArrayList<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        NamespaceObject endUserNamespace;
        {
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, developer, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);
            endUserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
            TopLevelASBIEPObject topLevelAsbiepWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, endUser, "WIP");
            biesForTesting.add(topLevelAsbiepWIP);

        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        for (TopLevelASBIEPObject topLevelAsbiep: biesForTesting){
            BIEMenu bieMenu = homePage.getBIEMenu();
            ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
            EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
            getDriver().manage().window().maximize();
            assertEquals("WIP", topLevelAsbiep.getState());
            editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
            bieMenu.openViewEditBIESubMenu();
            viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
            editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");

            assertEnabled(editBIEPage.getSourceDefinitionField());
            assertEnabled(editBIEPage.getDefinitionField());

            editBIEPage.setNamespace(endUserNamespace.getUri());

            assertDoesNotThrow( () ->{editBIEPage.hitUpdateButton();});

            editBIEPage.moveToQA();
            bieMenu.openViewEditBIESubMenu();
            viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
            editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");

            assertDisabled(editBIEPage.getSourceDefinitionField());
            assertDisabled(editBIEPage.getDefinitionField());

            assertDoesNotThrow( () ->{editBIEPage.backToWIP();});
            assertDoesNotThrow( () ->{editBIEPage.moveToQA();});
            assertDoesNotThrow( () ->{editBIEPage.moveToProduction();});

        }
    }

    @Test
    @DisplayName("TC_6_2_TA_4")
    public void test_TA_4() {
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject endUser;
        TopLevelASBIEPObject topLevelAsbiepWIP;
        ASCCPObject asccpToAppend;
        BCCPObject bccpToAppend;
        {
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, developer, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
            topLevelAsbiepWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, endUser, "WIP");

            ACCObject accToAppend = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            asccpToAppend = coreComponentAPI.createRandomASCCP(accToAppend, developer, namespace, "Published");

            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccpToAppend = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");

        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiepWIP);
        getDriver().manage().window().maximize();
        assertEquals("WIP", topLevelAsbiepWIP.getState());
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        // TODO:
        // Can't open the context menu in a small size of the screen.
        SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
        selectCCPropertyPage.selectAssociation(asccpToAppend.getDen());
        ACCExtensionViewEditPage.appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
        selectCCPropertyPage.selectAssociation(bccpToAppend.getDen());
        bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.openEditBIEPage(topLevelAsbiepWIP);

        WebElement node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/Extension/" +asccpToAppend.getPropertyTerm());
        assertTrue(node.isDisplayed());
        EditBIEPage.ASBIEPanel ASBIEPanel = editBIEPage.getASBIEPanel(node);

        /**
         * Assert that all options are disabled.
         */
        assertDisabled(ASBIEPanel.getNillableCheckbox());
        assertDisabled(ASBIEPanel.getUsedCheckbox());
        assertDisabled(ASBIEPanel.getCardinalityMinField());
        assertDisabled(ASBIEPanel.getCardinalityMaxField());
        assertDisabled(ASBIEPanel.getRemarkField());
        assertDisabled(ASBIEPanel.getContextDefinitionField());
        assertDisabled(ASBIEPanel.getAssociationDefinitionField());
        assertDisabled(ASBIEPanel.getComponentDefinitionField());
        assertDisabled(ASBIEPanel.getTypeDefinitionField());
        //TODO
        // Check if Business Term functionality is enabled. Currently, it is disabled.
        assertDisabled(ASBIEPanel.getBusinessTermField());


        node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/Extension/" +bccpToAppend.getPropertyTerm());
        assertTrue(node.isDisplayed());
        EditBIEPage.BBIEPanel BBIEPPanel = editBIEPage.getBBIEPanel(node);
        /**
         * Assert that all options are disabled.
         */
        assertDisabled(BBIEPPanel.getNillableCheckbox());
        assertDisabled(BBIEPPanel.getUsedCheckbox());
        assertDisabled(BBIEPPanel.getCardinalityMinField());
        assertDisabled(BBIEPPanel.getCardinalityMaxField());
        assertDisabled(BBIEPPanel.getRemarkField());
        assertDisabled(BBIEPPanel.getExampleField());
        assertDisabled(BBIEPPanel.getValueConstraintSelectField());
        assertDisabled(BBIEPPanel.getValueDomainRestrictionSelectField());
        assertDisabled(BBIEPPanel.getValueDomainField());
        assertDisabled(BBIEPPanel.getContextDefinitionField());
        assertDisabled(BBIEPPanel.getAssociationDefinitionField());
        assertDisabled(BBIEPPanel.getComponentDefinitionField());
        //TODO
        // Check if Business Term functionality is enabled. Currently, it is disabled.
        assertDisabled(BBIEPPanel.getBusinessTermField());

    }

    @Test
    @DisplayName("TC_6_2_TA_5_1_and_TC_6_2_TA_5_3")
    public void test_TA_5_1_and_TA_5_3() {
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject endUserForBIE;
        TopLevelASBIEPObject topLevelAsbiepWIP;
        ArrayList<ASCCPObject> asccpsForTesting = new ArrayList<>();
        ArrayList<BCCPObject> bccpsForTesting = new ArrayList<>();
        NamespaceObject namespaceEU;
        {
            AppUserObject endUserForCC = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserForCC);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            acc = coreComponentAPI.createRandomACC(endUserForCC, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, endUserForCC, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, endUserForCC, namespace, "Published");

            ACCObject accToAppend = coreComponentAPI.createRandomACC(endUserForCC, release, namespace, "Published");
            ASCCPObject asccpToAppendWIP = coreComponentAPI.createRandomASCCP(accToAppend, endUserForCC, namespace, "WIP");
            accToAppend = coreComponentAPI.createRandomACC(endUserForCC, release, namespace, "Published");
            ASCCPObject asccpToAppendQA = coreComponentAPI.createRandomASCCP(accToAppend, endUserForCC, namespace, "QA");
            asccpsForTesting.add(asccpToAppendWIP);
            asccpsForTesting.add(asccpToAppendQA);

            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            BCCPObject bccpToAppendWIP = coreComponentAPI.createRandomBCCP(dataType, endUserForCC, namespace, "WIP");
            BCCPObject bccpToAppendQA = coreComponentAPI.createRandomBCCP(dataType, endUserForCC, namespace, "QA");
            bccpsForTesting.add(bccpToAppendWIP);
            bccpsForTesting.add(bccpToAppendQA);

            endUserForBIE = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserForBIE);
            thisAccountWillBeDeletedAfterTests(endUserForBIE);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUserForBIE);
            topLevelAsbiepWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, endUserForBIE, "WIP");

        }
        HomePage homePage = loginPage().signIn(endUserForBIE.getLoginId(), endUserForBIE.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiepWIP);
        getDriver().manage().window().maximize();
        assertEquals("WIP", topLevelAsbiepWIP.getState());
        // TODO:
        // Can't open the context menu in a small size of the screen.
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        for(ASCCPObject asccpToAppend: asccpsForTesting){
            /**
             * It has child association to an end user ASCCP which is not in Production state
             */
            assertNotEquals("Production", asccpToAppend.getState());
            SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
            selectCCPropertyPage.selectAssociation(asccpToAppend.getDen());
        }
        for (BCCPObject bccpToAppend: bccpsForTesting){
            /**
             * It has child association to an end user BCCP which is not in Production state
             */
            assertNotEquals("Production", bccpToAppend.getState());
            SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
            selectCCPropertyPage.selectAssociation(bccpToAppend.getDen());
        }
        ACCExtensionViewEditPage.setNamespace(namespaceEU);
        ACCExtensionViewEditPage.hitUpdateButton();
        ACCExtensionViewEditPage.moveToQA();
        /**
         *  there is a corresponding UEGACC in Production state
         */
        ACCExtensionViewEditPage.moveToProduction();
        bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.openEditBIEPage(topLevelAsbiepWIP);

        for (BCCPObject bccpToAppend: bccpsForTesting) {
            WebElement node = editBIEPage.getNodeByPath(
                    "/" + asccp.getPropertyTerm() + "/Extension/" + bccpToAppend.getPropertyTerm());
            assertTrue(node.isDisplayed());
            EditBIEPage.BBIEPanel BBIEPPanel = editBIEPage.getBBIEPanel(node);
            /**
             * Assert that all options are disabled.
             */
            assertDisabled(BBIEPPanel.getNillableCheckbox());
            assertDisabled(BBIEPPanel.getUsedCheckbox());
            assertDisabled(BBIEPPanel.getCardinalityMinField());
            assertDisabled(BBIEPPanel.getCardinalityMaxField());
            assertDisabled(BBIEPPanel.getRemarkField());
            assertDisabled(BBIEPPanel.getExampleField());
            assertDisabled(BBIEPPanel.getValueConstraintSelectField());
            assertDisabled(BBIEPPanel.getValueDomainRestrictionSelectField());
            assertDisabled(BBIEPPanel.getValueDomainField());
            assertDisabled(BBIEPPanel.getContextDefinitionField());
            assertDisabled(BBIEPPanel.getAssociationDefinitionField());
            assertDisabled(BBIEPPanel.getComponentDefinitionField());
            //TODO
            // Check if Business Term functionality is enabled. Currently, it is disabled.
            assertDisabled(BBIEPPanel.getBusinessTermField());
        }
        for(ASCCPObject asccpToAppend: asccpsForTesting) {
            WebElement node = editBIEPage.getNodeByPath(
                    "/" + asccp.getPropertyTerm() + "/Extension/" + asccpToAppend.getPropertyTerm());
            assertTrue(node.isDisplayed());
            EditBIEPage.ASBIEPanel ASBIEPanel = editBIEPage.getASBIEPanel(node);

            /**
             * Assert that all options are disabled.
             */
            assertDisabled(ASBIEPanel.getNillableCheckbox());
            assertDisabled(ASBIEPanel.getUsedCheckbox());
            assertDisabled(ASBIEPanel.getCardinalityMinField());
            assertDisabled(ASBIEPanel.getCardinalityMaxField());
            assertDisabled(ASBIEPanel.getRemarkField());
            assertDisabled(ASBIEPanel.getContextDefinitionField());
            assertDisabled(ASBIEPanel.getAssociationDefinitionField());
            assertDisabled(ASBIEPanel.getComponentDefinitionField());
            assertDisabled(ASBIEPanel.getTypeDefinitionField());
            //TODO
            // Check if Business Term functionality is enabled. Currently, it is disabled.
            assertDisabled(ASBIEPanel.getBusinessTermField());
        }
    }

    @Test
    @DisplayName("TC_6_2_TA_5_2")
    public void test_TA_5_2() {
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject endUserForBIE;
        TopLevelASBIEPObject topLevelAsbiepWIP;
        ArrayList<ASCCPObject> asccpsForTesting = new ArrayList<>();
        NamespaceObject namespaceEU;
        Map<ASCCPObject, ACCObject> asccpACCMap = new HashMap<>();
        Map<ACCObject, BCCPObject> accBCCPMap = new HashMap<>();
        {
            AppUserObject endUserForCC = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserForCC);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            acc = coreComponentAPI.createRandomACC(endUserForCC, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, endUserForCC, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, endUserForCC, namespace, "Published");

            ACCObject accToAppend = coreComponentAPI.createRandomACC(endUserForCC, release, namespace, "QA");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            BCCPObject bccpToAppend = coreComponentAPI.createRandomBCCP(dataType, endUserForCC, namespace, "QA");
            BCCObject bccObject = coreComponentAPI.appendBCC(accToAppend, bccpToAppend, "QA");
            accBCCPMap.put(accToAppend, bccpToAppend);
            ASCCPObject asccpToAppendProduction = coreComponentAPI.createRandomASCCP(accToAppend, endUserForCC, namespace, "Production");
            asccpsForTesting.add(asccpToAppendProduction);
            asccpACCMap.put(asccpToAppendProduction, accToAppend);

            endUserForBIE = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserForBIE);
            thisAccountWillBeDeletedAfterTests(endUserForBIE);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUserForBIE);
            topLevelAsbiepWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, endUserForBIE, "WIP");

        }
        HomePage homePage = loginPage().signIn(endUserForBIE.getLoginId(), endUserForBIE.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiepWIP);
        getDriver().manage().window().maximize();
        assertEquals("WIP", topLevelAsbiepWIP.getState());
        // TODO:
        // Can't open the context menu in a small size of the screen.
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        for(ASCCPObject asccpToAppend: asccpsForTesting){
            /**
             * It has child association to an end user ASCCP that is in the Production state
             */
            assertEquals("Production", asccpToAppend.getState());
            /**
             * The end user ACC (of the ASCCP) is not in the Production state
             */
            ACCObject ACCAssociation = asccpACCMap.get(asccpToAppend);
            assertNotEquals("Production", ACCAssociation.getState());
            SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
            selectCCPropertyPage.selectAssociation(asccpToAppend.getDen());
        }

        ACCExtensionViewEditPage.setNamespace(namespaceEU);
        ACCExtensionViewEditPage.hitUpdateButton();
        ACCExtensionViewEditPage.moveToQA();
        /**
         *  there is a corresponding UEGACC in Production state
         */
        ACCExtensionViewEditPage.moveToProduction();
        bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.openEditBIEPage(topLevelAsbiepWIP);

        for(ASCCPObject asccpToAppend: asccpsForTesting) {
            WebElement node = editBIEPage.getNodeByPath(
                    "/" + asccp.getPropertyTerm() + "/Extension/" + asccpToAppend.getPropertyTerm());
            assertTrue(node.isDisplayed());
            EditBIEPage.ASBIEPanel ASBIEPanel = editBIEPage.getASBIEPanel(node);

            /**
             * Assert that all options are disabled.
             */
            assertDisabled(ASBIEPanel.getNillableCheckbox());
            assertDisabled(ASBIEPanel.getUsedCheckbox());
            assertDisabled(ASBIEPanel.getCardinalityMinField());
            assertDisabled(ASBIEPanel.getCardinalityMaxField());
            assertDisabled(ASBIEPanel.getRemarkField());
            assertDisabled(ASBIEPanel.getContextDefinitionField());
            assertDisabled(ASBIEPanel.getAssociationDefinitionField());
            assertDisabled(ASBIEPanel.getComponentDefinitionField());
            assertDisabled(ASBIEPanel.getTypeDefinitionField());
            //TODO
            // Check if Business Term functionality is enabled. Currently, it is disabled.
            assertDisabled(ASBIEPanel.getBusinessTermField());

            /**
             * Assert that all options for descendant nodes are also disabled
             */
            ACCObject ACCAssociation = asccpACCMap.get(asccpToAppend);
            BCCPObject bccp = accBCCPMap.get(ACCAssociation);
            node = editBIEPage.getNodeByPath(
                    "/" + asccp.getPropertyTerm() + "/Extension/" + asccpToAppend.getPropertyTerm() +"/"+bccp.getPropertyTerm());
            assertTrue(node.isDisplayed());
            EditBIEPage.BBIEPanel BBIEPPanel = editBIEPage.getBBIEPanel(node);
            /**
             * Assert that all options are disabled.
             */
            assertDisabled(BBIEPPanel.getNillableCheckbox());
            assertDisabled(BBIEPPanel.getUsedCheckbox());
            assertDisabled(BBIEPPanel.getCardinalityMinField());
            assertDisabled(BBIEPPanel.getCardinalityMaxField());
            assertDisabled(BBIEPPanel.getRemarkField());
            assertDisabled(BBIEPPanel.getExampleField());
            assertDisabled(BBIEPPanel.getValueConstraintSelectField());
            assertDisabled(BBIEPPanel.getValueDomainRestrictionSelectField());
            assertDisabled(BBIEPPanel.getValueDomainField());
            assertDisabled(BBIEPPanel.getContextDefinitionField());
            assertDisabled(BBIEPPanel.getAssociationDefinitionField());
            assertDisabled(BBIEPPanel.getComponentDefinitionField());
            //TODO
            // Check if Business Term functionality is enabled. Currently, it is disabled.
            assertDisabled(BBIEPPanel.getBusinessTermField());
        }
    }

    @Test
    @DisplayName("TC_6_2_TA_5_4")
    public void test_TA_5_4() {
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject endUserForBIE;
        TopLevelASBIEPObject topLevelAsbiepWIP;
        ArrayList<ASCCPObject> asccpsForTesting = new ArrayList<>();
        NamespaceObject namespaceEU;
        Map<ASCCPObject, ACCObject> asccpACCMap = new HashMap<>();
        Map<ACCObject, BCCPObject> accBCCPMap = new HashMap<>();
        Map<ACCObject, ASCCPObject> accASCCPPMap = new HashMap<>();
        {
            AppUserObject endUserForCC = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserForCC);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            acc = coreComponentAPI.createRandomACC(endUserForCC, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, endUserForCC, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, endUserForCC, namespace, "Published");

            /**
             * The end user ACC of the ASCCP is also in the Production state and was amended.
             */
            ACCObject accToAppend = coreComponentAPI.createRandomACC(endUserForCC, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            BCCPObject bccpToAppend = coreComponentAPI.createRandomBCCP(dataType, endUserForCC, namespace, "QA");
            coreComponentAPI.appendBCC(accToAppend, bccpToAppend, "QA");
            accBCCPMap.put(accToAppend, bccpToAppend);
            /**
             * The ACC has a child ASCC that points another end user ASCCP2 that is not in the Production state.
             */
            endUserForCC = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            ACCObject accQA = coreComponentAPI.createRandomACC(endUserForCC, release, namespace, "QA");
            ASCCPObject asccp2 = coreComponentAPI.createRandomASCCP(accQA, endUserForCC, namespace, "QA");
            coreComponentAPI.appendASCC(accToAppend, asccp2, "QA");
            accASCCPPMap.put(accToAppend, asccp2);
            /**
             * Amend the ACC that should be amended.
             */
            coreComponentAPI.createRevisedACC(accToAppend, endUserForCC, release, "WIP");
            /**
             * There is a corresponding UEGACC that has a child association to an end user ASCCP that is in the Production state
             */
            ASCCPObject asccpToAppendProduction = coreComponentAPI.createRandomASCCP(accToAppend, endUserForCC, namespace, "Production");
            asccpsForTesting.add(asccpToAppendProduction);
            asccpACCMap.put(asccpToAppendProduction, accToAppend);

            endUserForBIE = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserForBIE);
            thisAccountWillBeDeletedAfterTests(endUserForBIE);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUserForBIE);
            topLevelAsbiepWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, endUserForBIE, "WIP");

        }
        HomePage homePage = loginPage().signIn(endUserForBIE.getLoginId(), endUserForBIE.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiepWIP);
        getDriver().manage().window().maximize();
        assertEquals("WIP", topLevelAsbiepWIP.getState());
        // TODO:
        // Can't open the context menu in a small size of the screen.
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        for(ASCCPObject asccpToAppend: asccpsForTesting){
            /**
             * It has child association to an end user ASCCP that is in the Production state
             */
            assertEquals("Production", asccpToAppend.getState());
            /**
             * The end user ACC (of the ASCCP) is not in the Production state
             */
            ACCObject ACCAssociation = asccpACCMap.get(asccpToAppend);
            assertEquals("Production", ACCAssociation.getState());
            SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
            selectCCPropertyPage.selectAssociation(asccpToAppend.getDen());
        }

        ACCExtensionViewEditPage.setNamespace(namespaceEU);
        ACCExtensionViewEditPage.hitUpdateButton();
        ACCExtensionViewEditPage.moveToQA();
        /**
         *  there is a corresponding UEGACC in Production state
         */
        ACCExtensionViewEditPage.moveToProduction();

        bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.openEditBIEPage(topLevelAsbiepWIP);

        for(ASCCPObject asccpToAppend: asccpsForTesting) {
            WebElement node = editBIEPage.getNodeByPath(
                    "/" + asccp.getPropertyTerm() + "/Extension/" + asccpToAppend.getPropertyTerm());
            assertTrue(node.isDisplayed());
            EditBIEPage.ASBIEPanel ASBIEPanel = editBIEPage.getASBIEPanel(node);

            /**
             * Assert that options are enabled for appended ACC that is in the Production state.
             */
            assertEnabled(ASBIEPanel.getUsedCheckbox());
            ASBIEPanel.toggleUsed();

            assertEnabled(ASBIEPanel.getCardinalityMinField());
            assertEnabled(ASBIEPanel.getCardinalityMaxField());
            assertEnabled(ASBIEPanel.getRemarkField());
            assertEnabled(ASBIEPanel.getContextDefinitionField());
            //TODO
            // Check if Business Term functionality is enabled. Currently, it is disabled.
            assertEnabled(ASBIEPanel.getBusinessTermField());

            /**
             * Assert that all options for descendant nodes, that are not in Production state, are disabled
             */
            ACCObject ACCAssociation = asccpACCMap.get(asccpToAppend);
            BCCPObject bccpDescendant = accBCCPMap.get(ACCAssociation);
            node = editBIEPage.getNodeByPath(
                    "/" + asccp.getPropertyTerm() + "/Extension/" + asccpToAppend.getPropertyTerm() +"/"+bccpDescendant.getPropertyTerm());
            assertTrue(node.isDisplayed());
            EditBIEPage.BBIEPanel BBIEPPanel = editBIEPage.getBBIEPanel(node);
            /**
             * Assert that all options are disabled for descendant BCC not in Production state.
             */
            assertDisabled(BBIEPPanel.getNillableCheckbox());
            assertDisabled(BBIEPPanel.getUsedCheckbox());
            assertDisabled(BBIEPPanel.getCardinalityMinField());
            assertDisabled(BBIEPPanel.getCardinalityMaxField());
            assertDisabled(BBIEPPanel.getRemarkField());
            assertDisabled(BBIEPPanel.getExampleField());
            assertDisabled(BBIEPPanel.getValueConstraintSelectField());
            assertDisabled(BBIEPPanel.getValueDomainRestrictionSelectField());
            assertDisabled(BBIEPPanel.getValueDomainField());
            assertDisabled(BBIEPPanel.getContextDefinitionField());
            assertDisabled(BBIEPPanel.getAssociationDefinitionField());
            assertDisabled(BBIEPPanel.getComponentDefinitionField());
            //TODO
            // Check if Business Term functionality is enabled. Currently, it is disabled.
            assertDisabled(BBIEPPanel.getBusinessTermField());

            ASCCPObject asccpDescendant = accASCCPPMap.get(ACCAssociation);
            node = editBIEPage.getNodeByPath(
                    "/" + asccp.getPropertyTerm() + "/Extension/" + asccpToAppend.getPropertyTerm() +"/"+asccpDescendant.getPropertyTerm());
            assertTrue(node.isDisplayed());
            ASBIEPanel = editBIEPage.getASBIEPanel(node);

            /**
             * Assert that all options are disabled for descendant ASCC not in Production state.
             */
            assertDisabled(ASBIEPanel.getNillableCheckbox());
            assertDisabled(ASBIEPanel.getUsedCheckbox());
            assertDisabled(ASBIEPanel.getCardinalityMinField());
            assertDisabled(ASBIEPanel.getCardinalityMaxField());
            assertDisabled(ASBIEPanel.getRemarkField());
            assertDisabled(ASBIEPanel.getContextDefinitionField());
            assertDisabled(ASBIEPanel.getAssociationDefinitionField());
            assertDisabled(ASBIEPanel.getComponentDefinitionField());
            assertDisabled(ASBIEPanel.getTypeDefinitionField());
            //TODO
            // Check if Business Term functionality is enabled. Currently, it is disabled.
            assertDisabled(ASBIEPanel.getBusinessTermField());

        }
    }

    @Test
    @DisplayName("TC_6_2_TA_5_5")
    public void test_TA_5_5() {
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject endUserForBIE;
        TopLevelASBIEPObject topLevelAsbiepWIP;
        ArrayList<ASCCPObject> asccpsForTesting = new ArrayList<>();
        NamespaceObject namespaceEU;
        Map<ASCCPObject, ACCObject> asccpACCMap = new HashMap<>();
        Map<ACCObject, BCCPObject> accBCCPMap = new HashMap<>();
        Map<ACCObject, ASCCPObject> accASCCPPMap = new HashMap<>();
        {
            AppUserObject endUserForCC = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserForCC);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            acc = coreComponentAPI.createRandomACC(endUserForCC, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, endUserForCC, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, endUserForCC, namespace, "Published");

            /**
             * The end user ACC of the ASCCP has a Group component type and is NOT in the Production state.
             */
            ACCObject accToAppend = coreComponentAPI.createRandomACCSemanticGroupType(endUserForCC, release, namespace, "QA");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            BCCPObject bccpToAppend = coreComponentAPI.createRandomBCCP(dataType, endUserForCC, namespace, "QA");
            coreComponentAPI.appendBCC(accToAppend, bccpToAppend, "QA");
            accBCCPMap.put(accToAppend, bccpToAppend);

            endUserForCC = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            ACCObject accQA = coreComponentAPI.createRandomACC(endUserForCC, release, namespace, "QA");
            ASCCPObject asccp2 = coreComponentAPI.createRandomASCCP(accQA, endUserForCC, namespace, "QA");
            coreComponentAPI.appendASCC(accToAppend, asccp2, "QA");
            accASCCPPMap.put(accToAppend, asccp2);

            /**
             * There is a corresponding UEGACC that has a child association to an end user ASCCP that is in the Production state
             */
            ASCCPObject asccpToAppendProduction = coreComponentAPI.createRandomASCCP(accToAppend, endUserForCC, namespace, "Production");
            asccpsForTesting.add(asccpToAppendProduction);
            asccpACCMap.put(asccpToAppendProduction, accToAppend);

            endUserForBIE = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserForBIE);
            thisAccountWillBeDeletedAfterTests(endUserForBIE);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUserForBIE);
            topLevelAsbiepWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, endUserForBIE, "WIP");

        }
        HomePage homePage = loginPage().signIn(endUserForBIE.getLoginId(), endUserForBIE.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiepWIP);
        getDriver().manage().window().maximize();
        assertEquals("WIP", topLevelAsbiepWIP.getState());
        // TODO:
        // Can't open the context menu in a small size of the screen.
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        for(ASCCPObject asccpToAppend: asccpsForTesting){
            /**
             * It has child association to an end user ASCCP that is in the Production state
             */
            assertEquals("Production", asccpToAppend.getState());
            /**
             * The end user ACC (of the ASCCP) is not in the Production state
             */
            ACCObject ACCAssociation = asccpACCMap.get(asccpToAppend);
            assertNotEquals("Production", ACCAssociation.getState());
            SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
            selectCCPropertyPage.selectAssociation(asccpToAppend.getDen());
        }

        ACCExtensionViewEditPage.setNamespace(namespaceEU);
        ACCExtensionViewEditPage.hitUpdateButton();
        ACCExtensionViewEditPage.moveToQA();
        /**
         *  there is a corresponding UEGACC in Production state
         */
        ACCExtensionViewEditPage.moveToProduction();

        bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.openEditBIEPage(topLevelAsbiepWIP);

        for(ASCCPObject asccpToAppend: asccpsForTesting) {

            /**
             * Either NO children of that group ACC shall be visible, or all children shall be grey out and uneditable.
             */

            ACCObject ACCAssociation = asccpACCMap.get(asccpToAppend);
            BCCPObject bccpDescendant = accBCCPMap.get(ACCAssociation);
            WebElement node = editBIEPage.getNodeByPath(
                    "/" + asccp.getPropertyTerm() + "/Extension/" + bccpDescendant.getPropertyTerm());
            assertTrue(node.isDisplayed());
            EditBIEPage.BBIEPanel BBIEPPanel = editBIEPage.getBBIEPanel(node);
            /**
             * Assert that all options are disabled for descendant BCC not in Production state.
             */
            assertDisabled(BBIEPPanel.getNillableCheckbox());
            assertDisabled(BBIEPPanel.getUsedCheckbox());
            assertDisabled(BBIEPPanel.getCardinalityMinField());
            assertDisabled(BBIEPPanel.getCardinalityMaxField());
            assertDisabled(BBIEPPanel.getRemarkField());
            assertDisabled(BBIEPPanel.getExampleField());
            assertDisabled(BBIEPPanel.getValueConstraintSelectField());
            assertDisabled(BBIEPPanel.getValueDomainRestrictionSelectField());
            assertDisabled(BBIEPPanel.getValueDomainField());
            assertDisabled(BBIEPPanel.getContextDefinitionField());
            assertDisabled(BBIEPPanel.getAssociationDefinitionField());
            assertDisabled(BBIEPPanel.getComponentDefinitionField());
            //TODO
            // Check if Business Term functionality is enabled. Currently, it is disabled.
            assertDisabled(BBIEPPanel.getBusinessTermField());

            ASCCPObject asccpDescendant = accASCCPPMap.get(ACCAssociation);
            node = editBIEPage.getNodeByPath(
                    "/" + asccp.getPropertyTerm() + "/Extension/" +asccpDescendant.getPropertyTerm());
            assertTrue(node.isDisplayed());
            EditBIEPage.ASBIEPanel ASBIEPanel = editBIEPage.getASBIEPanel(node);

            /**
             * Assert that all options are disabled for descendant ASCC not in Production state.
             */
            assertDisabled(ASBIEPanel.getNillableCheckbox());
            assertDisabled(ASBIEPanel.getUsedCheckbox());
            assertDisabled(ASBIEPanel.getCardinalityMinField());
            assertDisabled(ASBIEPanel.getCardinalityMaxField());
            assertDisabled(ASBIEPanel.getRemarkField());
            assertDisabled(ASBIEPanel.getContextDefinitionField());
            assertDisabled(ASBIEPanel.getAssociationDefinitionField());
            assertDisabled(ASBIEPanel.getComponentDefinitionField());
            assertDisabled(ASBIEPanel.getTypeDefinitionField());
            //TODO
            // Check if Business Term functionality is enabled. Currently, it is disabled.
            assertDisabled(ASBIEPanel.getBusinessTermField());
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
