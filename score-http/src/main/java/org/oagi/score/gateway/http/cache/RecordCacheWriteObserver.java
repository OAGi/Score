package org.oagi.score.gateway.http.cache;

/**
 * Told by {@link TableWriteListener} that a statement has written to a table the cached records are made of. It is a hint
 * for this instance only; what changed is in the change log, which every instance reads.
 */
public interface RecordCacheWriteObserver {

    /**
     * Inside a transaction the writing thread bypasses the cache until the transaction ends, and a commit reads the change log
     * at once so that the writer sees its own change; outside one the write is already visible and the log is read at once.
     */
    void written();

}
