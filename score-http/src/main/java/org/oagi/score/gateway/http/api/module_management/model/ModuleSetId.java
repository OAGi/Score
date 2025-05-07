package org.oagi.score.gateway.http.api.module_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a module set.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record ModuleSetId(BigInteger value) implements Id {

    @JsonCreator
    public static ModuleSetId from(String value) {
        return new ModuleSetId(new BigInteger(value));
    }

    @JsonCreator
    public static ModuleSetId from(BigInteger value) {
        return new ModuleSetId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
