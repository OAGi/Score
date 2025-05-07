package org.oagi.score.gateway.http.api.cc_management.model.bcc;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for a BCC manifest.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record BccManifestId(BigInteger value) implements ManifestId {

    @JsonCreator
    public static BccManifestId from(String value) {
        return new BccManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static BccManifestId from(BigInteger value) {
        return new BccManifestId(value);
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
