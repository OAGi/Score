package org.oagi.score.gateway.http.api.cc_management.cache;

import jakarta.annotation.PostConstruct;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The {@link CcCache} on top of the Spring cache named {@value #CACHE_NAME}, for the providers that live in the JVM
 * (Caffeine, the default). Which provider that is, and its size and expiry, is Spring Boot's to decide:
 * {@code spring.cache.type} and the properties of that provider, e.g. {@code spring.cache.caffeine.spec}.
 * (With {@code spring.cache.type=redis} the cache is a {@link RedisCcCache} instead, which reads in bulk.)
 * <p>
 * What Spring's cache does not offer is kept here: the generation that keeps a value read around a write out of the
 * cache, and the bypass of a transaction that has written. A write empties the whole cache once it is committed;
 * the other instances are told through a Redis topic.
 * <p>
 * A provider that fails is a miss, never an error: the caller reads the database.
 */
@Component
@ConditionalOnExpression("!'${spring.cache.type:caffeine}'.equalsIgnoreCase('redis')")
public class SpringCcCache implements CcCache, CcCacheWriteObserver {

    static final String CACHE_NAME = "cc-records";

    private static final Logger logger = LoggerFactory.getLogger(SpringCcCache.class);

    private static final String TOPIC_NAME = "score:cc-cache:invalidate";

    private final Cache cache;
    private final AtomicLong generation = new AtomicLong();
    private final String instanceId = UUID.randomUUID().toString();
    private final ObjectProvider<RedissonClient> redissonProvider;
    private final SingleFlight singleFlight = new SingleFlight();

    public SpringCcCache(CacheManager cacheManager, ObjectProvider<RedissonClient> redissonProvider) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            throw new IllegalStateException("The Spring cache '" + CACHE_NAME + "' is not available "
                    + "(spring.cache.type=none turns every cache off).");
        }
        this.cache = cache;
        this.redissonProvider = redissonProvider;
    }

    @PostConstruct
    void listenToOtherInstances() {
        RedissonClient redisson = redissonProvider.getIfAvailable();
        if (redisson == null) {
            return;
        }
        try {
            redisson.getTopic(TOPIC_NAME).addListener(String.class, (channel, sender) -> {
                if (!instanceId.equals(sender)) {
                    invalidateAll();
                }
            });
        } catch (RuntimeException e) {
            logger.warn("The CC cache cannot hear the other instances; its entries expire on their own instead.", e);
        }
    }

    @Override
    public long generation() {
        return generation.get();
    }

    @Override
    public Object get(long generation, String key) {
        try {
            Cache.ValueWrapper wrapper = cache.get(key);
            return (wrapper != null) ? wrapper.get() : null;
        } catch (RuntimeException e) {
            logger.warn("The CC cache could not read '{}'; reading the database instead.", key, e);
            return null;
        }
    }

    @Override
    public Map<String, Object> getAll(long generation, Collection<String> keys) {
        Map<String, Object> found = new HashMap<>();
        for (String key : keys) {
            Object value = get(generation, key);
            if (value != null) {
                found.put(key, value);
            }
        }
        return found;
    }

    @Override
    public void put(long generation, String key, Object value) {
        if (value == null || this.generation.get() != generation) {
            return;
        }
        try {
            cache.put(key, value);
        } catch (RuntimeException e) {
            logger.warn("The CC cache could not hold '{}'.", key, e);
        }
    }

    @Override
    public void putAll(long generation, Map<String, Object> values) {
        values.forEach((key, value) -> put(generation, key, value));
    }

    @Override
    public boolean isBypassed() {
        return WriteTracker.isDirty();
    }

    @Override
    public void singleFlight(String key, Runnable task) {
        singleFlight.run(key, task);
    }

    @Override
    public void written() {
        WriteTracker.written(this::invalidateAndNotify);
    }

    private void invalidateAndNotify() {
        invalidateAll();
        RedissonClient redisson = redissonProvider.getIfAvailable();
        if (redisson == null) {
            return;
        }
        try {
            RTopic topic = redisson.getTopic(TOPIC_NAME);
            topic.publish(instanceId);
        } catch (RuntimeException e) {
            logger.warn("The other instances were not told to empty their CC cache; their entries expire on their own.", e);
        }
    }

    private void invalidateAll() {
        generation.incrementAndGet();
        try {
            cache.clear();
        } catch (RuntimeException e) {
            logger.warn("The CC cache could not be emptied; its entries expire on their own.", e);
        }
    }

}
