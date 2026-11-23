package org.oagi.score.gateway.http.api.release_management.guard;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The work of a release has a limit of time (score.release.in-progress-timeout): the work that takes longer is rolled back, and the
 * release is given back as when anything else fails, so that nothing holds the release for ever. The limit here is one second, and the
 * work stops for longer than that.
 */
@EnabledIfSystemProperty(named = "score.releaseGuard", matches = "true")
class ReleaseWorkTimeoutDbTest {

    static GuardHarness harness;

    @BeforeAll
    static void open() {
        System.setProperty("score.release.in-progress-timeout", "PT1S");
        harness = GuardHarness.open();
    }

    @AfterAll
    static void close() {
        harness.close();
        System.clearProperty("score.release.in-progress-timeout");
    }

    @Test
    void aDraftThatTakesLongerThanTheLimitIsRolledBackAndTheReleaseIsGivenBack() {
        Arena arena = harness.arrange();
        try {
            GuardHarness.Observed before = arena.observe();
            harness.drainEvents();
            // the work is slow: it stops for longer than the limit before it moves the first component
            FaultInjector.Probe slow = new FaultInjector.Probe().hook(true,
                    s -> s.updates("acc") && s.hasParam("ReleaseDraft"), () -> {
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
            FaultInjector.attach(slow);
            try {
                arena.draft();
            } finally {
                FaultInjector.detach();
            }
            GuardHarness.Observed after = arena.observe();
            assertEquals("Initialized", after.releaseState(), "the release is given back");
            assertEquals(before, after, "nothing of the work is left");
            assertTrue(harness.progress.isBusy(arena.release()) == false, "nothing holds it");
        } finally {
            arena.remove();
        }
    }
}
