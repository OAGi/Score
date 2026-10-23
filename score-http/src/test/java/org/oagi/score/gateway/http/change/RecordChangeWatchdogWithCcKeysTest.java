package org.oagi.score.gateway.http.change;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListRecordTypes;
import org.oagi.score.gateway.http.api.cc_management.model.CcRecordTypes;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListRecordTypes;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceRecordTypes;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseRecordTypes;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtRecordTypes;

import org.oagi.score.gateway.http.change.repository.RecordChangeLogRepository;

import org.oagi.score.gateway.http.cache.CaffeineRecordCache;
import org.oagi.score.gateway.http.cache.RecordCache;
import org.oagi.score.gateway.http.cache.RecordCacheStatus;
import org.oagi.score.gateway.http.cache.TestRecordKeys;

import org.oagi.score.gateway.http.api.cc_management.repository.AffectedRecordsRepository;
import org.oagi.score.gateway.http.change.RecordRef;



import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.junit.jupiter.api.Test;
import static org.oagi.score.gateway.http.change.TestIds.id;

import java.math.BigInteger;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RecordChangeWatchdogWithCcKeysTest {

    /** A change log that is a list the test adds to, with the rows that are not yet visible kept apart. */
    private static final class FakeLog implements RecordChangeLogRepository, AffectedRecordsRepository {
        final List<ChangeLogEntry> visible = new ArrayList<>();
        final Map<BigInteger, List<RecordRef>> releases = new HashMap<>();
        final List<BigInteger> releaseIds = new ArrayList<>();
        boolean failing;
        int restarts;

        @Override
        public long maxSeq() {
            check();
            return visible.stream().mapToLong(ChangeLogEntry::seq).max().orElse(0);
        }

        @Override
        public List<ChangeLogEntry> readAfter(long seq, int limit) {
            check();
            return visible.stream().filter(e -> e.seq() > seq).sorted(Comparator.comparingLong(ChangeLogEntry::seq)).limit(limit).toList();
        }

        @Override
        public List<ChangeLogEntry> readRange(long from, long to) {
            check();
            return visible.stream().filter(e -> e.seq() >= from && e.seq() <= to).sorted(Comparator.comparingLong(ChangeLogEntry::seq)).toList();
        }

        @Override
        public int purgeOlderThan(java.time.Duration age, int limit) {
            return 0;
        }

        final Map<RecordType, List<RecordRef>> showing = new HashMap<>();

        @Override
        public List<RecordRef> recordsShowing(RecordType kind, Collection<BigInteger> manifestIds) {
            return showing.getOrDefault(kind, List.of());
        }

        @Override
        public boolean restartNumbering() {
            restarts++;
            return true;
        }

        @Override
        public List<RecordRef> recordsOfRelease(BigInteger releaseId) {
            return releases.getOrDefault(releaseId, List.of());
        }

        @Override
        public List<RecordRef> recordsOfLibrary(BigInteger libraryId) {
            return List.of();
        }

        @Override
        public List<RecordRef> recordsOfUser(BigInteger userId) {
            return List.of();
        }

        @Override
        public List<RecordRef> recordsTaggedWith(BigInteger tagId) {
            return List.of();
        }

        @Override
        public List<BigInteger> allReleaseIds() {
            return releaseIds;
        }

        private void check() {
            if (failing) {
                throw new IllegalStateException("table 'record_change_log' doesn't exist");
            }
        }
    }

    private final FakeLog log = new FakeLog();

    private static RepositoryFactory factory(RecordChangeLogRepository log) {
        RepositoryFactory factory = org.mockito.Mockito.mock(RepositoryFactory.class);
        org.mockito.Mockito.when(factory.recordChangeLogRepository()).thenReturn(log);
        org.mockito.Mockito.when(factory.affectedRecordsRepository()).thenReturn((FakeLog) log);
        return factory;
    }
    private final RecordCacheStatus status = new RecordCacheStatus();
    private final CaffeineRecordCache cache = new CaffeineRecordCache("", status);
    private final RecordChangeWatchdog watchdog = ChangeFeed.watchdog(factory(log), List.of(TestRecordKeys.resolver(log)), cache, status);

    private void fill(String... keys) {
        Map<String, Object> values = new HashMap<>();
        for (String key : keys) {
            values.put(key, key);
        }
        cache.putAll(cache.epoch(), values);
    }

    private static ChangeLogEntry change(long seq, RecordType kind, long id, Long parent) {
        return new ChangeLogEntry(seq, kind, id(id), (parent == null) ? null : id(parent));
    }

    @Test
    void aChangedRecordIsForgottenWithWhatIsListedUnderItAndNothingElse() {
        fill("acc:1", "acc:2", "ascc:7", "ascc-from-acc:1", "seq-key-from-acc:1", "ascc-from-acc:2", "tags:acc:1");
        watchdog.poll();

        log.visible.add(change(1, CcRecordTypes.ASCC, 7, 1L));
        watchdog.poll();

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
        watchdog.poll();

        log.visible.add(change(1, CcRecordTypes.ACC, 1, null));
        watchdog.poll();

        for (String key : List.of("acc:1", "tags:acc:1", "ascc-from-acc:1", "bcc-from-acc:1", "seq-key-from-acc:1")) {
            assertNull(cache.get(key), key);
        }
        assertEquals("acc:2", cache.get("acc:2"));
    }

    @Test
    void aRowThatBecomesVisibleLateIsStillForgotten() {
        fill("acc:1", "acc:2", "acc:3");
        log.visible.add(change(10, CcRecordTypes.ACC, 3, null));
        watchdog.poll();   // starts after row 10

        // 12 commits before 11, as the transaction that wrote 11 is longer
        log.visible.add(change(12, CcRecordTypes.ACC, 2, null));
        watchdog.poll();
        assertNull(cache.get("acc:2"));
        assertEquals("acc:1", cache.get("acc:1"));

        log.visible.add(change(11, CcRecordTypes.ACC, 1, null));
        watchdog.catchUp();
        long deadline = System.nanoTime() + 5_000_000_000L;
        while (cache.get("acc:1") != null && System.nanoTime() < deadline) {
            watchdog.poll();    // the gap is looked at again after a delay
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
        log.visible.add(change(5, CcRecordTypes.ACC, 1, null));
        status.bind(null);   // so that filling the cache does not read the log
        fill("acc:1");

        watchdog.poll();   // the first read starts after row 5 but looks back
        watchdog.poll();

        assertNull(cache.get("acc:1"));
    }

    @Test
    void aChangedReleaseForgetsEveryRecordOfIt() {
        log.releases.put(id(9), List.of(new RecordRef(CcRecordTypes.ACC, id(1), null), new RecordRef(CcRecordTypes.ASCC, id(2), id(1)),
                new RecordRef(CcRecordTypes.DT_SC, id(3), id(4))));
        fill("acc:1", "ascc:2", "dt_sc:3", "dt_sc-of-dt:4", "acc:77");
        watchdog.poll();

        log.visible.add(change(1, ReleaseRecordTypes.RELEASE, 9, null));
        watchdog.poll();

        for (String key : List.of("acc:1", "ascc:2", "dt_sc:3", "dt_sc-of-dt:4")) {
            assertNull(cache.get(key), key);
        }
        assertEquals("acc:77", cache.get("acc:77"), "a record of another release stays");
    }

    @Test
    void aChangeOfTheDependenciesOfReleasesForgetsTheirScopes() {
        log.releaseIds.addAll(List.of(id(1), id(2)));
        fill("scope:1", "scope:2", "acc:1");
        watchdog.poll();

        log.visible.add(change(1, ReleaseRecordTypes.RELEASE_DEP, 2, null));
        watchdog.poll();

        assertNull(cache.get("scope:1"));
        assertNull(cache.get("scope:2"));
        assertEquals("acc:1", cache.get("acc:1"));
    }

    @Test
    void theCacheIsUsedOnlyWhileTheLogCanBeRead() {
        log.failing = true;
        watchdog.poll();
        assertTrue(cache.isBypassed(), "without the change log nothing would be forgotten");

        log.failing = false;
        watchdog.poll();
        assertFalse(cache.isBypassed());
    }

    @Test
    void aForgettingThatFailsIsTriedAgainWithTheSameRows() {
        fill("acc:1");
        watchdog.poll();
        log.visible.add(change(1, CcRecordTypes.ACC, 1, null));
        RecordCache flaky = new RecordCache() {
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
        RecordCacheStatus status2 = new RecordCacheStatus();
        RecordChangeWatchdog feed2 = ChangeFeed.watchdog(factory(log), List.of(TestRecordKeys.resolver(log)), flaky, status2);
        feed2.poll();                       // starts after row 1 and looks back at it: the first forgetting fails
        assertEquals("acc:1", cache.get("acc:1"));
        feed2.poll();                       // and is tried again
        assertNull(cache.get("acc:1"));
    }

    /** A cache that counts what it is told to forget. */
    private static final class Counting implements RecordCache {
        final RecordCache inner;
        final List<Collection<String>> calls = new ArrayList<>();

        Counting(RecordCache inner) {
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
        RecordCacheStatus status2 = new RecordCacheStatus();
        RecordChangeWatchdog counted = ChangeFeed.watchdog(factory(log), List.of(TestRecordKeys.resolver(log)), counting, status2);
        status2.bind(null);
        counted.poll();                                           // starts: nothing in the log
        log.visible.add(change(1, CcRecordTypes.ACC, 1, null));
        log.visible.add(change(3, CcRecordTypes.ACC, 3, null));    // 2 is a gap

        counted.poll();
        assertEquals(1, counting.calls.size());
        assertEquals(Set.of("acc:1", "tags:acc:1", "ascc-from-acc:1", "bcc-from-acc:1", "seq-key-from-acc:1",
                "acc:3", "tags:acc:3", "ascc-from-acc:3", "bcc-from-acc:3", "seq-key-from-acc:3"), counting.calls.get(0));

        for (int i = 0; i < 5; i++) {
            counted.poll();
        }
        assertEquals(1, counting.calls.size(), "nothing new in the log, nothing forgotten");

        log.visible.add(change(2, CcRecordTypes.ACC, 2, null));    // the gap fills
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
        assertTrue(RecordChangeWatchdog.RETENTION.toNanos() > RecordChangeWatchdog.GAP_LIFETIME_NANOS,
                "a row that is deleted before the watchdog stops looking for it could be missed");
    }

    @Test
    void numbersThatGoBackAreReadFromTheNewEnd() {
        fill("acc:1", "acc:2");
        log.visible.add(change(5000, CcRecordTypes.ACC, 9, null));
        watchdog.poll();                       // the cursor is at 5000

        log.visible.clear();               // the table was emptied and its numbers start again
        log.visible.add(change(1, CcRecordTypes.ACC, 1, null));
        log.visible.add(change(2, CcRecordTypes.ACC, 2, null));
        watchdog.poll();

        assertNull(cache.get("acc:1"), "a change with a number below the old cursor is not missed");
        assertNull(cache.get("acc:2"));
        assertFalse(cache.isBypassed());

        log.visible.add(change(3, CcRecordTypes.ACC, 3, null));
        fill("acc:3");
        watchdog.poll();
        assertNull(cache.get("acc:3"), "and the next one is read as usual");
    }

    @Test
    void anEmptiedTableIsReadFromItsNewEnd() {
        log.visible.add(change(100, CcRecordTypes.ACC, 9, null));
        watchdog.poll();
        log.visible.clear();
        watchdog.poll();
        fill("acc:1");

        log.visible.add(change(1, CcRecordTypes.ACC, 1, null));
        watchdog.poll();

        assertNull(cache.get("acc:1"));
    }

    @Test
    void anEmptyLogPutsTheCursorBackSoThatNumbersFromOneAreRead() {
        fill("acc:1");
        log.visible.add(change(5000, CcRecordTypes.ACC, 9, null));
        watchdog.poll();                       // the cursor is at 5000
        log.visible.clear();               // the old rows were deleted
        watchdog.poll();                       // the watchdog sees the empty log

        log.visible.add(change(1, CcRecordTypes.ACC, 1, null));    // and the numbers start again
        log.visible.add(change(2, CcRecordTypes.ACC, 2, null));
        watchdog.poll();

        assertNull(cache.get("acc:1"));
        assertEquals(0, log.restarts, "the watchdog does not restart the numbers itself, only the housekeeping does");
    }

    @Test
    void theNumbersAreRestartedOnlyAfterTheLogHasStayedEmptyForAWhile() {
        watchdog.restartAfterEmptyNanos = 5_000_000_000L;
        watchdog.poll();                       // empty: seen now
        watchdog.purge();
        assertEquals(0, log.restarts, "not yet");

        watchdog.restartAfterEmptyNanos = 0;
        watchdog.purge();
        assertEquals(1, log.restarts, "empty for long enough");

        log.visible.add(change(1, CcRecordTypes.ACC, 1, null));
        watchdog.poll();                       // a row: the log is not empty any more
        watchdog.purge();
        assertEquals(1, log.restarts, "never while there are rows (the numbers would go back under the cursors)");
    }

    @Test
    void aLogThatWasNeverSeenEmptyIsNeverRestarted() {
        log.visible.add(change(7, CcRecordTypes.ACC, 1, null));
        watchdog.restartAfterEmptyNanos = 0;
        watchdog.poll();
        watchdog.purge();
        assertEquals(0, log.restarts);
    }

    @Test
    void anXbtForgetsItsReleaseListAndThePrimitivesThatShowItsName() {
        log.showing.put(XbtRecordTypes.XBT, List.of(new RecordRef(CcRecordTypes.DT, id(5), null), new RecordRef(CcRecordTypes.DT_SC, id(6), id(5))));
        fill("xbts:9", "xbts:10", "dt-awd-pri:5", "dt_sc-awd-pri:6", "dt-awd-pri:7", "dt:5");
        watchdog.poll();

        log.visible.add(new ChangeLogEntry(1, XbtRecordTypes.XBT, id(77), null, id(9)));
        watchdog.poll();

        assertNull(cache.get("xbts:9"));
        assertNull(cache.get("dt-awd-pri:5"));
        assertNull(cache.get("dt_sc-awd-pri:6"));
        assertNull(cache.get("dt:5"), "the data type itself goes with it");
        assertEquals("xbts:10", cache.get("xbts:10"), "the XBTs of another release");
        assertEquals("dt-awd-pri:7", cache.get("dt-awd-pri:7"), "a data type that does not use the XBT");
    }

    @Test
    void aNamespaceAndTheAllowedPrimitivesOfADataTypeAreForgotten() {
        fill("namespace:3", "namespace:4", "dt-awd-pri:8", "dt_sc-awd-pri:9", "dt_sc-of-dt:8");
        watchdog.poll();

        log.visible.add(change(1, NamespaceRecordTypes.NAMESPACE, 3, null));
        log.visible.add(change(2, CcRecordTypes.DT, 8, null));
        log.visible.add(change(3, CcRecordTypes.DT_SC, 9, 8L));
        watchdog.poll();

        assertNull(cache.get("namespace:3"));
        assertEquals("namespace:4", cache.get("namespace:4"));
        assertNull(cache.get("dt-awd-pri:8"));
        assertNull(cache.get("dt_sc-awd-pri:9"));
    }

    @Test
    void aCodeListAndAnAgencyIdListAreForgottenAndSoAreThePrimitivesThatShowTheirNames() {
        log.showing.put(CodeListRecordTypes.CODE_LIST, List.of(new RecordRef(CcRecordTypes.DT, id(5), null)));
        log.showing.put(AgencyIdListRecordTypes.AGENCY_ID_LIST, List.of(new RecordRef(CcRecordTypes.DT_SC, id(6), id(5))));
        fill("code-list:1", "code-list:2", "agency-id-list:3", "agency-id-list:4", "dt-awd-pri:5", "dt_sc-awd-pri:6", "dt-awd-pri:7");
        watchdog.poll();

        log.visible.add(change(1, CodeListRecordTypes.CODE_LIST, 1, null));
        log.visible.add(change(2, AgencyIdListRecordTypes.AGENCY_ID_LIST, 3, null));
        watchdog.poll();

        assertNull(cache.get("code-list:1"));
        assertNull(cache.get("agency-id-list:3"));
        assertNull(cache.get("dt-awd-pri:5"));
        assertNull(cache.get("dt_sc-awd-pri:6"));
        assertEquals("code-list:2", cache.get("code-list:2"));
        assertEquals("agency-id-list:4", cache.get("agency-id-list:4"));
        assertEquals("dt-awd-pri:7", cache.get("dt-awd-pri:7"));
    }
}
