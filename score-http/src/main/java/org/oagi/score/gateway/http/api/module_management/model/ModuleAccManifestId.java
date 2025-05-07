package org.oagi.score.gateway.http.api.module_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a module acc manifest.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record ModuleAccManifestId(BigInteger value) implements Id {

    @JsonCreator
    public static ModuleAccManifestId from(String value) {
        return new ModuleAccManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static ModuleAccManifestId from(BigInteger value) {
        return new ModuleAccManifestId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
