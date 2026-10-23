package org.oagi.score.gateway.http.change.repository;

import org.oagi.score.gateway.http.change.ChangeLogEntry;

import java.time.Duration;
import java.util.List;

/**
 * Access to {@code record_change_log}, which the triggers on the tables write. It reads and purges rows and decides nothing about them.
 */
public interface RecordChangeLogRepository {

    /** The greatest sequence number that is visible, or 0 when there is none. */
    long maxSeq();

    /** The visible rows after the sequence number, in order, at most {@code limit}. */
    List<ChangeLogEntry> readAfter(long seq, int limit);

    /** The visible rows with a sequence number from {@code from} to {@code to}. */
    List<ChangeLogEntry> readRange(long from, long to);

    /** Deletes rows older than the age, at most {@code limit}; @return how many were deleted. */
    int purgeOlderThan(Duration age, int limit);

    /**
     * Makes the numbers start again: the next row gets 1 if the table is empty, or one more than the greatest number in it
     * otherwise (deleting rows does not do it by itself). It does not wait: with a transaction open that has written to the table
     * (a release being published) it gives up, and the caller tries again later.
     *
     * @return whether it was done
     */
    boolean restartNumbering();
}
