package org.oagi.score.gateway.http.api.module_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a module set release.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record ModuleSetReleaseId(BigInteger value) implements Id {

    @JsonCreator
    public static ModuleSetReleaseId from(String value) {
        return new ModuleSetReleaseId(new BigInteger(value));
    }

    @JsonCreator
    public static ModuleSetReleaseId from(BigInteger value) {
        return new ModuleSetReleaseId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
