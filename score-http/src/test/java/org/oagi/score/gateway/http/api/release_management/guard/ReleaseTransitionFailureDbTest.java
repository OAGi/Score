package org.oagi.score.gateway.http.api.release_management.guard;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A release goes from 'Initialized' to a draft, from a draft back to 'Initialized', and from a draft to published. When
 * something goes wrong in the middle of any of them, the release and every one of its components are as they were: nothing
 * is half done. The failure is injected after each write of the transition (all of them with
 * {@code -Dguard.allSteps=true}; the first and the last ones, and others in between, without it), so that it comes at every
 * point of the work, in the request as well as in the handler of its event.
 */
@EnabledIfSystemProperty(named = "score.releaseGuard", matches = "true")
class ReleaseTransitionFailureDbTest {

    static GuardHarness harness;

    @BeforeAll
    static void open() {
        harness = GuardHarness.open();
    }

    @AfterAll
    static void close() {
        harness.close();
    }

    /** The writes to fail after: the first ones, the last ones, and evenly spread in between. */
    static List<Integer> points(int writes) {
        if (Boolean.getBoolean("guard.allSteps") || writes <= 30) {
            List<Integer> all = new ArrayList<>();
            for (int i = 1; i <= writes; i++) {
                all.add(i);
            }
            return all;
        }
        TreeSet<Integer> points = new TreeSet<>();
        for (int i = 1; i <= 4; i++) {
            points.add(i);
            points.add(writes - i + 1);
        }
        for (int i = 1; i <= 22; i++) {
            points.add(Math.max(1, Math.min(writes, i * writes / 23)));
        }
        return new ArrayList<>(points);
    }

    /** Prepares the release for the transition, and does the transition. */
    private record Transition(String name, Function<Arena, Arena> prepare, Function<Arena, Throwable> act) {
    }

    private static final Transition TO_DRAFT = new Transition("Initialized -> Draft", a -> a, Arena::draft);
    private static final Transition TO_INITIALIZED = new Transition("Draft -> Initialized", Arena::toDraft, Arena::initialize);
    private static final Transition TO_PUBLISHED = new Transition("Draft -> Published", Arena::toDraft, Arena::publish);

    private void everyFailureLeavesNothingHalfDone(Transition transition) {
        // how many writes the transition makes
        Arena dry = transition.prepare().apply(harness.arrange());
        FaultInjector.Probe counter = new FaultInjector.Probe();
        harness.drainEvents();
        FaultInjector.attach(counter);
        Throwable dryFailure;
        try {
            dryFailure = transition.act().apply(dry);
        } finally {
            FaultInjector.detach();
        }
        dry.remove();
        assertEquals(null, dryFailure, "the transition without a failure");
        int writes = counter.writes();
        assertTrue(writes > 5, "the transition makes writes: " + writes);

        List<String> mismatches = new ArrayList<>();
        List<Integer> points = points(writes);
        for (int point : points) {
            Arena arena = transition.prepare().apply(harness.arrange());
            try {
                GuardHarness.Observed before = arena.observe();
                harness.drainEvents();
                FaultInjector.Probe probe = new FaultInjector.Probe().failAfterWrite(point);
                FaultInjector.attach(probe);
                try {
                    transition.act().apply(arena);
                } finally {
                    FaultInjector.detach();
                }
                harness.drainEvents();
                GuardHarness.Observed after = arena.observe();
                if (probe.writes() < point) {
                    mismatches.add("write " + point + ": the failure was not injected, the transition made only "
                            + probe.writes() + " writes");
                } else if (!before.equals(after)) {
                    List<String> trace = probe.trace();
                    mismatches.add("write " + point + " (" + trace.get(Math.min(point, trace.size()) - 1) + "): before "
                            + before + " but after " + after);
                }
            } finally {
                arena.remove();
            }
        }
        System.out.println(transition.name() + ": " + writes + " writes, failures injected after " + points.size()
                + " of them, " + mismatches.size() + " left something half done");
        mismatches.stream().limit(6).forEach(m -> System.out.println("  " + m));
        assertEquals(List.of(), mismatches.stream().limit(6).toList(),
                transition.name() + ": " + mismatches.size() + " of " + points.size() + " failures left something half done");
    }

    @Test
    void aFailureWhileAReleaseBecomesADraftLeavesNothingHalfDone() {
        everyFailureLeavesNothingHalfDone(TO_DRAFT);
    }

    @Test
    void aFailureWhileADraftBecomesInitializedAgainLeavesNothingHalfDone() {
        everyFailureLeavesNothingHalfDone(TO_INITIALIZED);
    }

    @Test
    void aFailureWhileADraftIsPublishedLeavesNothingHalfDone() {
        everyFailureLeavesNothingHalfDone(TO_PUBLISHED);
    }
}
