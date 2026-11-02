package org.oagi.score.gateway.http.cache;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Runs a task for a key in one thread at a time: the threads that ask while it runs wait for it (with
 * {@link CompletableFuture#join()}, which the common pool makes up for, instead of a monitor) and then carry on.
 */
final class SingleFlight {

    private final ConcurrentMap<String, CompletableFuture<Void>> inFlight = new ConcurrentHashMap<>();

    void run(String key, Runnable task) {
        CompletableFuture<Void> mine = new CompletableFuture<>();
        CompletableFuture<Void> running = inFlight.putIfAbsent(key, mine);
        if (running != null) {
            try {
                running.join();
            } catch (CompletionException | CancellationException e) {
                // The other task failed: its caller sees the error; this one reads what it needs by itself.
            }
            return;
        }
        try {
            task.run();
            mine.complete(null);
        } catch (RuntimeException | Error e) {
            mine.completeExceptionally(e);
            throw e;
        } finally {
            inFlight.remove(key, mine);
        }
    }

}
