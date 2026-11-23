package org.oagi.score.gateway.http.api.release_management.guard;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The work of a release does not write the change log for what it copies, removes or moves, and the whole cache is forgotten when it
 * has committed: once for each piece of work, and never for one that failed.
 */
@EnabledIfSystemProperty(named = "score.releaseGuard", matches = "true")
class ReleaseWorkCacheDbTest {

    static GuardHarness harness;

    @BeforeAll
    static void open() {
        harness = GuardHarness.open();
    }

    @AfterAll
    static void close() {
        harness.close();
    }

    /** What the log says about the components: the rows the release itself and the users of it write are not counted. */
    private static long componentRows() {
        return harness.one("select count(*) from record_change_log where record_type not in ('RELEASE', 'RELEASE_DEP', 'LIBRARY', 'USER', 'TAG')");
    }

    @Test
    void everyPieceOfWorkSkipsTheChangeLogAndForgetsTheCacheOnce() {
        Arena arena = harness.arrange();
        try {
            // the draft: the components are moved and what the release holds is copied
            long before = componentRows();
            harness.drainEvents();
            Mockito.clearInvocations(harness.recordCache);
            assertNull(arena.draft());
            assertTrue(arena.observe().rows() > 0, "what the release holds was copied");
            assertEquals(before, componentRows(), "the draft writes nothing to the log for the components");
            Mockito.verify(harness.recordCache, Mockito.times(1)).invalidateAll();

            // the removal
            Mockito.clearInvocations(harness.recordCache);
            assertNull(arena.initialize());
            assertEquals("Initialized", arena.observe().releaseState());
            assertEquals(before, componentRows(), "taking it back writes nothing to the log for the components");
            Mockito.verify(harness.recordCache, Mockito.times(1)).invalidateAll();

            // the publication
            arena.toDraft();
            Mockito.clearInvocations(harness.recordCache);
            assertNull(arena.publish());
            assertEquals("Published", arena.observe().releaseState());
            assertEquals(before, componentRows(), "publishing writes nothing to the log for the components");
            Mockito.verify(harness.recordCache, Mockito.times(1)).invalidateAll();
            assertEquals(30, harness.sessionsDisablingTheChangeLog().stream().filter(java.util.Objects::isNull).count(),
                    "no session of the pool is left telling the triggers to write nothing");
        } finally {
            arena.remove();
        }
    }

    @Test
    void aPieceOfWorkThatFailsForgetsNothing() {
        Arena arena = harness.arrange();
        try {
            harness.drainEvents();
            Mockito.clearInvocations(harness.recordCache);
            FaultInjector.Probe probe = new FaultInjector.Probe().failAfterWrite(10);
            FaultInjector.attach(probe);
            try {
                arena.draft();
            } finally {
                FaultInjector.detach();
            }
            assertEquals("Initialized", arena.observe().releaseState());
            Mockito.verify(harness.recordCache, Mockito.never()).invalidateAll();
            assertEquals(30, harness.sessionsDisablingTheChangeLog().stream().filter(java.util.Objects::isNull).count(),
                    "the session of the work that failed is given back as it was");
        } finally {
            arena.remove();
        }
    }
}
