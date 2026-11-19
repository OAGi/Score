package org.oagi.score.gateway.http.common.lock;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Set;

/** MariaDB: the SELECT says how long it waits ({@code WAIT n}, or {@code NOWAIT}). */
final class MariaDbLockDialect extends AbstractRowLockDialect {

    static final MariaDbLockDialect INSTANCE = new MariaDbLockDialect();

    /** ER_LOCK_WAIT_TIMEOUT, and ER_LOCK_ABORTED (the wait that gave up with NOWAIT or WAIT). */
    private static final Set<Integer> TIMEOUTS = Set.of(1205, 1689);

    private MariaDbLockDialect() {
    }

    @Override
    String sharedClause() {
        return "LOCK IN SHARE MODE";
    }

    @Override
    void lock(Connection connection, String name, String mode, Duration wait) throws SQLException {
        select(connection, name, mode + " " + (noWait(wait) ? "NOWAIT" : "WAIT " + seconds(wait)));
    }

    @Override
    Set<Integer> timeoutCodes() {
        return TIMEOUTS;
    }
}
