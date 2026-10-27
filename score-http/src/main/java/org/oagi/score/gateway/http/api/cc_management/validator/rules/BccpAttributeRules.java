package org.oagi.score.gateway.http.api.cc_management.validator.rules;

import org.oagi.score.gateway.http.api.cc_management.validator.BccpValidationInput;
import org.oagi.score.gateway.http.api.validation.Severity;
import org.oagi.score.gateway.http.api.validation.ValidationRule;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/** The rules of a BCCP that an ASCCP does not have: what is about its fixed value. */
public final class BccpAttributeRules {

    private static final String PREFIX = "BCCP";

    private BccpAttributeRules() {
    }

    public static List<ValidationRule<BccpValidationInput>> all() {
        return List.of(fixedValueIsKept(), fixedValueExcludesNillable());
    }

    private static ValidationRule<BccpValidationInput> fixedValueIsKept() {
        String id = PropertyAttributeRules.id(PREFIX, PropertyAttributeRules.LAST + 1);
        return new FunctionRule<>(id, PropertyAttributeRules.CATEGORY, Severity.ERROR, input -> {
            if (input.previous() == null || input.previous().valueConstraint() == null
                    || !input.previous().valueConstraint().hasFixedValue()) {
                return Stream.empty();
            }
            String fixed = input.root().valueConstraint() == null ? null : input.root().valueConstraint().fixedValue();
            return Objects.equals(fixed, input.previous().valueConstraint().fixedValue()) ? Stream.empty()
                    : Stream.of(PropertyAttributeRules.onRoot(id, Severity.ERROR,
                    "The fixed value of the previous revision must not be changed.", input));
        });
    }

    private static ValidationRule<BccpValidationInput> fixedValueExcludesNillable() {
        String id = PropertyAttributeRules.id(PREFIX, PropertyAttributeRules.LAST + 2);
        return new FunctionRule<>(id, PropertyAttributeRules.CATEGORY, Severity.ERROR, input ->
                input.root().nillable() && input.root().valueConstraint() != null && input.root().valueConstraint().hasFixedValue()
                        ? Stream.of(PropertyAttributeRules.onRoot(id, Severity.ERROR, "A property with a fixed value must not be nillable.", input))
                        : Stream.empty());
    }
}
