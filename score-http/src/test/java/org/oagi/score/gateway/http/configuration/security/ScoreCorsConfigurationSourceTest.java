package org.oagi.score.gateway.http.configuration.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreCorsConfigurationSourceTest {

    private final ScoreCorsConfigurationSource source =
            new ScoreCorsConfigurationSource(List.of("http://localhost:4200"));

    private CorsConfiguration configFor(String host) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Host", host);
        return source.getCorsConfiguration(request);
    }

    @Test
    void anUnknownOriginIsNotAllowedEvenWithCredentials() {
        CorsConfiguration config = configFor("score.example.org");

        assertThat(config.getAllowCredentials()).isTrue();
        assertThat(config.checkOrigin("http://evil.example")).isNull();
        assertThat(config.checkOrigin("https://other.example.org")).isNull();
    }

    @Test
    void configuredOriginsAreAllowed() {
        assertThat(configFor("score.example.org").checkOrigin("http://localhost:4200"))
                .isEqualTo("http://localhost:4200");
    }

    @Test
    void theOriginTheRequestIsAddressedToIsAllowed() {
        CorsConfiguration config = configFor("score.example.org");

        assertThat(config.checkOrigin("https://score.example.org")).isEqualTo("https://score.example.org");
        assertThat(config.checkOrigin("http://score.example.org")).isEqualTo("http://score.example.org");
    }
}
