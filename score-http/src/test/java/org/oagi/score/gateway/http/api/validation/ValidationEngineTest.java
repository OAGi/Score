package org.oagi.score.gateway.http.api.validation;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ValidationEngineTest {

    private final ScoreUser requester = new ScoreUser(new UserId(BigInteger.TEN), "tester", "Tester",
            "tester@example.com", false, List.of(ScoreRole.END_USER));
    private final ValidationSubject subject = new ValidationSubject(SubjectType.ACC, "1");

    private record TestRule(String id, Severity severity, Set<ValidationTrigger> triggers, boolean throwing)
            implements ValidationRule<String> {
        @Override
        public String category() {
            return "TEST";
        }

        @Override
        public Stream<Violation> evaluate(String model) {
            if (throwing) {
                throw new IllegalStateException("boom");
            }
            return Stream.of(new Violation(id, severity, model, Map.of(),
                    List.of(new Location(id, List.of(), null))));
        }
    }

    private ValidationEngine engineWith(ValidationRule<String>... rules) {
        return new ValidationEngine(List.of(new SubjectValidator<String>() {
            @Override
            public SubjectType type() {
                return SubjectType.ACC;
            }

            @Override
            public ValidationInputLoader<String> loader() {
                return (user, s) -> "model of " + s.id();
            }

            @Override
            public List<ValidationRule<String>> rules() {
                return List.of(rules);
            }
        }));
    }

    private static TestRule rule(String id, Severity severity) {
        return new TestRule(id, severity, Set.of(ValidationTrigger.values()), false);
    }

    @Test
    void errors_come_first_and_decide_the_status() {
        ValidationReport report = engineWith(rule("B", Severity.WARNING), rule("C", Severity.ERROR), rule("A", Severity.INFO))
                .validate(requester, subject, ValidationTrigger.ON_DEMAND);

        assertEquals(List.of("C", "B", "A"), report.violations().stream().map(Violation::ruleId).toList());
        assertEquals(ReportStatus.HAS_ERRORS, report.status());
        assertEquals("model of 1", report.violations().get(0).message());
    }

    @Test
    void only_warnings_make_a_report_with_warnings_and_nothing_makes_it_valid() {
        assertEquals(ReportStatus.HAS_WARNINGS, engineWith(rule("A", Severity.WARNING))
                .validate(requester, subject, ValidationTrigger.ON_DEMAND).status());
        assertEquals(ReportStatus.VALID, engineWith(rule("A", Severity.INFO))
                .validate(requester, subject, ValidationTrigger.ON_DEMAND).status());
        assertEquals(ReportStatus.VALID, engineWith().validate(requester, subject, ValidationTrigger.ON_DEMAND).status());
    }

    @Test
    void a_rule_runs_only_for_the_triggers_it_applies_to() {
        ValidationEngine engine = engineWith(
                new TestRule("RELEASE-ONLY", Severity.ERROR, Set.of(ValidationTrigger.RELEASE), false));

        assertTrue(engine.validate(requester, subject, ValidationTrigger.STATE_TRANSITION).violations().isEmpty());
        assertEquals(1, engine.validate(requester, subject, ValidationTrigger.RELEASE).violations().size());
    }

    @Test
    void a_rule_that_throws_is_reported_and_the_others_still_run() {
        ValidationReport report = engineWith(
                new TestRule("BAD", Severity.ERROR, Set.of(ValidationTrigger.values()), true),
                rule("GOOD", Severity.WARNING))
                .validate(requester, subject, ValidationTrigger.ON_DEMAND);

        assertEquals(List.of("GOOD", "VALIDATION-RULE-FAILED"), report.violations().stream().map(Violation::ruleId).toList());
        assertEquals(ReportStatus.HAS_WARNINGS, report.status());
    }

    @Test
    void a_subject_without_a_validator_is_a_bad_request() {
        ValidationEngine engine = new ValidationEngine(List.of());
        assertThrows(IllegalArgumentException.class, () -> engine.validate(requester, subject, ValidationTrigger.ON_DEMAND));
    }

    @Test
    void two_validators_for_one_type_are_refused() {
        SubjectValidator<String> one = new SubjectValidator<>() {
            public SubjectType type() { return SubjectType.ACC; }
            public ValidationInputLoader<String> loader() { return (u, s) -> ""; }
            public List<ValidationRule<String>> rules() { return List.of(); }
        };
        assertThrows(IllegalStateException.class, () -> new ValidationEngine(List.of(one, one)));
    }
}
