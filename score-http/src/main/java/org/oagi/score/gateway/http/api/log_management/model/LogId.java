package org.oagi.score.gateway.http.api.log_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.CoreComponentId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a log.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record LogId(BigInteger value) implements CoreComponentId {

    @JsonCreator
    public static LogId from(String value) {
        return new LogId(new BigInteger(value));
    }

    @JsonCreator
    public static LogId from(BigInteger value) {
        return new LogId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
