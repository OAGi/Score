package org.oagi.score.gateway.http.api.bie_management.model.asbiep;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.api.bie_management.model.BieId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for an ASBIEP.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record AsbiepId(BigInteger value) implements BieId {

    @JsonCreator
    public static AsbiepId from(String value) {
        return new AsbiepId(new BigInteger(value));
    }

    @JsonCreator
    public static AsbiepId from(BigInteger value) {
        return new AsbiepId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }

}