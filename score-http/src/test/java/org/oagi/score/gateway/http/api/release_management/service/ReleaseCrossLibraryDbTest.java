package org.oagi.score.gateway.http.api.release_management.service;

import org.jooq.Record;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseDependencyValidatorMatrixTest.Location;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseDependencyValidatorMatrixTest.Scenario;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Components of the library that refer to components of another library, which the library depends on through 'release_dep':
 * a release that is the latest published one, a release in the middle of the history, the 'Working' release, and a release
 * that is not depended on at all; the component in the other library is Published, WIP, Draft or Candidate. Against the
 * development database, in a transaction that is rolled back:
 * <pre>
 *   mvn test -Dtest=ReleaseCrossLibraryDbTest -DargLine=-Dscore.dtAwdPri=true
 * </pre>
 * It checks what the validation says about the component that refers, and what the new release refers to.
 */
@EnabledIfSystemProperty(named = "score.dtAwdPri", matches = "true")
class ReleaseCrossLibraryDbTest extends ReleaseScenarioFixture {

    enum Edge {
        ACC_BASE("acc", Kind.ACC), ASCCP_ROLE_OF_ACC("acc", Kind.ASCCP), BCCP_DT("dt", Kind.BCCP), ASCC_ASCCP("asccp", Kind.ACC),
        BCC_BCCP("bccp", Kind.ACC), DT_BASE("dt", Kind.DT), DT_CODE_LIST("code_list", Kind.DT),
        DT_AGENCY_ID_LIST("agency_id_list", Kind.DT);

        /** The table of what is referred to. */
        final String target;
        final Kind dependent;

        Edge(String target, Kind dependent) {
            this.target = target;
            this.dependent = dependent;
        }
    }

    /** Where the component that is referred to is. */
    enum Place {
        LATEST_PUBLISHED, MIDDLE_RELEASE, WORKING_RELEASE, NOT_DEPENDED_ON
    }

    private static final List<CcState> TARGET_STATES = List.of(CcState.Published, CcState.WIP, CcState.Draft, CcState.Candidate);

    /** How a dependent came about: with what it refers to from the previous release on, made to refer to it, or new. */
    enum How {
        NATURAL, RETARGETED, NEW
    }

    record Dependent(How how, Cls cls) {
    }

    private static final List<Dependent> DEPENDENTS = List.of(
            new Dependent(How.NATURAL, new Cls(Variant.P, false)),
            new Dependent(How.NATURAL, new Cls(Variant.R_WIP, false)),
            new Dependent(How.NATURAL, new Cls(Variant.R_CAND, true)),
            new Dependent(How.RETARGETED, new Cls(Variant.R_WIP, false)),
            new Dependent(How.RETARGETED, new Cls(Variant.R_CAND, true)),
            new Dependent(How.NEW, new Cls(Variant.N_WIP, false)),
            new Dependent(How.NEW, new Cls(Variant.N_CAND, true)));

    record Target(Place place, CcState state, long manifest) {
    }

    record Pair(Edge edge, Dependent dependent, Target target, Inst instance, long association) {
    }

    private long connect;
    private final Map<Place, Long> releases = new LinkedHashMap<>();

    private void dependencies() {
        connect = one("select library_id from library where name = 'connectSpec'");
        releases.put(Place.LATEST_PUBLISHED, one("select max(release_id) from `release` where library_id = ? and release_num <> 'Working' "
                + "and state = 'Published'", connect));
        releases.put(Place.MIDDLE_RELEASE, one("select release_id from `release` where library_id = ? and release_num = '10.8'", connect));
        releases.put(Place.WORKING_RELEASE, one("select release_id from `release` where library_id = ? and release_num = 'Working'", connect));
        releases.put(Place.NOT_DEPENDED_ON, one("select release_id from `release` where library_id = ? and release_num = '10.9'", connect));
        for (Place place : List.of(Place.LATEST_PUBLISHED, Place.MIDDLE_RELEASE, Place.WORKING_RELEASE)) {
            dsl.execute("insert into release_dep (release_id, depend_on_release_id) values (?, ?)", working, releases.get(place));
            dsl.execute("insert into release_dep (release_id, depend_on_release_id) values (?, ?)", published, releases.get(place));
        }
    }

    /** A copy of the record, with the given state, as the record of a component that is in that state. */
    private long cloneRecord(String table, long record, CcState state) {
        List<String> columns = dsl.fetch("select column_name from information_schema.columns where table_schema = database() "
                        + "and table_name = ? order by ordinal_position", table).map(r -> r.get(0, String.class));
        List<String> names = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (String c : columns) {
            if (c.equals(table + "_id")) {
                continue;
            }
            names.add("`" + c + "`");
            if (c.equals("guid")) {
                values.add("replace(uuid(), '-', '')");
            } else if (c.equals("state")) {
                values.add("'" + state.name() + "'");
            } else if (c.startsWith("prev_") || c.startsWith("next_") || c.startsWith("replacement_")) {
                values.add("null");
            } else {
                values.add("`" + c + "`");
            }
        }
        dsl.execute("insert into `" + table + "` (" + String.join(", ", names) + ") select " + String.join(", ", values)
                + " from `" + table + "` where `" + table + "_id` = ?", record);
        return one("select last_insert_id()");
    }

    /** A component of the other library, in a release of it, in the state. */
    private Target target(Edge edge, Place place, CcState state) {
        long release = releases.get(place);
        String table = edge.target;
        String manifest = table + "_manifest";
        String id = table + "_manifest_id";
        String record = table + "_id";
        long manifestId;
        if (table.equals("code_list") || table.equals("agency_id_list")) {
            long template = one("select min(`" + record + "`) from `" + manifest + "` where release_id = ?", release);
            long cloned = cloneRecord(table, template, state);
            dsl.execute("insert into `" + manifest + "` (release_id, `" + record + "`) values (?, ?)", release, cloned);
            manifestId = one("select last_insert_id()");
        } else {
            String usedList = String.join(",", used.stream().map(String::valueOf).toList()).replaceAll("^$", "0");
            manifestId = one("select m.`" + id + "` from `" + manifest + "` m join `" + table + "` x using (`" + record + "`) "
                    + "where m.release_id = ? and x.state = 'Published' and m.`" + id + "` not in (" + usedList + ") order by m.`" + id
                    + "` limit 1", release);
            used.add(manifestId);
            if (state != CcState.Published) {
                dsl.execute("update `" + manifest + "` set `" + record + "` = ? where `" + id + "` = ?",
                        cloneRecord(table, one("select `" + record + "` from `" + manifest + "` where `" + id + "` = ?", manifestId), state),
                        manifestId);
            }
        }
        return new Target(place, state, manifestId);
    }

    private static Location location(Place place) {
        return switch (place) {
            case LATEST_PUBLISHED, MIDDLE_RELEASE -> Location.PUBLISHED_RELEASE;
            case WORKING_RELEASE -> Location.RELEASE_IN_WORK;
            case NOT_DEPENDED_ON -> Location.NOWHERE;
        };
    }

    private static Scenario scenario(Dependent d, Target t) {
        return new Scenario(d.cls().variant().state, d.cls().assigned(), location(t.place()), t.state(), false, false);
    }

    private static boolean valid(Dependent d, Target t) {
        return ReleaseDependencyValidatorMatrixTest.expected(scenario(d, t)).stream().noneMatch(m -> m.startsWith("Error"));
    }

    // ---- what refers to what ----

    /** Makes the component refer to the target, in 'Working' or in the previous release. */
    private long wire(Edge edge, long dependent, long target, boolean working) {
        switch (edge) {
            case ACC_BASE -> {
                dsl.execute("update acc_manifest set based_acc_manifest_id = ? where acc_manifest_id = ?", target, dependent);
                dsl.execute("update acc set based_acc_id = ? where acc_id = ?", recordOfAny("acc", target), recordOf(Kind.ACC, dependent));
            }
            case ASCCP_ROLE_OF_ACC -> {
                dsl.execute("update asccp_manifest set role_of_acc_manifest_id = ? where asccp_manifest_id = ?", target, dependent);
                dsl.execute("update asccp set role_of_acc_id = ? where asccp_id = ?", recordOfAny("acc", target), recordOf(Kind.ASCCP, dependent));
            }
            case BCCP_DT -> {
                dsl.execute("update bccp_manifest set bdt_manifest_id = ? where bccp_manifest_id = ?", target, dependent);
                dsl.execute("update bccp set bdt_id = ? where bccp_id = ?", recordOfAny("dt", target), recordOf(Kind.BCCP, dependent));
            }
            case DT_BASE -> {
                dsl.execute("update dt_manifest set based_dt_manifest_id = ? where dt_manifest_id = ?", target, dependent);
                dsl.execute("update dt set based_dt_id = ? where dt_id = ?", recordOfAny("dt", target), recordOf(Kind.DT, dependent));
            }
            case ASCC_ASCCP -> {
                return factory.accCommandRepository(REQUESTER).createAscc(new AccManifestId(n(dependent)),
                        new AsccpManifestId(n(target)), null, null, true).value().longValue();
            }
            case BCC_BCCP -> {
                return factory.accCommandRepository(REQUESTER).createBcc(new AccManifestId(n(dependent)),
                        new BccpManifestId(n(target)), null, null, true).value().longValue();
            }
            case DT_CODE_LIST, DT_AGENCY_ID_LIST -> {
                String column = edge == Edge.DT_CODE_LIST ? "code_list_manifest_id" : "agency_id_list_manifest_id";
                long release = working ? this.working : published;
                long sc = scOf(dependent);
                dsl.execute("insert into dt_awd_pri (release_id, dt_id, cdt_pri_id, `" + column + "`, is_default) values (?, ?, "
                        + "(select cdt_pri_id from cdt_pri where name = 'Token'), ?, 0)", release, recordOf(Kind.DT, dependent), target);
                dsl.execute("insert into dt_sc_awd_pri (release_id, dt_sc_id, cdt_pri_id, `" + column + "`, is_default) values (?, ?, "
                        + "(select cdt_pri_id from cdt_pri where name = 'Token'), ?, 0)", release,
                        one("select dt_sc_id from dt_sc_manifest where dt_sc_manifest_id = ?", sc), target);
            }
        }
        return 0;
    }

    private long recordOfAny(String table, long manifest) {
        return one("select `" + table + "_id` from `" + table + "_manifest` where `" + table + "_manifest_id` = ?", manifest);
    }

    private long scOf(long dtManifest) {
        return one("select min(dt_sc_manifest_id) from dt_sc_manifest where owner_dt_manifest_id = ?", dtManifest);
    }

    private Pair build(Edge edge, Dependent d, Target t) {
        Kind kind = edge.dependent;
        Cls cls = d.cls();
        long id;
        long association = 0;
        if (d.how() == How.NEW) {
            id = make(kind, cls).id();
            association = wire(edge, id, t.manifest(), true);
        } else {
            id = pool(kind);
            boolean natural = d.how() == How.NATURAL;
            boolean association2 = edge == Edge.ASCC_ASCCP || edge == Edge.BCC_BCCP;
            if (natural && !association2) {
                wire(edge, one("select prev_" + kind.id() + " from " + kind.manifest() + " where " + kind.id() + " = ?", id), t.manifest(), false);
                wire(edge, id, t.manifest(), true);
            }
            if (cls.variant().revised()) {
                switch (kind) {
                    case ACC -> factory.accCommandRepository(REQUESTER).revise(new AccManifestId(n(id)));
                    case ASCCP -> factory.asccpCommandRepository(REQUESTER).revise(new AsccpManifestId(n(id)));
                    case BCCP -> factory.bccpCommandRepository(REQUESTER).revise(new BccpManifestId(n(id)));
                    case DT -> factory.dtCommandRepository(REQUESTER).revise(new DtManifestId(n(id)));
                    default -> throw new IllegalStateException();
                }
            }
            if (cls.variant() != Variant.P) {
                setState(kind, id, cls.variant().state);
            }
            if (!natural || association2) {
                association = wire(edge, id, t.manifest(), true);
            }
        }
        return new Pair(edge, d, t, new Inst(kind, id, cls), association);
    }

    private Set<ReleaseValidationResponse.ValidationMessage> messages(ReleaseValidationResponse r, Inst i) {
        Set<ReleaseValidationResponse.ValidationMessage> found = switch (i.kind()) {
            case ACC -> r.getStatusMapForAcc().get(AccManifestId.from(n(i.id())));
            case ASCCP -> r.getStatusMapForAsccp().get(AsccpManifestId.from(n(i.id())));
            case BCCP -> r.getStatusMapForBccp().get(BccpManifestId.from(n(i.id())));
            case DT -> r.getStatusMapForDt().get(DtManifestId.from(n(i.id())));
            default -> null;
        };
        return found == null ? Set.of() : found;
    }

    @ParameterizedTest
    @EnumSource(Edge.class)
    void whatIsReferredToInAnotherLibraryIsValidatedAndKeptAsItIsInTheNewRelease(Edge edge) {
        findLibrary("Ag eStandards / Chem eStandards");
        dependencies();

        // the components of the other library
        List<Pair> pairs = new ArrayList<>();
        for (Place place : Place.values()) {
            for (CcState state : TARGET_STATES) {
                Target t = target(edge, place, state);
                for (Dependent d : DEPENDENTS) {
                    // a Data Type whose allowed primitives are of another library cannot be revised (the allowed primitives of a
                    // Data Type have to point to manifests of its own release), so it is not revised with them
                    boolean allowedPrimitive = edge == Edge.DT_CODE_LIST || edge == Edge.DT_AGENCY_ID_LIST;
                    if (allowedPrimitive && d.how() == How.NATURAL && d.cls().variant().revised()) {
                        continue;
                    }
                    pairs.add(build(edge, d, t));
                }
            }
        }

        Map<Kind, Set<Long>> assigned = new LinkedHashMap<>();
        for (Kind k : Kind.values()) {
            assigned.put(k, new HashSet<>());
        }
        for (Pair p : pairs) {
            if (p.dependent().cls().assigned()) {
                assigned.get(p.instance().kind()).add(p.instance().id());
            }
        }
        long release = newRelease("8.1");
        ReleaseId releaseId = new ReleaseId(n(release));

        // 1. the validation
        ReleaseValidator validator = new ReleaseValidator(REQUESTER, releaseId, factory);
        validator.setAssignedAccComponentManifestIds(assigned.get(Kind.ACC).stream().map(i -> new AccManifestId(n(i))).toList());
        validator.setAssignedAsccpComponentManifestIds(assigned.get(Kind.ASCCP).stream().map(i -> new AsccpManifestId(n(i))).toList());
        validator.setAssignedBccpComponentManifestIds(assigned.get(Kind.BCCP).stream().map(i -> new BccpManifestId(n(i))).toList());
        validator.setAssignedDtComponentManifestIds(assigned.get(Kind.DT).stream().map(i -> new DtManifestId(n(i))).toList());
        validator.setAssignedCodeListComponentManifestIds(List.of());
        validator.setAssignedAgencyIdListComponentManifestIds(List.of());
        ReleaseValidationResponse response = validator.validate();

        List<String> failures = new ArrayList<>();
        Map<String, Integer> outcomes = new TreeMap<>();
        for (Pair p : pairs) {
            Set<String> expected = new TreeSet<>(ReleaseDependencyValidatorMatrixTest.expected(scenario(p.dependent(), p.target())));
            Set<String> actual = new TreeSet<>();
            messages(response, p.instance()).stream()
                    .filter(m -> !m.getMessage().equals("Namespace is required."))
                    // what the component's own dependents say about it is not about what it refers to
                    .filter(m -> !m.getMessage().contains("has been revised but not included in the release assignment")
                            && !m.getMessage().contains("is needed in the release assignment")
                            && !m.getMessage().contains("should be in 'Candidate'"))
                    .forEach(m -> actual.add(m.getLevel() + "@dependent"));
            outcomes.merge(expected.toString(), 1, Integer::sum);
            if (!expected.equals(actual)) {
                failures.add(p.edge() + ": " + p.dependent().how() + " " + p.dependent().cls() + " -> " + p.target().place() + " "
                        + p.target().state() + ": expected " + expected + " but was " + actual + " "
                        + messages(response, p.instance()).stream().map(m -> m.getMessage()).toList());
            }
        }
        System.out.println(edge + ": " + pairs.size() + " dependents, validation outcomes " + outcomes);

        // 2. the new release, of what can be released
        List<Pair> valid = pairs.stream().filter(p -> valid(p.dependent(), p.target())).toList();
        for (Kind k : Kind.values()) {
            if (!assigned.get(k).isEmpty()) {
                dsl.execute("update `" + k.table + "` set state = 'ReleaseDraft' where `" + k.record() + "` in (select `" + k.record()
                        + "` from `" + k.manifest() + "` where `" + k.id() + "` in ("
                        + String.join(",", assigned.get(k).stream().map(String::valueOf).toList()) + "))");
            }
        }
        Map<String, String> others = snapshotLibraries(release);
        factory.ccCommandRepository(REQUESTER).copyWorkingManifests(releaseId, new ReleaseId(n(working)),
                assigned.get(Kind.ACC).stream().map(i -> new AccManifestId(n(i))).toList(),
                assigned.get(Kind.ASCCP).stream().map(i -> new AsccpManifestId(n(i))).toList(),
                assigned.get(Kind.BCCP).stream().map(i -> new BccpManifestId(n(i))).toList(),
                assigned.get(Kind.DT).stream().map(i -> new DtManifestId(n(i))).toList(), List.of(), List.of());
        factory.releaseCommandRepository(REQUESTER).copyDepsFromWorking(releaseId, new ReleaseId(n(working)));

        for (Pair p : valid) {
            String label = edge + ": " + p.dependent().how() + " " + p.dependent().cls() + " -> " + p.target().place() + " " + p.target().state();
            Inst d = p.instance();
            Long d1 = dsl.fetch("select `" + d.kind().id() + "` from `" + d.kind().manifest() + "` where release_id = ? and `next_"
                    + d.kind().id() + "` = ?", release, d.id()).stream().map(r -> r.get(0, Long.class)).findFirst().orElse(null);
            boolean inRelease = d.cls().assigned() || d.cls().variant().released;
            if (!inRelease) {
                if (d1 != null) {
                    failures.add(label + ": a component that is new and not assigned is in the new release");
                }
                continue;
            }
            if (d1 == null) {
                failures.add(label + ": the component is not in the new release");
                continue;
            }
            // does the component of the new release refer to the component of the other library, as it is?
            boolean refers = refersTo(edge, p, d1, release);
            // it does when it is assigned, and when it is as it was in the previous release, where it referred to it
            boolean association = edge == Edge.ASCC_ASCCP || edge == Edge.BCC_BCCP;
            boolean shouldRefer = d.cls().assigned() || (p.dependent().how() == How.NATURAL && !association);
            if (refers != shouldRefer) {
                failures.add(label + ": " + (refers ? "refers to" : "does not refer to") + " the component of the other library but "
                        + (shouldRefer ? "should" : "should not"));
            }
        }

        // 3. what the release depends on is what 'Working' depends on; the other libraries are as they were
        List<Long> depsOfWorking = ids("select depend_on_release_id from release_dep where release_id = ? order by 1", working);
        List<Long> depsOfRelease = ids("select depend_on_release_id from release_dep where release_id = ? order by 1", release);
        if (!depsOfWorking.equals(depsOfRelease)) {
            failures.add("the release depends on " + depsOfRelease + " but 'Working' depends on " + depsOfWorking);
        }
        Map<String, String> after = snapshotLibraries(release);
        List<String> changed = new ArrayList<>();
        others.forEach((t, v) -> {
            if (!v.equals(after.get(t))) {
                changed.add(t);
            }
        });
        if (!changed.isEmpty()) {
            failures.add("the copy changed the manifests of the other library: " + changed);
        }

        System.out.println(edge + ": failures " + failures.size());
        failures.stream().limit(60).forEach(f -> System.out.println("  " + f));
        assertEquals(List.of(), failures.stream().limit(25).toList(), failures.size() + " of " + pairs.size() + " dependents differ");
    }

    /** Whether the component of the new release refers to the target. */
    private boolean refersTo(Edge edge, Pair p, long d1, long release) {
        long target = p.target().manifest();
        switch (edge) {
            case ACC_BASE:
                return one("select count(*) from acc_manifest where acc_manifest_id = ? and based_acc_manifest_id = ?", d1, target) > 0;
            case ASCCP_ROLE_OF_ACC:
                return one("select count(*) from asccp_manifest where asccp_manifest_id = ? and role_of_acc_manifest_id = ?", d1, target) > 0;
            case BCCP_DT:
                return one("select count(*) from bccp_manifest where bccp_manifest_id = ? and bdt_manifest_id = ?", d1, target) > 0;
            case DT_BASE:
                return one("select count(*) from dt_manifest where dt_manifest_id = ? and based_dt_manifest_id = ?", d1, target) > 0;
            case ASCC_ASCCP:
                return one("select count(*) from ascc_manifest where release_id = ? and from_acc_manifest_id = ? and to_asccp_manifest_id = ?",
                        release, d1, target) > 0;
            case BCC_BCCP:
                return one("select count(*) from bcc_manifest where release_id = ? and from_acc_manifest_id = ? and to_bccp_manifest_id = ?",
                        release, d1, target) > 0;
            default:
                String column = edge == Edge.DT_CODE_LIST ? "code_list_manifest_id" : "agency_id_list_manifest_id";
                long dtRecord = recordOf(Kind.DT, d1);
                long scRecord = one("select dt_sc_id from dt_sc_manifest where release_id = ? and owner_dt_manifest_id = ? "
                        + "order by dt_sc_manifest_id limit 1", release, d1);
                boolean dt = one("select count(*) from dt_awd_pri where release_id = ? and dt_id = ? and `" + column + "` = ?",
                        release, dtRecord, target) > 0;
                boolean sc = one("select count(*) from dt_sc_awd_pri where release_id = ? and dt_sc_id = ? and `" + column + "` = ?",
                        release, scRecord, target) > 0;
                assertEquals(dt, sc, "the Data Type and its supplementary component refer to the same");
                return dt;
        }
    }

    /** count and checksum of the manifests of the libraries but the one under test. */
    private Map<String, String> snapshotLibraries(long release) {
        Map<String, String> snapshot = new LinkedHashMap<>();
        for (String kind : List.of("acc", "asccp", "bccp", "ascc", "bcc", "dt", "dt_sc", "code_list", "code_list_value",
                "agency_id_list", "agency_id_list_value", "xbt")) {
            String table = kind + "_manifest";
            StringBuilder row = new StringBuilder("concat_ws('|'");
            dsl.fetch("select column_name from information_schema.columns where table_schema = database() and table_name = ? "
                            + "order by ordinal_position", table)
                    .forEach(c -> row.append(", ifnull(`").append(c.get(0, String.class)).append("`, '<null>')"));
            row.append(")");
            Record r = dsl.fetchOne("select count(*), ifnull(sum(crc32(" + row + ")), 0) from `" + table + "` where release_id not in ("
                    + working + "," + published + "," + release + ")");
            snapshot.put(table, r.get(0) + " rows, checksum " + r.get(1));
        }
        return snapshot;
    }
}
