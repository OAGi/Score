package org.oagi.score.e2e.TS_10_WorkingBranchCoreComponentManagementBehaviorsForDeveloper.bccp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.BCCPChangeBDTDialog;
import org.oagi.score.e2e.page.core_component.BCCPViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;

@Execution(ExecutionMode.CONCURRENT)
public class TC_10_21_EditingRevisionDeveloperBCCP extends BaseTest {

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
    public void test_TA_10_21_1_a() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        randomBCCP.setNillable(false);
        coreComponentAPI.updateBCCP(randomBCCP);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByDenAndBranch(randomBCCP.getDen(), branch);
        bccpViewEditPage.hitReviseButton();

        // reload the page
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());

        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertDisabled(bccpPanel.getGUIDField());
        assertDisabled(bccpPanel.getDENField());
        assertDisabled(bccpPanel.getNamespaceSelectField());
        assertDisabled(bccpPanel.getPropertyTermField());
    }

    @Test
    public void test_TA_10_21_1_b() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        randomBCCP.setNillable(false);
        randomBCCP.setFixedValue(randomAlphabetic(5, 10));
        coreComponentAPI.updateBCCP(randomBCCP);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByDenAndBranch(randomBCCP.getDen(), branch);
        bccpViewEditPage.hitReviseButton();

        // reload the page
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());

        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertEquals("Fixed Value", getText(bccpPanel.getValueConstraintSelectField()));
        assertEquals(randomBCCP.getFixedValue(), getText(bccpPanel.getFixedValueField()));
        assertDisabled(bccpPanel.getFixedValueField());
    }

    @Test
    public void test_TA_10_21_1_c_deprecated_in_previous_revision() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        randomBCCP.setDeprecated(true);
        coreComponentAPI.updateBCCP(randomBCCP);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByDenAndBranch(randomBCCP.getDen(), branch);
        bccpViewEditPage.hitReviseButton();

        // reload the page
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());

        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertChecked(bccpPanel.getDeprecatedCheckbox());
        assertDisabled(bccpPanel.getDeprecatedCheckbox());
    }

    @Test
    public void test_TA_10_21_1_c_not_deprecated_in_previous_revision() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        randomBCCP.setDeprecated(false);
        coreComponentAPI.updateBCCP(randomBCCP);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByDenAndBranch(randomBCCP.getDen(), branch);
        bccpViewEditPage.hitReviseButton();

        // reload the page
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());

        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertNotChecked(bccpPanel.getDeprecatedCheckbox());
        assertEnabled(bccpPanel.getDeprecatedCheckbox());
    }

    @Test
    public void test_TA_10_21_1_d_nillable_in_previous_revision() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        randomBCCP.setNillable(true);
        coreComponentAPI.updateBCCP(randomBCCP);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        bccpViewEditPage.hitReviseButton();

        // reload the page
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());

        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertChecked(bccpPanel.getNillableCheckbox());
        assertDisabled(bccpPanel.getNillableCheckbox());
    }

    @Test
    public void test_TA_10_21_1_d_not_nillable_in_previous_revision() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        randomBCCP.setNillable(false);
        coreComponentAPI.updateBCCP(randomBCCP);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        bccpViewEditPage.hitReviseButton();

        // reload the page
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());

        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertNotChecked(bccpPanel.getNillableCheckbox());
        assertEnabled(bccpPanel.getNillableCheckbox());
    }

    @Test
    public void test_TA_10_21_1_e() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        randomBCCP.setDefaultValue(randomAlphabetic(5, 10));
        coreComponentAPI.updateBCCP(randomBCCP);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        bccpViewEditPage.hitReviseButton();

        // reload the page
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());

        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertEquals("Default Value", getText(bccpPanel.getValueConstraintSelectField()));
        assertEquals(randomBCCP.getDefaultValue(), getText(bccpPanel.getDefaultValueField()));
    }

    @Test
    public void test_TA_10_21_1_f() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        randomBCCP.setDefinitionSource(randomPrint(50, 100).trim());
        randomBCCP.setDefinition(randomPrint(50, 100).trim());
        coreComponentAPI.updateBCCP(randomBCCP);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        bccpViewEditPage.hitReviseButton();

        // reload the page
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());

        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertEquals(randomBCCP.getDefinitionSource(), getText(bccpPanel.getDefinitionSourceField()));
        assertEquals(randomBCCP.getDefinition(), getText(bccpPanel.getDefinitionField()));

        String newDefinitionSource = randomPrint(50, 100).trim();
        bccpPanel.setDefinitionSource(newDefinitionSource);
        String newDefinition = randomPrint(50, 100).trim();
        bccpPanel.setDefinition(newDefinition);
        bccpViewEditPage.hitUpdateButton();

        // reload the page
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());

        bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertEquals(newDefinitionSource, getText(bccpPanel.getDefinitionSourceField()));
        assertEquals(newDefinition, getText(bccpPanel.getDefinitionField()));
    }

    @Test
    public void test_TA_10_21_1_g() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        randomBCCP.setDefinitionSource(randomPrint(50, 100).trim());
        // No definition
        randomBCCP.setDefinition(null);
        coreComponentAPI.updateBCCP(randomBCCP);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        bccpViewEditPage.hitReviseButton();

        // reload the page
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());

        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        bccpPanel.setDefinitionSource(randomPrint(50, 100).trim());
        BCCPViewEditPage finalBccpViewEditPage = bccpViewEditPage;
        assertThrows(TimeoutException.class, () -> finalBccpViewEditPage.hitUpdateButton());
        assertEquals("Update without definitions.", getText(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//div[contains(@class, \"header\")]"))));
    }

    @Test
    public void test_TA_10_21_1_h() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        bccpViewEditPage.hitReviseButton();

        // reload the page
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        BCCPViewEditPage.DTPanel dtPanel = bccpViewEditPage.getBCCPPanelContainer().getDTPanel();

        assertFalse(dtPanel.getCoreComponentField().isEnabled());
        assertEquals("DT", getText(dtPanel.getCoreComponentField()));
        assertFalse(dtPanel.getReleaseField().isEnabled());
        assertEquals(getText(bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel().getReleaseField()),
                getText(dtPanel.getReleaseField()));
        assertFalse(dtPanel.getRevisionField().isEnabled());
        assertFalse(dtPanel.getStateField().isEnabled());
        assertEquals(dataType.getState(), getText(dtPanel.getStateField()));
        assertFalse(dtPanel.getOwnerField().isEnabled());
        assertFalse(dtPanel.getGUIDField().isEnabled());
        assertFalse(dtPanel.getDENField().isEnabled());
        assertEquals(dataType.getDen(), getText(dtPanel.getDENField()));
        assertFalse(dtPanel.getDataTypeTermField().isEnabled());
        assertEquals(dataType.getDataTypeTerm(), getText(dtPanel.getDataTypeTermField()));
        assertFalse(dtPanel.getQualifierField().isEnabled());
        assertEquals(dataType.getQualifier(), getText(dtPanel.getQualifierField()));
        assertFalse(dtPanel.getDefinitionSourceField().isEnabled());
        assertFalse(dtPanel.getDefinitionField().isEnabled());

        BCCPViewEditPage finalBccpViewEditPage = bccpViewEditPage;
        assertThrows(WebDriverException.class, () -> finalBccpViewEditPage.openChangeBDTDialog());
    }

    @Test
    @Disabled
    public void test_TA_10_21_2() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("fea2278fc93f48b98cf5bb3e32c004e8", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        bccpViewEditPage.hitReviseButton();

        // reload the page
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        BCCPChangeBDTDialog bccpChangeBDTDialog = bccpViewEditPage.openChangeBDTDialog();
        assertTrue(bccpChangeBDTDialog.isOpened());
    }
    @Test
    public void test_TA_10_21_3() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        bccpViewEditPage.hitReviseButton();

        // reload the page
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        bccpViewEditPage.hitCancelButton();

        // reload the page
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertEquals("Published", getText(bccpPanel.getStateField()));
        assertEquals("1", getText(bccpPanel.getRevisionField()));
    }

}
