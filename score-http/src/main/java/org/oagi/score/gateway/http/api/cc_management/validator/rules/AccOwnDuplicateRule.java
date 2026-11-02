package org.oagi.score.gateway.http.api.cc_management.validator.rules;

import org.springframework.stereotype.Component;

/** Two associations of the same ACC share a property term. */
@Component
public class AccOwnDuplicateRule extends DuplicatePropertyTermRule {

    public AccOwnDuplicateRule() {
        super(Origin.OWN, "DUP-001", "is used by more than one association of this ACC");
    }
}
