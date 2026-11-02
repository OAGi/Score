package org.oagi.score.gateway.http.configuration.websocket;

import org.oagi.score.gateway.http.configuration.security.ScoreCorsConfigurationSource;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.session.Session;
import org.springframework.session.web.socket.config.annotation.AbstractSessionWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration
        extends AbstractSessionWebSocketMessageBrokerConfigurer<Session> {

    @Value("${score.cors.allowed-origins:}")
    private List<String> allowedOrigins;

    @Autowired
    private SessionService sessionService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureStompEndpoints(StompEndpointRegistry registry) {
        // The origin is checked by WebSocketOriginInterceptor (the origin the request was addressed to, with http or
        // https, and the configured CORS origins), so Spring's own check, which compares the scheme, lets all pass.
        registry.addEndpoint("/ws")
                .addInterceptors(new WebSocketOriginInterceptor(new ScoreCorsConfigurationSource(
                        allowedOrigins.stream().filter(e -> !e.isBlank()).toList())),
                        new HttpSessionHandshakeInterceptor())
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new WebSocketAuthorizationInterceptor(sessionService));
    }
}
