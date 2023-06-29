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
import org.oagi.score.e2e.page.agency_id_list.EditAgencyIDListValueDialog;
import org.oagi.score.e2e.page.agency_id_list.ViewEditAgencyIDListPage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.impl.PageHelper.escape;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_37_7_RestoringEndUserAgencyIDList extends BaseTest {

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
    @DisplayName("TC_37_7_1")
    public void TA_1() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

        AgencyIDListObject agencyIDList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(endUser, namespace, release, "Deleted");
        List<AgencyIDListValueObject> agencyIDListValues = Arrays.asList(
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(endUser, agencyIDList),
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(endUser, agencyIDList),
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(endUser, agencyIDList));

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(agencyIDList.getName(), release.getReleaseNumber());
        editAgencyIDListPage.restore();

        assertEquals("Agency ID List", getText(editAgencyIDListPage.getCoreComponentField()));
        assertEquals(Integer.toString(1), getText(editAgencyIDListPage.getRevisionField()));
        assertEquals(release.getReleaseNumber(), getText(editAgencyIDListPage.getReleaseField()));
        assertEquals("WIP", getText(editAgencyIDListPage.getStateField()));
        assertEquals(endUser.getLoginId(), getText(editAgencyIDListPage.getOwnerField()));
        assertEquals(agencyIDList.getName(), getText(editAgencyIDListPage.getAgencyIDListNameField()));
        assertEquals(agencyIDList.getListId(), getText(editAgencyIDListPage.getListIDField()));
        assertEquals(agencyIDList.getVersionId(), getText(editAgencyIDListPage.getVersionField()));
        assertEquals(agencyIDList.getDefinition(), getText(editAgencyIDListPage.getDefinitionField()));
        assertEquals(agencyIDList.getDefinitionSource(), getText(editAgencyIDListPage.getDefinitionSourceField()));

        for (AgencyIDListValueObject agencyIDListValue : agencyIDListValues) {
            EditAgencyIDListValueDialog editAgencyIDListValueDialog =
                    editAgencyIDListPage.openAgencyIDListValueDialogByValue(agencyIDListValue.getValue());

            assertEquals(agencyIDListValue.getValue(), getText(editAgencyIDListValueDialog.getValueField()));
            assertEquals(agencyIDListValue.getName(), getText(editAgencyIDListValueDialog.getMeaningField()));
            assertEquals(agencyIDListValue.getDefinition(), getText(editAgencyIDListValueDialog.getDefinitionField()));
            assertEquals(agencyIDListValue.getDefinitionSource(), getText(editAgencyIDListValueDialog.getDefinitionSourceField()));
            escape(getDriver());
        }
    }

    @Test
    @DisplayName("TC_37_7_2")
    public void TA_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherEndUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherEndUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherEndUser);

        AgencyIDListObject agencyIDList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(anotherEndUser, namespace, release, "Deleted");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(agencyIDList.getName(), release.getReleaseNumber());
        editAgencyIDListPage.restore();

        assertEquals("WIP", getText(editAgencyIDListPage.getStateField()));
        assertEquals(endUser.getLoginId(), getText(editAgencyIDListPage.getOwnerField()));
    }

}
