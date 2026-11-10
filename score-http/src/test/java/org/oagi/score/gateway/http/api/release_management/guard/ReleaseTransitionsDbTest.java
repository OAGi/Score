package org.oagi.score.gateway.http.api.release_management.guard;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The transitions of a release without anything going wrong: what the tests that make something go wrong start from. */
@EnabledIfSystemProperty(named = "score.releaseGuard", matches = "true")
class ReleaseTransitionsDbTest {

    static GuardHarness harness;

    @BeforeAll
    static void open() {
        harness = GuardHarness.open();
    }

    @AfterAll
    static void close() {
        harness.close();
    }

    @Test
    void aReleaseIsADraftThenInitializedAgain() {
        Arena arena = harness.arrange();
        try {
            assertEquals("Initialized", arena.observe().releaseState());
            assertEquals(0, arena.observe().rows());
            assertNull(arena.draft());
            var draft = arena.observe();
            System.out.println("draft: " + draft);
            System.out.println("draft tables: " + harness.rowsByTable(arena.release().value().longValue()));
            assertEquals("Draft", draft.releaseState());
            draft.states().values().forEach(s -> assertEquals("ReleaseDraft", s));
            assertTrue(draft.rows() > 0);
            assertNull(arena.initialize());
            var back = arena.observe();
            System.out.println("back: " + back);
            System.out.println("back tables: " + harness.rowsByTable(arena.release().value().longValue()));
            assertEquals("Initialized", back.releaseState());
            back.states().values().forEach(s -> assertEquals("Candidate", s));
            assertEquals(0, back.rows());
        } finally {
            arena.remove();
        }
    }

    @Test
    void aDraftIsPublished() {
        Arena arena = harness.arrange().toDraft();
        try {
            assertNull(arena.publish());
            var published = arena.observe();
            System.out.println("published: " + published);
            assertEquals("Published", published.releaseState());
            published.states().values().forEach(s -> assertEquals("Published", s));
        } finally {
            arena.remove();
        }
    }
}
