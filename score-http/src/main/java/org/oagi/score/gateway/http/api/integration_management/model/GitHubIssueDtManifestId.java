package org.oagi.score.gateway.http.api.integration_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a single DT GitHub issue link row
 * ({@code github_issue_dt_manifest} primary key). Implements the {@link Id} interface to provide a
 * standardized way to retrieve the identifier value.
 */
public record GitHubIssueDtManifestId(BigInteger value) implements Id {

    @JsonCreator
    public static GitHubIssueDtManifestId from(String value) {
        return new GitHubIssueDtManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static GitHubIssueDtManifestId from(BigInteger value) {
        return new GitHubIssueDtManifestId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
