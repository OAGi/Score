package org.oagi.score.gateway.http.api.context_management.context_scheme.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a context scheme.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record ContextSchemeId(BigInteger value) implements Id {

    @JsonCreator
    public static ContextSchemeId from(String value) {
        return new ContextSchemeId(new BigInteger(value));
    }

    @JsonCreator
    public static ContextSchemeId from(BigInteger value) {
        return new ContextSchemeId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
