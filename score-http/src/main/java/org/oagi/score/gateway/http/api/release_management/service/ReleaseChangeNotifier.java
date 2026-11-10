package org.oagi.score.gateway.http.api.release_management.service;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.configuration.websocket.WebSocketAuthorizationInterceptor;
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

    /** What is announced to everyone: which release, of which library (none when it is not known). Nothing more. */
    public record Change(LibraryId libraryId, ReleaseId releaseId) {
    }

    /** The event of {@link Failure}, as the screens that read the topic of an account tell it from a message. */
    public static final String RELEASE_FAILED = "releaseFailed";

    /**
     * What is told to the user who asked for a release, and to no one else, when the work for it failed: on the topic of
     * their own account, which no one else may subscribe to. Not why: that is in the message sent to the user.
     */
    public record Failure(String event, LibraryId libraryId, ReleaseId releaseId) {
    }

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

    /**
     * The work for the release failed and the release is given back: everyone is told that it changed, and the user who
     * asked for it, on the topic of their account, that it failed.
     */
    public void failed(ReleaseId releaseId, String requesterUsername) {
        try {
            LibraryId libraryId = repositoryFactory.releaseProgressRepository().libraryOfRelease(releaseId).orElse(null);
            changed(libraryId, releaseId);
            Failure failure = new Failure(RELEASE_FAILED, libraryId, releaseId);
            afterCommit(() -> send(WebSocketAuthorizationInterceptor.messageTopicOf(requesterUsername), failure));
        } catch (RuntimeException e) {
            logger.warn("The failure of the release " + releaseId + " could not be announced: " + e.getMessage());
        }
    }

    public void changed(LibraryId libraryId, ReleaseId releaseId) {
        Change change = new Change(libraryId, releaseId);
        afterCommit(() -> send(TOPIC, change));
    }

    private static void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    private void send(String destination, Object message) {
        try {
            messagingTemplate.convertAndSend(destination, message);
        } catch (RuntimeException e) {
            logger.warn("The change of a release could not be announced: " + e.getMessage());
        }
    }
}
