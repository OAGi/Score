package org.oagi.score.gateway.http.api.cc_management.model.ascc;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.CoreComponentId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for an ASCC.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record AsccId(BigInteger value) implements CoreComponentId {

    @JsonCreator
    public static AsccId from(String value) {
        return new AsccId(new BigInteger(value));
    }

    @JsonCreator
    public static AsccId from(BigInteger value) {
        return new AsccId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
