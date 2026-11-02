package org.oagi.score.gateway.http.api.cc_management.validator.rules;

import org.springframework.stereotype.Component;

/** A group puts a property term in the ACC that the ACC, a base ACC or another group already has. */
@Component
public class AccGroupDuplicateRule extends DuplicatePropertyTermRule {

    public AccGroupDuplicateRule() {
        super(Origin.GROUP, "DUP-003", "is brought in through a group, and this ACC or its base ACC already has it");
    }
}
