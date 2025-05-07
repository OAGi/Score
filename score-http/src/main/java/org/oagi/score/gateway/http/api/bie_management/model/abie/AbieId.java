package org.oagi.score.gateway.http.api.bie_management.model.abie;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.api.bie_management.model.BieId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for an ABIE.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record AbieId(BigInteger value) implements BieId {

    @JsonCreator
    public static AbieId from(String value) {
        return new AbieId(new BigInteger(value));
    }

    @JsonCreator
    public static AbieId from(BigInteger value) {
        return new AbieId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }

}
