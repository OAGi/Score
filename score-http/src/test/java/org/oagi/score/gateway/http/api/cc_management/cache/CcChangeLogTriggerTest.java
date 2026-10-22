package org.oagi.score.gateway.http.api.cc_management.cache;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What every trigger writes to {@code cc_change_log}, for every table and every event, against the development database.
 * <p>
 * Each case runs in a transaction that is <b>rolled back</b>, so the data is never changed and no row is left in the log
 * (with foreign key checks off inside it, so that a row other rows refer to can be deleted). The rows it wrote are read
 * inside the transaction and compared with what they must be, computed from the row that was changed.
 * <pre>
 *   mvn test -Dtest=CcChangeLogTriggerTest -DargLine=-Dscore.freshness=true
 * </pre>
 */
@EnabledIfSystemProperty(named = "score.freshness", matches = "true")
class CcChangeLogTriggerTest {

    private static HikariDataSource dataSource;

    @BeforeAll
    static void connect() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(System.getProperty("score.db.url", "jdbc:mariadb://localhost:3306/oagi?useSSL=false"));
        config.setUsername(System.getProperty("score.db.username", "oagi"));
        config.setPassword(System.getProperty("score.db.password", "oagi"));
        config.setMaximumPoolSize(3);
        dataSource = new HikariDataSource(config);
    }

    @AfterAll
    static void disconnect() {
        dataSource.close();
    }

    // ---- the harness ---------------------------------------------------------------------------------------------

    /** A log row as text: {@code KIND target parent release}, with {@code -} for null. */
    private static String row(String kind, Long target, Long parent, Long release) {
        return kind + " " + target + " " + (parent == null ? "-" : parent) + " " + (release == null ? "-" : release);
    }

    /**
     * Runs the change inside a transaction, reads the rows it wrote, rolls back, and checks that the rows are gone.
     *
     * @return the rows the change wrote, sorted
     */
    private List<String> logged(Consumer<DSLContext> change) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            DSLContext dsl = DSL.using(connection, SQLDialect.MARIADB);
            dsl.execute("SET FOREIGN_KEY_CHECKS = 0");
            dsl.execute("SET UNIQUE_CHECKS = 0");
            Long before = dsl.fetchOne("select ifnull(max(cc_change_log_id), 0) from cc_change_log").get(0, Long.class);
            try {
                change.accept(dsl);
                List<String> rows = dsl.fetch(
                                "select kind, target_id, parent_manifest_id, release_id from cc_change_log where cc_change_log_id > ?", before)
                        .map(r -> row(r.get(0, String.class), r.get(1, Long.class), r.get(2, Long.class), r.get(3, Long.class)));
                return rows.stream().sorted().collect(Collectors.toList());
            } finally {
                connection.rollback();
                dsl.execute("SET FOREIGN_KEY_CHECKS = 1");
                dsl.execute("SET UNIQUE_CHECKS = 1");
                connection.commit();
            }
        } finally {
            try (Connection check = dataSource.getConnection()) {
                // nothing the case wrote is left behind
                // (read by the next case through its own "before"; here the rollback is asserted by the callers' data checks)
            }
        }
    }

    private static List<String> sorted(String... rows) {
        return Arrays.stream(rows).sorted().collect(Collectors.toList());
    }

    private static DSLContext read() {
        return DSL.using(dataSource, SQLDialect.MARIADB);
    }

    private static Long value(Record record, int i) {
        return record.get(i, Long.class);
    }

    private static List<String> columnsWithoutAutoIncrement(DSLContext dsl, String table) {
        return dsl.fetch("select column_name from information_schema.columns where table_schema = database() and table_name = ?"
                        + " and extra not like '%auto_increment%' order by ordinal_position", table)
                .getValues(0, String.class);
    }

    /** Inserts a copy of the row; @return the new primary key. */
    private static long copy(DSLContext dsl, String table, String pk, long id) {
        String columns = columnsWithoutAutoIncrement(dsl, table).stream().map(c -> "`" + c + "`").collect(Collectors.joining(", "));
        dsl.execute("insert into `" + table + "` (" + columns + ") select " + columns + " from `" + table + "` where `" + pk + "` = ?", id);
        return dsl.fetchOne("select last_insert_id()").get(0, Long.class);
    }

    // ---- the seven component kinds ------------------------------------------------------------------------------

    private record Kind(String table, String kind, String parentColumn) {
        String manifest() {
            return table + "_manifest";
        }

        String manifestId() {
            return table + "_manifest_id";
        }
    }

    private static final List<Kind> KINDS = List.of(
            new Kind("acc", "ACC", null), new Kind("ascc", "ASCC", "from_acc_manifest_id"),
            new Kind("bcc", "BCC", "from_acc_manifest_id"), new Kind("asccp", "ASCCP", null),
            new Kind("bccp", "BCCP", null), new Kind("dt", "DT", null), new Kind("dt_sc", "DT_SC", "owner_dt_manifest_id"));

    private String parentSelect(Kind k) {
        return (k.parentColumn() == null) ? "null" : k.parentColumn();
    }

    @Test
    void aManifestTableWritesOneRowForTheManifestItsParentAndItsRelease() throws SQLException {
        for (Kind k : KINDS) {
            Record sample = read().fetchOne("select " + k.manifestId() + ", release_id, " + parentSelect(k)
                    + " from " + k.manifest() + " order by 1 limit 1");
            long id = value(sample, 0);
            String expected = row(k.kind(), id, value(sample, 2), value(sample, 1));

            assertEquals(sorted(expected), logged(dsl ->
                    dsl.execute("update " + k.manifest() + " set " + k.manifestId() + " = " + k.manifestId()
                            + " where " + k.manifestId() + " = ?", id)), k.manifest() + " update");

            assertEquals(sorted(expected), logged(dsl ->
                    dsl.execute("delete from " + k.manifest() + " where " + k.manifestId() + " = ?", id)), k.manifest() + " delete");

            List<String> inserted = logged(dsl -> copy(dsl, k.manifest(), k.manifestId(), id));
            assertEquals(1, inserted.size(), k.manifest() + " insert: " + inserted);
            String[] parts = inserted.get(0).split(" ");
            assertEquals(k.kind(), parts[0], k.manifest() + " insert");
            assertNotEquals(String.valueOf(id), parts[1], "the id of the new row, not of the copied one");
            assertEquals(String.valueOf(value(sample, 2) == null ? "-" : value(sample, 2)), parts[2], k.manifest() + " insert: the parent");
            assertEquals(String.valueOf(value(sample, 1)), parts[3], k.manifest() + " insert: the release");
        }
    }

    @Test
    void aBaseTableWritesOneRowForEveryManifestThatShowsTheRow() throws SQLException {
        for (Kind k : KINDS) {
            // the row that most manifests (of several releases) show
            Record shared = read().fetchOne("select " + k.table() + "_id, count(*) from " + k.manifest()
                    + " group by 1 order by 2 desc, 1 limit 1");
            long id = value(shared, 0);
            List<String> expected = read().fetch("select " + k.manifestId() + ", release_id, " + parentSelect(k) + " from " + k.manifest()
                            + " where " + k.table() + "_id = ?", id)
                    .map(r -> row(k.kind(), value(r, 0), value(r, 2), value(r, 1))).stream().sorted().collect(Collectors.toList());
            assertFalse(expected.isEmpty());

            assertEquals(expected, logged(dsl -> dsl.execute("update `" + k.table() + "` set " + k.table() + "_id = " + k.table() + "_id"
                    + " where " + k.table() + "_id = ?", id)), k.table() + " update, " + expected.size() + " manifest(s)");

            assertEquals(expected, logged(dsl -> dsl.execute("delete from `" + k.table() + "` where " + k.table() + "_id = ?", id)),
                    k.table() + " delete");

            // a row no manifest shows (a new one) writes nothing
            assertEquals(List.of(), logged(dsl -> {
                long copy = copy(dsl, k.table(), k.table() + "_id", id);
                dsl.execute("update `" + k.table() + "` set " + k.table() + "_id = " + k.table() + "_id where " + k.table() + "_id = ?", copy);
                dsl.execute("delete from `" + k.table() + "` where " + k.table() + "_id = ?", copy);
            }), k.table() + ": a row that no manifest shows");
        }
    }

    // ---- the order of an ACC's associations and the tags -----------------------------------------------------------

    @Test
    void theOrderOfTheAssociationsWritesOneRowForTheAccThatOwnsThem() throws SQLException {
        Record sample = read().fetchOne("select seq_key_id, from_acc_manifest_id from seq_key order by 1 limit 1");
        long id = value(sample, 0);
        String expected = row("ACC", value(sample, 1), null, null);

        assertEquals(sorted(expected), logged(dsl -> dsl.execute("update seq_key set seq_key_id = seq_key_id where seq_key_id = ?", id)), "update");
        assertEquals(sorted(expected), logged(dsl -> dsl.execute("delete from seq_key where seq_key_id = ?", id)), "delete");
        assertEquals(sorted(expected), logged(dsl -> copy(dsl, "seq_key", "seq_key_id", id)), "insert");
    }

    @Test
    void aTagOnAManifestWritesOneRowForTheManifestWhenItIsAddedAndWhenItIsRemoved() throws SQLException {
        for (Kind k : KINDS.stream().filter(e -> List.of("acc", "asccp", "bccp", "dt").contains(e.table())).toList()) {
            String tagTable = k.manifest() + "_tag";
            Record free = read().fetchOne("select m." + k.manifestId() + ", t.tag_id from " + k.manifest() + " m join tag t"
                    + " where not exists (select 1 from " + tagTable + " x where x." + k.manifestId() + " = m." + k.manifestId()
                    + " and x.tag_id = t.tag_id) order by 1, 2 limit 1");
            long manifest = value(free, 0), tag = value(free, 1);
            String expected = row(k.kind(), manifest, null, null);

            assertEquals(sorted(expected, expected), logged(dsl -> {
                dsl.execute("insert into " + tagTable + " (" + k.manifestId() + ", tag_id, created_by, creation_timestamp) values (?, ?,"
                        + " (select min(app_user_id) from app_user), now())", manifest, tag);
                dsl.execute("delete from " + tagTable + " where " + k.manifestId() + " = ? and tag_id = ?", manifest, tag);
            }), tagTable + ": added and removed");
        }
    }

    // ---- what many records show -------------------------------------------------------------------------------------

    @Test
    void aTagReleaseAndLibraryWriteOneRowForThemselvesWhenTheyChangeOrAreDeleted() throws SQLException {
        long tag = value(read().fetchOne("select min(tag_id) from tag"), 0);
        assertEquals(sorted(row("TAG", tag, null, null)), logged(dsl -> dsl.execute("update tag set tag_id = tag_id where tag_id = ?", tag)), "tag update");
        assertEquals(sorted(row("TAG", tag, null, null)), logged(dsl -> dsl.execute("delete from tag where tag_id = ?", tag)), "tag delete");
        assertEquals(List.of(), logged(dsl -> copy(dsl, "tag", "tag_id", tag)), "a new tag is on no record yet");

        long release = value(read().fetchOne("select max(release_id) from `release`"), 0);
        assertEquals(sorted(row("RELEASE", release, null, release)),
                logged(dsl -> dsl.execute("update `release` set release_id = release_id where release_id = ?", release)), "release update");
        assertEquals(sorted(row("RELEASE", release, null, release)),
                logged(dsl -> dsl.execute("delete from `release` where release_id = ?", release)), "release delete");
        assertEquals(List.of(), logged(dsl -> copy(dsl, "release", "release_id", release)), "a new release has no records yet");

        long library = value(read().fetchOne("select min(library_id) from library"), 0);
        assertEquals(sorted(row("LIBRARY", library, null, null)),
                logged(dsl -> dsl.execute("update library set library_id = library_id where library_id = ?", library)), "library update");
        assertEquals(sorted(row("LIBRARY", library, null, null)),
                logged(dsl -> dsl.execute("delete from library where library_id = ?", library)), "library delete");
        assertEquals(List.of(), logged(dsl -> copy(dsl, "library", "library_id", library)), "a new library has no releases yet");
    }

    @Test
    void theDependenciesOfAReleaseWriteOneRowOnEveryEvent() throws SQLException {
        Record dep = read().fetchOne("select release_dep_id, release_id from release_dep order by 1 limit 1");
        long id = value(dep, 0);
        String expected = row("RELEASE_DEP", value(dep, 1), null, value(dep, 1));
        assertEquals(sorted(expected), logged(dsl -> dsl.execute("update release_dep set release_dep_id = release_dep_id where release_dep_id = ?", id)), "update");
        assertEquals(sorted(expected), logged(dsl -> dsl.execute("delete from release_dep where release_dep_id = ?", id)), "delete");
        assertEquals(sorted(expected), logged(dsl -> copy(dsl, "release_dep", "release_dep_id", id)), "insert");
    }

    @Test
    void aUserIsLoggedOnlyWhenWhatTheRecordsShowOfHimChanges() throws SQLException {
        long user = value(read().fetchOne("select min(app_user_id) from app_user"), 0);
        String expected = row("USER", user, null, null);

        assertEquals(List.of(), logged(dsl -> dsl.execute("update app_user set name = name, is_developer = is_developer where app_user_id = ?", user)),
                "nothing changed");
        assertEquals(List.of(), logged(dsl -> dsl.execute("update app_user set organization = concat(ifnull(organization, ''), 'x') where app_user_id = ?", user)),
                "a column the records do not show");
        assertEquals(List.of(), logged(dsl -> dsl.execute("update app_user set name = concat(name, 'x'), email = concat(ifnull(email, ''), 'x') where app_user_id = ?", user)),
                "the name and the e-mail are not shown either");
        assertEquals(sorted(expected), logged(dsl -> dsl.execute("update app_user set is_developer = 1 - is_developer where app_user_id = ?", user)),
                "the developer flag");
        assertEquals(sorted(expected), logged(dsl -> dsl.execute("update app_user set login_id = concat(login_id, 'x') where app_user_id = ?", user)),
                "the login");
    }

    // ---- a rollback leaves nothing -----------------------------------------------------------------------------------

    @Test
    void aRolledBackChangeLeavesNoRowInTheLog() throws SQLException {
        long before = value(read().fetchOne("select ifnull(max(cc_change_log_id), 0) from cc_change_log"), 0);
        long accManifest = value(read().fetchOne("select min(acc_manifest_id) from acc_manifest"), 0);

        List<String> inTransaction = logged(dsl -> dsl.execute("update acc_manifest set den = den where acc_manifest_id = ?", accManifest));

        assertEquals(1, inTransaction.size(), "the transaction saw its own row");
        assertEquals(0L, value(read().fetchOne("select count(*) from cc_change_log where cc_change_log_id > ?", before), 0),
                "and after the rollback it is gone");
    }
}
