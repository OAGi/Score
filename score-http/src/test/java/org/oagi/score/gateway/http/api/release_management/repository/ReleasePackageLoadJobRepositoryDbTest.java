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
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJob;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJobId;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadState;
import org.oagi.score.gateway.http.api.release_management.repository.jooq.JooqReleasePackageLoadJobRepository;
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
class ReleasePackageLoadJobRepositoryDbTest {

    @Container
    static final MariaDBContainer MARIADB = new MariaDBContainer("mariadb:11.8").withUsername("root");

    private static final UserId OWNER = new UserId(BigInteger.valueOf(7));

    private HikariDataSource pool;
    private DSLContext dsl;
    private ReleasePackageLoadJobRepository jobs;

    @BeforeEach
    void table() throws Exception {
        String schema = "jobs_" + System.nanoTime();
        try (var con = java.sql.DriverManager.getConnection(MARIADB.getJdbcUrl(), "root", MARIADB.getPassword());
             var statement = con.createStatement()) {
            statement.execute("CREATE DATABASE `" + schema + "`");
        }
        HikariConfig config = new HikariConfig();
        // The migration is several statements, which the driver is told it may be given at once.
        config.setJdbcUrl(MARIADB.getJdbcUrl().replaceFirst("/test(\\?|$)", "/" + schema + "$1")
                + (MARIADB.getJdbcUrl().contains("?") ? "&" : "?") + "allowMultiQueries=true");
        config.setUsername("root");
        config.setPassword(MARIADB.getPassword());
        config.setMaximumPoolSize(10);
        pool = new HikariDataSource(config);
        dsl = DSL.using(new DataSourceConnectionProvider(pool), SQLDialect.MARIADB);
        // The table is the one the migration makes, as the migration says it.
        String migration = new ClassPathResource("db/migration/V3_6_2__upgrade_from_3_6_1.sql")
                .getContentAsString(StandardCharsets.UTF_8);
        try (var con = pool.getConnection(); var statement = con.createStatement()) {
            // The migration also drops leftovers of two other tables, which a schema made for this test does not have.
            statement.execute("CREATE TABLE `log` (`log_id` BIGINT PRIMARY KEY)");
            statement.execute("CREATE TABLE `xbt_manifest` (`xbt_manifest_id` BIGINT PRIMARY KEY)");
            statement.execute(migration);
        }
        jobs = new JooqReleasePackageLoadJobRepository(dsl, null, null);
    }

    @AfterEach
    void close() {
        pool.close();
    }

    private ReleasePackageLoadJobId start() {
        return jobs.createUnlessActive(OWNER, "10_13_3.zip", "10.13.3", "node-1").orElseThrow();
    }

    @Test
    void aLoadIsRecordedAsPendingWithWhoStartedItAndWhichPackage() {
        ReleasePackageLoadJob job = jobs.find(start()).orElseThrow();

        assertEquals(ReleasePackageLoadState.PENDING, job.state());
        assertEquals(OWNER, job.ownerUserId());
        assertEquals("10_13_3.zip", job.packageFileName());
        assertEquals("10.13.3", job.releaseNum());
        assertEquals("node-1", job.nodeId());
        assertNull(job.stagingSchema());
        assertNotNull(job.creationTimestamp());
    }

    @Test
    void aLoadIsNotStartedWhileAnotherGoesOnButIsAfterItEnded() {
        ReleasePackageLoadJobId first = start();

        assertEquals(Optional.empty(), jobs.createUnlessActive(OWNER, "other.zip", null, "node-2"));
        assertEquals(List.of(first), jobs.findActive().stream().map(ReleasePackageLoadJob::id).toList());

        assertTrue(jobs.moveState(first, Set.of(ReleasePackageLoadState.PENDING), ReleasePackageLoadState.FAILED, "gave up"));

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
        ReleasePackageLoadJobId id = start();

        assertFalse(jobs.moveState(id, Set.of(ReleasePackageLoadState.LOADING), ReleasePackageLoadState.MIGRATING, "wrong"));
        assertEquals(ReleasePackageLoadState.PENDING, jobs.find(id).orElseThrow().state());

        assertTrue(jobs.moveState(id, Set.of(ReleasePackageLoadState.PENDING, ReleasePackageLoadState.PREFLIGHT), ReleasePackageLoadState.LOADING, "loading"));
        ReleasePackageLoadJob job = jobs.find(id).orElseThrow();
        assertEquals(ReleasePackageLoadState.LOADING, job.state());
        assertEquals("loading", job.message());
    }

    @Test
    void whatTheLoadSaysAndTheSchemasItUsesAreRecordedAndASchemaNotGivenIsKept() {
        ReleasePackageLoadJobId id = start();

        jobs.progress(id, "Executing mig.sql");
        jobs.schemas(id, "oagi_load_1", null, null);
        jobs.schemas(id, null, "oagi_backup_1", null);

        ReleasePackageLoadJob job = jobs.find(id).orElseThrow();
        assertEquals("Executing mig.sql", job.message());
        assertEquals("oagi_load_1", job.stagingSchema());
        assertEquals("oagi_backup_1", job.backupSchema());
        assertNull(job.discardedSchema());
    }

    @Test
    void theLatestLoadThatEndedAsAStateIsFound() {
        ReleasePackageLoadJobId first = start();
        jobs.moveState(first, ReleasePackageLoadState.ACTIVE, ReleasePackageLoadState.SUCCEEDED, "done");
        ReleasePackageLoadJobId second = start();
        jobs.moveState(second, ReleasePackageLoadState.ACTIVE, ReleasePackageLoadState.SUCCEEDED, "done");
        ReleasePackageLoadJobId third = start();
        jobs.moveState(third, ReleasePackageLoadState.ACTIVE, ReleasePackageLoadState.FAILED, "failed");

        assertEquals(second, jobs.findLatest(ReleasePackageLoadState.SUCCEEDED).orElseThrow().id());
        assertEquals(third, jobs.findLatest(ReleasePackageLoadState.FAILED).orElseThrow().id());
        assertEquals(Optional.empty(), jobs.findLatest(ReleasePackageLoadState.ROLLED_BACK));
    }

    @Test
    void onlyALoadThatGoesOnAndHasNotChangedForLongIsStale() {
        ReleasePackageLoadJobId id = start();
        LocalDateTime now = LocalDateTime.now();

        assertEquals(List.of(), jobs.lockActiveSince(now.minusHours(1)), "a load that has just changed is not stale");

        dsl.execute("UPDATE release_package_load_job SET last_update_timestamp = ?", now.minusHours(3));
        assertEquals(List.of(id), jobs.lockActiveSince(now.minusHours(1)).stream().map(ReleasePackageLoadJob::id).toList());

        jobs.moveState(id, ReleasePackageLoadState.ACTIVE, ReleasePackageLoadState.FAILED, "over");
        dsl.execute("UPDATE release_package_load_job SET last_update_timestamp = ?", now.minusHours(3));
        assertEquals(List.of(), jobs.lockActiveSince(now.minusHours(1)), "one that has ended is not");
    }
}
