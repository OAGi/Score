package org.oagi.score.e2e.TS_41_ReleaseBranchDataTypeManagementForEndUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.DTViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.openqa.selenium.TimeoutException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_41_9_CreatingNewRevisionOfAnEndUserDT extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_41_9_TA_1")
    public void test_TA_1() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        DTObject baseCDT;
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.8");
            NamespaceObject namespaceEUA = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);
            NamespaceObject namespaceEUB = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB, library);

            baseCDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());
            DTObject randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseCDT, endUserA, namespaceEUA, "Production");
            dtForTesting.add(randomBDT);

            randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseCDT, endUserB, namespaceEUB, "Production");
            dtForTesting.add(randomBDT);
        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        for (DTObject dt : dtForTesting) {
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            assertTrue(dt.getState().equals("Production"));
            dtViewEditPage.hitAmendButton();
            assertTrue(dtViewEditPage.getStateFieldValue().equals("WIP"));
            assertTrue(dtViewEditPage.getReleaseFieldValue().equals(branch.getReleaseNumber()));
            assertTrue(dtViewEditPage.getRevisionFieldValue().equals("2"));
            assertTrue(dtViewEditPage.getDataTypeTermFieldValue().equals(dt.getDataTypeTerm()));
            assertTrue(dtViewEditPage.getRepresentationTermFieldValue().equals(dt.getRepresentationTerm()));
            assertTrue(dtViewEditPage.getQualifierFieldValue().equals(dt.getQualifier()));
            dtViewEditPage.showValueDomain();
            List<String> valueDomains = getAPIFactory().getCoreComponentAPI().getValueDomainsByCDTRepresentationTerm(dt.getRepresentationTerm());
            String defaultValueDomain = getAPIFactory().getCoreComponentAPI().getDefaultValueDomainByCDTRepresentationTerm(dt.getRepresentationTerm());
            for (String valueDomain: valueDomains){
                assertDoesNotThrow(() -> dtViewEditPage.getTableRecordByValue(valueDomain));
            }
            assertTrue(dtViewEditPage.getDefaultValueDomainFieldValue().contains(defaultValueDomain));
            if (dt.getDefinition() != null){
                assertTrue(dtViewEditPage.getDefinitionFieldValue().equals(dt.getDefinition()));
            }

            if (dt.getDefinitionSource() != null){
                assertTrue(dtViewEditPage.getDefinitionSourceFieldValue().equals(dt.getDefinitionSource()));
            }

            if (dt.getContentComponentDefinition() != null){
                assertTrue(dtViewEditPage.getContentComponentDefinitionFieldValue().equals(dt.getContentComponentDefinition()));
            }
        }
    }
    @Test
    @DisplayName("TC_41_9_TA_2")
    public void test_TA_2() {
        AppUserObject endUserA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        DTObject baseCDT;
        {
            endUserA = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserA);

            AppUserObject endUserB = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
            thisAccountWillBeDeletedAfterTests(endUserB);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.6");
            NamespaceObject namespaceEUA = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserA, library);
            NamespaceObject namespaceEUB = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUserB, library);

            baseCDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());
            DTObject randomBDTWIP = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseCDT, endUserA, namespaceEUA, "WIP");
            dtForTesting.add(randomBDTWIP);

            randomBDTWIP = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseCDT, endUserB, namespaceEUB, "WIP");
            dtForTesting.add(randomBDTWIP);

            DTObject randomBDTQA = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseCDT, endUserA, namespaceEUA, "QA");
            dtForTesting.add(randomBDTQA);

            randomBDTQA = getAPIFactory().getCoreComponentAPI().createRandomBDT(baseCDT, endUserB, namespaceEUB, "QA");
            dtForTesting.add(randomBDTQA);

        }

        HomePage homePage = loginPage().signIn(endUserA.getLoginId(), endUserA.getPassword());
        for (DTObject dt : dtForTesting) {
            ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
            DTViewEditPage dtViewEditPage = viewEditCoreComponentPage.openDTViewEditPageByDenAndBranch(dt.getDen(), branch.getReleaseNumber());
            assertFalse(dt.getState().equals("Production"));
            assertThrows(TimeoutException.class, () -> dtViewEditPage.hitAmendButton());

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
