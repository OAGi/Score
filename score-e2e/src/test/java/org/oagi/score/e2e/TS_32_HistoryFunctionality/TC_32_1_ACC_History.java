package org.oagi.score.e2e.TS_32_HistoryFunctionality;

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
import org.oagi.score.e2e.page.core_component.*;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

@Execution(ExecutionMode.CONCURRENT)
public class TC_32_1_ACC_History extends BaseTest {

    private List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        // Delete random accounts
        this.randomAccounts.forEach(newUser -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId());
        });
    }

    @Test
    @DisplayName("TC_32_issue_1043")
    public void TA_issue_1043() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        // make history records
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.createACC(release.getReleaseNumber());
        WebElement rootNode = accViewEditPage.getNodeByPath("/Object Class Term. Details");
        ACCViewEditPage.ACCPanel accPanel = accViewEditPage.getACCPanel(rootNode);
        ACCObject randomACCObject = ACCObject.createRandomACC(developer, namespace, "WIP");
        accPanel.setObjectClassTerm(randomACCObject.getObjectClassTerm());
        accPanel.setNamespace(namespace);
        accPanel.setDefinition(randomACCObject.getDefinition());
        accPanel.setDefinitionSource(randomACCObject.getDefinitionSource());
        accViewEditPage.hitUpdateButton();
        for (int i = 0; i < 4; ++i) {
            accViewEditPage.moveToDraft();
            accViewEditPage.moveToCandidate();
            accViewEditPage.backToWIP();
        }

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(randomACCObject.getDen(), release.getReleaseNumber());

        HistoryPage historyPage = accViewEditPage.showHistory();
        historyPage.checkRecordAtIndex(1);

        historyPage.goToNextPage();
        historyPage.checkRecordAtIndex(1);

        HistoryCompareDialog historyCompareDialog = historyPage.compare();
        assertEquals(developer.getLoginId(), getText(historyCompareDialog.getLeftHistoryRecordPanel().getHistoryItemPanel(0).getOwnerField()));
    }

    @Test
    @DisplayName("TC_32_issue_898 (for ACC)")
    public void TA_issue_898_for_ACC() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, release, namespace, "Published");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();
        waitFor(ofSeconds(3L)); // wait until the previous snack-bar disappears.
        accViewEditPage.hitCancelButton();

        HistoryPage historyPage = accViewEditPage.showHistory();
        WebElement tr = historyPage.getTableRecordAtIndex(1);
        WebElement td = historyPage.getColumnByName(tr, "revisionNum");
        assertEquals("1", getText(td));

        td = historyPage.getColumnByName(tr, "revisionAction");
        assertNotEquals("Revised", getText(td));
    }

    @Test
    @DisplayName("TC_32_issue_898 (for ASCCP)")
    public void TA_issue_898_for_ASCCP() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        ACCObject acc;
        ASCCPObject asccp;
        {
            CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

            acc = coreComponentAPI.createRandomACC(developer, release, namespace, "Published");
            asccp = coreComponentAPI.createRandomASCCP(acc, developer, namespace, "Published");
        }

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        asccpViewEditPage.hitReviseButton();
        waitFor(ofSeconds(3L)); // wait until the previous snack-bar disappears.
        asccpViewEditPage.hitCancelButton();

        HistoryPage historyPage = asccpViewEditPage.showHistory();
        WebElement tr = historyPage.getTableRecordAtIndex(1);
        WebElement td = historyPage.getColumnByName(tr, "revisionNum");
        assertEquals("1", getText(td));

        td = historyPage.getColumnByName(tr, "revisionAction");
        assertNotEquals("Revised", getText(td));
    }

    @Test
    @DisplayName("TC_32_issue_898 (for BCCP)")
    public void TA_issue_898_for_BCCP() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();

        // Indicator. Type
        DTObject dataType = coreComponentAPI.getBDTByGuidAndReleaseNum("ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject bccp = coreComponentAPI.createRandomBCCP(dataType, developer, namespace, "Published");
        bccp.setNillable(false);
        coreComponentAPI.updateBCCP(bccp);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage =
                homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());
        bccpViewEditPage.hitReviseButton();
        waitFor(ofSeconds(3L)); // wait until the previous snack-bar disappears.
        bccpViewEditPage.hitCancelButton();

        HistoryPage historyPage = bccpViewEditPage.showHistory();
        WebElement tr = historyPage.getTableRecordAtIndex(1);
        WebElement td = historyPage.getColumnByName(tr, "revisionNum");
        assertEquals("1", getText(td));

        td = historyPage.getColumnByName(tr, "revisionAction");
        assertNotEquals("Revised", getText(td));
    }

}
