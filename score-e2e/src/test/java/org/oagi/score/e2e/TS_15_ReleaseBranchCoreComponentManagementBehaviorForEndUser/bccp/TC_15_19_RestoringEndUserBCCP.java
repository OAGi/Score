package org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.bccp;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.BCCPViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

@Execution(ExecutionMode.CONCURRENT)
public class TC_15_19_RestoringEndUserBCCP extends BaseTest {

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
    public void test_TA_15_19_1_owned_by_myself() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        String branch = "10.8.7.1";
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

        // Code. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum(library, "ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(release, dataType, endUser, namespace, "Deleted");

        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        bccpViewEditPage.hitRestoreButton();

        // reload the page
        bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertEquals("WIP", getText(bccpPanel.getStateField()));
    }

    @Test
    public void test_TA_15_19_1_owned_by_another_user() {
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
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(release, dataType, anotherUser, namespace, "Deleted");

        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertEquals(anotherUser.getLoginId(), getText(bccpPanel.getOwnerField()));
        bccpViewEditPage.hitRestoreButton();

        // reload the page
        bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertEquals("WIP", getText(bccpPanel.getStateField()));
    }

    @Test
    public void test_TA_15_19_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
        ReleaseObject branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

        DTObject cdt = coreComponentAPI.getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());

        library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.7.1");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

        DTObject randomBDT = coreComponentAPI.createRandomBDT(branch, cdt, endUser, namespace, "Deleted");
        BCCPObject randomBCCP = coreComponentAPI.createRandomBCCP(branch, randomBDT, endUser, namespace, "WIP");
        viewEditCoreComponentPage.setBranch(branch.getReleaseNumber());
        waitFor(Duration.ofMillis(1500));
        BCCPViewEditPage bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        BCCPViewEditPage.DTPanel dtPanel = bccpViewEditPage.getBCCPPanelContainer().getDTPanel();
        assertEquals("Deleted", getText(dtPanel.getStateField()));
        bccpViewEditPage.hitDeleteButton();

        // reload the page
        bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        BCCPViewEditPage.BCCPPanel bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertEquals("Deleted", getText(bccpPanel.getStateField()));
        bccpViewEditPage.hitRestoreButton();

        // reload the page
        bccpViewEditPage =
                viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(randomBCCP.getBccpManifestId());
        bccpPanel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertEquals("WIP", getText(bccpPanel.getStateField()));
    }
}
