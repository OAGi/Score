package org.oagi.score.gateway.http.api.cc_management.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpringCcCacheTest {

    private final ObjectProvider<RedissonClient> noRedis = new StaticListableBeanFactory().getBeanProvider(RedissonClient.class);

    private SpringCcCache cache() {
        return new SpringCcCache(new ConcurrentMapCacheManager(), noRedis);
    }

    @AfterEach
    void clearTransaction() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        WriteTracker.reset();
    }

    private void completeTransaction(int status) {
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCompletion(status);
        }
        TransactionSynchronizationManager.clearSynchronization();
    }

    @Test
    void aValueIsCachedUnderTheGenerationItWasReadIn() {
        SpringCcCache cache = cache();
        cache.put(cache.generation(), "acc:1", "a");
        assertEquals("a", cache.get(cache.generation(), "acc:1"));
        assertNull(cache.get(cache.generation(), "acc:2"));
    }

    @Test
    void aWriteOutsideATransactionEmptiesTheCacheAndRejectsTheValueReadBefore() {
        SpringCcCache cache = cache();
        long readIn = cache.generation();
        cache.put(readIn, "acc:1", "a");

        cache.written();

        assertNull(cache.get(cache.generation(), "acc:1"));
        cache.put(readIn, "acc:1", "stale");
        assertNull(cache.get(cache.generation(), "acc:1"));
        cache.put(cache.generation(), "acc:1", "fresh");
        assertEquals("fresh", cache.get(cache.generation(), "acc:1"));
    }

    @Test
    void aTransactionBypassesTheCacheFromItsFirstWriteAndEmptiesItOnCommit() {
        SpringCcCache cache = cache();
        cache.put(cache.generation(), "acc:1", "a");
        TransactionSynchronizationManager.initSynchronization();

        assertFalse(cache.isBypassed());
        cache.written();
        assertTrue(cache.isBypassed());
        assertEquals("a", cache.get(cache.generation(), "acc:1"), "the other requests still see the committed state until the commit");

        completeTransaction(TransactionSynchronization.STATUS_COMMITTED);

        assertFalse(cache.isBypassed());
        assertNull(cache.get(cache.generation(), "acc:1"));
    }

    @Test
    void aRolledBackTransactionLeavesTheCacheAsItWas() {
        SpringCcCache cache = cache();
        cache.put(cache.generation(), "acc:1", "a");
        long before = cache.generation();
        TransactionSynchronizationManager.initSynchronization();

        cache.written();
        completeTransaction(TransactionSynchronization.STATUS_ROLLED_BACK);

        assertFalse(cache.isBypassed());
        assertEquals(before, cache.generation());
        assertEquals("a", cache.get(cache.generation(), "acc:1"));
    }

    @Test
    void aProviderThatFailsIsAMissAndNeverAnError() {
        Cache failing = mock(Cache.class);
        when(failing.get("acc:1")).thenThrow(new IllegalStateException("redis is down"));
        org.mockito.Mockito.doThrow(new IllegalStateException("redis is down")).when(failing).put("acc:1", "a");
        org.mockito.Mockito.doThrow(new IllegalStateException("redis is down")).when(failing).clear();
        CacheManager manager = mock(CacheManager.class);
        when(manager.getCache(SpringCcCache.CACHE_NAME)).thenReturn(failing);
        SpringCcCache cache = new SpringCcCache(manager, noRedis);

        assertNull(cache.get(cache.generation(), "acc:1"));
        assertDoesNotThrow(() -> cache.put(cache.generation(), "acc:1", "a"));
        assertDoesNotThrow(cache::written);
    }

    @Test
    void withoutACacheTheApplicationDoesNotStart() {
        CacheManager manager = mock(CacheManager.class);
        assertThrows(IllegalStateException.class, () -> new SpringCcCache(manager, noRedis));
    }

    @Test
    void requestsThatFindTheCacheEmptyTogetherDoTheWorkOnce() throws Exception {
        SpringCcCache cache = cache();
        java.util.concurrent.atomic.AtomicInteger runs = new java.util.concurrent.atomic.AtomicInteger();
        java.util.concurrent.CountDownLatch started = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(4);
        try {
            java.util.concurrent.Future<?> leader = pool.submit(() -> cache.singleFlight("graph:acc:1", () -> {
                runs.incrementAndGet();
                started.countDown();
                try {
                    release.await();
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
                cache.put(cache.generation(), "acc:1", "a");
            }));
            started.await();
            java.util.List<java.util.concurrent.Future<Object>> waiters = new java.util.ArrayList<>();
            for (int i = 0; i < 3; i++) {
                waiters.add(pool.submit(() -> {
                    cache.singleFlight("graph:acc:1", runs::incrementAndGet);
                    return cache.get(cache.generation(), "acc:1");
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

            cache.singleFlight("graph:acc:1", runs::incrementAndGet);
            assertEquals(2, runs.get(), "a later request runs it again");
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void aTaskThatFailsFailsOnlyItsOwnCallerAndFreesTheKey() {
        SpringCcCache cache = cache();
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
        CcCache.DISABLED.singleFlight("k", runs::incrementAndGet);
        assertEquals(1, runs.get());
        assertTrue(CcCache.DISABLED.isBypassed());
        CcCache.DISABLED.put(0, "acc:1", "a");
        assertNull(CcCache.DISABLED.get(0, "acc:1"));
    }

}
