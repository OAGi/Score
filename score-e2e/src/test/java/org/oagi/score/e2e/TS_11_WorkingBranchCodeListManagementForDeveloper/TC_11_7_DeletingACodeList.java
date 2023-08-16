package org.oagi.score.e2e.TS_11_WorkingBranchCodeListManagementForDeveloper;

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
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
import org.oagi.score.e2e.page.core_component.*;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_11_7_DeletingACodeList extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_11_7_TA_1")
    public void test_TA_1() {
        AppUserObject developerA;
        ReleaseObject workingBranch;
        List<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<BigInteger, CodeListValueObject> codeListCodeListValueMap = new HashMap<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "WIP");
            CodeListValueObject codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerA);
            codeListCodeListValueMap.put(codeList.getCodeListManifestId(), codeListValue);
            codeListForTesting.add(codeList);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (CodeListObject codeList : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
            assertEquals("Working", getText(editCodeListPage.getReleaseField()));
            assertEquals(developerA.getLoginId(), getText(editCodeListPage.getOwnerField()));
            assertEquals("WIP", getText(editCodeListPage.getStateField()));
            editCodeListPage.hitDeleteButton();
            codeList.setState("Deleted");

            viewEditCodeListPage.openPage();
            editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
            assertEquals("Deleted", getText(editCodeListPage.getStateField()));
        }
    }

    @Test
    @DisplayName("TC_11_7_TA_2")
    public void test_TA_2() {
        AppUserObject developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developerA);

        ReleaseObject workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
        CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "WIP");

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.createDT("Process Category_ Code. Type", workingBranch.getReleaseNumber());
        dtViewEditPage.showValueDomain();
        dtViewEditPage.addCodeListValueDomain(codeList.getName());
        String qualifier = "testDataType" + randomAlphabetic(5, 10);
        dtViewEditPage.setQualifier(qualifier);
        String definition = getText(dtViewEditPage.getDefinitionField());
        if (definition == null) {
            click(dtViewEditPage.getUpdateButton(true));
            waitFor(Duration.ofMillis(500));
            dtViewEditPage.hitUpdateAnywayButton();
        } else{
            dtViewEditPage.hitUpdateButton();
        }

        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
        editCodeListPage.hitDeleteButton();
        codeList.setState("Deleted");

        viewEditCoreComponentPage.openPage();
        DTViewEditPage dtViewEditPageNew = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(qualifier + "_ Code. Type", workingBranch.getReleaseNumber());
        dtViewEditPageNew.showValueDomain();
        assertDoesNotThrow(() -> dtViewEditPageNew.codeListIdMarkedAsDeleted(codeList));
        escape(getDriver());
        dtViewEditPageNew.changeCodeListValueDomain(codeList.getName());

        editCodeListPage.openPage();
        editCodeListPage.hitRestoreButton();

        dtViewEditPageNew.openPage();
        dtViewEditPageNew.showValueDomain();
        assertThrows(TimeoutException.class, () -> dtViewEditPageNew.codeListIdMarkedAsDeleted(codeList));
    }

    @Test
    @DisplayName("TC_11_7_TA_3")
    public void test_TA_3() {
        AppUserObject developerA;
        ReleaseObject workingBranch;
        ACCObject acc;
        List<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<BigInteger, CodeListValueObject> codeListCodeListValueMap = new HashMap<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");
            acc = getAPIFactory().getCoreComponentAPI().createRandomACC(developerA, workingBranch, namespace, "WIP");

            CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "WIP");
            CodeListValueObject codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerA);
            codeListCodeListValueMap.put(codeList.getCodeListManifestId(), codeListValue);
            codeListForTesting.add(codeList);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (CodeListObject codeList : codeListForTesting) {
            /**
             * Create BDT that uses the newly created Code List
             */
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.createDT("Process Category_ Code. Type", workingBranch.getReleaseNumber());
            dtViewEditPage.showValueDomain();
            dtViewEditPage.addCodeListValueDomain(codeList.getName());
            dtViewEditPage.setDefaultValueDomain(codeList.getName());
            String qualifier = "testDataType" + randomAlphabetic(5, 10);
            dtViewEditPage.setQualifier(qualifier);
            String definition = getText(dtViewEditPage.getDefinitionField());
            if (definition == null) {
                click(dtViewEditPage.getUpdateButton(true));
                dtViewEditPage.hitUpdateAnywayButton();
            } else{
                dtViewEditPage.hitUpdateButton();
            }
            /**
             * Create new BCCP that uses previously created BDT
             */
            viewEditCoreComponentPage.openPage();
            BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.createBCCP(qualifier + "_ Code. Type", workingBranch.getReleaseNumber(), developerA);
            String BCCPPropertyTerm = "testBCCPProperty";
            bccpViewEditPage.setPropertyTerm(BCCPPropertyTerm);
            bccpViewEditPage.setNamespace("http://www.openapplications.org/oagis/10");
            bccpViewEditPage.setDefinition("definition");
            definition = getText(bccpViewEditPage.getDefinitionField());
            if (definition == null) {
                click(bccpViewEditPage.getUpdateButton(true));
                bccpViewEditPage.hitUpdateAnywayButton();
            }else{
                bccpViewEditPage.hitUpdateButton();
            }
            BCCPObject createdBCCP = getAPIFactory().getCoreComponentAPI().getLatestBCCPCreatedByUser(developerA, workingBranch.getReleaseNumber());
            /**
             * Create ACC that has previously created BCCP
             */
            viewEditCoreComponentPage.openPage();
            ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
            SelectAssociationDialog selectAssociationDialog = accViewEditPage.appendPropertyAtLast("/" + acc.getDen());
            selectAssociationDialog.selectAssociation(createdBCCP.getDen());

            /**
             * Delete Code List
             */
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
            editCodeListPage.hitDeleteButton();
            /*TODO:
            As the developer expands the tree down to the BCCP using the BDT that the deleted CL, the CL shall be flagged as deleted.
             */
            viewEditCoreComponentPage.openPage();
            accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByDenAndBranch(acc.getDen(), workingBranch.getReleaseNumber());
            WebElement bccNode = accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + createdBCCP.getPropertyTerm());
            ACCViewEditPage.BCCPanelContainer bccPanelContainer = accViewEditPage.getBCCPanelContainer(bccNode);
        }
    }

    @Test
    @DisplayName("TC_11_7_TA_4")
    public void test_TA_4() {
        AppUserObject developerA;
        ReleaseObject workingBranch;
        List<CodeListObject> codeListForTesting = new ArrayList<>();
        Map<BigInteger, CodeListValueObject> codeListCodeListValueMap = new HashMap<>();
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);
            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            workingBranch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber("Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI("http://www.openapplications.org/oagis/10");

            CodeListObject codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerA, namespace, workingBranch, "Published");
            CodeListValueObject codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerA);
            codeListCodeListValueMap.put(codeList.getCodeListManifestId(), codeListValue);
            codeListForTesting.add(codeList);

            codeList = getAPIFactory().getCodeListAPI().createRandomCodeList(developerB, namespace, workingBranch, "Published");
            codeListValue = getAPIFactory().getCodeListValueAPI().createRandomCodeListValue(codeList, developerB);
            codeListCodeListValueMap.put(codeList.getCodeListManifestId(), codeListValue);
            codeListForTesting.add(codeList);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (CodeListObject codeList : codeListForTesting) {
            ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
            EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(codeList);
            editCodeListPage.hitRevise();

            viewEditCodeListPage.openPage();
            codeList = getAPIFactory().getCodeListAPI().getCodeListByManifestId(codeList.getCodeListManifestId());
            EditCodeListPage editCodeListPageNew = viewEditCodeListPage.openCodeListViewEditPage(codeList);
            assertEquals("WIP", getText(editCodeListPage.getStateField()));
            assertTrue(Integer.valueOf(getText(editCodeListPage.getRevisionField())) > 1);
            assertThrows(TimeoutException.class, () -> {
                editCodeListPageNew.hitDeleteButton();
            });
            editCodeListPageNew.moveToDraft();
            assertEquals("Draft", getText(editCodeListPage.getStateField()));
            assertThrows(TimeoutException.class, () -> {
                editCodeListPageNew.hitDeleteButton();
            });
            editCodeListPageNew.moveToCandidate();
            assertEquals("Candidate", getText(editCodeListPage.getStateField()));
            assertThrows(TimeoutException.class, () -> {
                editCodeListPageNew.hitDeleteButton();
            });
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
