package org.oagi.score.gateway.http.api.bie_management.service;

/**
 * Directs whether a {@link BieVisitor} traversal descends into the children of the
 * node that was just visited. Modeled on {@link java.nio.file.FileVisitResult}.
 */
public enum BieVisitResult {

    /** Continue the traversal, descending into the visited node's children. */
    CONTINUE,

    /**
     * Do not descend into the visited node's children. For an ASBIE this skips its
     * to_asbiep subtree — used when the ASBIE is uplifted as a reuse reference, whose
     * subtree lives in the referenced top-level BIE and must not be re-traversed.
     */
    SKIP_SUBTREE
}
