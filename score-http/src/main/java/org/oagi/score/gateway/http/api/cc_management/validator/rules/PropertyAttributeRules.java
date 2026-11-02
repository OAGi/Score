package org.oagi.score.gateway.http.api.cc_management.validator.rules;

import org.oagi.score.gateway.http.api.cc_management.validator.PropertyValidationInput;
import org.oagi.score.gateway.http.api.cc_management.validator.PropertyValidationInput.Facts;
import org.oagi.score.gateway.http.api.validation.Location;
import org.oagi.score.gateway.http.api.validation.PathSegment;
import org.oagi.score.gateway.http.api.validation.Severity;
import org.oagi.score.gateway.http.api.validation.ValidationRule;
import org.oagi.score.gateway.http.api.validation.Violation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.oagi.score.gateway.http.api.cc_management.validator.rules.AccRuleSupport.blank;

/**
 * The rules an ASCCP and a BCCP share: what a property of a core component is by itself, and what it keeps from one
 * revision to the next. The ids do not name the kind of the subject: a problem has the same id whatever it is found in.
 */
public final class PropertyAttributeRules {

    private PropertyAttributeRules() {
    }

    /** The rules for the attributes of a property. */
    public static <M extends PropertyValidationInput> List<ValidationRule<M>> of() {
        List<ValidationRule<M>> rules = new ArrayList<>();
        rules.add(rule("ATR-001", Severity.ERROR, "The property term is required.",
                (m, f) -> blank(f.propertyTerm())));
        rules.add(rule("ATR-002", Severity.ERROR, "The namespace is required.",
                (m, f) -> f.namespaceId() == null));
        rules.add(rule("ATR-003", Severity.ERROR, "A property of a developer must have a standard namespace.",
                (m, f) -> f.owner() != null && f.owner().isDeveloper() && m.namespace() != null && !m.namespace().standard()));
        rules.add(rule("ATR-004", Severity.ERROR, "A property of the first revision must not be deprecated.",
                (m, f) -> f.revisionNum() <= 1 && f.deprecated()));
        rules.add(revision("ATR-005", Severity.ERROR, "The namespace of a revision must not differ from the previous revision.",
                (f, p) -> !Objects.equals(f.namespaceId(), p.namespaceId())));
        rules.add(revision("ATR-006", Severity.ERROR, "The property term of a revision must not differ from the previous revision.",
                (f, p) -> !Objects.equals(f.propertyTerm(), p.propertyTerm())));
        rules.add(revision("ATR-007", Severity.WARNING, "The property was deprecated in the previous revision and is not any more.",
                (f, p) -> p.deprecated() && !f.deprecated()));
        rules.add(revision("ATR-009", Severity.WARNING, "The property was nillable in the previous revision and is not any more.",
                (f, p) -> p.nillable() && !f.nillable()));
        rules.add(rule("ATR-008", Severity.WARNING, "The property has no definition.",
                (m, f) -> blank(f.definition())));
        return rules;
    }

    static final String CATEGORY = "ATTRIBUTE";

    /** A rule on the property itself. */
    private static <M extends PropertyValidationInput> ValidationRule<M> rule(String id, Severity severity, String message,
                                                                    java.util.function.BiPredicate<M, Facts> wrong) {
        return new FunctionRule<>(id, CATEGORY, severity, input -> wrong.test(input, input.facts())
                ? Stream.of(onRoot(id, severity, message, input)) : Stream.empty());
    }

    /** A rule on how the property differs from the previous revision. */
    private static <M extends PropertyValidationInput> ValidationRule<M> revision(String id, Severity severity, String message,
                                                                        java.util.function.BiPredicate<Facts, Facts> wrong) {
        return new FunctionRule<>(id, CATEGORY, severity, input -> input.previousFacts() != null
                && wrong.test(input.facts(), input.previousFacts())
                ? Stream.of(onRoot(id, severity, message, input)) : Stream.empty());
    }

    static Violation onRoot(String id, Severity severity, String message, PropertyValidationInput input) {
        return new Violation(id, severity, message, Map.of(),
                List.of(new Location(input.facts().den(), input.rootPath(), "problem")));
    }

    /** A violation about a node under the root of the tree. */
    static Violation onChild(String id, Severity severity, String message, PropertyValidationInput input, String label, PathSegment child) {
        List<PathSegment> path = new ArrayList<>(input.rootPath());
        path.add(child);
        return new Violation(id, severity, message, Map.of(), List.of(new Location(label, path, "problem")));
    }
}
