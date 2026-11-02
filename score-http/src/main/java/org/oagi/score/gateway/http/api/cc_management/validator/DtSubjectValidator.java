package org.oagi.score.gateway.http.api.cc_management.validator;

import org.oagi.score.gateway.http.api.cc_management.validator.rules.*;
import org.oagi.score.gateway.http.api.validation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

/** Validates a DT. */
@Component
public class DtSubjectValidator implements SubjectValidator<DtValidationInput> {

    @Autowired
    private DtValidationInputLoader loader;

    private final List<ValidationRule<DtValidationInput>> rules = Stream.of(
                    DtAttributeRules.all(),
                    DtValueDomainRules.all(),
                    DtSupplementaryComponentRules.all())
            .flatMap(List::stream).toList();

    @Override
    public SubjectType type() {
        return SubjectType.DT;
    }

    @Override
    public ValidationInputLoader<DtValidationInput> loader() {
        return loader;
    }

    @Override
    public List<ValidationRule<DtValidationInput>> rules() {
        return rules;
    }
}
