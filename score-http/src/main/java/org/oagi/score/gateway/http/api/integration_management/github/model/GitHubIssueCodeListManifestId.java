package org.oagi.score.gateway.http.api.integration_management.github.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a single code list GitHub issue link row
 * ({@code github_issue_code_list_manifest} primary key). Implements the {@link Id} interface to provide
 * a standardized way to retrieve the identifier value.
 */
public record GitHubIssueCodeListManifestId(BigInteger value) implements Id {

    @JsonCreator
    public static GitHubIssueCodeListManifestId from(String value) {
        return new GitHubIssueCodeListManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static GitHubIssueCodeListManifestId from(BigInteger value) {
        return new GitHubIssueCodeListManifestId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
