package org.oagi.score.e2e.TS_10_WorkingBranchCoreComponentManagementBehaviorsForDeveloper.asccp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofSeconds;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.AssertionHelper.assertEnabled;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_10_12_EditingBrandNewDeveloperASCCP extends BaseTest {
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
    public void test_TA_10_12_1_a() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create(acc.getDen());
        String url = getDriver().getCurrentUrl();
        BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        assertTrue(asccpPanel.getPropertyTermField().isEnabled());
        assertEnabled(asccpPanel.getReusableCheckbox());
        assertEnabled(asccpPanel.getNillableCheckbox());
        assertTrue(asccpPanel.getNamespaceSelectField().isEnabled());
        assertTrue(asccpPanel.getDefinitionField().isEnabled());
        assertTrue(asccpPanel.getDefinitionSourceField().isEnabled());
    }

    @Test
    public void test_TA_10_12_1_b() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create(acc.getDen());
        String url = getDriver().getCurrentUrl();
        BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        assertDisabled(asccpPanel.getDeprecatedCheckbox());

    }

    @Test
    public void test_TA_10_12_1_c() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create(acc.getDen());
        String url = getDriver().getCurrentUrl();
        BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();

        String randomPropertyTerm = randomAlphabetic(5, 10).replaceAll(" ", "");
        randomPropertyTerm = Character.toUpperCase(randomPropertyTerm.charAt(0)) + randomPropertyTerm.substring(1).toLowerCase();
        asccpPanel.setPropertyTerm("Test Object " + randomPropertyTerm);

        String denText = getText(asccpPanel.getDENField());
        assertTrue(denText.startsWith("Test Object " + randomPropertyTerm));
    }

    @Test
    public void test_TA_10_12_1_d() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create(acc.getDen());
        String url = getDriver().getCurrentUrl();
        BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();

        String randomPropertyTerm = randomAlphabetic(5, 10).replaceAll(" ", "");
        randomPropertyTerm = Character.toUpperCase(randomPropertyTerm.charAt(0)) + randomPropertyTerm.substring(1).toLowerCase();
        asccpPanel.setPropertyTerm("Test Object " + randomPropertyTerm);
        asccpPanel.toggleNillable();
        asccpPanel.setNamespace(namespace.getUri());

        assertThrows(TimeoutException.class, () -> asccpViewEditPage.hitUpdateButton());
        assertEquals("Update without definitions.", getText(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//div[contains(@class, \"header\")]"))));

    }

    @Test
    public void test_TA_10_12_1_e() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create(acc.getDen());
        String url = getDriver().getCurrentUrl();
        BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        assertDisabled(asccpPanel.getGUIDField());
        assertDisabled(asccpPanel.getDENField());
    }

    @Test
    public void test_TA_10_12_1_f() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        BCCPObject bccp;
        ACCObject acc;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            ACCObject acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "WIP");
        }

        ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create(acc.getDen());
        String url = getDriver().getCurrentUrl();
        BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        WebElement accNode = asccpViewEditPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/" + acc.getDen());
        ASCCPViewEditPage.ACCPanel accPanel = asccpViewEditPage.getACCPanel(accNode);
        assertFalse(accPanel.getCoreComponentField().isEnabled());
        assertEquals("ACC", getText(accPanel.getCoreComponentField()));
        assertFalse(accPanel.getReleaseField().isEnabled());
        assertFalse(accPanel.getRevisionField().isEnabled());
        assertFalse(accPanel.getStateField().isEnabled());
        assertEquals("WIP", getText(accPanel.getStateField()));
        assertFalse(accPanel.getOwnerField().isEnabled());
        assertFalse(accPanel.getGUIDField().isEnabled());
        assertFalse(accPanel.getDENField().isEnabled());
        assertFalse(accPanel.getObjectClassTermField().isEnabled());
        assertFalse(accPanel.getComponentTypeSelectField().isEnabled());
        assertFalse(accPanel.getNamespaceSelectField().isEnabled());
        assertFalse(accPanel.getDefinitionSourceField().isEnabled());
        assertFalse(accPanel.getDefinitionField().isEnabled());

        //BCCP node cannot be changed
        WebElement bccNode = asccpViewEditPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/" + acc.getDen() + "/" + bccp.getPropertyTerm());
        ASCCPViewEditPage.BCCPPanel bccpPanel = asccpViewEditPage.getBCCPanelContainer(bccNode).getBCCPPanel();
        assertFalse(bccpPanel.getCoreComponentField().isEnabled());
        assertEquals("BCCP", getText(bccpPanel.getCoreComponentField()));
        assertFalse(bccpPanel.getReleaseField().isEnabled());
        assertFalse(bccpPanel.getRevisionField().isEnabled());
        assertFalse(bccpPanel.getStateField().isEnabled());
        assertEquals("WIP", getText(bccpPanel.getStateField()));
        assertFalse(bccpPanel.getOwnerField().isEnabled());
        assertFalse(bccpPanel.getGUIDField().isEnabled());
        assertFalse(bccpPanel.getDENField().isEnabled());
        assertFalse(bccpPanel.getPropertyTermField().isEnabled());
        assertFalse(bccpPanel.getNillableCheckbox().isEnabled());
        assertFalse(bccpPanel.getDeprecatedCheckbox().isEnabled());
        assertFalse(bccpPanel.getValueConstraintSelectField().isEnabled());
        assertFalse(bccpPanel.getNamespaceSelectField().isEnabled());
        assertFalse(bccpPanel.getDefinitionSourceField().isEnabled());
        assertFalse(bccpPanel.getDefinitionField().isEnabled());
    }

    @Test
    public void test_TA_10_12_1_g() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create(acc.getDen());
        String url = getDriver().getCurrentUrl();
        BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();

        String randomPropertyTerm = randomAlphabetic(5, 10).replaceAll(" ", "");
        randomPropertyTerm = Character.toUpperCase(randomPropertyTerm.charAt(0)) + randomPropertyTerm.substring(1).toLowerCase();
        asccpPanel.setPropertyTerm("Test Object " + randomPropertyTerm);
        asccpPanel.toggleNillable();
        assertTrue(asccpPanel.getReusableCheckbox().isEnabled());
        asccpPanel.setNamespace(namespace.getUri());
        String definition = randomPrint(50, 100).trim();
        asccpPanel.setDefinition(definition);
        assertTrue(asccpViewEditPage.getUpdateButton(true).isEnabled());
    }

    @Test
    public void test_TA_10_12_1_h() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create(acc.getDen());
        String url = getDriver().getCurrentUrl();
        BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();

        //change ACC
        ACCObject anotherACC = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        SelectAssociationDialog selectAssociationDialog = asccpViewEditPage.changeACC("/" + asccp.getPropertyTerm());
        selectAssociationDialog.selectAssociation(anotherACC.getDen());
        selectAssociationDialog.hitUpdateButton();

        asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        String asccpDEN = getText(asccpPanel.getDENField());
        assertTrue(asccpDEN.endsWith(anotherACC.getDen()));
    }

    @Test
    public void test_TA_10_12_1_i() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        NamespaceObject endUserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create(acc.getDen());
        String url = getDriver().getCurrentUrl();
        BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();

        assertThrows(TimeoutException.class, () -> asccpPanel.setNamespace(endUserNamespace.getUri()));
    }

    @Test
    public void test_TA_10_12_2() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create(acc.getDen());
        String url = getDriver().getCurrentUrl();
        BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);

        viewEditCoreComponentPage.openPage();
        waitFor(ofSeconds(1L));

        String den = asccp.getDen();
        {
            viewEditCoreComponentPage.setDEN(den);
            viewEditCoreComponentPage.hitSearchButton();

            WebElement tr = viewEditCoreComponentPage.getTableRecordByValue(den);
            WebElement td = viewEditCoreComponentPage.getColumnByName(tr, "transferOwnership");
            assertTrue(td.findElement(By.tagName("button")).isEnabled());

            TransferCCOwnershipDialog transferCCOwnershipDialog =
                    viewEditCoreComponentPage.openTransferCCOwnershipDialog(tr);
            transferCCOwnershipDialog.transfer(anotherDeveloper.getLoginId());

            viewEditCoreComponentPage.setDEN(den);
            viewEditCoreComponentPage.hitSearchButton();

            tr = viewEditCoreComponentPage.getTableRecordByValue(den);
            td = viewEditCoreComponentPage.getColumnByName(tr, "owner");
            assertEquals(anotherDeveloper.getLoginId(), getText(td));
        }

        homePage.logout();
        homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());
        viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        {
            viewEditCoreComponentPage.setDEN(den);
            viewEditCoreComponentPage.hitSearchButton();

            WebElement tr = viewEditCoreComponentPage.getTableRecordByValue(den);
            WebElement td = viewEditCoreComponentPage.getColumnByName(tr, "transferOwnership");
            assertTrue(td.findElement(By.tagName("button")).isEnabled());

            TransferCCOwnershipDialog transferCCOwnershipDialog =
                    viewEditCoreComponentPage.openTransferCCOwnershipDialog(tr);
            assertThrows(NoSuchElementException.class, () -> transferCCOwnershipDialog.transfer(endUser.getLoginId()));
        }

    }

    @Test
    public void test_TA_10_12_3() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create(acc.getDen());
        String url = getDriver().getCurrentUrl();
        BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();

        String randomPropertyTerm = randomAlphabetic(5, 10).replaceAll(" ", "");
        randomPropertyTerm = Character.toUpperCase(randomPropertyTerm.charAt(0)) + randomPropertyTerm.substring(1).toLowerCase();
        randomPropertyTerm = "Test Object " + randomPropertyTerm;

        acc.setObjectClassTerm(randomPropertyTerm);
        getAPIFactory().getCoreComponentAPI().updateACC(acc);
        WebElement accNode = asccpViewEditPage.getNodeByPath("/" +asccp.getPropertyTerm() + "/" +  acc.getDen());
        ASCCPViewEditPage.ACCPanel accPanel = asccpViewEditPage.getACCPanel(accNode);
        assertEquals("1", getText(accPanel.getRevisionField()));

        viewEditCoreComponentPage.openPage();
        waitFor(ofSeconds(1L));
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch(asccp.getDen(), branch);
        asccpPanel = asccpViewEditPage.getASCCPPanel();
        String asccpDEN = getText(asccpPanel.getDENField());
        assertTrue(asccpDEN.endsWith(randomPropertyTerm));
        assertEquals("1", getText(asccpPanel.getRevisionField()));
    }

    @Test
    public void test_TA_10_12_4() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ASCCPObject asccp;
        BCCPObject bccp;
        ACCObject acc, randomACC1, randomACC2;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            ACCObject acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "WIP");

            randomACC1 = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            randomACC2 = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "WIP");
            ASCCObject randomASCC1 = coreComponentAPI.appendASCC(randomACC1, asccp, "WIP");
            randomASCC1.setCardinalityMax(1);
            coreComponentAPI.updateASCC(randomASCC1);
            ASCCObject randomASCC2 = coreComponentAPI.appendASCC(randomACC2, asccp, "WIP");
            randomASCC2.setCardinalityMax(1);
            coreComponentAPI.updateASCC(randomASCC2);
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPPanel();

        String randomPropertyTerm = randomAlphabetic(5, 10).replaceAll(" ", "");
        randomPropertyTerm = Character.toUpperCase(randomPropertyTerm.charAt(0)) + randomPropertyTerm.substring(1).toLowerCase();
        randomPropertyTerm = "Test Object " + randomPropertyTerm;
        asccpPanel.setPropertyTerm(randomPropertyTerm);
        asccpViewEditPage.hitUpdateButton();

        {
            viewEditCoreComponentPage.openPage();
            waitFor(ofSeconds(1L));
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(randomACC1.getAccManifestId());
            WebElement asccNode = accViewEditPage.getNodeByPath("/"  + randomACC1.getDen() + "/" + randomPropertyTerm);
            ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
            assertTrue(getText(asccPanel.getDENField()).contains(randomPropertyTerm));
        }

        {
            viewEditCoreComponentPage.openPage();
            waitFor(ofSeconds(1L));
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(randomACC2.getAccManifestId());
            WebElement asccNode = accViewEditPage.getNodeByPath("/"  + randomACC2.getDen() + "/" + randomPropertyTerm);
            ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
            assertTrue(getText(asccPanel.getDENField()).contains(randomPropertyTerm));
        }
    }

    @Test
    public void test_TA_10_12_5() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create(acc.getDen());
        String url = getDriver().getCurrentUrl();
        BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();

        //change ACC
        ACCObject anotherACC = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        viewEditCoreComponentPage.openPage();
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        SelectAssociationDialog selectAssociationDialog = asccpViewEditPage.changeACC("/" + asccp.getPropertyTerm());
        selectAssociationDialog.selectAssociation(anotherACC.getDen());
        selectAssociationDialog.hitUpdateButton();

        asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        String asccpDEN = getText(asccpPanel.getDENField());
        assertTrue(asccpDEN.endsWith(anotherACC.getDen()));

        //only semantics or semantic group ACC can be selected
        selectAssociationDialog = asccpViewEditPage.changeACC("/" + asccp.getPropertyTerm());
        selectAssociationDialog.setDEN("Issued Item Instance Base. Details");
        selectAssociationDialog.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"Issued Item Instance Base. Details\")]")).size());
        selectAssociationDialog = asccpViewEditPage.changeACC("/" + asccp.getPropertyTerm());
        selectAssociationDialog.setDEN("Issued Item Instance Extension. Details");
        selectAssociationDialog.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"Issued Item Instance Base. Details\")]")).size());
        selectAssociationDialog = asccpViewEditPage.changeACC("/" + asccp.getPropertyTerm());
        selectAssociationDialog.setDEN("Any Structured Content. Details");
        selectAssociationDialog.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"Any Structured Content. Details\")]")).size());
        selectAssociationDialog = asccpViewEditPage.changeACC("/" + asccp.getPropertyTerm());
        selectAssociationDialog.setDEN("OAGIS10 Nouns. Details");
        selectAssociationDialog.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"OAGIS10 Nouns. Details\")]")).size());
        selectAssociationDialog = asccpViewEditPage.changeACC("/" + asccp.getPropertyTerm());
        selectAssociationDialog.setDEN("OAGIS10 BODs. Details");
        selectAssociationDialog.hitSearchButton();
        assertEquals(0, getDriver().findElements(By.xpath("//mat-dialog-content//a[contains(text(),\"OAGIS10 BODs. Details\")]")).size());
    }
}
