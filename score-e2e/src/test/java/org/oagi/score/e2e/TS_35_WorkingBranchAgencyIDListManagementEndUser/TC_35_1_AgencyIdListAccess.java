package org.oagi.score.e2e.TS_35_WorkingBranchAgencyIDListManagementEndUser;

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
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.AssertionHelper.assertEnabled;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_35_1_AgencyIdListAccess extends BaseTest {

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
    @DisplayName("TC_35_1_1")
    public void TA_1() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        AgencyIDListObject wipAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "WIP");
        AgencyIDListObject draftAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "Draft");
        AgencyIDListObject candidateAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "Candidate");
        AgencyIDListObject publishedAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "Published");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        viewEditAgencyIDListPage.setBranch(release.getReleaseNumber());
        viewEditAgencyIDListPage.setName(wipAgencyIdList.getName());
        viewEditAgencyIDListPage.hitSearchButton();
        WebElement tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
        assertEquals("WIP", getText(viewEditAgencyIDListPage.getColumnByName(tr, "state")));

        viewEditAgencyIDListPage.setName(draftAgencyIdList.getName());
        viewEditAgencyIDListPage.setBranch(release.getReleaseNumber());
        viewEditAgencyIDListPage.hitSearchButton();
        tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
        assertEquals("Draft", getText(viewEditAgencyIDListPage.getColumnByName(tr, "state")));

        viewEditAgencyIDListPage.setName(candidateAgencyIdList.getName());
        viewEditAgencyIDListPage.setBranch(release.getReleaseNumber());
        viewEditAgencyIDListPage.hitSearchButton();
        tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
        assertEquals("Candidate", getText(viewEditAgencyIDListPage.getColumnByName(tr, "state")));

        viewEditAgencyIDListPage.setName(publishedAgencyIdList.getName());
        viewEditAgencyIDListPage.setBranch(release.getReleaseNumber());
        viewEditAgencyIDListPage.hitSearchButton();
        tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
        assertEquals("Published", getText(viewEditAgencyIDListPage.getColumnByName(tr, "state")));
    }

    @Test
    @DisplayName("TC_35_1_2")
    public void TA_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        AgencyIDListObject wipAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "WIP");
        AgencyIDListValueObject agencyIDListValue =
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(developer, wipAgencyIdList);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(wipAgencyIdList.getName(), release.getReleaseNumber());

        assertEquals("Agency ID List", getText(editAgencyIDListPage.getCoreComponentField()));
        assertEquals(wipAgencyIdList.getGuid(), getText(editAgencyIDListPage.getGUIDField()));
        assertEquals(release.getReleaseNumber(), getText(editAgencyIDListPage.getReleaseField()));
        assertEquals(wipAgencyIdList.getState(), getText(editAgencyIDListPage.getStateField()));
        assertEquals(developer.getLoginId(), getText(editAgencyIDListPage.getOwnerField()));
        assertEquals(wipAgencyIdList.getName(), getText(editAgencyIDListPage.getAgencyIDListNameField()));
        assertEquals(wipAgencyIdList.getListId(), getText(editAgencyIDListPage.getListIDField()));
        assertEquals(wipAgencyIdList.getVersionId(), getText(editAgencyIDListPage.getVersionField()));
        assertEquals(wipAgencyIdList.getDefinition(), getText(editAgencyIDListPage.getDefinitionField()));
        assertEquals(wipAgencyIdList.getDefinitionSource(), getText(editAgencyIDListPage.getDefinitionSourceField()));

        assertDisabled(editAgencyIDListPage.getAgencyIDListNameField());
        assertDisabled(editAgencyIDListPage.getListIDField());
        assertDisabled(editAgencyIDListPage.getVersionField());
        assertDisabled(editAgencyIDListPage.getDefinitionField());
        assertDisabled(editAgencyIDListPage.getDefinitionSourceField());
        assertEnabled(editAgencyIDListPage.getCommentButton());

        EditAgencyIDListValueDialog editAgencyIDListValueDialog =
                editAgencyIDListPage.openAgencyIDListValueDialogByValue(agencyIDListValue.getValue());

        assertEquals(agencyIDListValue.getValue(), getText(editAgencyIDListValueDialog.getValueField()));
        assertEquals(agencyIDListValue.getName(), getText(editAgencyIDListValueDialog.getMeaningField()));
        assertEquals(agencyIDListValue.getDefinition(), getText(editAgencyIDListValueDialog.getDefinitionField()));
        assertEquals(agencyIDListValue.getDefinitionSource(), getText(editAgencyIDListValueDialog.getDefinitionSourceField()));

        assertDisabled(editAgencyIDListValueDialog.getValueField());
        assertDisabled(editAgencyIDListValueDialog.getMeaningField());
        assertDisabled(editAgencyIDListValueDialog.getDefinitionField());
        assertDisabled(editAgencyIDListValueDialog.getDefinitionSourceField());
    }

    @Test
    @DisplayName("TC_35_1_3")
    public void TA_3() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        viewEditAgencyIDListPage.setOwner(endUser.getLoginId());
        viewEditAgencyIDListPage.setBranch("Working");
        viewEditAgencyIDListPage.hitSearchButton();
        assertEquals(0, viewEditAgencyIDListPage.getTotalNumberOfItems());
    }

    @Test
    @DisplayName("TC_35_1_4")
    public void TA_4() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        viewEditAgencyIDListPage.setBranch(release.getReleaseNumber());
        viewEditAgencyIDListPage.hitSearchButton();

        assertThrows(TimeoutException.class, () -> viewEditAgencyIDListPage.openNewAgencyIDList(endUser, release.getReleaseNumber()));
    }

}
