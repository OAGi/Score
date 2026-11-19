package org.oagi.score.gateway.http.common.lock;

import org.jooq.SQLDialect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.*;

/** The lock of the database, on a database made for the test (one subclass for each that is known): shared locks go together, an exclusive one is alone, and it is the thread that owns a lock. */
abstract class DatabaseLockTest {

    private static final ExecutorService OTHER = Executors.newCachedThreadPool();

    /** The dialect of the database of the test. */
    abstract SQLDialect sqlDialect();

    /** A connection to the database of the test, in which there is nothing yet. */
    abstract Connection connect() throws SQLException;

    private final DataSource dataSource = new SimpleDataSource();


    @BeforeEach
    void table() throws SQLException {
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.execute("DROP TABLE IF EXISTS named_lock");
            s.execute("CREATE TABLE named_lock (lock_name varchar(100) NOT NULL PRIMARY KEY) ENGINE = InnoDB");
            s.execute("INSERT INTO named_lock (lock_name) VALUES ('a'), ('b')");
        }
    }

    private DatabaseLock lock(String name) {
        return new DatabaseLock(dataSource, sqlDialect(), name, Duration.ofSeconds(5));
    }

    private static <T> T inOtherThread(Callable<T> task) throws Exception {
        return OTHER.submit(task).get(30, TimeUnit.SECONDS);
    }

    /** Whether another thread gets the lock at once (which it lets go again). */
    private boolean otherThreadGets(String name, boolean exclusive) throws Exception {
        return inOtherThread(() -> {
            DatabaseLock other = lock(name);
            Lock mode = exclusive ? other.writeLock() : other.readLock();
            boolean got = mode.tryLock();
            if (got) {
                mode.unlock();
            }
            return got;
        });
    }

    @Test
    void sharedLocksAreHeldTogether() throws Exception {
        DatabaseLock lock = lock("a");
        lock.readLock().lock();
        try {
            assertTrue(otherThreadGets("a", false));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Test
    void anExclusiveLockIsAlone() throws Exception {
        DatabaseLock lock = lock("a");
        lock.writeLock().lock();
        try {
            assertFalse(otherThreadGets("a", false));
            assertFalse(inOtherThread(() -> lock("a").writeLock().tryLock(1, TimeUnit.SECONDS)));
            assertTrue(otherThreadGets("b", true), "another name is not held");
        } finally {
            lock.writeLock().unlock();
        }
        assertTrue(otherThreadGets("a", true), "it is let go by unlock");
    }

    @Test
    void aReadLockIsHeldWhileAWriterWaitsForIt() throws Exception {
        DatabaseLock lock = lock("a");
        lock.readLock().lock();
        try {
            assertFalse(otherThreadGets("a", true));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Test
    void aLockIsTakenAgainByItsThread() throws Exception {
        DatabaseLock lock = lock("a");
        lock.writeLock().lock();
        lock.writeLock().lock();
        lock.readLock().lock();
        lock.readLock().unlock();
        lock.writeLock().unlock();
        assertFalse(otherThreadGets("a", false), "it is held until the last unlock");
        lock.writeLock().unlock();
        assertTrue(otherThreadGets("a", false));
    }

    @Test
    void aReadLockIsNotMadeAWriteLock() {
        DatabaseLock lock = lock("a");
        lock.readLock().lock();
        try {
            assertThrows(IllegalStateException.class, () -> lock.writeLock().lock());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Test
    void aLockIsLetGoOnlyByItsOwner() {
        DatabaseLock lock = lock("a");
        assertThrows(IllegalMonitorStateException.class, () -> lock.writeLock().unlock());
        assertThrows(UnsupportedOperationException.class, () -> lock.writeLock().newCondition());
    }

    @Test
    void aLockOfAnotherThreadIsWaitedForUntilTheTimeIsOut() throws Exception {
        DatabaseLock lock = lock("a");
        lock.writeLock().lock();
        try {
            long started = System.nanoTime();
            assertFalse(inOtherThread(() -> lock("a").writeLock().tryLock(1, TimeUnit.SECONDS)));
            assertTrue(Duration.ofNanos(System.nanoTime() - started).toMillis() >= 900, "it waited for the second");
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Test
    void aNameWithNoRowIsNotLocked() {
        assertThrows(IllegalStateException.class, () -> lock("missing").writeLock().lock());
    }

    private final class SimpleDataSource implements DataSource {
        @Override
        public Connection getConnection() throws SQLException {
            return connect();
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return getConnection();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new SQLException("not a wrapper");
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return false;
        }

        @Override
        public java.io.PrintWriter getLogWriter() {
            return null;
        }

        @Override
        public void setLogWriter(java.io.PrintWriter out) {
        }

        @Override
        public void setLoginTimeout(int seconds) {
        }

        @Override
        public int getLoginTimeout() {
            return 0;
        }

        @Override
        public java.util.logging.Logger getParentLogger() {
            return java.util.logging.Logger.getLogger("DatabaseLockTest");
        }
    }
}
