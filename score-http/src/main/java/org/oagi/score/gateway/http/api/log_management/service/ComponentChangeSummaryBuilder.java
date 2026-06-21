package org.oagi.score.gateway.http.api.log_management.service;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListDetailsRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueDetailsRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.CcAssociation;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScDetailsRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListDetailsRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListValueDetailsRecord;
import org.oagi.score.gateway.http.api.log_management.model.ComponentChangeSummary;
import org.oagi.score.gateway.http.api.log_management.model.ComponentChildChange;
import org.oagi.score.gateway.http.api.log_management.model.ComponentChildSummary;
import org.oagi.score.gateway.http.api.log_management.model.ComponentFieldChange;
import org.oagi.score.gateway.http.api.log_management.model.ComponentSummaryField;
import org.oagi.score.gateway.http.api.log_management.model.LogSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Pure record-to-record diff/summary builders behind {@link ComponentChangeSummaryService}
 * (issue #1533, sub-task 3). Each per-type method takes the current details record and the prior
 * revision's details record ({@code null} for a new component) and produces a
 * {@link ComponentChangeSummary}.
 * <p>
 * Children (ACC associations, DT supplementary components, code/agency list values) are matched
 * across revisions by GUID — GUIDs survive {@code revise()} copies, so a same-GUID pair is the
 * same logical child. DT value-domain rows have no GUID and are matched by their logical identity
 * (the CDT primitive plus the referenced XBT / code list / agency ID list). Lifecycle fields
 * (state, owner, timestamps) are deliberately not compared: the summary covers content changes only.
 * <p>
 * {@code sameManifestPrev} marks an end-user in-place amendment: the prior revision shares the
 * current manifest, and the prev-details mappers resolve manifest-keyed references (ACC
 * associations and based ACC, ASCCP role-of ACC, BCCP data type, DT supplementary components)
 * from the <em>current</em> manifest row, so those always mirror the current state. Comparing them
 * would silently report "no change", so the builders skip them on that path instead.
 */
final class ComponentChangeSummaryBuilder {

    private ComponentChangeSummaryBuilder() {
    }

    // ----- ACC -----

    static ComponentChangeSummary acc(AccDetailsRecord current, AccDetailsRecord prev, boolean sameManifestPrev) {
        if (prev == null) {
            List<ComponentSummaryField> fields = new ArrayList<>();
            addField(fields, "Object Class Term", current.objectClassTerm());
            addField(fields, "Object Class Qualifier", current.objectClassQualifier());
            addField(fields, "Component Type", componentTypeName(current.componentType()));
            addFlag(fields, "Abstract", current.isAbstract());
            addFlag(fields, "Deprecated", current.deprecated());
            addField(fields, "Based ACC", den(current.based()));
            addField(fields, "Namespace", uri(current.namespace()));
            addField(fields, "Definition", content(current.definition()));
            addField(fields, "Definition Source", source(current.definition()));

            List<ComponentChildSummary> children = new ArrayList<>();
            for (CcAssociation association : nonNull(current.associations())) {
                children.add(associationSummary(association));
            }
            return ComponentChangeSummary.newComponent(CcType.ACC, current.den(), guid(current.guid()),
                    revisionNum(current.log()), fields, children);
        }

        List<ComponentFieldChange> changes = new ArrayList<>();
        addChange(changes, "Object Class Term", prev.objectClassTerm(), current.objectClassTerm());
        addChange(changes, "Object Class Qualifier", prev.objectClassQualifier(), current.objectClassQualifier());
        addChange(changes, "Component Type",
                componentTypeName(prev.componentType()), componentTypeName(current.componentType()));
        addFlagChange(changes, "Abstract", prev.isAbstract(), current.isAbstract());
        addFlagChange(changes, "Deprecated", prev.deprecated(), current.deprecated());
        addChange(changes, "Namespace", uri(prev.namespace()), uri(current.namespace()));
        addChange(changes, "Definition", content(prev.definition()), content(current.definition()));
        addChange(changes, "Definition Source", source(prev.definition()), source(current.definition()));

        List<ComponentChildSummary> added = List.of();
        List<ComponentChildSummary> removed = List.of();
        List<ComponentChildChange> changed = new ArrayList<>();

        if (!sameManifestPrev) {
            addChange(changes, "Based ACC", den(prev.based()), den(current.based()));

            GuidDiff<CcAssociation> diff = diffByGuid(
                    nonNull(current.associations()), nonNull(prev.associations()),
                    ComponentChangeSummaryBuilder::associationGuid);

            added = diff.added().stream()
                    .map(ComponentChangeSummaryBuilder::associationSummary).collect(Collectors.toList());
            removed = diff.removed().stream()
                    .map(a -> new ComponentChildSummary(associationKind(a), associationName(a), List.of()))
                    .collect(Collectors.toList());

            for (GuidPair<CcAssociation> pair : diff.matched()) {
                List<ComponentFieldChange> childChanges = associationChanges(pair.current(), pair.prev());
                if (!childChanges.isEmpty()) {
                    changed.add(new ComponentChildChange(
                            associationKind(pair.current()), associationName(pair.current()), childChanges));
                }
            }

            // Association order is semantic (SEQ_KEY); report a reorder of the surviving associations
            // even when none of them changed field-wise.
            List<String> currentOrder = diff.matched().stream()
                    .map(p -> associationGuid(p.current())).collect(Collectors.toList());
            List<String> prevOrder = nonNull(prev.associations()).stream()
                    .map(ComponentChangeSummaryBuilder::associationGuid)
                    .filter(currentOrder::contains).collect(Collectors.toList());
            if (!currentOrder.equals(prevOrder)) {
                changes.add(new ComponentFieldChange("Association Sequence",
                        associationNamesInOrder(prev.associations(), prevOrder),
                        associationNamesInOrder(current.associations(), currentOrder)));
            }
        }

        return ComponentChangeSummary.revised(CcType.ACC, current.den(), guid(current.guid()),
                revisionNum(current.log()), revisionNum(current.log()) - 1, changes, added, removed, changed);
    }

    private static String associationGuid(CcAssociation association) {
        if (association instanceof AsccSummaryRecord ascc) {
            return guid(ascc.guid());
        }
        if (association instanceof BccSummaryRecord bcc) {
            return guid(bcc.guid());
        }
        throw new IllegalArgumentException("Unsupported association type: " + association.getClass());
    }

    private static String associationKind(CcAssociation association) {
        return association.isAscc() ? "ASCC" : "BCC";
    }

    private static String associationName(CcAssociation association) {
        if (association instanceof AsccSummaryRecord ascc) {
            return ascc.den();
        }
        return ((BccSummaryRecord) association).den();
    }

    private static ComponentChildSummary associationSummary(CcAssociation association) {
        List<ComponentSummaryField> fields = new ArrayList<>();
        if (association instanceof AsccSummaryRecord ascc) {
            addField(fields, "Cardinality", cardinality(ascc.cardinality()));
            addFlag(fields, "Deprecated", ascc.deprecated());
            addField(fields, "Definition", content(ascc.definition()));
            addField(fields, "Definition Source", source(ascc.definition()));
        } else {
            BccSummaryRecord bcc = (BccSummaryRecord) association;
            addField(fields, "Cardinality", cardinality(bcc.cardinality()));
            addField(fields, "Entity Type", bcc.entityType() != null ? bcc.entityType().name() : null);
            addField(fields, "Value Constraint", valueConstraint(bcc.valueConstraint()));
            addFlag(fields, "Nillable", bcc.nillable());
            addFlag(fields, "Deprecated", bcc.deprecated());
            addField(fields, "Definition", content(bcc.definition()));
            addField(fields, "Definition Source", source(bcc.definition()));
        }
        return new ComponentChildSummary(associationKind(association), associationName(association), fields);
    }

    private static List<ComponentFieldChange> associationChanges(CcAssociation current, CcAssociation prev) {
        List<ComponentFieldChange> changes = new ArrayList<>();
        if (current instanceof AsccSummaryRecord ascc && prev instanceof AsccSummaryRecord prevAscc) {
            addChange(changes, "Cardinality", cardinality(prevAscc.cardinality()), cardinality(ascc.cardinality()));
            addFlagChange(changes, "Deprecated", prevAscc.deprecated(), ascc.deprecated());
            addChange(changes, "Definition", content(prevAscc.definition()), content(ascc.definition()));
            addChange(changes, "Definition Source", source(prevAscc.definition()), source(ascc.definition()));
        } else if (current instanceof BccSummaryRecord bcc && prev instanceof BccSummaryRecord prevBcc) {
            addChange(changes, "Cardinality", cardinality(prevBcc.cardinality()), cardinality(bcc.cardinality()));
            addChange(changes, "Entity Type",
                    prevBcc.entityType() != null ? prevBcc.entityType().name() : null,
                    bcc.entityType() != null ? bcc.entityType().name() : null);
            addChange(changes, "Value Constraint",
                    valueConstraint(prevBcc.valueConstraint()), valueConstraint(bcc.valueConstraint()));
            addFlagChange(changes, "Nillable", prevBcc.nillable(), bcc.nillable());
            addFlagChange(changes, "Deprecated", prevBcc.deprecated(), bcc.deprecated());
            addChange(changes, "Definition", content(prevBcc.definition()), content(bcc.definition()));
            addChange(changes, "Definition Source", source(prevBcc.definition()), source(bcc.definition()));
        }
        return changes;
    }

    private static String associationNamesInOrder(List<CcAssociation> associations, List<String> guidOrder) {
        Map<String, String> nameByGuid = nonNull(associations).stream().collect(Collectors.toMap(
                ComponentChangeSummaryBuilder::associationGuid,
                ComponentChangeSummaryBuilder::associationName,
                (a, b) -> a));
        return guidOrder.stream().map(nameByGuid::get).collect(Collectors.joining(" | "));
    }

    // ----- ASCCP -----

    static ComponentChangeSummary asccp(AsccpDetailsRecord current, AsccpDetailsRecord prev, boolean sameManifestPrev) {
        if (prev == null) {
            List<ComponentSummaryField> fields = new ArrayList<>();
            addField(fields, "Property Term", current.propertyTerm());
            addField(fields, "Role of ACC", den(current.roleOfAcc()));
            addFlag(fields, "Reusable", current.reusable());
            addFlag(fields, "Nillable", current.nillable());
            addFlag(fields, "Deprecated", current.deprecated());
            addField(fields, "Namespace", uri(current.namespace()));
            addField(fields, "Definition", content(current.definition()));
            addField(fields, "Definition Source", source(current.definition()));
            return ComponentChangeSummary.newComponent(CcType.ASCCP, current.den(), guid(current.guid()),
                    revisionNum(current.log()), fields, List.of());
        }

        List<ComponentFieldChange> changes = new ArrayList<>();
        addChange(changes, "Property Term", prev.propertyTerm(), current.propertyTerm());
        if (!sameManifestPrev) {
            addChange(changes, "Role of ACC", den(prev.roleOfAcc()), den(current.roleOfAcc()));
        }
        addFlagChange(changes, "Reusable", prev.reusable(), current.reusable());
        addFlagChange(changes, "Nillable", prev.nillable(), current.nillable());
        addFlagChange(changes, "Deprecated", prev.deprecated(), current.deprecated());
        addChange(changes, "Namespace", uri(prev.namespace()), uri(current.namespace()));
        addChange(changes, "Definition", content(prev.definition()), content(current.definition()));
        addChange(changes, "Definition Source", source(prev.definition()), source(current.definition()));
        return ComponentChangeSummary.revised(CcType.ASCCP, current.den(), guid(current.guid()),
                revisionNum(current.log()), revisionNum(current.log()) - 1, changes, List.of(), List.of(), List.of());
    }

    // ----- BCCP -----

    static ComponentChangeSummary bccp(BccpDetailsRecord current, BccpDetailsRecord prev, boolean sameManifestPrev) {
        if (prev == null) {
            List<ComponentSummaryField> fields = new ArrayList<>();
            addField(fields, "Property Term", current.propertyTerm());
            addField(fields, "Data Type", den(current.dt()));
            addField(fields, "Representation Term", current.representationTerm());
            addField(fields, "Value Constraint", valueConstraint(current.valueConstraint()));
            addFlag(fields, "Nillable", current.nillable());
            addFlag(fields, "Deprecated", current.deprecated());
            addField(fields, "Namespace", uri(current.namespace()));
            addField(fields, "Definition", content(current.definition()));
            addField(fields, "Definition Source", source(current.definition()));
            return ComponentChangeSummary.newComponent(CcType.BCCP, current.den(), guid(current.guid()),
                    revisionNum(current.log()), fields, List.of());
        }

        List<ComponentFieldChange> changes = new ArrayList<>();
        addChange(changes, "Property Term", prev.propertyTerm(), current.propertyTerm());
        // The BCCP's representation term mirrors the assigned DT's, so a DT swap already implies it;
        // report it separately only when it moved on its own. On the same-manifest path the prev DT
        // is unresolvable (mirrors the current manifest), so only the frozen representation term —
        // which then implies the DT swap — can be reported.
        boolean dtChanged = !sameManifestPrev && addChange(changes, "Data Type", den(prev.dt()), den(current.dt()));
        if (!dtChanged) {
            addChange(changes, "Representation Term", prev.representationTerm(), current.representationTerm());
        }
        addChange(changes, "Value Constraint",
                valueConstraint(prev.valueConstraint()), valueConstraint(current.valueConstraint()));
        addFlagChange(changes, "Nillable", prev.nillable(), current.nillable());
        addFlagChange(changes, "Deprecated", prev.deprecated(), current.deprecated());
        addChange(changes, "Namespace", uri(prev.namespace()), uri(current.namespace()));
        addChange(changes, "Definition", content(prev.definition()), content(current.definition()));
        addChange(changes, "Definition Source", source(prev.definition()), source(current.definition()));
        return ComponentChangeSummary.revised(CcType.BCCP, current.den(), guid(current.guid()),
                revisionNum(current.log()), revisionNum(current.log()) - 1, changes, List.of(), List.of(), List.of());
    }

    // ----- DT -----

    /**
     * Builds the DT summary. {@code prevScList} is the prior revision's supplementary components,
     * or {@code null} when they are not resolvable — the end-user in-place amendment keeps the same
     * manifest, so the prior SC rows cannot be fetched by manifest id; in that case SC changes are
     * limited to cardinality (carried on the record as {@code prevCardinality}) and SC adds/removes
     * are not reported.
     */
    static ComponentChangeSummary dt(DtDetailsRecord current, List<DtScDetailsRecord> currentScList,
                                     DtDetailsRecord prev, List<DtScDetailsRecord> prevScList) {
        if (prev == null) {
            List<ComponentSummaryField> fields = new ArrayList<>();
            addField(fields, "Data Type Term", current.dataTypeTerm());
            addField(fields, "Qualifier", current.qualifier());
            addField(fields, "Representation Term", current.representationTerm());
            addField(fields, "Based Data Type", den(current.based()));
            addField(fields, "Six Hexadecimal Identifier", current.sixDigitId());
            addFlag(fields, "Deprecated", current.deprecated());
            addField(fields, "Namespace", uri(current.namespace()));
            addField(fields, "Definition", content(current.definition()));
            addField(fields, "Definition Source", source(current.definition()));
            addField(fields, "Content Component Definition", current.contentComponentDefinition());

            List<ComponentChildSummary> children = new ArrayList<>();
            for (ValueDomain domain : dtValueDomains(current.dtAwdPriList())) {
                children.add(new ComponentChildSummary("Value Domain", domain.name(),
                        domain.isDefault() ? List.of(new ComponentSummaryField("Default", "Yes")) : List.of()));
            }
            for (DtScDetailsRecord sc : nonNull(currentScList)) {
                children.add(dtScSummary(sc));
            }
            return ComponentChangeSummary.newComponent(CcType.DT, current.den(), guid(current.guid()),
                    revisionNum(current.log()), fields, children);
        }

        List<ComponentFieldChange> changes = new ArrayList<>();
        addChange(changes, "Data Type Term", prev.dataTypeTerm(), current.dataTypeTerm());
        addChange(changes, "Qualifier", prev.qualifier(), current.qualifier());
        addChange(changes, "Representation Term", prev.representationTerm(), current.representationTerm());
        addChange(changes, "Based Data Type", den(prev.based()), den(current.based()));
        addChange(changes, "Six Hexadecimal Identifier", prev.sixDigitId(), current.sixDigitId());
        addFlagChange(changes, "Deprecated", prev.deprecated(), current.deprecated());
        addChange(changes, "Namespace", uri(prev.namespace()), uri(current.namespace()));
        addChange(changes, "Definition", content(prev.definition()), content(current.definition()));
        addChange(changes, "Definition Source", source(prev.definition()), source(current.definition()));
        addChange(changes, "Content Component Definition",
                prev.contentComponentDefinition(), current.contentComponentDefinition());

        List<ComponentChildSummary> added = new ArrayList<>();
        List<ComponentChildSummary> removed = new ArrayList<>();
        List<ComponentChildChange> changed = new ArrayList<>();

        diffValueDomains(changes, added, removed,
                dtValueDomains(current.dtAwdPriList()), dtValueDomains(prev.dtAwdPriList()));

        if (prevScList != null) {
            GuidDiff<DtScDetailsRecord> diff = diffByGuid(nonNull(currentScList), prevScList,
                    sc -> guid(sc.guid()));
            for (DtScDetailsRecord sc : diff.added()) {
                added.add(dtScSummary(sc));
            }
            for (DtScDetailsRecord sc : diff.removed()) {
                removed.add(new ComponentChildSummary("Supplementary Component", sc.den(), List.of()));
            }
            for (GuidPair<DtScDetailsRecord> pair : diff.matched()) {
                List<ComponentFieldChange> childChanges = dtScChanges(pair.current(), pair.prev());
                if (!childChanges.isEmpty()) {
                    changed.add(new ComponentChildChange("Supplementary Component",
                            pair.current().den(), childChanges));
                }
            }
        } else {
            // Same-manifest (end-user) amendment: the only prior SC data on the record is the
            // previous cardinality.
            for (DtScDetailsRecord sc : nonNull(currentScList)) {
                if (sc.prevCardinality() != null
                        && !Objects.equals(cardinality(sc.prevCardinality()), cardinality(sc.cardinality()))) {
                    changed.add(new ComponentChildChange("Supplementary Component", sc.den(),
                            List.of(new ComponentFieldChange("Cardinality",
                                    cardinality(sc.prevCardinality()), cardinality(sc.cardinality())))));
                }
            }
        }

        return ComponentChangeSummary.revised(CcType.DT, current.den(), guid(current.guid()),
                revisionNum(current.log()), revisionNum(current.log()) - 1, changes, added, removed, changed);
    }

    private static ComponentChildSummary dtScSummary(DtScDetailsRecord sc) {
        List<ComponentSummaryField> fields = new ArrayList<>();
        addField(fields, "Cardinality", cardinality(sc.cardinality()));
        addField(fields, "Value Constraint", valueConstraint(sc.valueConstraint()));
        addFlag(fields, "Deprecated", sc.deprecated());
        addField(fields, "Definition", content(sc.definition()));
        addField(fields, "Definition Source", source(sc.definition()));
        return new ComponentChildSummary("Supplementary Component", sc.den(), fields);
    }

    private static List<ComponentFieldChange> dtScChanges(DtScDetailsRecord current, DtScDetailsRecord prev) {
        List<ComponentFieldChange> changes = new ArrayList<>();
        addChange(changes, "Property Term", prev.propertyTerm(), current.propertyTerm());
        addChange(changes, "Representation Term", prev.representationTerm(), current.representationTerm());
        addChange(changes, "Cardinality", cardinality(prev.cardinality()), cardinality(current.cardinality()));
        addChange(changes, "Value Constraint",
                valueConstraint(prev.valueConstraint()), valueConstraint(current.valueConstraint()));
        addFlagChange(changes, "Deprecated", prev.deprecated(), current.deprecated());
        addChange(changes, "Definition", content(prev.definition()), content(current.definition()));
        addChange(changes, "Definition Source", source(prev.definition()), source(current.definition()));
        List<ValueDomain> prevDomains = dtScValueDomains(prev.dtScAwdPriList());
        List<ValueDomain> currentDomains = dtScValueDomains(current.dtScAwdPriList());
        addChange(changes, "Value Domains", domainNames(prevDomains), domainNames(currentDomains));
        addChange(changes, "Default Value Domain", defaultDomainName(prevDomains), defaultDomainName(currentDomains));
        return changes;
    }

    // ----- DT value domains (no GUID; matched by logical identity) -----

    private record ValueDomain(String name, boolean isDefault) {
    }

    private static List<ValueDomain> dtValueDomains(List<DtAwdPriDetailsRecord> list) {
        return nonNull(list).stream()
                .map(row -> new ValueDomain(
                        valueDomainName(row.cdtPriName(), row.xbt(), row.codeList(), row.agencyIdList()),
                        row.isDefault()))
                .collect(Collectors.toList());
    }

    private static List<ValueDomain> dtScValueDomains(List<DtScAwdPriDetailsRecord> list) {
        return nonNull(list).stream()
                .map(row -> new ValueDomain(
                        valueDomainName(row.cdtPriName(), row.xbt(), row.codeList(), row.agencyIdList()),
                        row.isDefault()))
                .collect(Collectors.toList());
    }

    private static String valueDomainName(String cdtPriName, XbtSummaryRecord xbt,
                                          CodeListSummaryRecord codeList, AgencyIdListSummaryRecord agencyIdList) {
        if (codeList != null) {
            return "Code List \"" + codeList.name() + "\"";
        }
        if (agencyIdList != null) {
            return "Agency ID List \"" + agencyIdList.name() + "\"";
        }
        // The CDT primitive qualifies the name: the same XBT can legitimately back two rows under
        // different primitives, and the rendered name is also the diff identity.
        String name = (xbt != null && norm(xbt.name()) != null) ? xbt.name() : cdtPriName;
        return (norm(cdtPriName) == null || Objects.equals(cdtPriName, name))
                ? "Primitive \"" + name + "\""
                : "Primitive \"" + name + "\" (" + cdtPriName + ")";
    }

    private static void diffValueDomains(List<ComponentFieldChange> changes,
                                         List<ComponentChildSummary> added, List<ComponentChildSummary> removed,
                                         List<ValueDomain> current, List<ValueDomain> prev) {
        Map<String, ValueDomain> prevByName = prev.stream()
                .collect(Collectors.toMap(ValueDomain::name, Function.identity(), (a, b) -> a, LinkedHashMap::new));
        Map<String, ValueDomain> currentByName = current.stream()
                .collect(Collectors.toMap(ValueDomain::name, Function.identity(), (a, b) -> a, LinkedHashMap::new));

        for (ValueDomain domain : current) {
            if (!prevByName.containsKey(domain.name())) {
                added.add(new ComponentChildSummary("Value Domain", domain.name(),
                        domain.isDefault() ? List.of(new ComponentSummaryField("Default", "Yes")) : List.of()));
            }
        }
        for (ValueDomain domain : prev) {
            if (!currentByName.containsKey(domain.name())) {
                removed.add(new ComponentChildSummary("Value Domain", domain.name(), List.of()));
            }
        }
        addChange(changes, "Default Value Domain", defaultDomainName(prev), defaultDomainName(current));
    }

    private static String defaultDomainName(List<ValueDomain> domains) {
        return domains.stream().filter(ValueDomain::isDefault).map(ValueDomain::name).findFirst().orElse(null);
    }

    private static String domainNames(List<ValueDomain> domains) {
        return domains.isEmpty() ? null
                : domains.stream().map(ValueDomain::name).sorted().collect(Collectors.joining(", "));
    }

    // ----- Code list -----

    static ComponentChangeSummary codeList(CodeListDetailsRecord current, CodeListDetailsRecord prev) {
        if (prev == null) {
            List<ComponentSummaryField> fields = new ArrayList<>();
            addField(fields, "Name", current.name());
            addField(fields, "List ID", current.listId());
            addField(fields, "Agency ID", agencyIdValue(current.agencyIdListValue()));
            addField(fields, "Version", current.versionId());
            addFlag(fields, "Extensible", current.extensible());
            addFlag(fields, "Deprecated", current.deprecated());
            addField(fields, "Based Code List", current.based() != null ? current.based().name() : null);
            addField(fields, "Namespace", uri(current.namespace()));
            addField(fields, "Definition", content(current.definition()));
            addField(fields, "Definition Source", source(current.definition()));
            addField(fields, "Remark", current.remark());

            List<ComponentChildSummary> children = new ArrayList<>();
            for (CodeListValueDetailsRecord value : nonNull(current.valueList())) {
                children.add(codeListValueSummary(value));
            }
            return ComponentChangeSummary.newComponent(CcType.CODE_LIST, current.name(), guid(current.guid()),
                    revisionNum(current.log()), fields, children);
        }

        List<ComponentFieldChange> changes = new ArrayList<>();
        addChange(changes, "Name", prev.name(), current.name());
        addChange(changes, "List ID", prev.listId(), current.listId());
        addChange(changes, "Agency ID", agencyIdValue(prev.agencyIdListValue()), agencyIdValue(current.agencyIdListValue()));
        addChange(changes, "Version", prev.versionId(), current.versionId());
        addFlagChange(changes, "Extensible", prev.extensible(), current.extensible());
        addFlagChange(changes, "Deprecated", prev.deprecated(), current.deprecated());
        addChange(changes, "Based Code List",
                prev.based() != null ? prev.based().name() : null,
                current.based() != null ? current.based().name() : null);
        addChange(changes, "Namespace", uri(prev.namespace()), uri(current.namespace()));
        addChange(changes, "Definition", content(prev.definition()), content(current.definition()));
        addChange(changes, "Definition Source", source(prev.definition()), source(current.definition()));
        addChange(changes, "Remark", prev.remark(), current.remark());

        GuidDiff<CodeListValueDetailsRecord> diff = diffByGuid(
                nonNull(current.valueList()), nonNull(prev.valueList()), v -> guid(v.guid()));

        List<ComponentChildSummary> added = diff.added().stream()
                .map(ComponentChangeSummaryBuilder::codeListValueSummary).collect(Collectors.toList());
        List<ComponentChildSummary> removed = diff.removed().stream()
                .map(v -> new ComponentChildSummary("Value", v.value(), List.of()))
                .collect(Collectors.toList());
        List<ComponentChildChange> changed = new ArrayList<>();
        for (GuidPair<CodeListValueDetailsRecord> pair : diff.matched()) {
            List<ComponentFieldChange> childChanges = new ArrayList<>();
            addChange(childChanges, "Code", pair.prev().value(), pair.current().value());
            addChange(childChanges, "Meaning", pair.prev().meaning(), pair.current().meaning());
            addChange(childChanges, "Definition", content(pair.prev().definition()), content(pair.current().definition()));
            addChange(childChanges, "Definition Source", source(pair.prev().definition()), source(pair.current().definition()));
            addFlagChange(childChanges, "Deprecated", pair.prev().deprecated(), pair.current().deprecated());
            if (!childChanges.isEmpty()) {
                changed.add(new ComponentChildChange("Value", pair.current().value(), childChanges));
            }
        }

        return ComponentChangeSummary.revised(CcType.CODE_LIST, current.name(), guid(current.guid()),
                revisionNum(current.log()), revisionNum(current.log()) - 1, changes, added, removed, changed);
    }

    private static ComponentChildSummary codeListValueSummary(CodeListValueDetailsRecord value) {
        List<ComponentSummaryField> fields = new ArrayList<>();
        addField(fields, "Meaning", value.meaning());
        addFlag(fields, "Deprecated", value.deprecated());
        addField(fields, "Definition", content(value.definition()));
        addField(fields, "Definition Source", source(value.definition()));
        return new ComponentChildSummary("Value", value.value(), fields);
    }

    // ----- Agency ID list -----

    static ComponentChangeSummary agencyIdList(AgencyIdListDetailsRecord current, AgencyIdListDetailsRecord prev) {
        if (prev == null) {
            List<ComponentSummaryField> fields = new ArrayList<>();
            addField(fields, "Name", current.name());
            addField(fields, "List ID", current.listId());
            addField(fields, "Agency ID List Value", agencyIdValue(current.agencyIdListValue()));
            addField(fields, "Version", current.versionId());
            addFlag(fields, "Deprecated", current.deprecated());
            addField(fields, "Based Agency ID List", current.based() != null ? current.based().name() : null);
            addField(fields, "Namespace", uri(current.namespace()));
            addField(fields, "Definition", content(current.definition()));
            addField(fields, "Definition Source", source(current.definition()));
            addField(fields, "Remark", current.remark());

            List<ComponentChildSummary> children = new ArrayList<>();
            for (AgencyIdListValueDetailsRecord value : nonNull(current.valueList())) {
                children.add(agencyIdListValueSummary(value));
            }
            return ComponentChangeSummary.newComponent(CcType.AGENCY_ID_LIST, current.name(), guid(current.guid()),
                    revisionNum(current.log()), fields, children);
        }

        List<ComponentFieldChange> changes = new ArrayList<>();
        addChange(changes, "Name", prev.name(), current.name());
        addChange(changes, "List ID", prev.listId(), current.listId());
        addChange(changes, "Agency ID List Value",
                agencyIdValue(prev.agencyIdListValue()), agencyIdValue(current.agencyIdListValue()));
        addChange(changes, "Version", prev.versionId(), current.versionId());
        addFlagChange(changes, "Deprecated", prev.deprecated(), current.deprecated());
        addChange(changes, "Based Agency ID List",
                prev.based() != null ? prev.based().name() : null,
                current.based() != null ? current.based().name() : null);
        addChange(changes, "Namespace", uri(prev.namespace()), uri(current.namespace()));
        addChange(changes, "Definition", content(prev.definition()), content(current.definition()));
        addChange(changes, "Definition Source", source(prev.definition()), source(current.definition()));
        addChange(changes, "Remark", prev.remark(), current.remark());

        GuidDiff<AgencyIdListValueDetailsRecord> diff = diffByGuid(
                nonNull(current.valueList()), nonNull(prev.valueList()), v -> guid(v.guid()));

        List<ComponentChildSummary> added = diff.added().stream()
                .map(ComponentChangeSummaryBuilder::agencyIdListValueSummary).collect(Collectors.toList());
        List<ComponentChildSummary> removed = diff.removed().stream()
                .map(v -> new ComponentChildSummary("Value", v.value(), List.of()))
                .collect(Collectors.toList());
        List<ComponentChildChange> changed = new ArrayList<>();
        for (GuidPair<AgencyIdListValueDetailsRecord> pair : diff.matched()) {
            List<ComponentFieldChange> childChanges = new ArrayList<>();
            addChange(childChanges, "Value", pair.prev().value(), pair.current().value());
            addChange(childChanges, "Meaning", pair.prev().name(), pair.current().name());
            addChange(childChanges, "Definition", content(pair.prev().definition()), content(pair.current().definition()));
            addChange(childChanges, "Definition Source", source(pair.prev().definition()), source(pair.current().definition()));
            addFlagChange(childChanges, "Deprecated", pair.prev().deprecated(), pair.current().deprecated());
            addFlagChange(childChanges, "Developer Default", pair.prev().isDeveloperDefault(), pair.current().isDeveloperDefault());
            addFlagChange(childChanges, "User Default", pair.prev().isUserDefault(), pair.current().isUserDefault());
            if (!childChanges.isEmpty()) {
                changed.add(new ComponentChildChange("Value", pair.current().value(), childChanges));
            }
        }

        return ComponentChangeSummary.revised(CcType.AGENCY_ID_LIST, current.name(), guid(current.guid()),
                revisionNum(current.log()), revisionNum(current.log()) - 1, changes, added, removed, changed);
    }

    private static ComponentChildSummary agencyIdListValueSummary(AgencyIdListValueDetailsRecord value) {
        List<ComponentSummaryField> fields = new ArrayList<>();
        addField(fields, "Meaning", value.name());
        addFlag(fields, "Deprecated", value.deprecated());
        addFlag(fields, "Developer Default", value.isDeveloperDefault());
        addFlag(fields, "User Default", value.isUserDefault());
        addField(fields, "Definition", content(value.definition()));
        addField(fields, "Definition Source", source(value.definition()));
        return new ComponentChildSummary("Value", value.value(), fields);
    }

    private static String agencyIdValue(AgencyIdListValueSummaryRecord value) {
        if (value == null) {
            return null;
        }
        String name = norm(value.name());
        return (name == null) ? value.value() : value.value() + " (" + name + ")";
    }

    // ----- GUID-keyed child matching -----

    record GuidPair<T>(T current, T prev) {
    }

    record GuidDiff<T>(List<T> added, List<T> removed, List<GuidPair<T>> matched) {
    }

    /**
     * Splits two child lists into added (current only), removed (prev only) and matched pairs,
     * keyed by GUID; {@code matched} preserves the current list's order.
     */
    static <T> GuidDiff<T> diffByGuid(List<T> current, List<T> prev, Function<T, String> key) {
        Map<String, T> prevByKey = prev.stream()
                .collect(Collectors.toMap(key, Function.identity(), (a, b) -> a, LinkedHashMap::new));
        List<T> added = new ArrayList<>();
        List<GuidPair<T>> matched = new ArrayList<>();
        for (T item : current) {
            T prevItem = prevByKey.remove(key.apply(item));
            if (prevItem == null) {
                added.add(item);
            } else {
                matched.add(new GuidPair<>(item, prevItem));
            }
        }
        return new GuidDiff<>(added, new ArrayList<>(prevByKey.values()), matched);
    }

    // ----- Field/value normalization and rendering -----

    static int revisionNum(LogSummaryRecord log) {
        return (log == null) ? 1 : log.revisionNum();
    }

    private static <T> List<T> nonNull(List<T> list) {
        return (list == null) ? List.of() : list;
    }

    /** Trims to {@code null}: absent, null and blank all mean "unset". */
    static String norm(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    static void addField(List<ComponentSummaryField> fields, String label, String value) {
        String normalized = norm(value);
        if (normalized != null) {
            fields.add(new ComponentSummaryField(label, normalized));
        }
    }

    private static void addFlag(List<ComponentSummaryField> fields, String label, boolean value) {
        if (value) {
            fields.add(new ComponentSummaryField(label, "Yes"));
        }
    }

    /** Records a change when the normalized values differ; returns whether a change was recorded. */
    static boolean addChange(List<ComponentFieldChange> changes, String label, String before, String after) {
        String normBefore = norm(before);
        String normAfter = norm(after);
        if (Objects.equals(normBefore, normAfter)) {
            return false;
        }
        changes.add(new ComponentFieldChange(label, normBefore, normAfter));
        return true;
    }

    private static void addFlagChange(List<ComponentFieldChange> changes, String label, boolean before, boolean after) {
        if (before != after) {
            changes.add(new ComponentFieldChange(label, yesNo(before), yesNo(after)));
        }
    }

    private static String yesNo(boolean value) {
        return value ? "Yes" : "No";
    }

    static String cardinality(Cardinality cardinality) {
        if (cardinality == null || cardinality.min() == null || cardinality.max() == null) {
            return null;
        }
        return cardinality.min() + ".." + (cardinality.max() == -1 ? "unbounded" : cardinality.max());
    }

    static String valueConstraint(ValueConstraint constraint) {
        if (constraint == null) {
            return null;
        }
        if (constraint.hasFixedValue()) {
            return "fixed \"" + constraint.fixedValue() + "\"";
        }
        if (constraint.hasDefaultValue()) {
            return "default \"" + constraint.defaultValue() + "\"";
        }
        return null;
    }

    private static String content(Definition definition) {
        return (definition == null) ? null : definition.content();
    }

    private static String source(Definition definition) {
        return (definition == null) ? null : definition.source();
    }

    private static String uri(NamespaceSummaryRecord namespace) {
        return (namespace == null) ? null : namespace.uri();
    }

    private static String den(AccSummaryRecord acc) {
        return (acc == null) ? null : acc.den();
    }

    private static String den(DtSummaryRecord dt) {
        return (dt == null) ? null : dt.den();
    }

    private static String guid(Guid guid) {
        return (guid == null) ? null : guid.value();
    }

    /** The editor UI's display name for an OAGIS component type, e.g. {@code UserExtensionGroup} → "User Extension Group". */
    static String componentTypeName(OagisComponentType componentType) {
        if (componentType == null) {
            return null;
        }
        if (componentType == OagisComponentType.Base) {
            return "Base (Abstract)";
        }
        return componentType.name().replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ");
    }
}
