package org.oagi.score.gateway.http.api.bie_management.service;

public interface BieVisitContext {

    BieDocument getBieDocument();

    /** Immutable full source path. BBIE and BBIEP callbacks share the owning BCC path. */
    String getOccurrencePath();

    /**
     * Structural owner path, not necessarily the lexical parent of the path.
     * BBIE_SC points directly to its owning BBIE; the root has a null owner.
     */
    String getParentOccurrencePath();

}
