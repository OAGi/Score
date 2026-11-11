package org.oagi.score.e2e.TS_11_WorkingBranchCodeListManagementForDeveloper;

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
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.impl.PageHelper.getSnackBarMessage;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.invisibilityOfLoadingContainerElement;

/**
 * The history (log) that a cancel goes back to is gone from the database: the developer is asked whether a new history may be
 * written. If so, the cancel leaves revision 1; if not, nothing is changed.
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_11_10_CancelWhenTheLogIsGone extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    @Test
    @DisplayName("TC_11_10_TA_1")
    public void cancel_asks_whether_a_new_log_may_be_written_when_the_log_to_go_back_to_is_gone() {
        AppUserObject user = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        randomAccounts.add(user);
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(user, namespace, branch, "Published");
        getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, user);

        HomePage homePage = loginPage().signIn(user.getLoginId(), user.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        editCodeListPage.hitRevise();

        getAPIFactory().getCoreComponentAPI().deleteLogsBeforeTheLatestRevision(codeList.getGuid());

        editCodeListPage.getCancelButton().click();
        LogNotFoundDialog.confirm(getDriver(), "Okay");
        LogNotFoundDialog.awaitOpened(getDriver());
        // the user does not let a new log be written: nothing is changed
        LogNotFoundDialog.decline(getDriver());
        getDriver().navigate().refresh();
        assertEquals("WIP", getText(editCodeListPage.getStateField()));

        // the user lets it be written
        editCodeListPage.getCancelButton().click();
        LogNotFoundDialog.confirm(getDriver(), "Okay");
        LogNotFoundDialog.awaitOpened(getDriver());
        LogNotFoundDialog.createAndContinue(getDriver());
        invisibilityOfLoadingContainerElement(getDriver());
        assertEquals("Canceled", getSnackBarMessage(getDriver()));

        getDriver().navigate().refresh();
        org.oagi.score.e2e.impl.PageHelper.waitFor(java.time.Duration.ofSeconds(3));
        assertEquals("Published", getText(editCodeListPage.getStateField()));
        assertEquals("1", getText(editCodeListPage.getRevisionField()));
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        randomAccounts.forEach(user -> getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(user.getLoginId()));
    }
}
