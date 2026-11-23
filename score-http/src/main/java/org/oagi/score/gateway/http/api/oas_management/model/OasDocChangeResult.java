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
                                 List<DerivedOperation> operations, List<AddedOperation> added) {

    /** What the server made of an operation that was added in the page: the names, by the key that the page gave the row. */
    public record AddedOperation(String clientKey, String operationId, String resourceName, String messageBody) {
    }

    public OasDocChangeResult(boolean valid, List<OasDocViolation> violations, List<OasDocHttpHeader> httpHeaders,
                              List<DerivedOperation> operations) {
        this(valid, violations, httpHeaders, operations, List.of());
    }

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

    public static OasDocChangeResult valid(List<OasDocHttpHeader> httpHeaders, List<DerivedOperation> operations, List<AddedOperation> added) {
        return new OasDocChangeResult(true, List.of(), httpHeaders, operations, added);
    }

    /**
     * What the server made of the names is answered when the change cannot be saved too: it is what the user is to see. The entries
     * are those that the document would have, when the server got as far as to make them (the registered ones follow the operations
     * whether or not the change can be saved), and {@code null} when it did not: the page then leaves its own as they are.
     */
    public static OasDocChangeResult invalid(List<OasDocViolation> violations, List<DerivedOperation> operations) {
        return new OasDocChangeResult(false, violations, null, operations);
    }

    public static OasDocChangeResult invalid(List<OasDocViolation> violations, List<DerivedOperation> operations, List<AddedOperation> added) {
        return new OasDocChangeResult(false, violations, null, operations, added);
    }

    public static OasDocChangeResult invalid(List<OasDocViolation> violations, List<DerivedOperation> operations, List<AddedOperation> added,
                                             List<OasDocHttpHeader> httpHeaders) {
        return new OasDocChangeResult(false, violations, httpHeaders, operations, added);
    }
}
