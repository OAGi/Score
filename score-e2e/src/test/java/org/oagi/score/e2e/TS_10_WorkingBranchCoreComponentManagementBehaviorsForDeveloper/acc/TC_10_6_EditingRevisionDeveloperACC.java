package org.oagi.score.e2e.TS_10_WorkingBranchCoreComponentManagementBehaviorsForDeveloper.acc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.ACCObject;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_10_6_EditingRevisionDeveloperACC extends BaseTest {

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
    public void test_TA_10_6_1_a() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "Published");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("2", getText(accPanel.getRevisionField()));
        assertDisabled(accPanel.getComponentTypeSelectField());

    }

    @Test
    public void test_TA_10_6_1_b() {

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        //abstract can change only from true to false
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch("Currency Exchange ABIE. Details", branch);
        accViewEditPage.hitReviseButton();
        WebElement accNode = accViewEditPage.getNodeByPath("/Currency Exchange ABIE. Details");
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("2", getText(accPanel.getRevisionField()));
        assertDisabled(accPanel.getComponentTypeSelectField());
        assertEquals("Semantics", getText(accPanel.getComponentTypeSelectField()));
        assertEnabled(accPanel.getAbstractCheckbox());
        assertChecked(accPanel.getAbstractCheckbox());

        //false to true cannot be changed when semantics
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch("Business Object Document. Details", branch);
        accViewEditPage.hitReviseButton();
        accNode = accViewEditPage.getNodeByPath("/Business Object Document. Details");
        accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("2", getText(accPanel.getRevisionField()));
        assertDisabled(accPanel.getComponentTypeSelectField());
        assertEquals("Semantics", getText(accPanel.getComponentTypeSelectField()));
        assertDisabled(accPanel.getAbstractCheckbox());
        assertNotChecked(accPanel.getAbstractCheckbox());

        //false to true cannot be changed when extension
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch("Change Status Extension. Details", branch);
        accViewEditPage.hitReviseButton();
        accNode = accViewEditPage.getNodeByPath("/Change Status Extension. Details");
        accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("2", getText(accPanel.getRevisionField()));
        assertDisabled(accPanel.getComponentTypeSelectField());
        assertEquals("Extension", getText(accPanel.getComponentTypeSelectField()));
        assertDisabled(accPanel.getAbstractCheckbox());
        assertNotChecked(accPanel.getAbstractCheckbox());

        //false to true cannot be changed when semantic group

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch("Container Instance Identifiers Group. Details", branch);
        accViewEditPage.hitReviseButton();
        accNode = accViewEditPage.getNodeByPath("/Container Instance Identifiers Group. Details");
        accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("2", getText(accPanel.getRevisionField()));
        assertDisabled(accPanel.getComponentTypeSelectField());
        assertEquals("Semantic Group", getText(accPanel.getComponentTypeSelectField()));
        assertDisabled(accPanel.getAbstractCheckbox());
        assertNotChecked(accPanel.getAbstractCheckbox());

        //abstract true cannot be changed when base
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch("Person Base. Details", branch);
        accViewEditPage.hitReviseButton();
        accNode = accViewEditPage.getNodeByPath("/Person Base. Details");
        accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("2", getText(accPanel.getRevisionField()));
        assertDisabled(accPanel.getComponentTypeSelectField());
        assertEquals("Base (Abstract)", getText(accPanel.getComponentTypeSelectField()));
        assertDisabled(accPanel.getAbstractCheckbox());
        assertChecked(accPanel.getAbstractCheckbox());

    }


    @Test
    public void test_TA_10_6_1_c() {

    }
    @Test
    public void test_TA_10_6_1_d() {

    }

    @Test
    public void test_TA_10_6_1_e() {

    }
}
