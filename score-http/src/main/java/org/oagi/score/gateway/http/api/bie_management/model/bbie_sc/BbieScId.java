package org.oagi.score.gateway.http.api.bie_management.model.bbie_sc;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.api.bie_management.model.BieId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a BBIE SC.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record BbieScId(BigInteger value) implements BieId {

    @JsonCreator
    public static BbieScId from(String value) {
        return new BbieScId(new BigInteger(value));
    }

    @JsonCreator
    public static BbieScId from(BigInteger value) {
        return new BbieScId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }

}