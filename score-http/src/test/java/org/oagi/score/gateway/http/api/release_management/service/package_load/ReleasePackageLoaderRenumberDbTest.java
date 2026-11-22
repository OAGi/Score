package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Info;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Metadata;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Migration;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Table;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The ids of a loaded release package moved in the log snapshots and the BIE paths, in a real MariaDB. */
@Testcontainers(disabledWithoutDocker = true)
class ReleasePackageLoaderRenumberDbTest {

    @Container
    static final MariaDBContainer MARIADB = new MariaDBContainer("mariadb:11.8").withUsername("root");

    private Connection con;

    @BeforeEach
    void tables() throws Exception {
        con = DriverManager.getConnection(MARIADB.getJdbcUrl(), "root", MARIADB.getPassword());
        try (Statement statement = con.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS `log`");
            statement.execute("DROP TABLE IF EXISTS `abie`");
            statement.execute("CREATE TABLE `log` (`log_id` BIGINT PRIMARY KEY, `snapshot` LONGTEXT)");
            statement.execute("CREATE TABLE `abie` (`abie_id` BIGINT PRIMARY KEY, `path` VARCHAR(1000))");
        }
    }

    @AfterEach
    void close() throws Exception {
        con.close();
    }

    private static Metadata metadata(long maxLogId) {
        Table log = new Table();
        log.setTableName("log");
        log.setMaxId(maxLogId);
        Metadata metadata = new Metadata();
        metadata.setTables(List.of(log));
        return metadata;
    }

    private void invoke(String name, Class<?>[] types, Object... args) throws Exception {
        Method method = ReleasePackageLoader.class.getDeclaredMethod(name, types);
        method.setAccessible(true);
        method.invoke(new ReleasePackageLoader(1000L, null, null, null), args);
    }

    private String one(String sql) throws Exception {
        try (Statement statement = con.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            rs.next();
            return rs.getString(1);
        }
    }

    @Test
    void anIdThatIsTheStartOfAnotherIdInALogSnapshotIsLeftAlone() throws Exception {
        try (Statement statement = con.createStatement()) {
            statement.execute("INSERT INTO `log` VALUES (1, '{\"dt_id\": 12, \"based_dt_id\": 12}'), "
                    + "(2, '{\"dt_id\": 123}'), (3, '{\"x\": 1, \"dt_id\": 12}'), (4, '{\"dt_id\": 12, \"to_asccp_manifest_id\": 5}')");
        }

        invoke("renumberSnapshot", new Class<?>[]{Connection.class, Metadata.class, String.class, long.class, long.class},
                con, metadata(0L), "dt_id", 12L, 1012L);
        invoke("renumberSnapshot", new Class<?>[]{Connection.class, Metadata.class, String.class, long.class, long.class},
                con, metadata(0L), "to_asccp_manifest_id", 5L, 1005L);

        assertEquals("{\"dt_id\": 1012, \"based_dt_id\": 12}", one("SELECT `snapshot` FROM `log` WHERE `log_id` = 1"));
        assertEquals("{\"dt_id\": 123}", one("SELECT `snapshot` FROM `log` WHERE `log_id` = 2"));
        assertEquals("{\"x\": 1, \"dt_id\": 1012}", one("SELECT `snapshot` FROM `log` WHERE `log_id` = 3"));
        assertEquals("{\"dt_id\": 1012, \"to_asccp_manifest_id\": 1005}", one("SELECT `snapshot` FROM `log` WHERE `log_id` = 4"));
    }

    @Test
    void theSnapshotsOfTheRecordsAlreadyInTheDatabaseAreNotTouched() throws Exception {
        try (Statement statement = con.createStatement()) {
            statement.execute("INSERT INTO `log` VALUES (1, '{\"dt_id\": 12}'), (2, '{\"dt_id\": 12}')");
        }

        invoke("renumberSnapshot", new Class<?>[]{Connection.class, Metadata.class, String.class, long.class, long.class},
                con, metadata(1L), "dt_id", 12L, 1012L);

        assertEquals("{\"dt_id\": 12}", one("SELECT `snapshot` FROM `log` WHERE `log_id` = 1"));
        assertEquals("{\"dt_id\": 1012}", one("SELECT `snapshot` FROM `log` WHERE `log_id` = 2"));
    }

    @Test
    void aSegmentOfABiePathThatIsTheStartOfAnotherSegmentIsLeftAlone() throws Exception {
        try (Statement statement = con.createStatement()) {
            statement.execute("INSERT INTO `abie` VALUES (1, 'ACC-3>ASCC-4>ACC-5>DT-12'), (2, 'ACC-3>DT-123'), "
                    + "(3, 'ACC-3>DT-12>DT_SC-12>DT-12'), (4, 'ACC-1>ACC-1>ACC-12')");
        }

        invoke("renumberPath", new Class<?>[]{Connection.class, String.class, String.class, long.class, long.class},
                con, "abie", "DT", 12L, 1012L);
        invoke("renumberPath", new Class<?>[]{Connection.class, String.class, String.class, long.class, long.class},
                con, "abie", "ACC", 1L, 1001L);

        assertEquals("ACC-3>ASCC-4>ACC-5>DT-1012", one("SELECT `path` FROM `abie` WHERE `abie_id` = 1"));
        assertEquals("ACC-3>DT-123", one("SELECT `path` FROM `abie` WHERE `abie_id` = 2"));
        assertEquals("ACC-3>DT-1012>DT_SC-12>DT-1012", one("SELECT `path` FROM `abie` WHERE `abie_id` = 3"));
        assertEquals("ACC-1001>ACC-1001>ACC-12", one("SELECT `path` FROM `abie` WHERE `abie_id` = 4"));
    }

    @Test
    void aLibraryTheDatabaseHasAndThePackageDoesNotIsMovedWithEverythingThatReferencesIt() throws Exception {
        try (Statement statement = con.createStatement()) {
            for (String table : List.of("library", "release", "namespace", "module_set", "bie_package")) {
                statement.execute("DROP TABLE IF EXISTS `" + table + "`");
                statement.execute("CREATE TABLE `" + table + "` (`" + table + "_id` BIGINT PRIMARY KEY"
                        + ("library".equals(table) ? "" : ", `library_id` BIGINT") + ")");
            }
            statement.execute("INSERT INTO `library` VALUES (1), (7)");
            for (String table : List.of("release", "namespace", "module_set", "bie_package")) {
                statement.execute("INSERT INTO `" + table + "` VALUES (1, 1), (2, 7)");
            }
        }
        Table library = new Table();
        library.setTableName("library");
        library.setMaxId(1L);
        Metadata metadata = new Metadata();
        metadata.setTables(List.of(library));
        Migration migration = new Migration();
        migration.setMetadata(metadata);
        Info info = new Info();
        info.setMigration(migration);

        invoke("relocatingLibrary", new Class<?>[]{Connection.class, Info.class}, con, info);

        assertEquals("1,1007", one("SELECT GROUP_CONCAT(`library_id` ORDER BY `library_id`) FROM `library`"));
        for (String table : List.of("release", "namespace", "module_set", "bie_package")) {
            assertEquals("1,1007", one("SELECT GROUP_CONCAT(`library_id` ORDER BY `" + table + "_id`) FROM `" + table + "`"));
        }
    }
}
