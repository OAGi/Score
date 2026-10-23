package org.oagi.score.gateway.http.configuration.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Method-level security. In Spring Security 7 {@code @EnableGlobalMethodSecurity} and
 * {@code GlobalMethodSecurityConfiguration} were removed in favour of {@code @EnableMethodSecurity}
 * (#1750). {@code prePostEnabled} defaults to {@code true}; {@code @Secured} support is opt-in so
 * {@code securedEnabled = true} is kept.
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class MethodSecurityConfigurer {
}
