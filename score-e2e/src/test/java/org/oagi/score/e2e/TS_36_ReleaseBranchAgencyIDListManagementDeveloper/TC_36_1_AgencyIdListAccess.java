package org.oagi.score.e2e.TS_36_ReleaseBranchAgencyIDListManagementDeveloper;

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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.AssertionHelper.assertEnabled;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_36_1_AgencyIdListAccess extends BaseTest {

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
    @DisplayName("TC_36_1_1")
    public void TA_1() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        NamespaceObject endUserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
        ReleaseObject latestRelease = getAPIFactory().getReleaseAPI().getTheLatestRelease();

        AgencyIDListObject wipAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(endUser, endUserNamespace, latestRelease, "WIP");
        AgencyIDListObject qaAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(endUser, endUserNamespace, latestRelease, "QA");
        AgencyIDListObject productionAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(endUser, endUserNamespace, latestRelease, "Production");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        viewEditAgencyIDListPage.setBranch(latestRelease.getReleaseNumber());
        viewEditAgencyIDListPage.setOwner("oagis");
        viewEditAgencyIDListPage.hitSearchButton();

        assertEquals(1, viewEditAgencyIDListPage.getTotalNumberOfItems());
        WebElement tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
        WebElement td_state = viewEditAgencyIDListPage.getColumnByName(tr, "state");
        assertEquals("Published", getText(td_state));

        viewEditAgencyIDListPage.openPage();
        viewEditAgencyIDListPage.setBranch(latestRelease.getReleaseNumber());
        viewEditAgencyIDListPage.setOwner(endUser.getLoginId());
        viewEditAgencyIDListPage.setName(wipAgencyIdList.getName());
        viewEditAgencyIDListPage.hitSearchButton();
        tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
        assertEquals("WIP", getText(viewEditAgencyIDListPage.getColumnByName(tr, "state")));

        viewEditAgencyIDListPage.setName(qaAgencyIdList.getName());
        viewEditAgencyIDListPage.hitSearchButton();
        tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
        assertEquals("QA", getText(viewEditAgencyIDListPage.getColumnByName(tr, "state")));

        viewEditAgencyIDListPage.setName(productionAgencyIdList.getName());
        viewEditAgencyIDListPage.hitSearchButton();
        tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
        assertEquals("Production", getText(viewEditAgencyIDListPage.getColumnByName(tr, "state")));
    }

    @Test
    @DisplayName("TC_36_1_2")
    public void TA_2() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject latestRelease = getAPIFactory().getReleaseAPI().getTheLatestRelease();

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        viewEditAgencyIDListPage.setBranch(latestRelease.getReleaseNumber());
        viewEditAgencyIDListPage.setOwner("oagis");
        viewEditAgencyIDListPage.hitSearchButton();

        WebElement tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
        WebElement td_name = viewEditAgencyIDListPage.getColumnByName(tr, "name");
        String name = getText(td_name.findElement(By.cssSelector("a > span")));
        AgencyIDListObject agencyIdList = getAPIFactory().getAgencyIDListAPI().getAgencyIDListByNameAndBranch(name, latestRelease.getReleaseNumber());

        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(agencyIdList.getName(), latestRelease.getReleaseNumber());

        assertEquals("Agency ID List", getText(editAgencyIDListPage.getCoreComponentField()));
        assertEquals(agencyIdList.getGuid(), getText(editAgencyIDListPage.getGUIDField()));
        assertEquals(latestRelease.getReleaseNumber(), getText(editAgencyIDListPage.getReleaseField()));
        assertEquals(agencyIdList.getState(), getText(editAgencyIDListPage.getStateField()));
        assertEquals("oagis", getText(editAgencyIDListPage.getOwnerField()));
        assertEquals(agencyIdList.getName(), getText(editAgencyIDListPage.getAgencyIDListNameField()));
        assertEquals(agencyIdList.getListId(), getText(editAgencyIDListPage.getListIDField()));
        assertEquals(agencyIdList.getVersionId(), getText(editAgencyIDListPage.getVersionField()));
        assertEquals(agencyIdList.getDefinition(), getText(editAgencyIDListPage.getDefinitionField()));
        assertEquals(agencyIdList.getDefinitionSource(), getText(editAgencyIDListPage.getDefinitionSourceField()));

        assertDisabled(editAgencyIDListPage.getAgencyIDListNameField());
        assertDisabled(editAgencyIDListPage.getListIDField());
        assertDisabled(editAgencyIDListPage.getVersionField());
        assertDisabled(editAgencyIDListPage.getDefinitionField());
        assertDisabled(editAgencyIDListPage.getDefinitionSourceField());
        assertEnabled(editAgencyIDListPage.getCommentButton());

        List<AgencyIDListValueObject> agencyIDListValues =
                getAPIFactory().getAgencyIDListValueAPI().getAgencyIDListValueByAgencyListID(agencyIdList);
        AgencyIDListValueObject agencyIDListValue = agencyIDListValues.get(0);

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
    @DisplayName("TC_36_1_3")
    public void TA_3() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        NamespaceObject endUserNamespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);
        ReleaseObject latestRelease = getAPIFactory().getReleaseAPI().getTheLatestRelease();

        AgencyIDListObject wipAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(endUser, endUserNamespace, latestRelease, "WIP");
        AgencyIDListObject qaAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(endUser, endUserNamespace, latestRelease, "QA");
        AgencyIDListObject productionAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(endUser, endUserNamespace, latestRelease, "Production");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        for (AgencyIDListObject agencyIdList : Arrays.asList(wipAgencyIdList, qaAgencyIdList, productionAgencyIdList)) {
            ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
            viewEditAgencyIDListPage.setOwner(endUser.getLoginId());
            EditAgencyIDListPage editAgencyIDListPage =
                    viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(agencyIdList.getName(), latestRelease.getReleaseNumber());

            assertEquals("Agency ID List", getText(editAgencyIDListPage.getCoreComponentField()));
            assertEquals(agencyIdList.getGuid(), getText(editAgencyIDListPage.getGUIDField()));
            assertEquals(latestRelease.getReleaseNumber(), getText(editAgencyIDListPage.getReleaseField()));
            assertEquals(agencyIdList.getState(), getText(editAgencyIDListPage.getStateField()));
            assertEquals(endUser.getLoginId(), getText(editAgencyIDListPage.getOwnerField()));
            assertEquals(agencyIdList.getName(), getText(editAgencyIDListPage.getAgencyIDListNameField()));
            assertEquals(agencyIdList.getListId(), getText(editAgencyIDListPage.getListIDField()));
            assertEquals(agencyIdList.getVersionId(), getText(editAgencyIDListPage.getVersionField()));
            assertEquals(agencyIdList.getDefinition(), getText(editAgencyIDListPage.getDefinitionField()));
            assertEquals(agencyIdList.getDefinitionSource(), getText(editAgencyIDListPage.getDefinitionSourceField()));

            assertDisabled(editAgencyIDListPage.getAgencyIDListNameField());
            assertDisabled(editAgencyIDListPage.getListIDField());
            assertDisabled(editAgencyIDListPage.getVersionField());
            assertDisabled(editAgencyIDListPage.getDefinitionField());
            assertDisabled(editAgencyIDListPage.getDefinitionSourceField());
            assertEnabled(editAgencyIDListPage.getCommentButton());
        }
    }

}
