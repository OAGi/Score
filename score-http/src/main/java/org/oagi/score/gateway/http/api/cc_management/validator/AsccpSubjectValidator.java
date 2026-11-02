package org.oagi.score.gateway.http.api.cc_management.validator;

import org.oagi.score.gateway.http.api.cc_management.validator.rules.*;
import org.oagi.score.gateway.http.api.validation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

/** Validates an ASCCP. */
@Component
public class AsccpSubjectValidator implements SubjectValidator<AsccpValidationInput> {

    @Autowired
    private AsccpValidationInputLoader loader;

    private final List<ValidationRule<AsccpValidationInput>> rules = Stream.of(
            PropertyAttributeRules.<AsccpValidationInput>of(),
            AsccpReferenceRules.all())
            .flatMap(List::stream).toList();

    @Override
    public SubjectType type() {
        return SubjectType.ASCCP;
    }

    @Override
    public ValidationInputLoader<AsccpValidationInput> loader() {
        return loader;
    }

    @Override
    public List<ValidationRule<AsccpValidationInput>> rules() {
        return rules;
    }
}
