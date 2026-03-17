package org.oagi.score.gateway.http.api.oas_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for an OpenAPI operation.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record OasOperationId(BigInteger value) implements Id {

    @JsonCreator
    public static OasOperationId from(String value) {
        return new OasOperationId(new BigInteger(value));
    }

    @JsonCreator
    public static OasOperationId from(BigInteger value) {
        return new OasOperationId(value);
    }
}
