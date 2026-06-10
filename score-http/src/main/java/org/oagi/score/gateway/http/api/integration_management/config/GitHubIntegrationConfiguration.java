package org.oagi.score.gateway.http.api.integration_management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Beans for the GitHub integration (issue #1533).
 */
@Configuration
public class GitHubIntegrationConfiguration {

    /**
     * A dedicated {@link RestTemplate} for GitHub API calls with bounded connect/read timeouts. GitHub
     * issue metadata is refreshed synchronously on view (conditional GET with ETag), so a slow or
     * unreachable GitHub must fail fast and fall back to the cache rather than hang the request thread.
     */
    @Bean("gitHubRestTemplate")
    public RestTemplate gitHubRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        return new RestTemplate(factory);
    }
}
