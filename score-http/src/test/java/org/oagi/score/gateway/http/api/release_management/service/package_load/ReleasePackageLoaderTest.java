package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Executable;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Info;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Metadata;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Migration;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Release;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Table;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ReleasePackageLoaderTest {

    private static Info info(long... knownReleaseIds) {
        Info info = new Info();
        Migration migration = new Migration();
        Metadata metadata = new Metadata();
        List<Release> releases = new ArrayList<>();
        for (long id : knownReleaseIds) {
            Release release = new Release();
            release.setId(id);
            release.setReleaseNum("r" + id);
            releases.add(release);
        }
        metadata.setReleases(releases);
        metadata.setTables(List.of());
        migration.setMetadata(metadata);
        migration.setProcesses(List.<Executable>of());
        info.setMigration(migration);
        return info;
    }

    @Test
    void aDatabaseWithAReleaseThePackageDoesNotKnowOfIsRefusedBeforeAnythingIsChanged() throws Exception {
        Connection con = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet rs = mock(ResultSet.class);
        when(con.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getLong("release_id")).thenReturn(1L, 9L);
        // Only a release the package does not know of is named, so the name is asked for once.
        when(rs.getString("release_num")).thenReturn("99.9");
        List<String> reported = new ArrayList<>();

        ReleasePackageLoader loader = new ReleasePackageLoader(100000000L, null, reported::add, null);
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> loader.run(con, info(1, 2)));

        assertTrue(e.getMessage().contains("99.9"), e.getMessage());
        assertFalse(e.getMessage().contains("10.6"), e.getMessage());
        assertTrue(reported.isEmpty());
        verify(con).rollback();
        verify(con, never()).commit();
        // Nothing of the load was run: the only statements are the read of the releases and the reset of the session.
        verify(statement, never()).execute("SET @disable_record_change_log = 1");
    }

    @Test
    void theAutoCommitOfTheConnectionIsGivenBack() throws Exception {
        Connection con = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet rs = mock(ResultSet.class);
        when(con.getAutoCommit()).thenReturn(true);
        when(con.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getLong("release_id")).thenReturn(9L);
        when(rs.getString("release_num")).thenReturn("99.9");

        assertThrows(IllegalStateException.class, () -> new ReleasePackageLoader(100000000L, null, null, null).run(con, info(1)));

        InOrderHelper.assertAutoCommitOffThenOn(con);
    }

    private static List<String> statementsOf(String relocating, String idColumn, String manifestIdColumn, String table, String manifestTable) throws Exception {
        Info info = info();
        List<Table> tables = new ArrayList<>();
        for (String name : List.of(table, manifestTable, "log")) {
            Table t = new Table();
            t.setTableName(name);
            t.setMaxId(0L);
            tables.add(t);
        }
        info.getMigration().getMetadata().setTables(tables);

        Connection con = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet ids = mock(ResultSet.class);
        ResultSet manifestIds = mock(ResultSet.class);
        when(con.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenAnswer(inv ->
                ((String) inv.getArgument(0)).contains("`" + manifestTable + "`.`" + manifestIdColumn + "`") ? manifestIds : ids);
        when(ids.next()).thenReturn(true, false);
        when(ids.getLong(idColumn)).thenReturn(5L);
        when(manifestIds.next()).thenReturn(true, false);
        when(manifestIds.getLong(manifestIdColumn)).thenReturn(7L);

        Method method = ReleasePackageLoader.class.getDeclaredMethod(relocating, Connection.class, Info.class);
        method.setAccessible(true);
        method.invoke(new ReleasePackageLoader(1000L, null, null, null), con, info);

        var captor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(statement, atLeast(0)).execute(captor.capture());
        return captor.getAllValues();
    }

    @Test
    void theToAsccpManifestIdKeyOfALogSnapshotKeepsItsNameWhenTheIdIsRelocated() throws Exception {
        List<String> sql = statementsOf("relocatingAsccp", "asccp_id", "asccp_manifest_id", "asccp", "asccp_manifest");

        assertTrue(sql.stream().anyMatch(s -> s.contains("REPLACE(`snapshot`, '\"to_asccp_manifest_id\": 7', '\"to_asccp_manifest_id\": 1007')")), sql.toString());
        assertTrue(sql.stream().noneMatch(s -> s.contains("from_asccp_manifest_id")), sql.toString());
    }

    @Test
    void theToBccpManifestIdKeyOfALogSnapshotKeepsItsNameWhenTheIdIsRelocated() throws Exception {
        List<String> sql = statementsOf("relocatingBccp", "bccp_id", "bccp_manifest_id", "bccp", "bccp_manifest");

        assertTrue(sql.stream().anyMatch(s -> s.contains("REPLACE(`snapshot`, '\"to_bccp_manifest_id\": 7', '\"to_bccp_manifest_id\": 1007')")), sql.toString());
        assertTrue(sql.stream().noneMatch(s -> s.contains("from_bccp_manifest_id")), sql.toString());
    }

    private static final class InOrderHelper {
        static void assertAutoCommitOffThenOn(Connection con) throws Exception {
            var order = inOrder(con);
            order.verify(con).setAutoCommit(false);
            order.verify(con).setAutoCommit(true);
        }
    }
}
