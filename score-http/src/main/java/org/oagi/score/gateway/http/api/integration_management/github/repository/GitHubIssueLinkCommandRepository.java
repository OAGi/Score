package org.oagi.score.gateway.http.api.integration_management.github.repository;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.integration_management.github.model.GitHubIssueAccManifestId;
import org.oagi.score.gateway.http.api.integration_management.github.model.GitHubIssueAgencyIdListManifestId;
import org.oagi.score.gateway.http.api.integration_management.github.model.GitHubIssueAsccpManifestId;
import org.oagi.score.gateway.http.api.integration_management.github.model.GitHubIssueBccpManifestId;
import org.oagi.score.gateway.http.api.integration_management.github.model.GitHubIssueCodeListManifestId;
import org.oagi.score.gateway.http.api.integration_management.github.model.GitHubIssueDtManifestId;
import org.oagi.score.gateway.http.api.integration_management.github.model.GitHubIssueId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

/**
 * Repository interface for writing GitHub issue links and the cached issue registry (issue #1533).
 * <p>
 * All component types share a single registry ({@code github_issue}); each type has its own link table
 * ({@code github_issue_<type>_manifest}) and its own typed manifest / link id, so the link writes are
 * typed to exactly one component type — no jOOQ types and no {@code CcType} discriminator reach this
 * boundary.
 */
public interface GitHubIssueLinkCommandRepository {

    /**
     * Inserts the GitHub issue registry row and returns its generated id. The synced timestamp is set only
     * when {@code cachedMetadata} is non-null. Callers register an issue at most once per
     * {@code (repo_owner, repo_name, issue_number)} (the service checks {@link GitHubIssueLinkQueryRepository#findIssueId}
     * first within the same transaction); this is a plain insert, so a genuinely concurrent first-time
     * registration of the same coordinates fails on the unique key and rolls the transaction back rather
     * than corrupting state.
     *
     * @param repoOwner      GitHub repository owner/org. Must not be {@code null}.
     * @param repoName       GitHub repository name. Must not be {@code null}.
     * @param issueNumber    GitHub issue number.
     * @param cachedMetadata cached GitHub issue metadata JSON, or {@code null} if not yet available.
     * @return the id of the newly created registry row.
     */
    GitHubIssueId createIssue(String repoOwner, String repoName, int issueNumber, @Nullable String cachedMetadata);

    /**
     * Updates the cached metadata (and synced/last-update timestamps) of a registry row by id.
     *
     * @param issueId        the registry row id. Must not be {@code null}.
     * @param cachedMetadata the new cached metadata JSON.
     * @return {@code true} if exactly one row was updated, {@code false} otherwise.
     */
    boolean updateIssueCache(GitHubIssueId issueId, String cachedMetadata);

    /**
     * Updates the cached metadata (and synced/last-update timestamps) of a registry row identified by
     * its repository coordinates. Used by the webhook path, which has no registry id (existing rows only).
     *
     * @param repoOwner      GitHub repository owner/org.
     * @param repoName       GitHub repository name.
     * @param issueNumber    GitHub issue number.
     * @param cachedMetadata the new cached metadata JSON.
     * @return {@code true} if at least one row was updated, {@code false} otherwise.
     */
    boolean updateIssueCache(String repoOwner, String repoName, int issueNumber, String cachedMetadata);

    // ----- Link a component to a GitHub issue (per type), recording the requester as creator/updater -----
    //
    // A plain insert returning the new link-row id. The service skips this when the link already exists
    // (its isLinked() check runs in the same transaction); a genuinely concurrent first-time insert of the
    // same (manifest_id, github_issue_id) fails on the unique key and rolls back rather than duplicating.

    GitHubIssueAccManifestId createIssueLink(AccManifestId manifestId, GitHubIssueId issueId);

    GitHubIssueAsccpManifestId createIssueLink(AsccpManifestId manifestId, GitHubIssueId issueId);

    GitHubIssueBccpManifestId createIssueLink(BccpManifestId manifestId, GitHubIssueId issueId);

    GitHubIssueDtManifestId createIssueLink(DtManifestId manifestId, GitHubIssueId issueId);

    GitHubIssueCodeListManifestId createIssueLink(CodeListManifestId manifestId, GitHubIssueId issueId);

    GitHubIssueAgencyIdListManifestId createIssueLink(AgencyIdListManifestId manifestId, GitHubIssueId issueId);

    // ----- Delete a GitHub issue link row (per type, by link id) -----

    boolean deleteIssueLink(GitHubIssueAccManifestId linkId);

    boolean deleteIssueLink(GitHubIssueAsccpManifestId linkId);

    boolean deleteIssueLink(GitHubIssueBccpManifestId linkId);

    boolean deleteIssueLink(GitHubIssueDtManifestId linkId);

    boolean deleteIssueLink(GitHubIssueCodeListManifestId linkId);

    boolean deleteIssueLink(GitHubIssueAgencyIdListManifestId linkId);

    /**
     * Deletes the {@code github_issue} registry row for {@code issueId}, but only if no link table still
     * references it — an atomic {@code NOT EXISTS} guard across all per-type link tables, so a
     * concurrently-added link is respected and the FK is never violated. Call after removing a link to
     * garbage-collect an issue that is no longer linked to any component. Issue #1533.
     *
     * @param issueId the GitHub issue registry id. Must not be {@code null}.
     * @return {@code true} if the registry row was deleted, {@code false} if it is still referenced (or absent).
     */
    boolean deleteIssueIfOrphaned(GitHubIssueId issueId);

    /**
     * Carries GitHub issue links over from the library's {@code Working} release into a target (Draft)
     * release. The Working release is resolved internally from the target release's library (each library
     * has exactly one), so a mismatched release pair can no longer be passed. For each supported component
     * type, copies every link row attached to a Working-release manifest onto the target release's
     * corresponding manifest, matched via the target manifest's {@code NEXT_*_MANIFEST_ID} pointer
     * (mirroring how component tags are carried over). The cached {@code github_issue} registry is shared,
     * so only the link rows are duplicated. No-op if the library has no Working release. Issue #1533.
     *
     * @param targetReleaseId the Draft release receiving the links.
     */
    void copyLinksFromWorking(ReleaseId targetReleaseId);

    /**
     * Deletes every GitHub issue link attached to any component manifest that belongs to the given
     * release. Used when a release is fully torn down (Draft revert, library discard) to remove all of
     * that release's links. Issue #1533.
     *
     * @param releaseId the release whose component links are removed.
     */
    void deleteLinksByRelease(ReleaseId releaseId);

    /**
     * Deletes — from the library's {@code Working} release — the GitHub issue links of the components
     * <em>included in</em> the given (published) release: the Working manifests that the release's manifests
     * were copied from, matched via the published manifest's {@code NEXT_<type>_MANIFEST_ID} pointer (the
     * inverse of {@link #copyLinksFromWorking}). The Working release is resolved internally from the given
     * release's library. Links on Working components that are NOT part of this release are left intact, and
     * the release's own copied links are kept, so the carried issues live only in the published release going
     * forward. Contrast {@link #deleteLinksByRelease}, which removes a release's own links wholesale. Used on
     * publish. No-op if the library has no Working release. Issue #1533.
     *
     * @param releaseId the published release whose included components' Working links are removed.
     */
    void deleteWorkingLinksIncludedInRelease(ReleaseId releaseId);
}
