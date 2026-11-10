package org.oagi.score.gateway.http.common.async;

import org.oagi.score.gateway.http.common.async.AsyncJob.Outcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Runs background work that a request leaves to an event: in a transaction of its own, so that it is all or nothing,
 * whichever way the event reached it (a message listener calls the bean itself, not its proxy, so an annotation on the
 * handler would give no transaction). It does not throw unless the job says it is waited for: a run ends as an
 * {@link Outcome}.
 *
 * <p>The order of a run: begin the transaction (with its time limit); ask the idempotency guard; do the work; commit. When any of that
 * fails, the transaction is rolled back and the {@link RetryPolicy} is asked: to try again from the beginning, or to give
 * the work back, which is to run {@code onFailure} in a transaction of its own. When that fails too it is logged, and what
 * is left is for whatever takes back work that no one is doing (see {@link StaleJobSweeper}). That an event is handled once, though it is delivered more than once, is for the
 * {@link IdempotencyGuard}.</p>
 */
@Component
public class AsyncJobRunner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PlatformTransactionManager transactionManager;
    private final RetryPolicy retryPolicy;

    public AsyncJobRunner(PlatformTransactionManager transactionManager, RetryPolicy retryPolicy) {
        this.transactionManager = transactionManager;
        this.retryPolicy = retryPolicy;
    }

    public Outcome run(AsyncJob job) {
        try {
            return attempts(job);
        } finally {
            finish(job);
        }
    }

    private Outcome attempts(AsyncJob job) {
        for (int attempt = 1; ; attempt++) {
            try {
                return job.transactional() ? inTransaction(job) : handle(job);
            } catch (RuntimeException e) {
                RetryPolicy.Decision decision = retryPolicy.decide(new RetryPolicy.Failure(job.name(), attempt, e));
                if (decision.retry() && waited(job, attempt, decision, e)) {
                    continue;
                }
                logger.error(job.name() + " failed: " + e.getMessage(), e);
                giveBack(job, e);
                if (job.rethrow()) {
                    throw e;
                }
                return Outcome.FAILED;
            }
        }
    }

    /** Waits as long as the policy says; false when the wait was interrupted, which is the end of trying. */
    private boolean waited(AsyncJob job, int attempt, RetryPolicy.Decision decision, RuntimeException cause) {
        logger.warn(job.name() + " failed (attempt " + attempt + "), and is tried again in " + decision.delay() + ": "
                + cause.getMessage());
        try {
            Thread.sleep(decision.delay().toMillis());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private Outcome inTransaction(AsyncJob job) {
        Outcome outcome = newTransaction(job).execute(status -> handle(job));
        return outcome == null ? Outcome.ALREADY_HANDLED : outcome;
    }

    private Outcome handle(AsyncJob job) {
        if (job.idempotencyGuard() != null && !job.idempotencyGuard().isFirstHandling()) {
            return Outcome.ALREADY_HANDLED;
        }
        job.work().run();
        return Outcome.COMPLETED;
    }

    private void finish(AsyncJob job) {
        if (job.always() == null) {
            return;
        }
        try {
            job.always().run();
        } catch (RuntimeException e) {
            logger.error(job.name() + " could not be finished: " + e.getMessage(), e);
        }
    }

    private TransactionTemplate newTransaction(AsyncJob job) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        if (job.timeout() != null) {
            template.setTimeout((int) job.timeout().toSeconds());
        }
        return template;
    }

    private void giveBack(AsyncJob job, RuntimeException cause) {
        if (job.onFailure() == null) {
            return;
        }
        try {
            if (job.transactional()) {
                new TransactionTemplate(transactionManager).executeWithoutResult(status -> job.onFailure().accept(cause));
            } else {
                job.onFailure().accept(cause);
            }
        } catch (RuntimeException e) {
            logger.error(job.name() + " could not be given back after its failure: " + e.getMessage(), e);
        }
    }
}
