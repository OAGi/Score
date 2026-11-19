package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * The write freeze that is done in the database, so that it holds for every process that writes to it and for every server: the load
 * takes the gate row with an exclusive lock (see {@link ReleasePackageLoadWriteGate}) on a connection of its own and keeps it, uncommitted, until the freeze
 * ends. The writers that are at work finish first, the ones that come after wait, reading is not touched, and when the server that
 * froze dies its connection goes with it and the lock is let go.
 */
@Component
public class ReleasePackageLoadDatabaseWriteFreeze implements ReleasePackageLoadWriteFreeze {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DataSource dataSource;
    private final ReleasePackageLoadSettings settings;

    private Connection held;
    private volatile Thread owner;

    public ReleasePackageLoadDatabaseWriteFreeze(DataSource dataSource, ReleasePackageLoadSettings settings) {
        this.dataSource = dataSource;
        this.settings = settings;
    }

    @Override
    public synchronized void freeze() {
        if (held != null) {
            throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.ANOTHER_LOAD);
        }
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            ReleasePackageLoadWriteGate.close(connection, settings.drainWait());
        } catch (SQLException e) {
            closeQuietly(connection);
            logger.warn("Writing could not be stopped (" + (ReleasePackageLoadWriteGate.isTimeout(e) ? "the writers did not finish within " + settings.drainWait() : "an error") + "): " + e.getMessage());
            throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.CANNOT_STOP_WRITING);
        }
        held = connection;
        owner = Thread.currentThread();
    }

    @Override
    public synchronized void release() {
        Connection connection = held;
        held = null;
        owner = null;
        closeQuietly(connection);
    }

    /** Whether the thread is the one that stopped writing: what it writes, the load does (to the schema it makes, and to the record of the load). */
    boolean isOwnedBy(Thread thread) {
        return owner == thread;
    }

    private void closeQuietly(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.rollback();
        } catch (SQLException e) {
            logger.warn("The gate could not be let go by a rollback (closing the connection does): " + e.getMessage());
        }
        try {
            connection.close();
        } catch (SQLException e) {
            logger.warn("The connection of the freeze could not be closed: " + e.getMessage());
        }
    }
}
