package org.oagi.score.gateway.http.api.release_management.service;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;


/**
 * Tells the screens that show the releases of a library that a release has changed (created, discarded, or moved to
 * another state), so that they load the list again by themselves. It is sent once the surrounding transaction has
 * committed: a screen that loads the list at once reads what was written, and a transition that is rolled back tells
 * no one. The message holds no more than which release of which library, since every signed-in user may read the list.
 */
@Component
public class ReleaseChangeNotifier {

    public static final String TOPIC = "/topic/release";

    /**
     * What is announced: which release, of which library (none when it is not known), and whether it changed or the work
     * for it failed, in which case the login id of who asked for it, for that user's screen to say so. Not why: that is told to the user
     * in a message.
     */
    public record Change(LibraryId libraryId, ReleaseId releaseId, String outcome, String username) {
    }

    public static final String CHANGED = "changed";
    public static final String FAILED = "failed";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SimpMessagingTemplate messagingTemplate;
    private final RepositoryFactory repositoryFactory;

    public ReleaseChangeNotifier(SimpMessagingTemplate messagingTemplate, RepositoryFactory repositoryFactory) {
        this.messagingTemplate = messagingTemplate;
        this.repositoryFactory = repositoryFactory;
    }

    /** Call it before the release is deleted: the library is found from the release. */
    public void changed(ReleaseId releaseId) {
        try {
            LibraryId libraryId = repositoryFactory.releaseProgressRepository().libraryOfRelease(releaseId).orElse(null);
            changed(libraryId, releaseId);
        } catch (RuntimeException e) {
            logger.warn("The change of the release " + releaseId + " could not be announced: " + e.getMessage());
        }
    }

    /** The work for the release failed: the release is given back, and the user who asked for it is told. */
    public void failed(ReleaseId releaseId, String requesterUsername) {
        try {
            LibraryId libraryId = repositoryFactory.releaseProgressRepository().libraryOfRelease(releaseId).orElse(null);
            announce(libraryId, releaseId, FAILED, requesterUsername);
        } catch (RuntimeException e) {
            logger.warn("The failure of the release " + releaseId + " could not be announced: " + e.getMessage());
        }
    }

    public void changed(LibraryId libraryId, ReleaseId releaseId) {
        announce(libraryId, releaseId, CHANGED, null);
    }

    private void announce(LibraryId libraryId, ReleaseId releaseId, String outcome, String username) {
        Change message = new Change(libraryId, releaseId, outcome, username);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(message);
                }
            });
        } else {
            send(message);
        }
    }

    private void send(Change message) {
        try {
            messagingTemplate.convertAndSend(TOPIC, message);
        } catch (RuntimeException e) {
            logger.warn("The change of a release could not be announced: " + e.getMessage());
        }
    }
}
