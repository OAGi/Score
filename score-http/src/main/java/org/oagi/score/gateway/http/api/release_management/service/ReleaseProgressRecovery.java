package org.oagi.score.gateway.http.api.release_management.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Takes a release back that has been drafted or published for too long for anyone to be doing it: the server that was doing
 * it stopped, or the event of the request was lost. A transition that is being done holds its release, so it is not
 * taken back from under its transaction. A release that was being drafted is 'Initialized' again, one that was being
 * published is a 'Draft' again; the components were not touched, since they are moved together with the end of the transition.
 */
@Component
public class ReleaseProgressRecovery {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RepositoryFactory repositoryFactory;
    private final ReleaseFailureReporter failureReporter;
    private final PlatformTransactionManager transactionManager;
    private final Duration timeout;

    public ReleaseProgressRecovery(RepositoryFactory repositoryFactory, ReleaseFailureReporter failureReporter,
                                   PlatformTransactionManager transactionManager,
                                   @Value("${score.release.in-progress-timeout:PT30M}") Duration timeout) {
        this.repositoryFactory = repositoryFactory;
        this.failureReporter = failureReporter;
        this.transactionManager = transactionManager;
        this.timeout = timeout;
    }

    @Scheduled(initialDelayString = "${score.release.recovery-initial-delay:PT1M}",
            fixedDelayString = "${score.release.recovery-interval:PT5M}")
    public void recover() {
        try {
            recoverSince(LocalDateTime.now().minus(timeout));
        } catch (RuntimeException e) {
            logger.error("The releases that are in the middle of a transition could not be taken back: " + e.getMessage(), e);
        }
    }

    /** Takes back the releases that have been in the middle of a transition since before the time; how many. */
    public int recoverSince(LocalDateTime time) {
        return new TransactionTemplate(transactionManager).execute(status -> {
            var progress = repositoryFactory.releaseProgressRepository();
            int taken = 0;
            for (var inProgress : progress.lockInProgressSince(time)) {
                ReleaseState back = inProgress.state().givenBack();
                if (progress.moveState(inProgress.releaseId(), inProgress.state(), back)) {
                    failureReporter.abandoned(inProgress.releaseId(), inProgress.state(), inProgress.lastUpdatedBy(), time);
                    taken++;
                }
            }
            return taken;
        });
    }
}
