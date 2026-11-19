package org.oagi.score.gateway.http.common.lock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Set;

/** What the databases that lock a row with a SELECT have in common: the statement, and that the row is there. */
abstract class AbstractRowLockDialect implements DatabaseLockDialect {

    private static final String EXCLUSIVE = "FOR UPDATE";

    /** The clause of the SELECT that takes a shared lock. */
    abstract String sharedClause();

    /** Takes the lock of the SELECT, which ends with the clause of the mode, waiting for as long as the wait. */
    abstract void lock(Connection connection, String name, String mode, Duration wait) throws SQLException;

    /** The codes of the errors of a lock that was not got in time. */
    abstract Set<Integer> timeoutCodes();

    @Override
    public final void shared(Connection connection, String name, Duration wait) throws SQLException {
        lock(connection, name, sharedClause(), wait);
    }

    @Override
    public final void exclusive(Connection connection, String name, Duration wait) throws SQLException {
        lock(connection, name, EXCLUSIVE, wait);
    }

    @Override
    public final boolean isTimeout(SQLException e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && timeoutCodes().contains(sql.getErrorCode())) {
                return true;
            }
        }
        return false;
    }

    /** A zero wait is not to wait at all; any other is at least a second, which is the shortest that the databases wait. */
    static boolean noWait(Duration wait) {
        return wait.isZero() || wait.isNegative();
    }

    static long seconds(Duration wait) {
        return Math.max(1, (wait.toMillis() + 999) / 1000);
    }

    /** Runs the SELECT of the row of the name with what comes after the lock clause; a row that is not there locks nothing, so it is an error. */
    final void select(Connection connection, String name, String clauses) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT lock_name FROM " + DatabaseLock.TABLE + " WHERE lock_name = ? " + clauses)) {
            statement.setString(1, name);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("The row '" + name + "' of the table " + DatabaseLock.TABLE + " is missing, so it cannot lock anything.");
                }
            }
        }
    }
}
