package org.oagi.score.gateway.http.api.bie_management.service.state_transition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;

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
     * Stable row key shared by dialog rendering and validation responses.
     */
    private String nodeKey;
    /**
     * Kind of dependency node represented by this row.
     */
    private BieStateDependencyNodeType nodeType;
    /**
     * Top-level BIE represented by this row.
     *
     * <p>This id is the stable key shared across the dependency graph, dialog
     * row projection, validation responses, and final state update request.</p>
     */
    private TopLevelAsbiepId topLevelAsbiepId;
    /**
     * Code list represented by this row when {@link #nodeType} is
     * {@link BieStateDependencyNodeType#CODE_LIST}.
     */
    private CodeListManifestId codeListManifestId;
    /**
     * Direct prerequisite rows for this BIE inside the visible dependency graph.
     *
     * <p>These ids come from the graph-building step and represent the adjacent
     * parent nodes that this row depends on for the requested transition.</p>
     */
    private List<TopLevelAsbiepId> dependencyTopLevelAsbiepIds;
    /**
     * Direct graph parents whose transition path made this row relevant to the
     * current dialog.
     *
     * <p>The service returns these ids so the client can preserve the same
     * graph context across repeated validation requests.</p>
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
     * BIE DEN shown for BIE rows.
     */
    private String den;
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
     * Code list name shown for code-list rows.
     */
    private String name;
    /**
     * GUID of the target BIE or code list.
     *
     * <p>The dialog shows this beneath the row title so users can distinguish
     * rows that share the same business name.</p>
     */
    private String guid;
    /**
     * Owner login ID shown for both BIE and code list rows.
     */
    private String ownerLoginId;
    /**
     * Agency ID value shown for code list rows when available.
     */
    private String agencyId;
    /**
     * Agency ID display name shown as tooltip/context for code list rows.
     */
    private String agencyIdName;
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
     * <p>For BIE rows this comes from the top-level BIE summary; for code-list
     * rows it comes from the code list summary.</p>
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
    private String state;
    /**
     * Whether this row may be toggled in the dialog.
     */
    private boolean selectable;
    /**
     * Server-normalized checkbox state for the current dialog selection.
     *
     * <p>The UI sends selected ids to the validation endpoint, and the
     * transition service returns the authoritative checked state here.</p>
     */
    private boolean checked;
    /**
     * Blocking issues shown on this row.
     *
     * <p>Multiple issues may coexist, for example an ownership blocker plus a
     * direct state-compatibility requirement.</p>
     */
    private List<BieStateDependencyIssue> issues;
}
