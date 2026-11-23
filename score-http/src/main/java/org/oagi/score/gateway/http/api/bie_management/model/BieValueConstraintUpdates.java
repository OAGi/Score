package org.oagi.score.gateway.http.api.bie_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;

import java.util.Objects;
import java.util.function.Consumer;

import static org.springframework.util.StringUtils.hasLength;

/**
 * Applies and compares the {@link ValueConstraint} (Default/Fixed Value) of a BBIE (SC). The update request carries
 * the whole value constraint, so a null or empty value means there is none; with neither value left
 * (Value Constraint "None") both are cleared. An empty value is the same as no value wherever two value
 * constraints are compared.
 */
public final class BieValueConstraintUpdates {

    private BieValueConstraintUpdates() {
    }

    public static void apply(ValueConstraint requested,
                             Consumer<String> setDefaultValue, Consumer<String> setFixedValue) {
        if (requested.hasFixedValue()) {
            setDefaultValue.accept(null);
            setFixedValue.accept(requested.fixedValue());
        } else if (requested.hasDefaultValue()) {
            setDefaultValue.accept(requested.defaultValue());
            setFixedValue.accept(null);
        } else {
            setDefaultValue.accept(null);
            setFixedValue.accept(null);
        }
    }

    /**
     * A value constraint is changed when the fixed value or the default value differs from the prior version,
     * including when one of them is added or removed.
     */
    public static boolean changed(ValueConstraint prior, ValueConstraint current) {
        return !Objects.equals(valueOf(prior.fixedValue()), valueOf(current.fixedValue())) ||
               !Objects.equals(valueOf(prior.defaultValue()), valueOf(current.defaultValue()));
    }

    private static String valueOf(String value) {
        return hasLength(value) ? value : null;
    }
}
