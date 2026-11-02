package org.oagi.score.gateway.http.configuration.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SameOriginRequestFilterTest {

    private final SameOriginRequestFilter filter = new SameOriginRequestFilter(
            new ScoreCorsConfigurationSource(List.of("http://localhost:4200")));

    private MockHttpServletResponse run(String method, String path, String header, String value) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.addHeader("Host", "score.example.org");
        if (header != null) {
            request.addHeader(header, value);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        if (chain.getRequest() == null) {
            assertThat(response.getStatus()).isEqualTo(403);
        }
        return response;
    }

    private boolean passes(String method, String header, String value) throws Exception {
        return run(method, "/login", header, value).getStatus() != 403;
    }

    @Test
    void aCrossOriginFormPostIsRejected() throws Exception {
        assertThat(passes("POST", "Origin", "http://evil.example")).isFalse();
        assertThat(passes("POST", "Origin", "null")).isFalse();
        assertThat(passes("POST", "Origin", "https://sibling.example.org")).isFalse();
        assertThat(passes("POST", "Origin", "http://score.example.org:8080")).isFalse();
    }

    @Test
    void stateChangingMethodsAreAllChecked() throws Exception {
        for (String method : List.of("POST", "PUT", "PATCH", "DELETE")) {
            assertThat(passes(method, "Origin", "http://evil.example")).isFalse();
        }
    }

    @Test
    void theAppsOwnAndConfiguredOriginsPass() throws Exception {
        assertThat(passes("POST", "Origin", "https://score.example.org")).isTrue();
        assertThat(passes("POST", "Origin", "http://score.example.org")).isTrue();
        assertThat(passes("POST", "Origin", "http://localhost:4200")).isTrue();
    }

    @Test
    void theRefererIsCheckedWhenThereIsNoOrigin() throws Exception {
        assertThat(passes("POST", "Referer", "http://evil.example/page")).isFalse();
        assertThat(passes("POST", "Referer", "not a url")).isFalse();
        assertThat(passes("POST", "Referer", "https://score.example.org/login?x=1")).isTrue();
    }

    @Test
    void requestsWithoutOriginOrRefererAndSafeMethodsPass() throws Exception {
        assertThat(passes("POST", null, null)).isTrue();
        assertThat(passes("GET", "Origin", "http://evil.example")).isTrue();
        assertThat(passes("OPTIONS", "Origin", "http://evil.example")).isTrue();
    }
}
