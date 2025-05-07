package org.oagi.score.gateway.http.api.bie_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a top-level ASBIEP.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record TopLevelAsbiepId(BigInteger value) implements Id {

    @JsonCreator
    public static TopLevelAsbiepId from(String value) {
        return new TopLevelAsbiepId(new BigInteger(value));
    }

    @JsonCreator
    public static TopLevelAsbiepId from(BigInteger value) {
        return new TopLevelAsbiepId(value);
    }

    @JsonValue
    public BigInteger value() {
        return value;
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
    
}
