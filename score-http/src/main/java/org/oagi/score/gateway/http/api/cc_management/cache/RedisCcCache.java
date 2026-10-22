package org.oagi.score.gateway.http.api.cc_management.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.info.BuildProperties;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/**
 * The {@link CcCache} in Redis ({@code spring.cache.type=redis}), with nothing of it in the JVM.
 * <p>
 * <b>Generations.</b> The generation is a counter in Redis, and it is part of every key
 * ({@code {cc:<application version>}:<generation>:<key>}; the braces make all keys of the cache one slot of a Redis cluster). A write that has been committed adds one to it, which at once
 * makes every entry of the old generation unreachable for everyone, on every instance, and the entries of it expire
 * by themselves. A reader that pinned a generation before a write can only store what it read under the old one, which
 * nobody reads any more: so there is nothing to compare when storing, and no message to send to the other instances.
 * The version of the application is part of the key as well, so a deployment starts from an empty cache.
 * <p>
 * <b>Cost.</b> The entries are read and written in bulk ({@code MGET}, a pipeline of {@code SET}), because a round trip
 * per entry takes seconds for a graph.
 * <p>
 * <b>Failure.</b> Redis that does not answer is a miss, never an error; for a few seconds afterwards the cache is
 * bypassed altogether, so that a dead Redis does not add its time-out to every lookup.
 * <p>
 * The cache shares its Redis with whatever else uses it (the sessions): it never scans or flushes it, it only counts
 * generations and gives every entry an expiry.
 */
@Component
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisCcCache implements CcCache, CcCacheWriteObserver {

    private static final Logger logger = LoggerFactory.getLogger(RedisCcCache.class);

    /** The most keys of one command. */
    private static final int CHUNK = 1000;
    /** The most entries one script call stores. */
    private static final int STORE_CHUNK = 500;
    private static final byte[] STORE_SCRIPT = (
            "for i = 1, #KEYS do redis.call('SET', KEYS[i], ARGV[i], 'PX', ARGV[#ARGV]) end return #KEYS")
            .getBytes(StandardCharsets.US_ASCII);
    private static final long BYPASS_AFTER_FAILURE_NANOS = Duration.ofSeconds(5).toNanos();

    private final RedisConnectionFactory connectionFactory;
    private final CcRecordSerializer serializer = new CcRecordSerializer();
    private final SingleFlight singleFlight = new SingleFlight();
    private final String prefix;
    private final byte[] generationKey;
    private final long ttlMillis;

    /** The {@link System#nanoTime()} until which Redis is not asked, after a failure. */
    private volatile long unavailableUntil = System.nanoTime() - 1;

    @Autowired
    public RedisCcCache(RedisConnectionFactory connectionFactory,
                        ObjectProvider<BuildProperties> buildProperties,
                        @Value("${spring.cache.redis.time-to-live:10m}") Duration timeToLive) {
        this(connectionFactory, "{cc:" + version(buildProperties) + "}:", timeToLive);
    }

    private static String version(ObjectProvider<BuildProperties> buildProperties) {
        BuildProperties properties = buildProperties.getIfAvailable();
        return (properties != null && properties.getVersion() != null) ? properties.getVersion() : "dev";
    }

    /**
     * @param prefix the start of every key, which ends with a colon. In a Redis cluster the commands that take several keys
     *               need them in one slot, so the part of the prefix that is the same for all keys is in braces
     *               (a hash tag), as the application's prefix is
     */
    public RedisCcCache(RedisConnectionFactory connectionFactory, String prefix, Duration timeToLive) {
        this.connectionFactory = connectionFactory;
        this.prefix = prefix;
        this.generationKey = (this.prefix + "generation").getBytes(StandardCharsets.UTF_8);
        this.ttlMillis = timeToLive.toMillis();
    }

    // ---- the keys ------------------------------------------------------------------------------------------------

    private byte[] keyOf(long generation, String key) {
        return (prefix + generation + ":" + key).getBytes(StandardCharsets.UTF_8);
    }

    // ---- failure -------------------------------------------------------------------------------------------------

    private boolean unavailable() {
        return System.nanoTime() - unavailableUntil < 0;
    }

    private void failed(String what, RuntimeException e) {
        boolean wasAvailable = !unavailable();
        unavailableUntil = System.nanoTime() + BYPASS_AFTER_FAILURE_NANOS;
        if (wasAvailable) {
            logger.warn("The CC cache could not {}; Redis is not asked for a few seconds and the database answers instead.", what, e);
        }
    }

    // ---- CcCache -------------------------------------------------------------------------------------------------

    @Override
    public long generation() {
        if (unavailable()) {
            return -1;
        }
        try (RedisConnection connection = connectionFactory.getConnection()) {
            byte[] value = connection.stringCommands().get(generationKey);
            if (value == null) {
                // The counter is gone (Redis was flushed, as an instance does when it starts). It starts again from the
                // time, not from zero, so that a reader that still holds a generation of before cannot meet it again.
                connection.stringCommands().set(generationKey, Long.toString(System.currentTimeMillis()).getBytes(StandardCharsets.US_ASCII),
                        Expiration.persistent(), SetOption.SET_IF_ABSENT);
                value = connection.stringCommands().get(generationKey);
            }
            return (value == null) ? 0 : Long.parseLong(new String(value, StandardCharsets.US_ASCII));
        } catch (RuntimeException e) {
            failed("read its generation", e);
            return -1;
        }
    }

    @Override
    public Object get(long generation, String key) {
        return getAll(generation, List.of(key)).get(key);
    }

    @Override
    public Map<String, Object> getAll(long generation, Collection<String> keys) {
        if (generation < 0 || keys.isEmpty() || unavailable()) {
            return Collections.emptyMap();
        }
        Map<String, Object> found = new HashMap<>();
        List<String> all = new ArrayList<>(keys);
        try (RedisConnection connection = connectionFactory.getConnection()) {
            for (int from = 0; from < all.size(); from += CHUNK) {
                List<String> chunk = all.subList(from, Math.min(from + CHUNK, all.size()));
                byte[][] redisKeys = chunk.stream().map(e -> keyOf(generation, e)).toArray(byte[][]::new);
                List<byte[]> values = connection.stringCommands().mGet(redisKeys);
                for (int i = 0; i < chunk.size(); i++) {
                    byte[] value = (values != null) ? values.get(i) : null;
                    if (value != null) {
                        Object read = read(chunk.get(i), value);
                        if (read != null) {
                            found.put(chunk.get(i), read);
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            failed("read entries", e);
            return Collections.emptyMap();
        }
        return found;
    }

    private Object read(String key, byte[] value) {
        try {
            return serializer.deserialize(value);
        } catch (RuntimeException e) {
            logger.warn("The CC cache could not read the entry '{}' and takes it for missing.", key, e);
            return null;
        }
    }

    @Override
    public void put(long generation, String key, Object value) {
        putAll(generation, Map.of(key, value));
    }

    @Override
    public void putAll(long generation, Map<String, Object> values) {
        if (generation < 0 || values.isEmpty() || unavailable()) {
            return;
        }
        List<Map.Entry<String, Object>> entries = new ArrayList<>(values.entrySet());
        try (RedisConnection connection = connectionFactory.getConnection()) {
            for (int from = 0; from < entries.size(); from += STORE_CHUNK) {
                List<byte[]> keys = new ArrayList<>();
                List<byte[]> bytes = new ArrayList<>();
                for (Map.Entry<String, Object> entry : entries.subList(from, Math.min(from + STORE_CHUNK, entries.size()))) {
                    byte[] value = write(entry.getKey(), entry.getValue());
                    if (value != null) {
                        keys.add(keyOf(generation, entry.getKey()));
                        bytes.add(value);
                    }
                }
                if (keys.size() == 1) {
                    connection.stringCommands().set(keys.get(0), bytes.get(0), Expiration.milliseconds(ttlMillis), SetOption.UPSERT);
                } else if (!keys.isEmpty()) {
                    // One round trip for the chunk, on the shared connection (a pipeline would open a connection of its own).
                    byte[][] keysAndArguments = new byte[keys.size() * 2 + 1][];
                    for (int i = 0; i < keys.size(); i++) {
                        keysAndArguments[i] = keys.get(i);
                        keysAndArguments[keys.size() + i] = bytes.get(i);
                    }
                    keysAndArguments[keys.size() * 2] = Long.toString(ttlMillis).getBytes(StandardCharsets.US_ASCII);
                    connection.scriptingCommands().eval(STORE_SCRIPT, ReturnType.INTEGER, keys.size(), keysAndArguments);
                }
            }
        } catch (RuntimeException e) {
            failed("store entries", e);
        }
    }

    private byte[] write(String key, Object value) {
        try {
            return serializer.serialize(value);
        } catch (RuntimeException e) {
            logger.warn("The CC cache could not store the entry '{}'.", key, e);
            return null;
        }
    }

    @Override
    public boolean isBypassed() {
        return WriteTracker.isDirty() || unavailable();
    }

    @Override
    public void singleFlight(String key, Runnable task) {
        singleFlight.run(key, task);
    }

    // ---- CcCacheWriteObserver --------------------------------------------------------------------------------------

    @Override
    public void written() {
        WriteTracker.written(this::startNewGeneration);
    }

    private void startNewGeneration() {
        try (RedisConnection connection = connectionFactory.getConnection()) {
            connection.stringCommands().incr(generationKey);
        } catch (RuntimeException e) {
            failed("start a new generation", e);
        }
    }

}
