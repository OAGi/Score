package org.oagi.score.gateway.http.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HttpSessionIdResolver;

@Configuration
@EnableRedisIndexedHttpSession(redisNamespace = "score:session")
public class SessionConfiguration {

    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        CookieHttpSessionIdResolver httpSessionIdResolver = new CookieHttpSessionIdResolver();
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setDomainNamePattern("^([\\s\\S]+)$");
        cookieSerializer.setCookiePath("/");
        cookieSerializer.setUseHttpOnlyCookie(true);
        httpSessionIdResolver.setCookieSerializer(cookieSerializer);
        return httpSessionIdResolver;
    }

    /**
     * Runs the messages Redis sends about keys (a key expired, a key was deleted) that Spring Session listens to in order to
     * end sessions. Without this the listener container starts a new thread for every message, and every key of the Redis
     * that expires or is deleted is a message, whatever the key is for (the CC cache puts thousands there). The threads
     * pile up until the process cannot start another ("pthread_create failed (EAGAIN)").
     */
    @Bean(name = "springSessionRedisTaskExecutor")
    public ThreadPoolTaskExecutor springSessionRedisTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("springSessionRedisMessageListenerContainer-");
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100_000);
        // a full queue slows the thread that receives the messages down instead of dropping a message that ends a session
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.setAllowCoreThreadTimeOut(true);
        executor.setKeepAliveSeconds(60);
        return executor;
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

}
