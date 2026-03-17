package org.oagi.score.gateway.http.api.bie_management.service.state_transition;

import org.oagi.score.gateway.http.api.bie_management.service.state_transition.rule.BieStateTransitionRule;

/**
 * Signals that a {@link BieStateTransitionRule} rejected one directed dependency
 * edge during BIE state transition validation.
 *
 * <p>The exception itself intentionally carries no user-facing message. The
 * calling service is responsible for translating the violated dependency edge
 * into dialog text that matches the current UI wording.</p>
 */
public class BieStateTransitionRuleViolationException extends Exception {

    /**
     * Creates a new violation marker for a failed state transition rule.
     */
    public BieStateTransitionRuleViolationException() {
        super();
    }
}
