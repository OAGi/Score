package org.oagi.score.e2e.TS_37_ReleaseBranchAgencyIDListManagementEndUser;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.AssertionHelper.assertEnabled;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_37_3_EditingBrandNewEndUserAgencyIDList extends BaseTest {

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
    @DisplayName("TC_37_3_1_a")
    public void TA_1_a() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);
        AppUserObject anotherEndUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(anotherEndUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(anotherEndUser, library);

        AgencyIDListObject wipAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(anotherEndUser, namespace, release, "WIP");
        AgencyIDListValueObject agencyIDListValue =
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(anotherEndUser, wipAgencyIdList);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openNewAgencyIDList(endUser, release.getReleaseNumber());

        editAgencyIDListPage.setListID(wipAgencyIdList.getListId());
        editAgencyIDListPage.setVersion(wipAgencyIdList.getVersionId());
        editAgencyIDListPage.setNamespace(namespace);
        String definition = RandomStringUtils.secure().nextPrint(50, 100).trim();
        String definitionSource = RandomStringUtils.secure().nextAlphanumeric(5, 10);
        editAgencyIDListPage.setDefinition(definition);
        editAgencyIDListPage.setDefinitionSource(definitionSource);
        EditAgencyIDListValueDialog editAgencyIDListValueDialog = editAgencyIDListPage.addAgencyIDListValue();
        editAgencyIDListValueDialog.setValue(agencyIDListValue.getValue());
        editAgencyIDListValueDialog.setMeaning(agencyIDListValue.getName());
        editAgencyIDListValueDialog.setDefinition(agencyIDListValue.getDefinition());
        editAgencyIDListValueDialog.setDefinitionSource(agencyIDListValue.getDefinitionSource());
        editAgencyIDListValueDialog.hitAddButton();
        editAgencyIDListPage.hitUpdateButton();

        String xpathExpr = "//score-confirm-dialog//p";
        WebElement dialogMessage = retry(() -> visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertEquals("Another agency ID list with the triplet (ListID, AgencyID, Version) already exist!", getText(dialogMessage));
    }

    @Test
    @DisplayName("TC_37_3_1_b")
    public void TA_1_b() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        AgencyIDListObject agencyIDListObject =
                getAPIFactory().getAgencyIDListAPI().getAgencyIDListByNameAndBranchAndState(
                        "clm63055D16B_AgencyIdentification", release.getReleaseNumber(), "Published");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage = viewEditAgencyIDListPage.openEditAgencyIDListPage(agencyIDListObject);
        editAgencyIDListPage.hitDeriveAgencyIDListButton();

        assertDisabled(editAgencyIDListPage.getBasedAgencyIDListField());
        assertEquals("clm63055D16B_AgencyIdentification", getText(editAgencyIDListPage.getBasedAgencyIDListField()));
    }

    @Test
    @DisplayName("TC_37_3_1_c")
    public void TA_1_c() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage = viewEditAgencyIDListPage.openNewAgencyIDList(endUser, release.getReleaseNumber());
        assertDisabled(editAgencyIDListPage.getDeprecatedCheckbox());

        // All other parts are tested by TC_37_2_2
    }

    @Test
    @DisplayName("TC_37_3_1_d")
    public void TA_1_d() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openNewAgencyIDList(endUser, release.getReleaseNumber());

        String listId = Integer.toString(RandomUtils.secure().randomInt(5, 10));
        String versionId = RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String name = "clm" + listId + versionId + "_AgencyIdentification";

        editAgencyIDListPage.setName(name);
        editAgencyIDListPage.setListID(listId);
        editAgencyIDListPage.setVersion(versionId);
        editAgencyIDListPage.setNamespace(namespace);
        editAgencyIDListPage.hitUpdateButton();

        String xpathExpr = "//score-confirm-dialog//p";
        WebElement dialogMessage = retry(() -> visibilityOfElementLocated(getDriver(), By.xpath(xpathExpr)));
        assertEquals("Are you sure you want to update this without definitions?", getText(dialogMessage));
    }

    @Test
    @DisplayName("TC_37_3_2")
    public void TA_2() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);

        AgencyIDListObject agencyIDListObject =
                getAPIFactory().getAgencyIDListAPI().getAgencyIDListByNameAndBranchAndState(
                        "clm63055D16B_AgencyIdentification", release.getReleaseNumber(), "Published");
        List<AgencyIDListValueObject> agencyIDListValueList =
                getAPIFactory().getAgencyIDListValueAPI().getAgencyIDListValueByAgencyListID(agencyIDListObject);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage = viewEditAgencyIDListPage.openEditAgencyIDListPage(agencyIDListObject);
        editAgencyIDListPage.hitDeriveAgencyIDListButton();

        EditAgencyIDListValueDialog editAgencyIDListValueDialog = editAgencyIDListPage.addAgencyIDListValue();
        AgencyIDListValueObject agencyIDListValue = agencyIDListValueList.get(0);
        assertDisabled(editAgencyIDListValueDialog.getDeprecatedSelectField());
        editAgencyIDListValueDialog.setValue(agencyIDListValue.getValue());
        editAgencyIDListValueDialog.setMeaning(agencyIDListValue.getName());
        editAgencyIDListValueDialog.setDefinition(agencyIDListValue.getDefinition());
        editAgencyIDListValueDialog.setDefinitionSource(agencyIDListValue.getDefinitionSource());
        editAgencyIDListValueDialog.hitAddButton();

        assertEquals(agencyIDListValue.getValue() + " already exist", getSnackBarMessage(getDriver()));
        assertDisabled(editAgencyIDListPage.getUpdateButton(false));
    }

    @Test
    @DisplayName("TC_37_3_3")
    public void TA_3() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

        AgencyIDListObject agencyIDListObject =
                getAPIFactory().getAgencyIDListAPI().getAgencyIDListByNameAndBranchAndState(
                        "clm63055D16B_AgencyIdentification", release.getReleaseNumber(), "Published");
        List<AgencyIDListValueObject> agencyIDListValueList =
                getAPIFactory().getAgencyIDListValueAPI().getAgencyIDListValueByAgencyListID(agencyIDListObject);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage = viewEditAgencyIDListPage.openEditAgencyIDListPage(agencyIDListObject);
        editAgencyIDListPage.hitDeriveAgencyIDListButton();

        String listId = Integer.toString(RandomUtils.secure().randomInt(5, 10));
        String versionId = RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String name = "clm" + listId + versionId + "_AgencyIdentification";
        String definition = RandomStringUtils.secure().nextPrint(50, 100).trim();
        String definitionSource = RandomStringUtils.secure().nextAlphanumeric(5, 10);

        editAgencyIDListPage.setName(name);
        editAgencyIDListPage.setListID(listId);
        editAgencyIDListPage.setVersion(versionId);
        editAgencyIDListPage.setNamespace(namespace);
        editAgencyIDListPage.setDefinition(definition);
        editAgencyIDListPage.setDefinitionSource(definitionSource);

        AgencyIDListValueObject agencyIDListValue = agencyIDListValueList.get(0);
        WebElement tr = editAgencyIDListPage.getTableRecordByValue(agencyIDListValue.getValue());
        WebElement td = editAgencyIDListPage.getColumnByName(tr, "select");
        click(td);
        editAgencyIDListPage.hitRemoveAgencyIDListValueButton();
        editAgencyIDListPage.hitUpdateButton();

        viewEditAgencyIDListPage.openPage();
        editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(name, release.getReleaseNumber());

        EditAgencyIDListValueDialog editAgencyIDListValueDialog = editAgencyIDListPage.openAgencyIDListValueDialogByValue(agencyIDListValue.getValue());
        assertNotEquals(agencyIDListValue.getValue(), getText(editAgencyIDListValueDialog.getValueField()));
    }

    @Test
    @DisplayName("TC_37_3_4")
    public void TA_4() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

        AgencyIDListObject agencyIDListObject =
                getAPIFactory().getAgencyIDListAPI().getAgencyIDListByNameAndBranchAndState(
                        "clm63055D16B_AgencyIdentification", release.getReleaseNumber(), "Published");
        List<AgencyIDListValueObject> agencyIDListValueList =
                getAPIFactory().getAgencyIDListValueAPI().getAgencyIDListValueByAgencyListID(agencyIDListObject);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage = viewEditAgencyIDListPage.openEditAgencyIDListPage(agencyIDListObject);
        editAgencyIDListPage.hitDeriveAgencyIDListButton();

        String listId = Integer.toString(RandomUtils.secure().randomInt(5, 10));
        String versionId = RandomStringUtils.secure().nextAlphanumeric(5, 10);
        String name = "clm" + listId + versionId + "_AgencyIdentification";
        String definition = RandomStringUtils.secure().nextPrint(50, 100).trim();
        String definitionSource = RandomStringUtils.secure().nextAlphanumeric(5, 10);

        editAgencyIDListPage.setName(name);
        editAgencyIDListPage.setListID(listId);
        editAgencyIDListPage.setVersion(versionId);
        editAgencyIDListPage.setNamespace(namespace);
        editAgencyIDListPage.setDefinition(definition);
        editAgencyIDListPage.setDefinitionSource(definitionSource);

        AgencyIDListValueObject agencyIDListValue = agencyIDListValueList.get(0);
        EditAgencyIDListValueDialog editAgencyIDListValueDialog = editAgencyIDListPage.openAgencyIDListValueDialogByValue(agencyIDListValue.getValue());

        String meaning = RandomStringUtils.secure().nextAlphanumeric(5, 10).trim();
        definition = RandomStringUtils.secure().nextPrint(50, 100).trim();
        definitionSource = RandomStringUtils.secure().nextAlphanumeric(5, 10);

        assertDisabled(editAgencyIDListValueDialog.getValueField());
        editAgencyIDListValueDialog.setMeaning(meaning);
        editAgencyIDListValueDialog.setDefinition(definition);
        editAgencyIDListValueDialog.setDefinitionSource(definitionSource);
        editAgencyIDListValueDialog.hitSaveButton();
        editAgencyIDListPage.hitUpdateButton();

        viewEditAgencyIDListPage.openPage();
        editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(name, release.getReleaseNumber());

        editAgencyIDListValueDialog = editAgencyIDListPage.openAgencyIDListValueDialogByValue(agencyIDListValue.getValue());

        assertEquals(agencyIDListValue.getValue(), getText(editAgencyIDListValueDialog.getValueField()));
        assertEquals(meaning, getText(editAgencyIDListValueDialog.getMeaningField()));
        assertEquals(definition, getText(editAgencyIDListValueDialog.getDefinitionField()));
        assertEquals(definitionSource, getText(editAgencyIDListValueDialog.getDefinitionSourceField()));
    }

    @Test
    @DisplayName("TC_37_3_5")
    public void TA_5() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openNewAgencyIDList(endUser, release.getReleaseNumber());

        EditAgencyIDListValueDialog editAgencyIDListValueDialog = editAgencyIDListPage.addAgencyIDListValue();
        String value = RandomStringUtils.secure().nextAlphanumeric(5, 10).trim();
        String meaning = RandomStringUtils.secure().nextAlphanumeric(5, 10).trim();
        editAgencyIDListValueDialog.setValue(value);
        editAgencyIDListValueDialog.setMeaning(meaning);
        editAgencyIDListValueDialog.hitAddButton();

        WebElement tr = editAgencyIDListPage.getTableRecordByValue(value);
        assertEquals(meaning, getText(editAgencyIDListPage.getColumnByName(tr, "name")));
        assertTrue(StringUtils.isEmpty(getText(editAgencyIDListPage.getColumnByName(tr, "definition"))));
        assertTrue(StringUtils.isEmpty(getText(editAgencyIDListPage.getColumnByName(tr, "definitionSource"))));
    }

    @Test
    @DisplayName("TC_37_3_6")
    public void TA_6() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

        AgencyIDListObject wipAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(endUser, namespace, release, "WIP");
        AgencyIDListValueObject agencyIDListValue1 =
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(endUser, wipAgencyIdList);
        AgencyIDListValueObject agencyIDListValue2 =
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(endUser, wipAgencyIdList);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(wipAgencyIdList.getName(), release.getReleaseNumber());
        editAgencyIDListPage.setAgencyIDListValue(agencyIDListValue2);
        editAgencyIDListPage.hitUpdateButton();
        editAgencyIDListPage.openPage();

        // New agency ID list value without any references can be discarded.
        WebElement tr = editAgencyIDListPage.getTableRecordByValue(agencyIDListValue1.getValue());
        WebElement td = editAgencyIDListPage.getColumnByName(tr, "select");
        click(td);
        editAgencyIDListPage.hitRemoveAgencyIDListValueButton();
        editAgencyIDListPage.hitUpdateButton();
        editAgencyIDListPage.openPage();
        assertThrows(TimeoutException.class, () -> editAgencyIDListPage.getTableRecordByValue(agencyIDListValue1.getValue()));

        // New agency ID list value with any references cannot be discarded.
        WebElement tr2 = editAgencyIDListPage.getTableRecordByValue(agencyIDListValue2.getValue());
        WebElement td2 = editAgencyIDListPage.getColumnByName(tr2, "select");
        assertThrows(ElementNotInteractableException.class, () -> assertDisabled(td2));
    }

    @Test
    @DisplayName("TC_37_3_7")
    public void TA_7() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

        AgencyIDListObject wipAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(endUser, namespace, release, "WIP");
        AgencyIDListValueObject agencyIDListValue =
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(endUser, wipAgencyIdList);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(wipAgencyIdList.getName(), release.getReleaseNumber());

        EditAgencyIDListValueDialog editAgencyIDListValueDialog =
                editAgencyIDListPage.openAgencyIDListValueDialogByValue(agencyIDListValue.getValue());
        assertEnabled(editAgencyIDListValueDialog.getValueField());
        assertEnabled(editAgencyIDListValueDialog.getMeaningField());
        assertEnabled(editAgencyIDListValueDialog.getDefinitionField());
        assertEnabled(editAgencyIDListValueDialog.getDefinitionSourceField());
        assertDisabled(editAgencyIDListPage.getDeprecatedCheckbox());
    }

    @Test
    @DisplayName("TC_37_3_8_a")
    public void TA_8_a() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

        AgencyIDListObject wipAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(endUser, namespace, release, "WIP");
        AgencyIDListValueObject agencyIDListValue =
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(endUser, wipAgencyIdList);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(wipAgencyIdList.getName(), release.getReleaseNumber());
        editAgencyIDListPage.setAgencyIDListValue(agencyIDListValue);
        editAgencyIDListPage.hitUpdateButton();
        editAgencyIDListPage.openPage();

        assertEquals(agencyIDListValue.getName() + " (" + agencyIDListValue.getValue() + ")",
                getText(editAgencyIDListPage.getAgencyIDListValueSelectField()));
    }

    @Test
    @DisplayName("TC_37_3_8_b")
    public void TA_8_b() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getTheLatestRelease(library);
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);

        AgencyIDListObject wipAgencyIdList =
                getAPIFactory().getAgencyIDListAPI().createRandomAgencyIDList(endUser, namespace, release, "WIP");
        AgencyIDListValueObject agencyIDListValue =
                getAPIFactory().getAgencyIDListValueAPI().createRandomAgencyIDListValue(endUser, wipAgencyIdList);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage =
                viewEditAgencyIDListPage.openEditAgencyIDListPageByNameAndBranch(wipAgencyIdList.getName(), release.getReleaseNumber());
        editAgencyIDListPage.setAgencyIDListValue(agencyIDListValue);
        editAgencyIDListPage.hitUpdateButton();
        editAgencyIDListPage.openPage();

        // New agency ID list value with any references cannot be discarded.
        WebElement tr = editAgencyIDListPage.getTableRecordByValue(agencyIDListValue.getValue());
        WebElement td = editAgencyIDListPage.getColumnByName(tr, "select");
        assertThrows(ElementNotInteractableException.class, () -> assertDisabled(td));
    }

}
