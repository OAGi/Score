package org.oagi.srt.config;

import org.oagi.srt.cache.SimpleCacheKeyGenerator;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.faces.context.FacesContext;

@Configuration
@EnableCaching(proxyTargetClass = true)
public class CacheConfiguration {

    @Bean
    public KeyGenerator simpleCacheKeyGenerator() {
        SimpleCacheKeyGenerator keyGenerator = new SimpleCacheKeyGenerator();
        return keyGenerator;
    }
}
