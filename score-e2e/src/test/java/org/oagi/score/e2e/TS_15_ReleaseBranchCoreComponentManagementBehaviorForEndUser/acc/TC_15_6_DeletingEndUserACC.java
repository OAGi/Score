package org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.acc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.obj.ACCObject;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.ACCSetBaseACCDialog;
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_15_6_DeletingEndUserACC extends BaseTest {
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
    public void test_TA_15_6_1() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(endUser, release, namespace, "WIP");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitDeleteButton();

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("1", getText(accPanel.getRevisionField()));
        assertEquals("Deleted", getText(accPanel.getStateField()));
    }

    @Test
    public void test_TA_15_6_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
        ACCObject acc, acc_descendant, acc_descendant_base, new_acc_base;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(endUser, release, namespace, "WIP");
            new_acc_base = coreComponentAPI.createRandomACC(endUser, release, namespace, "Published");

            acc_descendant = coreComponentAPI.createRandomACC(endUser, release, namespace, "WIP");
            acc_descendant_base = coreComponentAPI.createRandomACC(endUser, release, namespace, "Deleted");
            coreComponentAPI.updateBasedACC(acc_descendant, acc_descendant_base);

            coreComponentAPI.updateBasedACC(acc, acc_descendant);
        }
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + acc_descendant.getDen());
        assertEquals(1, getDriver().findElements(By.xpath("//*[@ng-reflect-message=\"Deleted\" or contains(@class,'text-line-through')]")).size());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc_descendant_base.getAccManifestId());
        accViewEditPage.hitRestoreButton();

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        assertEquals(0, getDriver().findElements(By.xpath("//*[@ng-reflect-message=\"Deleted\" or contains(@class,'text-line-through')]")).size());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc_descendant.getAccManifestId());
        accNode = accViewEditPage.getNodeByPath("/" + acc_descendant.getDen() + "/" + acc_descendant_base.getDen());
        accViewEditPage.deleteBaseACC("/" + acc_descendant.getDen() + "/" + acc_descendant_base.getDen());
        ACCSetBaseACCDialog accSetBaseACCDialog = accViewEditPage.setBaseACC("/" + acc_descendant.getDen());
        accSetBaseACCDialog.hitApplyButton(new_acc_base.getDen());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + acc_descendant.getDen() + "/" + new_acc_base.getDen());
        assertTrue(accNode.isDisplayed());
    }


    @Test
    public void test_TA_15_6_3() {

    }


    @Test
    public void test_TA_15_6_4() {

    }

    @Test
    public void test_TA_15_6_5() {

    }


    @Test
    public void test_TA_15_6_6() {

    }

    @Test
    public void test_TA_15_6_7() {

    }


}
