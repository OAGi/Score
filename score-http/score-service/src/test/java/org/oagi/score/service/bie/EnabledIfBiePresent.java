package org.oagi.score.service.bie;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(EnabledIfBiePresentCondition.class)
public @interface EnabledIfBiePresent {

    String propertyTerm();

    String release();
}
