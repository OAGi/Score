package org.oagi.score.gateway.http.cache;

import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.redis.cache.BatchStrategy;
import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.data.redis.cache.CacheStatisticsCollector;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.util.ByteUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Copied from org.springframework.data.redis.cache.DefaultRedisCacheWriter class.
 */
public class ScoreRedisCacheWriter implements RedisCacheWriter {

    private static final boolean REACTIVE_REDIS_CONNECTION_FACTORY_PRESENT = ClassUtils
            .isPresent("org.springframework.data.redis.connection.ReactiveRedisConnectionFactory", null);

    private final BatchStrategy batchStrategy;

    private final CacheStatisticsCollector statistics;

    private final Duration sleepTime;

    private final RedisConnectionFactory connectionFactory;

    private final TtlFunction lockTtl;

    private final AsyncCacheWriter asyncCacheWriter;

    /**
     * @param connectionFactory must not be {@literal null}.
     * @param batchStrategy     must not be {@literal null}.
     */
    public ScoreRedisCacheWriter(RedisConnectionFactory connectionFactory, BatchStrategy batchStrategy) {
        this(connectionFactory, Duration.ZERO, batchStrategy);
    }

    /**
     * @param connectionFactory must not be {@literal null}.
     * @param sleepTime         sleep time between lock request attempts. Must not be {@literal null}. Use {@link Duration#ZERO}
     *                          to disable locking.
     * @param batchStrategy     must not be {@literal null}.
     */
    public ScoreRedisCacheWriter(RedisConnectionFactory connectionFactory, Duration sleepTime, BatchStrategy batchStrategy) {
        this(connectionFactory, sleepTime, TtlFunction.persistent(), CacheStatisticsCollector.none(), batchStrategy);
    }

    /**
     * @param connectionFactory        must not be {@literal null}.
     * @param sleepTime                sleep time between lock request attempts. Must not be {@literal null}. Use {@link Duration#ZERO}
     *                                 to disable locking.
     * @param lockTtl                  Lock TTL function must not be {@literal null}.
     * @param cacheStatisticsCollector must not be {@literal null}.
     * @param batchStrategy            must not be {@literal null}.
     */
    public ScoreRedisCacheWriter(RedisConnectionFactory connectionFactory, Duration sleepTime, TtlFunction lockTtl,
                                 CacheStatisticsCollector cacheStatisticsCollector, BatchStrategy batchStrategy) {

        Assert.notNull(connectionFactory, "ConnectionFactory must not be null");
        Assert.notNull(sleepTime, "SleepTime must not be null");
        Assert.notNull(lockTtl, "Lock TTL Function must not be null");
        Assert.notNull(cacheStatisticsCollector, "CacheStatisticsCollector must not be null");
        Assert.notNull(batchStrategy, "BatchStrategy must not be null");

        this.connectionFactory = connectionFactory;
        this.sleepTime = sleepTime;
        this.lockTtl = lockTtl;
        this.statistics = cacheStatisticsCollector;
        this.batchStrategy = batchStrategy;

        if (REACTIVE_REDIS_CONNECTION_FACTORY_PRESENT && this.connectionFactory instanceof ReactiveRedisConnectionFactory) {
            asyncCacheWriter = new AsynchronousCacheWriterDelegate();
        } else {
            asyncCacheWriter = UnsupportedAsyncCacheWriter.INSTANCE;
        }
    }

    @Override
    public byte[] get(String name, byte[] key) {
        return get(name, key, null);
    }

    @Override
    public byte[] get(String name, byte[] key, @Nullable Duration ttl) {

        Assert.notNull(name, "Name must not be null");
        Assert.notNull(key, "Key must not be null");

        byte[] result = shouldExpireWithin(ttl)
                ? execute(name, connection -> connection.stringCommands().getEx(key, Expiration.from(ttl)))
                : execute(name, connection -> connection.stringCommands().get(key));

        statistics.incGets(name);

        if (result != null) {
            statistics.incHits(name);
        } else {
            statistics.incMisses(name);
        }

        return result;
    }

    @Override
    public boolean supportsAsyncRetrieve() {
        return asyncCacheWriter.isSupported();
    }

    @Override
    public CompletableFuture<byte[]> retrieve(String name, byte[] key, @Nullable Duration ttl) {

        Assert.notNull(name, "Name must not be null");
        Assert.notNull(key, "Key must not be null");

        return asyncCacheWriter.retrieve(name, key, ttl) //
                .thenApply(cachedValue -> {

                    statistics.incGets(name);

                    if (cachedValue != null) {
                        statistics.incHits(name);
                    } else {
                        statistics.incMisses(name);
                    }

                    return cachedValue;
                });
    }

    @Override
    public void put(String name, byte[] key, byte[] value, @Nullable Duration ttl) {

        Assert.notNull(name, "Name must not be null");
        Assert.notNull(key, "Key must not be null");
        Assert.notNull(value, "Value must not be null");

        execute(name, connection -> {

            if (shouldExpireWithin(ttl)) {
                connection.stringCommands().set(key, value, Expiration.from(ttl.toMillis(), TimeUnit.MILLISECONDS),
                        SetOption.upsert());
            } else {
                connection.stringCommands().set(key, value);
            }

            return "OK";
        });

        statistics.incPuts(name);
    }

    @Override
    public CompletableFuture<Void> store(String name, byte[] key, byte[] value, @Nullable Duration ttl) {

        Assert.notNull(name, "Name must not be null");
        Assert.notNull(key, "Key must not be null");
        Assert.notNull(value, "Value must not be null");

        return asyncCacheWriter.store(name, key, value, ttl) //
                .thenRun(() -> statistics.incPuts(name));
    }

    @Override
    public byte[] putIfAbsent(String name, byte[] key, byte[] value, @Nullable Duration ttl) {

        Assert.notNull(name, "Name must not be null");
        Assert.notNull(key, "Key must not be null");
        Assert.notNull(value, "Value must not be null");

        return execute(name, connection -> {

            if (isLockingCacheWriter()) {
                doLock(name, key, value, connection);
            }

            try {

                boolean put;

                if (shouldExpireWithin(ttl)) {
                    put = ObjectUtils.nullSafeEquals(
                            connection.stringCommands().set(key, value, Expiration.from(ttl), SetOption.ifAbsent()), true);
                } else {
                    put = ObjectUtils.nullSafeEquals(connection.stringCommands().setNX(key, value), true);
                }

                if (put) {
                    statistics.incPuts(name);
                    return null;
                }

                return connection.stringCommands().get(key);

            } finally {
                if (isLockingCacheWriter()) {
                    doUnlock(name, connection);
                }
            }
        });
    }

    @Override
    public void remove(String name, byte[] key) {

        Assert.notNull(name, "Name must not be null");
        Assert.notNull(key, "Key must not be null");

        execute(name, connection -> connection.keyCommands().del(key));
        statistics.incDeletes(name);
    }

    @Override
    public void clean(String name, byte[] pattern) {

        Assert.notNull(name, "Name must not be null");
        Assert.notNull(pattern, "Pattern must not be null");

        execute(name, connection -> {

            boolean wasLocked = false;

            try {
                if (isLockingCacheWriter()) {
                    doLock(name, name, pattern, connection);
                    wasLocked = true;
                }

                long deleteCount = batchStrategy.cleanCache(connection, name, pattern);

                while (deleteCount > Integer.MAX_VALUE) {
                    statistics.incDeletesBy(name, Integer.MAX_VALUE);
                    deleteCount -= Integer.MAX_VALUE;
                }

                statistics.incDeletesBy(name, (int) deleteCount);

            } finally {
                if (wasLocked && isLockingCacheWriter()) {
                    doUnlock(name, connection);
                }
            }

            return "OK";
        });
    }

    @Override
    public CacheStatistics getCacheStatistics(String cacheName) {
        return statistics.getCacheStatistics(cacheName);
    }

    @Override
    public void clearStatistics(String name) {
        statistics.reset(name);
    }

    @Override
    public RedisCacheWriter withStatisticsCollector(CacheStatisticsCollector cacheStatisticsCollector) {
        return new ScoreRedisCacheWriter(connectionFactory, sleepTime, lockTtl, cacheStatisticsCollector,
                this.batchStrategy);
    }

    /**
     * Explicitly set a write lock on a cache.
     *
     * @param name the meaning of the cache to lock.
     */
    void lock(String name) {
        execute(name, connection -> doLock(name, name, null, connection));
    }

    @Nullable
    private Boolean doLock(String name, Object contextualKey, @Nullable Object contextualValue,
                           RedisConnection connection) {

        Expiration expiration = Expiration.from(this.lockTtl.getTimeToLive(contextualKey, contextualValue));

        return connection.stringCommands().set(createCacheLockKey(name), new byte[0], expiration, SetOption.SET_IF_ABSENT);
    }

    /**
     * Explicitly remove a write lock from a cache.
     *
     * @param name the meaning of the cache to unlock.
     */
    void unlock(String name) {
        executeLockFree(connection -> doUnlock(name, connection));
    }

    @Nullable
    private Long doUnlock(String name, RedisConnection connection) {
        return connection.keyCommands().del(createCacheLockKey(name));
    }

    private <T> T execute(String name, Function<RedisConnection, T> callback) {

        try (RedisConnection connection = this.connectionFactory.getConnection()) {
            checkAndPotentiallyWaitUntilUnlocked(name, connection);
            return callback.apply(connection);
        }
    }

    private <T> T executeLockFree(Function<RedisConnection, T> callback) {

        try (RedisConnection connection = this.connectionFactory.getConnection()) {
            return callback.apply(connection);
        }
    }

    /**
     * Determines whether this {@link RedisCacheWriter} uses locks during caching operations.
     *
     * @return {@literal true} if {@link RedisCacheWriter} uses locks.
     */
    private boolean isLockingCacheWriter() {
        return !this.sleepTime.isZero() && !this.sleepTime.isNegative();
    }

    private void checkAndPotentiallyWaitUntilUnlocked(String name, RedisConnection connection) {

        if (!isLockingCacheWriter()) {
            return;
        }

        long lockWaitTimeNs = System.nanoTime();

        try {
            while (doCheckLock(name, connection)) {
                Thread.sleep(this.sleepTime.toMillis());
            }
        } catch (InterruptedException ex) {

            // Re-interrupt current Thread to allow other participants to react.
            Thread.currentThread().interrupt();

            String message = String.format("Interrupted while waiting to unlock cache %s", name);

            throw new PessimisticLockingFailureException(message, ex);
        } finally {
            this.statistics.incLockTime(name, System.nanoTime() - lockWaitTimeNs);
        }
    }

    boolean doCheckLock(String name, RedisConnection connection) {
        return ObjectUtils.nullSafeEquals(connection.keyCommands().exists(createCacheLockKey(name)), true);
    }

    byte[] createCacheLockKey(String name) {
        return (name + "~lock").getBytes(StandardCharsets.UTF_8);
    }

    public static boolean shouldExpireWithin(@Nullable Duration ttl) {
        return ttl != null && !ttl.isZero() && !ttl.isNegative();
    }

    /**
     * Interface for asynchronous cache retrieval.
     *
     * @since 3.2
     */
    interface AsyncCacheWriter {

        /**
         * @return {@code true} if async cache operations are supported; {@code false} otherwise.
         */
        boolean isSupported();

        /**
         * Retrieve a cache entry asynchronously.
         *
         * @param name the cache meaning from which to retrieve the cache entry.
         * @param key  the cache entry key.
         * @param ttl  optional TTL to set for Time-to-Idle eviction.
         * @return a future that completes either with a value if the value exists or completing with {@code null} if the
         * cache does not contain an entry.
         */
        CompletableFuture<byte[]> retrieve(String name, byte[] key, @Nullable Duration ttl);

        /**
         * Store a cache entry asynchronously.
         *
         * @param name  the cache meaning which to store the cache entry to.
         * @param key   the key for the cache entry. Must not be {@literal null}.
         * @param value the value stored for the key. Must not be {@literal null}.
         * @param ttl   optional expiration time. Can be {@literal null}.
         * @return a future that signals completion.
         */
        CompletableFuture<Void> store(String name, byte[] key, byte[] value, @Nullable Duration ttl);

    }

    /**
     * Unsupported variant of a {@link AsyncCacheWriter}.
     *
     * @since 3.2
     */
    enum UnsupportedAsyncCacheWriter implements AsyncCacheWriter {

        INSTANCE;

        @Override
        public boolean isSupported() {
            return false;
        }

        @Override
        public CompletableFuture<byte[]> retrieve(String name, byte[] key, @Nullable Duration ttl) {
            throw new UnsupportedOperationException("async retrieve not supported");
        }

        @Override
        public CompletableFuture<Void> store(String name, byte[] key, byte[] value, @Nullable Duration ttl) {
            throw new UnsupportedOperationException("async store not supported");
        }
    }

    /**
     * Delegate implementing {@link AsyncCacheWriter} to provide asynchronous cache retrieval and storage operations using
     * {@link ReactiveRedisConnectionFactory}.
     *
     * @since 3.2
     */
    class AsynchronousCacheWriterDelegate implements AsyncCacheWriter {

        @Override
        public boolean isSupported() {
            return true;
        }

        @Override
        public CompletableFuture<byte[]> retrieve(String name, byte[] key, @Nullable Duration ttl) {

            return doWithConnection(connection -> {

                ByteBuffer wrappedKey = ByteBuffer.wrap(key);
                Mono<?> cacheLockCheck = isLockingCacheWriter() ? waitForLock(connection, name) : Mono.empty();
                ReactiveStringCommands stringCommands = connection.stringCommands();

                Mono<ByteBuffer> get = shouldExpireWithin(ttl)
                        ? stringCommands.getEx(wrappedKey, Expiration.from(ttl))
                        : stringCommands.get(wrappedKey);

                return cacheLockCheck.then(get).map(ByteUtils::getBytes).toFuture();
            });
        }

        @Override
        public CompletableFuture<Void> store(String name, byte[] key, byte[] value, @Nullable Duration ttl) {

            return doWithConnection(connection -> {

                Mono<?> mono = isLockingCacheWriter()
                        ? doStoreWithLocking(name, key, value, ttl, connection)
                        : doStore(key, value, ttl, connection);

                return mono.then().toFuture();
            });
        }

        private Mono<Boolean> doStoreWithLocking(String name, byte[] key, byte[] value, @Nullable Duration ttl,
                                                 ReactiveRedisConnection connection) {

            return Mono.usingWhen(doLock(name, key, value, connection), unused -> doStore(key, value, ttl, connection),
                    unused -> doUnlock(name, connection));
        }

        private Mono<Boolean> doStore(byte[] cacheKey, byte[] value, @Nullable Duration ttl,
                                      ReactiveRedisConnection connection) {

            ByteBuffer wrappedKey = ByteBuffer.wrap(cacheKey);
            ByteBuffer wrappedValue = ByteBuffer.wrap(value);

            if (shouldExpireWithin(ttl)) {
                return connection.stringCommands().set(wrappedKey, wrappedValue,
                        Expiration.from(ttl.toMillis(), TimeUnit.MILLISECONDS), SetOption.upsert());
            } else {
                return connection.stringCommands().set(wrappedKey, wrappedValue);
            }
        }


        private Mono<Object> doLock(String name, Object contextualKey, @Nullable Object contextualValue,
                                    ReactiveRedisConnection connection) {

            ByteBuffer key = ByteBuffer.wrap(createCacheLockKey(name));
            ByteBuffer value = ByteBuffer.wrap(new byte[0]);
            Expiration expiration = Expiration.from(lockTtl.getTimeToLive(contextualKey, contextualValue));

            return connection.stringCommands().set(key, value, expiration, SetOption.SET_IF_ABSENT) //
                    // Ensure we emit an object, otherwise, the Mono.usingWhen operator doesn't run the inner resource function.
                    .thenReturn(Boolean.TRUE);
        }

        private Mono<Void> doUnlock(String name, ReactiveRedisConnection connection) {
            return connection.keyCommands().del(ByteBuffer.wrap(createCacheLockKey(name))).then();
        }

        private Mono<Void> waitForLock(ReactiveRedisConnection connection, String cacheName) {

            AtomicLong lockWaitTimeNs = new AtomicLong();
            byte[] cacheLockKey = createCacheLockKey(cacheName);

            Flux<Long> wait = Flux.interval(Duration.ZERO, sleepTime);
            Mono<Boolean> exists = connection.keyCommands().exists(ByteBuffer.wrap(cacheLockKey)).filter(it -> !it);

            return wait.doOnSubscribe(subscription -> lockWaitTimeNs.set(System.nanoTime())) //
                    .flatMap(it -> exists) //
                    .doFinally(signalType -> statistics.incLockTime(cacheName, System.nanoTime() - lockWaitTimeNs.get())) //
                    .next() //
                    .then();
        }

        private <T> CompletableFuture<T> doWithConnection(
                Function<ReactiveRedisConnection, CompletableFuture<T>> callback) {

            ReactiveRedisConnectionFactory cf = (ReactiveRedisConnectionFactory) connectionFactory;

            return Mono.usingWhen(Mono.fromSupplier(cf::getReactiveConnection), //
                            it -> Mono.fromCompletionStage(callback.apply(it)), //
                            ReactiveRedisConnection::closeLater) //
                    .toFuture();
        }
    }
}
