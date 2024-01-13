package org.oagi.score.e2e.TS_17_ReleaseBranchCodeListManagementForEndUser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.bie.*;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.EditCodeListValueDialog;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.AssertionHelper.assertDisabled;
import static org.oagi.score.e2e.AssertionHelper.assertEnabled;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_17_4_AmendAnEndUserCodeList extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_17_4_TA_1")
    public void test_TA_1() {
        AppUserObject endUserA;
        ReleaseObject branch;
        List<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<CodeListObject, CodeListValueObject> codeListValueMap = new HashMap<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespaceEUA = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA);
            NamespaceObject namespaceEUB = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB);

            /**
             * Create Production end-user Code List for a particular release branch.
             */
            CodeListObject codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserB, namespaceEUB, branch, "Production");
            CodeListValueObject value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUserB);
            codeListForTesting.add(codeList);
            codeListValueMap.put(codeList, value);

            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserA, namespaceEUA, branch, "Production");
            value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUserA);
            codeListForTesting.add(codeList);
            codeListValueMap.put(codeList, value);
        }
        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());

        for (CodeListObject cl : codeListForTesting) {
            CodeListValueObject value = codeListValueMap.get(cl);
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(cl);
            int previousRevisionNumber = Integer.parseInt(getText(editCodeListPage.getRevisionField()));
            editCodeListPage.hitAmendButton();
            assertTrue(getText(editCodeListPage.getStateField()).equals("WIP"));
            assertEquals(previousRevisionNumber + 1, Integer.parseInt(getText(editCodeListPage.getRevisionField())));
            assertTrue(getText(editCodeListPage.getCodeListNameField()).equals(cl.getName()));
            assertTrue(getText(editCodeListPage.getDefinitionField()).equals(cl.getDefinition()));
            assertTrue(getText(editCodeListPage.getDefinitionSourceField()).equals(cl.getDefinitionSource()));
            assertTrue(getText(editCodeListPage.getListIDField()).equals(cl.getListId()));
            assertTrue(getText(editCodeListPage.getRemarkField()).equals(cl.getRemark()));
            boolean deprecated;
            if (editCodeListPage.getDeprecatedSelectField().isSelected()) {
                deprecated = true;
            } else {
                deprecated = false;
            }
            assertEquals(cl.isDeprecated(), deprecated);

            EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.editCodeListValue(value.getValue());
            assertTrue(getText(editCodeListValueDialog.getCodeField()).equals(value.getValue()));
            assertTrue(getText(editCodeListValueDialog.getMeaningField()).equals(value.getMeaning()));
            assertTrue(getText(editCodeListValueDialog.getDefinitionField()).equals(value.getDefinition()));
            assertTrue(getText(editCodeListValueDialog.getDefinitionSourceField()).equals(value.getDefinitionSource()));

            if (editCodeListValueDialog.getDeprecatedSelectField().isSelected()) {
                deprecated = true;
            } else {
                deprecated = false;
            }
            assertEquals(value.isDeprecated(), deprecated);
            escape(getDriver());
        }
    }

    @Test
    @DisplayName("TC_17_4_TA_2")
    public void test_TA_2() {
        AppUserObject endUserA;
        ReleaseObject branch;
        List<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<CodeListObject, CodeListValueObject> codeListValueMap = new HashMap<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            /**
             * Create Published developer Code List for a particular release branch.
             */
            CodeListObject codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(developer, namespace, branch, "Published");
            CodeListValueObject value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developer);
            codeListForTesting.add(codeList);
            codeListValueMap.put(codeList, value);
        }
        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());

        for (CodeListObject cl : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(cl);
            AppUserObject owner = getAPIFactory().getAppUserAPI().getAppUserByID(cl.getOwnerUserId());
            assertTrue(owner.isDeveloper());
            assertThrows(TimeoutException.class, () -> {
                editCodeListPage.hitAmendButton();
            });
        }
    }

    @Test
    @DisplayName("TC_17_4_TA_3")
    public void test_TA_3() {
        AppUserObject endUserA;
        ReleaseObject branch;
        List<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<CodeListObject, CodeListValueObject> codeListValueMap = new HashMap<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespaceEUA = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA);
            NamespaceObject namespaceEUB = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB);

            /**
             * Create Production end-user Code List for a particular release branch.
             */
            CodeListObject codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserB, namespaceEUB, branch, "Production");
            CodeListValueObject value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUserB);
            codeListForTesting.add(codeList);
            codeListValueMap.put(codeList, value);

            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserA, namespaceEUA, branch, "Production");
            codeList.setDeprecated(true);
            getAPIFactory().getCodeListAPI().updateCodeList(codeList);
            value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUserA);
            codeListForTesting.add(codeList);
            codeListValueMap.put(codeList, value);
        }
        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());

        for (CodeListObject cl : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(cl);
            int previousRevisionNumber = Integer.parseInt(getText(editCodeListPage.getRevisionField()));
            editCodeListPage.hitAmendButton();
            assertTrue(getText(editCodeListPage.getStateField()).equals("WIP"));
            assertEquals(previousRevisionNumber + 1, Integer.parseInt(getText(editCodeListPage.getRevisionField())));
            /**
             * Test Assertion #17.4.3.a
             */
            if (cl.isDeprecated()) {
                assertDisabled(editCodeListPage.getDeprecatedSelectField());
            } else {
                assertEnabled(editCodeListPage.getDeprecatedSelectField());
                editCodeListPage.toggleDeprecated();
            }
            /**
             * Test Assertion #17.4.3.b
             */
            assertDisabled(editCodeListPage.getNamespaceSelectField());
            assertDisabled(editCodeListPage.getListIDField());
            assertDisabled(editCodeListPage.getAgencyIDListField());
            String versionAfterAmendment = cl.getVersionId() + "_New";
            assertTrue(getText(editCodeListPage.getVersionField()).equals(versionAfterAmendment));
            assertEnabled(editCodeListPage.getVersionField());
            editCodeListPage.setVersion("something new");
            /**
             * Test Assertion #17.4.3.c
             */
            assertEnabled(editCodeListPage.getDefinitionField());
            editCodeListPage.setDefinition("new definition");
            assertEnabled(editCodeListPage.getDefinitionSourceField());
            editCodeListPage.setDefinitionSource("new definition source");
            assertEnabled(editCodeListPage.getRemarkField());
            editCodeListPage.setRemark("new remark");
            editCodeListPage.hitUpdateButton();
        }
    }

    @Test
    @DisplayName("TC_17_4_TA_4")
    public void test_TA_4() {
        AppUserObject endUserA;
        ReleaseObject branch;
        List<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<CodeListObject, CodeListValueObject> codeListValueMap = new HashMap<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespaceEUA = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA);
            NamespaceObject namespaceEUB = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB);

            /**
             * Create Production end-user Code List for a particular release branch.
             */
            CodeListObject codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserB, namespaceEUB, branch, "Production");
            CodeListValueObject value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUserB);
            codeListForTesting.add(codeList);
            codeListValueMap.put(codeList, value);

            codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserA, namespaceEUA, branch, "Production");
            value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUserA);
            value.setDeprecated(true);
            getAPIFactory().getCodeListValueAPI().updateCodeListValue(value);
            codeListForTesting.add(codeList);
            codeListValueMap.put(codeList, value);
        }
        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());

        for (CodeListObject cl : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(cl);
            waitFor(ofSeconds(1L));
            int previousRevisionNumber = Integer.parseInt(getText(editCodeListPage.getRevisionField()));
            editCodeListPage.hitAmendButton();
            assertTrue(getText(editCodeListPage.getStateField()).equals("WIP"));
            assertEquals(previousRevisionNumber + 1, Integer.parseInt(getText(editCodeListPage.getRevisionField())));

            CodeListValueObject value = codeListValueMap.get(cl);
            assertThrows(Exception.class, () -> editCodeListPage.selectCodeListValue(value.getValue()));
            assertThrows(Exception.class, () -> editCodeListPage.removeCodeListValue());
            EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.editCodeListValue(value.getValue());
            editCodeListValueDialog.setMeaning("new meaning for value");
            editCodeListValueDialog.setDefinition("new definition for value");
            editCodeListValueDialog.setDefinitionSource("new definition source for value");
            boolean previousDeprecatedStatusForValue = value.isDeprecated();
            if (previousDeprecatedStatusForValue == true) {
                assertDisabled(editCodeListValueDialog.getDeprecatedSelectField());
            } else {
                assertEnabled(editCodeListValueDialog.getDeprecatedSelectField());
                editCodeListValueDialog.toggleDeprecated();
            }
            editCodeListValueDialog.hitSaveButton();
            editCodeListPage.hitUpdateButton();
        }
    }

    @Test
    @DisplayName("TC_17_4_TA_5")
    public void test_TA_5() {
        AppUserObject endUserA;
        ReleaseObject branch;
        List<CodeListObject> codeListForTesting = new ArrayList<>();
        List<CodeListValueObject> values = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespaceEUB = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB);

            /**
             * Create Production end-user Code List for a particular release branch.
             */
            CodeListObject baseCodeList = getAPIFactory().getCodeListAPI().
                    getCodeListByCodeListNameAndReleaseNum("oacl_ResponseCode", branch.getReleaseNumber());
            values = getAPIFactory().getCodeListValueAPI().getCodeListValuesByCodeListManifestId(baseCodeList.getCodeListManifestId());

            CodeListObject codeList = getAPIFactory().getCodeListAPI().
                    createDerivedCodeList(baseCodeList, endUserB, namespaceEUB, branch, "Production");
            codeListForTesting.add(codeList);
        }
        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());

        for (CodeListObject cl : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(cl);
            int previousRevisionNumber = Integer.parseInt(getText(editCodeListPage.getRevisionField()));
            editCodeListPage.hitAmendButton();
            assertTrue(getText(editCodeListPage.getStateField()).equals("WIP"));
            assertEquals(previousRevisionNumber + 1, Integer.parseInt(getText(editCodeListPage.getRevisionField())));

            CodeListValueObject value = values.get(0);
            assertThrows(Exception.class, () -> editCodeListPage.selectCodeListValue(value.getValue()));
            assertThrows(Exception.class, () -> editCodeListPage.removeCodeListValue());
            EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.editCodeListValue(value.getValue());
            editCodeListValueDialog.setMeaning("new meaning for value");
            editCodeListValueDialog.setDefinition("new definition for value");
            editCodeListValueDialog.setDefinitionSource("new definition source for value");
            boolean previousDeprecatedStatusForValue = value.isDeprecated();
            if (previousDeprecatedStatusForValue == true) {
                assertDisabled(editCodeListValueDialog.getDeprecatedSelectField());
            } else {
                assertEnabled(editCodeListValueDialog.getDeprecatedSelectField());
                editCodeListValueDialog.toggleDeprecated();
            }
            editCodeListValueDialog.hitSaveButton();
            editCodeListPage.hitUpdateButton();
        }
    }

    @Test
    @DisplayName("TC_17_4_TA_6")
    public void test_TA_6() {
        AppUserObject endUserA;
        ReleaseObject branch;
        List<CodeListObject> codeListForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespaceEUB = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB);

            /**
             * Create Production end-user Code List for a particular release branch.
             */
            CodeListObject codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserB, namespaceEUB, branch, "Production");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUserB);
            codeListForTesting.add(codeList);
        }
        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());

        for (CodeListObject cl : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(cl);
            int previousRevisionNumber = Integer.parseInt(getText(editCodeListPage.getRevisionField()));
            editCodeListPage.hitAmendButton();
            assertTrue(getText(editCodeListPage.getStateField()).equals("WIP"));
            assertEquals(previousRevisionNumber + 1, Integer.parseInt(getText(editCodeListPage.getRevisionField())));
            EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
            String newValueCode = "new value code";
            editCodeListValueDialog.setCode(newValueCode);
            editCodeListValueDialog.setMeaning("new value meaning");
            editCodeListValueDialog.hitAddButton();
            editCodeListValueDialog = editCodeListPage.editCodeListValue(newValueCode);
            editCodeListValueDialog.setMeaning("changed meaning");
            editCodeListValueDialog.setDefinition("added definition");
            editCodeListValueDialog.setDefinitionSource("added definition source");
            editCodeListValueDialog.hitSaveButton();
            editCodeListPage.hitUpdateButton();
        }
    }

    @Test
    @DisplayName("TC_17_4_TA_7")
    public void test_TA_7() {
        AppUserObject endUserA;
        ReleaseObject branch;
        List<CodeListObject> codeListForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespaceEUB = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB);

            /**
             * Create Production end-user Code List for a particular release branch.
             */
            CodeListObject codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserB, namespaceEUB, branch, "Production");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUserB);
            codeListForTesting.add(codeList);
        }
        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());

        for (CodeListObject cl : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(cl);
            int previousRevisionNumber = Integer.parseInt(getText(editCodeListPage.getRevisionField()));
            editCodeListPage.hitAmendButton();
            assertTrue(getText(editCodeListPage.getStateField()).equals("WIP"));
            assertEquals(previousRevisionNumber + 1, Integer.parseInt(getText(editCodeListPage.getRevisionField())));
            EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
            String newValueCode = "new value code";
            editCodeListValueDialog.setCode(newValueCode);
            editCodeListValueDialog.setMeaning("new value meaning");
            editCodeListValueDialog.hitAddButton();
            editCodeListPage.selectCodeListValue(newValueCode);
            editCodeListPage.removeCodeListValue();
        }
    }

    @Test
    @DisplayName("TC_17_4_TA_8")
    public void test_TA_8() {
        AppUserObject endUserA;
        ReleaseObject branch;
        List<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<CodeListObject, CodeListValueObject> codeListValueMap = new HashMap<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            NamespaceObject namespaceEUB = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB);

            /**
             * Create Production end-user Code List for a particular release branch.
             */
            CodeListObject codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserB, namespaceEUB, branch, "Production");
            CodeListValueObject value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUserB);
            codeListForTesting.add(codeList);
            codeListValueMap.put(codeList, value);
        }
        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());

        for (CodeListObject cl : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(cl);
            int previousRevisionNumber = Integer.parseInt(getText(editCodeListPage.getRevisionField()));
            editCodeListPage.hitAmendButton();
            assertTrue(getText(editCodeListPage.getStateField()).equals("WIP"));
            assertEquals(previousRevisionNumber + 1, Integer.parseInt(getText(editCodeListPage.getRevisionField())));
            editCodeListPage.setDefinition("new definition");
            editCodeListPage.setDefinitionSource("new definition source");
            editCodeListPage.setVersion("new version");
            EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
            String newValueCode = "new value code";
            editCodeListValueDialog.setCode(newValueCode);
            editCodeListValueDialog.setMeaning("new value meaning");
            editCodeListValueDialog.hitAddButton();
            editCodeListPage.hitUpdateButton();

            editCodeListPage.hitCancelButton();
            assertEquals(cl.getState(), getText(editCodeListPage.getStateField()));
            assertEquals(previousRevisionNumber, Integer.valueOf(getText(editCodeListPage.getRevisionField())));
            assertEquals(cl.getVersionId(), getText(editCodeListPage.getVersionField()));
            assertEquals(cl.getDefinition(), getText(editCodeListPage.getDefinitionField()));
            assertEquals(cl.getDefinitionSource(), getText(editCodeListPage.getDefinitionSourceField()));
            CodeListValueObject oldValue = codeListValueMap.get(cl);
            assertDoesNotThrow(() -> editCodeListPage.valueExists(oldValue.getValue()));
            assertThrows(TimeoutException.class, () -> {
                editCodeListPage.valueExists(newValueCode);
            });
        }
    }

    @Test
    @DisplayName("TC_17_4_TA_9")
    public void test_TA_9() {
        AppUserObject endUserA;
        AppUserObject developer;
        ReleaseObject branch;
        NamespaceObject namespace;
        List<CodeListObject> codeListForTesting = new ArrayList<>();
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            developer = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(developer);

            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("10.8.5");
            namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            NamespaceObject namespaceEUB = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB);

            /**
             * Create Production end-user Code List for a particular release branch.
             */
            CodeListObject codeList = getAPIFactory().getCodeListAPI().
                    createRandomCodeList(endUserB, namespaceEUB, branch, "Production");
            getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUserB);
            codeListForTesting.add(codeList);

            CodeListObject baseCodeList = getAPIFactory().getCodeListAPI().
                    getCodeListByCodeListNameAndReleaseNum("oacl_ReasonCode", branch.getReleaseNumber());

            codeList = getAPIFactory().getCodeListAPI().
                    createDerivedCodeList(baseCodeList, endUserB, namespaceEUB, branch, "Production");
            codeListForTesting.add(codeList);
        }
        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());

        for (CodeListObject cl : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(cl);
            int previousRevisionNumber = Integer.parseInt(getText(editCodeListPage.getRevisionField()));
            editCodeListPage.hitAmendButton();
            assertTrue(getText(editCodeListPage.getStateField()).equals("WIP"));
            assertEquals(previousRevisionNumber + 1, Integer.parseInt(getText(editCodeListPage.getRevisionField())));
            EditCodeListValueDialog editCodeListValueDialog = editCodeListPage.addCodeListValue();
            String newValueCode = "new value code";
            editCodeListValueDialog.setCode(newValueCode);
            editCodeListValueDialog.setMeaning("new value meaning");
            editCodeListValueDialog.hitAddButton();
            editCodeListPage.hitUpdateButton();
            if (cl.getDefinition() == null) {
                editCodeListPage.hitUpdateAnywayButton();
            }
            /**
             * Prepare Core Components and Business Context
             */
            ACCObject acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developer, branch, namespace, "Published");
            DTObject dataType = getAPIFactory().getCoreComponentAPI().getBDTByGuidAndReleaseNum("dd0c8f86b160428da3a82d2866a5b48d", branch.getReleaseNumber());
            BCCPObject bccp = getAPIFactory().getCoreComponentAPI().createRandomBCCP(dataType, developer, namespace, "Published");
            getAPIFactory().getCoreComponentAPI().appendBCC(acc, bccp, "Published");
            ASCCPObject asccp = getAPIFactory().getCoreComponentAPI().createRandomASCCP(acc, developer, namespace, "Published");
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUserA);

            ViewEditBIEPage viewEditBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu();
            CreateBIEForSelectBusinessContextsPage createBIEForSelectBusinessContextsPage = viewEditBIEPage.openCreateBIEPage();
            CreateBIEForSelectTopLevelConceptPage selectTopLevelConceptPage = createBIEForSelectBusinessContextsPage.next(List.of(context));
            EditBIEPage editBIEPage = selectTopLevelConceptPage.createBIE(asccp.getDen(), branch.getReleaseNumber());
            WebElement node = editBIEPage.getNodeByPath("/" + asccp.getPropertyTerm() + "/" + bccp.getPropertyTerm());
            assertTrue(node.isDisplayed());
            EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(node);
            waitFor(Duration.ofMillis(2000));
            bbiePanel.toggleUsed();
            bbiePanel.setValueDomainRestriction("Code");
            bbiePanel.setValueDomain(cl.getName());
            editBIEPage.hitUpdateButton();

            ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();

            TopLevelASBIEPObject topLevelAsbiep = getAPIFactory().getBusinessInformationEntityAPI().getTopLevelASBIEPByDENAndReleaseNum(asccp.getDen(), branch.getReleaseNumber());
            expressBIEPage.selectBIEForExpression(topLevelAsbiep);
            expressBIEPage.selectJSONSchemaExpression();
            File file = null;
            try {
                file = expressBIEPage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.JSON);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(file);
                JsonNode rootNode = root.path("definitions");
                List<JsonNode> values = rootNode.findValues("enum");
                JsonNode value = values.get(0);
                ArrayList<String> jsonValues = new ArrayList<>();
                for (int i = 0; i < value.size(); i++) {
                    jsonValues.add(value.get(i).asText());
                }
                List<CodeListValueObject> codeListValues = getAPIFactory().getCodeListValueAPI().getCodeListValuesByCodeListManifestId(cl.getCodeListManifestId());
                for (CodeListValueObject codeListalue : codeListValues) {
                    assertTrue(jsonValues.contains(codeListalue.getValue()));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (file != null) {
                    file.delete();
                }
            }

        }
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
