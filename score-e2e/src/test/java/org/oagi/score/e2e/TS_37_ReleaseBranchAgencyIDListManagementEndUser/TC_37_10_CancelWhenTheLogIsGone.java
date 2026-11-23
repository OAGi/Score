package org.oagi.score.e2e.TS_37_ReleaseBranchAgencyIDListManagementEndUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.LogNotFoundDialog;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.agency_id_list.EditAgencyIDListPage;
import org.oagi.score.e2e.page.agency_id_list.ViewEditAgencyIDListPage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.impl.PageHelper.getSnackBarMessage;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.invisibilityOfLoadingContainerElement;

/**
 * The history (log) that a cancel goes back to is gone from the database: the end user is asked whether a new history may be
 * written. If so, the cancel leaves revision 1; if not, nothing is changed.
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_37_10_CancelWhenTheLogIsGone extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    @Test
    @DisplayName("TC_37_10_TA_1")
    public void cancel_asks_whether_a_new_log_may_be_written_when_the_log_to_go_back_to_is_gone() {
        AppUserObject user = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        randomAccounts.add(user);
        AppUserObject owner = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        randomAccounts.add(owner);
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(owner, library);
        AgencyIDListObject agencyIDList = getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(owner, namespace, release, "Production");

        HomePage homePage = loginPage().signIn(user.getLoginId(), user.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(agencyIDList.getName(), release.getReleaseNumber());
        editAgencyIDListPage.amend();

        getAPIFactory().getCoreComponentAPI().deleteLogsBeforeTheLatestRevision(agencyIDList.getGuid());

        editAgencyIDListPage.openPage();
        editAgencyIDListPage.getCancelButton().click();
        LogNotFoundDialog.confirm(getDriver(), "Okay");
        LogNotFoundDialog.awaitOpened(getDriver());
        // the user does not let a new log be written: nothing is changed
        LogNotFoundDialog.decline(getDriver());
        editAgencyIDListPage.openPage();
        assertEquals("WIP", getText(editAgencyIDListPage.getStateField()));

        // the user lets it be written
        editAgencyIDListPage.getCancelButton().click();
        LogNotFoundDialog.confirm(getDriver(), "Okay");
        LogNotFoundDialog.awaitOpened(getDriver());
        LogNotFoundDialog.createAndContinue(getDriver());
        invisibilityOfLoadingContainerElement(getDriver());
        assertEquals("Canceled", getSnackBarMessage(getDriver()));

        editAgencyIDListPage.openPage();
        assertEquals("Production", getText(editAgencyIDListPage.getStateField()));
        assertEquals("1", getText(editAgencyIDListPage.getRevisionField()));
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        randomAccounts.forEach(user -> getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(user.getLoginId()));
    }
}
