package org.oagi.score.gateway.http.api.cc_management.model.bcc;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.CoreComponentId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a BCC.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record BccId(BigInteger value) implements CoreComponentId {

    @JsonCreator
    public static BccId from(String value) {
        return new BccId(new BigInteger(value));
    }

    @JsonCreator
    public static BccId from(BigInteger value) {
        return new BccId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
