package org.oagi.score.gateway.http.api.validation;

/** Why a validation runs; a rule can limit itself to some of these. */
public enum ValidationTrigger {
    /** The user asked for it. */
    ON_DEMAND,
    /** A component is about to move on to the next state. */
    STATE_TRANSITION,
    /** A release is being validated. */
    RELEASE
}
