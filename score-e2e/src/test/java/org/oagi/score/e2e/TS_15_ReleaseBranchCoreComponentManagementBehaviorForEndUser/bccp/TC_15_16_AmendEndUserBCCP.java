package org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.bccp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.menu.CoreComponentMenu;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.ASCCPViewEditPage;
import org.oagi.score.e2e.page.core_component.BCCPViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.AssertionHelper.assertEnabled;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;


@Execution(ExecutionMode.CONCURRENT)
public class TC_15_16_AmendEndUserBCCP extends BaseTest {
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
    public void test_TA_15_16_1() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
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
        BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());

        bccpViewEditPage.hitAmendButton();

        //reload the page to verify
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());
        WebElement asccNode = bccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertEquals("2", getText(bccpPanel.getRevisionField()));
        assertEquals("WIP", getText(bccpPanel.getStateField()));
        assertEquals(bccp.getPropertyTerm(), getText(bccpPanel.getPropertyTermField()));
        assertEquals(bccp.getDen(), getText(bccpPanel.getDENField()));

        assertNotChecked(bccpPanel.getNillableCheckbox());
        assertEnabled(bccpPanel.getNillableCheckbox());

        assertNotChecked(bccpPanel.getDeprecatedCheckbox());
        assertEnabled(bccpPanel.getDeprecatedCheckbox());

        assertEquals(bccp.getDefinitionSource(), getText(bccpPanel.getDefinitionSourceField()));
        assertEquals(bccp.getDefinition(), getText(bccpPanel.getDefinitionField()));
    }

    @Test
    public void test_TA_15_16_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "10.8.7.1";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        ASCCPObject asccp;
        BCCPObject bccp;
        ACCObject acc;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Published");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            ACCObject acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "Published");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "Published");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());

        assertEquals(0, getDriver().findElements(By.xpath("//span[contains(text(), \"Amend\")]//ancestor::button[1]")).size());

    }

    @Test
    public void test_TA_15_16_3_a() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
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
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch(asccp.getDen(), branch);
        BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());

        asccpViewEditPage.hitAmendButton();

        //reload the page to verify
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertDisabled(bccpPanel.getGUIDField());
        assertDisabled(bccpPanel.getDENField());
        assertDisabled(bccpPanel.getPropertyTermField());
        assertDisabled(bccpPanel.getNamespaceSelectField());
    }

    @Test
    public void test_TA_15_16_3_b() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, anotherUser, namespace, "Production");
        randomBCCP.setNillable(false);
        randomBCCP.setFixedValue(randomAlphabetic(5, 10));
        coreComponentAPI.updateBCCP(randomBCCP);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        bccpViewEditPage.hitAmendButton();

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
    public void test_TA_15_16_3_c_deprecated_in_previous_version() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ASCCPObject asccp;
        BCCPObject bccp;
        ACCObject acc;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, anotherUser, namespace, "Production");
            bccp.setDeprecated(true);
            coreComponentAPI.updateBCCP(bccp);
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Production");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            ACCObject acc_association = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, anotherUser, namespace, "Production");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Production");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, anotherUser, namespace, "Production");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "Production");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());
        bccpViewEditPage.hitAmendButton();

        //reload the page
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertChecked(bccpPanel.getDeprecatedCheckbox());
        assertDisabled(bccpPanel.getDeprecatedCheckbox());

    }

    @Test
    public void test_TA_15_16_3_c_not_deprecated_in_previous_version() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ASCCPObject asccp;
        BCCPObject bccp;
        ACCObject acc;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, anotherUser, namespace, "Production");
            bccp.setDeprecated(false);
            coreComponentAPI.updateBCCP(bccp);
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Production");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            ACCObject acc_association = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, anotherUser, namespace, "Production");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Production");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, anotherUser, namespace, "Production");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "Production");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());
        bccpViewEditPage.hitAmendButton();

        //reload the page
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertNotChecked(bccpPanel.getDeprecatedCheckbox());
        assertEnabled(bccpPanel.getDeprecatedCheckbox());

    }

    @Test
    public void test_TA_15_16_3_d_nillable_in_previous_version() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ASCCPObject asccp;
        BCCPObject bccp;
        ACCObject acc;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, anotherUser, namespace, "Production");
            bccp.setNillable(true);
            coreComponentAPI.updateBCCP(bccp);
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Production");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            ACCObject acc_association = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, anotherUser, namespace, "Production");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Production");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, anotherUser, namespace, "Production");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "Production");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }

        BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());
        bccpViewEditPage.hitAmendButton();

        //reload the page
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertChecked(bccpPanel.getNillableCheckbox());
        assertDisabled(bccpPanel.getNillableCheckbox());

    }

    @Test
    public void test_TA_15_16_3_d_not_nillable_in_previous_version() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ASCCPObject asccp;
        BCCPObject bccp;
        ACCObject acc;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, anotherUser, namespace, "Production");
            bccp.setNillable(false);
            coreComponentAPI.updateBCCP(bccp);
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Production");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            ACCObject acc_association = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, anotherUser, namespace, "Production");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Production");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, anotherUser, namespace, "Production");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "Production");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }

        BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());
        bccpViewEditPage.hitAmendButton();

        //reload the page
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertNotChecked(bccpPanel.getNillableCheckbox());
        assertEnabled(bccpPanel.getNillableCheckbox());

    }

    @Test
    public void test_TA_15_16_3_e() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, anotherUser, namespace, "Production");
        randomBCCP.setDefaultValue(randomAlphabetic(5, 10));
        coreComponentAPI.updateBCCP(randomBCCP);

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
    public void test_TA_15_16_3_f() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, anotherUser, namespace, "Production");
        randomBCCP.setDefinitionSource(randomPrint(50, 100).trim());
        randomBCCP.setDefinition(randomPrint(50, 100).trim());
        coreComponentAPI.updateBCCP(randomBCCP);

        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        bccpViewEditPage.hitAmendButton();

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
    public void test_TA_15_16_3_g() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, anotherUser, namespace, "Production");
        randomBCCP.setDefinitionSource(randomPrint(50, 100).trim());
        // No definition
        randomBCCP.setDefinition(null);
        coreComponentAPI.updateBCCP(randomBCCP);

        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        bccpViewEditPage.hitAmendButton();

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
    public void test_TA_15_16_3_h() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, anotherUser, namespace, "Production");

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
    public void test_TA_15_16_4() {


    }

    @Test
    public void test_TA_15_16_5() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, anotherUser, namespace, "Production");
        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        bccpViewEditPage.hitAmendButton();

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
