package org.oagi.score.gateway.http.change;

/**
 * Tells the other instances that a transaction of this one has committed a change to the change log, so that they read it now
 * and not at their next poll. It carries nothing but the wish: what changed is in the log, which stays the only truth, and an
 * instance that never hears the signal reads the log at its next poll.
 */
public interface ChangeSignal {

    /** Called when what this instance wrote is visible to everyone. Never waits for the signal to arrive and never fails. */
    void announce();

}
