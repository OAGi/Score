package org.oagi.score.gateway.http.api.release_management.service.migrationload;

import org.oagi.score.gateway.http.api.release_management.model.MigrationLoadJobId;
import org.oagi.score.gateway.http.api.release_management.model.MigrationLoadState;
import org.oagi.score.gateway.http.common.async.StompNotifier;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.api.release_management.repository.MigrationLoadRepositories;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Says where a load is: in the record of the loads (which whoever asks how it goes reads), and to the user who started it over
 * STOMP, on the topic of their account.
 */
@Component
public class MigrationLoadReporter {

    /** What the user is told. */
    public record Progress(MigrationLoadJobId jobId, MigrationLoadState state, String message) {
    }

    private final MigrationLoadRepositories repositories;
    private final ObjectProvider<StompNotifier> notifier;

    public MigrationLoadReporter(MigrationLoadRepositories repositories, ObjectProvider<StompNotifier> notifier) {
        this.repositories = repositories;
        this.notifier = notifier;
    }

    /**
     * Moves the load to the state, from any state in which it goes on.
     *
     * @throws IllegalStateException when the load is not in one (it was taken back as abandoned), so that whoever goes on with it stops
     */
    public void moved(MigrationLoadJobId id, ScoreUser requester, MigrationLoadState to, String message) {
        moved(id, requester, MigrationLoadState.ACTIVE, to, message);
    }

    /** Moves the load to the state, from the states given. */
    public void moved(MigrationLoadJobId id, ScoreUser requester, Set<MigrationLoadState> from, MigrationLoadState to, String message) {
        if (!repositories.jobs().moveState(id, from, to, message)) {
            throw new IllegalStateException("The load " + id + " is not " + from + " any more, and is not moved to " + to + ".");
        }
        tell(requester, new Progress(id, to, message));
    }

    /** Says what the load does, as it goes on in the state it is in. */
    public void said(MigrationLoadJobId id, ScoreUser requester, MigrationLoadState state, String message) {
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
