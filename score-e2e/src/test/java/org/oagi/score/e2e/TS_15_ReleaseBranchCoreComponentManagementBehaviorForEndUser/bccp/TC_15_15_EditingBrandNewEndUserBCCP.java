package org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.bccp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.BCCPCreateDialog;
import org.oagi.score.e2e.page.core_component.BCCPViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.AssertionHelper.assertEnabled;

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
        String namespace = "http://www.openapplications.org/oagis/10";
        bccpPanel.setNamespace(namespace);
        String definition = randomPrint(50, 100).trim();
        bccpPanel.setDefinition(definition);

        assertTrue(bccpViewEditPage.getUpdateButton(true).isEnabled());
    }

    @Test
    public void test_TA_15_15_1_b() {


    }

    @Test
    public void test_TA_15_15_1_c() {

    }

    @Test
    public void test_TA_15_15_1_d() {

    }

    @Test
    public void test_TA_15_15_1_e() {

    }
    @Test
    public void test_TA_15_15_1_f() {

    }

    @Test
    public void test_TA_15_15_1_g() {

    }

    @Test
    public void test_TA_15_15_1_h() {

    }

    @Test
    public void test_TA_15_15_2() {

    }

    @Test
    public void test_TA_15_15_3() {

    }


    @Test
    public void test_TA_15_15_4() {

    }

}
