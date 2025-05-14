package org.oagi.score.gateway.http.api.oas_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for an OpenAPI response.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record OasResponseId(BigInteger value) implements Id {

    @JsonCreator
    public static OasResponseId from(String value) {
        return new OasResponseId(new BigInteger(value));
    }

    @JsonCreator
    public static OasResponseId from(BigInteger value) {
        return new OasResponseId(value);
    }
}
