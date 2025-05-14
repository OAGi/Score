package org.oagi.score.gateway.http.api.business_term_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a business term.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record AsbieBusinessTermId(BigInteger value) implements Id {

    @JsonCreator
    public static AsbieBusinessTermId from(String value) {
        return new AsbieBusinessTermId(new BigInteger(value));
    }

    @JsonCreator
    public static AsbieBusinessTermId from(BigInteger value) {
        return new AsbieBusinessTermId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }

}
