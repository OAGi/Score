package org.oagi.score.gateway.http.common.model;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface AccessControl {

    boolean ignore() default false;

    ScoreRole[] requiredAnyRole() default {};

}
