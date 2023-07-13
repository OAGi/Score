package org.oagi.score.e2e.TS_6_EndUserAccessRightScoreCoreFunctions;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
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
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

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
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
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
            TopLevelASBIEPObject useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");
            biesForTesting.add(useraBIEWIP);

            TopLevelASBIEPObject useraBIEProduction = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "Production");
            biesForTesting.add(useraBIEProduction);

            TopLevelASBIEPObject useraBIEQA = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "QA");
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
            useraBIE = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(contextFirstUser), asccp, usera, "WIP");

            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            BusinessContextObject contextSecondUser = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            userbBIE = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(contextSecondUser), asccp, userb, "WIP");
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
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
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
            TopLevelASBIEPObject topLevelAsbiepWIP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");
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
            waitFor(Duration.ofMillis(2000));
            assertEquals("WIP", accExtensionViewEditPage.getStateFieldValue());

            accExtensionViewEditPage.moveToQA();
            waitFor(Duration.ofMillis(2000));
            assertEquals("QA", accExtensionViewEditPage.getStateFieldValue());

            accExtensionViewEditPage.moveToProduction();
            waitFor(Duration.ofMillis(2000));
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
            useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");

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

        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.
                extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.
                appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
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
        List<ASCCPObject> asccpsForTesting = new ArrayList<>();
        List<BCCPObject> bccpsForTesting = new ArrayList<>();
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
            useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
        assertEquals("WIP", useraBIEWIP.getState());
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.
                extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        for (ASCCPObject asccpToAppend : asccpsForTesting) {
            /**
             * It has child association to an end user ASCCP which is not in Production state
             */
            assertNotEquals("Production", asccpToAppend.getState());
            SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.
                    appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
            selectCCPropertyPage.selectAssociation(asccpToAppend.getDen());
        }
        for (BCCPObject bccpToAppend : bccpsForTesting) {
            /**
             * It has child association to an end user BCCP which is not in Production state
             */
            assertNotEquals("Production", bccpToAppend.getState());
            SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.
                    appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
            selectCCPropertyPage.selectAssociation(bccpToAppend.getDen());
        }

        ACCExtensionViewEditPage.setNamespace(namespaceEU);
        ACCExtensionViewEditPage.hitUpdateButton();
        ACCExtensionViewEditPage.moveToQA();
        /**
         * there is a corresponding UEGACC in Production state
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
            // TODO
            // Check if Business Term functionality is enabled. Currently, it is disabled.
            if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
                // TODO:
                // Check business term abilities are disabled
            } else {
                assertDisabled(BBIEPPanel.getBusinessTermField());
            }
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
            // TODO
            // Check if Business Term functionality is enabled. Currently, it is disabled.
            if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
                // TODO:
                // Check business term abilities are disabled
            } else {
                assertDisabled(ASBIEPanel.getBusinessTermField());
            }

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
            // TODO
            // Check if Business Term functionality is enabled. Currently, it is disabled.
            if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
                // TODO:
                // Check business term abilities are disabled
            } else {
                assertDisabled(BBIEPPanel.getBusinessTermField());
            }
        }
    }

    @Test
    @DisplayName("TC_6_2_TA_5_2")
    public void test_TA_5_2() {
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject usera;
        TopLevelASBIEPObject useraBIEWIP;
        List<ASCCPObject> asccpsForTesting = new ArrayList<>();
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
            ASCCPObject asccpToAppendProduction = coreComponentAPI.
                    createRandomASCCP(accToAppend, endUserForCC, namespace, "Production");
            asccpsForTesting.add(asccpToAppendProduction);
            asccpACCMap.put(asccpToAppendProduction, accToAppend);

            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
            thisAccountWillBeDeletedAfterTests(usera);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
        assertEquals("WIP", useraBIEWIP.getState());
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.
                extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");

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
            SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.
                    appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
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
            if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
                // TODO:
                // Check business term abilities are disabled
            } else {
                assertDisabled(ASBIEPanel.getBusinessTermField());
            }

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
            if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
                // TODO:
                // Check business term abilities are disabled
            } else {
                assertDisabled(ASBIEPanel.getBusinessTermField());
            }
        }
    }

    @Test
    @DisplayName("TC_6_2_TA_5_4")
    public void test_TA_5_4() {
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject usera;
        TopLevelASBIEPObject useraBIEWIP;
        List<ASCCPObject> asccpsForTesting = new ArrayList<>();
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
            useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
        assertEquals("WIP", useraBIEWIP.getState());
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.
                extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");

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
            SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.
                    appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
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
            if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
                // TODO:
                // Check business term abilities are disabled
            } else {
                assertDisabled(ASBIEPanel.getBusinessTermField());
            }

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
            if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
                // TODO:
                // Check business term abilities are disabled
            } else {
                assertDisabled(ASBIEPanel.getBusinessTermField());
            }

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
            if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
                // TODO:
                // Check business term abilities are disabled
            } else {
                assertDisabled(ASBIEPanel.getBusinessTermField());
            }
        }
    }

    @Test
    @DisplayName("TC_6_2_TA_5_5")
    public void test_TA_5_5() {
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject usera;
        TopLevelASBIEPObject useraBIEWIP;
        List<ASCCPObject> asccpsForTesting = new ArrayList<>();
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
            useraBIEWIP = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");

        }
        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
        assertEquals("WIP", useraBIEWIP.getState());
        // TODO:
        // Can't open the context menu in a small size of the screen.
        ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.
                extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");

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
            SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.
                    appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
            selectCCPropertyPage.selectAssociation(asccpToAppend.getDen());
        }

        ACCExtensionViewEditPage.setNamespace(namespaceEU);
        ACCExtensionViewEditPage.hitUpdateButton();
        ACCExtensionViewEditPage.moveToQA();
        /**
         * there is a corresponding UEGACC in Production state
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
            if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
                // TODO:
                // Check business term abilities are disabled
            } else {
                assertDisabled(BBIEPPanel.getBusinessTermField());
            }

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
            if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
                // TODO:
                // Check business term abilities are disabled
            } else {
                assertDisabled(ASBIEPanel.getBusinessTermField());
            }
        }
    }

    @Test
    @DisplayName("TC_6_2_TA_6_1")
    public void test_TA_6_1() {
        Map<TopLevelASBIEPObject, ASCCPObject> bieASCCPMap = new HashMap<>();
        AppUserObject usera;
        NamespaceObject useraNamespace;
        Map<ASCCPObject, BCCPObject> asccpBCCPMap = new HashMap<>();
        Map<BCCPObject, BCCObject> bccpBCCMap = new HashMap<>();
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
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
            DTObject dataTypeReleaseOne = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", this.release);
            BCCPObject bccpReleaseOne = coreComponentAPI.createRandomBCCP(dataTypeReleaseOne, developer, namespace, "Published");
            BCCObject bccReleaseOne = coreComponentAPI.appendBCC(accReleaseOne, bccpReleaseOne, "Published");
            bccReleaseOne.setDefinitionSource("bcc definition source");
            bccReleaseOne.setCardinalityMax(5);
            bccReleaseOne.setCardinalityMin(1);
            coreComponentAPI.updateBCC(bccReleaseOne);
            bccpBCCMap.put(bccpReleaseOne, bccReleaseOne);

            ASCCPObject asccpReleaseOne = coreComponentAPI.createRandomASCCP(accReleaseOne, developer, namespace, "Published");
            asccpReleaseOne.setDefinition(accReleaseOne.getDefinition());
            asccpBCCPMap.put(asccpReleaseOne, bccpReleaseOne);
            coreComponentAPI.updateASCCP(asccpReleaseOne);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            useraNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
            thisAccountWillBeDeletedAfterTests(usera);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            TopLevelASBIEPObject useraBIEWIPReleaseOne = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseOne, usera, "WIP");
            biesForTesting.add(useraBIEWIPReleaseOne);
            bieASCCPMap.put(useraBIEWIPReleaseOne, asccpReleaseOne);

            TopLevelASBIEPObject useraBIEProductionReleaseOne = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseOne, usera, "Production");
            biesForTesting.add(useraBIEProductionReleaseOne);
            bieASCCPMap.put(useraBIEProductionReleaseOne, asccpReleaseOne);

            TopLevelASBIEPObject useraBIEQAReleaseOne = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseOne, usera, "QA");
            biesForTesting.add(useraBIEQAReleaseOne);
            bieASCCPMap.put(useraBIEQAReleaseOne, asccpReleaseOne);

            // create the revision in another release
            ACCObject accReleaseTwo = coreComponentAPI.createRevisedACC(accReleaseOne, developer, releaseTwo, "Published");
            coreComponentAPI.appendExtension(accReleaseTwo, developer, namespace, "Published");
            accReleaseTwo.setDefinition("definition 2");
            coreComponentAPI.updateACC(accReleaseTwo);
            DTObject dataTypeReleaseTwo = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", releaseTwo.getReleaseNumber());
            BCCPObject bccpReleaseTwo = coreComponentAPI.createRandomBCCP(dataTypeReleaseTwo, developer, namespace, "Published");
            BCCObject bccReleaseTwo = coreComponentAPI.appendBCC(accReleaseTwo, bccpReleaseTwo, "Published");
            bccReleaseTwo.setDefinitionSource("bcc definition source");
            bccReleaseTwo.setCardinalityMax(3);
            bccReleaseTwo.setCardinalityMin(3);
            coreComponentAPI.updateBCC(bccReleaseTwo);
            bccpBCCMap.put(bccpReleaseTwo, bccReleaseTwo);

            ASCCPObject asccpReleaseTwo = coreComponentAPI.createRandomASCCP(accReleaseTwo, developer, namespace, "Published");
            asccpReleaseTwo.setDefinition(accReleaseTwo.getDefinition());
            asccpBCCPMap.put(asccpReleaseTwo, bccpReleaseTwo);
            coreComponentAPI.updateASCCP(asccpReleaseTwo);

            TopLevelASBIEPObject useraBIEWIPReleaseTwo = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseTwo, usera, "WIP");
            biesForTesting.add(useraBIEWIPReleaseTwo);
            bieASCCPMap.put(useraBIEWIPReleaseTwo, asccpReleaseTwo);

            TopLevelASBIEPObject useraBIEProductionReleaseTwo = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseTwo, usera, "Production");
            biesForTesting.add(useraBIEProductionReleaseTwo);
            bieASCCPMap.put(useraBIEProductionReleaseTwo, asccpReleaseTwo);

            TopLevelASBIEPObject useraBIEQAReleaseTwo = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseTwo, usera, "QA");
            biesForTesting.add(useraBIEQAReleaseTwo);
            bieASCCPMap.put(useraBIEQAReleaseTwo, asccpReleaseTwo);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            BIEMenu bieMenu = homePage.getBIEMenu();
            ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
            EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
            ASCCPObject asccp = bieASCCPMap.get(topLevelAsbiep);
            /**
             * Assert that Type Definition field in BIE has the same value as ASCCP's definition on which it is based.
             * Note that there are two ASCCPs in two releases having different definitions
             */
            String ASCCPDefinition = editBIEPage.getTypeDefinitionValue();
            assertEquals(asccp.getDefinition(), ASCCPDefinition);
            BCCPObject bccp = asccpBCCPMap.get(asccp);
            BCCObject bcc = bccpBCCMap.get(bccp);
            WebElement node = editBIEPage.getNodeByPath(
                    "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
            assertTrue(node.isDisplayed());
            EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
            int originalCardinalityMin = Integer.valueOf(getText(bbiePanel.getCardinalityMinField()));
            int originalCardinalityMax = Integer.valueOf(getText(bbiePanel.getCardinalityMaxField()));
            assertEquals(bcc.getCardinalityMin(), originalCardinalityMin);
            assertEquals(bcc.getCardinalityMax(), originalCardinalityMax);
            if (topLevelAsbiep.getState().equals("WIP")) {
                ACCExtensionViewEditPage accExtensionViewEditPage =
                        editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
                String revision = getText(accExtensionViewEditPage.getRevisionField());
                assertEquals("1", revision);

                accExtensionViewEditPage.setNamespace(useraNamespace);
                accExtensionViewEditPage.setDefinition(ASCCPDefinition);
                accExtensionViewEditPage.hitUpdateButton();

                accExtensionViewEditPage.moveToQA();
                accExtensionViewEditPage.moveToProduction();
                viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
                editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
                accExtensionViewEditPage = editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
                revision = getText(accExtensionViewEditPage.getRevisionField());
                assertEquals("2", revision);
                assertEquals(ASCCPDefinition, getText(accExtensionViewEditPage.getDefinitionField()));
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
    @DisplayName("TC_6_2_TA_6_2")
    public void test_TA_6_2() {
        AppUserObject usera;
        AppUserObject userb;
        TopLevelASBIEPObject useraBIEReleaseOne;
        TopLevelASBIEPObject userbBIEReleaseOne;
        TopLevelASBIEPObject useraBIEReleaseTwo;
        TopLevelASBIEPObject userbBIEReleaseTwo;
        NamespaceObject useraNamespace;
        Map<TopLevelASBIEPObject, ASCCPObject> bieASCCPMap = new HashMap<>();
        Map<ASCCPObject, BCCPObject> asccpBCCPMap = new HashMap<>();
        Map<BCCPObject, BCCObject> bccpBCCMap = new HashMap<>();
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
            DTObject dataTypeReleaseOne = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", this.release);
            BCCPObject bccpReleaseOne = coreComponentAPI.createRandomBCCP(dataTypeReleaseOne, developer, namespace, "Published");
            BCCObject bccReleaseOne = coreComponentAPI.appendBCC(accReleaseOne, bccpReleaseOne, "Published");
            bccReleaseOne.setCardinalityMax(5);
            bccReleaseOne.setCardinalityMin(1);
            coreComponentAPI.updateBCC(bccReleaseOne);
            bccpBCCMap.put(bccpReleaseOne, bccReleaseOne);

            ASCCPObject asccpReleaseOne = coreComponentAPI.createRandomASCCP(accReleaseOne, developer, namespace, "Published");
            asccpReleaseOne.setDefinition(accReleaseOne.getDefinition());
            asccpBCCPMap.put(asccpReleaseOne, bccpReleaseOne);
            coreComponentAPI.updateASCCP(asccpReleaseOne);

            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            useraNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
            thisAccountWillBeDeletedAfterTests(usera);

            BusinessContextObject contextFirstUser = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            useraBIEReleaseOne = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(contextFirstUser), asccpReleaseOne, usera, "WIP");
            bieASCCPMap.put(useraBIEReleaseOne, asccpReleaseOne);
            userb = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(userb);

            BusinessContextObject contextSecondUser = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(userb);
            userbBIEReleaseOne = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(contextSecondUser), asccpReleaseOne, userb, "WIP");
            bieASCCPMap.put(userbBIEReleaseOne, asccpReleaseOne);

            // create the revision in another release
            ACCObject accReleaseTwo = coreComponentAPI.createRevisedACC(accReleaseOne, developer, releaseTwo, "Published");
            coreComponentAPI.appendExtension(accReleaseTwo, developer, namespace, "Published");
            accReleaseTwo.setDefinition("definition 2");
            coreComponentAPI.updateACC(accReleaseTwo);
            DTObject dataTypeReleaseTwo = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", releaseTwo.getReleaseNumber());
            BCCPObject bccpReleaseTwo = coreComponentAPI.createRandomBCCP(dataTypeReleaseTwo, developer, namespace, "Published");
            BCCObject bccReleaseTwo = coreComponentAPI.appendBCC(accReleaseTwo, bccpReleaseTwo, "Published");
            bccReleaseTwo.setDefinitionSource("bcc definition source");
            bccReleaseTwo.setCardinalityMax(3);
            bccReleaseTwo.setCardinalityMin(3);
            coreComponentAPI.updateBCC(bccReleaseTwo);
            bccpBCCMap.put(bccpReleaseTwo, bccReleaseTwo);

            ASCCPObject asccpReleaseTwo = coreComponentAPI.createRandomASCCP(accReleaseTwo, developer, namespace, "Published");
            asccpReleaseTwo.setDefinition(accReleaseTwo.getDefinition());
            asccpBCCPMap.put(asccpReleaseTwo, bccpReleaseTwo);
            coreComponentAPI.updateASCCP(asccpReleaseTwo);

            useraBIEReleaseTwo = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(contextFirstUser), asccpReleaseTwo, usera, "WIP");
            bieASCCPMap.put(useraBIEReleaseTwo, asccpReleaseTwo);
            userbBIEReleaseTwo = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(contextSecondUser), asccpReleaseTwo, userb, "WIP");
            bieASCCPMap.put(userbBIEReleaseTwo, asccpReleaseTwo);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEReleaseOne);
        assertEquals("WIP", useraBIEReleaseOne.getState());
        ASCCPObject asccp = bieASCCPMap.get(useraBIEReleaseOne);
        /**
         * Assert that Type Definition field in BIE has the same value as ASCCP's definition on which it is based.
         * Note that there are two ASCCPs in two releases having different definitions
         */
        String ASCCPDefinition = editBIEPage.getTypeDefinitionValue();
        assertEquals(asccp.getDefinition(), ASCCPDefinition);
        BCCPObject bccp = asccpBCCPMap.get(asccp);
        BCCObject bcc = bccpBCCMap.get(bccp);
        WebElement node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
        int originalCardinalityMin = Integer.valueOf(getText(bbiePanel.getCardinalityMinField()));
        int originalCardinalityMax = Integer.valueOf(getText(bbiePanel.getCardinalityMaxField()));
        assertEquals(bcc.getCardinalityMin(), originalCardinalityMin);
        assertEquals(bcc.getCardinalityMax(), originalCardinalityMax);
        ACCExtensionViewEditPage accExtensionViewEditPage =
                editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        accExtensionViewEditPage.setNamespace(useraNamespace);
        accExtensionViewEditPage.setDefinition(ASCCPDefinition);
        accExtensionViewEditPage.hitUpdateButton();
        accExtensionViewEditPage.moveToQA();
        assertEquals("QA", accExtensionViewEditPage.getStateFieldValue());
        homePage.logout();

        homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(userbBIEReleaseOne);
        assertEquals("WIP", userbBIEReleaseOne.getState());
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
        assertEquals(ASCCPDefinition, getText(ACCExtensionViewEditPage.getDefinitionField()));
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
        getDriver().close();
        switchToMainTab(getDriver());
        homePage.logout();

        homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEReleaseOne);
        ACCExtensionViewEditPage ACCExtensionViewEditPageTwo = editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        editBIEPage.backToWIP();
        homePage.logout();

        homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(userbBIEReleaseOne);
        editBIEPage.getExtendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        assertEquals("Editing extension already exist.", getSnackBarMessage(getDriver()));

        //Try everything with the another release

        viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(userbBIEReleaseTwo);

        assertEquals("WIP", userbBIEReleaseTwo.getState());
        asccp = bieASCCPMap.get(userbBIEReleaseTwo);
        /**
         * Assert that Type Definition field in BIE has the same value as ASCCP's definition on which it is based.
         * Note that there are two ASCCPs in two releases having different definitions
         */
        ASCCPDefinition = editBIEPage.getTypeDefinitionValue();
        assertEquals(asccp.getDefinition(), ASCCPDefinition);
        bccp = asccpBCCPMap.get(asccp);
        bcc = bccpBCCMap.get(bccp);
        node = editBIEPage.getNodeByPath(
                "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        bbiePanel = editBIEPage.getBBIEPanel(node);
        originalCardinalityMin = Integer.valueOf(getText(bbiePanel.getCardinalityMinField()));
        originalCardinalityMax = Integer.valueOf(getText(bbiePanel.getCardinalityMaxField()));
        assertEquals(bcc.getCardinalityMin(), originalCardinalityMin);
        assertEquals(bcc.getCardinalityMax(), originalCardinalityMax);
        ACCExtensionViewEditPage ACCExtensionViewEditPageThree =
                editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        ACCExtensionViewEditPageThree.setNamespace(useraNamespace);
        ACCExtensionViewEditPageThree.setDefinition(ASCCPDefinition);
        ACCExtensionViewEditPageThree.hitUpdateButton();
        ACCExtensionViewEditPageThree.moveToQA();
        assertEquals("QA", ACCExtensionViewEditPageThree.getStateFieldValue());
        homePage.logout();

        homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEReleaseTwo);
        assertEquals("WIP", useraBIEReleaseTwo.getState());
        editBIEPage.getExtendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        /**
         * Display a dialog indicating “The core component is being extended by “ + [the owner of the UEGACC] or similar.
         */
        assertEquals("Another user is working on the extension.",
                editBIEPage.getAttentionDialogMessage());
        ACCExtensionViewEditPage ACCExtensionViewEditPageFour = editBIEPage.continueToExtendBIEOnNode();
        /**
         * If the UEGACC is in QA state, the end user can view its details but cannot make any change.
         */
        assertEquals("QA", ACCExtensionViewEditPageFour.getStateFieldValue());
        assertEquals(ASCCPDefinition, getText(ACCExtensionViewEditPageFour.getDefinitionField()));
        assertThrows(TimeoutException.class, () -> {
            ACCExtensionViewEditPageFour.getUpdateButton(false);
        });
        assertThrows(TimeoutException.class, () -> {
            ACCExtensionViewEditPageFour.getMoveToQAButton(false);
        });
        assertThrows(TimeoutException.class, () -> {
            ACCExtensionViewEditPageFour.getMoveToProductionButton(false);
        });
        assertEquals(userb.getLoginId(), ACCExtensionViewEditPageFour.getOwnerFieldValue());
        assertDisabled(ACCExtensionViewEditPageFour.getDefinitionField());
        assertDisabled(ACCExtensionViewEditPageFour.getObjectClassTermField());
        assertDisabled(ACCExtensionViewEditPageFour.getDefinitionSourceField());
        getDriver().close();
        switchToMainTab(getDriver());
        homePage.logout();

        homePage = loginPage().signIn(userb.getLoginId(), userb.getPassword());
        viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(userbBIEReleaseTwo);
        ACCExtensionViewEditPage ACCExtensionViewEditPageFive = editBIEPage.
                extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        editBIEPage.backToWIP();
        homePage.logout();

        homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEReleaseTwo);
        editBIEPage.getExtendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        assertEquals("Editing extension already exist.", getSnackBarMessage(getDriver()));
    }

    @Test
    @DisplayName("TC_6_2_TA_6_3")
    public void test_TA_6_3() {
        Map<TopLevelASBIEPObject, ASCCPObject> bieASCCPMap = new HashMap<>();
        Map<ASCCPObject, BCCPObject> asccpBCCPMap = new HashMap<>();
        Map<BCCPObject, BCCObject> bccpBCCMap = new HashMap<>();
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        NamespaceObject useraNamespace;
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
            DTObject dataTypeReleaseOne = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", this.release);
            BCCPObject bccpReleaseOne = coreComponentAPI.createRandomBCCP(dataTypeReleaseOne, developer, namespace, "Published");
            BCCObject bccReleaseOne = coreComponentAPI.appendBCC(accReleaseOne, bccpReleaseOne, "Published");
            bccReleaseOne.setCardinalityMax(5);
            bccReleaseOne.setCardinalityMin(1);
            coreComponentAPI.updateBCC(bccReleaseOne);
            bccpBCCMap.put(bccpReleaseOne, bccReleaseOne);

            ASCCPObject asccpReleaseOne = coreComponentAPI.createRandomASCCP(accReleaseOne, developer, namespace, "Published");
            asccpReleaseOne.setDefinition(accReleaseOne.getDefinition());
            asccpBCCPMap.put(asccpReleaseOne, bccpReleaseOne);
            coreComponentAPI.updateASCCP(asccpReleaseOne);

            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);
            useraNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            TopLevelASBIEPObject topLevelAsbiepWIPReleaseOne = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseOne, usera, "WIP");
            biesForTesting.add(topLevelAsbiepWIPReleaseOne);
            bieASCCPMap.put(topLevelAsbiepWIPReleaseOne, asccpReleaseOne);

            // create the revision in another release
            ACCObject accReleaseTwo = coreComponentAPI.createRevisedACC(accReleaseOne, developer, releaseTwo, "Published");
            coreComponentAPI.appendExtension(accReleaseTwo, developer, namespace, "Published");
            accReleaseTwo.setDefinition("definition 2");
            coreComponentAPI.updateACC(accReleaseTwo);
            DTObject dataTypeReleaseTwo = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", releaseTwo.getReleaseNumber());
            BCCPObject bccpReleaseTwo = coreComponentAPI.createRandomBCCP(dataTypeReleaseTwo, developer, namespace, "Published");
            BCCObject bccReleaseTwo = coreComponentAPI.appendBCC(accReleaseTwo, bccpReleaseTwo, "Published");
            bccReleaseTwo.setDefinitionSource("bcc definition source");
            bccReleaseTwo.setCardinalityMax(3);
            bccReleaseTwo.setCardinalityMin(3);
            coreComponentAPI.updateBCC(bccReleaseTwo);
            bccpBCCMap.put(bccpReleaseTwo, bccReleaseTwo);

            ASCCPObject asccpReleaseTwo = coreComponentAPI.createRandomASCCP(accReleaseTwo, developer, namespace, "Published");
            asccpReleaseTwo.setDefinition(accReleaseTwo.getDefinition());
            asccpBCCPMap.put(asccpReleaseTwo, bccpReleaseTwo);
            coreComponentAPI.updateASCCP(asccpReleaseTwo);

            TopLevelASBIEPObject topLevelAsbiepWIPReleaseTwo = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseTwo, usera, "WIP");
            biesForTesting.add(topLevelAsbiepWIPReleaseTwo);
            bieASCCPMap.put(topLevelAsbiepWIPReleaseTwo, asccpReleaseTwo);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            BIEMenu bieMenu = homePage.getBIEMenu();
            ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
            EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
            assertEquals("WIP", topLevelAsbiep.getState());
            ASCCPObject asccp = bieASCCPMap.get(topLevelAsbiep);
            /**
             * Assert that Type Definition field in BIE has the same value as ASCCP's definition on which it is based.
             * Note that there are two ASCCPs in two releases having different definitions
             */
            String ASCCPDefinition = editBIEPage.getTypeDefinitionValue();
            assertEquals(asccp.getDefinition(), ASCCPDefinition);
            BCCPObject bccp = asccpBCCPMap.get(asccp);
            BCCObject bcc = bccpBCCMap.get(bccp);
            WebElement node = editBIEPage.getNodeByPath(
                    "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
            assertTrue(node.isDisplayed());
            EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
            int originalCardinalityMin = Integer.valueOf(getText(bbiePanel.getCardinalityMinField()));
            int originalCardinalityMax = Integer.valueOf(getText(bbiePanel.getCardinalityMaxField()));
            assertEquals(bcc.getCardinalityMin(), originalCardinalityMin);
            assertEquals(bcc.getCardinalityMax(), originalCardinalityMax);

            ACCExtensionViewEditPage accExtensionViewEditPage =
                    editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");

            assertEnabled(accExtensionViewEditPage.getDefinitionSourceField());
            assertEnabled(accExtensionViewEditPage.getDefinitionField());

            accExtensionViewEditPage.setNamespace(useraNamespace);
            accExtensionViewEditPage.setDefinition(ASCCPDefinition);
            accExtensionViewEditPage.hitUpdateButton();

            accExtensionViewEditPage.moveToQA();
            assertEquals("QA", accExtensionViewEditPage.getStateFieldValue());

            viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
            editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
            accExtensionViewEditPage = editBIEPage.
                    extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");

            assertDisabled(accExtensionViewEditPage.getDefinitionSourceField());
            assertDisabled(accExtensionViewEditPage.getDefinitionField());
            assertEquals(ASCCPDefinition, getText(accExtensionViewEditPage.getDefinitionField()));

            accExtensionViewEditPage.backToWIP();
            waitFor(Duration.ofMillis(2000));
            assertEquals("WIP", accExtensionViewEditPage.getStateFieldValue());

            accExtensionViewEditPage.moveToQA();
            waitFor(Duration.ofMillis(2000));
            assertEquals("QA", accExtensionViewEditPage.getStateFieldValue());

            accExtensionViewEditPage.moveToProduction();
            waitFor(Duration.ofMillis(2000));
            assertEquals("Production", accExtensionViewEditPage.getStateFieldValue());
        }
    }

    @Test
    @DisplayName("TC_6_2_TA_6_4")
    public void test_TA_6_4() {
        Map<TopLevelASBIEPObject, ASCCPObject> bieASCCPMap = new HashMap<>();
        Map<ASCCPObject, BCCPObject> asccpBCCPMap = new HashMap<>();
        Map<BCCPObject, BCCObject> bccpBCCMap = new HashMap<>();
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        ASCCPObject asccpToAppend;
        BCCPObject bccpToAppend;
        ACCObject accToAppend;
        Map<ACCObject, BCCPObject> accBCCPMap = new HashMap<>();
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
            DTObject dataTypeReleaseOne = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", releaseOne.getReleaseNumber());
            DTObject dataTypeReleaseTwo = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", releaseTwo.getReleaseNumber());
            BCCPObject bccpReleaseOne = coreComponentAPI.createRandomBCCP(dataTypeReleaseOne, developer, namespace, "Published");
            BCCObject bccReleaseOne = coreComponentAPI.appendBCC(accReleaseOne, bccpReleaseOne, "Published");
            bccReleaseOne.setCardinalityMax(5);
            bccReleaseOne.setCardinalityMin(1);
            coreComponentAPI.updateBCC(bccReleaseOne);
            bccpBCCMap.put(bccpReleaseOne, bccReleaseOne);

            ASCCPObject asccpReleaseOne = coreComponentAPI.createRandomASCCP(accReleaseOne, developer, namespace, "Published");
            asccpReleaseOne.setDefinition(accReleaseOne.getDefinition());
            asccpBCCPMap.put(asccpReleaseOne, bccpReleaseOne);
            coreComponentAPI.updateASCCP(asccpReleaseOne);

            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(usera);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            TopLevelASBIEPObject useraBIEWIPReleaseOne = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseOne, usera, "WIP");
            biesForTesting.add(useraBIEWIPReleaseOne);
            bieASCCPMap.put(useraBIEWIPReleaseOne, asccpReleaseOne);

            accToAppend = coreComponentAPI.createRandomACC(developer, releaseOne, namespace, "Published");
            ACCObject accToAppendRevised = coreComponentAPI.createRevisedACC(accToAppend, developer, releaseTwo, "Published");

            BCCPObject bccp = coreComponentAPI.createRandomBCCP(dataTypeReleaseOne, developer, namespace, "Published");
            BCCPObject bccpRevised = coreComponentAPI.createRevisedBCCP(bccp, dataTypeReleaseTwo, developer, releaseTwo, "Published");
            coreComponentAPI.appendBCC(accToAppend, bccp, "Published");
            coreComponentAPI.appendBCC(accToAppendRevised, bccpRevised, "Published");
            accBCCPMap.put(accToAppend, bccp);
            accBCCPMap.put(accToAppendRevised, bccpRevised);
            asccpToAppend = coreComponentAPI.createRandomASCCP(accToAppend, developer, namespace, "Published");
            coreComponentAPI.createRevisedASCCP(asccpToAppend, accToAppendRevised, developer, releaseTwo, "Published");

            bccpToAppend = coreComponentAPI.createRandomBCCP(dataTypeReleaseOne, developer, namespace, "Published");

            coreComponentAPI.createRevisedBCCP(bccpToAppend, dataTypeReleaseTwo, developer, releaseTwo, "Published");

            // create the revision in another release
            ACCObject accReleaseTwo = coreComponentAPI.createRevisedACC(accReleaseOne, developer, releaseTwo, "Published");
            coreComponentAPI.appendExtension(accReleaseTwo, developer, namespace, "Published");
            accReleaseTwo.setDefinition("definition 2");
            coreComponentAPI.updateACC(accReleaseTwo);
            BCCPObject bccpReleaseTwo = coreComponentAPI.createRandomBCCP(dataTypeReleaseTwo, developer, namespace, "Published");
            BCCObject bccReleaseTwo = coreComponentAPI.appendBCC(accReleaseTwo, bccpReleaseTwo, "Published");
            bccReleaseTwo.setDefinitionSource("bcc definition source");
            bccReleaseTwo.setCardinalityMax(3);
            bccReleaseTwo.setCardinalityMin(3);
            coreComponentAPI.updateBCC(bccReleaseTwo);
            bccpBCCMap.put(bccpReleaseTwo, bccReleaseTwo);

            ASCCPObject asccpReleaseTwo = coreComponentAPI.createRandomASCCP(accReleaseTwo, developer, namespace, "Published");
            asccpReleaseTwo.setDefinition(accReleaseTwo.getDefinition());
            asccpBCCPMap.put(asccpReleaseTwo, bccpReleaseTwo);
            coreComponentAPI.updateASCCP(asccpReleaseTwo);

            TopLevelASBIEPObject useraBIEWIPReleaseTwo = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseTwo, usera, "WIP");
            biesForTesting.add(useraBIEWIPReleaseTwo);
            bieASCCPMap.put(useraBIEWIPReleaseTwo, asccpReleaseTwo);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        for (TopLevelASBIEPObject useraBIEWIP : biesForTesting) {
            EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
            assertEquals("WIP", useraBIEWIP.getState());
            ASCCPObject asccp = bieASCCPMap.get(useraBIEWIP);
            /**
             * Assert that Type Definition field in BIE has the same value as ASCCP's definition on which it is based.
             * Note that there are two ASCCPs in two releases having different definitions
             */
            String ASCCPDefinition = editBIEPage.getTypeDefinitionValue();
            assertEquals(asccp.getDefinition(), ASCCPDefinition);
            BCCPObject bccp = asccpBCCPMap.get(asccp);
            BCCObject bcc = bccpBCCMap.get(bccp);
            WebElement node = editBIEPage.getNodeByPath(
                    "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
            assertTrue(node.isDisplayed());
            EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
            int originalCardinalityMin = Integer.valueOf(getText(bbiePanel.getCardinalityMinField()));
            int originalCardinalityMax = Integer.valueOf(getText(bbiePanel.getCardinalityMaxField()));
            assertEquals(bcc.getCardinalityMin(), originalCardinalityMin);
            assertEquals(bcc.getCardinalityMax(), originalCardinalityMax);

            ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.
                    extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
            SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.
                    appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
            selectCCPropertyPage.selectAssociation(asccpToAppend.getDen());
            waitFor(Duration.ofMillis(2000));
            ACCExtensionViewEditPage.appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
            selectCCPropertyPage.selectAssociation(bccpToAppend.getDen());
            bieMenu.openViewEditBIESubMenu();
            viewEditBIEPage.openEditBIEPage(useraBIEWIP);

            node = editBIEPage.getNodeByPath(
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

            viewEditBIEPage.openPage();
        }
    }

    @Test
    @DisplayName("TC_6_2_TA_6_5_1_and_TA_6_2_TA_6_5_3")
    public void test_TA_6_5_1_and_TA_6_5_3() {
        AppUserObject usera;
        List<ASCCPObject> asccpsForTesting = new ArrayList<>();
        List<BCCPObject> bccpsForTesting = new ArrayList<>();
        NamespaceObject namespaceEU;
        Map<ACCObject, BCCPObject> accBCCPMap = new HashMap<>();
        Map<ASCCPObject, ACCObject> ASCCPassociatedACC = new HashMap<>();
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        Map<TopLevelASBIEPObject, ASCCPObject> bieASCCPMap = new HashMap<>();
        Map<ASCCPObject, BCCPObject> asccpBCCPMap = new HashMap<>();
        Map<BCCPObject, BCCObject> bccpBCCMap = new HashMap<>();
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            ReleaseObject releaseOne = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            ReleaseObject releaseTwo = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            DTObject dataTypeReleaseOne = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", releaseOne.getReleaseNumber());
            DTObject dataTypeReleaseTwo = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", releaseTwo.getReleaseNumber());
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            /**
             * Users needed for test script
             */
            AppUserObject endUserForCC = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserForCC);

            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
            thisAccountWillBeDeletedAfterTests(usera);

            /**
             * Release One Core Components and BIEs
             */
            ACCObject accReleaseOne = coreComponentAPI.
                    createRandomACC(endUserForCC, releaseOne, namespace, "Published");
            coreComponentAPI.appendExtension(accReleaseOne, endUserForCC, namespace, "Published");
            accReleaseOne.setDefinition("definition 1");
            coreComponentAPI.updateACC(accReleaseOne);

            BCCPObject bccpReleaseOne = coreComponentAPI.
                    createRandomBCCP(dataTypeReleaseOne, endUserForCC, namespace, "Published");
            BCCObject bccReleaseOne = coreComponentAPI.appendBCC(accReleaseOne, bccpReleaseOne, "Published");
            bccReleaseOne.setCardinalityMax(5);
            bccReleaseOne.setCardinalityMin(1);
            coreComponentAPI.updateBCC(bccReleaseOne);
            bccpBCCMap.put(bccpReleaseOne, bccReleaseOne);

            ASCCPObject asccpReleaseOne = coreComponentAPI.
                    createRandomASCCP(accReleaseOne, endUserForCC, namespace, "Published");
            asccpReleaseOne.setDefinition(accReleaseOne.getDefinition());
            asccpBCCPMap.put(asccpReleaseOne, bccpReleaseOne);
            coreComponentAPI.updateASCCP(asccpReleaseOne);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            TopLevelASBIEPObject useraBIEWIPReleaseOne = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseOne, usera, "WIP");
            biesForTesting.add(useraBIEWIPReleaseOne);
            bieASCCPMap.put(useraBIEWIPReleaseOne, asccpReleaseOne);

            /**
             * Core Components to append
             */
            ACCObject accToAppend1ReleaseOne = coreComponentAPI.
                    createRandomACC(endUserForCC, releaseOne, namespace, "Published");

            BCCPObject bccp1ReleaseOne = coreComponentAPI.
                    createRandomBCCP(dataTypeReleaseOne, endUserForCC, namespace, "Published");
            coreComponentAPI.appendBCC(accToAppend1ReleaseOne, bccp1ReleaseOne, "Published");
            accBCCPMap.put(accToAppend1ReleaseOne, bccp1ReleaseOne);
            ASCCPObject asccpToAppendWIPReleaseOne = coreComponentAPI.
                    createRandomASCCP(accToAppend1ReleaseOne, endUserForCC, namespace, "WIP");
            ASCCPassociatedACC.put(asccpToAppendWIPReleaseOne, accToAppend1ReleaseOne);

            ACCObject accToAppend2ReleaseOne = coreComponentAPI.
                    createRandomACC(endUserForCC, releaseOne, namespace, "Published");

            BCCPObject bccp2ReleaseOne = coreComponentAPI.
                    createRandomBCCP(dataTypeReleaseOne, endUserForCC, namespace, "Published");
            coreComponentAPI.appendBCC(accToAppend2ReleaseOne, bccp2ReleaseOne, "Published");
            accBCCPMap.put(accToAppend2ReleaseOne, bccp2ReleaseOne);
            ASCCPObject asccpToAppendQAReleaseOne = coreComponentAPI.
                    createRandomASCCP(accToAppend2ReleaseOne, endUserForCC, namespace, "QA");
            ASCCPassociatedACC.put(asccpToAppendQAReleaseOne, accToAppend2ReleaseOne);

            asccpsForTesting.add(asccpToAppendWIPReleaseOne);
            asccpsForTesting.add(asccpToAppendQAReleaseOne);

            BCCPObject bccpToAppendWIPReleaseOne = coreComponentAPI.
                    createRandomBCCP(dataTypeReleaseOne, endUserForCC, namespace, "WIP");
            BCCPObject bccpToAppendQAReleaseOne = coreComponentAPI.
                    createRandomBCCP(dataTypeReleaseOne, endUserForCC, namespace, "QA");
            bccpsForTesting.add(bccpToAppendWIPReleaseOne);
            bccpsForTesting.add(bccpToAppendQAReleaseOne);

            /**
             * Release Two Core Components and BIEs
             */
            ACCObject accReleaseTwo = coreComponentAPI.
                    createRevisedACC(accReleaseOne, endUserForCC, releaseTwo, "Published");
            coreComponentAPI.appendExtension(accReleaseTwo, endUserForCC, namespace, "Published");
            accReleaseTwo.setDefinition("definition 2");
            coreComponentAPI.updateACC(accReleaseTwo);
            BCCPObject bccpReleaseTwo = coreComponentAPI.
                    createRandomBCCP(dataTypeReleaseTwo, endUserForCC, namespace, "Published");
            BCCObject bccReleaseTwo = coreComponentAPI.appendBCC(accReleaseTwo, bccpReleaseTwo, "Published");
            bccReleaseTwo.setDefinitionSource("bcc definition source");
            bccReleaseTwo.setCardinalityMax(3);
            bccReleaseTwo.setCardinalityMin(3);
            coreComponentAPI.updateBCC(bccReleaseTwo);
            bccpBCCMap.put(bccpReleaseTwo, bccReleaseTwo);

            ASCCPObject asccpReleaseTwo = coreComponentAPI.
                    createRandomASCCP(accReleaseTwo, endUserForCC, namespace, "Published");
            asccpReleaseTwo.setDefinition(accReleaseTwo.getDefinition());
            asccpBCCPMap.put(asccpReleaseTwo, bccpReleaseTwo);
            coreComponentAPI.updateASCCP(asccpReleaseTwo);

            TopLevelASBIEPObject useraBIEWIPReleaseTwo = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseTwo, usera, "WIP");
            biesForTesting.add(useraBIEWIPReleaseTwo);
            bieASCCPMap.put(useraBIEWIPReleaseTwo, asccpReleaseTwo);

            /**
             * Core Components to append
             */
            ACCObject accToAppend1ReleaseTwo = coreComponentAPI.
                    createRevisedACC(accToAppend1ReleaseOne, endUserForCC, releaseTwo, "Published");

            BCCPObject bccp1ReleaseTwo = coreComponentAPI
                    .createRevisedBCCP(bccp1ReleaseOne, dataTypeReleaseTwo, endUserForCC, releaseTwo, "Published");
            coreComponentAPI.appendBCC(accToAppend1ReleaseTwo, bccp1ReleaseTwo, "Published");
            accBCCPMap.put(accToAppend1ReleaseTwo, bccp1ReleaseTwo);
            ASCCPObject asccpToAppendWIPReleaseTwo = coreComponentAPI.
                    createRevisedASCCP(asccpToAppendWIPReleaseOne, accToAppend1ReleaseTwo, endUserForCC, releaseTwo, "WIP");
            ASCCPassociatedACC.put(asccpToAppendWIPReleaseTwo, accToAppend1ReleaseTwo);

            ACCObject accToAppend2ReleaseTwo = coreComponentAPI.
                    createRevisedACC(accToAppend2ReleaseOne, endUserForCC, releaseTwo, "Published");

            BCCPObject bccp2ReleaseTwo = coreComponentAPI.
                    createRevisedBCCP(bccp2ReleaseOne, dataTypeReleaseTwo, endUserForCC, releaseTwo, "Published");
            coreComponentAPI.appendBCC(accToAppend2ReleaseTwo, bccp2ReleaseTwo, "Published");
            accBCCPMap.put(accToAppend2ReleaseTwo, bccp2ReleaseTwo);
            ASCCPObject asccpToAppendQAReleaseTwo = coreComponentAPI.
                    createRevisedASCCP(asccpToAppendQAReleaseOne, accToAppend2ReleaseTwo, endUserForCC, releaseTwo, "QA");
            ASCCPassociatedACC.put(asccpToAppendQAReleaseTwo, accToAppend2ReleaseTwo);

            BCCPObject bccpToAppendWIPReleaseTwo = coreComponentAPI.
                    createRevisedBCCP(bccpToAppendWIPReleaseOne, dataTypeReleaseTwo, endUserForCC, releaseTwo, "WIP");
            BCCPObject bccpToAppendQAReleaseTwo = coreComponentAPI.
                    createRevisedBCCP(bccpToAppendQAReleaseOne, dataTypeReleaseTwo, endUserForCC, releaseTwo, "QA");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        for (TopLevelASBIEPObject useraBIEWIP : biesForTesting) {
            BIEMenu bieMenu = homePage.getBIEMenu();
            ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
            EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
            getDriver().manage().window().maximize();
            assertEquals("WIP", useraBIEWIP.getState());
            ASCCPObject asccp = bieASCCPMap.get(useraBIEWIP);
            /**
             * Assert that Type Definition field in BIE has the same value as ASCCP's definition on which it is based.
             * Note that there are two ASCCPs in two releases having different definitions
             */
            String ASCCPDefinition = editBIEPage.getTypeDefinitionValue();
            assertEquals(asccp.getDefinition(), ASCCPDefinition);
            BCCPObject bccp = asccpBCCPMap.get(asccp);
            BCCObject bcc = bccpBCCMap.get(bccp);
            WebElement node = editBIEPage.getNodeByPath(
                    "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
            assertTrue(node.isDisplayed());
            EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
            int originalCardinalityMin = Integer.valueOf(getText(bbiePanel.getCardinalityMinField()));
            int originalCardinalityMax = Integer.valueOf(getText(bbiePanel.getCardinalityMaxField()));
            assertEquals(bcc.getCardinalityMin(), originalCardinalityMin);
            assertEquals(bcc.getCardinalityMax(), originalCardinalityMax);
            // TODO:
            // Can't open the context menu in a small size of the screen.
            ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.
                    extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
            for (ASCCPObject asccpToAppend : asccpsForTesting) {
                /**
                 * It has child association to an end user ASCCP which is not in Production state
                 */
                assertNotEquals("Production", asccpToAppend.getState());
                SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.
                        appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
                selectCCPropertyPage.selectAssociation(asccpToAppend.getDen());
            }
            for (BCCPObject bccpToAppend : bccpsForTesting) {
                /**
                 * It has child association to an end user BCCP which is not in Production state
                 */
                assertNotEquals("Production", bccpToAppend.getState());
                SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.
                        appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
                selectCCPropertyPage.selectAssociation(bccpToAppend.getDen());
            }
            ACCExtensionViewEditPage.setNamespace(namespaceEU);
            ACCExtensionViewEditPage.setDefinition(ASCCPDefinition);
            ACCExtensionViewEditPage.hitUpdateButton();
            ACCExtensionViewEditPage.moveToQA();
            /**
             *  there is a corresponding UEGACC in Production state
             */
            ACCExtensionViewEditPage.moveToProduction();
            bieMenu.openViewEditBIESubMenu();
            viewEditBIEPage.openEditBIEPage(useraBIEWIP);

            for (BCCPObject bccpToAppend : bccpsForTesting) {
                node = editBIEPage.getNodeByPath(
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
                // TODO
                // Check if Business Term functionality is enabled. Currently, it is disabled.
                if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
                    // TODO:
                    // Check business term abilities are disabled
                } else {
                    assertDisabled(BBIEPPanel.getBusinessTermField());
                }
            }
            for (ASCCPObject asccpToAppend : asccpsForTesting) {
                ACCObject associatedACC = ASCCPassociatedACC.get(asccpToAppend);
                node = editBIEPage.getNodeByPath(
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
                // TODO
                // Check if Business Term functionality is enabled. Currently, it is disabled.
                if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
                    // TODO:
                    // Check business term abilities are disabled
                } else {
                    assertDisabled(ASBIEPanel.getBusinessTermField());
                }

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
                // TODO
                // Check if Business Term functionality is enabled. Currently, it is disabled.
                if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
                    // TODO:
                    // Check business term abilities are disabled
                } else {
                    assertDisabled(BBIEPPanel.getBusinessTermField());
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_2_TA_6_5_2")
    public void test_TA_6_5_2() {
        AppUserObject usera;
        List<ASCCPObject> asccpsForTesting = new ArrayList<>();
        NamespaceObject namespaceEU;
        Map<ASCCPObject, ACCObject> asccpACCMap = new HashMap<>();
        Map<ACCObject, BCCPObject> accBCCPMap = new HashMap<>();
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        Map<TopLevelASBIEPObject, ASCCPObject> bieASCCPMap = new HashMap<>();
        Map<ASCCPObject, BCCPObject> asccpBCCPMap = new HashMap<>();
        Map<BCCPObject, BCCObject> bccpBCCMap = new HashMap<>();
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            ReleaseObject releaseOne = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            ReleaseObject releaseTwo = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            DTObject dataTypeReleaseOne = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", releaseOne.getReleaseNumber());
            DTObject dataTypeReleaseTwo = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", releaseTwo.getReleaseNumber());
            AppUserObject endUserForCC = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserForCC);
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
            thisAccountWillBeDeletedAfterTests(usera);

            /**
             * Core Components and BIEs for the Release One
             */
            ACCObject accReleaseOne = coreComponentAPI.createRandomACC(endUserForCC, releaseOne, namespace, "Published");
            coreComponentAPI.appendExtension(accReleaseOne, endUserForCC, namespace, "Published");
            accReleaseOne.setDefinition("definition 1");
            coreComponentAPI.updateACC(accReleaseOne);

            BCCPObject bccpReleaseOne = coreComponentAPI.createRandomBCCP(dataTypeReleaseOne, endUserForCC, namespace, "Published");
            BCCObject bccReleaseOne = coreComponentAPI.appendBCC(accReleaseOne, bccpReleaseOne, "Published");
            bccReleaseOne.setCardinalityMax(5);
            bccReleaseOne.setCardinalityMin(1);
            coreComponentAPI.updateBCC(bccReleaseOne);
            bccpBCCMap.put(bccpReleaseOne, bccReleaseOne);

            ASCCPObject asccpReleaseOne = coreComponentAPI.createRandomASCCP(accReleaseOne, endUserForCC, namespace, "Published");
            asccpReleaseOne.setDefinition(accReleaseOne.getDefinition());
            asccpBCCPMap.put(asccpReleaseOne, bccpReleaseOne);
            coreComponentAPI.updateASCCP(asccpReleaseOne);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            TopLevelASBIEPObject useraBIEWIPReleaseOne = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseOne, usera, "WIP");
            biesForTesting.add(useraBIEWIPReleaseOne);
            bieASCCPMap.put(useraBIEWIPReleaseOne, asccpReleaseOne);

            /**
             * Core Components to append
             */
            ACCObject accToAppendReleaseOne = coreComponentAPI.createRandomACC(endUserForCC, releaseOne, namespace, "QA");
            BCCPObject bccpToAppendReleaseOne = coreComponentAPI.createRandomBCCP(dataTypeReleaseOne, endUserForCC, namespace, "QA");
            coreComponentAPI.appendBCC(accToAppendReleaseOne, bccpToAppendReleaseOne, "QA");
            accBCCPMap.put(accToAppendReleaseOne, bccpToAppendReleaseOne);
            ASCCPObject asccpToAppendProductionReleaseOne = coreComponentAPI.
                    createRandomASCCP(accToAppendReleaseOne, endUserForCC, namespace, "Production");
            asccpsForTesting.add(asccpToAppendProductionReleaseOne);
            asccpACCMap.put(asccpToAppendProductionReleaseOne, accToAppendReleaseOne);

            /**
             * Core Components and BIEs for the Release Two
             */
            ACCObject accReleaseTwo = coreComponentAPI.createRevisedACC(accReleaseOne, endUserForCC, releaseTwo, "Published");
            coreComponentAPI.appendExtension(accReleaseTwo, endUserForCC, namespace, "Published");
            accReleaseTwo.setDefinition("definition 2");
            coreComponentAPI.updateACC(accReleaseTwo);

            BCCPObject bccpReleaseTwo = coreComponentAPI.
                    createRevisedBCCP(bccpReleaseOne, dataTypeReleaseTwo, endUserForCC, releaseTwo, "Published");
            BCCObject bccReleaseTwo = coreComponentAPI.appendBCC(accReleaseTwo, bccpReleaseTwo, "Published");
            bccReleaseTwo.setCardinalityMax(3);
            bccReleaseTwo.setCardinalityMin(3);
            coreComponentAPI.updateBCC(bccReleaseTwo);
            bccpBCCMap.put(bccpReleaseTwo, bccReleaseTwo);

            ASCCPObject asccpReleaseTwo = coreComponentAPI.
                    createRevisedASCCP(asccpReleaseOne, accReleaseTwo, endUserForCC, releaseTwo, "Published");
            asccpReleaseTwo.setDefinition(accReleaseTwo.getDefinition());
            asccpBCCPMap.put(asccpReleaseTwo, bccpReleaseTwo);
            coreComponentAPI.updateASCCP(asccpReleaseTwo);

            TopLevelASBIEPObject useraBIEWIPReleaseTwo = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseTwo, usera, "WIP");
            biesForTesting.add(useraBIEWIPReleaseTwo);
            bieASCCPMap.put(useraBIEWIPReleaseTwo, asccpReleaseTwo);

            /**
             * Core Components to append
             */
            ACCObject accToAppendReleaseTwo = coreComponentAPI.
                    createRevisedACC(accToAppendReleaseOne, endUserForCC, releaseTwo, "QA");
            BCCPObject bccpToAppendReleaseTwo = coreComponentAPI.
                    createRevisedBCCP(bccpToAppendReleaseOne, dataTypeReleaseTwo, endUserForCC, releaseTwo, "QA");
            coreComponentAPI.appendBCC(accToAppendReleaseTwo, bccpToAppendReleaseTwo, "QA");
            accBCCPMap.put(accToAppendReleaseTwo, bccpToAppendReleaseTwo);
            ASCCPObject asccpToAppendProductionReleaseTwo = coreComponentAPI.
                    createRevisedASCCP(asccpToAppendProductionReleaseOne, accToAppendReleaseTwo, endUserForCC, releaseTwo, "Production");
            asccpACCMap.put(asccpToAppendProductionReleaseTwo, accToAppendReleaseTwo);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ;
        for (TopLevelASBIEPObject useraBIEWIP : biesForTesting) {
            ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
            EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
            assertEquals("WIP", useraBIEWIP.getState());
            ASCCPObject asccp = bieASCCPMap.get(useraBIEWIP);
            /**
             * Assert that Type Definition field in BIE has the same value as ASCCP's definition on which it is based.
             * Note that there are two ASCCPs in two releases having different definitions
             */
            String ASCCPDefinition = editBIEPage.getTypeDefinitionValue();
            assertEquals(asccp.getDefinition(), ASCCPDefinition);
            BCCPObject bccp = asccpBCCPMap.get(asccp);
            BCCObject bcc = bccpBCCMap.get(bccp);
            WebElement node = editBIEPage.getNodeByPath(
                    "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
            assertTrue(node.isDisplayed());
            EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
            int originalCardinalityMin = Integer.valueOf(getText(bbiePanel.getCardinalityMinField()));
            int originalCardinalityMax = Integer.valueOf(getText(bbiePanel.getCardinalityMaxField()));
            assertEquals(bcc.getCardinalityMin(), originalCardinalityMin);
            assertEquals(bcc.getCardinalityMax(), originalCardinalityMax);
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
                node = editBIEPage.getNodeByPath(
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

                /**
                 * Assert that all options for descendant nodes are also disabled
                 */
                ACCObject ACCAssociation = asccpACCMap.get(asccpToAppend);
                bccp = accBCCPMap.get(ACCAssociation);
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
                if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
                    // TODO:
                    // Check business term abilities are disabled
                } else {
                    assertDisabled(ASBIEPanel.getBusinessTermField());
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_2_TA_6_5_4")
    public void test_TA_6_5_4() {
        AppUserObject usera;
        List<ASCCPObject> asccpsForTesting = new ArrayList<>();
        NamespaceObject namespaceEU;
        Map<ASCCPObject, ACCObject> asccpACCMap = new HashMap<>();
        Map<ACCObject, BCCPObject> accBCCPMap = new HashMap<>();
        Map<ACCObject, ASCCPObject> accASCCPPMap = new HashMap<>();
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        Map<TopLevelASBIEPObject, ASCCPObject> bieASCCPMap = new HashMap<>();
        Map<ASCCPObject, BCCPObject> asccpBCCPMap = new HashMap<>();
        Map<BCCPObject, BCCObject> bccpBCCMap = new HashMap<>();
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            ReleaseObject releaseOne = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            ReleaseObject releaseTwo = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            DTObject dataTypeReleaseOne = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", releaseOne.getReleaseNumber());
            DTObject dataTypeReleaseTwo = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", releaseTwo.getReleaseNumber());
            AppUserObject endUserForCCFirst = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            AppUserObject endUserForCCSecond = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserForCCFirst);
            thisAccountWillBeDeletedAfterTests(endUserForCCSecond);
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
            thisAccountWillBeDeletedAfterTests(usera);
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            /**
             * Core Components and BIEs for Release One
             */
            ACCObject accReleaseOne = coreComponentAPI.createRandomACC(endUserForCCFirst, releaseOne, namespace, "Published");
            coreComponentAPI.appendExtension(accReleaseOne, endUserForCCFirst, namespace, "Published");
            accReleaseOne.setDefinition("definition 1");
            coreComponentAPI.updateACC(accReleaseOne);

            BCCPObject bccpReleaseOne = coreComponentAPI.createRandomBCCP(dataTypeReleaseOne, endUserForCCFirst, namespace, "Published");
            BCCObject bccReleaseOne = coreComponentAPI.appendBCC(accReleaseOne, bccpReleaseOne, "Published");
            bccReleaseOne.setCardinalityMax(5);
            bccReleaseOne.setCardinalityMin(1);
            coreComponentAPI.updateBCC(bccReleaseOne);
            bccpBCCMap.put(bccpReleaseOne, bccReleaseOne);

            ASCCPObject asccpReleaseOne = coreComponentAPI.createRandomASCCP(accReleaseOne, endUserForCCFirst, namespace, "Published");
            asccpReleaseOne.setDefinition(accReleaseOne.getDefinition());
            asccpBCCPMap.put(asccpReleaseOne, bccpReleaseOne);
            coreComponentAPI.updateASCCP(asccpReleaseOne);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            TopLevelASBIEPObject useraBIEWIPReleaseOne = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseOne, usera, "WIP");
            biesForTesting.add(useraBIEWIPReleaseOne);
            bieASCCPMap.put(useraBIEWIPReleaseOne, asccpReleaseOne);

            /**
             * Core Components to append
             */

            /**
             * The end user ACC of the ASCCP is also in the Production state and was amended.
             */
            ACCObject accToAppendReleaseOne = coreComponentAPI.createRandomACC(endUserForCCFirst, releaseOne, namespace, "Production");

            BCCPObject bccpToAppendReleaseOne = coreComponentAPI.createRandomBCCP(dataTypeReleaseOne, endUserForCCFirst, namespace, "QA");
            coreComponentAPI.appendBCC(accToAppendReleaseOne, bccpToAppendReleaseOne, "QA");
            accBCCPMap.put(accToAppendReleaseOne, bccpToAppendReleaseOne);
            /**
             * The ACC has a child ASCC that points another end user ASCCP2 that is not in the Production state.
             */
            ACCObject accQAReleaseOne = coreComponentAPI.createRandomACC(endUserForCCSecond, releaseOne, namespace, "QA");
            ASCCPObject asccp2ReleaseOne = coreComponentAPI.createRandomASCCP(accQAReleaseOne, endUserForCCSecond, namespace, "QA");
            coreComponentAPI.appendASCC(accToAppendReleaseOne, asccp2ReleaseOne, "QA");
            accASCCPPMap.put(accToAppendReleaseOne, asccp2ReleaseOne);
            /**
             * Amend the ACC that should be amended.
             */
            coreComponentAPI.createRevisedACC(accToAppendReleaseOne, endUserForCCSecond, releaseOne, "WIP");
            /**
             * There is a corresponding UEGACC that has a child association to an end user ASCCP that is in the Production state
             */
            ASCCPObject asccpToAppendProductionReleaseOne = coreComponentAPI.
                    createRandomASCCP(accToAppendReleaseOne, endUserForCCSecond, namespace, "Production");
            asccpsForTesting.add(asccpToAppendProductionReleaseOne);
            asccpACCMap.put(asccpToAppendProductionReleaseOne, accToAppendReleaseOne);

            /**
             * Core Components and BIEs for Release Two
             */
            ACCObject accReleaseTwo = coreComponentAPI.createRevisedACC(accReleaseOne, endUserForCCFirst, releaseTwo, "Published");
            coreComponentAPI.appendExtension(accReleaseTwo, endUserForCCFirst, namespace, "Published");
            accReleaseOne.setDefinition("definition 2");
            coreComponentAPI.updateACC(accReleaseTwo);

            BCCPObject bccpReleaseTwo = coreComponentAPI.
                    createRevisedBCCP(bccpReleaseOne, dataTypeReleaseTwo, endUserForCCFirst, releaseTwo, "Published");
            BCCObject bccReleaseTwo = coreComponentAPI.appendBCC(accReleaseTwo, bccpReleaseTwo, "Published");
            bccReleaseTwo.setCardinalityMax(3);
            bccReleaseTwo.setCardinalityMin(3);
            coreComponentAPI.updateBCC(bccReleaseTwo);
            bccpBCCMap.put(bccpReleaseTwo, bccReleaseTwo);

            ASCCPObject asccpReleaseTwo = coreComponentAPI.
                    createRevisedASCCP(asccpReleaseOne, accReleaseTwo, endUserForCCFirst, releaseTwo, "Published");
            asccpReleaseTwo.setDefinition(accReleaseTwo.getDefinition());
            asccpBCCPMap.put(asccpReleaseTwo, bccpReleaseTwo);
            coreComponentAPI.updateASCCP(asccpReleaseTwo);

            TopLevelASBIEPObject useraBIEWIPReleaseTwo = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseTwo, usera, "WIP");
            biesForTesting.add(useraBIEWIPReleaseTwo);
            bieASCCPMap.put(useraBIEWIPReleaseTwo, asccpReleaseTwo);

            /**
             * Core Components to append
             */

            /**
             * The end user ACC of the ASCCP is also in the Production state and was amended.
             */
            ACCObject accToAppendReleaseTwo = coreComponentAPI.createRevisedACC(accToAppendReleaseOne, endUserForCCFirst, releaseTwo, "Production");

            BCCPObject bccpToAppendReleaseTwo = coreComponentAPI.
                    createRevisedBCCP(bccpToAppendReleaseOne, dataTypeReleaseTwo, endUserForCCFirst, releaseTwo, "QA");
            coreComponentAPI.appendBCC(accToAppendReleaseTwo, bccpToAppendReleaseTwo, "QA");
            accBCCPMap.put(accToAppendReleaseTwo, bccpToAppendReleaseTwo);
            /**
             * The ACC has a child ASCC that points another end user ASCCP2 that is not in the Production state.
             */
            ACCObject accQAReleaseTwo = coreComponentAPI.createRevisedACC(accQAReleaseOne, endUserForCCSecond, releaseTwo, "QA");
            ASCCPObject asccp2ReleaseTwo = coreComponentAPI.
                    createRevisedASCCP(asccp2ReleaseOne, accQAReleaseTwo, endUserForCCSecond, releaseTwo, "QA");
            coreComponentAPI.appendASCC(accToAppendReleaseTwo, asccp2ReleaseTwo, "QA");
            accASCCPPMap.put(accToAppendReleaseTwo, asccp2ReleaseTwo);
            /**
             * Amend the ACC that should be amended.
             */
            coreComponentAPI.createRevisedACC(accToAppendReleaseTwo, endUserForCCSecond, releaseTwo, "WIP");
            /**
             * There is a corresponding UEGACC that has a child association to an end user ASCCP that is in the Production state
             */
            ASCCPObject asccpToAppendProductionReleaseTwo = coreComponentAPI.
                    createRevisedASCCP(asccpToAppendProductionReleaseOne, accToAppendReleaseTwo, endUserForCCSecond, releaseTwo, "Production");
            asccpACCMap.put(asccpToAppendProductionReleaseTwo, accToAppendReleaseTwo);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        for (TopLevelASBIEPObject useraBIEWIP : biesForTesting) {
            ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
            EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
            assertEquals("WIP", useraBIEWIP.getState());
            ASCCPObject asccp = bieASCCPMap.get(useraBIEWIP);
            /**
             * Assert that Type Definition field in BIE has the same value as ASCCP's definition on which it is based.
             * Note that there are two ASCCPs in two releases having different definitions
             */
            String ASCCPDefinition = editBIEPage.getTypeDefinitionValue();
            assertEquals(asccp.getDefinition(), ASCCPDefinition);
            BCCPObject bccp = asccpBCCPMap.get(asccp);
            BCCObject bcc = bccpBCCMap.get(bccp);
            WebElement node = editBIEPage.getNodeByPath(
                    "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
            assertTrue(node.isDisplayed());
            EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
            int originalCardinalityMin = Integer.valueOf(getText(bbiePanel.getCardinalityMinField()));
            int originalCardinalityMax = Integer.valueOf(getText(bbiePanel.getCardinalityMaxField()));
            assertEquals(bcc.getCardinalityMin(), originalCardinalityMin);
            assertEquals(bcc.getCardinalityMax(), originalCardinalityMax);
            ACCExtensionViewEditPage ACCExtensionViewEditPage = editBIEPage.
                    extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
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
                SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.
                        appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
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
                node = editBIEPage.getNodeByPath(
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
                if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
                    // TODO:
                    // Check business term abilities are disabled
                } else {
                    assertDisabled(ASBIEPanel.getBusinessTermField());
                }

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
                if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
                    // TODO:
                    // Check business term abilities are disabled
                } else {
                    assertDisabled(ASBIEPanel.getBusinessTermField());
                }

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
                if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
                    // TODO:
                    // Check business term abilities are disabled
                } else {
                    assertDisabled(ASBIEPanel.getBusinessTermField());
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_2_TA_6_5_5")
    public void test_TA_6_5_5() {
        AppUserObject usera;
        List<ASCCPObject> asccpsForTesting = new ArrayList<>();
        NamespaceObject namespaceEU;
        Map<ASCCPObject, ACCObject> asccpACCMap = new HashMap<>();
        Map<ACCObject, BCCPObject> accBCCPMap = new HashMap<>();
        Map<ACCObject, ASCCPObject> accASCCPPMap = new HashMap<>();
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        Map<TopLevelASBIEPObject, ASCCPObject> bieASCCPMap = new HashMap<>();
        Map<ASCCPObject, BCCPObject> asccpBCCPMap = new HashMap<>();
        Map<BCCPObject, BCCObject> bccpBCCMap = new HashMap<>();
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            ReleaseObject releaseOne = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(this.release);
            ReleaseObject releaseTwo = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            DTObject dataTypeReleaseOne = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", releaseOne.getReleaseNumber());
            DTObject dataTypeReleaseTwo = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", releaseTwo.getReleaseNumber());
            AppUserObject endUserForCCFirst = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserForCCFirst);
            AppUserObject endUserForCCSecond = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserForCCSecond);
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            usera = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(usera);
            thisAccountWillBeDeletedAfterTests(usera);

            /**
             * Core Components and BIEs for Release One
             */

            ACCObject accReleaseOne = coreComponentAPI.createRandomACC(endUserForCCFirst, releaseOne, namespace, "Published");
            coreComponentAPI.appendExtension(accReleaseOne, endUserForCCFirst, namespace, "Published");
            accReleaseOne.setDefinition("definition 1");
            coreComponentAPI.updateACC(accReleaseOne);

            BCCPObject bccpReleaseOne = coreComponentAPI.createRandomBCCP(dataTypeReleaseOne, endUserForCCFirst, namespace, "Published");
            BCCObject bccReleaseOne = coreComponentAPI.appendBCC(accReleaseOne, bccpReleaseOne, "Published");
            bccReleaseOne.setCardinalityMax(5);
            bccReleaseOne.setCardinalityMin(1);
            coreComponentAPI.updateBCC(bccReleaseOne);
            bccpBCCMap.put(bccpReleaseOne, bccReleaseOne);

            ASCCPObject asccpReleaseOne = coreComponentAPI.createRandomASCCP(accReleaseOne, endUserForCCFirst, namespace, "Published");
            asccpReleaseOne.setDefinition(accReleaseOne.getDefinition());
            asccpBCCPMap.put(asccpReleaseOne, bccpReleaseOne);
            coreComponentAPI.updateASCCP(asccpReleaseOne);

            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(usera);
            TopLevelASBIEPObject useraBIEWIPReleaseOne = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseOne, usera, "WIP");
            biesForTesting.add(useraBIEWIPReleaseOne);
            bieASCCPMap.put(useraBIEWIPReleaseOne, asccpReleaseOne);

            /**
             * Core Components to append
             */

            /**
             * The end user ACC of the ASCCP has a Group component type and is NOT in the Production state.
             */
            ACCObject accToAppendReleaseOne = coreComponentAPI.createRandomACCSemanticGroupType(endUserForCCFirst, releaseOne, namespace, "QA");
            BCCPObject bccpToAppendReleaseOne = coreComponentAPI.createRandomBCCP(dataTypeReleaseOne, endUserForCCFirst, namespace, "QA");
            coreComponentAPI.appendBCC(accToAppendReleaseOne, bccpToAppendReleaseOne, "QA");
            accBCCPMap.put(accToAppendReleaseOne, bccpToAppendReleaseOne);

            ACCObject accQAReleaseOne = coreComponentAPI.createRandomACC(endUserForCCSecond, releaseOne, namespace, "QA");
            ASCCPObject asccp2ReleaseOne = coreComponentAPI.createRandomASCCP(accQAReleaseOne, endUserForCCSecond, namespace, "QA");
            coreComponentAPI.appendASCC(accToAppendReleaseOne, asccp2ReleaseOne, "QA");
            accASCCPPMap.put(accToAppendReleaseOne, asccp2ReleaseOne);

            /**
             * There is a corresponding UEGACC that has a child association to an end user ASCCP that is in the Production state
             */
            ASCCPObject asccpToAppendProductionReleaseOne = coreComponentAPI.
                    createRandomASCCP(accToAppendReleaseOne, endUserForCCSecond, namespace, "Production");
            asccpsForTesting.add(asccpToAppendProductionReleaseOne);
            asccpACCMap.put(asccpToAppendProductionReleaseOne, accToAppendReleaseOne);

            /**
             * Core Components and BIEs for Release Two
             */

            ACCObject accReleaseTwo = coreComponentAPI.createRevisedACC(accReleaseOne, endUserForCCFirst, releaseTwo, "Published");
            coreComponentAPI.appendExtension(accReleaseTwo, endUserForCCFirst, namespace, "Published");
            accReleaseTwo.setDefinition("definition 2");
            coreComponentAPI.updateACC(accReleaseTwo);

            BCCPObject bccpReleaseTwo = coreComponentAPI.createRevisedBCCP(bccpReleaseOne, dataTypeReleaseTwo, endUserForCCFirst, releaseTwo, "Published");
            BCCObject bccReleaseTwo = coreComponentAPI.appendBCC(accReleaseTwo, bccpReleaseTwo, "Published");
            bccReleaseTwo.setCardinalityMax(3);
            bccReleaseTwo.setCardinalityMin(3);
            coreComponentAPI.updateBCC(bccReleaseTwo);
            bccpBCCMap.put(bccpReleaseTwo, bccReleaseTwo);

            ASCCPObject asccpReleaseTwo = coreComponentAPI.createRevisedASCCP(asccpReleaseOne, accReleaseTwo, endUserForCCFirst, releaseTwo, "Published");
            asccpReleaseTwo.setDefinition(accReleaseTwo.getDefinition());
            asccpBCCPMap.put(asccpReleaseTwo, bccpReleaseTwo);
            coreComponentAPI.updateASCCP(asccpReleaseTwo);

            TopLevelASBIEPObject useraBIEWIPReleaseTwo = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccpReleaseTwo, usera, "WIP");
            biesForTesting.add(useraBIEWIPReleaseTwo);
            bieASCCPMap.put(useraBIEWIPReleaseTwo, asccpReleaseTwo);

            /**
             * Core Components to append
             */

            /**
             * The end user ACC of the ASCCP has a Group component type and is NOT in the Production state.
             */
            ACCObject accToAppendReleaseTwo = coreComponentAPI.createRevisedACC(accToAppendReleaseOne, endUserForCCFirst, releaseTwo, "QA");
            BCCPObject bccpToAppendReleaseTwo = coreComponentAPI.createRevisedBCCP(bccpToAppendReleaseOne, dataTypeReleaseTwo, endUserForCCFirst, releaseTwo, "QA");
            coreComponentAPI.appendBCC(accToAppendReleaseTwo, bccpToAppendReleaseTwo, "QA");
            accBCCPMap.put(accToAppendReleaseTwo, bccpToAppendReleaseTwo);

            ACCObject accQAReleaseTwo = coreComponentAPI.createRevisedACC(accQAReleaseOne, endUserForCCSecond, releaseTwo, "QA");
            ASCCPObject asccp2ReleaseTwo = coreComponentAPI.createRevisedASCCP(asccp2ReleaseOne, accQAReleaseTwo, endUserForCCSecond, releaseTwo, "QA");
            coreComponentAPI.appendASCC(accToAppendReleaseTwo, asccp2ReleaseTwo, "QA");
            accASCCPPMap.put(accToAppendReleaseTwo, asccp2ReleaseTwo);

            /**
             * There is a corresponding UEGACC that has a child association to an end user ASCCP that is in the Production state
             */
            ASCCPObject asccpToAppendProductionReleaseTwo = coreComponentAPI.createRevisedASCCP(asccpToAppendProductionReleaseOne, accToAppendReleaseTwo, endUserForCCSecond, releaseTwo, "Production");
            asccpACCMap.put(asccpToAppendProductionReleaseTwo, accToAppendReleaseTwo);
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        for (TopLevelASBIEPObject useraBIEWIP : biesForTesting) {
            ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
            EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
            assertEquals("WIP", useraBIEWIP.getState());
            ASCCPObject asccp = bieASCCPMap.get(useraBIEWIP);
            /**
             * Assert that Type Definition field in BIE has the same value as ASCCP's definition on which it is based.
             * Note that there are two ASCCPs in two releases having different definitions
             */
            String ASCCPDefinition = editBIEPage.getTypeDefinitionValue();
            assertEquals(asccp.getDefinition(), ASCCPDefinition);
            BCCPObject bccp = asccpBCCPMap.get(asccp);
            BCCObject bcc = bccpBCCMap.get(bccp);
            WebElement node = editBIEPage.getNodeByPath(
                    "/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
            assertTrue(node.isDisplayed());
            EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
            int originalCardinalityMin = Integer.valueOf(getText(bbiePanel.getCardinalityMinField()));
            int originalCardinalityMax = Integer.valueOf(getText(bbiePanel.getCardinalityMaxField()));
            assertEquals(bcc.getCardinalityMin(), originalCardinalityMin);
            assertEquals(bcc.getCardinalityMax(), originalCardinalityMax);
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
                SelectAssociationDialog selectCCPropertyPage = ACCExtensionViewEditPage.
                        appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
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
                node = editBIEPage.getNodeByPath(
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
                if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
                    // TODO:
                    // Check business term abilities are disabled
                } else {
                    assertDisabled(BBIEPPanel.getBusinessTermField());
                }

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
                if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
                    // TODO:
                    // Check business term abilities are disabled
                } else {
                    assertDisabled(ASBIEPanel.getBusinessTermField());
                }
            }
        }
    }

    @Test
    @DisplayName("TC_6_2_TA_8_1_and_TA_8_3")
    public void test_TA_8_1_and_TA_8_3() {
        List<CodeListObject> codeListsForTesting = new ArrayList<>();
        Map<CodeListObject, ReleaseObject> codeListReleaseMap = new HashMap<>();
        ASCCPObject asccp;
        AppUserObject usera;
        TopLevelASBIEPObject useraBIE;
        Map<BCCPObject, DTObject> bccpDTMap = new HashMap<>();
        List<BCCPObject> bccpForTesting = new ArrayList<>();
        {
            /**
             * Production developer Code List for the latest and older release
             */
            ReleaseObject latestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");
            ReleaseObject olderRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
            AppUserObject developerUserForCodeList = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerUserForCodeList);
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developerUserForCodeList);
            CodeListObject baseCodeList = getAPIFactory().getCodeListAPI().
                    getCodeListByCodeListNameAndReleaseNum("oacl_ResponseCode", latestRelease.getReleaseNumber());
            CodeListObject developerCodeListLatestRelease = getAPIFactory().getCodeListAPI().
                    createDerivedCodeList(baseCodeList, developerUserForCodeList, namespace, latestRelease, "Production");
            getAPIFactory().getCodeListAPI().addCodeListToAnotherRelease(developerCodeListLatestRelease, olderRelease, developerUserForCodeList);
            codeListsForTesting.add(developerCodeListLatestRelease);
            codeListReleaseMap.put(developerCodeListLatestRelease, latestRelease);

            baseCodeList = getAPIFactory().getCodeListAPI().getCodeListByCodeListNameAndReleaseNum("oacl_StateCode", olderRelease.getReleaseNumber());
            CodeListObject developerCodeListOlderRelease = getAPIFactory().getCodeListAPI().
                    createDerivedCodeList(baseCodeList, developerUserForCodeList, namespace, olderRelease, "Production");
            codeListsForTesting.add(developerCodeListOlderRelease);
            codeListReleaseMap.put(developerCodeListOlderRelease, olderRelease);

            /**
             * Production end-user Code List for the latest and older release
             */
            AppUserObject endUserForCodeList = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserForCodeList);
            namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserForCodeList);
            baseCodeList = getAPIFactory().getCodeListAPI().
                    getCodeListByCodeListNameAndReleaseNum("oacl_ReasonCode", latestRelease.getReleaseNumber());
            CodeListObject endUserCodeListLatestRelease = getAPIFactory().getCodeListAPI().
                    createDerivedCodeList(baseCodeList, endUserForCodeList, namespace, latestRelease, "Production");
            codeListsForTesting.add(endUserCodeListLatestRelease);
            codeListReleaseMap.put(endUserCodeListLatestRelease, latestRelease);

            baseCodeList = getAPIFactory().getCodeListAPI().
                    getCodeListByCodeListNameAndReleaseNum("oacl_RiskCode", olderRelease.getReleaseNumber());
            CodeListObject endUserCodeListOlderRelease = getAPIFactory().getCodeListAPI().
                    createDerivedCodeList(baseCodeList, endUserForCodeList, namespace, olderRelease, "Production");
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
            useraBIE = getAPIFactory().getBusinessInformationEntityAPI().
                    generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, usera, "WIP");
        }

        HomePage homePage = loginPage().signIn(usera.getLoginId(), usera.getPassword());
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIE);
        for (BCCPObject bccp : bccpForTesting) {
            WebElement node = editBIEPage.getNodeByPath("/" + bccp.getPropertyTerm());
            assertTrue(node.isDisplayed());
            EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
            bbiePanel.toggleUsed();
            bbiePanel.setValueDomainRestriction("Code");
            String BIEreleaseNumber = useraBIE.getReleaseNumber();
            DTObject dataType = bccpDTMap.get(bccp);
            List<CodeListObject> defaultCodeLists = getAPIFactory().getCodeListAPI().
                    getDefaultCodeListsForDT(dataType.getGuid(), dataType.getReleaseId());
            if (!defaultCodeLists.isEmpty()) {
                for (CodeListObject cl : defaultCodeLists) {
                    bbiePanel.setValueDomain(cl.getName());
                }
            } else {
                for (CodeListObject codeList : codeListsForTesting) {
                    /**
                     * Only production, compatible code lists in the same release as the BIE shall be included,
                     * i.e., a code list exists only in a newer release shall not be included.
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
        List<CodeListObject> codeListsForTesting = new ArrayList<>();
        Map<CodeListObject, ReleaseObject> codeListReleaseMap = new HashMap<>();
        ASCCPObject asccp;
        AppUserObject usera;
        TopLevelASBIEPObject useraBIE;
        Map<BCCPObject, DTObject> bccpDTMap = new HashMap<>();
        List<BCCPObject> bccpForTesting = new ArrayList<>();
        {
            /**
             * Developer Code List for the latest and older release in WIP state
             */
            ReleaseObject latestRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.4");
            ReleaseObject olderRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.3");
            AppUserObject developerUserForCodeList = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerUserForCodeList);
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developerUserForCodeList);

            CodeListObject baseCodeList = getAPIFactory().getCodeListAPI().
                    getCodeListByCodeListNameAndReleaseNum("oacl_CategoryCode", latestRelease.getReleaseNumber());
            CodeListObject developerWIPCodeListLatestRelease = getAPIFactory().getCodeListAPI().
                    createDerivedCodeList(baseCodeList, developerUserForCodeList, namespace, latestRelease, "WIP");
            codeListsForTesting.add(developerWIPCodeListLatestRelease);
            codeListReleaseMap.put(developerWIPCodeListLatestRelease, latestRelease);

            baseCodeList = getAPIFactory().getCodeListAPI().
                    getCodeListByCodeListNameAndReleaseNum("oacl_ChargeCode", latestRelease.getReleaseNumber());
            CodeListObject developerWIPCodeListOlderRelease = getAPIFactory().getCodeListAPI().
                    createDerivedCodeList(baseCodeList, developerUserForCodeList, namespace, olderRelease, "WIP");
            codeListsForTesting.add(developerWIPCodeListOlderRelease);
            codeListReleaseMap.put(developerWIPCodeListOlderRelease, olderRelease);

            /**
             * End-user Code List for the latest and older release in QA state
             */
            AppUserObject endUserForCodeList = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserForCodeList);
            namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserForCodeList);

            baseCodeList = getAPIFactory().getCodeListAPI().
                    getCodeListByCodeListNameAndReleaseNum("oacl_ConfirmationCode", latestRelease.getReleaseNumber());
            CodeListObject endUserWIPCodeListLatestRelease = getAPIFactory().getCodeListAPI().
                    createDerivedCodeList(baseCodeList, endUserForCodeList, namespace, latestRelease, "QA");
            codeListsForTesting.add(endUserWIPCodeListLatestRelease);
            codeListReleaseMap.put(endUserWIPCodeListLatestRelease, latestRelease);
            getAPIFactory().getCodeListAPI().addCodeListToAnotherRelease(endUserWIPCodeListLatestRelease, olderRelease, endUserForCodeList);

            baseCodeList = getAPIFactory().getCodeListAPI().
                    getCodeListByCodeListNameAndReleaseNum("oacl_ClassificationCode", olderRelease.getReleaseNumber());
            CodeListObject endUserWIPCodeListOlderRelease = getAPIFactory().getCodeListAPI().
                    createDerivedCodeList(baseCodeList, endUserForCodeList, namespace, olderRelease, "QA");
            codeListsForTesting.add(endUserWIPCodeListOlderRelease);
            codeListReleaseMap.put(endUserWIPCodeListOlderRelease, olderRelease);

            /**
             * Deleted end-user Code List for the latest and older release
             */
            baseCodeList = getAPIFactory().getCodeListAPI().
                    getCodeListByCodeListNameAndReleaseNum("oacl_CountryCode", olderRelease.getReleaseNumber());
            CodeListObject endUserDeletedCodeListOlderRelease = getAPIFactory().getCodeListAPI().
                    createDerivedCodeList(baseCodeList, endUserForCodeList, namespace, olderRelease, "Deleted");
            codeListsForTesting.add(endUserDeletedCodeListOlderRelease);
            codeListReleaseMap.put(endUserDeletedCodeListOlderRelease, olderRelease);

            baseCodeList = getAPIFactory().getCodeListAPI().
                    getCodeListByCodeListNameAndReleaseNum("oacl_ControlCode", olderRelease.getReleaseNumber());
            CodeListObject endUserDeletedCodeListLatestRelease = getAPIFactory().getCodeListAPI().
                    createDerivedCodeList(baseCodeList, endUserForCodeList, namespace, latestRelease, "Deleted");
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

        for (BCCPObject bccp : bccpForTesting) {
            WebElement node = editBIEPage.getNodeByPath("/" + bccp.getPropertyTerm());
            assertTrue(node.isDisplayed());
            EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
            bbiePanel.toggleUsed();
            bbiePanel.setValueDomainRestriction("Code");
            String BIEreleaseNumber = useraBIE.getReleaseNumber();
            DTObject dataType = bccpDTMap.get(bccp);
            ArrayList<CodeListObject> defaultCodeLists = getAPIFactory().getCodeListAPI().
                    getDefaultCodeListsForDT(dataType.getGuid(), dataType.getReleaseId());
            if (!defaultCodeLists.isEmpty()) {
                for (CodeListObject cl : defaultCodeLists) {
                    bbiePanel.setValueDomain(cl.getName());
                }
            } else {
                /**
                 * If there is no default code list, all developer code lists in the published state in the same release
                 * and end user code lists in the same release shall be included.
                 * End user code lists shall be displayed in the same way as described in 8.2.
                 */
                for (CodeListObject codeList : codeListsForTesting) {
                    Boolean exists = getAPIFactory().getCodeListAPI().doesCodeListExistInTheRelease(codeList, BIEreleaseNumber);
                    if (exists) {
                        if (codeList.getState().equals("QA") || codeList.getState().equals("WIP")) {
                            /**
                             * if it is in the WIP or QA state, flag that the code list is being changed
                             * (maybe use dark yellow and italicized font – yellow like a warning light),
                             * the meaning is the code list is usable but unstable.
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
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // Ignore
        } else {
            bbiePanel.setBusinessTerm("test business term");
        }
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
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // Ignore
        } else {
            assertEquals("test business term", getText(bbiePanel.getBusinessTermField()));
        }
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
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // Ignore
        } else {
            assertTrue(StringUtils.isEmpty(getText(bbiePanel.getBusinessTermField())));
        }
        assertTrue(StringUtils.isEmpty(getText(bbiePanel.getRemarkField())));
        assertTrue(StringUtils.isEmpty(getText(bbiePanel.getExampleField())));
        assertTrue(StringUtils.isEmpty(getText(bbiePanel.getContextDefinitionField())));
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
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage =
                viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE(asccpTopLevel.getDen(), this.release);
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                getTopLevelASBIEPByDENAndReleaseNum(asccpTopLevel.getDen(), this.release);
        /**
         * The end user ASCCP is in Production State
         */
        assertEquals("Production", asccp.getState());
        /**
         * Assert descendent nodes are editable
         */
        WebElement node = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        EditBIEPage.ASBIEPanel ASBIEPanel = editBIEPage.getASBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        ASBIEPanel.toggleUsed();
        assertEnabled(ASBIEPanel.getUsedCheckbox());
        assertEnabled(ASBIEPanel.getCardinalityMinField());
        assertEnabled(ASBIEPanel.getCardinalityMaxField());
        assertEnabled(ASBIEPanel.getRemarkField());
        assertEnabled(ASBIEPanel.getContextDefinitionField());
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check if Business Term functionality is enabled. Currently, it is disabled.
        } else {
            assertEnabled(ASBIEPanel.getBusinessTermField());
        }

        node = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
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
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check if Business Term functionality is enabled. Currently, it is disabled.
        } else {
            assertEnabled(bbiePanel.getBusinessTermField());
        }
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
         * If the end user ASCCP is amended (i.e., moved to WIP state), the BIE cannot be edited.
         * The fields of the BIE nodes are disabled including the “Used” checkbox.
         */
        node = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        ASBIEPanel = editBIEPage.getASBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        assertDisabled(ASBIEPanel.getUsedCheckbox());
        assertDisabled(ASBIEPanel.getCardinalityMinField());
        assertDisabled(ASBIEPanel.getCardinalityMaxField());
        assertDisabled(ASBIEPanel.getRemarkField());
        assertDisabled(ASBIEPanel.getContextDefinitionField());
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check if Business Term functionality is enabled. Currently, it is disabled.
        } else {
            assertDisabled(ASBIEPanel.getBusinessTermField());
        }

        node = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
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
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check if Business Term functionality is enabled. Currently, it is disabled.
        } else {
            assertDisabled(bbiePanel.getBusinessTermField());
        }
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
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage =
                viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE(asccpTopLevel.getDen(), this.release);
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                getTopLevelASBIEPByDENAndReleaseNum(asccpTopLevel.getDen(), this.release);
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
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check if Business Term functionality is enabled. Currently, it is disabled.
        } else {
            assertEnabled(ASBIEPanel.getBusinessTermField());
        }

        node = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
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
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check if Business Term functionality is enabled. Currently, it is disabled.
        } else {
            assertEnabled(bbiePanel.getBusinessTermField());
        }
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
         * If the end user ASCCP is amended (i.e., moved to WIP state), the BIE cannot be edited.
         * The fields of the BIE nodes are disabled including the “Used” checkbox.
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
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check if Business Term functionality is enabled. Currently, it is disabled.
        } else {
            assertDisabled(ASBIEPanel.getBusinessTermField());
        }

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
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check if Business Term functionality is enabled. Currently, it is disabled.
        } else {
            assertDisabled(bbiePanel.getBusinessTermField());
        }
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
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage =
                viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE(asccpTopLevel.getDen(), this.release);
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                getTopLevelASBIEPByDENAndReleaseNum(asccpTopLevel.getDen(), this.release);
        /**
         * The end user ASCCP is in Production State
         */
        assertEquals("Production", asccp.getState());
        /**
         * Assert descendent nodes are editable
         */
        WebElement node = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        EditBIEPage.ASBIEPanel ASBIEPanel = editBIEPage.getASBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        ASBIEPanel.toggleUsed();
        assertEnabled(ASBIEPanel.getUsedCheckbox());
        assertEnabled(ASBIEPanel.getCardinalityMinField());
        assertEnabled(ASBIEPanel.getCardinalityMaxField());
        assertEnabled(ASBIEPanel.getRemarkField());
        assertEnabled(ASBIEPanel.getContextDefinitionField());
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check if Business Term functionality is enabled. Currently, it is disabled.
        } else {
            assertEnabled(ASBIEPanel.getBusinessTermField());
        }

        node = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
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
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check if Business Term functionality is enabled. Currently, it is disabled.
        } else {
            assertEnabled(bbiePanel.getBusinessTermField());
        }
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
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage =
                viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE(asccpTopLevel.getDen(), this.release);
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                getTopLevelASBIEPByDENAndReleaseNum(asccpTopLevel.getDen(), this.release);
        /**
         * The end user ACC is in Production State
         */
        assertEquals("Production", acc.getState());
        /**
         * Assert descendent nodes are editable
         */
        WebElement node = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm());
        assertTrue(node.isDisplayed());
        EditBIEPage.ASBIEPanel ASBIEPanel = editBIEPage.getASBIEPanel(node);
        waitFor(Duration.ofMillis(2000));
        ASBIEPanel.toggleUsed();
        assertEnabled(ASBIEPanel.getUsedCheckbox());
        assertEnabled(ASBIEPanel.getCardinalityMinField());
        assertEnabled(ASBIEPanel.getCardinalityMaxField());
        assertEnabled(ASBIEPanel.getRemarkField());
        assertEnabled(ASBIEPanel.getContextDefinitionField());
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check if Business Term functionality is enabled. Currently, it is disabled.
        } else {
            assertEnabled(ASBIEPanel.getBusinessTermField());
        }

        node = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
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
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check if Business Term functionality is enabled. Currently, it is disabled.
        } else {
            assertEnabled(bbiePanel.getBusinessTermField());
        }
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
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check if Business Term functionality is enabled. Currently, it is disabled.
        } else {
            assertDisabled(ASBIEPanel.getBusinessTermField());
        }

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
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check if Business Term functionality is enabled. Currently, it is disabled.
        } else {
            assertDisabled(bbiePanel.getBusinessTermField());
        }
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
        node = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
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
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check if Business Term functionality is enabled. Currently, it is disabled.
        } else {
            assertDisabled(bbiePanel.getBusinessTermField());
        }

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
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check if Business Term functionality is enabled. Currently, it is disabled.
        } else {
            assertDisabled(bbiePanel.getBusinessTermField());
        }
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
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check if Business Term functionality is enabled. Currently, it is disabled.
        } else {
            assertDisabled(bbiePanel.getBusinessTermField());
        }
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
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check if Business Term functionality is enabled. Currently, it is disabled.
        } else {
            assertDisabled(bbiePanel.getBusinessTermField());
        }
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
        CreateBIEForSelectTopLevelConceptPage createBIEForSelectTopLevelConceptPage =
                viewEditBIEPage.openCreateBIEPage().next(Arrays.asList(context));
        EditBIEPage editBIEPage = createBIEForSelectTopLevelConceptPage.createBIE(asccpTopLevel.getDen(), this.release);
        TopLevelASBIEPObject topLevelASBIEP = getAPIFactory().getBusinessInformationEntityAPI().
                getTopLevelASBIEPByDENAndReleaseNum(asccpTopLevel.getDen(), this.release);
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
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check if Business Term functionality is enabled. Currently, it is disabled.
        } else {
            assertDisabled(bbiePanel.getBusinessTermField());
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
