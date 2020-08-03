package org.oagi.score.gateway.http.configuration.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.session.web.http.SessionRepositoryFilter;

@Configuration
public class SessionConfiguration extends RedisHttpSessionConfiguration {

    @Autowired
    private RedisTemplate redisTemplate;

    public SessionConfiguration() {
        setConfigureRedisAction(ConfigureRedisAction.NO_OP);
    }

    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        CookieHttpSessionIdResolver httpSessionIdResolver = new CookieHttpSessionIdResolver();
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setUseHttpOnlyCookie(true);
        cookieSerializer.setSameSite("Strict");
        httpSessionIdResolver.setCookieSerializer(cookieSerializer);
        return httpSessionIdResolver;
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Override
    public <S extends Session> SessionRepositoryFilter<? extends Session> springSessionRepositoryFilter(
            SessionRepository<S> sessionRepository) {
        return super.springSessionRepositoryFilter(new SafeDeserializationRepository(sessionRepository, redisTemplate));
    }
}
