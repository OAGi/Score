package org.oagi.score.gateway.http.api.integration_management.github.model;

import org.oagi.score.gateway.http.common.model.Id;

/**
 * A single GitHub issue link row joined with its referenced {@code github_issue} registry entry,
 * as read from the database. The cached metadata is the raw JSON document stored in
 * {@code github_issue.cached_metadata}; parsing it into a presentation object is a service concern.
 *
 * @param linkId        the primary key of the {@code github_issue_<type>_manifest} link row, typed per
 *                      component (e.g. {@link GitHubIssueAccManifestId}); held as the common {@link Id}
 *                      since this read DTO is shared across all component types
 * @param issueId       the referenced {@code github_issue} registry row
 * @param repoOwner     GitHub repository owner/org
 * @param repoName      GitHub repository name
 * @param issueNumber   GitHub issue number within the repository
 * @param cachedMetadata cached GitHub issue metadata JSON, or {@code null} if never synced
 */
public record GitHubIssueLinkRecord(Id linkId,
                                    GitHubIssueId issueId,
                                    String repoOwner,
                                    String repoName,
                                    int issueNumber,
                                    String cachedMetadata) {
}
