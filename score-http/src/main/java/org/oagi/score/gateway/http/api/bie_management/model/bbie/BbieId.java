package org.oagi.score.gateway.http.api.bie_management.model.bbie;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.api.bie_management.model.BieId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a BBIE.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record BbieId(BigInteger value) implements BieId {

    @JsonCreator
    public static BbieId from(String value) {
        return new BbieId(new BigInteger(value));
    }

    @JsonCreator
    public static BbieId from(BigInteger value) {
        return new BbieId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }

}