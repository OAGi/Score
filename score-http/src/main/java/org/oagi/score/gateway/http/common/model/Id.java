package org.oagi.score.gateway.http.common.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.math.BigInteger;

/**
 * A marker interface for all identifier types.
 * Provides a method to retrieve the underlying identifier value.
 */
public interface Id {

    /**
     * Retrieves the identifier value.
     * The value is serialized as a JSON property.
     *
     * @return the identifier as a {@link BigInteger}.
     */
    @JsonValue
    BigInteger value();

}
