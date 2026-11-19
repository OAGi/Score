package org.oagi.score.gateway.http.api.release_management.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.release_management.repository.flyway.FlywaySchemaMigrator;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * That the Flyway migrator goes by the Flyway of the application, which Spring has configured from {@code application.yml}
 * and the properties over it, and not by anything of its own: a context of the application's Flyway auto-configuration, with
 * the real {@code application.yml}, over a real MariaDB.
 */
@Testcontainers(disabledWithoutDocker = true)
class FlywaySchemaMigratorSpringConfigTest {

    @Container
    static final MariaDBContainer MARIADB = new MariaDBContainer("mariadb:11.8").withUsername("root");

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    private final List<String> made = new ArrayList<>();
    private final List<HikariDataSource> pools = new ArrayList<>();

    @AfterEach
    void drop() throws Exception {
        for (HikariDataSource pool : pools) {
            pool.close();
        }
        for (String schema : made) {
            sql("DROP DATABASE IF EXISTS `" + schema + "`");
        }
    }

    private static String url(String schema) {
        return "jdbc:mariadb://" + MARIADB.getHost() + ":" + MARIADB.getMappedPort(3306) + "/" + schema;
    }

    private String schema(String prefix) throws SQLException {
        String schema = prefix + "_" + SEQUENCE.incrementAndGet();
        made.add(schema);
        sql("CREATE DATABASE `" + schema + "`");
        return schema;
    }

    private void sql(String sql) throws SQLException {
        try (Connection con = DriverManager.getConnection(url("mysql"), "root", MARIADB.getPassword());
             Statement statement = con.createStatement()) {
            statement.execute(sql);
        }
    }

    private List<String> strings(String sql) throws SQLException {
        List<String> values = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url("mysql"), "root", MARIADB.getPassword());
             Statement statement = con.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                values.add(rs.getString(1));
            }
        }
        return values;
    }

    private HikariDataSource pool(String schema) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url(schema));
        config.setUsername("root");
        config.setPassword(MARIADB.getPassword());
        config.setMaximumPoolSize(3);
        HikariDataSource pool = new HikariDataSource(config);
        pools.add(pool);
        return pool;
    }

    /** The auto-configuration of Flyway over the real application.yml, with the properties given over it. */
    private ApplicationContextRunner application(HikariDataSource served, String... properties) {
        return new ApplicationContextRunner()
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .withConfiguration(AutoConfigurations.of(FlywayAutoConfiguration.class))
                .withBean(DataSource.class, () -> served)
                .withPropertyValues(properties);
    }

    private List<String> tablesOf(String schema) throws SQLException {
        return strings("SELECT table_name FROM information_schema.tables WHERE table_schema = '" + schema + "' ORDER BY table_name");
    }

    @Test
    void theMigrationsOfApplicationYmlAreApplied() throws Exception {
        HikariDataSource served = pool(schema("served"));
        String staging = schema("stg");

        // Nothing is given over application.yml, which says spring.flyway.locations: classpath:db/migration. (That is also
        // Flyway's own default of the application, so that anotherLocationInThePropertiesIsFollowed is what tells it is read.)
        application(served).run(context -> {
            Flyway flyway = context.getBean(Flyway.class);
            assertEquals(List.of("classpath:db/migration"),
                    java.util.Arrays.stream(flyway.getConfiguration().getLocations()).map(Object::toString).toList());

            new FlywaySchemaMigrator(served, flyway.getConfiguration()).migrate(staging, "3.4.0");
        });

        assertTrue(tablesOf(staging).contains("library"), "the baseline of db/migration was applied: " + tablesOf(staging));
        assertEquals("3.4.0", strings("SELECT MAX(version) FROM `" + staging + "`.flyway_schema_history WHERE success = 1").get(0));
    }

    @Test
    void anotherLocationInThePropertiesIsFollowed() throws Exception {
        HikariDataSource served = pool(schema("served"));
        String staging = schema("stg");

        application(served, "spring.flyway.locations=classpath:db/test-migration").run(context ->
                new FlywaySchemaMigrator(served, context.getBean(Flyway.class).getConfiguration())
                        .migrate(staging, null));

        assertEquals(List.of("flyway_schema_history", "spring_config_marker"), tablesOf(staging));
    }

    @Test
    void aSchemaThatHasTablesButNoHistoryIsRefusedWhateverTheBaselineSettingOfTheApplicationIs() throws Exception {
        HikariDataSource served = pool(schema("served"));
        String staging = schema("stg");
        sql("CREATE TABLE `" + staging + "`.something_else (x INT)");

        application(served, "spring.flyway.locations=classpath:db/test-migration", "spring.flyway.baseline-on-migrate=true").run(context -> {
            assertTrue(context.getBean(Flyway.class).getConfiguration().isBaselineOnMigrate(), "the application baselines");

            assertThrows(FlywayException.class, () ->
                    new FlywaySchemaMigrator(served, context.getBean(Flyway.class).getConfiguration())
                            .migrate(staging, null));
        });

        assertEquals(List.of("something_else"), tablesOf(staging), "nothing was done to it");
    }

    @Test
    void withoutAFlywayOfTheApplicationThereIsNothingToMigrateBy() throws Exception {
        HikariDataSource served = pool(schema("served"));

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new FlywaySchemaMigrator(served, null).migrate("any_schema", null));

        assertTrue(e.getMessage().contains("spring.flyway.enabled"), e.getMessage());
    }
}
