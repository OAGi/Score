package org.oagi.score.e2e.TS_38_WorkingBranchDataTypeManagementForDeveloper;

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
import org.oagi.score.e2e.page.core_component.DTViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditDataTypePage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.impl.PageHelper.getSnackBarMessage;
import static org.oagi.score.e2e.impl.PageHelper.invisibilityOfLoadingContainerElement;

/**
 * The history (log) that a cancel goes back to is gone from the database: the developer is asked whether a new history
 * may be written. If so, the cancel leaves revision 1; if not, nothing is changed.
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_38_17_CancelWhenTheLogIsGone extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    @Test
    @DisplayName("TC_38_17_TA_1")
    public void cancel_asks_whether_a_new_log_may_be_written_when_the_log_to_go_back_to_is_gone() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        randomAccounts.add(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
        ReleaseObject cdtRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");
        DTObject baseCDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", cdtRelease.getReleaseNumber());
        library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        DTObject dt = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseCDT, developer, namespace, "Published");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
        dtViewEditPage.hitReviseButton();

        getAPIFactory().getCoreComponentAPI().deleteLogsBeforeTheLatestRevision(dt.getGuid());

        viewEditDataTypePage.openPage();
        dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
        dtViewEditPage.getCancelButton().click();
        LogNotFoundDialog.confirm(getDriver(), "Okay");
        LogNotFoundDialog.awaitOpened(getDriver());
        LogNotFoundDialog.decline(getDriver());

        viewEditDataTypePage.openPage();
        dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
        assertEquals("WIP", dtViewEditPage.getStateFieldValue());

        dtViewEditPage.getCancelButton().click();
        LogNotFoundDialog.confirm(getDriver(), "Okay");
        LogNotFoundDialog.awaitOpened(getDriver());
        LogNotFoundDialog.createAndContinue(getDriver());
        invisibilityOfLoadingContainerElement(getDriver());
        assertEquals("Canceled", getSnackBarMessage(getDriver()));

        viewEditDataTypePage.openPage();
        dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
        assertEquals("Published", dtViewEditPage.getStateFieldValue());
        assertEquals("1", dtViewEditPage.getRevisionFieldValue());
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        this.randomAccounts.forEach(newUser ->
                getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId()));
    }
}
