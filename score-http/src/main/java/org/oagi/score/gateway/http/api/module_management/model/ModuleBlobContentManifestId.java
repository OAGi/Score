package org.oagi.score.gateway.http.api.module_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a module blob content manifest.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record ModuleBlobContentManifestId(BigInteger value) implements Id {

    @JsonCreator
    public static ModuleBlobContentManifestId from(String value) {
        return new ModuleBlobContentManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static ModuleBlobContentManifestId from(BigInteger value) {
        return new ModuleBlobContentManifestId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
