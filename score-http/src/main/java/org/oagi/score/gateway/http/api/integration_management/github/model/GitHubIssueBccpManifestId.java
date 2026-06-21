package org.oagi.score.gateway.http.api.integration_management.github.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a single BCCP GitHub issue link row
 * ({@code github_issue_bccp_manifest} primary key). Implements the {@link Id} interface to provide a
 * standardized way to retrieve the identifier value.
 */
public record GitHubIssueBccpManifestId(BigInteger value) implements Id {

    @JsonCreator
    public static GitHubIssueBccpManifestId from(String value) {
        return new GitHubIssueBccpManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static GitHubIssueBccpManifestId from(BigInteger value) {
        return new GitHubIssueBccpManifestId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
