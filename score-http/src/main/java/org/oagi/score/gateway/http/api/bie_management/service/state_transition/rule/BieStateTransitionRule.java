package org.oagi.score.gateway.http.api.bie_management.service.state_transition.rule;

import org.oagi.score.gateway.http.api.bie_management.service.state_transition.BieStateTransitionDependency;
import org.oagi.score.gateway.http.api.bie_management.service.state_transition.BieStateTransitionRuleViolationException;

/**
 * Contract for validating one directed dependency edge against projected
 * future states.
 *
 * <p>The source and target can represent different record types, such as
 * BIE-to-BIE or BIE-to-code-list validation. Implementations only answer
 * whether the supplied edge is compatible; the calling service owns message
 * construction and row-level issue aggregation.</p>
 */
public interface BieStateTransitionRule {

    /**
     * Validates whether the directed dependency edge is compatible with the
     * projected future states of both participants.
     *
     * @param source the source participant, including its projected future
     *               state
     * @param target the target participant, including its projected future
     *               state
     * @param dependency the business dependency type between {@code source} and
     *                   {@code target}
     * @throws BieStateTransitionRuleViolationException when the rule does not
     *         allow the transition
     */
    void validate(FutureStateCarrier<?, ?> source,
                  FutureStateCarrier<?, ?> target,
                  BieStateTransitionDependency dependency)
            throws BieStateTransitionRuleViolationException;
}
