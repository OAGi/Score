package org.oagi.score.gateway.http.configuration.websocket;

import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;

/**
 * A STOMP client has to be signed in to connect and subscribe, and may subscribe to the message topic of
 * its own account only. A destination with pattern characters is refused: the simple broker matches published
 * destinations against it, so '/topic/**' would deliver the message topic of every account.
 */
public class WebSocketAuthorizationInterceptor implements ChannelInterceptor {

    static final String MESSAGE_TOPIC_PREFIX = "/topic/message/";

    /**
     * The message topic of an account. A login id is free text, so the characters that make a destination a pattern
     * (and the escape character itself) are written as %XX: the topic of an account is then never a pattern, and
     * never matches the topic of another account. The web client writes it the same way.
     */
    public static String messageTopicOf(String username) {
        return MESSAGE_TOPIC_PREFIX + username.replace("%", "%25").replace("*", "%2A")
                .replace("?", "%3F").replace("{", "%7B");
    }

    private final SessionService sessionService;

    public WebSocketAuthorizationInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (StompCommand.CONNECT.equals(command) || StompCommand.STOMP.equals(command)
                || StompCommand.SUBSCRIBE.equals(command) || StompCommand.SEND.equals(command)) {
            if (!(accessor.getUser() instanceof Authentication authentication) || !authentication.isAuthenticated()) {
                throw new MessagingException("Authentication is required.");
            }

            if (StompCommand.SUBSCRIBE.equals(command)) {
                String destination = accessor.getDestination();
                if (destination != null && containsPattern(destination)) {
                    throw new MessagingException("Not allowed to subscribe to '" + destination + "'.");
                }
                if (destination != null && destination.startsWith(MESSAGE_TOPIC_PREFIX)) {
                    ScoreUser user = sessionService.asScoreUser(authentication);
                    if (user == null || !destination.equals(messageTopicOf(user.username()))) {
                        throw new MessagingException("Not allowed to subscribe to '" + destination + "'.");
                    }
                }
            }
        }
        return message;
    }

    private static boolean containsPattern(String destination) {
        return destination.indexOf('*') >= 0 || destination.indexOf('?') >= 0 || destination.indexOf('{') >= 0;
    }
}
