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
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.TestDataSource;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The life of a draft release of a library that depends on another library, on the development database, in a transaction
 * that is rolled back: every component of the 'Working' release becomes {@code Candidate}, a release is created, every
 * component is assigned to it, its manifests are copied, and then the release goes back to 'Initialized'.
 * <pre>
 *   mvn test -Dtest=ReleaseDraftLifecycleDbTest -DargLine=-Dscore.dtAwdPri=true
 * </pre>
 */
@EnabledIfSystemProperty(named = "score.dtAwdPri", matches = "true")
class ReleaseDraftLifecycleDbTest {

    private static final ScoreUser REQUESTER = new ScoreUser(new UserId(BigInteger.ZERO), "sysadm", "sysadm",
            "sysadm@example.com", true, List.of(ScoreRole.ADMINISTRATOR));

    private static final String LIBRARY = "connectSpec";

    /** The tables that hold the components of a library, with the column of the record the manifest holds. */
    private static final Map<String, String> MANIFEST_RECORD = new LinkedHashMap<>();

    static {
        MANIFEST_RECORD.put("acc_manifest", "acc_id");
        MANIFEST_RECORD.put("asccp_manifest", "asccp_id");
        MANIFEST_RECORD.put("bccp_manifest", "bccp_id");
        MANIFEST_RECORD.put("ascc_manifest", "ascc_id");
        MANIFEST_RECORD.put("bcc_manifest", "bcc_id");
        MANIFEST_RECORD.put("dt_manifest", "dt_id");
        MANIFEST_RECORD.put("dt_sc_manifest", "dt_sc_id");
        MANIFEST_RECORD.put("code_list_manifest", "code_list_id");
        MANIFEST_RECORD.put("code_list_value_manifest", "code_list_value_id");
        MANIFEST_RECORD.put("agency_id_list_manifest", "agency_id_list_id");
        MANIFEST_RECORD.put("agency_id_list_value_manifest", "agency_id_list_value_id");
        MANIFEST_RECORD.put("xbt_manifest", "xbt_id");
    }

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
    }

    @AfterEach
    void rollback() throws SQLException {
        connection.rollback();
        connection.close();
    }

    private long one(String sql, Object... bindings) {
        return dsl.fetchOne(sql, bindings).get(0, Long.class);
    }

    private List<Long> ids(String sql, Object... bindings) {
        return dsl.fetch(sql, bindings).map(r -> r.get(0, Long.class));
    }

    // ---- fingerprints of the whole database, but for the rows of the release under test ----

    private record Table(String name, String primaryKey, boolean hasRelease, List<String> columns) {
    }

    private List<Table> tables() {
        List<Table> tables = new ArrayList<>();
        for (Record t : dsl.fetch("select table_name from information_schema.tables where table_schema = database() "
                + "and table_type = 'BASE TABLE' and (table_name like '%manifest%' or table_name like '%awd\\_pri' "
                + "or table_name in ('seq_key', 'release_dep', 'acc', 'asccp', 'bccp', 'dt', 'dt_sc', 'ascc', 'bcc', "
                + "'code_list', 'code_list_value', 'agency_id_list', 'agency_id_list_value', 'xbt', 'blob_content', "
                + "'bie_view_order') or table_name like 'github\\_issue%') order by table_name")) {
            String name = t.get(0, String.class);
            List<String> columns = ids2(name);
            List<Record> pk = dsl.fetch("select column_name from information_schema.columns where table_schema = database() "
                    + "and table_name = ? and column_key = 'PRI'", name);
            tables.add(new Table(name, pk.size() == 1 ? pk.get(0).get(0, String.class) : null,
                    columns.contains("release_id"), columns));
        }
        return tables;
    }

    private List<String> ids2(String table) {
        return dsl.fetch("select column_name from information_schema.columns where table_schema = database() "
                        + "and table_name = ? order by ordinal_position", table)
                .map(r -> r.get(0, String.class));
    }

    private Map<String, Long> maxKeys(List<Table> tables) {
        Map<String, Long> max = new LinkedHashMap<>();
        for (Table t : tables) {
            if (t.primaryKey() != null) {
                Long value = dsl.fetchOne("select max(`" + t.primaryKey() + "`) from `" + t.name() + "`").get(0, Long.class);
                max.put(t.name(), value == null ? 0L : value);
            }
        }
        return max;
    }

    /**
     * count and a checksum of every row of the table, but the rows of the release under test and the rows created after
     * the keys were read.
     */
    private Map<String, String> snapshot(List<Table> tables, long release, Map<String, Long> maxKeys) {
        Map<String, String> snapshot = new LinkedHashMap<>();
        for (Table t : tables) {
            StringBuilder row = new StringBuilder("concat_ws('|'");
            t.columns().forEach(c -> row.append(", ifnull(`").append(c).append("`, '<null>')"));
            row.append(")");
            List<String> where = new ArrayList<>();
            if (t.hasRelease()) {
                where.add("release_id <> " + release);
            }
            if (t.primaryKey() != null && maxKeys.containsKey(t.name())) {
                where.add("`" + t.primaryKey() + "` <= " + maxKeys.get(t.name()));
            }
            Record r = dsl.fetchOne("select count(*), ifnull(sum(crc32(" + row + ")), 0) from `" + t.name() + "`"
                    + (where.isEmpty() ? "" : " where " + String.join(" and ", where)));
            snapshot.put(t.name(), r.get(0) + " rows, checksum " + r.get(1));
        }
        return snapshot;
    }

    private List<String> differences(Map<String, String> before, Map<String, String> after) {
        List<String> differences = new ArrayList<>();
        before.forEach((table, value) -> {
            if (!value.equals(after.get(table))) {
                differences.add(table + ": " + value + " -> " + after.get(table));
            }
        });
        return differences;
    }

    // ---- the life of the release ----

    @Test
    void aDraftReleaseOfEveryComponentIsCopiedWithinTheLibraryAndGoesAwayWhenItIsInitializedAgain() {
        long library = one("select library_id from library where name = ?", LIBRARY);
        long working = one("select release_id from `release` where library_id = ? and release_num = 'Working'", library);
        long namespace = one("select namespace_id from `release` where release_id = ?", working);
        List<Long> dependencyReleases = ids("select depend_on_release_id from release_dep where release_id = ?", working);
        assertTrue(!dependencyReleases.isEmpty(), "the library depends on another library's release");

        // 1. every component of 'Working' is Candidate.
        for (String t : List.of("acc", "asccp", "bccp", "dt", "code_list", "agency_id_list")) {
            dsl.execute("update `" + t + "` set state = 'Candidate' where `" + t + "_id` in "
                    + "(select `" + t + "_id` from `" + t + "_manifest` where release_id = ?)", working);
        }
        for (String t : List.of("acc", "asccp", "bccp", "dt", "code_list", "agency_id_list")) {
            assertEquals(0, one("select count(*) from `" + t + "` x join `" + t + "_manifest` m using (`" + t + "_id`) "
                    + "where m.release_id = ? and x.state <> 'Candidate'", working), t + " is all Candidate");
        }

        List<Table> tables = tables();
        Map<String, Long> maxKeys = maxKeys(tables);
        Map<String, String> candidates = snapshot(tables, -1, maxKeys);

        // 2. create the release '8.0' in 'Initialized' state.
        long user = one("select min(app_user_id) from app_user");
        dsl.execute("insert into `release` (library_id, guid, release_num, namespace_id, created_by, last_updated_by, "
                + "creation_timestamp, last_update_timestamp, state) values (?, replace(uuid(), '-', ''), '8.0', ?, ?, ?, "
                + "now(6), now(6), 'Initialized')", library, namespace, user, user);
        long release = one("select last_insert_id()");
        ReleaseId releaseId = new ReleaseId(BigInteger.valueOf(release));
        ReleaseId workingId = new ReleaseId(BigInteger.valueOf(working));

        // 3. assign every Candidate and validate.
        List<AccManifestId> acc = ids("select acc_manifest_id from acc_manifest where release_id = ?", working)
                .stream().map(i -> new AccManifestId(BigInteger.valueOf(i))).toList();
        List<AsccpManifestId> asccp = ids("select asccp_manifest_id from asccp_manifest where release_id = ?", working)
                .stream().map(i -> new AsccpManifestId(BigInteger.valueOf(i))).toList();
        List<BccpManifestId> bccp = ids("select bccp_manifest_id from bccp_manifest where release_id = ?", working)
                .stream().map(i -> new BccpManifestId(BigInteger.valueOf(i))).toList();
        List<DtManifestId> dt = ids("select dt_manifest_id from dt_manifest where release_id = ?", working)
                .stream().map(i -> new DtManifestId(BigInteger.valueOf(i))).toList();
        List<CodeListManifestId> codeList = ids("select code_list_manifest_id from code_list_manifest where release_id = ?", working)
                .stream().map(i -> new CodeListManifestId(BigInteger.valueOf(i))).toList();
        List<AgencyIdListManifestId> agencyIdList = ids("select agency_id_list_manifest_id from agency_id_list_manifest "
                + "where release_id = ?", working).stream().map(i -> new AgencyIdListManifestId(BigInteger.valueOf(i))).toList();

        ReleaseValidator validator = new ReleaseValidator(REQUESTER, releaseId, factory);
        validator.setAssignedAccComponentManifestIds(acc);
        validator.setAssignedAsccpComponentManifestIds(asccp);
        validator.setAssignedBccpComponentManifestIds(bccp);
        validator.setAssignedDtComponentManifestIds(dt);
        validator.setAssignedCodeListComponentManifestIds(codeList);
        validator.setAssignedAgencyIdListComponentManifestIds(agencyIdList);
        ReleaseValidationResponse response = validator.validate();
        Map<String, Integer> problems = new LinkedHashMap<>();
        List.of(response.getStatusMapForAcc(), response.getStatusMapForAsccp(), response.getStatusMapForBccp(),
                        response.getStatusMapForDt(), response.getStatusMapForCodeList(),
                        response.getStatusMapForAgencyIdList())
                .forEach(map -> map.values().forEach(set -> set.forEach(m ->
                        problems.merge(m.getLevel() + ": " + m.getMessage(), 1, Integer::sum))));
        System.out.println("validation: succeed=" + response.isSucceed() + " " + problems);
        assertTrue(response.isSucceed(), "the validation of the assignment of everything: " + problems);

        // 4. the assigned components are 'ReleaseDraft' and the manifests are copied.
        for (String t : List.of("acc", "asccp", "bccp", "dt", "code_list", "agency_id_list")) {
            dsl.execute("update `" + t + "` set state = 'ReleaseDraft' where `" + t + "_id` in "
                    + "(select `" + t + "_id` from `" + t + "_manifest` where release_id = ?)", working);
        }
        Map<String, String> beforeCopy = snapshot(tables, release, maxKeys);

        factory.ccCommandRepository(REQUESTER).copyWorkingManifests(releaseId, workingId, acc, asccp, bccp, dt, codeList, agencyIdList);
        factory.releaseCommandRepository(REQUESTER).copyDepsFromWorking(releaseId, workingId);
        factory.gitHubIssueLinkCommandRepository(REQUESTER).copyLinksFromWorking(releaseId);
        factory.bieViewOrderCommandRepository(REQUESTER).copyFromWorking(releaseId);

        // 5. what is copied.
        List<String> report = new ArrayList<>();
        List<String> failures = new ArrayList<>();

        for (var entry : MANIFEST_RECORD.entrySet()) {
            String table = entry.getKey();
            String record = entry.getValue();
            long inWorking = one("select count(*) from `" + table + "` where release_id = ?", working);
            long inRelease = one("select count(*) from `" + table + "` where release_id = ?", release);
            report.add(String.format("%-32s working %7d, new release %7d", table, inWorking, inRelease));
            if (inWorking != inRelease) {
                failures.add(table + ": " + inWorking + " manifests in 'Working' but " + inRelease + " in the new release");
            }

            // every manifest has the link to its manifest of 'Working', once.
            String next = "next_" + table.replace("_manifest", "") + "_manifest_id";
            long notLinked = one("select count(*) from `" + table + "` n where n.release_id = ? and not exists "
                    + "(select 1 from `" + table + "` w where w.release_id = ? and w.`" + table + "_id` = n.`" + next + "`)",
                    release, working);
            long linkedTwice = one("select count(*) from (select `" + next + "` from `" + table + "` where release_id = ? "
                    + "group by `" + next + "` having count(*) > 1) x", release);
            if (notLinked != 0 || linkedTwice != 0) {
                failures.add(table + ": " + notLinked + " not linked to 'Working', " + linkedTwice + " linked twice");
            }

            // the record belongs to the library, or to a release it depends on.
            long foreign = one("select count(*) from `" + table + "` n where n.release_id = ? and not exists "
                    + "(select 1 from `" + table + "` o join `release` r on r.release_id = o.release_id "
                    + "where o.`" + record + "` = n.`" + record + "` and (r.library_id = ? or r.release_id in ("
                    + String.join(",", dependencyReleases.stream().map(String::valueOf).toList()) + ")) "
                    + "and o.release_id <> n.release_id)", release, library);
            if (foreign != 0) {
                failures.add(table + ": " + foreign + " records of the new release belong to neither the library nor a release it depends on");
            }
        }

        for (String table : List.of("dt_awd_pri", "dt_sc_awd_pri")) {
            long inWorking = one("select count(*) from `" + table + "` where release_id = ?", working);
            long inRelease = one("select count(*) from `" + table + "` where release_id = ?", release);
            report.add(String.format("%-32s working %7d, new release %7d", table, inWorking, inRelease));
            if (inWorking != inRelease) {
                failures.add(table + ": " + inWorking + " in 'Working' but " + inRelease + " in the new release");
            }
        }
        report.add("seq_key rows: " + (one("select count(*) from seq_key") - maxKeysCount(maxKeys)));

        // every manifest a row of the new release points to is in the new release, or in a release the library depends on.
        String allowed = release + "," + String.join(",", dependencyReleases.stream().map(String::valueOf).toList());
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
            long rows = one("select count(*) from `" + table + "` t where t.release_id = ? and t.`" + column + "` is not null", release);
            report.add(String.format("  %-34s %-34s %7d refs, %d outside", table, column, rows, outside));
            if (outside != 0) {
                failures.add(table + "." + column + ": " + outside + " references point outside the new release and its dependency releases");
            }
        }

        // an allowed primitive belongs to a Data Type / supplementary component of the new release.
        for (String[] t : new String[][]{{"dt_awd_pri", "dt_id", "dt_manifest"}, {"dt_sc_awd_pri", "dt_sc_id", "dt_sc_manifest"}}) {
            long orphan = one("select count(*) from `" + t[0] + "` p where p.release_id = ? and not exists (select 1 from `"
                    + t[2] + "` m where m.release_id = p.release_id and m.`" + t[1] + "` = p.`" + t[1] + "`)", release);
            if (orphan != 0) {
                failures.add(t[0] + ": " + orphan + " rows of the new release belong to no manifest of it");
            }
        }

        // the library's records are not touched, and neither is anything of the other releases.
        Map<String, String> afterCopy = snapshot(tables, release, maxKeys);
        List<String> changed = differences(beforeCopy, afterCopy);
        report.add("tables that changed outside the new release by the copy: " + changed);
        if (!changed.isEmpty()) {
            failures.add("the copy changed rows of other releases: " + changed);
        }
        long recordsCreated = 0;
        for (String record : List.of("acc", "asccp", "bccp", "dt", "dt_sc", "ascc", "bcc", "code_list", "code_list_value",
                "agency_id_list", "agency_id_list_value", "xbt")) {
            recordsCreated += one("select count(*) from `" + record + "` where `" + record + "_id` > ?", maxKeys.get(record));
        }
        report.add("records created by the copy: " + recordsCreated);
        if (recordsCreated != 0) {
            failures.add("the copy created " + recordsCreated + " records");
        }

        // 6. 'Initialized' again: the same as the transition of the release service does.
        var ccCommand = factory.ccCommandRepository(REQUESTER);
        for (String t : List.of("acc", "asccp", "bccp", "dt", "code_list", "agency_id_list")) {
            dsl.execute("update `" + t + "` set state = 'Candidate' where `" + t + "_id` in "
                    + "(select `" + t + "_id` from `" + t + "_manifest` where release_id = ?)", working);
        }
        factory.releaseCommandRepository(REQUESTER).deleteDeps(releaseId);
        removeTags(releaseId);
        factory.gitHubIssueLinkCommandRepository(REQUESTER).deleteLinksByRelease(releaseId);
        factory.bieViewOrderCommandRepository(REQUESTER).deleteByRelease(releaseId);
        ccCommand.clearReplacement(releaseId);
        ccCommand.delete(releaseId);

        long left = 0;
        for (Table t : tables) {
            if (t.hasRelease()) {
                left += one("select count(*) from `" + t.name() + "` where release_id = ?", release);
            }
        }
        report.add("rows left of the new release: " + left);
        if (left != 0) {
            failures.add(left + " rows of the new release are left");
        }
        Map<String, String> afterRevert = snapshot(tables, release, maxKeys);
        List<String> notRestored = differences(candidates, afterRevert);
        report.add("tables that differ from before the release: " + notRestored);
        if (!notRestored.isEmpty()) {
            failures.add("what was there before the release was not restored: " + notRestored);
        }
        long seqKeyLeft = one("select count(*) from seq_key where seq_key_id > ?", maxKeys.get("seq_key"));
        if (seqKeyLeft != 0) {
            failures.add(seqKeyLeft + " seq_key rows are left");
        }

        report.forEach(System.out::println);
        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }

    private long maxKeysCount(Map<String, Long> maxKeys) {
        return one("select count(*) from seq_key where seq_key_id <= ?", maxKeys.get("seq_key"));
    }

    private void removeTags(ReleaseId releaseId) {
        var tagQuery = factory.tagQueryRepository(REQUESTER);
        var tagCommand = factory.tagCommandRepository(REQUESTER);
        tagQuery.getAccManifestTagList(List.of(releaseId)).forEach(t -> tagCommand.removeTag(t.tagId(), t.accManifestId()));
        tagQuery.getAsccpManifestTagList(List.of(releaseId)).forEach(t -> tagCommand.removeTag(t.tagId(), t.asccpManifestId()));
        tagQuery.getBccpManifestTagList(List.of(releaseId)).forEach(t -> tagCommand.removeTag(t.tagId(), t.bccpManifestId()));
        tagQuery.getDtManifestTagList(List.of(releaseId)).forEach(t -> tagCommand.removeTag(t.tagId(), t.dtManifestId()));
    }
}
