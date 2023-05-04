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
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.AssertionHelper.assertEnabled;
import static org.oagi.score.e2e.impl.PageHelper.getText;


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

    }


    @Test
    public void test_TA_15_16_3_a() {

    }

    @Test
    public void test_TA_15_16_3_b() {

    }


    @Test
    public void test_TA_15_16_3_c() {

    }

    @Test
    public void test_TA_15_16_3_d() {

    }

    @Test
    public void test_TA_15_16_3_e() {

    }

    @Test
    public void test_TA_15_16_3_f() {

    }

    @Test
    public void test_TA_15_16_3_g() {

    }

    @Test
    public void test_TA_15_16_3_h() {

    }

    @Test
    public void test_TA_15_16_4() {

    }

    @Test
    public void test_TA_15_16_5() {

    }

}
