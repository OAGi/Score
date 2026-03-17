package org.oagi.score.gateway.http.api.oas_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for an OpenAPI resource tag.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record OasResourceTagId(BigInteger value) implements Id {

    @JsonCreator
    public static OasResourceTagId from(String value) {
        return new OasResourceTagId(new BigInteger(value));
    }

    @JsonCreator
    public static OasResourceTagId from(BigInteger value) {
        return new OasResourceTagId(value);
    }
}
