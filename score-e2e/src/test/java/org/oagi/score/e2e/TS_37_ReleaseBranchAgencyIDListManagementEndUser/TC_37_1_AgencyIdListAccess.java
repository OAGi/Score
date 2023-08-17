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
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.AssertionHelper.assertEnabled;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_37_1_AgencyIdListAccess extends BaseTest {

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
    @DisplayName("TC_37_1_1")
    public void TA_1() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

        AgencyIDListObject wipAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(endUser, namespace, release, "WIP");
        AgencyIDListObject qaAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(endUser, namespace, release, "QA");
        AgencyIDListObject productionAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(endUser, namespace, release, "Production");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        viewEditAgencyIDListPage.setBranch(release.getReleaseNumber());
        viewEditAgencyIDListPage.setName(wipAgencyIdList.getName());
        viewEditAgencyIDListPage.hitSearchButton();
        WebElement tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
        assertEquals("WIP", getText(viewEditAgencyIDListPage.getColumnByName(tr, "state")));

        viewEditAgencyIDListPage.openPage();
        viewEditAgencyIDListPage.setBranch(release.getReleaseNumber());
        viewEditAgencyIDListPage.setName(qaAgencyIdList.getName());
        viewEditAgencyIDListPage.hitSearchButton();
        tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
        assertEquals("QA", getText(viewEditAgencyIDListPage.getColumnByName(tr, "state")));

        viewEditAgencyIDListPage.openPage();
        viewEditAgencyIDListPage.setBranch(release.getReleaseNumber());
        viewEditAgencyIDListPage.setName(productionAgencyIdList.getName());
        viewEditAgencyIDListPage.hitSearchButton();
        tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
        assertEquals("Production", getText(viewEditAgencyIDListPage.getColumnByName(tr, "state")));

        viewEditAgencyIDListPage.openPage();
        viewEditAgencyIDListPage.setBranch(release.getReleaseNumber());
        viewEditAgencyIDListPage.setName("clm63055D16B_AgencyIdentification");
        viewEditAgencyIDListPage.hitSearchButton();
        tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
        assertEquals("Published", getText(viewEditAgencyIDListPage.getColumnByName(tr, "state")));
    }

    @Test
    @DisplayName("TC_37_1_2")
    public void TA_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

        AgencyIDListObject wipAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(endUser, namespace, release, "WIP");
        AgencyIDListValueObject agencyIDListValue =
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(endUser, wipAgencyIdList);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(wipAgencyIdList.getName(), release.getReleaseNumber());

        assertEquals("Agency ID List", getText(editAgencyIDListPage.getCoreComponentField()));
        assertEquals(wipAgencyIdList.getGuid(), getText(editAgencyIDListPage.getGUIDField()));
        assertEquals(release.getReleaseNumber(), getText(editAgencyIDListPage.getReleaseField()));
        assertEquals(wipAgencyIdList.getState(), getText(editAgencyIDListPage.getStateField()));
        assertEquals(endUser.getLoginId(), getText(editAgencyIDListPage.getOwnerField()));
        assertEquals(wipAgencyIdList.getName(), getText(editAgencyIDListPage.getAgencyIDListNameField()));
        assertEquals(wipAgencyIdList.getListId(), getText(editAgencyIDListPage.getListIDField()));
        assertEquals(wipAgencyIdList.getVersionId(), getText(editAgencyIDListPage.getVersionField()));
        assertEquals(wipAgencyIdList.getDefinition(), getText(editAgencyIDListPage.getDefinitionField()));
        assertEquals(wipAgencyIdList.getDefinitionSource(), getText(editAgencyIDListPage.getDefinitionSourceField()));

        EditAgencyIDListValueDialog editAgencyIDListValueDialog =
                editAgencyIDListPage.openAgencyIDListValueDialogByValue(agencyIDListValue.getValue());

        assertEquals(agencyIDListValue.getValue(), getText(editAgencyIDListValueDialog.getValueField()));
        assertEquals(agencyIDListValue.getName(), getText(editAgencyIDListValueDialog.getMeaningField()));
        assertEquals(agencyIDListValue.getDefinition(), getText(editAgencyIDListValueDialog.getDefinitionField()));
        assertEquals(agencyIDListValue.getDefinitionSource(), getText(editAgencyIDListValueDialog.getDefinitionSourceField()));
    }

    @Test
    @DisplayName("TC_37_1_3")
    public void TA_3() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherEndUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherEndUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherEndUser);

        AgencyIDListObject wipAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(anotherEndUser, namespace, release, "WIP");
        AgencyIDListValueObject agencyIDListValue =
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherEndUser, wipAgencyIdList);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(wipAgencyIdList.getName(), release.getReleaseNumber());

        assertEquals(wipAgencyIdList.getState(), getText(editAgencyIDListPage.getStateField()));
        assertDisabled(editAgencyIDListPage.getAgencyIDListNameField());
        assertDisabled(editAgencyIDListPage.getListIDField());
        assertDisabled(editAgencyIDListPage.getVersionField());
        assertDisabled(editAgencyIDListPage.getDefinitionField());
        assertDisabled(editAgencyIDListPage.getDefinitionSourceField());
        assertEnabled(editAgencyIDListPage.getCommentButton());

        EditAgencyIDListValueDialog editAgencyIDListValueDialog =
                editAgencyIDListPage.openAgencyIDListValueDialogByValue(agencyIDListValue.getValue());

        assertDisabled(editAgencyIDListValueDialog.getValueField());
        assertDisabled(editAgencyIDListValueDialog.getMeaningField());
        assertDisabled(editAgencyIDListValueDialog.getDefinitionField());
        assertDisabled(editAgencyIDListValueDialog.getDefinitionSourceField());
    }

    @Test
    @DisplayName("TC_37_1_4 (QA)")
    public void TA_4_QA() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherEndUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherEndUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherEndUser);

        AgencyIDListObject qaAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(anotherEndUser, namespace, release, "QA");
        AgencyIDListValueObject agencyIDListValue =
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherEndUser, qaAgencyIdList);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(qaAgencyIdList.getName(), release.getReleaseNumber());

        assertEquals(qaAgencyIdList.getState(), getText(editAgencyIDListPage.getStateField()));
        assertDisabled(editAgencyIDListPage.getAgencyIDListNameField());
        assertDisabled(editAgencyIDListPage.getListIDField());
        assertDisabled(editAgencyIDListPage.getVersionField());
        assertDisabled(editAgencyIDListPage.getDefinitionField());
        assertDisabled(editAgencyIDListPage.getDefinitionSourceField());
        assertEnabled(editAgencyIDListPage.getCommentButton());

        EditAgencyIDListValueDialog editAgencyIDListValueDialog =
                editAgencyIDListPage.openAgencyIDListValueDialogByValue(agencyIDListValue.getValue());

        assertDisabled(editAgencyIDListValueDialog.getValueField());
        assertDisabled(editAgencyIDListValueDialog.getMeaningField());
        assertDisabled(editAgencyIDListValueDialog.getDefinitionField());
        assertDisabled(editAgencyIDListValueDialog.getDefinitionSourceField());
    }

    @Test
    @DisplayName("TC_37_1_4 (Production)")
    public void TA_4_Production() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherEndUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherEndUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherEndUser);

        AgencyIDListObject productionAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(anotherEndUser, namespace, release, "Production");
        AgencyIDListValueObject agencyIDListValue =
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherEndUser, productionAgencyIdList);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage = viewEditAgencyIDListPage.openEditAgencyIDListPage(productionAgencyIdList);

        assertEquals(productionAgencyIdList.getState(), getText(editAgencyIDListPage.getStateField()));
        assertDisabled(editAgencyIDListPage.getAgencyIDListNameField());
        assertDisabled(editAgencyIDListPage.getListIDField());
        assertDisabled(editAgencyIDListPage.getVersionField());
        assertDisabled(editAgencyIDListPage.getDefinitionField());
        assertDisabled(editAgencyIDListPage.getDefinitionSourceField());
        assertEnabled(editAgencyIDListPage.getCommentButton());

        EditAgencyIDListValueDialog editAgencyIDListValueDialog =
                editAgencyIDListPage.openAgencyIDListValueDialogByValue(agencyIDListValue.getValue());

        assertDisabled(editAgencyIDListValueDialog.getValueField());
        assertDisabled(editAgencyIDListValueDialog.getMeaningField());
        assertDisabled(editAgencyIDListValueDialog.getDefinitionField());
        assertDisabled(editAgencyIDListValueDialog.getDefinitionSourceField());
    }

    @Test
    @DisplayName("TC_37_1_5")
    public void TA_5() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease();

        AgencyIDListObject publishedAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().getAgencyIDListByNameAndBranchAndState(
                        "clm63055D16B_AgencyIdentification", release.getReleaseNumber(), "Published");
        List<AgencyIDListValueObject> agencyIDListValueList =
                getAPIFactory().getAgencyIDListValueAPI().getAgencyIDListValueByAgencyListID(publishedAgencyIdList);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage = viewEditAgencyIDListPage.openEditAgencyIDListPage(publishedAgencyIdList);

        assertEquals(publishedAgencyIdList.getState(), getText(editAgencyIDListPage.getStateField()));
        assertDisabled(editAgencyIDListPage.getAgencyIDListNameField());
        assertDisabled(editAgencyIDListPage.getListIDField());
        assertDisabled(editAgencyIDListPage.getVersionField());
        assertDisabled(editAgencyIDListPage.getDefinitionField());
        assertDisabled(editAgencyIDListPage.getDefinitionSourceField());
        assertEnabled(editAgencyIDListPage.getCommentButton());

        EditAgencyIDListValueDialog editAgencyIDListValueDialog =
                editAgencyIDListPage.openAgencyIDListValueDialogByValue(agencyIDListValueList.get(0).getValue());

        assertDisabled(editAgencyIDListValueDialog.getValueField());
        assertDisabled(editAgencyIDListValueDialog.getMeaningField());
        assertDisabled(editAgencyIDListValueDialog.getDefinitionField());
        assertDisabled(editAgencyIDListValueDialog.getDefinitionSourceField());
    }

    @Test
    @DisplayName("TC_37_1_6")
    public void TA_6() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

        AgencyIDListObject wipAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(endUser, namespace, release, "WIP");
        AgencyIDListValueObject agencyIDListValue =
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(endUser, wipAgencyIdList);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(wipAgencyIdList.getName(), release.getReleaseNumber());

        assertEquals(wipAgencyIdList.getState(), getText(editAgencyIDListPage.getStateField()));
        assertDisabled(editAgencyIDListPage.getAgencyIDListNameField());
        assertDisabled(editAgencyIDListPage.getListIDField());
        assertDisabled(editAgencyIDListPage.getVersionField());
        assertDisabled(editAgencyIDListPage.getDefinitionField());
        assertDisabled(editAgencyIDListPage.getDefinitionSourceField());
        assertEnabled(editAgencyIDListPage.getCommentButton());

        EditAgencyIDListValueDialog editAgencyIDListValueDialog =
                editAgencyIDListPage.openAgencyIDListValueDialogByValue(agencyIDListValue.getValue());

        assertDisabled(editAgencyIDListValueDialog.getValueField());
        assertDisabled(editAgencyIDListValueDialog.getMeaningField());
        assertDisabled(editAgencyIDListValueDialog.getDefinitionField());
        assertDisabled(editAgencyIDListValueDialog.getDefinitionSourceField());
    }

}
