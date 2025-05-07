package org.oagi.score.gateway.http.api.bie_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a BIE package.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record BiePackageId(BigInteger value) implements Id {

    @JsonCreator
    public static BiePackageId from(String value) {
        return new BiePackageId(new BigInteger(value));
    }

    @JsonCreator
    public static BiePackageId from(BigInteger value) {
        return new BiePackageId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }

}
