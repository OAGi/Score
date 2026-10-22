package org.oagi.score.gateway.http.api.cc_management.cache;

import org.oagi.score.gateway.http.common.TestDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.cc_management.repository.CcChangeLogRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * When the rows of {@code cc_change_log} are deleted: those older than the retention, at most a number at a time, and never
 * the younger ones that the watchdog may still need.
 * <pre>
 *   mvn test -Dtest=CcChangeLogRetentionTest -DargLine=-Dscore.freshness=true
 * </pre>
 * It empties the log of the development database first, and again at the end.
 */
@EnabledIfSystemProperty(named = "score.freshness", matches = "true")
class CcChangeLogRetentionTest {

    private static TestDataSource dataSource;
    private static DSLContext dsl;
    private RepositoryFactory repositoryFactory;
    private CcChangeLogRepository repository;

    @BeforeAll
    static void connect() {
        dataSource = TestDataSource.open(3);
        dsl = DSL.using(dataSource, SQLDialect.MARIADB);
    }

    @AfterAll
    static void disconnect() {
        dataSource.close();
    }

    @BeforeEach
    void setUp() {
        dsl.execute("delete from cc_change_log");
        repositoryFactory = new RepositoryFactory(dsl);
        repository = repositoryFactory.ccChangeLogRepository();
    }

    @AfterEach
    void tearDown() {
        dsl.execute("delete from cc_change_log");
    }

    private void row(int ageMinutes, long target) {
        dsl.execute("insert into cc_change_log (kind, target_id, creation_timestamp) values ('ACC', ?, now(6) - interval ? minute)",
                target, ageMinutes);
    }

    private long count() {
        return dsl.fetchOne("select count(*) from cc_change_log").get(0, Long.class);
    }

    @Test
    void onlyTheRowsOlderThanTheAgeAreDeleted() {
        row(180, 1);
        row(121, 2);
        row(119, 3);
        row(60, 4);
        row(0, 5);

        assertEquals(2, repository.purgeOlderThan(Duration.ofHours(2), 100));

        assertEquals(3, count());
        assertEquals(3, dsl.fetchOne("select count(*) from cc_change_log where target_id in (3, 4, 5)").get(0, Long.class),
                "the rows that were kept are the younger ones");
    }

    @Test
    void atMostTheLimitIsDeletedAtATime() {
        for (int i = 0; i < 5; i++) {
            row(300, i);
        }
        row(1, 99);

        assertEquals(2, repository.purgeOlderThan(Duration.ofHours(2), 2));
        assertEquals(4, count());
        assertEquals(2, repository.purgeOlderThan(Duration.ofHours(2), 2));
        assertEquals(1, repository.purgeOlderThan(Duration.ofHours(2), 2));
        assertEquals(0, repository.purgeOlderThan(Duration.ofHours(2), 2));
        assertEquals(1, count(), "the young row is still there");
    }

    @Test
    void theFeedDeletesWhatIsOlderThanItsRetentionAndNothingElse() {
        long retentionMinutes = CcCacheWatchdog.RETENTION.toMinutes();
        row((int) retentionMinutes + 5, 1);
        row((int) retentionMinutes + 1, 2);
        row((int) retentionMinutes - 5, 3);
        row(0, 4);
        CcCacheStatus status = new CcCacheStatus();
        CcCacheWatchdog watchdog = new CcCacheWatchdog(repositoryFactory, new SpringCcCache(new ConcurrentMapCacheManager(), status), status);

        watchdog.purge();

        assertEquals(2, count());
        assertEquals(2, dsl.fetchOne("select count(*) from cc_change_log where target_id in (3, 4)").get(0, Long.class));
    }

    @Test
    void aBigBacklogIsDeletedInSeveralRoundsOfTheFeed() {
        dsl.execute("insert into cc_change_log (kind, target_id, creation_timestamp)"
                + " select 'ACC', seq, now(6) - interval 5 hour from seq_1_to_12000");
        row(0, 1);
        CcCacheStatus status = new CcCacheStatus();
        CcCacheWatchdog watchdog = new CcCacheWatchdog(repositoryFactory, new SpringCcCache(new ConcurrentMapCacheManager(), status), status);

        watchdog.purge();

        assertEquals(1, count(), "all the old rows went, in chunks of 5,000, and the young one stayed");
    }

    private long nextNumber() {
        return dsl.fetchOne("select auto_increment from information_schema.tables where table_schema = database() and table_name = 'cc_change_log'")
                .get(0, Long.class);
    }

    @Test
    void theNumbersStartAgainFromOneWhenTheTableIsEmpty() {
        for (int i = 0; i < 5; i++) {
            row(0, i);
        }
        dsl.execute("delete from cc_change_log");
        org.junit.jupiter.api.Assertions.assertTrue(nextNumber() > 5, "deleting rows does not lower the numbers");

        org.junit.jupiter.api.Assertions.assertTrue(repository.restartNumbering());

        assertEquals(1, nextNumber());
        row(0, 1);
        assertEquals(1, dsl.fetchOne("select min(cc_change_log_id) from cc_change_log").get(0, Long.class));
    }

    @Test
    void theNumbersFollowTheGreatestOneThatIsLeft() {
        for (int i = 0; i < 6; i++) {
            row(0, i);
        }
        long max = dsl.fetchOne("select max(cc_change_log_id) from cc_change_log").get(0, Long.class);
        dsl.execute("delete from cc_change_log where cc_change_log_id < ?", max - 1);   // the two last stay

        org.junit.jupiter.api.Assertions.assertTrue(repository.restartNumbering());

        assertEquals(max + 1, nextNumber(), "not lower than the greatest number in the table");
    }

    @Test
    void restartingTheNumbersDoesNotWaitForATransactionThatHasWrittenToTheTable() throws Exception {
        try (java.sql.Connection open = dataSource.getConnection()) {
            open.setAutoCommit(false);
            try (java.sql.Statement statement = open.createStatement()) {
                statement.executeUpdate("insert into cc_change_log (kind, target_id) values ('ACC', 1)");
            }
            long started = System.nanoTime();

            boolean done = repository.restartNumbering();

            org.junit.jupiter.api.Assertions.assertFalse(done, "it gives up");
            org.junit.jupiter.api.Assertions.assertTrue(System.nanoTime() - started < 2_000_000_000L, "and at once");
            // and a writer is not held back behind it
            started = System.nanoTime();
            dsl.execute("insert into cc_change_log (kind, target_id) values ('ACC', 2)");
            org.junit.jupiter.api.Assertions.assertTrue(System.nanoTime() - started < 2_000_000_000L);
            open.rollback();
        }
    }
}
