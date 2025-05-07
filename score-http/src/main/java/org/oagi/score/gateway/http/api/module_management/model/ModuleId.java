package org.oagi.score.gateway.http.api.module_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a module.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record ModuleId(BigInteger value) implements Id {

    @JsonCreator
    public static ModuleId from(String value) {
        return new ModuleId(new BigInteger(value));
    }

    @JsonCreator
    public static ModuleId from(BigInteger value) {
        return new ModuleId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
