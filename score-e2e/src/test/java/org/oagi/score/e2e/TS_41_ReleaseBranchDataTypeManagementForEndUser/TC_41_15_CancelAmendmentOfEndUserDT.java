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
import org.oagi.score.e2e.page.core_component.BCCPViewEditPage;
import org.oagi.score.e2e.page.core_component.DTViewEditPage;
import org.oagi.score.e2e.page.core_component.ViewEditCoreComponentPage;
import org.oagi.score.e2e.page.core_component.ViewEditDataTypePage;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.getText;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

/**
 * The end user cancels the amendment of a Data Type: the Data Type is as it was before it was amended.
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_41_15_CancelAmendmentOfEndUserDT extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_41_15_TA_1")
    public void cancelling_the_amendment_takes_back_the_supplementary_component_that_was_added_and_the_edit_of_an_inherited_one() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
        ReleaseObject cdtRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");
        DTObject baseCDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", cdtRelease.getReleaseNumber());

        library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.8");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        DTObject dt = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseCDT, endUser, namespace, "Production");

        DTSCObject existing = getAPIFactory().getCoreComponentAPI()
                .getSupplementaryComponentsForDT(baseCDT.getDtId(), cdtRelease.getReleaseNumber()).get(0);
        String existingName = existing.getObjectClassTerm() + ". " + existing.getPropertyTerm() + ". " + existing.getRepresentationTerm();

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());

        DTViewEditPage.SupplementaryComponentPanel before = dtViewEditPage.getSCPanel(
                dtViewEditPage.getNodeByPath("/" + dt.getDen() + "/" + existingName));
        String cardinality = before.getCardinalityFieldValue();
        String constraintType = before.getValueConstraintTypeFieldValue();

        dtViewEditPage.hitAmendButton();
        assertEquals("WIP", dtViewEditPage.getStateFieldValue());

        // add a supplementary component, and change an inherited one
        dtViewEditPage.addSupplementaryComponent("/" + dt.getDen());
        waitFor(Duration.ofMillis(3000L));
        DTViewEditPage.SupplementaryComponentPanel inherited = dtViewEditPage.getSCPanel(
                dtViewEditPage.getNodeByPath("/" + dt.getDen() + "/" + existingName));
        inherited.setCardinality("Required");
        inherited.setValueConstraintType("Fixed Value");
        inherited.setValueConstraint("fixed value");
        dtViewEditPage.hitUpdateButton();

        dtViewEditPage.hitCancelButton();

        // back as it was
        viewEditDataTypePage.openPage();
        dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
        assertEquals("Production", dtViewEditPage.getStateFieldValue());
        assertEquals("1", dtViewEditPage.getRevisionFieldValue());
        DTViewEditPage.SupplementaryComponentPanel after = dtViewEditPage.getSCPanel(
                dtViewEditPage.getNodeByPath("/" + dt.getDen() + "/" + existingName));
        assertEquals(cardinality, after.getCardinalityFieldValue());
        assertEquals(constraintType, after.getValueConstraintTypeFieldValue());
        assertNull(getAPIFactory().getCoreComponentAPI().getRevisedDT(dt), "no amendment is left behind");

        // and it can be amended again
        dtViewEditPage.hitAmendButton();
        assertEquals("WIP", dtViewEditPage.getStateFieldValue());
        assertTrue(Integer.parseInt(dtViewEditPage.getRevisionFieldValue()) > 1);
    }

    @Test
    @DisplayName("TC_41_15_TA_2")
    public void cancelling_the_amendment_of_a_dt_that_an_amended_bccp_uses_keeps_the_bccp_on_the_dt() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("CCTS Data Type Catalogue v3");
        ReleaseObject cdtRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "3.1");
        DTObject baseCDT = getAPIFactory().getCoreComponentAPI().getCDTByDENAndReleaseNum(library, "Code. Type", cdtRelease.getReleaseNumber());

        library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject branch = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.8");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        DTObject dt = getAPIFactory().getCoreComponentAPI().createRandomBDT(branch, baseCDT, endUser, namespace, "Production");
        // the helper that makes a released BCCP looks for the Data Type in the latest release, which a new Data Type is not in
        BCCPObject bccp = getAPIFactory().getCoreComponentAPI().createRandomBCCP(branch, dt, endUser, namespace, "WIP");
        bccp.setState("Production");
        getAPIFactory().getCoreComponentAPI().updateBCCP(bccp);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditDataTypePage viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        DTViewEditPage dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
        dtViewEditPage.hitAmendButton();

        // the BCCP is amended with the amended Data Type
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());
        bccpViewEditPage.hitAmendButton();

        // cancel the amendment of the Data Type
        viewEditDataTypePage = homePage.getCoreComponentMenu().openViewEditDataTypeSubMenu();
        dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
        dtViewEditPage.hitCancelButton();
        viewEditDataTypePage.openPage();
        dtViewEditPage = viewEditDataTypePage.openDTViewEditPageByManifestID(dt.getDtManifestId());
        assertEquals("Production", dtViewEditPage.getStateFieldValue());
        assertEquals("1", dtViewEditPage.getRevisionFieldValue());

        // the BCCP is still in the amendment and on the Data Type, and it can be cancelled too
        viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());
        assertEquals("WIP", getText(bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel().getStateField()));
        bccpViewEditPage.hitCancelButton();
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());
        assertEquals("Production", getText(bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel().getStateField()));
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        this.randomAccounts.forEach(newUser ->
                getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId()));
    }
}
