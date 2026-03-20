package org.oagi.score.gateway.http.api.bie_management.service.state_transition;

/**
 * Categorizes one blocking issue shown on a dependency dialog row.
 */
public enum BieStateDependencyIssueType {
    /**
     * The row cannot be updated by the current requester because ownership
     * rules block the cascade.
     */
    OWNERSHIP,

    /**
     * The row's own persisted or projected state is not compatible with the
     * requested transition.
     */
    STATE_COMPATIBILITY,

    /**
     * The row is blocked because one of its related dependency rows still has
     * unresolved issues.
     */
    DEPENDENCY_CONFLICT
}
