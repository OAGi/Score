package org.oagi.score.gateway.http.api.cc_management.validator.rules;

import org.oagi.score.gateway.http.api.cc_management.validator.AsccpValidationInput;
import org.oagi.score.gateway.http.api.validation.PathSegment;
import org.oagi.score.gateway.http.api.validation.Severity;
import org.oagi.score.gateway.http.api.validation.ValidationRule;

import java.util.List;
import java.util.stream.Stream;

/** The rules about what an ASCCP points at: the ACC it is the role of. */
public final class AsccpReferenceRules {

    private AsccpReferenceRules() {
    }

    public static List<ValidationRule<AsccpValidationInput>> all() {
        return List.of(roleOfAccIsNotDeprecated());
    }

    private static ValidationRule<AsccpValidationInput> roleOfAccIsNotDeprecated() {
        String id = "REF-001";
        return new FunctionRule<>(id, "REFERENCE-INTEGRITY", Severity.WARNING, input ->
                input.roleOf() != null && input.roleOf().deprecated() && !input.root().deprecated()
                        ? Stream.of(PropertyAttributeRules.onChild(id, Severity.WARNING, "The ACC of the ASCCP is deprecated.", input,
                        input.roleOf().den(), new PathSegment("ACC", String.valueOf(input.roleOf().accManifestId()))))
                        : Stream.empty());
    }
}
