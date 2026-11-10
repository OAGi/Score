package org.oagi.score.gateway.http.common.async;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * One run of background work, as {@link AsyncJobRunner} runs it.
 *
 * <p>The work is all or nothing: it is done in a transaction of its own, and what it wrote is rolled back when it fails.
 * What it does about the failure is said in {@code onFailure}, which runs in a transaction of its own after that (give the
 * thing back, remove what was half made, report it).</p>
 *
 * @param name      what is run, for the log; give the one it is run for ({@code release.drafting#12})
 * @param timeout   when set, the longest the transaction of the work may last; after it the work is rolled back as when
 *                  it fails
 * @param idempotencyGuard when set, asked first in the transaction of the work whether this is the first handling of the
 *                  event: when it is not, the run does nothing and ends as {@link Outcome#ALREADY_HANDLED}
 * @param work      the work, in the transaction
 * @param onFailure when set, told why the work failed, in a transaction of its own
 * @param transactional whether the work is done in a transaction (the default); work that touches no database does not
 *                  need one, and neither does what it does about a failure then
 * @param rethrow   whether the failure is thrown to the caller after it was given back, for a caller that is
 *                  waiting for the work (the default is that a run never throws)
 * @param always    when set, run once the work and what is done about its failure are over, whichever way it ended,
 *                  outside of any transaction
 */
public record AsyncJob(String name, Duration timeout, IdempotencyGuard idempotencyGuard, Runnable work,
                       Consumer<Throwable> onFailure, boolean transactional, boolean rethrow, Runnable always) {

    public static Builder named(String name) {
        return new Builder(name);
    }

    public static final class Builder {
        private final String name;
        private Duration timeout;
        private IdempotencyGuard idempotencyGuard;
        private Runnable work;
        private Consumer<Throwable> onFailure;
        private boolean transactional = true;
        private boolean rethrow;
        private Runnable always;

        private Builder(String name) {
            this.name = name;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /** Makes the job an idempotent consumer: see {@link IdempotencyGuard}. */
        public Builder idempotentOn(IdempotencyGuard idempotencyGuard) {
            this.idempotencyGuard = idempotencyGuard;
            return this;
        }

        public Builder work(Runnable work) {
            this.work = work;
            return this;
        }

        public Builder onFailure(Consumer<Throwable> onFailure) {
            this.onFailure = onFailure;
            return this;
        }

        public Builder withoutTransaction() {
            this.transactional = false;
            return this;
        }

        public Builder rethrow() {
            this.rethrow = true;
            return this;
        }

        public Builder always(Runnable always) {
            this.always = always;
            return this;
        }

        public AsyncJob build() {
            if (work == null) {
                throw new IllegalStateException("The work of " + name + " is not given.");
            }
            return new AsyncJob(name, timeout, idempotencyGuard, work, onFailure, transactional, rethrow, always);
        }
    }

    /** How a run ended. */
    public enum Outcome {
        /** The work was done and committed. */
        COMPLETED,
        /** Nothing was done: the event had been handled already (see {@link IdempotencyGuard}). */
        ALREADY_HANDLED,
        /** The work was rolled back, and {@code onFailure} was run. */
        FAILED
    }
}
