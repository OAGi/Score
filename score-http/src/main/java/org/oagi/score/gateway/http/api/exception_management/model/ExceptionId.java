package org.oagi.score.gateway.http.api.exception_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a record of an exception.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record ExceptionId(BigInteger value) implements Id {

    @JsonCreator
    public static ExceptionId from(String value) {
        return new ExceptionId(new BigInteger(value));
    }

    @JsonCreator
    public static ExceptionId from(BigInteger value) {
        return new ExceptionId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
