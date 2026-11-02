package org.oagi.score.gateway.http.api.validation;

import java.util.Set;
import java.util.stream.Stream;

/**
 * One check on the input of a subject. It is a pure function of the input: it reads nothing else and is not expected to
 * throw (the engine reports a rule that does, and goes on).
 */
public interface ValidationRule<I> {

    String id();

    String category();

    Severity severity();

    /** The triggers the rule applies to; by default the ones that are about the state of the subject itself. */
    default Set<ValidationTrigger> triggers() {
        return Set.of(ValidationTrigger.ON_DEMAND, ValidationTrigger.STATE_TRANSITION, ValidationTrigger.RELEASE);
    }

    Stream<Violation> evaluate(I input);
}
