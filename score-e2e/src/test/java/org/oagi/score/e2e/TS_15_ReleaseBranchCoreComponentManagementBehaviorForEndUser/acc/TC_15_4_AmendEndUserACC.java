package org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.acc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.menu.BIEMenu;
import org.oagi.score.e2e.menu.CoreComponentMenu;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.ViewEditBIEPage;
import org.oagi.score.e2e.page.core_component.ACCExtensionViewEditPage;
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.SelectAssociationDialog;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.AssertionHelper.assertNotChecked;
import static org.oagi.score.e2e.impl.PageHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.escape;

@Execution(ExecutionMode.CONCURRENT)
public class TC_15_4_AmendEndUserACC extends BaseTest {
    private List<AppUserObject> randomAccounts = new ArrayList<>();

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
    public void test_TA_15_4_1() {

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.7.1");
        NamespaceObject namespace_endUser = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

        ASCCPObject asccp;
        BCCPObject bccp;
        ACCObject acc;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(anotherUser, release, namespace_endUser, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, anotherUser, namespace_endUser, "Production");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Production");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            ACCObject acc_association = coreComponentAPI.createRandomACC(anotherUser, release, namespace_endUser, "Production");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, anotherUser, namespace_endUser, "Production");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Production");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, anotherUser, namespace_endUser, "Production");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "Production");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), release.getReleaseNumber());

        accViewEditPage.hitAmendButton();

        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("2", getText(accPanel.getRevisionField()));
        assertEquals(acc.getObjectClassTerm(), getText(accPanel.getObjectClassTermField()));
        assertEquals(acc.getDen(), getText(accPanel.getDENField()));
        assertEquals(acc.getDefinition(), getText(accPanel.getDefinitionField()));
    }

    @Test
    public void test_TA_15_4_2() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "Published");

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        assertThrows(TimeoutException.class, () -> accViewEditPage.getAmendButton());
    }

    @Test
    public void test_TA_15_4_3() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ASCCPObject asccp;
        BCCPObject bccpToAppend;
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(anotherUser);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser);
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
            ACCObject acc = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Published");
            coreComponentAPI.appendExtension(acc, anotherUser, namespace, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc, anotherUser, namespace, "Published");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccpToAppend = coreComponentAPI.createRandomBCCP(dataType, anotherUser, namespace, "Published");

        }
        TopLevelASBIEPObject topLevelAsbiep = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(context), asccp, anotherUser, "WIP");

        BIEMenu bieMenu = homePage.getBIEMenu();
        ViewEditBIEPage viewEditBIEPage = bieMenu.openViewEditBIESubMenu();
        EditBIEPage editBIEPage = viewEditBIEPage.openEditBIEPage(topLevelAsbiep);
        ACCExtensionViewEditPage accExtensionViewEditPage =
                editBIEPage.extendBIELocallyOnNode("/" + asccp.getPropertyTerm() + "/Extension");
        getDriver().manage().window().maximize();
        SelectAssociationDialog selectCCPropertyPage = accExtensionViewEditPage.appendPropertyAtLast("/" + asccp.getPropertyTerm() + " User Extension Group. Details");
        selectCCPropertyPage.selectAssociation(bccpToAppend.getDen());

        accExtensionViewEditPage.setNamespace(namespace);
        accExtensionViewEditPage.hitUpdateButton();
        accExtensionViewEditPage.moveToQA();
        topLevelAsbiep.setState("QA");
        getAPIFactory().getBusinessInformationEntityAPI().updateTopLevelASBIEP(topLevelAsbiep);

        accExtensionViewEditPage.moveToProduction();
        topLevelAsbiep.setState("Production");
        getAPIFactory().getBusinessInformationEntityAPI().updateTopLevelASBIEP(topLevelAsbiep);

        accExtensionViewEditPage.openPage();
        click(accExtensionViewEditPage.getAmendButton(true));
        WebElement extensionNode = accExtensionViewEditPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/Extension");
        ACCExtensionViewEditPage.ACCPanel accPanel = accExtensionViewEditPage.getACCPanel(extensionNode);
        assertDisabled(accExtensionViewEditPage.getObjectClassTermField());
        assertDisabled(accExtensionViewEditPage.getDENField());
        assertDisabled(accPanel.getAbstractCheckbox());
        assertDisabled(accPanel.getDeprecatedCheckbox());
    }

    @Test
    public void test_TA_15_4_4_a() {
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(anotherUser, release, namespace, "Production");

        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();

        assertDisabled(accViewEditPage.getCoreComponentTypeField());

    }

    @Test
    public void test_TA_15_4_4_b_Abstract_true_in_original_acc() {
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(anotherUser, release, namespace, "Production");
        acc.setAbstract(true);
        getAPIFactory().getCoreComponentAPI().updateACC(acc);
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();

        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertTrue(getText(accPanel.getComponentTypeSelectField()).contains("Semantics"));
        assertChecked(accPanel.getAbstractCheckbox());
        assertEnabled(accPanel.getAbstractCheckbox());

        click(accPanel.getAbstractCheckbox());
        accViewEditPage.hitUpdateButton();
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        accPanel = accViewEditPage.getACCPanel(accNode);
        assertTrue(getText(accPanel.getComponentTypeSelectField()).contains("Semantics"));
        assertNotChecked(accPanel.getAbstractCheckbox());
        assertEnabled(accPanel.getAbstractCheckbox());

    }

    @Test
    public void test_TA_15_4_4_b_base_component_type_in_original_acc() {
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(anotherUser, release, namespace, "Production");
        acc.setComponentType(ComponentType.valueOf("Base (Abstract)"));
        getAPIFactory().getCoreComponentAPI().updateACC(acc);
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();

        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertTrue(getText(accPanel.getComponentTypeSelectField()).contains("Base (Abstract)"));
        assertChecked(accPanel.getAbstractCheckbox());
        assertDisabled(accPanel.getAbstractCheckbox());
    }

    @Test
    public void test_TA_15_4_4_c() {
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(anotherUser, release, namespace, "Production");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();

        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertDisabled(accPanel.getNamespaceSelectField());

        String randomPropertyTerm = randomAlphabetic(5, 10).replaceAll(" ", "");
        randomPropertyTerm = Character.toUpperCase(randomPropertyTerm.charAt(0)) + randomPropertyTerm.substring(1).toLowerCase();
        randomPropertyTerm = "Test Object " + randomPropertyTerm;

        accPanel.setObjectClassTerm(randomPropertyTerm);
        accViewEditPage.hitUpdateButton();
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals(randomPropertyTerm, getText(accPanel.getObjectClassTermField()));
    }

    @Test
    public void test_TA_15_4_4_d_deprecated_true_in_previous_version() {
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(anotherUser, release, namespace, "Production");
        acc.setDeprecated(true);
        getAPIFactory().getCoreComponentAPI().updateACC(acc);
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();

        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertTrue(getText(accPanel.getComponentTypeSelectField()).contains("Semantics"));
        assertChecked(accPanel.getDeprecatedCheckbox());
        assertDisabled(accPanel.getDeprecatedCheckbox());

    }

    @Test
    public void test_TA_15_4_4_d_deprecated_false_in_previous_version() {
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(anotherUser, release, namespace, "Production");
        acc.setDeprecated(false);
        getAPIFactory().getCoreComponentAPI().updateACC(acc);
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();

        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertTrue(getText(accPanel.getComponentTypeSelectField()).contains("Semantics"));
        assertNotChecked(accPanel.getDeprecatedCheckbox());
        assertEnabled(accPanel.getDeprecatedCheckbox());

    }

    @Test
    public void test_TA_15_4_4_e() {
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(anotherUser, release, namespace, "Published");
        acc.setDefinition(null);
        getAPIFactory().getCoreComponentAPI().updateACC(acc);
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        String randomPropertyTerm = randomAlphabetic(5, 10).replaceAll(" ", "");
        randomPropertyTerm = Character.toUpperCase(randomPropertyTerm.charAt(0)) + randomPropertyTerm.substring(1).toLowerCase();
        accPanel.setObjectClassTerm(randomPropertyTerm);

        assertThrows(TimeoutException.class, () -> accViewEditPage.hitUpdateButton());
        assertEquals("Update without definitions.", getText(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//div[contains(@class, \"header\")]"))));

    }

    @Test
    public void test_TA_15_4_5() {



    }

}
