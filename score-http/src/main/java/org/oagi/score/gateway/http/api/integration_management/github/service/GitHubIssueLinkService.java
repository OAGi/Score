package org.oagi.score.gateway.http.api.integration_management.github.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.integration_management.github.config.GitHubIntegrationProperties;
import org.oagi.score.gateway.http.api.integration_management.github.model.ComponentOwnerState;
import org.oagi.score.gateway.http.api.integration_management.github.model.GitHubIssueAccManifestId;
import org.oagi.score.gateway.http.api.integration_management.github.model.GitHubIssueAgencyIdListManifestId;
import org.oagi.score.gateway.http.api.integration_management.github.model.GitHubIssueAsccpManifestId;
import org.oagi.score.gateway.http.api.integration_management.github.model.GitHubIssueBccpManifestId;
import org.oagi.score.gateway.http.api.integration_management.github.model.GitHubIssueCodeListManifestId;
import org.oagi.score.gateway.http.api.integration_management.github.model.GitHubIssueDtManifestId;
import org.oagi.score.gateway.http.api.integration_management.github.model.GitHubIssueId;
import org.oagi.score.gateway.http.api.integration_management.github.model.GitHubIssueLinkRecord;
import org.oagi.score.gateway.http.api.integration_management.github.model.IssueFetchResult;
import org.oagi.score.gateway.http.api.integration_management.github.repository.GitHubIssueLinkCommandRepository;
import org.oagi.score.gateway.http.api.integration_management.github.repository.GitHubIssueLinkQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

/**
 * Links GitHub issues to Core Components (issue #1533). A single {@code github_issue} registry holds
 * the cached issue metadata; a per-type link table ({@code github_issue_<type>_manifest}) maps it to a
 * component manifest, so the same issue can be linked to any number of ACC/ASCCP/BCCP/DT/code-list/
 * agency-id-list components. Only the component owner may link/unlink, and only while the component is in WIP.
 *
 * <p>Cached GitHub metadata (title/state/type/milestone/labels/assignees/url) is stored as a single
 * JSON document in {@code github_issue.cached_metadata} and refreshed on view (conditional GET) or via webhook.</p>
 *
 * <p>This service holds the GitHub orchestration, authorization and the {@code cached_metadata} JSON
 * marshalling, and acts as the dispatch layer between the generic HTTP API ({@link CcType} + raw ids) and
 * the per-type repository methods: all database access is delegated to {@link GitHubIssueLinkQueryRepository} /
 * {@link GitHubIssueLinkCommandRepository}, so no jOOQ types leak into this layer. The {@code switch} on
 * {@link CcType} below is the single place that maps a component type to its typed manifest / link id.</p>
 */
@Service
public class GitHubIssueLinkService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private GitHubIntegrationProperties properties;

    @Autowired
    private GitHubIntegrationService integrationService;

    @Autowired
    private ObjectMapper objectMapper;

    /** Self proxy, so {@link #link} can invoke the transactional {@link #doLink} through the Spring proxy
     * (a same-class call would bypass the proxy and silently skip the transaction). */
    @Autowired
    @Lazy
    private GitHubIssueLinkService self;

    public record Milestone(String title, String state) {
    }

    public record Label(String name, String color, String description) {
    }

    public record Assignee(String login, String htmlUrl, String avatarUrl) {
    }

    public record LinkedIssue(String linkId, String repoOwner, String repoName, int issueNumber,
                              String htmlUrl, String title, String state, String type,
                              Milestone milestone, List<Label> labels, List<Assignee> assignees) {
    }

    /** Rejects unauthenticated callers — every user-facing operation requires a resolved user. */
    private void assertAuthenticated(ScoreUser requester) {
        if (requester == null || requester.userId() == null) {
            throw new AccessDeniedException("Authentication is required to access GitHub issue links.");
        }
    }

    private GitHubIssueLinkQueryRepository query(ScoreUser requester) {
        return repositoryFactory.gitHubIssueLinkQueryRepository(requester);
    }

    private GitHubIssueLinkCommandRepository command(ScoreUser requester) {
        return repositoryFactory.gitHubIssueLinkCommandRepository(requester);
    }

    // ----- Read -----

    /**
     * Reads the linked GitHub issues from the DB cache only — never calls the GitHub API. Used by
     * {@link #refreshFor} as the fallback when the viewer is not connected to GitHub, and directly
     * by the batch lookup behind the state-change dialog (issue #1533, sub-task 5), where one
     * dialog may ask for many components at once and a per-issue GitHub round-trip would be both
     * slow and rate-limit-hostile.
     */
    public List<LinkedIssue> listFor(ScoreUser requester, CcType ccType, ManifestId manifestId) {
        assertAuthenticated(requester);
        List<LinkedIssue> result = new ArrayList<>();
        for (GitHubIssueLinkRecord row : getLinkedIssues(query(requester), ccType, manifestId.value())) {
            result.add(toLinkedIssue(row, row.cachedMetadata()));
        }
        return result;
    }

    /**
     * Refreshes each linked issue's cached metadata from GitHub using the viewer's token, then returns the
     * resulting list. Uses a conditional request (ETag / {@code If-None-Match}) per issue: an unchanged
     * issue returns {@code 304 Not Modified} — cheap and, for authenticated requests, rate-limit-free — so
     * this is fast enough to run synchronously on view, and the DB cache is written only when the issue
     * actually changed. Best-effort and per-issue: a {@code 304}, a failed/timed-out call, or a
     * not-connected viewer leaves that issue's cache as-is.
     */
    public List<LinkedIssue> refreshFor(ScoreUser requester, CcType ccType, ManifestId manifestId) {
        assertAuthenticated(requester);
        if (!integrationService.isConnected(requester)) {
            return listFor(requester, ccType, manifestId);
        }
        List<GitHubIssueLinkRecord> rows = getLinkedIssues(query(requester), ccType, manifestId.value());

        List<LinkedIssue> result = new ArrayList<>();
        for (GitHubIssueLinkRecord row : rows) {
            String metadata = row.cachedMetadata();
            IssueFetchResult fetched = integrationService.fetchIssueIfModified(
                    requester, row.repoOwner(), row.repoName(), row.issueNumber());
            if (fetched.isModified()) {
                String fresh = buildMetadataJson(fetched.issue());
                if (fresh != null && !fresh.equals(metadata)) {
                    command(requester).updateIssueCache(row.issueId(), fresh);
                    metadata = fresh;
                }
            }
            result.add(toLinkedIssue(row, metadata));
        }
        return result;
    }

    /**
     * The linked-issue references of a component, read from the DB only — no GitHub calls and no
     * metadata parsing. This is the fan-out list for the status post made when a component state
     * change event arrives (issue #1533, sub-task 4).
     */
    public List<GitHubIssueLinkRecord> getLinkedIssueRefs(ScoreUser requester, CcType ccType, ManifestId manifestId) {
        assertAuthenticated(requester);
        return getLinkedIssues(query(requester), ccType, manifestId.value());
    }

    // ----- Write -----

    public void link(ScoreUser requester, CcType ccType, ManifestId manifestId,
                     String repoOwner, String repoName, int issueNumber) {
        assertAuthenticated(requester);
        String owner = hasText(repoOwner) ? repoOwner : properties.getDefaultRepoOwner();
        String repo = hasText(repoName) ? repoName : properties.getDefaultRepoName();
        if (!hasText(owner) || !hasText(repo) || issueNumber <= 0) {
            throw new IllegalArgumentException("A repository and a positive issue number are required.");
        }
        assertOwnerInWip(requester, ccType, manifestId);

        // Fetch the issue metadata from GitHub OUTSIDE the transaction, so no DB connection is held during
        // the (potentially slow) HTTP call. The DB writes then run atomically in doLink, invoked through the
        // self proxy so its @Transactional actually applies (a same-class call would bypass it).
        String metadata = buildMetadataJson(integrationService.fetchIssue(requester, owner, repo, issueNumber));
        self.doLink(requester, ccType, manifestId, owner, repo, issueNumber, metadata);
    }

    /**
     * Transactional, DB-only part of {@link #link}: registers/refreshes the {@code github_issue} row and
     * inserts the link in one transaction, so a partial failure leaves no orphaned registry row. Public
     * only so the Spring proxy can apply {@code @Transactional} — call {@link #link} instead.
     */
    @Transactional
    public void doLink(ScoreUser requester, CcType ccType, ManifestId manifestId,
                       String owner, String repo, int issueNumber, String metadata) {
        GitHubIssueId githubIssueId = upsertIssue(requester, owner, repo, issueNumber, metadata);

        // Fast-path: skip the insert (and an auto-increment burn) when already linked, checked in the same
        // transaction. With the idempotent upsert removed, a genuinely concurrent first-time link of the
        // same (manifest_id, github_issue_id) can still race past this check; the unique key then makes the
        // losing insert fail and roll its own transaction back (a rare 500) rather than creating a duplicate.
        if (isLinked(query(requester), ccType, manifestId, githubIssueId)) {
            return;
        }
        createIssueLink(command(requester), ccType, manifestId, githubIssueId);
    }

    /** Transactional so the link removal and the orphan GC commit together (no half-deleted state). */
    @Transactional
    public void unlink(ScoreUser requester, CcType ccType, BigInteger linkId) {
        assertAuthenticated(requester);
        GitHubIssueLinkQueryRepository query = query(requester);
        ComponentOwnerState ownerState = getComponentOwnerStateByLink(query, ccType, linkId);
        if (ownerState == null) {
            return;
        }
        assertOwnerInWip(requester, ownerState);

        // Capture the linked issue before removing the link, then garbage-collect the github_issue
        // registry row (and evict its cached ETag) if this was the last component referencing it (#1533).
        GitHubIssueLinkRecord linked = getLinkedIssue(query, ccType, linkId);
        deleteIssueLink(command(requester), ccType, linkId);
        if (linked != null && command(requester).deleteIssueIfOrphaned(linked.issueId())) {
            integrationService.evictIssueEtag(linked.repoOwner(), linked.repoName(), linked.issueNumber());
        }
    }

    /**
     * Push-updates the cached metadata of a tracked issue from a webhook event (existing rows only).
     * This is a system path authenticated by the webhook HMAC signature (verified in the controller),
     * not a user request, so it carries no {@link ScoreUser}.
     */
    public void updateIssueCacheFromGitHub(String owner, String repo, int number, JsonNode issue) {
        if (owner == null || repo == null || number <= 0) {
            return;
        }
        String metadata = buildMetadataJson(issue);
        if (metadata == null) {
            return;
        }
        command(null).updateIssueCache(owner, repo, number, metadata);
    }

    /**
     * Ensures a {@code github_issue} registry row exists for the given coordinates (using the already-fetched
     * {@code metadata}, so this makes no GitHub call) and returns its id. Inserts a new row when absent.
     */
    private GitHubIssueId upsertIssue(ScoreUser requester, String owner, String repo, int number, String metadata) {
        GitHubIssueId existing = query(requester).findIssueId(owner, repo, number);

        if (existing != null) {
            if (metadata != null) {
                command(requester).updateIssueCache(existing, metadata);
            }
            return existing;
        }
        // No registry row yet: insert one and return its generated id. findIssueId + createIssue run in the
        // same transaction as doLink, so this is single-writer in the common case.
        return command(requester).createIssue(owner, repo, number, metadata);
    }

    /**
     * Verifies the requester owns the component (by manifest id) and that it is in WIP — the only state
     * in which links may change.
     */
    private void assertOwnerInWip(ScoreUser requester, CcType ccType, ManifestId manifestId) {
        ComponentOwnerState ownerState = getComponentOwnerState(query(requester), ccType, manifestId);
        if (ownerState == null) {
            throw new IllegalArgumentException("Component not found: " + ccType + " " + manifestId);
        }
        assertOwnerInWip(requester, ownerState);
    }

    /** Verifies the component is in WIP and owned by the requester. */
    private void assertOwnerInWip(ScoreUser requester, ComponentOwnerState ownerState) {
        if (!"WIP".equals(ownerState.state())) {
            throw new IllegalStateException("Issues can be linked only while the component is in WIP.");
        }
        if (requester.userId() == null || !requester.userId().equals(ownerState.ownerUserId())) {
            throw new IllegalStateException("Only the component owner can link or unlink issues.");
        }
    }

    // ----- CcType dispatch to the per-type repository methods -----
    //
    // The HTTP API is generic (a ccType string + raw numeric id), but the repository boundary is fully
    // typed; these helpers are the single place that maps a CcType to its typed manifest / link id.

    private List<GitHubIssueLinkRecord> getLinkedIssues(GitHubIssueLinkQueryRepository query,
                                                        CcType ccType, BigInteger id) {
        return switch (ccType) {
            case ACC -> query.getLinkedIssues(new AccManifestId(id));
            case ASCCP -> query.getLinkedIssues(new AsccpManifestId(id));
            case BCCP -> query.getLinkedIssues(new BccpManifestId(id));
            case DT -> query.getLinkedIssues(new DtManifestId(id));
            case CODE_LIST -> query.getLinkedIssues(new CodeListManifestId(id));
            case AGENCY_ID_LIST -> query.getLinkedIssues(new AgencyIdListManifestId(id));
            default -> throw unsupported(ccType);
        };
    }

    private boolean isLinked(GitHubIssueLinkQueryRepository query, CcType ccType,
                             ManifestId manifestId, GitHubIssueId issueId) {
        BigInteger id = manifestId.value();
        return switch (ccType) {
            case ACC -> query.isLinked(new AccManifestId(id), issueId);
            case ASCCP -> query.isLinked(new AsccpManifestId(id), issueId);
            case BCCP -> query.isLinked(new BccpManifestId(id), issueId);
            case DT -> query.isLinked(new DtManifestId(id), issueId);
            case CODE_LIST -> query.isLinked(new CodeListManifestId(id), issueId);
            case AGENCY_ID_LIST -> query.isLinked(new AgencyIdListManifestId(id), issueId);
            default -> throw unsupported(ccType);
        };
    }

    private ComponentOwnerState getComponentOwnerState(GitHubIssueLinkQueryRepository query,
                                                       CcType ccType, ManifestId manifestId) {
        BigInteger id = manifestId.value();
        return switch (ccType) {
            case ACC -> query.getComponentOwnerState(new AccManifestId(id));
            case ASCCP -> query.getComponentOwnerState(new AsccpManifestId(id));
            case BCCP -> query.getComponentOwnerState(new BccpManifestId(id));
            case DT -> query.getComponentOwnerState(new DtManifestId(id));
            case CODE_LIST -> query.getComponentOwnerState(new CodeListManifestId(id));
            case AGENCY_ID_LIST -> query.getComponentOwnerState(new AgencyIdListManifestId(id));
            default -> throw unsupported(ccType);
        };
    }

    private ComponentOwnerState getComponentOwnerStateByLink(GitHubIssueLinkQueryRepository query,
                                                             CcType ccType, BigInteger linkId) {
        return switch (ccType) {
            case ACC -> query.getComponentOwnerStateByLink(new GitHubIssueAccManifestId(linkId));
            case ASCCP -> query.getComponentOwnerStateByLink(new GitHubIssueAsccpManifestId(linkId));
            case BCCP -> query.getComponentOwnerStateByLink(new GitHubIssueBccpManifestId(linkId));
            case DT -> query.getComponentOwnerStateByLink(new GitHubIssueDtManifestId(linkId));
            case CODE_LIST -> query.getComponentOwnerStateByLink(new GitHubIssueCodeListManifestId(linkId));
            case AGENCY_ID_LIST -> query.getComponentOwnerStateByLink(new GitHubIssueAgencyIdListManifestId(linkId));
            default -> throw unsupported(ccType);
        };
    }

    private GitHubIssueLinkRecord getLinkedIssue(GitHubIssueLinkQueryRepository query,
                                                 CcType ccType, BigInteger linkId) {
        return switch (ccType) {
            case ACC -> query.getLinkedIssue(new GitHubIssueAccManifestId(linkId));
            case ASCCP -> query.getLinkedIssue(new GitHubIssueAsccpManifestId(linkId));
            case BCCP -> query.getLinkedIssue(new GitHubIssueBccpManifestId(linkId));
            case DT -> query.getLinkedIssue(new GitHubIssueDtManifestId(linkId));
            case CODE_LIST -> query.getLinkedIssue(new GitHubIssueCodeListManifestId(linkId));
            case AGENCY_ID_LIST -> query.getLinkedIssue(new GitHubIssueAgencyIdListManifestId(linkId));
            default -> throw unsupported(ccType);
        };
    }

    private void createIssueLink(GitHubIssueLinkCommandRepository command, CcType ccType,
                                 ManifestId manifestId, GitHubIssueId issueId) {
        BigInteger id = manifestId.value();
        switch (ccType) {
            case ACC -> command.createIssueLink(new AccManifestId(id), issueId);
            case ASCCP -> command.createIssueLink(new AsccpManifestId(id), issueId);
            case BCCP -> command.createIssueLink(new BccpManifestId(id), issueId);
            case DT -> command.createIssueLink(new DtManifestId(id), issueId);
            case CODE_LIST -> command.createIssueLink(new CodeListManifestId(id), issueId);
            case AGENCY_ID_LIST -> command.createIssueLink(new AgencyIdListManifestId(id), issueId);
            default -> throw unsupported(ccType);
        }
    }

    private boolean deleteIssueLink(GitHubIssueLinkCommandRepository command, CcType ccType, BigInteger linkId) {
        return switch (ccType) {
            case ACC -> command.deleteIssueLink(new GitHubIssueAccManifestId(linkId));
            case ASCCP -> command.deleteIssueLink(new GitHubIssueAsccpManifestId(linkId));
            case BCCP -> command.deleteIssueLink(new GitHubIssueBccpManifestId(linkId));
            case DT -> command.deleteIssueLink(new GitHubIssueDtManifestId(linkId));
            case CODE_LIST -> command.deleteIssueLink(new GitHubIssueCodeListManifestId(linkId));
            case AGENCY_ID_LIST -> command.deleteIssueLink(new GitHubIssueAgencyIdListManifestId(linkId));
            default -> throw unsupported(ccType);
        };
    }

    private static UnsupportedOperationException unsupported(CcType ccType) {
        return new UnsupportedOperationException("Unsupported component type for GitHub issue linking: " + ccType);
    }

    // ----- GitHub issue JSON <-> cached_metadata -----

    /** Builds the curated cached_metadata JSON document from a GitHub issue object. Null if absent. */
    private String buildMetadataJson(JsonNode issue) {
        if (issue == null || issue.isMissingNode() || issue.isNull()) {
            return null;
        }
        ObjectNode m = objectMapper.createObjectNode();
        m.put("title", text(issue, "title"));
        m.put("state", text(issue, "state"));
        m.put("stateReason", text(issue, "state_reason"));
        m.put("htmlUrl", text(issue, "html_url"));
        m.put("nodeId", text(issue, "node_id"));

        JsonNode type = issue.path("type");
        m.put("type", type.isObject() ? text(type, "name") : null);

        JsonNode ms = issue.path("milestone");
        if (ms.isObject()) {
            ObjectNode mn = m.putObject("milestone");
            mn.put("title", text(ms, "title"));
            mn.put("state", text(ms, "state"));
        } else {
            m.putNull("milestone");
        }

        ArrayNode labels = m.putArray("labels");
        for (JsonNode l : issue.path("labels")) {
            ObjectNode ln = labels.addObject();
            ln.put("name", text(l, "name"));
            ln.put("color", text(l, "color"));
            ln.put("description", text(l, "description"));
        }

        ArrayNode assignees = m.putArray("assignees");
        for (JsonNode a : issue.path("assignees")) {
            ObjectNode an = assignees.addObject();
            an.put("login", text(a, "login"));
            an.put("htmlUrl", text(a, "html_url"));
            an.put("avatarUrl", text(a, "avatar_url"));
        }

        try {
            return objectMapper.writeValueAsString(m);
        } catch (Exception e) {
            return null;
        }
    }

    private LinkedIssue toLinkedIssue(GitHubIssueLinkRecord row, String metadataJson) {
        return toLinkedIssue(String.valueOf(row.linkId()), row.repoOwner(), row.repoName(),
                row.issueNumber(), metadataJson);
    }

    private LinkedIssue toLinkedIssue(String linkId, String owner, String repo, int number, String metadataJson) {
        String htmlUrl = "https://github.com/" + owner + "/" + repo + "/issues/" + number;
        String title = null, state = null, type = null;
        Milestone milestone = null;
        List<Label> labels = new ArrayList<>();
        List<Assignee> assignees = new ArrayList<>();

        if (hasText(metadataJson)) {
            try {
                JsonNode m = objectMapper.readTree(metadataJson);
                title = text(m, "title");
                state = text(m, "state");
                type = text(m, "type");
                if (text(m, "htmlUrl") != null) {
                    htmlUrl = text(m, "htmlUrl");
                }
                JsonNode ms = m.path("milestone");
                if (ms.isObject()) {
                    milestone = new Milestone(text(ms, "title"), text(ms, "state"));
                }
                for (JsonNode l : m.path("labels")) {
                    labels.add(new Label(text(l, "name"), text(l, "color"), text(l, "description")));
                }
                for (JsonNode a : m.path("assignees")) {
                    assignees.add(new Assignee(text(a, "login"), text(a, "htmlUrl"), text(a, "avatarUrl")));
                }
            } catch (Exception ignored) {
                // Treat unparseable cache as empty metadata.
            }
        }
        return new LinkedIssue(linkId, owner, repo, number, htmlUrl, title, state, type, milestone, labels, assignees);
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return (v.isMissingNode() || v.isNull()) ? null : v.asText();
    }
}
