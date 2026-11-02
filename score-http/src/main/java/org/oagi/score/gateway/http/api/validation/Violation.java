package org.oagi.score.gateway.http.api.validation;

import java.util.List;
import java.util.Map;

/**
 * One problem found: what rule found it, how serious it is, a text for the user, and the node(s) it is about. The
 * subject is set when a validation reports on more than one subject (the ones a change would affect); it is null when
 * every violation of the report is about the subject of the report.
 */
public record Violation(String ruleId,
                        Severity severity,
                        String message,
                        Map<String, String> params,
                        List<Location> locations,
                        SubjectRef subject) {

    public Violation(String ruleId, Severity severity, String message, Map<String, String> params, List<Location> locations) {
        this(ruleId, severity, message, params, locations, null);
    }

    public Violation about(SubjectRef subject) {
        return new Violation(ruleId, severity, message, params, locations, subject);
    }
}
