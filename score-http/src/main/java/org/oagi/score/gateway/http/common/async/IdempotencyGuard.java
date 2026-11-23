package org.oagi.score.gateway.http.common.async;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * What makes the handler of an event an idempotent consumer: whatever way the event is delivered, it may come more than
 * once (to every instance, again after a failure, again after a rebalance), and the work is done for the first of them
 * only. The work then does not have to be work that can be done twice.
 *
 * <p>It is asked first, in the transaction of the work, so that what it reads is what the work would change, and what it
 * locks is held until the work has ended: the second one waits for the first, and finds the work done. How the first
 * handling is told from the others is for the guard to say, and the work and the transport do not know it: the state
 * of a row that the work moves on at its end ({@link #stateIs}), or, for work that has no state of its own, a record of the
 * events that have been handled.</p>
 */
@FunctionalInterface
public interface IdempotencyGuard {

    /** Whether this is the first handling of the event: when it is not, the run ends as {@link AsyncJob.Outcome#ALREADY_HANDLED}. */
    boolean isFirstHandling();

    /**
     * The first handling is the one that finds the state that the work is moved on from at its end. The state is read
     * locked until the end of the transaction, and a state that is not there (the thing was removed, as a failed one is)
     * is not the first handling.
     *
     * @param lockedState reads the state of what the event is about, locked until the end of the transaction
     * @param expected    the state it is in until the work has moved it
     */
    static <S> IdempotencyGuard stateIs(Supplier<Optional<S>> lockedState, S expected) {
        return () -> lockedState.get().filter(expected::equals).isPresent();
    }
}
