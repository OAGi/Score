package org.oagi.score.gateway.http.api.integration_management.github.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.integration_management.github.handler.GitHubWebhookEventDispatcher;
import org.oagi.score.gateway.http.api.integration_management.github.service.GitHubIntegrationService;
import org.oagi.score.gateway.http.api.integration_management.github.service.GitHubIssueLinkService;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.springframework.util.StringUtils.hasText;

/**
 * Per-user GitHub connection endpoints (issue #1533).
 * Reached by the SPA as /api/integration/github/* (the dev proxy / gateway strips the /api prefix).
 * The /callback endpoint is permitAll (see SecurityConfiguration) and protected by the OAuth state token.
 */
@RestController
@Tag(name = "Integration - GitHub", description = "Per-user GitHub account connection")
@RequestMapping("/integration/github")
public class GitHubIntegrationController {

    /** Component types for which GitHub issue linking is implemented. */
    private static final Set<CcType> SUPPORTED_TYPES = EnumSet.of(
            CcType.ACC, CcType.ASCCP, CcType.BCCP, CcType.DT, CcType.CODE_LIST, CcType.AGENCY_ID_LIST);

    @Autowired
    private GitHubIntegrationService service;

    @Autowired
    private GitHubIssueLinkService issueLinkService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private GitHubWebhookEventDispatcher webhookEventDispatcher;

    @Autowired
    private ObjectMapper objectMapper;

    public record GitHubStatusResponse(boolean enabled, boolean connected, String login) {
    }

    public record LinkIssueRequest(String ccType, BigInteger manifestId, Integer issueNumber,
                                   String repoOwner, String repoName) {
    }

    public record IssueLookupRequestItem(String ccType, BigInteger manifestId) {
    }

    public record IssueLookupResponseItem(String ccType, BigInteger manifestId,
                                          List<GitHubIssueLinkService.LinkedIssue> issues) {
    }

    /** Parses a request component type into a supported {@link CcType}; the type is required. */
    private static CcType ccType(String type) {
        if (!hasText(type)) {
            throw new IllegalArgumentException("A component type (ccType) is required.");
        }
        CcType ccType;
        try {
            ccType = CcType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            ccType = null;
        }
        if (ccType == null || !SUPPORTED_TYPES.contains(ccType)) {
            // IllegalArgumentException so a bad client-supplied type maps to 400, not a 500.
            throw new IllegalArgumentException("Unsupported component type for GitHub issue linking: " + type);
        }
        return ccType;
    }

    /** Casts a raw manifest id to the typed {@link ManifestId} matching the component type. */
    private static ManifestId manifestId(CcType ccType, BigInteger value) {
        if (value == null) {
            throw new IllegalArgumentException("A manifestId is required.");
        }
        return switch (ccType) {
            case ACC -> new AccManifestId(value);
            case ASCCP -> new AsccpManifestId(value);
            case BCCP -> new BccpManifestId(value);
            case DT -> new DtManifestId(value);
            case CODE_LIST -> new CodeListManifestId(value);
            case AGENCY_ID_LIST -> new AgencyIdListManifestId(value);
            default -> throw new UnsupportedOperationException(
                    "Unsupported component type for GitHub issue linking: " + ccType);
        };
    }

    /** Whether the integration is available and whether the current user is connected. */
    @GetMapping("/status")
    public GitHubStatusResponse status(@AuthenticationPrincipal AuthenticatedPrincipal user) {
        ScoreUser requester = sessionService.asScoreUser(user);
        if (!service.isConfigured()) {
            return new GitHubStatusResponse(false, false, null);
        }
        boolean connected = service.isConnected(requester);
        return new GitHubStatusResponse(true, connected, connected ? service.getLogin(requester) : null);
    }

    /** Starts the OAuth authorization-code flow (full-page redirect to GitHub). */
    @GetMapping("/connect")
    public void connect(@AuthenticationPrincipal AuthenticatedPrincipal user,
                        @RequestParam(name = "returnUrl", required = false) String returnUrl,
                        HttpServletResponse response) throws IOException {
        ScoreUser requester = sessionService.asScoreUser(user);
        if (!service.isConfigured()) {
            response.sendRedirect(service.getWebBaseUrl());
            return;
        }
        response.sendRedirect(service.beginConnect(requester, returnUrl));
    }

    /** OAuth callback: exchanges the code for a token, persists it, and returns the user to the SPA. */
    @GetMapping("/callback")
    public void callback(@RequestParam(name = "code", required = false) String code,
                         @RequestParam(name = "state", required = false) String state,
                         HttpServletResponse response) throws IOException {
        String returnUrl = null;
        if (hasText(code) && hasText(state)) {
            returnUrl = service.completeCallback(code, state);
        }
        response.sendRedirect(hasText(returnUrl) ? returnUrl : service.getWebBaseUrl());
    }

    /** Disconnects the current user's GitHub account. */
    @DeleteMapping("/connection")
    public void disconnect(@AuthenticationPrincipal AuthenticatedPrincipal user) {
        service.disconnect(sessionService.asScoreUser(user));
    }

    /**
     * Lists GitHub issues linked to a component (ACC/ASCCP/BCCP/DT/code list/agency ID list), refreshed
     * from GitHub on the fly using a per-issue conditional request (ETag / If-None-Match) so the response
     * reflects the latest title/state/label/... without a page reload. Unchanged issues return 304 (cheap
     * and rate-limit-free), so this stays fast; if the viewer is not connected to GitHub the cached values
     * are returned. (The webhook also keeps the cache fresh for everyone.)
     */
    @GetMapping("/issues")
    public List<GitHubIssueLinkService.LinkedIssue> listIssues(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam(name = "ccType") String ccType,
            @RequestParam(name = "manifestId") BigInteger manifestId) {
        ScoreUser requester = sessionService.asScoreUser(user);
        CcType type = ccType(ccType);
        ManifestId mid = manifestId(type, manifestId);
        return issueLinkService.refreshFor(requester, type, mid);
    }

    /**
     * Batch lookup of the GitHub issues linked to each given component, for the state-change
     * confirmation dialog (issue #1533, sub-task 5). Open to any authenticated user — no owner or
     * WIP requirement, since anyone allowed to change a state may see what would be posted — and
     * strictly cache-only: the linked-issue metadata is read from the DB as-is, never refreshed
     * from the GitHub API, so a many-component dialog opens fast and burns no rate limit. Each
     * response item echoes its request coordinates and carries the same {@code LinkedIssue} shape
     * as {@code GET /issues}. Item failures are per-item — one unresolvable entry (unsupported
     * type, missing id) answers with an empty issue list instead of failing the whole batch and
     * hiding every other target's issues.
     */
    @PostMapping("/issues/lookup")
    public List<IssueLookupResponseItem> lookupIssues(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody(required = false) List<IssueLookupRequestItem> request) {
        if (request == null || request.isEmpty()) {
            return List.of();
        }
        ScoreUser requester = sessionService.asScoreUser(user);
        List<IssueLookupResponseItem> response = new ArrayList<>(request.size());
        for (IssueLookupRequestItem item : request) {
            if (item == null) {
                continue;
            }
            List<GitHubIssueLinkService.LinkedIssue> issues;
            try {
                CcType type = ccType(item.ccType());
                issues = issueLinkService.listFor(requester, type, manifestId(type, item.manifestId()));
            } catch (IllegalArgumentException e) {
                issues = List.of();
            }
            response.add(new IssueLookupResponseItem(item.ccType(), item.manifestId(), issues));
        }
        return response;
    }

    /** Links a GitHub issue to a component (owner-only, WIP-only). */
    @PostMapping("/issues")
    public List<GitHubIssueLinkService.LinkedIssue> linkIssue(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody LinkIssueRequest request) {
        ScoreUser requester = sessionService.asScoreUser(user);
        CcType type = ccType(request.ccType());
        ManifestId mid = manifestId(type, request.manifestId());
        issueLinkService.link(requester, type, mid,
                request.repoOwner(), request.repoName(), request.issueNumber());
        return issueLinkService.refreshFor(requester, type, mid);
    }

    /** Removes a GitHub issue link (owner-only, WIP-only). */
    @DeleteMapping("/issues/{linkId:[\\d]+}")
    public void unlinkIssue(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("linkId") BigInteger linkId,
            @RequestParam(name = "ccType") String ccType) {
        ScoreUser requester = sessionService.asScoreUser(user);
        issueLinkService.unlink(requester, ccType(ccType), linkId);
    }

    /**
     * Inbound GitHub webhook (permitAll; protected by HMAC-SHA256 of the raw body against the configured
     * webhook secret). Verifies the signature, then hands the event to {@link GitHubWebhookEventDispatcher}
     * which routes it to the matching handler(s) — {@code issues} events are processed asynchronously via
     * Redis pub/sub. Always acknowledges with 200 (GitHub does not retry failures); processing is
     * best-effort and the view-time refresh is the reconciliation safety net.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestHeader(name = "X-Hub-Signature-256", required = false) String signature,
            @RequestHeader(name = "X-GitHub-Event", required = false) String event,
            @RequestBody(required = false) byte[] body) {
        if (body == null || !service.verifyWebhookSignature(body, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            webhookEventDispatcher.dispatch(event, objectMapper.readTree(body));
        } catch (Exception ignored) {
            // Malformed payload: acknowledge so GitHub does not retry; nothing to process.
        }
        return ResponseEntity.ok().build();
    }
}
