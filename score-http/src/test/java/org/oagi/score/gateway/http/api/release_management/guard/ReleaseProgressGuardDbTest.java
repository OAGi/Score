package org.oagi.score.gateway.http.api.release_management.guard;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.release_management.guard.GuardHarness.K;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.model.event.ReleaseCreateRequestEvent;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** What keeps a library as it is while one of its releases is being drafted, and what takes a release back that is stuck. */
@EnabledIfSystemProperty(named = "score.releaseGuard", matches = "true")
class ReleaseProgressGuardDbTest {

    static GuardHarness harness;
    static ExecutorService pool;

    @BeforeAll
    static void open() {
        harness = GuardHarness.open();
        pool = Executors.newFixedThreadPool(4);
    }

    @AfterAll
    static void close() {
        pool.shutdownNow();
        harness.close();
    }

    @Test
    void ofTwoRequestsAtTheSameTimeOnlyOneTakesTheRelease() throws Exception {
        Arena arena = harness.arrange();
        harness.drainEvents();
        try {
            CountDownLatch go = new CountDownLatch(1);
            List<Future<Throwable>> requests = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                requests.add(pool.submit(() -> {
                    go.await();
                    return arena.requestDraft();
                }));
            }
            go.countDown();
            int succeeded = 0;
            for (var request : requests) {
                if (request.get(120, TimeUnit.SECONDS) == null) {
                    succeeded++;
                }
            }
            assertEquals(1, succeeded, "the requests that succeeded");
            assertEquals(1, harness.publisher.events.size(), "the events that were published");
            assertEquals("Drafting", arena.observe().releaseState());
            arena.handleEvents();
            assertEquals("Draft", arena.observe().releaseState());
        } finally {
            arena.remove();
        }
    }

    @Test
    void theLibraryOfAComponentIsFoundFromItsManifest() {
        Arena arena = harness.arrange();
        try {
            long library = harness.one("select library_id from `release` where release_id = ?",
                    arena.release().value().longValue());
            var expected = java.util.Optional.of(new org.oagi.score.gateway.http.api.library_management.model.LibraryId(
                    java.math.BigInteger.valueOf(library)));
            assertEquals(expected, harness.progress.libraryOfRelease(arena.release()));
            for (K kind : K.values()) {
                org.oagi.score.gateway.http.api.cc_management.model.ManifestId manifest = switch (kind) {
                    case ACC -> new org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId(
                            java.math.BigInteger.valueOf(arena.components().get(kind)));
                    case ASCCP -> new org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId(
                            java.math.BigInteger.valueOf(arena.components().get(kind)));
                    case BCCP -> new org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId(
                            java.math.BigInteger.valueOf(arena.components().get(kind)));
                    case DT -> new org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId(
                            java.math.BigInteger.valueOf(arena.components().get(kind)));
                    case CODE_LIST -> new org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId(
                            java.math.BigInteger.valueOf(arena.components().get(kind)));
                    case AGENCY_ID_LIST -> new org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId(
                            java.math.BigInteger.valueOf(arena.components().get(kind)));
                };
                assertEquals(expected, harness.progress.libraryOfManifest(manifest), kind.name());
            }
            // the manifest of what is not known to be of a release tells nothing
            var unknown = new org.oagi.score.gateway.http.api.cc_management.model.ManifestId() {
                @Override
                public java.math.BigInteger value() {
                    return java.math.BigInteger.ONE;
                }
            };
            assertEquals(java.util.Optional.empty(), harness.progress.libraryOfManifest(unknown));
        } finally {
            arena.remove();
        }
    }

    @Test
    void aReleaseRequestWaitsForAChangeThatIsUnderWayAndThenSeesIt() throws Exception {
        Arena arena = harness.arrange();
        harness.drainEvents();
        try {
            CountDownLatch changed = new CountDownLatch(1);
            CountDownLatch commit = new CountDownLatch(1);
            Future<?> user = pool.submit(() -> new TransactionTemplate(harness.transactionManager).executeWithoutResult(status -> {
                harness.updateState(K.ACC, arena.components().get(K.ACC), CcState.WIP);
                changed.countDown();
                try {
                    commit.await(60, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
            assertTrue(changed.await(60, TimeUnit.SECONDS));
            Future<Throwable> request = pool.submit(arena::requestDraft);
            assertThrows(java.util.concurrent.TimeoutException.class, () -> request.get(1500, TimeUnit.MILLISECONDS),
                    "the request waits for the change");
            commit.countDown();
            user.get(60, TimeUnit.SECONDS);
            assertNotNull(request.get(60, TimeUnit.SECONDS), "the request sees the change, and fails at once");
            var observed = arena.observe();
            assertEquals("Initialized", observed.releaseState());
            assertEquals("WIP", observed.states().get(K.ACC));
            assertEquals(0, observed.rows());
        } finally {
            arena.remove();
        }
    }

    @Test
    void anEventThatIsHandledTwiceIsHandledOnce() {
        Arena arena = harness.arrange();
        harness.drainEvents();
        try {
            assertNull(arena.requestDraft());
            Object event = harness.drainEvents().get(0);
            harness.releaseService.onReleaseCreateRequestEventReceived((ReleaseCreateRequestEvent) event);
            var once = arena.observe();
            assertEquals("Draft", once.releaseState());
            harness.releaseService.onReleaseCreateRequestEventReceived((ReleaseCreateRequestEvent) event);
            assertEquals(once, arena.observe());
        } finally {
            arena.remove();
        }
    }

    @Test
    void aChangeIsRefusedWhileAReleaseOfTheLibraryIsBeingDrafted() {
        Arena arena = harness.arrange();
        try {
            ReleaseId release = arena.release();
            assertTrue(harness.progress.moveState(release, ReleaseState.Initialized, ReleaseState.Drafting));
            for (K kind : K.values()) {
                var failure = assertThrows(IllegalArgumentException.class,
                        () -> harness.updateState(kind, arena.components().get(kind), CcState.WIP), kind.name());
                assertTrue(failure.getMessage().contains("being drafted or published"), failure.getMessage());
            }
            // nothing changed
            arena.observe().states().values().forEach(state -> assertEquals("Candidate", state));
            // and it is allowed again when the release is not in the middle of anything
            assertTrue(harness.progress.moveState(release, ReleaseState.Drafting, ReleaseState.Initialized));
            harness.updateState(K.ACC, arena.components().get(K.ACC), CcState.WIP);
            assertEquals("WIP", arena.observe().states().get(K.ACC));
        } finally {
            arena.remove();
        }
    }

    @Test
    void aChangeIsRefusedWithTheSameMessageWhileTheWorkOfTheReleaseHoldsItsRows() throws Exception {
        // The release is in the middle of its work: its transaction holds the rows, so the question is not even answered.
        Arena arena = harness.arrange();
        try {
            ReleaseId release = arena.release();
            assertTrue(harness.progress.moveState(release, ReleaseState.Initialized, ReleaseState.Drafting));
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
            try {
                for (K kind : K.values()) {
                    var failure = assertThrows(IllegalArgumentException.class,
                            () -> harness.updateState(kind, arena.components().get(kind), CcState.WIP), kind.name());
                    assertTrue(failure.getMessage().contains("being drafted or published"), failure.getMessage());
                }
            } finally {
                done.countDown();
                holder.get(60, TimeUnit.SECONDS);
            }
            arena.observe().states().values().forEach(state -> assertEquals("Candidate", state));
        } finally {
            arena.remove();
        }
    }

    @Test
    void aReleaseThatWasLeftInTheMiddleOfItsTransitionIsTakenBack() {
        Arena arena = harness.arrange();
        try {
            ReleaseId release = arena.release();
            assertTrue(harness.progress.moveState(release, ReleaseState.Initialized, ReleaseState.Drafting));
            // recently: not yet
            assertEquals(0, harness.recovery.recoverSince(LocalDateTime.now().minusHours(1)));
            assertEquals("Drafting", arena.observe().releaseState());
            // for too long: taken back
            assertEquals(1, harness.recovery.recoverSince(LocalDateTime.now().plusHours(1)));
            assertEquals("Initialized", arena.observe().releaseState());
        } finally {
            arena.remove();
        }
    }

    @Test
    void aReleaseThatIsBeingDoneIsNotTakenBackFromUnderItsTransaction() throws Exception {
        Arena arena = harness.arrange();
        try {
            ReleaseId release = arena.release();
            assertTrue(harness.progress.moveState(release, ReleaseState.Initialized, ReleaseState.Drafting));
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
            assertEquals(0, harness.recovery.recoverSince(LocalDateTime.now().plusHours(1)), "while it is held");
            assertEquals("Drafting", arena.observe().releaseState());
            done.countDown();
            holder.get(60, TimeUnit.SECONDS);
            assertEquals(1, harness.recovery.recoverSince(LocalDateTime.now().plusHours(1)), "when it is not");
        } finally {
            arena.remove();
        }
    }

    @Test
    void aReleaseIsBusyWhileATransactionHoldsItAndNotWhenItWasLeftInTheMiddle() throws Exception {
        Arena arena = harness.arrange();
        try {
            ReleaseId release = arena.release();
            assertTrue(harness.progress.moveState(release, ReleaseState.Initialized, ReleaseState.Drafting));
            assertEquals(false, harness.progress.isBusy(release), "left in the middle: nothing holds it");

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
            assertEquals(true, harness.progress.isBusy(release), "the work holds it");
            done.countDown();
            holder.get(60, TimeUnit.SECONDS);
            assertEquals(false, harness.progress.isBusy(release), "the work is over");
        } finally {
            arena.remove();
        }
    }

    @Test
    void aReleaseLeftInProcessingByAnEarlierVersionDoesNotHoldItsLibrary() {
        Arena arena = harness.arrange();
        try {
            ReleaseId release = arena.release();
            assertTrue(harness.progress.moveState(release, ReleaseState.Initialized, ReleaseState.Processing));
            harness.updateState(K.ACC, arena.components().get(K.ACC), CcState.WIP);
            assertNotNull(arena.observe());
        } finally {
            arena.remove();
        }
    }
}
