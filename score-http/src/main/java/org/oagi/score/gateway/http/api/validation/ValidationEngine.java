package org.oagi.score.gateway.http.api.validation;

import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The one way in to validation. It finds the {@link SubjectValidator} of the kind of subject, loads the input once, runs
 * the rules that apply to the trigger and sorts what they found. A new kind of subject needs only a new validator bean.
 */
@Service
@Transactional(readOnly = true)
public class ValidationEngine {

    private static final String RULE_FAILED = "VALIDATION-RULE-FAILED";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<SubjectType, SubjectValidator<?>> validators = new EnumMap<>(SubjectType.class);

    @Autowired
    public ValidationEngine(List<SubjectValidator<?>> validators) {
        for (SubjectValidator<?> validator : validators) {
            if (this.validators.put(validator.type(), validator) != null) {
                throw new IllegalStateException("More than one validator for " + validator.type());
            }
        }
    }

    public ValidationReport validate(ScoreUser requester, ValidationSubject subject, ValidationTrigger trigger) {
        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }
        if (subject == null || subject.type() == null || subject.id() == null) {
            throw new IllegalArgumentException("'subject' must have a type and an id.");
        }
        if (trigger == null) {
            throw new IllegalArgumentException("'trigger' must not be null.");
        }
        SubjectValidator<?> validator = validators.get(subject.type());
        if (validator == null) {
            throw new IllegalArgumentException("Validation of " + subject.type() + " is not supported.");
        }
        return run(validator, requester, subject, trigger);
    }

    private <I> ValidationReport run(SubjectValidator<I> validator, ScoreUser requester,
                                     ValidationSubject subject, ValidationTrigger trigger) {
        I input = validator.loader().load(requester, subject);
        List<Violation> violations = evaluate(validator, subject, input, trigger).stream().sorted(ORDER).collect(Collectors.toList());
        return new ValidationReport(subject, new Date(), statusOf(violations), violations);
    }

    /**
     * Runs the rules that apply to the trigger on an input that is already there, for a caller that makes the input
     * itself (the ones of the subjects a change would affect, read once for all of them). A rule that fails is reported
     * and does not stop the others.
     */
    public <I> List<Violation> evaluate(SubjectValidator<I> validator, ValidationSubject subject, I input,
                                        ValidationTrigger trigger) {
        List<Violation> violations = new ArrayList<>();
        for (ValidationRule<I> rule : validator.rules()) {
            if (!rule.triggers().contains(trigger)) {
                continue;
            }
            try (Stream<Violation> found = rule.evaluate(input)) {
                found.forEach(violations::add);
            } catch (RuntimeException e) {
                logger.warn("The rule {} failed on {}", rule.id(), subject, e);
                violations.add(new Violation(RULE_FAILED, Severity.INFO,
                        "The rule " + rule.id() + " could not be evaluated.",
                        Map.of("ruleId", rule.id()), List.of()));
            }
        }
        return violations;
    }

    static final Comparator<Violation> ORDER = Comparator
            .comparing(Violation::severity)
            .thenComparing(Violation::ruleId)
            .thenComparing(v -> v.locations().isEmpty() ? "" : v.locations().get(0).label());

    private static ReportStatus statusOf(List<Violation> violations) {
        if (violations.stream().anyMatch(v -> v.severity() == Severity.ERROR)) {
            return ReportStatus.HAS_ERRORS;
        }
        if (violations.stream().anyMatch(v -> v.severity() == Severity.WARNING)) {
            return ReportStatus.HAS_WARNINGS;
        }
        return ReportStatus.VALID;
    }
}
