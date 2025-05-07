package org.oagi.score.gateway.http.cache;

import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

import static org.oagi.score.gateway.http.cache.ScoreRedisCacheWriter.shouldExpireWithin;

public class ScoreRedisCache extends RedisCache {

    private final RedisConnectionFactory connectionFactory;

    protected ScoreRedisCache(RedisConnectionFactory connectionFactory,
                              String name, RedisCacheWriter cacheWriter, RedisCacheConfiguration cacheConfig) {
        super(name, cacheWriter, cacheConfig);
        this.connectionFactory = connectionFactory;
    }

    @Override
    protected Object lookup(Object key) {
        Object obj = super.lookup(key);
        if (obj != null) {
            Duration ttl = getCacheConfiguration().getTtl();
            if (shouldExpireWithin(ttl)) {
                RedisConnection connection = connectionFactory.getConnection();
                try {
                    connection.pExpire(createAndConvertCacheKey(key), ttl.toMillis());
                } finally {
                    connection.close();
                }
            }
        }
        return obj;
    }

    private byte[] createAndConvertCacheKey(Object key) {
        return serializeCacheKey(createCacheKey(key));
    }
}
