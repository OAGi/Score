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
 *     <li>{@link #USES_CODE_LIST}: source uses target code list.</li>
 *     <li>{@link #USED_BY_BIE}: source code list is used by target BIE.</li>
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
    IS_A_BASED_OF,

    /**
     * Source BIE uses target code list.
     */
    USES_CODE_LIST,

    /**
     * Source code list is used by target BIE.
     */
    USED_BY_BIE
}
