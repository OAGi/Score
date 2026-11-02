package org.oagi.score.gateway.http.api.cc_management.validator.rules;

import org.oagi.score.gateway.http.api.cc_management.validator.BccpValidationInput;
import org.oagi.score.gateway.http.api.validation.PathSegment;
import org.oagi.score.gateway.http.api.validation.Severity;
import org.oagi.score.gateway.http.api.validation.ValidationRule;

import java.util.List;
import java.util.stream.Stream;

/** The rules about what a BCCP points at: its data type. */
public final class BccpReferenceRules {

    private BccpReferenceRules() {
    }

    public static List<ValidationRule<BccpValidationInput>> all() {
        return List.of(bdtIsNotDeprecated());
    }

    private static ValidationRule<BccpValidationInput> bdtIsNotDeprecated() {
        String id = "REF-001";
        return new FunctionRule<>(id, "REFERENCE-INTEGRITY", Severity.WARNING, input ->
                input.bdt() != null && input.bdt().deprecated() && !input.root().deprecated()
                        ? Stream.of(PropertyAttributeRules.onChild(id, Severity.WARNING, "The data type of the BCCP is deprecated.", input,
                        input.bdt().den(), new PathSegment("DT", String.valueOf(input.bdt().dtManifestId()))))
                        : Stream.empty());
    }
}
