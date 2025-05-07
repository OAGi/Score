package org.oagi.score.gateway.http.api.bie_management.model.bbiep;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.api.bie_management.model.BieId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a BBIEP.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record BbiepId(BigInteger value) implements BieId {

    @JsonCreator
    public static BbiepId from(String value) {
        return new BbiepId(new BigInteger(value));
    }

    @JsonCreator
    public static BbiepId from(BigInteger value) {
        return new BbiepId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }

}