package org.oagi.score.gateway.http.api.log_management.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;

import java.util.List;

/**
 * A human-oriented summary of a component revision (issue #1533, sub-task 3), shared by the
 * GitHub status auto-post (sub-task 4) and the revert-warning dialog (sub-task 5).
 * <p>
 * For a {@link ChangeSummaryType#NEW NEW} component (revision 1, or no resolvable prior revision)
 * the summary lists the current state: {@code fields} + {@code children} (or {@code values} for a code list or an agency ID list); the change buckets are
 * empty. For a {@link ChangeSummaryType#REVISED REVISED} component (revision 2+) it lists what
 * changed since the prior revision: {@code fieldChanges} + {@code childrenAdded}/{@code Removed}/
 * {@code Changed} (and {@code valuesAdded}/{@code Removed}/{@code Changed}); the state listing is empty. Lifecycle data (state, owner, timestamps) is never
 * part of the summary — only content changes are.
 * <p>
 * Computed on the fly from the current and prior {@code *DetailsRecord} pair (not from LOG
 * snapshots, which lack DT value domains among other fields); children are matched across
 * revisions by GUID.
 */
public record ComponentChangeSummary(
        CcType ccType,
        String name,
        String guid,
        int revisionNum,
        Integer prevRevisionNum,
        ChangeSummaryType summaryType,

        List<ComponentSummaryField> fields,
        List<ComponentChildItem> children,

        List<ComponentFieldChange> fieldChanges,
        List<ComponentChildItem> childrenAdded,
        List<ComponentChildItem> childrenRemoved,
        List<ComponentChildChange> childrenChanged,

        List<ComponentValueItem> values,
        List<ComponentValueItem> valuesAdded,
        List<ComponentValueItem> valuesRemoved,
        List<ComponentValueChange> valuesChanged) {

    public static ComponentChangeSummary newComponent(
            CcType ccType, String name, String guid, int revisionNum,
            List<ComponentSummaryField> fields, List<ComponentChildItem> children) {
        return new ComponentChangeSummary(ccType, name, guid, revisionNum, null, ChangeSummaryType.NEW,
                fields, children, List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of());
    }

    /** A {@code NEW} code list or agency ID list: its fields and its values. */
    public static ComponentChangeSummary newList(
            CcType ccType, String name, String guid, int revisionNum,
            List<ComponentSummaryField> fields, List<ComponentValueItem> values) {
        return new ComponentChangeSummary(ccType, name, guid, revisionNum, null, ChangeSummaryType.NEW,
                fields, List.of(), List.of(), List.of(), List.of(), List.of(),
                values, List.of(), List.of(), List.of());
    }

    public static ComponentChangeSummary revised(
            CcType ccType, String name, String guid, int revisionNum, Integer prevRevisionNum,
            List<ComponentFieldChange> fieldChanges,
            List<ComponentChildItem> childrenAdded,
            List<ComponentChildItem> childrenRemoved,
            List<ComponentChildChange> childrenChanged) {
        return new ComponentChangeSummary(ccType, name, guid, revisionNum, prevRevisionNum, ChangeSummaryType.REVISED,
                List.of(), List.of(), fieldChanges, childrenAdded, childrenRemoved, childrenChanged,
                List.of(), List.of(), List.of(), List.of());
    }

    /** A {@code REVISED} code list or agency ID list: its field changes and its value changes. */
    public static ComponentChangeSummary revisedList(
            CcType ccType, String name, String guid, int revisionNum, Integer prevRevisionNum,
            List<ComponentFieldChange> fieldChanges,
            List<ComponentValueItem> valuesAdded,
            List<ComponentValueItem> valuesRemoved,
            List<ComponentValueChange> valuesChanged) {
        return new ComponentChangeSummary(ccType, name, guid, revisionNum, prevRevisionNum, ChangeSummaryType.REVISED,
                List.of(), List.of(), fieldChanges, List.of(), List.of(), List.of(),
                List.of(), valuesAdded, valuesRemoved, valuesChanged);
    }

    /** {@code true} when a {@code REVISED} summary detected no content change at all. */
    @JsonIgnore
    public boolean isEmpty() {
        return fields.isEmpty() && children.isEmpty() && fieldChanges.isEmpty()
                && childrenAdded.isEmpty() && childrenRemoved.isEmpty() && childrenChanged.isEmpty()
                && values.isEmpty() && valuesAdded.isEmpty() && valuesRemoved.isEmpty() && valuesChanged.isEmpty();
    }
}
