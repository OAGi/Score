package org.oagi.score.e2e.TS_10_WorkingBranchCoreComponentManagementBehaviorsForDeveloper.bccp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.BCCPViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.switchToMainTab;

@Execution(ExecutionMode.CONCURRENT)
public class TC_10_23_DeletingDeveloperBCCP extends BaseTest {

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
    public void test_TA_10_23_1() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        bccpViewEditPage.hitDeleteButton();
        assertTrue(viewEditCoreComponentPage.isOpened());
        bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertEquals("Deleted", getText(bccpPanel.getStateField()));
    }

    @Test
    public void test_TA_10_23_2() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        ACCObject randomACC = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");

        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "WIP");
        coreComponentAPI.appendBCC(randomACC, randomBCCP, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        bccpViewEditPage.hitDeleteButton();
        assertTrue(viewEditCoreComponentPage.isOpened());
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(randomACC.getAccManifestId());
        WebElement bccNode = accViewEditPage.getNodeByPath("/" + randomACC.getDen() + "/" + randomBCCP.getPropertyTerm());
        assertTrue(accViewEditPage.isDeleted(bccNode));
        ACCViewEditPage.BCCPanelContainer bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
        assertEquals("Deleted", getText(bccPanelContainer.getBCCPPanel().getStateField()));
    }

    @Test
    public void test_TA_10_23_3() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        String branch = "Working";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        ACCObject randomACC = coreComponentAPI.createRandomACC(developer, release, namespace, "WIP");

        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, anotherDeveloper, namespace, "WIP");
        coreComponentAPI.appendBCC(randomACC, randomBCCP, "WIP");

        HomePage homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        bccpViewEditPage.hitDeleteButton();
        assertTrue(viewEditCoreComponentPage.isOpened());
        homePage.logout();

        homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(randomACC.getAccManifestId());
        String bccPath = "/" + randomACC.getDen() + "/" + randomBCCP.getPropertyTerm();
        WebElement bccNode = accViewEditPage.clickOnDropDownMenuByPath(bccPath);
        assertTrue(accViewEditPage.isDeleted(bccNode));
        bccpViewEditPage = accViewEditPage.openBCCPInNewTab(bccNode);
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertEquals("Deleted", getText(bccpPanel.getStateField()));
        assertEquals(anotherDeveloper.getLoginId(), getText(bccpPanel.getOwnerField()));
        bccpViewEditPage.hitRestoreButton();

        switchToMainTab(getDriver());
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(randomACC.getAccManifestId());
        bccNode = accViewEditPage.clickOnDropDownMenuByPath(bccPath);
        assertFalse(accViewEditPage.isDeleted(bccNode));
        bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertEquals("WIP", getText(bccpPanel.getStateField()));
        assertEquals(developer.getLoginId(), getText(bccpPanel.getOwnerField()));
    }

    @Test
    public void test_TA_10_23_4() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        String branch = "Working";
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        bccpViewEditPage.hitReviseButton();

        // reload the page
        bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertEquals("2", getText(bccpPanel.getRevisionField()));
        BCCPViewEditPage finalBccpViewEditPage = bccpViewEditPage;
        assertThrows(TimeoutException.class, () -> finalBccpViewEditPage.getDeleteButton());
    }

}
