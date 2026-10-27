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
 * revision to the next. The ids start with the kind of the subject, {@code ASCCP-ATTR-01} or {@code BCCP-ATTR-01}.
 */
public final class PropertyAttributeRules {

    private PropertyAttributeRules() {
    }

    /** The rules for the kind of subject whose ids start with {@code prefix}. */
    public static <M extends PropertyValidationInput> List<ValidationRule<M>> of(String prefix) {
        List<ValidationRule<M>> rules = new ArrayList<>();
        rules.add(rule(prefix, 1, Severity.ERROR, "The property term is required.",
                (m, f) -> blank(f.propertyTerm())));
        rules.add(rule(prefix, 2, Severity.ERROR, "The namespace is required.",
                (m, f) -> f.namespaceId() == null));
        rules.add(rule(prefix, 3, Severity.ERROR, "A property of a developer must have a standard namespace.",
                (m, f) -> f.owner() != null && f.owner().isDeveloper() && m.namespace() != null && !m.namespace().standard()));
        rules.add(rule(prefix, 4, Severity.ERROR, "A property of the first revision must not be deprecated.",
                (m, f) -> f.revisionNum() <= 1 && f.deprecated()));
        rules.add(revision(prefix, 5, Severity.ERROR, "The namespace of a revision must not differ from the previous revision.",
                (f, p) -> !Objects.equals(f.namespaceId(), p.namespaceId())));
        rules.add(revision(prefix, 6, Severity.ERROR, "The property term of a revision must not differ from the previous revision.",
                (f, p) -> !Objects.equals(f.propertyTerm(), p.propertyTerm())));
        rules.add(revision(prefix, 7, Severity.WARNING, "The property was deprecated in the previous revision and is not any more.",
                (f, p) -> p.deprecated() && !f.deprecated()));
        rules.add(revision(prefix, 8, Severity.WARNING, "The property was nillable in the previous revision and is not any more.",
                (f, p) -> p.nillable() && !f.nillable()));
        rules.add(rule(prefix, 9, Severity.WARNING, "The property has no definition.",
                (m, f) -> blank(f.definition())));
        return rules;
    }

    /** The number of the last rule of {@link #of}; the rules of one kind of subject go on from it. */
    public static final int LAST = 9;

    static final String CATEGORY = "ATTRIBUTE";

    /** A rule on the property itself. */
    private static <M extends PropertyValidationInput> ValidationRule<M> rule(String prefix, int number, Severity severity, String message,
                                                                    java.util.function.BiPredicate<M, Facts> wrong) {
        return new FunctionRule<>(id(prefix, number), CATEGORY, severity, input -> wrong.test(input, input.facts())
                ? Stream.of(onRoot(id(prefix, number), severity, message, input)) : Stream.empty());
    }

    /** A rule on how the property differs from the previous revision. */
    private static <M extends PropertyValidationInput> ValidationRule<M> revision(String prefix, int number, Severity severity, String message,
                                                                        java.util.function.BiPredicate<Facts, Facts> wrong) {
        return new FunctionRule<>(id(prefix, number), CATEGORY, severity, input -> input.previousFacts() != null
                && wrong.test(input.facts(), input.previousFacts())
                ? Stream.of(onRoot(id(prefix, number), severity, message, input)) : Stream.empty());
    }

    static String id(String prefix, int number) {
        return prefix + "-ATTR-" + String.format("%02d", number);
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
