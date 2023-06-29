package org.oagi.score.e2e.TS_37_ReleaseBranchAgencyIDListManagementEndUser;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.agency_id_list.EditAgencyIDListPage;
import org.oagi.score.e2e.page.agency_id_list.EditAgencyIDListValueDialog;
import org.oagi.score.e2e.page.agency_id_list.ViewEditAgencyIDListPage;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_37_4_AmendEndUserAgencyIDList extends BaseTest {

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
    @DisplayName("TC_37_4_1")
    public void TA_1() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherEndUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherEndUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherEndUser);

        AgencyIDListObject agencyIDList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(anotherEndUser, namespace, release, "Production");
        List<AgencyIDListValueObject> agencyIDListValues = Arrays.asList(
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherEndUser, agencyIDList),
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherEndUser, agencyIDList),
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherEndUser, agencyIDList));

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(agencyIDList.getName(), release.getReleaseNumber());
        int revision = Integer.valueOf(getText(editAgencyIDListPage.getRevisionField()));
        editAgencyIDListPage.amend();

        assertEquals("Agency ID List", getText(editAgencyIDListPage.getCoreComponentField()));
        assertEquals(Integer.toString(revision + 1), getText(editAgencyIDListPage.getRevisionField()));
        assertEquals(agencyIDList.getGuid(), getText(editAgencyIDListPage.getGUIDField()));
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
    @DisplayName("TC_37_4_2")
    public void TA_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease();

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch("clm63055D16B_AgencyIdentification", release.getReleaseNumber());
        assertThrows(TimeoutException.class, () -> editAgencyIDListPage.amend());
    }

    @Test
    @Disabled
    @DisplayName("TC_37_4_3_a")
    public void TA_3_a() {
        // not implemented yet
    }

    @Test
    @DisplayName("TC_37_4_3_b")
    public void TA_3_b() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherEndUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherEndUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherEndUser);

        AgencyIDListObject agencyIDList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(anotherEndUser, namespace, release, "Production");
        List<AgencyIDListValueObject> agencyIDListValues = Arrays.asList(
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherEndUser, agencyIDList),
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherEndUser, agencyIDList),
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherEndUser, agencyIDList));

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(agencyIDList.getName(), release.getReleaseNumber());
        editAgencyIDListPage.amend();

        assertDisabled(editAgencyIDListPage.getNamespaceSelectField());
        assertDisabled(editAgencyIDListPage.getListIDField());
        // TODO: The Version field is initially set to pre-amendment + “New” (same as developer code list)
        assertEquals(agencyIDList.getVersionId(), getText(editAgencyIDListPage.getVersionField()));

        for (AgencyIDListValueObject agencyIDListValue : agencyIDListValues) {
            EditAgencyIDListValueDialog editAgencyIDListValueDialog =
                    editAgencyIDListPage.openAgencyIDListValueDialogByValue(agencyIDListValue.getValue());

            assertDisabled(editAgencyIDListValueDialog.getValueField());
            escape(getDriver());
        }
    }

    @Test
    @DisplayName("TC_37_4_3_c")
    public void TA_3_c() {
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

        String versionId = randomAlphanumeric(5, 10);
        String definition = randomPrint(50, 100).trim();
        String definitionSource = randomAlphanumeric(5, 10);
        String remark = randomPrint(50, 100).trim();
        editAgencyIDListPage.setVersion(versionId);
        editAgencyIDListPage.setDefinition(definition);
        editAgencyIDListPage.setDefinitionSource(definitionSource);
        editAgencyIDListPage.setRemark(remark);
        editAgencyIDListPage.hitUpdateButton();
        editAgencyIDListPage.openPage();

        assertEquals(versionId, getText(editAgencyIDListPage.getVersionField()));
        assertEquals(definition, getText(editAgencyIDListPage.getDefinitionField()));
        assertEquals(definitionSource, getText(editAgencyIDListPage.getDefinitionSourceField()));
        assertEquals(remark, getText(editAgencyIDListPage.getRemarkField()));
    }

    @Test
    @Disabled
    @DisplayName("TC_37_4_4")
    public void TA_4() {
        // Tested by TC_37_4_3_b
        // 'Replaced By' function has not been implemented yet.
    }

    @Test
    @Disabled
    @DisplayName("TC_37_4_5")
    public void TA_5() {
        // Duplicated with TC_37_4_4
    }

    @Test
    @DisplayName("TC_37_4_6")
    public void TA_6() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherEndUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherEndUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherEndUser);

        AgencyIDListObject agencyIDList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(anotherEndUser, namespace, release, "Production");
        List<AgencyIDListValueObject> agencyIDListValues = Arrays.asList(
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherEndUser, agencyIDList),
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherEndUser, agencyIDList),
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherEndUser, agencyIDList));

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(agencyIDList.getName(), release.getReleaseNumber());
        editAgencyIDListPage.amend();

        AgencyIDListValueObject agencyIDListValue = AgencyIDListValueObject.createRandomAgencyIDListValue(endUser);
        EditAgencyIDListValueDialog editAgencyIDListValueDialog = editAgencyIDListPage.addAgencyIDListValue();
        editAgencyIDListValueDialog.setValue(agencyIDListValue.getValue());
        editAgencyIDListValueDialog.setMeaning(agencyIDListValue.getName());
        editAgencyIDListValueDialog.setDefinition(agencyIDListValue.getDefinition());
        editAgencyIDListValueDialog.setDefinitionSource(agencyIDListValue.getDefinitionSource());
        editAgencyIDListValueDialog.hitAddButton();
        editAgencyIDListPage.hitUpdateButton();

        editAgencyIDListPage.openPage();
        editAgencyIDListValueDialog =
                editAgencyIDListPage.openAgencyIDListValueDialogByValue(agencyIDListValue.getValue());

        assertEquals(agencyIDListValue.getValue(), getText(editAgencyIDListValueDialog.getValueField()));
        assertEquals(agencyIDListValue.getName(), getText(editAgencyIDListValueDialog.getMeaningField()));
        assertEquals(agencyIDListValue.getDefinition(), getText(editAgencyIDListValueDialog.getDefinitionField()));
        assertEquals(agencyIDListValue.getDefinitionSource(), getText(editAgencyIDListValueDialog.getDefinitionSourceField()));
    }

    @Test
    @DisplayName("TC_37_4_7")
    public void TA_7() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherEndUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherEndUser);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease();
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherEndUser);

        AgencyIDListObject agencyIDList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(anotherEndUser, namespace, release, "Production");
        List<AgencyIDListValueObject> agencyIDListValues = Arrays.asList(
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherEndUser, agencyIDList),
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherEndUser, agencyIDList),
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherEndUser, agencyIDList));

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(agencyIDList.getName(), release.getReleaseNumber());
        editAgencyIDListPage.amend();

        AgencyIDListValueObject agencyIDListValue = AgencyIDListValueObject.createRandomAgencyIDListValue(endUser);
        EditAgencyIDListValueDialog editAgencyIDListValueDialog = editAgencyIDListPage.addAgencyIDListValue();
        editAgencyIDListValueDialog.setValue(agencyIDListValue.getValue());
        editAgencyIDListValueDialog.setMeaning(agencyIDListValue.getName());
        editAgencyIDListValueDialog.setDefinition(agencyIDListValue.getDefinition());
        editAgencyIDListValueDialog.setDefinitionSource(agencyIDListValue.getDefinitionSource());
        editAgencyIDListValueDialog.hitAddButton();
        editAgencyIDListPage.hitUpdateButton();

        editAgencyIDListPage.openPage();
        WebElement tr = editAgencyIDListPage.getTableRecordByValue(agencyIDListValue.getValue());
        WebElement td = editAgencyIDListPage.getColumnByName(tr, "select");
        click(td);
        editAgencyIDListPage.hitRemoveAgencyIDListValueButton();
        editAgencyIDListPage.hitUpdateButton();
        viewEditAgencyIDListPage.openPage();
        editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(agencyIDList.getName(), release.getReleaseNumber());

        EditAgencyIDListPage finalEditAgencyIDListPage = editAgencyIDListPage;
        assertThrows(WebDriverException.class, () -> finalEditAgencyIDListPage.openAgencyIDListValueDialogByValue(agencyIDListValue.getValue()));
    }

    @Test
    @DisplayName("TC_37_4_8")
    public void TA_8() {
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

        String versionId = randomAlphanumeric(5, 10);
        String definition = randomPrint(50, 100).trim();
        String definitionSource = randomAlphanumeric(5, 10);
        String remark = randomPrint(50, 100).trim();
        editAgencyIDListPage.setVersion(versionId);
        editAgencyIDListPage.setDefinition(definition);
        editAgencyIDListPage.setDefinitionSource(definitionSource);
        editAgencyIDListPage.setRemark(remark);
        editAgencyIDListPage.hitUpdateButton();

        editAgencyIDListPage.openPage();
        editAgencyIDListPage.cancel();

        assertEquals(Integer.toString(1), getText(editAgencyIDListPage.getRevisionField()));
        assertEquals(anotherEndUser.getLoginId(), getText(editAgencyIDListPage.getOwnerField()));
        assertEquals(agencyIDList.getVersionId(), getText(editAgencyIDListPage.getVersionField()));
        assertEquals(agencyIDList.getDefinition(), getText(editAgencyIDListPage.getDefinitionField()));
        assertEquals(agencyIDList.getDefinitionSource(), getText(editAgencyIDListPage.getDefinitionSourceField()));
        assertEquals(agencyIDList.getRemark(), getText(editAgencyIDListPage.getRemarkField()));
    }

}
