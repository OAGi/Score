package org.oagi.score.gateway.http.api.release_management.service.migrationload;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.release_management.model.MigrationLoadJob;
import org.oagi.score.gateway.http.api.release_management.model.MigrationLoadJobId;
import org.oagi.score.gateway.http.api.release_management.model.MigrationLoadState;
import org.oagi.score.gateway.http.api.release_management.repository.MigrationLoadJobRepository;
import org.oagi.score.gateway.http.common.model.AccessControlException;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.api.release_management.repository.MigrationLoadRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A release package loaded into a copy of the schema of the application and swapped for it, with the whole application running:
 * on a MariaDB and a Redis made for the test, as the user a deployment would give it (all privileges on the schemas named
 * after the application's own), with a write freeze that the test controls, since the application's own is not made yet.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class MigrationLoadEndToEndTest {

    @Container
    static final MariaDBContainer MARIADB = new MariaDBContainer("mariadb:11.8").withUsername("root").withDatabaseName("oagi");

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7.4").withExposedPorts(6379);

    private static final String APPLICATION_USER = "oagi_app";
    private static final String APPLICATION_PASSWORD = "app-secret";
    private static final long DELTA = 100000000L;

    @DynamicPropertySource
    static void application(DynamicPropertyRegistry registry) throws SQLException {
        try (Connection root = DriverManager.getConnection(url("mysql"), "root", MARIADB.getPassword()); Statement statement = root.createStatement()) {
            statement.execute("CREATE USER IF NOT EXISTS '" + APPLICATION_USER + "'@'%' IDENTIFIED BY '" + APPLICATION_PASSWORD + "'");
            statement.execute("GRANT ALL PRIVILEGES ON `oagi%`.* TO '" + APPLICATION_USER + "'@'%'");
        }
        registry.add("DB_HOST", MARIADB::getHost);
        registry.add("DB_PORT", () -> MARIADB.getMappedPort(3306));
        registry.add("DB_DATABASE", () -> "oagi");
        registry.add("DB_USERNAME", () -> APPLICATION_USER);
        registry.add("DB_PASSWORD", () -> APPLICATION_PASSWORD);
        registry.add("REDIS_HOST", REDIS::getHost);
        registry.add("REDIS_PORT", () -> REDIS.getMappedPort(6379));
        registry.add("score.migration-load.enabled", () -> "true");
    }

    /** The write freeze of the test: it records what it is asked, and does what the test says. */
    static final class RecordingFreeze implements WriteFreeze {
        final List<String> calls = new CopyOnWriteArrayList<>();
        volatile boolean refuse;
        volatile Runnable afterFreeze;
        volatile CountDownLatch hold;
        final CountDownLatch entered = new CountDownLatch(1);

        @Override
        public void freeze() {
            calls.add("freeze");
            if (refuse) {
                throw new MigrationLoadRefusedException("There is no way to stop writing.");
            }
            Runnable action = afterFreeze;
            if (action != null) {
                action.run();
            }
            CountDownLatch latch = hold;
            if (latch != null) {
                entered.countDown();
                try {
                    latch.await(60, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        @Override
        public void release() {
            calls.add("release");
        }

        void reset() {
            calls.clear();
            refuse = false;
            afterFreeze = null;
            hold = null;
        }
    }

    @TestConfiguration
    static class FreezeConfiguration {
        @Bean
        RecordingFreeze writeFreeze() {
            return new RecordingFreeze();
        }
    }

    @Autowired
    private MigrationLoadCommandService service;

    @Autowired
    private MigrationLoadWatchdog watchdog;

    @Autowired
    private MigrationLoadRepositories repositories;

    @Autowired
    private RecordingFreeze freeze;

    private static final ScoreUser ADMIN = new ScoreUser(new UserId(BigInteger.ONE), "admin", "Admin", "admin@example.com", true,
            List.of(ScoreRole.ADMINISTRATOR));
    private static final ScoreUser END_USER = new ScoreUser(new UserId(BigInteger.valueOf(2)), "user", "User", "user@example.com", true,
            List.of(ScoreRole.END_USER));

    private Connection root;
    private final List<Path> files = new ArrayList<>();

    // ---------------------------------------------------------------------------------------------------------------
    // The database the tests start from
    // ---------------------------------------------------------------------------------------------------------------

    private static String url(String schema) {
        return "jdbc:mariadb://" + MARIADB.getHost() + ":" + MARIADB.getMappedPort(3306) + "/" + schema;
    }

    @BeforeEach
    void freshInstance() throws Exception {
        root = DriverManager.getConnection(url("oagi"), "root", MARIADB.getPassword());
        for (String schema : strings("SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'oagi\\_%'")) {
            sql("DROP DATABASE `" + schema + "`");
        }
        freeze.reset();
        sql("SET foreign_key_checks = 0");
        sql("SET @disable_record_change_log = 1");
        for (String table : List.of("migration_load_job", "namespace", "library", "tag", "record_change_log")) {
            sql("DELETE FROM `" + table + "`");
        }
        sql("DELETE FROM configuration WHERE type = 'test'");
        // The developers' data of the instance (which a package replaces), and what its users made.
        sql("INSERT INTO library (library_id, name, created_by, last_updated_by, creation_timestamp, last_update_timestamp) VALUES "
                + "(3, 'dev-only-old', 1, 1, NOW(), NOW()), (5, 'dev-lib-old', 1, 1, NOW(), NOW()), "
                + "(" + (DELTA + 1) + ", 'end-user-lib', 1, 1, NOW(), NOW())");
        sql("INSERT INTO namespace (namespace_id, library_id, uri, owner_user_id, created_by, last_updated_by, creation_timestamp, last_update_timestamp) VALUES "
                + "(" + (DELTA + 1) + ", 5, 'urn:end-user', 1, 1, 1, NOW(), NOW())");
        sql("INSERT INTO tag (name, text_color, background_color, created_by, last_updated_by, creation_timestamp, last_update_timestamp) "
                + "VALUES ('my-tag', '#000', '#fff', 1, 1, NOW(), NOW())");
        sql("INSERT INTO configuration (name, type, value) VALUES ('setting', 'test', 'mine')");
        // The instance's own first account, which the package does not carry: it is what the instance made of it.
        sql("UPDATE app_user SET name = 'Administrator of this instance' WHERE app_user_id = 1");
        sql("SET @disable_record_change_log = NULL");
        sql("SET foreign_key_checks = 1");
        // An instance that has had packages loaded before: the numbering of the tables is pinned.
        for (String table : new org.oagi.score.gateway.http.api.release_management.service.migrationload.format.ReleasePackageFormatV1().numberedFromDelta()) {
            sql("ALTER TABLE `" + table + "` AUTO_INCREMENT = " + (DELTA + 1));
        }
    }

    @AfterEach
    void clean() throws Exception {
        root.close();
        for (Path file : files) {
            Files.deleteIfExists(file);
        }
    }

    private void sql(String sql) throws SQLException {
        try (Statement statement = root.createStatement()) {
            statement.execute(sql);
        }
    }

    private List<String> strings(String sql) throws SQLException {
        List<String> values = new ArrayList<>();
        try (Statement statement = root.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                values.add(rs.getString(1));
            }
        }
        return values;
    }

    private int count(String sql) throws SQLException {
        return Integer.parseInt(strings(sql).get(0));
    }

    private boolean schemaExists(String schema) throws SQLException {
        return count("SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = '" + schema + "'") > 0;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // The package of the tests
    // ---------------------------------------------------------------------------------------------------------------

    private static final List<String> LOADER_TABLES = List.of("acc", "acc_manifest", "ascc", "ascc_manifest", "asccp",
            "asccp_manifest", "bcc", "bcc_manifest", "bccp", "bccp_manifest", "code_list", "code_list_manifest",
            "code_list_value", "code_list_value_manifest", "dt", "dt_manifest", "dt_sc", "dt_sc_manifest", "library",
            "log", "namespace", "seq_key");

    /** What a package of a release does: the data of the developers, and the numbering of the tables the users' records are numbered in. */
    private static String developerData() {
        String pins = new org.oagi.score.gateway.http.api.release_management.service.migrationload.format.ReleasePackageFormatV1().numberedFromDelta().stream()
                .map(table -> "ALTER TABLE `" + table + "` AUTO_INCREMENT = " + (DELTA + 1) + ";")
                .collect(Collectors.joining("\n"));
        return "INSERT INTO `library` (`library_id`, `name`, `created_by`, `last_updated_by`, `creation_timestamp`, `last_update_timestamp`) "
                + "VALUES (5, 'dev-lib-new', 1, 1, NOW(), NOW());\n" + pins + "\n";
    }

    private String latestSchemaVersion() {
        try {
            return strings("SELECT version FROM flyway_schema_history WHERE success = 1 ORDER BY installed_rank DESC LIMIT 1").get(0);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private Path pack(String schemaVersion, String script, String scriptName) throws IOException {
        return pack("1.2", schemaVersion, script, scriptName);
    }

    private Path pack(String version, String schemaVersion, String script, String scriptName) throws IOException {
        String tables = LOADER_TABLES.stream().map(t -> "{\"table_name\": \"" + t + "\", \"max_id\": 0}").collect(Collectors.joining(", "));
        String info = """
                {"version": "%s", "maintainer": "test", "description": "Add 10.0 release",
                 %s
                 "migration": {"release": {"id": 77, "release_num": "10.0"},
                   "processes": [{"order": 1, "executable": {"script": "%s"}}],
                   "metadata": {"releases": [], "tables": [%s]}}}
                """.formatted(version, schemaVersion == null ? "" : "\"schema_version\": \"" + schemaVersion + "\",", scriptName, tables);
        Path file = Files.createTempFile("package", ".zip");
        files.add(file);
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(file))) {
            zip.putNextEntry(new ZipEntry("mig_info_10.0.json"));
            zip.write(info.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            if (script != null) {
                zip.putNextEntry(new ZipEntry(scriptName));
                zip.write(script.getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return file;
    }

    private Path pack() throws IOException {
        return pack(latestSchemaVersion(), developerData(), "mig_10.0.sql");
    }

    private MigrationLoadJob awaitEnd(MigrationLoadJobId id) {
        await().atMost(Duration.ofSeconds(180)).pollInterval(Duration.ofMillis(100))
                .until(() -> !service.get(ADMIN, id).state().isActive());
        return service.get(ADMIN, id);
    }

    private MigrationLoadJob loaded() throws Exception {
        MigrationLoadJob started = service.start(ADMIN, pack(), "10_0.zip");
        return awaitEnd(started.id());
    }

    // ---------------------------------------------------------------------------------------------------------------
    // The load that goes through, and its undo
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void aPackageIsLoadedAndSwappedInWithTheWorkOfTheUsersAndItIsUndone() throws Exception {
        int triggers = count("SELECT COUNT(*) FROM information_schema.triggers WHERE trigger_schema = 'oagi'");
        assertTrue(triggers > 50, "triggers: " + triggers);

        MigrationLoadJob started = service.start(ADMIN, pack(), "10_0.zip");
        List<MigrationLoadState> seen = new ArrayList<>();
        await().atMost(Duration.ofSeconds(180)).pollInterval(Duration.ofMillis(20)).until(() -> {
            MigrationLoadState state = service.get(ADMIN, started.id()).state();
            if (seen.isEmpty() || seen.get(seen.size() - 1) != state) {
                seen.add(state);
            }
            return !state.isActive();
        });
        MigrationLoadJob job = service.get(ADMIN, started.id());

        assertEquals(MigrationLoadState.SUCCEEDED, job.state(), job.message());
        assertEquals(seen.stream().sorted().toList(), seen, "the states are gone through in order: " + seen);
        assertEquals("oagi_load_" + job.id(), job.stagingSchema());
        assertEquals("oagi_backup_" + job.id(), job.backupSchema());
        assertEquals("10.0", job.releaseNum());
        assertEquals(ADMIN.userId(), job.ownerUserId());

        // The developers' data is the package's, and what the users made is where it was.
        assertEquals(List.of("dev-lib-new", "end-user-lib"), strings("SELECT name FROM oagi.library ORDER BY library_id"));
        assertEquals(List.of("urn:end-user"), strings("SELECT uri FROM oagi.namespace"));
        assertEquals(List.of("my-tag"), strings("SELECT name FROM oagi.tag"));
        assertEquals(List.of("mine"), strings("SELECT value FROM oagi.configuration WHERE name = 'setting'"));
        assertEquals(List.of("Administrator of this instance"), strings("SELECT name FROM oagi.app_user WHERE app_user_id = 1"), "the first account is the instance's own");
        assertEquals(0, count("SELECT COUNT(*) FROM oagi.record_change_log"), "what was copied is not a change");
        assertEquals(triggers, count("SELECT COUNT(*) FROM information_schema.triggers WHERE trigger_schema = 'oagi'"));
        assertTrue(count("SELECT auto_increment FROM information_schema.tables WHERE table_schema = 'oagi' AND table_name = 'library'") > DELTA);
        // What was served is kept, the staging schema is gone, and the record of the load went through the swap.
        assertEquals(List.of("dev-only-old", "dev-lib-old", "end-user-lib"), strings("SELECT name FROM " + job.backupSchema() + ".library ORDER BY library_id"));
        assertFalse(schemaExists(job.stagingSchema()));
        assertEquals(List.of("freeze", "release"), freeze.calls);

        // The undo.
        service.rollback(ADMIN, job.id());
        MigrationLoadJob undone = awaitEnd(job.id());

        assertEquals(MigrationLoadState.ROLLED_BACK, undone.state(), undone.message());
        assertEquals(List.of("dev-only-old", "dev-lib-old", "end-user-lib"), strings("SELECT name FROM oagi.library ORDER BY library_id"));
        assertEquals(List.of("dev-lib-new", "end-user-lib"), strings("SELECT name FROM " + undone.discardedSchema() + ".library ORDER BY library_id"));
        assertFalse(schemaExists(job.backupSchema()), "what is put back is no more kept apart");
        assertEquals(triggers, count("SELECT COUNT(*) FROM information_schema.triggers WHERE trigger_schema = 'oagi'"));
        assertEquals(List.of("freeze", "release", "freeze", "release"), freeze.calls);
        assertThrows(MigrationLoadRefusedException.class, () -> service.rollback(ADMIN, job.id()), "a swap is undone once");
    }

    @Test
    void aPackageWrittenForAnOlderSchemaIsLoadedAtThatShapeAndThenBroughtToThisOne() throws Exception {
        Path older = pack("3.4.0", developerData(), "mig_10.0.sql");

        MigrationLoadJob job = awaitEnd(service.start(ADMIN, older, "10_0_old.zip").id());

        assertEquals(MigrationLoadState.SUCCEEDED, job.state(), job.message());
        assertEquals(1, count("SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'oagi' AND table_name = 'library' AND column_name = 'is_default'"),
                "the shape of this version, which the package did not know");
        assertEquals(List.of("dev-lib-new", "end-user-lib"), strings("SELECT name FROM oagi.library ORDER BY library_id"));
    }

    @Test
    void theRecordOfTheLoadIsKeptThroughTheSwapAndIsTheOneThatIsReadAfterIt() throws Exception {
        MigrationLoadJob job = loaded();

        assertEquals(MigrationLoadState.SUCCEEDED, job.state(), job.message());
        MigrationLoadJobRepository jobs = repositories.jobs();
        assertEquals(List.of(), jobs.findActive());
        assertEquals(1, count("SELECT COUNT(*) FROM oagi.migration_load_job"), "one record, not the empty one of the staging schema");
        assertEquals(job.id(), jobs.findLatest(MigrationLoadState.SUCCEEDED).orElseThrow().id());
    }

    // ---------------------------------------------------------------------------------------------------------------
    // The load that is refused or fails: the served schema is as it was
    // ---------------------------------------------------------------------------------------------------------------

    private void assertServedSchemaUntouched() throws SQLException {
        assertEquals(List.of("dev-only-old", "dev-lib-old", "end-user-lib"), strings("SELECT name FROM oagi.library ORDER BY library_id"));
        assertEquals(List.of("urn:end-user"), strings("SELECT uri FROM oagi.namespace"));
        assertEquals(List.of(), strings("SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'oagi\\_%'"),
                "no schema of a load is left");
    }

    @Test
    void aTableThatIsNotNumberedFromTheNumberOfTheUsersRefusesTheLoadBeforeAnythingIsChanged() throws Exception {
        sql("ALTER TABLE acc AUTO_INCREMENT = 5");

        MigrationLoadJob job = loaded();

        assertEquals(MigrationLoadState.FAILED, job.state());
        assertTrue(job.message().contains("acc"), job.message());
        assertTrue(job.message().contains("data-loader"), job.message());
        assertEquals(List.of(), freeze.calls);
        assertServedSchemaUntouched();
    }

    @Test
    void aScriptThatFailsLeavesNoSchemaAndTheServedOneAsItWas() throws Exception {
        MigrationLoadJob started = service.start(ADMIN, pack(latestSchemaVersion(), "INSERT INTO no_such_table VALUES (1);", "mig_10.0.sql"), "bad.zip");

        MigrationLoadJob job = awaitEnd(started.id());

        assertEquals(MigrationLoadState.FAILED, job.state());
        assertTrue(job.message().contains("no_such_table"), job.message());
        assertEquals(List.of(), freeze.calls, "it failed before writing was stopped");
        assertServedSchemaUntouched();
    }

    @Test
    void aWriteThatGetsPastTheFreezeAfterTheWatermarkIsFoundBeforeTheSwapAndNothingIsSwapped() throws Exception {
        // The write comes from another connection once the copy has started, which is after the watermark was taken.
        List<Throwable> failures = new CopyOnWriteArrayList<>();
        Thread writer = new Thread(() -> {
            try (Connection other = DriverManager.getConnection(url("oagi"), "root", MARIADB.getPassword());
                 Statement statement = other.createStatement()) {
                await().atMost(Duration.ofSeconds(120)).pollInterval(Duration.ofMillis(5)).until(() -> {
                    try (ResultSet rs = statement.executeQuery("SELECT message FROM migration_load_job")) {
                        return rs.next() && rs.getString(1) != null && rs.getString(1).startsWith("Copying");
                    }
                });
                statement.execute("INSERT INTO configuration (name, type, value) VALUES ('late', 'test', 'written after the freeze')");
            } catch (Throwable e) {
                failures.add(e);
            }
        });
        freeze.afterFreeze = writer::start;

        MigrationLoadJob job = loaded();
        writer.join(TimeUnit.SECONDS.toMillis(30));

        assertEquals(List.of(), failures);
        assertEquals(MigrationLoadState.FAILED, job.state(), job.message());
        assertTrue(job.message().contains("written to configuration"), job.message());
        assertEquals(List.of("freeze", "release"), freeze.calls, "the freeze is ended when the load fails");
        assertEquals(List.of("dev-only-old", "dev-lib-old", "end-user-lib"), strings("SELECT name FROM oagi.library ORDER BY library_id"));
        assertEquals(1, count("SELECT COUNT(*) FROM oagi.configuration WHERE name = 'late'"), "the write is where it was written");
        assertEquals(List.of(), strings("SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'oagi\\_%'"));
    }

    @Test
    void whatTheUsersMadeThatPointsAtDeveloperDataThePackageNoLongerHasIsNotSwappedInAndSaysWhat() throws Exception {
        // A namespace of a user in a library of the developers that the new package does not have (it has 5, not 3).
        sql("SET foreign_key_checks = 0");
        sql("UPDATE namespace SET library_id = 3");
        sql("SET foreign_key_checks = 1");

        MigrationLoadJob job = loaded();

        assertEquals(MigrationLoadState.FAILED, job.state());
        assertTrue(job.message().contains("namespace") && job.message().contains("library") && job.message().contains("not there"), job.message());
        assertEquals(List.of("freeze", "release"), freeze.calls);
        assertEquals(List.of("dev-only-old", "dev-lib-old", "end-user-lib"), strings("SELECT name FROM oagi.library ORDER BY library_id"));
    }

    @Test
    void aLoadThatCannotStopWritingIsRefusedAndLeavesNothing() throws Exception {
        freeze.refuse = true;

        MigrationLoadJob job = loaded();

        assertEquals(MigrationLoadState.FAILED, job.state());
        assertTrue(job.message().contains("stop writing"), job.message());
        assertEquals(List.of("freeze"), freeze.calls, "there was nothing to end");
        assertServedSchemaUntouched();
    }

    @Test
    void aSecondLoadIsRefusedWhileOneGoesOnAndTheFirstIsNotHarmed() throws Exception {
        CountDownLatch hold = new CountDownLatch(1);
        freeze.hold = hold;
        MigrationLoadJob first = service.start(ADMIN, pack(), "first.zip");
        assertTrue(freeze.entered.await(120, TimeUnit.SECONDS), "the first load is where writing is stopped");

        Path second = pack();
        MigrationLoadRefusedException refused = assertThrows(MigrationLoadRefusedException.class, () -> service.start(ADMIN, second, "second.zip"));
        assertTrue(refused.getMessage().contains("Another load goes on"), refused.getMessage());
        assertFalse(Files.exists(second), "a package that is refused is deleted");
        hold.countDown();

        assertEquals(MigrationLoadState.SUCCEEDED, awaitEnd(first.id()).state());
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Who may ask, and what is not a package
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void onlyAnAdministratorLoadsAPackageOrSeesHowItGoes() throws Exception {
        Path file = pack();

        assertThrows(AccessControlException.class, () -> service.start(END_USER, file, "x.zip"));
        assertFalse(Files.exists(file), "the file of a refused request is deleted");
        MigrationLoadJob job = loaded();
        assertThrows(AccessControlException.class, () -> service.get(END_USER, job.id()));
        assertThrows(AccessControlException.class, () -> service.rollback(END_USER, job.id()));
    }

    @Test
    void whenLoadsAreTurnedOffNothingIsLoaded() throws Exception {
        MigrationLoadCommandService off = new MigrationLoadCommandService(repositories,
                new MigrationLoadSettings(false, DELTA, Duration.ofSeconds(5), Duration.ofHours(1)), null, null, null);
        Path file = pack();

        MigrationLoadRefusedException e = assertThrows(MigrationLoadRefusedException.class, () -> off.start(ADMIN, file, "x.zip"));

        assertTrue(e.getMessage().contains("turned off"), e.getMessage());
        assertEquals(0, count("SELECT COUNT(*) FROM oagi.migration_load_job"));
    }

    @Test
    void whatIsNotAPackageIsRefusedAndNothingIsRecorded() throws Exception {
        Path notZip = Files.createTempFile("not", ".zip");
        Files.writeString(notZip, "this is not a zip file");

        assertThrows(MigrationLoadRefusedException.class, () -> service.start(ADMIN, notZip, "not.zip"));

        assertEquals(0, count("SELECT COUNT(*) FROM oagi.migration_load_job"));
        assertFalse(Files.exists(notZip));
    }

    @Test
    void aPackageWhoseScriptIsMissingOrIsNotSqlIsRefusedBeforeAnythingIsMade() throws Exception {
        MigrationLoadJob missing = awaitEnd(service.start(ADMIN, pack(latestSchemaVersion(), null, "mig_10.0.sql"), "missing.zip").id());
        MigrationLoadJob notSql = awaitEnd(service.start(ADMIN, pack(latestSchemaVersion(), "println(1)", "mig_10.0.kts"), "code.zip").id());

        assertEquals(MigrationLoadState.FAILED, missing.state());
        assertTrue(missing.message().contains("missing"), missing.message());
        assertEquals(MigrationLoadState.FAILED, notSql.state());
        assertTrue(notSql.message().contains("not SQL"), notSql.message());
        assertServedSchemaUntouched();
    }

    // ---------------------------------------------------------------------------------------------------------------
    // The version of the package says how it is loaded
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void aPackageOfAVersionThatIsNotLoadedIsRefusedBeforeAnythingIsRecordedAndItsFileIsDeleted() throws Exception {
        Path newer = pack("2.0", latestSchemaVersion(), developerData(), "mig_10.0.sql");
        Path older = pack("1.0", null, developerData(), "mig_10.0.sql");

        MigrationLoadRefusedException e = assertThrows(MigrationLoadRefusedException.class, () -> service.start(ADMIN, newer, "newer.zip"));
        assertThrows(MigrationLoadRefusedException.class, () -> service.start(ADMIN, older, "older.zip"));

        assertTrue(e.getMessage().contains("version 2.0") && e.getMessage().contains("does not load"), e.getMessage());
        assertEquals(0, count("SELECT COUNT(*) FROM oagi.migration_load_job"));
        assertFalse(Files.exists(newer));
        assertFalse(Files.exists(older));
        assertServedSchemaUntouched();
    }

    @Test
    void aPackageOfTheVersionOnePointOneThatSaysNoVersionOfTheSchemaIsLoadedIntoTheLatest() throws Exception {
        Path path = pack("1.1", null, developerData(), "mig_10.0.sql");

        MigrationLoadJob job = awaitEnd(service.start(ADMIN, path, "10_0_v11.zip").id());

        assertEquals(MigrationLoadState.SUCCEEDED, job.state(), job.message());
        assertEquals(List.of("dev-lib-new", "end-user-lib"), strings("SELECT name FROM oagi.library ORDER BY library_id"));
    }

    @Test
    void theStructureOfATableThatAPackageMakesAgainIsTheSchemasOwn() throws Exception {
        // As the generator writes it, the structure of a table is a copy: with a column that the schema does not have.
        String script = "DROP TABLE IF EXISTS `xbt_manifest`;\n"
                + "CREATE TABLE `xbt_manifest` (`xbt_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `stale_column` int DEFAULT NULL, PRIMARY KEY (`xbt_manifest_id`)) ENGINE=InnoDB;\n"
                + developerData();

        MigrationLoadJob job = awaitEnd(service.start(ADMIN, pack(latestSchemaVersion(), script, "mig_10.0.sql"), "stale.zip").id());

        assertEquals(MigrationLoadState.SUCCEEDED, job.state(), job.message());
        assertEquals(0, count("SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'oagi' AND table_name = 'xbt_manifest' AND column_name = 'stale_column'"));
        assertTrue(count("SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'oagi' AND table_name = 'xbt_manifest'") > 3);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // A load that nobody does any more
    // ---------------------------------------------------------------------------------------------------------------

    private MigrationLoadJobId abandonedIn(MigrationLoadState state, String staging) throws Exception {
        MigrationLoadJobRepository jobs = repositories.jobs();
        MigrationLoadJobId id = jobs.createUnlessActive(ADMIN.userId(), "gone.zip", "10.0", "another-server").orElseThrow();
        jobs.moveState(id, MigrationLoadState.ACTIVE, state, "going on");
        jobs.schemas(id, staging, null, null);
        sql("CREATE DATABASE `" + staging + "`");
        sql("UPDATE migration_load_job SET last_update_timestamp = '2020-01-01 00:00:00'");
        return id;
    }

    @Test
    void aLoadThatNobodyDoesAnyMoreIsTakenBackItsStagingSchemaIsDroppedAndTheFreezeEnded() throws Exception {
        MigrationLoadJobId id = abandonedIn(MigrationLoadState.COPYING, "oagi_load_gone");

        assertEquals(1, watchdog.recoverSince(LocalDateTime.now().minusHours(1)));

        MigrationLoadJob job = repositories.jobs().find(id).orElseThrow();
        assertEquals(MigrationLoadState.FAILED, job.state());
        assertTrue(job.message().contains("abandoned") && job.message().contains("another-server"), job.message());
        assertFalse(schemaExists("oagi_load_gone"));
        assertEquals(List.of("release"), freeze.calls);
        assertEquals(0, watchdog.recoverSince(LocalDateTime.now().minusHours(1)), "it is taken back once");
    }

    @Test
    void aLoadThatWasSwappingWhenItWasAbandonedIsMarkedAndItsSchemasAreLeftToBeLookedAt() throws Exception {
        MigrationLoadJobId id = abandonedIn(MigrationLoadState.SWAPPING, "oagi_load_midswap");

        watchdog.recoverSince(LocalDateTime.now().minusHours(1));

        assertEquals(MigrationLoadState.FAILED, repositories.jobs().find(id).orElseThrow().state());
        assertTrue(schemaExists("oagi_load_midswap"), "what was being moved is not dropped");
    }

    @Test
    void aLoadThatChangedLatelyIsNotTakenBack() throws Exception {
        MigrationLoadJobId id = abandonedIn(MigrationLoadState.LOADING, "oagi_load_busy");
        repositories.jobs().progress(id, "still at it");

        assertEquals(0, watchdog.recoverSince(LocalDateTime.now().minusHours(1)));

        assertEquals(MigrationLoadState.LOADING, repositories.jobs().find(id).orElseThrow().state());
        assertTrue(schemaExists("oagi_load_busy"));
    }
}
