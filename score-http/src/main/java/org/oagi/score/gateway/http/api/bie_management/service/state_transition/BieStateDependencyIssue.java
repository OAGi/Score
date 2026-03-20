package org.oagi.score.gateway.http.api.bie_management.service.state_transition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One blocking issue attached to a dependency dialog row.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BieStateDependencyIssue {
    /**
     * Machine-readable classification used by the UI to group and style the
     * blocking reason.
     */
    private BieStateDependencyIssueType type;

    /**
     * User-facing explanation for the blocking reason.
     */
    private String message;
}
