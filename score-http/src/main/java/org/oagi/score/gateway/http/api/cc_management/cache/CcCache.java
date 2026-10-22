package org.oagi.score.gateway.http.api.cc_management.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * A cache of the immutable summary records of core components, shared by every request (and every instance, when the
 * provider is shared).
 * <p>
 * It is never the source of truth. A reader takes the {@link #epoch()} once, asks for what it needs with
 * {@link #get} / {@link #getAll}, reads the rest from the database and offers it with {@link #put} / {@link #putAll}
 * under the epoch it took. {@link CcChangeFeed} forgets the entries of the records that changed, with {@link #invalidate}.
 * <p>
 * <b>Why the epoch.</b> A reader may read a row, the row may then change and be forgotten, and the reader may only then
 * store what it read. {@link #invalidate} therefore starts a new epoch before it forgets, and a store under an older epoch
 * is dropped: it costs the reader its fill, and costs the entries of everyone else nothing.
 * <p>
 * The reads and writes come in bulk because a provider outside the JVM costs a round trip per call.
 */
public interface CcCache {

    /** A cache that never holds anything, so every read goes to the database. */
    CcCache DISABLED = new CcCache() {
        @Override
        public long epoch() {
            return 0;
        }

        @Override
        public Object get(String key) {
            return null;
        }

        @Override
        public Map<String, Object> getAll(Collection<String> keys) {
            return Collections.emptyMap();
        }

        @Override
        public void put(long epoch, String key, Object value) {
        }

        @Override
        public void putAll(long epoch, Map<String, Object> values) {
        }

        @Override
        public void invalidate(Collection<String> keys) {
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

    /**
     * The current epoch: it changes every time entries are forgotten. Take it before reading the database.
     * It is also where this cache sees a change another instance made (the feed reads the log if it has not just done so).
     */
    long epoch();

    /** The value cached under the key, or {@code null} when there is none. */
    Object get(String key);

    /** The values cached under the keys; a key without a value is not in the result. */
    Map<String, Object> getAll(Collection<String> keys);

    /** Caches the value, unless entries have been forgotten since the epoch. */
    void put(long epoch, String key, Object value);

    /** Caches the values, unless entries have been forgotten since the epoch. */
    void putAll(long epoch, Map<String, Object> values);

    /**
     * Forgets the entries. The epoch changes first, so that nothing read before is stored after. Unlike the reads and
     * the writes it does not hide a failure: an entry that is not forgotten is stale, so the caller must retry.
     */
    void invalidate(Collection<String> keys);

    /**
     * Whether the cache must not be used by the current thread: its transaction has written to that data without
     * committing yet (neither the cache nor the database it was filled from reflects such a write), the feed that forgets
     * what changed is not keeping up, or the provider is not reachable. The caller reads the database and caches nothing.
     */
    boolean isBypassed();

    /**
     * Runs the task unless another thread of this JVM is running one for the same key, in which case it waits for that one to
     * end instead; either way the caller carries on afterwards, reading what the task cached.
     * It keeps the requests that find the cache empty together from all doing the same work.
     */
    void singleFlight(String key, Runnable task);

}
