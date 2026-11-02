package org.oagi.score.gateway.http.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The {@link RecordCache} in the memory of this JVM, on Caffeine; the default provider ({@code spring.cache.type=caffeine}). Its size
 * and expiry are those of {@code spring.cache.caffeine.spec}. Spring's cache abstraction is not used: it has no epoch and no read of
 * many keys at once.
 * <p>
 * Every instance has a cache of its own and forgets the same keys.
 */
@Component
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "caffeine", matchIfMissing = true)
public class CaffeineRecordCache implements RecordCache {

    private static final Logger logger = LoggerFactory.getLogger(CaffeineRecordCache.class);

    private final Cache<String, Object> cache;
    private final RecordCacheStatus status;
    private final AtomicLong epoch = new AtomicLong();
    /** Stores share it; forgetting is exclusive, so that no store lands between the epoch changing and the forgetting. */
    private final ReentrantReadWriteLock storeLock = new ReentrantReadWriteLock();
    private final SingleFlight singleFlight = new SingleFlight();

    /** @param spec the Caffeine spec: {@code spring.cache.caffeine.spec}, e.g. {@code maximumSize=200000,expireAfterWrite=600s} */
    @Autowired
    public CaffeineRecordCache(@Value("${spring.cache.caffeine.spec:maximumSize=200000,expireAfterWrite=600s}") String spec,
                               RecordCacheStatus status) {
        this.cache = Caffeine.from(spec).build();
        this.status = status;
    }

    @Override
    public long epoch() {
        status.freshen();
        return epoch.get();
    }

    @Override
    public Object get(String key) {
        return cache.getIfPresent(key);
    }

    @Override
    public Map<String, Object> getAll(Collection<String> keys) {
        return cache.getAllPresent(keys);
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
                    cache.put(key, value);
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
            cache.invalidateAll(keys);
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

    /** The keys that are in the cache now; for tests. */
    public Set<String> keys() {
        cache.cleanUp();
        return new TreeSet<>(cache.asMap().keySet());
    }
}
