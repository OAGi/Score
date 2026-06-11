package org.oagi.score.gateway.http.api.integration_management.github.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a single ACC GitHub issue link row
 * ({@code github_issue_acc_manifest} primary key). Implements the {@link Id} interface to provide a
 * standardized way to retrieve the identifier value.
 */
public record GitHubIssueAccManifestId(BigInteger value) implements Id {

    @JsonCreator
    public static GitHubIssueAccManifestId from(String value) {
        return new GitHubIssueAccManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static GitHubIssueAccManifestId from(BigInteger value) {
        return new GitHubIssueAccManifestId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
