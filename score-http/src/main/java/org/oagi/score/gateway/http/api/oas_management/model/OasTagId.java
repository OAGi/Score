package org.oagi.score.gateway.http.api.oas_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for an OpenAPI tag.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record OasTagId(BigInteger value) implements Id {

    @JsonCreator
    public static OasTagId from(String value) {
        return new OasTagId(new BigInteger(value));
    }

    @JsonCreator
    public static OasTagId from(BigInteger value) {
        return new OasTagId(value);
    }
}
