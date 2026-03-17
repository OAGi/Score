package org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.bccp;

import org.apache.commons.lang3.RandomStringUtils;
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
import org.oagi.score.e2e.page.core_component.*;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.visibilityOfElementLocated;


@Execution(ExecutionMode.CONCURRENT)
public class TC_15_16_AmendEndUserBCCP extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

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
    public void on_the_cc_detail_page_of_an_end_user_bccp_in_production_state_the() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace_endUser = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

        ASCCPObject asccp;
        BCCPObject bccp;
        ACCObject acc;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(anotherUser, release, namespace_endUser, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, anotherUser, namespace_endUser, "Production");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Production");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            ACCObject acc_association = coreComponentAPI.createRandomACC(anotherUser, release, namespace_endUser, "Production");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, anotherUser, namespace_endUser, "Production");
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
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertEquals("2", getText(bccpPanel.getRevisionField()));
        assertEquals("WIP", getText(bccpPanel.getStateField()));
        assertEquals(bccp.getPropertyTerm(), getText(bccpPanel.getPropertyTermField()));
        assertEquals(bccp.getDen(), getText(bccpPanel.getDENField()));

        assertChecked(bccpPanel.getNillableCheckbox());
        assertEnabled(bccpPanel.getNillableCheckbox());

        assertNotChecked(bccpPanel.getDeprecatedCheckbox());
        assertEnabled(bccpPanel.getDeprecatedCheckbox());

        assertEquals(bccp.getDefinitionSource(), getText(bccpPanel.getDefinitionSourceField()));
        assertEquals(bccp.getDefinition(), getText(bccpPanel.getDefinitionField()));
    }

    @Test
    public void end_user_cannot_amend_a_released_developer_bccp() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        ASCCPObject asccp;
        BCCPObject bccp;
        ACCObject acc;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, developer, namespace, "Published");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Published");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            ACCObject acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, developer, namespace, "Published");
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
    public void fields_guid_den_namespace_and_property_term_cannot_be_changed() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace_endUser = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

        ASCCPObject asccp;
        BCCPObject bccp;
        ACCObject acc;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(anotherUser, release, namespace_endUser, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, anotherUser, namespace_endUser, "Production");
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Production");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            ACCObject acc_association = coreComponentAPI.createRandomACC(anotherUser, release, namespace_endUser, "Production");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, anotherUser, namespace_endUser, "Production");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Production");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, anotherUser, namespace_endUser, "Production");
            ASCCObject ascc = coreComponentAPI.appendASCC(acc, asccp, "Production");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        CoreComponentMenu coreComponentMenu = homePage.getCoreComponentMenu();
        ViewEditCoreComponentPage viewEditCoreComponentPage = coreComponentMenu.openViewEditCoreComponentSubMenu();
        viewEditCoreComponentPage.toggleToDevView();
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
    public void if_a_fixed_value_is_already_applied_it_cannot_be_changed_hence_neither_default() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "Production");
        randomBCCP.setNillable(false);
        randomBCCP.setFixedValue(RandomStringUtils.secure().nextAlphabetic(5, 10));
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
    public void if_the_deprecated_was_already_true_in_the_previous_revision_the_field_along_with() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ASCCPObject asccp;
        BCCPObject bccp;
        ACCObject acc;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "Production");
            bccp.setDeprecated(true);
            coreComponentAPI.updateBCCP(bccp);
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Production");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            ACCObject acc_association = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "Production");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Production");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, endUser, namespace, "Production");
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
        assertEnabled(bccpPanel.getDeprecatedCheckbox());
    }

    @Test
    public void if_the_deprecated_was_already_true_in_the_previous_revision_the_field_along_with_scenario_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser, library);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ASCCPObject asccp;
        BCCPObject bccp;
        ACCObject acc;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, anotherUser, namespace, "Production");
            bccp.setDeprecated(false);
            coreComponentAPI.updateBCCP(bccp);
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Production");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            ACCObject acc_association = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, anotherUser, namespace, "Production");
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
    public void if_nillable_can_change_it_can_only_be_changed_from_false_to_true_nillable() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ASCCPObject asccp;
        BCCPObject bccp;
        ACCObject acc;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "Production");
            bccp.setNillable(true);
            coreComponentAPI.updateBCCP(bccp);
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Production");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            ACCObject acc_association = coreComponentAPI.createRandomACC(endUser, release, namespace, "Production");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "Production");
            coreComponentAPI.appendBCC(acc_association, bccp_to_append, "Production");

            asccp = coreComponentAPI.createRandomASCCP(acc_association, endUser, namespace, "Production");
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
        assertEnabled(bccpPanel.getNillableCheckbox());
    }

    @Test
    public void if_nillable_can_change_it_can_only_be_changed_from_false_to_true_nillable_scenario_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser, library);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ASCCPObject asccp;
        BCCPObject bccp;
        ACCObject acc;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");
            DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "dd0c8f86b160428da3a82d2866a5b48d", release.getReleaseNumber());
            bccp = coreComponentAPI.createRandomBCCP(release, dataType, anotherUser, namespace, "Production");
            bccp.setNillable(false);
            coreComponentAPI.updateBCCP(bccp);
            BCCObject bcc = coreComponentAPI.appendBCC(acc, bccp, "Production");
            bcc.setCardinalityMax(1);
            coreComponentAPI.updateBCC(bcc);

            ACCObject acc_association = coreComponentAPI.createRandomACC(anotherUser, release, namespace, "Production");
            BCCPObject bccp_to_append = coreComponentAPI.createRandomBCCP(release, dataType, anotherUser, namespace, "Production");
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
    public void if_the_default_value_can_change_it_can_change_to_any_valid_value_w() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "Production");
        randomBCCP.setDefaultValue(RandomStringUtils.secure().nextAlphabetic(5, 10));
        coreComponentAPI.updateBCCP(randomBCCP);

        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        bccpViewEditPage.hitAmendButton();

        // reload the page
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());

        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertEquals("Default Value", getText(bccpPanel.getValueConstraintSelectField()));
        assertEquals(randomBCCP.getDefaultValue(), getText(bccpPanel.getDefaultValueField()));
    }

    @Test
    public void definition_and_definition_source_can_change() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser, library);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(release, dataType, anotherUser, namespace, "Production");
        randomBCCP.setDefinitionSource(RandomStringUtils.secure().nextPrint(50, 100).trim());
        randomBCCP.setDefinition(RandomStringUtils.secure().nextPrint(50, 100).trim());
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

        String newDefinitionSource = RandomStringUtils.secure().nextPrint(50, 100).trim();
        bccpPanel.setDefinitionSource(newDefinitionSource);
        String newDefinition = RandomStringUtils.secure().nextPrint(50, 100).trim();
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
    public void warning_should_be_given_when_the_definition_is_empty() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser, library);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(release, dataType, anotherUser, namespace, "Production");
        randomBCCP.setDefinitionSource(RandomStringUtils.secure().nextPrint(50, 100).trim());
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
        bccpPanel.setDefinitionSource(RandomStringUtils.secure().nextPrint(50, 100).trim());
        BCCPViewEditPage finalBccpViewEditPage = bccpViewEditPage;
        assertThrows(TimeoutException.class, () -> finalBccpViewEditPage.hitUpdateButton());
        assertEquals("Update without definitions.", getText(visibilityOfElementLocated(getDriver(),
                By.xpath("//mat-dialog-container//div[contains(@class, \"mat-mdc-dialog-title\")]/span"))));

    }

    @Test
    public void fields_of_the_bdt_and_its_supplementary_components_cannot_be_changed() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser, library);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(release, dataType, anotherUser, namespace, "Production");

        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        bccpViewEditPage.hitAmendButton();

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
        assertDisabled(dtPanel.getDefinitionSourceField());
        assertEnabled(dtPanel.getDefinitionField());
    }

    @Test
    public void end_user_cannot_change_the_bdt_to_another_one() {
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
        BCCPCreateDialog bccpCreateDialog = viewEditCoreComponentPage.openBCCPCreateDialog(branch);
        BCCPViewEditPage bccpViewEditPage = bccpCreateDialog.create("System Environment_ Code. Type");
        BCCPChangeDTDialog bccpChangeDTDialog = bccpViewEditPage.openChangeDTDialog();
        String nextBDTDen = "Telephone_ Value. Type";
        bccpChangeDTDialog.update(nextBDTDen);

        BCCPViewEditPage.DTPanel dtPanel = bccpViewEditPage.getBCCPPanelContainer().getDTPanel();
        assertEquals(nextBDTDen, getText(dtPanel.getDENField()));
        assertEquals("Value", getText(dtPanel.getDataTypeTermField()));
        assertEquals("Telephone", getText(dtPanel.getQualifierField()));
    }

    @Test
    public void end_user_can_cancel_the_amendment_in_which_case_the_system_rollbacks_all_changes() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherUser);

        String branch = "10.8.7.1";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherUser, library);
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(release, dataType, anotherUser, namespace, "Production");
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
        assertEquals("Production", getText(bccpPanel.getStateField()));
        assertEquals("1", getText(bccpPanel.getRevisionField()));

    }
}
