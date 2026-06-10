package org.oagi.score.gateway.http.api.integration_management.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.springframework.util.StringUtils.hasText;

/**
 * Binds the {@code score.integration.github.*} configuration (application.yml).
 * Issue #1533. The OAuth client credentials live in the deployment environment
 * (GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET), never in the database.
 */
@Component
public class GitHubIntegrationProperties {

    @Value("${score.integration.github.enabled:false}")
    private boolean enabled;

    @Value("${score.integration.github.client-id:}")
    private String clientId;

    @Value("${score.integration.github.client-secret:}")
    private String clientSecret;

    @Value("${score.integration.github.scope:repo}")
    private String scope;

    @Value("${score.integration.github.authorization-uri:https://github.com/login/oauth/authorize}")
    private String authorizationUri;

    @Value("${score.integration.github.token-uri:https://github.com/login/oauth/access_token}")
    private String tokenUri;

    @Value("${score.integration.github.api-base-url:https://api.github.com}")
    private String apiBaseUrl;

    @Value("${score.integration.github.default-repo-owner:}")
    private String defaultRepoOwner;

    @Value("${score.integration.github.default-repo-name:}")
    private String defaultRepoName;

    @Value("${score.integration.github.redirect-uri:http://localhost:4200/api/integration/github/callback}")
    private String redirectUri;

    @Value("${score.integration.github.web-base-url:http://localhost:4200}")
    private String webBaseUrl;

    @Value("${score.integration.github.webhook-secret:}")
    private String webhookSecret;

    /** Whether the administrator turned the integration on. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Enabled AND the OAuth App credentials are present; otherwise the feature is unavailable. */
    public boolean isConfigured() {
        return enabled && hasText(clientId) && hasText(clientSecret);
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getScope() {
        return scope;
    }

    public String getAuthorizationUri() {
        return authorizationUri;
    }

    public String getTokenUri() {
        return tokenUri;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public String getDefaultRepoOwner() {
        return defaultRepoOwner;
    }

    public String getDefaultRepoName() {
        return defaultRepoName;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getWebBaseUrl() {
        return webBaseUrl;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }
}
