package org.oagi.score.gateway.http.api.cc_management.validator.rules;

import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.EntityType;
import org.oagi.score.gateway.http.api.cc_management.validator.AccValidationInput;
import org.oagi.score.gateway.http.api.validation.Severity;
import org.oagi.score.gateway.http.api.validation.ValidationRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.stream.Stream;

import static org.oagi.score.gateway.http.api.cc_management.validator.rules.AccRuleSupport.*;

/** The rules about what an ACC is by itself: its name, its namespace, its type and what the type requires. */
@Configuration
public class AccAttributeRules {

    @Bean
    ValidationRule<AccValidationInput> accObjectClassTermRequired() {
        return new FunctionRule<>("ACC-ATTR-01", "ATTRIBUTE", Severity.ERROR, input ->
                blank(input.root().objectClassTerm())
                        ? onAcc(input, "ACC-ATTR-01", Severity.ERROR, "The object class term is required.")
                        : Stream.empty());
    }

    @Bean
    ValidationRule<AccValidationInput> accNamespaceRequired() {
        return new FunctionRule<>("ACC-ATTR-02", "ATTRIBUTE", Severity.ERROR, input ->
                input.root().namespaceId() == null
                        ? onAcc(input, "ACC-ATTR-02", Severity.ERROR, "The namespace is required.")
                        : Stream.empty());
    }

    @Bean
    ValidationRule<AccValidationInput> accSemanticGroupMustNotBeAbstract() {
        return new FunctionRule<>("ACC-ATTR-03", "ATTRIBUTE", Severity.ERROR, input -> {
            AccSummaryRecord acc = input.root();
            return acc.componentType() == OagisComponentType.SemanticGroup && acc.isAbstract()
                    ? onAcc(input, "ACC-ATTR-03", Severity.ERROR, "An ACC of the component type Semantic Group must not be abstract.")
                    : Stream.empty();
        });
    }

    @Bean
    ValidationRule<AccValidationInput> accAttributeGroupHoldsAttributesOnly() {
        return new FunctionRule<>("ACC-ATTR-04", "ATTRIBUTE", Severity.ERROR, input -> {
            if (input.root().componentType() != OagisComponentType.AttributeGroup) {
                return Stream.empty();
            }
            Stream<org.oagi.score.gateway.http.api.validation.Violation> asccs = input.ownAsccs().stream()
                    .map(a -> on("ACC-ATTR-04", Severity.ERROR, "An attribute group has an association that is not an attribute.",
                            a.asccp() != null ? a.asccp().den() : a.ascc().den(), a.path()));
            Stream<org.oagi.score.gateway.http.api.validation.Violation> bccs = input.ownBccs().stream()
                    .filter(b -> b.bcc().entityType() != EntityType.Attribute)
                    .map(b -> on("ACC-ATTR-04", Severity.ERROR, "An attribute group has an association that is not an attribute.",
                            b.bccp() != null ? b.bccp().den() : b.bcc().den(), b.path()));
            return Stream.concat(asccs, bccs);
        });
    }

    @Bean
    ValidationRule<AccValidationInput> accFirstRevisionMustNotBeDeprecated() {
        return new FunctionRule<>("ACC-ATTR-09", "ATTRIBUTE", Severity.ERROR, input -> {
            AccSummaryRecord acc = input.root();
            return acc.revisionNum() <= 1 && acc.deprecated()
                    ? onAcc(input, "ACC-ATTR-09", Severity.ERROR, "An ACC of the first revision must not be deprecated.")
                    : Stream.empty();
        });
    }

    @Bean
    ValidationRule<AccValidationInput> accComponentTypeIsKept() {
        return new FunctionRule<>("ACC-ATTR-05", "ATTRIBUTE", Severity.ERROR, input -> {
            AccSummaryRecord previous = input.previous();
            return previous != null && input.root().componentType() != previous.componentType()
                    ? onAcc(input, "ACC-ATTR-05", Severity.ERROR, "The component type of a revision must not differ from the previous revision.")
                    : Stream.empty();
        });
    }

    @Bean
    ValidationRule<AccValidationInput> accSemanticsDoesNotBecomeAbstract() {
        return new FunctionRule<>("ACC-ATTR-06", "ATTRIBUTE", Severity.ERROR, input -> {
            AccSummaryRecord previous = input.previous();
            return previous != null && input.root().componentType() == OagisComponentType.Semantics
                    && input.root().isAbstract() && !previous.isAbstract()
                    ? onAcc(input, "ACC-ATTR-06", Severity.ERROR, "An ACC of the component type Semantics must not become abstract in a revision.")
                    : Stream.empty();
        });
    }

    @Bean
    ValidationRule<AccValidationInput> accObjectClassTermIsKept() {
        return new FunctionRule<>("ACC-ATTR-07", "ATTRIBUTE", Severity.ERROR, input -> {
            AccSummaryRecord previous = input.previous();
            return previous != null && !Objects.equals(input.root().objectClassTerm(), previous.objectClassTerm())
                    ? onAcc(input, "ACC-ATTR-07", Severity.ERROR, "The object class term of a revision must not differ from the previous revision.")
                    : Stream.empty();
        });
    }

    @Bean
    ValidationRule<AccValidationInput> accNamespaceIsKept() {
        return new FunctionRule<>("ACC-ATTR-08", "ATTRIBUTE", Severity.ERROR, input -> {
            AccSummaryRecord previous = input.previous();
            return previous != null && !Objects.equals(input.root().namespaceId(), previous.namespaceId())
                    ? onAcc(input, "ACC-ATTR-08", Severity.ERROR, "The namespace of a revision must not differ from the previous revision.")
                    : Stream.empty();
        });
    }

    @Bean
    ValidationRule<AccValidationInput> accOnlyCarriedOverAssociationIsDeprecated() {
        String message = "An association that was not in the previous revision must not be deprecated.";
        return new FunctionRule<>("ACC-ATTR-10", "ATTRIBUTE", Severity.ERROR, input -> Stream.concat(
                input.ownAsccs().stream().filter(a -> a.ascc().deprecated() && a.previous() == null)
                        .map(a -> on("ACC-ATTR-10", Severity.ERROR, message, label(a.asccp(), a.ascc().den()), a.path())),
                input.ownBccs().stream().filter(b -> b.bcc().deprecated() && b.previous() == null)
                        .map(b -> on("ACC-ATTR-10", Severity.ERROR, message, label(b.bccp(), b.bcc().den()), b.path()))));
    }

    @Bean
    ValidationRule<AccValidationInput> accDefinitionIsGiven() {
        return new FunctionRule<>("ACC-ATTR-11", "ATTRIBUTE", Severity.WARNING, input ->
                blank(input.root().definition())
                        ? onAcc(input, "ACC-ATTR-11", Severity.WARNING, "The ACC has no definition.")
                        : Stream.empty());
    }

    @Bean
    ValidationRule<AccValidationInput> accElementHasNoDefaultOrFixedValue() {
        return new FunctionRule<>("ACC-ATTR-12", "ATTRIBUTE", Severity.WARNING, input -> input.ownBccs().stream()
                .filter(b -> b.bcc().entityType() == EntityType.Element && b.bcc().valueConstraint() != null
                        && (b.bcc().valueConstraint().hasDefaultValue() || b.bcc().valueConstraint().hasFixedValue()))
                .map(b -> on("ACC-ATTR-12", Severity.WARNING, "An element has a default or a fixed value, which only an attribute has.",
                        label(b.bccp(), b.bcc().den()), b.path())));
    }

    @Bean
    ValidationRule<AccValidationInput> accAttributeHasNoSupplementaryComponent() {
        return new FunctionRule<>("ACC-ATTR-13", "ATTRIBUTE", Severity.ERROR, input -> input.ownBccs().stream()
                .filter(AccValidationInput.OwnBcc::targetHasSupplementaryComponents)
                .map(b -> on("ACC-ATTR-13", Severity.ERROR, "An attribute has a data type with a supplementary component.",
                        label(b.bccp(), b.bcc().den()), b.path())));
    }

    @Bean
    ValidationRule<AccValidationInput> accNamespaceMatchesItsOwner() {
        return new FunctionRule<>("ACC-ATTR-14", "ATTRIBUTE", Severity.ERROR, input -> {
            var owner = input.root().owner();
            return owner != null && owner.isDeveloper() && input.namespace() != null && !input.namespace().standard()
                    ? onAcc(input, "ACC-ATTR-14", Severity.ERROR, "An ACC of a developer must have a standard namespace.")
                    : Stream.empty();
        });
    }
}
