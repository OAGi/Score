package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.springframework.util.StringUtils;

import java.util.function.Consumer;

/**
 * Applies the Default/Fixed Value of a BBIE (SC) update request. The request carries the whole value constraint,
 * so a null or empty value means there is none; with neither value left (Value Constraint "None") both are cleared.
 */
final class BieValueConstraintUpdates {

    private BieValueConstraintUpdates() {
    }

    static void apply(String defaultValue, String fixedValue,
                      Consumer<String> setDefaultValue, Consumer<String> setFixedValue) {
        if (StringUtils.hasLength(defaultValue)) {
            setDefaultValue.accept(defaultValue);
            setFixedValue.accept(null);
        } else if (StringUtils.hasLength(fixedValue)) {
            setDefaultValue.accept(null);
            setFixedValue.accept(fixedValue);
        } else {
            setDefaultValue.accept(null);
            setFixedValue.accept(null);
        }
    }
}
