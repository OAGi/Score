package org.oagi.score.gateway.http.api.log_management.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.log_management.model.ComponentChangeSummary;
import org.oagi.score.gateway.http.api.log_management.model.ComponentChildChange;
import org.oagi.score.gateway.http.api.log_management.model.ComponentChildSummary;
import org.oagi.score.gateway.http.api.log_management.model.ComponentFieldChange;
import org.oagi.score.gateway.http.api.log_management.model.ComponentSummaryField;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.log_management.service.ComponentChangeSummaryBuilder.GuidDiff;
import static org.oagi.score.gateway.http.api.log_management.service.ComponentChangeSummaryBuilder.GuidPair;
import static org.oagi.score.gateway.http.api.log_management.service.ComponentChangeSummaryBuilder.addChange;
import static org.oagi.score.gateway.http.api.log_management.service.ComponentChangeSummaryBuilder.addField;
import static org.oagi.score.gateway.http.api.log_management.service.ComponentChangeSummaryBuilder.diffByGuid;
import static org.oagi.score.gateway.http.api.log_management.service.ComponentChangeSummaryBuilder.norm;

/**
 * Diffs two LOG snapshot documents of the same component into a {@link ComponentChangeSummary}
 * (issue #1533) — the engine behind the log compare view's summary, where the user picks the two
 * revisions to compare, so the baseline is exactly what they selected (e.g. the last commit of the
 * previous revision vs the latest state).
 * <p>
 * The snapshot JSON is the {@code LogSerializer} output: per-type scalar keys plus GUID-keyed child
 * arrays ({@code associations}, {@code supplementaryComponents}, {@code values}). Children are
 * matched by GUID; lifecycle data ({@code state}, {@code ownerUser}, {@code _metadata}) is not
 * compared. Snapshot limitations apply, matching the compare view itself: DT value domains, the
 * supplementary components' deprecated flag and the agency ID list remark are not stored in
 * snapshots and therefore cannot be diffed here.
 */
final class SnapshotChangeSummaryBuilder {

    private SnapshotChangeSummaryBuilder() {
    }

    /**
     * Builds the summary of {@code afterSnapshot} relative to {@code beforeSnapshot}.
     *
     * @param beforeSnapshot  the older LOG snapshot JSON.
     * @param afterSnapshot   the newer LOG snapshot JSON.
     * @param revisionNum     the newer log row's revision number.
     * @param prevRevisionNum the older log row's revision number.
     * @throws IllegalArgumentException when the snapshots are not comparable (different or unknown components).
     */
    static ComponentChangeSummary diff(String beforeSnapshot, String afterSnapshot,
                                       int revisionNum, int prevRevisionNum) {
        JsonObject before = JsonParser.parseString(beforeSnapshot).getAsJsonObject();
        JsonObject after = JsonParser.parseString(afterSnapshot).getAsJsonObject();

        String component = str(after, "component");
        if (component == null || !component.equals(str(before, "component"))) {
            throw new IllegalArgumentException("The selected log entries hold different component types.");
        }

        return switch (component) {
            case "acc" -> acc(before, after, revisionNum, prevRevisionNum);
            case "asccp" -> asccp(before, after, revisionNum, prevRevisionNum);
            case "bccp" -> bccp(before, after, revisionNum, prevRevisionNum);
            case "dt" -> dt(before, after, revisionNum, prevRevisionNum);
            case "codeList" -> codeList(before, after, revisionNum, prevRevisionNum);
            case "agencyIdList" -> agencyIdList(before, after, revisionNum, prevRevisionNum);
            default -> throw new IllegalArgumentException(
                    "Unsupported component type for a change summary: " + component);
        };
    }

    /**
     * Builds a {@code NEW}-type summary (the component's current definition) from a single LOG
     * snapshot — used for a revision-1 component, which has no baseline to diff against.
     *
     * @param snapshot    the LOG snapshot JSON.
     * @param revisionNum the log row's revision number.
     */
    static ComponentChangeSummary initial(String snapshot, int revisionNum) {
        JsonObject doc = JsonParser.parseString(snapshot).getAsJsonObject();
        String component = str(doc, "component");
        if (component == null) {
            throw new IllegalArgumentException("The log entry holds no component snapshot.");
        }

        List<ComponentSummaryField> fields = new ArrayList<>();
        List<ComponentChildSummary> children = new ArrayList<>();
        CcType ccType;
        String name;
        switch (component) {
            case "acc" -> {
                ccType = CcType.ACC;
                name = accName(doc);
                addField(fields, "Object Class Term", str(doc, "objectClassTerm"));
                addField(fields, "Object Class Qualifier", str(doc, "objectClassQualifier"));
                addField(fields, "Component Type", componentTypeName(str(doc, "componentType")));
                addFlag(fields, "Abstract", bool(doc, "abstract"));
                addFlag(fields, "Deprecated", bool(doc, "deprecated"));
                addField(fields, "Based ACC", nestedStr(doc, "basedAcc", "den"));
                addField(fields, "Namespace", nestedStr(doc, "namespace", "uri"));
                addField(fields, "Definition", str(doc, "definition"));
                addField(fields, "Definition Source", str(doc, "definitionSource"));
                objects(doc, "associations").forEach(a -> children.add(associationSummary(a)));
            }
            case "asccp" -> {
                ccType = CcType.ASCCP;
                String den = metadataDen(doc, "asccpManifest");
                name = (den != null) ? den : str(doc, "propertyTerm");
                addField(fields, "Property Term", str(doc, "propertyTerm"));
                addField(fields, "Role of ACC", nestedStr(doc, "roleOfAcc", "den"));
                addFlag(fields, "Reusable", bool(doc, "reusable"));
                addFlag(fields, "Nillable", bool(doc, "nillable"));
                addFlag(fields, "Deprecated", bool(doc, "deprecated"));
                addField(fields, "Namespace", nestedStr(doc, "namespace", "uri"));
                addField(fields, "Definition", str(doc, "definition"));
                addField(fields, "Definition Source", str(doc, "definitionSource"));
            }
            case "bccp" -> {
                ccType = CcType.BCCP;
                String den = metadataDen(doc, "bccpManifest");
                name = (den != null) ? den : str(doc, "propertyTerm");
                addField(fields, "Property Term", str(doc, "propertyTerm"));
                addField(fields, "Data Type", nestedStr(doc, "bdt", "den"));
                addField(fields, "Representation Term", str(doc, "representationTerm"));
                addField(fields, "Value Constraint", valueConstraint(doc));
                addFlag(fields, "Nillable", bool(doc, "nillable"));
                addFlag(fields, "Deprecated", bool(doc, "deprecated"));
                addField(fields, "Namespace", nestedStr(doc, "namespace", "uri"));
                addField(fields, "Definition", str(doc, "definition"));
                addField(fields, "Definition Source", str(doc, "definitionSource"));
            }
            case "dt" -> {
                ccType = CcType.DT;
                String den = metadataDen(doc, "dtManifest");
                name = (den != null) ? den : str(doc, "dataTypeTerm");
                addField(fields, "Data Type Term", str(doc, "dataTypeTerm"));
                addField(fields, "Qualifier", str(doc, "qualifier"));
                addField(fields, "Representation Term", str(doc, "representationTerm"));
                addField(fields, "Based Data Type", nestedStr(doc, "basedDt", "den"));
                addField(fields, "Six Hexadecimal Identifier", str(doc, "sixDigitId"));
                addFlag(fields, "Deprecated", bool(doc, "deprecated"));
                addField(fields, "Namespace", nestedStr(doc, "namespace", "uri"));
                addField(fields, "Definition", str(doc, "definition"));
                addField(fields, "Definition Source", str(doc, "definitionSource"));
                addField(fields, "Content Component Definition", str(doc, "contentComponentDefinition"));
                objects(doc, "supplementaryComponents").forEach(sc -> children.add(dtScSummary(sc)));
            }
            case "codeList" -> {
                ccType = CcType.CODE_LIST;
                name = str(doc, "name");
                addField(fields, "Name", str(doc, "name"));
                addField(fields, "List ID", str(doc, "listId"));
                addField(fields, "Agency ID", agencyIdValue(doc, "agencyId"));
                addField(fields, "Version", str(doc, "versionId"));
                addFlag(fields, "Extensible", bool(doc, "extensible"));
                addFlag(fields, "Deprecated", bool(doc, "deprecated"));
                addField(fields, "Based Code List", nestedStr(doc, "basedCodeList", "name"));
                addField(fields, "Namespace", nestedStr(doc, "namespace", "uri"));
                addField(fields, "Definition", str(doc, "definition"));
                addField(fields, "Definition Source", str(doc, "definitionSource"));
                addField(fields, "Remark", str(doc, "remark"));
                objects(doc, "values").forEach(v -> children.add(listValueSummary(v, "meaning")));
            }
            case "agencyIdList" -> {
                ccType = CcType.AGENCY_ID_LIST;
                name = str(doc, "name");
                addField(fields, "Name", str(doc, "name"));
                addField(fields, "List ID", str(doc, "listId"));
                addField(fields, "Agency ID List Value", agencyIdValue(doc, "agencyIdListValue"));
                addField(fields, "Version", str(doc, "versionId"));
                addFlag(fields, "Deprecated", bool(doc, "deprecated"));
                addField(fields, "Based Agency ID List", nestedStr(doc, "basedAgencyIdList", "name"));
                addField(fields, "Namespace", nestedStr(doc, "namespace", "uri"));
                addField(fields, "Definition", str(doc, "definition"));
                addField(fields, "Definition Source", str(doc, "definitionSource"));
                objects(doc, "values").forEach(v -> children.add(listValueSummary(v, "name")));
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported component type for a change summary: " + component);
        }
        return ComponentChangeSummary.newComponent(ccType, name, str(doc, "guid"), revisionNum, fields, children);
    }

    // ----- ACC -----

    private static ComponentChangeSummary acc(JsonObject before, JsonObject after,
                                              int revisionNum, int prevRevisionNum) {
        List<ComponentFieldChange> changes = new ArrayList<>();
        addChange(changes, "Object Class Term", str(before, "objectClassTerm"), str(after, "objectClassTerm"));
        addChange(changes, "Object Class Qualifier", str(before, "objectClassQualifier"), str(after, "objectClassQualifier"));
        addChange(changes, "Component Type",
                componentTypeName(str(before, "componentType")), componentTypeName(str(after, "componentType")));
        addFlagChange(changes, "Abstract", bool(before, "abstract"), bool(after, "abstract"));
        addFlagChange(changes, "Deprecated", bool(before, "deprecated"), bool(after, "deprecated"));
        addChange(changes, "Based ACC", nestedStr(before, "basedAcc", "den"), nestedStr(after, "basedAcc", "den"));
        addChange(changes, "Namespace", nestedStr(before, "namespace", "uri"), nestedStr(after, "namespace", "uri"));
        addChange(changes, "Definition", str(before, "definition"), str(after, "definition"));
        addChange(changes, "Definition Source", str(before, "definitionSource"), str(after, "definitionSource"));

        List<JsonObject> beforeAssociations = objects(before, "associations");
        List<JsonObject> afterAssociations = objects(after, "associations");
        GuidDiff<JsonObject> diff = diffByGuid(afterAssociations, beforeAssociations, a -> str(a, "guid"));

        List<ComponentChildSummary> added = diff.added().stream()
                .map(SnapshotChangeSummaryBuilder::associationSummary).collect(Collectors.toList());
        List<ComponentChildSummary> removed = diff.removed().stream()
                .map(a -> new ComponentChildSummary(associationKind(a), associationName(a), List.of()))
                .collect(Collectors.toList());

        List<ComponentChildChange> changed = new ArrayList<>();
        for (GuidPair<JsonObject> pair : diff.matched()) {
            List<ComponentFieldChange> childChanges = associationChanges(pair.current(), pair.prev());
            if (!childChanges.isEmpty()) {
                changed.add(new ComponentChildChange(
                        associationKind(pair.current()), associationName(pair.current()), childChanges));
            }
        }

        // The associations array order is the SEQ_KEY order; report a reorder of the surviving ones.
        List<String> afterOrder = diff.matched().stream()
                .map(p -> str(p.current(), "guid")).collect(Collectors.toList());
        List<String> beforeOrder = beforeAssociations.stream()
                .map(a -> str(a, "guid"))
                .filter(afterOrder::contains).collect(Collectors.toList());
        if (!afterOrder.equals(beforeOrder)) {
            changes.add(new ComponentFieldChange("Association Sequence",
                    associationNamesInOrder(beforeAssociations, beforeOrder),
                    associationNamesInOrder(afterAssociations, afterOrder)));
        }

        return ComponentChangeSummary.revised(CcType.ACC, accName(after), str(after, "guid"),
                revisionNum, prevRevisionNum, changes, added, removed, changed);
    }

    private static String accName(JsonObject snapshot) {
        String den = metadataDen(snapshot, "accManifest");
        return (den != null) ? den : str(snapshot, "objectClassTerm");
    }

    private static String associationKind(JsonObject association) {
        return "ascc".equals(str(association, "component")) ? "ASCC" : "BCC";
    }

    private static String associationName(JsonObject association) {
        JsonObject to = nested(association, "ascc".equals(str(association, "component")) ? "toAsccp" : "toBccp");
        if (to != null) {
            String den = str(to, "den");
            if (den != null) {
                return den;
            }
            return str(to, "propertyTerm");
        }
        return str(association, "guid");
    }

    private static ComponentChildSummary associationSummary(JsonObject association) {
        List<ComponentSummaryField> fields = new ArrayList<>();
        addField(fields, "Cardinality", cardinality(association));
        if ("bcc".equals(str(association, "component"))) {
            addField(fields, "Entity Type", str(association, "entityType"));
            addField(fields, "Value Constraint", valueConstraint(association));
            addFlag(fields, "Nillable", bool(association, "nillable"));
        }
        addFlag(fields, "Deprecated", bool(association, "deprecated"));
        addField(fields, "Definition", str(association, "definition"));
        addField(fields, "Definition Source", str(association, "definitionSource"));
        return new ComponentChildSummary(associationKind(association), associationName(association), fields);
    }

    private static List<ComponentFieldChange> associationChanges(JsonObject after, JsonObject before) {
        List<ComponentFieldChange> changes = new ArrayList<>();
        addChange(changes, "Cardinality", cardinality(before), cardinality(after));
        if ("bcc".equals(str(after, "component"))) {
            addChange(changes, "Entity Type", str(before, "entityType"), str(after, "entityType"));
            addChange(changes, "Value Constraint", valueConstraint(before), valueConstraint(after));
            addFlagChange(changes, "Nillable", bool(before, "nillable"), bool(after, "nillable"));
        }
        addFlagChange(changes, "Deprecated", bool(before, "deprecated"), bool(after, "deprecated"));
        addChange(changes, "Definition", str(before, "definition"), str(after, "definition"));
        addChange(changes, "Definition Source", str(before, "definitionSource"), str(after, "definitionSource"));
        return changes;
    }

    private static String associationNamesInOrder(List<JsonObject> associations, List<String> guidOrder) {
        return guidOrder.stream()
                .map(guid -> associations.stream()
                        .filter(a -> guid.equals(str(a, "guid"))).findFirst()
                        .map(SnapshotChangeSummaryBuilder::associationName).orElse(guid))
                .collect(Collectors.joining(" | "));
    }

    // ----- ASCCP -----

    private static ComponentChangeSummary asccp(JsonObject before, JsonObject after,
                                                int revisionNum, int prevRevisionNum) {
        List<ComponentFieldChange> changes = new ArrayList<>();
        addChange(changes, "Property Term", str(before, "propertyTerm"), str(after, "propertyTerm"));
        addChange(changes, "Role of ACC", nestedStr(before, "roleOfAcc", "den"), nestedStr(after, "roleOfAcc", "den"));
        addFlagChange(changes, "Reusable", bool(before, "reusable"), bool(after, "reusable"));
        addFlagChange(changes, "Nillable", bool(before, "nillable"), bool(after, "nillable"));
        addFlagChange(changes, "Deprecated", bool(before, "deprecated"), bool(after, "deprecated"));
        addChange(changes, "Namespace", nestedStr(before, "namespace", "uri"), nestedStr(after, "namespace", "uri"));
        addChange(changes, "Definition", str(before, "definition"), str(after, "definition"));
        addChange(changes, "Definition Source", str(before, "definitionSource"), str(after, "definitionSource"));

        String name = metadataDen(after, "asccpManifest");
        return ComponentChangeSummary.revised(CcType.ASCCP,
                (name != null) ? name : str(after, "propertyTerm"), str(after, "guid"),
                revisionNum, prevRevisionNum, changes, List.of(), List.of(), List.of());
    }

    // ----- BCCP -----

    private static ComponentChangeSummary bccp(JsonObject before, JsonObject after,
                                               int revisionNum, int prevRevisionNum) {
        List<ComponentFieldChange> changes = new ArrayList<>();
        addChange(changes, "Property Term", str(before, "propertyTerm"), str(after, "propertyTerm"));
        // The BCCP's representation term mirrors the assigned DT's, so a DT swap already implies it;
        // report it separately only when it moved on its own.
        boolean dtChanged = addChange(changes, "Data Type",
                nestedStr(before, "bdt", "den"), nestedStr(after, "bdt", "den"));
        if (!dtChanged) {
            addChange(changes, "Representation Term",
                    str(before, "representationTerm"), str(after, "representationTerm"));
        }
        addChange(changes, "Value Constraint", valueConstraint(before), valueConstraint(after));
        addFlagChange(changes, "Nillable", bool(before, "nillable"), bool(after, "nillable"));
        addFlagChange(changes, "Deprecated", bool(before, "deprecated"), bool(after, "deprecated"));
        addChange(changes, "Namespace", nestedStr(before, "namespace", "uri"), nestedStr(after, "namespace", "uri"));
        addChange(changes, "Definition", str(before, "definition"), str(after, "definition"));
        addChange(changes, "Definition Source", str(before, "definitionSource"), str(after, "definitionSource"));

        String name = metadataDen(after, "bccpManifest");
        return ComponentChangeSummary.revised(CcType.BCCP,
                (name != null) ? name : str(after, "propertyTerm"), str(after, "guid"),
                revisionNum, prevRevisionNum, changes, List.of(), List.of(), List.of());
    }

    // ----- DT -----

    private static ComponentChangeSummary dt(JsonObject before, JsonObject after,
                                             int revisionNum, int prevRevisionNum) {
        List<ComponentFieldChange> changes = new ArrayList<>();
        addChange(changes, "Data Type Term", str(before, "dataTypeTerm"), str(after, "dataTypeTerm"));
        addChange(changes, "Qualifier", str(before, "qualifier"), str(after, "qualifier"));
        addChange(changes, "Representation Term", str(before, "representationTerm"), str(after, "representationTerm"));
        addChange(changes, "Based Data Type", nestedStr(before, "basedDt", "den"), nestedStr(after, "basedDt", "den"));
        addChange(changes, "Six Hexadecimal Identifier", str(before, "sixDigitId"), str(after, "sixDigitId"));
        addFlagChange(changes, "Deprecated", bool(before, "deprecated"), bool(after, "deprecated"));
        addChange(changes, "Namespace", nestedStr(before, "namespace", "uri"), nestedStr(after, "namespace", "uri"));
        addChange(changes, "Definition", str(before, "definition"), str(after, "definition"));
        addChange(changes, "Definition Source", str(before, "definitionSource"), str(after, "definitionSource"));
        addChange(changes, "Content Component Definition",
                str(before, "contentComponentDefinition"), str(after, "contentComponentDefinition"));

        GuidDiff<JsonObject> diff = diffByGuid(
                objects(after, "supplementaryComponents"), objects(before, "supplementaryComponents"),
                sc -> str(sc, "guid"));

        List<ComponentChildSummary> added = diff.added().stream()
                .map(SnapshotChangeSummaryBuilder::dtScSummary).collect(Collectors.toList());
        List<ComponentChildSummary> removed = diff.removed().stream()
                .map(sc -> new ComponentChildSummary("Supplementary Component", dtScName(sc), List.of()))
                .collect(Collectors.toList());

        List<ComponentChildChange> changed = new ArrayList<>();
        for (GuidPair<JsonObject> pair : diff.matched()) {
            List<ComponentFieldChange> childChanges = new ArrayList<>();
            addChange(childChanges, "Property Term",
                    str(pair.prev(), "propertyTerm"), str(pair.current(), "propertyTerm"));
            addChange(childChanges, "Representation Term",
                    str(pair.prev(), "representationTerm"), str(pair.current(), "representationTerm"));
            addChange(childChanges, "Cardinality", cardinality(pair.prev()), cardinality(pair.current()));
            addChange(childChanges, "Value Constraint", valueConstraint(pair.prev()), valueConstraint(pair.current()));
            addChange(childChanges, "Definition", str(pair.prev(), "definition"), str(pair.current(), "definition"));
            addChange(childChanges, "Definition Source",
                    str(pair.prev(), "definitionSource"), str(pair.current(), "definitionSource"));
            if (!childChanges.isEmpty()) {
                changed.add(new ComponentChildChange("Supplementary Component", dtScName(pair.current()), childChanges));
            }
        }

        String name = metadataDen(after, "dtManifest");
        return ComponentChangeSummary.revised(CcType.DT,
                (name != null) ? name : str(after, "dataTypeTerm"), str(after, "guid"),
                revisionNum, prevRevisionNum, changes, added, removed, changed);
    }

    private static String dtScName(JsonObject sc) {
        String propertyTerm = str(sc, "propertyTerm");
        String representationTerm = str(sc, "representationTerm");
        if (propertyTerm != null && representationTerm != null) {
            return propertyTerm + ". " + representationTerm;
        }
        return (propertyTerm != null) ? propertyTerm : str(sc, "guid");
    }

    private static ComponentChildSummary dtScSummary(JsonObject sc) {
        List<ComponentSummaryField> fields = new ArrayList<>();
        addField(fields, "Cardinality", cardinality(sc));
        addField(fields, "Value Constraint", valueConstraint(sc));
        addField(fields, "Definition", str(sc, "definition"));
        addField(fields, "Definition Source", str(sc, "definitionSource"));
        return new ComponentChildSummary("Supplementary Component", dtScName(sc), fields);
    }

    // ----- Code list -----

    private static ComponentChangeSummary codeList(JsonObject before, JsonObject after,
                                                   int revisionNum, int prevRevisionNum) {
        List<ComponentFieldChange> changes = new ArrayList<>();
        addChange(changes, "Name", str(before, "name"), str(after, "name"));
        addChange(changes, "List ID", str(before, "listId"), str(after, "listId"));
        addChange(changes, "Agency ID", agencyIdValue(before, "agencyId"), agencyIdValue(after, "agencyId"));
        addChange(changes, "Version", str(before, "versionId"), str(after, "versionId"));
        addFlagChange(changes, "Extensible", bool(before, "extensible"), bool(after, "extensible"));
        addFlagChange(changes, "Deprecated", bool(before, "deprecated"), bool(after, "deprecated"));
        addChange(changes, "Based Code List",
                nestedStr(before, "basedCodeList", "name"), nestedStr(after, "basedCodeList", "name"));
        addChange(changes, "Namespace", nestedStr(before, "namespace", "uri"), nestedStr(after, "namespace", "uri"));
        addChange(changes, "Definition", str(before, "definition"), str(after, "definition"));
        addChange(changes, "Definition Source", str(before, "definitionSource"), str(after, "definitionSource"));
        addChange(changes, "Remark", str(before, "remark"), str(after, "remark"));

        GuidDiff<JsonObject> diff = diffByGuid(
                objects(after, "values"), objects(before, "values"), v -> str(v, "guid"));

        List<ComponentChildSummary> added = diff.added().stream()
                .map(v -> listValueSummary(v, "meaning")).collect(Collectors.toList());
        List<ComponentChildSummary> removed = diff.removed().stream()
                .map(v -> new ComponentChildSummary("Value", str(v, "value"), List.of()))
                .collect(Collectors.toList());

        List<ComponentChildChange> changed = new ArrayList<>();
        for (GuidPair<JsonObject> pair : diff.matched()) {
            List<ComponentFieldChange> childChanges = new ArrayList<>();
            addChange(childChanges, "Code", str(pair.prev(), "value"), str(pair.current(), "value"));
            addChange(childChanges, "Meaning", str(pair.prev(), "meaning"), str(pair.current(), "meaning"));
            addChange(childChanges, "Definition", str(pair.prev(), "definition"), str(pair.current(), "definition"));
            addChange(childChanges, "Definition Source",
                    str(pair.prev(), "definitionSource"), str(pair.current(), "definitionSource"));
            addFlagChange(childChanges, "Deprecated", bool(pair.prev(), "deprecated"), bool(pair.current(), "deprecated"));
            if (!childChanges.isEmpty()) {
                changed.add(new ComponentChildChange("Value", str(pair.current(), "value"), childChanges));
            }
        }

        return ComponentChangeSummary.revised(CcType.CODE_LIST, str(after, "name"), str(after, "guid"),
                revisionNum, prevRevisionNum, changes, added, removed, changed);
    }

    // ----- Agency ID list -----

    private static ComponentChangeSummary agencyIdList(JsonObject before, JsonObject after,
                                                       int revisionNum, int prevRevisionNum) {
        List<ComponentFieldChange> changes = new ArrayList<>();
        addChange(changes, "Name", str(before, "name"), str(after, "name"));
        addChange(changes, "List ID", str(before, "listId"), str(after, "listId"));
        addChange(changes, "Agency ID List Value",
                agencyIdValue(before, "agencyIdListValue"), agencyIdValue(after, "agencyIdListValue"));
        addChange(changes, "Version", str(before, "versionId"), str(after, "versionId"));
        addFlagChange(changes, "Deprecated", bool(before, "deprecated"), bool(after, "deprecated"));
        addChange(changes, "Based Agency ID List",
                nestedStr(before, "basedAgencyIdList", "name"), nestedStr(after, "basedAgencyIdList", "name"));
        addChange(changes, "Namespace", nestedStr(before, "namespace", "uri"), nestedStr(after, "namespace", "uri"));
        addChange(changes, "Definition", str(before, "definition"), str(after, "definition"));
        addChange(changes, "Definition Source", str(before, "definitionSource"), str(after, "definitionSource"));

        GuidDiff<JsonObject> diff = diffByGuid(
                objects(after, "values"), objects(before, "values"), v -> str(v, "guid"));

        List<ComponentChildSummary> added = diff.added().stream()
                .map(v -> listValueSummary(v, "name")).collect(Collectors.toList());
        List<ComponentChildSummary> removed = diff.removed().stream()
                .map(v -> new ComponentChildSummary("Value", str(v, "value"), List.of()))
                .collect(Collectors.toList());

        List<ComponentChildChange> changed = new ArrayList<>();
        for (GuidPair<JsonObject> pair : diff.matched()) {
            List<ComponentFieldChange> childChanges = new ArrayList<>();
            addChange(childChanges, "Value", str(pair.prev(), "value"), str(pair.current(), "value"));
            addChange(childChanges, "Meaning", str(pair.prev(), "name"), str(pair.current(), "name"));
            addChange(childChanges, "Definition", str(pair.prev(), "definition"), str(pair.current(), "definition"));
            addChange(childChanges, "Definition Source",
                    str(pair.prev(), "definitionSource"), str(pair.current(), "definitionSource"));
            addFlagChange(childChanges, "Deprecated", bool(pair.prev(), "deprecated"), bool(pair.current(), "deprecated"));
            if (!childChanges.isEmpty()) {
                changed.add(new ComponentChildChange("Value", str(pair.current(), "value"), childChanges));
            }
        }

        return ComponentChangeSummary.revised(CcType.AGENCY_ID_LIST, str(after, "name"), str(after, "guid"),
                revisionNum, prevRevisionNum, changes, added, removed, changed);
    }

    private static ComponentChildSummary listValueSummary(JsonObject value, String meaningKey) {
        List<ComponentSummaryField> fields = new ArrayList<>();
        addField(fields, "Meaning", str(value, meaningKey));
        addFlag(fields, "Deprecated", bool(value, "deprecated"));
        addField(fields, "Definition", str(value, "definition"));
        addField(fields, "Definition Source", str(value, "definitionSource"));
        return new ComponentChildSummary("Value", str(value, "value"), fields);
    }

    private static String agencyIdValue(JsonObject snapshot, String key) {
        JsonObject value = nested(snapshot, key);
        if (value == null) {
            return null;
        }
        String name = norm(str(value, "name"));
        String id = norm(str(value, "value"));
        if (id == null) {
            return name;
        }
        return (name == null) ? id : id + " (" + name + ")";
    }

    // ----- Snapshot JSON accessors -----
    //
    // The serializer (Gson) omits null values, so an absent key means "unset"; resolver-produced
    // references serialize as an EMPTY object when null, which the nested accessors treat as unset.

    private static String str(JsonObject obj, String key) {
        if (obj == null) {
            return null;
        }
        JsonElement element = obj.get(key);
        return (element == null || element.isJsonNull() || !element.isJsonPrimitive()) ? null : element.getAsString();
    }

    private static boolean bool(JsonObject obj, String key) {
        if (obj == null) {
            return false;
        }
        JsonElement element = obj.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()
                && element.getAsBoolean();
    }

    private static Integer intValue(JsonObject obj, String key) {
        JsonElement element = (obj == null) ? null : obj.get(key);
        return (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber())
                ? null : element.getAsInt();
    }

    private static JsonObject nested(JsonObject obj, String key) {
        JsonElement element = (obj == null) ? null : obj.get(key);
        return (element != null && element.isJsonObject()) ? element.getAsJsonObject() : null;
    }

    private static String nestedStr(JsonObject obj, String key, String childKey) {
        return str(nested(obj, key), childKey);
    }

    private static List<JsonObject> objects(JsonObject obj, String key) {
        JsonElement element = (obj == null) ? null : obj.get(key);
        if (element == null || !element.isJsonArray()) {
            return List.of();
        }
        JsonArray array = element.getAsJsonArray();
        List<JsonObject> result = new ArrayList<>(array.size());
        for (JsonElement entry : array) {
            if (entry.isJsonObject()) {
                result.add(entry.getAsJsonObject());
            }
        }
        return result;
    }

    private static String metadataDen(JsonObject snapshot, String manifestKey) {
        return nestedStr(nested(snapshot, "_metadata"), manifestKey, "den");
    }

    private static String cardinality(JsonObject obj) {
        Integer min = intValue(obj, "cardinalityMin");
        Integer max = intValue(obj, "cardinalityMax");
        if (min == null || max == null) {
            return null;
        }
        return min + ".." + (max == -1 ? "unbounded" : max);
    }

    private static String valueConstraint(JsonObject obj) {
        String fixedValue = norm(str(obj, "fixedValue"));
        if (fixedValue != null) {
            return "fixed \"" + fixedValue + "\"";
        }
        String defaultValue = norm(str(obj, "defaultValue"));
        return (defaultValue == null) ? null : "default \"" + defaultValue + "\"";
    }

    private static void addFlag(List<ComponentSummaryField> fields, String label, boolean value) {
        if (value) {
            fields.add(new ComponentSummaryField(label, "Yes"));
        }
    }

    private static void addFlagChange(List<ComponentFieldChange> changes, String label, boolean before, boolean after) {
        if (before != after) {
            changes.add(new ComponentFieldChange(label, before ? "Yes" : "No", after ? "Yes" : "No"));
        }
    }

    /** Mirrors {@link ComponentChangeSummaryBuilder#componentTypeName} for the snapshot's enum-name string. */
    private static String componentTypeName(String enumName) {
        if (enumName == null) {
            return null;
        }
        if ("Base".equals(enumName)) {
            return "Base (Abstract)";
        }
        return enumName.replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ");
    }
}
