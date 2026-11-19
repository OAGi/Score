package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJob;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadState;
import org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadJobRepository;
import org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Discards the record of a load whose schemas are only kept for an undo: the schema that holds the tables the swap took out, and the one
 * that holds the tables of a load that was undone, are dropped, and then the record is removed, so that the load is no longer the one
 * that can be undone and says nothing on the screen.
 *
 * <p>A load that failed is discarded only when the schemas it names hold no tables. One that failed before the swap has none (its schemas
 * were dropped), but one whose swap or undo could not be put right has the tables of the served schema in its backup schema, which is
 * then the only copy of them and is kept for an administrator to look at. A load that has replaced others is discarded with them, as what an older load kept can no longer be put back after a later load has
 * swapped (and would be the one to undo, once the later one is gone).</p>
 */
@Component
public class ReleasePackageLoadDiscarder {

    /** The states of a load that is over, which a record of is discarded (a failed load only when it keeps no tables, see {@link #canDiscard}). */
    public static final Set<ReleasePackageLoadState> DISCARDABLE =
            EnumSet.of(ReleasePackageLoadState.SUCCEEDED, ReleasePackageLoadState.ROLLED_BACK, ReleasePackageLoadState.FAILED);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ReleasePackageLoadRepositories repositories;

    public ReleasePackageLoadDiscarder(ReleasePackageLoadRepositories repositories) {
        this.repositories = repositories;
    }

    /** Whether the load can be discarded: it is over, and keeps no tables that a failure left in the schemas it names. */
    public boolean canDiscard(ReleasePackageLoadJob job) {
        if (!DISCARDABLE.contains(job.state())) {
            return false;
        }
        return job.state() != ReleasePackageLoadState.FAILED
                || (!holdsTables(job.backupSchema()) && !holdsTables(job.discardedSchema()));
    }

    private boolean holdsTables(String schema) {
        return schema != null && !repositories.query().tables(schema).isEmpty();
    }

    /**
     * Discards the load, and the loads that it replaced.
     *
     * @return how many records were removed
     * @throws IllegalStateException when the load cannot be discarded, see {@link #canDiscard}
     * @throws RuntimeException when a schema could not be dropped; the record of that load is then kept, so that it is tried again
     */
    public int discard(ReleasePackageLoadJob job) {
        if (!canDiscard(job)) {
            throw new IllegalStateException("The load " + job.id() + " cannot be discarded: it is not over, or it keeps tables that a failure left.");
        }
        ReleasePackageLoadJobRepository jobs = repositories.jobs();
        int removed = discardOne(job) ? 1 : 0;
        if (job.state() == ReleasePackageLoadState.SUCCEEDED) {
            List<ReleasePackageLoadJob> replaced = jobs.findBefore(job.id(), ReleasePackageLoadState.SUCCEEDED);
            for (ReleasePackageLoadJob older : replaced) {
                removed += discardOne(older) ? 1 : 0;
            }
        }
        return removed;
    }

    private boolean discardOne(ReleasePackageLoadJob job) {
        dropIfAny(job.backupSchema());
        dropIfAny(job.discardedSchema());
        boolean removed = repositories.jobs().delete(job.id(), DISCARDABLE);
        if (removed) {
            logger.info("The load " + job.id() + " of " + job.packageFileName() + " was discarded with its schemas.");
        }
        return removed;
    }

    private void dropIfAny(String schema) {
        if (schema != null) {
            repositories.command().dropSchema(schema);
        }
    }
}
