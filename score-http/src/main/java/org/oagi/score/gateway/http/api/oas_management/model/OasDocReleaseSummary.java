package org.oagi.score.gateway.http.api.oas_management.model;

import java.math.BigInteger;

/**
 * A distinct release (id + num) among a document's BIE-backed message bodies.
 *
 * <p>Computed by a single {@code SELECT DISTINCT} query (see
 * {@code BieForOasDocQueryRepository#getDistinctReleases}) so the Error Response "apply to all"
 * ConfirmMessage Branch selector no longer fetches the whole paginated BIE list only to derive its
 * releases client-side. Bodyless operations carry no BIE and therefore no release, so they are excluded
 * by the query's inner joins.
 */
public class OasDocReleaseSummary {

    private final BigInteger releaseId;
    private final String releaseNum;

    public OasDocReleaseSummary(BigInteger releaseId, String releaseNum) {
        this.releaseId = releaseId;
        this.releaseNum = releaseNum;
    }

    public BigInteger getReleaseId() {
        return releaseId;
    }

    public String getReleaseNum() {
        return releaseNum;
    }
}
