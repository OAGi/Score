package org.oagi.score.gateway.http.common.lock;

import org.jooq.SQLDialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;

/**
 * What differs, from one database to another, in locking the row of a name in the table {@value DatabaseLock#TABLE}: the statement that takes a shared
 * or an exclusive lock, how long it waits, and the error of a lock that was not got in time. The lock is held until the transaction of the
 * connection ends. The row of a name must be there (added by a migration), because a row that is not there locks nothing.
 */
interface DatabaseLockDialect {

    /** Waits (a zero wait is not to wait at all) for the exclusive locks of the name to be let go, and holds a shared one until the transaction ends. */
    void shared(Connection connection, String name, Duration wait) throws SQLException;

    /** Waits (a zero wait is not to wait at all) for the shared and exclusive locks of the name to be let go, and holds an exclusive one until the transaction ends. */
    void exclusive(Connection connection, String name, Duration wait) throws SQLException;

    /** Whether the error is that the lock was not got within the time. */
    boolean isTimeout(SQLException e);

    /** The dialect for the one that the application's jOOQ is set to: MariaDB and MySQL are known. */
    static DatabaseLockDialect of(SQLDialect dialect) {
        return switch (dialect.family()) {
            case MARIADB -> MariaDbLockDialect.INSTANCE;
            case MYSQL -> MySqlLockDialect.INSTANCE;
            default -> throw new UnsupportedOperationException("A lock of the database is not made for " + dialect + ".");
        };
    }
}
