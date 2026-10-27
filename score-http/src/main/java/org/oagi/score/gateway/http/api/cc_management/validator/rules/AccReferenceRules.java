package org.oagi.score.gateway.http.api.cc_management.validator.rules;

import org.oagi.score.gateway.http.api.cc_management.validator.AccValidationInput;
import org.oagi.score.gateway.http.api.validation.Severity;
import org.oagi.score.gateway.http.api.validation.ValidationRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Stream;

import static org.oagi.score.gateway.http.api.cc_management.validator.rules.AccRuleSupport.on;

/** The rules about what the associations of an ACC point at: who owns it and how many point at it. */
@Configuration
public class AccReferenceRules {

    @Bean
    ValidationRule<AccValidationInput> accNonReusableAsccpIsReferencedOnce() {
        return new FunctionRule<>("ACC-RI-01", "REFERENCE-INTEGRITY", Severity.ERROR, input -> input.ownAsccs().stream()
                .filter(a -> a.asccp() != null && !a.asccp().reusable() && a.references() > 1)
                .map(a -> on("ACC-RI-01", Severity.ERROR,
                        "The ASCCP is not reusable but " + a.references() + " ASCCs point at it.", a.asccp().den(), a.path())));
    }

    @Bean
    ValidationRule<AccValidationInput> accDeveloperAccUsesDeveloperProperties() {
        return new FunctionRule<>("ACC-RI-02", "REFERENCE-INTEGRITY", Severity.ERROR, input -> {
            var owner = input.root().owner();
            if (owner == null || !owner.isDeveloper()) {
                return Stream.empty();
            }
            String message = "An ACC of a developer points at a property of an end user.";
            return Stream.concat(
                    input.ownAsccs().stream()
                            .filter(a -> a.asccp() != null && endUser(a.asccp().owner()))
                            .map(a -> on("ACC-RI-02", Severity.ERROR, message, a.asccp().den(), a.path())),
                    input.ownBccs().stream()
                            .filter(b -> b.bccp() != null && endUser(b.bccp().owner()))
                            .map(b -> on("ACC-RI-02", Severity.ERROR, message, b.bccp().den(), b.path())));
        });
    }

    private static boolean endUser(org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord owner) {
        return owner != null && !owner.isDeveloper();
    }
}
