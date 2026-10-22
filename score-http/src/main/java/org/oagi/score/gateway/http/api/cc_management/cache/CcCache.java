package org.oagi.score.gateway.http.api.cc_management.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * A cache of the immutable summary records of core components, shared by every request (and every instance, when the
 * provider is shared).
 * <p>
 * It is never the source of truth. A reader pins the {@link #generation()} once, asks for what it needs with
 * {@link #get} / {@link #getAll}, reads the rest from the database and offers it with {@link #put} / {@link #putAll}
 * under the generation it pinned. A write to the data the records are made of starts a new generation, so what was
 * read around a write can never be seen by a reader that started after it.
 * <p>
 * The reads and writes come in bulk because a provider outside the JVM costs a round trip per call.
 */
public interface CcCache {

    /** A cache that never holds anything, so every read goes to the database. */
    CcCache DISABLED = new CcCache() {
        @Override
        public long generation() {
            return 0;
        }

        @Override
        public Object get(long generation, String key) {
            return null;
        }

        @Override
        public Map<String, Object> getAll(long generation, Collection<String> keys) {
            return Collections.emptyMap();
        }

        @Override
        public void put(long generation, String key, Object value) {
        }

        @Override
        public void putAll(long generation, Map<String, Object> values) {
        }

        @Override
        public boolean isBypassed() {
            return true;
        }

        @Override
        public void singleFlight(String key, Runnable task) {
            task.run();
        }
    };

    /** The current generation: a new one starts with every committed write to the data the cached records are made of. */
    long generation();

    /** The value cached for the key in the generation, or {@code null} when there is none. */
    Object get(long generation, String key);

    /** The values cached for the keys in the generation; a key without a value is not in the result. */
    Map<String, Object> getAll(long generation, Collection<String> keys);

    /** Caches the value in the generation. */
    void put(long generation, String key, Object value);

    /** Caches the values in the generation. */
    void putAll(long generation, Map<String, Object> values);

    /**
     * Whether the cache must not be used by the current thread: its transaction has written to that data without
     * committing yet (neither the cache nor the database it was filled from reflects such a write), or the provider is
     * not reachable. The caller reads the database and caches nothing.
     */
    boolean isBypassed();

    /**
     * Runs the task unless another thread of this JVM is running one for the same key, in which case it waits for that one to
     * end instead; either way the caller carries on afterwards, reading what the task cached.
     * It keeps the requests that find the cache empty together from all doing the same work.
     */
    void singleFlight(String key, Runnable task);

}
