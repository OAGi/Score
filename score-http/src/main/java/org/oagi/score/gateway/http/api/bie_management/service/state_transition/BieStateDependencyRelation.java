package org.oagi.score.gateway.http.api.bie_management.service.state_transition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;

/**
 * Minimal relation metadata for a BIE that appears in the state dependency graph.
 *
 * <p>The dependency dialog only needs an id, label, and GUID for linked rows and
 * tooltips, so this model intentionally stays compact.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BieStateDependencyRelation {
    /**
     * Related top-level BIE referenced by this adjacency entry.
     *
     * <p>This id links the UI-facing relation back to another
     * {@link BieStateDependencyTarget} row in the same dependency graph.</p>
     */
    private TopLevelAsbiepId topLevelAsbiepId;
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
     * <p>This label is rendered in the "Dependencies" column and its tooltip,
     * together with {@link #dependency}.</p>
     */
    private String label;
    /**
     * GUID of the related BIE.
     *
     * <p>The current dialog mostly hides GUIDs in tooltips, but the value
     * remains available so the workflow can still disambiguate duplicate names
     * when needed.</p>
     */
    private String guid;
}
