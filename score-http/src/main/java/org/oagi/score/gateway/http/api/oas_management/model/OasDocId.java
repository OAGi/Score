package org.oagi.score.gateway.http.api.oas_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for an OpenAPI document.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record OasDocId(BigInteger value) implements Id {

    @JsonCreator
    public static OasDocId from(String value) {
        return new OasDocId(new BigInteger(value));
    }

    @JsonCreator
    public static OasDocId from(BigInteger value) {
        return new OasDocId(value);
    }
}
