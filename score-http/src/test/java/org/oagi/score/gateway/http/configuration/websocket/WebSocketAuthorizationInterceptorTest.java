package org.oagi.score.gateway.http.configuration.websocket;

import org.springframework.security.core.Authentication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebSocketAuthorizationInterceptorTest {

    private final UsernamePasswordAuthenticationToken authentication =
            UsernamePasswordAuthenticationToken.authenticated("tester", "n/a", List.of());
    private WebSocketAuthorizationInterceptor interceptor;

    @BeforeEach
    void setUp() {
        SessionService sessionService = mock(SessionService.class);
        when(sessionService.asScoreUser(any(Authentication.class))).thenReturn(
                new ScoreUser(new UserId(BigInteger.ONE), "tester", "Tester", null, false, List.of(ScoreRole.END_USER)));
        interceptor = new WebSocketAuthorizationInterceptor(sessionService);
    }

    private Message<?> message(StompCommand command, String destination, boolean signedIn) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setDestination(destination);
        if (signedIn) {
            accessor.setUser(authentication);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    void anonymousClientCannotConnectOrSubscribe() {
        assertThrows(MessagingException.class,
                () -> interceptor.preSend(message(StompCommand.CONNECT, null, false), null));
        assertThrows(MessagingException.class,
                () -> interceptor.preSend(message(StompCommand.SUBSCRIBE, "/topic/acc/1", false), null));
    }

    @Test
    void aSignedInClientCanConnectAndSubscribeToOwnMessageTopicAndComponentTopics() {
        Message<?> connect = message(StompCommand.CONNECT, null, true);
        Message<?> own = message(StompCommand.SUBSCRIBE, "/topic/message/tester", true);
        Message<?> component = message(StompCommand.SUBSCRIBE, "/topic/acc/1", true);

        assertSame(connect, interceptor.preSend(connect, null));
        assertSame(own, interceptor.preSend(own, null));
        assertSame(component, interceptor.preSend(component, null));
    }

    @Test
    void aSignedInClientCannotSubscribeToTheMessageTopicOfAnotherUser() {
        assertThrows(MessagingException.class,
                () -> interceptor.preSend(message(StompCommand.SUBSCRIBE, "/topic/message/someone", true), null));
        assertThrows(MessagingException.class,
                () -> interceptor.preSend(message(StompCommand.SUBSCRIBE, "/topic/message/tester2", true), null));
    }
}
