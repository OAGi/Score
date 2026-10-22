package org.oagi.score.gateway.http.api.cc_management.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The {@link CcCache} on top of the Spring cache named {@value #CACHE_NAME}, for the providers that live in the JVM
 * (Caffeine, the default). Which provider that is, and its size and expiry, is Spring Boot's to decide:
 * {@code spring.cache.type} and the properties of that provider, e.g. {@code spring.cache.caffeine.spec}.
 * (With {@code spring.cache.type=redis} the cache is a {@link RedisCcCache} instead, which reads in bulk.)
 * <p>
 * What Spring's cache does not offer is kept here: the epoch that keeps a value read around a change out of the cache,
 * and the bypass of a transaction that has written. Every instance has a cache of its own, and each forgets what changed
 * because each reads the change log.
 * <p>
 * A provider that fails is a miss, never an error: the caller reads the database.
 */
@Component
@ConditionalOnExpression("!'${spring.cache.type:caffeine}'.equalsIgnoreCase('redis')")
public class SpringCcCache implements CcCache {

    static final String CACHE_NAME = "cc-records";

    private static final Logger logger = LoggerFactory.getLogger(SpringCcCache.class);

    private final Cache cache;
    private final CcCacheStatus status;
    private final AtomicLong epoch = new AtomicLong();
    /** Stores share it; forgetting is exclusive, so that no store lands between the epoch changing and the forgetting. */
    private final ReentrantReadWriteLock storeLock = new ReentrantReadWriteLock();
    private final SingleFlight singleFlight = new SingleFlight();

    public SpringCcCache(CacheManager cacheManager, CcCacheStatus status) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            throw new IllegalStateException("The Spring cache '" + CACHE_NAME + "' is not available "
                    + "(spring.cache.type=none turns every cache off).");
        }
        this.cache = cache;
        this.status = status;
    }

    @Override
    public long epoch() {
        status.freshen();
        return epoch.get();
    }

    @Override
    public Object get(String key) {
        try {
            Cache.ValueWrapper wrapper = cache.get(key);
            return (wrapper != null) ? wrapper.get() : null;
        } catch (RuntimeException e) {
            logger.warn("The CC cache could not read '{}'; reading the database instead.", key, e);
            return null;
        }
    }

    @Override
    public Map<String, Object> getAll(Collection<String> keys) {
        Map<String, Object> found = new HashMap<>();
        for (String key : keys) {
            Object value = get(key);
            if (value != null) {
                found.put(key, value);
            }
        }
        return found;
    }

    @Override
    public void put(long epoch, String key, Object value) {
        if (value != null) {
            putAll(epoch, Map.of(key, value));
        }
    }

    @Override
    public void putAll(long epoch, Map<String, Object> values) {
        storeLock.readLock().lock();
        try {
            if (this.epoch.get() != epoch) {
                return;
            }
            values.forEach((key, value) -> {
                if (value != null) {
                    try {
                        cache.put(key, value);
                    } catch (RuntimeException e) {
                        logger.warn("The CC cache could not hold '{}'.", key, e);
                    }
                }
            });
        } finally {
            storeLock.readLock().unlock();
        }
    }

    @Override
    public void invalidate(Collection<String> keys) {
        storeLock.writeLock().lock();
        try {
            epoch.incrementAndGet();
            keys.forEach(cache::evict);
        } finally {
            storeLock.writeLock().unlock();
        }
    }

    @Override
    public boolean isBypassed() {
        return WriteTracker.isDirty() || !status.isReady();
    }

    @Override
    public void singleFlight(String key, Runnable task) {
        singleFlight.run(key, task);
    }

}
