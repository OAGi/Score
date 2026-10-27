package org.oagi.score.gateway.http.api.cc_management.validator.rules;

import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.validator.DtValidationInput;
import org.oagi.score.gateway.http.api.cc_management.validator.DtValidationInput.SupplementaryComponent;
import org.oagi.score.gateway.http.api.validation.Location;
import org.oagi.score.gateway.http.api.validation.Severity;
import org.oagi.score.gateway.http.api.validation.ValidationRule;
import org.oagi.score.gateway.http.api.validation.Violation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.oagi.score.gateway.http.api.cc_management.validator.rules.AccRuleSupport.blank;
import static org.oagi.score.gateway.http.api.cc_management.validator.rules.AccRuleSupport.on;

/** The rules about the supplementary components (SCs) of a DT. */
public final class DtSupplementaryComponentRules {

    private static final String PREFIX = "DT-SC-";
    private static final String CATEGORY = "SUPPLEMENTARY-COMPONENT";

    private DtSupplementaryComponentRules() {
    }

    public static List<ValidationRule<DtValidationInput>> all() {
        return List.of(uniqueTerms(), cardinality(), definitionIsGiven());
    }

    /** Two SCs of one DT with the same property term and the same representation term: they have the same name. */
    private static ValidationRule<DtValidationInput> uniqueTerms() {
        String id = PREFIX + "01";
        return new FunctionRule<>(id, CATEGORY, Severity.WARNING, input -> {
            Map<List<String>, List<SupplementaryComponent>> byTerms = new LinkedHashMap<>();
            for (SupplementaryComponent e : input.supplementaryComponents()) {
                byTerms.computeIfAbsent(java.util.Arrays.asList(e.sc().propertyTerm(), e.sc().representationTerm()),
                        k -> new ArrayList<>()).add(e);
            }
            return byTerms.values().stream().filter(list -> list.size() > 1).map(list -> {
                List<Location> locations = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    locations.add(new Location(list.get(i).sc().den(), list.get(i).path(), i == 0 ? "first occurrence" : "conflicts with"));
                }
                return new Violation(id, Severity.WARNING,
                        "The property term \"" + list.get(0).sc().propertyTerm() + "\" and the representation term \""
                                + list.get(0).sc().representationTerm() + "\" are used by more than one supplementary component of this data type.",
                        Map.of(), locations);
            });
        });
    }

    private static ValidationRule<DtValidationInput> cardinality() {
        String id = PREFIX + "02";
        return new FunctionRule<>(id, CATEGORY, Severity.ERROR, input -> input.supplementaryComponents().stream()
                .filter(e -> wrong(e.sc().cardinality()))
                .map(e -> on(id, Severity.ERROR, "The cardinality of the supplementary component is not valid.",
                        e.sc().den(), e.path())));
    }

    private static boolean wrong(Cardinality c) {
        return c != null && ((c.min() != null && c.min() < 0)
                || (c.max() != null && c.max() < -1)
                || (c.min() != null && c.max() != null && c.max() >= 0 && c.min() > c.max()));
    }

    private static ValidationRule<DtValidationInput> definitionIsGiven() {
        String id = PREFIX + "03";
        return new FunctionRule<>(id, CATEGORY, Severity.WARNING, input -> input.supplementaryComponents().stream()
                .filter(e -> blank(e.sc().definition()))
                .map(e -> on(id, Severity.WARNING, "The supplementary component has no definition.", e.sc().den(), e.path())));
    }
}
