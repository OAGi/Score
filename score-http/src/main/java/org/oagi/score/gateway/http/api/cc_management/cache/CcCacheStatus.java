package org.oagi.score.gateway.http.api.cc_management.cache;

import org.springframework.stereotype.Component;

/**
 * Whether the cache may be trusted: only while {@link CcChangeFeed} keeps up with the change log, because an entry is
 * forgotten only when the feed reads that its record changed. Without the feed (the table is not there yet, the database
 * does not answer) the cache is bypassed and the database answers.
 */
@Component
public class CcCacheStatus {

    /** A feed that has not read for this long is not keeping up. */
    private static final long MAX_SILENCE_NANOS = 15_000_000_000L;
    /** A reader that starts a document wants the feed to have read within this time. */
    private static final long FRESH_NANOS = 250_000_000L;
    /** How long such a reader waits for a read that is under way. */
    private static final long FRESHEN_WAIT_MILLIS = 50;

    /** For a cache that has no feed to depend on (tests). */
    public static CcCacheStatus alwaysReady() {
        CcCacheStatus status = new CcCacheStatus();
        status.alwaysReady = true;
        return status;
    }

    private volatile boolean alwaysReady;
    private volatile long lastReadNanos;
    private volatile boolean started;
    private volatile CcChangeFeed feed;

    void bind(CcChangeFeed feed) {
        this.feed = feed;
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
     * Has the feed read the changes that were committed before now, unless it has just done so. It is the cheap way to
     * see a change another instance made: one indexed query, at most four times a second.
     */
    void freshen() {
        CcChangeFeed feed = this.feed;
        if (feed != null && System.nanoTime() - lastReadNanos > FRESH_NANOS) {
            feed.catchUp(FRESHEN_WAIT_MILLIS);
        }
    }
}
