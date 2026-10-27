package org.oagi.score.gateway.http.api.validation;

/** Why a validation runs; a rule can limit itself to some of these. */
public enum ValidationTrigger {
    /** The user asked for it. */
    ON_DEMAND,
    /** A component is about to move on to the next state. */
    STATE_TRANSITION,
    /** A release is being validated. */
    RELEASE,
    /**
     * The user is about to change the structure of a core component (refactor to a base ACC, ungroup), and asks what
     * the change would break. A rule runs for it only when it says so: {@link ValidationRule#triggers()} leaves it out.
     */
    STRUCTURE_CHANGE
}
