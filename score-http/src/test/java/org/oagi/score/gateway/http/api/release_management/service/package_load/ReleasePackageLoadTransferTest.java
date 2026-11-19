package org.oagi.score.gateway.http.api.release_management.service.package_load;

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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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


import org.oagi.score.gateway.http.common.model.AccessControlException;
import org.oagi.score.gateway.http.common.model.NotFoundException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A release package that comes to the server (sent), is looked at without anything being
 * changed, and is loaded: on a MariaDB made for the test, 
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class ReleasePackageLoadTransferTest {

    @Container
    static final MariaDBContainer MARIADB = new MariaDBContainer("mariadb:11.8").withUsername("root").withDatabaseName("oagi");

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7.4").withExposedPorts(6379);

    private static final String APPLICATION_USER = "oagi_app";
    private static final String APPLICATION_PASSWORD = "app-secret";
    private static final long DELTA = 100000000L;
    private static volatile byte[] servedPackage = new byte[0];

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
        registry.add("score.release-package-load.max-package-size", () -> "1000000");
    }

    @Autowired
    private ReleasePackageLoadUploads uploads;
    @Autowired
    private ReleasePackageLoadCommandService service;

    private static final ScoreUser ADMIN = new ScoreUser(new UserId(BigInteger.ONE), "admin", "Admin", "admin@example.com", true,
            List.of(ScoreRole.ADMINISTRATOR));
    private static final ScoreUser END_USER = new ScoreUser(new UserId(BigInteger.valueOf(2)), "user", "User", "user@example.com", true,
            List.of(ScoreRole.END_USER));

    private Connection root;

    private static String url(String schema) {
        return "jdbc:mariadb://" + MARIADB.getHost() + ":" + MARIADB.getMappedPort(3306) + "/" + schema;
    }

    @BeforeEach
    void freshInstance() throws Exception {
        root = DriverManager.getConnection(url("oagi"), "root", MARIADB.getPassword());
        for (String schema : strings("SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'oagi\\_%'")) {
            sql("DROP DATABASE `" + schema + "`");
        }
        sql("SET foreign_key_checks = 0");
        sql("SET @disable_record_change_log = 1");
        for (String table : List.of("release_package_load_job", "namespace", "library", "tag", "record_change_log")) {
            sql("DELETE FROM `" + table + "`");
        }
        sql("INSERT INTO library (library_id, name, created_by, last_updated_by, creation_timestamp, last_update_timestamp) VALUES "
                + "(3, 'dev-only-old', 1, 1, NOW(), NOW()), (5, 'dev-lib-old', 1, 1, NOW(), NOW()), "
                + "(" + (DELTA + 1) + ", 'end-user-lib', 1, 1, NOW(), NOW())");
        sql("SET @disable_record_change_log = NULL");
        sql("SET foreign_key_checks = 1");
        for (String table : new org.oagi.score.gateway.http.api.release_management.service.package_load.format.ReleasePackageFormatV1().numberedFromDelta()) {
            sql("ALTER TABLE `" + table + "` AUTO_INCREMENT = " + (DELTA + 1));
        }
        servedPackage = pack("1.2", latestSchemaVersion());
    }

    @AfterEach
    void clean() throws Exception {
        root.close();
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

    private String latestSchemaVersion() throws SQLException {
        return strings("SELECT version FROM flyway_schema_history WHERE success = 1 ORDER BY installed_rank DESC LIMIT 1").get(0);
    }

    private static final List<String> LOADER_TABLES = List.of("acc", "acc_manifest", "ascc", "ascc_manifest", "asccp",
            "asccp_manifest", "bcc", "bcc_manifest", "bccp", "bccp_manifest", "code_list", "code_list_manifest",
            "code_list_value", "code_list_value_manifest", "dt", "dt_manifest", "dt_sc", "dt_sc_manifest", "library",
            "log", "namespace", "seq_key");

    /** The bytes of a package, as the tests make one. */
    private static byte[] pack(String version, String schemaVersion) throws IOException {
        String pins = new org.oagi.score.gateway.http.api.release_management.service.package_load.format.ReleasePackageFormatV1().numberedFromDelta().stream()
                .map(table -> "ALTER TABLE `" + table + "` AUTO_INCREMENT = " + (DELTA + 1) + ";")
                .collect(Collectors.joining("\n"));
        String script = "INSERT INTO `library` (`library_id`, `name`, `created_by`, `last_updated_by`, `creation_timestamp`, `last_update_timestamp`) "
                + "VALUES (5, 'dev-lib-new', 1, 1, NOW(), NOW());\n" + pins + "\n";
        String tables = LOADER_TABLES.stream().map(t -> "{\"table_name\": \"" + t + "\", \"max_id\": 1000}").collect(Collectors.joining(", "));
        String info = """
                {"version": "%s", "maintainer": "test", "description": "Add 10.0 release",
                 "schema_version": "%s",
                 "migration": {"release": {"id": 77, "release_num": "10.0"},
                   "processes": [{"order": 1, "executable": {"script": "mig_10.0.sql"}}],
                   "metadata": {"releases": [], "tables": [%s]}}}
                """.formatted(version, schemaVersion, tables);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry("mig_info_10.0.json"));
            zip.write(info.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("mig_10.0.sql"));
            zip.write(script.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return bytes.toByteArray();
    }

    private ReleasePackageLoadUploads.Upload receive(byte[] bytes) {
        return uploads.receive(ADMIN, "10_0.zip", new ByteArrayInputStream(bytes), bytes.length);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // A package that is sent
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void aPackageThatIsSentIsReadAndKeptUntilItIsLoadedOrThrownAway() throws Exception {
        ReleasePackageLoadUploads.Upload upload = receive(servedPackage);

        assertEquals(ReleasePackageLoadUploads.State.READY, upload.state());
        assertEquals("10.0", upload.releaseNum());
        assertEquals("1.2", upload.version());
        assertEquals(servedPackage.length, upload.bytes());
        assertTrue(Files.exists(upload.file()));

        uploads.discard(ADMIN, upload.id());

        assertFalse(Files.exists(upload.file()));
        assertThrows(NotFoundException.class, () -> uploads.get(ADMIN, upload.id()));
    }

    @Test
    void whatIsNotAPackageOrNotNamedAsOneOrTooLargeIsRefusedAndNothingIsKept() throws Exception {
        byte[] notAPackage = "not a zip".getBytes(StandardCharsets.UTF_8);
        assertThrows(ReleasePackageLoadRefusedException.class,
                () -> uploads.receive(ADMIN, "10_0.zip", new ByteArrayInputStream(notAPackage), notAPackage.length));
        assertThrows(ReleasePackageLoadRefusedException.class,
                () -> uploads.receive(ADMIN, "../10_0.zip", new ByteArrayInputStream(servedPackage), servedPackage.length));
        assertThrows(ReleasePackageLoadRefusedException.class,
                () -> uploads.receive(ADMIN, "10_0.txt", new ByteArrayInputStream(servedPackage), servedPackage.length));
        byte[] large = new byte[1_500_000];
        large[0] = 'P';
        large[1] = 'K';
        large[2] = 3;
        large[3] = 4;
        ReleasePackageLoadRefusedException tooLarge = assertThrows(ReleasePackageLoadRefusedException.class,
                () -> uploads.receive(ADMIN, "10_0.zip", new ByteArrayInputStream(large), -1));
        assertTrue(tooLarge.getMessage().contains("larger"), tooLarge.getMessage());
        assertThrows(ReleasePackageLoadRefusedException.class,
                () -> uploads.receive(ADMIN, "10_0.zip", new ByteArrayInputStream(servedPackage), 5_000_000));
    }

    @Test
    void aZipWithoutTheInformationFileOrWithAnUnsafeEntryNameIsRefusedAndNothingIsKept() throws Exception {
        for (String entry : new String[]{"readme.txt", "../mig_info_1.json"}) {
            java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
            try (java.util.zip.ZipOutputStream zip = new java.util.zip.ZipOutputStream(bytes)) {
                zip.putNextEntry(new java.util.zip.ZipEntry(entry));
                zip.write("{}".getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
            byte[] archive = bytes.toByteArray();
            assertThrows(ReleasePackageLoadRefusedException.class,
                    () -> uploads.receive(ADMIN, "10_0.zip", new ByteArrayInputStream(archive), archive.length));
        }
        // A file that does not begin like a zip is refused at its first bytes.
        byte[] script = "#!/bin/sh\nrm -rf /\n".getBytes(StandardCharsets.UTF_8);
        assertThrows(ReleasePackageLoadRefusedException.class,
                () -> uploads.receive(ADMIN, "10_0.zip", new ByteArrayInputStream(script), script.length));
    }

    @Test
    void onlyAnAdministratorSendsOrFetchesOrLooksAtAPackage() throws Exception {
        ReleasePackageLoadUploads.Upload upload = receive(servedPackage);

        assertThrows(AccessControlException.class,
                () -> uploads.receive(END_USER, "10_0.zip", new ByteArrayInputStream(servedPackage), servedPackage.length));
        assertThrows(AccessControlException.class, () -> uploads.get(END_USER, upload.id()));
        assertThrows(AccessControlException.class, () -> service.preflight(END_USER, upload.file()));
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Looking at it
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void aPackageThatHasNothingInTheWayIsToldToHaveNoneAndNothingIsChanged() throws Exception {
        ReleasePackageLoadUploads.Upload upload = receive(servedPackage);

        ReleasePackageLoadPreflightReport report = service.preflight(ADMIN, upload.file());

        assertEquals(List.of(), report.problems());
        assertTrue(report.ok());
        assertEquals("10.0", report.releaseNum());
        assertEquals("1.2", report.version());
        assertNotNull(report.format());
        assertEquals(List.of("dev-only-old", "dev-lib-old", "end-user-lib"), strings("SELECT name FROM oagi.library ORDER BY library_id"));
        assertEquals(List.of(), strings("SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'oagi\\_%'"));
        assertEquals("0", strings("SELECT COUNT(*) FROM release_package_load_job").get(0), "no load is recorded by looking");
    }

    @Test
    void aPackageOfAVersionThatIsNotLoadedIsToldSoWhereTheLoadWouldRefuseIt() throws Exception {
        ReleasePackageLoadUploads.Upload upload = receive(pack("9.0", latestSchemaVersion()));

        ReleasePackageLoadPreflightReport report = service.preflight(ADMIN, upload.file());

        assertFalse(report.ok());
        assertNull(report.format());
        assertEquals(1, report.problems().size(), report.problems().toString());
    }

    @Test
    void aTableThatIsNotNumberedFromTheNumberOfTheUsersIsNotInTheWayOfALoad() throws Exception {
        sql("ALTER TABLE `namespace` AUTO_INCREMENT = 10");
        ReleasePackageLoadUploads.Upload upload = receive(servedPackage);

        ReleasePackageLoadPreflightReport report = service.preflight(ADMIN, upload.file());

        assertTrue(report.ok(), report.problems().toString());
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Loading a package that has come
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void aPackageThatIsTakenIsLoadedAndIsNoMoreOnTheServerAsAPackageToLoad() throws Exception {
        ReleasePackageLoadUploads.Upload upload = receive(servedPackage);

        ReleasePackageLoadUploads.Upload taken = uploads.take(ADMIN, upload.id());
        MigrationLoadJobHolder.run(service, ADMIN, taken);

        assertThrows(NotFoundException.class, () -> uploads.get(ADMIN, upload.id()));
    }

    /** Starts the load of a package that is taken, and waits for it to end. */
    private static final class MigrationLoadJobHolder {
        static void run(ReleasePackageLoadCommandService service, ScoreUser requester, ReleasePackageLoadUploads.Upload taken) {
            var job = service.start(requester, taken.file(), taken.fileName());
            await().atMost(Duration.ofSeconds(180)).pollInterval(Duration.ofMillis(100))
                    .until(() -> !service.get(requester, job.id()).state().isActive());
            var done = service.get(requester, job.id());
            assertEquals(ReleasePackageLoadState.SUCCEEDED, done.state(), done.message());
        }
    }
}
