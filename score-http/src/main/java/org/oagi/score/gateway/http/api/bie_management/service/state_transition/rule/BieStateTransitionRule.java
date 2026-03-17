package org.oagi.score.gateway.http.api.bie_management.service.state_transition.rule;

import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.service.state_transition.BieStateTransitionDependency;
import org.oagi.score.gateway.http.api.bie_management.service.state_transition.BieStateTransitionRuleViolationException;

/**
 * Contract for a single state transition rule between two top-level BIEs.
 *
 * <p>The long-term goal is to move the current hard-coded validation branches
 * in the dependency graph builder into a small set of explicit rule
 * implementations. Each rule evaluates one dependency edge between two
 * top-level ASBIEPs for a requested future state and can either accept the
 * transition or throw a violation exception.</p>
 *
 * <p>The rule is evaluated from the point of view of the {@code source}
 * top-level ASBIEP attempting to move to {@code sourceFutureState}. The
 * {@code target} top-level ASBIEP is the BIE connected by the supplied
 * {@code dependency} edge.</p>
 */
public interface BieStateTransitionRule {

    /**
     * Validates whether the source BIE may move to the requested future state
     * with respect to one connected target BIE.
     *
     * @param source the top-level ASBIEP whose state transition is being
     *               evaluated
     * @param target the connected top-level ASBIEP referenced by the dependency
     *               edge
     * @param dependency the business dependency type between {@code source} and
     *                   {@code target}
     * @param sourceFutureState the destination state requested for
     *                          {@code source}
     * @param targetFutureState the destination state requested for
     *                          {@code target}. This may be the target's current
     *                          state when the target is not changing.
     * @throws BieStateTransitionRuleViolationException when the rule does not
     *                                               allow the transition
     */
    void validate(TopLevelAsbiepSummaryRecord source,
                  TopLevelAsbiepSummaryRecord target,
                  BieStateTransitionDependency dependency,
                  BieState sourceFutureState,
                  BieState targetFutureState) throws BieStateTransitionRuleViolationException;
}
