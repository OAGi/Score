package org.oagi.score.gateway.http.api.integration_management.github.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.oagi.score.gateway.http.api.integration_management.github.client.GitHubApiClient;
import org.oagi.score.gateway.http.api.integration_management.github.config.GitHubIntegrationProperties;
import org.oagi.score.gateway.http.api.integration_management.github.model.ProjectFieldOptions;
import org.oagi.score.gateway.http.api.integration_management.github.model.IssueFetchResult;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasText;

/**
 * Per-user GitHub connection: OAuth authorization-code flow + token storage in Redis.
 * Issue #1533. The access token and connection metadata live ONLY in Redis (never in the DB),
 * keyed by the Score user id. A short-lived state -> {userId, returnUrl} mapping protects the
 * callback (CSRF) and carries the user across the redirect so the callback need not be authenticated.
 *
 * <p>This service owns the business rules and local state (token/scope/login + issue-ETag storage in
 * Redis, OAuth state, webhook HMAC, the in-process project-refs cache, board field selection and the
 * anti-clobber guard); every actual call to GitHub is delegated to {@link GitHubApiClient}, so changing
 * how we talk to GitHub never touches this class.</p>
 */
@Service
public class GitHubIntegrationService {

    private static final String NS = "score:integration:github:";
    private static final String TOKEN_KEY = NS + "token:";
    private static final String LOGIN_KEY = NS + "login:";
    /** The OAuth scopes GitHub granted a user, captured at callback so the UI can tell if board sync works. */
    private static final String SCOPE_KEY = NS + "scope:";
    private static final String STATE_KEY = NS + "state:";
    private static final String CONNECTED_USERS = NS + "connected-users";
    /** Cached ETag of an issue's last fetched representation, keyed by repo coordinates (resource-global). */
    private static final String ISSUE_ETAG_KEY = NS + "issue-etag:";
    /** Cached issue ETags expire after this idle period so orphaned entries cannot accumulate. */
    private static final Duration ISSUE_ETAG_TTL = Duration.ofDays(30);
    /** The Projects v2 project/field/option ids are stable; cache them in-process for this long. */
    private static final long PROJECT_REFS_TTL_MILLIS = Duration.ofMinutes(30).toMillis();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** In-process cache of the resolved project node id / status field id / fieldOption option ids. */
    private volatile CachedProjectRefs projectRefsCache;

    @Autowired
    private GitHubIntegrationProperties properties;

    @Autowired
    private ProjectFieldOptions projectFieldOptions;

    @Autowired
    private GitHubApiClient gitHubApiClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public boolean isConfigured() {
        return properties.isConfigured();
    }

    public String getWebBaseUrl() {
        return properties.getWebBaseUrl();
    }

    public boolean isConnected(ScoreUser user) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_KEY + user.userId()));
    }

    public String getLogin(ScoreUser user) {
        Object v = redisTemplate.opsForValue().get(LOGIN_KEY + user.userId());
        return (v == null) ? null : v.toString();
    }

    public String getAccessToken(ScoreUser user) {
        Object v = redisTemplate.opsForValue().get(TOKEN_KEY + user.userId());
        return (v == null) ? null : v.toString();
    }

    /**
     * Begins the connect flow: stores a state token tied to the user and the page to return to,
     * and returns the GitHub authorization URL to redirect the browser to.
     */
    public String beginConnect(ScoreUser user, String returnUrl) {
        String state = UUID.randomUUID().toString().replace("-", "");
        Map<String, String> stateData = new HashMap<>();
        stateData.put("userId", String.valueOf(user.userId()));
        stateData.put("returnUrl", (returnUrl == null) ? "" : returnUrl);
        redisTemplate.opsForValue().set(STATE_KEY + state, stateData, Duration.ofMinutes(10));

        return UriComponentsBuilder.fromUriString(properties.getAuthorizationUri())
                .queryParam("client_id", properties.getClientId())
                .queryParam("redirect_uri", properties.getRedirectUri())
                .queryParam("scope", properties.getEffectiveScope())
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    /**
     * Completes the callback: validates state, exchanges the code for an access token, fetches the
     * GitHub login, and persists the token + login in Redis. Returns the page to return to (may be empty).
     * Returns {@code null} if the state is invalid/expired or the token exchange failed.
     */
    public String completeCallback(String code, String state) {
        Object raw = redisTemplate.opsForValue().get(STATE_KEY + state);
        if (raw == null) {
            return null;
        }
        redisTemplate.delete(STATE_KEY + state);
        Map<?, ?> stateData = (Map<?, ?>) raw;
        String userId = String.valueOf(stateData.get("userId"));
        String returnUrl = (stateData.get("returnUrl") == null) ? "" : stateData.get("returnUrl").toString();

        GitHubApiClient.OAuthToken token = gitHubApiClient.exchangeOAuthCode(code, state);
        if (token == null || !hasText(token.accessToken())) {
            return null;
        }
        String accessToken = token.accessToken();
        String login = gitHubApiClient.fetchLogin(accessToken);

        redisTemplate.opsForValue().set(TOKEN_KEY + userId, accessToken);
        if (login != null) {
            redisTemplate.opsForValue().set(LOGIN_KEY + userId, login);
        }
        // The token-exchange response carries the granted scopes (comma-separated); store them so the
        // status can tell whether this user can write the project board or must reconnect (issue #1533).
        if (token.scope() != null) {
            redisTemplate.opsForValue().set(SCOPE_KEY + userId, token.scope());
        }
        redisTemplate.opsForSet().add(CONNECTED_USERS, userId);
        return returnUrl;
    }

    public void disconnect(ScoreUser user) {
        String userId = String.valueOf(user.userId());
        redisTemplate.delete(TOKEN_KEY + userId);
        redisTemplate.delete(LOGIN_KEY + userId);
        redisTemplate.delete(SCOPE_KEY + userId);
        redisTemplate.opsForSet().remove(CONNECTED_USERS, userId);
    }

    /**
     * Whether {@code user}'s stored GitHub token was granted the {@code project} scope, required for the
     * Projects v2 board writes (which use the user's own token). False when the user connected before
     * fieldOption sync was enabled and has not reconnected — board writes then silently no-op for them.
     */
    public boolean hasProjectScope(ScoreUser user) {
        Object v = redisTemplate.opsForValue().get(SCOPE_KEY + user.userId());
        return v != null && GitHubIntegrationProperties.grantsProjectWrite(v.toString());
    }

    /**
     * Whether a board fieldOption sync would actually run for {@code user}: fieldOption sync is configured, the user
     * is connected, and their token has the {@code project} scope. Surfaced in the status so the dialog
     * only previews a board move it can keep, and the UI can prompt a reconnect otherwise.
     */
    public boolean isProjectSyncAvailable(ScoreUser user) {
        return properties.isProjectConfigured() && isConnected(user) && hasProjectScope(user);
    }

    public String getDefaultRepoOwner() {
        return properties.getDefaultRepoOwner();
    }

    public String getDefaultRepoName() {
        return properties.getDefaultRepoName();
    }

    public boolean isWebhookConfigured() {
        String secret = properties.getWebhookSecret();
        return secret != null && !secret.isBlank();
    }

    /**
     * Verifies a GitHub webhook payload against the configured secret using HMAC-SHA256
     * (the {@code X-Hub-Signature-256} header), with a constant-time comparison.
     */
    public boolean verifyWebhookSignature(byte[] body, String signatureHeader) {
        String secret = properties.getWebhookSecret();
        if (secret == null || secret.isBlank() || body == null
                || signatureHeader == null || !signatureHeader.startsWith("sha256=")) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(body);
            StringBuilder sb = new StringBuilder("sha256=");
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return MessageDigest.isEqual(
                    sb.toString().getBytes(StandardCharsets.UTF_8),
                    signatureHeader.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Best-effort fetch of a single issue's metadata (title/state/html_url/node_id) using the given
     * user's token; primes the ETag cache. Returns {@code null} if the user is not connected or the
     * call fails.
     */
    public JsonNode fetchIssue(ScoreUser user, String owner, String repo, int number) {
        String token = getAccessToken(user);
        if (token == null) {
            return null;
        }
        GitHubApiClient.FetchedIssue fetched = gitHubApiClient.fetchIssue(token, owner, repo, number);
        if (fetched == null) {
            return null;
        }
        JsonNode body = fetched.body();
        // Prime the cached ETag only when we actually got a body, so an empty response can't pin a
        // stale cache via a future 304.
        if (body != null && !body.isNull()) {
            storeIssueEtag(owner, repo, number, fetched.etag());
        }
        return body;
    }

    /**
     * Conditionally fetches an issue using the cached ETag ({@code If-None-Match}). When the issue has not
     * changed GitHub returns {@code 304 Not Modified} — cheap, with no body and (for authenticated requests)
     * no rate-limit cost — so this is safe to call synchronously on every view. Returns {@link
     * IssueFetchResult#notModified()} on 304, {@link IssueFetchResult#modified} (and re-stores the new ETag)
     * on 200, and {@link IssueFetchResult#unavailable()} if the user is not connected or the call fails.
     */
    public IssueFetchResult fetchIssueIfModified(ScoreUser user, String owner, String repo, int number) {
        String token = getAccessToken(user);
        if (token == null) {
            return IssueFetchResult.unavailable();
        }
        GitHubApiClient.ConditionalIssue result =
                gitHubApiClient.fetchIssueConditional(token, owner, repo, number, getIssueEtag(owner, repo, number));
        switch (result.status()) {
            case NOT_MODIFIED:
                return IssueFetchResult.notModified();
            case MODIFIED:
                // Store the ETag only once we actually have (and parsed) the body, so a truncated/empty
                // 200 can't pin a stale cache via a future 304.
                storeIssueEtag(owner, repo, number, result.etag());
                return IssueFetchResult.modified(result.body());
            default:
                return IssueFetchResult.unavailable();
        }
    }

    /**
     * Posts a comment on a GitHub issue with the given user's token (issue #1533, sub-task 4 —
     * the status update when a linked component reaches Candidate or is reverted). Works on closed
     * issues too (commenting never reopens them); only a locked conversation rejects it. Best-effort:
     * returns the created comment's {@code html_url}, or {@code null} when the user is not connected
     * or the call fails — a GitHub outage must never affect the state transition itself.
     */
    public String postIssueComment(ScoreUser user, String owner, String repo, int number, String body) {
        String token = getAccessToken(user);
        if (token == null || body == null || body.isBlank()) {
            return null;
        }
        return gitHubApiClient.postIssueComment(token, owner, repo, number, body);
    }

    private String issueEtagKey(String owner, String repo, int number) {
        return ISSUE_ETAG_KEY + owner + "/" + repo + "/" + number;
    }

    private String getIssueEtag(String owner, String repo, int number) {
        Object value = redisTemplate.opsForValue().get(issueEtagKey(owner, repo, number));
        return (value == null) ? null : value.toString();
    }

    private void storeIssueEtag(String owner, String repo, int number, String etag) {
        if (etag != null && !etag.isBlank()) {
            // (a) Bound the lifetime so an orphaned ETag (e.g. left after the github_issue row is deleted)
            // cannot linger forever; it is simply re-primed on the next full fetch.
            redisTemplate.opsForValue().set(issueEtagKey(owner, repo, number), etag, ISSUE_ETAG_TTL);
        }
    }

    /** (b) Removes the cached ETag for an issue, e.g. when its github_issue registry row is garbage-collected. */
    public void evictIssueEtag(String owner, String repo, int number) {
        redisTemplate.delete(issueEtagKey(owner, repo, number));
    }

    // ----------------------------------------------------------------------------------------------
    // Projects v2 board fieldOption sync (issue #1533, Feature 2). This service owns only the business
    // rules — which board/field/option, the maintainer gate-fieldOption anti-clobber guard, the project-refs
    // cache — and delegates every GitHub call to GitHubApiClient, so changing how we reach GitHub (GraphQL
    // today, possibly the REST Projects API later) never touches this service. Board writes use the ACTING
    // (connected) user's own GitHub token — the same token as comments — so the user needs the `project`
    // OAuth scope (requested at connect). Every step is best-effort and returns null/false on any failure
    // (user not connected, lacks project scope, org ACL, outage) — a board problem must never affect the
    // state transition that triggered it.
    // ----------------------------------------------------------------------------------------------

    /**
     * Moves the linked issue into the {@code desiredFieldOption} of the configured Projects v2 board using
     * {@code user}'s token, adding the issue to the board first if it is not on it yet. The card's
     * current fieldOption is read first: a maintainer gate fieldOption ({@link ProjectFieldOptions#isGateFieldOption}) is left
     * untouched. Best-effort — returns the fieldOption the card ended up in (the moved-to fieldOption, or the
     * preserved gate fieldOption), or {@code null} when nothing could be done (user not connected / lacks the
     * project scope, the fieldOption is not a board option, or any GitHub call failed).
     *
     * @param cachedMetadata the linked issue's cached metadata JSON; its {@code nodeId} is used when
     *                       present, otherwise the issue node id is resolved from GitHub
     */
    public String moveIssueToFieldOption(ScoreUser user, String owner, String repo, int number,
                                  String cachedMetadata, String desiredFieldOption) {
        return moveIssueToFieldOption(user, owner, repo, number, cachedMetadata, desiredFieldOption, false);
    }

    /**
     * As {@link #moveIssueToFieldOption(ScoreUser, String, String, int, String, String)}, but when
     * {@code force} is true the maintainer gate-fieldOption guard is bypassed (issue #1533, Feature 2): an
     * <em>explicit</em> user fieldOption override is honoured even if the card currently sits in a gate fieldOption
     * ("Member review"). Automatic, config-driven moves pass {@code force=false} and still preserve a
     * maintainer's review placement. The "already in the desired fieldOption" short-circuit is kept either way.
     */
    public String moveIssueToFieldOption(ScoreUser user, String owner, String repo, int number,
                                  String cachedMetadata, String desiredFieldOption, boolean force) {
        String token = getAccessToken(user);
        if (!hasText(token) || !hasText(desiredFieldOption)) {
            return null;
        }
        try {
            ProjectRefs refs = resolveProjectRefs(token);
            if (refs == null) {
                return null;
            }
            String desiredOptionId = refs.optionIdByName().get(desiredFieldOption);
            if (desiredOptionId == null) {
                logger.warn("GitHub project fieldOption '{}' is not an option of the '{}' field in {} project #{}.",
                        desiredFieldOption, refs.statusFieldName(),
                        properties.getProjectOwner(), properties.getProjectNumber());
                return null;
            }
            String issueNodeId = resolveIssueNodeId(token, owner, repo, number, cachedMetadata);
            if (!hasText(issueNodeId)) {
                return null;
            }

            GitHubApiClient.ProjectItem item = gitHubApiClient.findIssueProjectItem(token, issueNodeId,
                    refs.projectNodeId(), refs.statusFieldName());
            String itemId;
            String currentFieldOption;
            if (item == null) {
                itemId = gitHubApiClient.addProjectItem(token, refs.projectNodeId(), issueNodeId);
                currentFieldOption = null;
            } else {
                itemId = item.itemId();
                currentFieldOption = item.currentFieldOption();
            }
            if (!hasText(itemId)) {
                return null;
            }
            // Anti-clobber: never undo a maintainer's review decision — unless the user explicitly
            // forced this fieldOption (an override), which is a deliberate human choice.
            if (!force && projectFieldOptions.isGateFieldOption(currentFieldOption)) {
                return currentFieldOption;
            }
            if (desiredFieldOption.equals(currentFieldOption)) {
                return currentFieldOption;
            }
            boolean ok = gitHubApiClient.setProjectItemFieldOption(token, refs.projectNodeId(), itemId,
                    refs.statusFieldId(), desiredOptionId);
            return ok ? desiredFieldOption : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Removes the linked issue's card from the configured Projects v2 board using {@code user}'s token
     * (e.g. once the component is released). Best-effort — returns {@code true} if the card was removed,
     * {@code false} when the user is not connected / lacks the project scope, the issue is not on the
     * board (nothing to remove), or any GitHub call failed.
     */
    public boolean removeIssueFromProject(ScoreUser user, String owner, String repo, int number, String cachedMetadata) {
        String token = getAccessToken(user);
        if (!hasText(token)) {
            return false;
        }
        try {
            ProjectRefs refs = resolveProjectRefs(token);
            if (refs == null) {
                return false;
            }
            String issueNodeId = resolveIssueNodeId(token, owner, repo, number, cachedMetadata);
            if (!hasText(issueNodeId)) {
                return false;
            }
            GitHubApiClient.ProjectItem item = gitHubApiClient.findIssueProjectItem(token, issueNodeId,
                    refs.projectNodeId(), refs.statusFieldName());
            if (item == null || !hasText(item.itemId())) {
                return false;
            }
            return gitHubApiClient.deleteProjectItem(token, refs.projectNodeId(), item.itemId());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Places the linked issue in the initial fieldOption ({@link ProjectFieldOptions#getDefaultFieldOption}, e.g. "New") on
     * the configured board, adding it first if absent (issue #1533, Feature 2). Linking a component
     * starts tracking the issue, so it is reset to the initial fieldOption even when it is already on the board
     * at another fieldOption (e.g. left there by another linked component) — unless it sits in a maintainer gate
     * fieldOption, which is left untouched. Best-effort; returns the resulting fieldOption name or {@code null}. Used
     * when a GitHub issue is linked to a component, with the linking user's token.
     */
    public String addIssueToProjectOnLink(ScoreUser user, String owner, String repo, int number, String cachedMetadata) {
        return moveIssueToFieldOption(user, owner, repo, number, cachedMetadata, projectFieldOptions.getDefaultFieldOption());
    }

    /**
     * The board's auto-discovered fieldOption (single-select) field — its name and all of its DISTINCT option
     * names in board order — resolved with {@code user}'s token (issue #1533, Feature 2). (Projects v2
     * rarely allows two options with the same display name; such a duplicate collapses to one entry,
     * since the fieldOption is keyed by name throughout.) The state-change dialog
     * reads this to render the fieldOption-override dropdown. Best-effort: {@code null} when the user is not
     * connected / lacks the project scope, the project is not configured, or the field cannot be
     * resolved. Cached in-process for 30 min (see {@link #resolveProjectRefs}), so the dialog's call is
     * cheap after the first resolution.
     */
    public ProjectField getProjectField(ScoreUser user) {
        String token = getAccessToken(user);
        if (!hasText(token) || !properties.isProjectConfigured()) {
            return null;
        }
        ProjectRefs refs = resolveProjectRefs(token);
        if (refs == null) {
            return null;
        }
        List<FieldOption> options = new ArrayList<>();
        // optionColorByName preserves the board order (LinkedHashMap built alongside optionIdByName).
        refs.optionColorByName().forEach((name, color) -> options.add(new FieldOption(name, color)));
        return new ProjectField(refs.projectTitle(), refs.statusFieldName(), options);
    }

    /**
     * For the state-change dialog (issue #1533): only the GitHub-resource permission checks the dialog
     * needs for its warnings — per issue, {@code repoAccessible} (whether the user's token can see the
     * issue's repository, a prerequisite for posting a comment); board-wide, {@code projectAccessible}
     * (can READ the configured board) and {@code projectWritable} (best-effort whether they can WRITE it —
     * active org membership for an org board, needs the {@code read:org} scope; see {@link #canWriteProject}).
     *
     * <p>The per-issue CURRENT board fieldOption is deliberately NOT fetched here: resolving it pages each
     * issue's {@code projectItems} on GitHub and is slow, so the dialog shows only the DESTINATION fieldOption
     * (GitHub-sidebar style) and never blocks on the current one. All checks are best-effort and degrade
     * safely on any failure — they only drive warnings.</p>
     */
    public ProjectAccessStatus getProjectAccessStatus(ScoreUser user, List<IssueRef> issues) {
        String token = getAccessToken(user);
        boolean projectConfigured = properties.isProjectConfigured();
        List<IssueRepoAccess> items = new ArrayList<>();
        if (!hasText(token) || issues == null) {
            return new ProjectAccessStatus(projectConfigured, false, false, items);
        }
        boolean projectAccessible = projectConfigured && isProjectReadable(token);
        boolean projectWritable = projectAccessible && canWriteProject(token);
        for (IssueRef issue : issues) {
            if (issue == null) {
                continue;
            }
            items.add(new IssueRepoAccess(issue.owner(), issue.repo(), issue.number(),
                    gitHubApiClient.isRepoAccessible(token, issue.owner(), issue.repo())));
        }
        return new ProjectAccessStatus(projectConfigured, projectAccessible, projectWritable, items);
    }

    /**
     * Best-effort check of whether {@code token}'s user can WRITE the configured board. ProjectV2 exposes
     * no viewer-write flag, so for an ORG board this verifies active organization membership (needs the
     * {@code read:org} scope); a user board is assumed writable by its owner. Returns true when membership
     * cannot be determined (e.g. the token lacks {@code read:org}) so we never warn on a false negative —
     * the warning fires only when the user is DEFINITELY not a member (a 404 from the membership endpoint).
     */
    private boolean canWriteProject(String token) {
        if ("user".equalsIgnoreCase(properties.getProjectOwnerType())) {
            return true;
        }
        Boolean member = gitHubApiClient.orgMembershipActive(token, properties.getProjectOwner());
        return member == null || member;
    }

    /** Whether {@code token} can READ the configured project board. */
    private boolean isProjectReadable(String token) {
        return gitHubApiClient.isProjectReadable(token, properties.getProjectOwnerType(),
                properties.getProjectOwner(), properties.getProjectNumber());
    }

    /** The issue's GraphQL node id, from the cached metadata when present, else resolved from GitHub. */
    private String resolveIssueNodeId(String token, String owner, String repo, int number, String cachedMetadata) {
        String issueNodeId = nodeIdFromMetadata(cachedMetadata);
        return hasText(issueNodeId) ? issueNodeId : gitHubApiClient.resolveIssueNodeId(token, owner, repo, number);
    }

    /** Reads the issue's GraphQL node id out of the cached metadata JSON, or {@code null} if absent. */
    private String nodeIdFromMetadata(String cachedMetadata) {
        if (!hasText(cachedMetadata)) {
            return null;
        }
        try {
            String nodeId = objectMapper.readTree(cachedMetadata).path("nodeId").asText(null);
            return hasText(nodeId) ? nodeId : null;
        } catch (Exception e) {
            return null;
        }
    }

    /** Resolves the project node id, fieldOption field id/name, and fieldOption→option-id map, cached in-process. */
    private ProjectRefs resolveProjectRefs(String token) {
        CachedProjectRefs cached = projectRefsCache;
        long now = System.currentTimeMillis();
        if (cached != null && cached.expiresAt() > now
                && cached.ownerType().equals(properties.getProjectOwnerType())
                && cached.owner().equals(properties.getProjectOwner())
                && cached.number() == properties.getProjectNumber()) {
            return cached.refs();
        }
        ProjectRefs refs = fetchProjectRefs(token);
        if (refs != null) {
            projectRefsCache = new CachedProjectRefs(properties.getProjectOwnerType(),
                    properties.getProjectOwner(), properties.getProjectNumber(), refs,
                    now + PROJECT_REFS_TTL_MILLIS);
        }
        return refs;
    }

    /**
     * Resolves the project node id and the board's fieldOption field — the single-select field NAMED by
     * {@code score.integration.github.project-status-field-name} (default "Status"; change it to point
     * at a differently-named field). When no field matches that name it falls back to auto-discovery
     * (the single-select field whose options best cover {@link ProjectFieldOptions#fieldOptionNames()}). Returns null
     * if the project cannot be read or no usable field is found.
     */
    private ProjectRefs fetchProjectRefs(String token) {
        GitHubApiClient.ProjectData project = gitHubApiClient.fetchProject(token,
                properties.getProjectOwnerType(), properties.getProjectOwner(), properties.getProjectNumber());
        if (project == null || !hasText(project.projectNodeId())) {
            logger.warn("Could not resolve GitHub project #{} of {}.",
                    properties.getProjectNumber(), properties.getProjectOwner());
            return null;
        }
        String projectNodeId = project.projectNodeId();
        String projectTitle = project.title();
        // Select the fieldOption field by its CONFIGURED name (e.g. "Status"); change the config to use a
        // differently-named field. The overlap-with-our-fieldOption-names heuristic is kept only as a FALLBACK
        // for when no field matches the configured name (e.g. the board renamed the field).
        String configuredFieldName = properties.getProjectStatusFieldName();
        Set<String> wantedFieldOptions = projectFieldOptions.fieldOptionNames();
        String namedFieldId = null;
        String namedFieldName = null;
        Map<String, String> namedOptions = null;
        Map<String, String> namedColors = null;
        String bestFieldId = null;
        String bestFieldName = null;
        Map<String, String> bestOptions = null;
        Map<String, String> bestColors = null;
        int bestOverlap = 0;
        for (GitHubApiClient.SingleSelectField field : project.fields()) {
            String fieldId = field.id();
            String fieldName = field.name();
            if (!hasText(fieldId) || !hasText(fieldName) || field.options() == null) {
                continue;
            }
            // LinkedHashMap so getProjectField() can expose the options in the board's own order.
            Map<String, String> optionIdByName = new LinkedHashMap<>();
            // Parallel name -> GitHub color enum (GRAY/BLUE/.../PURPLE), kept in the same board order, so
            // the dialog can render each fieldOption in its board color (issue #1533). May hold a null
            // when an option carries no color.
            Map<String, String> optionColorByName = new LinkedHashMap<>();
            int overlap = 0;
            for (GitHubApiClient.SingleSelectOption option : field.options()) {
                String name = option.name();
                String id = option.id();
                if (hasText(name) && hasText(id)) {
                    optionIdByName.put(name, id);
                    optionColorByName.put(name, option.color());
                    if (wantedFieldOptions.contains(name)) {
                        overlap++;
                    }
                }
            }
            if (hasText(configuredFieldName) && configuredFieldName.equalsIgnoreCase(fieldName)) {
                namedFieldId = fieldId;
                namedFieldName = fieldName;
                namedOptions = optionIdByName;
                namedColors = optionColorByName;
            }
            if (overlap > bestOverlap) {
                bestOverlap = overlap;
                bestFieldId = fieldId;
                bestFieldName = fieldName;
                bestOptions = optionIdByName;
                bestColors = optionColorByName;
            }
        }
        if (namedFieldId != null) {
            return new ProjectRefs(projectNodeId, projectTitle, namedFieldId, namedFieldName, namedOptions, namedColors);
        }
        if (bestFieldId != null) {
            logger.warn("GitHub project #{} of {} has no single-select field named '{}'; fell back to the " +
                            "overlap-discovered field '{}'. Set score.integration.github.project-status-field-name.",
                    properties.getProjectNumber(), properties.getProjectOwner(), configuredFieldName, bestFieldName);
            return new ProjectRefs(projectNodeId, projectTitle, bestFieldId, bestFieldName, bestOptions, bestColors);
        }
        logger.warn("GitHub project #{} of {} has no single-select field named '{}', nor one whose options " +
                        "include the fieldOptionByState {}.",
                properties.getProjectNumber(), properties.getProjectOwner(), configuredFieldName, wantedFieldOptions);
        return null;
    }

    /**
     * Resolved project coordinates: project node id + title, the fieldOption field id + name, and the
     * fieldOption name → option id / fieldOption name → GitHub color maps (both in board order).
     */
    private record ProjectRefs(String projectNodeId, String projectTitle, String statusFieldId,
                               String statusFieldName, Map<String, String> optionIdByName,
                               Map<String, String> optionColorByName) {
    }

    /** One option of the board's fieldOption field: its name + GitHub color enum (may be {@code null}). */
    public record FieldOption(String name, String color) {
    }

    /**
     * The board's fieldOption (single-select) field for the dialog (issue #1533): the owning project's
     * title, the field's name, and all of its options (name + GitHub color) in board order.
     */
    public record ProjectField(String projectTitle, String name, List<FieldOption> options) {
    }

    /** A GitHub issue's coordinates, for {@link #getProjectAccessStatus}. */
    public record IssueRef(String owner, String repo, int number) {
    }

    /** Whether one issue's repository is accessible to the connected user — a prerequisite for posting a
     *  comment (issue #1533). The issue's CURRENT board fieldOption is intentionally NOT fetched here (it
     *  would page the issue's projectItems on GitHub and is slow — see {@link #getProjectAccessStatus}). */
    public record IssueRepoAccess(String owner, String repo, int number, boolean repoAccessible) {
    }

    /**
     * The connected user's access to the configured project board, for a set of issues (issue #1533) —
     * what the state-change dialog needs for its permission warnings, NOT the board's contents.
     * {@code projectAccessible} = the user can READ the configured board; {@code projectWritable} =
     * best-effort whether they can WRITE it (active org membership for an org board) — used to warn when a
     * fieldOption move would not be applied. {@code items} carries each issue's repository accessibility.
     */
    public record ProjectAccessStatus(boolean projectConfigured, boolean projectAccessible,
                                      boolean projectWritable, List<IssueRepoAccess> items) {
    }

    /** A {@link ProjectRefs} cached with the project coordinates it was resolved for and an expiry. The
     *  owner TYPE (org vs user) is part of the key: a board can switch between an org and a user board
     *  via {@code project-url} while keeping the same owner login + number, which resolves differently. */
    private record CachedProjectRefs(String ownerType, String owner, int number, ProjectRefs refs, long expiresAt) {
    }
}
