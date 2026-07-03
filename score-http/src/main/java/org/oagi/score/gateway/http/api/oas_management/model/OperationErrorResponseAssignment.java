package org.oagi.score.gateway.http.api.oas_management.model;

import java.math.BigInteger;

/**
 * Issue #1347: a resolved per-operation error-response assignment used by the document-level
 * "apply to all" bulk update (and by new-operation inheritance).
 *
 * <p>The service decides WHICH operations to touch — every operation for NONE/PROBLEM_DETAILS, or the
 * chosen release's operations plus bodyless operations for CONFIRM_MESSAGE — and hands the command
 * repository a flat list of these. The repository just persists each one, so the release-matching
 * business logic stays out of the jOOQ layer.
 */
public class OperationErrorResponseAssignment {

    private final BigInteger oasOperationId;
    private final String errorResponseBodyType;
    private final BigInteger confirmTopLevelAsbiepId;

    public OperationErrorResponseAssignment(BigInteger oasOperationId, String errorResponseBodyType,
                                            BigInteger confirmTopLevelAsbiepId) {
        this.oasOperationId = oasOperationId;
        this.errorResponseBodyType = errorResponseBodyType;
        this.confirmTopLevelAsbiepId = confirmTopLevelAsbiepId;
    }

    public BigInteger getOasOperationId() {
        return oasOperationId;
    }

    public String getErrorResponseBodyType() {
        return errorResponseBodyType;
    }

    public BigInteger getConfirmTopLevelAsbiepId() {
        return confirmTopLevelAsbiepId;
    }
}
