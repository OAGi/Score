package org.oagi.score.gateway.http.api.release_management.service.migrationload;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Container.ExecResult;
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
 * The round trip on a real database: the dump of an instance (the file named by the environment variable SCORE_ROUNDTRIP_DUMP, a
 * dump of the database as mariadb-dump writes it) is restored into a MariaDB made for the test, the application is started on it
 * (which migrates it, as it does a database that was made before), and the package the application writes of it is loaded back into it.
 * It takes long (the packages of an instance are some hundred megabytes), so it runs only when asked for:
 *
 * <pre>SCORE_ROUNDTRIP_DUMP=/path/to/dump.sql mvn test -Dtest=MigrationLoadRoundTripRealDumpTest</pre>
 */
@EnabledIfEnvironmentVariable(named = "SCORE_ROUNDTRIP_DUMP", matches = ".+")
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@Import(RecordingWriteFreeze.Configuration.class)
class MigrationLoadRoundTripRealDumpTest extends AbstractRoundTripTest {

    private static String dump() {
        String path = System.getenv("SCORE_ROUNDTRIP_DUMP");
        return path == null || path.isBlank() ? "/dev/null" : path;
    }

    // A package has statements of a thousand records each, and a record of a file of the modules is large.
    @Container
    static final MariaDBContainer MARIADB = new MariaDBContainer("mariadb:11.8").withUsername("root").withDatabaseName("oagi")
            .withFileSystemBind(dump(), "/dump.sql", BindMode.READ_ONLY)
            .withCommand("--max-allowed-packet=1G", "--innodb-buffer-pool-size=2G", "--innodb-flush-log-at-trx-commit=2", "--skip-log-bin");

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7.4").withExposedPorts(6379);

    private static Connection root;

    @DynamicPropertySource
    static void application(DynamicPropertyRegistry registry) throws Exception {
        try (Connection admin = DriverManager.getConnection(url("mysql"), "root", MARIADB.getPassword()); Statement statement = admin.createStatement()) {
            // The triggers of the dump are of the user the instance ran as.
            statement.execute("CREATE USER IF NOT EXISTS 'oagi'@'%' IDENTIFIED BY 'unused'");
            statement.execute("CREATE USER IF NOT EXISTS 'oagi_app'@'%' IDENTIFIED BY 'app-secret'");
            statement.execute("GRANT ALL PRIVILEGES ON `oagi%`.* TO 'oagi_app'@'%'");
            statement.execute("GRANT ALL PRIVILEGES ON `oagi%`.* TO 'oagi'@'%'");
        }
        long started = System.currentTimeMillis();
        ExecResult restored = MARIADB.execInContainer("sh", "-c", "mariadb -uroot -p" + MARIADB.getPassword() + " oagi < /dump.sql");
        System.out.println("ROUNDTRIP dump restored in " + (System.currentTimeMillis() - started) / 1000 + " s: exit " + restored.getExitCode()
                + " " + restored.getStderr());
        if (restored.getExitCode() != 0) {
            throw new IllegalStateException("The dump was not restored: " + restored.getStderr());
        }
        registry.add("DB_HOST", MARIADB::getHost);
        registry.add("DB_PORT", () -> MARIADB.getMappedPort(3306));
        registry.add("DB_DATABASE", () -> "oagi");
        registry.add("DB_USERNAME", () -> "oagi_app");
        registry.add("DB_PASSWORD", () -> "app-secret");
        registry.add("REDIS_HOST", REDIS::getHost);
        registry.add("REDIS_PORT", () -> REDIS.getMappedPort(6379));
        registry.add("score.migration-load.enabled", () -> "true");
        registry.add("score.migration-load.lock-wait", () -> "PT10M");
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
        return Duration.ofHours(3);
    }

    @AfterAll
    static void close() throws SQLException {
        if (root != null) {
            root.close();
        }
    }
}
