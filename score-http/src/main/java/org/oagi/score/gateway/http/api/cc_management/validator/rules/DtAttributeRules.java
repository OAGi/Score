package org.oagi.score.gateway.http.api.cc_management.validator.rules;

import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.validator.DtValidationInput;
import org.oagi.score.gateway.http.api.validation.Location;
import org.oagi.score.gateway.http.api.validation.Severity;
import org.oagi.score.gateway.http.api.validation.ValidationRule;
import org.oagi.score.gateway.http.api.validation.Violation;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.oagi.score.gateway.http.api.cc_management.validator.rules.AccRuleSupport.blank;

/** The rules about what a DT is by itself, and what a revision of it keeps. */
public final class DtAttributeRules {

    private static final String PREFIX = "DT-ATTR-";
    private static final String CATEGORY = "ATTRIBUTE";

    private DtAttributeRules() {
    }

    public static List<ValidationRule<DtValidationInput>> all() {
        return List.of(
                own(1, Severity.ERROR, "The namespace is required.",
                        input -> input.root().namespaceId() == null),
                own(2, Severity.ERROR, "A data type of a developer must have a standard namespace.",
                        input -> input.root().owner() != null && input.root().owner().isDeveloper()
                                && input.namespace() != null && !input.namespace().standard()),
                own(3, Severity.ERROR, "A data type of the first revision must not be deprecated.",
                        input -> input.root().revisionNum() <= 1 && input.root().deprecated()),
                revision(4, Severity.ERROR, "The namespace of a revision must not differ from the previous revision.",
                        (dt, previous) -> !Objects.equals(dt.namespaceId(), previous.namespaceId())),
                revision(5, Severity.ERROR, "The data type term of a revision must not differ from the previous revision.",
                        (dt, previous) -> !Objects.equals(dt.dataTypeTerm(), previous.dataTypeTerm())),
                revision(6, Severity.WARNING, "The data type was deprecated in the previous revision and is not any more.",
                        (dt, previous) -> previous.deprecated() && !dt.deprecated()),
                own(7, Severity.WARNING, "The data type has no definition.",
                        input -> blank(input.root().definition())));
    }

    private static ValidationRule<DtValidationInput> own(int number, Severity severity, String message,
                                                         Predicate<DtValidationInput> wrong) {
        String id = PREFIX + String.format("%02d", number);
        return new FunctionRule<>(id, CATEGORY, severity,
                input -> wrong.test(input) ? Stream.of(onRoot(id, severity, message, input)) : Stream.empty());
    }

    private static ValidationRule<DtValidationInput> revision(int number, Severity severity, String message,
                                                              BiPredicate<DtSummaryRecord, DtSummaryRecord> wrong) {
        String id = PREFIX + String.format("%02d", number);
        return new FunctionRule<>(id, CATEGORY, severity, input -> input.previous() != null
                && wrong.test(input.root(), input.previous()) ? Stream.of(onRoot(id, severity, message, input)) : Stream.empty());
    }

    private static Violation onRoot(String id, Severity severity, String message, DtValidationInput input) {
        return new Violation(id, severity, message, Map.of(),
                List.of(new Location(input.root().den(), input.rootPath(), "problem")));
    }
}
