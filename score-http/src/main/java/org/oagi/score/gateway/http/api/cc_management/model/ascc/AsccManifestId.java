package org.oagi.score.gateway.http.api.cc_management.model.ascc;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for an ASCC manifest.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record AsccManifestId(BigInteger value) implements ManifestId {

    @JsonCreator
    public static AsccManifestId from(String value) {
        return new AsccManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static AsccManifestId from(BigInteger value) {
        return new AsccManifestId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
