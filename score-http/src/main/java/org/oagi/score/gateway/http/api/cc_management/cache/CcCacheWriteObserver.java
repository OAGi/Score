package org.oagi.score.gateway.http.api.cc_management.cache;

/** Told by {@link CcCacheWriteListener} that a statement has written to the data the cached records are made of. */
public interface CcCacheWriteObserver {

    /**
     * Inside a transaction the cache starts a new generation when it commits, and until then the writing thread bypasses
     * the cache; outside one the write is already visible and the new generation starts at once.
     */
    void written();

}
