package org.oagi.score.gateway.http.api.business_term_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a business term.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record BusinessTermId(BigInteger value) implements Id {

    @JsonCreator
    public static BusinessTermId from(String value) {
        return new BusinessTermId(new BigInteger(value));
    }

    @JsonCreator
    public static BusinessTermId from(BigInteger value) {
        return new BusinessTermId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }

}
