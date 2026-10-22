package org.oagi.score.gateway.http.api.cc_management.cache;

import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.repository.CcChangeLogRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.CcChangeLogRepository.RecordRef;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CcChangeFeedTest {

    /** A change log that is a list the test adds to, with the rows that are not yet visible kept apart. */
    private static final class FakeLog implements CcChangeLogRepository {
        final List<CcChange> visible = new ArrayList<>();
        final Map<Long, List<RecordRef>> releases = new HashMap<>();
        final List<Long> releaseIds = new ArrayList<>();
        boolean failing;
        int restarts;

        @Override
        public long maxSeq() {
            check();
            return visible.stream().mapToLong(CcChange::seq).max().orElse(0);
        }

        @Override
        public List<CcChange> readAfter(long seq, int limit) {
            check();
            return visible.stream().filter(e -> e.seq() > seq).sorted(Comparator.comparingLong(CcChange::seq)).limit(limit).toList();
        }

        @Override
        public List<CcChange> readRange(long from, long to) {
            check();
            return visible.stream().filter(e -> e.seq() >= from && e.seq() <= to).sorted(Comparator.comparingLong(CcChange::seq)).toList();
        }

        @Override
        public int purgeOlderThan(java.time.Duration age, int limit) {
            return 0;
        }

        @Override
        public boolean restartNumbering() {
            restarts++;
            return true;
        }

        @Override
        public List<RecordRef> recordsOfRelease(long releaseId) {
            return releases.getOrDefault(releaseId, List.of());
        }

        @Override
        public List<RecordRef> recordsOfLibrary(long libraryId) {
            return List.of();
        }

        @Override
        public List<RecordRef> recordsOfUser(long userId) {
            return List.of();
        }

        @Override
        public List<RecordRef> recordsTaggedWith(long tagId) {
            return List.of();
        }

        @Override
        public List<Long> allReleaseIds() {
            return releaseIds;
        }

        private void check() {
            if (failing) {
                throw new IllegalStateException("table 'cc_change_log' doesn't exist");
            }
        }
    }

    private final FakeLog log = new FakeLog();

    private static RepositoryFactory factory(CcChangeLogRepository log) {
        RepositoryFactory factory = org.mockito.Mockito.mock(RepositoryFactory.class);
        org.mockito.Mockito.when(factory.ccChangeLogRepository()).thenReturn(log);
        return factory;
    }
    private final CcCacheStatus status = new CcCacheStatus();
    private final SpringCcCache cache = new SpringCcCache(new ConcurrentMapCacheManager(), status);
    private final CcChangeFeed feed = new CcChangeFeed(factory(log), cache, status);

    private void fill(String... keys) {
        Map<String, Object> values = new HashMap<>();
        for (String key : keys) {
            values.put(key, key);
        }
        cache.putAll(cache.epoch(), values);
    }

    private static CcChange change(long seq, CcChangeKind kind, long id, Long parent) {
        return new CcChange(seq, kind, id, parent);
    }

    @Test
    void aChangedRecordIsForgottenWithWhatIsListedUnderItAndNothingElse() {
        fill("acc:1", "acc:2", "ascc:7", "ascc-from-acc:1", "seq-key-from-acc:1", "ascc-from-acc:2", "tags:acc:1");
        feed.poll();

        log.visible.add(change(1, CcChangeKind.ASCC, 7, 1L));
        feed.poll();

        assertNull(cache.get("ascc:7"));
        assertNull(cache.get("ascc-from-acc:1"), "the list of the ACC it belongs to");
        assertNull(cache.get("seq-key-from-acc:1"));
        assertEquals("acc:1", cache.get("acc:1"));
        assertEquals("acc:2", cache.get("acc:2"));
        assertEquals("ascc-from-acc:2", cache.get("ascc-from-acc:2"), "the list of another ACC");
        assertEquals("tags:acc:1", cache.get("tags:acc:1"));
    }

    @Test
    void anAccChangeForgetsItsOwnListsAndTagsToo() {
        fill("acc:1", "tags:acc:1", "ascc-from-acc:1", "bcc-from-acc:1", "seq-key-from-acc:1", "acc:2");
        feed.poll();

        log.visible.add(change(1, CcChangeKind.ACC, 1, null));
        feed.poll();

        for (String key : List.of("acc:1", "tags:acc:1", "ascc-from-acc:1", "bcc-from-acc:1", "seq-key-from-acc:1")) {
            assertNull(cache.get(key), key);
        }
        assertEquals("acc:2", cache.get("acc:2"));
    }

    @Test
    void aRowThatBecomesVisibleLateIsStillForgotten() {
        fill("acc:1", "acc:2", "acc:3");
        log.visible.add(change(10, CcChangeKind.ACC, 3, null));
        feed.poll();   // starts after row 10

        // 12 commits before 11, as the transaction that wrote 11 is longer
        log.visible.add(change(12, CcChangeKind.ACC, 2, null));
        feed.poll();
        assertNull(cache.get("acc:2"));
        assertEquals("acc:1", cache.get("acc:1"));

        log.visible.add(change(11, CcChangeKind.ACC, 1, null));
        feed.catchUp();
        long deadline = System.nanoTime() + 5_000_000_000L;
        while (cache.get("acc:1") != null && System.nanoTime() < deadline) {
            feed.poll();    // the gap is looked at again after a delay
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        assertNull(cache.get("acc:1"), "the row that came late was not missed");
    }

    @Test
    void whatWasWrittenJustBeforeTheInstanceStartedIsLookedAtOnce() {
        log.visible.add(change(5, CcChangeKind.ACC, 1, null));
        status.bind(null);   // so that filling the cache does not read the log
        fill("acc:1");

        feed.poll();   // the first read starts after row 5 but looks back
        feed.poll();

        assertNull(cache.get("acc:1"));
    }

    @Test
    void aChangedReleaseForgetsEveryRecordOfIt() {
        log.releases.put(9L, List.of(new RecordRef(CcChangeKind.ACC, 1, null), new RecordRef(CcChangeKind.ASCC, 2, 1L),
                new RecordRef(CcChangeKind.DT_SC, 3, 4L)));
        fill("acc:1", "ascc:2", "dt_sc:3", "dt_sc-of-dt:4", "acc:77");
        feed.poll();

        log.visible.add(change(1, CcChangeKind.RELEASE, 9, null));
        feed.poll();

        for (String key : List.of("acc:1", "ascc:2", "dt_sc:3", "dt_sc-of-dt:4")) {
            assertNull(cache.get(key), key);
        }
        assertEquals("acc:77", cache.get("acc:77"), "a record of another release stays");
    }

    @Test
    void aChangeOfTheDependenciesOfReleasesForgetsTheirScopes() {
        log.releaseIds.addAll(List.of(1L, 2L));
        fill("scope:1", "scope:2", "acc:1");
        feed.poll();

        log.visible.add(change(1, CcChangeKind.RELEASE_DEP, 2, null));
        feed.poll();

        assertNull(cache.get("scope:1"));
        assertNull(cache.get("scope:2"));
        assertEquals("acc:1", cache.get("acc:1"));
    }

    @Test
    void theCacheIsUsedOnlyWhileTheLogCanBeRead() {
        log.failing = true;
        feed.poll();
        assertTrue(cache.isBypassed(), "without the change log nothing would be forgotten");

        log.failing = false;
        feed.poll();
        assertFalse(cache.isBypassed());
    }

    @Test
    void aForgettingThatFailsIsTriedAgainWithTheSameRows() {
        fill("acc:1");
        feed.poll();
        log.visible.add(change(1, CcChangeKind.ACC, 1, null));
        CcCache flaky = new CcCache() {
            int calls;

            @Override
            public long epoch() {
                return cache.epoch();
            }

            @Override
            public Object get(String key) {
                return cache.get(key);
            }

            @Override
            public Map<String, Object> getAll(Collection<String> keys) {
                return cache.getAll(keys);
            }

            @Override
            public void put(long epoch, String key, Object value) {
                cache.put(epoch, key, value);
            }

            @Override
            public void putAll(long epoch, Map<String, Object> values) {
                cache.putAll(epoch, values);
            }

            @Override
            public void invalidate(Collection<String> keys) {
                if (calls++ == 0) {
                    throw new IllegalStateException("redis is down");
                }
                cache.invalidate(keys);
            }

            @Override
            public boolean isBypassed() {
                return cache.isBypassed();
            }

            @Override
            public void singleFlight(String key, Runnable task) {
                task.run();
            }
        };
        CcCacheStatus status2 = new CcCacheStatus();
        CcChangeFeed feed2 = new CcChangeFeed(factory(log), flaky, status2);
        feed2.poll();                       // starts after row 1 and looks back at it: the first forgetting fails
        assertEquals("acc:1", cache.get("acc:1"));
        feed2.poll();                       // and is tried again
        assertNull(cache.get("acc:1"));
    }

    /** A cache that counts what it is told to forget. */
    private static final class Counting implements CcCache {
        final CcCache inner;
        final List<Collection<String>> calls = new ArrayList<>();

        Counting(CcCache inner) {
            this.inner = inner;
        }

        @Override
        public long epoch() {
            return inner.epoch();
        }

        @Override
        public Object get(String key) {
            return inner.get(key);
        }

        @Override
        public Map<String, Object> getAll(Collection<String> keys) {
            return inner.getAll(keys);
        }

        @Override
        public void put(long epoch, String key, Object value) {
            inner.put(epoch, key, value);
        }

        @Override
        public void putAll(long epoch, Map<String, Object> values) {
            inner.putAll(epoch, values);
        }

        @Override
        public void invalidate(Collection<String> keys) {
            calls.add(new TreeSet<>(keys));
            inner.invalidate(keys);
        }

        @Override
        public boolean isBypassed() {
            return inner.isBypassed();
        }

        @Override
        public void singleFlight(String key, Runnable task) {
            inner.singleFlight(key, task);
        }
    }

    @Test
    void aRowIsForgottenOnceNoMatterHowOftenTheLogIsRead() {
        Counting counting = new Counting(cache);
        CcCacheStatus status2 = new CcCacheStatus();
        CcChangeFeed counted = new CcChangeFeed(factory(log), counting, status2);
        status2.bind(null);
        counted.poll();                                           // starts: nothing in the log
        log.visible.add(change(1, CcChangeKind.ACC, 1, null));
        log.visible.add(change(3, CcChangeKind.ACC, 3, null));    // 2 is a gap

        counted.poll();
        assertEquals(1, counting.calls.size());
        assertEquals(Set.of("acc:1", "tags:acc:1", "ascc-from-acc:1", "bcc-from-acc:1", "seq-key-from-acc:1",
                "acc:3", "tags:acc:3", "ascc-from-acc:3", "bcc-from-acc:3", "seq-key-from-acc:3"), counting.calls.get(0));

        for (int i = 0; i < 5; i++) {
            counted.poll();
        }
        assertEquals(1, counting.calls.size(), "nothing new in the log, nothing forgotten");

        log.visible.add(change(2, CcChangeKind.ACC, 2, null));    // the gap fills
        long deadline = System.nanoTime() + 5_000_000_000L;
        while (counting.calls.size() < 2 && System.nanoTime() < deadline) {
            counted.poll();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        assertEquals(2, counting.calls.size());
        assertEquals(Set.of("acc:2", "tags:acc:2", "ascc-from-acc:2", "bcc-from-acc:2", "seq-key-from-acc:2"), counting.calls.get(1));
        for (int i = 0; i < 5; i++) {
            counted.poll();
        }
        assertEquals(2, counting.calls.size(), "and the row that came late is not forgotten again");
    }

    @Test
    void theRowsAreKeptLongerThanTheFeedLooksForARowThatComesLate() {
        assertTrue(CcChangeFeed.RETENTION.toNanos() > CcChangeFeed.GAP_LIFETIME_NANOS,
                "a row that is deleted before the feed stops looking for it could be missed");
    }

    @Test
    void numbersThatGoBackAreReadFromTheNewEnd() {
        fill("acc:1", "acc:2");
        log.visible.add(change(5000, CcChangeKind.ACC, 9, null));
        feed.poll();                       // the cursor is at 5000

        log.visible.clear();               // the table was emptied and its numbers start again
        log.visible.add(change(1, CcChangeKind.ACC, 1, null));
        log.visible.add(change(2, CcChangeKind.ACC, 2, null));
        feed.poll();

        assertNull(cache.get("acc:1"), "a change with a number below the old cursor is not missed");
        assertNull(cache.get("acc:2"));
        assertFalse(cache.isBypassed());

        log.visible.add(change(3, CcChangeKind.ACC, 3, null));
        fill("acc:3");
        feed.poll();
        assertNull(cache.get("acc:3"), "and the next one is read as usual");
    }

    @Test
    void anEmptiedTableIsReadFromItsNewEnd() {
        log.visible.add(change(100, CcChangeKind.ACC, 9, null));
        feed.poll();
        log.visible.clear();
        feed.poll();
        fill("acc:1");

        log.visible.add(change(1, CcChangeKind.ACC, 1, null));
        feed.poll();

        assertNull(cache.get("acc:1"));
    }

    @Test
    void anEmptyLogPutsTheCursorBackSoThatNumbersFromOneAreRead() {
        fill("acc:1");
        log.visible.add(change(5000, CcChangeKind.ACC, 9, null));
        feed.poll();                       // the cursor is at 5000
        log.visible.clear();               // the old rows were deleted
        feed.poll();                       // the feed sees the empty log

        log.visible.add(change(1, CcChangeKind.ACC, 1, null));    // and the numbers start again
        log.visible.add(change(2, CcChangeKind.ACC, 2, null));
        feed.poll();

        assertNull(cache.get("acc:1"));
        assertEquals(0, log.restarts, "the feed does not restart the numbers itself, only the housekeeping does");
    }

    @Test
    void theNumbersAreRestartedOnlyAfterTheLogHasStayedEmptyForAWhile() {
        feed.restartAfterEmptyNanos = 5_000_000_000L;
        feed.poll();                       // empty: seen now
        feed.purge();
        assertEquals(0, log.restarts, "not yet");

        feed.restartAfterEmptyNanos = 0;
        feed.purge();
        assertEquals(1, log.restarts, "empty for long enough");

        log.visible.add(change(1, CcChangeKind.ACC, 1, null));
        feed.poll();                       // a row: the log is not empty any more
        feed.purge();
        assertEquals(1, log.restarts, "never while there are rows (the numbers would go back under the cursors)");
    }

    @Test
    void aLogThatWasNeverSeenEmptyIsNeverRestarted() {
        log.visible.add(change(7, CcChangeKind.ACC, 1, null));
        feed.restartAfterEmptyNanos = 0;
        feed.poll();
        feed.purge();
        assertEquals(0, log.restarts);
    }
}
