package org.oagi.score.gateway.http.common.lock;

import org.jooq.SQLDialect;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A {@link ReadWriteLock} that the whole application, in whatever process, shares through the database: the row of the table {@value #TABLE}
 * whose {@code lock_name} is the name is locked, and InnoDB holds the lock until the transaction of the connection ends. The read lock is
 * shared (held together with other shared locks) and the write lock is exclusive (it waits for them, and makes those that come after wait
 * behind it). The row of a name must be there (added by a migration), because a row that is not there locks nothing.
 * A lock that is held has a connection of its own with a transaction that is not
 * ended until the lock is let go, so hold it for a short while only (it takes a connection of the pool).
 *
 * <p>Like {@link java.util.concurrent.locks.ReentrantReadWriteLock}: a lock is owned by the thread that took it, and may be taken again by that thread
 * (each time must be let go); the write lock may be taken while the thread has the read lock only if it is the write lock that it has,
 * and the read lock may be taken while the thread has the write lock. Taking the write lock while the thread has the read lock (an upgrade) is
 * refused, as it would wait for itself. Use one instance for a name in a thread. There is no {@link Condition}. A wait for a lock is not
 * interrupted (JDBC does not), so {@code lockInterruptibly} and {@code tryLock} with a time look at the interrupt before they wait.
 *
 * <p>What is done in a transaction of the caller has no lock to take: {@link #holdShared} and {@link #holdExclusive} lock the row on the connection of
 * the caller, and the lock is let go when its transaction ends.
 */
public final class DatabaseLock implements ReadWriteLock {

    /** The table of the named locks, which stays where it is when the schema is swapped. */
    public static final String TABLE = "named_lock";

    private final DataSource dataSource;
    private final DatabaseLockDialect dialect;
    private final String name;
    private final Duration patience;
    private final ThreadLocal<Hold> holds = new ThreadLocal<>();
    private final Lock readLock = new Mode(false);
    private final Lock writeLock = new Mode(true);

    /**
     * @param dialect  the SQL dialect of the database of the data source, which tells how it locks a row; MariaDB and MySQL are known
     * @param patience how long {@code lock()} waits before it gives up with an {@link IllegalStateException}, so that a lock that is never let go
     *                 does not make the thread wait for ever
     */
    public DatabaseLock(DataSource dataSource, SQLDialect dialect, String name, Duration patience) {
        this.dataSource = dataSource;
        this.dialect = DatabaseLockDialect.of(dialect);
        this.name = name;
        this.patience = patience;
    }

    /**
     * Holds the lock shared on a connection that the caller has, for what is done in the transaction of the caller: it waits (a zero wait is not to
     * wait at all) for the exclusive lock to be let go, and holds until the transaction of the connection ends. The thread does not own it.
     */
    public void holdShared(Connection connection, Duration wait) throws SQLException {
        dialect.shared(connection, name, wait);
    }

    /** Like {@link #holdShared}, exclusive: waits for the shared and exclusive locks to be let go. */
    public void holdExclusive(Connection connection, Duration wait) throws SQLException {
        dialect.exclusive(connection, name, wait);
    }

    /** Whether the error is that the lock was not got within the time. */
    public boolean isTimeout(SQLException e) {
        return dialect.isTimeout(e);
    }

    @Override
    public Lock readLock() {
        return readLock;
    }

    @Override
    public Lock writeLock() {
        return writeLock;
    }

    /** What a thread holds: the connection whose transaction keeps the lock, whether it is the exclusive one, and how many times it was taken. */
    private static final class Hold {
        final Connection connection;
        final boolean exclusive;
        int count = 1;

        Hold(Connection connection, boolean exclusive) {
            this.connection = connection;
            this.exclusive = exclusive;
        }
    }

    private final class Mode implements Lock {
        private final boolean exclusive;

        Mode(boolean exclusive) {
            this.exclusive = exclusive;
        }

        @Override
        public void lock() {
            if (!acquire(patience)) {
                throw new IllegalStateException("The lock '" + name + "' was not got within " + patience + ".");
            }
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            checkInterrupt();
            lock();
        }

        @Override
        public boolean tryLock() {
            return acquire(Duration.ZERO);
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            checkInterrupt();
            return acquire(Duration.of(time, unit.toChronoUnit()));
        }

        @Override
        public void unlock() {
            Hold hold = holds.get();
            if (hold == null) {
                throw new IllegalMonitorStateException("The thread does not hold the lock '" + name + "'.");
            }
            if (--hold.count == 0) {
                holds.remove();
                release(hold.connection);
            }
        }

        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException("A lock of the database has no condition.");
        }

        private boolean acquire(Duration wait) {
            Hold hold = holds.get();
            if (hold != null) {
                if (exclusive && !hold.exclusive) {
                    throw new IllegalStateException("The thread holds the read lock '" + name + "', which cannot be made a write lock.");
                }
                hold.count++;
                return true;
            }
            Connection connection = null;
            try {
                connection = dataSource.getConnection();
                connection.setAutoCommit(false);
                if (exclusive) {
                    dialect.exclusive(connection, name, wait);
                } else {
                    dialect.shared(connection, name, wait);
                }
                holds.set(new Hold(connection, exclusive));
                return true;
            } catch (SQLException e) {
                release(connection);
                if (dialect.isTimeout(e)) {
                    return false;
                }
                throw new IllegalStateException("The lock '" + name + "' could not be taken: " + e.getMessage(), e);
            }
        }

        private void checkInterrupt() throws InterruptedException {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }
    }

    /** Ends the transaction, which lets the lock go, and gives the connection back. */
    private static void release(Connection connection) {
        if (connection == null) {
            return;
        }
        try (connection) {
            connection.rollback();
        } catch (SQLException ignored) {
            // The connection is closed after it, which ends its transaction in any case.
        }
    }
}
