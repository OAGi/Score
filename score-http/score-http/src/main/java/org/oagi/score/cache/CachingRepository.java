package org.oagi.score.cache;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.oagi.score.repository.ScoreRepository;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CachingRepository<T> extends DatabaseCacheHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private CacheSerializer serializer;

    @Autowired
    private RedissonClient redissonClient;

    private final ScoreRepository<T> delegate;

    public CachingRepository(String tableName, Class<T> mappedClass, ScoreRepository<T> delegate) {
        super(tableName, mappedClass);
        this.delegate = delegate;
    }

    public List<T> findAll() {
        return execute(connection -> {
            byte[] key = serializer.serializeCacheKey(getTableName());
            Map<byte[], byte[]> results = connection.hGetAll(key);
            if (results == null) {
                return Collections.emptyList();
            }

            return results.values().stream()
                    .map(e -> (T) serializer.deserializeCacheValue(e))
                    .collect(Collectors.toList());
        });
    }

    public T findById(BigInteger id) {
        return execute(connection -> {
            String checksumFromDatabase = getChecksumFromDatabase(id);
            String checksumFromRedis = checksumFromRedis(connection, id);
            if (!checksumFromDatabase.equals(checksumFromRedis)) {
                T result = this.delegate.findById(id);

                setValue(connection, getTableName() + ":checksum", "" + id, checksumFromDatabase);
                setValue(connection, getTableName(), "" + id, result);

                return result;
            }

            return (T) getValue(connection, getTableName(), "" + id);
        });
    }

    private String getChecksumFromDatabase(BigInteger id) {
        StringBuilder query = new StringBuilder(getChecksumByIdQuery());
        Record record = dslContext.fetchOne(query.toString(), id);
        return record.getValue("checksum").toString();
    }

    private String checksumFromRedis(RedisConnection connection, BigInteger id) {
        return (String) getValue(connection, getTableName() + ":checksum", "" + id);
    }

    private Object getValue(RedisConnection connection, String keyStr, String fieldStr) {
        byte[] key = serializer.serializeCacheKey(keyStr);
        byte[] field = serializer.serializeCacheKey(fieldStr);
        byte[] value = connection.hGet(key, field);

        return serializer.deserializeCacheValue(value);
    }

    private void setValue(RedisConnection connection, String keyStr, String fieldStr, Object value) {
        byte[] key = serializer.serializeCacheKey(keyStr);
        byte[] field = serializer.serializeCacheKey(fieldStr);
        connection.hSet(key, field, serializer.serializeCacheValue(value));
    }

    protected <R> R execute(Function<RedisConnection, R> callback) {
        RedisConnection redisConnection = redisConnectionFactory.getConnection();
        try {
            String lockName = "rwlock:" + getTableName();
            RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(lockName);
            try {
                if (!readWriteLock.readLock().tryLock(5L, 5L, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("`" + lockName + "` read-lock acquisition failure by time-out.");
                }
            } catch (InterruptedException e) {
                throw new IllegalStateException("`" + lockName + "` read-lock acquisition failure by interrupt.", e);
            }
            try {
                return callback.apply(redisConnection);
            } finally {
                readWriteLock.readLock().unlock();
            }
        } finally {
            redisConnection.close();
        }
    }
}
