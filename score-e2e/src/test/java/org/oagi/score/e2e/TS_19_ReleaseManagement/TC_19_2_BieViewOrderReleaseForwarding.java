package org.oagi.score.e2e.TS_19_ReleaseManagement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.release.EditReleasePage;
import org.oagi.score.e2e.page.release.ReleaseAssignmentPage;
import org.oagi.score.e2e.page.release.ViewEditReleasePage;

import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.getSnackBarMessage;
import static org.oagi.score.e2e.impl.PageHelper.waitFor;

/**
 * Issue #1638 — sibling view-order weights forward from 'Working' into a new release.
 *
 * <p>Order weighting is authored on the 'Working' release. When a new release is drafted, the
 * {@code bie_view_order} rows applied to 'Working' must be copied onto the corresponding new-release
 * manifests; when the draft is rolled back to 'Initialized', those forwarded rows (and only those)
 * must be removed, leaving the 'Working' rows untouched.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class TC_19_2_BieViewOrderReleaseForwarding extends BaseTest {

    private static final int ASCC_WEIGHT = 30;
    private static final int BCC_WEIGHT = 40;

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    /** The view-parent ACC whose 'Working' view-order rows this test authored (for teardown cleanup). */
    private ACCObject viewOrderParentAcc;

    /** The release drafted by this test (for teardown cleanup of any forwarded rows on the failure path). */
    private BigInteger draftedReleaseId;

    @BeforeEach
    public void init() {
        super.init();
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        // Remove the view-order rows this test authored, before the account cleanup deletes their
        // manifests, so no orphan bie_view_order rows are left behind on a shared e2e DB.
        if (this.draftedReleaseId != null) {
            getAPIFactory().getBieViewOrderAPI().deleteViewOrdersByRelease(this.draftedReleaseId);
        }
        if (this.viewOrderParentAcc != null) {
            getAPIFactory().getBieViewOrderAPI()
                    .deleteViewOrdersByFromAccManifestId(this.viewOrderParentAcc.getAccManifestId());
        }

        this.randomAccounts.forEach(randomAccount ->
                getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(randomAccount.getLoginId()));
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    public void view_order_forwards_to_a_drafted_release_and_is_removed_when_rolled_back() {
        AppUserObject developer = getAPIFactory().getAppUserAPI().createRandomDeveloperAccount(false);
        thisAccountWillBeDeletedAfterTests(developer);
        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        NamespaceObject developerNamespace = getAPIFactory().getNamespaceAPI().createRandomDeveloperNamespace(developer, library);

        CoreComponentAPI coreComponentAPI = getAPIFactory().getCoreComponentAPI();
        ReleaseObject workingRelease = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "Working");

        // A self-consistent set of Candidate CCs on 'Working' so that "Assign All" + "Validate" succeeds:
        // a view-parent ACC that owns one ASCC child (an ASCCP whose role is a second ACC) and one BCC child.
        ACCObject parentAcc = coreComponentAPI.createRandomACC(developer, workingRelease, developerNamespace, "Candidate");
        this.viewOrderParentAcc = parentAcc;
        ACCObject roleAcc = coreComponentAPI.createRandomACC(developer, workingRelease, developerNamespace, "Candidate");
        ASCCPObject childAsccp = coreComponentAPI.createRandomASCCP(roleAcc, developer, developerNamespace, "Candidate");
        ASCCObject ascc = coreComponentAPI.appendASCC(parentAcc, childAsccp, "Candidate");

        DTObject bdt = coreComponentAPI.getBDTByDENAndReleaseNum(
                library, "System Environment_ Code. Type", workingRelease.getReleaseNumber()).get(0);
        BCCPObject childBccp = coreComponentAPI.createRandomBCCP(workingRelease, bdt, developer, developerNamespace, "Candidate");
        BCCObject bcc = coreComponentAPI.appendBCC(parentAcc, childBccp, "Candidate");

        // Author the view-order weights on 'Working'.
        getAPIFactory().getBieViewOrderAPI().setAsccViewOrder(parentAcc, ascc, ASCC_WEIGHT);
        getAPIFactory().getBieViewOrderAPI().setBccViewOrder(parentAcc, bcc, BCC_WEIGHT);

        List<BieViewOrderObject> workingOrders =
                getAPIFactory().getBieViewOrderAPI().getViewOrdersByFromAccManifestId(parentAcc.getAccManifestId());
        assertEquals(2, workingOrders.size(),
                "Pre-condition: two view-order rows must exist under the parent ACC on 'Working'.");

        ReleaseObject randomRelease = getAPIFactory().getReleaseAPI().createRandomRelease(developer, library, developerNamespace);
        String newReleaseNum = randomRelease.getReleaseNumber();
        BigInteger newReleaseId = randomRelease.getReleaseId();
        this.draftedReleaseId = newReleaseId;

        HomePage homePage = loginPage().signIn(developer.getLoginId(), developer.getPassword());
        ViewEditReleasePage viewEditReleasePage = homePage.getCoreComponentMenu().openViewEditReleaseSubMenu();
        EditReleasePage editReleasePage = viewEditReleasePage.openReleaseViewEditPageByReleaseAndState(newReleaseNum, "Initialized");
        ReleaseAssignmentPage releaseAssignmentPage = editReleasePage.hitCreateDraftButton();
        releaseAssignmentPage.hitAssignAllButton();
        releaseAssignmentPage.hitValidateButton();
        assertEquals("All components are valid.", getSnackBarMessage(getDriver()));

        releaseAssignmentPage.hitCreateButton();
        waitForReleaseState(library, newReleaseNum, "Draft");
        assertEquals("Draft",
                getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, newReleaseNum).getState());

        // The view-order rows must have been forwarded onto the new release's copied manifests.
        BigInteger forwardedParentAccManifestId =
                getAPIFactory().getBieViewOrderAPI().findForwardedAccManifestId(parentAcc.getAccManifestId(), newReleaseId);
        assertNotNull(forwardedParentAccManifestId, "The parent ACC must have been copied into the new release.");

        // The forwarded rows must re-anchor the CHILD onto the new release's copied ASCC/BCC manifests,
        // NOT keep pointing at the 'Working' child (the 'Working' manifests still exist and the FK is
        // RESTRICT, not unique, so a parent-only re-anchor would still insert a valid but wrong row).
        BigInteger workingAsccManifestId = workingOrders.stream()
                .filter(o -> o.getAsccManifestId() != null).findFirst().orElseThrow().getAsccManifestId();
        BigInteger workingBccManifestId = workingOrders.stream()
                .filter(o -> o.getBccManifestId() != null).findFirst().orElseThrow().getBccManifestId();
        BigInteger forwardedAsccManifestId =
                getAPIFactory().getBieViewOrderAPI().findForwardedAsccManifestId(workingAsccManifestId, newReleaseId);
        BigInteger forwardedBccManifestId =
                getAPIFactory().getBieViewOrderAPI().findForwardedBccManifestId(workingBccManifestId, newReleaseId);
        assertNotNull(forwardedAsccManifestId, "The ASCC child must have been copied into the new release.");
        assertNotNull(forwardedBccManifestId, "The BCC child must have been copied into the new release.");
        assertNotEquals(workingAsccManifestId, forwardedAsccManifestId,
                "The forwarded ASCC child must be a new-release manifest, not the 'Working' one.");
        assertNotEquals(workingBccManifestId, forwardedBccManifestId,
                "The forwarded BCC child must be a new-release manifest, not the 'Working' one.");

        List<BieViewOrderObject> forwardedOrders =
                getAPIFactory().getBieViewOrderAPI().getViewOrdersByFromAccManifestId(forwardedParentAccManifestId);
        assertEquals(2, forwardedOrders.size(), "Both view-order rows must be forwarded onto the new release.");
        assertTrue(forwardedOrders.stream().anyMatch(
                        o -> forwardedAsccManifestId.equals(o.getAsccManifestId()) && o.getBccManifestId() == null
                                && o.getWeight() == ASCC_WEIGHT),
                "The forwarded ASCC row must point to the new release's ASCC manifest with the preserved weight.");
        assertTrue(forwardedOrders.stream().anyMatch(
                        o -> forwardedBccManifestId.equals(o.getBccManifestId()) && o.getAsccManifestId() == null
                                && o.getWeight() == BCC_WEIGHT),
                "The forwarded BCC row must point to the new release's BCC manifest with the preserved weight.");
        // Release-scoped sanity: this release carries at least this test's two forwarded rows. Kept
        // as a lower bound (not ==2) because a shared DB may hold UI-authored weights on other
        // 'Working' ACCs that "Assign All" would also forward; the exact count is enforced
        // parent-scoped just above, and the post-rollback assertion below is exact (==0).
        assertTrue(getAPIFactory().getBieViewOrderAPI().getViewOrdersByRelease(newReleaseId).size() >= 2,
                "The new release must carry at least this test's two forwarded rows.");

        // 'Working' rows must be untouched by the forwarding.
        assertEquals(2, getAPIFactory().getBieViewOrderAPI().getViewOrdersByFromAccManifestId(parentAcc.getAccManifestId()).size(),
                "The 'Working' view-order rows must be untouched by forwarding.");

        // Roll the draft back to 'Initialized'.
        editReleasePage.openPage();
        editReleasePage.backToInitialized();
        waitForReleaseState(library, newReleaseNum, "Initialized");
        assertEquals("Initialized",
                getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, newReleaseNum).getState());

        // Only the new release's forwarded rows must be removed; the 'Working' rows must remain.
        assertEquals(0, getAPIFactory().getBieViewOrderAPI().getViewOrdersByRelease(newReleaseId).size(),
                "The forwarded rows must be removed when the release is rolled back.");
        assertEquals(2, getAPIFactory().getBieViewOrderAPI().getViewOrdersByFromAccManifestId(parentAcc.getAccManifestId()).size(),
                "The 'Working' view-order rows must survive the roll-back.");
    }

    /**
     * Poll the release's state (read straight from the DB the backend commits to) until it reaches
     * {@code expectedState}. Release drafting and roll-back both run asynchronously / behind an
     * atomic transaction, so this state flip is the barrier after which the {@code bie_view_order}
     * copy (or delete) is guaranteed committed.
     */
    private void waitForReleaseState(LibraryObject library, String releaseNum, String expectedState) {
        long timeout = Duration.ofSeconds(300L).toMillis();
        long begin = System.currentTimeMillis();
        while (System.currentTimeMillis() - begin < timeout) {
            String state = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, releaseNum).getState();
            if (expectedState.equals(state)) {
                return;
            }
            waitFor(Duration.ofSeconds(2L));
        }
        fail("Timed out waiting for release '" + releaseNum + "' to reach the '" + expectedState + "' state.");
    }

}
