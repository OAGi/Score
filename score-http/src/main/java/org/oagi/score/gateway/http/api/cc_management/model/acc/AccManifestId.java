package org.oagi.score.gateway.http.api.cc_management.model.acc;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;

/**
 * Represents a unique identifier for an ACC manifest.
 * Implements the {@link Id} interface to provide a standardized way
 * to retrieve the identifier value.
 */
public record AccManifestId(BigInteger value) implements ManifestId, Comparable<AccManifestId> {

    @JsonCreator
    public static AccManifestId from(String value) {
        return new AccManifestId(new BigInteger(value));
    }

    @JsonCreator
    public static AccManifestId from(BigInteger value) {
        return new AccManifestId(value);
    }

    public int compareTo(AccManifestId id) {
        if (id == null || id.value() == null) {
            return (this.value == null) ? 0 : 1;
        }
        if (this.value == null) {
            return -1;
        }
        return value.compareTo(id.value());
    }

    public String toString() {
        return (value() != null) ? value().toString() : null;
    }
}
