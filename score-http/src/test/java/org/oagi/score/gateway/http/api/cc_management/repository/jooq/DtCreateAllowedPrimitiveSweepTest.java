package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

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
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.TestDataSource;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every Data Type of the Data Type Catalogue, ISO 15000-5 and connectSpec, derived in a library that depends on it, in a
 * transaction that is rolled back:
 * <pre>
 *   mvn test -Dtest=DtCreateAllowedPrimitiveSweepTest -DargLine=-Dscore.dtAwdPri=true
 * </pre>
 * For the allowed primitives of the Data Type and of its supplementary components that use an XBT: the XBT is the manifest of
 * the new release when the new release has the same XBT (the primitives of XML Schema, which every library has), and the
 * manifest of the base, unchanged, when only the library it depends on has it (a custom XBT). Nothing is dropped or added.
 */
@EnabledIfSystemProperty(named = "score.dtAwdPri", matches = "true")
class DtCreateAllowedPrimitiveSweepTest {

    private static final ScoreUser REQUESTER = new ScoreUser(new UserId(BigInteger.ZERO), "sysadm", "sysadm",
            "sysadm@example.com", true, List.of(ScoreRole.ADMINISTRATOR));

    private static TestDataSource dataSource;
    private Connection connection;
    private DSLContext dsl;
    private RepositoryFactory factory;

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
        // the test does not read what the change log is for, and writing it for every row costs time
        factory.recordChangeLogRepository().stopWriting();
    }

    @AfterEach
    void rollback() throws SQLException {
        connection.rollback();
        factory.recordChangeLogRepository().resumeWriting();
        connection.close();
    }

    private long release(String library, String releaseNum) {
        return dsl.fetchOne("select r.release_id from `release` r join library l using (library_id) "
                + "where l.name = ? and r.release_num = ?", library, releaseNum).get(0, Long.class);
    }

    /** A library that depends on the release, with the XML Schema primitives of its own, as every library has. */
    private long libraryDependingOn(long... dependencies) {
        long user = dsl.fetchOne("select min(app_user_id) from app_user").get(0, Long.class);
        dsl.execute("insert into library (name, type, state, is_read_only, is_default, created_by, last_updated_by, "
                + "creation_timestamp, last_update_timestamp) values ('sweep', 'Standard', 'Published', 0, 0, ?, ?, now(6), now(6))",
                user, user);
        long library = dsl.fetchOne("select last_insert_id()").get(0, Long.class);
        dsl.execute("insert into `release` (library_id, guid, release_num, created_by, last_updated_by, creation_timestamp, "
                + "last_update_timestamp, state) values (?, replace(uuid(), '-', ''), 'Working', ?, ?, now(6), now(6), 'Published')",
                library, user, user);
        long release = dsl.fetchOne("select last_insert_id()").get(0, Long.class);
        for (long dependency : dependencies) {
            dsl.execute("insert into release_dep (release_id, depend_on_release_id) values (?, ?)", release, dependency);
        }
        dsl.execute("insert into xbt_manifest (release_id, xbt_id) select ?, xbt_id from xbt_manifest where release_id = ?",
                release, release("CCTS Data Type Catalogue v3", "3.1"));
        return release;
    }

    private long create(long releaseId, long basedDtManifestId) {
        return factory.dtCommandRepository(REQUESTER).create(
                new ReleaseId(BigInteger.valueOf(releaseId)),
                new DtManifestId(BigInteger.valueOf(basedDtManifestId))).value().longValue();
    }

    private record Group(long manifest, String what) {
    }

    private Map<Group, Long> groups(String sql, Object... bindings) {
        Map<Group, Long> groups = new HashMap<>();
        for (Record r : dsl.fetch(sql, bindings)) {
            groups.put(new Group(r.get(0, Long.class), r.get(1) + "/" + r.get(2) + "/" + r.get(3)), r.get(4, Long.class));
        }
        return groups;
    }

    /** What an allowed primitive of a base has to become in the target: the target's own XBT when it has the same, else the base's. */
    private static String expected(String allowedPrimitive, String manifest, String record, String of, String key) {
        return "select " + key + " w, p.cdt_pri_id, p.is_default, case when p.xbt_manifest_id is null then 'other' "
                + "when exists (select 1 from xbt_manifest n where n.release_id = ? and n.xbt_id = x.xbt_id) "
                + "then concat('release x', x.xbt_id) else concat('manifest ', x.xbt_manifest_id) end k, count(*) n "
                + "from " + allowedPrimitive + " p join " + manifest + " m on m." + record + " = p." + record
                + " and m.release_id = p.release_id join sweep_pair pr on " + of
                + " left join xbt_manifest x on x.xbt_manifest_id = p.xbt_manifest_id group by 1, 2, 3, 4";
    }

    private static String actual(String allowedPrimitive, String manifest, String record, String of, String key) {
        return "select " + key + " w, p.cdt_pri_id, p.is_default, case when p.xbt_manifest_id is null then 'other' "
                + "when x.release_id = ? then concat('release x', x.xbt_id) else concat('manifest ', x.xbt_manifest_id) end k, "
                + "count(*) n from " + allowedPrimitive + " p join " + manifest + " m on m." + record + " = p." + record
                + " and m.release_id = p.release_id join sweep_pair pr on " + of
                + " left join xbt_manifest x on x.xbt_manifest_id = p.xbt_manifest_id group by 1, 2, 3, 4";
    }

    /**
     * Derives every Data Type of the source release in the target release and checks the allowed primitives, all at once in the
     * database. @return one line of the report, and the problems found.
     */
    private String sweep(String name, long source, long target, List<String> problems) {
        List<long[]> pairs = new ArrayList<>();
        for (long base : dsl.fetch("select dt_manifest_id from dt_manifest where release_id = ? order by 1", source)
                .map(r -> r.get(0, Long.class))) {
            try {
                pairs.add(new long[]{base, create(target, base)});
            } catch (IllegalArgumentException e) {
                problems.add(name + ": base DT manifest " + base + " could not be derived: " + e.getMessage());
            }
        }
        dsl.execute("create temporary table sweep_pair (base bigint unsigned not null, created bigint unsigned not null, "
                + "primary key (base), unique key (created))");
        dsl.batch(pairs.stream().map(p -> dsl.query("insert into sweep_pair (base, created) values (?, ?)", p[0], p[1])).toList())
                .execute();

        long dtRows = 0, dtOwn = 0, dtKept = 0, scRows = 0, scOwn = 0, scKept = 0;
        long scs = dsl.fetchOne("select count(*) from dt_sc_manifest s join sweep_pair pr on pr.base = s.owner_dt_manifest_id")
                .get(0, Long.class);
        String[][] kinds = {
                {"DT", "dt_awd_pri", "dt_manifest", "dt_id", "pr.base = m.dt_manifest_id", "pr.base", "pr.created = m.dt_manifest_id"},
                {"DT_SC", "dt_sc_awd_pri", "dt_sc_manifest", "dt_sc_id", "pr.base = m.owner_dt_manifest_id",
                        "m.dt_sc_manifest_id", "pr.created = m.owner_dt_manifest_id"}};
        for (String[] k : kinds) {
            boolean sc = k[0].equals("DT_SC");
            Map<Group, Long> expected = groups(expected(k[1], k[2], k[3], k[4], k[5]), target);
            Map<Group, Long> actual = groups(actual(k[1], k[2], k[3], k[6], sc ? "m.based_dt_sc_manifest_id" : "pr.base"), target);
            long rows = 0, own = 0, kept = 0;
            for (Map.Entry<Group, Long> e : expected.entrySet()) {
                if (!e.getKey().what().endsWith("/other")) {
                    rows += e.getValue();
                }
            }
            for (Map.Entry<Group, Long> e : actual.entrySet()) {
                if (e.getKey().what().contains("/release x")) {
                    own += e.getValue();
                } else if (e.getKey().what().contains("/manifest ")) {
                    kept += e.getValue();
                }
            }
            if (sc) {
                scRows = rows;
                scOwn = own;
                scKept = kept;
            } else {
                dtRows = rows;
                dtOwn = own;
                dtKept = kept;
            }
            Map<Long, List<String>> differences = new TreeMap<>();
            for (Group g : expected.keySet()) {
                if (!expected.get(g).equals(actual.get(g))) {
                    differences.computeIfAbsent(g.manifest(), x -> new ArrayList<>())
                            .add("expected " + g.what() + " x" + expected.get(g) + ", found x" + actual.get(g));
                }
            }
            for (Group g : actual.keySet()) {
                if (!expected.containsKey(g)) {
                    differences.computeIfAbsent(g.manifest(), x -> new ArrayList<>())
                            .add("expected nothing of " + g.what() + ", found x" + actual.get(g));
                }
            }
            differences.forEach((manifest, what) -> problems.add(name + " " + k[0] + " " + manifest + ": " + what));

            // independent of the rule: a primitive of XML Schema is the new release's, a custom XBT is the base's
            for (Record r : dsl.fetch("select pr.created, b.builtin_type, x.release_id, count(*) from " + k[1] + " p join " + k[2]
                    + " m on m." + k[3] + " = p." + k[3] + " and m.release_id = p.release_id join sweep_pair pr on " + k[6]
                    + " join xbt_manifest x on x.xbt_manifest_id = p.xbt_manifest_id join xbt b on b.xbt_id = x.xbt_id "
                    + "where (b.builtin_type like 'xsd:%') <> (x.release_id = ?) group by 1, 2, 3", target)) {
                problems.add(name + " " + k[0] + " " + r.get(0) + ": " + r.get(3) + " XBT " + r.get(1) + " of release " + r.get(2)
                        + ", the new release is " + target);
            }
        }
        dsl.execute("drop temporary table sweep_pair");
        return String.format("%-44s DT %3d: %5d xbt rows, %5d of the new release, %4d of the base | "
                        + "DT_SC %4d: %5d xbt rows, %5d of the new release, %4d of the base",
                name, pairs.size(), dtRows, dtOwn, dtKept, scs, scRows, scOwn, scKept);
    }

    @Test
    void everyDataTypeOfTheCatalogueIsDerivedInTheLibrariesThatDependOnIt() {
        long catalogue = release("CCTS Data Type Catalogue v3", "3.1");
        List<String> problems = new ArrayList<>();
        List<String> report = new ArrayList<>();
        report.add(sweep("Catalogue 3.1 -> ADAPT Standard", catalogue, release("ADAPT Standard", "Working"), problems));
        report.add(sweep("Catalogue 3.1 -> connectSpec", catalogue, release("connectSpec", "Working"), problems));
        report.forEach(System.out::println);
        problems.stream().limit(30).forEach(System.out::println);
        assertTrue(problems.isEmpty(), problems.size() + " problems, the first: " + problems.stream().limit(5).toList());
    }

    @Test
    void everyDataTypeOfIso15000IsDerivedInConnectSpec() {
        List<String> problems = new ArrayList<>();
        System.out.println(sweep("ISO 15000-5 2014 -> connectSpec", release("ISO 15000-5", "2014"),
                release("connectSpec", "Working"), problems));
        problems.stream().limit(30).forEach(System.out::println);
        assertTrue(problems.isEmpty(), problems.size() + " problems, the first: " + problems.stream().limit(5).toList());
    }

    @Test
    void everyDataTypeOfConnectSpecIsDerivedInALibraryThatDependsOnIt() {
        long connectSpec = release("connectSpec", "10.13.3");
        long target = libraryDependingOn(connectSpec, release("CCTS Data Type Catalogue v3", "3.1"));
        List<String> problems = new ArrayList<>();
        System.out.println(sweep("connectSpec 10.13.3 -> a library on it", connectSpec, target, problems));
        problems.stream().limit(30).forEach(System.out::println);
        assertTrue(problems.isEmpty(), problems.size() + " problems, the first: " + problems.stream().limit(5).toList());
    }
}
