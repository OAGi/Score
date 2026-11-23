package org.oagi.score.e2e.TS_10_WorkingBranchCoreComponentManagementBehaviorsForDeveloper.cancel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.LogNotFoundDialog;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.ACCViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.impl.PageHelper.getSnackBarMessage;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.invisibilityOfLoadingContainerElement;

/**
 * The history (log) that a cancel goes back to is gone from the database. The user is asked whether a new history may be
 * written. If so, the cancel leaves revision 1, and a revision after it is revision 2. If not, nothing is changed.
 * (A component that has no log at all cannot be opened, so a revision without a log is covered by the backend tests.)
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_10_26_CancelWhenTheLogIsGone extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_10_26_TA_1")
    public void cancel_asks_whether_a_new_log_may_be_written_when_the_log_to_go_back_to_is_gone() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        CoreComponentAPI api = getAPIFactory().getCoreComponentAPI();
        ACCObject acc = api.createRandomACC(developer, release, namespace, "Published");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();

        // somebody removed the history the component was in before the revision from the database
        api.deleteLogsBeforeTheLatestRevision(acc.getGuid());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.getCancelButton().click();
        LogNotFoundDialog.confirm(getDriver(), "Okay");
        LogNotFoundDialog.awaitOpened(getDriver());

        // the user does not let a new log be written: nothing is changed
        LogNotFoundDialog.decline(getDriver());
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        ACCViewEditPage.ACCPanel panel = accViewEditPage.getACCPanel(accViewEditPage.getNodeByPath("/" + acc.getDen()));
        assertEquals("WIP", getText(panel.getStateField()));

        // the user lets it be written
        accViewEditPage.getCancelButton().click();
        LogNotFoundDialog.confirm(getDriver(), "Okay");
        LogNotFoundDialog.awaitOpened(getDriver());
        LogNotFoundDialog.createAndContinue(getDriver());
        invisibilityOfLoadingContainerElement(getDriver());
        assertEquals("Canceled", getSnackBarMessage(getDriver()));

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        panel = accViewEditPage.getACCPanel(accViewEditPage.getNodeByPath("/" + acc.getDen()));
        assertEquals("Published", getText(panel.getStateField()));
        assertEquals("1", getText(panel.getRevisionField()));

        // and it can be revised again, as revision 2
        accViewEditPage.hitReviseButton();
        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        panel = accViewEditPage.getACCPanel(accViewEditPage.getNodeByPath("/" + acc.getDen()));
        assertEquals("2", getText(panel.getRevisionField()));
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        this.randomAccounts.forEach(newUser ->
                getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId()));
    }
}
