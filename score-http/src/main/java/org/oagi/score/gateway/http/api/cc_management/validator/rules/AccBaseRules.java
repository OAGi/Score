package org.oagi.score.gateway.http.api.cc_management.validator.rules;

import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.validator.AccValidationInput;
import org.oagi.score.gateway.http.api.validation.Severity;
import org.oagi.score.gateway.http.api.validation.ValidationRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Stream;

import static org.oagi.score.gateway.http.api.cc_management.validator.rules.AccRuleSupport.on;

/** The rules about the ACC that an ACC is based on. */
@Configuration
public class AccBaseRules {

    @Bean
    ValidationRule<AccValidationInput> accBaseIsNotDeprecated() {
        return new FunctionRule<>("BAS-001", "BASED-ACC", Severity.WARNING, input -> {
            AccSummaryRecord acc = input.root();
            return acc.deprecated() ? Stream.empty() : input.bases().stream().findFirst()
                    .filter(b -> b.acc().deprecated())
                    .map(b -> on("BAS-001", Severity.WARNING,
                            "The base ACC \"" + b.acc().den() + "\" is deprecated.", b.acc().den(), b.path()))
                    .stream();
        });
    }
}
