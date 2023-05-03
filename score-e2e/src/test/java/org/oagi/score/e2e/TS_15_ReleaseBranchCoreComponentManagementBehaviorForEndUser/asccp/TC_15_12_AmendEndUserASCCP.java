package org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.asccp;


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
import org.oagi.score.e2e.page.core_component.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.click;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_15_12_AmendEndUserASCCP extends BaseTest {
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
    public void test_TA_15_12_1() {
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

        asccpViewEditPage.hitAmendButton();

        //reload the page to verify
        viewEditCoreComponentPage.openPage();
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        WebElement asccNode = asccpViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        ASCCPViewEditPage.ASCCPPanel asccpPanel = asccpViewEditPage.getASCCPanelContainer(asccNode).getASCCPPanel();
        assertEquals("2", getText(asccpPanel.getRevisionField()));
        assertEquals("WIP", getText(asccpPanel.getStateField()));
        assertEquals(asccp.getPropertyTerm(), getText(asccpPanel.getPropertyTermField()));
        assertEquals(asccp.getDen(), getText(asccpPanel.getDENField()));

        assertChecked(asccpPanel.getReusableCheckbox());
        assertEnabled(asccpPanel.getReusableCheckbox());

        assertNotChecked(asccpPanel.getNillableCheckbox());
        assertEnabled(asccpPanel.getNillableCheckbox());

        assertNotChecked(asccpPanel.getDeprecatedCheckbox());
        assertEnabled(asccpPanel.getDeprecatedCheckbox());

        assertEquals(asccp.getDefinitionSource(), getText(asccpPanel.getDefinitionSourceField()));
        assertEquals(asccp.getDefinition(), getText(asccpPanel.getDefinitionField()));
    }

    @Test
    public void test_TA_15_12_2() {
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
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());

        assertEquals(0, getDriver().findElements(By.xpath("//span[contains(text(), \"Amend\")]//ancestor::button[1]")).size());
    }

    @Test
    public void test_TA_15_12_3() {
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
        assertEquals(0, getDriver().findElements(By.xpath("//span[contains(text(), \"Amend\")]//ancestor::button[1]")).size());

    }

    @Test
    public void test_TA_15_12_4_a() {
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

        asccpViewEditPage.hitAmendButton();

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
    public void test_TA_15_12_4_b() {


    }

    @Test
    public void test_TA_15_12_4_c() {

    }

    @Test
    public void test_TA_15_12_4_d() {

    }

    @Test
    public void test_TA_15_12_4_e() {

    }

    @Test
    public void test_TA_15_12_4_f() {

    }

    @Test
    public void test_TA_15_12_4_g() {

    }

    @Test
    public void test_TA_15_12_4_h() {

    }

    @Test
    public void test_TA_15_12_5() {

    }


    @Test
    public void test_TA_15_12_6() {

    }
}
