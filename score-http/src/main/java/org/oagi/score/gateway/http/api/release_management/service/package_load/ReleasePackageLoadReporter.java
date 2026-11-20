package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJobId;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadState;
import org.oagi.score.gateway.http.common.async.StompNotifier;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadRepositories;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Says where a load is: in the record of the loads (which whoever asks how it goes reads), and to the user who started it over
 * STOMP, on the topic of their account.
 */
@Component
public class ReleasePackageLoadReporter {

    /** What the user is told. */
    public record Progress(ReleasePackageLoadJobId jobId, ReleasePackageLoadState state, String message) {
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ReleasePackageLoadRepositories repositories;
    private final ObjectProvider<StompNotifier> notifier;

    public ReleasePackageLoadReporter(ReleasePackageLoadRepositories repositories, ObjectProvider<StompNotifier> notifier) {
        this.repositories = repositories;
        this.notifier = notifier;
    }

    /**
     * Moves the load to the state, from any state in which it goes on.
     *
     * @throws IllegalStateException when the load is not in one (it was taken back as abandoned), so that whoever goes on with it stops
     */
    public void moved(ReleasePackageLoadJobId id, ScoreUser requester, ReleasePackageLoadState to, String message) {
        moved(id, requester, ReleasePackageLoadState.ACTIVE, to, message);
    }

    /** Moves the load to the state, from the states given. */
    public void moved(ReleasePackageLoadJobId id, ScoreUser requester, Set<ReleasePackageLoadState> from, ReleasePackageLoadState to, String message) {
        if (!repositories.jobs().moveState(id, from, to, message)) {
            throw new IllegalStateException("The load " + id + " is not " + from + " any more, and is not moved to " + to + ".");
        }
        logger.info("Load " + id + " is " + to + ": " + message);
        tell(requester, new Progress(id, to, message));
    }

    /** Says what the load does, as it goes on in the state it is in. */
    public void said(ReleasePackageLoadJobId id, ScoreUser requester, ReleasePackageLoadState state, String message) {
        repositories.jobs().progress(id, message);
        tell(requester, new Progress(id, state, message));
    }

    private void tell(ScoreUser requester, Progress progress) {
        StompNotifier stomp = notifier.getIfAvailable();
        if (stomp != null && requester != null) {
            stomp.toUser(requester.username(), progress);
        }
    }
}
