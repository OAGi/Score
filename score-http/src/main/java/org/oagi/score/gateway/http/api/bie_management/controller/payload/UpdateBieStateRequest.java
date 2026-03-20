package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;

import java.util.List;

/**
 * Single-BIE state transition request.
 */
@Data
public class UpdateBieStateRequest {
    /** Destination state requested by the user. */
    private BieState state;
    /** Dependency rows approved in the dialog for the final state update. */
    private List<TopLevelAsbiepId> dependencyTopLevelAsbiepIds;
    /** Code list dependency rows approved in the dialog for the final state update. */
    private List<CodeListManifestId> dependencyCodeListManifestIds;
}
