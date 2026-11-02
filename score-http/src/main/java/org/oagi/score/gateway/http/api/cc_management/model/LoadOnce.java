package org.oagi.score.gateway.http.api.cc_management.model;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Builds a value on the first call and hands it to every caller, however many threads ask at once.
 * <p>
 * The threads that wait do not hold a monitor: loading a release uses the common pool, and a pool worker that
 * waits for a monitor is not replaced, so when the callers are themselves common pool workers (a parallel stream)
 * the load would never get a worker to run on. {@link CompletableFuture#join()} does make up for a waiting worker.
 * A load that fails is not remembered: the next call tries again.
 */
final class LoadOnce<T> {

    private final Supplier<T> loader;
    private final AtomicReference<CompletableFuture<T>> future = new AtomicReference<>();

    LoadOnce(Supplier<T> loader) {
        this.loader = loader;
    }

    T get() {
        CompletableFuture<T> mine = new CompletableFuture<>();
        CompletableFuture<T> running = future.compareAndExchange(null, mine);
        if (running == null) {
            try {
                mine.complete(loader.get());
            } catch (RuntimeException | Error e) {
                future.compareAndSet(mine, null);
                mine.completeExceptionally(e);
                throw e;
            }
            return mine.join();
        }
        try {
            return running.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            if (e.getCause() instanceof Error cause) {
                throw cause;
            }
            throw e;
        }
    }

}
