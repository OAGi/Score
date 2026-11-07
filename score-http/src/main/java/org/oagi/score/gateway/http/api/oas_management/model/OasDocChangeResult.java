package org.oagi.score.gateway.http.api.oas_management.model;

import java.util.List;

/**
 * What the server says of a change set: whether it can be saved, what it breaks if not, and what the document would be if
 * it were saved (the request parameters and response headers, those that the system registered for the operations
 * included), which is what the client shows.
 */
public record OasDocChangeResult(boolean valid, List<OasDocViolation> violations, List<OasDocHttpHeader> httpHeaders) {

    public static OasDocChangeResult valid(List<OasDocHttpHeader> httpHeaders) {
        return new OasDocChangeResult(true, List.of(), httpHeaders);
    }

    public static OasDocChangeResult invalid(List<OasDocViolation> violations) {
        return new OasDocChangeResult(false, violations, List.of());
    }
}
