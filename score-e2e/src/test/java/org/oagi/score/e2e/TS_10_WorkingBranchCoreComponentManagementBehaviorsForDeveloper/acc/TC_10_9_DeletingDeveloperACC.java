package org.oagi.score.e2e.TS_10_WorkingBranchCoreComponentManagementBehaviorsForDeveloper.acc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.ACCSetBaseACCDialog;
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.ASCCPViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_10_9_DeletingDeveloperACC extends BaseTest {
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
    @DisplayName("TC_10_9_TA_1")
    public void revision_one_wip_acc_can_be_deleted() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "WIP");
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
    @DisplayName("TC_10_9_TA_2")
    public void deleted_base_of_descendant_acc_is_flagged_until_restored_or_replaced() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_descendant, acc_descendant_base, new_acc_base;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            new_acc_base = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");

            acc_descendant = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            acc_descendant_base = coreComponentAPI.createRandomACC(developer, release, namespace, "Deleted");
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
    @DisplayName("TC_10_9_TA_3")
    public void deleted_direct_base_acc_is_flagged_until_restored_or_replaced() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_descendant_base, new_acc_base, acc_base_for_delete;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            acc_base_for_delete = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            new_acc_base = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            acc_descendant_base = coreComponentAPI.createRandomACC(developer, release, namespace, "Deleted");
            coreComponentAPI.updateBasedACC(acc, acc_descendant_base);
        }
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + acc_descendant_base.getDen());
        assertEquals(1, getDriver().findElements(By.xpath("//*[@ng-reflect-message=\"Deleted\" or contains(@class,'text-line-through')]")).size());

        accViewEditPage.deleteBaseACC("/" + acc.getDen() + "/" + acc_descendant_base.getDen());
        ACCSetBaseACCDialog accSetBaseACCDialog = accViewEditPage.setBaseACC("/" + acc.getDen());
        accSetBaseACCDialog.hitApplyButton(new_acc_base.getDen());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + new_acc_base.getDen());
        assertTrue(accNode.isDisplayed());
        accViewEditPage.deleteBaseACC("/" + acc.getDen() + "/" + new_acc_base.getDen());
        accSetBaseACCDialog = accViewEditPage.setBaseACC("/" + acc.getDen());
        accSetBaseACCDialog.hitApplyButton(acc_base_for_delete.getDen());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc_base_for_delete.getAccManifestId());
        accViewEditPage.hitDeleteButton();

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + acc_base_for_delete.getDen());
        assertEquals(1, getDriver().findElements(By.xpath("//*[@ng-reflect-message=\"Deleted\" or contains(@class,'text-line-through')]")).size());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc_base_for_delete.getAccManifestId());
        accViewEditPage.hitRestoreButton();

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + acc_base_for_delete.getDen());
        assertEquals(0, getDriver().findElements(By.xpath("//*[@ng-reflect-message=\"Deleted\" or contains(@class,'text-line-through')]")).size());
    }

    @Test
    @DisplayName("TC_10_9_TA_4")
    public void deleted_acc_used_by_ascc_association_is_flagged_until_restored() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_descendant_base, new_acc_base, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "Deleted");
            new_acc_base = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.updateBasedACC(acc, new_acc_base);

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "WIP");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "WIP");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        waitFor(Duration.ofMillis(5000L));
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());
        assertEquals(1, getDriver().findElements(By.xpath("//*[@ng-reflect-message=\"Deleted\" or contains(@class,'text-line-through')]")).size());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc_association.getAccManifestId());
        accViewEditPage.hitRestoreButton();

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm());

        assertEquals(0, getDriver().findElements(By.xpath("//*[@ng-reflect-message=\"Deleted\" or contains(@class,'text-line-through')]")).size());
    }

    @Test
    @DisplayName("TC_10_9_TA_5")
    public void deleted_descendant_acc_is_flagged_until_restored() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        ACCObject acc, acc_descendant_base;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            acc_descendant_base = coreComponentAPI.createRandomACC(developer, release, namespace, "Deleted");
            coreComponentAPI.updateBasedACC(acc, acc_descendant_base);
        }
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        assertEquals(1, getDriver().findElements(By.xpath("//*[@ng-reflect-message=\"Deleted\" or contains(@class,'text-line-through')]")).size());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc_descendant_base.getAccManifestId());
        accViewEditPage.hitRestoreButton();

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        assertEquals(0, getDriver().findElements(By.xpath("//*[@ng-reflect-message=\"Deleted\" or contains(@class,'text-line-through')]")).size());
    }

    @Test
    @DisplayName("TC_10_9_TA_6")
    public void deleted_acc_used_by_asccp_is_flagged_until_restored() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        ACCObject acc, new_acc_base, acc_association;
        ASCCObject ascc;
        ASCCPObject asccp;

        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");
            acc_association = coreComponentAPI.createRandomACC(developer, release, namespace, "Deleted");
            new_acc_base = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            coreComponentAPI.updateBasedACC(acc, new_acc_base);

            asccp = coreComponentAPI.createRandomASCCP(acc_association, developer, namespace, "WIP");
            ascc = coreComponentAPI.appendASCC(acc, asccp, "WIP");
            ascc.setCardinalityMax(1);
            coreComponentAPI.updateASCC(ascc);
        }
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        WebElement accNode = asccpViewEditPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/" + acc_association.getDen());
        assertEquals(1, getDriver().findElements(By.xpath("//*[@ng-reflect-message=\"Deleted\" or contains(@class,'text-line-through')]")).size());

        ACCViewEditPage accViewEditPage = asccpViewEditPage.openACCInNewTab(accNode);
        accViewEditPage.hitRestoreButton();

        switchToMainTab(getDriver());
        viewEditCoreComponentPage.openPage();
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        WebElement asccpNode = asccpViewEditPage.getNodeByPath("/" + asccp.getPropertyTerm());
        assertEquals(0, getDriver().findElements(By.xpath("//*[@ng-reflect-message=\"Deleted\" or contains(@class,'text-line-through')]")).size());

    }

    @Test
    @DisplayName("TC_10_9_TA_7")
    public void acc_revision_greater_than_one_cannot_be_deleted_in_any_state() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "Published");
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        WebElement accNode = accViewEditPage.getNodeByPath("/" + acc.getDen());
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(accNode);
        assertEquals("2", getText(accPanel.getRevisionField()));
        assertEquals("WIP", getText(accPanel.getStateField()));
        assertEquals(0, getDriver().findElements(By.xpath("//span[contains(text(),\"Delete\")]//ancestor::button[1]")).size());

        accViewEditPage.moveToDraft();
        assertEquals("2", getText(accPanel.getRevisionField()));
        assertEquals("Draft", getText(accPanel.getStateField()));
        assertEquals(0, getDriver().findElements(By.xpath("//span[contains(text(),\"Delete\")]//ancestor::button[1]")).size());

        accViewEditPage.moveToCandidate();
        assertEquals("2", getText(accPanel.getRevisionField()));
        assertEquals("Candidate", getText(accPanel.getStateField()));
        assertEquals(0, getDriver().findElements(By.xpath("//span[contains(text(),\"Delete\")]//ancestor::button[1]")).size());
    }
}
