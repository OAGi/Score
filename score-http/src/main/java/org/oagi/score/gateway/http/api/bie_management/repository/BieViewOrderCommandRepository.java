package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.bie_management.model.BieViewOrderEntry;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;

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
}
