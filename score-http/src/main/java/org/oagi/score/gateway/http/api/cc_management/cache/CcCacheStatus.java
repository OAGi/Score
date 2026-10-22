package org.oagi.score.gateway.http.api.cc_management.cache;

import org.springframework.stereotype.Component;

/**
 * Whether the cache may be trusted: only while {@link CcCacheWatchdog} keeps up with the change log, because an entry is
 * forgotten only when the watchdog reads that its record changed. Without the watchdog (the table is not there yet, the database
 * does not answer) the cache is bypassed and the database answers.
 */
@Component
public class CcCacheStatus {

    /** A watchdog that has not read for this long is not keeping up. */
    private static final long MAX_SILENCE_NANOS = 15_000_000_000L;
    /** A reader that starts a document wants the watchdog to have read within this time. */
    private static final long FRESH_NANOS = 250_000_000L;
    /** How long such a reader waits for a read that is under way. */
    private static final long FRESHEN_WAIT_MILLIS = 50;

    /** For a cache that has no watchdog to depend on (tests). */
    public static CcCacheStatus alwaysReady() {
        CcCacheStatus status = new CcCacheStatus();
        status.alwaysReady = true;
        return status;
    }

    private volatile boolean alwaysReady;
    private volatile long lastReadNanos;
    private volatile boolean started;
    private volatile CcCacheWatchdog watchdog;

    void bind(CcCacheWatchdog watchdog) {
        this.watchdog = watchdog;
    }

    void succeeded() {
        lastReadNanos = System.nanoTime();
        started = true;
    }

    void failed() {
        started = false;
    }

    public boolean isReady() {
        return alwaysReady || (started && System.nanoTime() - lastReadNanos < MAX_SILENCE_NANOS);
    }

    /**
     * Has the watchdog read the changes that were committed before now, unless it has just done so. It is the cheap way to
     * see a change another instance made: one indexed query, at most four times a second.
     */
    void freshen() {
        CcCacheWatchdog watchdog = this.watchdog;
        if (watchdog != null && System.nanoTime() - lastReadNanos > FRESH_NANOS) {
            watchdog.catchUp(FRESHEN_WAIT_MILLIS);
        }
    }
}
