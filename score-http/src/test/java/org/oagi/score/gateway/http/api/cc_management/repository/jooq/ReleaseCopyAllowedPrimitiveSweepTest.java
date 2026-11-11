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
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
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
 * The 'Working' release of connectSpec (and of a library that depends on it) copied to a Draft release, in a transaction that
 * is rolled back:
 * <pre>
 *   mvn test -Dtest=ReleaseCopyAllowedPrimitiveSweepTest -DargLine=-Dscore.dtAwdPri=true
 * </pre>
 * For every allowed primitive of a Data Type and of its supplementary components that uses an XBT, the copy points to the XBT
 * of the new release when 'Working' points to one of its own, or when the new release has the same XBT as the release the
 * allowed primitive points to (the primitives of XML Schema, which every library has); and to the XBT of that release, as it
 * is, when only the library it depends on has it (a custom XBT). Nothing is dropped or added.
 */
@EnabledIfSystemProperty(named = "score.dtAwdPri", matches = "true")
class ReleaseCopyAllowedPrimitiveSweepTest {

    private static final ScoreUser REQUESTER = new ScoreUser(new UserId(BigInteger.ZERO), "sysadm", "sysadm",
            "sysadm@example.com", true, List.of(ScoreRole.DEVELOPER, ScoreRole.ADMINISTRATOR));

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

    private long newRelease(long libraryId, String releaseNum, String state, long working) {
        long user = dsl.fetchOne("select min(app_user_id) from app_user").get(0, Long.class);
        dsl.execute("insert into `release` (library_id, guid, release_num, created_by, last_updated_by, "
                + "creation_timestamp, last_update_timestamp, state) values (?, replace(uuid(), '-', ''), ?, ?, ?, "
                + "now(6), now(6), ?)", libraryId, releaseNum, user, user, state);
        long release = dsl.fetchOne("select last_insert_id()").get(0, Long.class);
        dsl.execute("insert into release_dep (release_id, depend_on_release_id) select ?, depend_on_release_id "
                + "from release_dep where release_id = ?", release, working);
        return release;
    }

    /** A library that depends on the releases, with the Working release of its own that has the primitives of XML Schema. */
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

    private void deriveAll(long source, long target) {
        for (Record r : dsl.fetch("select dt_manifest_id from dt_manifest where release_id = ? order by 1", source)) {
            factory.dtCommandRepository(REQUESTER).create(new ReleaseId(BigInteger.valueOf(target)),
                    new DtManifestId(BigInteger.valueOf(r.get(0, Long.class))));
        }
    }

    private long libraryOf(long release) {
        return dsl.fetchOne("select library_id from `release` where release_id = ?", release).get(0, Long.class);
    }

    /** The tables of a Data Type and of a supplementary component. */
    private enum Kind {
        DT("dt_awd_pri", "dt_manifest", "dt_id", "dt_manifest_id", "next_dt_manifest_id"),
        DT_SC("dt_sc_awd_pri", "dt_sc_manifest", "dt_sc_id", "dt_sc_manifest_id", "next_dt_sc_manifest_id");

        final String allowedPrimitive;
        final String manifest;
        final String record;
        final String manifestId;
        final String next;

        Kind(String allowedPrimitive, String manifest, String record, String manifestId, String next) {
            this.allowedPrimitive = allowedPrimitive;
            this.manifest = manifest;
            this.record = record;
            this.manifestId = manifestId;
            this.next = next;
        }
    }

    /**
     * What each allowed primitive of 'Working' has to become: the XBT of the new release when 'Working' points to one of its own
     * or when the new release has the same XBT, else the XBT as it is; the other allowed primitives (Code List, Agency ID List)
     * only by number. Grouped by the manifest of 'Working' and what the allowed primitive is.
     */
    private String expected(Kind k) {
        return "select wm." + k.manifestId + " w, p.cdt_pri_id, p.is_default, "
                + "case when p.xbt_manifest_id is null then 'other' "
                + "when x.release_id = ? or exists (select 1 from xbt_manifest n where n.release_id = ? and n.xbt_id = x.xbt_id) "
                + "then concat('release x', x.xbt_id) else concat('kept ', x.xbt_manifest_id) end k, count(*) n "
                + "from " + k.allowedPrimitive + " p join " + k.manifest + " wm on wm." + k.record + " = p." + k.record
                + " and wm.release_id = p.release_id join " + k.manifest + " c on c." + k.next + " = wm." + k.manifestId
                + " and c.release_id = ? left join xbt_manifest x on x.xbt_manifest_id = p.xbt_manifest_id "
                + "where p.release_id = ? group by 1, 2, 3, 4";
    }

    /** What the copy has, grouped the same way. */
    private String actual(Kind k) {
        return "select c." + k.next + " w, p.cdt_pri_id, p.is_default, "
                + "case when p.xbt_manifest_id is null then 'other' when x.release_id = ? then concat('release x', x.xbt_id) "
                + "else concat('kept ', x.xbt_manifest_id) end k, count(*) n "
                + "from " + k.allowedPrimitive + " p join " + k.manifest + " c on c." + k.record + " = p." + k.record
                + " and c.release_id = p.release_id left join xbt_manifest x on x.xbt_manifest_id = p.xbt_manifest_id "
                + "where p.release_id = ? group by 1, 2, 3, 4";
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

    /** Copies 'Working' to a Draft release and checks it; @return one line of the report. */
    private String sweep(String name, long working, List<String> problems) {
        long release = newRelease(libraryOf(working), "sweep-draft", "Draft", working);
        List<Long> dts = dsl.fetch("select dt_manifest_id from dt_manifest where release_id = ? order by 1", working)
                .map(r -> r.get(0, Long.class));
        factory.ccCommandRepository(REQUESTER).copyWorkingManifests(new ReleaseId(BigInteger.valueOf(release)),
                new ReleaseId(BigInteger.valueOf(working)),
                List.<AccManifestId>of(), List.<AsccpManifestId>of(), List.<BccpManifestId>of(),
                dts.stream().map(id -> new DtManifestId(BigInteger.valueOf(id))).toList(),
                List.<CodeListManifestId>of(), List.<AgencyIdListManifestId>of());

        long[] total = new long[8]; // dt: rows, new, kept; sc: rows, new, kept; dts, scs
        for (Kind k : Kind.values()) {
            int at = k == Kind.DT ? 0 : 3;
            for (Record r : dsl.fetch("select wm." + k.manifestId + " from " + k.manifest + " wm where wm.release_id = ? "
                    + "and not exists (select 1 from " + k.manifest + " c where c." + k.next + " = wm." + k.manifestId
                    + " and c.release_id = ?)", working, release)) {
                problems.add(name + ": " + k + " manifest " + r.get(0) + " has no copy");
            }
            total[6 + (k == Kind.DT ? 0 : 1)] = dsl.fetchOne("select count(*) from " + k.manifest + " wm where wm.release_id = ? "
                    + "and exists (select 1 from " + k.manifest + " c where c." + k.next + " = wm." + k.manifestId
                    + " and c.release_id = ?)", working, release).get(0, Long.class);

            Map<Group, Long> expected = groups(expected(k), working, release, release, working);
            Map<Group, Long> actual = groups(actual(k), release, release);
            expected.forEach((g, n) -> {
                if (!g.what().endsWith("/other")) {
                    total[at] += n;
                }
            });
            actual.forEach((g, n) -> {
                if (g.what().contains("/release x")) {
                    total[at + 1] += n;
                } else if (g.what().contains("/kept ")) {
                    total[at + 2] += n;
                }
            });
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
            differences.forEach((manifest, what) -> problems.add(name + " " + k + " " + manifest + ": " + what));
        }
        return String.format("%-46s DT %3d: %5d xbt rows, %5d of the new release, %5d of the dependency | "
                        + "DT_SC %4d: %5d xbt rows, %5d of the new release, %4d of the dependency",
                name, total[6], total[0], total[1], total[2], total[7], total[3], total[4], total[5]);
    }

    private void report(String line, List<String> problems) {
        System.out.println(line);
        problems.stream().limit(30).forEach(System.out::println);
        assertTrue(problems.isEmpty(), problems.size() + " problems, the first: " + problems.stream().limit(5).toList());
    }

    @Test
    void theWorkingReleaseOfConnectSpecIsCopied() {
        List<String> problems = new ArrayList<>();
        report(sweep("connectSpec Working", release("connectSpec", "Working"), problems), problems);
    }

    @Test
    void theWorkingReleaseOfConnectSpecWithDataTypesDerivedFromTheCatalogueAndIsoIsCopied() {
        long working = release("connectSpec", "Working");
        deriveAll(release("CCTS Data Type Catalogue v3", "3.1"), working);
        deriveAll(release("ISO 15000-5", "2014"), working);
        List<String> problems = new ArrayList<>();
        report(sweep("connectSpec Working + derived from CCTS and ISO", working, problems), problems);
    }

    @Test
    void theWorkingReleaseOfALibraryThatDerivesConnectSpecIsCopied() {
        long connectSpec = release("connectSpec", "10.13.3");
        long working = libraryDependingOn(connectSpec, release("CCTS Data Type Catalogue v3", "3.1"));
        deriveAll(connectSpec, working);
        List<String> problems = new ArrayList<>();
        report(sweep("a library deriving connectSpec 10.13.3", working, problems), problems);
    }
}
