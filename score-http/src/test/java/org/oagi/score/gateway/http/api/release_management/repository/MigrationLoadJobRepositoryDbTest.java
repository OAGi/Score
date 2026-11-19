package org.oagi.score.gateway.http.api.release_management.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DataSourceConnectionProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.release_management.model.MigrationLoadJob;
import org.oagi.score.gateway.http.api.release_management.model.MigrationLoadJobId;
import org.oagi.score.gateway.http.api.release_management.model.MigrationLoadState;
import org.oagi.score.gateway.http.api.release_management.repository.jooq.JooqMigrationLoadJobRepository;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

/** The record of the loads of release packages, on the table the migration makes, in a real MariaDB. */
@Testcontainers(disabledWithoutDocker = true)
class MigrationLoadJobRepositoryDbTest {

    @Container
    static final MariaDBContainer MARIADB = new MariaDBContainer("mariadb:11.8").withUsername("root");

    private static final UserId OWNER = new UserId(BigInteger.valueOf(7));

    private HikariDataSource pool;
    private DSLContext dsl;
    private MigrationLoadJobRepository jobs;

    @BeforeEach
    void table() throws Exception {
        String schema = "jobs_" + System.nanoTime();
        try (var con = java.sql.DriverManager.getConnection(MARIADB.getJdbcUrl(), "root", MARIADB.getPassword());
             var statement = con.createStatement()) {
            statement.execute("CREATE DATABASE `" + schema + "`");
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(MARIADB.getJdbcUrl().replaceFirst("/test(\\?|$)", "/" + schema + "$1"));
        config.setUsername("root");
        config.setPassword(MARIADB.getPassword());
        config.setMaximumPoolSize(10);
        pool = new HikariDataSource(config);
        dsl = DSL.using(new DataSourceConnectionProvider(pool), SQLDialect.MARIADB);
        // The table is the one the migration makes, as the migration says it.
        String migration = new ClassPathResource("db/migration/V3_6_2__upgrade_from_3_6_1.sql")
                .getContentAsString(StandardCharsets.UTF_8);
        try (var con = pool.getConnection(); var statement = con.createStatement()) {
            statement.execute(migration);
        }
        jobs = new JooqMigrationLoadJobRepository(dsl, null, null);
    }

    @AfterEach
    void close() {
        pool.close();
    }

    private MigrationLoadJobId start() {
        return jobs.createUnlessActive(OWNER, "10_13_3.zip", "10.13.3", "node-1").orElseThrow();
    }

    @Test
    void aLoadIsRecordedAsPendingWithWhoStartedItAndWhichPackage() {
        MigrationLoadJob job = jobs.find(start()).orElseThrow();

        assertEquals(MigrationLoadState.PENDING, job.state());
        assertEquals(OWNER, job.ownerUserId());
        assertEquals("10_13_3.zip", job.packageFileName());
        assertEquals("10.13.3", job.releaseNum());
        assertEquals("node-1", job.nodeId());
        assertNull(job.stagingSchema());
        assertNotNull(job.creationTimestamp());
    }

    @Test
    void aLoadIsNotStartedWhileAnotherGoesOnButIsAfterItEnded() {
        MigrationLoadJobId first = start();

        assertEquals(Optional.empty(), jobs.createUnlessActive(OWNER, "other.zip", null, "node-2"));
        assertEquals(List.of(first), jobs.findActive().stream().map(MigrationLoadJob::id).toList());

        assertTrue(jobs.moveState(first, Set.of(MigrationLoadState.PENDING), MigrationLoadState.FAILED, "gave up"));

        assertTrue(jobs.createUnlessActive(OWNER, "other.zip", null, "node-2").isPresent());
    }

    @Test
    void ofManyThatTryAtOnceExactlyOneStarts() throws Exception {
        int tries = 8;
        ExecutorService threads = Executors.newFixedThreadPool(tries);
        CyclicBarrier together = new CyclicBarrier(tries);
        List<Future<Boolean>> results = new ArrayList<>();
        for (int i = 0; i < tries; i++) {
            int n = i;
            Callable<Boolean> attempt = () -> {
                together.await();
                try {
                    return jobs.createUnlessActive(OWNER, "p" + n + ".zip", null, "node").isPresent();
                } catch (RuntimeException deadlock) {
                    return false; // the database refused it, which is not a second load either
                }
            };
            results.add(threads.submit(attempt));
        }
        long started = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) {
                started++;
            }
        }
        threads.shutdown();

        assertEquals(1, started);
        assertEquals(1, jobs.findActive().size());
    }

    @Test
    void aLoadIsMovedOnlyFromTheStatesItIsToBeMovedFrom() {
        MigrationLoadJobId id = start();

        assertFalse(jobs.moveState(id, Set.of(MigrationLoadState.LOADING), MigrationLoadState.MIGRATING, "wrong"));
        assertEquals(MigrationLoadState.PENDING, jobs.find(id).orElseThrow().state());

        assertTrue(jobs.moveState(id, Set.of(MigrationLoadState.PENDING, MigrationLoadState.PREFLIGHT), MigrationLoadState.LOADING, "loading"));
        MigrationLoadJob job = jobs.find(id).orElseThrow();
        assertEquals(MigrationLoadState.LOADING, job.state());
        assertEquals("loading", job.message());
    }

    @Test
    void whatTheLoadSaysAndTheSchemasItUsesAreRecordedAndASchemaNotGivenIsKept() {
        MigrationLoadJobId id = start();

        jobs.progress(id, "Executing mig.sql");
        jobs.schemas(id, "oagi_load_1", null, null);
        jobs.schemas(id, null, "oagi_backup_1", null);

        MigrationLoadJob job = jobs.find(id).orElseThrow();
        assertEquals("Executing mig.sql", job.message());
        assertEquals("oagi_load_1", job.stagingSchema());
        assertEquals("oagi_backup_1", job.backupSchema());
        assertNull(job.discardedSchema());
    }

    @Test
    void theLatestLoadThatEndedAsAStateIsFound() {
        MigrationLoadJobId first = start();
        jobs.moveState(first, MigrationLoadState.ACTIVE, MigrationLoadState.SUCCEEDED, "done");
        MigrationLoadJobId second = start();
        jobs.moveState(second, MigrationLoadState.ACTIVE, MigrationLoadState.SUCCEEDED, "done");
        MigrationLoadJobId third = start();
        jobs.moveState(third, MigrationLoadState.ACTIVE, MigrationLoadState.FAILED, "failed");

        assertEquals(second, jobs.findLatest(MigrationLoadState.SUCCEEDED).orElseThrow().id());
        assertEquals(third, jobs.findLatest(MigrationLoadState.FAILED).orElseThrow().id());
        assertEquals(Optional.empty(), jobs.findLatest(MigrationLoadState.ROLLED_BACK));
    }

    @Test
    void onlyALoadThatGoesOnAndHasNotChangedForLongIsStale() {
        MigrationLoadJobId id = start();
        LocalDateTime now = LocalDateTime.now();

        assertEquals(List.of(), jobs.lockActiveSince(now.minusHours(1)), "a load that has just changed is not stale");

        dsl.execute("UPDATE migration_load_job SET last_update_timestamp = ?", now.minusHours(3));
        assertEquals(List.of(id), jobs.lockActiveSince(now.minusHours(1)).stream().map(MigrationLoadJob::id).toList());

        jobs.moveState(id, MigrationLoadState.ACTIVE, MigrationLoadState.FAILED, "over");
        dsl.execute("UPDATE migration_load_job SET last_update_timestamp = ?", now.minusHours(3));
        assertEquals(List.of(), jobs.lockActiveSince(now.minusHours(1)), "one that has ended is not");
    }
}
