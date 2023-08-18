package org.oagi.score.e2e.TS_17_ReleaseBranchCodeListManagementForEndUser;

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
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.EditCodeListValueDialog;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;

import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.AssertionHelper.assertNotChecked;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_17_3_EditingABrandNewEndUserCodeList extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_17_3_TA_1")
    public void test_TA_1() {
        AppUserObject endUser;
        ReleaseObject branch;
        CodeListObject codeList;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

            /**
             * Create WIP end-user Code List for a particular release branch.
             */
            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUser, namespaceEU, branch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        /**
         * Test Assertion #11.3.1.a
         */
        editCodeListPage.setName("new name");
        editCodeListPage.setVersion("new version");
        editCodeListPage.hitUpdateButton();
        String agencyIDList = getText(editCodeListPage.getAgencyIDListField());
        assertTrue(getAPIFactory().getCodeListAPI().checkCodeListUniqueness(codeList, agencyIDList));
        /**
         * Test Assertion #11.3.1.b
         * Note: For developer Based Code list is not visible on the UI
         */
        assertTrue(codeList.getBasedCodeListManifestId() == null);
        /**
         * Test Assertion #11.3.1.c
         */
        assertEquals("true", editCodeListPage.getCodeListNameField().getAttribute("aria-required"));
        assertEquals("true", editCodeListPage.getAgencyIDListField().getAttribute("aria-required"));
        assertEquals("true", editCodeListPage.getVersionField().getAttribute("aria-required"));
        assertEquals("true", editCodeListPage.getNamespaceSelectField().getAttribute("aria-required"));
        assertDisabled(editCodeListPage.getDeprecatedSelectField());
        assertNotChecked(editCodeListPage.getDeprecatedSelectField());
        ArrayList<NamespaceObject> standardNamespaces = getAPIFactory().getNamespaceAPI().getStandardNamespacesURIs();
        for (NamespaceObject namespace : standardNamespaces) {
            assertThrows(Exception.class, () -> {
                editCodeListPage.setNamespace(namespace);
            });
        }
        escape(getDriver());
        /**
         * Test Assertion #11.3.1.d
         */
        editCodeListPage.setDefinition("");
        editCodeListPage.hitUpdateButton();
        assertEquals("Are you sure you want to update this without definitions?",
                editCodeListPage.getDefinitionWarningDialogMessage());
        editCodeListPage.hitUpdateAnywayButton();
    }

    @Test
    @DisplayName("TC_17_3_TA_2")
    public void test_TA_2() {
        AppUserObject endUser;
        ReleaseObject branch;
        CodeListObject codeList;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

            /**
             * Create WIP end-user Code List for a particular release branch.
             */
            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUser, namespaceEU, branch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
        editCodeListValueDialog.setCode("new value code");
        editCodeListValueDialog.setMeaning("new value meaning");
        editCodeListValueDialog.setDefinition("new value definition");
        editCodeListValueDialog.setDefinitionSource("new value definition source");
        assertDisabled(editCodeListValueDialog.getDeprecatedSelectField());
        assertNotChecked(editCodeListValueDialog.getDeprecatedSelectField());
        editCodeListValueDialog.hitAddButton();

        editCodeListValueDialog = editCodeListPage.addCodeListValue();
        editCodeListValueDialog.setCode("new value code");
        editCodeListValueDialog.setMeaning("new value meaning");
        editCodeListValueDialog.setDefinition("new value definition");
        editCodeListValueDialog.setDefinitionSource("new value definition source");
        assertDisabled(editCodeListValueDialog.getDeprecatedSelectField());
        assertNotChecked(editCodeListValueDialog.getDeprecatedSelectField());
        String enteredValue = getText(editCodeListValueDialog.getCodeField());
        editCodeListValueDialog.hitAddButton();
        String message = enteredValue + " already exist";
        assert message.equals(getSnackBarMessage(getDriver()));
    }

    @Test
    @DisplayName("TC_17_3_TA_3")
    public void test_TA_3() {
        AppUserObject endUser;
        ReleaseObject branch;
        CodeListObject codeList;
        List<CodeListValueObject> values;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

            /**
             * Create derived WIP end-user Code List for a particular release branch.
             */
            CodeListObject baseCodeList = getAPIFactory().getCodeListAPI().
                    getCodeListByCodeListNameAndReleaseNum("oacl_ResponseCode", branch.getReleaseNumber());

            codeList = getAPIFactory().getCodeListAPI().
                    createDerivedCodeList(baseCodeList, endUser, namespaceEU, branch, "WIP");
            values = getAPIFactory().getCodeListValueAPI().getCodeListValuesByCodeListManifestId(codeList.getCodeListManifestId());
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        editCodeListPage.selectCodeListValue(values.get(1).getValue());
        editCodeListPage.removeCodeListValue();
        editCodeListPage.hitUpdateButton();
    }

    @Test
    @DisplayName("TC_17_3_TA_4")
    public void test_TA_4() {
        AppUserObject endUser;
        ReleaseObject branch;
        CodeListObject codeList;
        List<CodeListValueObject> values;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

            /**
             * Create derived WIP end-user Code List for a particular release branch.
             */
            CodeListObject baseCodeList = getAPIFactory().getCodeListAPI().
                    getCodeListByCodeListNameAndReleaseNum("oacl_ResponseCode", branch.getReleaseNumber());

            codeList = getAPIFactory().getCodeListAPI().
                    createDerivedCodeList(baseCodeList, endUser, namespaceEU, branch, "WIP");
            values = getAPIFactory().getCodeListValueAPI().getCodeListValuesByCodeListManifestId(codeList.getCodeListManifestId());

        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.editCodeListValue(values.get(1).getValue());
        editCodeListValueDialog.setMeaning("changed meaning");
        editCodeListValueDialog.setDefinition("changed definition");
        editCodeListValueDialog.setDefinitionSource("changed definition source");
        editCodeListValueDialog.hitSaveButton();
        editCodeListPage.hitUpdateButton();
    }

    @Test
    @DisplayName("TC_17_3_TA_5")
    public void test_TA_5() {
        AppUserObject endUser;
        ReleaseObject branch;
        CodeListObject codeList;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

            /**
             * Create derived WIP end-user Code List for a particular release branch.
             */
            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUser, namespaceEU, branch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
        assertEquals("true", editCodeListValueDialog.getCodeField().getAttribute("aria-required"));
        assertEquals("true", editCodeListValueDialog.getMeaningField().getAttribute("aria-required"));
        assertEquals("false", editCodeListValueDialog.getDefinitionSourceField().getAttribute("aria-required"));
        assertEquals("false", editCodeListValueDialog.getDefinitionField().getAttribute("aria-required"));
        editCodeListValueDialog.setCode("new value code");
        editCodeListValueDialog.setMeaning("new value meaning");
        editCodeListValueDialog.hitAddButton();
        editCodeListPage.hitUpdateButton();
    }

    @Test
    @DisplayName("TC_17_3_TA_6")
    public void test_TA_6() {
        AppUserObject endUser;
        ReleaseObject branch;
        CodeListObject codeList;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

            /**
             * Create derived WIP end-user Code List for a particular release branch.
             */
            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUser, namespaceEU, branch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
        editCodeListValueDialog.setCode("new value code");
        editCodeListValueDialog.setMeaning("new value meaning");
        editCodeListValueDialog.hitAddButton();

        editCodeListPage.selectCodeListValue("new value code");
        editCodeListPage.removeCodeListValue();
    }

    @Test
    @DisplayName("TC_17_3_TA_7")
    public void test_TA_7() {
        AppUserObject endUser;
        ReleaseObject branch;
        CodeListObject codeList;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

            /**
             * Create derived WIP end-user Code List for a particular release branch.
             */
            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUser, namespaceEU, branch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
        editCodeListValueDialog.setCode("new value code");
        editCodeListValueDialog.setMeaning("new value meaning");
        editCodeListValueDialog.hitAddButton();

        editCodeListValueDialog = editCodeListPage.editCodeListValue("new value code");
        editCodeListValueDialog.setMeaning("changed meaning");
        editCodeListValueDialog.setDefinition("added definition");
        assertDisabled(editCodeListValueDialog.getDeprecatedSelectField());
        editCodeListValueDialog.hitSaveButton();
        editCodeListPage.hitUpdateButton();
    }

    @Test
    @DisplayName("TC_17_3_TA_8")
    public void test_TA_8() {
        AppUserObject endUser;
        ReleaseObject branch;
        CodeListObject codeList;
        NamespaceObject namespaceEU;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser);

            /**
             * Create derived WIP end-user Code List for a particular release branch.
             */
            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUser, namespaceEU, branch, "WIP");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu().openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage = viewEditAgencyIDListPage.openNewAgencyIDList(endUser, branch.getReleaseNumber());
        waitFor(ofMillis(1000L));
        editAgencyIDListPage.setName("TestAgencyIDList");
        editAgencyIDListPage.setNamespace(namespaceEU);
        editAgencyIDListPage.setDefinition("some definition");
        editAgencyIDListPage.setVersion("some version");

        EditAgencyIDListValueDialog editAgencyIDListValueDialog = editAgencyIDListPage.addAgencyIDListValue();
        editAgencyIDListValueDialog.setValue("value");
        editAgencyIDListValueDialog.setMeaning("value meaning");
        editAgencyIDListValueDialog.setDefinition("value definition");
        editAgencyIDListValueDialog.setDefinitionSource("value definition source");
        editAgencyIDListValueDialog.hitAddButton();
        editAgencyIDListPage.hitUpdateButton();
        editAgencyIDListPage.moveToQA();
        editAgencyIDListPage.moveToProduction();

        AgencyIDListObject agencyIDList = getAPIFactory().getAgencyIDListAPI().getNewlyCreatedAgencyIDList(endUser, branch.getReleaseNumber());
        List<AgencyIDListValueObject> agencyIDListValues = getAPIFactory().getAgencyIDListValueAPI().getAgencyIDListValueByAgencyListID(agencyIDList);
        AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(agencyIDList.getOwnerUserId());
        assertTrue(agencyIDList.getState().equals("Production"));
        assertFalse(owner.isDeveloper());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        editCodeListPage.setAgencyIDList(agencyIDList);
        editCodeListPage.setAgencyIDListValue(agencyIDListValues.get(0));
        editCodeListPage.hitUpdateButton();
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        // Delete random accounts
        this.randomAccounts.forEach(newUser -> {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId());
        });
    }
}
