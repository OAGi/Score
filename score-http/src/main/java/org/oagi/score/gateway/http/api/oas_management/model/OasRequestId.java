package org.oagi.score.gateway.http.api.oas_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for an OpenAPI request.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record OasRequestId(BigInteger value) implements Id {

    @JsonCreator
    public static OasRequestId from(String value) {
        return new OasRequestId(new BigInteger(value));
    }

    @JsonCreator
    public static OasRequestId from(BigInteger value) {
        return new OasRequestId(value);
    }
}
