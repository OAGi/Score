package org.oagi.score.gateway.http.configuration.websocket;

import org.oagi.score.gateway.http.configuration.security.ScoreCorsConfigurationSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Refuses a WebSocket handshake whose {@code Origin} is neither a configured origin nor the origin the request was
 * addressed to, with the same rule as the API ({@link ScoreCorsConfigurationSource}): the {@code Host} header, with
 * http or https. Spring's own same-origin check compares the scheme the backend sees, which is http behind a proxy
 * that ends TLS, so a browser at https://host would be refused. A request without {@code Origin} is not from a
 * browser and passes; it still needs a session.
 */
public class WebSocketOriginInterceptor implements HandshakeInterceptor {

    private final ScoreCorsConfigurationSource allowedOrigins;

    public WebSocketOriginInterceptor(ScoreCorsConfigurationSource allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String origin = request.getHeaders().getFirst(HttpHeaders.ORIGIN);
        if (origin == null || !(request instanceof ServletServerHttpRequest servletRequest)) {
            return true;
        }
        String normalized = stripTrailingSlash(origin.trim());
        boolean allowed = allowedOrigins.allowedOrigins(servletRequest.getServletRequest()).stream()
                .anyMatch(e -> stripTrailingSlash(e.trim()).equalsIgnoreCase(normalized));
        if (!allowed) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
        }
        return allowed;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

    private static String stripTrailingSlash(String origin) {
        return origin.endsWith("/") ? origin.substring(0, origin.length() - 1) : origin;
    }
}
