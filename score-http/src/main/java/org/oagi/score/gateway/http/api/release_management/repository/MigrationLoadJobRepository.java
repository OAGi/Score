package org.oagi.score.gateway.http.api.release_management.repository;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.release_management.model.MigrationLoadJob;
import org.oagi.score.gateway.http.api.release_management.model.MigrationLoadJobId;
import org.oagi.score.gateway.http.api.release_management.model.MigrationLoadState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The record of the loads of release packages, which is what follows a load from its start to its end, and after it. The
 * table of it stays when the schema is swapped, so it is read and written the same way whatever the swap has done. Each
 * change is its own, committed as soon as it is made, for whoever asks how the load goes. It is for no user to ask.
 */
public interface MigrationLoadJobRepository {

    /**
     * Records a load that is to start, unless another one goes on.
     *
     * @return the load, or empty when another goes on
     */
    Optional<MigrationLoadJobId> createUnlessActive(UserId owner, String packageFileName, String releaseNum, String nodeId);

    Optional<MigrationLoadJob> find(MigrationLoadJobId id);

    /** The loads that go on, which is at most one. */
    List<MigrationLoadJob> findActive();

    /** The latest load that ended as {@code state}, for what undoing a swap needs. */
    Optional<MigrationLoadJob> findLatest(MigrationLoadState state);

    /**
     * Moves the load to the state, only if it is in one of the states it is moved from.
     *
     * @param message what it says from now on
     * @return whether it was moved
     */
    boolean moveState(MigrationLoadJobId id, Set<MigrationLoadState> from, MigrationLoadState to, String message);

    /** Changes what the load says, and when it last did, which a load that goes on does as it makes progress. */
    void progress(MigrationLoadJobId id, String message);

    /** Records the schemas the load uses; a schema that is not given is as it was. */
    void schemas(MigrationLoadJobId id, String stagingSchema, String backupSchema, String discardedSchema);

    /** The loads that go on and have not changed since before the time, locked until the end of the transaction. */
    List<MigrationLoadJob> lockActiveSince(LocalDateTime time);
}
