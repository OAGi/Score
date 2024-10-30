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
import org.oagi.score.e2e.page.core_component.ASCCPChangeACCDialog;
import org.oagi.score.e2e.page.core_component.ASCCPViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofSeconds;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_10_14_EditingRevisionDeveloperASCCP extends BaseTest {
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
    public void test_TA_10_14_1_a() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
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
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch(asccp.getDen(), branch);
        asccpViewEditPage.hitReviseButton();

        //reload the page to verify
        viewEditCoreComponentPage.openPage();
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        assertDisabled(asccpPanel.getGUIDField());
        assertDisabled(asccpPanel.getPropertyTermField());
        assertDisabled(asccpPanel.getNamespaceSelectField());
    }

    @Test
    public void test_TA_10_14_1_b() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
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
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch(asccp.getDen(), branch);
        asccpViewEditPage.hitReviseButton();

        //reload the page
        viewEditCoreComponentPage.openPage();
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        String newDefinitionSource = randomPrint(50, 100).trim();
        asccpPanel.setDefinitionSource(newDefinitionSource);
        String newDefinition = randomPrint(50, 100).trim();
        asccpPanel.setDefinition(newDefinition);
        asccpViewEditPage.hitUpdateButton();

        //reload the page
        viewEditCoreComponentPage.openPage();
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        asccpPanel = asccpViewEditPage.getASCCPPanel();
        assertEquals(newDefinitionSource, getText(asccpPanel.getDefinitionSourceField()));
        assertEquals(newDefinition, getText(asccpPanel.getDefinitionField()));
    }

    @Test
    public void test_TA_10_14_1_c() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        ASCCPObject asccp, asccp_not_reusable;
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
            ACCObject acc_association_for_noreusable = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "Published");
            asccp_not_reusable = coreComponentAPI.createRandomASCCP(acc_association_for_noreusable, developer, namespace, "Published");
            asccp_not_reusable.setReusable(false);
            coreComponentAPI.updateASCCP(asccp_not_reusable);

            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "Published");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        assertChecked(asccpPanel.getReusableCheckbox());
        asccpViewEditPage.hitReviseButton();

        //reload the page
        viewEditCoreComponentPage.openPage();
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        asccpPanel = asccpViewEditPage.getASCCPPanel();
        assertChecked(asccpPanel.getReusableCheckbox());
        assertDisabled(asccpPanel.getReusableCheckbox());

        //Test when "Reusable" checkbox is unchecked in the original developer ASCCP
        //reload the page
        viewEditCoreComponentPage.openPage();
        waitFor(ofSeconds(1L));
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp_not_reusable.getAsccpManifestId());
        WebElement asccNodeNotReusable = asccpViewEditPage.getNodeByPath("/" + asccp_not_reusable.getPropertyTerm());
        asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNodeNotReusable).getASCCPPanel();
        ASCCPViewEditPage.ASCCPPanel finalAsccpPanel = asccpPanel;
        assertThrows(ElementClickInterceptedException.class, () -> finalAsccpPanel.toggleReusable());
        assertNotChecked(asccpPanel.getReusableCheckbox());
        assertDisabled(asccpPanel.getReusableCheckbox());
        asccpViewEditPage.hitReviseButton();

        asccpViewEditPage.openPage();
        asccNodeNotReusable = asccpViewEditPage.getNodeByPath("/" + asccp_not_reusable.getPropertyTerm());
        asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNodeNotReusable).getASCCPPanel();
        assertEquals("2", getText(asccpPanel.getRevisionField()));
        assertNotChecked(asccpPanel.getReusableCheckbox());
        assertEnabled(asccpPanel.getReusableCheckbox());
    }

    @Test
    public void test_TA_10_14_1_d_deprecated_in_previous_version() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
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
            asccp.setDeprecated(true);
            coreComponentAPI.updateASCCP(asccp);
            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "Published");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        asccpViewEditPage.hitReviseButton();

        //reload the page
        viewEditCoreComponentPage.openPage();
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        assertChecked(asccpPanel.getDeprecatedCheckbox());
        assertDisabled(asccpPanel.getDeprecatedCheckbox());
    }

    @Test
    public void test_TA_10_14_1_d_not_deprecated_in_previous_version() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
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
            asccp.setDeprecated(false);
            coreComponentAPI.updateASCCP(asccp);
            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "Published");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        asccpViewEditPage.hitReviseButton();

        //reload the page
        viewEditCoreComponentPage.openPage();
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        assertNotChecked(asccpPanel.getDeprecatedCheckbox());
        assertEnabled(asccpPanel.getDeprecatedCheckbox());
    }

    @Test
    public void test_TA_10_14_1_e_nillable_in_previous_revision() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
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
            asccp.setNillable(true);
            coreComponentAPI.updateASCCP(asccp);
            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "Published");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch(asccp.getDen(), branch);
        asccpViewEditPage.hitReviseButton();

        //reload the page
        viewEditCoreComponentPage.openPage();
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        assertChecked(asccpPanel.getNillableCheckbox());
        assertDisabled(asccpPanel.getNillableCheckbox());
    }

    @Test
    public void test_TA_10_14_1_e_not_nillable_in_previous_revision() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
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
            asccp.setNillable(false);
            coreComponentAPI.updateASCCP(asccp);
            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "Published");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByDenAndBranch(asccp.getDen(), branch);
        asccpViewEditPage.hitReviseButton();

        //reload the page
        viewEditCoreComponentPage.openPage();
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        assertNotChecked(asccpPanel.getNillableCheckbox());
        assertEnabled(asccpPanel.getNillableCheckbox());
    }

    @Test
    public void test_TA_10_14_1_f() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
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
            asccp.setDefinitionSource(randomPrint(50, 100).trim());
            asccp.setDefinition(null);
            coreComponentAPI.updateASCCP(asccp);
            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "Published");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        asccpViewEditPage.hitReviseButton();

        //reload the page
        viewEditCoreComponentPage.openPage();
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        asccpPanel.setDefinitionSource(randomPrint(50, 100).trim());
        ASCCPViewEditPage finalAsccpViewEditPage = asccpViewEditPage;
        assertThrows(TimeoutException.class, () -> finalAsccpViewEditPage.hitUpdateButton());
        assertEquals("Update without definitions.", getText(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//div[contains(@class, \"mat-mdc-dialog-title\")]/span"))));

    }

    @Test
    public void test_TA_10_14_1_g() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        ASCCPObject asccp;
        BCCPObject bccp, bccp_to_append;
        ACCObject acc, acc_association;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Published");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            bccp_to_append = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Published");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "Published");
            coreComponentAPI.updateASCCP(asccp);
            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "Published");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        asccpViewEditPage.hitReviseButton();

        //reload the page
        viewEditCoreComponentPage.openPage();
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        //ACC node cannot be changed
        WebElement accNode = asccpViewEditPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/" + acc_association.getDen());
        ASCCPViewEditPage.ACCPanel accPanel = asccpViewEditPage.getACCPanel(accNode);
        assertFalse(accPanel.getCoreComponentField().isEnabled());
        assertEquals("ACC", getText(accPanel.getCoreComponentField()));
        assertFalse(accPanel.getReleaseField().isEnabled());
        assertFalse(accPanel.getRevisionField().isEnabled());
        assertFalse(accPanel.getStateField().isEnabled());
        assertFalse(accPanel.getOwnerField().isEnabled());
        assertFalse(accPanel.getGUIDField().isEnabled());
        assertFalse(accPanel.getDENField().isEnabled());
        assertFalse(accPanel.getObjectClassTermField().isEnabled());
        assertDisabled(accPanel.getComponentTypeSelectField());
        assertDisabled(accPanel.getNamespaceSelectField());
        assertFalse(accPanel.getDefinitionSourceField().isEnabled());
        assertEquals("true", accPanel.getDefinitionField().getAttribute("readonly"));

        //BCCP node cannot be changed
        //reload the page
        viewEditCoreComponentPage.openPage();
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        WebElement bccpNode = asccpViewEditPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/" + acc_association.getDen() + "/" + bccp_to_append.getPropertyTerm());
        ASCCPViewEditPage.BCCPPanel bccpPanel = asccpViewEditPage.getBCCPanelContainer(bccpNode).getBCCPPanel();
        assertFalse(bccpPanel.getCoreComponentField().isEnabled());
        assertEquals("BCCP", getText(bccpPanel.getCoreComponentField()));
        assertFalse(bccpPanel.getReleaseField().isEnabled());
        assertFalse(bccpPanel.getRevisionField().isEnabled());
        assertFalse(bccpPanel.getStateField().isEnabled());
        assertFalse(bccpPanel.getOwnerField().isEnabled());
        assertFalse(bccpPanel.getGUIDField().isEnabled());
        assertFalse(bccpPanel.getDENField().isEnabled());
        assertFalse(bccpPanel.getPropertyTermField().isEnabled());
        assertDisabled(bccpPanel.getNillableCheckbox());
        assertDisabled(bccpPanel.getDeprecatedCheckbox());
        assertDisabled(bccpPanel.getValueConstraintSelectField());
        assertDisabled(bccpPanel.getNamespaceSelectField());
        assertFalse(bccpPanel.getDefinitionSourceField().isEnabled());
        assertEquals("true", bccpPanel.getDefinitionField().getAttribute("readonly"));

    }

    @Test
    public void test_TA_10_14_2() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
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
            coreComponentAPI.updateASCCP(asccp);
            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "Published");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        asccpViewEditPage.hitReviseButton();

        //reload the page
        viewEditCoreComponentPage.openPage();
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());


        //change ACC
        ACCObject anotherACC = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ASCCPChangeACCDialog asccpChangeACCDialog = asccpViewEditPage.openChangeACCDialog("/" + asccp.getPropertyTerm());
        asccpChangeACCDialog.hitUpdateButton(anotherACC.getDen());

        viewEditCoreComponentPage.openPage();
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPPanel();
        String asccpDEN = getText(asccpPanel.getDENField());
        assertTrue(asccpDEN.endsWith(anotherACC.getObjectClassTerm()));
    }

    @Test
    public void test_TA_10_14_3() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
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
            coreComponentAPI.updateASCCP(asccp);
            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "Published");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        asccpViewEditPage.hitReviseButton();

        //reload the page
        viewEditCoreComponentPage.openPage();
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        asccpViewEditPage.hitCancelButton();

        //reload the page
        viewEditCoreComponentPage.openPage();
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        assertEquals("Published", getText(asccpPanel.getStateField()));
        assertEquals("1", getText(asccpPanel.getRevisionField()));
    }
}
