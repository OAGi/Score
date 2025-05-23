package org.oagi.score.e2e.TS_33_CodeListUplifting;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.UpliftCodeListPage;
import org.openqa.selenium.WebDriverException;

import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_33_2_EndUserCodeListUplifting extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_33_2_TA_1")
    public void test_TA_1() {
        AppUserObject endUser;
        LibraryObject library;
        CodeListObject codeList;
        ReleaseObject release;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, namespace, release, "Published");

        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        UpliftCodeListPage upliftCodeListPage = homePage.getBIEMenu().openUpliftCodeListSubMenu();
        upliftCodeListPage.setSourceRelease(release.getReleaseNumber());
        ReleaseObject targetRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
        upliftCodeListPage.setTargetRelease(targetRelease.getReleaseNumber());
        upliftCodeListPage.selectCodeList(codeList.getName());

        /**
         * The target release must be greater than the source release.
         */
        List<String> releasesBefore = getAPIFactory().getReleaseAPI().getAllReleasesBeforeRelease(library, release);
        for (String earlierRelease : releasesBefore) {
            assertThrows(WebDriverException.class, () -> upliftCodeListPage.setTargetRelease(earlierRelease));
        }
        assertThrows(WebDriverException.class, () -> upliftCodeListPage.setTargetRelease(release.getReleaseNumber()));

        /**
         * The end user cannot select the Working branch.
         */
        escape(getDriver());
        ReleaseObject workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        assertThrows(WebDriverException.class, () -> upliftCodeListPage.setSourceRelease(workingBranch.getReleaseNumber()));
        assertThrows(WebDriverException.class, () -> upliftCodeListPage.setTargetRelease(workingBranch.getReleaseNumber()));
    }

    @Test
    @DisplayName("TC_33_2_TA_2")
    public void test_TA_2() {
        AppUserObject endUser;
        LibraryObject library;
        List<CodeListObject> codeListForTesting = new ArrayList<>();
        ReleaseObject release;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            CodeListObject codeListWIP = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, namespace, release, "WIP");
            codeListForTesting.add(codeListWIP);

            CodeListObject codeListQA = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, namespace, release, "QA");
            codeListForTesting.add(codeListQA);

            CodeListObject codeListProduction = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, namespace, release, "WIP");
            codeListForTesting.add(codeListProduction);

            CodeListObject codeListDeprecated = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, namespace, release, "WIP");
            codeListDeprecated.setDeprecated(true);
            getAPIFactory().getCodeListAPI().updateCodeList(codeListDeprecated);
            codeListForTesting.add(codeListDeprecated);

            CodeListObject codeListDeleted = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, namespace, release, "Deleted");
            codeListForTesting.add(codeListDeleted);

            CodeListObject baseCodeList = getAPIFactory().getCodeListAPI().getCodeListByCodeListNameAndReleaseNum("clm6DateFormatCode1_DateFormatCode", release.getReleaseNumber());
            CodeListObject codeListDerived = getAPIFactory().getCodeListAPI().createDerivedCodeList(baseCodeList, endUser, namespace, release, "WIP");
            codeListForTesting.add(codeListDerived);
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        UpliftCodeListPage upliftCodeListPage = homePage.getBIEMenu().openUpliftCodeListSubMenu();
        upliftCodeListPage.setSourceRelease(release.getReleaseNumber());
        ReleaseObject targetRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
        upliftCodeListPage.setTargetRelease(targetRelease.getReleaseNumber());
        for (CodeListObject cl : codeListForTesting) {
            assertDoesNotThrow(() -> upliftCodeListPage.selectCodeList(cl.getName()));
        }
    }

    @Test
    @DisplayName("TC_33_2_TA_3")
    public void test_TA_3() {
        AppUserObject endUser;
        LibraryObject library;
        CodeListObject codeList;
        CodeListValueObject value;
        ReleaseObject release;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, namespace, release, "WIP");
            value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        UpliftCodeListPage upliftCodeListPage = homePage.getBIEMenu().openUpliftCodeListSubMenu();
        ReleaseObject targetRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
        EditCodeListPage editCodeListPage = upliftCodeListPage.hitUpliftButton(codeList, release, targetRelease);
        assertEquals(codeList.getName(), getText(editCodeListPage.getCodeListNameField()));
        assertEquals(codeList.getListId(), getText(editCodeListPage.getListIDField()));
        assertEquals(codeList.getVersionId(), getText(editCodeListPage.getVersionField()));
        assertEquals(codeList.getRemark(), getText(editCodeListPage.getRemarkField()));
        assertEquals(codeList.getDefinition(), getText(editCodeListPage.getDefinitionField()));
        assertEquals(codeList.getDefinitionSource(), getText(editCodeListPage.getDefinitionSourceField()));
        assertEquals("1", getText(editCodeListPage.getRevisionField()));
        assertEquals("WIP", getText(editCodeListPage.getStateField()));
        assertEquals(targetRelease.getReleaseNumber(), getText(editCodeListPage.getReleaseField()));
        assertDoesNotThrow(() -> editCodeListPage.selectCodeListValue(value.getValue()));
    }

    @Test
    @DisplayName("TC_33_2_TA_4")
    public void test_TA_4() {
        AppUserObject endUser;
        LibraryObject library;
        CodeListObject codeList;
        CodeListValueObject value;
        ReleaseObject release;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, namespace, release, "QA");
            value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        UpliftCodeListPage upliftCodeListPage = homePage.getBIEMenu().openUpliftCodeListSubMenu();
        ReleaseObject targetRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
        EditCodeListPage editCodeListPage = upliftCodeListPage.hitUpliftButton(codeList, release, targetRelease);
        assertEquals(codeList.getName(), getText(editCodeListPage.getCodeListNameField()));
        assertEquals(codeList.getListId(), getText(editCodeListPage.getListIDField()));
        assertEquals(codeList.getVersionId(), getText(editCodeListPage.getVersionField()));
        assertEquals(codeList.getRemark(), getText(editCodeListPage.getRemarkField()));
        assertEquals(codeList.getDefinition(), getText(editCodeListPage.getDefinitionField()));
        assertEquals(codeList.getDefinitionSource(), getText(editCodeListPage.getDefinitionSourceField()));
        assertEquals("1", getText(editCodeListPage.getRevisionField()));
        assertEquals("WIP", getText(editCodeListPage.getStateField()));
        assertEquals(targetRelease.getReleaseNumber(), getText(editCodeListPage.getReleaseField()));
        assertDoesNotThrow(() -> editCodeListPage.selectCodeListValue(value.getValue()));
    }

    @Test
    @DisplayName("TC_33_2_TA_5")
    public void test_TA_5() {
        AppUserObject endUser;
        LibraryObject library;
        CodeListObject codeList;
        CodeListValueObject value;
        ReleaseObject release;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, namespace, release, "Production");
            value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
        }

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        UpliftCodeListPage upliftCodeListPage = homePage.getBIEMenu().openUpliftCodeListSubMenu();
        ReleaseObject targetRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
        EditCodeListPage editCodeListPage = upliftCodeListPage.hitUpliftButton(codeList, release, targetRelease);
        assertEquals(codeList.getName(), getText(editCodeListPage.getCodeListNameField()));
        assertEquals(codeList.getListId(), getText(editCodeListPage.getListIDField()));
        assertEquals(codeList.getVersionId(), getText(editCodeListPage.getVersionField()));
        assertEquals(codeList.getRemark(), getText(editCodeListPage.getRemarkField()));
        assertEquals(codeList.getDefinition(), getText(editCodeListPage.getDefinitionField()));
        assertEquals(codeList.getDefinitionSource(), getText(editCodeListPage.getDefinitionSourceField()));
        assertEquals("1", getText(editCodeListPage.getRevisionField()));
        assertEquals("WIP", getText(editCodeListPage.getStateField()));
        assertEquals(targetRelease.getReleaseNumber(), getText(editCodeListPage.getReleaseField()));
        assertDoesNotThrow(() -> editCodeListPage.selectCodeListValue(value.getValue()));
    }

    @Test
    @DisplayName("TC_33_2_TA_6")
    public void test_TA_6() {
        AppUserObject endUser;
        LibraryObject library;
        CodeListObject codeList;
        CodeListValueObject value;
        ReleaseObject release;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, namespace, release, "Deleted");
            value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        UpliftCodeListPage upliftCodeListPage = homePage.getBIEMenu().openUpliftCodeListSubMenu();
        ReleaseObject targetRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
        EditCodeListPage editCodeListPage = upliftCodeListPage.hitUpliftButton(codeList, release, targetRelease);
        assertEquals(codeList.getName(), getText(editCodeListPage.getCodeListNameField()));
        assertEquals(codeList.getListId(), getText(editCodeListPage.getListIDField()));
        assertEquals(codeList.getVersionId(), getText(editCodeListPage.getVersionField()));
        assertEquals(codeList.getRemark(), getText(editCodeListPage.getRemarkField()));
        assertEquals(codeList.getDefinition(), getText(editCodeListPage.getDefinitionField()));
        assertEquals(codeList.getDefinitionSource(), getText(editCodeListPage.getDefinitionSourceField()));
        assertEquals("1", getText(editCodeListPage.getRevisionField()));
        assertEquals("WIP", getText(editCodeListPage.getStateField()));
        assertEquals(targetRelease.getReleaseNumber(), getText(editCodeListPage.getReleaseField()));
        assertDoesNotThrow(() -> editCodeListPage.selectCodeListValue(value.getValue()));
    }

    @Test
    @DisplayName("TC_33_2_TA_7")
    public void test_TA_7() {
        AppUserObject endUser;
        LibraryObject library;
        CodeListObject codeList;
        CodeListValueObject value;
        ReleaseObject release;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, namespace, release, "WIP");
            codeList.setDeprecated(true);
            getAPIFactory().getCodeListAPI().updateCodeList(codeList);

            value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        UpliftCodeListPage upliftCodeListPage = homePage.getBIEMenu().openUpliftCodeListSubMenu();
        ReleaseObject targetRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
        EditCodeListPage editCodeListPage = upliftCodeListPage.hitUpliftButton(codeList, release, targetRelease);
        assertEquals(codeList.getName(), getText(editCodeListPage.getCodeListNameField()));
        assertEquals(codeList.getListId(), getText(editCodeListPage.getListIDField()));
        assertEquals(codeList.getVersionId(), getText(editCodeListPage.getVersionField()));
        assertEquals(codeList.getRemark(), getText(editCodeListPage.getRemarkField()));
        assertEquals(codeList.getDefinition(), getText(editCodeListPage.getDefinitionField()));
        assertEquals(codeList.getDefinitionSource(), getText(editCodeListPage.getDefinitionSourceField()));
        assertEquals("1", getText(editCodeListPage.getRevisionField()));
        assertEquals("WIP", getText(editCodeListPage.getStateField()));
        assertEquals(targetRelease.getReleaseNumber(), getText(editCodeListPage.getReleaseField()));
        assertDoesNotThrow(() -> editCodeListPage.selectCodeListValue(value.getValue()));
    }

    @Test
    @DisplayName("TC_33_2_TA_8")
    public void test_TA_8() {
        AppUserObject endUser;
        LibraryObject library;
        CodeListObject codeList;
        CodeListValueObject value;
        ReleaseObject release;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(endUser, namespace, release, "Production");

            value = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, endUser);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        EditCodeListPage editCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu().openCodeListViewEditPage(codeList);
        editCodeListPage.hitAmendButton();
        waitFor(ofMillis(500L));
        CodeListObject amendedCL = getAPIFactory().getCodeListAPI().getNewlyCreatedCodeList(endUser, release.getReleaseNumber());

        UpliftCodeListPage upliftCodeListPage = homePage.getBIEMenu().openUpliftCodeListSubMenu();
        ReleaseObject targetRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
        upliftCodeListPage.hitUpliftButton(amendedCL, release, targetRelease);
        assertEquals(amendedCL.getName(), getText(editCodeListPage.getCodeListNameField()));
        assertEquals(amendedCL.getListId(), getText(editCodeListPage.getListIDField()));
        assertEquals(amendedCL.getVersionId(), getText(editCodeListPage.getVersionField()));
        assertEquals(amendedCL.getRemark(), getText(editCodeListPage.getRemarkField()));
        assertEquals(amendedCL.getDefinition(), getText(editCodeListPage.getDefinitionField()));
        assertEquals(amendedCL.getDefinitionSource(), getText(editCodeListPage.getDefinitionSourceField()));
        assertEquals("1", getText(editCodeListPage.getRevisionField()));
        assertEquals("WIP", getText(editCodeListPage.getStateField()));
        assertEquals(targetRelease.getReleaseNumber(), getText(editCodeListPage.getReleaseField()));
        assertDoesNotThrow(() -> editCodeListPage.selectCodeListValue(value.getValue()));
    }

    @Test
    @DisplayName("TC_33_2_TA_9")
    public void test_TA_9() {
        AppUserObject endUser;
        LibraryObject library;
        CodeListObject codeList;
        List<CodeListValueObject> baseCodeListValues = new ArrayList<>();
        ReleaseObject release;
        ReleaseObject targetRelease;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            targetRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
            CodeListObject baseCodeList = getAPIFactory().getCodeListAPI().
                    getCodeListByCodeListNameAndReleaseNum("oacl_ControlCode", release.getReleaseNumber());
            baseCodeListValues = getAPIFactory().getCodeListValueAPI().getCodeListValuesByCodeListManifestId(baseCodeList.getCodeListManifestId());

            codeList = getAPIFactory().getCodeListAPI().createDerivedCodeList(baseCodeList, endUser, namespace, release, "WIP");
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        UpliftCodeListPage upliftCodeListPage = homePage.getBIEMenu().openUpliftCodeListSubMenu();
        EditCodeListPage editCodeListPage = upliftCodeListPage.hitUpliftButton(codeList, release, targetRelease);
        assertEquals(codeList.getName(), getText(editCodeListPage.getCodeListNameField()));
        assertEquals(codeList.getListId(), getText(editCodeListPage.getListIDField()));
        assertEquals(codeList.getVersionId(), getText(editCodeListPage.getVersionField()));
        assertEquals(codeList.getRemark(), getText(editCodeListPage.getRemarkField()));
        assertEquals(codeList.getDefinition(), getText(editCodeListPage.getDefinitionField()));
        assertEquals(codeList.getDefinitionSource(), getText(editCodeListPage.getDefinitionSourceField()));
        assertEquals("1", getText(editCodeListPage.getRevisionField()));
        assertEquals("WIP", getText(editCodeListPage.getStateField()));
        assertEquals(targetRelease.getReleaseNumber(), getText(editCodeListPage.getReleaseField()));
        for (CodeListValueObject value : baseCodeListValues) {
            assertDoesNotThrow(() -> editCodeListPage.selectCodeListValue(value.getValue()));
        }
    }

    @Test
    @DisplayName("TC_33_2_TA_10")
    public void test_TA_10() {
        AppUserObject endUser;
        LibraryObject library;
        CodeListObject codeListEU;
        List<CodeListValueObject> baseCodeListValues;
        CodeListValueObject euCLValue;
        ReleaseObject release;
        ReleaseObject targetRelease;
        {
            endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUser);

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            NamespaceObject namespaceEU = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
            release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.4");
            targetRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.5");
            CodeListObject baseCodeList = getAPIFactory().getCodeListAPI().
                    getCodeListByCodeListNameAndReleaseNum("oacl_ControlCode", release.getReleaseNumber());
            baseCodeListValues = getAPIFactory().getCodeListValueAPI().getCodeListValuesByCodeListManifestId(baseCodeList.getCodeListManifestId());

            codeListEU = getAPIFactory().getCodeListAPI().createDerivedCodeList(baseCodeList, endUser, namespaceEU, release, "WIP");

            euCLValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeListEU, endUser);
        }
        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        UpliftCodeListPage upliftCodeListPage = homePage.getBIEMenu().openUpliftCodeListSubMenu();
        EditCodeListPage editCodeListPage = upliftCodeListPage.hitUpliftButton(codeListEU, release, targetRelease);
        assertEquals(codeListEU.getName(), getText(editCodeListPage.getCodeListNameField()));
        assertEquals(codeListEU.getListId(), getText(editCodeListPage.getListIDField()));
        assertEquals(codeListEU.getVersionId(), getText(editCodeListPage.getVersionField()));
        assertEquals(codeListEU.getRemark(), getText(editCodeListPage.getRemarkField()));
        assertEquals(codeListEU.getDefinition(), getText(editCodeListPage.getDefinitionField()));
        assertEquals(codeListEU.getDefinitionSource(), getText(editCodeListPage.getDefinitionSourceField()));
        assertEquals("1", getText(editCodeListPage.getRevisionField()));
        assertEquals("WIP", getText(editCodeListPage.getStateField()));
        assertEquals(targetRelease.getReleaseNumber(), getText(editCodeListPage.getReleaseField()));
        for (CodeListValueObject value : baseCodeListValues) {
            assertDoesNotThrow(() -> editCodeListPage.selectCodeListValue(value.getValue()));
        }

        /**
         * If the code list of the target release and the uplifted end user code list contain some same values,
         * the code list values are used.
         */
        assertDoesNotThrow(() -> editCodeListPage.selectCodeListValue(euCLValue.getValue()));
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
