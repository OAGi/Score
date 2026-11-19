package org.oagi.score.gateway.http.api.release_management.repository;

import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The whole application, started on a MariaDB and a Redis that are made for the test (nothing of the developer's own is
 * touched: the application flushes Redis as it starts), that the repositories of the load are wired the way the application
 * runs: the data source and the Flyway of the application are given to the Flyway migrator, and what it makes is a schema
 * that is the same as the one the application migrated itself, from empty, as it started.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class ReleasePackageLoadApplicationBootTest {

    @Container
    static final MariaDBContainer MARIADB = new MariaDBContainer("mariadb:11.8").withUsername("root").withDatabaseName("oagi");

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7.4").withExposedPorts(6379);

    /**
     * The user of the application, which has what a deployment would give it: all privileges on the schemas whose name starts
     * with that of the application's own ({@code oagi%}), which are those the load makes, and nothing else.
     */
    private static final String APPLICATION_USER = "oagi_app";
    private static final String APPLICATION_PASSWORD = "app-secret";

    private static void createApplicationUser() {
        try (java.sql.Connection root = java.sql.DriverManager.getConnection(
                "jdbc:mariadb://" + MARIADB.getHost() + ":" + MARIADB.getMappedPort(3306) + "/mysql", "root", MARIADB.getPassword());
             java.sql.Statement statement = root.createStatement()) {
            statement.execute("CREATE USER IF NOT EXISTS '" + APPLICATION_USER + "'@'%' IDENTIFIED BY '" + APPLICATION_PASSWORD + "'");
            statement.execute("GRANT ALL PRIVILEGES ON `oagi%`.* TO '" + APPLICATION_USER + "'@'%'");
        } catch (java.sql.SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void application(DynamicPropertyRegistry registry) {
        createApplicationUser();
        registry.add("DB_HOST", MARIADB::getHost);
        registry.add("DB_PORT", () -> MARIADB.getMappedPort(3306));
        registry.add("DB_DATABASE", () -> "oagi");
        registry.add("DB_USERNAME", () -> APPLICATION_USER);
        registry.add("DB_PASSWORD", () -> APPLICATION_PASSWORD);
        registry.add("REDIS_HOST", REDIS::getHost);
        registry.add("REDIS_PORT", () -> REDIS.getMappedPort(6379));
    }

    @Autowired
    private ReleasePackageLoadRepositories repositories;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private Flyway flyway;

    @Test
    void theApplicationStartsOnAnEmptyDatabaseAndTheLoadRepositoriesAreWiredOnItsFlywayAndItsDataSource() {
        ReleasePackageLoadQueryRepository query = repositories.query();
        ReleasePackageLoadCommandRepository command = repositories.command();
        assertEquals("oagi", query.currentSchema());
        assertNotNull(flyway.info().current(), "the application migrated the empty database as it started");

        command.createSchema("oagi_load_boot");
        try {
            command.migrateSchema("oagi_load_boot", null);

            assertEquals(List.of(), query.differences("oagi", "oagi_load_boot"),
                    "a schema the load made is the same as the one the application migrated");
            assertEquals("oagi", query.currentSchema(), "and the application is still on its own");
        } finally {
            command.dropSchema("oagi_load_boot");
        }
    }

    @Test
    void theUserOfThePatternGrantCannotTouchASchemaThatIsNotOfTheApplication() throws Exception {
        assertEquals(APPLICATION_USER, dsl.resultQuery("SELECT SUBSTRING_INDEX(USER(), '@', 1)").fetchOne(0, String.class),
                "the application runs as the user of the pattern grant, not as root");

        assertThrows(Exception.class, () -> dsl.execute("CREATE DATABASE someone_elses"));
        assertThrows(Exception.class, () -> dsl.execute("SELECT * FROM mysql.user"));
        assertEquals(List.of(), dsl.resultQuery("SELECT schema_name FROM information_schema.schemata WHERE schema_name = 'someone_elses'").fetch(0, String.class));
    }

    private int triggersOfTheApplication() {
        return dsl.resultQuery("SELECT COUNT(*) FROM information_schema.triggers WHERE trigger_schema = 'oagi'").fetchOne(0, Integer.class);
    }

    @Test
    void theApplicationGoesOnAfterItsSchemaIsSwappedAndAfterTheSwapIsUndone() {
        ReleasePackageLoadCommandRepository command = repositories.command();
        command.createSchema("oagi_load_swap");
        command.createSchema("oagi_backup_swap");
        command.createSchema("oagi_discarded_swap");
        try {
            command.migrateSchema("oagi_load_swap", null);
            dsl.execute("INSERT INTO oagi_load_swap.configuration (name, type, value) VALUES ('boot-test', 'test', 'staging')");
            int triggers = triggersOfTheApplication();
            assertTrue(triggers > 50, "triggers: " + triggers);

            command.swap("oagi_load_swap", "oagi_backup_swap", Duration.ofSeconds(30));

            assertEquals(List.of("staging"), dsl.resultQuery("SELECT value FROM configuration WHERE name = 'boot-test'").fetch(0, String.class),
                    "what the application reads is now what the staging schema had");
            assertEquals(triggers, triggersOfTheApplication(), "with its triggers");

            command.rollbackSwap("oagi_backup_swap", "oagi_discarded_swap", Duration.ofSeconds(30));

            assertEquals(List.of(), dsl.resultQuery("SELECT value FROM configuration WHERE name = 'boot-test'").fetch(0, String.class),
                    "and after the undo it reads what it read before");
            assertEquals(triggers, triggersOfTheApplication());
        } finally {
            command.dropSchema("oagi_load_swap");
            command.dropSchema("oagi_backup_swap");
            command.dropSchema("oagi_discarded_swap");
        }
    }
}
