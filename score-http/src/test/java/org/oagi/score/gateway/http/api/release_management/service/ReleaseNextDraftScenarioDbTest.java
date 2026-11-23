package org.oagi.score.gateway.http.api.release_management.service;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpType;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseDependencyValidatorMatrixTest.Location;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseDependencyValidatorMatrixTest.Scenario;
import org.oagi.score.gateway.http.common.TestDataSource;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The release '8.0' of the library is published; a release '8.1' is then drafted and published after 'Working' has been
 * filled with components of every kind of state: published and untouched, revised, and new, each in WIP, Draft and
 * Candidate, assigned or not, wired to each other along every kind of dependency. Against the development database, in a
 * transaction that is rolled back:
 * <pre>
 *   mvn test -Dtest=ReleaseNextDraftScenarioDbTest -DargLine=-Dscore.dtAwdPri=true
 * </pre>
 * It needs the published release '8.0' of the library (see {@link ReleaseDraftLifecycleDbTest}).
 */
@EnabledIfSystemProperty(named = "score.dtAwdPri", matches = "true")
class ReleaseNextDraftScenarioDbTest extends ReleaseScenarioFixture {

    private static final String LIBRARY = "connectSpec";

    private void findLibrary() {
        findLibrary(LIBRARY);
    }

    // ---- the dependencies ----

    private List<Edge> edges() {
        ReleaseId workingId = new ReleaseId(n(working));
        var acc = factory.accCommandRepository(REQUESTER);
        var bccp = factory.bccpCommandRepository(REQUESTER);
        var dt = factory.dtCommandRepository(REQUESTER);
        return List.of(
                new Edge("ASCCP -> ACC", Kind.ASCCP, Kind.ACC, (d, t) -> dsl.execute(
                        "update asccp_manifest set role_of_acc_manifest_id = ? where asccp_manifest_id = ?", t.id(), d.id())),
                new Edge("ACC -> base ACC", Kind.ACC, Kind.ACC, (d, t) -> {
                    // as updateBasedAccManifestId does, without its check of conflicting associations
                    dsl.execute("update acc_manifest set based_acc_manifest_id = ? where acc_manifest_id = ?", t.id(), d.id());
                    dsl.execute("update acc set based_acc_id = ? where acc_id = ?", recordOf(Kind.ACC, t.id()),
                            recordOf(Kind.ACC, d.id()));
                }),
                new Edge("ACC -> ASCCP (ASCC)", Kind.ACC, Kind.ASCCP, (d, t) ->
                        acc.createAscc(new AccManifestId(n(d.id())), new AsccpManifestId(n(t.id())), null, null, true)),
                new Edge("ACC -> BCCP (BCC)", Kind.ACC, Kind.BCCP, (d, t) ->
                        acc.createBcc(new AccManifestId(n(d.id())), new BccpManifestId(n(t.id())), null, null, true)),
                new Edge("BCCP -> DT", Kind.BCCP, Kind.DT, (d, t) ->
                        bccp.updateDt(new BccpManifestId(n(d.id())), new DtManifestId(n(t.id())))),
                new Edge("DT -> base DT", Kind.DT, Kind.DT, (d, t) -> dsl.execute(
                        "update dt_manifest set based_dt_manifest_id = ? where dt_manifest_id = ?", t.id(), d.id())),
                new Edge("DT, DT_SC -> Code List", Kind.DT, Kind.CODE_LIST, (d, t) -> {
                    long dtId = recordOf(Kind.DT, d.id());
                    dsl.execute("update dt_awd_pri set is_default = 0 where release_id = ? and dt_id = ?", working, dtId);
                    dt.createDtAwdPri(workingId, new DtId(n(dtId)), "Token", null,
                            new CodeListManifestId(n(t.id())), null, true);
                    dsl.execute("insert into dt_sc_awd_pri (release_id, dt_sc_id, cdt_pri_id, code_list_manifest_id, is_default) "
                                    + "select ?, s.dt_sc_id, (select cdt_pri_id from cdt_pri where name = 'Token'), ?, 0 "
                                    + "from dt_sc_manifest s where s.owner_dt_manifest_id = ?",
                            working, t.id(), d.id());
                }));
    }

    private record Model(Map<String, Map<Cls, Inst>> targets, List<List<Object>> pairs, List<Inst> all) {
    }

    /** Fills 'Working'; {@code onlyValid} wires only what can be released. */
    private Model build(boolean onlyValid) {
        Model model = new Model(new LinkedHashMap<>(), new ArrayList<>(), new ArrayList<>());
        for (Edge edge : edges()) {
            Map<Cls, Inst> targets = new LinkedHashMap<>();
            for (Cls t : classes()) {
                Inst target = make(edge.target(), t);
                targets.put(t, target);
                model.all().add(target);
            }
            model.targets().put(edge.name(), targets);
            for (Cls t : classes()) {
                for (Cls d : classes()) {
                    if (onlyValid && !valid(d, t)) {
                        continue;
                    }
                    Inst dependent = make(edge.dependent(), d);
                    model.all().add(dependent);
                    edge.wire().accept(dependent, targets.get(t));
                    model.pairs().add(List.of(edge.name(), dependent, targets.get(t)));
                }
            }
        }
        return model;
    }

    private record Assigned(List<AccManifestId> acc, List<AsccpManifestId> asccp, List<BccpManifestId> bccp,
                            List<DtManifestId> dt, List<CodeListManifestId> codeList) {
    }

    private Assigned assigned(Model model) {
        Assigned a = new Assigned(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>());
        for (Inst i : model.all()) {
            if (i.cls().assigned()) {
                switch (i.kind()) {
                    case ACC -> a.acc().add(new AccManifestId(n(i.id())));
                    case ASCCP -> a.asccp().add(new AsccpManifestId(n(i.id())));
                    case BCCP -> a.bccp().add(new BccpManifestId(n(i.id())));
                    case DT -> a.dt().add(new DtManifestId(n(i.id())));
                    case CODE_LIST -> a.codeList().add(new CodeListManifestId(n(i.id())));
                }
            }
        }
        return a;
    }

    private ReleaseValidationResponse validate(long release, Assigned a) {
        ReleaseValidator validator = new ReleaseValidator(REQUESTER, new ReleaseId(n(release)), factory);
        validator.setAssignedAccComponentManifestIds(a.acc());
        validator.setAssignedAsccpComponentManifestIds(a.asccp());
        validator.setAssignedBccpComponentManifestIds(a.bccp());
        validator.setAssignedDtComponentManifestIds(a.dt());
        validator.setAssignedCodeListComponentManifestIds(a.codeList());
        validator.setAssignedAgencyIdListComponentManifestIds(List.<AgencyIdListManifestId>of());
        return validator.validate();
    }

    private Set<ReleaseValidationResponse.ValidationMessage> messages(ReleaseValidationResponse r, Inst i) {
        Set<ReleaseValidationResponse.ValidationMessage> found = switch (i.kind()) {
            case ACC -> r.getStatusMapForAcc().get(AccManifestId.from(n(i.id())));
            case ASCCP -> r.getStatusMapForAsccp().get(AsccpManifestId.from(n(i.id())));
            case BCCP -> r.getStatusMapForBccp().get(BccpManifestId.from(n(i.id())));
            case DT -> r.getStatusMapForDt().get(DtManifestId.from(n(i.id())));
            case CODE_LIST -> r.getStatusMapForCodeList().get(CodeListManifestId.from(n(i.id())));
        };
        return found == null ? Set.of() : found;
    }

    // ---- 1. what the validation says about every combination ----

    @Test
    void theValidationSaysWhatTheRulesSayForEveryCombinationOfStateAssignmentAndDependency() {
        findLibrary();
        Model model = build(false);
        long release = newRelease("8.1");
        ReleaseValidationResponse response = validate(release, assigned(model));

        List<String> mismatches = new ArrayList<>();
        Map<String, Integer> outcomes = new TreeMap<>();
        int checked = 0;
        for (var edge : model.targets().entrySet()) {
            for (var target : edge.getValue().entrySet()) {
                // what the dependents ask of the dependency: the message appears when one of them is in the release.
                Set<String> expected = new TreeSet<>();
                for (Cls d : classes()) {
                    expected.addAll(ReleaseDependencyValidatorMatrixTest.expected(scenario(d, target.getKey())));
                }
                Set<String> actual = new TreeSet<>();
                messages(response, target.getValue()).stream()
                        .filter(m -> !m.getMessage().equals("Namespace is required."))
                        .forEach(m -> actual.add(m.getLevel() + "@dependency"));
                checked++;
                outcomes.merge(expected.toString(), 1, Integer::sum);
                if (!expected.equals(actual)) {
                    mismatches.add(edge.getKey() + " target " + target.getKey() + ": expected " + expected
                            + " but was " + actual + " " + messages(response, target.getValue()));
                }
            }
        }
        long dependents = model.pairs().size();
        System.out.println("dependent/dependency pairs built: " + dependents + ", dependencies checked: " + checked
                + ", components in 'Working': " + model.all().size() + ", expected outcomes " + outcomes);
        assertEquals(List.of(), mismatches.stream().limit(25).toList(), mismatches.size() + " of " + checked + " differ");
    }

    // ---- 2. the draft and the publication of '8.1' ----

    private static final List<String> MANIFESTS = List.of("acc", "asccp", "bccp", "ascc", "bcc", "dt", "dt_sc", "code_list",
            "code_list_value", "agency_id_list", "agency_id_list_value", "xbt");

    /** count and checksum of the manifests of the releases that are not given. */
    private Map<String, String> snapshotExcept(long... releases) {
        Map<String, String> snapshot = new LinkedHashMap<>();
        for (String kind : MANIFESTS) {
            String table = kind + "_manifest";
            StringBuilder row = new StringBuilder("concat_ws('|'");
            dsl.fetch("select column_name from information_schema.columns where table_schema = database() "
                            + "and table_name = ? order by ordinal_position", table)
                    .forEach(c -> row.append(", ifnull(`").append(c.get(0, String.class)).append("`, '<null>')"));
            row.append(")");
            Record r = dsl.fetchOne("select count(*), ifnull(sum(crc32(" + row + ")), 0) from `" + table
                    + "` where release_id not in (" + String.join(",", java.util.Arrays.stream(releases)
                    .mapToObj(String::valueOf).toList()) + ")");
            snapshot.put(table, r.get(0) + " rows, checksum " + r.get(1));
        }
        return snapshot;
    }

    private static String list(List<? extends Object> ids) {
        return ids.isEmpty() ? "0" : String.join(",", ids.stream().map(i -> {
            try {
                return String.valueOf(i.getClass().getMethod("value").invoke(i));
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }).toList());
    }

    @Test
    void aDraftOfEveryKindOfStateIsValidatedCopiedAndLinkedWhenPublished() {
        findLibrary();
        Model model = build(true);
        long release = newRelease("8.1");
        ReleaseId releaseId = new ReleaseId(n(release));
        ReleaseId workingId = new ReleaseId(n(working));
        Assigned a = assigned(model);
        List<String> report = new ArrayList<>();
        List<String> failures = new ArrayList<>();

        // validation: nothing that stops the release.
        ReleaseValidationResponse response = validate(release, a);
        Map<String, Integer> problems = new TreeMap<>();
        List.of(response.getStatusMapForAcc(), response.getStatusMapForAsccp(), response.getStatusMapForBccp(),
                        response.getStatusMapForDt(), response.getStatusMapForCodeList())
                .forEach(map -> map.values().forEach(set -> set.forEach(m ->
                        problems.merge(m.getLevel() + ": " + m.getMessage().replaceAll("'[^']*'", "'..'"), 1, Integer::sum))));
        report.add("validation: succeed=" + response.isSucceed() + " " + problems);
        assertTrue(response.isSucceed(), "the validation of the assignment: " + problems);

        Map<String, String> others = snapshotExcept(working, published, release);

        // the assigned components become 'ReleaseDraft', the manifests are copied.
        String assignedIds = "acc:" + list(a.acc()) + " asccp:" + list(a.asccp()) + " bccp:" + list(a.bccp())
                + " dt:" + list(a.dt()) + " code_list:" + list(a.codeList());
        report.add("assigned " + assignedIds.replaceAll("[0-9,]{40,}", "..."));
        Map<Kind, String> assignedSql = new LinkedHashMap<>();
        assignedSql.put(Kind.ACC, list(a.acc()));
        assignedSql.put(Kind.ASCCP, list(a.asccp()));
        assignedSql.put(Kind.BCCP, list(a.bccp()));
        assignedSql.put(Kind.DT, list(a.dt()));
        assignedSql.put(Kind.CODE_LIST, list(a.codeList()));
        for (Kind k : Kind.values()) {
            dsl.execute("update `" + k.table + "` set state = 'ReleaseDraft' where `" + k.record() + "` in "
                    + "(select `" + k.record() + "` from `" + k.manifest() + "` where `" + k.id() + "` in ("
                    + assignedSql.get(k) + "))");
        }
        factory.ccCommandRepository(REQUESTER).copyWorkingManifests(releaseId, workingId, a.acc(), a.asccp(), a.bccp(),
                a.dt(), a.codeList(), List.of());
        factory.releaseCommandRepository(REQUESTER).copyDepsFromWorking(releaseId, workingId);

        // what is copied: the assigned components and those that have been released, and their record.
        for (Kind k : Kind.values()) {
            String assignedList = assignedSql.get(k);
            long expected = one("select count(*) from `" + k.manifest() + "` where release_id = ? and (`prev_" + k.id()
                    + "` is not null or `" + k.id() + "` in (" + assignedList + "))", working);
            long copied = one("select count(*) from `" + k.manifest() + "` where release_id = ?", release);
            long wrongRecord = one("select count(*) from `" + k.manifest() + "` n join `" + k.manifest() + "` w on w.`"
                    + k.id() + "` = n.`next_" + k.id() + "` left join `" + k.manifest() + "` p on p.`" + k.id()
                    + "` = n.`prev_" + k.id() + "` where n.release_id = ? and ("
                    // an assigned component keeps its record of 'Working'; the others keep the record of the release before
                    + "(w.`" + k.id() + "` in (" + assignedList + ") and n.`" + k.record() + "` <> w.`" + k.record() + "`) or "
                    + "(w.`" + k.id() + "` not in (" + assignedList + ") and p.`" + k.id() + "` is not null and n.`"
                    + k.record() + "` <> p.`" + k.record() + "`))", release);
            report.add(String.format("%-10s in 'Working' %4d, to copy %4d, copied %4d, with the wrong record %d", k,
                    one("select count(*) from `" + k.manifest() + "` where release_id = ?", working), expected, copied, wrongRecord));
            if (expected != copied) {
                failures.add(k + ": " + expected + " manifests to copy but " + copied + " copied");
            }
            if (wrongRecord != 0) {
                failures.add(k + ": " + wrongRecord + " copied manifests hold the wrong record");
            }
        }

        // every manifest a row of the new release points to is in the new release or a release the library depends on.
        String allowed = release + "," + String.join(",", ids("select depend_on_release_id from release_dep where release_id = ?",
                working).stream().map(String::valueOf).toList());
        for (Record fk : dsl.fetch("select k.table_name, k.column_name, k.referenced_table_name "
                + "from information_schema.key_column_usage k "
                + "join information_schema.columns c on c.table_schema = k.table_schema and c.table_name = k.table_name "
                + "and c.column_name = 'release_id' "
                + "where k.table_schema = database() and k.referenced_table_name like '%\\_manifest' "
                + "and k.column_name not like 'next\\_%' and k.column_name not like 'prev\\_%' "
                + "and k.column_name not like 'replacement\\_%' and k.table_name not like 'module%' "
                + "and k.table_name not like 'github%' order by 1, 2")) {
            String table = fk.get(0, String.class);
            String column = fk.get(1, String.class);
            String referenced = fk.get(2, String.class);
            long outside = one("select count(*) from `" + table + "` t join `" + referenced + "` r on r.`" + referenced
                    + "_id` = t.`" + column + "` where t.release_id = ? and r.release_id not in (" + allowed + ")", release);
            if (outside != 0) {
                Map<Long, String> names = new LinkedHashMap<>();
                model.pairs().forEach(p -> {
                    Inst dep = (Inst) p.get(1);
                    Inst tgt = (Inst) p.get(2);
                    names.put(dep.id(), p.get(0) + ": " + dep.cls() + " -> " + tgt.cls());
                });
                List<String> who = new ArrayList<>();
                for (Record r : dsl.fetch("select t.release_id, t.`next_" + table + "_id`, r.release_id from `" + table
                        + "` t join `" + referenced + "` r on r.`" + referenced + "_id` = t.`" + column
                        + "` where t.release_id = ? and r.release_id not in (" + allowed + ") limit 12", release)) {
                    who.add(String.valueOf(names.get(r.get(1, Long.class))) + " (references release " + r.get(2) + ")");
                }
                failures.add(table + "." + column + ": " + outside + " references point outside the new release: " + who);
            }
        }

        // publishing.
        for (Kind k : Kind.values()) {
            dsl.execute("update `" + k.table + "` set state = 'Published' where state = 'ReleaseDraft' and `" + k.record()
                    + "` in (select `" + k.record() + "` from `" + k.manifest() + "` where release_id = " + working + ")");
        }
        factory.ccCommandRepository(REQUESTER).cleanUp(releaseId);
        factory.releaseCommandRepository(REQUESTER).updateState(releaseId, ReleaseState.Published);

        for (String kind : MANIFESTS) {
            String table = kind + "_manifest";
            String id = table + "_id";
            String prev = "prev_" + table + "_id";
            String next = "next_" + table + "_id";
            String all = working + "," + published + "," + release;

            long inWorking = one("select count(*) from `" + table + "` where release_id = ?", working);
            long in80 = one("select count(*) from `" + table + "` where release_id = ?", published);
            long in81 = one("select count(*) from `" + table + "` where release_id = ?", release);
            // the links are symmetric.
            long asymmetric = one("select count(*) from `" + table + "` a join `" + table + "` b on b.`" + id + "` = a.`" + next
                    + "` where a.release_id in (" + all + ") and b.`" + prev + "` <> a.`" + id + "`")
                    + one("select count(*) from `" + table + "` a join `" + table + "` b on b.`" + id + "` = a.`" + prev
                    + "` where a.release_id in (" + all + ") and b.`" + next + "` <> a.`" + id + "`");
            // 'Working' goes back to the last published release, and only the manifests that have been released do.
            long workingNotBackToLast = one("select count(*) from `" + table + "` w where w.release_id = ? and w.`" + prev
                    + "` is not null and not exists (select 1 from `" + table + "` p where p.`" + id + "` = w.`" + prev
                    + "` and p.release_id = ?)", working, release);
            long workingWithoutPrevButLinked = one("select count(*) from `" + table + "` w where w.release_id = ? and w.`"
                    + prev + "` is null and exists (select 1 from `" + table + "` p where p.`" + next + "` = w.`" + id
                    + "` and p.release_id in (" + published + "," + release + "))", working);
            // 8.1 goes on to 'Working', 8.0 goes on to 8.1.
            long lastNotOnToWorking = one("select count(*) from `" + table + "` p where p.release_id = ? and not exists "
                    + "(select 1 from `" + table + "` w where w.`" + id + "` = p.`" + next + "` and w.release_id = ?)",
                    release, working);
            long firstNotOnToNext = one("select count(*) from `" + table + "` p where p.release_id = ? and not exists "
                    + "(select 1 from `" + table + "` w where w.`" + id + "` = p.`" + next + "` and w.release_id = ?)",
                    published, release);
            long nextRelease = one("select count(*) from `" + table + "` p where p.release_id = ? and p.`" + prev + "` is not null "
                    + "and not exists (select 1 from `" + table + "` w where w.`" + id + "` = p.`" + prev + "` and w.release_id = ?)",
                    release, published);
            report.add(String.format("%-30s 8.0 %5d, 8.1 %5d, working %5d | asymmetric %d | working not back to 8.1: %d, "
                            + "no prev but linked: %d | 8.1 not on to working: %d | 8.0 not on to 8.1: %d | 8.1 prev not 8.0: %d",
                    table, in80, in81, inWorking, asymmetric, workingNotBackToLast, workingWithoutPrevButLinked,
                    lastNotOnToWorking, firstNotOnToNext, nextRelease));
            if (asymmetric + workingNotBackToLast + workingWithoutPrevButLinked + lastNotOnToWorking + firstNotOnToNext
                    + nextRelease != 0) {
                failures.add(table + ": the previous/next links are not what they should be");
            }
        }

        Map<String, String> after = snapshotExcept(working, published, release);
        List<String> changed = new ArrayList<>();
        others.forEach((t, v) -> {
            if (!v.equals(after.get(t))) {
                changed.add(t + ": " + v + " -> " + after.get(t));
            }
        });
        report.add("manifests of the other libraries changed: " + changed);
        if (!changed.isEmpty()) {
            failures.add("the other libraries changed: " + changed);
        }

        report.forEach(System.out::println);
        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }

    // ---- 3. an association removed from a revised ACC ----

    /** Revises two ACCs of 'Working' and removes an ASCC and a BCC from each; only one of them is assigned. */
    @Test
    void anAssociationRemovedFromARevisedAccIsNotLostFromTheReleaseWhenTheAccIsNotAssigned() {
        findLibrary();
        var acc = factory.accCommandRepository(REQUESTER);
        List<String> report = new ArrayList<>();
        Map<String, Long> accs = new LinkedHashMap<>();
        Map<String, long[]> removed = new LinkedHashMap<>();
        for (String name : List.of("unassigned", "assigned")) {
            long id = one("select m.acc_manifest_id from acc_manifest m join acc a using (acc_id) where m.release_id = ? "
                    + "and a.state = 'Published' and m.prev_acc_manifest_id is not null and m.acc_manifest_id not in ("
                    + String.join(",", used.stream().map(String::valueOf).toList()).replaceAll("^$", "0") + ") "
                    + "and (select count(*) from ascc_manifest c where c.from_acc_manifest_id = m.acc_manifest_id) >= 2 "
                    + "and (select count(*) from bcc_manifest c where c.from_acc_manifest_id = m.acc_manifest_id) >= 2 "
                    + "order by m.acc_manifest_id limit 1", working);
            used.add(id);
            acc.revise(new AccManifestId(n(id)));
            long ascc = one("select min(ascc_manifest_id) from ascc_manifest where from_acc_manifest_id = ?", id);
            long bcc = one("select min(bcc_manifest_id) from bcc_manifest where from_acc_manifest_id = ?", id);
            acc.delete(new org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId(n(ascc)));
            acc.delete(new org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId(n(bcc)));
            setState(Kind.ACC, id, name.equals("assigned") ? CcState.Candidate : CcState.WIP);
            accs.put(name, id);
            removed.put(name, new long[]{ascc, bcc});
        }
        long assignedId = accs.get("assigned");

        long release = newRelease("8.1");
        ReleaseId releaseId = new ReleaseId(n(release));
        ReleaseId workingId = new ReleaseId(n(working));
        dsl.execute("update acc set state = 'ReleaseDraft' where acc_id = (select acc_id from acc_manifest where acc_manifest_id = ?)",
                assignedId);
        factory.ccCommandRepository(REQUESTER).copyWorkingManifests(releaseId, workingId,
                List.of(new AccManifestId(n(assignedId))), List.of(), List.of(), List.of(), List.of(), List.of());

        for (var e : accs.entrySet()) {
            long id = e.getValue();
            long in80 = one("select count(*) from ascc_manifest c join acc_manifest a on a.acc_manifest_id = c.from_acc_manifest_id "
                    + "where a.release_id = ? and a.acc_manifest_id = (select prev_acc_manifest_id from acc_manifest where acc_manifest_id = ?)",
                    published, id);
            long bcc80 = one("select count(*) from bcc_manifest c join acc_manifest a on a.acc_manifest_id = c.from_acc_manifest_id "
                    + "where a.release_id = ? and a.acc_manifest_id = (select prev_acc_manifest_id from acc_manifest where acc_manifest_id = ?)",
                    published, id);
            long inWorking = one("select count(*) from ascc_manifest where from_acc_manifest_id = ?", id);
            long bccWorking = one("select count(*) from bcc_manifest where from_acc_manifest_id = ?", id);
            long in81 = one("select count(*) from ascc_manifest c where c.release_id = ? and c.from_acc_manifest_id = "
                    + "(select acc_manifest_id from acc_manifest where release_id = ? and next_acc_manifest_id = ?)",
                    release, release, id);
            long bcc81 = one("select count(*) from bcc_manifest c where c.release_id = ? and c.from_acc_manifest_id = "
                    + "(select acc_manifest_id from acc_manifest where release_id = ? and next_acc_manifest_id = ?)",
                    release, release, id);
            report.add(e.getKey() + " ACC: ASCC 8.0 " + in80 + ", Working " + inWorking + ", 8.1 " + in81
                    + " | BCC 8.0 " + bcc80 + ", Working " + bccWorking + ", 8.1 " + bcc81);
            long expectedAscc = e.getKey().equals("assigned") ? inWorking : in80;
            long expectedBcc = e.getKey().equals("assigned") ? bccWorking : bcc80;
            if (e.getKey().equals("unassigned")) {
                long m1 = one("select acc_manifest_id from acc_manifest where release_id = ? and next_acc_manifest_id = ?", release, id);
                long m0 = one("select prev_acc_manifest_id from acc_manifest where acc_manifest_id = ?", id);
                // the order of the associations of the ACC in 8.1 is the one of 8.0
                assertEquals(sequence(m0, false), sequence(m1, true), "the order of the associations of the ACC in 8.1");
            }
            assertEquals(expectedAscc, in81, e.getKey() + " ACC: the ASCCs of the ACC in 8.1");
            assertEquals(expectedBcc, bcc81, e.getKey() + " ACC: the BCCs of the ACC in 8.1");
        }
        report.forEach(System.out::println);
    }

    /** The associations of an ACC in the order of its sequence, as the manifests of the previous release they follow. */
    private List<Long> sequence(long accManifest, boolean viaPrev) {
        List<Record> keys = dsl.fetch("select k.seq_key_id, k.prev_seq_key_id, k.next_seq_key_id, "
                + "coalesce(" + (viaPrev ? "a.prev_ascc_manifest_id, b.prev_bcc_manifest_id" : "a.ascc_manifest_id, b.bcc_manifest_id")
                + ") as association from seq_key k left join ascc_manifest a on a.ascc_manifest_id = k.ascc_manifest_id "
                + "left join bcc_manifest b on b.bcc_manifest_id = k.bcc_manifest_id where k.from_acc_manifest_id = ?", accManifest);
        Map<Long, Record> byId = new LinkedHashMap<>();
        keys.forEach(k -> byId.put(k.get(0, Long.class), k));
        List<Long> heads = keys.stream().filter(k -> k.get(1) == null).map(k -> k.get(0, Long.class)).toList();
        assertEquals(1, heads.size(), "one first association of the ACC " + accManifest);
        List<Long> order = new ArrayList<>();
        Long current = heads.get(0);
        while (current != null) {
            Record k = byId.get(current);
            order.add(k.get("association", Long.class));
            current = k.get(2, Long.class);
        }
        assertEquals(keys.size(), order.size(), "the sequence of the ACC " + accManifest + " is one chain");
        return order;
    }

    // ---- 4. a base that a Data Type got after its previous release ----

    /**
     * A Data Type of 8.0 that had no base; it is revised in 'Working', which gives it a base that is new and not assigned.
     * The Data Type is not assigned, so 8.1 has it as it was in 8.0: without a base.
     */
    @Test
    void aDataTypeNotAssignedGoesBackToTheBaseItHadInThePreviousRelease() {
        findLibrary();
        long id = pool(Kind.DT);
        long prev = one("select prev_dt_manifest_id from dt_manifest where dt_manifest_id = ?", id);
        // as if it had no base in 8.0
        dsl.execute("update dt set based_dt_id = null where dt_id in (select dt_id from dt_manifest where dt_manifest_id in (?, ?))", id, prev);
        dsl.execute("update dt_manifest set based_dt_manifest_id = null where dt_manifest_id in (?, ?)", id, prev);
        factory.dtCommandRepository(REQUESTER).revise(new DtManifestId(n(id)));
        setState(Kind.DT, id, CcState.WIP);
        Inst base = make(Kind.DT, new Cls(Variant.N_WIP, false));
        dsl.execute("update dt_manifest set based_dt_manifest_id = ? where dt_manifest_id = ?", base.id(), id);
        dsl.execute("update dt set based_dt_id = ? where dt_id = ?", recordOf(Kind.DT, base.id()), recordOf(Kind.DT, id));

        long release = newRelease("8.1");
        factory.ccCommandRepository(REQUESTER).copyWorkingManifests(new ReleaseId(n(release)), new ReleaseId(n(working)),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

        Long based = dsl.fetchOne("select based_dt_manifest_id from dt_manifest where release_id = ? and next_dt_manifest_id = ?",
                release, id).get(0, Long.class);
        assertEquals(null, based, "the base of the Data Type in 8.1, as it was in 8.0");
    }
}
