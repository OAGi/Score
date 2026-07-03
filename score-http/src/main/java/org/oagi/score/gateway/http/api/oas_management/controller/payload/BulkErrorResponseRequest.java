package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;

import java.math.BigInteger;

/**
 * Issue #1347: wire payload for the document-level "apply Error Response Body Type to all operations"
 * endpoint. NONE / PROBLEM_DETAILS apply to every operation (releaseId/confirm ignored); CONFIRM_MESSAGE
 * applies to the chosen {@code releaseId}'s operations plus bodyless operations, referencing
 * {@code confirmMessageTopLevelAsbiepId}.
 */
@Data
public class BulkErrorResponseRequest {

    private OasDocId oasDocId;
    private String errorResponseBodyType;
    private BigInteger confirmMessageTopLevelAsbiepId;
    private BigInteger releaseId;
}
