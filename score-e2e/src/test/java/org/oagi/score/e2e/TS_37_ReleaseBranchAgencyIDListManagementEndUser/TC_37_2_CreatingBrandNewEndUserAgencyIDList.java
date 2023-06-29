package org.oagi.score.e2e.TS_37_ReleaseBranchAgencyIDListManagementEndUser;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.agency_id_list.EditAgencyIDListPage;
import org.oagi.score.e2e.page.agency_id_list.EditAgencyIDListValueDialog;
import org.oagi.score.e2e.page.agency_id_list.ViewEditAgencyIDListPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_37_2_CreatingBrandNewEndUserAgencyIDList extends BaseTest {

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
    @DisplayName("TC_37_2_1")
    public void TA_1() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease();

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openNewAgencyIDList(endUser, release.getReleaseNumber());

        assertEquals("Agency ID List", getText(editAgencyIDListPage.getCoreComponentField()));
        assertEquals(Integer.toString(1), getText(editAgencyIDListPage.getRevisionField()));
        assertEquals(release.getReleaseNumber(), getText(editAgencyIDListPage.getReleaseField()));
        assertEquals("WIP", getText(editAgencyIDListPage.getStateField()));
        assertEquals(endUser.getLoginId(), getText(editAgencyIDListPage.getOwnerField()));
        assertEquals("AgencyIdentification", getText(editAgencyIDListPage.getAgencyIDListNameField()));
        assertNotNull(getText(editAgencyIDListPage.getListIDField()));
        assertTrue(StringUtils.isEmpty(getText(editAgencyIDListPage.getVersionField())));
        assertTrue(StringUtils.isEmpty(getText(editAgencyIDListPage.getDefinitionField())));
        assertTrue(StringUtils.isEmpty(getText(editAgencyIDListPage.getDefinitionSourceField())));
    }

    @Test
    @DisplayName("TC_37_2_2")
    public void TA_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openNewAgencyIDList(endUser, release.getReleaseNumber());

        String listId = randomNumeric(5, 10);
        String versionId = randomAlphanumeric(5, 10);
        String name = "clm" + listId + versionId + "_AgencyIdentification";
        String definition = randomPrint(50, 100).trim();
        String definitionSource = randomAlphanumeric(5, 10);
        AgencyIDListValueObject agencyIDListValue = AgencyIDListValueObject.createRandomAgencyIDListValue(endUser);

        editAgencyIDListPage.setName(name);
        editAgencyIDListPage.setListID(listId);
        editAgencyIDListPage.setVersion(versionId);
        editAgencyIDListPage.setNamespace(namespace);
        editAgencyIDListPage.setDefinition(definition);
        editAgencyIDListPage.setDefinitionSource(definitionSource);
        EditAgencyIDListValueDialog editAgencyIDListValueDialog = editAgencyIDListPage.addAgencyIDListValue();
        editAgencyIDListValueDialog.setValue(agencyIDListValue.getValue());
        editAgencyIDListValueDialog.setMeaning(agencyIDListValue.getName());
        editAgencyIDListValueDialog.setDefinition(agencyIDListValue.getDefinition());
        editAgencyIDListValueDialog.setDefinitionSource(agencyIDListValue.getDefinitionSource());
        editAgencyIDListValueDialog.hitAddButton();
        editAgencyIDListPage.hitUpdateButton();

        editAgencyIDListPage.openPage();
        // TC_37_2_8
        editAgencyIDListPage.setAgencyIDListValue(agencyIDListValue);
        editAgencyIDListPage.hitUpdateButton();

        editAgencyIDListPage.openPage();
        assertEquals("Agency ID List", getText(editAgencyIDListPage.getCoreComponentField()));
        assertEquals(Integer.toString(1), getText(editAgencyIDListPage.getRevisionField()));
        assertEquals(release.getReleaseNumber(), getText(editAgencyIDListPage.getReleaseField()));
        assertEquals("WIP", getText(editAgencyIDListPage.getStateField()));
        assertEquals(endUser.getLoginId(), getText(editAgencyIDListPage.getOwnerField()));
        assertEquals(name, getText(editAgencyIDListPage.getAgencyIDListNameField()));
        assertEquals(listId, getText(editAgencyIDListPage.getListIDField()));
        assertEquals(versionId, getText(editAgencyIDListPage.getVersionField()));
        assertEquals(definition, getText(editAgencyIDListPage.getDefinitionField()));
        assertEquals(definitionSource, getText(editAgencyIDListPage.getDefinitionSourceField()));

        editAgencyIDListValueDialog =
                editAgencyIDListPage.openAgencyIDListValueDialogByValue(agencyIDListValue.getValue());

        assertEquals(agencyIDListValue.getValue(), getText(editAgencyIDListValueDialog.getValueField()));
        assertEquals(agencyIDListValue.getName(), getText(editAgencyIDListValueDialog.getMeaningField()));
        assertEquals(agencyIDListValue.getDefinition(), getText(editAgencyIDListValueDialog.getDefinitionField()));
        assertEquals(agencyIDListValue.getDefinitionSource(), getText(editAgencyIDListValueDialog.getDefinitionSourceField()));
    }

    @Test
    @DisplayName("TC_37_2_3")
    public void TA_3() {
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

        WebElement tr = editAgencyIDListPage.getTableRecordByValue(agencyIDListValue.getValue());
        WebElement td = editAgencyIDListPage.getColumnByName(tr, "select");
        click(td);
        editAgencyIDListPage.hitRemoveAgencyIDListValueButton();
        editAgencyIDListPage.hitUpdateButton();
        editAgencyIDListPage.openPage();

        assertThrows(TimeoutException.class, () -> editAgencyIDListPage.getTableRecordByValue(agencyIDListValue.getValue()));
    }

    @Test
    @DisplayName("TC_37_2_4 and TC_37_2_5")
    public void TA_4_and_5() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

        AgencyIDListObject agencyIDListObject =
                getAPIFactory().getAgencyIDListAPI().getAgencyIDListByNameAndBranch("clm63055D16B_AgencyIdentification", release.getReleaseNumber());

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch("clm63055D16B_AgencyIdentification", release.getReleaseNumber());
        editAgencyIDListPage.hitDeriveAgencyIDListButton();

        editAgencyIDListPage.setListID(agencyIDListObject.getListId());
        editAgencyIDListPage.setVersion(agencyIDListObject.getVersionId());
        editAgencyIDListPage.setNamespace(namespace);
        String definition = randomPrint(50, 100).trim();
        String definitionSource = randomAlphanumeric(5, 10);
        editAgencyIDListPage.setDefinition(definition);
        editAgencyIDListPage.setDefinitionSource(definitionSource);
        editAgencyIDListPage.hitUpdateButton();

        String xpathExpr = "//score-confirm-dialog//p";
        WebElement dialogMessage = retry(() -> visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertEquals("Another agency ID list with the triplet (ListID, AgencyID, Version) already exist!", getText(dialogMessage));
    }

    @Test
    @DisplayName("TC_37_2_6")
    public void TA_6() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherEndUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherEndUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherEndUser);

        AgencyIDListObject productionAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(anotherEndUser, namespace, release, "Production");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(productionAgencyIdList.getName(), release.getReleaseNumber());
        assertThrows(TimeoutException.class, () -> editAgencyIDListPage.hitDeriveAgencyIDListButton());
    }

    @Test
    @Disabled
    @DisplayName("TC_37_2_7")
    public void TA_7() {
        // not implemented yet
    }

    @Test
    @Disabled
    @DisplayName("TC_37_2_8")
    public void TA_8() {
        // tested by TC_37_2_2.
    }

}
