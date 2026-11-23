package org.oagi.score.e2e.TS_37_ReleaseBranchAgencyIDListManagementEndUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.agency_id_list.EditAgencyIDListPage;
import org.oagi.score.e2e.page.agency_id_list.ViewEditAgencyIDListPage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.getText;

/**
 * The end user cancels the amendment of an Agency ID List after changing its version: the list is as it was. The Agency ID
 * Value of the list itself cannot be changed in a revision, and it is what it was.
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_37_9_AgencyIDListValueIsFixedInAnAmendmentAndCancelBringsBackTheRest extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    @Test
    @DisplayName("TC_37_9_TA_1")
    public void the_agency_id_list_value_is_fixed_in_an_amendment_and_cancelling_brings_back_the_version() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        randomAccounts.add(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        // the Agency ID List is another end user's
        AppUserObject anotherEndUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        randomAccounts.add(anotherEndUser);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherEndUser, library);
        AgencyIDListObject agencyIDList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(anotherEndUser, namespace, release, "Production");
        getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherEndUser, agencyIDList);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(agencyIDList.getName(), release.getReleaseNumber());
        String agencyIDListValue = getText(editAgencyIDListPage.getAgencyIDListValueSelectField());

        editAgencyIDListPage.amend();
        // the Agency ID Value of the list cannot be changed in a revision
        assertDisabled(editAgencyIDListPage.getAgencyIDListValueSelectField());
        editAgencyIDListPage.setVersion("new version");
        editAgencyIDListPage.hitUpdateButton();
        editAgencyIDListPage.openPage();
        assertEquals(agencyIDListValue, getText(editAgencyIDListPage.getAgencyIDListValueSelectField()));

        editAgencyIDListPage.cancel();
        editAgencyIDListPage.openPage();

        assertEquals("Production", getText(editAgencyIDListPage.getStateField()));
        assertEquals("1", getText(editAgencyIDListPage.getRevisionField()));
        assertEquals(agencyIDListValue, getText(editAgencyIDListPage.getAgencyIDListValueSelectField()),
                "the Agency ID List Value of the list is as it was");
        assertEquals(agencyIDList.getVersionId(), getText(editAgencyIDListPage.getVersionField()));
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        randomAccounts.forEach(user -> getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(user.getLoginId()));
    }
}
