package org.oagi.score.gateway.http.api.integration_management.github.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.StringUtils.hasText;

/**
 * Binds the {@code score.integration.github.*} configuration (application.yml).
 * Issue #1533. The OAuth client credentials live in the deployment environment
 * (GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET), never in the database.
 */
@Component
public class GitHubIntegrationProperties {

    /** Matches a Projects v2 URL: {@code .../orgs|users/<owner>/projects/<n>}; trailing path ignored. */
    private static final Pattern PROJECT_URL_PATTERN =
            Pattern.compile("github\\.com/(orgs|users)/([^/]+)/projects/(\\d{1,9})");

    /** Matches a repository URL: {@code github.com/<owner>/<repo>}; any trailing path/.git ignored. */
    private static final Pattern REPO_URL_PATTERN =
            Pattern.compile("github\\.com/([^/]+)/([^/?#]+)");

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

    /** The default repository's URL, e.g. {@code https://github.com/OAGi/oagis}. */
    @Value("${score.integration.github.default-repo-url:}")
    private String defaultRepoUrl;

    @Value("${score.integration.github.redirect-uri:http://localhost:4200/api/integration/github/callback}")
    private String redirectUri;

    @Value("${score.integration.github.web-base-url:http://localhost:4200}")
    private String webBaseUrl;

    @Value("${score.integration.github.webhook-secret:}")
    private String webhookSecret;

    // --- Projects v2 board fieldOption sync (issue #1533, Feature 2) ---

    @Value("${score.integration.github.project-enabled:false}")
    private boolean projectEnabled;

    /** The target board's URL, e.g. {@code https://github.com/orgs/OAGi/projects/8}. */
    @Value("${score.integration.github.project-url:}")
    private String projectUrl;

    /** The name of the board's single-select fieldOption field (the "Status" field), e.g. {@code Status}. */
    @Value("${score.integration.github.project-status-field-name:Status}")
    private String projectStatusFieldName;

    /** Whether the administrator turned the integration on. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Enabled AND the OAuth App credentials are present; otherwise the feature is unavailable. */
    public boolean isConfigured() {
        return enabled && hasText(clientId) && hasText(clientSecret);
    }

    /**
     * Whether the Projects v2 fieldOption sync is fully configured: the base integration is configured, the
     * administrator turned fieldOption sync on, and {@code project-url} parses to a project. Board writes use
     * the acting (connected) user's GitHub token, so no separate credential is configured here. The
     * status field is auto-discovered at runtime (the single-select field whose options carry the
     * fieldOptionByState). When false, a state change never touches the board.
     */
    public boolean isProjectConfigured() {
        return isConfigured() && projectEnabled && parseProjectUrl() != null;
    }

    /**
     * The OAuth scope to request at connect time. When fieldOption sync is on, two scopes are appended to the
     * configured scope (whichever are missing): {@code project} — board writes go through the connected
     * user's token and Projects v2 needs it — and {@code read:org} — so the server can verify the user's
     * organization membership and warn when they lack board-write permission. An operator only flips
     * {@code project-enabled}; when fieldOption sync is off, users are not asked for scopes the feature does not use.
     */
    public String getEffectiveScope() {
        if (!projectEnabled) {
            return scope;
        }
        String result = hasText(scope) ? scope : "";
        result = appendScope(result, "project");
        result = appendScope(result, "read:org");
        return result;
    }

    /** Appends {@code token} to a comma-separated scope string unless it is already an exact element. */
    private static String appendScope(String scope, String token) {
        for (String s : scope.split(",")) {
            if (token.equals(s.trim())) {
                return scope;
            }
        }
        return scope.isEmpty() ? token : scope + "," + token;
    }

    /**
     * Whether a comma-separated GitHub OAuth scope string grants the <em>write</em> {@code project}
     * scope that Projects v2 board writes need. Matches the {@code project} token exactly — the
     * read-only {@code read:project} scope (which contains the substring "project" but cannot write)
     * does not count, so a substring test would wrongly accept it.
     */
    public static boolean grantsProjectWrite(String scope) {
        if (!hasText(scope)) {
            return false;
        }
        for (String token : scope.split(",")) {
            if ("project".equals(token.trim())) {
                return true;
            }
        }
        return false;
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

    public String getDefaultRepoUrl() {
        return defaultRepoUrl;
    }

    /** Owner of the default repository URL, or {@code null} if it is unset or malformed. */
    public String getDefaultRepoOwner() {
        ParsedRepoUrl repo = parseRepoUrl();
        return (repo == null) ? null : repo.owner();
    }

    /** Name of the default repository URL, or {@code null} if it is unset or malformed. */
    public String getDefaultRepoName() {
        ParsedRepoUrl repo = parseRepoUrl();
        return (repo == null) ? null : repo.name();
    }

    /**
     * Parses {@code default-repo-url} (e.g. {@code https://github.com/OAGi/oagis}, with any trailing
     * path or {@code .git} suffix ignored) into the owner and repository name — or {@code null} if it
     * is blank or malformed.
     */
    private ParsedRepoUrl parseRepoUrl() {
        if (!hasText(defaultRepoUrl)) {
            return null;
        }
        Matcher matcher = REPO_URL_PATTERN.matcher(defaultRepoUrl);
        if (!matcher.find()) {
            return null;
        }
        String name = matcher.group(2);
        if (name.endsWith(".git")) {
            name = name.substring(0, name.length() - ".git".length());
        }
        return new ParsedRepoUrl(matcher.group(1), name);
    }

    private record ParsedRepoUrl(String owner, String name) {
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

    public boolean isProjectEnabled() {
        return projectEnabled;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    /** The configured name of the board's single-select fieldOption field (default {@code Status}). */
    public String getProjectStatusFieldName() {
        return projectStatusFieldName;
    }

    /** Owner login of the configured project URL, or {@code null} if it does not parse. */
    public String getProjectOwner() {
        ParsedProjectUrl project = parseProjectUrl();
        return (project == null) ? null : project.owner();
    }

    /** {@code org} or {@code user} for the configured project URL ({@code org} if it does not parse). */
    public String getProjectOwnerType() {
        ParsedProjectUrl project = parseProjectUrl();
        return (project == null) ? "org" : project.ownerType();
    }

    /** Number of the configured project URL, or {@code 0} if it does not parse. */
    public int getProjectNumber() {
        ParsedProjectUrl project = parseProjectUrl();
        return (project == null) ? 0 : project.number();
    }

    /**
     * Parses {@code project-url} (e.g. {@code https://github.com/orgs/OAGi/projects/8}, or a
     * {@code .../users/<login>/projects/<n>} user board, with any trailing {@code /views/<n>}
     * ignored) into the owner type, owner login, and project number — or {@code null} if it is blank
     * or malformed.
     */
    private ParsedProjectUrl parseProjectUrl() {
        if (!hasText(projectUrl)) {
            return null;
        }
        Matcher matcher = PROJECT_URL_PATTERN.matcher(projectUrl);
        if (!matcher.find()) {
            return null;
        }
        String ownerType = "users".equals(matcher.group(1)) ? "user" : "org";
        return new ParsedProjectUrl(ownerType, matcher.group(2), Integer.parseInt(matcher.group(3)));
    }

    private record ParsedProjectUrl(String ownerType, String owner, int number) {
    }
}
