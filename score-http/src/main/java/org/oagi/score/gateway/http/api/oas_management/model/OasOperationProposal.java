package org.oagi.score.gateway.http.api.oas_management.model;

import java.util.List;

/** What the server makes of a candidate operation, and what is wrong with adding it to the document. */
public record OasOperationProposal(String resourceName, String operationId, List<OasDocViolation> violations) {

    public boolean valid() {
        return violations.isEmpty();
    }
}
