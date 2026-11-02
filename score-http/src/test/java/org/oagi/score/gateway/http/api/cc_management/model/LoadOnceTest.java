package org.oagi.score.gateway.http.api.cc_management.model;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class LoadOnceTest {

    @Test
    void theValueIsBuiltOnceForAnyNumberOfCallers() {
        AtomicInteger builds = new AtomicInteger();
        LoadOnce<String> once = new LoadOnce<>(() -> {
            builds.incrementAndGet();
            return "value";
        });

        IntStream.range(0, 500).parallel().forEach(i -> assertEquals("value", once.get()));

        assertEquals(1, builds.get());
    }

    @Test
    void aFailedBuildIsNotRememberedAndTheNextCallTriesAgain() {
        AtomicInteger builds = new AtomicInteger();
        LoadOnce<String> once = new LoadOnce<>(() -> {
            if (builds.incrementAndGet() == 1) {
                throw new IllegalStateException("first time");
            }
            return "value";
        });

        assertThrows(IllegalStateException.class, once::get);
        assertEquals("value", once.get());
    }

    /**
     * What the releases are loaded with: a build that hands its work to the common pool and waits for it, asked for by
     * threads that are themselves common pool workers. With a monitor around the build every worker waits for the
     * monitor and the work never runs.
     */
    @Test
    void aBuildThatNeedsTheCommonPoolDoesNotStarveWhenTheCallersAreWorkersOfIt() {
        LoadOnce<Integer> once = new LoadOnce<>(() -> CompletableFuture
                .supplyAsync(() -> 42, ForkJoinPool.commonPool()).join());

        assertTimeoutPreemptively(java.time.Duration.ofSeconds(20), () ->
                IntStream.range(0, 2000).parallel().forEach(i -> assertEquals(42, once.get())));
    }

}
