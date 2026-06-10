package org.oagi.score.gateway.http.api.integration_management.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.oagi.score.gateway.http.api.integration_management.config.GitHubIntegrationProperties;
import org.oagi.score.gateway.http.api.integration_management.model.IssueFetchResult;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Per-user GitHub connection: OAuth authorization-code flow + token storage in Redis.
 * Issue #1533. The access token and connection metadata live ONLY in Redis (never in the DB),
 * keyed by the Score user id. A short-lived state -> {userId, returnUrl} mapping protects the
 * callback (CSRF) and carries the user across the redirect so the callback need not be authenticated.
 */
@Service
public class GitHubIntegrationService {

    private static final String NS = "score:integration:github:";
    private static final String TOKEN_KEY = NS + "token:";
    private static final String LOGIN_KEY = NS + "login:";
    private static final String STATE_KEY = NS + "state:";
    private static final String CONNECTED_USERS = NS + "connected-users";
    /** Cached ETag of an issue's last fetched representation, keyed by repo coordinates (resource-global). */
    private static final String ISSUE_ETAG_KEY = NS + "issue-etag:";
    /** Cached issue ETags expire after this idle period so orphaned entries cannot accumulate. */
    private static final Duration ISSUE_ETAG_TTL = Duration.ofDays(30);

    @Autowired
    private GitHubIntegrationProperties properties;

    @Autowired
    @Qualifier("gitHubRestTemplate")
    private RestTemplate restTemplate;

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
                .queryParam("scope", properties.getScope())
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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        form.add("code", code);
        form.add("redirect_uri", properties.getRedirectUri());
        form.add("state", state);

        Map<?, ?> tokenResp;
        try {
            tokenResp = restTemplate.postForObject(properties.getTokenUri(),
                    new HttpEntity<>(form, headers), Map.class);
        } catch (Exception e) {
            return null;
        }
        if (tokenResp == null || tokenResp.get("access_token") == null) {
            return null;
        }
        String accessToken = tokenResp.get("access_token").toString();
        String login = fetchLogin(accessToken);

        redisTemplate.opsForValue().set(TOKEN_KEY + userId, accessToken);
        if (login != null) {
            redisTemplate.opsForValue().set(LOGIN_KEY + userId, login);
        }
        redisTemplate.opsForSet().add(CONNECTED_USERS, userId);
        return returnUrl;
    }

    private String fetchLogin(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setAccept(List.of(MediaType.parseMediaType("application/vnd.github+json")));
            ResponseEntity<Map> resp = restTemplate.exchange(properties.getApiBaseUrl() + "/user",
                    HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            Object login = (resp.getBody() == null) ? null : resp.getBody().get("login");
            return (login == null) ? null : login.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public void disconnect(ScoreUser user) {
        String userId = String.valueOf(user.userId());
        redisTemplate.delete(TOKEN_KEY + userId);
        redisTemplate.delete(LOGIN_KEY + userId);
        redisTemplate.opsForSet().remove(CONNECTED_USERS, userId);
    }

    public String getDefaultRepoOwner() {
        return properties.getDefaultRepoOwner();
    }

    public String getDefaultRepoName() {
        return properties.getDefaultRepoName();
    }

    /**
     * Best-effort fetch of a single issue's metadata (title/state/html_url/node_id) using the
     * given user's token. Returns {@code null} if the user is not connected or the call fails.
     */
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

    public JsonNode fetchIssue(ScoreUser user, String owner, String repo, int number) {
        String token = getAccessToken(user);
        if (token == null) {
            return null;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(List.of(MediaType.parseMediaType("application/vnd.github+json")));
            ResponseEntity<JsonNode> resp = restTemplate.exchange(issueUrl(owner, repo, number),
                    HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
            JsonNode body = resp.getBody();
            // Prime the cached ETag only when we actually got a body, so an empty response can't pin a
            // stale cache via a future 304.
            if (body != null && !body.isNull()) {
                storeIssueEtag(owner, repo, number, resp.getHeaders().getETag());
            }
            return body;
        } catch (Exception e) {
            return null;
        }
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
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(List.of(MediaType.parseMediaType("application/vnd.github+json")));
            String etag = getIssueEtag(owner, repo, number);
            if (etag != null) {
                headers.setIfNoneMatch(etag);
            }
            ResponseEntity<byte[]> resp = restTemplate.exchange(issueUrl(owner, repo, number),
                    HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
            if (resp.getStatusCode().value() == HttpStatus.NOT_MODIFIED.value()) {
                return IssueFetchResult.notModified();
            }
            byte[] body = resp.getBody();
            if (body == null || body.length == 0) {
                return IssueFetchResult.unavailable();
            }
            JsonNode issue = objectMapper.readTree(body);
            // Store the ETag only once we actually have (and parsed) the body, so a truncated/empty 200
            // can't pin a stale cache via a future 304.
            storeIssueEtag(owner, repo, number, resp.getHeaders().getETag());
            return IssueFetchResult.modified(issue);
        } catch (HttpStatusCodeException e) {
            // Some clients surface 304 as an exception; treat it as not-modified, everything else as a miss.
            return (e.getStatusCode().value() == HttpStatus.NOT_MODIFIED.value())
                    ? IssueFetchResult.notModified() : IssueFetchResult.unavailable();
        } catch (Exception e) {
            return IssueFetchResult.unavailable();
        }
    }

    private String issueUrl(String owner, String repo, int number) {
        return properties.getApiBaseUrl() + "/repos/" + owner + "/" + repo + "/issues/" + number;
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
}
