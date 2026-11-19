package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Executable;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Info;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Metadata;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Migration;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Release;

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

    private static final class InOrderHelper {
        static void assertAutoCommitOffThenOn(Connection con) throws Exception {
            var order = inOrder(con);
            order.verify(con).setAutoCommit(false);
            order.verify(con).setAutoCommit(true);
        }
    }
}
