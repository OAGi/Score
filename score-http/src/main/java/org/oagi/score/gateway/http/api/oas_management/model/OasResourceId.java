package org.oagi.score.gateway.http.api.oas_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for an OpenAPI resource.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record OasResourceId(BigInteger value) implements Id {

    @JsonCreator
    public static OasResourceId from(String value) {
        return new OasResourceId(new BigInteger(value));
    }

    @JsonCreator
    public static OasResourceId from(BigInteger value) {
        return new OasResourceId(value);
    }
}
