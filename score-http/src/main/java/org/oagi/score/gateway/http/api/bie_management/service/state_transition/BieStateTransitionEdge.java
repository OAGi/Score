package org.oagi.score.gateway.http.api.bie_management.service.state_transition;

/**
 * Directed dependency edge used by BIE state transition validation.
 *
 * <p>The source and target are ordered to match the validation rule for the
 * supplied dependency type.</p>
 */
public record BieStateTransitionEdge<S, T>(
        /**
         * Source node identifier in rule-evaluation order.
         */
        S source,
        /**
         * Target node identifier in rule-evaluation order.
         */
        T target,
        /**
         * Relationship semantics for the directed edge.
         */
        BieStateTransitionDependency dependency) {
}
