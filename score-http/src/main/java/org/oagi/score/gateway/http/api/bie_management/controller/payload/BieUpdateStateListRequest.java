package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;

import java.util.List;

/**
 * Bulk-BIE state transition request issued from the list page.
 */
@Data
public class BieUpdateStateListRequest {
    /** UI action label used by the list workflow. */
    private String action;
    /** Destination state requested for every selected root row. */
    private BieState toState;
    /** Root BIE ids selected on the list page. */
    private List<TopLevelAsbiepId> topLevelAsbiepIds;
    /** Dependency rows approved by the user for the bulk transition. */
    private List<TopLevelAsbiepId> dependencyTopLevelAsbiepIds;
}
