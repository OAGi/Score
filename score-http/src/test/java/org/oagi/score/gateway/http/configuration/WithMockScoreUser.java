package org.oagi.score.gateway.http.configuration;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockScoreUserSecurityContextFactory.class)
public @interface WithMockScoreUser {

    long userId() default 0L;

    String username() default "";

    String password() default "";

    String role() default "";

}
