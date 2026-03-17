package org.oagi.score.gateway.http.api.bie_management.service.state_transition;

/**
 * Supported dependency edge kinds in the BIE state transition graph.
 *
 * <p>The BIE state transition validator models each business relationship as a
 * directed edge so messages and validation remain clear from the perspective of
 * the affected BIE row.</p>
 *
 * <ul>
 *     <li>{@link #REUSES}: source reuses target.</li>
 *     <li>{@link #REUSED_BY}: source is reused by target.</li>
 *     <li>{@link #INHERITS_FROM}: source inherits from target.</li>
 *     <li>{@link #IS_A_BASED_OF}: source is a base of target.</li>
 * </ul>
 */
public enum BieStateTransitionDependency {
    /**
     * Source reuses target.
     */
    REUSES,

    /**
     * Source is reused by target.
     */
    REUSED_BY,

    /**
     * Source inherits from target.
     */
    INHERITS_FROM,

    /**
     * Source is a base of target.
     */
    IS_A_BASED_OF
}
