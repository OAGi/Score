package org.oagi.score.gateway.http.configuration.websocket;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.configuration.security.ScoreCorsConfigurationSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebSocketOriginInterceptorTest {

    private final WebSocketOriginInterceptor interceptor =
            new WebSocketOriginInterceptor(new ScoreCorsConfigurationSource(List.of("https://app.example.org")));

    private MockHttpServletResponse lastResponse;

    private boolean handshake(String host, String origin) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ws");
        request.addHeader("Host", host);
        if (origin != null) {
            request.addHeader("Origin", origin);
        }
        lastResponse = new MockHttpServletResponse();
        return interceptor.beforeHandshake(new ServletServerHttpRequest(request),
                new ServletServerHttpResponse(lastResponse), null, new HashMap<>());
    }

    @Test
    void theOriginTheRequestWasAddressedToIsAcceptedWhateverTheSchemeTheBackendSees() {
        // Behind a proxy that ends TLS the backend sees http, while the browser sends https.
        assertTrue(handshake("score.example.org", "https://score.example.org"));
        assertTrue(handshake("score.example.org", "http://score.example.org"));
    }

    @Test
    void aConfiguredOriginIsAccepted() {
        assertTrue(handshake("score.example.org", "https://app.example.org/"));
    }

    @Test
    void anotherOriginIsRefusedWithForbidden() {
        assertFalse(handshake("score.example.org", "https://evil.example.com"));
        assertEquals(HttpStatus.FORBIDDEN.value(), lastResponse.getStatus());
        assertFalse(handshake("score.example.org", "https://score.example.org.evil.com"));
    }

    @Test
    void aRequestWithoutAnOriginIsNotFromABrowserAndPasses() {
        assertTrue(handshake("score.example.org", null));
    }
}
