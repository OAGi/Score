package org.oagi.score.e2e.TS_38_WorkingBranchDataTypeManagementForDeveloper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.DTObject;
import org.oagi.score.e2e.obj.LibraryObject;
import org.oagi.score.e2e.obj.ReleaseObject;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.DTViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditDataTypePage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Issue #1884: a Data Type created from a base Data Type of the release the library depends on (the CCTS Data Type
 * Catalogue) keeps the allowed primitives of the base, now pointing to the manifests of its own release.
 * The manifests themselves are not visible in the UI; they are covered by DtCreateAllowedPrimitiveReleaseTest in score-http.
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_38_15_CreatingDTFromBaseOfDependencyRelease extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_38_15_TA_1")
    public void developer_can_create_a_data_type_in_working_branch_from_every_data_type_of_the_release_the_library_depends_on() {
        AppUserObject developerA;
        ReleaseObject working;
        LibraryObject catalogue;
        ReleaseObject catalogueRelease;
        List<String> baseDENs;
        {
            developerA = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
            thisAccountWillBeDeletedAfterTests(developerA);

            catalogue = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
            catalogueRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(catalogue, "3.1");
            // every Data Type of the catalogue is named "<representation term>. Type"
            baseDENs = getAPIFactory().getCoreComponentAPI().getRepresentationTermsForCDTs(catalogueRelease.getReleaseNumber())
                    .stream().map(term -> term + ". Type").sorted().toList();

            LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
            working = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        }
        assertFalse(baseDENs.isEmpty());

        HomePage homePage = loginPage().signIn(developerA.getLoginId(), developerA.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();

        List<String> failures = new ArrayList<>();
        for (String den : baseDENs) {
            try {
                DTObject baseDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(
                        catalogue, den, catalogueRelease.getReleaseNumber());
                // the allowed primitives of the base, as the Value Domain list of the new Data Type has to show them
                List<String> primitives = getAPIFactory().getCoreComponentAPI()
                        .getValueDomainsByCDTRepresentationTerm(baseDT.getRepresentationTerm());

                DTViewEditPage dtViewEditPage = viewEditDataTypePage.createDT(baseDT.getDen(), working.getReleaseNumber());
                assertEquals(baseDT.getDen(), dtViewEditPage.getBasedDataTypeFieldValue());
                assertEquals("Working", dtViewEditPage.getReleaseFieldValue());

                dtViewEditPage.showValueDomain();
                for (String primitive : primitives) {
                    assertDoesNotThrow(() -> dtViewEditPage.getTableRecordByValue(primitive),
                            "value domain '" + primitive + "' of " + den);
                }

                // It can be saved, and it is found in the list afterwards.
                String qualifier = "dep" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
                dtViewEditPage.setQualifier(qualifier);
                dtViewEditPage.setDefinition("TC_38_15 definition");
                dtViewEditPage.hitUpdateButton();

                homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
                viewEditDataTypePage.setBranch(working.getReleaseNumber());
                viewEditDataTypePage.setDEN(qualifier + "_ " + baseDT.getDen());
                viewEditDataTypePage.hitSearchButton();
                assertDoesNotThrow(() -> viewEditDataTypePage.getTableRecordByValue(qualifier + "_ " + baseDT.getDen()));
            } catch (Throwable e) {
                failures.add(den + ": " + e);
                homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
            }
        }
        assertTrue(failures.isEmpty(), "Data Types that could not be created from their base: " + failures);
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        this.randomAccounts.forEach(newUser ->
                getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId()));
    }
}
