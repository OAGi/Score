package org.oagi.score.gateway.http.common.lock;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Set;

/**
 * MySQL (8.0 and later): the SELECT can only say that it does not wait ({@code NOWAIT}), so how long it waits is the session's
 * {@code innodb_lock_wait_timeout}, which is set for the statement and put back as it was after it (the connection is a pooled one).
 */
final class MySqlLockDialect extends AbstractRowLockDialect {

    static final MySqlLockDialect INSTANCE = new MySqlLockDialect();

    /** ER_LOCK_WAIT_TIMEOUT, and ER_LOCK_NOWAIT (a lock that is not got at once, with NOWAIT). */
    private static final Set<Integer> TIMEOUTS = Set.of(1205, 3572);

    private MySqlLockDialect() {
    }

    @Override
    String sharedClause() {
        return "FOR SHARE";
    }

    @Override
    void lock(Connection connection, String name, String mode, Duration wait) throws SQLException {
        if (noWait(wait)) {
            select(connection, name, mode + " NOWAIT");
            return;
        }
        long before = sessionTimeout(connection);
        setSessionTimeout(connection, seconds(wait));
        try {
            select(connection, name, mode);
        } finally {
            setSessionTimeout(connection, before);
        }
    }

    @Override
    Set<Integer> timeoutCodes() {
        return TIMEOUTS;
    }

    private static long sessionTimeout(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery("SELECT @@SESSION.innodb_lock_wait_timeout")) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private static void setSessionTimeout(Connection connection, long seconds) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("SET SESSION innodb_lock_wait_timeout = " + seconds);
        }
    }
}
