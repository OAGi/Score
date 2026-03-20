package org.oagi.score.gateway.http.api.bie_management.service.state_transition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;

/**
 * Minimal relation metadata for one dependency entry shown in the dialog's
 * "Dependencies" column.
 *
 * <p>The current UI only renders related BIE links there, even for code-list
 * rows, so this model stays compact and BIE-oriented.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BieStateDependencyRelation {
    /**
     * Stable row key for the related dependency node.
     */
    private String nodeKey;
    /**
     * Kind of dependency node referenced by this relation.
     */
    private BieStateDependencyNodeType nodeType;
    /**
     * Related top-level BIE referenced by this adjacency entry.
     *
     * <p>This id links the UI-facing relation back to another
     * {@link BieStateDependencyTarget} row in the same dependency graph.</p>
     */
    private TopLevelAsbiepId topLevelAsbiepId;
    /**
     * Related code list referenced by this adjacency entry.
     */
    private CodeListManifestId codeListManifestId;
    /**
     * Directed dependency type for this adjacency.
     *
     * <p>The transition service uses the same relationship direction when it
     * builds rule-evaluation edges, so the dialog wording and rule wording stay
     * aligned.</p>
     */
    private BieStateTransitionDependency dependency;
    /**
     * User-facing name of the related BIE.
     *
     * <p>This label is rendered in the "Dependencies" column together with
     * {@link #dependency}.</p>
     */
    private String label;
    /**
     * GUID of the related BIE.
     *
     * <p>The current dialog does not display it directly, but the value stays
     * available for future disambiguation or linking needs.</p>
     */
    private String guid;
}
