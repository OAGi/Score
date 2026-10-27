package org.oagi.score.gateway.http.api.cc_management.validator;

import org.oagi.score.gateway.http.api.validation.ValidationRule;
import org.oagi.score.gateway.http.api.validation.ValidationInputLoader;
import org.oagi.score.gateway.http.api.validation.SubjectType;
import org.oagi.score.gateway.http.api.validation.SubjectValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/** Validates an ACC: the input is the ACC laid out as its tree, the rules are whatever {@code ValidationRule<AccValidationInput>} beans there are. */
@Component
public class AccSubjectValidator implements SubjectValidator<AccValidationInput> {

    @Autowired
    private AccValidationInputLoader loader;

    @Autowired
    private List<ValidationRule<AccValidationInput>> rules;

    @Override
    public SubjectType type() {
        return SubjectType.ACC;
    }

    @Override
    public ValidationInputLoader<AccValidationInput> loader() {
        return loader;
    }

    @Override
    public List<ValidationRule<AccValidationInput>> rules() {
        return rules;
    }
}
