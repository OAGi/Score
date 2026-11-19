package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJob;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadState;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

import javax.sql.DataSource;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Writing is stopped in the database while the schema is swapped: with the freeze of the application itself (the gate), on a MariaDB
 * made for the test. The writers that are at work are waited for, the writers and the readers that come while writing is stopped are
 * five each, the writers go on when the swap is over (into the schema that is swapped in), the readers are not held, and no connection of
 * the pool that was there before the swap is there after it.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class ReleasePackageLoadWriteGateTest {

    @Container
    static final MariaDBContainer MARIADB = new MariaDBContainer("mariadb:11.8").withUsername("root").withDatabaseName("oagi");

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7.4").withExposedPorts(6379);

    private static final String APPLICATION_USER = "oagi_app";
    private static final String APPLICATION_PASSWORD = "app-secret";
    private static final long DELTA = 100000000L;
    private static final int WORKERS = 5;

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
        registry.add("score.release-package-load.drain-wait", () -> "PT60S");
        registry.add("score.release-package-load.write-wait", () -> "PT120S");
    }

    /**
     * The freeze of the application, with what the test does once writing is stopped (which is just before the users' records are
     * copied, and so before the swap), and what it does before the load asks for it.
     */
    static final class HookedFreeze implements ReleasePackageLoadWriteFreeze {
        private final ReleasePackageLoadDatabaseWriteFreeze real;
        volatile Runnable afterFreeze;
        final AtomicInteger frozen = new AtomicInteger();

        HookedFreeze(ReleasePackageLoadDatabaseWriteFreeze real) {
            this.real = real;
        }

        @Override
        public void freeze() {
            real.freeze();
            frozen.incrementAndGet();
            Runnable action = afterFreeze;
            if (action != null) {
                action.run();
            }
        }

        @Override
        public void release() {
            real.release();
        }
    }

    @TestConfiguration
    static class FreezeConfiguration {
        @Bean
        @Primary
        HookedFreeze hookedFreeze(ReleasePackageLoadDatabaseWriteFreeze real) {
            return new HookedFreeze(real);
        }
    }

    @Autowired
    private ReleasePackageLoadCommandService service;
    @Autowired
    private HookedFreeze freeze;
    @Autowired
    private DSLContext dsl;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private DataSource dataSource;

    private static final ScoreUser ADMIN = new ScoreUser(new UserId(BigInteger.ONE), "admin", "Admin", "admin@example.com", true,
            List.of(ScoreRole.ADMINISTRATOR));

    private Connection root;
    private final List<Path> files = new ArrayList<>();
    private ExecutorService pool;

    private static String url(String schema) {
        return "jdbc:mariadb://" + MARIADB.getHost() + ":" + MARIADB.getMappedPort(3306) + "/" + schema;
    }

    @BeforeEach
    void freshInstance() throws Exception {
        root = DriverManager.getConnection(url("oagi"), "root", MARIADB.getPassword());
        for (String schema : strings("SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'oagi\\_%'")) {
            sql("DROP DATABASE `" + schema + "`");
        }
        freeze.afterFreeze = null;
        freeze.frozen.set(0);
        sql("SET foreign_key_checks = 0");
        sql("SET @disable_record_change_log = 1");
        for (String table : List.of("release_package_load_job", "namespace", "library", "tag", "record_change_log")) {
            sql("DELETE FROM `" + table + "`");
        }
        sql("DELETE FROM configuration WHERE type = 'test'");
        sql("INSERT INTO library (library_id, name, created_by, last_updated_by, creation_timestamp, last_update_timestamp) VALUES "
                + "(3, 'dev-only-old', 1, 1, NOW(), NOW()), (5, 'dev-lib-old', 1, 1, NOW(), NOW()), "
                + "(" + (DELTA + 1) + ", 'end-user-lib', 1, 1, NOW(), NOW())");
        sql("INSERT INTO configuration (name, type, value) VALUES ('setting', 'test', 'mine')");
        sql("SET @disable_record_change_log = NULL");
        sql("SET foreign_key_checks = 1");
        for (String table : new org.oagi.score.gateway.http.api.release_management.service.package_load.format.ReleasePackageFormatV1().numberedFromDelta()) {
            sql("ALTER TABLE `" + table + "` AUTO_INCREMENT = " + (DELTA + 1));
        }
        pool = Executors.newFixedThreadPool(2 * WORKERS);
    }

    @AfterEach
    void clean() throws Exception {
        pool.shutdownNow();
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

    // ---------------------------------------------------------------------------------------------------------------
    // The package of the tests (see ReleasePackageLoadEndToEndTest)
    // ---------------------------------------------------------------------------------------------------------------

    private static final List<String> LOADER_TABLES = List.of("acc", "acc_manifest", "ascc", "ascc_manifest", "asccp",
            "asccp_manifest", "bcc", "bcc_manifest", "bccp", "bccp_manifest", "code_list", "code_list_manifest",
            "code_list_value", "code_list_value_manifest", "dt", "dt_manifest", "dt_sc", "dt_sc_manifest", "library",
            "log", "namespace", "seq_key");

    private Path pack() throws IOException {
        String pins = new org.oagi.score.gateway.http.api.release_management.service.package_load.format.ReleasePackageFormatV1().numberedFromDelta().stream()
                .map(table -> "ALTER TABLE `" + table + "` AUTO_INCREMENT = " + (DELTA + 1) + ";")
                .collect(Collectors.joining("\n"));
        String script = "INSERT INTO `library` (`library_id`, `name`, `created_by`, `last_updated_by`, `creation_timestamp`, `last_update_timestamp`) "
                + "VALUES (5, 'dev-lib-new', 1, 1, NOW(), NOW());\n" + pins + "\n";
        String version;
        try {
            version = strings("SELECT version FROM flyway_schema_history WHERE success = 1 ORDER BY installed_rank DESC LIMIT 1").get(0);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        String tables = LOADER_TABLES.stream().map(t -> "{\"table_name\": \"" + t + "\", \"max_id\": 1000}").collect(Collectors.joining(", "));
        String info = """
                {"version": "1.2", "maintainer": "test", "description": "Add 10.0 release",
                 "schema_version": "%s",
                 "migration": {"release": {"id": 77, "release_num": "10.0"},
                   "processes": [{"order": 1, "executable": {"script": "mig_10.0.sql"}}],
                   "metadata": {"releases": [], "tables": [%s]}}}
                """.formatted(version, tables);
        Path file = Files.createTempFile("package", ".zip");
        files.add(file);
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(file))) {
            zip.putNextEntry(new ZipEntry("mig_info_10.0.json"));
            zip.write(info.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("mig_10.0.sql"));
            zip.write(script.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return file;
    }

    private ReleasePackageLoadJob awaitEnd(ReleasePackageLoadJob started) {
        await().atMost(Duration.ofSeconds(180)).pollInterval(Duration.ofMillis(100))
                .until(() -> !service.get(ADMIN, started.id()).state().isActive());
        return service.get(ADMIN, started.id());
    }

    // ---------------------------------------------------------------------------------------------------------------
    // What the application's writers and readers do
    // ---------------------------------------------------------------------------------------------------------------

    /** A write of the application: in a transaction, as the services do it. */
    private void write(String name) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                dsl.execute("INSERT INTO configuration (name, type, value) VALUES (?, 'test', ?)", name, "written-" + name));
    }

    private String read() {
        return dsl.resultQuery("SELECT value FROM configuration WHERE name = 'setting' AND type = 'test'").fetchOne(0, String.class);
    }

    /** The ids that the database gives to {@code count} connections of the pool, which are all in use at the same time. */
    private Set<String> connectionIds(int count) throws Exception {
        List<Connection> held = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        try {
            for (int i = 0; i < count; i++) {
                Connection connection = dataSource.getConnection();
                held.add(connection);
                try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery("SELECT CONNECTION_ID()")) {
                    rs.next();
                    ids.add(rs.getString(1));
                }
            }
        } finally {
            for (Connection connection : held) {
                connection.close();
            }
        }
        return ids;
    }

    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void fiveWritersAndFiveReadersThatComeWhileWritingIsStoppedAreAllThroughAfterTheSwapAndNoOldConnectionIsLeft() throws Exception {
        Set<String> before = connectionIds(20);
        List<Future<?>> writers = new CopyOnWriteArrayList<>();
        List<Future<String>> readers = new CopyOnWriteArrayList<>();
        AtomicInteger writersDone = new AtomicInteger();
        List<String> readValuesWhileStopped = new CopyOnWriteArrayList<>();
        List<Integer> doneWhileStopped = new CopyOnWriteArrayList<>();

        // Writing is stopped now. Five writers and five readers come: the writers are held at the gate, the readers are not.
        freeze.afterFreeze = () -> {
            for (int i = 0; i < WORKERS; i++) {
                String name = "w" + i;
                writers.add(pool.submit(() -> {
                    write(name);
                    writersDone.incrementAndGet();
                }));
                readers.add(pool.submit(this::read));
            }
            try {
                for (Future<String> reader : readers) {
                    readValuesWhileStopped.add(reader.get(30, TimeUnit.SECONDS));
                }
                Thread.sleep(3000);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            doneWhileStopped.add(writersDone.get());
        };

        ReleasePackageLoadJob job = awaitEnd(service.start(ADMIN, pack(), "10_0.zip"));

        assertEquals(ReleasePackageLoadState.SUCCEEDED, job.state(), job.message());
        // The readers were not held: they had their answers while writing was stopped, and the answers are one.
        assertEquals(List.of("mine", "mine", "mine", "mine", "mine"), readValuesWhileStopped);
        // The writers were held for as long as writing was stopped...
        assertEquals(List.of(0), doneWhileStopped, "no writer got through while writing was stopped");
        // ... and are through when it is over.
        for (Future<?> writer : writers) {
            writer.get(60, TimeUnit.SECONDS);
        }
        assertEquals(WORKERS, writersDone.get());
        // All five writes are in the schema that is served now (the one that was swapped in), and none is in the one that was swapped out.
        assertEquals(List.of("w0", "w1", "w2", "w3", "w4"),
                strings("SELECT name FROM oagi.configuration WHERE type = 'test' AND name LIKE 'w%' ORDER BY name"));
        assertEquals(0, strings("SELECT name FROM " + job.backupSchema() + ".configuration WHERE name LIKE 'w%'").size());
        assertEquals(List.of("mine"), strings("SELECT value FROM oagi.configuration WHERE name = 'setting'"));
        for (int i = 0; i < WORKERS; i++) {
            assertEquals(List.of("written-w" + i), strings("SELECT value FROM oagi.configuration WHERE name = 'w" + i + "'"));
        }
        // Readers that come after it read what is served now: the same, from every one.
        Set<String> after = new HashSet<>();
        for (int i = 0; i < WORKERS; i++) {
            Future<String> reader = pool.submit(this::read);
            after.add(reader.get(30, TimeUnit.SECONDS));
        }
        assertEquals(Set.of("mine"), after);
        // No connection of the pool from before the swap is in it now.
        Set<String> now = connectionIds(20);
        Set<String> survivors = new HashSet<>(before);
        survivors.retainAll(now);
        assertEquals(Set.of(), survivors, "connections of before the swap that are still used");
    }

    @Test
    void aWriterThatIsAtWorkIsWaitedForAndItsWriteIsInTheSchemaThatIsSwappedIn() throws Exception {
        CountDownLatch inside = new CountDownLatch(1);
        CountDownLatch finish = new CountDownLatch(1);
        Future<?> writer = pool.submit(() -> new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            dsl.execute("INSERT INTO configuration (name, type, value) VALUES ('in-flight', 'test', 'was at work')");
            inside.countDown();
            try {
                finish.await(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
        assertTrue(inside.await(30, TimeUnit.SECONDS));

        ReleasePackageLoadJob started = service.start(ADMIN, pack(), "10_0.zip");
        await().atMost(Duration.ofSeconds(120)).until(() -> service.get(ADMIN, started.id()).state() == ReleasePackageLoadState.COPYING);
        Thread.sleep(3000);
        assertEquals(0, freeze.frozen.get(), "writing is not stopped while a writer is at work");

        finish.countDown();
        writer.get(30, TimeUnit.SECONDS);
        ReleasePackageLoadJob job = awaitEnd(started);

        assertEquals(ReleasePackageLoadState.SUCCEEDED, job.state(), job.message());
        assertEquals(1, freeze.frozen.get());
        assertEquals(List.of("was at work"), strings("SELECT value FROM oagi.configuration WHERE name = 'in-flight'"),
                "what the writer that was waited for wrote is in the schema that was swapped in");
    }

    @Test
    void writingGoesOnAfterALoadAndNoGateIsLeftTaken() throws Exception {
        ReleasePackageLoadJob job = awaitEnd(service.start(ADMIN, pack(), "10_0.zip"));
        assertEquals(ReleasePackageLoadState.SUCCEEDED, job.state(), job.message());

        write("after");

        assertEquals(List.of("written-after"), strings("SELECT value FROM oagi.configuration WHERE name = 'after'"));
    }

    @Test
    void aGateWhoseRowIsMissingStopsNothingSoTheLoadIsRefusedAndNothingIsChanged() throws Exception {
        sql("DELETE FROM named_lock WHERE lock_name = 'write_gate'");
        try {
            ReleasePackageLoadJob job = awaitEnd(service.start(ADMIN, pack(), "10_0.zip"));

            assertEquals(ReleasePackageLoadState.FAILED, job.state());
            assertTrue(job.message().contains("could not be paused"), job.message());
            assertEquals(List.of("dev-only-old", "dev-lib-old", "end-user-lib"), strings("SELECT name FROM oagi.library ORDER BY library_id"));
        } finally {
            sql("INSERT INTO named_lock (lock_name) VALUES ('write_gate')");
        }
    }
}
