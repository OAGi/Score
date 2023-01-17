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
import org.oagi.score.e2e.page.bie.CreateBIEForSelectBusinessContextsPage;
import org.oagi.score.e2e.page.bie.CreateBIEForSelectTopLevelConceptPage;
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
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject usera;
        NamespaceObject useraNamespace;
        ArrayList<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, developer, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            useraNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
            thisAccountWillBeDeletedAfterTests(usera);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");
            biesForTesting.add(useraBIEWIP);

            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "Production");
            biesForTesting.add(useraBIEProduction);

            TopLevelASBIEPObject useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "QA");
            biesForTesting.add(useraBIEQA);

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            BIEMenu bieMenu = homePage.getBIEMenu();
            ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
            EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
            getDriver().manage().window().maximize();
            if (topLevelAsbiep.getState().equals("WIP")) {
                editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
                String revision = getText(editBIEPage.getRevisionField());
                assertEquals("1", revision);
                editBIEPage.setNamespace(useraNamespace.getUri());
                editBIEPage.hitUpdateButton();
                editBIEPage.moveToQA();
                editBIEPage.moveToProduction();
                bieMenu.openViewEditBIESubMenu();
                viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
                editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
                revision = getText(editBIEPage.getRevisionField());
                assertEquals("2", revision);
            } else {
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
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject usera;
        AppUserObject userb;
        TopLevelASBIEPObject useraBIE;
        TopLevelASBIEPObject userbBIE;
        NamespaceObject useraNamespace;
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, developer, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            useraNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
            thisAccountWillBeDeletedAfterTests(usera);

            BusinessContextObject contextFirstUser = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            useraBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(contextFirstUser), asccp, usera, "WIP");

            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            BusinessContextObject contextSecondUser = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            userbBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(contextSecondUser), asccp, userb, "WIP");

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIE);
        getDriver().manage().window().maximize();
        assertEquals("WIP", useraBIE.getState());
        editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        editBIEPage.setNamespace(useraNamespace.getUri());
        editBIEPage.hitUpdateButton();
        editBIEPage.moveToQA();
        homePage.logout();
        loginPage().signIn(userb.getLoginId(), userb.getPassword());
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.openEditBIEPage(userbBIE);
        assertEquals("WIP", userbBIE.getState());
        editBIEPage.getExtendBIELocallyOptionForNode("/" + asccp.getPropertyTerm() + "/Extension");
        /**
         * Display a dialog indicating “The core component is being extended by “ + [the owner of the UEGACC] or similar.
         */
        assertEquals("Another user is working on the extension.",
                editBIEPage.getAtentionDialogMessage());
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.continueToExtendBIEOnNode();
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
        assertEquals(usera.getLoginId(), ACCExtensionViewEditPage.getOwnerFieldValue());
        assertDisabled(ACCExtensionViewEditPage.getDefinitionField());
        assertDisabled(ACCExtensionViewEditPage.getObjectClassTermField());
        assertDisabled(ACCExtensionViewEditPage.getDefinitionSourceField());
        switchToMainTab(getDriver());
        homePage.logout();

        loginPage().signIn(usera.getLoginId(), usera.getPassword());
        bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.openEditBIEPage(useraBIE);
        editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        editBIEPage.backToWIP();
        homePage.logout();

        loginPage().signIn(userb.getLoginId(), userb.getPassword());
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.openEditBIEPage(userbBIE);
        editBIEPage.getExtendBIELocallyOptionForNode("/" + asccp.getPropertyTerm() + "/Extension");
        assertEquals("Editing extension already exist.", getSnackBarMessage(getDriver()));
    }

    @Test
    @DisplayName("TC_6_2_TA_3")
    public void test_TA_3() {
        ASCCPObject asccp;
        AppUserObject usera;
        ArrayList<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        NamespaceObject useraNamespace;
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, developer, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);
            useraNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            TopLevelASBIEPObject topLevelAsbiepWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");
            biesForTesting.add(topLevelAsbiepWIP);

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
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

            editBIEPage.setNamespace(useraNamespace.getUri());

            assertDoesNotThrow(() -> {
                editBIEPage.hitUpdateButton();
            });

            editBIEPage.moveToQA();
            bieMenu.openViewEditBIESubMenu();
            viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
            editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");

            assertDisabled(editBIEPage.getSourceDefinitionField());
            assertDisabled(editBIEPage.getDefinitionField());

            assertDoesNotThrow(() -> {
                editBIEPage.backToWIP();
            });
            assertDoesNotThrow(() -> {
                editBIEPage.moveToQA();
            });
            assertDoesNotThrow(() -> {
                editBIEPage.moveToProduction();
            });

        }
    }

    @Test
    @DisplayName("TC_6_2_TA_4")
    public void test_TA_4() {
        ASCCPObject asccp;
        AppUserObject usera;
        TopLevelASBIEPObject useraBIEWIP;
        ASCCPObject asccpToAppend;
        BCCPObject bccpToAppend;
        ACCObject accToAppend;
        Map<ACCObject, BCCPObject> accBCCPMap = new HashMap<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, developer, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");

            accToAppend = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            BCCPObject bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
            coreComponentAPI.appendBCC(accToAppend, bccp, "Published");
            accBCCPMap.put(accToAppend, bccp);
            asccpToAppend = coreComponentAPI.createRandomASCCP(accToAppend, developer, namespace, "Published");
            bccpToAppend = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
        getDriver().manage().window().maximize();
        assertEquals("WIP", useraBIEWIP.getState());
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        // TODO:
        // Can't open the context menu in a small size of the screen.
        SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
        selectCCPropertyPage.selectAssociation(asccpToAppend.getDen());
        ACCExtensionViewEditPage.appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
        selectCCPropertyPage.selectAssociation(bccpToAppend.getDen());
        bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.openEditBIEPage(useraBIEWIP);

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

        node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/Extension/" + asccpToAppend.getPropertyTerm() + "/" +
                        accBCCPMap.get(accToAppend).getPropertyTerm());
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

        node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/Extension/" + bccpToAppend.getPropertyTerm());
        assertTrue(node.isDisplayed());
        BBIEPPanel = editBIEPage.getBBIEPanel(node);
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
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject usera;
        TopLevelASBIEPObject useraBIEWIP;
        ArrayList<ASCCPObject> asccpsForTesting = new ArrayList<>();
        ArrayList<BCCPObject> bccpsForTesting = new ArrayList<>();
        NamespaceObject namespaceEU;
        Map<ACCObject, BCCPObject> accBCCPMap = new HashMap<>();
        Map<ASCCPObject, ACCObject> ASCCPassociatedACC = new HashMap<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            AppUserObject endUserForCC = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserForCC);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            acc = coreComponentAPI.createRandomACC(endUserForCC, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, endUserForCC, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, endUserForCC, namespace, "Published");

            ACCObject accToAppend = coreComponentAPI.createRandomACC(endUserForCC, release, namespace, "Published");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            BCCPObject bccp = coreComponentAPI.createRandomBCCP(dataType, endUserForCC, namespace, "Published");
            coreComponentAPI.appendBCC(accToAppend, bccp, "Published");
            accBCCPMap.put(accToAppend, bccp);
            ASCCPObject asccpToAppendWIP = coreComponentAPI.createRandomASCCP(accToAppend, endUserForCC, namespace, "WIP");
            ASCCPassociatedACC.put(asccpToAppendWIP, accToAppend);

            accToAppend = coreComponentAPI.createRandomACC(endUserForCC, release, namespace, "Published");
            dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, endUserForCC, namespace, "Published");
            coreComponentAPI.appendBCC(accToAppend, bccp, "Published");
            accBCCPMap.put(accToAppend, bccp);
            ASCCPObject asccpToAppendQA = coreComponentAPI.createRandomASCCP(accToAppend, endUserForCC, namespace, "QA");
            ASCCPassociatedACC.put(asccpToAppendQA, accToAppend);

            asccpsForTesting.add(asccpToAppendWIP);
            asccpsForTesting.add(asccpToAppendQA);

            dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            BCCPObject bccpToAppendWIP = coreComponentAPI.createRandomBCCP(dataType, endUserForCC, namespace, "WIP");
            BCCPObject bccpToAppendQA = coreComponentAPI.createRandomBCCP(dataType, endUserForCC, namespace, "QA");
            bccpsForTesting.add(bccpToAppendWIP);
            bccpsForTesting.add(bccpToAppendQA);

            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
            thisAccountWillBeDeletedAfterTests(usera);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
        getDriver().manage().window().maximize();
        assertEquals("WIP", useraBIEWIP.getState());
        // TODO:
        // Can't open the context menu in a small size of the screen.
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        for (ASCCPObject asccpToAppend : asccpsForTesting) {
            /**
             * It has child association to an end user ASCCP which is not in Production state
             */
            assertNotEquals("Production", asccpToAppend.getState());
            SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
            selectCCPropertyPage.selectAssociation(asccpToAppend.getDen());
        }
        for (BCCPObject bccpToAppend : bccpsForTesting) {
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
        viewEditBIEPage.openEditBIEPage(useraBIEWIP);

        for (BCCPObject bccpToAppend : bccpsForTesting) {
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
        for (ASCCPObject asccpToAppend : asccpsForTesting) {
            ACCObject associatedACC = ASCCPassociatedACC.get(asccpToAppend);
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

            node = editBIEPage.getNodeByPath(
                    "/" + asccp.getPropertyTerm() + "/Extension/" + asccpToAppend.getPropertyTerm() + "/" +
                            accBCCPMap.get(associatedACC).getPropertyTerm());
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
    @DisplayName("TC_6_2_TA_5_2")
    public void test_TA_5_2() {
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject usera;
        TopLevelASBIEPObject useraBIEWIP;
        ArrayList<ASCCPObject> asccpsForTesting = new ArrayList<>();
        NamespaceObject namespaceEU;
        Map<ASCCPObject, ACCObject> asccpACCMap = new HashMap<>();
        Map<ACCObject, BCCPObject> accBCCPMap = new HashMap<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
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
            coreComponentAPI.appendBCC(accToAppend, bccpToAppend, "QA");
            accBCCPMap.put(accToAppend, bccpToAppend);
            ASCCPObject asccpToAppendProduction = coreComponentAPI.createRandomASCCP(accToAppend, endUserForCC, namespace, "Production");
            asccpsForTesting.add(asccpToAppendProduction);
            asccpACCMap.put(asccpToAppendProduction, accToAppend);

            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
            thisAccountWillBeDeletedAfterTests(usera);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
        getDriver().manage().window().maximize();
        assertEquals("WIP", useraBIEWIP.getState());
        // TODO:
        // Can't open the context menu in a small size of the screen.
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        for (ASCCPObject asccpToAppend : asccpsForTesting) {
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
        viewEditBIEPage.openEditBIEPage(useraBIEWIP);

        for (ASCCPObject asccpToAppend : asccpsForTesting) {
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
                    "/" + asccp.getPropertyTerm() + "/Extension/" + asccpToAppend.getPropertyTerm() + "/" + bccp.getPropertyTerm());
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
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject usera;
        TopLevelASBIEPObject useraBIEWIP;
        ArrayList<ASCCPObject> asccpsForTesting = new ArrayList<>();
        NamespaceObject namespaceEU;
        Map<ASCCPObject, ACCObject> asccpACCMap = new HashMap<>();
        Map<ACCObject, BCCPObject> accBCCPMap = new HashMap<>();
        Map<ACCObject, ASCCPObject> accASCCPPMap = new HashMap<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
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
            thisAccountWillBeDeletedAfterTests(endUserForCC);
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

            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
            thisAccountWillBeDeletedAfterTests(usera);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
        getDriver().manage().window().maximize();
        assertEquals("WIP", useraBIEWIP.getState());
        // TODO:
        // Can't open the context menu in a small size of the screen.
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        for (ASCCPObject asccpToAppend : asccpsForTesting) {
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
        viewEditBIEPage.openEditBIEPage(useraBIEWIP);

        for (ASCCPObject asccpToAppend : asccpsForTesting) {
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
                    "/" + asccp.getPropertyTerm() + "/Extension/" + asccpToAppend.getPropertyTerm() + "/" + bccpDescendant.getPropertyTerm());
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
                    "/" + asccp.getPropertyTerm() + "/Extension/" + asccpToAppend.getPropertyTerm() + "/" + asccpDescendant.getPropertyTerm());
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
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject usera;
        TopLevelASBIEPObject useraBIEWIP;
        ArrayList<ASCCPObject> asccpsForTesting = new ArrayList<>();
        NamespaceObject namespaceEU;
        Map<ASCCPObject, ACCObject> asccpACCMap = new HashMap<>();
        Map<ACCObject, BCCPObject> accBCCPMap = new HashMap<>();
        Map<ACCObject, ASCCPObject> accASCCPPMap = new HashMap<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
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
            thisAccountWillBeDeletedAfterTests(endUserForCC);
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

            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
            thisAccountWillBeDeletedAfterTests(usera);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
        getDriver().manage().window().maximize();
        assertEquals("WIP", useraBIEWIP.getState());
        // TODO:
        // Can't open the context menu in a small size of the screen.
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        for (ASCCPObject asccpToAppend : asccpsForTesting) {
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
        viewEditBIEPage.openEditBIEPage(useraBIEWIP);

        for (ASCCPObject asccpToAppend : asccpsForTesting) {

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
                    "/" + asccp.getPropertyTerm() + "/Extension/" + asccpDescendant.getPropertyTerm());
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

    @Test
    @DisplayName("TC_6_2_TA_7_1")
    public void test_TA_7_1() {
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject usera;
        NamespaceObject useraNamespace;
        ArrayList<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, developer, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            useraNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
            thisAccountWillBeDeletedAfterTests(usera);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");
            biesForTesting.add(useraBIEWIP);

            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "Production");
            biesForTesting.add(useraBIEProduction);

            TopLevelASBIEPObject useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "QA");
            biesForTesting.add(useraBIEQA);

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            BIEMenu bieMenu = homePage.getBIEMenu();
            ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
            EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
            getDriver().manage().window().maximize();
            if (topLevelAsbiep.getState().equals("WIP")) {
                editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
                String revision = getText(editBIEPage.getRevisionField());
                assertEquals("1", revision);
                editBIEPage.setNamespace(useraNamespace.getUri());
                editBIEPage.hitUpdateButton();
                editBIEPage.moveToQA();
                editBIEPage.moveToProduction();
                bieMenu.openViewEditBIESubMenu();
                viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
                editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
                revision = getText(editBIEPage.getRevisionField());
                assertEquals("2", revision);
            } else {
                assertThrows(TimeoutException.class, () -> {
                    editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
                });
                escape(getDriver());
            }
        }
    }

    @Test
    @DisplayName("TC_6_2_TA_7_2")
    public void test_TA_7_2() {
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject usera;
        AppUserObject userb;
        TopLevelASBIEPObject useraBIE;
        TopLevelASBIEPObject userbBIE;
        NamespaceObject useraNamespace;
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, developer, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            useraNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
            thisAccountWillBeDeletedAfterTests(usera);

            BusinessContextObject contextFirstUser = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            useraBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(contextFirstUser), asccp, usera, "WIP");

            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            BusinessContextObject contextSecondUser = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            userbBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(contextSecondUser), asccp, userb, "WIP");

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIE);
        getDriver().manage().window().maximize();
        assertEquals("WIP", useraBIE.getState());
        editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        editBIEPage.setNamespace(useraNamespace.getUri());
        editBIEPage.hitUpdateButton();
        editBIEPage.moveToQA();
        homePage.logout();
        loginPage().signIn(userb.getLoginId(), userb.getPassword());
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.openEditBIEPage(userbBIE);
        assertEquals("WIP", userbBIE.getState());
        editBIEPage.getExtendBIEGloballyOptionForNode("/" + asccp.getPropertyTerm() + "/Extension");
        /**
         * Display a dialog indicating “The core component is being extended by “ + [the owner of the UEGACC] or similar.
         */
        assertEquals("Another user is working on the extension.",
                editBIEPage.getAtentionDialogMessage());
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.continueToExtendBIEOnNode();
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
        assertEquals(usera.getLoginId(), ACCExtensionViewEditPage.getOwnerFieldValue());
        assertDisabled(ACCExtensionViewEditPage.getDefinitionField());
        assertDisabled(ACCExtensionViewEditPage.getObjectClassTermField());
        assertDisabled(ACCExtensionViewEditPage.getDefinitionSourceField());
        switchToMainTab(getDriver());
        homePage.logout();

        loginPage().signIn(usera.getLoginId(), usera.getPassword());
        bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.openEditBIEPage(useraBIE);
        editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        editBIEPage.backToWIP();
        homePage.logout();

        loginPage().signIn(userb.getLoginId(), userb.getPassword());
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.openEditBIEPage(userbBIE);
        editBIEPage.getExtendBIEGloballyOptionForNode("/" + asccp.getPropertyTerm() + "/Extension");
        assertEquals("Editing extension already exist.", getSnackBarMessage(getDriver()));
    }

    @Test
    @DisplayName("TC_6_2_TA_7_3")
    public void test_TA_7_3() {
        ASCCPObject asccp;
        AppUserObject usera;
        ArrayList<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        NamespaceObject useraNamespace;
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, developer, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);
            useraNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            TopLevelASBIEPObject topLevelAsbiepWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");
            biesForTesting.add(topLevelAsbiepWIP);

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            BIEMenu bieMenu = homePage.getBIEMenu();
            ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
            EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
            getDriver().manage().window().maximize();
            assertEquals("WIP", topLevelAsbiep.getState());
            editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
            bieMenu.openViewEditBIESubMenu();
            viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
            editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");

            assertEnabled(editBIEPage.getSourceDefinitionField());
            assertEnabled(editBIEPage.getDefinitionField());

            editBIEPage.setNamespace(useraNamespace.getUri());

            assertDoesNotThrow(() -> {
                editBIEPage.hitUpdateButton();
            });

            editBIEPage.moveToQA();
            bieMenu.openViewEditBIESubMenu();
            viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
            editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");

            assertDisabled(editBIEPage.getSourceDefinitionField());
            assertDisabled(editBIEPage.getDefinitionField());

            assertDoesNotThrow(() -> {
                editBIEPage.backToWIP();
            });
            assertDoesNotThrow(() -> {
                editBIEPage.moveToQA();
            });
            assertDoesNotThrow(() -> {
                editBIEPage.moveToProduction();
            });

        }
    }

    @Test
    @DisplayName("TC_6_2_TA_7_4")
    public void test_TA_7_4() {
        ASCCPObject asccp;
        AppUserObject usera;
        TopLevelASBIEPObject useraBIEWIP;
        BCCPObject bccpToAppend;
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, developer, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");

            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccpToAppend = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
        getDriver().manage().window().maximize();
        assertEquals("WIP", useraBIEWIP.getState());
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        // TODO:
        // Can't open the context menu in a small size of the screen.
        SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.appendPropertyAtLast("/All User Extension Group. Details");

        /**
         *  ASCCP cannot be appended to the global extension.
         */
        assertThrows(NoSuchElementException.class, () -> {
            selectCCPropertyPage.setAssociationType("ASCCP");
        });
        escape(getDriver());
        selectCCPropertyPage.selectAssociation(bccpToAppend.getDen());
        bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.openEditBIEPage(useraBIEWIP);

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

    @Test
    @DisplayName("TC_6_2_TA_7_5_1_and_TC_6_2_TA_7_5_3")
    public void test_TA_7_5_1_and_TA_7_5_3() {
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject usera;
        TopLevelASBIEPObject useraBIEWIP;
        ArrayList<ASCCPObject> asccpsForTesting = new ArrayList<>();
        ArrayList<BCCPObject> bccpsForTesting = new ArrayList<>();
        NamespaceObject namespaceEU;
        Map<ACCObject, BCCPObject> accBCCPMap = new HashMap<>();
        Map<ASCCPObject, ACCObject> ASCCPassociatedACC = new HashMap<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            AppUserObject endUserForCC = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserForCC);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            acc = coreComponentAPI.createRandomACC(endUserForCC, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, endUserForCC, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, endUserForCC, namespace, "Published");

            ACCObject accToAppend = coreComponentAPI.createRandomACC(endUserForCC, release, namespace, "Published");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            BCCPObject bccp = coreComponentAPI.createRandomBCCP(dataType, endUserForCC, namespace, "Published");
            coreComponentAPI.appendBCC(accToAppend, bccp, "Published");
            accBCCPMap.put(accToAppend, bccp);
            ASCCPObject asccpToAppendWIP = coreComponentAPI.createRandomASCCP(accToAppend, endUserForCC, namespace, "WIP");
            ASCCPassociatedACC.put(asccpToAppendWIP, accToAppend);

            accToAppend = coreComponentAPI.createRandomACC(endUserForCC, release, namespace, "Published");
            dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, endUserForCC, namespace, "Published");
            coreComponentAPI.appendBCC(accToAppend, bccp, "Published");
            accBCCPMap.put(accToAppend, bccp);
            ASCCPObject asccpToAppendQA = coreComponentAPI.createRandomASCCP(accToAppend, endUserForCC, namespace, "QA");
            ASCCPassociatedACC.put(asccpToAppendQA, accToAppend);

            asccpsForTesting.add(asccpToAppendWIP);
            asccpsForTesting.add(asccpToAppendQA);

            dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            BCCPObject bccpToAppendWIP = coreComponentAPI.createRandomBCCP(dataType, endUserForCC, namespace, "WIP");
            BCCPObject bccpToAppendQA = coreComponentAPI.createRandomBCCP(dataType, endUserForCC, namespace, "QA");
            bccpsForTesting.add(bccpToAppendWIP);
            bccpsForTesting.add(bccpToAppendQA);

            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
            thisAccountWillBeDeletedAfterTests(usera);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
        getDriver().manage().window().maximize();
        assertEquals("WIP", useraBIEWIP.getState());
        // TODO:
        // Can't open the context menu in a small size of the screen.
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        for (ASCCPObject asccpToAppend : asccpsForTesting) {
            /**
             * It has child association to an end user ASCCP which is not in Production state
             */
            assertNotEquals("Production", asccpToAppend.getState());
            SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.appendPropertyAtLast("/All User Extension Group. Details");
            /**
             *  ASCCP cannot be appended to the global extension.
             */
            assertThrows(NoSuchElementException.class, () -> {
                selectCCPropertyPage.setAssociationType("ASCCP");
            });
            escape(getDriver());
            assertThrows(NoSuchElementException.class, () -> {
                selectCCPropertyPage.selectAssociation(asccpToAppend.getDen());
            });
            selectCCPropertyPage.hitCancelButton();
        }
        for (BCCPObject bccpToAppend : bccpsForTesting) {
            /**
             * It has child association to an end user BCCP which is not in Production state
             */
            assertNotEquals("Production", bccpToAppend.getState());
            SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.appendPropertyAtLast("/All User Extension Group. Details");
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
        viewEditBIEPage.openEditBIEPage(useraBIEWIP);

        for (BCCPObject bccpToAppend : bccpsForTesting) {
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
        for (ASCCPObject asccpToAppend : asccpsForTesting) {
            assertThrows(TimeoutException.class, () -> {
                WebElement node = editBIEPage.getNodeByPath(
                        "/" + asccp.getPropertyTerm() + "/Extension/" + asccpToAppend.getPropertyTerm());
            });

        }
    }

    @Test
    @DisplayName("TC_6_2_TA_7_5_2")
    public void test_TA_7_5_2() {
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject usera;
        TopLevelASBIEPObject useraBIEWIP;
        ArrayList<ASCCPObject> asccpsForTesting = new ArrayList<>();
        NamespaceObject namespaceEU;
        Map<ASCCPObject, ACCObject> asccpACCMap = new HashMap<>();
        Map<ACCObject, BCCPObject> accBCCPMap = new HashMap<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
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
            coreComponentAPI.appendBCC(accToAppend, bccpToAppend, "QA");
            accBCCPMap.put(accToAppend, bccpToAppend);
            ASCCPObject asccpToAppendProduction = coreComponentAPI.createRandomASCCP(accToAppend, endUserForCC, namespace, "Production");
            asccpsForTesting.add(asccpToAppendProduction);
            asccpACCMap.put(asccpToAppendProduction, accToAppend);

            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
            thisAccountWillBeDeletedAfterTests(usera);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
        getDriver().manage().window().maximize();
        assertEquals("WIP", useraBIEWIP.getState());
        // TODO:
        // Can't open the context menu in a small size of the screen.
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        for (ASCCPObject asccpToAppend : asccpsForTesting) {
            /**
             * It has child association to an end user ASCCP that is in the Production state
             */
            assertEquals("Production", asccpToAppend.getState());
            /**
             * The end user ACC (of the ASCCP) is not in the Production state
             */
            ACCObject ACCAssociation = asccpACCMap.get(asccpToAppend);
            assertNotEquals("Production", ACCAssociation.getState());
            SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.appendPropertyAtLast("/All User Extension Group. Details");

            /**
             *  ASCCP cannot be appended to the global extension.
             */
            assertThrows(NoSuchElementException.class, () -> {
                selectCCPropertyPage.setAssociationType("ASCCP");
            });
            escape(getDriver());
            assertThrows(NoSuchElementException.class, () -> {
                selectCCPropertyPage.selectAssociation(asccpToAppend.getDen());
            });
            selectCCPropertyPage.hitCancelButton();
        }

        ACCExtensionViewEditPage.setNamespace(namespaceEU);
        ACCExtensionViewEditPage.hitUpdateButton();
        ACCExtensionViewEditPage.moveToQA();
        /**
         *  there is a corresponding UEGACC in Production state
         */
        ACCExtensionViewEditPage.moveToProduction();
        bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.openEditBIEPage(useraBIEWIP);

        for (ASCCPObject asccpToAppend : asccpsForTesting) {
            assertThrows(TimeoutException.class, () -> {
                WebElement node = editBIEPage.getNodeByPath(
                        "/" + asccp.getPropertyTerm() + "/Extension/" + asccpToAppend.getPropertyTerm());
            });

        }
    }

    @Test
    @DisplayName("TC_6_2_TA_7_5_4")
    public void test_TA_7_5_4() {
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject usera;
        TopLevelASBIEPObject useraBIEWIP;
        ArrayList<ASCCPObject> asccpsForTesting = new ArrayList<>();
        NamespaceObject namespaceEU;
        Map<ASCCPObject, ACCObject> asccpACCMap = new HashMap<>();
        Map<ACCObject, BCCPObject> accBCCPMap = new HashMap<>();
        Map<ACCObject, ASCCPObject> accASCCPPMap = new HashMap<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
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
            thisAccountWillBeDeletedAfterTests(endUserForCC);
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

            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
            thisAccountWillBeDeletedAfterTests(usera);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
        getDriver().manage().window().maximize();
        assertEquals("WIP", useraBIEWIP.getState());
        // TODO:
        // Can't open the context menu in a small size of the screen.
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        for (ASCCPObject asccpToAppend : asccpsForTesting) {
            /**
             * It has child association to an end user ASCCP that is in the Production state
             */
            assertEquals("Production", asccpToAppend.getState());
            /**
             * The end user ACC (of the ASCCP) is not in the Production state
             */
            ACCObject ACCAssociation = asccpACCMap.get(asccpToAppend);
            assertEquals("Production", ACCAssociation.getState());
            SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.appendPropertyAtLast("/All User Extension Group. Details");
            /**
             *  ASCCP cannot be appended to the global extension.
             */
            assertThrows(NoSuchElementException.class, () -> {
                selectCCPropertyPage.setAssociationType("ASCCP");
            });
            escape(getDriver());
            assertThrows(NoSuchElementException.class, () -> {
                selectCCPropertyPage.selectAssociation(asccpToAppend.getDen());
            });
            selectCCPropertyPage.hitCancelButton();
        }

        ACCExtensionViewEditPage.setNamespace(namespaceEU);
        ACCExtensionViewEditPage.hitUpdateButton();
        ACCExtensionViewEditPage.moveToQA();
        /**
         *  there is a corresponding UEGACC in Production state
         */
        ACCExtensionViewEditPage.moveToProduction();

        bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.openEditBIEPage(useraBIEWIP);

        for (ASCCPObject asccpToAppend : asccpsForTesting) {
            assertThrows(TimeoutException.class, () -> {
                WebElement node = editBIEPage.getNodeByPath(
                        "/" + asccp.getPropertyTerm() + "/Extension/" + asccpToAppend.getPropertyTerm());
            });
        }
    }

    @Test
    @DisplayName("TC_6_2_TA_7_5_5")
    public void test_TA_7_5_5() {
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject usera;
        TopLevelASBIEPObject useraBIEWIP;
        ArrayList<ASCCPObject> asccpsForTesting = new ArrayList<>();
        NamespaceObject namespaceEU;
        Map<ASCCPObject, ACCObject> asccpACCMap = new HashMap<>();
        Map<ACCObject, BCCPObject> accBCCPMap = new HashMap<>();
        Map<ACCObject, ASCCPObject> accASCCPPMap = new HashMap<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
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
            thisAccountWillBeDeletedAfterTests(endUserForCC);
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

            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
            thisAccountWillBeDeletedAfterTests(usera);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
        getDriver().manage().window().maximize();
        assertEquals("WIP", useraBIEWIP.getState());
        // TODO:
        // Can't open the context menu in a small size of the screen.
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        for (ASCCPObject asccpToAppend : asccpsForTesting) {
            /**
             * It has child association to an end user ASCCP that is in the Production state
             */
            assertEquals("Production", asccpToAppend.getState());
            /**
             * The end user ACC (of the ASCCP) is not in the Production state
             */
            ACCObject ACCAssociation = asccpACCMap.get(asccpToAppend);
            assertNotEquals("Production", ACCAssociation.getState());
            SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.appendPropertyAtLast("/All User Extension Group. Details");
            /**
             *  ASCCP cannot be appended to the global extension.
             */
            assertThrows(NoSuchElementException.class, () -> {
                selectCCPropertyPage.setAssociationType("ASCCP");
            });
            escape(getDriver());
            assertThrows(NoSuchElementException.class, () -> {
                selectCCPropertyPage.selectAssociation(asccpToAppend.getDen());
            });
            selectCCPropertyPage.hitCancelButton();
        }

        ACCExtensionViewEditPage.setNamespace(namespaceEU);
        ACCExtensionViewEditPage.hitUpdateButton();
        ACCExtensionViewEditPage.moveToQA();
        /**
         *  there is a corresponding UEGACC in Production state
         */
        ACCExtensionViewEditPage.moveToProduction();

        bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.openEditBIEPage(useraBIEWIP);

        for (ASCCPObject asccpToAppend : asccpsForTesting) {
            ACCObject ACCAssociation = asccpACCMap.get(asccpToAppend);
            BCCPObject bccpDescendant = accBCCPMap.get(ACCAssociation);
            assertThrows(TimeoutException.class, () -> {
                WebElement node = editBIEPage.getNodeByPath(
                        "/" + asccp.getPropertyTerm() + "/Extension/" + asccpToAppend.getPropertyTerm());
            });

        }
    }
    @Test
    @DisplayName("TC_6_2_TA_9")
    public void test_TA_9() {
        ASCCPObject asccp;
        AppUserObject usera;
        BusinessContextObject context;
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            AppUserObject endUserForCC = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserForCC);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACCSemanticGroupType(endUserForCC, release, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, endUserForCC, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);;
        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        createBIEForSelectTopLevelConceptPage.setBranch(this.release);
        assertThrows(NoSuchElementException.class, ()-> {createBIEForSelectTopLevelConceptPage.selectCoreComponentByDEN(asccp.getDen());});
    }

    @Test
    @DisplayName("TC_6_2_TA_10")
    public void test_TA_10() {
        ASCCPObject asccp;
        BCCPObject bccp;
        AppUserObject usera;
        BusinessContextObject context;
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
        {
            AppUserObject endUserForCC = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserForCC);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(endUserForCC, release, namespace, "Published");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, endUserForCC, namespace, "Published");
            coreComponentAPI.appendBCC(acc, bccp, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, endUserForCC, namespace, "Published");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);;
        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE(asccp.getDen(), this.release);
        WebElement node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        EditBIEPage.BBIEPanel BBIEPPanel = editBIEPage.getBBIEPanel(node);
        BBIEPPanel.toggleUsed();
        BBIEPPanel.setCardinalityMax(10);
        BBIEPPanel.setCardinalityMin(5);
        BBIEPPanel.setBusinessTerm("test business term");
        BBIEPPanel.setRemark("test remark");
        BBIEPPanel.setExample("test example");
        BBIEPPanel.setValueConstraint("Fixed");
        BBIEPPanel.setFixedValue("test value");
        BBIEPPanel.setValueDomainRestriction("Primitive");
        BBIEPPanel.setValueDomain("token");
        BBIEPPanel.setContextDefinition("test context definition");
        editBIEPage.hitUpdateButton();
        BBIEPPanel.hitResetButton();
        String message = "Are you sure you want to reset values to initial values?";
        assertEquals(message, BBIEPPanel.getResetDialogMessage());
        BBIEPPanel.confirmToReset();
        node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        BBIEPPanel = editBIEPage.getBBIEPanel(node);
        assertEquals("unbounded", getText(BBIEPPanel.getCardinalityMaxField()));
        assertEquals("0", getText(BBIEPPanel.getCardinalityMinField()));
        assertEquals("", getText(BBIEPPanel.getBusinessTermField()));
        assertEquals("", getText(BBIEPPanel.getRemarkField()));
        assertEquals("", getText(BBIEPPanel.getExampleField()));
        assertEquals("", getText(BBIEPPanel.getContextDefinitionField()));
    }

    @Test
    @DisplayName("TC_6_2_TA_12")
    public void test_TA_12() {
        ASCCPObject asccp_owner_usera;
        ASCCPObject asccp_owner_userb;
        AppUserObject usera;
        AppUserObject userb;
        BusinessContextObject context;
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
        {
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            /**
             * The owner of the ASCCP is usera
             */
            ACCObject acc = coreComponentAPI.createRandomACC(usera, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            BCCPObject bccp = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(acc, bccp, "Production");

            asccp_owner_usera = coreComponentAPI.createRandomASCCP(acc, usera, namespace, "Production");

            /**
             * The owner of the ASCCP is userb
             */

            acc = coreComponentAPI.createRandomACC(userb, release, namespace, "Production");
            bccp = coreComponentAPI.createRandomBCCP(dataType, userb, namespace, "Production");
            coreComponentAPI.appendBCC(acc, bccp, "Production");

            asccp_owner_userb = coreComponentAPI.createRandomASCCP(acc, userb, namespace, "Production");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);

        }
        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        assertDoesNotThrow(() -> {
            createBIEForSelectTopLevelConceptPage.createBIE(asccp_owner_usera.getDen(), this.release);
        });

        bieMenu.openViewEditBIESubMenu();
        viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        assertDoesNotThrow(() -> {
            createBIEForSelectTopLevelConceptPage.createBIE(asccp_owner_userb.getDen(), this.release);
        });
    }

    @Test
    @DisplayName("TC_6_2_TA_13")
    public void test_TA_13() {
        ASCCPObject asccp_owner_usera;
        AppUserObject usera;
        AppUserObject userb;
        BusinessContextObject context;
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
        {
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            /**
             * ACC has a group component type.
             */
            ACCObject acc = coreComponentAPI.createRandomACCSemanticGroupType(usera, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            BCCPObject bccp = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(acc, bccp, "Production");

            asccp_owner_usera = coreComponentAPI.createRandomASCCP(acc, usera, namespace, "Production");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);

        }
        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        /**
         * The end user cannot create a new BIE from an ASCCP whose ACC has a group component type.
         */
        assertThrows(NoSuchElementException.class, () -> {
            createBIEForSelectTopLevelConceptPage.createBIE(asccp_owner_usera.getDen(), this.release);
        });
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
