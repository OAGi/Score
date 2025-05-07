package org.oagi.score.gateway.http.api.oas_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for an OpenAPI message body.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record OasMessageBodyId(BigInteger value) implements Id {

    @JsonCreator
    public static OasMessageBodyId from(String value) {
        return new OasMessageBodyId(new BigInteger(value));
    }

    @JsonCreator
    public static OasMessageBodyId from(BigInteger value) {
        return new OasMessageBodyId(value);
    }
}
