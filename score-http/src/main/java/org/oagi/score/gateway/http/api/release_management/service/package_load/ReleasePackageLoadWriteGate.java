package org.oagi.score.gateway.http.api.release_management.service.package_load;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;

/**
 * The statements on the row that is the gate of the writers (the row {@code write_gate} of table {@code named_lock}): the writers of every process read
 * it with a shared lock that lasts until their transaction ends, and a load takes it with an exclusive lock. InnoDB makes the exclusive
 * request wait for the writers that are at work, and makes the writers that come after it wait behind it, so it is what lets
 * the whole application, in whatever process, stop writing for a while and go on after it.
 */
public final class ReleasePackageLoadWriteGate {

    /** The table of the named locks, which stays where it is when the schema is swapped. */
    public static final String TABLE = "named_lock";

    /** The row of the table that is the gate. */
    public static final String NAME = "write_gate";

    /** MariaDB: the lock wait timeout (ER_LOCK_WAIT_TIMEOUT). */
    private static final int LOCK_WAIT_TIMEOUT = 1205;
    /** MariaDB: the statement was ended by a wait on a lock that gave up with NOWAIT or WAIT (ER_LOCK_ABORTED). */
    private static final int LOCK_ABORTED = 1689;

    private ReleasePackageLoadWriteGate() {
    }

    /** A writer's way in: waits for as long as a load has the gate, and holds it (shared) until the transaction of the connection ends. */
    public static void enter(Connection connection, Duration wait) throws SQLException {
        run(connection, "SELECT lock_name FROM " + TABLE + " WHERE lock_name = '" + NAME + "' LOCK IN SHARE MODE WAIT " + seconds(wait));
    }

    /** A load's way in: waits for the writers that are at work, and holds the gate (exclusive) until the transaction of the connection ends. */
    public static void close(Connection connection, Duration wait) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT lock_name FROM " + TABLE + " WHERE lock_name = '" + NAME + "' FOR UPDATE WAIT " + seconds(wait))) {
            // A row that is not there locks nothing (only the gap where it would be, which the writers' own locks do not conflict with).
            if (!rs.next()) {
                throw new SQLException("The row '" + NAME + "' of the table " + TABLE + " is missing, so it cannot stop the writers.");
            }
        }
    }

    public static boolean isTimeout(SQLException e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && (sql.getErrorCode() == LOCK_WAIT_TIMEOUT || sql.getErrorCode() == LOCK_ABORTED)) {
                return true;
            }
        }
        return false;
    }

    private static long seconds(Duration wait) {
        return Math.max(1, wait.toSeconds());
    }

    private static void run(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
