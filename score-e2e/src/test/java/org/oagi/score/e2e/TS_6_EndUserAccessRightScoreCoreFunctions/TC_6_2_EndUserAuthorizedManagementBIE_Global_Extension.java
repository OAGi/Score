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
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.AssertionHelper.assertEnabled;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.SAME_THREAD)
public class TC_6_2_EndUserAuthorizedManagementBIE_Global_Extension extends BaseTest {

    private List<AppUserObject> randomAccounts = new ArrayList<>();
    private String release = "10.8.4";

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        // Delete random accounts
        this.randomAccounts.forEach(newUser -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId());
        });
    }

    @Test
    @DisplayName("TC_6_2_TA_7_1")
    public void test_TA_7_1() {
        ASCCPObject asccp;
        ACCObject acc;
        AppUserObject usera;
        NamespaceObject useraNamespace;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.8");
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
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        for (TopLevelASBIEPObject topLevelAsbiep : biesForTesting) {
            EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
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
                assertThrows(WebDriverException.class, () -> {
                    finalEditBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
                });
                escape(getDriver());
            }

            viewEditBIEPage.openPage();
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
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.7.0.1");
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

        assertThrows(WebDriverException.class, () -> {
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
        assertThrows(WebDriverException.class, () -> {
            finalEditBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        });
        // TODO: The duration of the Snackbar is less than 3 secs, but WebDriverException occurs after 3 secs.
        // assertEquals("Editing extension already exist.", getSnackBarMessage(getDriver()));
    }

    @Test
    @DisplayName("TC_6_2_TA_7_3")
    public void test_TA_7_3() {
        ASCCPObject asccp;
        AppUserObject usera;
        List<TopLevelASBIEPObject> biesForTesting = new ArrayList<>();
        NamespaceObject useraNamespace;
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.8");
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
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.7.2");
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
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();
        ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
        assertEquals("WIP", useraBIEWIP.getState());

        ACCExtensionViewEditPage accExtensionViewEditPage =
                editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        SelectAssociationDialog selectCCPropertyPage =
                accExtensionViewEditPage.appendPropertyAtLast("/All User Extension Group. Details");
        selectCCPropertyPage.showAdvancedSearchPanel();

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
        assertEnabled(bbiePanel.getNillableCheckbox());
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
        if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
            // TODO:
            // Check if Business Term functionality is enabled. Currently, it is disabled.
        } else {
            assertDisabled(bbiePanel.getBusinessTermField());
        }
    }

    @Test
    @DisplayName("TC_6_2_TA_7_5_1_and_TC_6_2_TA_7_5_3")
    public void test_TA_7_5_1_and_TA_7_5_3() {
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
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8");
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
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
        assertEquals("WIP", useraBIEWIP.getState());
        ACCExtensionViewEditPage accExtensionViewEditPage =
                editBIEPage.extendBIEGloballyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        for (ASCCPObject asccpToAppend : asccpsForTesting) {
            /**
             * It has child association to an end user ASCCP which is not in Production state
             */
            assertNotEquals("Production", asccpToAppend.getState());
            SelectAssociationDialog selectCCPropertyPage =
                    accExtensionViewEditPage.appendPropertyAtLast("/All User Extension Group. Details");
            selectCCPropertyPage.showAdvancedSearchPanel();

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
            assertEnabled(bbiePanel.getNillableCheckbox());
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
            if (getAPIFactory().getApplicationSettingsAPI().isBusinessTermEnabled()) {
                // TODO:
                // Check if Business Term functionality is enabled. Currently, it is disabled.
            } else {
                assertDisabled(bbiePanel.getBusinessTermField());
            }
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
        List<ASCCPObject> asccpsForTesting = new ArrayList<>();
        NamespaceObject namespaceEU;
        Map<ASCCPObject, ACCObject> asccpACCMap = new HashMap<>();
        Map<ACCObject, BCCPObject> accBCCPMap = new HashMap<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.7.3");
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
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
        assertEquals("WIP", useraBIEWIP.getState());
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
            selectCCPropertyPage.showAdvancedSearchPanel();

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
        List<ASCCPObject> asccpsForTesting = new ArrayList<>();
        NamespaceObject namespaceEU;
        Map<ASCCPObject, ACCObject> asccpACCMap = new HashMap<>();
        Map<ACCObject, BCCPObject> accBCCPMap = new HashMap<>();
        Map<ACCObject, ASCCPObject> accASCCPPMap = new HashMap<>();
        {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.7.4");
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
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
        assertEquals("WIP", useraBIEWIP.getState());
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
            selectCCPropertyPage.showAdvancedSearchPanel();

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
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.7.5");
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
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();
        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(useraBIEWIP);
        assertEquals("WIP", useraBIEWIP.getState());
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
            selectCCPropertyPage.showAdvancedSearchPanel();

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

}
