package org.oagi.score.gateway.http.api.validation;

import java.util.List;
import java.util.Map;

/** One problem found: what rule found it, how serious it is, a text for the user, and the node(s) it is about. */
public record Violation(String ruleId,
                        Severity severity,
                        String message,
                        Map<String, String> params,
                        List<Location> locations) {
}
