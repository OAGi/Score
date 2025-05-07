package org.oagi.score.gateway.http.api.context_management.business_context.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a business context value.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record BusinessContextValueId(BigInteger value) implements Id {

    @JsonCreator
    public static BusinessContextValueId from(String value) {
        return new BusinessContextValueId(new BigInteger(value));
    }

    @JsonCreator
    public static BusinessContextValueId from(BigInteger value) {
        return new BusinessContextValueId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
