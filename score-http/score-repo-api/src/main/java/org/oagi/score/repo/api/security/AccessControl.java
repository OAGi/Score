package org.oagi.score.repo.api.security;

import org.oagi.score.repo.api.user.model.ScoreRole;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface AccessControl {

    boolean ignore() default false;

    ScoreRole[] requiredAnyRole() default {};

}
