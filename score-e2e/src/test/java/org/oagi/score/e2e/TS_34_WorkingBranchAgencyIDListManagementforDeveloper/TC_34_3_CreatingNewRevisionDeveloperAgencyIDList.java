package org.oagi.score.e2e.TS_34_WorkingBranchAgencyIDListManagementforDeveloper;

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
public class TC_34_3_CreatingNewRevisionDeveloperAgencyIDList extends BaseTest {

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
    @DisplayName("TC_34_3_1")
    public void TA_1() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

        AgencyIDListObject agencyIDList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "Published");
        List<AgencyIDListValueObject> agencyIDListValues = Arrays.asList(
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(developer, agencyIDList),
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(developer, agencyIDList),
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(developer, agencyIDList));

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(agencyIDList.getName(), release.getReleaseNumber());
        int revision = Integer.valueOf(getText(editAgencyIDListPage.getRevisionField()));
        editAgencyIDListPage.revise();

        assertEquals("Agency ID List", getText(editAgencyIDListPage.getCoreComponentField()));
        assertEquals(Integer.toString(revision + 1), getText(editAgencyIDListPage.getRevisionField()));
        assertEquals(agencyIDList.getGuid(), getText(editAgencyIDListPage.getGUIDField()));
        assertEquals(release.getReleaseNumber(), getText(editAgencyIDListPage.getReleaseField()));
        assertEquals("WIP", getText(editAgencyIDListPage.getStateField()));
        assertEquals(developer.getLoginId(), getText(editAgencyIDListPage.getOwnerField()));
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

}
