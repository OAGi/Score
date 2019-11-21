package org.oagi.srt.cache;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.support.NullValue;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.util.ByteUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.nio.ByteBuffer;

@Component
public class CacheSerializer implements InitializingBean {

    private static byte[] BINARY_NULL_VALUE;

    @Autowired
    private RedisCacheConfiguration cacheConfig;

    private boolean allowNullValues;

    @Override
    public void afterPropertiesSet() throws Exception {
        BINARY_NULL_VALUE = ByteUtils.getBytes(cacheConfig.getValueSerializationPair().write(NullValue.INSTANCE));
    }

    public boolean isAllowNullValues() {
        return allowNullValues;
    }

    public void setAllowNullValues(boolean allowNullValues) {
        this.allowNullValues = allowNullValues;
    }

    public byte[] serializeCacheKey(String cacheKey) {
        return ByteUtils.getBytes(cacheConfig.getKeySerializationPair().write(cacheKey));
    }

    public byte[] serializeCacheValue(Object value) {
        if (isAllowNullValues() && value instanceof NullValue) {
            return BINARY_NULL_VALUE;
        }

        return ByteUtils.getBytes(cacheConfig.getValueSerializationPair().write(value));
    }

    public Object deserializeCacheValue(byte[] value) {
        if (value == null) {
            return null;
        }

        if (isAllowNullValues() && ObjectUtils.nullSafeEquals(value, BINARY_NULL_VALUE)) {
            return NullValue.INSTANCE;
        }

        return cacheConfig.getValueSerializationPair().read(ByteBuffer.wrap(value));
    }
}
