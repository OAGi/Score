package org.oagi.score.gateway.http.api.release_management.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DataSourceConnectionProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Info;
import org.oagi.score.gateway.http.api.release_management.service.package_load.ReleasePackageInfoLoader;
import org.oagi.score.gateway.http.api.release_management.service.package_load.script.ScriptSource;
import org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadCommandRepository.SwapOutcome;
import org.oagi.score.gateway.http.api.release_management.repository.flyway.FlywaySchemaMigrator;
import org.oagi.score.gateway.http.api.release_management.repository.jooq.JooqReleasePackageLoadCommandRepository;
import org.oagi.score.gateway.http.api.release_management.repository.jooq.JooqReleasePackageLoadQueryRepository;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What loads a release package into a schema of its own and swaps it in, against a MariaDB of the version the application
 * runs on: the schema of the application is made by its own migrations (a hundred tables, with triggers and foreign keys),
 * since it is what a swap has to move.
 */
@Testcontainers(disabledWithoutDocker = true)
class ReleasePackageLoadRepositoryDbTest {

    @Container
    static final MariaDBContainer MARIADB = new MariaDBContainer("mariadb:11.8").withUsername("root");

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    /** What Spring makes of spring.flyway.* of application.yml, as far as these tests need it (see FlywaySchemaMigratorSpringConfigTest). */
    private static final org.flywaydb.core.api.configuration.Configuration APPLICATION_FLYWAY =
            org.flywaydb.core.Flyway.configure().locations("classpath:db/migration").load().getConfiguration();

    /** The tables the loader asks the information file about. */
    private static final List<String> LOADER_TABLES = List.of("acc", "acc_manifest", "ascc", "ascc_manifest", "asccp",
            "asccp_manifest", "bcc", "bcc_manifest", "bccp", "bccp_manifest", "code_list", "code_list_manifest",
            "code_list_value", "code_list_value_manifest", "dt", "dt_manifest", "dt_sc", "dt_sc_manifest", "library",
            "log", "namespace", "seq_key");

    private final List<String> made = new ArrayList<>();
    private final List<HikariDataSource> pools = new ArrayList<>();
    private Connection root;

    @AfterEach
    void drop() throws Exception {
        for (HikariDataSource pool : pools) {
            pool.close();
        }
        for (String schema : made) {
            sql("DROP DATABASE IF EXISTS `" + schema + "`");
        }
        if (root != null) {
            root.close();
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // The schema the application serves, and the things around it
    // ---------------------------------------------------------------------------------------------------------------

    /** A schema the application serves: made by its own migrations, with the repositories of a connection to it. */
    private final class Served {
        final String schema = name("live");
        final ReleasePackageLoadCommandRepository command;
        final ReleasePackageLoadQueryRepository query;

        Served(boolean migrated) throws Exception {
            sql("CREATE DATABASE `" + schema + "`");
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url(schema));
            config.setUsername("root");
            config.setPassword(MARIADB.getPassword());
            config.setMaximumPoolSize(4);
            HikariDataSource pool = new HikariDataSource(config);
            pools.add(pool);
            DSLContext dsl = DSL.using(new DataSourceConnectionProvider(pool), SQLDialect.MARIADB);
            command = new JooqReleasePackageLoadCommandRepository(dsl, null, null,
                    new FlywaySchemaMigrator(pool, APPLICATION_FLYWAY));
            query = new JooqReleasePackageLoadQueryRepository(dsl, null, null);
            if (migrated) {
                command.migrateSchema(schema, null);
            }
        }
    }

    private String name(String prefix) {
        String name = prefix + "_" + SEQUENCE.incrementAndGet();
        made.add(name);
        return name;
    }

    private static String url(String schema) {
        return "jdbc:mariadb://" + MARIADB.getHost() + ":" + MARIADB.getMappedPort(3306) + "/" + schema;
    }

    private Connection root() throws SQLException {
        if (root == null) {
            root = DriverManager.getConnection(url("mysql"), "root", MARIADB.getPassword());
        }
        return root;
    }

    private void sql(String sql) throws SQLException {
        try (Statement statement = root().createStatement()) {
            statement.execute(sql);
        }
    }

    /** A statement with the foreign keys not checked, for the records a test sets up without the rest of what they refer to. */
    private void sqlWithoutKeys(String sql) throws SQLException {
        sql("SET foreign_key_checks = 0");
        try {
            sql(sql);
        } finally {
            sql("SET foreign_key_checks = 1");
        }
    }

    private List<String> strings(String sql) throws SQLException {
        List<String> values = new ArrayList<>();
        try (Statement statement = root().createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                values.add(rs.getString(1));
            }
        }
        return values;
    }

    private int count(String sql) throws SQLException {
        return Integer.parseInt(strings(sql).get(0));
    }

    private Map<String, String> triggers(String schema) throws SQLException {
        Map<String, String> triggers = new TreeMap<>();
        try (Statement statement = root().createStatement();
             ResultSet rs = statement.executeQuery("SELECT trigger_name, action_statement FROM information_schema.triggers WHERE trigger_schema = '" + schema + "'")) {
            while (rs.next()) {
                triggers.put(rs.getString(1), rs.getString(2));
            }
        }
        return triggers;
    }

    private int tables(String schema) throws SQLException {
        return count("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '" + schema + "' AND table_type = 'BASE TABLE'");
    }

    private void marker(String schema, String name) throws SQLException {
        sql("INSERT INTO `" + schema + "`.`configuration` (`name`, `type`, `value`) VALUES ('" + name + "', 'test', 'v')");
    }

    private List<String> markers(String schema) throws SQLException {
        return strings("SELECT `name` FROM `" + schema + "`.`configuration` WHERE `type` = 'test' ORDER BY `name`");
    }

    /** What the loader needs of a package, with the script it is to run. */
    private static Info packageInfo(String... knownReleaseIds) throws Exception {
        String tables = LOADER_TABLES.stream()
                .map(table -> "{\"table_name\": \"" + table + "\", \"max_id\": 0}")
                .collect(Collectors.joining(", "));
        String releases = java.util.Arrays.stream(knownReleaseIds)
                .map(id -> "{\"id\": " + id + ", \"release_num\": \"r" + id + "\"}")
                .collect(Collectors.joining(", "));
        String json = """
                {"version": "1.2", "maintainer": "test", "description": "A package of a test",
                 "migration": {"release": {"id": 77, "release_num": "10.0"},
                   "processes": [{"order": 1, "executable": {"script": "mig.sql"}}],
                   "metadata": {"releases": [%s], "tables": [%s]}}}
                """.formatted(releases, tables);
        return ReleasePackageInfoLoader.loadFromJson(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
    }

    private static ScriptSource script(String sql) {
        return new ScriptSource() {
            @Override
            public boolean exists(String script) {
                return true;
            }

            @Override
            public InputStream open(String script) {
                return new ByteArrayInputStream(sql.getBytes(StandardCharsets.UTF_8));
            }
        };
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Making, migrating, loading
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void aSchemaOfAnotherNameIsMadeByTheMigrationsAndIsTheSameAsTheOneTheApplicationServes() throws Exception {
        Served served = new Served(true);
        String staging = name("stg");

        served.command.createSchema(staging);
        served.command.migrateSchema(staging, null);

        assertEquals(List.of(), served.query.differences(served.schema, staging));
        assertEquals(served.schema, served.query.currentSchema(), "the schema the application serves is still the one it serves");
        assertTrue(tables(staging) > 100, "tables: " + tables(staging));
        assertFalse(triggers(staging).isEmpty());
        assertEquals(strings("SELECT version FROM `" + served.schema + "`.flyway_schema_history WHERE success = 1 ORDER BY installed_rank"),
                strings("SELECT version FROM `" + staging + "`.flyway_schema_history WHERE success = 1 ORDER BY installed_rank"));
    }

    @Test
    void aSchemaOfAnOlderVersionDiffersAndIsBroughtCurrentByTheRestOfTheMigrations() throws Exception {
        Served served = new Served(true);
        String staging = name("stg");
        served.command.createSchema(staging);

        served.command.migrateSchema(staging, "3.4.0");
        List<String> differences = served.query.differences(served.schema, staging);

        assertFalse(differences.isEmpty());
        assertTrue(differences.stream().anyMatch(d -> d.startsWith("only in " + served.schema + ": column library|is_default|")),
                "a column that V3_4_1 added is not in the older one: " + differences.stream().limit(5).toList());

        served.command.migrateSchema(staging, null);
        assertEquals(List.of(), served.query.differences(served.schema, staging));
    }

    @Test
    void aSchemaIsMadeOfTheCharacterSetOfTheServedOneAndNamesAreOfLettersAndDigits() throws Exception {
        Served served = new Served(false);
        String staging = name("stg");

        served.command.createSchema(staging);

        assertEquals(strings("SELECT CONCAT(default_character_set_name, '/', default_collation_name) FROM information_schema.schemata WHERE schema_name = '" + served.schema + "'"),
                strings("SELECT CONCAT(default_character_set_name, '/', default_collation_name) FROM information_schema.schemata WHERE schema_name = '" + staging + "'"));
        assertThrows(IllegalStateException.class, () -> served.command.createSchema(staging), "a schema that exists is not made again");
        assertThrows(IllegalArgumentException.class, () -> served.command.createSchema("x`; DROP DATABASE `" + served.schema));
        assertThrows(IllegalArgumentException.class, () -> served.command.dropSchema("a b"));
        assertThrows(IllegalArgumentException.class, () -> served.command.dropSchema(served.schema), "the schema that is served is not dropped");
        assertTrue(served.query.schemaExists(served.schema));

        served.command.dropSchema(staging);
        assertFalse(served.query.schemaExists(staging));
    }

    @Test
    void recordsAreCopiedByTheColumnsTheTwoHaveInCommonAndFromAValueOfTheKey() throws Exception {
        Served served = new Served(true);
        String staging = name("stg");
        served.command.createSchema(staging);
        served.command.migrateSchema(staging, "3.4.0"); // older: lacks library.is_default

        sqlWithoutKeys("INSERT INTO `" + served.schema + "`.library (library_id, name, is_default, created_by, last_updated_by, creation_timestamp, last_update_timestamp) VALUES "
                + "(5, 'developer', 1, 1, 1, NOW(), NOW()), (100000001, 'end user 1', 0, 1, 1, NOW(), NOW()), (100000002, 'end user 2', 0, 1, 1, NOW(), NOW())");

        int copied = served.command.copyRecords(served.schema, staging, "library", "library_id", BigInteger.valueOf(100000000));

        assertEquals(2, copied);
        assertEquals(List.of("end user 1", "end user 2"), strings("SELECT name FROM `" + staging + "`.library ORDER BY library_id"));

        sql("DELETE FROM `" + staging + "`.library");
        assertEquals(3, served.command.copyRecords(served.schema, staging, "library", null, null), "without a key, all are copied");
        assertEquals(List.of("developer", "end user 1", "end user 2"), strings("SELECT name FROM `" + staging + "`.library ORDER BY library_id"));
        assertThrows(IllegalArgumentException.class, () -> served.command.copyRecords(served.schema, staging, "no_such_table", null, null));
    }

    @Test
    void endUsersNumberedBelowTheDeltaAreGivenNewNumbersWhileTheyAreCopiedAndThePackagesAccountsAreLeftAlone() throws Exception {
        Served served = new Served(true);
        String staging = name("stg");
        served.command.createSchema(staging);
        served.command.migrateSchema(staging, null);
        long delta = 100000000L;

        // The package carries a developer numbered 5; the instance has an end user numbered 5, one numbered 6 and one that is numbered right.
        sqlWithoutKeys("INSERT INTO `" + staging + "`.app_user (app_user_id, login_id, name, is_developer, is_admin, is_enabled) VALUES (5, 'dev', 'Dev', 1, 0, 1)");
        sqlWithoutKeys("INSERT INTO `" + served.schema + "`.app_user (app_user_id, login_id, name, is_developer, is_admin, is_enabled) VALUES "
                + "(5, 'alice', 'Alice', 0, 0, 1), (6, 'bob', 'Bob', 0, 0, 1), (7, 'olddev', 'Old developer', 1, 0, 1), (100000003, 'carol', 'Carol', 0, 0, 1)");
        sqlWithoutKeys("INSERT INTO `" + served.schema + "`.biz_ctx (biz_ctx_id, guid, name, created_by, last_updated_by, creation_timestamp, last_update_timestamp) VALUES "
                + "(1, 'a1', 'by alice', 5, 6, NOW(), NOW()), (2, 'a2', 'by carol', 100000003, 100000003, NOW(), NOW()), (3, 'a3', 'by admin', 1, 1, NOW(), NOW())");

        Info info = packageInfo();
        org.oagi.score.gateway.http.api.release_management.service.package_load.ReleasePackageLoadCopier copier =
                new org.oagi.score.gateway.http.api.release_management.service.package_load.ReleasePackageLoadCopier(repositoriesOf(served));
        var format = new org.oagi.score.gateway.http.api.release_management.service.package_load.format.ReleasePackageFormatV1();
        var steps = copier.stepsToCopy(format, served.schema, staging);
        copier.copy(served.schema, staging, steps, delta, info, message -> {
        });

        assertEquals(List.of("sysadm", "admin", "dev", "carol", "alice", "bob"),
                strings("SELECT login_id FROM `" + staging + "`.app_user ORDER BY app_user_id"),
                "the end users are above the numbers present, the package's developer is where it was, and a developer of the instance is not carried");
        assertEquals("100000004", strings("SELECT app_user_id FROM `" + staging + "`.app_user WHERE login_id = 'alice'").get(0));
        assertEquals("100000005", strings("SELECT app_user_id FROM `" + staging + "`.app_user WHERE login_id = 'bob'").get(0));
        assertEquals("5", strings("SELECT app_user_id FROM `" + staging + "`.app_user WHERE login_id = 'dev'").get(0));
        assertEquals(List.of("100000004/100000005", "100000003/100000003", "1/1"),
                strings("SELECT CONCAT(created_by, '/', last_updated_by) FROM `" + staging + "`.biz_ctx ORDER BY biz_ctx_id"));
        assertEquals(List.of(), served.query.foreignKeyViolations(staging));
        assertEquals(List.of(), copier.missing(served.schema, staging, steps, delta, info));
        assertTrue(served.query.autoIncrement(staging, "app_user").orElseThrow().compareTo(BigInteger.valueOf(100000006)) >= 0);
        assertEquals(0, count("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '" + staging + "' AND table_name = 'release_package_load_id_map'"));
        assertEquals(List.of("alice", "bob", "carol"), strings("SELECT login_id FROM `" + served.schema + "`.app_user WHERE is_developer = 0 ORDER BY app_user_id").stream().sorted().toList(),
                "the served schema is not touched");
        assertEquals("5", strings("SELECT app_user_id FROM `" + served.schema + "`.app_user WHERE login_id = 'alice'").get(0));
    }

    private org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadRepositories repositoriesOf(Served served) {
        var repositories = org.mockito.Mockito.mock(ReleasePackageLoadRepositories.class);
        org.mockito.Mockito.when(repositories.command()).thenReturn(served.command);
        org.mockito.Mockito.when(repositories.query()).thenReturn(served.query);
        return repositories;
    }

    @Test
    void aPackageIsLoadedIntoTheStagingSchemaAndNotIntoTheServedOne() throws Exception {
        Served served = new Served(true);
        String staging = name("stg");
        served.command.createSchema(staging);
        served.command.migrateSchema(staging, null);
        List<String> reported = new ArrayList<>();

        served.command.load(staging, packageInfo("1", "2"),
                script("CREATE TABLE `load_marker` (`x` INT); INSERT INTO `load_marker` VALUES (7);"), 100000000L, reported::add);

        assertEquals(List.of("7"), strings("SELECT x FROM `" + staging + "`.load_marker"));
        assertEquals(0, count("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'load_marker' AND table_schema = '" + served.schema + "'"));
        assertEquals(served.schema, served.query.currentSchema());
        assertTrue(reported.contains("Completed."), reported.toString());
        assertEquals(1, count("SELECT @@foreign_key_checks"), "the session of the connection is put back");
    }

    @Test
    void aStagingSchemaWithAReleaseThePackageDoesNotKnowOfIsRefusedAndNothingIsLoaded() throws Exception {
        Served served = new Served(true);
        String staging = name("stg");
        served.command.createSchema(staging);
        served.command.migrateSchema(staging, null);
        sqlWithoutKeys("INSERT INTO `" + staging + "`.`release` (release_id, library_id, guid, release_num, created_by, last_updated_by, creation_timestamp, last_update_timestamp) "
                + "VALUES (9, 1, 'g9', '99.9', 1, 1, NOW(), NOW())");

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> served.command.load(
                staging, packageInfo("1", "2"), script("CREATE TABLE `load_marker` (`x` INT);"), 100000000L, null));

        assertTrue(e.getMessage().contains("99.9"), e.getMessage());
        assertEquals(0, count("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'load_marker'"));
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Swapping
    // ---------------------------------------------------------------------------------------------------------------

    private record Pair(Served served, String staging, String backup) {
    }

    /** A served schema with a record of its own, and a staging schema (made the same way) with another. */
    private Pair twoSchemas() throws Exception {
        Served served = new Served(true);
        String staging = name("stg");
        String backup = name("bak");
        served.command.createSchema(staging);
        served.command.migrateSchema(staging, null);
        served.command.createSchema(backup);
        marker(served.schema, "of-served");
        marker(staging, "of-staging");
        return new Pair(served, staging, backup);
    }

    @Test
    void theStagingSchemaIsPutInThePlaceOfTheServedOneAndTheTriggersAndTheFunctionGoWithIt() throws Exception {
        Pair p = twoSchemas();
        Map<String, String> stagingTriggers = triggers(p.staging());
        Map<String, String> servedTriggers = triggers(p.served().schema);
        int tablesOfServed = tables(p.served().schema);
        int routines = count("SELECT COUNT(*) FROM information_schema.routines WHERE routine_schema = '" + p.staging() + "'");
        assertTrue(stagingTriggers.size() > 50, "triggers: " + stagingTriggers.size());

        SwapOutcome outcome = p.served().command.swap(p.staging(), p.backup(), Duration.ofSeconds(10));

        assertEquals(tablesOfServed, outcome.tables());
        assertEquals(stagingTriggers.size(), outcome.triggers());
        assertEquals(List.of("of-staging"), markers(p.served().schema), "the served schema now has what the staging schema had");
        assertEquals(List.of("of-served"), markers(p.backup()), "and what it had is in the backup schema");
        assertEquals(stagingTriggers, triggers(p.served().schema), "with the triggers of the staging schema");
        assertEquals(servedTriggers, triggers(p.backup()), "the backup schema has the triggers of the tables it holds");
        assertEquals(routines, count("SELECT COUNT(*) FROM information_schema.routines WHERE routine_schema = '" + p.backup() + "'"),
                "and the routines");
        assertEquals(0, tables(p.staging()), "nothing is left in the staging schema");
        assertEquals(routines, count("SELECT COUNT(*) FROM information_schema.routines WHERE routine_schema = '" + p.served().schema + "'"));
        assertEquals(tablesOfServed, tables(p.backup()), "the tables of the served schema");
        assertEquals(p.served().schema, p.served().query.currentSchema());
        // The connection of the application is of the served schema, and what it asks is what the staging schema had.
        assertEquals(List.of("of-staging"), markers(p.served().schema));
        // The foreign keys went with the tables, on both sides.
        assertThrows(SQLException.class, () -> sql("INSERT INTO `" + p.served().schema + "`.namespace (library_id, uri, owner_user_id, created_by, last_updated_by, creation_timestamp, last_update_timestamp) VALUES (999999, 'u', 1, 1, 1, NOW(), NOW())"));
        assertThrows(SQLException.class, () -> sql("INSERT INTO `" + p.backup() + "`.namespace (library_id, uri, owner_user_id, created_by, last_updated_by, creation_timestamp, last_update_timestamp) VALUES (999999, 'u', 1, 1, 1, NOW(), NOW())"));
    }

    @Test
    void aSwapIsUndoneAndTheServedSchemaIsAsItWasWithItsTriggersAndItsFunction() throws Exception {
        Pair p = twoSchemas();
        Map<String, String> before = triggers(p.served().schema);
        Map<String, String> stagingTriggers = triggers(p.staging());
        int routines = count("SELECT COUNT(*) FROM information_schema.routines WHERE routine_schema = '" + p.served().schema + "'");
        String discarded = name("discarded");
        p.served().command.createSchema(discarded);
        p.served().command.swap(p.staging(), p.backup(), Duration.ofSeconds(10));
        marker(p.served().schema, "after-the-swap");

        SwapOutcome outcome = p.served().command.rollbackSwap(p.backup(), discarded, Duration.ofSeconds(10));

        assertEquals(before.size(), outcome.triggers());
        assertEquals(List.of("of-served"), markers(p.served().schema));
        assertEquals(before, triggers(p.served().schema));
        assertEquals(routines, count("SELECT COUNT(*) FROM information_schema.routines WHERE routine_schema = '" + p.served().schema + "'"));
        assertEquals(List.of("after-the-swap", "of-staging"), markers(discarded), "what was swapped in, and what was written to it, is kept apart");
        assertEquals(0, tables(p.backup()), "nothing is left in the backup schema");
        assertEquals(Map.of(), triggers(p.backup()));
        assertEquals(stagingTriggers, triggers(discarded), "what was swapped in is kept apart whole, with its triggers");
        assertEquals(routines, count("SELECT COUNT(*) FROM information_schema.routines WHERE routine_schema = '" + discarded + "'"));
        assertEquals(p.served().schema, p.served().query.currentSchema());
    }

    @Test
    void aSchemaThatHoldsNoSwapCannotBeRolledBackTo() throws Exception {
        Pair p = twoSchemas();
        String discarded = name("discarded");
        p.served().command.createSchema(discarded);

        assertThrows(SchemaSwapException.class, () -> p.served().command.rollbackSwap(p.backup(), discarded, Duration.ofSeconds(5)));
        assertEquals(List.of("of-served"), markers(p.served().schema));
    }

    @Test
    void aStagingSchemaThatLacksATableOfTheServedOneIsRefusedAndNothingIsChanged() throws Exception {
        Pair p = twoSchemas();
        sql("CREATE TABLE `" + p.served().schema + "`.only_in_served (x INT)");
        Map<String, String> before = triggers(p.served().schema);

        SchemaSwapException e = assertThrows(SchemaSwapException.class,
                () -> p.served().command.swap(p.staging(), p.backup(), Duration.ofSeconds(5)));

        assertTrue(e.getMessage().contains("only_in_served"), e.getMessage());
        assertEquals(before, triggers(p.served().schema));
        assertEquals(0, tables(p.backup()));
    }

    @Test
    void aBackupSchemaThatIsNotEmptyIsRefused() throws Exception {
        Pair p = twoSchemas();
        sql("CREATE TABLE `" + p.backup() + "`.left_over (x INT)");

        assertThrows(SchemaSwapException.class, () -> p.served().command.swap(p.staging(), p.backup(), Duration.ofSeconds(5)));
        assertEquals(List.of("of-served"), markers(p.served().schema));
    }

    @Test
    void theThreeSchemasOfASwapAreThree() throws Exception {
        Pair p = twoSchemas();

        assertThrows(IllegalArgumentException.class, () -> p.served().command.swap(p.served().schema, p.backup(), Duration.ofSeconds(5)));
        assertThrows(IllegalArgumentException.class, () -> p.served().command.swap(p.staging(), p.staging(), Duration.ofSeconds(5)));
        assertThrows(IllegalArgumentException.class, () -> p.served().command.swap(p.staging(), p.served().schema, Duration.ofSeconds(5)));
    }

    @Test
    void aSwapThatCannotGetTheLockOfATableLeavesTheSchemasAsTheyWereAndCanBeTriedAgain() throws Exception {
        Pair p = twoSchemas();
        Map<String, String> servedTriggers = triggers(p.served().schema);
        Map<String, String> stagingTriggers = triggers(p.staging());

        // A transaction of another connection holds a table of the served schema, so a trigger of it cannot be dropped.
        try (Connection other = DriverManager.getConnection(url("mysql"), "root", MARIADB.getPassword())) {
            other.setAutoCommit(false);
            try (Statement statement = other.createStatement()) {
                statement.executeQuery("SELECT COUNT(*) FROM `" + p.served().schema + "`.acc").close();

                SchemaSwapException e = assertThrows(SchemaSwapException.class,
                        () -> p.served().command.swap(p.staging(), p.backup(), Duration.ofSeconds(1)));

                assertTrue(e.getMessage().contains("as they were"), e.getMessage());
            }
            other.rollback();
        }

        assertEquals(servedTriggers, triggers(p.served().schema), "the triggers that were dropped are made again");
        assertEquals(stagingTriggers, triggers(p.staging()));
        assertEquals(List.of("of-served"), markers(p.served().schema));
        assertEquals(0, tables(p.backup()), "and the record of the swap is gone");

        p.served().command.swap(p.staging(), p.backup(), Duration.ofSeconds(10));
        assertEquals(List.of("of-staging"), markers(p.served().schema));
    }

    @Test
    void anUndoThatCannotGetTheLockOfATableLeavesTheSchemasAsTheyWereAndCanBeTriedAgain() throws Exception {
        Pair p = twoSchemas();
        Map<String, String> servedTriggers = triggers(p.served().schema);
        String discarded = name("discarded");
        p.served().command.createSchema(discarded);
        p.served().command.swap(p.staging(), p.backup(), Duration.ofSeconds(10));
        Map<String, String> swappedIn = triggers(p.served().schema);

        try (Connection other = DriverManager.getConnection(url("mysql"), "root", MARIADB.getPassword())) {
            other.setAutoCommit(false);
            try (Statement statement = other.createStatement()) {
                statement.executeQuery("SELECT COUNT(*) FROM `" + p.served().schema + "`.acc").close();

                SchemaSwapException e = assertThrows(SchemaSwapException.class,
                        () -> p.served().command.rollbackSwap(p.backup(), discarded, Duration.ofSeconds(1)));

                assertTrue(e.getMessage().contains("as they were"), e.getMessage());
            }
            other.rollback();
        }

        assertEquals(swappedIn, triggers(p.served().schema), "the triggers that were dropped are made again");
        assertEquals(servedTriggers, triggers(p.backup()));
        assertEquals(List.of("of-staging"), markers(p.served().schema));
        assertEquals(0, tables(discarded));

        p.served().command.rollbackSwap(p.backup(), discarded, Duration.ofSeconds(10));
        assertEquals(List.of("of-served"), markers(p.served().schema));
        assertEquals(servedTriggers, triggers(p.served().schema));
    }

    @Test
    void anUndoCanBeUndoneAndTheSwapDoneAgainWithNothingLostOnTheWay() throws Exception {
        Pair p = twoSchemas();
        Map<String, String> servedTriggers = triggers(p.served().schema);
        Map<String, String> stagingTriggers = triggers(p.staging());
        String discarded = name("discarded");
        p.served().command.createSchema(discarded);

        p.served().command.swap(p.staging(), p.backup(), Duration.ofSeconds(10));
        p.served().command.rollbackSwap(p.backup(), discarded, Duration.ofSeconds(10));
        // The undo is the same thing as the swap, so what it put aside can be swapped in again, into the schema that was emptied.
        p.served().command.swap(discarded, p.backup(), Duration.ofSeconds(10));

        assertEquals(List.of("of-staging"), markers(p.served().schema));
        assertEquals(stagingTriggers, triggers(p.served().schema));
        assertEquals(List.of("of-served"), markers(p.backup()));
        assertEquals(servedTriggers, triggers(p.backup()));
        assertEquals(0, tables(discarded));
    }

    @Test
    void aTriggerThatNamesTheSchemaOfTheBackupSideIsRefusedToo() throws Exception {
        Pair p = twoSchemas();
        String discarded = name("discarded");
        p.served().command.createSchema(discarded);
        p.served().command.swap(p.staging(), p.backup(), Duration.ofSeconds(10));
        Map<String, String> before = triggers(p.served().schema);
        // A trigger of the schema that is to be swapped back in, which names its own schema.
        sql("CREATE TABLE `" + p.backup() + "`.names_itself (x INT)");
        sql("CREATE TRIGGER `" + p.backup() + "`.names_itself_bi BEFORE INSERT ON `" + p.backup() + "`.names_itself FOR EACH ROW SET @x = (SELECT COUNT(*) FROM `" + p.backup() + "`.names_itself)");

        SchemaSwapException e = assertThrows(SchemaSwapException.class,
                () -> p.served().command.rollbackSwap(p.backup(), discarded, Duration.ofSeconds(5)));

        assertTrue(e.getMessage().contains("names_itself_bi"), e.getMessage());
        assertEquals(before, triggers(p.served().schema));
        assertEquals(0, tables(discarded));
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Tables that stay, numbering, and what tells that something was written
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void aTableThatStaysInPlaceIsMovedNeitherBySwapNorByTheUndo() throws Exception {
        Pair p = twoSchemas();
        String job = "release_package_load_job";
        String insert = "INSERT INTO `%s`.release_package_load_job (state, package_file_name, owner_user_id, creation_timestamp, last_update_timestamp) VALUES ('%s', 'p.zip', 1, NOW(), NOW())";
        sql(insert.formatted(p.served().schema, "SWAPPING"));
        String discarded = name("discarded");
        p.served().command.createSchema(discarded);
        int servedTables = tables(p.served().schema);

        SwapOutcome outcome = p.served().command.swap(p.staging(), p.backup(), Duration.ofSeconds(10), List.of(job));

        assertEquals(servedTables - 1, outcome.tables(), "every table but the one that stays");
        assertEquals(List.of("SWAPPING"), strings("SELECT state FROM `" + p.served().schema + "`.release_package_load_job"), "it is what it was");
        assertEquals(0, count("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '" + p.backup() + "' AND table_name = '" + job + "'"),
                "the backup schema does not have it");
        assertEquals(List.of(), strings("SELECT state FROM `" + p.staging() + "`.release_package_load_job"), "that of the staging schema stays in it");
        assertEquals(List.of("of-staging"), markers(p.served().schema));

        sql("UPDATE `" + p.served().schema + "`.release_package_load_job SET state = 'SUCCEEDED'");
        p.served().command.rollbackSwap(p.backup(), discarded, Duration.ofSeconds(10), List.of(job));

        assertEquals(List.of("SUCCEEDED"), strings("SELECT state FROM `" + p.served().schema + "`.release_package_load_job"), "and the undo leaves it too");
        assertEquals(List.of("of-served"), markers(p.served().schema));
        assertEquals(0, count("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '" + discarded + "' AND table_name = '" + job + "'"));
    }

    @Test
    void aTableThatStaysInPlaceMayNotHaveTriggersAndNothingIsChanged() throws Exception {
        Pair p = twoSchemas();
        Map<String, String> before = triggers(p.served().schema);

        SchemaSwapException e = assertThrows(SchemaSwapException.class,
                () -> p.served().command.swap(p.staging(), p.backup(), Duration.ofSeconds(5), List.of("tag")));

        assertTrue(e.getMessage().contains("triggers"), e.getMessage());
        assertEquals(before, triggers(p.served().schema));
        assertEquals(0, tables(p.backup()));
    }

    @Test
    void theNumberingOfATableGoesOnFromWhereItIsFurtherAndNeverGoesBack() throws Exception {
        Pair p = twoSchemas();
        sql("ALTER TABLE `" + p.served().schema + "`.configuration AUTO_INCREMENT = 5000");

        p.served().command.copyAutoIncrement(p.served().schema, p.staging(), "configuration");
        assertEquals(java.math.BigInteger.valueOf(5000), p.served().query.autoIncrement(p.staging(), "configuration").orElseThrow());

        sql("ALTER TABLE `" + p.served().schema + "`.configuration AUTO_INCREMENT = 10");
        p.served().command.copyAutoIncrement(p.served().schema, p.staging(), "configuration");
        assertEquals(java.math.BigInteger.valueOf(5000), p.served().query.autoIncrement(p.staging(), "configuration").orElseThrow());
    }

    @Test
    void aTableIsEmptiedWhateverRefersToItAndTheRecordsAreCounted() throws Exception {
        Pair p = twoSchemas();
        long users = p.served().query.countRecords(p.staging(), "app_user", null, null);
        assertTrue(users > 0, "the baseline has users");
        assertEquals(0, p.served().query.countRecords(p.staging(), "app_user", "app_user_id", java.math.BigInteger.valueOf(100000000)));

        assertEquals(users, p.served().command.emptyTable(p.staging(), "app_user"));

        assertEquals(0, p.served().query.countRecords(p.staging(), "app_user", null, null));
        assertTrue(p.served().query.tables(p.staging()).contains("app_user"));
        assertFalse(p.served().query.tables(p.staging()).contains("no_such_table"));
    }

    @Test
    void whatIsWrittenToATableChangesItsFingerprint() throws Exception {
        Pair p = twoSchemas();
        String before = p.served().query.fingerprint(p.served().schema, "library");

        sqlWithoutKeys("INSERT INTO `" + p.served().schema + "`.library (library_id, name, created_by, last_updated_by, creation_timestamp, last_update_timestamp) VALUES (100000001, 'x', 1, 1, NOW(), '2026-01-01 00:00:00')");
        String inserted = p.served().query.fingerprint(p.served().schema, "library");
        sql("UPDATE `" + p.served().schema + "`.library SET last_update_timestamp = '2026-02-02 00:00:00'");
        String updated = p.served().query.fingerprint(p.served().schema, "library");

        assertEquals(before, p.served().query.fingerprint(p.staging(), "library"), "two empty tables say the same");
        assertNotEquals(before, inserted);
        assertNotEquals(inserted, updated);
        assertEquals(inserted, p.served().query.fingerprint(p.served().schema, "library").replace("2026-02-02", "2026-01-01"));
        assertThrows(IllegalArgumentException.class, () -> p.served().query.fingerprint(p.served().schema, "library`; --"));
    }

    @Test
    void recordsBelowAKeyAreCountedRemovedAndCopied() throws Exception {
        Pair p = twoSchemas();
        sql("UPDATE `" + p.served().schema + "`.app_user SET name = 'Changed Administrator' WHERE app_user_id = 1");
        java.math.BigInteger two = java.math.BigInteger.TWO;
        long below = p.served().query.countRecordsBelow(p.served().schema, "app_user", "app_user_id", two);
        assertTrue(below > 0, "the baseline has the first account");
        assertEquals(below, p.served().query.countRecordsBelow(p.staging(), "app_user", "app_user_id", two));
        assertNotEquals(List.of("Changed Administrator"), strings("SELECT name FROM `" + p.staging() + "`.app_user WHERE app_user_id = 1"));

        assertEquals(below, p.served().command.removeRecordsBelow(p.staging(), "app_user", "app_user_id", two));
        assertEquals(0, p.served().query.countRecordsBelow(p.staging(), "app_user", "app_user_id", two));
        assertEquals(below, p.served().command.copyRecordsBelow(p.served().schema, p.staging(), "app_user", "app_user_id", two));

        assertEquals(List.of("Changed Administrator"), strings("SELECT name FROM `" + p.staging() + "`.app_user WHERE app_user_id = 1"));
        assertEquals(strings("SELECT app_user_id FROM `" + p.served().schema + "`.app_user WHERE app_user_id < 2 ORDER BY 1"),
                strings("SELECT app_user_id FROM `" + p.staging() + "`.app_user WHERE app_user_id < 2 ORDER BY 1"),
                "the account that is numbered 0 keeps its number");
        assertEquals(List.of("0", "1"), strings("SELECT app_user_id FROM `" + p.staging() + "`.app_user WHERE app_user_id < 2 ORDER BY 1"));
        assertThrows(IllegalArgumentException.class, () -> p.served().command.removeRecordsBelow(p.staging(), "app_user", "x`; --", two));
    }
}
