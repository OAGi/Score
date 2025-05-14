package org.oagi.score.gateway.http.api.context_management.context_scheme.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a context scheme value.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record ContextSchemeValueId(BigInteger value) implements Id {

    @JsonCreator
    public static ContextSchemeValueId from(String value) {
        return new ContextSchemeValueId(new BigInteger(value));
    }

    @JsonCreator
    public static ContextSchemeValueId from(BigInteger value) {
        return new ContextSchemeValueId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
