package org.oagi.score.gateway.http.api.integration_management.github.client;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * The boundary between {@link org.oagi.score.gateway.http.api.integration_management.github.service.GitHubIntegrationService
 * GitHubIntegrationService} and GitHub's HTTP/GraphQL APIs (issue #1533). Every outbound call to GitHub
 * goes through this port, expressed in GitHub-domain terms (issues, comments, the Projects v2 board) and
 * returning plain DTOs — never an {@code HttpHeaders}, a GraphQL query string, or a {@code RestTemplate}.
 *
 * <p>The service owns the business rules (which board/field/option, caching, the maintainer gate-option
 * anti-clobber guard, OAuth state, token storage); this port owns <em>how</em> GitHub is reached. As a
 * result the transport can change — GraphQL today, possibly the REST Projects API later (GA since
 * 2025-09) — by swapping only the implementation, with no edit to the service.</p>
 *
 * <p>Every method is best-effort and must never throw: a GitHub outage returns {@code null}/{@code false}
 * so a board or comment problem can never affect the state transition that triggered it.</p>
 */
public interface GitHubApiClient {

    // --- OAuth / identity -------------------------------------------------------------------------

    /**
     * Exchanges an OAuth authorization {@code code} (with its {@code state}) for an access token via the
     * configured token endpoint, using the configured client credentials and redirect URI. Returns the
     * token and the comma-separated scopes GitHub granted, or {@code null} if the exchange failed or
     * returned no access token.
     */
    OAuthToken exchangeOAuthCode(String code, String state);

    /** The GitHub login of the user owning {@code accessToken} ({@code GET /user}), or {@code null}. */
    String fetchLogin(String accessToken);

    // --- Issues (REST) ----------------------------------------------------------------------------

    /**
     * Fetches a single issue ({@code GET /repos/{owner}/{repo}/issues/{number}}) with {@code token}.
     * Returns the issue body together with its response {@code ETag} (for conditional re-fetches), or
     * {@code null} if the call failed.
     */
    FetchedIssue fetchIssue(String token, String owner, String repo, int number);

    /**
     * Conditionally fetches an issue using {@code etag} ({@code If-None-Match}); GitHub returns
     * {@code 304 Not Modified} when unchanged (cheap, no rate-limit cost for authenticated requests).
     * Returns {@link ConditionalIssue#notModified()} on 304, {@link ConditionalIssue#modified} (with the
     * fresh body and new ETag) on 200, and {@link ConditionalIssue#unavailable()} on any failure.
     */
    ConditionalIssue fetchIssueConditional(String token, String owner, String repo, int number, String etag);

    /**
     * Posts a comment on an issue with {@code token}. Returns the created comment's {@code html_url}, or
     * {@code null} if the call failed.
     */
    String postIssueComment(String token, String owner, String repo, int number, String body);

    /** Whether {@code token} can see {@code owner/repo} (a 2xx on {@code GET /repos/...}); false otherwise. */
    boolean isRepoAccessible(String token, String owner, String repo);

    /**
     * Whether {@code token}'s user is an active member of {@code org}: {@code TRUE}/{@code FALSE} from
     * {@code GET /user/memberships/orgs/{org}} (200 active vs 404 not-a-member), or {@code null} when it
     * cannot be determined (a 403 — the token lacks {@code read:org} — or any other error).
     */
    Boolean orgMembershipActive(String token, String org);

    // --- Projects v2 board ------------------------------------------------------------------------

    /** Whether {@code token} can READ the project board identified by owner type/login/number. */
    boolean isProjectReadable(String token, String ownerType, String owner, int number);

    /**
     * Resolves the project (its node id + title) and all of its single-select fields with their options,
     * in board order, or {@code null} if the project cannot be read. The caller selects the relevant
     * field; this method does not interpret which field is the "Status" field.
     */
    ProjectData fetchProject(String token, String ownerType, String owner, int number);

    /** The GraphQL node id of an issue, or {@code null} if it cannot be resolved. */
    String resolveIssueNodeId(String token, String owner, String repo, int number);

    /**
     * Finds the issue's card on the project identified by {@code projectNodeId} (with its current option
     * for the field named {@code fieldName}), paging the issue's project memberships, or {@code null} if
     * the issue is not on that board.
     */
    ProjectItem findIssueProjectItem(String token, String issueNodeId, String projectNodeId, String fieldName);

    /** Adds the issue to the board and returns the new item id, or {@code null} on failure. */
    String addProjectItem(String token, String projectNodeId, String issueNodeId);

    /** Sets the item's single-select field to {@code optionId} (the column move). Returns success. */
    boolean setProjectItemFieldOption(String token, String projectNodeId, String itemId,
                                      String fieldId, String optionId);

    /** Removes the item (card) from the board. Returns success. */
    boolean deleteProjectItem(String token, String projectNodeId, String itemId);

    // --- DTOs -------------------------------------------------------------------------------------

    /** An OAuth access token and the comma-separated scopes GitHub granted (may be {@code null}). */
    record OAuthToken(String accessToken, String scope) {
    }

    /** A fetched issue body together with the response {@code ETag} (may be {@code null}). */
    record FetchedIssue(JsonNode body, String etag) {
    }

    /**
     * The outcome of a conditional issue fetch: {@code MODIFIED} carries the fresh body and the new
     * {@code ETag}; {@code NOT_MODIFIED} and {@code UNAVAILABLE} carry neither.
     */
    record ConditionalIssue(Status status, JsonNode body, String etag) {

        public enum Status {MODIFIED, NOT_MODIFIED, UNAVAILABLE}

        public static ConditionalIssue modified(JsonNode body, String etag) {
            return new ConditionalIssue(Status.MODIFIED, body, etag);
        }

        public static ConditionalIssue notModified() {
            return new ConditionalIssue(Status.NOT_MODIFIED, null, null);
        }

        public static ConditionalIssue unavailable() {
            return new ConditionalIssue(Status.UNAVAILABLE, null, null);
        }
    }

    /** A project: its node id, title, and single-select fields (in board order). */
    record ProjectData(String projectNodeId, String title, List<SingleSelectField> fields) {
    }

    /** A single-select project field: its id, name, and options (in board order). */
    record SingleSelectField(String id, String name, List<SingleSelectOption> options) {
    }

    /** One option of a single-select field: its id, name, and GitHub color enum (may be {@code null}). */
    record SingleSelectOption(String id, String name, String color) {
    }

    /** A project item (card): its id and the name of its current field option (may be {@code null}). */
    record ProjectItem(String itemId, String currentFieldOption) {
    }
}
