package org.oagi.score.gateway.http.api.release_management.service.migrationload;

import org.oagi.score.gateway.http.api.release_management.model.MigrationLoadJob;
import org.oagi.score.gateway.http.api.release_management.model.MigrationLoadState;
import org.oagi.score.gateway.http.api.release_management.repository.MigrationLoadJobRepository;
import org.oagi.score.gateway.http.common.async.StaleJobSweeper;
import org.oagi.score.gateway.http.api.release_management.repository.MigrationLoadRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Takes a load back that has stayed as it is for too long for anyone to be doing it (the server that did it stopped): the load
 * ends as FAILED, the staging schema it made is dropped, and a freeze of writing that it left is ended. A load that was swapping
 * or undoing a swap is only marked: the schemas it was moving are to be looked at, not dropped.
 */
@Component
public class MigrationLoadWatchdog {

    private static final Set<MigrationLoadState> MOVING_SCHEMAS = Set.of(MigrationLoadState.SWAPPING, MigrationLoadState.ROLLING_BACK);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final MigrationLoadRepositories repositories;
    private final StaleJobSweeper sweeper;
    private final MigrationLoadSettings settings;
    private final ObjectProvider<WriteFreeze> writeFreeze;

    public MigrationLoadWatchdog(MigrationLoadRepositories repositories, StaleJobSweeper sweeper, MigrationLoadSettings settings,
                                 ObjectProvider<WriteFreeze> writeFreeze) {
        this.repositories = repositories;
        this.sweeper = sweeper;
        this.settings = settings;
        this.writeFreeze = writeFreeze;
    }

    @Scheduled(initialDelayString = "${score.migration-load.recovery-initial-delay:PT2M}",
            fixedDelayString = "${score.migration-load.recovery-interval:PT10M}")
    public void recover() {
        if (!settings.enabled()) {
            return;
        }
        try {
            recoverSince(LocalDateTime.now().minus(settings.staleAfter()));
        } catch (RuntimeException e) {
            logger.error("The loads of release packages that go on could not be taken back: " + e.getMessage(), e);
        }
    }

    /** Takes back the loads that have not changed since before the time; how many. */
    public int recoverSince(LocalDateTime time) {
        MigrationLoadJobRepository jobs = repositories.jobs();
        List<MigrationLoadJob> abandoned = new ArrayList<>();
        int taken = sweeper.sweep(time, jobs::lockActiveSince,
                job -> jobs.moveState(job.id(), MigrationLoadState.ACTIVE, MigrationLoadState.FAILED,
                        "The load was abandoned: it did not change since " + job.lastUpdateTimestamp() + ", and the server that did it ("
                                + job.nodeId() + ") may have stopped. The schemas it made are named in this record."),
                abandoned::add);
        for (MigrationLoadJob job : abandoned) {
            logger.warn("The load " + job.id() + " of " + job.packageFileName() + " was abandoned in " + job.state() + ".");
            putRight(job);
        }
        return taken;
    }

    /** What an abandoned load left: its staging schema, unless it was moving schemas, and the freeze of writing. */
    private void putRight(MigrationLoadJob job) {
        try {
            if (!MOVING_SCHEMAS.contains(job.state()) && job.stagingSchema() != null
                    && repositories.query().schemaExists(job.stagingSchema())) {
                repositories.command().dropSchema(job.stagingSchema());
            }
        } catch (RuntimeException e) {
            logger.error("The schema " + job.stagingSchema() + " of the abandoned load " + job.id() + " could not be dropped: " + e.getMessage(), e);
        }
        try {
            writeFreeze.getIfAvailable(UnavailableWriteFreeze::new).release();
        } catch (RuntimeException e) {
            logger.error("The freeze of writing of the abandoned load " + job.id() + " could not be ended: " + e.getMessage(), e);
        }
    }
}
