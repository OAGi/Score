package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.bie_management.model.BieViewOrderEntry;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;

import java.util.List;

public interface BieViewOrderQueryRepository {

    /**
     * The sibling view-order weights stored under a single view-parent ACC. The frontend fetches
     * these lazily, one view parent at a time, as each ACC node is expanded — the view parent is
     * known only on the client (group/choice flattening happens there). An empty list means that
     * parent keeps its current (seq_key + attributes-first) order.
     */
    List<BieViewOrderEntry> findByFromAccManifestId(AccManifestId fromAccManifestId);
}
