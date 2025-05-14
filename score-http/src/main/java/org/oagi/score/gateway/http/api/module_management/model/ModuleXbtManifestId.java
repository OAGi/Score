package org.oagi.score.gateway.http.api.module_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a module xbt manifest.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record ModuleXbtManifestId(BigInteger value) implements Id {

    @JsonCreator
    public static ModuleXbtManifestId from(String value) {
        return new ModuleXbtManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static ModuleXbtManifestId from(BigInteger value) {
        return new ModuleXbtManifestId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
