package org.oagi.score.gateway.http.api.oas_management.model;

import java.math.BigInteger;
import java.util.List;

/**
 * What the server says of a change set: whether it can be saved, what it breaks if not, and what the document would be if
 * it were saved (the request parameters and response headers, those that the system registered for the operations
 * included, and the Operation ID, Resource Name and Message Body that the server made of the verb or the array indicator
 * that the user changed), which is what the client shows.
 */
public record OasDocChangeResult(boolean valid, List<OasDocViolation> violations, List<OasDocHttpHeader> httpHeaders,
                                 List<DerivedOperation> operations) {

    /**
     * What the server made of an operation that the user changed the verb or the array indicator of. The row is the one of
     * {@code oasOperationId} that had {@code messageBody} when it was sent; {@code messageBody} here is the one it has now.
     */
    public record DerivedOperation(BigInteger oasOperationId, String sentMessageBody, String operationId,
                                   String resourceName, String messageBody) {
    }

    public static OasDocChangeResult valid(List<OasDocHttpHeader> httpHeaders, List<DerivedOperation> operations) {
        return new OasDocChangeResult(true, List.of(), httpHeaders, operations);
    }

    /** What the server made of the names is answered when the change cannot be saved too: it is what the user is to see. */
    public static OasDocChangeResult invalid(List<OasDocViolation> violations, List<DerivedOperation> operations) {
        return new OasDocChangeResult(false, violations, List.of(), operations);
    }
}
