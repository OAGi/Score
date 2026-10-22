package org.oagi.score.gateway.http.api.cc_management.repository;

import org.oagi.score.gateway.http.api.cc_management.cache.CcChange;
import org.oagi.score.gateway.http.api.cc_management.cache.CcChangeKind;

import java.time.Duration;
import java.util.List;

/**
 * The change log that the triggers on the core component tables write (see the migration {@code cc_change_log}), and the
 * lookups with which a change that reaches many records is spelled out. Not tied to a user: the cache is not either.
 */
public interface CcChangeLogRepository {

    /** A record the lookups below name: its kind, manifest id and the ACC or DT manifest it belongs to. */
    record RecordRef(CcChangeKind kind, long id, Long parentId) {
    }

    /** The greatest sequence number that is visible, or 0 when there is none. */
    long maxSeq();

    /** The visible rows after the sequence number, in order, at most {@code limit}. */
    List<CcChange> readAfter(long seq, int limit);

    /** The visible rows with a sequence number from {@code from} to {@code to}. */
    List<CcChange> readRange(long from, long to);

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

    List<RecordRef> recordsOfRelease(long releaseId);

    List<RecordRef> recordsOfLibrary(long libraryId);

    /** The records the user owns, created or updated last. */
    List<RecordRef> recordsOfUser(long userId);

    List<RecordRef> recordsTaggedWith(long tagId);

    List<Long> allReleaseIds();
}
