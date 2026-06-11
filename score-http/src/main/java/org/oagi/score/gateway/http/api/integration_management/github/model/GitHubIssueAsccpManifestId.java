package org.oagi.score.gateway.http.api.integration_management.github.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a single ASCCP GitHub issue link row
 * ({@code github_issue_asccp_manifest} primary key). Implements the {@link Id} interface to provide a
 * standardized way to retrieve the identifier value.
 */
public record GitHubIssueAsccpManifestId(BigInteger value) implements Id {

    @JsonCreator
    public static GitHubIssueAsccpManifestId from(String value) {
        return new GitHubIssueAsccpManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static GitHubIssueAsccpManifestId from(BigInteger value) {
        return new GitHubIssueAsccpManifestId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
