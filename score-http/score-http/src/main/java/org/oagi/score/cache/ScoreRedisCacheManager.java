package org.oagi.score.cache;

import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.lang.Nullable;

import java.util.Collections;

public class ScoreRedisCacheManager extends RedisCacheManager {

    private final RedisConnectionFactory connectionFactory;
    private final RedisCacheWriter cacheWriter;
    private final RedisCacheConfiguration defaultCacheConfig;

    public ScoreRedisCacheManager(RedisConnectionFactory connectionFactory,
                                  RedisCacheWriter cacheWriter,
                                  RedisCacheConfiguration defaultCacheConfiguration) {
        super(cacheWriter, defaultCacheConfiguration, Collections.emptyMap(), true);

        this.connectionFactory = connectionFactory;
        this.cacheWriter = cacheWriter;
        this.defaultCacheConfig = defaultCacheConfiguration;
    }

    protected RedisCache createRedisCache(String name, @Nullable RedisCacheConfiguration cacheConfig) {
        return new ScoreRedisCache(connectionFactory,
                name, cacheWriter, cacheConfig != null ? cacheConfig : defaultCacheConfig);
    }
}
