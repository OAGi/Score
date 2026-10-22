package org.oagi.score.gateway.http.api.cc_management.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.tag_management.model.TagId;
import org.oagi.score.gateway.http.api.tag_management.model.TagSummaryRecord;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigInteger;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** Needs a Redis at localhost:6379 (or -Dscore.redis.host / -Dscore.redis.port); skipped without one. */
class RedisCcCacheTest {

    private LettuceConnectionFactory factory;
    private String prefix;
    private RedisCcCache cache;

    /**
     * The topology to test against: -Dscore.redis.cluster.nodes=host:port,... (a cluster),
     * -Dscore.redis.sentinel.master=name -Dscore.redis.sentinel.nodes=host:port,... (Sentinel),
     * or else a standalone Redis.
     */
    private static LettuceConnectionFactory factory(String host, int port) {
        String clusterNodes = System.getProperty("score.redis.cluster.nodes", "");
        String sentinelMaster = System.getProperty("score.redis.sentinel.master", "");
        org.springframework.data.redis.connection.RedisConfiguration configuration;
        if (!clusterNodes.isBlank()) {
            configuration = new org.springframework.data.redis.connection.RedisClusterConfiguration(List.of(clusterNodes.split(",")));
        } else if (!sentinelMaster.isBlank()) {
            configuration = new org.springframework.data.redis.connection.RedisSentinelConfiguration(
                    sentinelMaster, new HashSet<>(List.of(System.getProperty("score.redis.sentinel.nodes", "").split(","))));
        } else {
            configuration = new RedisStandaloneConfiguration(host, port);
        }
        LettuceConnectionFactory factory = new LettuceConnectionFactory(configuration);
        factory.afterPropertiesSet();
        return factory;
    }

    @BeforeEach
    void connect() {
        factory = factory(System.getProperty("score.redis.host", "localhost"), Integer.getInteger("score.redis.port", 6379));
        boolean available;
        try (RedisConnection connection = factory.getConnection()) {
            available = "PONG".equals(connection.ping());
        } catch (RuntimeException e) {
            available = false;
        }
        Assumptions.assumeTrue(available, "no Redis to test with");
        prefix = "{zz-cc-test:" + UUID.randomUUID() + "}:";
        cache = new RedisCcCache(factory, prefix, org.oagi.score.gateway.http.api.cc_management.cache.CcCacheStatus.alwaysReady(), Duration.ofSeconds(60));
    }

    @AfterEach
    void cleanUp() {
        if (factory != null) {
            try (RedisConnection connection = factory.getConnection()) {
                Set<byte[]> keys = connection.keyCommands().keys((prefix + "*").getBytes());
                if (keys != null && !keys.isEmpty()) {
                    connection.keyCommands().del(keys.toArray(new byte[0][]));
                }
            } catch (RuntimeException e) {
                // nothing to clean up
            }
            factory.destroy();
        }
        TransactionSynchronizationManager.clear();
        WriteTracker.reset();
    }

    private static TagSummaryRecord tag(long id) {
        return new TagSummaryRecord(new TagId(BigInteger.valueOf(id)), "tag " + id, "#fff", "#000");
    }

    @Test
    void whatIsPutComesBackInBulkAndAMissingKeyIsNotInTheResult() {
        long epoch = cache.epoch();
        cache.putAll(epoch, Map.of("tags:acc:1", List.of(tag(1), tag(2)), "acc:2", tag(3)));

        Map<String, Object> found = cache.getAll(List.of("tags:acc:1", "acc:2", "acc:9"));

        assertEquals(Set.of("tags:acc:1", "acc:2"), found.keySet());
        assertEquals(List.of(tag(1), tag(2)), found.get("tags:acc:1"));
        assertEquals(tag(3), cache.get("acc:2"));
        assertNull(cache.get("acc:9"));
    }

    @Test
    void manyKeysAreReadAndWrittenInSeveralCommands() {
        long epoch = cache.epoch();
        Map<String, Object> values = new HashMap<>();
        for (int i = 0; i < 2500; i++) {
            values.put("acc:" + i, tag(i));
        }
        cache.putAll(epoch, values);

        assertEquals(2500, cache.getAll(values.keySet()).size());
    }

    @Test
    void forgettingTouchesOnlyTheNamedEntriesAndRejectsWhatWasReadBefore() {
        long before = cache.epoch();
        cache.putAll(before, Map.of("acc:1", tag(1), "acc:2", tag(2)));

        cache.invalidate(List.of("acc:1", "acc:404"));

        long after = cache.epoch();
        assertTrue(after > before);
        assertNull(cache.get("acc:1"));
        assertEquals(tag(2), cache.get("acc:2"), "what did not change stays");
        // what a reader that started before the change stores afterwards is refused
        cache.put(before, "acc:1", tag(9));
        assertNull(cache.get("acc:1"));
        cache.put(after, "acc:1", tag(1));
        assertEquals(tag(1), cache.get("acc:1"));
    }

    @Test
    void theEpochAndWhatWasForgottenAreSharedByEveryInstanceOfTheCache() {
        RedisCcCache other = new RedisCcCache(factory, prefix, CcCacheStatus.alwaysReady(), Duration.ofSeconds(60));
        long before = other.epoch();
        cache.putAll(cache.epoch(), Map.of("acc:1", tag(1)));
        assertEquals(tag(1), other.get("acc:1"));

        cache.invalidate(List.of("acc:1"));

        assertTrue(other.epoch() > before);
        assertNull(other.get("acc:1"));
    }

    @Test
    void aTransactionBypassesTheCacheFromItsFirstWriteUntilItEnds() {
        TransactionSynchronizationManager.initSynchronization();

        assertFalse(cache.isBypassed());
        WriteTracker.written(() -> {
        });
        assertTrue(cache.isBypassed());

        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        }
        TransactionSynchronizationManager.clearSynchronization();

        assertFalse(cache.isBypassed());
    }

    @Test
    void anEpochNeverGoesBackAfterTheKeysOfRedisAreGone() {
        cache.invalidate(List.of());
        long reached = cache.epoch();
        cache.putAll(reached, Map.of("acc:1", tag(1)));

        try (RedisConnection connection = factory.getConnection()) {
            // what FLUSHALL does at the start of an instance
            connection.keyCommands().del((prefix + "epoch").getBytes(), (prefix + "acc:1").getBytes());
        }

        long afterwards = cache.epoch();
        assertTrue(afterwards > reached, "epoch " + afterwards + " must be after " + reached);
        assertNull(cache.get("acc:1"));
        // a reader that took the epoch before the flush cannot store after it
        cache.put(reached, "acc:2", tag(2));
        assertNull(cache.get("acc:2"));
        assertEquals(afterwards, cache.epoch());
    }

    @Test
    void forgettingFailsWhenRedisCannotBeReachedSoThatTheFeedTriesAgain() {
        LettuceConnectionFactory dead = new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 1));
        dead.afterPropertiesSet();
        RedisCcCache broken = new RedisCcCache(dead, "zz-cc-test:dead:", CcCacheStatus.alwaysReady(), Duration.ofSeconds(60));

        assertThrows(RuntimeException.class, () -> broken.invalidate(List.of("acc:1")));
        dead.destroy();
    }

    @Test
    void aRedisThatDoesNotAnswerIsAMissAndTheCacheIsBypassed() {
        LettuceConnectionFactory dead = new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 1));
        dead.afterPropertiesSet();
        RedisCcCache broken = new RedisCcCache(dead, "zz-cc-test:dead:", CcCacheStatus.alwaysReady(), Duration.ofSeconds(60));

        long epoch = broken.epoch();

        assertTrue(epoch < 0);
        assertTrue(broken.isBypassed());
        assertNull(broken.get("acc:1"));
        assertTrue(broken.getAll(List.of("acc:1")).isEmpty());
        assertDoesNotThrow(() -> broken.put(epoch, "acc:1", tag(1)));
        dead.destroy();
    }

}
