package org.oagi.score.gateway.http.common.model;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface AccessControl {

    boolean ignore() default false;

    ScoreRole[] requiredAnyRole() default {};

    /**
     * What is told to a user who may not call the method, when it is to be told in other words than the ones made of the roles that
     * are required ("Only an administrator can access this.").
     */
    String errorMessage() default "";

}
