package org.oagi.score.gateway.http.api.bie_management.service;

import org.oagi.score.gateway.http.api.bie_management.model.BieViewOrderEntry;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read/write of the instance-level sibling view order (Issue #1638).
 *
 * <p>The write path is DEVELOPER-ONLY and has NO lifecycle gate (it applies on any release state,
 * immediately, with no revision/log/CC mutation). The read path is keyed by the VIEW-PARENT ACC
 * manifest id: the client fetches one parent at a time, lazily, as each ACC node is expanded —
 * group/choice flattening happens on the client, so only it knows the view parent of each child.
 */
@Service
public class BieViewOrderService {

    private final RepositoryFactory repositoryFactory;

    public BieViewOrderService(RepositoryFactory repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    /**
     * The sibling view-order weights stored under one view-parent ACC. Available to any viewer
     * (model browsing and BIE editing both display the order); an empty list means that parent keeps
     * its current seq_key order.
     */
    public List<BieViewOrderEntry> getViewOrder(ScoreUser requester, AccManifestId fromAccManifestId) {
        return repositoryFactory.bieViewOrderQueryRepository(requester).findByFromAccManifestId(fromAccManifestId);
    }

    /**
     * Upsert the affected child weights under a view parent. Developer-only; no state gate.
     * Transactional so a single reorder PUT (which may carry several entries) commits all-or-nothing,
     * matching the rest of {@code bie_management}.
     */
    @Transactional
    public void updateViewOrder(ScoreUser requester, AccManifestId fromAccManifestId, List<BieViewOrderEntry> entries) {
        if (!requester.isDeveloper()) {
            throw new AccessDeniedException("Only developers can change the sibling view order.");
        }
        // No WIP/Published state check by design — the view order is release-/lifecycle-decoupled.
        repositoryFactory.bieViewOrderCommandRepository(requester).upsert(fromAccManifestId, entries);
        // No makeLog(), no revision, no CC mutation by design.
    }

    /**
     * Remove ALL child weights stored directly under one view-parent ACC — the model browser's
     * root-node "Reset Order Weights". Developer-only; no state gate (mirrors {@link #updateViewOrder}).
     * Deletes only rows whose {@code from_acc_manifest_id} is this ACC, so children reordered under
     * deeper nested ACCs keep their order. Idempotent: a no-op when the parent has no weights.
     */
    @Transactional
    public void resetViewOrder(ScoreUser requester, AccManifestId fromAccManifestId) {
        if (!requester.isDeveloper()) {
            throw new AccessDeniedException("Only developers can change the sibling view order.");
        }
        // No WIP/Published state check by design — the view order is release-/lifecycle-decoupled.
        repositoryFactory.bieViewOrderCommandRepository(requester).deleteByFromAccManifestId(fromAccManifestId);
        // No makeLog(), no revision, no CC mutation by design.
    }
}
