package org.oagi.score.gateway.http.api.cc_management.validator;

import org.oagi.score.gateway.http.api.cc_management.validator.rules.*;
import org.oagi.score.gateway.http.api.validation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

/** Validates a BCCP. */
@Component
public class BccpSubjectValidator implements SubjectValidator<BccpValidationInput> {

    @Autowired
    private BccpValidationInputLoader loader;

    private final List<ValidationRule<BccpValidationInput>> rules = Stream.of(
            PropertyAttributeRules.<BccpValidationInput>of(),
            BccpAttributeRules.all(),
            BccpReferenceRules.all())
            .flatMap(List::stream).toList();

    @Override
    public SubjectType type() {
        return SubjectType.BCCP;
    }

    @Override
    public ValidationInputLoader<BccpValidationInput> loader() {
        return loader;
    }

    @Override
    public List<ValidationRule<BccpValidationInput>> rules() {
        return rules;
    }
}
