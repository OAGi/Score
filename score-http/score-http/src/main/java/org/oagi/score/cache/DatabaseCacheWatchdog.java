package org.oagi.score.cache;

import com.google.common.collect.Iterables;
import org.oagi.score.repository.ScoreRepository;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class DatabaseCacheWatchdog<T> extends DatabaseCacheHandler
        implements InitializingBean, DisposableBean, Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private CacheSerializer serializer;

    @Autowired
    private RedissonClient redissonClient;

    private final ScoreRepository<T> delegate;

    private long delay = 2L;
    private TimeUnit unit = TimeUnit.SECONDS;

    private ScheduledExecutorService scheduledExecutorService;

    public DatabaseCacheWatchdog(String tableName, Class<T> mappedClass,
                                 ScoreRepository<T> delegate) {
        super(tableName, mappedClass);
        this.delegate = delegate;
    }

    public void setDelay(long delay, TimeUnit unit) {
        this.delay = delay;
        this.unit = unit;
    }

    public String underscoreToCamelCase(String string) {
        String str = Arrays.asList(string.split("_")).stream()
                .map(e -> Character.toUpperCase(e.charAt(0)) + e.substring(1).toLowerCase())
                .collect(Collectors.joining(""));
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(this, 0L, delay, unit);
    }

    @Override
    public void destroy() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
    }

    @Override
    public void run() {
        try {
            execute();
        } catch (Throwable t) {
            logger.error("Database/Cache Watchdog for `" + getTableName() + "` table: get caught an exception.", t);
        }
    }

    private void execute() {
        String tableName = getTableName();
        if (logger.isTraceEnabled()) {
            logger.trace("Database/Cache Watchdog for `" + tableName + "` table: investigating...");
        }

        String lockName = "rwlock:" + tableName;
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(lockName);
        RedisConnection redisConnection = redisConnectionFactory.getConnection();
        try {
            Map<Long, String> checksumFromDatabase = getChecksumFromDatabase();
            Map<Long, String> invalidPrimaryKeys;

            try {
                if (!readWriteLock.readLock().tryLock(5L, 5L, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("`" + lockName + "` read-lock acquisition failure by time-out.");
                }
            } catch (InterruptedException e) {
                throw new IllegalStateException("`" + lockName + "` read-lock acquisition failure by interrupt.", e);
            }
            try {
                Map<Long, String> checksumFromRedis = getChecksumFromRedis(redisConnection);
                if (logger.isDebugEnabled()) {
                    logger.trace("Database/Cache Watchdog for `" + tableName + "` table: retrieved " + checksumFromDatabase.size() + " checksum items.");
                }

                invalidPrimaryKeys = getInvalidPrimaryKeys(checksumFromRedis, checksumFromDatabase);
                if (invalidPrimaryKeys.isEmpty()) {
                    logger.trace("Database/Cache Watchdog for `" + tableName + "` table: all data are valid.");
                    return;
                }
            } finally {
                readWriteLock.readLock().unlock();
            }

            try {
                if (!readWriteLock.writeLock().tryLock(5L, 5L, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("`" + lockName + "` write-lock acquisition failure by time-out.");
                }
            } catch (InterruptedException e) {
                throw new IllegalStateException("`" + lockName + "` write-lock acquisition failure by interrupt.", e);
            }
            try {
                logger.debug("Database/Cache Watchdog for `" + tableName + "` table: " + invalidPrimaryKeys.size() + " invalid items found.");

                updateChecksumAndData(redisConnection, invalidPrimaryKeys, checksumFromDatabase);
                logger.debug("Database/Cache Watchdog for `" + tableName + "` table: successfully updated.");
            } finally {
                readWriteLock.writeLock().unlock();
            }
        } finally {
            redisConnection.close();
        }
    }

    private Map<Long, String> getChecksumFromRedis(RedisConnection redisConnection) {
        byte[] checksumKey = serializer.serializeCacheKey(getTableName() + ":checksum");
        Map<byte[], byte[]> hGetAll = redisConnection.hGetAll(checksumKey);
        if (hGetAll == null) {
            hGetAll = Collections.emptyMap();
        }

        return hGetAll.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> Long.parseLong(new String(e.getKey())),
                        e -> (String) serializer.deserializeCacheValue(e.getValue())));
    }

    private Map<Long, String> getChecksumFromDatabase() {
        Map<Long, String> checksumMap = new HashMap();
        String checksumQuery = getChecksumQuery();
        String underscorePriKeyName = getUnderscorePriKeyName();
        jdbcTemplate.query(checksumQuery, rch -> {
            checksumMap.put(rch.getLong(underscorePriKeyName), rch.getString("checksum"));
        });
        return checksumMap;
    }

    private Map<Long, String> getInvalidPrimaryKeys(
            Map<Long, String> checksumFromRedis,
            Map<Long, String> checksumFromDatabase
    ) {
        Map<Long, String> invalidPrimaryKeys = new HashMap();

        for (Map.Entry<Long, String> entry : checksumFromDatabase.entrySet()) {
            long priKey = entry.getKey();
            String checksum = entry.getValue();
            if (checksum.equals(checksumFromRedis.get(priKey))) {
                continue;
            }

            invalidPrimaryKeys.put(priKey, "update");
        }

        for (Map.Entry<Long, String> entry : checksumFromRedis.entrySet()) {
            long priKey = entry.getKey();
            if (!checksumFromDatabase.containsKey(priKey)) {
                invalidPrimaryKeys.put(priKey, "delete");
            }
        }

        return invalidPrimaryKeys;
    }

    private void updateChecksumAndData(RedisConnection redisConnection,
                                       Map<Long, String> invalidPrimaryKeys,
                                       Map<Long, String> checksumFromDatabase) {
        Map<Long, T> objectFromDatabase = getObjectFromDatabase();
        if (logger.isTraceEnabled()) {
            logger.trace("Database/Cache Watchdog for `" + getTableName() + "` table: retrieved object data.");
        }

        Map<byte[], byte[]> updateChecksumMap = new HashMap();
        Map<byte[], byte[]> updateObjectMap = new HashMap();
        List<byte[]> deleteFields = new ArrayList();

        for (Map.Entry<Long, String> entry : invalidPrimaryKeys.entrySet()) {
            long invalidPrimaryKey = entry.getKey();
            byte[] field = serializer.serializeCacheKey("" + invalidPrimaryKey);

            switch (entry.getValue()) {
                case "update":
                    byte[] checksumValue = serializer.serializeCacheValue(checksumFromDatabase.get(invalidPrimaryKey));
                    byte[] objectValue = serializer.serializeCacheValue(objectFromDatabase.get(invalidPrimaryKey));

                    updateChecksumMap.put(field, checksumValue);
                    updateObjectMap.put(field, objectValue);

                    break;

                case "delete":
                    deleteFields.add(field);
                    break;
            }
        }

        if (!updateChecksumMap.isEmpty() && !updateObjectMap.isEmpty()) {
            byte[] checksumKey = serializer.serializeCacheKey(getTableName() + ":checksum");
            redisConnection.hMSet(checksumKey, updateChecksumMap);

            byte[] objectKey = serializer.serializeCacheKey(getTableName());
            redisConnection.hMSet(objectKey, updateObjectMap);
        }

        if (!deleteFields.isEmpty()) {
            byte[] checksumKey = serializer.serializeCacheKey(getTableName() + ":checksum");
            byte[] objectKey = serializer.serializeCacheKey(getTableName());

            byte[][] fields = Iterables.toArray(deleteFields, byte[].class);
            redisConnection.hDel(checksumKey, fields);
            redisConnection.hDel(objectKey, fields);
        }
    }

    private Map<Long, T> getObjectFromDatabase() {
        Class<T> mappedClass = getMappedClass();
        String camelCasePriKeyName = getCamelCasePriKeyName();
        return this.delegate.findAll().stream()
                .collect(Collectors.toMap(obj -> {
                    PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(mappedClass, camelCasePriKeyName);
                    try {
                        Long priKey = (Long) pd.getReadMethod().invoke(obj, new Object[]{});
                        return priKey;
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("'" + camelCasePriKeyName + "' method should be accessible.", e);
                    } catch (InvocationTargetException e) {
                        throw new IllegalStateException("'" + camelCasePriKeyName + "' method cannot access.", e.getCause());
                    }
                }, Function.identity()));
    }
}
