package org.oagi.score.gateway.http.common.async;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.common.async.AsyncJob.Outcome;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AsyncJobRunnerTest {

    /** Records what the transactions do, and what they were asked to be. */
    static class RecordingTransactionManager implements PlatformTransactionManager {
        final List<String> log = new ArrayList<>();
        final List<Integer> timeouts = new ArrayList<>();

        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            timeouts.add(definition.getTimeout());
            log.add("begin");
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {
            log.add("commit");
        }

        @Override
        public void rollback(TransactionStatus status) {
            log.add("rollback");
        }
    }

    private final RecordingTransactionManager transactions = new RecordingTransactionManager();
    private final AsyncJobRunner runner = new AsyncJobRunner(transactions, new GiveBackRetryPolicy());

    @Test
    void commitsTheWorkAndTellsThatItWasDone() {
        List<String> steps = new ArrayList<>();

        Outcome outcome = runner.run(AsyncJob.named("a").work(() -> steps.add("work")).build());

        assertEquals(Outcome.COMPLETED, outcome);
        assertEquals(List.of("work"), steps);
        assertEquals(List.of("begin", "commit"), transactions.log);
    }

    @Test
    void doesNothingWhenTheGuardSaysTheEventWasHandledAlready() {
        List<String> steps = new ArrayList<>();

        Outcome outcome = runner.run(AsyncJob.named("a").idempotentOn(() -> false).work(() -> steps.add("work"))
                .onFailure(e -> steps.add("failure")).build());

        assertEquals(Outcome.ALREADY_HANDLED, outcome);
        assertTrue(steps.isEmpty());
        assertEquals(List.of("begin", "commit"), transactions.log);
    }

    @Test
    void asksTheGuardInTheTransactionOfTheWorkBeforeTheWork() {
        List<String> steps = new ArrayList<>();

        runner.run(AsyncJob.named("a").idempotentOn(() -> steps.add("guard")).work(() -> steps.add("work")).build());

        assertEquals(List.of("guard", "work"), steps);
    }

    @Test
    void rollsBackAndGivesBackInATransactionOfItsOwnWhenTheWorkFails() {
        AtomicReference<Throwable> told = new AtomicReference<>();
        IllegalStateException failure = new IllegalStateException("broken");
        List<String> seenByTheFailureHandler = new ArrayList<>();

        Outcome outcome = runner.run(AsyncJob.named("a")
                .work(() -> {
                    throw failure;
                })
                .onFailure(cause -> {
                    told.set(cause);
                    seenByTheFailureHandler.addAll(transactions.log);
                })
                .build());

        assertEquals(Outcome.FAILED, outcome);
        assertSame(failure, told.get());
        // the work was rolled back before it was given back, and the giving back has its own transaction
        assertEquals(List.of("begin", "rollback", "begin"), seenByTheFailureHandler);
        assertEquals(List.of("begin", "rollback", "begin", "commit"), transactions.log);
    }

    @Test
    void doesNotThrowWhenGivingBackFailsToo() {
        Outcome outcome = runner.run(AsyncJob.named("a")
                .work(() -> {
                    throw new IllegalStateException("work");
                })
                .onFailure(cause -> {
                    throw new IllegalStateException("give back");
                })
                .build());

        assertEquals(Outcome.FAILED, outcome);
        assertEquals(List.of("begin", "rollback", "begin", "rollback"), transactions.log);
    }

    @Test
    void failsWithoutAHandlerWhenThereIsNoneToRun() {
        assertEquals(Outcome.FAILED, runner.run(AsyncJob.named("a").work(() -> {
            throw new IllegalStateException("x");
        }).build()));
    }

    @Test
    void givesTheTransactionOfTheWorkItsTimeLimitAndNotTheOneThatGivesBack() {
        runner.run(AsyncJob.named("a").timeout(Duration.ofMinutes(30)).work(() -> {
            throw new IllegalStateException("x");
        }).onFailure(c -> {
        }).build());

        assertEquals(List.of(1800, TransactionDefinition.TIMEOUT_DEFAULT), transactions.timeouts);
    }

    @Test
    void runsWithoutATransactionWhenTheWorkNeedsNone() {
        List<String> steps = new ArrayList<>();

        Outcome ok = runner.run(AsyncJob.named("a").withoutTransaction().work(() -> steps.add("work")).build());
        Outcome failed = runner.run(AsyncJob.named("b").withoutTransaction().work(() -> {
            throw new IllegalStateException("x");
        }).onFailure(c -> steps.add("failure")).build());

        assertEquals(Outcome.COMPLETED, ok);
        assertEquals(Outcome.FAILED, failed);
        assertEquals(List.of("work", "failure"), steps);
        assertTrue(transactions.log.isEmpty());
    }

    @Test
    void finishesAfterTheFailureWasGivenBackWhicheverWayTheWorkEnded() {
        List<String> steps = new ArrayList<>();

        runner.run(AsyncJob.named("a").work(() -> steps.add("work")).always(() -> steps.add("always")).build());
        runner.run(AsyncJob.named("b").work(() -> {
            throw new IllegalStateException("x");
        }).onFailure(c -> steps.add("failure")).always(() -> steps.add("always")).build());
        runner.run(AsyncJob.named("c").idempotentOn(() -> false).work(() -> steps.add("no")).always(() -> steps.add("always")).build());

        assertEquals(List.of("work", "always", "failure", "always", "always"), steps);
    }

    @Test
    void doesNotFailBecauseTheFinishFails() {
        assertEquals(Outcome.COMPLETED, runner.run(AsyncJob.named("a").work(() -> {
        }).always(() -> {
            throw new IllegalStateException("x");
        }).build()));
    }

    @Test
    void throwsTheFailureToACallerThatWaitsForItAfterGivingBack() {
        List<String> steps = new ArrayList<>();
        IllegalStateException failure = new IllegalStateException("x");

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> runner.run(AsyncJob.named("a").rethrow()
                .work(() -> {
                    throw failure;
                }).onFailure(c -> steps.add("failure")).build()));

        assertSame(failure, thrown);
        assertEquals(List.of("failure"), steps);
    }

    @Test
    void needsWork() {
        assertThrows(IllegalStateException.class, () -> AsyncJob.named("a").build());
    }

    @Test
    void asksThePolicyAfterEveryFailureAndTriesAgainFromTheGuardWhenItSaysSo() {
        List<RetryPolicy.Failure> asked = new ArrayList<>();
        AsyncJobRunner retrying = new AsyncJobRunner(transactions, failure -> {
            asked.add(failure);
            return failure.attempt() < 3 ? RetryPolicy.Decision.retryAfter(Duration.ZERO) : RetryPolicy.Decision.GIVE_BACK;
        });
        List<String> steps = new ArrayList<>();

        Outcome outcome = retrying.run(AsyncJob.named("a")
                .idempotentOn(() -> steps.add("guard"))
                .work(() -> {
                    steps.add("work");
                    throw new IllegalStateException("x");
                })
                .onFailure(c -> steps.add("failure"))
                .build());

        assertEquals(Outcome.FAILED, outcome);
        // given back once, after the last try, and not before
        assertEquals(List.of("guard", "work", "guard", "work", "guard", "work", "failure"), steps);
        assertEquals(List.of(1, 2, 3), asked.stream().map(RetryPolicy.Failure::attempt).toList());
        assertEquals("a", asked.get(0).jobName());
    }

    @Test
    void completesWhenATryThatFollowsAFailureSucceeds() {
        AsyncJobRunner retrying = new AsyncJobRunner(transactions, f -> RetryPolicy.Decision.retryAfter(Duration.ZERO));
        int[] tries = {0};

        Outcome outcome = retrying.run(AsyncJob.named("a").work(() -> {
            if (++tries[0] < 2) {
                throw new IllegalStateException("x");
            }
        }).onFailure(c -> fail("it was not given back")).build());

        assertEquals(Outcome.COMPLETED, outcome);
        assertEquals(List.of("begin", "rollback", "begin", "commit"), transactions.log);
    }

    @Test
    void givesBackWhenTheWaitBeforeTryingAgainIsInterrupted() {
        AsyncJobRunner retrying = new AsyncJobRunner(transactions, f -> RetryPolicy.Decision.retryAfter(Duration.ofMinutes(1)));
        List<String> steps = new ArrayList<>();
        Thread.currentThread().interrupt();

        Outcome outcome = retrying.run(AsyncJob.named("a").work(() -> {
            throw new IllegalStateException("x");
        }).onFailure(c -> steps.add("failure")).build());

        assertTrue(Thread.interrupted(), "the interruption is kept for whoever called");
        assertEquals(Outcome.FAILED, outcome);
        assertEquals(List.of("failure"), steps);
    }

    @Test
    void givesBackAtOnceWithThePolicyInUse() {
        assertEquals(RetryPolicy.Decision.GIVE_BACK,
                new GiveBackRetryPolicy().decide(new RetryPolicy.Failure("a", 1, new IllegalStateException())));
    }
}
