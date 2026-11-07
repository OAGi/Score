package org.oagi.score.gateway.http.api.oas_management.model;

import java.math.BigInteger;

/**
 * An operation of a document as stored: where it is (a verb on a path), the Operation ID it has, and which bodies it owns.
 */
public record OasOperationEndpoint(BigInteger oasOperationId, String path, String verb, String operationId,
                                   boolean hasRequestBody, boolean hasResponseBody) {
}
