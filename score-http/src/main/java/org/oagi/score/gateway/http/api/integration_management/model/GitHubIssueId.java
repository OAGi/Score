package org.oagi.score.gateway.http.api.integration_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a GitHub issue registry row ({@code github_issue}).
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record GitHubIssueId(BigInteger value) implements Id {

    @JsonCreator
    public static GitHubIssueId from(String value) {
        return new GitHubIssueId(new BigInteger(value));
    }

    @JsonCreator
    public static GitHubIssueId from(BigInteger value) {
        return new GitHubIssueId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
