package org.oagi.score.gateway.http.api.module_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a module dt manifest.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record ModuleDtManifestId(BigInteger value) implements Id {

    @JsonCreator
    public static ModuleDtManifestId from(String value) {
        return new ModuleDtManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static ModuleDtManifestId from(BigInteger value) {
        return new ModuleDtManifestId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
