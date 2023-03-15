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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.AssertionHelper.*;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_10_3_EditingBrandNewDeveloperACC extends BaseTest {

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
    public void test_TA_10_3_1_a() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertTrue(getText(accPanel.getComponentTypeSelectField()).contains("Base"));
        accPanel.getComponentTypeSelectField().sendKeys("Base(Abstract");
        assertDisabled(accPanel.getAbstractCheckbox());
        assertChecked(accPanel.getAbstractCheckbox());
    }

    @Test
    public void test_TA_10_3_1_b() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertTrue(getText(accPanel.getComponentTypeSelectField()).contains("Base"));
        accPanel.getComponentTypeSelectField().sendKeys("Base(Abstract");
        assertDisabled(accPanel.getAbstractCheckbox());
        assertChecked(accPanel.getAbstractCheckbox());
    }

    @Test
    public void test_TA_10_3_1_c() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertTrue(getText(accPanel.getComponentTypeSelectField()).contains("Base"));
        accPanel.getComponentTypeSelectField().sendKeys("Base(Abstract");
        assertDisabled(accPanel.getAbstractCheckbox());
        assertChecked(accPanel.getAbstractCheckbox());
    }

    @Test
    public void test_TA_10_3_1_d() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertTrue(getText(accPanel.getComponentTypeSelectField()).contains("Base"));
        accPanel.getComponentTypeSelectField().sendKeys("Base(Abstract");
        assertDisabled(accPanel.getAbstractCheckbox());
        assertChecked(accPanel.getAbstractCheckbox());
    }


    @Test
    public void test_TA_10_3_1_e() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertTrue(getText(accPanel.getComponentTypeSelectField()).contains("Base"));
        accPanel.getComponentTypeSelectField().sendKeys("Base(Abstract");
        assertDisabled(accPanel.getAbstractCheckbox());
        assertChecked(accPanel.getAbstractCheckbox());
    }

    @Test
    public void test_TA_10_3_1_f() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertTrue(getText(accPanel.getComponentTypeSelectField()).contains("Base"));
        accPanel.getComponentTypeSelectField().sendKeys("Base(Abstract");
        assertDisabled(accPanel.getAbstractCheckbox());
        assertChecked(accPanel.getAbstractCheckbox());
    }

    @Test
    public void test_TA_10_3_1_g() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertTrue(getText(accPanel.getComponentTypeSelectField()).contains("Base"));
        accPanel.getComponentTypeSelectField().sendKeys("Base(Abstract");
        assertDisabled(accPanel.getAbstractCheckbox());
        assertChecked(accPanel.getAbstractCheckbox());
    }

    @Test
    public void test_TA_10_3_1_h() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertTrue(getText(accPanel.getComponentTypeSelectField()).contains("Base"));
        accPanel.getComponentTypeSelectField().sendKeys("Base(Abstract");
        assertDisabled(accPanel.getAbstractCheckbox());
        assertChecked(accPanel.getAbstractCheckbox());
    }



}
