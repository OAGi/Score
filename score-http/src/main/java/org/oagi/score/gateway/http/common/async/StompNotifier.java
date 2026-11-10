package org.oagi.score.gateway.http.common.async;

import org.oagi.score.gateway.http.configuration.websocket.WebSocketAuthorizationInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Sends a message over STOMP once the surrounding transaction has committed, to a topic that everyone may read or to the
 * topic of the account of one user, which no one else may subscribe to. A failure to send is logged and never fails the
 * transaction: the screen that was to be told loads what it shows again when it is opened.
 */
@Component
public class StompNotifier {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SimpMessagingTemplate messagingTemplate;

    public StompNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /** To everyone who subscribes to the topic. Put nothing in it that not every signed-in user may know. */
    public void toTopic(String destination, Object message) {
        AfterCommit.run(() -> send(destination, message));
    }

    /** To the one user, on the topic of their account. */
    public void toUser(String username, Object message) {
        toTopic(WebSocketAuthorizationInterceptor.messageTopicOf(username), message);
    }

    private void send(String destination, Object message) {
        try {
            messagingTemplate.convertAndSend(destination, message);
        } catch (RuntimeException e) {
            logger.warn("A message to " + destination + " could not be sent: " + e.getMessage());
        }
    }
}
