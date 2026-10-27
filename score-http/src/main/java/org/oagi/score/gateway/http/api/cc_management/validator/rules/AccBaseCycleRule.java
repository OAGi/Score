package org.oagi.score.gateway.http.api.cc_management.validator.rules;

import org.oagi.score.gateway.http.api.cc_management.validator.AccValidationInput;
import org.oagi.score.gateway.http.api.validation.Location;
import org.oagi.score.gateway.http.api.validation.ValidationRule;
import org.oagi.score.gateway.http.api.validation.Severity;
import org.oagi.score.gateway.http.api.validation.Violation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** The base ACCs of an ACC lead back to an ACC that is already on the way. */
@Component
public class AccBaseCycleRule implements ValidationRule<AccValidationInput> {

    @Override
    public String id() {
        return "ACC-BASE-03";
    }

    @Override
    public String category() {
        return "BASED-ACC";
    }

    @Override
    public Severity severity() {
        return Severity.ERROR;
    }

    @Override
    public Stream<Violation> evaluate(AccValidationInput input) {
        return input.baseCycles().stream().map(cycle -> new Violation(id(), Severity.ERROR,
                "The chain of base ACCs forms a cycle: \"" + cycle.acc().den() + "\" is based on \""
                        + cycle.base().den() + "\", which is already above it.",
                Map.of("acc", cycle.acc().den(), "base", cycle.base().den()),
                List.of(new Location(cycle.base().den(), cycle.path(), "cycle"))));
    }
}
