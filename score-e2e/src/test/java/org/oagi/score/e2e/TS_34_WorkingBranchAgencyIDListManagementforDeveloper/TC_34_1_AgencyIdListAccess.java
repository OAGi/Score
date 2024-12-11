package org.oagi.score.e2e.TS_34_WorkingBranchAgencyIDListManagementforDeveloper;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.agency_id_list.AddCommentDialog;
import org.oagi.score.e2e.page.agency_id_list.EditAgencyIDListPage;
import org.oagi.score.e2e.page.agency_id_list.EditAgencyIDListValueDialog;
import org.oagi.score.e2e.page.agency_id_list.ViewEditAgencyIDListPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.AssertionHelper.assertEnabled;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_34_1_AgencyIdListAccess extends BaseTest {

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
    @DisplayName("TC_34_1_1")
    public void TA_1() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        AgencyIDListObject wipAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "WIP");
        AgencyIDListObject draftAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "Draft");
        AgencyIDListObject candidateAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "Candidate");
        AgencyIDListObject publishedAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "Published");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        viewEditAgencyIDListPage.setName(wipAgencyIdList.getName());
        viewEditAgencyIDListPage.hitSearchButton();
        WebElement tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
        assertEquals("WIP", getText(viewEditAgencyIDListPage.getColumnByName(tr, "state")));

        viewEditAgencyIDListPage.setName(draftAgencyIdList.getName());
        viewEditAgencyIDListPage.hitSearchButton();
        tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
        assertEquals("Draft", getText(viewEditAgencyIDListPage.getColumnByName(tr, "state")));

        viewEditAgencyIDListPage.setName(candidateAgencyIdList.getName());
        viewEditAgencyIDListPage.hitSearchButton();
        tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
        assertEquals("Candidate", getText(viewEditAgencyIDListPage.getColumnByName(tr, "state")));

        viewEditAgencyIDListPage.setName(publishedAgencyIdList.getName());
        viewEditAgencyIDListPage.hitSearchButton();
        tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
        assertEquals("Published", getText(viewEditAgencyIDListPage.getColumnByName(tr, "state")));
    }

    @Test
    @DisplayName("TC_34_1_2")
    public void TA_2() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        AgencyIDListObject wipAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "WIP");
        AgencyIDListValueObject agencyIDListValue =
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(developer, wipAgencyIdList);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
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

        EditAgencyIDListValueDialog editAgencyIDListValueDialog =
                editAgencyIDListPage.openAgencyIDListValueDialogByValue(agencyIDListValue.getValue());

        assertEquals(agencyIDListValue.getValue(), getText(editAgencyIDListValueDialog.getValueField()));
        assertEquals(agencyIDListValue.getName(), getText(editAgencyIDListValueDialog.getMeaningField()));
        assertEquals(agencyIDListValue.getDefinition(), getText(editAgencyIDListValueDialog.getDefinitionField()));
        assertEquals(agencyIDListValue.getDefinitionSource(), getText(editAgencyIDListValueDialog.getDefinitionSourceField()));
    }

    @Test
    @DisplayName("TC_34_1_3")
    public void TA_3() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        AgencyIDListObject wipAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(anotherDeveloper, namespace, release, "WIP");
        AgencyIDListValueObject agencyIDListValue =
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherDeveloper, wipAgencyIdList);

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

        EditAgencyIDListValueDialog editAgencyIDListValueDialog =
                editAgencyIDListPage.openAgencyIDListValueDialogByValue(agencyIDListValue.getValue());

        assertDisabled(editAgencyIDListValueDialog.getValueField());
        assertDisabled(editAgencyIDListValueDialog.getMeaningField());
        assertDisabled(editAgencyIDListValueDialog.getDefinitionField());
        assertDisabled(editAgencyIDListValueDialog.getDefinitionSourceField());
    }

    @Test
    @DisplayName("TC_34_1_4 (Draft)")
    public void TA_4_Draft() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        AgencyIDListObject draftAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(anotherDeveloper, namespace, release, "Draft");
        AgencyIDListValueObject agencyIDListValue =
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherDeveloper, draftAgencyIdList);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(draftAgencyIdList.getName(), release.getReleaseNumber());

        assertEquals(draftAgencyIdList.getState(), getText(editAgencyIDListPage.getStateField()));
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
    @DisplayName("TC_34_1_4 (Candidate)")
    public void TA_4_Candidate() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        AgencyIDListObject candidateAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(anotherDeveloper, namespace, release, "Candidate");
        AgencyIDListValueObject agencyIDListValue =
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherDeveloper, candidateAgencyIdList);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(candidateAgencyIdList.getName(), release.getReleaseNumber());

        assertEquals(candidateAgencyIdList.getState(), getText(editAgencyIDListPage.getStateField()));
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
    @DisplayName("TC_34_1_4 (Deleted)")
    public void TA_4_Deleted() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        AgencyIDListObject deletedAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(anotherDeveloper, namespace, release, "Deleted");
        AgencyIDListValueObject agencyIDListValue =
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherDeveloper, deletedAgencyIdList);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(deletedAgencyIdList.getName(), release.getReleaseNumber());

        assertEquals(deletedAgencyIdList.getState(), getText(editAgencyIDListPage.getStateField()));
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
    @DisplayName("TC_34_1_4 (Release Draft)")
    public void TA_4_ReleaseDraft() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        AgencyIDListObject releaseDraftAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(anotherDeveloper, namespace, release, "ReleaseDraft");
        AgencyIDListValueObject agencyIDListValue =
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherDeveloper, releaseDraftAgencyIdList);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(releaseDraftAgencyIdList.getName(), release.getReleaseNumber());

        assertEquals(releaseDraftAgencyIdList.getState(), getText(editAgencyIDListPage.getStateField()));
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
    @DisplayName("TC_34_1_5")
    public void TA_5() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        AgencyIDListObject publishedAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(anotherDeveloper, namespace, release, "Published");
        AgencyIDListValueObject agencyIDListValue =
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherDeveloper, publishedAgencyIdList);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(publishedAgencyIdList.getName(), release.getReleaseNumber());

        assertEquals(publishedAgencyIdList.getState(), getText(editAgencyIDListPage.getStateField()));
        assertDisabled(editAgencyIDListPage.getAgencyIDListNameField());
        assertDisabled(editAgencyIDListPage.getListIDField());
        assertDisabled(editAgencyIDListPage.getVersionField());
        assertDisabled(editAgencyIDListPage.getDefinitionField());
        assertDisabled(editAgencyIDListPage.getDefinitionSourceField());
        assertEnabled(editAgencyIDListPage.getCommentButton());
        assertEnabled(editAgencyIDListPage.getReviseButton());

        EditAgencyIDListValueDialog editAgencyIDListValueDialog =
                editAgencyIDListPage.openAgencyIDListValueDialogByValue(agencyIDListValue.getValue());

        assertDisabled(editAgencyIDListValueDialog.getValueField());
        assertDisabled(editAgencyIDListValueDialog.getMeaningField());
        assertDisabled(editAgencyIDListValueDialog.getDefinitionField());
        assertDisabled(editAgencyIDListValueDialog.getDefinitionSourceField());
    }

    @Test
    @DisplayName("TC_34_1_6")
    public void TA_6() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        viewEditAgencyIDListPage.showAdvancedSearchPanel();
        viewEditAgencyIDListPage.setOwner(endUser.getLoginId());
        viewEditAgencyIDListPage.hitSearchButton();

        assertEquals(0, viewEditAgencyIDListPage.getTotalNumberOfItems());
    }

    @Test
    @DisplayName("TC_34_1_7")
    @Disabled
    public void TA_7() {
        // Duplicated with TA_4_Deleted
    }

    @Test
    @DisplayName("TC_34_1_8 (WIP)")
    public void TA_8_WIP() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        AgencyIDListObject wipAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(anotherDeveloper, namespace, release, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(wipAgencyIdList.getName(), release.getReleaseNumber());
        AddCommentDialog commentDialog = editAgencyIDListPage.openCommentDialog();
        String commentText = RandomStringUtils.secure().nextPrint(50, 100).trim();
        commentDialog.setComment(commentText);
        homePage.logout();

        homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());
        editAgencyIDListPage.openPage();
        commentDialog = editAgencyIDListPage.openCommentDialog();
        AddCommentDialog.CommentContent content = commentDialog.getContent(1);
        assertEquals(developer.getLoginId(), content.getCreator());
        assertEquals(commentText, content.getCommentText());
    }

    @Test
    @DisplayName("TC_34_1_8 (Draft)")
    public void TA_8_Draft() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        AgencyIDListObject draftAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(anotherDeveloper, namespace, release, "Draft");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(draftAgencyIdList.getName(), release.getReleaseNumber());
        AddCommentDialog commentDialog = editAgencyIDListPage.openCommentDialog();
        String commentText = RandomStringUtils.secure().nextPrint(50, 100).trim();
        commentDialog.setComment(commentText);
        homePage.logout();

        homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());
        editAgencyIDListPage.openPage();
        commentDialog = editAgencyIDListPage.openCommentDialog();
        AddCommentDialog.CommentContent content = commentDialog.getContent(1);
        assertEquals(developer.getLoginId(), content.getCreator());
        assertEquals(commentText, content.getCommentText());
    }

    @Test
    @DisplayName("TC_34_1_8 (Candidate)")
    public void TA_8_Candidate() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        AgencyIDListObject candidateAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(anotherDeveloper, namespace, release, "Candidate");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(candidateAgencyIdList.getName(), release.getReleaseNumber());
        AddCommentDialog commentDialog = editAgencyIDListPage.openCommentDialog();
        String commentText = RandomStringUtils.secure().nextPrint(50, 100).trim();
        commentDialog.setComment(commentText);
        homePage.logout();

        homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());
        editAgencyIDListPage.openPage();
        commentDialog = editAgencyIDListPage.openCommentDialog();
        AddCommentDialog.CommentContent content = commentDialog.getContent(1);
        assertEquals(developer.getLoginId(), content.getCreator());
        assertEquals(commentText, content.getCommentText());
    }

    @Test
    @DisplayName("TC_34_1_8 (Release Draft)")
    public void TA_8_ReleaseDraft() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        AppUserObject anotherDeveloper = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherDeveloper);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        AgencyIDListObject candidateAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(anotherDeveloper, namespace, release, "ReleaseDraft");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(candidateAgencyIdList.getName(), release.getReleaseNumber());
        AddCommentDialog commentDialog = editAgencyIDListPage.openCommentDialog();
        String commentText = RandomStringUtils.secure().nextPrint(50, 100).trim();
        commentDialog.setComment(commentText);
        homePage.logout();

        homePage = loginPage().signIn(anotherDeveloper.getLoginId(), anotherDeveloper.getPassword());
        editAgencyIDListPage.openPage();
        commentDialog = editAgencyIDListPage.openCommentDialog();
        AddCommentDialog.CommentContent content = commentDialog.getContent(1);
        assertEquals(developer.getLoginId(), content.getCreator());
        assertEquals(commentText, content.getCommentText());
    }

    @Test
    @DisplayName("TC_34_1_9 (WIP)")
    public void TA_9_WIP() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        AgencyIDListObject wipAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(wipAgencyIdList.getName(), release.getReleaseNumber());
        AddCommentDialog commentDialog = editAgencyIDListPage.openCommentDialog();
        String commentText = RandomStringUtils.secure().nextPrint(50, 100).trim();
        commentDialog.setComment(commentText);
        homePage.logout();

        homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        editAgencyIDListPage.openPage();
        commentDialog = editAgencyIDListPage.openCommentDialog();
        AddCommentDialog.CommentContent content = commentDialog.getContent(1);
        assertEquals(endUser.getLoginId(), content.getCreator());
        assertEquals(commentText, content.getCommentText());
    }

    @Test
    @DisplayName("TC_34_1_9 (Draft)")
    public void TA_9_Draft() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        AgencyIDListObject draftAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "Draft");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(draftAgencyIdList.getName(), release.getReleaseNumber());
        AddCommentDialog commentDialog = editAgencyIDListPage.openCommentDialog();
        String commentText = RandomStringUtils.secure().nextPrint(50, 100).trim();
        commentDialog.setComment(commentText);
        homePage.logout();

        homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        editAgencyIDListPage.openPage();
        commentDialog = editAgencyIDListPage.openCommentDialog();
        AddCommentDialog.CommentContent content = commentDialog.getContent(1);
        assertEquals(endUser.getLoginId(), content.getCreator());
        assertEquals(commentText, content.getCommentText());
    }

    @Test
    @DisplayName("TC_34_1_9 (Candidate)")
    public void TA_9_Candidate() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        AgencyIDListObject candidateAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "Candidate");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(candidateAgencyIdList.getName(), release.getReleaseNumber());
        AddCommentDialog commentDialog = editAgencyIDListPage.openCommentDialog();
        String commentText = RandomStringUtils.secure().nextPrint(50, 100).trim();
        commentDialog.setComment(commentText);
        homePage.logout();

        homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        editAgencyIDListPage.openPage();
        commentDialog = editAgencyIDListPage.openCommentDialog();
        AddCommentDialog.CommentContent content = commentDialog.getContent(1);
        assertEquals(endUser.getLoginId(), content.getCreator());
        assertEquals(commentText, content.getCommentText());
    }

    @Test
    @DisplayName("TC_34_1_9 (Release Draft)")
    public void TA_9_ReleaseDraft() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        AgencyIDListObject releaseDraftAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "ReleaseDraft");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(releaseDraftAgencyIdList.getName(), release.getReleaseNumber());
        AddCommentDialog commentDialog = editAgencyIDListPage.openCommentDialog();
        String commentText = RandomStringUtils.secure().nextPrint(50, 100).trim();
        commentDialog.setComment(commentText);
        homePage.logout();

        homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        editAgencyIDListPage.openPage();
        commentDialog = editAgencyIDListPage.openCommentDialog();
        AddCommentDialog.CommentContent content = commentDialog.getContent(1);
        assertEquals(endUser.getLoginId(), content.getCreator());
        assertEquals(commentText, content.getCommentText());
    }

    @Test
    @DisplayName("TC_34_1_10")
    public void TA_10() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        List<AgencyIDListObject> agencyIDLists = Arrays.asList("10.6", "10.7.0.1", "10.7.1").stream()
                .map(branch -> getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, branch))
                .map(release -> getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "WIP"))
                .collect(Collectors.toList());

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        for (AgencyIDListObject agencyIDList : agencyIDLists) {
            ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseById(agencyIDList.getReleaseId());
            viewEditAgencyIDListPage.setBranch(release.getReleaseNumber());
            viewEditAgencyIDListPage.setName(agencyIDList.getName());
            viewEditAgencyIDListPage.hitSearchButton();

            WebElement tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
            assertEquals(agencyIDList.getName(), getText(viewEditAgencyIDListPage.getColumnByName(tr, "name").findElement(By.cssSelector("a"))));
        }
    }

    @Test
    @DisplayName("TC_34_1_11")
    public void TA_11() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        AgencyIDListObject agencyIDList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "WIP");
        agencyIDList.setDeprecated(true);
        getAPIFactory().getAgencyIDListAPI().updateAgencyIDList(agencyIDList);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        viewEditAgencyIDListPage.showAdvancedSearchPanel();
        viewEditAgencyIDListPage.setBranch(release.getReleaseNumber());
        viewEditAgencyIDListPage.setName(agencyIDList.getName());
        viewEditAgencyIDListPage.setDeprecated(true);
        viewEditAgencyIDListPage.hitSearchButton();

        WebElement tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
        assertEquals(agencyIDList.getName(), getText(viewEditAgencyIDListPage.getColumnByName(tr, "name").findElement(By.cssSelector("a"))));
    }

    @Test
    @DisplayName("TC_34_1_12")
    public void TA_12() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        List<AgencyIDListObject> agencyIDLists = Arrays.asList("WIP", "Draft", "Candidate", "ReleaseDraft", "Published", "Deleted").stream()
                .map(state -> getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, state))
                .collect(Collectors.toList());

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        for (AgencyIDListObject agencyIDList : agencyIDLists) {
            viewEditAgencyIDListPage.showAdvancedSearchPanel();
            viewEditAgencyIDListPage.setBranch(release.getReleaseNumber());
            viewEditAgencyIDListPage.setName(agencyIDList.getName());
            viewEditAgencyIDListPage.setState(agencyIDList.getState());
            viewEditAgencyIDListPage.hitSearchButton();

            WebElement tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
            assertEquals(agencyIDList.getName(), getText(viewEditAgencyIDListPage.getColumnByName(tr, "name").findElement(By.cssSelector("a"))));
            viewEditAgencyIDListPage.openPage();
        }
    }

    @Test
    @DisplayName("TC_34_1_13")
    public void TA_13() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        AgencyIDListObject agencyIDList = getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "WIP");
        agencyIDList.setCreationTimestamp(LocalDateTime.of(1999, 1, 1, 1, 1, 1));
        agencyIDList.setLastUpdateTimestamp(LocalDateTime.of(1999, 12, 31, 23, 59, 59));
        getAPIFactory().getAgencyIDListAPI().updateAgencyIDList(agencyIDList);

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        viewEditAgencyIDListPage.showAdvancedSearchPanel();
        viewEditAgencyIDListPage.setUpdatedStartDate(agencyIDList.getCreationTimestamp());
        viewEditAgencyIDListPage.setUpdatedEndDate(agencyIDList.getLastUpdateTimestamp());
        viewEditAgencyIDListPage.setName(agencyIDList.getName());
        viewEditAgencyIDListPage.hitSearchButton();

        WebElement tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
        assertEquals(agencyIDList.getName(), getText(viewEditAgencyIDListPage.getColumnByName(tr, "name").findElement(By.cssSelector("a"))));
    }

    @Test
    @DisplayName("TC_34_1_14")
    public void TA_14() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        AgencyIDListObject agencyIDList = getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        viewEditAgencyIDListPage.setName(agencyIDList.getName());
        viewEditAgencyIDListPage.hitSearchButton();

        WebElement tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
        assertEquals(agencyIDList.getName(), getText(viewEditAgencyIDListPage.getColumnByName(tr, "name").findElement(By.cssSelector("a"))));
    }

    @Test
    @DisplayName("TC_34_1_15")
    public void TA_15() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        AgencyIDListObject agencyIDList = getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "WIP");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        viewEditAgencyIDListPage.showAdvancedSearchPanel();
        viewEditAgencyIDListPage.setDefinition(agencyIDList.getDefinition().substring(0, 10));
        viewEditAgencyIDListPage.setName(agencyIDList.getName());
        viewEditAgencyIDListPage.hitSearchButton();

        WebElement tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
        assertEquals(agencyIDList.getName(), getText(viewEditAgencyIDListPage.getColumnByName(tr, "name").findElement(By.cssSelector("a"))));
    }

    @Test
    @DisplayName("TC_34_1_16")
    public void TA_16() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        AgencyIDListObject agencyIDList = getAPIFactory().getAgencyIDListAPI().getAgencyIDListByManifestId(BigInteger.ONE);
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseById(agencyIDList.getReleaseId());

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        viewEditAgencyIDListPage.showAdvancedSearchPanel();
        viewEditAgencyIDListPage.setBranch(release.getReleaseNumber());
        viewEditAgencyIDListPage.setModule("3055_D16B");
        viewEditAgencyIDListPage.hitSearchButton();

        WebElement tr = viewEditAgencyIDListPage.getTableRecordAtIndex(1);
        assertEquals(agencyIDList.getName(), getText(viewEditAgencyIDListPage.getColumnByName(tr, "name").findElement(By.cssSelector("a"))));
    }

}
