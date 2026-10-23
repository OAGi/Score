package org.oagi.score.gateway.http.cache;

import org.oagi.score.gateway.http.change.ChangeLogReadEvent;
import org.oagi.score.gateway.http.change.ChangeLogReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Whether the cache may be trusted: only while the {@link ChangeLogReader} keeps up with the change log, because an entry is
 * forgotten only when the reader tells that its record changed. Without the reader (the table is not there yet, the database
 * does not answer) the cache is bypassed and the database answers.
 */
@Component
public class RecordCacheStatus {

    /** A change log reader that has not read for this long is not keeping up. */
    private static final long MAX_SILENCE_NANOS = 15_000_000_000L;
    /** A reader of records wants the change log to have been read within this time. */
    private static final long FRESH_NANOS = 250_000_000L;
    /** How long a reader of records waits for a log read that is under way. */
    private static final long FRESHEN_WAIT_MILLIS = 50;

    /** For a cache that has no change log reader to depend on (tests). */
    public static RecordCacheStatus alwaysReady() {
        RecordCacheStatus status = new RecordCacheStatus();
        status.alwaysReady = true;
        return status;
    }

    private volatile boolean alwaysReady;
    private volatile long lastReadNanos;
    private volatile boolean started;
    private volatile ChangeLogReader reader;

    /** The reader of the change log that this status asks for a read when one is needed ({@code freshen}); wired by Spring. */
    @Autowired(required = false)
    public void bind(@Lazy ChangeLogReader reader) {
        this.reader = reader;
    }

    /** The reader tells after every read whether it could read the change log: that is what the cache depends on. */
    @EventListener
    public void on(ChangeLogReadEvent event) {
        if (event.succeeded()) {
            succeeded();
        } else {
            failed();
        }
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
     * Has the change log read up to now, unless it was read just before. This is how a change another instance made is seen.
     */
    void freshen() {
        ChangeLogReader reader = this.reader;
        if (reader != null && System.nanoTime() - lastReadNanos > FRESH_NANOS) {
            reader.catchUp(FRESHEN_WAIT_MILLIS);
        }
    }
}
