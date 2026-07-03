package org.oagi.score.gateway.http.api.oas_management.model;

import java.math.BigInteger;

/**
 * Issue #1347: a lightweight per-body row used to compute the document-level Error Response Body Type
 * (bulk targeting + new-operation inheritance) WITHOUT the heavyweight per-row security / ConfirmMessage-
 * DEN subqueries that the full {@code getBieForOasDoc} listing carries.
 *
 * <p>One operation may yield up to two of these (its Request body and its Response body), each with its
 * own {@code releaseId} (null for a bodyless operation).
 */
public class OperationErrorResponseSummary {

    private final BigInteger oasOperationId;
    private final BigInteger releaseId;
    private final String errorResponseBodyType;
    private final BigInteger confirmTopLevelAsbiepId;

    public OperationErrorResponseSummary(BigInteger oasOperationId, BigInteger releaseId,
                                         String errorResponseBodyType, BigInteger confirmTopLevelAsbiepId) {
        this.oasOperationId = oasOperationId;
        this.releaseId = releaseId;
        this.errorResponseBodyType = errorResponseBodyType;
        this.confirmTopLevelAsbiepId = confirmTopLevelAsbiepId;
    }

    public BigInteger getOasOperationId() {
        return oasOperationId;
    }

    public BigInteger getReleaseId() {
        return releaseId;
    }

    public String getErrorResponseBodyType() {
        return errorResponseBodyType;
    }

    public BigInteger getConfirmTopLevelAsbiepId() {
        return confirmTopLevelAsbiepId;
    }
}
