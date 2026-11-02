package org.oagi.score.gateway.http.api.cc_management.validator.rules;

import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpType;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.EntityType;
import org.oagi.score.gateway.http.api.cc_management.validator.AccValidationInput;
import org.oagi.score.gateway.http.api.validation.Severity;
import org.oagi.score.gateway.http.api.validation.ValidationRule;
import org.oagi.score.gateway.http.api.validation.Violation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Stream;

import static org.oagi.score.gateway.http.api.cc_management.validator.rules.AccRuleSupport.*;

/** The rules about the associations an ACC has: what they point at, how many of them and how often each can be. */
@Configuration
public class AccAssociationRules {

    @Bean
    ValidationRule<AccValidationInput> accOnlyOneExtension() {
        return new FunctionRule<>("ASC-001", "ASSOCIATION", Severity.ERROR, input -> {
            List<AccValidationInput.OwnAscc> extensions = input.ownAsccs().stream()
                    .filter(a -> a.asccp() != null && a.asccp().type() == AsccpType.Extension).toList();
            return extensions.size() < 2 ? Stream.<Violation>empty()
                    : extensions.stream().map(a -> on("ASC-001", Severity.ERROR,
                    "The ACC has more than one extension.", a.asccp().den(), a.path()));
        });
    }

    @Bean
    ValidationRule<AccValidationInput> accChoiceHasMembers() {
        return new FunctionRule<>("ASC-002", "ASSOCIATION", Severity.WARNING, input ->
                input.root().componentType() == OagisComponentType.Choice
                        && input.ownAsccs().size() + input.ownBccs().size() < 2
                        ? onAcc(input, "ASC-002", Severity.WARNING, "A choice should have at least two members.")
                        : Stream.empty());
    }

    @Bean
    ValidationRule<AccValidationInput> accCardinalityMinIsNotNegative() {
        return cardinality("CRD-001", "The minimum cardinality is less than 0.",
                (c, attribute) -> c.min() != null && c.min() < 0);
    }

    @Bean
    ValidationRule<AccValidationInput> accCardinalityMaxIsNotBelowMinusOne() {
        return cardinality("CRD-002", "The maximum cardinality is less than 0 and is not -1 (unbounded).",
                (c, attribute) -> c.max() != null && c.max() < -1);
    }

    @Bean
    ValidationRule<AccValidationInput> accCardinalityMinIsNotAboveMax() {
        return cardinality("CRD-003", "The minimum cardinality is greater than the maximum cardinality.",
                (c, attribute) -> c.min() != null && c.max() != null && c.max() >= 0 && c.min() > c.max());
    }

    @Bean
    ValidationRule<AccValidationInput> accAttributeMinIsAtMostOne() {
        return cardinality("CRD-004", "The minimum cardinality of an attribute is greater than 1.",
                (c, attribute) -> attribute && c.min() != null && c.min() > 1);
    }

    @Bean
    ValidationRule<AccValidationInput> accAttributeMaxIsZeroOrOne() {
        return cardinality("CRD-005", "The maximum cardinality of an attribute is not 0 or 1.",
                (c, attribute) -> attribute && c.max() != null && (c.max() < 0 || c.max() > 1));
    }

    /** A rule on the cardinality of every association of the ACC; {@code attribute} tells a BCC that is an attribute. */
    private static ValidationRule<AccValidationInput> cardinality(String id, String message,
                                                                 java.util.function.BiPredicate<Cardinality, Boolean> wrong) {
        return new FunctionRule<>(id, "CARDINALITY", Severity.ERROR, input -> Stream.concat(
                input.ownAsccs().stream()
                        .filter(a -> a.ascc().cardinality() != null && wrong.test(a.ascc().cardinality(), false))
                        .map(a -> on(id, Severity.ERROR, message,
                                a.asccp() != null ? a.asccp().den() : a.ascc().den(), a.path())),
                input.ownBccs().stream()
                        .filter(b -> b.bcc().cardinality() != null
                                && wrong.test(b.bcc().cardinality(), b.bcc().entityType() == EntityType.Attribute))
                        .map(b -> on(id, Severity.ERROR, message,
                                b.bccp() != null ? b.bccp().den() : b.bcc().den(), b.path()))));
    }

    @Bean
    ValidationRule<AccValidationInput> accAssociationTargetIsNotDeprecated() {
        String message = "The association points at a deprecated property.";
        return new FunctionRule<>("REF-001", "ASSOCIATION", Severity.WARNING, input -> Stream.concat(
                input.ownAsccs().stream()
                        .filter(a -> !a.ascc().deprecated() && a.asccp() != null && a.asccp().deprecated())
                        .map(a -> on("REF-001", Severity.WARNING, message, a.asccp().den(), a.path())),
                input.ownBccs().stream()
                        .filter(b -> !b.bcc().deprecated() && b.bccp() != null && b.bccp().deprecated())
                        .map(b -> on("REF-001", Severity.WARNING, message, b.bccp().den(), b.path()))));
    }

    @Bean
    ValidationRule<AccValidationInput> accAssociationDefinitionIsGiven() {
        String message = "The association has no definition.";
        return new FunctionRule<>("ATR-008", "ASSOCIATION", Severity.WARNING, input -> Stream.concat(
                input.ownAsccs().stream().filter(a -> blank(a.ascc().definition()))
                        .map(a -> on("ATR-008", Severity.WARNING, message, label(a.asccp(), a.ascc().den()), a.path())),
                input.ownBccs().stream().filter(b -> blank(b.bcc().definition()))
                        .map(b -> on("ATR-008", Severity.WARNING, message, label(b.bccp(), b.bcc().den()), b.path()))));
    }

    @Bean
    ValidationRule<AccValidationInput> accMinimumDoesNotRiseInRevision() {
        return revision("CRD-006", "The minimum cardinality is greater than in the previous revision.",
                (c, p) -> c.min() != null && p.min() != null && c.min() > p.min());
    }

    @Bean
    ValidationRule<AccValidationInput> accUnboundedMaximumIsKeptInRevision() {
        return revision("CRD-007", "The maximum cardinality was unbounded in the previous revision and is not any more.",
                (c, p) -> c.max() != null && p.max() != null && p.max() == -1 && c.max() != -1);
    }

    @Bean
    ValidationRule<AccValidationInput> accMaximumDoesNotFallInRevision() {
        return revision("CRD-008", "The maximum cardinality is less than in the previous revision.",
                (c, p) -> c.max() != null && p.max() != null && p.max() != -1 && c.max() != -1 && c.max() < p.max());
    }

    /** A rule on how the cardinality of every association of the ACC differs from the one of the previous revision. */
    private static ValidationRule<AccValidationInput> revision(String id, String message,
                                                              java.util.function.BiPredicate<Cardinality, Cardinality> wrong) {
        return new FunctionRule<>(id, "CARDINALITY", Severity.ERROR, input -> Stream.concat(
                input.ownAsccs().stream()
                        .filter(a -> a.previous() != null && a.ascc().cardinality() != null && a.previous().cardinality() != null
                                && wrong.test(a.ascc().cardinality(), a.previous().cardinality()))
                        .map(a -> on(id, Severity.ERROR, message, label(a.asccp(), a.ascc().den()), a.path())),
                input.ownBccs().stream()
                        .filter(b -> b.previous() != null && b.bcc().cardinality() != null && b.previous().cardinality() != null
                                && wrong.test(b.bcc().cardinality(), b.previous().cardinality()))
                        .map(b -> on(id, Severity.ERROR, message, label(b.bccp(), b.bcc().den()), b.path()))));
    }
}
