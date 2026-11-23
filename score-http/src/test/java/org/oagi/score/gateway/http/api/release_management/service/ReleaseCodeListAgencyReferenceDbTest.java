package org.oagi.score.gateway.http.api.release_management.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * What a Code List and an Agency ID List refer to: their base, and the Agency ID List their agency is a value of. For every
 * combination of the state of the dependent and of what it refers to, and whether each is assigned to the release, the
 * validation says what the rules say, and the release that is copied refers to the manifests of the new release as the
 * rules say: the one that follows what 'Working' refers to when the dependent is assigned, and the one that follows what the
 * previous release referred to when it is not.
 */
@EnabledIfSystemProperty(named = "score.dtAwdPri", matches = "true")
class ReleaseCodeListAgencyReferenceDbTest extends ReleaseScenarioFixture {

    private enum Ent {
        CODE_LIST("code_list"), AGENCY_ID_LIST("agency_id_list");

        final String table;

        Ent(String table) {
            this.table = table;
        }

        String manifest() {
            return table + "_manifest";
        }

        String id() {
            return table + "_manifest_id";
        }

        String record() {
            return table + "_id";
        }
    }

    /** {@code viaValue}: the reference is to a value of the target Agency ID List, not to the target itself. */
    private record Edge(String name, Ent dependent, Ent target, boolean viaValue, String column, String recordColumn) {
    }

    private static final Edge CODE_LIST_BASE = new Edge("Code List -> base Code List", Ent.CODE_LIST, Ent.CODE_LIST, false,
            "based_code_list_manifest_id", "based_code_list_id");
    private static final Edge CODE_LIST_AGENCY = new Edge("Code List -> Agency ID List (agency)", Ent.CODE_LIST,
            Ent.AGENCY_ID_LIST, true, "agency_id_list_value_manifest_id", null);
    private static final Edge AGENCY_BASE = new Edge("Agency ID List -> base Agency ID List", Ent.AGENCY_ID_LIST,
            Ent.AGENCY_ID_LIST, false, "based_agency_id_list_manifest_id", "based_agency_id_list_id");
    private static final Edge AGENCY_AGENCY = new Edge("Agency ID List -> Agency ID List (agency)", Ent.AGENCY_ID_LIST,
            Ent.AGENCY_ID_LIST, true, "agency_id_list_value_manifest_id", "agency_id_list_value_id");

    private static final List<Cls> RELEASED = classes().stream().filter(c -> c.variant().released).toList();
    private static final List<Cls> REVISED = classes().stream().filter(c -> c.variant().revised()).toList();
    private static final List<Cls> NEW = classes().stream().filter(c -> c.variant().isNew()).toList();

    private record Node(Ent ent, long id, Cls cls) {
    }

    private record Pair(String how, Node dependent, Node target) {
    }

    /** The value manifest of 'Working' that stands for each Agency ID List the test made. */
    private final Map<Long, Long> valueOf = new HashMap<>();

    // ---- the components ----

    private long owner() {
        return one("select min(app_user_id) from app_user");
    }

    /** A value of the Agency ID List, in 'Working', and in the previous release when the list has a manifest there. */
    private void valueFor(long ail) {
        long record = recordOf(Ent.AGENCY_ID_LIST, ail);
        dsl.execute("insert into agency_id_list_value (guid, value, name, owner_list_id, is_deprecated, created_by, "
                + "owner_user_id, last_updated_by) values (replace(uuid(), '-', ''), 'V', 'V', ?, 0, ?, ?, ?)",
                record, owner(), owner(), owner());
        long value = one("select last_insert_id()");
        Long previous = dsl.fetchOne("select prev_agency_id_list_manifest_id from agency_id_list_manifest "
                + "where agency_id_list_manifest_id = ?", ail).get(0, Long.class);
        Long previousValue = null;
        if (previous != null) {
            dsl.execute("insert into agency_id_list_value_manifest (release_id, agency_id_list_value_id, "
                    + "agency_id_list_manifest_id) values (?, ?, ?)", published, value, previous);
            previousValue = one("select last_insert_id()");
        }
        dsl.execute("insert into agency_id_list_value_manifest (release_id, agency_id_list_value_id, "
                + "agency_id_list_manifest_id, prev_agency_id_list_value_manifest_id) values (?, ?, ?, ?)",
                working, value, ail, previousValue);
        long current = one("select last_insert_id()");
        if (previousValue != null) {
            dsl.execute("update agency_id_list_value_manifest set next_agency_id_list_value_manifest_id = ? "
                    + "where agency_id_list_value_manifest_id = ?", current, previousValue);
        }
        valueOf.put(ail, current);
    }

    private long recordOf(Ent e, long id) {
        return one("select `" + e.record() + "` from `" + e.manifest() + "` where `" + e.id() + "` = ?", id);
    }

    private long previous(Ent e, long id) {
        return one("select `prev_" + e.id() + "` from `" + e.manifest() + "` where `" + e.id() + "` = ?", id);
    }

    private long madeUpAgencyIdList() {
        dsl.execute("insert into agency_id_list (guid, enum_type_guid, name, list_id, version_id, definition, "
                + "definition_source, remark, namespace_id, created_by, last_updated_by, creation_timestamp, "
                + "last_update_timestamp, state, is_deprecated, owner_user_id) "
                + "select replace(uuid(), '-', ''), enum_type_guid, name, list_id, version_id, definition, definition_source, "
                + "remark, ?, created_by, last_updated_by, creation_timestamp, last_update_timestamp, 'Published', 0, "
                + "owner_user_id from agency_id_list limit 1", namespace);
        long record = one("select last_insert_id()");
        dsl.execute("insert into agency_id_list_manifest (release_id, agency_id_list_id) values (?, ?)", published, record);
        long previous = one("select last_insert_id()");
        dsl.execute("insert into agency_id_list_manifest (release_id, agency_id_list_id, prev_agency_id_list_manifest_id) "
                + "values (?, ?, ?)", working, record, previous);
        long current = one("select last_insert_id()");
        dsl.execute("update agency_id_list_manifest set next_agency_id_list_manifest_id = ? where agency_id_list_manifest_id = ?",
                current, previous);
        return current;
    }

    /** A component that has been released in the previous release and not touched since. */
    private long released(Ent e) {
        if (e == Ent.CODE_LIST) {
            return pool(Kind.CODE_LIST);
        }
        long id = madeUpAgencyIdList();
        valueFor(id);
        return id;
    }

    private long created(Ent e, Cls cls) {
        if (e == Ent.CODE_LIST) {
            return make(Kind.CODE_LIST, cls).id();
        }
        long id = factory.agencyIdListCommandRepository(REQUESTER).create(new ReleaseId(n(working)), null).value().longValue();
        dsl.execute("update agency_id_list set namespace_id = ? where agency_id_list_id = ?", namespace, recordOf(e, id));
        valueFor(id);
        setState(e, id, cls.variant().state);
        return id;
    }

    private void revise(Ent e, long id) {
        if (e == Ent.CODE_LIST) {
            factory.codeListCommandRepository(REQUESTER).revise(new CodeListManifestId(n(id)));
        } else {
            factory.agencyIdListCommandRepository(REQUESTER).revise(new AgencyIdListManifestId(n(id)));
        }
    }

    private void setState(Ent e, long id, CcState state) {
        dsl.execute("update `" + e.table + "` set state = ? where `" + e.record() + "` = ?", state.name(), recordOf(e, id));
    }

    private void becomeClass(Node node) {
        if (node.cls().variant().revised()) {
            revise(node.ent(), node.id());
        }
        if (node.cls().variant() != Variant.P) {
            setState(node.ent(), node.id(), node.cls().variant().state);
        }
    }

    // ---- the references ----

    /** What the reference holds for a target: the target, or its value. */
    private long held(Edge edge, long target) {
        return edge.viaValue() ? valueOf.get(target) : target;
    }

    private long heldPrevious(Edge edge, long target) {
        return edge.viaValue()
                ? one("select prev_agency_id_list_value_manifest_id from agency_id_list_value_manifest "
                + "where agency_id_list_value_manifest_id = ?", valueOf.get(target))
                : previous(edge.target(), target);
    }

    private String heldTable(Edge edge) {
        return edge.viaValue() ? "agency_id_list_value_manifest" : edge.target().manifest();
    }

    private String heldId(Edge edge) {
        return edge.viaValue() ? "agency_id_list_value_manifest_id" : edge.target().id();
    }

    private void pointManifest(Edge edge, long dependent, long held) {
        dsl.execute("update `" + edge.dependent().manifest() + "` set `" + edge.column() + "` = ? where `"
                + edge.dependent().id() + "` = ?", held, dependent);
    }

    private void point(Edge edge, long dependent, long target) {
        pointManifest(edge, dependent, held(edge, target));
        if (edge.recordColumn() != null) {
            long record = edge.viaValue()
                    ? one("select agency_id_list_value_id from agency_id_list_value_manifest "
                    + "where agency_id_list_value_manifest_id = ?", held(edge, target))
                    : recordOf(edge.target(), target);
            dsl.execute("update `" + edge.dependent().table + "` set `" + edge.recordColumn() + "` = ? where `"
                    + edge.dependent().record() + "` = ?", record, recordOf(edge.dependent(), dependent));
        }
    }

    private Long refOf(Edge edge, long manifest) {
        return dsl.fetchOne("select `" + edge.column() + "` from `" + edge.dependent().manifest() + "` where `"
                + edge.dependent().id() + "` = ?", manifest).get(0, Long.class);
    }

    // ---- the Working release ----

    private List<Pair> build(Edge edge, boolean onlyValid) {
        List<Pair> pairs = new ArrayList<>();
        Map<Cls, Node> targets = new LinkedHashMap<>();
        for (Cls t : classes()) {
            long id = t.variant().released ? released(edge.target()) : created(edge.target(), t);
            targets.put(t, new Node(edge.target(), id, t));
        }
        // dependents that have referred to the target since the previous release
        for (Cls t : RELEASED) {
            for (Cls d : RELEASED) {
                if (onlyValid && !valid(d, t)) {
                    continue;
                }
                long id = released(edge.dependent());
                Node target = targets.get(t);
                pointManifest(edge, previous(edge.dependent(), id), heldPrevious(edge, target.id()));
                point(edge, id, target.id());
                Node dependent = new Node(edge.dependent(), id, d);
                becomeClass(dependent);
                pairs.add(new Pair("natural", dependent, target));
            }
        }
        for (Cls t : RELEASED) {
            becomeClass(targets.get(t));
        }
        for (Cls t : classes()) {
            for (Cls d : REVISED) {
                if (onlyValid && !valid(d, t)) {
                    continue;
                }
                long id = released(edge.dependent());
                Node dependent = new Node(edge.dependent(), id, d);
                becomeClass(dependent);
                point(edge, id, targets.get(t).id());
                pairs.add(new Pair("revised and re-pointed", dependent, targets.get(t)));
            }
            for (Cls d : NEW) {
                if (onlyValid && !valid(d, t)) {
                    continue;
                }
                Node dependent = new Node(edge.dependent(), created(edge.dependent(), d), d);
                point(edge, dependent.id(), targets.get(t).id());
                pairs.add(new Pair("new", dependent, targets.get(t)));
            }
        }
        return pairs;
    }

    private Map<Ent, Set<Long>> assigned(List<Pair> pairs) {
        Map<Ent, Set<Long>> assigned = new LinkedHashMap<>();
        for (Ent e : Ent.values()) {
            assigned.put(e, new HashSet<>());
        }
        for (Pair p : pairs) {
            for (Node node : List.of(p.dependent(), p.target())) {
                if (node.cls().assigned()) {
                    assigned.get(node.ent()).add(node.id());
                }
            }
        }
        return assigned;
    }

    private static String list(Set<Long> ids) {
        return ids.isEmpty() ? "0" : String.join(",", ids.stream().map(String::valueOf).toList());
    }

    // ---- 1. what the validation says ----

    private void validation(Edge edge) {
        findLibrary("connectSpec");
        List<Pair> pairs = build(edge, false);
        Map<Ent, Set<Long>> assigned = assigned(pairs);
        long release = newRelease("8.1");
        ReleaseValidator validator = new ReleaseValidator(REQUESTER, new ReleaseId(n(release)), factory);
        validator.setAssignedAccComponentManifestIds(List.of());
        validator.setAssignedAsccpComponentManifestIds(List.of());
        validator.setAssignedBccpComponentManifestIds(List.of());
        validator.setAssignedDtComponentManifestIds(List.of());
        validator.setAssignedCodeListComponentManifestIds(
                assigned.get(Ent.CODE_LIST).stream().map(i -> new CodeListManifestId(n(i))).toList());
        validator.setAssignedAgencyIdListComponentManifestIds(
                assigned.get(Ent.AGENCY_ID_LIST).stream().map(i -> new AgencyIdListManifestId(n(i))).toList());
        ReleaseValidationResponse response = validator.validate();

        Map<Long, Node> targets = new LinkedHashMap<>();
        pairs.forEach(p -> targets.put(p.target().id(), p.target()));
        List<String> mismatches = new ArrayList<>();
        Map<String, Integer> outcomes = new TreeMap<>();
        for (Node target : targets.values()) {
            Set<String> expected = new TreeSet<>();
            for (Cls d : classes()) {
                expected.addAll(ReleaseDependencyValidatorMatrixTest.expected(scenario(d, target.cls())));
            }
            var found = target.ent() == Ent.CODE_LIST
                    ? response.getStatusMapForCodeList().get(CodeListManifestId.from(n(target.id())))
                    : response.getStatusMapForAgencyIdList().get(AgencyIdListManifestId.from(n(target.id())));
            Set<String> actual = new TreeSet<>();
            if (found != null) {
                found.stream().filter(m -> !m.getMessage().equals("Namespace is required."))
                        .forEach(m -> actual.add(m.getLevel() + "@dependency"));
            }
            outcomes.merge(expected.toString(), 1, Integer::sum);
            if (!expected.equals(actual)) {
                mismatches.add(edge.name() + " target " + target.cls() + ": expected " + expected + " but was " + actual
                        + " " + found);
            }
        }
        System.out.println(edge.name() + ": pairs " + pairs.size() + ", targets " + targets.size() + ", outcomes " + outcomes);
        assertEquals(List.of(), mismatches.stream().limit(25).toList(), mismatches.size() + " targets differ");
    }

    // ---- 2. the copy ----

    private void copy(Edge edge) {
        findLibrary("connectSpec");
        List<Pair> pairs = build(edge, true);
        Map<Ent, Set<Long>> assigned = assigned(pairs);
        long release = newRelease("8.1");
        for (Ent e : Ent.values()) {
            dsl.execute("update `" + e.table + "` set state = 'ReleaseDraft' where `" + e.record() + "` in (select `"
                    + e.record() + "` from `" + e.manifest() + "` where `" + e.id() + "` in (" + list(assigned.get(e)) + "))");
        }
        factory.ccCommandRepository(REQUESTER).copyWorkingManifests(new ReleaseId(n(release)), new ReleaseId(n(working)),
                List.of(), List.of(), List.of(), List.of(),
                assigned.get(Ent.CODE_LIST).stream().map(i -> new CodeListManifestId(n(i))).toList(),
                assigned.get(Ent.AGENCY_ID_LIST).stream().map(i -> new AgencyIdListManifestId(n(i))).toList());

        List<String> failures = new ArrayList<>();
        Map<String, Integer> checked = new TreeMap<>();
        for (Pair p : pairs) {
            Node d = p.dependent();
            Node t = p.target();
            checked.merge(p.how(), 1, Integer::sum);
            String label = edge.name() + ": " + p.how() + " " + d.cls() + " -> " + t.cls();
            Long d1 = dsl.fetch("select `" + edge.dependent().id() + "` from `" + edge.dependent().manifest()
                    + "` where release_id = ? and `next_" + edge.dependent().id() + "` = ?", release, d.id())
                    .stream().map(r -> r.get(0, Long.class)).findFirst().orElse(null);
            if (!d.cls().assigned() && !d.cls().variant().released) {
                if (d1 != null) {
                    failures.add(label + ": a component that is new and not assigned is in the new release");
                }
                continue;
            }
            if (d1 == null) {
                failures.add(label + ": the dependent is not in the new release");
                continue;
            }
            Long r1 = refOf(edge, d1);
            Long previousRef = d.cls().assigned() ? null : refOf(edge, previous(edge.dependent(), d.id()));
            if (r1 == null) {
                if (d.cls().assigned() || previousRef != null) {
                    failures.add(label + ": the dependent refers to nothing in the new release");
                }
                continue;
            }
            long r1Release = one("select release_id from `" + heldTable(edge) + "` where `" + heldId(edge) + "` = ?", r1);
            if (r1Release != release) {
                failures.add(label + ": refers to a manifest of release " + r1Release + ", not of the new release");
                continue;
            }
            if (d.cls().assigned()) {
                Long next = dsl.fetchOne("select `next_" + heldId(edge) + "` from `" + heldTable(edge) + "` where `"
                        + heldId(edge) + "` = ?", r1).get(0, Long.class);
                long workingRef = refOf(edge, d.id());
                if (next == null || next != workingRef) {
                    failures.add(label + ": refers to the manifest that follows " + next + " instead of " + workingRef);
                }
            } else {
                Long prev = dsl.fetchOne("select `prev_" + heldId(edge) + "` from `" + heldTable(edge) + "` where `"
                        + heldId(edge) + "` = ?", r1).get(0, Long.class);
                if (previousRef == null || !previousRef.equals(prev)) {
                    failures.add(label + ": refers to the manifest that follows " + prev + " but in the previous release it "
                            + "referred to " + previousRef);
                }
            }
        }
        System.out.println(edge.name() + ": pairs checked " + checked + ", failures " + failures.size());
        failures.stream().limit(200).forEach(f -> System.out.println("  " + f));
        assertEquals(List.of(), failures.stream().limit(25).toList(), failures.size() + " of " + pairs.size() + " pairs differ");
    }

    @Test
    void theValidationOfACodeListAndItsBase() {
        validation(CODE_LIST_BASE);
    }

    @Test
    void theValidationOfACodeListAndItsAgency() {
        validation(CODE_LIST_AGENCY);
    }

    @Test
    void theValidationOfAnAgencyIdListAndItsBase() {
        validation(AGENCY_BASE);
    }

    @Test
    void theValidationOfAnAgencyIdListAndItsAgency() {
        validation(AGENCY_AGENCY);
    }

    @Test
    void theCopyOfACodeListAndItsBase() {
        copy(CODE_LIST_BASE);
    }

    @Test
    void theCopyOfACodeListAndItsAgency() {
        copy(CODE_LIST_AGENCY);
    }

    @Test
    void theCopyOfAnAgencyIdListAndItsBase() {
        copy(AGENCY_BASE);
    }

    @Test
    void theCopyOfAnAgencyIdListAndItsAgency() {
        copy(AGENCY_AGENCY);
    }
}
