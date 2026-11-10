package org.oagi.score.gateway.http.api.release_management.service;

import org.jooq.Record;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Which record a component refers to in the new release, for every state of the component and of what it refers to, and
 * whether either of them is assigned to the release. Against the development database, in a transaction that is rolled back:
 * <pre>
 *   mvn test -Dtest=ReleaseReferenceScenarioDbTest -DargLine=-Dscore.dtAwdPri=true
 * </pre>
 * The rule: a component that is assigned refers to what it refers to in 'Working', in the state of the release (the record of
 * 'Working' when that is assigned, the record of the previous release when it is not); a component that is not assigned
 * but has been released is as it was in the previous release, so it refers to what it referred to there.
 */
@EnabledIfSystemProperty(named = "score.dtAwdPri", matches = "true")
class ReleaseReferenceScenarioDbTest extends ReleaseScenarioFixture {

    /** A reference held by a manifest of the dependent kind, to a manifest of the target kind. */
    record Ref(String name, Kind dependent, Kind target, String column, String recordColumn) {
    }

    private static final List<Ref> REFS = List.of(
            new Ref("ASCCP -> ACC (role of)", Kind.ASCCP, Kind.ACC, "role_of_acc_manifest_id", "role_of_acc_id"),
            new Ref("ACC -> base ACC", Kind.ACC, Kind.ACC, "based_acc_manifest_id", "based_acc_id"),
            new Ref("BCCP -> DT", Kind.BCCP, Kind.DT, "bdt_manifest_id", "bdt_id"),
            new Ref("DT -> base DT", Kind.DT, Kind.DT, "based_dt_manifest_id", "based_dt_id"));

    /** The classes that exist in the previous release as well: the ones a reference can have been made to before. */
    private static final List<Cls> RELEASED = classes().stream().filter(c -> c.variant().released).toList();

    private static final List<Cls> REVISED = classes().stream().filter(c -> c.variant().revised()).toList();

    private static final List<Cls> NEW = classes().stream().filter(c -> c.variant().isNew()).toList();

    /** A pair of a dependent and what it refers to, and how the pair came about. */
    record Pair(Ref ref, String how, Inst dependent, Inst target) {
    }

    private void point(Ref ref, long dependent, long target) {
        dsl.execute("update `" + ref.dependent().manifest() + "` set `" + ref.column() + "` = ? where `"
                + ref.dependent().id() + "` = ?", target, dependent);
        dsl.execute("update `" + ref.dependent().table + "` set `" + ref.recordColumn() + "` = ? where `"
                + ref.dependent().record() + "` = ?", recordOf(ref.target(), target), recordOf(ref.dependent(), dependent));
    }

    private long previous(Kind kind, long manifest) {
        return one("select `prev_" + kind.id() + "` from `" + kind.manifest() + "` where `" + kind.id() + "` = ?", manifest);
    }

    /**
     * A dependent that has referred to the target since the previous release: both its manifest of the previous release and
     * its manifest of 'Working' say so.
     */
    private Inst naturalDependent(Ref ref, Cls cls, Inst target) {
        long id = pool(ref.dependent());
        long targetPrevious = previous(ref.target(), target.id());
        point(ref, previous(ref.dependent(), id), targetPrevious);
        point(ref, id, target.id());
        if (cls.variant().revised()) {
            revise(ref.dependent(), id);
        }
        if (cls.variant() != Variant.P) {
            setState(ref.dependent(), id, cls.variant().state);
        }
        used.add(id);
        return new Inst(ref.dependent(), id, cls);
    }

    private void revise(Kind kind, long id) {
        switch (kind) {
            case ACC -> factory.accCommandRepository(REQUESTER).revise(new AccManifestId(n(id)));
            case ASCCP -> factory.asccpCommandRepository(REQUESTER).revise(new AsccpManifestId(n(id)));
            case BCCP -> factory.bccpCommandRepository(REQUESTER).revise(new BccpManifestId(n(id)));
            case DT -> factory.dtCommandRepository(REQUESTER).revise(new DtManifestId(n(id)));
            case CODE_LIST -> factory.codeListCommandRepository(REQUESTER).revise(new CodeListManifestId(n(id)));
        }
    }

    /**
     * A target of a class that has been released: an existing component, of which the natural dependents refer to the
     * previous release's manifest; it is changed after they have been made to refer to it.
     */
    private Inst existing(Kind kind, Cls cls) {
        long id = pool(kind);
        return new Inst(kind, id, cls);
    }

    private void becomeClass(Inst target) {
        Variant v = target.cls().variant();
        if (v.revised()) {
            revise(target.kind(), target.id());
        }
        if (v != Variant.P) {
            setState(target.kind(), target.id(), v.state);
        }
    }

    private List<Pair> build(Ref ref) {
        List<Pair> pairs = new ArrayList<>();
        // the targets: the released ones are existing components, the others are new
        Map<Cls, Inst> targets = new LinkedHashMap<>();
        for (Cls t : classes()) {
            targets.put(t, t.variant().released ? existing(ref.target(), t) : make(ref.target(), t));
        }
        // dependents that have referred to the target since the previous release (those that are in the release as they were)
        if (ref != REFS.get(1)) { // the ACCs of the library have no base to have referred to
            for (Cls t : RELEASED) {
                for (Cls d : RELEASED) {
                    if (!valid(d, t)) {
                        continue;
                    }
                    pairs.add(new Pair(ref, "natural", naturalDependent(ref, d, targets.get(t)), targets.get(t)));
                }
            }
        }
        for (Cls t : RELEASED) {
            becomeClass(targets.get(t));
        }
        // revised dependents that are made to refer to the target, and new ones
        for (Cls t : classes()) {
            for (Cls d : REVISED) {
                if (valid(d, t)) {
                    long id = pool(ref.dependent());
                    revise(ref.dependent(), id);
                    setState(ref.dependent(), id, d.variant().state);
                    point(ref, id, targets.get(t).id());
                    pairs.add(new Pair(ref, "revised and re-pointed", new Inst(ref.dependent(), id, d), targets.get(t)));
                }
            }
            for (Cls d : NEW) {
                if (valid(d, t)) {
                    Inst created = make(ref.dependent(), d);
                    point(ref, created.id(), targets.get(t).id());
                    pairs.add(new Pair(ref, "new", created, targets.get(t)));
                }
            }
        }
        return pairs;
    }

    private Long refOf(Ref ref, long manifest) {
        return dsl.fetchOne("select `" + ref.column() + "` from `" + ref.dependent().manifest() + "` where `"
                + ref.dependent().id() + "` = ?", manifest).get(0, Long.class);
    }

    @Test
    void everyReferenceIsToTheRecordTheRulesSayInTheNewRelease() {
        findLibrary("connectSpec");
        runReferences();
    }

    private void runReferences() {
        List<Pair> pairs = new ArrayList<>();
        for (Ref ref : REFS) {
            pairs.addAll(build(ref));
        }

        // the assignment: everything of the classes that are assigned
        Map<Kind, Set<Long>> assigned = new LinkedHashMap<>();
        for (Kind k : Kind.values()) {
            assigned.put(k, new HashSet<>());
        }
        for (Pair p : pairs) {
            for (Inst i : List.of(p.dependent(), p.target())) {
                if (i.cls().assigned()) {
                    assigned.get(i.kind()).add(i.id());
                }
            }
        }
        long release = newRelease("8.1");
        ReleaseId releaseId = new ReleaseId(n(release));
        for (Kind k : Kind.values()) {
            if (!assigned.get(k).isEmpty()) {
                dsl.execute("update `" + k.table + "` set state = 'ReleaseDraft' where `" + k.record() + "` in (select `"
                        + k.record() + "` from `" + k.manifest() + "` where `" + k.id() + "` in ("
                        + String.join(",", assigned.get(k).stream().map(String::valueOf).toList()) + "))");
            }
        }
        factory.ccCommandRepository(REQUESTER).copyWorkingManifests(releaseId, new ReleaseId(n(working)),
                assigned.get(Kind.ACC).stream().map(i -> new AccManifestId(n(i))).toList(),
                assigned.get(Kind.ASCCP).stream().map(i -> new AsccpManifestId(n(i))).toList(),
                assigned.get(Kind.BCCP).stream().map(i -> new BccpManifestId(n(i))).toList(),
                assigned.get(Kind.DT).stream().map(i -> new DtManifestId(n(i))).toList(),
                assigned.get(Kind.CODE_LIST).stream().map(i -> new CodeListManifestId(n(i))).toList(), List.of());

        List<String> failures = new ArrayList<>();
        Map<String, Integer> checked = new TreeMap<>();
        for (Pair p : pairs) {
            Ref ref = p.ref();
            Inst d = p.dependent();
            Inst t = p.target();
            checked.merge(ref.name() + " / " + p.how(), 1, Integer::sum);
            Long d1 = dsl.fetch("select `" + d.kind().id() + "` from `" + d.kind().manifest() + "` where release_id = ? and `next_"
                    + d.kind().id() + "` = ?", release, d.id()).stream().map(r -> r.get(0, Long.class)).findFirst().orElse(null);
            String label = ref.name() + ": " + p.how() + " " + d.cls() + " -> " + t.cls();

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
            Long r1 = dsl.fetchOne("select `" + ref.column() + "` from `" + d.kind().manifest() + "` where `" + d.kind().id()
                    + "` = ?", d1).get(0, Long.class);
            if (r1 == null) {
                // fine for a component that is as it was in the previous release, where it referred to nothing
                boolean referredToNothing = !d.cls().assigned() && refOf(ref, previous(d.kind(), d.id())) == null;
                if (!referredToNothing) {
                    failures.add(label + ": the dependent refers to nothing in the new release");
                }
                continue;
            }
            if (!d.cls().assigned() && refOf(ref, previous(d.kind(), d.id())) == null) {
                failures.add(label + ": refers to a manifest but referred to nothing in the previous release");
                continue;
            }
            long r1Release = one("select release_id from `" + ref.target().manifest() + "` where `" + ref.target().id() + "` = ?", r1);
            if (r1Release != release) {
                failures.add(label + ": refers to a manifest of release " + r1Release + ", not of the new release");
                continue;
            }
            Long r1Next = dsl.fetchOne("select `next_" + ref.target().id() + "` from `" + ref.target().manifest() + "` where `"
                    + ref.target().id() + "` = ?", r1).get(0, Long.class);
            long r1Record = recordOf(ref.target(), r1);

            if (d.cls().assigned()) {
                // what the dependent refers to in 'Working', in the state of the release
                long workingTarget = refOf(ref, d.id());
                long expectedRecord = assigned.get(ref.target()).contains(workingTarget)
                        ? recordOf(ref.target(), workingTarget) : recordOf(ref.target(), previous(ref.target(), workingTarget));
                if (r1Next == null || r1Next != workingTarget) {
                    failures.add(label + ": refers to the manifest that follows " + r1Next + " instead of " + workingTarget);
                } else if (r1Record != expectedRecord) {
                    failures.add(label + ": refers to record " + r1Record + " instead of " + expectedRecord);
                }
            } else {
                // as it was in the previous release
                Long previousTarget = refOf(ref, previous(d.kind(), d.id()));
                Long r1Prev = dsl.fetchOne("select `prev_" + ref.target().id() + "` from `" + ref.target().manifest()
                        + "` where `" + ref.target().id() + "` = ?", r1).get(0, Long.class);
                if (previousTarget == null || !previousTarget.equals(r1Prev)) {
                    failures.add(label + ": refers to the manifest that follows " + r1Prev + " but in the previous release it "
                            + "referred to " + previousTarget);
                } else {
                    Long workingOfPrevious = dsl.fetchOne("select `next_" + ref.target().id() + "` from `" + ref.target().manifest()
                            + "` where `" + ref.target().id() + "` = ?", previousTarget).get(0, Long.class);
                    long expectedRecord = workingOfPrevious != null && assigned.get(ref.target()).contains(workingOfPrevious)
                            ? recordOf(ref.target(), workingOfPrevious) : recordOf(ref.target(), previousTarget);
                    if (r1Record != expectedRecord) {
                        failures.add(label + ": refers to record " + r1Record + " instead of " + expectedRecord);
                    }
                }
                long dRecord = recordOf(d.kind(), d1);
                long previousRecord = recordOf(d.kind(), previous(d.kind(), d.id()));
                if (dRecord != previousRecord) {
                    failures.add(label + ": holds record " + dRecord + " instead of " + previousRecord + " of the previous release");
                }
            }
        }
        System.out.println("pairs checked: " + checked);
        System.out.println("failures: " + failures.size());
        failures.stream().limit(400).forEach(f -> System.out.println("  " + f));
        assertEquals(List.of(), failures.stream().limit(25).toList(), failures.size() + " of " + pairs.size() + " pairs differ");
    }

    // ---- the allowed primitives of a Data Type and of its supplementary components ----

    /** What an allowed primitive refers to: a Code List or an Agency ID List. */
    private record Tgt(String table) {
        String manifest() {
            return table + "_manifest";
        }

        String id() {
            return table + "_manifest_id";
        }

        String record() {
            return table + "_id";
        }

        String column() {
            return table + "_manifest_id";
        }
    }

    private static final Tgt CODE_LIST = new Tgt("code_list");
    private static final Tgt AGENCY_ID_LIST = new Tgt("agency_id_list");

    private long tgtRecord(Tgt t, long manifest) {
        return one("select `" + t.record() + "` from `" + t.manifest() + "` where `" + t.id() + "` = ?", manifest);
    }

    private long tgtPrevious(Tgt t, long manifest) {
        return one("select `prev_" + t.id() + "` from `" + t.manifest() + "` where `" + t.id() + "` = ?", manifest);
    }

    private void tgtState(Tgt t, long manifest, CcState state) {
        dsl.execute("update `" + t.table + "` set state = ? where `" + t.record() + "` = ?", state.name(), tgtRecord(t, manifest));
    }

    /** A component that has been released in the previous release and not touched since: an existing one, or a made-up one. */
    private long tgtReleased(Tgt t) {
        if (t == CODE_LIST) {
            return pool(Kind.CODE_LIST);
        }
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

    private long tgtNew(Tgt t) {
        if (t == CODE_LIST) {
            return make(Kind.CODE_LIST, new Cls(Variant.N_WIP, false)).id();
        }
        long id = factory.agencyIdListCommandRepository(REQUESTER)
                .create(new ReleaseId(n(working)), null).value().longValue();
        dsl.execute("update agency_id_list set namespace_id = ? where agency_id_list_id = ?", namespace, tgtRecord(t, id));
        return id;
    }

    private void tgtRevise(Tgt t, long id) {
        if (t == CODE_LIST) {
            revise(Kind.CODE_LIST, id);
        } else {
            factory.agencyIdListCommandRepository(REQUESTER)
                    .revise(new org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId(n(id)));
        }
    }

    private void insertRow(String table, String owner, long release, long ownerRecord, Tgt t, long target) {
        dsl.execute("insert into `" + table + "` (release_id, `" + owner + "`, cdt_pri_id, `" + t.column()
                + "`, is_default) values (?, ?, (select cdt_pri_id from cdt_pri where name = 'Token'), ?, 0)",
                release, ownerRecord, target);
    }

    /** The first supplementary component of a Data Type manifest. */
    private long scOf(long dtManifest) {
        return one("select min(dt_sc_manifest_id) from dt_sc_manifest where owner_dt_manifest_id = ?", dtManifest);
    }

    private long scRecord(long scManifest) {
        return one("select dt_sc_id from dt_sc_manifest where dt_sc_manifest_id = ?", scManifest);
    }

    private long previous2(long scManifest) {
        return one("select prev_dt_sc_manifest_id from dt_sc_manifest where dt_sc_manifest_id = ?", scManifest);
    }

    private void addRows(long release, long dtManifest, long scManifest, Tgt t, long target) {
        insertRow("dt_awd_pri", "dt_id", release, recordOf(Kind.DT, dtManifest), t, target);
        insertRow("dt_sc_awd_pri", "dt_sc_id", release, scRecord(scManifest), t, target);
    }

    record Pair2(String how, Inst dependent, Cls targetClass, long target) {
    }

    private List<Pair2> buildAllowedPrimitives(Tgt t) {
        List<Pair2> pairs = new ArrayList<>();
        Map<Cls, Long> targets = new LinkedHashMap<>();
        for (Cls c : classes()) {
            targets.put(c, c.variant().released ? tgtReleased(t) : tgtNew(t));
        }
        // Data Types that have used the target since the previous release
        for (Cls c : RELEASED) {
            for (Cls d : RELEASED) {
                if (!valid(d, c)) {
                    continue;
                }
                long id = pool(Kind.DT);
                long scManifest = scOf(id);
                addRows(published, previous(Kind.DT, id), previous2(scManifest), t, tgtPrevious(t, targets.get(c)));
                addRows(working, id, scManifest, t, targets.get(c));
                if (d.variant().revised()) {
                    revise(Kind.DT, id);
                }
                if (d.variant() != Variant.P) {
                    setState(Kind.DT, id, d.variant().state);
                }
                pairs.add(new Pair2("natural", new Inst(Kind.DT, id, d), c, targets.get(c)));
            }
        }
        for (Cls c : classes()) {
            long id = targets.get(c);
            if (c.variant().revised()) {
                tgtRevise(t, id);
            }
            if (c.variant() != Variant.P) {
                tgtState(t, id, c.variant().state);
            }
        }
        for (Cls c : classes()) {
            for (Cls d : REVISED) {
                if (valid(d, c)) {
                    long id = pool(Kind.DT);
                    revise(Kind.DT, id);
                    setState(Kind.DT, id, d.variant().state);
                    addRows(working, id, scOf(id), t, targets.get(c));
                    pairs.add(new Pair2("revised and re-pointed", new Inst(Kind.DT, id, d), c, targets.get(c)));
                }
            }
            for (Cls d : NEW) {
                if (valid(d, c)) {
                    Inst created = make(Kind.DT, d);
                    addRows(working, created.id(), scOf(created.id()), t, targets.get(c));
                    pairs.add(new Pair2("new", created, c, targets.get(c)));
                }
            }
        }
        return pairs;
    }

    /** What the allowed primitive rows of a record refer to: the manifest they follow or are followed by, and its record. */
    private Set<String> refs(Tgt t, String table, String owner, long release, long ownerRecord, boolean byNext) {
        Set<String> found = new java.util.TreeSet<>();
        for (Record r : dsl.fetch("select p.`" + t.column() + "` from `" + table + "` p where p.release_id = ? and p.`" + owner
                + "` = ? and p.`" + t.column() + "` is not null", release, ownerRecord)) {
            long manifest = r.get(0, Long.class);
            Long link = dsl.fetchOne("select `" + (byNext ? "next_" : "prev_") + t.id() + "` from `" + t.manifest() + "` where `"
                    + t.id() + "` = ?", manifest).get(0, Long.class);
            found.add(link + " -> record " + tgtRecord(t, manifest));
        }
        return found;
    }

    private void runAllowedPrimitives(Tgt t) {
        List<Pair2> pairs = buildAllowedPrimitives(t);
        Set<Long> assignedDt = new HashSet<>();
        Set<Long> assignedTarget = new HashSet<>();
        for (Pair2 p : pairs) {
            if (p.dependent().cls().assigned()) {
                assignedDt.add(p.dependent().id());
            }
            if (p.targetClass().assigned()) {
                assignedTarget.add(p.target());
            }
        }
        long release = newRelease("8.1");
        dsl.execute("update dt set state = 'ReleaseDraft' where dt_id in (select dt_id from dt_manifest where dt_manifest_id in ("
                + (assignedDt.isEmpty() ? "0" : String.join(",", assignedDt.stream().map(String::valueOf).toList())) + "))");
        if (!assignedTarget.isEmpty()) {
            dsl.execute("update `" + t.table + "` set state = 'ReleaseDraft' where `" + t.record() + "` in (select `" + t.record()
                    + "` from `" + t.manifest() + "` where `" + t.id() + "` in ("
                    + String.join(",", assignedTarget.stream().map(String::valueOf).toList()) + "))");
        }
        factory.ccCommandRepository(REQUESTER).copyWorkingManifests(new ReleaseId(n(release)), new ReleaseId(n(working)),
                List.of(), List.of(), List.of(),
                assignedDt.stream().map(i -> new DtManifestId(n(i))).toList(),
                t == CODE_LIST ? assignedTarget.stream().map(i -> new CodeListManifestId(n(i))).toList() : List.of(),
                t == AGENCY_ID_LIST ? assignedTarget.stream().map(i -> new org.oagi.score.gateway.http.api.agency_id_management
                        .model.AgencyIdListManifestId(n(i))).toList() : List.of());

        List<String> failures = new ArrayList<>();
        Map<String, Integer> checked = new TreeMap<>();
        for (Pair2 p : pairs) {
            Inst d = p.dependent();
            checked.merge(p.how(), 1, Integer::sum);
            String label = t.table + ": " + p.how() + " " + d.cls() + " -> " + p.targetClass();
            Long d1 = dsl.fetch("select dt_manifest_id from dt_manifest where release_id = ? and next_dt_manifest_id = ?", release,
                    d.id()).stream().map(r -> r.get(0, Long.class)).findFirst().orElse(null);
            if (!d.cls().assigned() && !d.cls().variant().released) {
                if (d1 != null) {
                    failures.add(label + ": a Data Type that is new and not assigned is in the new release");
                }
                continue;
            }
            if (d1 == null) {
                failures.add(label + ": the Data Type is not in the new release");
                continue;
            }
            long wsc = scOf(d.id());
            Long sc1 = dsl.fetch("select dt_sc_manifest_id from dt_sc_manifest where release_id = ? and next_dt_sc_manifest_id = ?",
                    release, wsc).stream().map(r -> r.get(0, Long.class)).findFirst().orElse(null);
            for (boolean sc : new boolean[]{false, true}) {
                String table = sc ? "dt_sc_awd_pri" : "dt_awd_pri";
                String owner = sc ? "dt_sc_id" : "dt_id";
                String which = sc ? " (SC)" : " (DT)";
                if (sc && sc1 == null) {
                    failures.add(label + which + ": the supplementary component is not in the new release");
                    continue;
                }
                long record1 = sc ? scRecord(sc1) : recordOf(Kind.DT, d1);
                Set<String> expected = new java.util.TreeSet<>();
                Set<String> actual;
                if (d.cls().assigned()) {
                    long workingRecord = sc ? scRecord(wsc) : recordOf(Kind.DT, d.id());
                    for (Record r : dsl.fetch("select `" + t.column() + "` from `" + table + "` where release_id = ? and `" + owner
                            + "` = ? and `" + t.column() + "` is not null", working, workingRecord)) {
                        long wt = r.get(0, Long.class);
                        long record = assignedTarget.contains(wt) ? tgtRecord(t, wt) : tgtRecord(t, tgtPrevious(t, wt));
                        expected.add(wt + " -> record " + record);
                    }
                    actual = refs(t, table, owner, release, record1, true);
                    if (record1 != workingRecord) {
                        failures.add(label + which + ": holds record " + record1 + " instead of " + workingRecord);
                    }
                } else {
                    long previousRecord = sc ? scRecord(previous2(wsc)) : recordOf(Kind.DT, previous(Kind.DT, d.id()));
                    for (Record r : dsl.fetch("select `" + t.column() + "` from `" + table + "` where release_id = ? and `" + owner
                            + "` = ? and `" + t.column() + "` is not null", published, previousRecord)) {
                        long r0 = r.get(0, Long.class);
                        Long next = dsl.fetchOne("select `next_" + t.id() + "` from `" + t.manifest() + "` where `" + t.id()
                                + "` = ?", r0).get(0, Long.class);
                        long record = next != null && assignedTarget.contains(next) ? tgtRecord(t, next) : tgtRecord(t, r0);
                        expected.add(r0 + " -> record " + record);
                    }
                    actual = refs(t, table, owner, release, record1, false);
                    if (record1 != previousRecord) {
                        failures.add(label + which + ": holds record " + record1 + " instead of " + previousRecord);
                    }
                }
                if (!expected.equals(actual)) {
                    failures.add(label + which + ": refers to " + actual + " but should refer to " + expected);
                }
            }
        }
        System.out.println("pairs checked: " + checked);
        System.out.println("failures: " + failures.size());
        failures.stream().limit(400).forEach(f -> System.out.println("  " + f));
        assertEquals(List.of(), failures.stream().limit(25).toList(), failures.size() + " of " + pairs.size() + " pairs differ");
    }

    @Test
    void theCodeListAnAllowedPrimitiveUsesIsTheOneTheRulesSayInTheNewRelease() {
        findLibrary("connectSpec");
        runAllowedPrimitives(CODE_LIST);
    }

    @Test
    void theAgencyIdListAnAllowedPrimitiveUsesIsTheOneTheRulesSayInTheNewRelease() {
        findLibrary("connectSpec");
        runAllowedPrimitives(AGENCY_ID_LIST);
    }

    // ---- the supplementary components of new and revised Data Types ----

    private List<Long> scs(long dtManifest) {
        return ids("select dt_sc_manifest_id from dt_sc_manifest where owner_dt_manifest_id = ? order by dt_sc_manifest_id", dtManifest);
    }

    private void pointSc(long sc, long base) {
        dsl.execute("update dt_sc_manifest set based_dt_sc_manifest_id = ? where dt_sc_manifest_id = ?", base, sc);
        dsl.execute("update dt_sc set based_dt_sc_id = ? where dt_sc_id = ?", scRecord(base), scRecord(sc));
    }

    private Long ownerOf(long sc) {
        return one("select owner_dt_manifest_id from dt_sc_manifest where dt_sc_manifest_id = ?", sc);
    }

    /** Makes the Data Type derive from the base, and its supplementary components from the ones of the base. */
    private void deriveFrom(long dt, long base) {
        dsl.execute("update dt_manifest set based_dt_manifest_id = ? where dt_manifest_id = ?", base, dt);
        dsl.execute("update dt set based_dt_id = ? where dt_id = ?", recordOf(Kind.DT, base), recordOf(Kind.DT, dt));
        List<Long> own = scs(dt);
        List<Long> baseScs = scs(base);
        for (int i = 0; i < Math.min(own.size(), baseScs.size()); i++) {
            pointSc(own.get(i), baseScs.get(i));
        }
    }

    record Pair3(String how, Inst dependent, Cls baseClass, long base) {
    }

    @Test
    void theSupplementaryComponentsOfNewAndRevisedDataTypesAreWhatTheRulesSayInTheNewRelease() {
        findLibrary("connectSpec");
        var dtCommand = factory.dtCommandRepository(REQUESTER);
        List<Pair3> pairs = new ArrayList<>();
        Map<Cls, Long> bases = new LinkedHashMap<>();
        for (Cls c : classes()) {
            bases.put(c, c.variant().released ? pool(Kind.DT) : make(Kind.DT, c).id());
        }
        for (Cls c : RELEASED) {
            becomeClass(new Inst(Kind.DT, bases.get(c), c));
        }
        for (Cls c : classes()) {
            for (Cls d : classes()) {
                if (d.variant() == Variant.P || !valid(d, c)) {
                    continue;
                }
                long id;
                if (d.variant().revised()) {
                    id = pool(Kind.DT);
                    revise(Kind.DT, id);
                    setState(Kind.DT, id, d.variant().state);
                } else {
                    id = make(Kind.DT, d).id();
                }
                deriveFrom(id, bases.get(c));
                dtCommand.appendDtSc(new DtManifestId(n(id)));
                dtCommand.createDtScFromBase(new DtManifestId(n(id)),
                        new org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId(n(scs(bases.get(c)).get(0))));
                pairs.add(new Pair3(d.variant().revised() ? "revised" : "new", new Inst(Kind.DT, id, d), c, bases.get(c)));
            }
        }
        // revised Data Types that keep their base, and get supplementary components
        for (Cls d : REVISED) {
            long id = pool(Kind.DT);
            revise(Kind.DT, id);
            setState(Kind.DT, id, d.variant().state);
            dtCommand.appendDtSc(new DtManifestId(n(id)));
            pairs.add(new Pair3("revised, base kept", new Inst(Kind.DT, id, d), RELEASED.get(0), -1));
        }

        Set<Long> assignedDt = new HashSet<>();
        for (Pair3 p : pairs) {
            if (p.dependent().cls().assigned()) {
                assignedDt.add(p.dependent().id());
            }
        }
        for (Cls c : classes()) {
            if (c.assigned()) {
                assignedDt.add(bases.get(c));
            }
        }
        long release = newRelease("8.1");
        dsl.execute("update dt set state = 'ReleaseDraft' where dt_id in (select dt_id from dt_manifest where dt_manifest_id in ("
                + String.join(",", assignedDt.stream().map(String::valueOf).toList()) + "))");
        factory.ccCommandRepository(REQUESTER).copyWorkingManifests(new ReleaseId(n(release)), new ReleaseId(n(working)),
                List.of(), List.of(), List.of(), assignedDt.stream().map(i -> new DtManifestId(n(i))).toList(), List.of(), List.of());

        List<String> failures = new ArrayList<>();
        Map<String, Integer> checked = new TreeMap<>();
        for (Pair3 p : pairs) {
            Inst d = p.dependent();
            checked.merge(p.how(), 1, Integer::sum);
            String label = p.how() + " " + d.cls() + " (base " + p.baseClass() + ")";
            Long d1 = dsl.fetch("select dt_manifest_id from dt_manifest where release_id = ? and next_dt_manifest_id = ?", release,
                    d.id()).stream().map(r -> r.get(0, Long.class)).findFirst().orElse(null);
            if (!d.cls().assigned() && !d.cls().variant().released) {
                if (d1 != null) {
                    failures.add(label + ": a Data Type that is new and not assigned is in the new release");
                }
                continue;
            }
            if (d1 == null) {
                failures.add(label + ": the Data Type is not in the new release");
                continue;
            }
            List<Long> in81 = ids("select dt_sc_manifest_id from dt_sc_manifest where release_id = ? and owner_dt_manifest_id = ? "
                    + "order by dt_sc_manifest_id", release, d1);
            // the supplementary components that are in the release
            List<Long> expectedOrigins;
            boolean assigned = d.cls().assigned();
            if (assigned) {
                expectedOrigins = scs(d.id());
            } else {
                expectedOrigins = scs(previous(Kind.DT, d.id()));
            }
            List<Long> origins = new ArrayList<>();
            for (long sc1 : in81) {
                origins.add(one("select " + (assigned ? "next" : "prev") + "_dt_sc_manifest_id from dt_sc_manifest "
                        + "where dt_sc_manifest_id = ?", sc1));
            }
            if (!new java.util.TreeSet<>(origins).equals(new java.util.TreeSet<>(expectedOrigins))) {
                failures.add(label + ": the supplementary components in the new release follow " + origins + " instead of "
                        + expectedOrigins);
                continue;
            }
            // what each refers to as its base
            for (long sc1 : in81) {
                long origin = one("select " + (assigned ? "next" : "prev") + "_dt_sc_manifest_id from dt_sc_manifest where dt_sc_manifest_id = ?", sc1);
                Long based1 = dsl.fetchOne("select based_dt_sc_manifest_id from dt_sc_manifest where dt_sc_manifest_id = ?", sc1)
                        .get(0, Long.class);
                Long basedOrigin = dsl.fetchOne("select based_dt_sc_manifest_id from dt_sc_manifest where dt_sc_manifest_id = ?",
                        origin).get(0, Long.class);
                if (basedOrigin == null) {
                    if (based1 != null) {
                        failures.add(label + ": the supplementary component refers to a base, but it had none");
                    }
                    continue;
                }
                if (based1 == null) {
                    failures.add(label + ": the supplementary component refers to no base, but it had " + basedOrigin);
                    continue;
                }
                long based1Release = one("select release_id from dt_sc_manifest where dt_sc_manifest_id = ?", based1);
                if (based1Release != release) {
                    failures.add(label + ": the supplementary component has a base of release " + based1Release);
                    continue;
                }
                // the base, as the new release holds it
                long ownerOfBase = ownerOf(basedOrigin);
                long workingOfBase = assigned ? basedOrigin : one("select next_dt_sc_manifest_id from dt_sc_manifest "
                        + "where dt_sc_manifest_id = ?", basedOrigin);
                Long followed = dsl.fetchOne("select " + (assigned ? "next" : "prev") + "_dt_sc_manifest_id from dt_sc_manifest "
                        + "where dt_sc_manifest_id = ?", based1).get(0, Long.class);
                if (followed == null || !followed.equals(basedOrigin)) {
                    failures.add(label + ": the base is that of " + followed + " instead of " + basedOrigin);
                    continue;
                }
                boolean baseAssigned = assignedDt.contains(assigned ? ownerOf(workingOfBase) : ownerOf(workingOfBase));
                long expectedRecord = baseAssigned ? scRecord(workingOfBase)
                        : scRecord(assigned ? one("select prev_dt_sc_manifest_id from dt_sc_manifest where dt_sc_manifest_id = ?",
                        workingOfBase) : basedOrigin);
                if (scRecord(based1) != expectedRecord) {
                    failures.add(label + ": the base is record " + scRecord(based1) + " instead of " + expectedRecord);
                }
            }
        }
        System.out.println("Data Types checked: " + checked);
        System.out.println("failures: " + failures.size());
        failures.stream().limit(400).forEach(f -> System.out.println("  " + f));
        assertEquals(List.of(), failures.stream().limit(25).toList(), failures.size() + " of " + pairs.size() + " Data Types differ");
    }

    // ---- the associations of an ACC: ASCC -> ASCCP and BCC -> BCCP ----

    /** An association (ASCC or BCC) of an ACC to a property. */
    private record Assoc(String table, Kind target, String toColumn) {
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

    private static final Assoc ASCC = new Assoc("ascc", Kind.ASCCP, "to_asccp_manifest_id");
    private static final Assoc BCC = new Assoc("bcc", Kind.BCCP, "to_bccp_manifest_id");

    record Pair4(String how, Inst dependent, Inst target, long association) {
    }

    private long createAssociation(Assoc a, long acc, long property) {
        var command = factory.accCommandRepository(REQUESTER);
        return a == ASCC
                ? command.createAscc(new AccManifestId(n(acc)), new AsccpManifestId(n(property)), null, null, true).value().longValue()
                : command.createBcc(new AccManifestId(n(acc)), new BccpManifestId(n(property)), null, null, true).value().longValue();
    }

    /** An association of 'Working' that was in the previous release, between two components that are not used yet. */
    private long[] naturalAssociation(Assoc a) {
        String usedList = String.join(",", used.stream().map(String::valueOf).toList()).replaceAll("^$", "0");
        Record r = dsl.fetch("select x.`" + a.id() + "`, x.from_acc_manifest_id, x.`" + a.toColumn() + "` from `" + a.manifest()
                + "` x join acc_manifest f on f.acc_manifest_id = x.from_acc_manifest_id join acc fa on fa.acc_id = f.acc_id "
                + "join `" + a.target().manifest() + "` t on t.`" + a.target().id() + "` = x.`" + a.toColumn() + "` join `"
                + a.target().table + "` ta on ta.`" + a.target().record() + "` = t.`" + a.target().record() + "` "
                + "where x.release_id = ? and x.`prev_" + a.id() + "` is not null and fa.state = 'Published' and ta.state = 'Published' "
                + "and f.prev_acc_manifest_id is not null and t.`prev_" + a.target().id() + "` is not null and t.release_id = ? "
                + "and x.from_acc_manifest_id not in (" + usedList + ") and x.`" + a.toColumn() + "` not in (" + usedList + ") "
                + "order by x.`" + a.id() + "` limit 1", working, working).get(0);
        used.add(r.get(1, Long.class));
        used.add(r.get(2, Long.class));
        return new long[]{r.get(0, Long.class), r.get(1, Long.class), r.get(2, Long.class)};
    }

    private List<Pair4> buildAssociations(Assoc a) {
        List<Pair4> pairs = new ArrayList<>();
        // associations that have been there since the previous release
        for (Cls t : RELEASED) {
            for (Cls d : RELEASED) {
                if (!valid(d, t)) {
                    continue;
                }
                long[] natural = naturalAssociation(a);
                Inst dependent = new Inst(Kind.ACC, natural[1], d);
                Inst target = new Inst(a.target(), natural[2], t);
                pairs.add(new Pair4("natural", dependent, target, natural[0]));
            }
        }
        for (Pair4 p : pairs) {
            if (p.dependent().cls().variant().revised()) {
                revise(Kind.ACC, p.dependent().id());
            }
            if (p.dependent().cls().variant() != Variant.P) {
                setState(Kind.ACC, p.dependent().id(), p.dependent().cls().variant().state);
            }
            becomeClass(p.target());
        }
        // associations that are made in 'Working'
        Map<Cls, Inst> targets = new LinkedHashMap<>();
        for (Cls t : classes()) {
            targets.put(t, t.variant().released ? existing(a.target(), t) : make(a.target(), t));
        }
        for (Cls t : classes()) {
            if (t.variant().released) {
                becomeClass(targets.get(t));
            }
        }
        for (Cls t : classes()) {
            for (Cls d : REVISED) {
                if (valid(d, t)) {
                    long id = pool(Kind.ACC);
                    revise(Kind.ACC, id);
                    setState(Kind.ACC, id, d.variant().state);
                    pairs.add(new Pair4("revised and added", new Inst(Kind.ACC, id, d), targets.get(t),
                            createAssociation(a, id, targets.get(t).id())));
                }
            }
            for (Cls d : NEW) {
                if (valid(d, t)) {
                    Inst created = make(Kind.ACC, d);
                    pairs.add(new Pair4("new", created, targets.get(t), createAssociation(a, created.id(), targets.get(t).id())));
                }
            }
        }
        return pairs;
    }

    private void runAssociations(Assoc a) {
        List<Pair4> pairs = buildAssociations(a);
        Map<Kind, Set<Long>> assigned = new LinkedHashMap<>();
        for (Kind k : Kind.values()) {
            assigned.put(k, new HashSet<>());
        }
        for (Pair4 p : pairs) {
            for (Inst i : List.of(p.dependent(), p.target())) {
                if (i.cls().assigned()) {
                    assigned.get(i.kind()).add(i.id());
                }
            }
        }
        long release = newRelease("8.1");
        for (Kind k : List.of(Kind.ACC, a.target())) {
            if (!assigned.get(k).isEmpty()) {
                dsl.execute("update `" + k.table + "` set state = 'ReleaseDraft' where `" + k.record() + "` in (select `"
                        + k.record() + "` from `" + k.manifest() + "` where `" + k.id() + "` in ("
                        + String.join(",", assigned.get(k).stream().map(String::valueOf).toList()) + "))");
            }
        }
        factory.ccCommandRepository(REQUESTER).copyWorkingManifests(new ReleaseId(n(release)), new ReleaseId(n(working)),
                assigned.get(Kind.ACC).stream().map(i -> new AccManifestId(n(i))).toList(),
                assigned.get(Kind.ASCCP).stream().map(i -> new AsccpManifestId(n(i))).toList(),
                assigned.get(Kind.BCCP).stream().map(i -> new BccpManifestId(n(i))).toList(), List.of(), List.of(), List.of());

        List<String> failures = new ArrayList<>();
        Map<String, Integer> checked = new TreeMap<>();
        Kind target = a.target();
        for (Pair4 p : pairs) {
            Inst d = p.dependent();
            Inst t = p.target();
            checked.merge(a.table + " / " + p.how(), 1, Integer::sum);
            String label = a.table + ": " + p.how() + " " + d.cls() + " -> " + t.cls();
            Long d1 = dsl.fetch("select acc_manifest_id from acc_manifest where release_id = ? and next_acc_manifest_id = ?", release,
                    d.id()).stream().map(r -> r.get(0, Long.class)).findFirst().orElse(null);
            if (!d.cls().assigned() && !d.cls().variant().released) {
                if (d1 != null) {
                    failures.add(label + ": an ACC that is new and not assigned is in the new release");
                }
                continue;
            }
            if (d1 == null) {
                failures.add(label + ": the ACC is not in the new release");
                continue;
            }
            Long a1 = dsl.fetch("select `" + a.id() + "` from `" + a.manifest() + "` where release_id = ? and `next_" + a.id()
                    + "` = ?", release, p.association()).stream().map(r -> r.get(0, Long.class)).findFirst().orElse(null);
            long working0 = p.association();
            if (d.cls().assigned()) {
                if (a1 == null) {
                    failures.add(label + ": the association is not in the new release");
                    continue;
                }
                long from = one("select from_acc_manifest_id from `" + a.manifest() + "` where `" + a.id() + "` = ?", a1);
                long r1 = one("select `" + a.toColumn() + "` from `" + a.manifest() + "` where `" + a.id() + "` = ?", a1);
                long workingTarget = one("select `" + a.toColumn() + "` from `" + a.manifest() + "` where `" + a.id() + "` = ?", working0);
                long expectedRecord = assigned.get(target).contains(workingTarget) ? recordOf(target, workingTarget)
                        : recordOf(target, previous(target, workingTarget));
                Long next = dsl.fetchOne("select `next_" + target.id() + "` from `" + target.manifest() + "` where `" + target.id()
                        + "` = ?", r1).get(0, Long.class);
                if (from != d1) {
                    failures.add(label + ": the association is of ACC " + from + " instead of " + d1);
                } else if (next == null || next != workingTarget) {
                    failures.add(label + ": refers to the manifest that follows " + next + " instead of " + workingTarget);
                } else if (recordOf(target, r1) != expectedRecord) {
                    failures.add(label + ": refers to record " + recordOf(target, r1) + " instead of " + expectedRecord);
                }
                long associationRecord = one("select `" + a.record() + "` from `" + a.manifest() + "` where `" + a.id() + "` = ?", a1);
                long workingRecord = one("select `" + a.record() + "` from `" + a.manifest() + "` where `" + a.id() + "` = ?", working0);
                if (associationRecord != workingRecord) {
                    failures.add(label + ": the association holds record " + associationRecord + " instead of " + workingRecord);
                }
            } else {
                // as it was in the previous release
                Long a0 = dsl.fetchOne("select `prev_" + a.id() + "` from `" + a.manifest() + "` where `" + a.id() + "` = ?", working0)
                        .get(0, Long.class);
                if (a0 == null) {
                    if (a1 != null) {
                        failures.add(label + ": an association made after the previous release is in the new release");
                    }
                } else if (a1 == null) {
                    failures.add(label + ": an association of the previous release is not in the new release");
                } else {
                    long r0 = one("select `" + a.toColumn() + "` from `" + a.manifest() + "` where `" + a.id() + "` = ?", a0);
                    long r1 = one("select `" + a.toColumn() + "` from `" + a.manifest() + "` where `" + a.id() + "` = ?", a1);
                    Long prev = dsl.fetchOne("select `prev_" + target.id() + "` from `" + target.manifest() + "` where `"
                            + target.id() + "` = ?", r1).get(0, Long.class);
                    Long next0 = dsl.fetchOne("select `next_" + target.id() + "` from `" + target.manifest() + "` where `"
                            + target.id() + "` = ?", r0).get(0, Long.class);
                    long expectedRecord = next0 != null && assigned.get(target).contains(next0) ? recordOf(target, next0)
                            : recordOf(target, r0);
                    if (prev == null || prev != r0) {
                        failures.add(label + ": refers to the manifest that follows " + prev + " instead of " + r0);
                    } else if (recordOf(target, r1) != expectedRecord) {
                        failures.add(label + ": refers to record " + recordOf(target, r1) + " instead of " + expectedRecord);
                    }
                }
                long count1 = one("select count(*) from `" + a.manifest() + "` where from_acc_manifest_id = ?", d1);
                long count0 = one("select count(*) from `" + a.manifest() + "` where from_acc_manifest_id = ?", previous(Kind.ACC, d.id()));
                if (count1 != count0) {
                    failures.add(label + ": the ACC has " + count1 + " associations in the new release, " + count0
                            + " in the previous release");
                }
            }
        }
        System.out.println("pairs checked: " + checked);
        System.out.println("failures: " + failures.size());
        failures.stream().limit(400).forEach(f -> System.out.println("  " + f));
        assertEquals(List.of(), failures.stream().limit(25).toList(), failures.size() + " of " + pairs.size() + " pairs differ");
    }

    @Test
    void theAsccpAnAsccReferToIsTheOneTheRulesSayInTheNewRelease() {
        findLibrary("connectSpec");
        runAssociations(ASCC);
    }

    @Test
    void theBccpABccReferToIsTheOneTheRulesSayInTheNewRelease() {
        findLibrary("connectSpec");
        runAssociations(BCC);
    }
}
