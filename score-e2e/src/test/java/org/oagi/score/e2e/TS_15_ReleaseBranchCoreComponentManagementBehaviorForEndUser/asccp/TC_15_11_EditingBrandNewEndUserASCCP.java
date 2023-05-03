package org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.asccp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.ASCCPCreateDialog;
import org.oagi.score.e2e.page.core_component.ASCCPViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.AssertionHelper.assertEnabled;

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

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "WIP");
        ;
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

    }


    @Test
    public void test_TA_15_11_1_c() {

    }

    @Test
    public void test_TA_15_11_1_d() {

    }


    @Test
    public void test_TA_15_11_1_e() {

    }

    @Test
    public void test_TA_15_11_1_f() {

    }

    @Test
    public void test_TA_15_11_1_g() {

    }

    @Test
    public void test_TA_15_11_1_h() {

    }

    @Test
    public void test_TA_15_11_1_i() {

    }

    @Test
    public void test_TA_15_11_2() {

    }

    @Test
    public void test_TA_15_11_3() {

    }


    @Test
    public void test_TA_15_11_4() {

    }




}
