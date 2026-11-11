package org.oagi.score.e2e.TS_10_WorkingBranchCoreComponentManagementBehaviorsForDeveloper.cancel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.core_component.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.getText;

/**
 * The developer cancels the revision of an ACC, an ASCCP and a BCCP after changing what they refer to: the base ACC,
 * the ACC the ASCCP is the role of, and the Data Type of the BCCP. Cancel brings back what they referred to, and the DEN
 * that follows from it.
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_10_25_CancelRevisionRestoresWhatWasChanged extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_10_25_TA_1")
    public void cancelling_the_revision_of_an_asccp_brings_back_the_acc_it_is_the_role_of_and_its_den() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        CoreComponentAPI api = getAPIFactory().getCoreComponentAPI();
        ACCObject roleOf = api.createRandomACC(developer, release, namespace, "Published");
        ASCCPObject asccp = api.createRandomASCCP(roleOf, developer, namespace, "Published");
        ACCObject anotherACC = api.createRandomACC(developer, release, namespace, "Published");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ASCCPViewEditPage asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        String den = getText(asccpViewEditPage.getASCCPPanel().getDENField());
        asccpViewEditPage.hitReviseButton();
        viewEditCoreComponentPage.openPage();
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());

        ASCCPChangeACCDialog changeACCDialog = asccpViewEditPage.openChangeACCDialog("/" + asccp.getPropertyTerm());
        changeACCDialog.hitUpdateButton(anotherACC.getDen());
        viewEditCoreComponentPage.openPage();
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        assertTrue(getText(asccpViewEditPage.getASCCPPanel().getDENField()).endsWith(anotherACC.getObjectClassTerm()));
        asccpViewEditPage.hitCancelButton();

        viewEditCoreComponentPage.openPage();
        asccpViewEditPage = viewEditCoreComponentPage.openASCCPViewEditPageByManifestID(asccp.getAsccpManifestId());
        assertEquals(den, getText(asccpViewEditPage.getASCCPPanel().getDENField()), "the DEN is as it was");
        assertTrue(getText(asccpViewEditPage.getASCCPPanel().getDENField()).endsWith(roleOf.getObjectClassTerm()),
                "the ASCCP is the role of the ACC it was");
        ASCCPViewEditPage.ASCCPPanel panel = asccpViewEditPage.getASCCPPanel();
        assertEquals("Published", getText(panel.getStateField()));
        assertEquals("1", getText(panel.getRevisionField()));
    }

    @Test
    @DisplayName("TC_10_25_TA_2")
    public void cancelling_the_revision_of_a_bccp_brings_back_its_data_type_and_its_den() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        CoreComponentAPI api = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = api.getBDTByGuidAndReleaseNum(library, "ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject bccp = api.createRandomBCCP(release, dataType, developer, namespace, "Published");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());
        String den = getText(bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel().getDENField());
        bccpViewEditPage.hitReviseButton();

        String nextBDTDen = "Telephone_ Value. Type";
        bccpViewEditPage.openChangeDTDialog().update(nextBDTDen);
        assertEquals(nextBDTDen, getText(bccpViewEditPage.getBCCPPanelContainer().getDTPanel().getDENField()));
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());
        bccpViewEditPage.hitCancelButton();

        viewEditCoreComponentPage.openPage();
        bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());
        BCCPViewEditPage.BCCPPanel panel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertEquals("Published", getText(panel.getStateField()));
        assertEquals("1", getText(panel.getRevisionField()));
        assertEquals(den, getText(panel.getDENField()), "the DEN is as it was");
        assertEquals(dataType.getDen(), getText(bccpViewEditPage.getBCCPPanelContainer().getDTPanel().getDENField()),
                "the Data Type is as it was");
    }

    @Test
    @DisplayName("TC_10_25_TA_3")
    public void cancelling_the_revision_of_an_acc_brings_back_its_base_acc_and_takes_the_added_association_away() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().getNamespaceByURI(library, "http://www.openapplications.org/oagis/10");
        CoreComponentAPI api = getAPIFactory().getCoreComponentAPI();
        ACCObject accForBase = api.createRandomACC(developer, release, namespace, "Published");
        ACCObject acc = api.createRandomACC(developer, release, namespace, "Published");
        api.updateBasedACC(acc, accForBase);
        ACCObject accForAsccp = api.createRandomACC(developer, release, namespace, "Published");
        ASCCPObject asccp = api.createRandomASCCP(accForAsccp, developer, namespace, "Published");

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitReviseButton();

        accViewEditPage.deleteBaseACC("/" + acc.getDen() + "/" + accForBase.getDen());
        accViewEditPage.appendPropertyAtLast("/" + acc.getDen()).selectAssociation(asccp.getDen());
        assertTrue(accViewEditPage.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm()).isDisplayed());

        viewEditCoreComponentPage.openPage();
        accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitCancelButton();

        viewEditCoreComponentPage.openPage();
        ACCViewEditPage reopened = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        assertTrue(reopened.getNodeByPath("/" + acc.getDen() + "/" + accForBase.getDen()).isDisplayed(),
                "the base ACC is back");
        assertThrows(org.openqa.selenium.WebDriverException.class,
                () -> reopened.getNodeByPath("/" + acc.getDen() + "/" + asccp.getPropertyTerm()),
                "the added association is gone");
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        this.randomAccounts.forEach(newUser ->
                getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(newUser.getLoginId()));
    }
}
