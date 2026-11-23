package org.oagi.score.e2e.TS_15_ReleaseBranchCoreComponentManagementBehaviorForEndUser.cancel;

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
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.getText;

/**
 * The end user cancels the amendment of an ACC and a BCCP after changing what they refer to: the base ACC and the
 * Data Type of the BCCP. Cancel brings back what they referred to, and the DEN
 * that follows from it.
 */
@Execution(ExecutionMode.CONCURRENT)
public class TC_15_20_CancelAmendmentRestoresWhatWasChanged extends BaseTest {
    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_15_20_TA_1")
    public void cancelling_the_amendment_of_a_bccp_brings_back_its_data_type_and_its_den() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.7.1");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        CoreComponentAPI api = getAPIFactory().getCoreComponentAPI();
        // Code. Type
        DTObject dataType = api.getBDTByGuidAndReleaseNum(library, "ef32205ede95407f981064a45ffa652c", release.getReleaseNumber());
        BCCPObject bccp = api.createRandomBCCP(release, dataType, endUser, namespace, "Production");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        BCCPViewEditPage bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());
        String den = getText(bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel().getDENField());
        bccpViewEditPage.hitAmendButton();

        String nextBDTDen = "Telephone_ Value. Type";
        bccpViewEditPage.openChangeDTDialog().update(nextBDTDen);
        assertEquals(nextBDTDen, getText(bccpViewEditPage.getBCCPPanelContainer().getDTPanel().getDENField()));
        viewEditCoreComponentPage.openPage();
        bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());
        bccpViewEditPage.hitCancelButton();

        viewEditCoreComponentPage.openPage();
        bccpViewEditPage = viewEditCoreComponentPage.openBCCPViewEditPageByManifestID(bccp.getBccpManifestId());
        BCCPViewEditPage.BCCPPanel panel = bccpViewEditPage.getBCCPPanelContainer().getBCCPPanel();
        assertEquals("Production", getText(panel.getStateField()));
        assertEquals("1", getText(panel.getRevisionField()));
        assertEquals(den, getText(panel.getDENField()), "the DEN is as it was");
        assertEquals(dataType.getDen(), getText(bccpViewEditPage.getBCCPPanelContainer().getDTPanel().getDENField()),
                "the Data Type is as it was");
    }

    @Test
    @DisplayName("TC_15_20_TA_2")
    public void cancelling_the_amendment_of_an_acc_brings_back_its_base_acc_and_takes_the_added_association_away() {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.7.1");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        CoreComponentAPI api = getAPIFactory().getCoreComponentAPI();
        ACCObject accForBase = api.createRandomACC(endUser, release, namespace, "Production");
        ACCObject acc = api.createRandomACC(endUser, release, namespace, "Production");
        api.updateBasedACC(acc, accForBase);
        ACCObject accForAsccp = api.createRandomACC(endUser, release, namespace, "Production");
        ASCCPObject asccp = api.createRandomASCCP(accForAsccp, endUser, namespace, "Production");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCoreComponentPage viewEditCoreComponentPage = homePage.getCoreComponentMenu().openViewEditCoreComponentSubMenu();
        ACCViewEditPage accViewEditPage = viewEditCoreComponentPage.openACCViewEditPageByManifestID(acc.getAccManifestId());
        accViewEditPage.hitAmendButton();

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
