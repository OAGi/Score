package org.oagi.score.gateway.http.api.bie_management.model.asbie;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.api.bie_management.model.BieId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for an ASBIE.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record AsbieId(BigInteger value) implements BieId {

    @JsonCreator
    public static AsbieId from(String value) {
        return new AsbieId(new BigInteger(value));
    }

    @JsonCreator
    public static AsbieId from(BigInteger value) {
        return new AsbieId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }

}