package org.oagi.score.gateway.http.api.release_management.guard;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A release that is left being drafted or published (the server stopped, or the work for it did not end) is given back
 * to the state it was in before by a developer who asks for it, unless the work for it is going on.
 */
@EnabledIfSystemProperty(named = "score.releaseGuard", matches = "true")
class ReleaseRecoverDbTest {

    static GuardHarness harness;
    static final ExecutorService pool = Executors.newCachedThreadPool();

    @BeforeAll
    static void open() {
        harness = GuardHarness.open();
    }

    @AfterAll
    static void close() {
        pool.shutdownNow();
        harness.close();
    }

    @Test
    void aReleaseLeftBeingDraftedIsInitializedAgain() {
        Arena arena = harness.arrange();
        try {
            ReleaseId release = arena.release();
            GuardHarness.Observed before = arena.observe();
            assertTrue(harness.progress.takeHold(release, ReleaseState.Initialized, ReleaseState.Drafting, GuardHarness.REQUESTER.userId()));

            harness.releaseService.recover(GuardHarness.REQUESTER, release);

            // nothing is left of the transition: the release, its components and what the release holds are as they were
            assertEquals(before, arena.observe());
            // the event that was lost turns up late: the release is not drafted from under the one who recovered it
            arena.handleEvents();
            assertEquals("Initialized", arena.observe().releaseState());
        } finally {
            arena.remove();
        }
    }

    @Test
    void aReleaseLeftBeingPublishedIsADraftAgain() {
        Arena arena = harness.arrange().toDraft();
        try {
            ReleaseId release = arena.release();
            GuardHarness.Observed before = arena.observe();
            assertTrue(harness.progress.takeHold(release, ReleaseState.Draft, ReleaseState.Publishing, GuardHarness.REQUESTER.userId()));

            harness.releaseService.recover(GuardHarness.REQUESTER, release);

            assertEquals(before, arena.observe());
        } finally {
            arena.remove();
        }
    }

    @Test
    void aReleaseLeftBeingTakenBackToInitializedIsADraftAgain() {
        Arena arena = harness.arrange().toDraft();
        try {
            ReleaseId release = arena.release();
            GuardHarness.Observed before = arena.observe();
            assertTrue(harness.progress.takeHold(release, ReleaseState.Draft, ReleaseState.Initializing, GuardHarness.REQUESTER.userId()));

            harness.releaseService.recover(GuardHarness.REQUESTER, release);

            assertEquals(before, arena.observe());
        } finally {
            arena.remove();
        }
    }

    @Test
    void aReleaseThatIsNotInTheMiddleOfATransitionIsRefused() {
        Arena arena = harness.arrange();
        try {
            ReleaseId release = arena.release();
            var e = assertThrows(IllegalArgumentException.class, () -> harness.releaseService.recover(GuardHarness.REQUESTER, release));
            assertTrue(e.getMessage().contains("not in the middle"), e.getMessage());
            assertEquals("Initialized", arena.observe().releaseState());
        } finally {
            arena.remove();
        }
    }

    @Test
    void aUserWhoIsNotADeveloperIsRefused() {
        Arena arena = harness.arrange();
        try {
            ReleaseId release = arena.release();
            assertTrue(harness.progress.takeHold(release, ReleaseState.Initialized, ReleaseState.Drafting, GuardHarness.REQUESTER.userId()));
            ScoreUser viewer = new ScoreUser(new UserId(BigInteger.ZERO), "end", "end", "end@example.com", true,
                    List.of(ScoreRole.END_USER));

            assertThrows(IllegalArgumentException.class, () -> harness.releaseService.recover(viewer, release));
            assertEquals("Drafting", arena.observe().releaseState());
        } finally {
            arena.remove();
        }
    }

    @Test
    void aReleaseThatIsBeingWorkedOnIsRefusedAndRecoveredWhenTheWorkIsOver() throws Exception {
        Arena arena = harness.arrange();
        try {
            ReleaseId release = arena.release();
            assertTrue(harness.progress.takeHold(release, ReleaseState.Initialized, ReleaseState.Drafting, GuardHarness.REQUESTER.userId()));
            CountDownLatch holding = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(1);
            Future<?> holder = pool.submit(() -> new TransactionTemplate(harness.transactionManager).executeWithoutResult(status -> {
                harness.progress.lockState(release);
                holding.countDown();
                try {
                    done.await(60, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
            assertTrue(holding.await(60, TimeUnit.SECONDS));

            var e = assertThrows(IllegalArgumentException.class, () -> harness.releaseService.recover(GuardHarness.REQUESTER, release));
            assertTrue(e.getMessage().contains("being worked on"), e.getMessage());
            assertEquals("Drafting", arena.observe().releaseState());

            done.countDown();
            holder.get(60, TimeUnit.SECONDS);
            harness.releaseService.recover(GuardHarness.REQUESTER, release);
            assertEquals("Initialized", arena.observe().releaseState());
        } finally {
            arena.remove();
        }
    }
}
