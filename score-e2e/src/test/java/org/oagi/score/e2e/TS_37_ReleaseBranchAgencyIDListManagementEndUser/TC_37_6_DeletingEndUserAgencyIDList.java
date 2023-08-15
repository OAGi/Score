package org.oagi.score.e2e.TS_37_ReleaseBranchAgencyIDListManagementEndUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AgencyIDListObject;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.agency_id_list.EditAgencyIDListPage;
import org.oagi.score.e2e.page.agency_id_list.ViewEditAgencyIDListPage;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_37_6_DeletingEndUserAgencyIDList extends BaseTest {

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
    @DisplayName("TC_37_6_1")
    public void TA_1() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

        AgencyIDListObject agencyIDList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(endUser, namespace, release, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(agencyIDList.getName(), release.getReleaseNumber());
        editAgencyIDListPage.delete();

        viewEditAgencyIDListPage.setBranch(release.getReleaseNumber());
        viewEditAgencyIDListPage.setName(agencyIDList.getName());
        viewEditAgencyIDListPage.hitSearchButton();

        WebElement tr = viewEditAgencyIDListPage.getTableRecordByValue(agencyIDList.getName());
        WebElement td = viewEditAgencyIDListPage.getColumnByName(tr, "state");
        assertEquals("Deleted", getText(td));
    }

    @Test
    @DisplayName("TC_37_6_2")
    public void TA_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherEndUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherEndUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherEndUser);

        AgencyIDListObject agencyIDList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(anotherEndUser, namespace, release, "Production");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(agencyIDList.getName(), release.getReleaseNumber());
        editAgencyIDListPage.amend();

        assertThrows(TimeoutException.class, () -> editAgencyIDListPage.delete());
    }

}
