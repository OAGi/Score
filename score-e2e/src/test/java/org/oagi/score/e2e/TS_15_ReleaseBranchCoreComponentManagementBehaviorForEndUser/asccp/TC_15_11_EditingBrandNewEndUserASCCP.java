package org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.asccp;

import org.apache.commons.lang3.RandomStringUtils;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.AssertionHelper.assertEnabled;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_15_11_EditingBrandNewEndUserASCCP extends BaseTest {
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
    public void test_TA_15_11_1_a() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "WIP");

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
    public void test_TA_15_11_1_b() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "WIP");
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
    public void test_TA_15_11_1_c() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "WIP");

        ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create(acc.getDen());
        String url = getDriver().getCurrentUrl();
        BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();

        String randomPropertyTerm = RandomStringUtils.secure().nextAlphabetic(5, 10).replaceAll(" ", "");
        randomPropertyTerm = Character.toUpperCase(randomPropertyTerm.charAt(0)) + randomPropertyTerm.substring(1).toLowerCase();
        asccpPanel.setPropertyTerm("Test Object " + randomPropertyTerm);

        String denText = getText(asccpPanel.getDENField());
        assertTrue(denText.startsWith("Test Object " + randomPropertyTerm));
    }

    @Test
    public void test_TA_15_11_1_d() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "WIP");

        ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create(acc.getDen());
        String url = getDriver().getCurrentUrl();
        BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();

        String randomPropertyTerm = RandomStringUtils.secure().nextAlphabetic(5, 10).replaceAll(" ", "");
        randomPropertyTerm = Character.toUpperCase(randomPropertyTerm.charAt(0)) + randomPropertyTerm.substring(1).toLowerCase();
        asccpPanel.setPropertyTerm("Test Object " + randomPropertyTerm);
        asccpPanel.toggleNillable();
        asccpPanel.setNamespace(namespace.getUri());

        assertThrows(TimeoutException.class, () -> asccpViewEditPage.hitUpdateButton());
        assertEquals("Update without definitions.", getText(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//div[contains(@class, \"mat-mdc-dialog-title\")]/span"))));
    }


    @Test
    public void test_TA_15_11_1_e() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "WIP");

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
    public void test_TA_15_11_1_f() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

        BCCPObject bccp;
        ACCObject acc;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "WIP");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            ACCObject acc_association = coreComponentAPI.createRandomACC(endUser, release, namespace, "WIP");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "WIP");
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
        assertDisabled(accPanel.getComponentTypeSelectField());
        assertDisabled(accPanel.getNamespaceSelectField());
        assertDisabled(accPanel.getDefinitionSourceField());
        assertEnabled(accPanel.getDefinitionField());

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
        assertDisabled(bccpPanel.getPropertyTermField());
        assertEnabled(bccpPanel.getNillableCheckbox());
        assertDisabled(bccpPanel.getDeprecatedCheckbox());
        assertDisabled(bccpPanel.getValueConstraintSelectField());
        assertDisabled(bccpPanel.getNamespaceSelectField());
        assertDisabled(bccpPanel.getDefinitionSourceField());
        assertEnabled(bccpPanel.getDefinitionField());

    }

    @Test
    public void test_TA_15_11_1_g() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "WIP");
        ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create(acc.getDen());
        String url = getDriver().getCurrentUrl();
        BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);

        //change ACC
        ACCObject anotherACC = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "WIP");
        ASCCPChangeACCDialog asccpChangeACCDialog = asccpViewEditPage.openChangeACCDialog("/" + asccp.getPropertyTerm());
        asccpChangeACCDialog.hitUpdateButton(anotherACC.getDen());

        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPPanel();
        String asccpDEN = getText(asccpPanel.getDENField());
        assertTrue(asccpDEN.endsWith(anotherACC.getObjectClassTerm()));
    }

    @Test
    public void test_TA_15_11_1_h() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "WIP");
        NamespaceObject namespaceForDeveloper = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create(acc.getDen());
        String url = getDriver().getCurrentUrl();
        BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();

        assertThrows(TimeoutException.class, () -> asccpPanel.setNamespace(namespaceForDeveloper.getUri()));
    }

    @Test
    public void test_TA_15_11_1_i() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "WIP");

        ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create(acc.getDen());
        String url = getDriver().getCurrentUrl();
        BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();

        String randomPropertyTerm = RandomStringUtils.secure().nextAlphabetic(5, 10).replaceAll(" ", "");
        randomPropertyTerm = Character.toUpperCase(randomPropertyTerm.charAt(0)) + randomPropertyTerm.substring(1).toLowerCase();
        asccpPanel.setPropertyTerm("Test Object " + randomPropertyTerm);
        asccpPanel.toggleNillable();
        assertTrue(asccpPanel.getReusableCheckbox().isEnabled());
        asccpPanel.setNamespace(namespace.getUri());
        String definition = RandomStringUtils.secure().nextPrint(50, 100).trim();
        asccpPanel.setDefinition(definition);
        assertTrue(asccpViewEditPage.getUpdateButton(true).isEnabled());
    }

    @Test
    public void test_TA_15_11_2() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "WIP");
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
            WebElement td = viewEditCoreComponentPage.getColumnByName(tr, "owner");
            assertTrue(td.findElement(By.className("mat-icon")).isEnabled());

            TransferCCOwnershipDialog transferCCOwnershipDialog =
                    viewEditCoreComponentPage.openTransferCCOwnershipDialog(tr);
            transferCCOwnershipDialog.transfer(anotherUser.getLoginId());

            viewEditCoreComponentPage.setDEN(den);
            viewEditCoreComponentPage.hitSearchButton();

            tr = viewEditCoreComponentPage.getTableRecordByValue(den);
            td = viewEditCoreComponentPage.getColumnByName(tr, "owner");
            assertEquals(anotherUser.getLoginId(), getText(td));
        }

        homePage.logout();
        homePage = loginPage().signIn(anotherUser.getLoginId(), anotherUser.getPassword());
        viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        {
            viewEditCoreComponentPage.setBranch(branch);
            waitFor(Duration.ofMillis(1500));
            viewEditCoreComponentPage.setDEN(den);
            viewEditCoreComponentPage.hitSearchButton();

            WebElement tr = viewEditCoreComponentPage.getTableRecordByValue(den);
            WebElement td = viewEditCoreComponentPage.getColumnByName(tr, "owner");
            assertTrue(td.findElement(By.className("mat-icon")).isEnabled());

            TransferCCOwnershipDialog transferCCOwnershipDialog =
                    viewEditCoreComponentPage.openTransferCCOwnershipDialog(tr);
            assertThrows(NoSuchElementException.class, () -> transferCCOwnershipDialog.transfer(developer.getLoginId()));
        }
    }

    @Test
    public void test_TA_15_11_3() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "WIP");
        ASCCPCreateDialog asccpCreateDialog = viewEditCoreComponentPage.openASCCPCreateDialog(branch);
        ASCCPViewEditPage asccpViewEditPage = asccpCreateDialog.create(acc.getDen());
        String url = getDriver().getCurrentUrl();
        BigInteger asccpManifestId = new BigInteger(url.substring(url.lastIndexOf("/") + 1));
        ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().getASCCPByManifestId(asccpManifestId);
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();

        String randomPropertyTerm = RandomStringUtils.secure().nextAlphabetic(5, 10).replaceAll(" ", "");
        randomPropertyTerm = Character.toUpperCase(randomPropertyTerm.charAt(0)) + randomPropertyTerm.substring(1).toLowerCase();
        randomPropertyTerm = "Test Object " + randomPropertyTerm;

        acc.setObjectClassTerm(randomPropertyTerm);
        getAPIFactory().getCoreComponentAPI().updateACC(acc);
        WebElement accNode = asccpViewEditPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/" + acc.getDen());
        ASCCPViewEditPage.ACCPanel accPanel = asccpViewEditPage.getACCPanel(accNode);
        assertEquals("1", getText(accPanel.getRevisionField()));

        viewEditCoreComponentPage.openPage();
        waitFor(ofSeconds(1L));
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        asccpPanel = asccpViewEditPage.getASCCPPanel();
        String asccpDEN = getText(asccpPanel.getDENField());
        assertTrue(asccpDEN.endsWith(randomPropertyTerm));
        assertEquals("1", getText(asccpPanel.getRevisionField()));
    }

    @Test
    public void test_TA_15_11_4() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        ASCCPObject asccp;
        BCCPObject bccp;
        ACCObject acc, randomACC1, randomACC2;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "WIP");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "WIP");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "WIP");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            ACCObject acc_association = coreComponentAPI.createRandomACC(endUser, release, namespace, "WIP");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "WIP");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "WIP");

            randomACC1 = coreComponentAPI.createRandomACC(endUser, release, namespace, "WIP");
            randomACC2 = coreComponentAPI.createRandomACC(endUser, release, namespace, "WIP");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, endUser, namespace, "WIP");
            ASCCObject randomASCC1 = coreComponentAPI.appendASCC(randomACC1, asccp, "WIP");
            randomASCC1.setCardinalityMax(1);
            coreComponentAPI.updateASCC(randomASCC1);
            ASCCObject randomASCC2 = coreComponentAPI.appendASCC(randomACC2, asccp, "WIP");
            randomASCC2.setCardinalityMax(1);
            coreComponentAPI.updateASCC(randomASCC2);
        }

        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPPanel();

        String randomPropertyTerm = RandomStringUtils.secure().nextAlphabetic(5, 10).replaceAll(" ", "");
        randomPropertyTerm = Character.toUpperCase(randomPropertyTerm.charAt(0)) + randomPropertyTerm.substring(1).toLowerCase();
        randomPropertyTerm = "Test Object " + randomPropertyTerm;
        asccpPanel.setPropertyTerm(randomPropertyTerm);
        asccpViewEditPage.hitUpdateButton();

        {
            viewEditCoreComponentPage.openPage();
            waitFor(ofSeconds(1L));
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(randomACC1.getAccManifestId());
            WebElement asccNode = accViewEditPage.getNodeByPath("/" + randomACC1.getDen() + "/" + randomPropertyTerm);
            ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
            assertTrue(getText(asccPanel.getDENField()).contains(randomPropertyTerm));
        }

        {
            viewEditCoreComponentPage.openPage();
            waitFor(ofSeconds(1L));
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(randomACC2.getAccManifestId());
            WebElement asccNode = accViewEditPage.getNodeByPath("/" + randomACC2.getDen() + "/" + randomPropertyTerm);
            ACCViewEditPage.ASCCPanel asccPanel = accViewEditPage.getASCCPanelContainer(asccNode).getASCCPanel();
            assertTrue(getText(asccPanel.getDENField()).contains(randomPropertyTerm));
        }

    }
}
