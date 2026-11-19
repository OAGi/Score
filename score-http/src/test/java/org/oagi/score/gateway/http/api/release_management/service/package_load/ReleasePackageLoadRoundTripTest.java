package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;

/**
 * The round trip on a database of a few records, which has what a baseline has and a release, the data of the developers and of the
 * users, in the characters that a script is most likely to get wrong. It runs always; the one on a real database is
 * {@link ReleasePackageLoadRoundTripRealDumpTest}.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@Import(ReleasePackageLoadRecordingWriteFreeze.Configuration.class)
class ReleasePackageLoadRoundTripTest extends AbstractRoundTripTest {

    @Container
    static final MariaDBContainer MARIADB = new MariaDBContainer("mariadb:11.8").withUsername("root").withDatabaseName("oagi");

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7.4").withExposedPorts(6379);

    private static Connection root;

    @DynamicPropertySource
    static void application(DynamicPropertyRegistry registry) throws SQLException {
        try (Connection admin = DriverManager.getConnection(url("mysql"), "root", MARIADB.getPassword()); Statement statement = admin.createStatement()) {
            statement.execute("CREATE USER IF NOT EXISTS 'oagi_app'@'%' IDENTIFIED BY 'app-secret'");
            statement.execute("GRANT ALL PRIVILEGES ON `oagi%`.* TO 'oagi_app'@'%'");
        }
        registry.add("DB_HOST", MARIADB::getHost);
        registry.add("DB_PORT", () -> MARIADB.getMappedPort(3306));
        registry.add("DB_DATABASE", () -> "oagi");
        registry.add("DB_USERNAME", () -> "oagi_app");
        registry.add("DB_PASSWORD", () -> "app-secret");
        registry.add("REDIS_HOST", REDIS::getHost);
        registry.add("REDIS_PORT", () -> REDIS.getMappedPort(6379));
    }

    private static String url(String schema) {
        return "jdbc:mariadb://" + MARIADB.getHost() + ":" + MARIADB.getMappedPort(3306) + "/" + schema;
    }

    @Override
    Connection root() throws SQLException {
        if (root == null || root.isClosed()) {
            root = DriverManager.getConnection(url("oagi"), "root", MARIADB.getPassword());
        }
        return root;
    }

    @Override
    Duration timeout() {
        return Duration.ofMinutes(5);
    }

    @AfterAll
    static void close() throws SQLException {
        if (root != null) {
            root.close();
        }
    }

    /** What the developers and the users made, with what is hard to write in a script: quotes, backslashes, new lines, and letters of every kind. */
    @BeforeEach
    void data() throws Exception {
        Connection con = root();
        try (Statement statement = con.createStatement()) {
            statement.execute("SET foreign_key_checks = 0");
            statement.execute("SET @disable_record_change_log = 1");
            for (String table : java.util.List.of("namespace", "release", "library", "tag")) {
                statement.execute("DELETE FROM `" + table + "`");
            }
            statement.execute("INSERT INTO library (library_id, name, created_by, last_updated_by, creation_timestamp, last_update_timestamp) VALUES "
                    + "(1, 'OAGIS', 1, 1, '2026-01-02 03:04:05.123456', '2026-01-02 03:04:05.123456'), "
                    + "(" + (DELTA + 1) + ", 'a user''s library', 1, 1, NOW(6), NOW(6))");
            statement.execute("INSERT INTO `release` (release_id, library_id, guid, release_num, created_by, last_updated_by, creation_timestamp, last_update_timestamp, state) VALUES "
                    + "(1, 1, 'guid-working', 'Working', 1, 1, NOW(6), NOW(6), 'Initialized'), (2, 1, 'guid-10', '10.0', 1, 1, NOW(6), NOW(6), 'Published')");
            statement.execute("INSERT INTO namespace (namespace_id, library_id, uri, prefix, description, is_std_nmsp, owner_user_id, created_by, last_updated_by, creation_timestamp, last_update_timestamp) VALUES "
                    + "(1, 1, 'http://www.openapplications.org/oagis/10', 'oagis', 'O''Reilly \"quoted\" back\\\\slash, new\nline, tab\t, 한글, ありがとう, ünïcödé', 1, 1, 1, 1, NOW(6), NOW(6)), "
                    + "(" + (DELTA + 1) + ", " + (DELTA + 1) + ", 'urn:users', NULL, NULL, 0, 1, 1, 1, NOW(6), NOW(6))");
            statement.execute("INSERT INTO tag (name, description, text_color, background_color, created_by, last_updated_by, creation_timestamp, last_update_timestamp) "
                    + "VALUES ('users''s tag', 'with ''quotes'' and \\ backslash', '#000', '#fff', 1, 1, NOW(6), NOW(6))");
            statement.execute("SET @disable_record_change_log = NULL");
            statement.execute("SET foreign_key_checks = 1");
        }
    }
}
