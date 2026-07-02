package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.ACCObject;
import org.oagi.score.e2e.obj.ASCCObject;
import org.oagi.score.e2e.obj.BCCObject;
import org.oagi.score.e2e.obj.BieViewOrderObject;

import java.math.BigInteger;
import java.util.List;

/**
 * APIs for the sibling view-order weights ({@code bie_view_order}, issue #1638). Used by the
 * release-forwarding e2e tests to author weights on the 'Working' release and to read them back
 * per release / per view-parent ACC.
 */
public interface BieViewOrderAPI {

    /**
     * Store a view-order weight for an ASCC child under its view-parent ACC. The child's
     * {@code ascc_manifest_id} is resolved from the association's {@code ascc_id} within the
     * parent's release.
     *
     * @param fromAcc the view-parent ACC (supplies {@code from_acc_manifest_id} and the release)
     * @param ascc    the reordered ASCC association
     * @param weight  the sort weight
     */
    void setAsccViewOrder(ACCObject fromAcc, ASCCObject ascc, int weight);

    /**
     * Store a view-order weight for a BCC child under its view-parent ACC. The child's
     * {@code bcc_manifest_id} is resolved from the association's {@code bcc_id} within the
     * parent's release.
     *
     * @param fromAcc the view-parent ACC (supplies {@code from_acc_manifest_id} and the release)
     * @param bcc     the reordered BCC association
     * @param weight  the sort weight
     */
    void setBccViewOrder(ACCObject fromAcc, BCCObject bcc, int weight);

    /**
     * Return every view-order row stored directly under the given view-parent ACC manifest.
     *
     * @param fromAccManifestId the view-parent ACC manifest ID
     * @return the view-order rows (may be empty)
     */
    List<BieViewOrderObject> getViewOrdersByFromAccManifestId(BigInteger fromAccManifestId);

    /**
     * Delete every view-order row stored directly under the given view-parent ACC manifest. Used by
     * tests to clean up the rows they author on 'Working' (whose FK targets survive account cleanup),
     * so no orphan rows are left behind on a shared e2e DB.
     *
     * @param fromAccManifestId the view-parent ACC manifest ID
     */
    void deleteViewOrdersByFromAccManifestId(BigInteger fromAccManifestId);

    /**
     * Delete every view-order row scoped to a release (whose view-parent ACC manifest lives in the
     * release). Used by tests to clean up any rows forwarded into a drafted release if the test
     * aborts before rolling the draft back.
     *
     * @param releaseId the release ID
     */
    void deleteViewOrdersByRelease(BigInteger releaseId);

    /**
     * Return every view-order row scoped to a release, i.e. whose view-parent ACC manifest lives in
     * the given release. This matches the server-side {@code deleteByRelease} scope and is used to
     * assert that a release's forwarded rows appear (on draft) and disappear (on roll-back).
     *
     * @param releaseId the release ID
     * @return the view-order rows scoped to that release (may be empty)
     */
    List<BieViewOrderObject> getViewOrdersByRelease(BigInteger releaseId);

    /**
     * Resolve the ACC manifest in {@code releaseId} that was forwarded from the given 'Working'
     * ACC manifest, following the {@code next_acc_manifest_id} back-pointer set when a release draft
     * copies the 'Working' manifests.
     *
     * @param workingAccManifestId the 'Working' ACC manifest ID
     * @param releaseId            the target (new) release ID
     * @return the forwarded ACC manifest ID, or {@code null} if the ACC was not copied into the release
     */
    BigInteger findForwardedAccManifestId(BigInteger workingAccManifestId, BigInteger releaseId);

    /**
     * Resolve the ASCC manifest in {@code releaseId} forwarded from the given 'Working' ASCC
     * manifest, following the {@code next_ascc_manifest_id} back-pointer. Used to assert that a
     * forwarded view-order row's child was re-anchored onto the new release (not left pointing at
     * the 'Working' manifest).
     *
     * @param workingAsccManifestId the 'Working' ASCC manifest ID
     * @param releaseId             the target (new) release ID
     * @return the forwarded ASCC manifest ID, or {@code null} if the ASCC was not copied into the release
     */
    BigInteger findForwardedAsccManifestId(BigInteger workingAsccManifestId, BigInteger releaseId);

    /**
     * Resolve the BCC manifest in {@code releaseId} forwarded from the given 'Working' BCC manifest,
     * following the {@code next_bcc_manifest_id} back-pointer.
     *
     * @param workingBccManifestId the 'Working' BCC manifest ID
     * @param releaseId            the target (new) release ID
     * @return the forwarded BCC manifest ID, or {@code null} if the BCC was not copied into the release
     */
    BigInteger findForwardedBccManifestId(BigInteger workingBccManifestId, BigInteger releaseId);
}
