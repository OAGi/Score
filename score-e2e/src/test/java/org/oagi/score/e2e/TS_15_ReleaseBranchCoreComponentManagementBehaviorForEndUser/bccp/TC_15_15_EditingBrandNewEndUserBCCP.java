package org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.bccp;

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
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;
import static org.jooq.tools.StringUtils.isEmpty;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;

@Execution(ExecutionMode.CONCURRENT)
public class TC_15_15_EditingBrandNewEndUserBCCP extends BaseTest {
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
    public void test_TA_15_15_1_a() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPCreateDialog bccpCreateDialog = viewEditCoreComponentPage.openBCCPCreateDialog(branch);
        BCCPViewEditPage bccpViewEditPage = bccpCreateDialog.create("System Environment_ Code. Type");
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();

        assertTrue(bccpPanel.getPropertyTermField().isEnabled());
        assertEnabled(bccpPanel.getNillableCheckbox());
        assertTrue(bccpPanel.getValueConstraintSelectField().isEnabled());
        assertTrue(bccpPanel.getNamespaceSelectField().isEnabled());
        assertTrue(bccpPanel.getDefinitionField().isEnabled());
        assertTrue(bccpPanel.getDefinitionSourceField().isEnabled());

        String randomPropertyTerm = randomAlphabetic(5, 10).replaceAll(" ", "");
        randomPropertyTerm = Character.toUpperCase(randomPropertyTerm.charAt(0)) + randomPropertyTerm.substring(1).toLowerCase();
        bccpPanel.setPropertyTerm("Test Object " + randomPropertyTerm);
        bccpPanel.toggleNillable();
        String definition = randomPrint(50, 100).trim();
        bccpPanel.setDefinition(definition);

        assertTrue(bccpViewEditPage.getUpdateButton(true).isEnabled());
    }

    @Test
    public void test_TA_15_15_1_b() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPCreateDialog bccpCreateDialog = viewEditCoreComponentPage.openBCCPCreateDialog(branch);
        BCCPViewEditPage bccpViewEditPage = bccpCreateDialog.create("System Environment_ Code. Type");
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();

        String randomPropertyTerm = randomAlphabetic(5, 10).replaceAll(" ", "");
        randomPropertyTerm = Character.toUpperCase(randomPropertyTerm.charAt(0)) + randomPropertyTerm.substring(1).toLowerCase();
        bccpPanel.setPropertyTerm("Test Object " + randomPropertyTerm);

        String denText = getText(bccpPanel.getDENField());
        assertEquals("Test Object " + randomPropertyTerm + ". System Environment_ Code", denText);
    }

    @Test
    public void test_TA_15_15_1_c() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
        BCCPCreateDialog bccpCreateDialog = viewEditCoreComponentPage.openBCCPCreateDialog(branch);
        BCCPViewEditPage bccpViewEditPage = bccpCreateDialog.create("System Environment_ Code. Type");
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();

        String randomPropertyTerm = randomAlphabetic(5, 10).replaceAll(" ", "");
        randomPropertyTerm = Character.toUpperCase(randomPropertyTerm.charAt(0)) + randomPropertyTerm.substring(1).toLowerCase();
        bccpPanel.setPropertyTerm("Test Object " + randomPropertyTerm);
        bccpPanel.toggleNillable();
        bccpPanel.setNamespace(namespace.getUri());

        assertThrows(TimeoutException.class, () -> bccpViewEditPage.hitUpdateButton());
        assertEquals("Update without definitions.", getText(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//div[contains(@class, \"mat-mdc-dialog-title\")]/span"))));

    }

    @Test
    public void test_TA_15_15_1_d() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPCreateDialog bccpCreateDialog = viewEditCoreComponentPage.openBCCPCreateDialog(branch);
        BCCPViewEditPage bccpViewEditPage = bccpCreateDialog.create("System Environment_ Code. Type");
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();

        assertFalse(bccpPanel.getGUIDField().isEnabled());
        assertFalse(bccpPanel.getDENField().isEnabled());
        assertDisabled(bccpPanel.getDeprecatedCheckbox());
    }

    @Test
    public void test_TA_15_15_1_e() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPCreateDialog bccpCreateDialog = viewEditCoreComponentPage.openBCCPCreateDialog(branch);
        BCCPViewEditPage bccpViewEditPage = bccpCreateDialog.create("System Environment_ Code. Type");
        BCCPViewEditPage.DTPanel dtPanel = bccpViewEditPage.getBCCPPanelContainer().getDTPanel();

        assertFalse(dtPanel.getCoreComponentField().isEnabled());
        assertEquals("DT", getText(dtPanel.getCoreComponentField()));
        assertFalse(dtPanel.getReleaseField().isEnabled());
        assertEquals(getText(bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel().getReleaseField()),
                getText(dtPanel.getReleaseField()));
        assertFalse(dtPanel.getRevisionField().isEnabled());
        assertFalse(dtPanel.getStateField().isEnabled());
        assertEquals("Published", getText(dtPanel.getStateField()));
        assertFalse(dtPanel.getOwnerField().isEnabled());
        assertFalse(dtPanel.getGUIDField().isEnabled());
        assertFalse(dtPanel.getDENField().isEnabled());
        assertEquals("System Environment_ Code. Type", getText(dtPanel.getDENField()));
        assertFalse(dtPanel.getDataTypeTermField().isEnabled());
        assertEquals("Code", getText(dtPanel.getDataTypeTermField()));
        assertFalse(dtPanel.getQualifierField().isEnabled());
        assertEquals("System Environment", getText(dtPanel.getQualifierField()));
        assertDisabled(dtPanel.getDefinitionSourceField());
        assertEnabled(dtPanel.getDefinitionField());
    }

    @Test
    public void test_TA_15_15_1_f() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPCreateDialog bccpCreateDialog = viewEditCoreComponentPage.openBCCPCreateDialog(branch);
        BCCPViewEditPage bccpViewEditPage = bccpCreateDialog.create("System Environment_ Code. Type");
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();

        bccpPanel.setValueConstraint("Fixed Value");
        String fixedValue = randomAlphabetic(5, 10);
        bccpPanel.setFixedValue(fixedValue);

        bccpPanel.setValueConstraint("Default Value");
        String defaultValue = randomAlphabetic(5, 10);
        bccpPanel.setDefaultValue(defaultValue);

        bccpPanel.setValueConstraint("Fixed Value");
        assertTrue(isEmpty(getText(bccpPanel.getFixedValueField())));

        bccpPanel.setValueConstraint("Default Value");
        assertTrue(isEmpty(getText(bccpPanel.getDefaultValueField())));
    }

    @Test
    public void test_TA_15_15_1_g() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPCreateDialog bccpCreateDialog = viewEditCoreComponentPage.openBCCPCreateDialog(branch);
        BCCPViewEditPage bccpViewEditPage = bccpCreateDialog.create("System Environment_ Code. Type");
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();

        bccpPanel.setValueConstraint("Fixed Value");
        String fixedValue = randomAlphabetic(5, 10);
        bccpPanel.setFixedValue(fixedValue);

        bccpPanel.toggleNillable();

        String valueConstraintSelectText = getText(bccpPanel.getValueConstraintSelectField());
        assertEquals("None", valueConstraintSelectText);

        bccpPanel.setValueConstraint("Fixed Value");
        bccpPanel.setFixedValue(fixedValue);
        assertNotChecked(bccpPanel.getNillableCheckbox());

    }

    @Test
    public void test_TA_15_15_1_h() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPCreateDialog bccpCreateDialog = viewEditCoreComponentPage.openBCCPCreateDialog(branch);
        BCCPViewEditPage bccpViewEditPage = bccpCreateDialog.create("System Environment_ Code. Type");
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();

        assertTrue(bccpPanel.getPropertyTermField().isEnabled());
        assertEnabled(bccpPanel.getNillableCheckbox());
        assertTrue(bccpPanel.getValueConstraintSelectField().isEnabled());
        assertTrue(bccpPanel.getNamespaceSelectField().isEnabled());
        assertTrue(bccpPanel.getDefinitionField().isEnabled());
        assertTrue(bccpPanel.getDefinitionSourceField().isEnabled());

        String randomPropertyTerm = randomAlphabetic(5, 10).replaceAll(" ", "");
        randomPropertyTerm = Character.toUpperCase(randomPropertyTerm.charAt(0)) + randomPropertyTerm.substring(1).toLowerCase();
        bccpPanel.setPropertyTerm("Test Object " + randomPropertyTerm);
        bccpPanel.toggleNillable();
        String definition = randomPrint(50, 100).trim();
        bccpPanel.setDefinition(definition);

        assertTrue(bccpViewEditPage.getUpdateButton(true).isEnabled());

        NamespaceObject developerNamespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        assertThrows(TimeoutException.class, () -> bccpPanel.setNamespace(developerNamespace.getUri()));

    }

    @Test
    public void test_TA_15_15_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPCreateDialog bccpCreateDialog = viewEditCoreComponentPage.openBCCPCreateDialog(branch);
        BCCPViewEditPage bccpViewEditPage = bccpCreateDialog.create("System Environment_ Code. Type");
        BCCPChangeBDTDialog bccpChangeBDTDialog = bccpViewEditPage.openChangeBDTDialog();
        String nextBDTDen = "Telephone_ Value. Type";
        bccpChangeBDTDialog.update(nextBDTDen);

        BCCPViewEditPage.DTPanel dtPanel = bccpViewEditPage.getBCCPPanelContainer().getDTPanel();
        assertEquals(nextBDTDen, getText(dtPanel.getDENField()));
        assertEquals("Value", getText(dtPanel.getDataTypeTermField()));
        assertEquals("Telephone", getText(dtPanel.getQualifierField()));
    }

    @Test
    public void test_TA_15_15_3() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        ACCObject randomACC1 = coreComponentAPI.createRandomACC(endUser, release, namespace, "WIP");
        ACCObject randomACC2 = coreComponentAPI.createRandomACC(endUser, release, namespace, "WIP");

        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, endUser, namespace, "WIP");
        coreComponentAPI.appendBCC(randomACC1, randomBCCP, "WIP");
        coreComponentAPI.appendBCC(randomACC2, randomBCCP, "WIP");

        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByDenAndBranch(randomBCCP.getDen(), branch);
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        String randomPropertyTerm = randomAlphabetic(5, 10).replaceAll(" ", "");
        randomPropertyTerm = Character.toUpperCase(randomPropertyTerm.charAt(0)) + randomPropertyTerm.substring(1).toLowerCase();
        randomPropertyTerm = "Test Object " + randomPropertyTerm;
        bccpPanel.setPropertyTerm(randomPropertyTerm);
        bccpViewEditPage.hitUpdateButton();

        {
            viewEditCoreComponentPage.openPage();
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(randomACC1.getAccManifestId());
            WebElement bccNode = accViewEditPage.getNodeByPath("/" + randomACC1.getDen() + "/" + randomPropertyTerm);
            ACCViewEditPage.BCCPanelContainer bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
            assertEquals(randomPropertyTerm, getText(bccPanelContainer.getBCCPPanel().getPropertyTermField()));
        }

        {
            viewEditCoreComponentPage.openPage();
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(randomACC2.getAccManifestId());
            WebElement bccNode = accViewEditPage.getNodeByPath("/" + randomACC2.getDen() + "/" + randomPropertyTerm);
            ACCViewEditPage.BCCPanelContainer bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
            assertEquals(randomPropertyTerm, getText(bccPanelContainer.getBCCPPanel().getPropertyTermField()));
        }
    }

}
