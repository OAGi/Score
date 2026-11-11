package org.oagi.score.gateway.http.api.cc_management.service;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.common.TestDataSource;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * What the tests of revising ('Working' branch, developers) and amending (a release that is not 'Working', end-users) a
 * component, and of cancelling that, share. They run the real {@link CcCommandService} against the development database,
 * in a transaction that is rolled back:
 * <pre>
 *   mvn test -Drevision=3.5.0 -Dtest=CcReviseHistoryDbTest -DargLine=-Dscore.reviseCancel=true
 * </pre>
 * The components are those of the library 'Ag eStandards / Chem eStandards': the 'Working' release for a developer, and the
 * release '8.0' for an end-user, whose components are made {@code Production} and owned by an end-user first.
 */
@EnabledIfSystemProperty(named = "score.reviseCancel", matches = "true")
abstract class CcReviseCancelFixture {

    static final String LIBRARY = "Ag eStandards / Chem eStandards";

    enum Branch {DEVELOPER, END_USER}

    /** A kind of component and the table of its records and of its manifests. */
    enum Kind {
        ACC("acc"), ASCC("ascc"), BCC("bcc"), ASCCP("asccp"), BCCP("bccp"), DT("dt"), DT_SC("dt_sc");

        final String table;

        Kind(String table) {
            this.table = table;
        }

        String pk() {
            return table + "_id";
        }

        String manifest() {
            return table + "_manifest";
        }

        String mpk() {
            return table + "_manifest_id";
        }

        String prev() {
            return "prev_" + pk();
        }

        String next() {
            return "next_" + pk();
        }

        boolean hasState() {
            return this != DT_SC;
        }
    }

    static TestDataSource dataSource;
    Connection connection;
    DSLContext dsl;
    RepositoryFactory factory;
    CcCommandService service;

    @BeforeAll
    static void open() {
        dataSource = TestDataSource.open(2);
    }

    @AfterAll
    static void close() {
        dataSource.close();
    }

    @BeforeEach
    void begin() throws SQLException {
        connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        dsl = DSL.using(connection, SQLDialect.MARIADB);
        factory = new RepositoryFactory(dsl);
        service = new CcCommandService();
        ReflectionTestUtils.setField(service, "repositoryFactory", factory);
        ReflectionTestUtils.setField(service, "stateChangeEventPublisher",
                mock(ComponentStateChangeEventPublisher.class));
    }

    @AfterEach
    void finish() throws SQLException {
        if (Boolean.getBoolean("score.keep")) {
            connection.commit();
        } else {
            connection.rollback();
        }
        connection.close();
    }

    // ---- plain SQL helpers ----

    long one(String sql, Object... bindings) {
        return dsl.fetchOne(sql, bindings).get(0, Long.class);
    }

    List<Long> ids(String sql, Object... bindings) {
        return dsl.fetch(sql, bindings).map(r -> r.get(0, Long.class));
    }

    static BigInteger n(long v) {
        return BigInteger.valueOf(v);
    }

    // ---- the branch ----

    Branch branch;
    ScoreUser requester;
    long library;
    long release;
    /** The release of the branch, and the releases it depends on: what a snapshot covers. */
    String releases;
    final Set<Long> used = new HashSet<>();

    String publishedState() {
        return branch == Branch.DEVELOPER ? "Published" : "Production";
    }

    void useBranch(Branch branch) {
        this.branch = branch;
        library = one("select library_id from library where name = ?", LIBRARY);
        if (branch == Branch.DEVELOPER) {
            release = one("select release_id from `release` where library_id = ? and release_num = 'Working'", library);
            requester = new ScoreUser(new UserId(BigInteger.ZERO), "sysadm", "sysadm", "sysadm@example.com", true,
                    List.of(ScoreRole.DEVELOPER, ScoreRole.ADMINISTRATOR));
        } else {
            release = one("select release_id from `release` where library_id = ? and release_num = '8.0'", library);
            dsl.execute("insert into app_user (login_id, name, is_developer, is_admin, is_enabled, email_verified) "
                    + "values ('cc_eu_test', 'CC end-user', 0, 0, 1, 0)");
            long endUserId = one("select last_insert_id()");
            requester = new ScoreUser(new UserId(n(endUserId)), "cc_eu_test", "CC end-user", null, false,
                    List.of(ScoreRole.END_USER));
            for (Kind kind : Kind.values()) {
                if (kind.hasState()) {
                    dsl.execute("update `" + kind.table + "` set state = 'Production', owner_user_id = ? where state = 'Published' "
                            + "and `" + kind.pk() + "` in (select `" + kind.pk() + "` from `" + kind.manifest()
                            + "` where release_id = ?)", endUserId, release);
                }
            }
        }
        List<Long> scope = new ArrayList<>(List.of(release));
        scope.addAll(ids("select depend_on_release_id from release_dep where release_id = ?", release));
        releases = "(" + scope.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
    }

    /** The first manifest of the release that the condition accepts and no test step used yet. */
    long pick(Kind kind, String why, String condition) {
        String notUsed = used.isEmpty() ? "" : " and m.`" + kind.mpk() + "` not in ("
                + used.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
        String state = kind.hasState() ? " and x.state = '" + publishedState() + "' and x.`" + kind.next() + "` is null" : "";
        List<Long> found = ids("select m.`" + kind.mpk() + "` from `" + kind.manifest() + "` m join `" + kind.table
                + "` x on x.`" + kind.pk() + "` = m.`" + kind.pk() + "` where m.release_id = ?" + state
                + " and (" + condition + ")" + notUsed + " order by m.`" + kind.mpk() + "` limit 1", release);
        assertTrue(!found.isEmpty(), "no " + kind + " in the " + branch + " branch that is " + why);
        used.add(found.get(0));
        return found.get(0);
    }

    long recordOf(Kind kind, long manifestId) {
        return one("select `" + kind.pk() + "` from `" + kind.manifest() + "` where `" + kind.mpk() + "` = ?", manifestId);
    }

    /** Makes {@code derived} an ACC with the base {@code base}, as a user does in the editor. */
    void deriveAcc(long derived, long base) {
        long baseRecord = recordOf(Kind.ACC, base);
        long derivedRecord = recordOf(Kind.ACC, derived);
        dsl.execute("update acc_manifest set based_acc_manifest_id = ? where acc_manifest_id = ?", base, derived);
        dsl.execute("update acc set based_acc_id = ? where acc_id = ?", baseRecord, derivedRecord);
    }

    // ---- snapshots ----

    /** The rows a snapshot covers: every revision of the components that the releases hold. */
    private String scope(Kind kind) {
        String m = "select `" + kind.pk() + "` from `" + kind.manifest() + "` where release_id in " + releases;
        return "guid in (select guid from `" + kind.table + "` where `" + kind.pk() + "` in (" + m + "))";
    }

    private Map<String, String> scopes() {
        Map<String, String> scopes = new LinkedHashMap<>();
        for (Kind kind : Kind.values()) {
            scopes.put(kind.table, scope(kind));
            scopes.put(kind.manifest(), "release_id in " + releases);
        }
        scopes.put("seq_key", "from_acc_manifest_id in (select acc_manifest_id from acc_manifest where release_id in " + releases + ")");
        scopes.put("dt_awd_pri", "release_id in " + releases);
        scopes.put("dt_sc_awd_pri", "release_id in " + releases);
        scopes.put("log", "reference in (" + String.join(" union ", List.of(
                "select guid from acc where " + scope(Kind.ACC), "select guid from asccp where " + scope(Kind.ASCCP),
                "select guid from bccp where " + scope(Kind.BCCP), "select guid from dt where " + scope(Kind.DT))) + ")");
        return scopes;
    }

    private static final List<String> COUNTED = List.of("acc", "ascc", "bcc", "asccp", "bccp", "dt", "dt_sc", "seq_key",
            "dt_awd_pri", "dt_sc_awd_pri", "log");

    private final Map<String, String> selects = new LinkedHashMap<>();

    /** Every column; a text is its md5, which is enough to tell that it changed. */
    private String columns(String table) {
        return selects.computeIfAbsent(table, t -> dsl.fetch("select column_name, data_type from information_schema.columns "
                        + "where table_schema = database() and table_name = ? order by ordinal_position", t).stream()
                .map(r -> {
                    String c = r.get(0, String.class);
                    String type = r.get(1, String.class).toLowerCase();
                    boolean text = type.contains("text") || type.contains("blob") || type.contains("json");
                    return text ? "md5(`" + c + "`) as `" + c + "`" : "`" + c + "`";
                }).collect(Collectors.joining(", ")));
    }

    private static String pk(String table) {
        return table + "_id";
    }

    static final class Snapshot {
        final Map<String, Map<Object, Map<String, Object>>> rows = new LinkedHashMap<>();
        final Map<String, Long> counts = new LinkedHashMap<>();

        Map<String, Object> row(String table, Object pk) {
            Map<String, Object> row = rows.get(table).get(pk);
            return row;
        }

        Map<String, Object> row(Kind kind, long pk) {
            return row(kind.table, BigInteger.valueOf(pk));
        }

        Object get(Kind kind, long pk, String column) {
            Map<String, Object> row = row(kind, pk);
            return row == null ? null : row.get(column);
        }

        Map<String, Object> manifest(Kind kind, long pk) {
            return row(kind.manifest(), BigInteger.valueOf(pk));
        }
    }

    Snapshot snapshot() {
        Snapshot snapshot = new Snapshot();
        for (var scope : scopes().entrySet()) {
            String table = scope.getKey();
            String pk = pk(table);
            Map<Object, Map<String, Object>> rows = new LinkedHashMap<>();
            for (Record r : dsl.fetch("select " + columns(table) + " from `" + table + "` where " + scope.getValue())) {
                rows.put(new BigInteger(String.valueOf(r.get(pk))), r.intoMap());
            }
            snapshot.rows.put(table, rows);
        }
        for (String table : COUNTED) {
            snapshot.counts.put(table, one("select count(*) from `" + table + "`"));
        }
        return snapshot;
    }

    record Change(String table, Object pk, char kind, Set<String> columns) {
        @Override
        public String toString() {
            return kind + " " + table + "#" + pk + (columns.isEmpty() ? "" : " " + columns);
        }
    }

    static List<Change> diff(Snapshot before, Snapshot after) {
        List<Change> changes = new ArrayList<>();
        for (var table : before.rows.entrySet()) {
            Map<Object, Map<String, Object>> b = table.getValue();
            Map<Object, Map<String, Object>> a = after.rows.get(table.getKey());
            for (var row : b.entrySet()) {
                Map<String, Object> other = a.get(row.getKey());
                if (other == null) {
                    changes.add(new Change(table.getKey(), row.getKey(), '-', Set.of()));
                    continue;
                }
                Set<String> columns = new TreeSet<>();
                for (var column : row.getValue().entrySet()) {
                    if (!Objects.equals(column.getValue(), other.get(column.getKey()))) {
                        columns.add(column.getKey());
                    }
                }
                if (!columns.isEmpty()) {
                    changes.add(new Change(table.getKey(), row.getKey(), '~', columns));
                }
            }
            for (Object key : a.keySet()) {
                if (!b.containsKey(key)) {
                    changes.add(new Change(table.getKey(), key, '+', Set.of()));
                }
            }
        }
        return changes;
    }

    /** What a step may change; anything else it changes is reported. */
    static final class Expect {
        private final Map<String, Map<Object, Set<String>>> changed = new LinkedHashMap<>();
        private final Map<String, Set<String>> changedAny = new LinkedHashMap<>();
        private final Map<String, Integer> added = new LinkedHashMap<>();
        private final Map<String, Integer> removed = new LinkedHashMap<>();
        private final Set<String> free = new HashSet<>();

        /** The tables of which the rows that are added do not matter, since what adds them adds a varying number. */
        Expect ignoreAdded(String... tables) {
            free.addAll(List.of(tables));
            return this;
        }

        boolean isFree(String table) {
            return free.contains(table);
        }

        int net(String table) {
            return added.getOrDefault(table, 0) - removed.getOrDefault(table, 0);
        }

        Expect change(String table, long pk, String... columns) {
            changed.computeIfAbsent(table, t -> new LinkedHashMap<>())
                    .computeIfAbsent(BigInteger.valueOf(pk), k -> new LinkedHashSet<>()).addAll(List.of(columns));
            return this;
        }

        Expect change(Kind kind, long pk, String... columns) {
            return change(kind.table, pk, columns);
        }

        Expect changeManifest(Kind kind, long pk, String... columns) {
            return change(kind.manifest(), pk, columns);
        }

        Expect changeAny(String table, String... columns) {
            changedAny.computeIfAbsent(table, t -> new LinkedHashSet<>()).addAll(List.of(columns));
            return this;
        }

        Expect added(String table, int count) {
            added.merge(table, count, Integer::sum);
            return this;
        }

        Expect removed(String table, int count) {
            removed.merge(table, count, Integer::sum);
            return this;
        }

        List<String> unexplained(List<Change> diff) {
            List<String> problems = new ArrayList<>();
            Map<String, Integer> plus = new LinkedHashMap<>();
            Map<String, Integer> minus = new LinkedHashMap<>();
            for (Change c : diff) {
                switch (c.kind()) {
                    case '+' -> plus.merge(c.table(), 1, Integer::sum);
                    case '-' -> minus.merge(c.table(), 1, Integer::sum);
                    default -> {
                        Set<String> allowed = new HashSet<>(changedAny.getOrDefault(c.table(), Set.of()));
                        allowed.addAll(changed.getOrDefault(c.table(), Map.of()).getOrDefault(c.pk(), Set.of()));
                        Set<String> unexpected = new TreeSet<>(c.columns());
                        unexpected.removeAll(allowed);
                        if (!unexpected.isEmpty()) {
                            problems.add("unexpected change " + c.table() + "#" + c.pk() + " " + unexpected);
                        }
                    }
                }
            }
            free.forEach(plus::remove);
            free.forEach(minus::remove);
            compare("added", added, plus, problems);
            compare("removed", removed, minus, problems);
            return problems;
        }

        private static void compare(String what, Map<String, Integer> expected, Map<String, Integer> actual, List<String> problems) {
            Set<String> tables = new TreeSet<>(expected.keySet());
            tables.addAll(actual.keySet());
            for (String table : tables) {
                int e = expected.getOrDefault(table, 0);
                int a = actual.getOrDefault(table, 0);
                if (e != a) {
                    problems.add(what + " " + table + ": expected " + e + ", got " + a);
                }
            }
        }
    }

    // ---- the invariants the records of a release must keep ----

    /**
     * What must hold for the records of the release at all times, as the number of rows that violate it. 'chain' means a
     * reference to a record of the same component (any revision); 'exact' means to the very record the manifest holds,
     * which a revision does not keep for what refers to a revised component.
     */
    Map<String, Long> invariants() {
        long t = release;
        Map<String, Long> v = new LinkedHashMap<>();
        // the associations belong to the ACC, and refer to the property, the manifest holds
        v.put("ascc.from_acc exact", one("select count(*) from ascc_manifest m join ascc c on c.ascc_id = m.ascc_id "
                + "join acc_manifest f on f.acc_manifest_id = m.from_acc_manifest_id where m.release_id = ? and c.from_acc_id <> f.acc_id", t));
        v.put("ascc.to_asccp chain", one("select count(*) from ascc_manifest m join ascc c on c.ascc_id = m.ascc_id "
                + "join asccp cp on cp.asccp_id = c.to_asccp_id join asccp_manifest tm on tm.asccp_manifest_id = m.to_asccp_manifest_id "
                + "join asccp tp on tp.asccp_id = tm.asccp_id where m.release_id = ? and cp.guid <> tp.guid", t));
        v.put("ascc.to_asccp exact", one("select count(*) from ascc_manifest m join ascc c on c.ascc_id = m.ascc_id "
                + "join asccp_manifest tm on tm.asccp_manifest_id = m.to_asccp_manifest_id where m.release_id = ? and c.to_asccp_id <> tm.asccp_id", t));
        v.put("bcc.from_acc exact", one("select count(*) from bcc_manifest m join bcc c on c.bcc_id = m.bcc_id "
                + "join acc_manifest f on f.acc_manifest_id = m.from_acc_manifest_id where m.release_id = ? and c.from_acc_id <> f.acc_id", t));
        v.put("bcc.to_bccp chain", one("select count(*) from bcc_manifest m join bcc c on c.bcc_id = m.bcc_id "
                + "join bccp cp on cp.bccp_id = c.to_bccp_id join bccp_manifest tm on tm.bccp_manifest_id = m.to_bccp_manifest_id "
                + "join bccp tp on tp.bccp_id = tm.bccp_id where m.release_id = ? and cp.guid <> tp.guid", t));
        v.put("bcc.to_bccp exact", one("select count(*) from bcc_manifest m join bcc c on c.bcc_id = m.bcc_id "
                + "join bccp_manifest tm on tm.bccp_manifest_id = m.to_bccp_manifest_id where m.release_id = ? and c.to_bccp_id <> tm.bccp_id", t));
        // what ACC, ASCCP, BCCP, DT refer to
        v.put("acc.based chain", one("select count(*) from acc_manifest m join acc a on a.acc_id = m.acc_id "
                + "join acc ba on ba.acc_id = a.based_acc_id join acc_manifest bm on bm.acc_manifest_id = m.based_acc_manifest_id "
                + "join acc bt on bt.acc_id = bm.acc_id where m.release_id = ? and ba.guid <> bt.guid", t));
        v.put("acc.based exact", one("select count(*) from acc_manifest m join acc a on a.acc_id = m.acc_id "
                + "join acc_manifest bm on bm.acc_manifest_id = m.based_acc_manifest_id where m.release_id = ? and a.based_acc_id <> bm.acc_id", t));
        v.put("acc.based missing", one("select count(*) from acc_manifest m join acc a on a.acc_id = m.acc_id "
                + "where m.release_id = ? and ((a.based_acc_id is null) <> (m.based_acc_manifest_id is null))", t));
        v.put("asccp.role_of chain", one("select count(*) from asccp_manifest m join asccp a on a.asccp_id = m.asccp_id "
                + "join acc ra on ra.acc_id = a.role_of_acc_id join acc_manifest rm on rm.acc_manifest_id = m.role_of_acc_manifest_id "
                + "join acc rt on rt.acc_id = rm.acc_id where m.release_id = ? and ra.guid <> rt.guid", t));
        v.put("asccp.role_of exact", one("select count(*) from asccp_manifest m join asccp a on a.asccp_id = m.asccp_id "
                + "join acc_manifest rm on rm.acc_manifest_id = m.role_of_acc_manifest_id where m.release_id = ? and a.role_of_acc_id <> rm.acc_id", t));
        v.put("bccp.bdt chain", one("select count(*) from bccp_manifest m join bccp a on a.bccp_id = m.bccp_id "
                + "join dt bd on bd.dt_id = a.bdt_id join dt_manifest bm on bm.dt_manifest_id = m.bdt_manifest_id "
                + "join dt bt on bt.dt_id = bm.dt_id where m.release_id = ? and bd.guid <> bt.guid", t));
        v.put("bccp.bdt exact", one("select count(*) from bccp_manifest m join bccp a on a.bccp_id = m.bccp_id "
                + "join dt_manifest bm on bm.dt_manifest_id = m.bdt_manifest_id where m.release_id = ? and a.bdt_id <> bm.dt_id", t));
        v.put("dt.based chain", one("select count(*) from dt_manifest m join dt a on a.dt_id = m.dt_id "
                + "join dt ba on ba.dt_id = a.based_dt_id join dt_manifest bm on bm.dt_manifest_id = m.based_dt_manifest_id "
                + "join dt bt on bt.dt_id = bm.dt_id where m.release_id = ? and ba.guid <> bt.guid", t));
        v.put("dt.based exact", one("select count(*) from dt_manifest m join dt a on a.dt_id = m.dt_id "
                + "join dt_manifest bm on bm.dt_manifest_id = m.based_dt_manifest_id where m.release_id = ? and a.based_dt_id <> bm.dt_id", t));
        v.put("dt_sc.owner exact", one("select count(*) from dt_sc_manifest m join dt_sc s on s.dt_sc_id = m.dt_sc_id "
                + "join dt_manifest o on o.dt_manifest_id = m.owner_dt_manifest_id where m.release_id = ? and s.owner_dt_id <> o.dt_id", t));
        v.put("dt_sc.based chain", one("select count(*) from dt_sc_manifest m join dt_sc s on s.dt_sc_id = m.dt_sc_id "
                + "join dt_sc bs on bs.dt_sc_id = s.based_dt_sc_id join dt_sc_manifest bm on bm.dt_sc_manifest_id = m.based_dt_sc_manifest_id "
                + "join dt_sc bt on bt.dt_sc_id = bm.dt_sc_id where m.release_id = ? and bs.guid <> bt.guid", t));
        v.put("dt_sc.based exact", one("select count(*) from dt_sc_manifest m join dt_sc s on s.dt_sc_id = m.dt_sc_id "
                + "join dt_sc_manifest bm on bm.dt_sc_manifest_id = m.based_dt_sc_manifest_id where m.release_id = ? and s.based_dt_sc_id <> bm.dt_sc_id", t));
        // what refers to an older record than the manifest does is marked as in conflict
        v.put("ascc.to_asccp lags without the marker", one("select count(*) from ascc_manifest m join ascc c on c.ascc_id = m.ascc_id "
                + "join asccp_manifest tm on tm.asccp_manifest_id = m.to_asccp_manifest_id where m.release_id = ? and c.to_asccp_id <> tm.asccp_id and m.conflict = 0", t));
        v.put("bcc.to_bccp lags without the marker", one("select count(*) from bcc_manifest m join bcc c on c.bcc_id = m.bcc_id "
                + "join bccp_manifest tm on tm.bccp_manifest_id = m.to_bccp_manifest_id where m.release_id = ? and c.to_bccp_id <> tm.bccp_id and m.conflict = 0", t));
        v.put("acc.based lags without the marker", one("select count(*) from acc_manifest m join acc a on a.acc_id = m.acc_id "
                + "join acc_manifest bm on bm.acc_manifest_id = m.based_acc_manifest_id where m.release_id = ? and a.based_acc_id <> bm.acc_id and m.conflict = 0", t));
        v.put("asccp.role_of lags without the marker", one("select count(*) from asccp_manifest m join asccp a on a.asccp_id = m.asccp_id "
                + "join acc_manifest rm on rm.acc_manifest_id = m.role_of_acc_manifest_id where m.release_id = ? and a.role_of_acc_id <> rm.acc_id and m.conflict = 0", t));
        v.put("bccp.bdt lags without the marker", one("select count(*) from bccp_manifest m join bccp a on a.bccp_id = m.bccp_id "
                + "join dt_manifest bm on bm.dt_manifest_id = m.bdt_manifest_id where m.release_id = ? and a.bdt_id <> bm.dt_id and m.conflict = 0", t));
        v.put("dt.based lags without the marker", one("select count(*) from dt_manifest m join dt a on a.dt_id = m.dt_id "
                + "join dt_manifest bm on bm.dt_manifest_id = m.based_dt_manifest_id where m.release_id = ? and a.based_dt_id <> bm.dt_id and m.conflict = 0", t));
        v.put("dt_sc.based lags without the marker", one("select count(*) from dt_sc_manifest m join dt_sc a on a.dt_sc_id = m.dt_sc_id "
                + "join dt_sc_manifest bm on bm.dt_sc_manifest_id = m.based_dt_sc_manifest_id where m.release_id = ? and a.based_dt_sc_id <> bm.dt_sc_id and m.conflict = 0", t));
        // what is marked as in conflict while it refers to the latest record is a marker that is left, and not counted
        // allowed primitives belong to the record of the Data Type (and of the supplementary component) the manifest holds
        v.put("dt_awd_pri of a DT that no manifest holds and is not replaced", one("select count(*) from dt_awd_pri p "
                + "join dt d on d.dt_id = p.dt_id where p.release_id = ? and d.next_dt_id is null and not exists "
                + "(select 1 from dt_manifest m where m.release_id = p.release_id and m.dt_id = p.dt_id)", t));
        v.put("dt without dt_awd_pri", one("select count(*) from dt_manifest m where m.release_id = ? and not exists "
                + "(select 1 from dt_awd_pri p where p.release_id = m.release_id and p.dt_id = m.dt_id)", t));
        v.put("dt_sc_awd_pri of a dt_sc that no manifest holds and is not replaced", one("select count(*) from dt_sc_awd_pri p "
                + "join dt_sc s on s.dt_sc_id = p.dt_sc_id where p.release_id = ? and s.next_dt_sc_id is null and not exists "
                + "(select 1 from dt_sc_manifest m where m.release_id = p.release_id and m.dt_sc_id = p.dt_sc_id)", t));
        v.put("dt_sc without dt_sc_awd_pri", one("select count(*) from dt_sc_manifest m where m.release_id = ? and not exists "
                + "(select 1 from dt_sc_awd_pri p where p.release_id = m.release_id and p.dt_sc_id = m.dt_sc_id)", t));
        v.put("dt_awd_pri to a manifest of another release", one("select count(*) from dt_awd_pri p "
                + "left join xbt_manifest x on x.xbt_manifest_id = p.xbt_manifest_id "
                + "left join code_list_manifest c on c.code_list_manifest_id = p.code_list_manifest_id "
                + "left join agency_id_list_manifest g on g.agency_id_list_manifest_id = p.agency_id_list_manifest_id "
                + "where p.release_id = ? and (x.release_id <> p.release_id or c.release_id <> p.release_id or g.release_id <> p.release_id)", t));
        // the history of the records: a revision is linked both ways, and a record has one next revision
        for (Kind kind : Kind.values()) {
            String tb = "`" + kind.table + "`";
            v.put(kind.table + " next without prev", one("select count(*) from " + tb + " a left join " + tb + " b on b.`" + kind.pk()
                    + "` = a.`" + kind.next() + "` where a.`" + kind.next() + "` is not null and (b.`" + kind.pk() + "` is null or b.`"
                    + kind.prev() + "` is null or b.`" + kind.prev() + "` <> a.`" + kind.pk() + "`)"));
            v.put(kind.table + " prev without next", one("select count(*) from " + tb + " a left join " + tb + " b on b.`" + kind.pk()
                    + "` = a.`" + kind.prev() + "` where a.`" + kind.prev() + "` is not null and (b.`" + kind.pk() + "` is null or b.`"
                    + kind.next() + "` is null or b.`" + kind.next() + "` <> a.`" + kind.pk() + "`)"));
            v.put(kind.table + " revisions with the same prev", one("select count(*) from (select `" + kind.prev() + "` from "
                    + tb + " where `" + kind.prev() + "` is not null group by `" + kind.prev() + "` having count(*) > 1) d"));
        }
        // the manifests of the release are linked to those of the previous release, and back
        for (Kind kind : Kind.values()) {
            String m = "`" + kind.manifest() + "`";
            String id = "`" + kind.mpk() + "`";
            String prev = "`prev_" + kind.mpk() + "`";
            String next = "`next_" + kind.mpk() + "`";
            v.put(kind.manifest() + " prev that does not link back", one("select count(*) from " + m + " m join " + m + " p on p." + id
                    + " = m." + prev + " where m.release_id = ? and not (p." + next + " <=> m." + id + ")", t));
            v.put(kind.manifest() + " next that does not link back", one("select count(*) from " + m + " m join " + m + " n on n." + id
                    + " = m." + next + " where m.release_id = ? and not (n." + prev + " <=> m." + id + ")", t));
        }
        // a manifest of the release that holds a record that is a revision still in work is the leaf of its history
        return v;
    }

    /** The invariants that fail now and did not before, as problems. */
    static List<String> worsened(Map<String, Long> before, Map<String, Long> after) {
        List<String> problems = new ArrayList<>();
        for (var e : after.entrySet()) {
            long was = before.getOrDefault(e.getKey(), 0L);
            if (e.getValue() != was) {
                problems.add("invariant '" + e.getKey() + "': " + was + " rows violated before, " + e.getValue() + " after");
            }
        }
        return problems;
    }

    // ---- reporting ----

    final List<String> problems = new ArrayList<>();

    void check(boolean ok, String message) {
        if (!ok) {
            problems.add(message);
        }
    }

    void checkEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            problems.add(message + ": expected " + expected + ", got " + actual);
        }
    }

    void report() {
        if (!problems.isEmpty()) {
            throw new AssertionError(problems.size() + " problem(s) [" + branch + "]:\n  " + String.join("\n  ", problems));
        }
    }
}
