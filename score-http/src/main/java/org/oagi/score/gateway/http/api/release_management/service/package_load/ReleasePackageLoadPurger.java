package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJob;
import org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Discards the loads that are over (a failed one only when it keeps no tables) and have not changed for the time they are kept (a week unless the properties say
 * otherwise), so that the schemas they keep for an undo do not stay for ever. It does nothing while a load goes on.
 */
@Component
public class ReleasePackageLoadPurger {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ReleasePackageLoadRepositories repositories;
    private final ReleasePackageLoadProperties settings;
    private final ReleasePackageLoadDiscarder discarder;

    public ReleasePackageLoadPurger(ReleasePackageLoadRepositories repositories, ReleasePackageLoadProperties settings,
                               ReleasePackageLoadDiscarder discarder) {
        this.repositories = repositories;
        this.settings = settings;
        this.discarder = discarder;
    }

    @Scheduled(initialDelayString = "#{@releasePackageLoadProperties.purgeInitialDelay}", fixedDelayString = "#{@releasePackageLoadProperties.purgeInterval}")
    public void purge() {
        try {
            purgeBefore(LocalDateTime.now().minus(settings.getRetention()));
        } catch (RuntimeException e) {
            logger.error("The loads of release packages that are kept too long could not be discarded: " + e.getMessage(), e);
        }
    }

    /**
     * Discards the loads that have not changed since before the time.
     *
     * @return how many records were removed
     */
    public int purgeBefore(LocalDateTime time) {
        if (!repositories.jobs().findActive().isEmpty()) {
            return 0;
        }
        int removed = 0;
        List<ReleasePackageLoadJob> expired = repositories.jobs().findNotChangedSince(ReleasePackageLoadDiscarder.DISCARDABLE, time);
        for (ReleasePackageLoadJob job : expired) {
            if (!discarder.canDiscard(job)) {
                // A failure left tables in its schemas: they stay until an administrator has looked at them.
                continue;
            }
            try {
                removed += discarder.discard(job);
            } catch (RuntimeException e) {
                // One that cannot be discarded stays, to be tried again at the next time; the others go on.
                logger.warn("The load " + job.id() + " could not be discarded: " + e.getMessage(), e);
            }
        }
        return removed;
    }
}
