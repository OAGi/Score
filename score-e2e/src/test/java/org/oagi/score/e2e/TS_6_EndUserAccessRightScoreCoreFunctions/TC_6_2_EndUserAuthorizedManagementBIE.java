package org.oagi.score.e2e.TS_6_EndUserAccessRightScoreCoreFunctions;

import org.jooq.DataType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.Acc;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.menu.CoreComponentMenu;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.CreateBIEForSelectTopLevelConceptPage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.oagi.score.e2e.page.core_component.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.AssertionHelper.assertEnabled;
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
                ACCExtensionViewEditPage accExtensionViewEditPage =
                        editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
                String revision = getText(accExtensionViewEditPage.getRevisionField());
                assertEquals("1", revision);

                accExtensionViewEditPage.setNamespace(useraNamespace);
                accExtensionViewEditPage.hitUpdateButton();

                accExtensionViewEditPage.moveToQA();
                accExtensionViewEditPage.moveToProduction();
                viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
                editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
                accExtensionViewEditPage = editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
                revision = getText(accExtensionViewEditPage.getRevisionField());
                assertEquals("2", revision);
            } else {
                EditBIEPage finalEditBIEPage = editBIEPage;
                assertThrows(Exception.class, () -> {
                    finalEditBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
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
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIE);
        getDriver().manage().window().maximize();
        assertEquals("WIP", useraBIE.getState());

        ACCExtensionViewEditPage accExtensionViewEditPage =
                editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        accExtensionViewEditPage.setNamespace(useraNamespace);
        accExtensionViewEditPage.hitUpdateButton();
        accExtensionViewEditPage.moveToQA();
        assertEquals("QA", accExtensionViewEditPage.getStateFieldValue());
        homePage.logout();

        homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(userbBIE);
        assertEquals("WIP", userbBIE.getState());
        editBIEPage.getExtendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        /**
         * Display a dialog indicating “The core component is being extended by “ + [the owner of the UEGACC] or similar.
         */
        assertEquals("Another user is working on the extension.",
                editBIEPage.getAttentionDialogMessage());
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

        homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIE);
        editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        editBIEPage.backToWIP();
        homePage.logout();

        homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(userbBIE);
        editBIEPage.getExtendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
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

            ACCExtensionViewEditPage accExtensionViewEditPage =
                    editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
            viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
            editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
            accExtensionViewEditPage =
                    editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");

            assertEnabled(accExtensionViewEditPage.getDefinitionSourceField());
            assertEnabled(accExtensionViewEditPage.getDefinitionField());

            accExtensionViewEditPage.setNamespace(useraNamespace);
            accExtensionViewEditPage.hitUpdateButton();

            accExtensionViewEditPage.moveToQA();
            assertEquals("QA", accExtensionViewEditPage.getStateFieldValue());

            viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
            editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
            accExtensionViewEditPage = editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");

            assertDisabled(accExtensionViewEditPage.getDefinitionSourceField());
            assertDisabled(accExtensionViewEditPage.getDefinitionField());

            accExtensionViewEditPage.backToWIP();
            assertEquals("WIP", accExtensionViewEditPage.getStateFieldValue());

            accExtensionViewEditPage.moveToQA();
            assertEquals("QA", accExtensionViewEditPage.getStateFieldValue());

            accExtensionViewEditPage.moveToProduction();
            assertEquals("Production", accExtensionViewEditPage.getStateFieldValue());
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
        assertEquals("WIP", useraBIEWIP.getState());

        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
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

        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check business term abilities are disabled
        } else {
            assertDisabled(ASBIEPanel.getBusinessTermField());
        }

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

        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check business term abilities are disabled
        } else {
            assertDisabled(BBIEPPanel.getBusinessTermField());
        }

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

        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check business term abilities are disabled
        } else {
            assertDisabled(BBIEPPanel.getBusinessTermField());
        }
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
    @DisplayName("TC_6_2_TA_6_1")
    public void test_TA_6_1() {
        Map<TopLevelASBIEPObject, ASCCPObject> bieASCCPMap = new HashMap<>();
        AppUserObject usera;
        NamespaceObject useraNamespace;

        ArrayList<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            ReleaseObject releaseOne = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            ReleaseObject releaseTwo = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject accReleaseOne = coreComponentAPI.createRandomACC(developer, releaseOne, namespace, "Published");
            coreComponentAPI.appendExtension(accReleaseOne, developer, namespace, "Published");
            accReleaseOne.setDefinition("definition 1");
            coreComponentAPI.updateACC(accReleaseOne);

            ASCCPObject asccpReleaseOne = coreComponentAPI.createRandomASCCP(accReleaseOne, developer, namespace, "Published");
            asccpReleaseOne.setDefinition(accReleaseOne.getDefinition());
            coreComponentAPI.updateASCCP(asccpReleaseOne);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            useraNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
            thisAccountWillBeDeletedAfterTests(usera);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            TopLevelASBIEPObject useraBIEWIPReleaseOne = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseOne, usera, "WIP");
            biesForTesting.add(useraBIEWIPReleaseOne);
            bieASCCPMap.put(useraBIEWIPReleaseOne, asccpReleaseOne);

            TopLevelASBIEPObject useraBIEProductionReleaseOne = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseOne, usera, "Production");
            biesForTesting.add(useraBIEProductionReleaseOne);
            bieASCCPMap.put(useraBIEProductionReleaseOne, asccpReleaseOne);

            TopLevelASBIEPObject useraBIEQAReleaseOne = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseOne, usera, "QA");
            biesForTesting.add(useraBIEQAReleaseOne);
            bieASCCPMap.put(useraBIEQAReleaseOne, asccpReleaseOne);

            // create the revision in another release
            ACCObject accReleaseTwo = coreComponentAPI.createRevisedACC(accReleaseOne, developer, releaseTwo, "Published");
            coreComponentAPI.appendExtension(accReleaseTwo, developer, namespace, "Published");
            accReleaseTwo.setDefinition("definition 2");
            coreComponentAPI.updateACC(accReleaseTwo);

            ASCCPObject asccpReleaseTwo = coreComponentAPI.createRandomASCCP(accReleaseTwo, developer, namespace, "Published");
            asccpReleaseTwo.setDefinition(accReleaseTwo.getDefinition());
            coreComponentAPI.updateASCCP(asccpReleaseTwo);

            TopLevelASBIEPObject useraBIEWIPReleaseTwo = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseTwo, usera, "WIP");
            biesForTesting.add(useraBIEWIPReleaseTwo);
            bieASCCPMap.put(useraBIEWIPReleaseTwo, asccpReleaseTwo);

            TopLevelASBIEPObject useraBIEProductionReleaseTwo = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseTwo, usera, "Production");
            biesForTesting.add(useraBIEProductionReleaseTwo);
            bieASCCPMap.put(useraBIEProductionReleaseTwo, asccpReleaseTwo);

            TopLevelASBIEPObject useraBIEQAReleaseTwo = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseTwo, usera, "QA");
            biesForTesting.add(useraBIEQAReleaseTwo);
            bieASCCPMap.put(useraBIEQAReleaseTwo, asccpReleaseTwo);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            BIEMenu bieMenu = homePage.getBIEMenu();
            ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
            EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
            getDriver().manage().window().maximize();
            ASCCPObject asccp = bieASCCPMap.get(topLevelAsbiep);
            /**
             * Assert that Type Definition field in BIE has the same value as ASCCP's definition on which it is based.
             * Note that there are two ASCCPs in two releases having different definitions
             */
            String ASCCPDefinition = editBIEPage.getTypeDefinitionValue();
            assertEquals(asccp.getDefinition(), ASCCPDefinition);
            if (topLevelAsbiep.getState().equals("WIP")) {
                ACCExtensionViewEditPage accExtensionViewEditPage =
                        editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
                String revision = getText(accExtensionViewEditPage.getRevisionField());
                assertEquals("1", revision);

                accExtensionViewEditPage.setNamespace(useraNamespace);
                accExtensionViewEditPage.hitUpdateButton();

                accExtensionViewEditPage.moveToQA();
                accExtensionViewEditPage.moveToProduction();
                viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
                editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
                accExtensionViewEditPage = editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
                revision = getText(accExtensionViewEditPage.getRevisionField());
                assertEquals("2", revision);
            } else {
                EditBIEPage finalEditBIEPage = editBIEPage;
                assertThrows(Exception.class, () -> {
                    finalEditBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
                });
                escape(getDriver());
            }
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
                ACCExtensionViewEditPage accExtensionViewEditPage =
                        editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
                String revision = getText(accExtensionViewEditPage.getRevisionField());
                assertEquals("1", revision);

                accExtensionViewEditPage.setNamespace(useraNamespace);
                accExtensionViewEditPage.hitUpdateButton();
                accExtensionViewEditPage.moveToQA();
                accExtensionViewEditPage.moveToProduction();

                viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
                editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
                accExtensionViewEditPage =
                        editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
                revision = getText(accExtensionViewEditPage.getRevisionField());
                assertEquals("2", revision);
            } else {
                EditBIEPage finalEditBIEPage = editBIEPage;
                assertThrows(TimeoutException.class, () -> {
                    finalEditBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
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
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIE);
        getDriver().manage().window().maximize();
        assertEquals("WIP", useraBIE.getState());

        ACCExtensionViewEditPage accExtensionViewEditPage =
                editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        accExtensionViewEditPage.setNamespace(useraNamespace);
        accExtensionViewEditPage.hitUpdateButton();
        accExtensionViewEditPage.moveToQA();
        assertEquals("QA", accExtensionViewEditPage.getStateFieldValue());
        homePage.logout();

        homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        EditBIEPage nextEditBIEPage = viewEditBIEPage.openEditBIEPage(userbBIE);
        assertEquals("WIP", userbBIE.getState());

        assertThrows(AssertionError.class, () -> {
            nextEditBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        });

        /**
         * Display a dialog indicating “The core component is being extended by “ + [the owner of the UEGACC] or similar.
         */
        assertEquals("Another user is working on the extension.",
                nextEditBIEPage.getAttentionDialogMessage());
        ACCExtensionViewEditPage nextACCExtensionViewEditPage = nextEditBIEPage.continueToExtendBIEOnNode();

        /**
         * If the UEGACC is in QA state, the end user can view its details but cannot make any change.
         */
        assertEquals("QA", nextACCExtensionViewEditPage.getStateFieldValue());
        assertThrows(TimeoutException.class, () -> {
            nextACCExtensionViewEditPage.getUpdateButton(false);
        });
        assertThrows(TimeoutException.class, () -> {
            nextACCExtensionViewEditPage.getMoveToQAButton(false);
        });
        assertThrows(TimeoutException.class, () -> {
            nextACCExtensionViewEditPage.getMoveToProductionButton(false);
        });

        assertEquals(usera.getLoginId(), nextACCExtensionViewEditPage.getOwnerFieldValue());
        assertDisabled(nextACCExtensionViewEditPage.getDefinitionField());
        assertDisabled(nextACCExtensionViewEditPage.getObjectClassTermField());
        assertDisabled(nextACCExtensionViewEditPage.getDefinitionSourceField());
        switchToMainTab(getDriver());
        homePage.logout();

        homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIE);
        accExtensionViewEditPage =
                editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        accExtensionViewEditPage.backToWIP();
        assertEquals("WIP", accExtensionViewEditPage.getStateFieldValue());
        homePage.logout();

        homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        EditBIEPage finalEditBIEPage = viewEditBIEPage.openEditBIEPage(userbBIE);
        assertThrows(AssertionError.class, () -> {
            finalEditBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        });
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

            ACCExtensionViewEditPage accExtensionViewEditPage =
                    editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
            viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
            editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
            accExtensionViewEditPage =
                    editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");

            assertEnabled(accExtensionViewEditPage.getDefinitionSourceField());
            assertEnabled(accExtensionViewEditPage.getDefinitionField());

            accExtensionViewEditPage.setNamespace(useraNamespace);
            editBIEPage.hitUpdateButton();
            editBIEPage.moveToQA();

            viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
            editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
            accExtensionViewEditPage =
                    editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");

            assertDisabled(accExtensionViewEditPage.getDefinitionSourceField());
            assertDisabled(accExtensionViewEditPage.getDefinitionField());

            accExtensionViewEditPage.backToWIP();
            assertEquals("WIP", accExtensionViewEditPage.getStateFieldValue());

            accExtensionViewEditPage.moveToQA();
            assertEquals("QA", accExtensionViewEditPage.getStateFieldValue());

            accExtensionViewEditPage.moveToProduction();
            assertEquals("Production", accExtensionViewEditPage.getStateFieldValue());
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
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
        // TODO:
        // Can't open the context menu in a small size of the screen.
        getDriver().manage().window().maximize();
        assertEquals("WIP", useraBIEWIP.getState());

        ACCExtensionViewEditPage accExtensionViewEditPage =
                editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        SelectAssociationDialog selectCCPropertyPage =
                accExtensionViewEditPage.appendPropertyAtLast("/All User Extension Group. Details");

        /**
         * ASCCP cannot be appended to the global extension.
         */
        assertThrows(NoSuchElementException.class, () -> {
            selectCCPropertyPage.setAssociationType("ASCCP");
        });
        escape(getDriver());
        selectCCPropertyPage.selectAssociation(bccpToAppend.getDen());

        viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);

        WebElement node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/Extension/" + bccpToAppend.getPropertyTerm());
        assertTrue(node.isDisplayed());
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
        /*
         * Assert that all options are disabled.
         */
        assertDisabled(bbiePanel.getNillableCheckbox());
        assertDisabled(bbiePanel.getUsedCheckbox());
        assertDisabled(bbiePanel.getCardinalityMinField());
        assertDisabled(bbiePanel.getCardinalityMaxField());
        assertDisabled(bbiePanel.getRemarkField());
        assertDisabled(bbiePanel.getExampleField());
        assertDisabled(bbiePanel.getValueConstraintSelectField());
        assertDisabled(bbiePanel.getValueDomainRestrictionSelectField());
        assertDisabled(bbiePanel.getValueDomainField());
        assertDisabled(bbiePanel.getContextDefinitionField());
        assertDisabled(bbiePanel.getAssociationDefinitionField());
        assertDisabled(bbiePanel.getComponentDefinitionField());
        // TODO:
        // Check if Business Term functionality is enabled. Currently, it is disabled.
        assertDisabled(bbiePanel.getBusinessTermField());
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
        ACCExtensionViewEditPage accExtensionViewEditPage =
                editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        for (ASCCPObject asccpToAppend : asccpsForTesting) {
            /**
             * It has child association to an end user ASCCP which is not in Production state
             */
            assertNotEquals("Production", asccpToAppend.getState());
            SelectAssociationDialog selectCCPropertyPage =
                    accExtensionViewEditPage.appendPropertyAtLast("/All User Extension Group. Details");
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
            SelectAssociationDialog selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/All User Extension Group. Details");
            selectCCPropertyPage.selectAssociation(bccpToAppend.getDen());
        }

        accExtensionViewEditPage.setNamespace(namespaceEU);
        accExtensionViewEditPage.hitUpdateButton();
        accExtensionViewEditPage.moveToQA();
        /*
         * there is a corresponding UEGACC in Production state
         */
        accExtensionViewEditPage.moveToProduction();

        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage finalEditBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);

        for (BCCPObject bccpToAppend : bccpsForTesting) {
            WebElement node = finalEditBIEPage.getNodeByPath(
                    "/" + asccp.getPropertyTerm() + "/Extension/" + bccpToAppend.getPropertyTerm());
            assertTrue(node.isDisplayed());
            EditBIEPage.BBIEPanel bbiePanel = finalEditBIEPage.getBBIEPanel(node);
            /**
             * Assert that all options are disabled.
             */
            assertDisabled(bbiePanel.getNillableCheckbox());
            assertDisabled(bbiePanel.getUsedCheckbox());
            assertDisabled(bbiePanel.getCardinalityMinField());
            assertDisabled(bbiePanel.getCardinalityMaxField());
            assertDisabled(bbiePanel.getRemarkField());
            assertDisabled(bbiePanel.getExampleField());
            assertDisabled(bbiePanel.getValueConstraintSelectField());
            assertDisabled(bbiePanel.getValueDomainRestrictionSelectField());
            assertDisabled(bbiePanel.getValueDomainField());
            assertDisabled(bbiePanel.getContextDefinitionField());
            assertDisabled(bbiePanel.getAssociationDefinitionField());
            assertDisabled(bbiePanel.getComponentDefinitionField());
            // TODO:
            // Check if Business Term functionality is enabled. Currently, it is disabled.
            assertDisabled(bbiePanel.getBusinessTermField());
        }

        for (ASCCPObject asccpToAppend : asccpsForTesting) {
            assertThrows(TimeoutException.class, () -> {
                finalEditBIEPage.getNodeByPath(
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
        ACCExtensionViewEditPage accExtensionViewEditPage =
                editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        for (ASCCPObject asccpToAppend : asccpsForTesting) {
            /**
             * It has child association to an end user ASCCP that is in the Production state
             */
            assertEquals("Production", asccpToAppend.getState());
            /**
             * The end user ACC (of the ASCCP) is not in the Production state
             */
            ACCObject accAssociation = asccpACCMap.get(asccpToAppend);
            assertNotEquals("Production", accAssociation.getState());
            SelectAssociationDialog selectCCPropertyPage =
                    accExtensionViewEditPage.appendPropertyAtLast("/All User Extension Group. Details");

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

        accExtensionViewEditPage.setNamespace(namespaceEU);
        accExtensionViewEditPage.hitUpdateButton();
        accExtensionViewEditPage.moveToQA();
        /**
         *  there is a corresponding UEGACC in Production state
         */
        accExtensionViewEditPage.moveToProduction();

        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage finalEditBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);

        for (ASCCPObject asccpToAppend : asccpsForTesting) {
            assertThrows(TimeoutException.class, () -> {
                finalEditBIEPage.getNodeByPath(
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
        ACCExtensionViewEditPage accExtensionViewEditPage =
                editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
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
            SelectAssociationDialog selectCCPropertyPage =
                    accExtensionViewEditPage.appendPropertyAtLast("/All User Extension Group. Details");
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

        accExtensionViewEditPage.setNamespace(namespaceEU);
        accExtensionViewEditPage.hitUpdateButton();
        accExtensionViewEditPage.moveToQA();
        /**
         *  there is a corresponding UEGACC in Production state
         */
        accExtensionViewEditPage.moveToProduction();

        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage finalEditBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);

        for (ASCCPObject asccpToAppend : asccpsForTesting) {
            assertThrows(TimeoutException.class, () -> {
                finalEditBIEPage.getNodeByPath(
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
        ACCExtensionViewEditPage accExtensionViewEditPage =
                editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        for (ASCCPObject asccpToAppend : asccpsForTesting) {
            /**
             * It has child association to an end user ASCCP that is in the Production state
             */
            assertEquals("Production", asccpToAppend.getState());
            /**
             * The end user ACC (of the ASCCP) is not in the Production state
             */
            ACCObject accAssociation = asccpACCMap.get(asccpToAppend);
            assertNotEquals("Production", accAssociation.getState());
            SelectAssociationDialog selectCCPropertyPage =
                    accExtensionViewEditPage.appendPropertyAtLast("/All User Extension Group. Details");
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

        accExtensionViewEditPage.setNamespace(namespaceEU);
        accExtensionViewEditPage.hitUpdateButton();
        accExtensionViewEditPage.moveToQA();
        /**
         *  there is a corresponding UEGACC in Production state
         */
        accExtensionViewEditPage.moveToProduction();

        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage finalEditBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);

        for (ASCCPObject asccpToAppend : asccpsForTesting) {
            ACCObject ACCAssociation = asccpACCMap.get(asccpToAppend);
            BCCPObject bccpDescendant = accBCCPMap.get(ACCAssociation);
            assertThrows(TimeoutException.class, () -> {
                finalEditBIEPage.getNodeByPath(
                        "/" + asccp.getPropertyTerm() + "/Extension/" + asccpToAppend.getPropertyTerm());
            });
        }
    }

    @Test
    @DisplayName("TC_6_2_TA_8_1_and_TA_8_3")
    public void test_TA_8_1_and_TA_8_3() {
        ArrayList<CodeListObject> codeListsForTesting = new ArrayList<>();
        Map<CodeListObject, ReleaseObject> codeListReleaseMap = new HashMap<>();
        ASCCPObject asccp;
        AppUserObject usera;
        TopLevelASBIEPObject useraBIE;
        Map<BCCPObject, DTObject> bccpDTMap = new HashMap<>();
        ArrayList<BCCPObject> bccpForTesting = new ArrayList<>();
        {
            /**
             * Production developer Code List for the latest and older release
             */
            ReleaseObject latestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");
            ReleaseObject olderRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
            AppUserObject developerUserForCodeList = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerUserForCodeList);
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developerUserForCodeList);
            CodeListObject baseCodeList = getAPIFactory().getCodeListAPI().getCodeListByCodeListNameAndReleaseNum("oacl_ResponseCode", latestRelease.getReleaseNumber());
            CodeListObject developerCodeListLatestRelease = getAPIFactory().getCodeListAPI().createDerivedCodeList(baseCodeList, developerUserForCodeList, namespace, latestRelease, "Production");
            getAPIFactory().getCodeListAPI().addCodeListToAnotherRelease(developerCodeListLatestRelease, olderRelease, developerUserForCodeList);
            codeListsForTesting.add(developerCodeListLatestRelease);
            codeListReleaseMap.put(developerCodeListLatestRelease, latestRelease);

            baseCodeList = getAPIFactory().getCodeListAPI().getCodeListByCodeListNameAndReleaseNum("oacl_StateCode", olderRelease.getReleaseNumber());
            CodeListObject developerCodeListOlderRelease = getAPIFactory().getCodeListAPI().createDerivedCodeList(baseCodeList, developerUserForCodeList, namespace, olderRelease, "Production");
            codeListsForTesting.add(developerCodeListOlderRelease);
            codeListReleaseMap.put(developerCodeListOlderRelease, olderRelease);

            /**
             * Production end-user Code List for the latest and older release
             */
            AppUserObject endUserForCodeList = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserForCodeList);
            namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserForCodeList);
            baseCodeList = getAPIFactory().getCodeListAPI().getCodeListByCodeListNameAndReleaseNum("oacl_ReasonCode", latestRelease.getReleaseNumber());
            CodeListObject endUserCodeListLatestRelease = getAPIFactory().getCodeListAPI().createDerivedCodeList(baseCodeList, endUserForCodeList, namespace, latestRelease, "Production");
            codeListsForTesting.add(endUserCodeListLatestRelease);
            codeListReleaseMap.put(endUserCodeListLatestRelease, latestRelease);

            baseCodeList = getAPIFactory().getCodeListAPI().getCodeListByCodeListNameAndReleaseNum("oacl_RiskCode", olderRelease.getReleaseNumber());
            CodeListObject endUserCodeListOlderRelease = getAPIFactory().getCodeListAPI().createDerivedCodeList(baseCodeList, endUserForCodeList, namespace, olderRelease, "Production");
            codeListsForTesting.add(endUserCodeListOlderRelease);
            codeListReleaseMap.put(endUserCodeListOlderRelease, olderRelease);

            /**
             * Create CC and BIE
             */
            AppUserObject endUserForCC = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserForCC);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(endUserForCC, olderRelease, namespace, "Published");
            DTObject dataType_bccp_a = coreComponentAPI.getBDTByGuidAndReleaseNum("f1bf224d9da94fbea2d8e98af95c7a0b", olderRelease.getReleaseNumber());
            DTObject dataType_bccp_b = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", olderRelease.getReleaseNumber());
            BCCPObject bccp_a = coreComponentAPI.createRandomBCCP(dataType_bccp_a, endUserForCC, namespace, "Production");
            BCCPObject bccp_b = coreComponentAPI.createRandomBCCP(dataType_bccp_b, endUserForCC, namespace, "Production");
            bccpForTesting.add(bccp_a);
            bccpForTesting.add(bccp_b);
            bccpDTMap.put(bccp_a, dataType_bccp_a);
            bccpDTMap.put(bccp_b, dataType_bccp_b);
            coreComponentAPI.appendBCC(acc, bccp_a, "Production");
            coreComponentAPI.appendBCC(acc, bccp_b, "Production");
            asccp = coreComponentAPI.createRandomASCCP(acc, endUserForCC, namespace, "Published");

            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            useraBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIE);
        getDriver().manage().window().maximize();
        for (BCCPObject bccp: bccpForTesting){
            WebElement node = editBIEPage.getNodeByPath(
                    "/" + bccp.getPropertyTerm());
            assertTrue(node.isDisplayed());
            EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
            bbiePanel.toggleUsed();
            bbiePanel.setValueDomainRestriction("Code");
            String BIEreleaseNumber = useraBIE.getReleaseNumber();
            DTObject dataType = bccpDTMap.get(bccp);
            ArrayList<CodeListObject> defaultCodeLists = getAPIFactory().getCoreComponentAPI().getDefaultCodeListsForDT(dataType.getGuid(), dataType.getReleaseId());
            if (!defaultCodeLists.isEmpty()) {
                for (CodeListObject cl : defaultCodeLists) {
                    bbiePanel.setValueDomain(cl.getName());
                }
            } else {
                for (CodeListObject codeList : codeListsForTesting) {
                    /**
                     * Only production, compatible code lists in the same release as the BIE shall be included, i.e., a code list exists only in a newer release shall not be included.
                     */

                    Boolean exists = getAPIFactory().getCodeListAPI().doesCodeListExistInTheRelease(codeList, BIEreleaseNumber);
                    if (codeList.getState().equals("Production") && exists) {
                        bbiePanel.setValueDomain(codeList.getName());
                    } else {
                        assertThrows(TimeoutException.class, () -> {
                            bbiePanel.setValueDomain(codeList.getName());
                        });
                        escape(getDriver());
                    }

                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_2_TA_8_2_and_TA_8_3")
    public void test_TA_8_2_and_TA_8_3() {
        ArrayList<CodeListObject> codeListsForTesting = new ArrayList<>();
        Map<CodeListObject, ReleaseObject> codeListReleaseMap = new HashMap<>();
        ASCCPObject asccp;
        AppUserObject usera;
        TopLevelASBIEPObject useraBIE;
        Map<BCCPObject, DTObject> bccpDTMap = new HashMap<>();
        ArrayList<BCCPObject> bccpForTesting = new ArrayList<>();
        {
            /**
             * Developer Code List for the latest and older release in WIP state
             */
            ReleaseObject latestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");
            ReleaseObject olderRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
            AppUserObject developerUserForCodeList = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerUserForCodeList);
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developerUserForCodeList);

            CodeListObject baseCodeList = getAPIFactory().getCodeListAPI().getCodeListByCodeListNameAndReleaseNum("oacl_CategoryCode", latestRelease.getReleaseNumber());
            CodeListObject developerWIPCodeListLatestRelease = getAPIFactory().getCodeListAPI().createDerivedCodeList(baseCodeList, developerUserForCodeList, namespace, latestRelease, "WIP");
            codeListsForTesting.add(developerWIPCodeListLatestRelease);
            codeListReleaseMap.put(developerWIPCodeListLatestRelease, latestRelease);

            baseCodeList = getAPIFactory().getCodeListAPI().getCodeListByCodeListNameAndReleaseNum("oacl_ChargeCode", latestRelease.getReleaseNumber());
            CodeListObject developerWIPCodeListOlderRelease = getAPIFactory().getCodeListAPI().createDerivedCodeList(baseCodeList, developerUserForCodeList, namespace, olderRelease, "WIP");
            codeListsForTesting.add(developerWIPCodeListOlderRelease);
            codeListReleaseMap.put(developerWIPCodeListOlderRelease, olderRelease);

            /**
             * End-user Code List for the latest and older release in QA state
             */
            AppUserObject endUserForCodeList = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserForCodeList);
            namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserForCodeList);

            baseCodeList = getAPIFactory().getCodeListAPI().getCodeListByCodeListNameAndReleaseNum("oacl_ConfirmationCode", latestRelease.getReleaseNumber());
            CodeListObject endUserWIPCodeListLatestRelease = getAPIFactory().getCodeListAPI().createDerivedCodeList(baseCodeList, endUserForCodeList, namespace, latestRelease, "QA");
            codeListsForTesting.add(endUserWIPCodeListLatestRelease);
            codeListReleaseMap.put(endUserWIPCodeListLatestRelease, latestRelease);
            getAPIFactory().getCodeListAPI().addCodeListToAnotherRelease(endUserWIPCodeListLatestRelease, olderRelease, endUserForCodeList);

            baseCodeList = getAPIFactory().getCodeListAPI().getCodeListByCodeListNameAndReleaseNum("oacl_ClassificationCode", olderRelease.getReleaseNumber());
            CodeListObject endUserWIPCodeListOlderRelease = getAPIFactory().getCodeListAPI().createDerivedCodeList(baseCodeList, endUserForCodeList, namespace, olderRelease, "QA");
            codeListsForTesting.add(endUserWIPCodeListOlderRelease);
            codeListReleaseMap.put(endUserWIPCodeListOlderRelease, olderRelease);

            /**
             * Deleted end-user Code List for the latest and older release
             */
            baseCodeList = getAPIFactory().getCodeListAPI().getCodeListByCodeListNameAndReleaseNum("oacl_CountryCode", olderRelease.getReleaseNumber());
            CodeListObject endUserDeletedCodeListOlderRelease = getAPIFactory().getCodeListAPI().createDerivedCodeList(baseCodeList, endUserForCodeList, namespace, olderRelease, "Deleted");
            codeListsForTesting.add(endUserDeletedCodeListOlderRelease);
            codeListReleaseMap.put(endUserDeletedCodeListOlderRelease, olderRelease);

            baseCodeList = getAPIFactory().getCodeListAPI().getCodeListByCodeListNameAndReleaseNum("oacl_ControlCode", olderRelease.getReleaseNumber());
            CodeListObject endUserDeletedCodeListLatestRelease = getAPIFactory().getCodeListAPI().createDerivedCodeList(baseCodeList, endUserForCodeList, namespace, latestRelease, "Deleted");
            codeListsForTesting.add(endUserDeletedCodeListLatestRelease);
            codeListReleaseMap.put(endUserDeletedCodeListLatestRelease, latestRelease);

            /**
             * Create CC and BIE
             */
            AppUserObject endUserForCC = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserForCC);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject acc = coreComponentAPI.createRandomACC(endUserForCC, olderRelease, namespace, "Published");
            DTObject dataType_bccp_a = coreComponentAPI.getBDTByGuidAndReleaseNum("f1bf224d9da94fbea2d8e98af95c7a0b", olderRelease.getReleaseNumber());
            DTObject dataType_bccp_b = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", olderRelease.getReleaseNumber());
            BCCPObject bccp_a = coreComponentAPI.createRandomBCCP(dataType_bccp_a, endUserForCC, namespace, "Production");
            BCCPObject bccp_b = coreComponentAPI.createRandomBCCP(dataType_bccp_b, endUserForCC, namespace, "Production");
            bccpForTesting.add(bccp_a);
            bccpForTesting.add(bccp_b);
            bccpDTMap.put(bccp_a, dataType_bccp_a);
            bccpDTMap.put(bccp_b, dataType_bccp_b);
            coreComponentAPI.appendBCC(acc, bccp_a, "Production");
            coreComponentAPI.appendBCC(acc, bccp_b, "Production");
            asccp = coreComponentAPI.createRandomASCCP(acc, endUserForCC, namespace, "Published");

            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            useraBIE = getAPIFactory().getBusinessInformationEntityAPI().generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIE);
        getDriver().manage().window().maximize();

        for (BCCPObject bccp : bccpForTesting){
            WebElement node = editBIEPage.getNodeByPath(
                    "/" + bccp.getPropertyTerm());
            assertTrue(node.isDisplayed());
            EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
            bbiePanel.toggleUsed();
            bbiePanel.setValueDomainRestriction("Code");
            String BIEreleaseNumber = useraBIE.getReleaseNumber();
            DTObject dataType = bccpDTMap.get(bccp);
            ArrayList<CodeListObject> defaultCodeLists = getAPIFactory().getCoreComponentAPI().getDefaultCodeListsForDT(dataType.getGuid(), dataType.getReleaseId());
            if (!defaultCodeLists.isEmpty()) {
                for (CodeListObject cl : defaultCodeLists) {
                    bbiePanel.setValueDomain(cl.getName());
                }
            } else {
                /**
                 * If there is no default code list, all developer code lists in the published state in the same release and end user code lists in the same release shall be
                 * included. End user code lists shall be displayed in the same way as described in 8.2.
                 */
                for (CodeListObject codeList : codeListsForTesting) {
                    Boolean exists = getAPIFactory().getCodeListAPI().doesCodeListExistInTheRelease(codeList, BIEreleaseNumber);
                    if (exists) {
                        if (codeList.getState().equals("QA") || codeList.getState().equals("WIP")) {
                            /**
                             * if it is in the WIP or QA state, flag that the code list is being changed (maybe use dark yellow and italicized font – yellow
                             * like a warning light) (the meaning is the code list is usable but unstable.
                             */
                            assertEquals("This code list is usable but u", bbiePanel.getValueDomainWarningMessage(codeList.getName()));
                            escape(getDriver());
                        }
                        if (codeList.getState().equals("Deleted")) {
                            /**
                             * If the code list is in Deleted state use Strikethrough font.
                             */
                            assertEquals("This code list is deleted", bbiePanel.getValueDomainWarningMessage(codeList.getName()));
                            escape(getDriver());
                        }
                    } else {
                        assertThrows(TimeoutException.class, () -> {
                            bbiePanel.setValueDomain(codeList.getName());
                        });
                        escape(getDriver());
                    }
                }
            }
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
            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        createBIEForSelectTopLevelConceptPage.setBranch(this.release);
        assertThrows(NoSuchElementException.class, () -> {
            createBIEForSelectTopLevelConceptPage.selectCoreComponentByDEN(asccp.getDen());
        });
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

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE(asccp.getDen(), this.release);
        WebElement node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
        bbiePanel.toggleUsed();
        bbiePanel.setCardinalityMax(10);
        bbiePanel.setCardinalityMin(5);
        bbiePanel.setBusinessTerm("test business term");
        bbiePanel.setRemark("test remark");
        bbiePanel.setExample("test example");
        bbiePanel.setValueConstraint("Fixed");
        bbiePanel.setFixedValue("test value");
        bbiePanel.setValueDomainRestriction("Primitive");
        bbiePanel.setValueDomain("token");
        bbiePanel.setContextDefinition("test context definition");
        editBIEPage.hitUpdateButton();

        assertEquals("10", getText(bbiePanel.getCardinalityMaxField()));
        assertEquals("5", getText(bbiePanel.getCardinalityMinField()));
        assertEquals("test business term", getText(bbiePanel.getBusinessTermField()));
        assertEquals("test remark", getText(bbiePanel.getRemarkField()));
        assertEquals("test example", getText(bbiePanel.getExampleField()));
        assertEquals("test context definition", getText(bbiePanel.getContextDefinitionField()));

        bbiePanel.hitResetButton();
        String message = "Are you sure you want to reset values to initial values?";
        assertEquals(message, bbiePanel.getResetDialogMessage());
        bbiePanel.confirmToReset();
        node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        bbiePanel = editBIEPage.getBBIEPanel(node);
        assertEquals("unbounded", getText(bbiePanel.getCardinalityMaxField()));
        assertEquals("0", getText(bbiePanel.getCardinalityMinField()));
        assertEquals("", getText(bbiePanel.getBusinessTermField()));
        assertEquals("", getText(bbiePanel.getRemarkField()));
        assertEquals("", getText(bbiePanel.getExampleField()));
        assertEquals("", getText(bbiePanel.getContextDefinitionField()));
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

    @Test
    @DisplayName("TC_6_2_TA_14_and_TA_14_1")
    public void test_TA_14_and_TA_14_1() {
        ASCCPObject asccpTopLevel;
        BCCPObject bccp_ACCTopLevel;
        ASCCPObject asccp;
        BCCPObject bccp;
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

            ACCObject accTopLevel = coreComponentAPI.createRandomACC(usera, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp_ACCTopLevel = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(accTopLevel, bccp_ACCTopLevel, "Production");

            ACCObject acc = coreComponentAPI.createRandomACC(usera, release, namespace, "Production");
            bccp = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(acc, bccp, "Production");
            asccp = coreComponentAPI.createRandomASCCP(acc, usera, namespace, "Production");
            coreComponentAPI.appendASCC(accTopLevel, asccp, "Production");

            asccpTopLevel = coreComponentAPI.createRandomASCCP(accTopLevel, usera, namespace, "Production");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
        }

        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE(asccpTopLevel.getDen(), this.release);
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().getTopLevelASBIEPByDENAndReleaseNum(asccpTopLevel.getDen(), this.release);
        /**
         * The end user ASCCP is in Production State
         */
        assertEquals("Production", asccp.getState());
        /**
         * Assert descendent nodes are editable
         */
        WebElement node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        EditBIEPage.ASBIEPanel ASBIEPanel = editBIEPage.getASBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        ASBIEPanel.toggleUsed();
        assertEnabled(ASBIEPanel.getUsedCheckbox());
        assertEnabled(ASBIEPanel.getCardinalityMinField());
        assertEnabled(ASBIEPanel.getCardinalityMaxField());
        assertEnabled(ASBIEPanel.getRemarkField());
        assertEnabled(ASBIEPanel.getContextDefinitionField());

        //TODO
        // Check if Business Term functionality is enabled. Currently, it is disabled.
        assertEnabled(ASBIEPanel.getBusinessTermField());

        node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        bbiePanel.toggleUsed();
        assertEnabled(bbiePanel.getNillableCheckbox());
        assertEnabled(bbiePanel.getUsedCheckbox());
        assertEnabled(bbiePanel.getCardinalityMinField());
        assertEnabled(bbiePanel.getCardinalityMaxField());
        assertEnabled(bbiePanel.getRemarkField());
        assertEnabled(bbiePanel.getExampleField());
        assertEnabled(bbiePanel.getValueConstraintSelectField());
        assertEnabled(bbiePanel.getValueDomainRestrictionSelectField());
        assertEnabled(bbiePanel.getValueDomainField());
        assertEnabled(bbiePanel.getContextDefinitionField());
        //TODO
        // Check if Business Term functionality is enabled. Currently, it is disabled.
        assertEnabled(bbiePanel.getBusinessTermField());
        editBIEPage.hitUpdateButton();
        homePage.logout();

        /**
         * The end user ASCCP is amended
         */
        loginPage().signIn(usera.getLoginId(), usera.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch(asccp.getDen(), this.release);
        asccpViewEditPage.hitAmendButton();
        homePage.logout();

        loginPage().signIn(userb.getLoginId(), userb.getPassword());
        homePage.getBIEMenu();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);

        /**
         * If the end user ASCCP is amended (i.e., moved to WIP state), the BIE cannot be edited. The fields of the BIE nodes are disabled including the “Used” checkbox.
         */
        node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        ASBIEPanel = editBIEPage.getASBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        assertDisabled(ASBIEPanel.getUsedCheckbox());
        assertDisabled(ASBIEPanel.getCardinalityMinField());
        assertDisabled(ASBIEPanel.getCardinalityMaxField());
        assertDisabled(ASBIEPanel.getRemarkField());
        assertDisabled(ASBIEPanel.getContextDefinitionField());

        //TODO
        // Check if Business Term functionality is enabled. Currently, it is disabled.
        assertDisabled(ASBIEPanel.getBusinessTermField());

        node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        bbiePanel = editBIEPage.getBBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        assertDisabled(bbiePanel.getNillableCheckbox());
        assertDisabled(bbiePanel.getUsedCheckbox());
        assertDisabled(bbiePanel.getCardinalityMinField());
        assertDisabled(bbiePanel.getCardinalityMaxField());
        assertDisabled(bbiePanel.getRemarkField());
        assertDisabled(bbiePanel.getExampleField());
        assertDisabled(bbiePanel.getValueConstraintSelectField());
        assertDisabled(bbiePanel.getValueDomainRestrictionSelectField());
        assertDisabled(bbiePanel.getValueDomainField());
        assertDisabled(bbiePanel.getContextDefinitionField());
        //TODO
        // Check if Business Term functionality is enabled. Currently, it is disabled.
        assertDisabled(bbiePanel.getBusinessTermField());
    }

    @Test
    @DisplayName("TC_6_2_TA_14_2")
    public void test_TA_14_2() {
        ASCCPObject asccpTopLevel;
        BCCPObject bccp_ACCTopLevel;
        ASCCPObject asccp;
        BCCPObject bccp;
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

            ACCObject accTopLevel = coreComponentAPI.createRandomACC(usera, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp_ACCTopLevel = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(accTopLevel, bccp_ACCTopLevel, "Production");

            ACCObject acc = coreComponentAPI.createRandomACC(usera, release, namespace, "Production");
            bccp = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(acc, bccp, "Production");
            asccp = coreComponentAPI.createRandomASCCP(acc, usera, namespace, "Production");
            coreComponentAPI.appendASCC(accTopLevel, asccp, "Production");

            asccpTopLevel = coreComponentAPI.createRandomASCCP(accTopLevel, usera, namespace, "Production");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
        }

        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE(asccpTopLevel.getDen(), this.release);
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().getTopLevelASBIEPByDENAndReleaseNum(asccpTopLevel.getDen(), this.release);
        /**
         * The end user ASCCP is in Production State
         */
        assertEquals("Production", asccp.getState());
        /**
         * Assert descendent nodes are editable
         */
        WebElement node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        EditBIEPage.ASBIEPanel ASBIEPanel = editBIEPage.getASBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        ASBIEPanel.toggleUsed();
        assertEnabled(ASBIEPanel.getUsedCheckbox());
        assertEnabled(ASBIEPanel.getCardinalityMinField());
        assertEnabled(ASBIEPanel.getCardinalityMaxField());
        assertEnabled(ASBIEPanel.getRemarkField());
        assertEnabled(ASBIEPanel.getContextDefinitionField());

        //TODO
        // Check if Business Term functionality is enabled. Currently, it is disabled.
        assertEnabled(ASBIEPanel.getBusinessTermField());

        node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        bbiePanel.toggleUsed();
        assertEnabled(bbiePanel.getNillableCheckbox());
        assertEnabled(bbiePanel.getUsedCheckbox());
        assertEnabled(bbiePanel.getCardinalityMinField());
        assertEnabled(bbiePanel.getCardinalityMaxField());
        assertEnabled(bbiePanel.getRemarkField());
        assertEnabled(bbiePanel.getExampleField());
        assertEnabled(bbiePanel.getValueConstraintSelectField());
        assertEnabled(bbiePanel.getValueDomainRestrictionSelectField());
        assertEnabled(bbiePanel.getValueDomainField());
        assertEnabled(bbiePanel.getContextDefinitionField());
        //TODO
        // Check if Business Term functionality is enabled. Currently, it is disabled.
        assertEnabled(bbiePanel.getBusinessTermField());
        editBIEPage.hitUpdateButton();
        homePage.logout();

        /**
         * The end user ASCCP is amended
         */
        loginPage().signIn(usera.getLoginId(), usera.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch(asccp.getDen(), this.release);
        asccpViewEditPage.hitAmendButton();
        asccpViewEditPage.moveToQA();
        homePage.logout();

        loginPage().signIn(userb.getLoginId(), userb.getPassword());
        homePage.getBIEMenu();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);

        /**
         * If the end user ASCCP is amended (i.e., moved to WIP state), the BIE cannot be edited. The fields of the BIE nodes are disabled including the “Used” checkbox.
         */
        node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        ASBIEPanel = editBIEPage.getASBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        assertDisabled(ASBIEPanel.getUsedCheckbox());
        assertDisabled(ASBIEPanel.getCardinalityMinField());
        assertDisabled(ASBIEPanel.getCardinalityMaxField());
        assertDisabled(ASBIEPanel.getRemarkField());
        assertDisabled(ASBIEPanel.getContextDefinitionField());

        //TODO
        // Check if Business Term functionality is enabled. Currently, it is disabled.
        assertDisabled(ASBIEPanel.getBusinessTermField());

        node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        bbiePanel = editBIEPage.getBBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        assertDisabled(bbiePanel.getNillableCheckbox());
        assertDisabled(bbiePanel.getUsedCheckbox());
        assertDisabled(bbiePanel.getCardinalityMinField());
        assertDisabled(bbiePanel.getCardinalityMaxField());
        assertDisabled(bbiePanel.getRemarkField());
        assertDisabled(bbiePanel.getExampleField());
        assertDisabled(bbiePanel.getValueConstraintSelectField());
        assertDisabled(bbiePanel.getValueDomainRestrictionSelectField());
        assertDisabled(bbiePanel.getValueDomainField());
        assertDisabled(bbiePanel.getContextDefinitionField());
        //TODO
        // Check if Business Term functionality is enabled. Currently, it is disabled.
        assertDisabled(bbiePanel.getBusinessTermField());
    }

    @Test
    @DisplayName("TC_6_2_TA_14_3")
    public void test_TA_14_3() {
        ASCCPObject asccpTopLevel;
        BCCPObject bccp_ACCTopLevel;
        ASCCPObject asccp;
        BCCPObject bccp;
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

            ACCObject accTopLevel = coreComponentAPI.createRandomACC(usera, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp_ACCTopLevel = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(accTopLevel, bccp_ACCTopLevel, "Production");

            ACCObject acc = coreComponentAPI.createRandomACC(usera, release, namespace, "Production");
            bccp = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(acc, bccp, "Production");
            asccp = coreComponentAPI.createRandomASCCP(acc, usera, namespace, "Production");
            coreComponentAPI.appendASCC(accTopLevel, asccp, "Production");

            asccpTopLevel = coreComponentAPI.createRandomASCCP(accTopLevel, usera, namespace, "Production");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
        }

        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE(asccpTopLevel.getDen(), this.release);
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().getTopLevelASBIEPByDENAndReleaseNum(asccpTopLevel.getDen(), this.release);
        /**
         * The end user ASCCP is in Production State
         */
        assertEquals("Production", asccp.getState());
        /**
         * Assert descendent nodes are editable
         */
        WebElement node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        EditBIEPage.ASBIEPanel ASBIEPanel = editBIEPage.getASBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        ASBIEPanel.toggleUsed();
        assertEnabled(ASBIEPanel.getUsedCheckbox());
        assertEnabled(ASBIEPanel.getCardinalityMinField());
        assertEnabled(ASBIEPanel.getCardinalityMaxField());
        assertEnabled(ASBIEPanel.getRemarkField());
        assertEnabled(ASBIEPanel.getContextDefinitionField());

        //TODO
        // Check if Business Term functionality is enabled. Currently, it is disabled.
        assertEnabled(ASBIEPanel.getBusinessTermField());

        node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        bbiePanel.toggleUsed();
        assertEnabled(bbiePanel.getNillableCheckbox());
        assertEnabled(bbiePanel.getUsedCheckbox());
        assertEnabled(bbiePanel.getCardinalityMinField());
        assertEnabled(bbiePanel.getCardinalityMaxField());
        assertEnabled(bbiePanel.getRemarkField());
        assertEnabled(bbiePanel.getExampleField());
        assertEnabled(bbiePanel.getValueConstraintSelectField());
        assertEnabled(bbiePanel.getValueDomainRestrictionSelectField());
        assertEnabled(bbiePanel.getValueDomainField());
        assertEnabled(bbiePanel.getContextDefinitionField());
        //TODO
        // Check if Business Term functionality is enabled. Currently, it is disabled.
        assertEnabled(bbiePanel.getBusinessTermField());
        editBIEPage.hitUpdateButton();
        homePage.logout();

        /**
         * The end user ASCCP is moved to the Deprecated state
         */
        loginPage().signIn(usera.getLoginId(), usera.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch(asccp.getDen(), this.release);
        asccpViewEditPage.hitAmendButton();
        waitFor(Duration.ofMillis(5000));
        asccpViewEditPage.toggleDeprecated();
        asccpViewEditPage.hitUpdateButton();
        asccpViewEditPage.moveToQA();
        asccpViewEditPage.moveToProduction();
        homePage.logout();

        loginPage().signIn(userb.getLoginId(), userb.getPassword());
        homePage.getBIEMenu();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPageAfterDeprecation = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);

        /**
         * If the end user ASCCP is moved to the Deprecated state (i.e., it is deprecated), flag the root node of the BIE to indicate that status.
         */
        node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        ASBIEPanel = editBIEPage.getASBIEPanel(node);
        assertDoesNotThrow(() -> {
            editBIEPageAfterDeprecation.getDeprecatedFlag();
        });
    }

    @Test
    @DisplayName("TC_6_2_TA_14_4")
    public void test_TA_14_4() {
        ASCCPObject asccpTopLevel;
        BCCPObject bccp_ACCTopLevel;
        ASCCPObject asccp;
        BCCPObject bccp;
        AppUserObject usera;
        AppUserObject userb;
        BusinessContextObject context;
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
        ACCObject acc;
        ACCObject basedACC;
        BCCPObject bccpBasedACC;
        {
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            ACCObject accTopLevel = coreComponentAPI.createRandomACC(usera, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp_ACCTopLevel = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(accTopLevel, bccp_ACCTopLevel, "Production");

            acc = coreComponentAPI.createRandomACC(usera, release, namespace, "Production");
            basedACC = coreComponentAPI.createRandomACC(usera, release, namespace, "Production");
            bccpBasedACC = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(basedACC, bccpBasedACC, "Production");
            coreComponentAPI.updateBasedACC(acc, basedACC);
            bccp = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(acc, bccp, "Production");
            asccp = coreComponentAPI.createRandomASCCP(acc, usera, namespace, "Production");
            coreComponentAPI.appendASCC(accTopLevel, asccp, "Production");

            asccpTopLevel = coreComponentAPI.createRandomASCCP(accTopLevel, usera, namespace, "Production");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
        }

        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE(asccpTopLevel.getDen(), this.release);
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().getTopLevelASBIEPByDENAndReleaseNum(asccpTopLevel.getDen(), this.release);
        /**
         * The end user ACC is in Production State
         */
        assertEquals("Production", acc.getState());
        /**
         * Assert descendent nodes are editable
         */
        WebElement node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        EditBIEPage.ASBIEPanel ASBIEPanel = editBIEPage.getASBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        ASBIEPanel.toggleUsed();
        assertEnabled(ASBIEPanel.getUsedCheckbox());
        assertEnabled(ASBIEPanel.getCardinalityMinField());
        assertEnabled(ASBIEPanel.getCardinalityMaxField());
        assertEnabled(ASBIEPanel.getRemarkField());
        assertEnabled(ASBIEPanel.getContextDefinitionField());

        //TODO
        // Check if Business Term functionality is enabled. Currently, it is disabled.
        assertEnabled(ASBIEPanel.getBusinessTermField());

        node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        bbiePanel.toggleUsed();
        assertEnabled(bbiePanel.getNillableCheckbox());
        assertEnabled(bbiePanel.getUsedCheckbox());
        assertEnabled(bbiePanel.getCardinalityMinField());
        assertEnabled(bbiePanel.getCardinalityMaxField());
        assertEnabled(bbiePanel.getRemarkField());
        assertEnabled(bbiePanel.getExampleField());
        assertEnabled(bbiePanel.getValueConstraintSelectField());
        assertEnabled(bbiePanel.getValueDomainRestrictionSelectField());
        assertEnabled(bbiePanel.getValueDomainField());
        assertEnabled(bbiePanel.getContextDefinitionField());
        //TODO
        // Check if Business Term functionality is enabled. Currently, it is disabled.
        assertEnabled(bbiePanel.getBusinessTermField());
        editBIEPage.hitUpdateButton();
        homePage.logout();

        /**
         * The base ACC of the ASCCP is amended
         */
        loginPage().signIn(usera.getLoginId(), usera.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), this.release);
        accViewEditPage.hitAmendButton();
        homePage.logout();

        loginPage().signIn(userb.getLoginId(), userb.getPassword());
        homePage.getBIEMenu();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);

        /**
         * If any of the nodes of the base ACC of the ASCCP is not in Production state, their corresponding BIE nodes cannot be edited. Check the base ACC of the base ACC of the ASCCP.
         * Also check an ASCCP and BCCP node of the base ACC of the base ACC of the ASCCP.
         */
        node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        ASBIEPanel = editBIEPage.getASBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        assertDisabled(ASBIEPanel.getUsedCheckbox());
        assertDisabled(ASBIEPanel.getCardinalityMinField());
        assertDisabled(ASBIEPanel.getCardinalityMaxField());
        assertDisabled(ASBIEPanel.getRemarkField());
        assertDisabled(ASBIEPanel.getContextDefinitionField());

        //TODO
        // Check if Business Term functionality is enabled. Currently, it is disabled.
        assertDisabled(ASBIEPanel.getBusinessTermField());

        node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        bbiePanel = editBIEPage.getBBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        assertDisabled(bbiePanel.getNillableCheckbox());
        assertDisabled(bbiePanel.getUsedCheckbox());
        assertDisabled(bbiePanel.getCardinalityMinField());
        assertDisabled(bbiePanel.getCardinalityMaxField());
        assertDisabled(bbiePanel.getRemarkField());
        assertDisabled(bbiePanel.getExampleField());
        assertDisabled(bbiePanel.getValueConstraintSelectField());
        assertDisabled(bbiePanel.getValueDomainRestrictionSelectField());
        assertDisabled(bbiePanel.getValueDomainField());
        assertDisabled(bbiePanel.getContextDefinitionField());
        //TODO
        // Check if Business Term functionality is enabled. Currently, it is disabled.
        assertDisabled(bbiePanel.getBusinessTermField());
        homePage.logout();

        /**
         * The BCCP of the base ACC of the ASCCP is amended
         */
        loginPage().signIn(usera.getLoginId(), usera.getPassword());
        coreComponentMenu = homePage.getCoreComponentMenu();
        viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), this.release);
        accViewEditPage.moveToQA();
        accViewEditPage.moveToProduction();
        viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByDenAndBranch(bccp.getDen(), this.release);
        bccpViewEditPage.hitAmendButton();
        homePage.logout();

        loginPage().signIn(userb.getLoginId(), userb.getPassword());
        homePage.getBIEMenu();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);

        /**
         * If any of the nodes of the base ACC of the ASCCP is not in Production state, their corresponding BIE nodes cannot be edited.
         */

        node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        bbiePanel = editBIEPage.getBBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        assertDisabled(bbiePanel.getNillableCheckbox());
        assertDisabled(bbiePanel.getUsedCheckbox());
        assertDisabled(bbiePanel.getCardinalityMinField());
        assertDisabled(bbiePanel.getCardinalityMaxField());
        assertDisabled(bbiePanel.getRemarkField());
        assertDisabled(bbiePanel.getExampleField());
        assertDisabled(bbiePanel.getValueConstraintSelectField());
        assertDisabled(bbiePanel.getValueDomainRestrictionSelectField());
        assertDisabled(bbiePanel.getValueDomainField());
        assertDisabled(bbiePanel.getContextDefinitionField());
        //TODO
        // Check if Business Term functionality is enabled. Currently, it is disabled.
        assertDisabled(bbiePanel.getBusinessTermField());

        node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/" + bccpBasedACC.getPropertyTerm());
        assertTrue(node.isDisplayed());
        bbiePanel = editBIEPage.getBBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        bbiePanel.toggleUsed();
        assertEnabled(bbiePanel.getNillableCheckbox());
        assertEnabled(bbiePanel.getUsedCheckbox());
        assertEnabled(bbiePanel.getCardinalityMinField());
        assertEnabled(bbiePanel.getCardinalityMaxField());
        assertEnabled(bbiePanel.getRemarkField());
        assertEnabled(bbiePanel.getExampleField());
        assertEnabled(bbiePanel.getValueConstraintSelectField());
        assertEnabled(bbiePanel.getValueDomainRestrictionSelectField());
        assertEnabled(bbiePanel.getValueDomainField());
        assertEnabled(bbiePanel.getContextDefinitionField());
        //TODO
        // Check if Business Term functionality is enabled. Currently, it is disabled.
        assertEnabled(bbiePanel.getBusinessTermField());
        homePage.logout();

        /**
         * The base ACC of the base ACC of the ASCCP is amended
         */
        loginPage().signIn(usera.getLoginId(), usera.getPassword());
        coreComponentMenu = homePage.getCoreComponentMenu();
        viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(basedACC.getDen(), this.release);
        bccpViewEditPage.hitAmendButton();
        homePage.logout();

        loginPage().signIn(userb.getLoginId(), userb.getPassword());
        homePage.getBIEMenu();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);

        /**
         * Check the base ACC of the base ACC of the ASCCP.
         * Also check an ASCCP and BCCP node of the base ACC of the base ACC of the ASCCP.
         */

        node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/" + bccpBasedACC.getPropertyTerm());
        assertTrue(node.isDisplayed());
        bbiePanel = editBIEPage.getBBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        assertDisabled(bbiePanel.getNillableCheckbox());
        assertDisabled(bbiePanel.getUsedCheckbox());
        assertDisabled(bbiePanel.getCardinalityMinField());
        assertDisabled(bbiePanel.getCardinalityMaxField());
        assertDisabled(bbiePanel.getRemarkField());
        assertDisabled(bbiePanel.getExampleField());
        assertDisabled(bbiePanel.getValueConstraintSelectField());
        assertDisabled(bbiePanel.getValueDomainRestrictionSelectField());
        assertDisabled(bbiePanel.getValueDomainField());
        assertDisabled(bbiePanel.getContextDefinitionField());
        //TODO
        // Check if Business Term functionality is enabled. Currently, it is disabled.
        assertDisabled(bbiePanel.getBusinessTermField());
        homePage.logout();

        /**
         * The BCCP of the base ACC of the base ACC of the ASCCP is amended
         */
        loginPage().signIn(usera.getLoginId(), usera.getPassword());
        coreComponentMenu = homePage.getCoreComponentMenu();
        viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(basedACC.getDen(), this.release);
        accViewEditPage.moveToQA();
        accViewEditPage.moveToProduction();
        viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByDenAndBranch(bccpBasedACC.getDen(), this.release);
        bccpViewEditPage.hitAmendButton();
        homePage.logout();

        loginPage().signIn(userb.getLoginId(), userb.getPassword());
        homePage.getBIEMenu();
        viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelASBIEP);

        /**
         * Check the base ACC of the base ACC of the ASCCP.
         * Also check an ASCCP and BCCP node of the base ACC of the base ACC of the ASCCP.
         */

        node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/" + bccpBasedACC.getPropertyTerm());
        assertTrue(node.isDisplayed());
        bbiePanel = editBIEPage.getBBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        assertDisabled(bbiePanel.getNillableCheckbox());
        assertDisabled(bbiePanel.getUsedCheckbox());
        assertDisabled(bbiePanel.getCardinalityMinField());
        assertDisabled(bbiePanel.getCardinalityMaxField());
        assertDisabled(bbiePanel.getRemarkField());
        assertDisabled(bbiePanel.getExampleField());
        assertDisabled(bbiePanel.getValueConstraintSelectField());
        assertDisabled(bbiePanel.getValueDomainRestrictionSelectField());
        assertDisabled(bbiePanel.getValueDomainField());
        assertDisabled(bbiePanel.getContextDefinitionField());
        //TODO
        // Check if Business Term functionality is enabled. Currently, it is disabled.
        assertDisabled(bbiePanel.getBusinessTermField());
    }

    @Test
    @DisplayName("TC_6_2_TA_14_5")
    @Disabled
    public void test_TA_14_5() {
        /**
         * The user cannot delete CC on which some BIE was previously created.
         */
    }

    @Test
    @DisplayName("TC_6_2_TA_14_6")
    @Disabled
    public void test_TA_14_6() {
        ASCCPObject asccpTopLevel;
        BCCPObject bccp_ACCTopLevel;
        ASCCPObject asccp;
        ACCObject accGroupType;
        BCCPObject bccpFromtheGroup;
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

            ACCObject accTopLevel = coreComponentAPI.createRandomACC(usera, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp_ACCTopLevel = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(accTopLevel, bccp_ACCTopLevel, "Production");

            accGroupType = coreComponentAPI.createRandomACCSemanticGroupType(usera, release, namespace, "WIP");
            bccpFromtheGroup = coreComponentAPI.createRandomBCCP(dataType, usera, namespace, "Production");
            coreComponentAPI.appendBCC(accGroupType, bccpFromtheGroup, "Production");
            asccp = coreComponentAPI.createRandomASCCP(accGroupType, usera, namespace, "Production");
            coreComponentAPI.appendASCC(accTopLevel, asccp, "Production");

            asccpTopLevel = coreComponentAPI.createRandomASCCP(accTopLevel, usera, namespace, "Production");

            context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
        }

        HomePage homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage = viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE(asccpTopLevel.getDen(), this.release);
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().getTopLevelASBIEPByDENAndReleaseNum(asccpTopLevel.getDen(), this.release);
        /**
         * The end user ACC is group type and not in Production State
         */
        assertNotEquals("Production", accGroupType.getState());
        /**
         * If any child or descendant properties are from group and the group is not in Production state, those properties have to be locked in the BIE.
         */
        assertThrows(TimeoutException.class, () -> {
            editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/" + bccpFromtheGroup.getPropertyTerm());
        });
        WebElement node = editBIEPage.getNodeByPath(
                "/" + bccpFromtheGroup.getPropertyTerm());
        assertTrue(node.isDisplayed());
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        assertDisabled(bbiePanel.getNillableCheckbox());
        assertDisabled(bbiePanel.getUsedCheckbox());
        assertDisabled(bbiePanel.getCardinalityMinField());
        assertDisabled(bbiePanel.getCardinalityMaxField());
        assertDisabled(bbiePanel.getRemarkField());
        assertDisabled(bbiePanel.getExampleField());
        assertDisabled(bbiePanel.getValueConstraintSelectField());
        assertDisabled(bbiePanel.getValueDomainRestrictionSelectField());
        assertDisabled(bbiePanel.getValueDomainField());
        assertDisabled(bbiePanel.getContextDefinitionField());
        //TODO
        // Check if Business Term functionality is enabled. Currently, it is disabled.
        assertDisabled(bbiePanel.getBusinessTermField());
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
