package org.oagi.score.gateway.http.api.bie_management.service.state_transition;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;

/**
 * Directed dependency edge used by BIE state transition validation.
 *
 * <p>The source and target are ordered to match the validation rule for the
 * supplied dependency type.</p>
 */
public record BieStateTransitionEdge(
        TopLevelAsbiepId sourceTopLevelAsbiepId,
        TopLevelAsbiepId targetTopLevelAsbiepId,
        BieStateTransitionDependency dependency) {
}
