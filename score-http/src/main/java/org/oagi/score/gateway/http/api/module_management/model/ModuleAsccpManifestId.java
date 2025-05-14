package org.oagi.score.gateway.http.api.module_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a module asccp manifest.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record ModuleAsccpManifestId(BigInteger value) implements Id {

    @JsonCreator
    public static ModuleAsccpManifestId from(String value) {
        return new ModuleAsccpManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static ModuleAsccpManifestId from(BigInteger value) {
        return new ModuleAsccpManifestId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
