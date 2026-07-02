package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.bie_management.model.BieViewOrderEntry;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

import java.util.List;

public interface BieViewOrderCommandRepository {

    /**
     * Upsert the given child weights under {@code fromAccManifestId} (the view parent).
     * An entry with a {@code null} weight deletes that child's row (reset to seq_key position).
     * Each entry must carry exactly one of {@code asccManifestId} / {@code bccManifestId}.
     */
    void upsert(AccManifestId fromAccManifestId, List<BieViewOrderEntry> entries);

    /**
     * Remove all view-order rows for a view parent (full reset) — backs the model browser's
     * root-node "Reset Order Weights" and test cleanup. Idempotent.
     */
    void deleteByFromAccManifestId(AccManifestId fromAccManifestId);

    /**
     * Forward the {@code Working} release's view-order rows onto the corresponding manifests that were
     * just copied into {@code targetReleaseId} when a new release is drafted (issue #1638). Order
     * weighting is authored on {@code Working}; each drafted manifest records the {@code Working} manifest
     * it was copied from via its {@code NEXT_*_MANIFEST_ID}, so a row is re-anchored by remapping its
     * view-parent ACC and its ASCC/BCC child through that pointer. A row whose components were not
     * included in the release is naturally skipped (inner join), so no dangling reference is created.
     * Idempotent in practice: the target manifests are freshly created, so their rows do not yet exist.
     */
    void copyFromWorking(ReleaseId targetReleaseId);

    /**
     * Remove every view-order row that references any manifest belonging to {@code releaseId}. Called
     * before a drafted release's component manifests are deleted (release roll-back, issue #1638) so the
     * FKs to {@code acc_manifest} / {@code ascc_manifest} / {@code bcc_manifest} are never violated.
     * Idempotent.
     */
    void deleteByRelease(ReleaseId releaseId);
}
