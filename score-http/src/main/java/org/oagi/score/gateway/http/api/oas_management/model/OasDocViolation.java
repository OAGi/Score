package org.oagi.score.gateway.http.api.oas_management.model;

import java.math.BigInteger;

/**
 * A rule that a change set breaks, with where it was broken so that the client can show it there.
 *
 * @param scope          which part of the change set it is in
 * @param oasOperationId the operation it is about, when it is about one
 * @param messageBody    {@code Request} or {@code Response} of the row it is about, when it is about one
 * @param field          the field of the row or of the entry, when it is about one
 * @param message        what is wrong
 */
public record OasDocViolation(Scope scope, BigInteger oasOperationId, String messageBody, String field, String message) {

    public enum Scope {
        /** The fields of the document, its Security Schemes or its Security Requirements. */
        DOCUMENT,
        /** A row of the table. */
        OPERATION,
        /** A request parameter or a response header. */
        HEADER
    }

    public static OasDocViolation of(Scope scope, String message) {
        return new OasDocViolation(scope, null, null, null, message);
    }

    public static OasDocViolation ofOperation(BigInteger oasOperationId, String field, String message) {
        return new OasDocViolation(Scope.OPERATION, oasOperationId, null, field, message);
    }
}
