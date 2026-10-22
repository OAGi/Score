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
 * <b>Keys.</b> {@code {cc:<application version>}:<key>}; the braces make all keys of the cache one slot of a Redis cluster,
 * which the commands that take several keys need. The version of the application is part of the key, so a deployment starts
 * from an empty cache.
 * <p>
 * <b>Forgetting.</b> {@link CcCacheWatchdog} names the keys of the records that changed. An epoch counter in Redis changes
 * first and the keys are removed after, and a store is a script that stores only if the epoch is still the one the reader
 * took: so what a reader read before a change can never be put back after the entry was forgotten, on any instance.
 * The entries of everything that did not change stay.
 * <p>
 * <b>Cost.</b> The entries are read and written in bulk ({@code MGET}, a script of {@code SET}s), because a round trip
 * per entry takes seconds for a graph.
 * <p>
 * <b>Failure.</b> Redis that does not answer is a miss, never an error; for a few seconds afterwards the cache is
 * bypassed altogether, so that a dead Redis does not add its time-out to every lookup. Forgetting is the exception: it
 * fails, so that the watchdog tries again.
 * <p>
 * The cache shares its Redis with whatever else uses it (the sessions): it never scans or flushes it, it only counts
 * epochs and gives every entry an expiry. When Redis is flushed the epoch starts again from the clock, not from zero.
 */
@Component
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisCcCache implements CcCache {

    private static final Logger logger = LoggerFactory.getLogger(RedisCcCache.class);

    /** The most keys of one command. */
    private static final int CHUNK = 1000;
    /** The most entries one script call stores. */
    private static final int STORE_CHUNK = 500;
    /** KEYS[1] is the epoch and the others the entries; ARGV[1] the epoch of the reader, ARGV[i] the value of KEYS[i], the last ARGV the expiry. */
    private static final byte[] STORE_SCRIPT = (
            "local e = redis.call('GET', KEYS[1]) "
                    + "if e ~= ARGV[1] then return 0 end "
                    + "for i = 2, #KEYS do redis.call('SET', KEYS[i], ARGV[i], 'PX', ARGV[#ARGV]) end "
                    + "return #KEYS - 1")
            .getBytes(StandardCharsets.US_ASCII);
    private static final long BYPASS_AFTER_FAILURE_NANOS = Duration.ofSeconds(5).toNanos();

    private final RedisConnectionFactory connectionFactory;
    private final CcCacheStatus status;
    private final CcRecordSerializer serializer = new CcRecordSerializer();
    private final SingleFlight singleFlight = new SingleFlight();
    private final String prefix;
    private final byte[] epochKey;
    private final long ttlMillis;

    /** The {@link System#nanoTime()} until which Redis is not asked, after a failure. */
    private volatile long unavailableUntil = System.nanoTime() - 1;

    @Autowired
    public RedisCcCache(RedisConnectionFactory connectionFactory,
                        ObjectProvider<BuildProperties> buildProperties,
                        CcCacheStatus status,
                        @Value("${spring.cache.redis.time-to-live:10m}") Duration timeToLive) {
        this(connectionFactory, "{cc:" + version(buildProperties) + "}:", status, timeToLive);
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
    public RedisCcCache(RedisConnectionFactory connectionFactory, String prefix, CcCacheStatus status, Duration timeToLive) {
        this.connectionFactory = connectionFactory;
        this.prefix = prefix;
        this.status = status;
        this.epochKey = (this.prefix + "epoch").getBytes(StandardCharsets.UTF_8);
        this.ttlMillis = timeToLive.toMillis();
    }

    // ---- the keys ------------------------------------------------------------------------------------------------

    private byte[] keyOf(String key) {
        return (prefix + key).getBytes(StandardCharsets.UTF_8);
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
    public long epoch() {
        status.freshen();
        if (unavailable()) {
            return -1;
        }
        try (RedisConnection connection = connectionFactory.getConnection()) {
            byte[] value = connection.stringCommands().get(epochKey);
            if (value == null) {
                // The counter is gone (Redis was flushed, as an instance does when it starts). It starts again from the
                // time, not from zero, so that a reader that still holds an epoch of before cannot meet it again.
                connection.stringCommands().set(epochKey, Long.toString(System.currentTimeMillis()).getBytes(StandardCharsets.US_ASCII),
                        Expiration.persistent(), SetOption.SET_IF_ABSENT);
                value = connection.stringCommands().get(epochKey);
            }
            return (value == null) ? -1 : Long.parseLong(new String(value, StandardCharsets.US_ASCII));
        } catch (RuntimeException e) {
            failed("read its epoch", e);
            return -1;
        }
    }

    @Override
    public Object get(String key) {
        return getAll(List.of(key)).get(key);
    }

    @Override
    public Map<String, Object> getAll(Collection<String> keys) {
        if (keys.isEmpty() || unavailable()) {
            return Collections.emptyMap();
        }
        Map<String, Object> found = new HashMap<>();
        List<String> all = new ArrayList<>(keys);
        try (RedisConnection connection = connectionFactory.getConnection()) {
            for (int from = 0; from < all.size(); from += CHUNK) {
                List<String> chunk = all.subList(from, Math.min(from + CHUNK, all.size()));
                byte[][] redisKeys = chunk.stream().map(this::keyOf).toArray(byte[][]::new);
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
    public void put(long epoch, String key, Object value) {
        if (value != null) {
            putAll(epoch, Map.of(key, value));
        }
    }

    @Override
    public void putAll(long epoch, Map<String, Object> values) {
        if (epoch < 0 || values.isEmpty() || unavailable()) {
            return;
        }
        List<Map.Entry<String, Object>> entries = new ArrayList<>(values.entrySet());
        byte[] epochArgument = Long.toString(epoch).getBytes(StandardCharsets.US_ASCII);
        byte[] ttlArgument = Long.toString(ttlMillis).getBytes(StandardCharsets.US_ASCII);
        try (RedisConnection connection = connectionFactory.getConnection()) {
            for (int from = 0; from < entries.size(); from += STORE_CHUNK) {
                List<byte[]> keys = new ArrayList<>();
                List<byte[]> bytes = new ArrayList<>();
                for (Map.Entry<String, Object> entry : entries.subList(from, Math.min(from + STORE_CHUNK, entries.size()))) {
                    byte[] value = write(entry.getKey(), entry.getValue());
                    if (value != null) {
                        keys.add(keyOf(entry.getKey()));
                        bytes.add(value);
                    }
                }
                if (!keys.isEmpty()) {
                    // One round trip for the chunk, on the shared connection (a pipeline would open a connection of its own).
                    // KEYS: the epoch, then the entries. ARGV: the reader's epoch, then the value of each entry, then the expiry.
                    int n = keys.size();
                    byte[][] keysAndArguments = new byte[(n + 1) * 2 + 1][];
                    keysAndArguments[0] = epochKey;
                    for (int i = 0; i < n; i++) {
                        keysAndArguments[i + 1] = keys.get(i);
                    }
                    keysAndArguments[n + 1] = epochArgument;
                    for (int i = 0; i < n; i++) {
                        keysAndArguments[n + 2 + i] = bytes.get(i);
                    }
                    keysAndArguments[2 * n + 2] = ttlArgument;
                    connection.scriptingCommands().eval(STORE_SCRIPT, ReturnType.INTEGER, n + 1, keysAndArguments);
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
    public void invalidate(Collection<String> keys) {
        try (RedisConnection connection = connectionFactory.getConnection()) {
            // First the epoch: a store that began before this is refused by the script, so none can land after the removal.
            if (connection.stringCommands().get(epochKey) == null) {
                connection.stringCommands().set(epochKey, Long.toString(System.currentTimeMillis()).getBytes(StandardCharsets.US_ASCII),
                        Expiration.persistent(), SetOption.SET_IF_ABSENT);
            }
            connection.stringCommands().incr(epochKey);
            List<String> all = new ArrayList<>(keys);
            for (int from = 0; from < all.size(); from += CHUNK) {
                byte[][] redisKeys = all.subList(from, Math.min(from + CHUNK, all.size())).stream()
                        .map(this::keyOf).toArray(byte[][]::new);
                connection.keyCommands().unlink(redisKeys);
            }
        }
    }

    @Override
    public boolean isBypassed() {
        return WriteTracker.isDirty() || unavailable() || !status.isReady();
    }

    @Override
    public void singleFlight(String key, Runnable task) {
        singleFlight.run(key, task);
    }

}
