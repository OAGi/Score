package org.oagi.score.gateway.http.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CaffeineRecordCacheTest {

    private CaffeineRecordCache cache() {
        return new CaffeineRecordCache("", RecordCacheStatus.alwaysReady());
    }

    @AfterEach
    void clearTransaction() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        WriteTracker.reset();
    }

    @Test
    void aValueIsCachedUnderTheEpochItWasReadIn() {
        CaffeineRecordCache cache = cache();
        cache.put(cache.epoch(), "acc:1", "a");
        assertEquals("a", cache.get("acc:1"));
        assertNull(cache.get("acc:2"));
    }

    @Test
    void forgettingTouchesOnlyTheNamedEntriesAndRejectsWhatWasReadBefore() {
        CaffeineRecordCache cache = cache();
        long readIn = cache.epoch();
        cache.putAll(readIn, java.util.Map.of("acc:1", "a", "acc:2", "b", "acc:3", "c"));

        cache.invalidate(java.util.List.of("acc:1"));

        assertNull(cache.get("acc:1"));
        assertEquals("b", cache.get("acc:2"), "what did not change stays");
        assertEquals("c", cache.get("acc:3"));
        cache.put(readIn, "acc:1", "stale");
        assertNull(cache.get("acc:1"), "a reader that began before cannot put its value back");
        cache.put(cache.epoch(), "acc:1", "fresh");
        assertEquals("fresh", cache.get("acc:1"));
    }

    @Test
    void aTransactionBypassesTheCacheFromItsFirstWriteUntilItEnds() {
        CaffeineRecordCache cache = cache();
        cache.put(cache.epoch(), "acc:1", "a");
        TransactionSynchronizationManager.initSynchronization();

        assertFalse(cache.isBypassed());
        WriteTracker.written(() -> {
        });
        assertTrue(cache.isBypassed());
        assertEquals("a", cache.get("acc:1"), "the other requests still see the committed state");

        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        }
        TransactionSynchronizationManager.clearSynchronization();

        assertFalse(cache.isBypassed());
    }

    @Test
    void withoutAFeedThatKeepsUpTheCacheIsBypassed() {
        RecordCacheStatus status = new RecordCacheStatus();
        CaffeineRecordCache cache = new CaffeineRecordCache("", status);

        assertTrue(cache.isBypassed(), "nothing has read the change log yet");
        status.succeeded();
        assertFalse(cache.isBypassed());
        status.failed();
        assertTrue(cache.isBypassed());
    }

    @Test
    void theSpecOfTheProviderLimitsTheSize() {
        CaffeineRecordCache cache = new CaffeineRecordCache("maximumSize=2", RecordCacheStatus.alwaysReady());
        for (int i = 0; i < 50; i++) {
            cache.put(cache.epoch(), "acc:" + i, "a" + i);
        }
        cache.get("acc:0");   // a read lets the cache catch up with its eviction

        org.junit.jupiter.api.Assertions.assertTrue(cache.keys().size() <= 3, "kept " + cache.keys().size());
    }

    @Test
    void requestsThatFindTheCacheEmptyTogetherDoTheWorkOnce() throws Exception {
        CaffeineRecordCache cache = cache();
        java.util.concurrent.atomic.AtomicInteger runs = new java.util.concurrent.atomic.AtomicInteger();
        java.util.concurrent.CountDownLatch started = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(4);
        try {
            java.util.concurrent.Future<?> leader = pool.submit(() -> cache.singleFlight("dependencies:acc:1", () -> {
                runs.incrementAndGet();
                started.countDown();
                try {
                    release.await();
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
                cache.put(cache.epoch(), "acc:1", "a");
            }));
            started.await();
            java.util.List<java.util.concurrent.Future<Object>> waiters = new java.util.ArrayList<>();
            for (int i = 0; i < 3; i++) {
                waiters.add(pool.submit(() -> {
                    cache.singleFlight("dependencies:acc:1", runs::incrementAndGet);
                    return cache.get("acc:1");
                }));
            }
            Thread.sleep(100);
            assertTrue(waiters.stream().noneMatch(java.util.concurrent.Future::isDone), "they wait for the one that runs");
            release.countDown();
            leader.get(5, java.util.concurrent.TimeUnit.SECONDS);
            for (java.util.concurrent.Future<Object> waiter : waiters) {
                assertEquals("a", waiter.get(5, java.util.concurrent.TimeUnit.SECONDS), "and then read what it cached");
            }
            assertEquals(1, runs.get());

            cache.singleFlight("dependencies:acc:1", runs::incrementAndGet);
            assertEquals(2, runs.get(), "a later request runs it again");
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void aTaskThatFailsFailsOnlyItsOwnCallerAndFreesTheKey() {
        CaffeineRecordCache cache = cache();
        assertThrows(IllegalStateException.class, () -> cache.singleFlight("k", () -> {
            throw new IllegalStateException("boom");
        }));
        java.util.concurrent.atomic.AtomicInteger runs = new java.util.concurrent.atomic.AtomicInteger();
        cache.singleFlight("k", runs::incrementAndGet);
        assertEquals(1, runs.get());
    }

    @Test
    void aBareFactoryReadsTheDatabaseEveryTime() {
        java.util.concurrent.atomic.AtomicInteger runs = new java.util.concurrent.atomic.AtomicInteger();
        RecordCache.DISABLED.singleFlight("k", runs::incrementAndGet);
        assertEquals(1, runs.get());
        assertTrue(RecordCache.DISABLED.isBypassed());
        RecordCache.DISABLED.put(0, "acc:1", "a");
        assertNull(RecordCache.DISABLED.get("acc:1"));
    }

}
