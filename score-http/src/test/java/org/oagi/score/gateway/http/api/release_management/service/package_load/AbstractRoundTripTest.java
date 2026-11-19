package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.release_management.controller.payload.GenerateMigrationScriptResponse;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJob;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadState;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseQueryService;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadRepositories;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A package that the application writes of a database, loaded back into that database, makes the database it was: the
 * structure of every table, and the data of every table, the same by checksum.
 *
 * <p>The package is written by what the application writes it with ({@code ReleaseQueryService#generateMigrationScript}) and is
 * given, as it is, to the load. What the package does not hold is what the users made, and what the load copies from the schema that is
 * served, so what is compared is all of the database: the schema before, the schema after the swap, and the schema that the swap kept
 * (the one that was served).</p>
 */
abstract class AbstractRoundTripTest {

    static final long DELTA = 100000000L;

    /** What is not compared: the history of the migrations, the changes the triggers log, and the record of the loads. */
    private static final Set<String> NOT_COMPARED = Set.of("flyway_schema_history", "record_change_log", "release_package_load_job",
            "named_lock");

    private static final ScoreUser ADMIN = new ScoreUser(new UserId(BigInteger.ONE), "admin", "Admin", "admin@example.com", true,
            List.of(ScoreRole.ADMINISTRATOR));

    @Autowired
    private ReleaseQueryService releaseQueryService;

    @Autowired
    private ReleasePackageLoadCommandService service;

    @Autowired
    private ReleasePackageLoadRepositories repositories;

    @Autowired
    private ReleasePackageLoadRecordingWriteFreeze freeze;

    /** A connection of the owner of the database, to the schema the application serves. */
    abstract Connection root() throws Exception;

    /** How long a run may take. */
    abstract Duration timeout();

    private void sql(Connection con, String sql) throws Exception {
        try (Statement statement = con.createStatement()) {
            statement.execute(sql);
        }
    }

    /** The triggers and the routines of the schema, as they are told by the catalog. */
    private List<String> programs(Connection con, String schema) throws Exception {
        List<String> programs = new java.util.ArrayList<>();
        try (Statement statement = con.createStatement();
             ResultSet rs = statement.executeQuery("SELECT CONCAT_WS('|', 'trigger', trigger_name, event_object_table, action_timing, event_manipulation, action_statement) "
                     + "FROM information_schema.triggers WHERE trigger_schema = '" + schema + "' UNION ALL "
                     + "SELECT CONCAT_WS('|', 'routine', routine_name, routine_type, routine_definition) FROM information_schema.routines "
                     + "WHERE routine_schema = '" + schema + "' ORDER BY 1")) {
            while (rs.next()) {
                // As what made a body may have kept its comments or taken them out, they are not what is compared.
                programs.add(org.oagi.score.gateway.http.api.release_management.service.package_load.script.SqlText.normalize(rs.getString(1)));
            }
        }
        return programs;
    }

    private String string(Connection con, String sql) throws Exception {
        try (Statement statement = con.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    @Test
    void aPackageOfADatabaseLoadedIntoItMakesTheDatabaseItWasInStructureAndInData() throws Exception {
        Connection root = root();
        // The instance is one that has had packages loaded: the numbering of the tables the package numbers is pinned.
        for (String table : new org.oagi.score.gateway.http.api.release_management.service.package_load.format.ReleasePackageFormatV1().numberedFromDelta()) {
            sql(root, "ALTER TABLE `" + table + "` AUTO_INCREMENT = " + (DELTA + 1));
        }
        Map<String, String> before = SchemaChecksums.of(root, "oagi", NOT_COMPARED);
        List<String> programsBefore = programs(root, "oagi");
        System.out.println("ROUNDTRIP before: " + before.size() + " tables, " + SchemaChecksums.records(before) + " records");

        ReleaseId release = new ReleaseId(new BigInteger(string(root,
                "SELECT release_id FROM `release` WHERE release_num <> 'Working' ORDER BY release_id DESC LIMIT 1")));
        long started = System.currentTimeMillis();
        GenerateMigrationScriptResponse written = releaseQueryService.generateMigrationScript(ADMIN, release, false);
        System.out.println("ROUNDTRIP package " + written.getFilename() + ": " + Files.size(written.getFile().toPath()) + " bytes, written in "
                + (System.currentTimeMillis() - started) / 1000 + " s");

        ReleasePackageLoadJob job = service.start(ADMIN, written.getFile().toPath(), written.getFilename());
        await().atMost(timeout()).pollInterval(Duration.ofSeconds(2)).until(() -> {
            ReleasePackageLoadJob now = service.get(ADMIN, job.id());
            System.out.println("ROUNDTRIP " + now.state() + ": " + now.message());
            return !now.state().isActive();
        });
        ReleasePackageLoadJob done = service.get(ADMIN, job.id());
        assertEquals(ReleasePackageLoadState.SUCCEEDED, done.state(), done.message());
        assertEquals(List.of("freeze", "release"), freeze.calls);

        // The structure: every table, column, index, key and check of the schema that is served now is that of the one that was. (The
        // triggers and the routines are not compared with it: the swap drops the triggers of the tables it moves, so the schema it keeps has none.)
        List<String> structure = repositories.query().differences("oagi", done.backupSchema()).stream()
                .filter(line -> !line.matches("(?s)only in \\S+: (trigger|routine) .*"))
                .filter(line -> NOT_COMPARED.stream().noneMatch(table -> line.contains(" " + table + "|") || line.endsWith(" " + table))).toList();
        assertEquals(List.of(), structure, "structure");
        assertEquals(programsBefore, programs(root, "oagi"), "triggers and routines");
        assertFalse(programsBefore.isEmpty());

        // The data: every table, in the three places it is.
        Map<String, String> kept = SchemaChecksums.of(root, done.backupSchema(), NOT_COMPARED);
        Map<String, String> after = SchemaChecksums.of(root, "oagi", NOT_COMPARED);
        System.out.println("ROUNDTRIP after: " + after.size() + " tables, " + SchemaChecksums.records(after) + " records");
        assertEquals(List.of(), SchemaChecksums.differences(before, kept), "the schema that was served is what it was");
        assertEquals(List.of(), SchemaChecksums.differences(before, after), "data");
        assertTrue(after.size() > 100, "tables compared: " + after.size());
    }
}
