package org.oagi.score.gateway.http.api.bie_management.service.state_transition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;

import java.util.List;

/**
 * Full dependency row returned to the state transition dialog.
 *
 * <p>This model is shared by the initial dependency preview and the follow-up
 * validation calls that occur whenever the user changes checkbox selection.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BieStateDependencyTarget {
    /**
     * Top-level BIE represented by this row.
     *
     * <p>This id is the stable key shared across the dependency graph, dialog
     * row projection, validation responses, and final state update request.</p>
     */
    private TopLevelAsbiepId topLevelAsbiepId;
    /**
     * Direct prerequisite rows for this BIE inside the visible dependency graph.
     *
     * <p>These ids come from the graph-building step and represent the adjacent
     * parent nodes that this row depends on for the requested transition.</p>
     */
    private List<TopLevelAsbiepId> dependencyTopLevelAsbiepIds;
    /**
     * Direct graph parents whose state change makes this row mandatory.
     *
     * <p>The selection-normalization step checks these ids against the current
     * active-changing set to decide whether an unchecked row becomes a
     * selection conflict.</p>
     */
    private List<TopLevelAsbiepId> requiredDependencyTopLevelAsbiepIds;
    /**
     * UI-facing dependency relations for the "Dependencies" column.
     *
     * <p>Each relation is derived from the same graph as
     * {@link #dependencyTopLevelAsbiepIds} but is projected with labels and
     * dependency types for rendering and tooltips.</p>
     */
    private List<BieStateDependencyRelation> dependencies;
    /**
     * Shortest edge distance from any requested root BIE to this row.
     *
     * <p>The dialog uses this to keep nearby dependencies ordered ahead of
     * deeper ones.</p>
     */
    private int edgeDistance;
    /**
     * Raw property term from the underlying top-level BIE summary.
     *
     * <p>Used as the fallback display text when {@link #displayName} is empty.</p>
     */
    private String propertyTerm;
    /**
     * Preferred row title shown in the dialog when present.
     *
     * <p>This value is taken from the top-level BIE summary and is paired with
     * {@link #propertyTerm} and {@link #guid} so duplicate names remain
     * understandable to the user.</p>
     */
    private String displayName;
    /**
     * GUID of the target BIE.
     *
     * <p>The dialog shows this beneath the display name so users can
     * distinguish rows that share the same business name.</p>
     */
    private String guid;
    /**
     * Business context names assigned to this top-level BIE.
     *
     * <p>These are loaded during row projection and shown as read-only
     * descriptive metadata in the dialog table.</p>
     */
    private List<String> businessContexts;
    /**
     * Business version shown for the row.
     *
     * <p>This comes from the top-level BIE summary and is displayed to help
     * users identify which dependency instance they are updating.</p>
     */
    private String version;
    /**
     * Business status shown for the row.
     *
     * <p>This is descriptive row metadata only; it does not drive the state
     * transition rules directly.</p>
     */
    private String status;
    /**
     * Free-form remark loaded from the owning ASBIEP summary.
     *
     * <p>This is surfaced in the table so multiple reused or inherited rows can
     * be distinguished by users before they select them.</p>
     */
    private String remark;
    /**
     * Current persisted state before the requested transition is applied.
     *
     * <p>The transition service combines this with the requested next state and
     * the checkbox selection to build the simulated future-state map.</p>
     */
    private BieState state;
    /**
     * Whether this row is allowed to change with the root BIE.
     *
     * <p>This is primarily driven by same-owner and path eligibility checks in
     * the dependency graph traversal. The UI uses it to enable or disable the
     * row checkbox.</p>
     */
    private boolean dependencyUpdateAllowed;
    /**
     * Explanation shown when {@link #dependencyUpdateAllowed} is false.
     *
     * <p>This communicates ownership/path restrictions independently of the
     * rule-based state transition validation.</p>
     */
    private String dependencyUpdateMessage;
    /**
     * Whether the simulated future state for this row currently satisfies all
     * applicable state transition rules.
     *
     * <p>This value is first seeded from preview checks and then recalculated on
     * every validation request after checkbox changes.</p>
     */
    private boolean stateTransitionAllowed;
    /**
     * User-facing explanation for a failed rule or propagated dependency
     * conflict on this row.
     *
     * <p>This is the main row-level blocking message shown in the dialog.</p>
     */
    private String stateTransitionMessage;
    /**
     * Server-normalized checkbox state for the current dialog selection.
     *
     * <p>The UI sends selected ids to the validation endpoint, and the
     * transition service returns the authoritative checked state here.</p>
     */
    private boolean checked;
    /**
     * Whether this row is mandatory for the current selection but is not
     * checked.
     *
     * <p>This is computed from {@link #requiredDependencyTopLevelAsbiepIds} and
     * the current active-changing set.</p>
     */
    private boolean selectionConflict;
    /**
     * User-facing message for an unchecked required row.
     *
     * <p>This is separate from {@link #stateTransitionMessage} so the dialog can
     * distinguish rule violations from plain selection omissions.</p>
     */
    private String selectionConflictMessage;
}
