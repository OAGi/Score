package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;

import java.util.List;

/**
 * Request body for dependency preview and dependency selection validation.
 *
 * <p>The preview endpoint reads the root ids and target state. The validation
 * endpoint additionally uses the selected dependency ids that came back from the
 * dialog.</p>
 */
@Data
public class BieStateDependenciesRequest {
    /** Destination state requested by the user. */
    private BieState state;
    /** Root BIE ids whose dependency graph should be evaluated. */
    private List<TopLevelAsbiepId> topLevelAsbiepIds;
    /** Optional requested code list ids whose dependency graph should be evaluated. */
    private List<CodeListManifestId> requestedCodeListManifestIds;
    /** Dependency rows currently checked in the dialog. */
    private List<TopLevelAsbiepId> selectedTopLevelAsbiepIds;
    /** Code list dependency rows currently checked in the dialog. */
    private List<CodeListManifestId> selectedCodeListManifestIds;
}
