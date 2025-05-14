package org.oagi.score.gateway.http.api.context_management.business_context.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a business context.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record BusinessContextId(BigInteger value) implements Id {

    @JsonCreator
    public static BusinessContextId from(String value) {
        return new BusinessContextId(new BigInteger(value));
    }

    @JsonCreator
    public static BusinessContextId from(BigInteger value) {
        return new BusinessContextId(value);
    }

    @JsonValue
    public BigInteger value() {
        return value;
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
