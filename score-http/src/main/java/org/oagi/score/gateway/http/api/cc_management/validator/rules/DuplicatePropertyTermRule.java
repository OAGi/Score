package org.oagi.score.gateway.http.api.cc_management.validator.rules;

import org.oagi.score.gateway.http.api.cc_management.validator.AccValidationInput;
import org.oagi.score.gateway.http.api.cc_management.validator.AccValidationInput.Property;
import org.oagi.score.gateway.http.api.validation.Location;
import org.oagi.score.gateway.http.api.validation.ValidationRule;
import org.oagi.score.gateway.http.api.validation.ValidationTrigger;
import org.oagi.score.gateway.http.api.validation.Severity;
import org.oagi.score.gateway.http.api.validation.Violation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Two properties of an ACC that have the same property term make its content input ambiguous. Which rule owns a
 * duplicate depends on where the properties come from, so each concrete rule takes the duplicates of one origin and the
 * three of them together cover every duplicate once.
 */
abstract class DuplicatePropertyTermRule implements ValidationRule<AccValidationInput> {

    /** Where the properties of a duplicate come from, the widest reach first. */
    enum Origin {
        /** At least one of them comes through a group. */
        GROUP,
        /** They are not in the same ACC: one of them comes from a base ACC. */
        BASE,
        /** They are in the same ACC. */
        OWN
    }

    static Origin originOf(List<Property> duplicates) {
        if (duplicates.stream().anyMatch(Property::viaGroup)) {
            return Origin.GROUP;
        }
        long owners = duplicates.stream().map(p -> p.owner().accManifestId()).distinct().count();
        return owners > 1 ? Origin.BASE : Origin.OWN;
    }

    private final Origin origin;
    private final String id;
    private final String reason;

    DuplicatePropertyTermRule(Origin origin, String id, String reason) {
        this.origin = origin;
        this.id = id;
        this.reason = reason;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String category() {
        return "ASSOCIATION";
    }

    @Override
    public Severity severity() {
        return Severity.ERROR;
    }

    /** A change that moves properties around is what makes duplicates, so these rules are the ones it asks for. */
    @Override
    public Set<ValidationTrigger> triggers() {
        return Set.of(ValidationTrigger.values());
    }

    @Override
    public Stream<Violation> evaluate(AccValidationInput input) {
        List<Violation> violations = new ArrayList<>();
        for (Map.Entry<String, List<Property>> duplicate : input.duplicates().entrySet()) {
            if (originOf(duplicate.getValue()) != origin) {
                continue;
            }
            List<Location> locations = new ArrayList<>();
            for (Property property : duplicate.getValue()) {
                locations.add(property.location(locations.isEmpty() ? "first occurrence" : "conflicts with"));
            }
            violations.add(new Violation(id, Severity.ERROR,
                    "The property term \"" + duplicate.getKey() + "\" " + reason + ".",
                    Map.of("propertyTerm", duplicate.getKey()), locations));
        }
        return violations.stream();
    }
}
