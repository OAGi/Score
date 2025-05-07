package org.oagi.score.e2e.TS_38_WorkingBranchDataTypeManagementForDeveloper;

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
import org.oagi.score.e2e.page.core_component.ViewEditDataTypePage;
import org.openqa.selenium.TimeoutException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class TC_38_9_CreatingNewRevisionOfDeveloperDT extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();

    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_38_9_TA_1")
    public void test_TA_1() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        DTObject baseCDT;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            baseCDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            DTObject randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseCDT, developerA, namespace, "Published");
            dtForTesting.add(randomBDT);

            randomBDT = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseCDT, developerB, namespace, "Published");
            dtForTesting.add(randomBDT);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (DTObject dt : dtForTesting) {
            ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
            DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
            assertTrue(dt.getState().equals("Published"));
            dtViewEditPage.hitReviseButton();
            assertTrue(dtViewEditPage.getStateFieldValue().equals("WIP"));
            assertTrue(dtViewEditPage.getReleaseFieldValue().equals("Working"));
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
    @DisplayName("TC_38_9_TA_2")
    public void test_TA_2() {
        AppUserObject developerA;
        ReleaseObject branch;
        ArrayList<DTObject> dtForTesting = new ArrayList<>();
        DTObject baseCDT;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            AppUserObject developerB = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerB);

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");

            baseCDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", branch.getReleaseNumber());

            library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
            NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");

            DTObject randomBDTWIP = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseCDT, developerA, namespace, "WIP");
            dtForTesting.add(randomBDTWIP);

            randomBDTWIP = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseCDT, developerB, namespace, "WIP");
            dtForTesting.add(randomBDTWIP);

            DTObject randomBDTDraft = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseCDT, developerA, namespace, "Draft");
            dtForTesting.add(randomBDTDraft);

            randomBDTDraft = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseCDT, developerB, namespace, "Draft");
            dtForTesting.add(randomBDTDraft);

            DTObject randomBDTCandidate = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseCDT, developerA, namespace, "Candidate");
            dtForTesting.add(randomBDTCandidate);

            randomBDTCandidate = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseCDT, developerB, namespace, "Candidate");
            dtForTesting.add(randomBDTCandidate);
        }

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        for (DTObject dt : dtForTesting) {
            ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
            DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
            assertFalse(dt.getState().equals("Published"));
            assertThrows(TimeoutException.class, () -> dtViewEditPage.hitReviseButton());

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
