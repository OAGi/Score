package org.oagi.score.gateway.http.api.cc_management.validator.rules;

import org.oagi.score.gateway.http.api.validation.Severity;
import org.oagi.score.gateway.http.api.validation.ValidationRule;
import org.oagi.score.gateway.http.api.validation.Violation;

import java.util.function.Function;
import java.util.stream.Stream;

/** A rule that is a function of its input. */
public record FunctionRule<I>(String id, String category, Severity severity,
                              Function<I, Stream<Violation>> check) implements ValidationRule<I> {

    @Override
    public Stream<Violation> evaluate(I input) {
        return check.apply(input);
    }
}
