package org.oagi.score.gateway.http.configuration.websocket;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.tomcat.autoconfigure.servlet.TomcatServletWebServerAutoConfiguration;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.session.MapSessionRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.session.SessionRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The real {@link WebSocketConfiguration} on an embedded server, with a real STOMP client: what a signed-in client
 * can subscribe to and what it receives. No database; the account of a request is read from the X-Test-User header.
 */
@SpringBootTest(classes = WebSocketStompEndToEndTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketStompEndToEndTest {

    @Configuration
    @ImportAutoConfiguration({TomcatServletWebServerAutoConfiguration.class, DispatcherServletAutoConfiguration.class})
    @Import(WebSocketConfiguration.class)
    static class TestApplication {

        @Bean
        SessionRepository<?> sessionRepository() {
            return new MapSessionRepository(new ConcurrentHashMap<>());
        }

        /** Signs the request in as the account named by X-Test-User, as the Score session does for a real one. */
        @Bean
        OncePerRequestFilter testUserFilter() {
            return new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                                FilterChain chain) throws ServletException, IOException {
                    String user = request.getHeader("X-Test-User");
                    if (user == null) {
                        chain.doFilter(request, response);
                        return;
                    }
                    Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(user, "n/a", List.of());
                    chain.doFilter(new HttpServletRequestWrapper(request) {
                        @Override
                        public Principal getUserPrincipal() {
                            return authentication;
                        }
                    }, response);
                }
            };
        }
    }

    @LocalServerPort
    int port;

    @MockitoBean
    SessionService sessionService;

    @Autowired
    SimpMessagingTemplate template;

    @BeforeEach
    void signInByName() {
        org.mockito.Mockito.when(sessionService.asScoreUser(org.mockito.ArgumentMatchers.any(Authentication.class)))
                .thenAnswer(invocation -> new ScoreUser(new UserId(BigInteger.ONE),
                        invocation.<Authentication>getArgument(0).getName(), "Name", null, false,
                        List.of(ScoreRole.END_USER)));
    }

    private final List<WebSocketStompClient> clients = new java.util.ArrayList<>();

    @AfterEach
    void stopClients() {
        clients.forEach(WebSocketStompClient::stop);
    }

    /** A connected client: what it received as "destination|payload", and the ERROR frames the server sent. */
    private class Client {
        final BlockingQueue<String> received = new LinkedBlockingQueue<>();
        final BlockingQueue<String> errors = new LinkedBlockingQueue<>();
        StompSession session;

        Client(String user) throws Exception {
            WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
            stompClient.setMessageConverter(new StringMessageConverter());
            clients.add(stompClient);
            WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
            if (user != null) {
                headers.add("X-Test-User", user);
            }
            CountDownLatch connected = new CountDownLatch(1);
            session = stompClient.connectAsync("ws://localhost:" + port + "/ws", headers, new StompHeaders(),
                    new StompSessionHandlerAdapter() {
                        @Override
                        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                            connected.countDown();
                        }

                        @Override
                        public void handleFrame(StompHeaders frameHeaders, Object payload) {
                            errors.add(String.valueOf(frameHeaders.getFirst("message")));
                        }

                        @Override
                        public void handleException(StompSession s, StompCommand c, StompHeaders h, byte[] p, Throwable e) {
                            errors.add(String.valueOf(e));
                        }

                        @Override
                        public void handleTransportError(StompSession s, Throwable e) {
                            errors.add(String.valueOf(e));
                        }
                    }).get(10, TimeUnit.SECONDS);
            assertTrue(connected.await(10, TimeUnit.SECONDS), "CONNECTED was not received");
        }

        /** Subscribes; false if the server answered with an ERROR frame (and the connection is closed), true after a quiet moment. */
        boolean subscribe(String destination) throws Exception {
            session.subscribe(destination, new StompSessionHandlerAdapter() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return String.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    received.add(headers.getDestination() + "|" + payload);
                }
            });
            // The simple broker answers no receipt; a refusal is an ERROR frame that closes the connection.
            Thread.sleep(700);
            return errors.isEmpty() && session.isConnected();
        }

        String next(long millis) throws InterruptedException {
            return received.poll(millis, TimeUnit.MILLISECONDS);
        }
    }

    @Test
    void aClientReceivesTheTopicsItSubscribedTo() throws Exception {
        Client client = new Client("ben.craker");
        assertTrue(client.subscribe(WebSocketAuthorizationInterceptor.messageTopicOf("ben.craker")), client.errors.toString());
        assertTrue(client.subscribe("/topic/acc/12"));
        assertTrue(client.subscribe("/topic/webpage/info"));

        template.convertAndSend(WebSocketAuthorizationInterceptor.messageTopicOf("ben.craker"), "mine");
        template.convertAndSend("/topic/acc/12", "component");
        template.convertAndSend("/topic/webpage/info", "info");
        template.convertAndSend(WebSocketAuthorizationInterceptor.messageTopicOf("someone"), "not mine");

        assertEquals(List.of("/topic/message/ben.craker|mine", "/topic/acc/12|component", "/topic/webpage/info|info"),
                List.of(client.next(3000), client.next(3000), client.next(3000)));
        assertNull(client.next(500));
        assertTrue(client.errors.isEmpty(), client.errors.toString());
    }

    @Test
    void aLoginIdWithPatternCharactersReceivesOnlyItsOwnMessages() throws Exception {
        Client star = new Client("a*b");
        Client other = new Client("axb");
        assertTrue(star.subscribe(WebSocketAuthorizationInterceptor.messageTopicOf("a*b")));
        assertTrue(other.subscribe(WebSocketAuthorizationInterceptor.messageTopicOf("axb")));

        template.convertAndSend(WebSocketAuthorizationInterceptor.messageTopicOf("a*b"), "for star");
        template.convertAndSend(WebSocketAuthorizationInterceptor.messageTopicOf("axb"), "for axb");

        assertEquals("/topic/message/a%2Ab|for star", star.next(3000));
        assertEquals("/topic/message/axb|for axb", other.next(3000));
        assertNull(star.next(500));
        assertNull(other.next(500));
    }

    @Test
    void aWildcardSubscriptionIsRefusedAndReceivesNothing() throws Exception {
        for (String destination : List.of("/topic/**", "/topic/*/*", "/topic/m*/**", "/topic/message/*")) {
            Client attacker = new Client("mallory");
            assertFalse(attacker.subscribe(destination), destination);
            assertNotNull(attacker.errors.poll(3, TimeUnit.SECONDS), "no ERROR frame for " + destination);

            template.convertAndSend(WebSocketAuthorizationInterceptor.messageTopicOf("victim"), "secret");
            template.convertAndSend("/topic/acc/1", "component");
            assertNull(attacker.next(500), destination);
        }
    }

    @Test
    void anotherAccountsMessageTopicIsRefused() throws Exception {
        Client mallory = new Client("mallory");
        assertFalse(mallory.subscribe("/topic/message/victim"));
        assertNotNull(mallory.errors.poll(3, TimeUnit.SECONDS));
    }

    @Test
    void aClientWithoutASessionCannotConnect() {
        assertThrows(Exception.class, () -> {
            Client anonymous = new Client(null);
            assertNotNull(anonymous.errors.poll(3, TimeUnit.SECONDS));
            throw new IllegalStateException("connected without a session");
        });
    }
}
