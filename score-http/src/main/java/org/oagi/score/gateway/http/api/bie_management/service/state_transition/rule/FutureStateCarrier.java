package org.oagi.score.gateway.http.api.bie_management.service.state_transition.rule;

/**
 * Common shape for a transition participant that carries a domain record and
 * its projected future state.
 *
 * @param <R> domain record type participating in the transition
 * @param <S> future state type used by the rule layer
 */
public interface FutureStateCarrier<R, S> {

    /**
     * Returns the domain record participating in the transition.
     *
     * @return the underlying domain record
     */
    R record();

    /**
     * Returns the projected future state used for rule evaluation.
     *
     * @return the future state
     */
    S futureState();
}
