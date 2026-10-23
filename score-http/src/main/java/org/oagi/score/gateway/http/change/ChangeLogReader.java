package org.oagi.score.gateway.http.change;

/** Reads the change log when asked, besides when it does by itself. */
public interface ChangeLogReader {

    /**
     * Has the log read by the reader's own thread, which has a connection of its own, so that the caller (who may be inside a
     * transaction and holds a connection already) needs no second one.
     *
     * @param maxWaitMillis how long to wait for it. A caller who does not wait long enough may go on without having heard of the very
     *                      latest change.
     */
    void catchUp(long maxWaitMillis);

    /** Waits for the read, up to a generous limit. */
    default void catchUp() {
        catchUp(60_000);
    }
}
