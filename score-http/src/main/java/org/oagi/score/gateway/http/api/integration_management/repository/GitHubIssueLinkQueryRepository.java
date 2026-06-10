package org.oagi.score.gateway.http.api.integration_management.repository;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.integration_management.model.ComponentOwnerState;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueAccManifestId;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueAgencyIdListManifestId;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueAsccpManifestId;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueBccpManifestId;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueCodeListManifestId;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueDtManifestId;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueId;
import org.oagi.score.gateway.http.api.integration_management.model.GitHubIssueLinkRecord;

import java.util.List;

/**
 * Repository interface for reading GitHub issue links and the cached issue registry (issue #1533).
 * <p>
 * All component types share a single registry ({@code github_issue}); each type has its own link table
 * ({@code github_issue_<type>_manifest}) and its own typed manifest / link id, so every method is typed
 * to exactly one component type — no jOOQ types and no {@code CcType} discriminator reach this boundary.
 */
public interface GitHubIssueLinkQueryRepository {

    // ----- Linked issues for a component (by manifest id) -----

    List<GitHubIssueLinkRecord> getLinkedIssues(AccManifestId manifestId);

    List<GitHubIssueLinkRecord> getLinkedIssues(AsccpManifestId manifestId);

    List<GitHubIssueLinkRecord> getLinkedIssues(BccpManifestId manifestId);

    List<GitHubIssueLinkRecord> getLinkedIssues(DtManifestId manifestId);

    List<GitHubIssueLinkRecord> getLinkedIssues(CodeListManifestId manifestId);

    List<GitHubIssueLinkRecord> getLinkedIssues(AgencyIdListManifestId manifestId);

    /**
     * Finds the registry id of a GitHub issue by its repository coordinates.
     *
     * @param repoOwner   GitHub repository owner/org.
     * @param repoName    GitHub repository name.
     * @param issueNumber GitHub issue number.
     * @return the {@link GitHubIssueId}, or {@code null} if not yet registered.
     */
    GitHubIssueId findIssueId(String repoOwner, String repoName, int issueNumber);

    // ----- Existing-link check (by manifest id + issue id) -----

    boolean isLinked(AccManifestId manifestId, GitHubIssueId issueId);

    boolean isLinked(AsccpManifestId manifestId, GitHubIssueId issueId);

    boolean isLinked(BccpManifestId manifestId, GitHubIssueId issueId);

    boolean isLinked(DtManifestId manifestId, GitHubIssueId issueId);

    boolean isLinked(CodeListManifestId manifestId, GitHubIssueId issueId);

    boolean isLinked(AgencyIdListManifestId manifestId, GitHubIssueId issueId);

    // ----- Owner/state of a component (by manifest id) -----

    ComponentOwnerState getComponentOwnerState(AccManifestId manifestId);

    ComponentOwnerState getComponentOwnerState(AsccpManifestId manifestId);

    ComponentOwnerState getComponentOwnerState(BccpManifestId manifestId);

    ComponentOwnerState getComponentOwnerState(DtManifestId manifestId);

    ComponentOwnerState getComponentOwnerState(CodeListManifestId manifestId);

    ComponentOwnerState getComponentOwnerState(AgencyIdListManifestId manifestId);

    // ----- Owner/state of the component a link points to (by link id) -----

    ComponentOwnerState getComponentOwnerStateByLink(GitHubIssueAccManifestId linkId);

    ComponentOwnerState getComponentOwnerStateByLink(GitHubIssueAsccpManifestId linkId);

    ComponentOwnerState getComponentOwnerStateByLink(GitHubIssueBccpManifestId linkId);

    ComponentOwnerState getComponentOwnerStateByLink(GitHubIssueDtManifestId linkId);

    ComponentOwnerState getComponentOwnerStateByLink(GitHubIssueCodeListManifestId linkId);

    ComponentOwnerState getComponentOwnerStateByLink(GitHubIssueAgencyIdListManifestId linkId);

    // ----- Link row joined with its registry entry (by link id) -----
    //
    // Captured before a link is removed so the caller can garbage-collect the github_issue registry row
    // once no link references it and evict its cached ETag. Issue #1533.

    GitHubIssueLinkRecord getLinkedIssue(GitHubIssueAccManifestId linkId);

    GitHubIssueLinkRecord getLinkedIssue(GitHubIssueAsccpManifestId linkId);

    GitHubIssueLinkRecord getLinkedIssue(GitHubIssueBccpManifestId linkId);

    GitHubIssueLinkRecord getLinkedIssue(GitHubIssueDtManifestId linkId);

    GitHubIssueLinkRecord getLinkedIssue(GitHubIssueCodeListManifestId linkId);

    GitHubIssueLinkRecord getLinkedIssue(GitHubIssueAgencyIdListManifestId linkId);
}
