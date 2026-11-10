package org.oagi.score.gateway.http.common.async;

import java.time.Duration;

/**
 * What is done when background work has failed: give it back (undo what it was for, and tell who asked for it), or try it
 * again. It is asked after every failure of the work, with how many times the work has been tried, and the answer is the
 * policy's alone: what the work is, and what it is worth to try again, differs between kinds of work.
 *
 * <p>What is used now, {@link GiveBackRetryPolicy}, always gives back. To retry, a policy answers
 * {@link Decision#retryAfter}: the runner waits and does the work again from the idempotency guard, so work that is done again must
 * be work that can be (the guard sees what has been done). A policy is put in place by being the bean of this type.</p>
 */
@FunctionalInterface
public interface RetryPolicy {

    /** @param attempt how many times the work has been tried, this one included, from 1 */
    record Failure(String jobName, int attempt, Throwable cause) {
    }

    record Decision(boolean retry, Duration delay) {

        public static final Decision GIVE_BACK = new Decision(false, Duration.ZERO);

        public static Decision retryAfter(Duration delay) {
            return new Decision(true, delay);
        }
    }

    Decision decide(Failure failure);
}
