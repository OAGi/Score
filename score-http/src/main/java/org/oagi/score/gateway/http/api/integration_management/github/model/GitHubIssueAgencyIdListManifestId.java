package org.oagi.score.gateway.http.api.integration_management.github.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a single agency ID list GitHub issue link row
 * ({@code github_issue_agency_id_list_manifest} primary key). Implements the {@link Id} interface to
 * provide a standardized way to retrieve the identifier value.
 */
public record GitHubIssueAgencyIdListManifestId(BigInteger value) implements Id {

    @JsonCreator
    public static GitHubIssueAgencyIdListManifestId from(String value) {
        return new GitHubIssueAgencyIdListManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static GitHubIssueAgencyIdListManifestId from(BigInteger value) {
        return new GitHubIssueAgencyIdListManifestId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
