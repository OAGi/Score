package org.oagi.score.gateway.http.configuration.security;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.FlushMode;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.RedisIndexedHttpSessionConfiguration;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.session.web.http.SessionRepositoryFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SessionConfiguration extends RedisIndexedHttpSessionConfiguration {

    public SessionConfiguration() {
        setConfigureRedisAction(ConfigureRedisAction.NO_OP);
        setRedisNamespace("score:session");
    }

    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        CookieHttpSessionIdResolver httpSessionIdResolver = new CookieHttpSessionIdResolver();
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setDomainNamePattern("^([\\s\\S]+)$");
        cookieSerializer.setCookiePath("/");
        cookieSerializer.setUseHttpOnlyCookie(true);
        cookieSerializer.setSameSite("Strict");
        httpSessionIdResolver.setCookieSerializer(cookieSerializer);
        return httpSessionIdResolver;
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

}
