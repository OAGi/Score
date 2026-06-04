package org.oagi.score.gateway.http.api.bie_management.service;


import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.Abie;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.Asbie;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.Asbiep;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.Bbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieSc;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.Bbiep;

/**
 * Callback interface for traversing the nodes of a BIE (Business Information Entity) tree,
 * driven by {@link BieDocument#accept(BieVisitor)} in depth-first order.
 *
 * <p>Each node is visited, after which the traversal descends into that node's children.
 * The node-visit methods return a {@link BieVisitResult} that controls this descent,
 * mirroring {@link java.nio.file.FileVisitor}: {@link BieVisitResult#CONTINUE} descends
 * into the node's children, whereas {@link BieVisitResult#SKIP_SUBTREE} skips them. The
 * lifecycle callbacks {@link #visitStart} and {@link #visitEnd} bracket the whole
 * traversal and do not influence descent.
 *
 * <p>Every method has a {@code default} no-op implementation, so an implementation may
 * override only the callbacks it cares about.
 */
public interface BieVisitor {

    /**
     * Invoked once at the start of the traversal, before any node is visited.
     *
     * @param topLevelAsbiep the top-level ASBIEP whose tree is being traversed
     * @param context        the traversal context
     */
    default void visitStart(TopLevelAsbiepSummaryRecord topLevelAsbiep, BieVisitContext context) {}

    /**
     * Invoked once at the end of the traversal, after every node has been visited.
     *
     * @param topLevelAsbiep the top-level ASBIEP whose tree was traversed
     * @param context        the traversal context
     */
    default void visitEnd(TopLevelAsbiepSummaryRecord topLevelAsbiep, BieVisitContext context) {}

    /**
     * Visits an ABIE (aggregate BIE).
     *
     * @param abie    the ABIE being visited
     * @param context the traversal context
     * @return {@link BieVisitResult#CONTINUE} to descend into the ABIE's child associations
     *         (its ASBIEs and BBIEs), or {@link BieVisitResult#SKIP_SUBTREE} to skip them
     */
    default BieVisitResult visitAbie(Abie abie, BieVisitContext context) {
        return BieVisitResult.CONTINUE;
    }

    /**
     * Visits an ASBIE (association BIE).
     *
     * @param asbie   the ASBIE being visited
     * @param context the traversal context
     * @return {@link BieVisitResult#CONTINUE} to descend into the ASBIE's to_asbiep subtree,
     *         or {@link BieVisitResult#SKIP_SUBTREE} to skip it — e.g. when the ASBIE is
     *         uplifted as a reuse reference, whose subtree lives in the referenced top-level
     *         BIE and must not be re-traversed
     */
    default BieVisitResult visitAsbie(Asbie asbie, BieVisitContext context) {
        return BieVisitResult.CONTINUE;
    }

    /**
     * Visits a BBIE (basic BIE).
     *
     * @param bbie    the BBIE being visited
     * @param context the traversal context
     * @return {@link BieVisitResult#CONTINUE} to descend into the BBIE's BBIEP (and the
     *         supplementary components beneath it), or {@link BieVisitResult#SKIP_SUBTREE}
     *         to skip them
     */
    default BieVisitResult visitBbie(Bbie bbie, BieVisitContext context) {
        return BieVisitResult.CONTINUE;
    }

    /**
     * Visits an ASBIEP (associated BIE property).
     *
     * @param asbiep  the ASBIEP being visited
     * @param context the traversal context
     * @return {@link BieVisitResult#CONTINUE} to descend into the ASBIEP's role-of ABIE,
     *         or {@link BieVisitResult#SKIP_SUBTREE} to skip it
     */
    default BieVisitResult visitAsbiep(Asbiep asbiep, BieVisitContext context) {
        return BieVisitResult.CONTINUE;
    }

    /**
     * Visits a BBIEP (basic BIE property).
     *
     * @param bbiep   the BBIEP being visited
     * @param context the traversal context
     * @return {@link BieVisitResult#CONTINUE} to descend into the BBIEP's supplementary
     *         components (BBIE_SCs), or {@link BieVisitResult#SKIP_SUBTREE} to skip them
     */
    default BieVisitResult visitBbiep(Bbiep bbiep, BieVisitContext context) {
        return BieVisitResult.CONTINUE;
    }

    /**
     * Visits a BBIE_SC (supplementary component of a BBIE). A BBIE_SC is a leaf node with no
     * children, so the returned result is informational and does not affect the traversal.
     *
     * @param bbieSc  the BBIE_SC being visited
     * @param context the traversal context
     * @return {@link BieVisitResult#CONTINUE}
     */
    default BieVisitResult visitBbieSc(BbieSc bbieSc, BieVisitContext context) {
        return BieVisitResult.CONTINUE;
    }

}
