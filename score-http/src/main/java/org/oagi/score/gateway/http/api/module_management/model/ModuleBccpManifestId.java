package org.oagi.score.gateway.http.api.module_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a module bccp manifest.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record ModuleBccpManifestId(BigInteger value) implements Id {

    @JsonCreator
    public static ModuleBccpManifestId from(String value) {
        return new ModuleBccpManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static ModuleBccpManifestId from(BigInteger value) {
        return new ModuleBccpManifestId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
