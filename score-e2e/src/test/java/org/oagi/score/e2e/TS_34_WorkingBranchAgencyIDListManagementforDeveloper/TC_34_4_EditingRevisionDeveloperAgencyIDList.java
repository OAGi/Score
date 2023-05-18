package org.oagi.score.e2e.TS_34_WorkingBranchAgencyIDListManagementforDeveloper;

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
public class TC_34_4_EditingRevisionDeveloperAgencyIDList extends BaseTest {

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
    @DisplayName("TC_34_4_1_a")
    public void TA_1_a() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        AgencyIDListObject agencyIDList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "Published");
        List<AgencyIDListValueObject> agencyIDListValues = Arrays.asList(
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(developer, agencyIDList));

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(agencyIDList.getName(), release.getReleaseNumber());
        editAgencyIDListPage.revise();

        assertEquals("Agency ID List", getText(editAgencyIDListPage.getCoreComponentField()));
        assertEquals(release.getReleaseNumber(), getText(editAgencyIDListPage.getReleaseField()));
        assertEquals("WIP", getText(editAgencyIDListPage.getStateField()));
        assertEquals("2", getText(editAgencyIDListPage.getRevisionField()));
        assertDisabled(editAgencyIDListPage.getAgencyIDListNameField());
        assertDisabled(editAgencyIDListPage.getListIDField());
        assertDisabled(editAgencyIDListPage.getAgencyIDListValueSelectField());
        assertDisabled(editAgencyIDListPage.getNamespaceSelectField());
    }

    @Test
    @DisplayName("TC_34_4_1_b")
    public void TA_1_b() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        AgencyIDListObject agencyIDList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "Published");
        List<AgencyIDListValueObject> agencyIDListValues = Arrays.asList(
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(developer, agencyIDList));

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(agencyIDList.getName(), release.getReleaseNumber());
        editAgencyIDListPage.revise();

        assertEquals("Agency ID List", getText(editAgencyIDListPage.getCoreComponentField()));
        assertEquals(release.getReleaseNumber(), getText(editAgencyIDListPage.getReleaseField()));
        assertEquals("WIP", getText(editAgencyIDListPage.getStateField()));
        assertEquals("2", getText(editAgencyIDListPage.getRevisionField()));
        String newVersion = randomAlphanumeric(5, 10);
        editAgencyIDListPage.setVersion(newVersion);
        String newDefinition = randomPrint(50, 100).trim();
        editAgencyIDListPage.setDefinition(newDefinition);
        String newDefinitionSource = randomAlphanumeric(5, 10);
        editAgencyIDListPage.setDefinitionSource(newDefinitionSource);
        editAgencyIDListPage.hitUpdateButton();

        editAgencyIDListPage.openPage();
        assertEquals(newVersion, getText(editAgencyIDListPage.getVersionField()));
        assertEquals(newDefinition, getText(editAgencyIDListPage.getDefinitionField()));
        assertEquals(newDefinitionSource, getText(editAgencyIDListPage.getDefinitionSourceField()));
    }

    @Test
    @DisplayName("TC_34_4_1_c")
    @Disabled
    public void TA_1_c() {
        // No need to test the uniqueness for a new revision.
    }

    @Test
    @DisplayName("TC_34_4_2")
    public void TA_2() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

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
        editAgencyIDListPage.revise();

        for (AgencyIDListValueObject agencyIDListValue : agencyIDListValues) {
            EditAgencyIDListValueDialog editAgencyIDListValueDialog = editAgencyIDListPage.openAgencyIDListValueDialogByValue(agencyIDListValue.getValue());
            assertDisabled(editAgencyIDListValueDialog.getValueField());
            String newMeaning = randomAlphanumeric(5, 10);
            editAgencyIDListValueDialog.setMeaning(newMeaning);
            String newDefinition = randomPrint(50, 100).trim();
            editAgencyIDListValueDialog.setDefinition(newDefinition);
            String newDefinitionSource = randomAlphanumeric(5, 10);
            editAgencyIDListValueDialog.setDefinitionSource(newDefinitionSource);
            editAgencyIDListValueDialog.hitSaveButton();

            editAgencyIDListPage.hitUpdateButton();
            editAgencyIDListPage.openPage();
            editAgencyIDListValueDialog = editAgencyIDListPage.openAgencyIDListValueDialogByValue(agencyIDListValue.getValue());
            assertEquals(newMeaning, getText(editAgencyIDListValueDialog.getMeaningField()));
            assertEquals(newDefinition, getText(editAgencyIDListValueDialog.getDefinitionField()));
            assertEquals(newDefinitionSource, getText(editAgencyIDListValueDialog.getDefinitionSourceField()));
            escape(getDriver());
        }

        // TODO: 'Replaced By' field hasn't implemented yet.
    }

    @Test
    @DisplayName("TC_34_4_3")
    public void TA_3() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        AgencyIDListObject agencyIDList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "Published");
        List<AgencyIDListValueObject> agencyIDListValues = Arrays.asList(
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(developer, agencyIDList));

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(agencyIDList.getName(), release.getReleaseNumber());
        editAgencyIDListPage.revise();

        EditAgencyIDListValueDialog editAgencyIDListValueDialog = editAgencyIDListPage.addAgencyIDListValue();
        assertDisabled(editAgencyIDListValueDialog.getDeprecatedSelectField());

        String newValue = randomAlphanumeric(5, 10).trim();
        editAgencyIDListValueDialog.setValue(newValue);
        String newMeaning = randomAlphanumeric(5, 10).trim();
        editAgencyIDListValueDialog.setMeaning(newMeaning);
        String newDefinition = randomPrint(50, 100).trim();
        editAgencyIDListValueDialog.setDefinition(newDefinition);
        String newDefinitionSource = randomAlphanumeric(5, 10);
        editAgencyIDListValueDialog.setDefinitionSource(newDefinitionSource);
        editAgencyIDListValueDialog.hitAddButton();

        editAgencyIDListPage.hitUpdateButton();
        editAgencyIDListPage.openPage();

        editAgencyIDListValueDialog = editAgencyIDListPage.openAgencyIDListValueDialogByValue(newValue);
        assertEquals(newValue, getText(editAgencyIDListValueDialog.getValueField()));
        assertEquals(newMeaning, getText(editAgencyIDListValueDialog.getMeaningField()));
        assertEquals(newDefinition, getText(editAgencyIDListValueDialog.getDefinitionField()));
        assertEquals(newDefinitionSource, getText(editAgencyIDListValueDialog.getDefinitionSourceField()));
    }

    @Test
    @DisplayName("TC_34_4_4")
    public void TA_4() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        AgencyIDListObject agencyIDList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "Published");
        List<AgencyIDListValueObject> agencyIDListValues = Arrays.asList(
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(developer, agencyIDList));

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(agencyIDList.getName(), release.getReleaseNumber());
        editAgencyIDListPage.revise();

        EditAgencyIDListValueDialog editAgencyIDListValueDialog = editAgencyIDListPage.addAgencyIDListValue();
        assertDisabled(editAgencyIDListValueDialog.getDeprecatedSelectField());

        editAgencyIDListValueDialog.setValue(agencyIDListValues.get(0).getValue());
        editAgencyIDListValueDialog.setMeaning(agencyIDListValues.get(0).getName());
        editAgencyIDListValueDialog.setDefinition(agencyIDListValues.get(0).getDefinition());
        editAgencyIDListValueDialog.setDefinitionSource(agencyIDListValues.get(0).getDefinitionSource());
        editAgencyIDListValueDialog.hitAddButton();

        assertEquals(agencyIDListValues.get(0).getValue() + " already exist", getSnackBarMessage(getDriver()));
    }

    @Test
    @DisplayName("TC_34_4_5")
    public void TA_5() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        AgencyIDListObject agencyIDList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "Published");
        List<AgencyIDListValueObject> agencyIDListValues = Arrays.asList(
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(developer, agencyIDList));

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(agencyIDList.getName(), release.getReleaseNumber());
        editAgencyIDListPage.revise();

        EditAgencyIDListValueDialog editAgencyIDListValueDialog = editAgencyIDListPage.addAgencyIDListValue();
        assertDisabled(editAgencyIDListValueDialog.getDeprecatedSelectField());

        String newValue = randomAlphanumeric(5, 10).trim();
        editAgencyIDListValueDialog.setValue(newValue);
        String newMeaning = randomAlphanumeric(5, 10).trim();
        editAgencyIDListValueDialog.setMeaning(newMeaning);
        String newDefinition = randomPrint(50, 100).trim();
        editAgencyIDListValueDialog.setDefinition(newDefinition);
        String newDefinitionSource = randomAlphanumeric(5, 10);
        editAgencyIDListValueDialog.setDefinitionSource(newDefinitionSource);
        editAgencyIDListValueDialog.hitAddButton();

        editAgencyIDListPage.hitUpdateButton();
        editAgencyIDListPage.openPage();

        WebElement tr = editAgencyIDListPage.getTableRecordByValue(newValue);
        WebElement td = editAgencyIDListPage.getColumnByName(tr, "select");
        click(td);

        editAgencyIDListPage.hitRemoveAgencyIDListValueButton();
        editAgencyIDListPage.hitUpdateButton();
        editAgencyIDListPage.openPage();

        assertThrows(TimeoutException.class, () -> editAgencyIDListPage.getTableRecordByValue(newValue));
    }

    @Test
    @DisplayName("TC_34_4_6")
    public void TA_6() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

        AgencyIDListObject agencyIDList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(developer, namespace, release, "Published");
        List<AgencyIDListValueObject> agencyIDListValues = Arrays.asList(
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(developer, agencyIDList));

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(agencyIDList.getName(), release.getReleaseNumber());
        editAgencyIDListPage.revise();

        String newVersion = randomAlphanumeric(5, 10);
        editAgencyIDListPage.setVersion(newVersion);
        String newDefinition = randomPrint(50, 100).trim();
        editAgencyIDListPage.setDefinition(newDefinition);
        String newDefinitionSource = randomAlphanumeric(5, 10);
        editAgencyIDListPage.setDefinitionSource(newDefinitionSource);

        EditAgencyIDListValueDialog editAgencyIDListValueDialog = editAgencyIDListPage.addAgencyIDListValue();
        assertDisabled(editAgencyIDListValueDialog.getDeprecatedSelectField());

        String newValue = randomAlphanumeric(5, 10).trim();
        editAgencyIDListValueDialog.setValue(newValue);
        editAgencyIDListValueDialog.setMeaning(randomAlphanumeric(5, 10).trim());
        editAgencyIDListValueDialog.setDefinition(randomPrint(50, 100).trim());
        editAgencyIDListValueDialog.setDefinitionSource(randomAlphanumeric(5, 10));
        editAgencyIDListValueDialog.hitAddButton();

        editAgencyIDListPage.hitUpdateButton();
        editAgencyIDListPage.openPage();

        editAgencyIDListPage.cancel();
        editAgencyIDListPage.openPage();

        assertEquals("Agency ID List", getText(editAgencyIDListPage.getCoreComponentField()));
        assertEquals(release.getReleaseNumber(), getText(editAgencyIDListPage.getReleaseField()));
        assertEquals("Published", getText(editAgencyIDListPage.getStateField()));
        assertEquals("1", getText(editAgencyIDListPage.getRevisionField()));
        assertEquals(agencyIDList.getName(), getText(editAgencyIDListPage.getAgencyIDListNameField()));
        assertEquals(agencyIDList.getListId(), getText(editAgencyIDListPage.getListIDField()));
        assertEquals(agencyIDList.getVersionId(), getText(editAgencyIDListPage.getVersionField()));
        assertEquals(agencyIDList.getDefinition(), getText(editAgencyIDListPage.getDefinitionField()));
        assertEquals(agencyIDList.getDefinitionSource(), getText(editAgencyIDListPage.getDefinitionSourceField()));

        assertThrows(TimeoutException.class, () -> editAgencyIDListPage.getTableRecordByValue(newValue));
    }

}
