package org.oagi.score.gateway.http.api.release_management.guard;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.release_management.guard.GuardHarness.K;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Someone moves a {@code Candidate} component back to {@code WIP} while the release that holds it becomes a draft. For every
 * kind of component, at five moments:
 * <ul>
 *   <li>the move is done before the release is asked to become a draft: the release must fail, the component is {@code WIP};</li>
 *   <li>the move is tried after the release was asked, before its event is handled: the move must fail, and the release
 *   become a draft;</li>
 *   <li>the move is tried while the release is being asked, before it has committed, while it is just about to move the
 *   component, and while it has just moved it: whichever comes first wins, the other one fails, and the component, the
 *   release and the other components agree.</li>
 * </ul>
 * The moments are made by holding the thread of the release at the statement that moves the component to
 * {@code ReleaseDraft}, until the move has been tried.
 */
@EnabledIfSystemProperty(named = "score.releaseGuard", matches = "true")
class ReleaseInProgressRaceDbTest {

    enum Moment {
        MOVED_BEFORE_THE_RELEASE_IS_ASKED, MOVED_AFTER_THE_RELEASE_IS_ASKED,
        TRIED_JUST_BEFORE_THE_RELEASE_MOVES_THE_COMPONENT, TRIED_JUST_AFTER_THE_RELEASE_MOVED_THE_COMPONENT,
        TRIED_WHILE_THE_RELEASE_IS_BEING_ASKED
    }

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

    static Stream<Object[]> cases() {
        List<Object[]> cases = new ArrayList<>();
        for (Moment moment : Moment.values()) {
            for (K kind : K.values()) {
                cases.add(new Object[]{kind, moment});
            }
        }
        return cases.stream();
    }

    private static boolean moved(GuardHarness h, K kind, long manifest) {
        try {
            h.updateState(kind, manifest, CcState.WIP);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static <T> T await(Future<T> future, long seconds) throws Exception {
        return future.get(seconds, TimeUnit.SECONDS);
    }

    @ParameterizedTest(name = "{0}, {1}")
    @MethodSource("cases")
    void theMoveAndTheReleaseDoNotBothWin(K kind, Moment moment) throws Exception {
        Arena arena = harness.arrange();
        harness.drainEvents();
        long manifest = arena.components().get(kind);
        boolean userMoved;
        Throwable releaseFailure;
        try {
            switch (moment) {
                case MOVED_BEFORE_THE_RELEASE_IS_ASKED -> {
                    userMoved = moved(harness, kind, manifest);
                    releaseFailure = arena.draft();
                }
                case MOVED_AFTER_THE_RELEASE_IS_ASKED -> {
                    releaseFailure = arena.requestDraft();
                    userMoved = moved(harness, kind, manifest);
                    if (releaseFailure == null) {
                        try {
                            arena.handleEvents();
                        } catch (Throwable t) {
                            releaseFailure = t;
                        }
                    }
                }
                default -> {
                    boolean before = moment == Moment.TRIED_JUST_BEFORE_THE_RELEASE_MOVES_THE_COMPONENT;
                    boolean asked = moment == Moment.TRIED_WHILE_THE_RELEASE_IS_BEING_ASKED;
                    CountDownLatch atStatement = new CountDownLatch(1);
                    CountDownLatch resume = new CountDownLatch(1);
                    FaultInjector.Probe probe = new FaultInjector.Probe().hook(before,
                            s -> asked
                                    // the release has taken hold of the library, and has not committed
                                    ? s.updates("release") && (s.hasParam("Drafting") || s.hasParam("Processing"))
                                    : s.updates(kind.table) && s.hasParam("ReleaseDraft"), () -> {
                                atStatement.countDown();
                                try {
                                    resume.await(120, TimeUnit.SECONDS);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            });
                    Future<Throwable> release = pool.submit(() -> {
                        FaultInjector.attach(probe);
                        try {
                            return arena.draft();
                        } finally {
                            FaultInjector.detach();
                        }
                    });
                    boolean reached = atStatement.await(120, TimeUnit.SECONDS);
                    if (!reached) {
                        // the release did not come to the component: it failed before, which is for the test to see
                        resume.countDown();
                        throw new AssertionError("the release never came to move the " + kind + ": " + await(release, 60));
                    }
                    Future<Boolean> user = pool.submit(() -> moved(harness, kind, manifest));
                    Boolean done = null;
                    try {
                        done = user.get(1500, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException waiting) {
                        // the move waits for the release: it is let go on
                    }
                    resume.countDown();
                    releaseFailure = await(release, 120);
                    userMoved = done != null ? done : await(user, 120);
                }
            }

            GuardHarness.Observed after = arena.observe();
            String what = kind + " / " + moment + ": the move " + (userMoved ? "succeeded" : "failed") + ", the release "
                    + (releaseFailure == null ? "succeeded" : "failed (" + releaseFailure + ")") + ", after " + after;
            System.out.println("RACE " + what);

            boolean drafted = after.releaseState().equals("Draft")
                    && after.states().values().stream().allMatch(s -> s.equals("ReleaseDraft")) && after.rows() > 0;
            boolean notDrafted = after.releaseState().equals("Initialized") && after.rows() == 0
                    && after.states().entrySet().stream().allMatch(e -> e.getKey() == kind
                    ? e.getValue().equals(userMoved ? "WIP" : "Candidate") : e.getValue().equals("Candidate"));
            assertTrue(drafted ^ notDrafted, "neither the release nor the move won, or both: " + what);
            assertTrue(!drafted || !userMoved, "the move succeeded and the release took the component: " + what);
            assertTrue(!notDrafted || releaseFailure != null || !userMoved,
                    "the release succeeded but did not take the component: " + what);
            switch (moment) {
                case MOVED_BEFORE_THE_RELEASE_IS_ASKED -> {
                    assertTrue(userMoved, "the move is done first, so it succeeds: " + what);
                    assertNotNull(releaseFailure, "the release must fail when a component is no longer a candidate: " + what);
                    assertTrue(notDrafted, what);
                }
                case MOVED_AFTER_THE_RELEASE_IS_ASKED -> {
                    assertEquals(false, userMoved, "the release is asked, so the move must fail: " + what);
                    assertTrue(drafted, what);
                }
                default -> {
                    // whichever came first has won, and the other one has failed
                    assertTrue(userMoved ^ drafted, what);
                }
            }
        } finally {
            arena.remove();
        }
    }
}
