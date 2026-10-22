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
        cache = new RedisCcCache(factory, prefix, Duration.ofSeconds(60));
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
        long generation = cache.generation();
        cache.putAll(generation, Map.of("tags:acc:1", List.of(tag(1), tag(2)), "acc:2", tag(3)));

        Map<String, Object> found = cache.getAll(generation, List.of("tags:acc:1", "acc:2", "acc:9"));

        assertEquals(Set.of("tags:acc:1", "acc:2"), found.keySet());
        assertEquals(List.of(tag(1), tag(2)), found.get("tags:acc:1"));
        assertEquals(tag(3), cache.get(generation, "acc:2"));
        assertNull(cache.get(generation, "acc:9"));
    }

    @Test
    void manyKeysAreReadAndWrittenInSeveralCommands() {
        long generation = cache.generation();
        Map<String, Object> values = new HashMap<>();
        for (int i = 0; i < 2500; i++) {
            values.put("acc:" + i, tag(i));
        }
        cache.putAll(generation, values);

        assertEquals(2500, cache.getAll(generation, values.keySet()).size());
    }

    @Test
    void aWriteStartsANewGenerationThatNobodyOfTheOldOneCanSee() {
        long before = cache.generation();
        cache.put(before, "acc:1", tag(1));

        cache.written();

        long after = cache.generation();
        assertEquals(before + 1, after);
        assertNull(cache.get(after, "acc:1"));
        // what a reader that started before the write stores afterwards goes where nobody looks
        cache.put(before, "acc:2", tag(2));
        assertNull(cache.get(after, "acc:2"));
    }

    @Test
    void theGenerationIsSharedByEveryInstanceOfTheCache() {
        RedisCcCache other = new RedisCcCache(factory, prefix, Duration.ofSeconds(60));
        long before = other.generation();

        cache.written();

        assertEquals(before + 1, other.generation());
    }

    @Test
    void aTransactionBypassesTheCacheFromItsFirstWriteAndStartsTheGenerationWhenItCommits() {
        long before = cache.generation();
        TransactionSynchronizationManager.initSynchronization();

        assertFalse(cache.isBypassed());
        cache.written();
        assertTrue(cache.isBypassed());
        assertEquals(before, cache.generation());

        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        }
        TransactionSynchronizationManager.clearSynchronization();

        assertFalse(cache.isBypassed());
        assertEquals(before + 1, cache.generation());
    }

    @Test
    void aGenerationNeverGoesBackAfterTheKeysOfRedisAreGone() {
        cache.written();
        long before = cache.generation();
        cache.putAll(before, Map.of("acc:1", tag(1)));
        TransactionSynchronizationManager.initSynchronization();
        cache.written();
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        }
        TransactionSynchronizationManager.clearSynchronization();
        long reached = cache.generation();

        try (RedisConnection connection = factory.getConnection()) {
            // what FLUSHALL does at the start of an instance
            connection.keyCommands().del((prefix + "generation").getBytes(), (prefix + reached + ":acc:1").getBytes());
        }

        long afterwards = cache.generation();
        assertTrue(afterwards > reached, "generation " + afterwards + " must be after " + reached);
        assertNull(cache.get(afterwards, "acc:1"));
        assertEquals(afterwards, cache.generation());
    }

    @Test
    void aRedisThatDoesNotAnswerIsAMissAndTheCacheIsBypassed() {
        LettuceConnectionFactory dead = new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 1));
        dead.afterPropertiesSet();
        RedisCcCache broken = new RedisCcCache(dead, "zz-cc-test:dead:", Duration.ofSeconds(60));

        long generation = broken.generation();

        assertTrue(generation < 0);
        assertTrue(broken.isBypassed());
        assertNull(broken.get(generation, "acc:1"));
        assertTrue(broken.getAll(generation, List.of("acc:1")).isEmpty());
        assertDoesNotThrow(() -> broken.put(generation, "acc:1", tag(1)));
        assertDoesNotThrow(broken::written);
        dead.destroy();
    }

}
