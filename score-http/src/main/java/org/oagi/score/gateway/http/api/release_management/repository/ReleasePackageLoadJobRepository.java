package org.oagi.score.gateway.http.api.release_management.repository;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJob;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJobId;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The record of the loads of release packages, which is what follows a load from its start to its end, and after it. The
 * table of it stays when the schema is swapped, so it is read and written the same way whatever the swap has done. Each
 * change is its own, committed as soon as it is made, for whoever asks how the load goes. It is for no user to ask.
 */
public interface ReleasePackageLoadJobRepository {

    /**
     * Records a load that is to start, unless another one goes on.
     *
     * @return the load, or empty when another goes on
     */
    Optional<ReleasePackageLoadJobId> createUnlessActive(UserId owner, String packageFileName, String releaseNum, String nodeId);

    Optional<ReleasePackageLoadJob> find(ReleasePackageLoadJobId id);

    /** The loads that go on, which is at most one. */
    List<ReleasePackageLoadJob> findActive();

    /** The loads that were started last, the latest first. */
    List<ReleasePackageLoadJob> findRecent(int limit);

    /** The latest load that ended as {@code state}, for what undoing a swap needs. */
    Optional<ReleasePackageLoadJob> findLatest(ReleasePackageLoadState state);

    /** The loads that ended as {@code state} before the one given, the latest first; they are what that one has replaced. */
    List<ReleasePackageLoadJob> findBefore(ReleasePackageLoadJobId id, ReleasePackageLoadState state);

    /** The loads that are in one of the states and have not changed since before the time, the oldest first. */
    List<ReleasePackageLoadJob> findNotChangedSince(Set<ReleasePackageLoadState> states, LocalDateTime time);

    /**
     * Removes the record of the load, but only when it is in one of the states: the one that is being moved on is not removed.
     *
     * @return whether it was removed
     */
    boolean delete(ReleasePackageLoadJobId id, Set<ReleasePackageLoadState> states);

    /**
     * Moves the load to the state, only if it is in one of the states it is moved from.
     *
     * @param message what it says from now on
     * @return whether it was moved
     */
    boolean moveState(ReleasePackageLoadJobId id, Set<ReleasePackageLoadState> from, ReleasePackageLoadState to, String message);

    /** Changes what the load says, and when it last did, which a load that goes on does as it makes progress. */
    void progress(ReleasePackageLoadJobId id, String message);

    /** Records the schemas the load uses; a schema that is not given is as it was. */
    void schemas(ReleasePackageLoadJobId id, String stagingSchema, String backupSchema, String discardedSchema);

    /** The loads that go on and have not changed since before the time, locked until the end of the transaction. */
    List<ReleasePackageLoadJob> lockActiveSince(LocalDateTime time);
}
