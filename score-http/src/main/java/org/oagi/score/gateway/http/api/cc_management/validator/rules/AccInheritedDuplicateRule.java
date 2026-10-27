package org.oagi.score.gateway.http.api.cc_management.validator.rules;

import org.springframework.stereotype.Component;

/** A property term of the ACC is also one of a base ACC. */
@Component
public class AccInheritedDuplicateRule extends DuplicatePropertyTermRule {

    public AccInheritedDuplicateRule() {
        super(Origin.BASE, "DUP-002", "is used by this ACC and also by one of its base ACCs");
    }
}
