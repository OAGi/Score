package org.oagi.score.gateway.http.api.cc_management.validator.rules;

import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.validator.AccValidationInput;
import org.oagi.score.gateway.http.api.validation.Location;
import org.oagi.score.gateway.http.api.validation.PathSegment;
import org.oagi.score.gateway.http.api.validation.Severity;
import org.oagi.score.gateway.http.api.validation.Violation;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** What the rules of an ACC build a violation of. */
final class AccRuleSupport {

    private AccRuleSupport() {
    }

    /** A problem of the ACC itself. */
    static Stream<Violation> onAcc(AccValidationInput input, String ruleId, Severity severity, String message) {
        return Stream.of(new Violation(ruleId, severity, message, Map.of(),
                List.of(new Location(input.root().den(), List.of(new PathSegment("ACC",
                        String.valueOf(input.root().accManifestId()))), "problem"))));
    }

    /** A problem of a node under the ACC. */
    static Violation on(String ruleId, Severity severity, String message, String label, List<PathSegment> path) {
        return new Violation(ruleId, severity, message, Map.of(), List.of(new Location(label, path, "problem")));
    }

    /** What names an association on the screen: its property, or the association when there is no property. */
    static String label(AsccpSummaryRecord asccp, String fallback) {
        return asccp != null ? asccp.den() : fallback;
    }

    static String label(BccpSummaryRecord bccp, String fallback) {
        return bccp != null ? bccp.den() : fallback;
    }

    static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    static boolean blank(Definition definition) {
        return definition == null || blank(definition.content());
    }
}
