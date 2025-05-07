package org.oagi.score.gateway.http.api.module_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a module code list manifest.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record ModuleCodeListManifestId(BigInteger value) implements Id {

    @JsonCreator
    public static ModuleCodeListManifestId from(String value) {
        return new ModuleCodeListManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static ModuleCodeListManifestId from(BigInteger value) {
        return new ModuleCodeListManifestId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
