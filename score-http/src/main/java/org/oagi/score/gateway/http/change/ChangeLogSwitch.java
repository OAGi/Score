package org.oagi.score.gateway.http.change;

/**
 * Stops and starts the writing of the change log for the connection of the active transaction, which a bulk change of the cache
 * needs: what it writes is forgotten as a whole afterwards, so it is not logged one by one.
 */
public interface ChangeLogSwitch {

    /** The triggers write nothing to the change log for this transaction until {@link #resumeWriting}. */
    void stopWriting();

    /** The triggers write the change log again. */
    void resumeWriting();
}
